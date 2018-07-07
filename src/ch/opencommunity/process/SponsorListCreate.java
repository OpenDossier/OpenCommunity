package ch.opencommunity.process;

import ch.opencommunity.server.OpenCommunityServer;
import ch.opencommunity.base.OrganisationMemberInfo;
import ch.opencommunity.base.TextBlock;
import ch.opencommunity.base.Activity;
import ch.opencommunity.base.BatchActivity;
import ch.opencommunity.advertising.MemberAdAdministration;
import ch.opencommunity.view.OrganisationMemberList;
import ch.opencommunity.advertising.MemberAdAdministration;
import ch.opencommunity.advertising.MemberAdCategory;

import org.kubiki.ide.BasicProcess;
import org.kubiki.base.ConfigValue;
import org.kubiki.base.Property;
import org.kubiki.base.BasicClass;
import org.kubiki.base.ObjectCollection;
import org.kubiki.base.ProcessResult;
 
import org.kubiki.application.ApplicationContext;

import org.kubiki.util.DateConverter;

import org.kubiki.xml.XMLParser;
import org.kubiki.xml.XMLElement;

import org.kubiki.pdf.*;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.File;
import java.util.Vector;

import jxl.*;
import jxl.write.*;

public class SponsorListCreate extends BasicProcess{
	
	ObjectCollection results = null;
	MemberAdAdministration maa = null;
	
	PDFTemplateLibrary templib = null;
	
	PDFWriter pdfWriter;
	
	String excluded = "(118, 1957, 608, 2197, 1406, 2123, 416, 2001, 1393, 2036, 440, 2217, 239, 2014, 301, 2039, 406, 1749, 1558, 2020, 786, 2149, 453, 2295, 1974, 1120, 2210, 196, 1550, 1026, 2025, 1978, 809, 2050, 295, 1936, 2130, 1466, 2137, 570, 2171, 656, 2271, 2281, 2579, 600, 2666 )";
	
	public SponsorListCreate(){
		
		addNode(this);
		
		addProperty("CreateList", "Boolean", "true", false, "Liste erstellen");
		
		addProperty("Template", "Integer", "-1", false, "Brief Nr.");
		
		setCurrentNode(this);
		
		results = new ObjectCollection("Results", "*");
		
		
		
	}
	@Override
	public void initProcess(){
		
		excluded = "(0)";
		
		Vector templates = new Vector();
		templates.add(new ConfigValue("-1", "-1", "Alle"));
		templates.add(new ConfigValue("27", "27", "Brief 1"));
		templates.add(new ConfigValue("28", "28", "Brief 2"));
		templates.add(new ConfigValue("29", "29", "Brief 3"));
		templates.add(new ConfigValue("30", "30", "Brief 4"));
		templates.add(new ConfigValue("31", "31", "Brief 5"));
		
		getProperty("Template").setSelection(templates);
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		
		templib = (PDFTemplateLibrary)ocs.getObjectByName("PDFTemplateLibrary", "1");
		pdfWriter = new PDFWriter();
		
		maa = ocs.getMemberAdAdministration();
		
		
		String sql = "SELECT DISTINCT t1.*, t5.FamilyName, t5.FirstName, t5.Sex, t5.DateOfBirth, CASE WHEN t2.ID > 0 THEN 'X' ELSE '' END AS MEMBERID, CASE WHEN t3.ID > 0 THEN 'X' ELSE '' END AS SPONSORID, t1.DateCreated, CASE WHEN t1.DateCreated > '2016-06-05' THEN 'true' ELSE 'false' END AS ISNEWUSER,";
		sql += " CASE WHEN t6.Country='Deutschland' THEN t6.Country WHEN t6.Country='France' THEN t6.Country ELSE '' END AS COUNTRY";
		sql += " FROM OrganisationMember AS t1";
		
		sql += " LEFT JOIN MemberRole AS t2 ON t2.OrganisationMemberID=t1.ID AND t2.Role=2 AND t2.Status=0"; //Mitglieder
		sql += " LEFT JOIN MemberRole AS t3 ON t3.OrganisationMemberID=t1.ID AND t3.Role=3 AND t3.Status=0"; //Gönner
		
		sql += " LEFT JOIN Person AS t4 ON t1.Person=t4.ID";
		sql += " LEFT JOIN Identity AS t5 ON t5.PersonID=t4.ID";
		sql += " LEFT JOIN Address AS t6 ON t6.PersonID=t4.ID";
			
		//sql += " WHERE t1.Status=1";
		sql += " WHERE t1.Status > 0";
		
		sql += " AND t1.ID NOT IN " + excluded;
		
		sql += " ORDER BY COUNTRY DESC, t5.FamilyName, t5.FirstName";
		
		ocs.queryData(sql, results);
		
		ObjectCollection results2 = new ObjectCollection("Results2", "*");
		
		for(BasicClass record : results.getObjects()){
			
			results2.removeObjects();
			
			String omid = record.getString("ID");
			
			//Aktive Nutzer
			
			boolean isActiveUser = false;
			
			sql = "SELECT * FROM MemberAd WHERE OrganisationMemberID=" + omid + " AND ValidFrom >='2015-10-01'";
			
			ocs.queryData(sql, results2);
			
			
			if(results2.getObjects().size() > 0){
				isActiveUser = true;
				
				for(BasicClass o : results2.getObjects()){
					record.addProperty("ACTIVEUSER_" + o.getString("TEMPLATE"), "String", "X");
				}
			}
			
			

			
			results2.removeObjects();
			
			sql = "SELECT t2.Template FROM MemberAdRequest AS t1 JOIN MemberAd AS t2 ON t2.ID=t1.MemberAd WHERE t1.OrganisationMemberID=" + omid + " AND (t1.ValidFrom >='2015-10-01' OR t1.DateCreated >='2015-10-01')";
			
			ocs.queryData(sql, results2);
			
			if(results2.getObjects().size() > 0){
				isActiveUser = true;
				for(BasicClass o : results2.getObjects()){
					if(!record.hasProperty("ACTIVEUSER_" + o.getString("TEMPLATE"))){
						record.addProperty("ACTIVEUSER_" + o.getString("TEMPLATE"), "String", "X");	
					}
				}
			}
			
			record.addProperty("ISACTIVEUSER", "String", "" + isActiveUser);
			
			//-------------------------------------------------------------------------------------------------------------------------------------------
			
			//Spender letzter Versand
			
			results2.removeObjects();
			
			boolean isSponsor = false;
			
			sql = "select * from accountmovement where OrganisationMember=" + omid + " AND date >='2016-06-01' AND date <='2016-09-30' and debitaccount=2";
			
			ocs.queryData(sql, results2);
			
			if(results2.getObjects().size() > 0){
				isSponsor = true;
			}
			
			record.addProperty("ISSPONSOR", "String", "" + isSponsor);
			
			
			//-------------------------------------------------------------------------------------------------------------------------------------------
			
			//Spender 2015
			
			results2.removeObjects();
			
			//boolean isSponsor2 = false;
			
			sql = "select sum(amount) as sum from accountmovement where OrganisationMember=" + omid + " AND date >='2015-01-01' AND date <='2015-12-31'and debitaccount=2 GROUP BY OrganisationMember";
			
			ocs.queryData(sql, results2);
			
			String sum = "";
			
			for(BasicClass record2 : results2.getObjects()){
				sum = record2.getString("SUM");
			}
			
			record.addProperty("ISSPONSOR2015", "String", "" + sum);
			
			//-------------------------------------------------------------------------------------------------------------------------------------------
			
			//Mitglieder 2015
			
			results2.removeObjects();
			
			//boolean isSponsor2 = false;
			
			sql = "select sum(amount) as sum from accountmovement where OrganisationMember=" + omid + " AND date >='2015-01-01' AND date <='2015-12-31'and debitaccount=3 GROUP BY OrganisationMember";
			
			ocs.queryData(sql, results2);
			
			sum = "";
			
			for(BasicClass record2 : results2.getObjects()){
				sum = record2.getString("SUM");
			}
			
			record.addProperty("ISMEMBER2015", "String", "" + sum);
			
			//-------------------------------------------------------------------------------------------------------------------------------------------
			
			//Spender seit letztem Weihnachtsversand, faktisch 2016 
			
			results2.removeObjects();
			
			boolean isSponsor2 = false;
			
			sql = "select sum(amount) as sum from accountmovement where OrganisationMember=" + omid + " AND date >='2016-01-01' AND date <='2016-12-31'and debitaccount=2 GROUP BY OrganisationMember";
			
			ocs.queryData(sql, results2);
			
			sum = "";
			
			/*
			if(results2.getObjects().size() > 0){
				isSponsor = true;
			}
			*/
			
			for(BasicClass record2 : results2.getObjects()){
				sum = record2.getString("SUM");
			}
			
			//record.addProperty("ISSPONSOR2016", "String", "" + isSponsor2);
			
			record.addProperty("ISSPONSOR2016", "String", "" + sum);
			
			//-------------------------------------------------------------------------------------------------------------------------------------------
			
			//Mitglieder 2016
			

			results2.removeObjects();
			
			//boolean isSponsor2 = false;
			
			sql = "select sum(amount) as sum from accountmovement where OrganisationMember=" + omid + " AND date >='2016-01-01' AND date <='2016-12-31'and debitaccount=3 GROUP BY OrganisationMember";
			
			ocs.queryData(sql, results2);
			
			sum = "";
			
			/*
			if(results2.getObjects().size() > 0){
				isSponsor = true;
			}
			*/
			
			for(BasicClass record2 : results2.getObjects()){
				sum = record2.getString("SUM");
			}
			
			//record.addProperty("ISSPONSOR2016", "String", "" + isSponsor2);
			
			record.addProperty("ISMEMBER2016", "String", "" + sum);
			
			//-------------------------------------------------------------------------------------------------------------------------------------------
			
			results2.removeObjects();
			
			boolean isNewUser = record.getBoolean("ISNEWUSER");
			boolean isNewUser1 = false;
			boolean isNewUser2 = false;
			
			if(isNewUser){
				
				sql = "select * from accountmovement where OrganisationMember=" + omid + " and debitaccount=2";
				
				ocs.queryData(sql, results2);
				
				if(results2.getObjects().size() > 0){
					isNewUser1 = true;
				}
				else{
					isNewUser2 = true;
				}
			
			}	
			record.addProperty("ISNEWUSER1", "String", "" + isNewUser1);
			record.addProperty("ISNEWUSER2", "String", "" + isNewUser2);
			
			
			
			
			//-------------------------------------------------------------------------------------------------------------------------------------------
			
			//Reaktivierbare Gönner
			
			/*
			
			results2.removeObjects();
			
			boolean isSponsor = false;
			
			sql = "select * from accountmovement where OrganisationMember=" + omid + " AND date >='2016-06-05' AND date <='2016-09-30' and debitaccount=2";
			
			ocs.queryData(sql, results2);
			
			if(results2.getObjects().size() > 0){
				isSponsor = true;
			}
			
			record.addProperty("ISSPONSOR", "String", "" + isSponsor);
			
			*/
			
			record.addProperty("Template", "Integer", "0");
			
			if(!record.getString("MEMBERID").equals("X")){
				
				
				if(record.getString("SPONSORID").equals("X")){
					
					double v2015 = 0;
					if(record.getString("ISSPONSOR2015").length() > 0){
						v2015 = record.getDouble("ISSPONSOR2015").doubleValue();	
					}
					
					if(record.getString("ISSPONSOR2016").length()==0 && v2015 < 200){
						record.setProperty("Template", 30);	
					}					
				}
				
				if(record.getString("ISACTIVEUSER").equals("true")){
					if(record.getString("ISSPONSOR2016").length()==0 && record.getString("ISSPONSOR2015").length()==0){
						record.setProperty("Template", 31);	
					}
				}
				

			
				if(record.getString("ISNEWUSER").equals("true")){
					
					if(record.getString("ISNEWUSER1").equals("true")){
						record.setProperty("Template", 27);					
					}
					else if(record.getString("ISNEWUSER2").equals("true")){
						record.setProperty("Template", 28);											
					}
					
					
				}
				else{
					if(record.getString("ISSPONSOR").equals("true")){	
						record.setProperty("Template", 29);							
					}
				}


				
			}
			
		}
		
		
	}
	public static String cleanContent(String content){
		content = StringEscapeUtils.unescapeHtml4(content);
		StringBuilder html = new StringBuilder();
		String[] lines = content.split("\r\n|\r|\n");
		for(String line : lines){
			//html.append("\n" + line.trim());	
			line = line.trim();
			if(line.startsWith("<table ")){
				html.append("\n" + line);	
			}
			else if(line.startsWith("</table>")){
				html.append("\n" + line);	
			}
			else if(line.startsWith("<tr")){
				html.append("\n" + line);	
			}
			else if(line.startsWith("</tr")){
				html.append("\n" + line);	
			}
			else if(line.startsWith("<td")){
				html.append("\n" + line);	
			}
			else if(line.startsWith("</td")){
				html.append("\n" + line);	
			}
			else{
				line = line.replace("<p>", "");
				line = line.replace("</p>", "<p>");
				line = line.replace("<br />", "\n");
				html.append(line);	
			}
			
		}
								
		content = html.toString();
		/*
		content = content.replace("<p>", "");
		content = content.replace("</p>", "<p>");
		content = content.replace("<br />", "\n");
		*/
		content = content.trim();	
		
		return content;
	}
	public void createPDF(ProcessResult result, OpenCommunityServer ocs){
		
		Vector letters = new Vector();
		String content = "";
		String subject = "";
			
		for(BasicClass record : results.getObjects()){
			
			String id = record.getString("ID");
				
			String tbid = record.getString("Template");
				
			if(!tbid.equals("0") && id.equals("2213")==false){
				
				if(getID("Template")==-1 || ("" + getID("Template")).equals(tbid)){

				
					OrganisationMemberInfo omi = ocs.getOrganisationMemberInfo(id);
					
					//record.addProperty("FAMILYNAME", "String", omi.getString("FamilyName"));
					//record.addProperty("FIRSTNAME", "String", omi.getString("FirstName"));
					record.addProperty("STREET", "String", omi.getString("Street"));
					record.addProperty("NUMBER", "String", omi.getString("Number"));				
					record.addProperty("ZIPCODE", "String", omi.getString("Zipcode"));	
					record.addProperty("CITY", "String", omi.getString("City"));
					
					if(omi.getString("Country").equals("Deutschland") || omi.getString("Country").equals("France")){
						record.addProperty("COUNTRY", "String", omi.getString("Country"));
					}
					else{
						record.addProperty("COUNTRY", "String", "");
					}
					
					record.addProperty("ADDRESSATION", "String", omi.getString("Addressation"));	
					record.addProperty("EMAIL", "String", omi.getString("Email"));	
					
	
					
					record.addProperty("DATE", "String", DateConverter.dateToShortDisplay(new java.util.Date(), false));
	
					
					TextBlock tb = ocs.getTextblock(tbid);
					if(tb != null){
						content = tb.getString("Content");
						content = cleanContent(content);
						subject = tb.getString("Subject");
					}
					
					record.addProperty("CONTENT", "String", content);
					record.addProperty("SUBJECT", "String", subject);
					
	
					record.addProperty("ATTACHMENTS", "String", "");
					record.addProperty("ADDRESSES", "String", "");	
					record.addProperty("10RULES", "String", "");	
	
					record.addProperty("MEMBERADS", "String", "");
					
					letters.add(record);
				
				}
				
			}
		
		}
		String filename = ocs.createPassword(8);
			
		String path = ocs.getRootpath() + "/temp/" + filename + ".pdf";
			
		pdfWriter.createPDF(path, templib, "2", letters);
			
		result.setParam("download", "/temp/" + filename + ".pdf");
		
	}
	public void finish(ProcessResult result, ApplicationContext context){
			
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		
		int formulablock_column = 0;
		int formulablock_row = 0;		
		
		if(!getBoolean("CreateList")){
			createPDF(result, ocs);			
		}
		else{
			try{
				
				WorkbookSettings settings = new WorkbookSettings();
				
				WritableCellFormat times;
				WritableFont times10pt = new WritableFont(WritableFont.TIMES, 10);
				times = new WritableCellFormat(times10pt);
					
				String filename = ocs.createPassword(8);
							
				String path = ocs.getRootpath() + "/temp/" + filename + ".xls";
					
				File file = new File(path);
								
				WritableWorkbook workbook = Workbook.createWorkbook(file, settings);	
								
				workbook.createSheet("Spendenliste", 0);
								
				WritableSheet sheet = workbook.getSheet(0);
				
				//for(Object o : settings.getFunctionNames().functions){
				//	ocs.logAccess(o);	
				//}
					
					
				int row = 0;
				int col = 0;
	
				addCell(sheet, col, row, "ID", times);
				col++;
				
				addCell(sheet, col, row, "Template", times);
				col++;
				
				addCell(sheet, col, row, "Datum registriert", times);
				col++;
				
				addCell(sheet, col, row, "Name", times);
				col++;
				
				addCell(sheet, col, row, "Vorname", times);
				col++;
				
				addCell(sheet, col, row, "Geburtsjahr", times);
				col++;
				
				addCell(sheet, col, row, "Geschlecht", times);
				col++;
				
				addCell(sheet, col, row, "Land", times);
				col++;
				
				addCell(sheet, col, row, "Mitglied", times);
				col++;
				
				addCell(sheet, col, row, "Gönner", times);
				col++;
					
				addCell(sheet, col, row, "Registriert seit 2.6.2016", times);
				col++;
					
				addCell(sheet, col, row, "Neuer Nutzer mit Spende", times);
				col++;
					
				addCell(sheet, col, row, "Neuer Nutzer ohne Spende", times);
				col++;
				
				addCell(sheet, col, row, "Spende nach Versand Juni-September 2016", times);
				col++;
					
				
				addCell(sheet, col, row, "Spende 2016", times);
				col++;
				
				addCell(sheet, col, row, "Beitrag 2016", times);
				col++;
				
				addCell(sheet, col, row, "Total 2016", times);
				col++;
					
				addCell(sheet, col, row, "Spende 2015", times);
				col++;
				
				addCell(sheet, col, row, "Beitrag 2015", times);
				col++;
				
				addCell(sheet, col, row, "Total 2015", times);
				col++;
				
				addCell(sheet, col, row, "Aktiv seit 1.10.2015", times);
				col++;
				
				for(BasicClass mac : maa.getObjects("MemberAdCategory")){
					addCell(sheet, col, row, mac.getString("Title"), times);
					col++;				
				}
						
				row++;
				
				
				
				for(BasicClass record : results.getObjects()){	
					col = 0;
						
					addCell(sheet, col, row, record.getString("ID"), times);
					col++;
					
					addCell(sheet, col, row, record.getString("Template"), times);
					col++;
					
					addCell(sheet, col, row, DateConverter.sqlToShortDisplay(record.getString("DATECREATED"), false), times);
					col++;
					
					addCell(sheet, col, row, record.getString("FAMILYNAME"), times);
					col++;
					
					addCell(sheet, col, row, record.getString("FIRSTNAME"), times);
					col++;
					
					addCell(sheet, col, row, record.getString("DATEOFBIRTH"), times);
					col++;
					
					addCell(sheet, col, row, record.getString("SEX"), times);
					col++;
					
					addCell(sheet, col, row, record.getString("COUNTRY"), times);
					col++;
					
					addCell(sheet, col, row, record.getString("MEMBERID"), times);
					col++;
					
					addCell(sheet, col, row, record.getString("SPONSORID"), times);
					col++;
					
					addCell(sheet, col, row, record.getString("ISNEWUSER"), times);
					col++;
					
					addCell(sheet, col, row, record.getString("ISNEWUSER1"), times);
					col++;
					
					addCell(sheet, col, row, record.getString("ISNEWUSER2"), times);
					col++;
					
					addCell(sheet, col, row, record.getString("ISSPONSOR"), times);
					col++;
					
					formulablock_column = col;
					
					if(record.getString("ISSPONSOR2016").length() > 0){
						addNumericCell(sheet, col, row, record.getString("ISSPONSOR2016"), times);						
					}
					else{
						addCell(sheet, col, row, record.getString("ISSPONSOR2016"), times);
					}
					col++;
					
					if(record.getString("ISMEMBER2016").length() > 0){
						addNumericCell(sheet, col, row, record.getString("ISMEMBER2016"), times);						
					}
					else{
						addCell(sheet, col, row, record.getString("ISMEMBER2016"), times);
					}
					col++;
					
					addFormulaCell(sheet, col, row, "SUM(O" + (row+1) + ":P" +(row +1) + ")", times);
					

					
					col++;

					if(record.getString("ISSPONSOR2015").length() > 0){
						addNumericCell(sheet, col, row, record.getString("ISSPONSOR2015"), times);
					}
					else{
						addCell(sheet, col, row, record.getString("ISSPONSOR2015"), times);
					}
					col++;
					
					if(record.getString("ISMEMBER2015").length() > 0){
						addNumericCell(sheet, col, row, record.getString("ISMEMBER2015"), times);
					}
					else{
						addCell(sheet, col, row, record.getString("ISMEMBER2015"), times);
					}
					col++;
					
					addFormulaCell(sheet, col, row, "SUM(R" + (row+1) + ":S" +(row +1) + ")", times);
					
					col++;
					
					addCell(sheet, col, row, record.getString("ISACTIVEUSER"), times);
					col++;
					
					for(BasicClass mac : maa.getObjects("MemberAdCategory")){
						if(record.hasProperty("ACTIVEUSER_" + mac.getName())){
							addCell(sheet, col, row, "X", times);
							col++;	
						}
						else{
							addCell(sheet, col, row, "", times);
							col++;							
						}
					}
					
					
						
					row++;
				}
				
				formulablock_row = row;
				
				addFormulaCell(sheet, formulablock_column , row, "SUM(O1:O" + row + ")", times);
				addFormulaCell(sheet, formulablock_column + 1 , row, "SUM(P1:P" + row + ")", times);			
				addFormulaCell(sheet, formulablock_column + 2, row, "SUM(Q1:Q" + row + ")", times);
				addFormulaCell(sheet, formulablock_column + 3, row, "SUM(R1:R" + row + ")", times);
				addFormulaCell(sheet, formulablock_column + 4, row, "SUM(S1:S" + row + ")", times);
				addFormulaCell(sheet, formulablock_column + 5, row, "SUM(T1:T" + row + ")", times);
				
				
				
				addCell(sheet, formulablock_column , row + 1, "=ZÄHLENWENN(O2:O" + row + ";\">0\")", times);
				addCell(sheet, formulablock_column + 1, row + 1, "=ZÄHLENWENN(P2:P" + row + ";\">0\")", times);
				addCell(sheet, formulablock_column + 2, row + 1, "=ZÄHLENWENN(Q2:Q" + row + ";\">0\")", times);
				addCell(sheet, formulablock_column + 3, row + 1, "=ZÄHLENWENN(R2:R" + row + ";\">0\")", times);
				addCell(sheet, formulablock_column + 4, row + 1, "=ZÄHLENWENN(S2:S" + row + ";\">0\")", times);
				addCell(sheet, formulablock_column + 5, row + 1, "=ZÄHLENWENN(T2:T" + row + ";\">0\")", times);
				
				//addFormulaCell(sheet, formulablock_column , row + 2, "COUNTIF(O2:O" + row + ";>0)", times);
				
				//ZÄHLENWENN(Bereich;Suchkriterium)
				
				workbook.write();
				workbook.close();
					
				result.setParam("download", "/temp/" + filename + ".xls");
			}
			catch(java.lang.Exception e){
				ocs.logException(e);	
			}
		}
		
	}
	private void addCell(WritableSheet sheet, int column, int row, String s, WritableCellFormat format){
		try{
			Label label;
			label = new Label(column, row, s, format);
			sheet.addCell(label);
		}
		catch(java.lang.Exception e){
			
		}
	}
	private void addNumericCell(WritableSheet sheet, int column, int row, String s, WritableCellFormat format){
		try{
			double val = Double.parseDouble(s);
			jxl.write.Number n = new jxl.write.Number(column, row, val);
			sheet.addCell(n);
		}
		catch(java.lang.Exception e){
			
		}
	}
	private void addFormulaCell(WritableSheet sheet, int column, int row, String formula, WritableCellFormat format){
		try{

			jxl.write.Formula f = new jxl.write.Formula(column, row, formula);
			//jxl.write.Formula f = new jxl.write.Formula(column, row, "(1+1)");
			sheet.addCell(f);
		}
		catch(java.lang.Exception e){
			
		}
	}
	
}