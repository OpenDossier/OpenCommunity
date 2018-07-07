package ch.opencommunity.process;

import ch.opencommunity.server.OpenCommunityServer;
import ch.opencommunity.base.OrganisationMemberInfo;
import ch.opencommunity.base.TextBlock;
import ch.opencommunity.base.Activity;
import ch.opencommunity.base.BatchActivity;
import ch.opencommunity.advertising.MemberAdAdministration;
import ch.opencommunity.view.OrganisationMemberList;

import org.kubiki.ide.BasicProcess;
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

import jxl.*;
import jxl.write.*;

public class BatchActivityCreate extends BasicProcess{
	
	ObjectCollection results = null;
	
	PDFWriter pdfWriter = null;
	
	PDFTemplateLibrary templib = null;
	
	BatchActivity ba = null;
	
	String sql = null;
	
	String content = null;
	String subject = null;
	String content_alt = null;

	public BatchActivityCreate(){
		
		addNode(this);
		
		addProperty("Mode", "Integer", "", true, "Mode");
		addProperty("BAID", "Integer", "", true, "BAID");
		
		Property p = addProperty("Subject", "String", "", false, "Betreff");
		p = addProperty("TextBlockID", "Integer", "", true, "Standardtext");
		p.setAction("getNextNode('textblockid=' + this.value)");
		p = addProperty("Content", "Text", "", false, "Inhalt");
		
		addProperty("CreateActivity", "Boolean", "false", false, "Aktivitäten erstellen");
		
		
		results = addObjectCollection("Results", "*");
		
		setCurrentNode(this);
		
		pdfWriter = new PDFWriter();
		
	}
	public void initProcess(ApplicationContext context){
		/*
		Mode 1 = Erstspender
		Mode 2 = Brief Mitglieder
		Mode 3 = Mahnung Mitglieder
		Mode 4 = Spendenaufruf
		
		*/
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		templib = (PDFTemplateLibrary)ocs.getObjectByName("PDFTemplateLibrary", "1");
		
		if(getString("BAID") != null){
			ba = (BatchActivity)ocs.getObject(null, "BatchActivity", "ID", getString("BAID"));
			if(ba != null){
				
				XMLElement xmlDoc = XMLParser.parseString(ba.getString("Parameters"));
				if(xmlDoc != null && xmlDoc.getChild(0) != null){
					XMLParser.setProperties(this, xmlDoc.getChild(0));
				}
				getProperty("CreateActivity").setHidden(true);
			}
		}
		
		
		

		

		if(getID("Mode")==1){
			sql = "select t2.description, t3.description, t4.description, t5.description, t1.ID from organisationmember as t1"; 
			sql += " join accountmovement as t2 on t2.organisationmember=t1.id and t2.description='Spenden 2016'";
			sql += " left join accountmovement as t3 on t3.organisationmember=t1.id and t3.description='Spenden 2015'";
			sql += " left join accountmovement as t4 on t4.organisationmember=t1.id and t4.description='Spenden 2014'";
			sql += " left join accountmovement as t5 on t5.organisationmember=t1.id and t5.description='Spenden 2013'"; 
			sql += " where t3.description is null and t4.description is null and t5.description is null";
			
			if(ba == null){
				TextBlock tb = ocs.getTextblock("21");
				if(tb != null){
					content = tb.getString("Content");
					content = cleanContent(content);
					subject = tb.getString("Subject");
				}
			}
		}
		else if(getID("Mode")==2){
			sql = "SELECT DISTINCT t1.ID FROM OrganisationMember AS t1";
			sql += " JOIN Person AS t2 ON t1.Person=t2.ID";
			sql += " JOIN MemberRole AS t3 ON t3.OrganisationMemberID=t1.ID AND t3.Role=2";
			sql += " LEFT JOIN accountmovement as t4 on t4.organisationmember=t1.id and (t4.description='Mitgliedschaft 2016' OR t4.description='Spenden 2016')";
			sql += " WHERE t4.Amount IS NOT NULL";
			sql += " AND t1.Status NOT IN (2,3)";
			
			if(ba == null){
				TextBlock tb = ocs.getTextblock("22");
				if(tb != null){
					content = tb.getString("Content");
					content = cleanContent(content);
					subject = tb.getString("Subject");
				}
			}
		}
		else if(getID("Mode")==3){
			sql = "SELECT DISTINCT t1.ID FROM OrganisationMember AS t1";
			sql += " JOIN Person AS t2 ON t1.Person=t2.ID";
			sql += " JOIN MemberRole AS t3 ON t3.OrganisationMemberID=t1.ID AND t3.Role=2";
			sql += " LEFT JOIN accountmovement as t4 on t4.organisationmember=t1.id and (t4.description='Mitgliedschaft 2016' OR t4.description='Spenden 2016')";
			sql += " WHERE t4.Amount IS NULL";
			sql += " AND t1.Status NOT IN (2,3)";
			
			if(ba == null){
				TextBlock tb = ocs.getTextblock("23");
				if(tb != null){
					content = tb.getString("Content");
					content = cleanContent(content);
					subject = tb.getString("Subject");
				}
			}
		}
		else if(getID("Mode")==4){
			
			Property p = addProperty("CreateList", "Boolean", "false", false, "Liste erstellen");
			
			addProperty("TimeFrom", "DateTime", "", false, "Zeitraum von");
			addProperty("TimeTo", "DateTime", "", false, "Zeitraum bis");
			addProperty("AmountFrom", "Integer", "", false, "Betrag von");
			addProperty("AmountTo", "Integer", "", false, "Betrag bis");
			addProperty("IsUser", "Boolean", "", false, "Benutzer");
			addProperty("IsMember", "Boolean", "", false, "Mitglied");
			addProperty("IsSponsor", "Boolean", "", false, "Gönner");
			
			MemberAdAdministration maa = ocs.getMemberAdAdministration();
			
			p = addProperty("HasOffer", "Integer", "", false, "Inserat Angebot");
			p.setSelection(maa.getObjects("MemberAdCategory"));
			
			p = addProperty("HasRequest", "Integer", "", false, "Inserat Nachfrage");
			p.setSelection(maa.getObjects("MemberAdCategory"));
			
			p = addProperty("HasAddressRequest", "Integer", "", false, "Adressbestellung");
			p.setSelection(maa.getObjects("MemberAdCategory"));
		}
		else if(getID("Mode")==5){
			OrganisationMemberList organisationMemberList = (OrganisationMemberList)ocs.querylists.get("organisationMemberList");
			if(organisationMemberList != null){
				sql = organisationMemberList.getSQL(context);
				sql += organisationMemberList.getFilter().getFilterString(context);
				
				getProperty("TextBlockID").setHidden(false);
				getProperty("TextBlockID").setSelection(ocs.getTextblocks(1));
			}
			
		}
		else if(getID("Mode")==6){ //Spendenbescheinigung
			sql = "SELECT sum(t4.amount) AS SUM, t1.ID FROM OrganisationMember AS t1";
			sql += " JOIN Person AS t2 ON t1.Person=t2.ID";
			//sql += " JOIN MemberRole AS t3 ON t3.OrganisationMemberID=t1.ID AND t3.Role=2";
			sql += " JOIN accountmovement as t4 on t4.organisationmember=t1.id and (t4.debitaccount=2 OR t4.debitaccount=3)";
			sql += " WHERE t4.Date >= '2016-01-01'";
			sql += " GROUP BY t1.ID";
			sql += " HAVING sum(t4.amount) >= 50";
			//sql += " AND t1.Status NOT IN (2,3)";
			
			if(ba == null){
				TextBlock tb = ocs.getTextblock("35");
				if(tb != null){
					content = tb.getString("Content");
					content = cleanContent(content);
					subject = tb.getString("Subject");
				}
			}
		}
		else  if(ba != null){
			
			sql = "SELECT t1.ID, t2.ID AS ActivityID from organisationmember as t1 join activity as t2 on t2.organisationmemberid=t1.id and t2.batchactivityid=" + ba.getName(); 
			
			String tbid = ba.getString("TextBlockID");
			
			TextBlock tb = ocs.getTextblock(tbid);
			if(tb != null){
				content = tb.getString("Content");
				content = cleanContent(content);
				subject = tb.getString("Subject");
			}
			if(tbid.equals("18")){
				tb = ocs.getTextblock("26"); //todo: Relation abbilden im Textblock
				if(tb != null){
					content_alt = tb.getString("Content");
					content_alt = cleanContent(content_alt);
				}				
			}
			
		}
		if(content != null){
			setProperty("Content", content);	
			setProperty("Subject", subject);	
		}
		prepareLetters(ocs, sql, results, content, subject, content_alt);
	}
	public static void prepareLetters(OpenCommunityServer ocs, String sql, ObjectCollection results, String content, String subject, String content_alt){
		
		if(sql != null){
			
			results.removeObjects();
			
			ocs.queryData(sql, results);	
			
			for(BasicClass record : results.getObjects()){
				
				String id = record.getString("ID");
				String activityid = record.getString("ACTIVITYID");
				
				OrganisationMemberInfo omi = ocs.getOrganisationMemberInfo(id);
				
				record.addProperty("FAMILYNAME", "String", omi.getString("FamilyName"));
				record.addProperty("FIRSTNAME", "String", omi.getString("FirstName"));
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
				
				record.addProperty("SUBJECT", "String", subject);
				
				record.addProperty("DATE", "String", DateConverter.dateToShortDisplay(new java.util.Date(), false));
				
				//todo : variablen als Skript definierbar
				

				String email = omi.getString("Email");
				
				String content2 = content.replace("<#EMAIL>", email);
				
				if(record.hasProperty("SUM")){
					String sum = record.getString("SUM");
					String[] args = sum.split("\\.");
					content2 = content2.replace("<#SUM>", args[0]);					
				}
				
				else{
					
					String sum = "0";
					ObjectCollection results2 = new ObjectCollection("Results2", "*");
					ocs.queryData("SELECT SUM(Amount) AS SUM FROM AccountMovement WHERE (DebitAccount=2 OR DebitAccount=3) AND Date >= '2016-01-01' AND OrganisationMember=" + id, results2);
					for(BasicClass bc : results2.getObjects()){
						sum = bc.getString("SUM");	
					}
					String[] args = sum.split("\\.");
					content2 = content2.replace("<#SUM>", args[0]);							
					
				}

				
				record.addProperty("CONTENT", "String", content2);
				if((email == null || email.trim().length()==0) && content_alt != null){
					String content_alt2 = content_alt.replace("<#EMAIL>", email);
					record.setProperty("CONTENT", content_alt2);
				}
				record.addProperty("ATTACHMENTS", "String", "");
				record.addProperty("ADDRESSES", "String", "");	
				record.addProperty("10RULES", "String", "");	
				
				StringBuilder memberads = new StringBuilder();
				
				
				
				if(activityid != null && activityid.length() > 0){
					
					memberads.append("\n#NEWPAGE");
					memberads.append("\n<b>Aufgeschaltete Inserate</b>");
					
					ObjectCollection results2 = new ObjectCollection("Results2", "*");
					
					sql = "SELECT t1.*, t2.Title AS Category FROM MemberAd AS t1 JOIN MemberAdCategory AS t2 ON t1.Template=t2.ID JOIN ActivityObject AS t3 ON t3.MemberAdID=t1.ID";
					
					sql += " WHERE t3.ActivityID=" + activityid;
					
					ocs.queryData(sql, results2);
									
					for(BasicClass bc : results2.getObjects()){
						memberads.append("\n\n<b>" + bc.getString("CATEGORY") + "</b>");
						memberads.append("\n" + bc.getString("TITLE"));
						memberads.append("\n" + bc.getString("DESCRIPTION"));
						memberads.append("\nAufgeschaltet von " + DateConverter.sqlToShortDisplay(bc.getString("VALIDFROM"), false) + " - " + DateConverter.sqlToShortDisplay(bc.getString("VALIDUNTIL"), false));
					}
					
				}
				record.addProperty("MEMBERADS", "String", memberads.toString());
				
			}
		}
		
		
	}
	public void finish(ProcessResult result, ApplicationContext context){
		

		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		
		String batchid = null;
		
		if(ba != null){
			ba.setProperty("Title",  getString("Subject"));
			ocs.updateObject(ba);
		}
		else if(getBoolean("CreateActivity")){
			try{
				BatchActivity ba = (BatchActivity)ocs.createObject("ch.opencommunity.base.BatchActivity", null, context);	
				ba.setProperty("Context", getID("Mode"));
				ba.setProperty("Title",  getString("Subject"));
				ba.setProperty("DateCreated", DateConverter.dateToSQL(new java.util.Date(), true));
				String xml = getXMLString(false);
				ba.setProperty("Parameters", xml);
				batchid = ocs.insertObject(ba);
			}
			catch(java.lang.Exception e){
				ocs.logException(e);
			}
		}
		
		if(getID("Mode")==4 && getBoolean("CreateList")){
			
			try{
			
				WritableCellFormat times;
				WritableFont times10pt = new WritableFont(WritableFont.TIMES, 10);
				times = new WritableCellFormat(times10pt);
				
				String filename = ocs.createPassword(8);
						
				String path = ocs.getRootpath() + "/temp/" + filename + ".xls";
				
				File file = new File(path);
							
				WritableWorkbook workbook = Workbook.createWorkbook(file);	
							
				workbook.createSheet("Spendenliste", 0);
							
				WritableSheet sheet = workbook.getSheet(0);
				
				
				int row = 0;
				int col = 0;
				for(BasicClass record : results.getObjects()){
					
					col = 0;
					
					addCell(sheet, col, row, record.getString("FAMILYNAME"), times);
					col++;
					addCell(sheet, col, row, record.getString("FIRSTNAME"), times);
					
					col++;
					addCell(sheet, col, row, record.getString("DATEOFBIRTH"), times);
					
					col++;
					addCell(sheet, col, row, record.getString("SUM"), times);
					
					col++;
					addCell(sheet, col, row, record.getString("CNT"), times);
					
					
					row++;
				}
				
				
				workbook.write();
				workbook.close();
				
				result.setParam("download", "/temp/" + filename + ".xls");
				
			}
			catch(java.lang.Exception e){
				ocs.logException(e);
			}
			
		}
		else{
			for(BasicClass record : results.getObjects()){
				
				//record.setProperty("CONTENT", getString("Content"));
				//record.setProperty("SUBJECT", getString("Subject"));
				
				if(batchid != null){
					Activity activity = (Activity)ocs.createObject("ch.opencommunity.base.Activity", null, context);
					activity.addProperty("OrganisationMemberID", "String", record.getString("ID"));
					activity.setProperty("Template", 9);
					activity.setProperty("Title", getString("Subject"));
					activity.setProperty("Date", DateConverter.dateToSQL(new java.util.Date(), false));
					activity.setProperty("BatchActivityID", batchid);
					ocs.insertObject(activity);
				}
				
			}
			
			String filename = ocs.createPassword(8);
			
			String path = ocs.getRootpath() + "/temp/" + filename + ".pdf";
			
			pdfWriter.createPDF(path, templib, "2", results.getObjects());
			
			result.setParam("download", "/temp/" + filename + ".pdf");
		}
		
	}
	public boolean validate(ApplicationContext context){
		
		if(context.hasProperty("textblockid")){
			
			OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
			
			TextBlock tb = ocs.getTextblock(context.getString("textblockid"));
			if(tb != null){
				
				content = tb.getString("Content");
				content = cleanContent(content);
				subject = tb.getString("Subject");

				setProperty("Content", content);	
				setProperty("Subject", subject);	
			}
			
			return false;
		}
		else if(context.hasProperty("TextBlockID")){
			
			OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
			
			TextBlock tb = ocs.getTextblock(context.getString("TextBlockID"));
			if(tb != null){
				
				content = tb.getString("Content");
				content = cleanContent(content);
				subject = tb.getString("Subject");

				setProperty("Content", content);	
				setProperty("Subject", subject);	
			}
			
			prepareLetters(ocs, sql, results, content, subject, content_alt);
			
			return true;
		}
		else if(getID("Mode")==4){
			
			OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
			
			sql = "SELECT DISTINCT t1.ID, t12.DateOfBirth, SUM(t6.Amount) AS SUM,  Count(t6.ID) AS CNT FROM OrganisationMember AS t1";
			sql += " JOIN Person AS t2 ON t1.Person=t2.ID";
			sql += " JOIN Identity AS t12 ON t12.PersonID=t2.ID";

			if(getBoolean("IsUser")){
				sql += " JOIN MemberRole AS t3 ON t3.OrganisationMemberID=t1.ID AND t3.Role=1";
			}
			if(getBoolean("IsMember")){
				sql += " JOIN MemberRole AS t4 ON t4.OrganisationMemberID=t1.ID AND t4.Role=2";
			}
			if(getBoolean("IsSponsor")){
				sql += " JOIN MemberRole AS t5 ON t4.OrganisationMemberID=t1.ID AND t5.Role=3";
			}
			sql += " JOIN accountmovement as t6 on t6.organisationmember=t1.id and t6.DebitAccount=2";
			
			
			if(getInt("AmountFrom") > 0){
				sql += " AND t6.Amount >= " + getInt("AmountFrom");	
			}
			if(getInt("AmountTo") > 0){
				sql += " AND t6.Amount <= " + getInt("AmountTo");	
			}
			if(getString("TimeFrom").length() > 0){
				sql += " AND t6.Date >='" + getString("TimeFrom") + "'";	
			}
			if(getString("TimeTo").length() > 0){
				sql += " AND t6.Date <='" + getString("TimeTo") + "'";	
			}
			
			sql += " AND t1.Status NOT IN (2,3)";
			
			sql += " GROUP BY t1.ID, t12.DateOfBirth";
			
			prepareLetters(ocs, sql, results, "content", "subject", null);
			
			return true;		
		}
		else{
			return true;	
		}
		
		
	}
	public static String cleanContent(String content){
		content = StringEscapeUtils.unescapeHtml4(content);
		StringBuilder html = new StringBuilder();
		String[] lines = content.split("\r\n|\r|\n");
		for(String line : lines){
			//html.append("\n" + line.trim());	
			html.append(line.trim());	
		}
								
		content = html.toString();
		content = content.replace("<p>", "");
		content = content.replace("</p>", "<p>");
		content = content.replace("<br />", "\n");
		content = content.trim();	
		
		return content;
	}
	
	//----------------------------------------------------------------------------------------
	
	private void addCell(WritableSheet sheet, int column, int row, String s, WritableCellFormat format){
		try{
			Label label;
			label = new Label(column, row, s, format);
			sheet.addCell(label);
		}
		catch(java.lang.Exception e){
			
		}
	}
	
}