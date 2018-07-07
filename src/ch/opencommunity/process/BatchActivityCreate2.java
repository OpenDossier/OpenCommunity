package ch.opencommunity.process;

import ch.opencommunity.server.OpenCommunityServer;
import ch.opencommunity.common.OpenCommunityUserSession;
import ch.opencommunity.base.OrganisationMemberInfo;
import ch.opencommunity.base.TextBlock;
import ch.opencommunity.base.Activity;
import ch.opencommunity.base.BatchActivity;
import ch.opencommunity.advertising.MemberAdAdministration;
import ch.opencommunity.view.OrganisationMemberList;
import ch.opencommunity.query.QueryDefinition;

import org.kubiki.ide.BasicProcess;
import org.kubiki.base.Property;
import org.kubiki.base.BasicClass;
import org.kubiki.base.BasicClassComparator;
import org.kubiki.base.ObjectCollection;
import org.kubiki.base.ProcessResult;
 
import org.kubiki.application.ApplicationContext;
import org.kubiki.gui.html.HTMLFormManager;

import org.kubiki.util.DateConverter;

import org.kubiki.xml.XMLParser;
import org.kubiki.xml.XMLElement;

import org.kubiki.pdf.*;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.File;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;
import java.util.Collections;

import jxl.*;
import jxl.write.*;

public class BatchActivityCreate2 extends BasicProcess{
	
	ObjectCollection queries = null;
	ObjectCollection results = null;
	
	OpenCommunityServer server; 
	
	SortedMap records;
	
	PDFWriter pdfWriter = null;
	
	PDFTemplateLibrary templib = null;
	
	
	public 	BatchActivityCreate2(){
		
		setName("BatchActivityCreateNode");
		
		results = addObjectCollection("Results", "*");
		queries = addObjectCollection("QueryTextRelation", "*");
		
		addNode(this);
		

		
		setCurrentNode(this);
		
		
		
	}
	public void initProcess(){
		server = (OpenCommunityServer)getRoot();	
		
		templib = (PDFTemplateLibrary)server.getObjectByName("PDFTemplateLibrary", "1");
		
		pdfWriter = new PDFWriter();
		
		addQueryTextRelation();
	}
	public String getBatchActivityCreateForm(ApplicationContext context){
		
		StringBuilder html = new StringBuilder();
		
		HTMLFormManager formManager = server.getFormManager();
		
		html.append("<input type=\"button\" onclick=\"getNextNode('addquery=true')\" value=\"Abfrage hinzufügen\">");
		
		html.append("<form id=\"processNodeForm\" name=\"processNodeForm\">");
		
		html.append("<table>");
		
		int i = 1;
		
		for(BasicClass bc : getObjects("QueryTextRelation")){
			
			
			
			html.append("<tr>");
			
			html.append("<td>" + formManager.getSelection(bc.getProperty("QueryDefinition"), true, i + "_") + "</td>");
			html.append("<td>" + formManager.getSelection(bc.getProperty("TextBlock"), true, i + "_") + "</td>");
			
			html.append("</tr>");		
			
			i++;
		}
		
		html.append("</table>");
		
		html.append("</form>");


		html.append("<input type=\"button\" onclick=\"cancel()\" value=\"Abbrechen\">");
		html.append("<input type=\"button\" onclick=\"getNextNode()\" value=\"Abschliessen\">");
		
		return html.toString();		
	}
	
	public boolean validate(ApplicationContext context){
		
		int i = 1;
			
		for(BasicClass bc : getObjects("QueryTextRelation")){
				
			if(context.hasProperty(i + "_QueryDefinition")){
				bc.setProperty("QueryDefinition", context.getString(i + "_QueryDefinition"));	
			}
			if(context.hasProperty(i + "_TextBlock")){
				bc.setProperty("TextBlock", context.getString(i + "_TextBlock"));	
			}				
			i++;
				
		}
		
		if(context.hasProperty("addquery")){
					
			addQueryTextRelation();
			return false;
		}
		else{
			return true;	
		}
	}
	
	public void addQueryTextRelation(){
		QueryTextRelation qtr = new QueryTextRelation();
		qtr.setParent(this);
		addSubobject("QueryTextRelation", qtr);
		qtr.initObjectLocal();
	}
	public void finish(ProcessResult result, ApplicationContext context){
		
		records = new TreeMap();
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
		OrganisationMemberList organisationMemberList = (OrganisationMemberList)ocs.querylists.get("organisationMemberList");
		
		String content = null;
		String subject = null;
		
		if(organisationMemberList != null){
			
			int i = 1;
		
			for(BasicClass bc : getObjects("QueryTextRelation")){
				
				int queryid = bc.getID("QueryDefinition");
				int textblockid = bc.getID("TextBlock");
				
				if(queryid > 0){
				
					QueryDefinition queryDefinition = (QueryDefinition)ocs.getObjectByName("QueryDefinition", "" + queryid);
					if(queryDefinition != null){
						String xml = queryDefinition.getString("XML");
						ocs.logAccess(xml);
						XMLElement xmlDoc = XMLParser.parseString(xml);
						if(xmlDoc.getChild("ch.opencommunity.common.OpenCommunityAdminSession") != null){
							XMLParser.parseSubelements(userSession, xmlDoc.getChild(0));	
							XMLParser.setProperties(userSession, xmlDoc.getChild(0));
							
							String sql = organisationMemberList.getSQL(context);
							sql += organisationMemberList.getFilter().getFilterString(context);
							
							
							TextBlock tb = ocs.getTextblock("" + textblockid);
							
							if(tb != null){
								content = tb.getString("Content");
								content = cleanContent(content);
								subject = tb.getString("Subject");
								ocs.logAccess(content);
							}
							
							prepareLetters(ocs, sql, results, content, subject, subject, i); 
							
	
						}
					}
					
				}
				i++;
				
			}
		}
		
		Vector records2 = new Vector(records.values());
		
		BasicClassComparator comparator = new BasicClassComparator("SortKey", "String", false);
		Collections.sort(records2, comparator);
		
		String filename = ocs.createPassword(8);
			
		String path = ocs.getRootpath() + "/temp/" + filename + ".pdf";
			
		pdfWriter.createPDF(path, templib, "4", records2);
			
		result.setParam("download", "/temp/" + filename + ".pdf");

		
		
		
		
	}
	
	
	public void prepareLetters(OpenCommunityServer ocs, String sql, ObjectCollection results, String content, String subject, String content_alt, int textblockid){
		
		if(sql != null){
			
			results.removeObjects();
			
			ocs.queryData(sql, results);	
			
			ocs.logAccess("results: " + results.getObjects().size());
			
			for(BasicClass record : results.getObjects()){
				
				String id = record.getString("ID");
				
				record.addProperty("SortKey", "String", textblockid + "_" + id);
				
				records.put(id, record);
				
				String activityid = record.getString("ACTIVITYID");
				
				OrganisationMemberInfo omi = ocs.getOrganisationMemberInfo(id);
				
				record.addProperty("FAMILYNAME", "String", omi.getString("FamilyName"));
				record.addProperty("FIRSTNAME", "String", omi.getString("FirstName"));
				record.addProperty("STREET", "String", omi.getString("Street"));
				record.addProperty("NUMBER", "String", omi.getString("Number"));				
				record.addProperty("ZIPCODE", "String", omi.getString("Zipcode"));	
				record.addProperty("CITY", "String", omi.getString("City"));
				String additionalline = omi.getString("AdditionalLine");
				if(additionalline.length() > 0){
					additionalline = "\n" + additionalline;	
				}
				record.addProperty("ADDITIONALLINE", "String", additionalline);
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
					//ocs.queryData("SELECT SUM(Amount) AS SUM FROM AccountMovement WHERE (DebitAccount=2 OR DebitAccount=3) AND Date >= '2016-01-01' AND OrganisationMember=" + id, results2);
					ocs.queryData("SELECT SUM(Amount) AS SUM FROM AccountMovement WHERE (DebitAccount=2 OR DebitAccount=3) AND Date >= '2017-01-01' AND OrganisationMember=" + id, results2);
					for(BasicClass bc : results2.getObjects()){
						sum = bc.getString("SUM");	
					}
					String[] args = sum.split("\\.");
					content2 = content2.replace("<#SUM>", args[0]);							
					
				}

				
				record.addProperty("CONTENT", "String", content2);
				
				/*
				if((email == null || email.trim().length()==0) && content_alt != null){
					String content_alt2 = content_alt.replace("<#EMAIL>", email);
					record.setProperty("CONTENT", content_alt2);
				}
				record.addProperty("ATTACHMENTS", "String", "");
				record.addProperty("ADDRESSES", "String", "");	
				record.addProperty("10RULES", "String", "");	
				
				*/
				
				/*
				
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
				
				*/
				
			}
		}
		
		
	}
	public static String cleanContent2(String content){
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
	public static String cleanContent(String content){
		
		content = content.replaceAll("&#8226;", "-");
		
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
	class QueryTextRelation extends BasicClass{
		
		public QueryTextRelation(){
			
			addProperty("QueryDefinition", "Integer", "");	
			addProperty("TextBlock", "Integer", "");	
		}
		public void initObjectLocal(){
			
			getProperty("QueryDefinition").setSelection(server.getObjects("QueryDefinition"));
			getProperty("TextBlock").setSelection(server.getTextblocks(1, 3));			

			
		}
		
	}
	
}