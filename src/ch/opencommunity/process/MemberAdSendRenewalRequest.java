package ch.opencommunity.process;

import ch.opencommunity.server.OpenCommunityServer;

import ch.opencommunity.base.ActivityObject;
import ch.opencommunity.base.BatchActivity;
import ch.opencommunity.base.TextBlock;

import org.kubiki.process.BatchProcess;
import org.kubiki.process.BatchProcessNode;
import org.kubiki.ide.BasicProcessNode;
import org.kubiki.base.ObjectCollection;
import org.kubiki.base.BasicClass;
import org.kubiki.base.Property;
import org.kubiki.base.ProcessResult;
import org.kubiki.application.ApplicationContext;
import org.kubiki.ide.BasicProcess;
import org.kubiki.util.DateConverter;

import org.kubiki.pdf.*;

import java.util.Hashtable;
import java.util.Vector;





public class MemberAdSendRenewalRequest extends BatchProcess{
	
	BasicProcessNode node1 = null;
	PDFTemplateLibrary templib = null;

	public MemberAdSendRenewalRequest(){
		
		//addNode(this);
		//setCurrentNode(this);
		addProperty("Mode", "Integer", "0");
		addProperty("Count", "Integer", "0");
		
		
	}
	public void initProcess(){
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		
		ObjectCollection results = new ObjectCollection("Results", "*");
		
		String sql = null;
		
		if(getID("Mode")==1){
		
			sql = "SELECT DISTINCT t1.*, t2.ID AS OMID, t4.FamilyName, t4.FirstName, t4.Sex, t5.Value AS Email, t1.NotificationMode FROM MemberAd AS t1";
			sql += " INNER JOIN OrganisationMember AS t2 ON t1.OrganisationMemberID=t2.ID";
			sql += " LEFT JOIN Person AS t3 ON t2.Person=t3.ID";
			sql += " LEFT JOIN Identity AS t4 ON t4.PersonID=t3.ID";
			sql += " LEFT JOIN Contact AS t5 ON t5.PersonID=t3.ID AND t5.Type=3";


			//sql += " WHERE (t1.DateReminder < NOW() OR t1.ValidUntil < NOW())";
			sql += " WHERE (t1.ValidUntil < NOW())";
			
			sql += " AND t1.Status=1";
			sql += " AND t2.Status=1";
 
			sql += " ORDER BY t2.ID";
			//sql += " LIMIT 5";
		
		}
		else if(getID("Mode")==2){
		
			sql = "SELECT DISTINCT t1.*, t2.ID AS OMID, t4.FamilyName, t4.FirstName, t4.Sex, t5.Value AS Email, t1.NotificationMode FROM MemberAd AS t1";
			sql += " INNER JOIN OrganisationMember AS t2 ON t1.OrganisationMemberID=t2.ID";
			sql += " LEFT JOIN Person AS t3 ON t2.Person=t3.ID";
			sql += " LEFT JOIN Identity AS t4 ON t4.PersonID=t3.ID";
			sql += " LEFT JOIN Contact AS t5 ON t5.PersonID=t3.ID AND t5.Type=3";


			//sql += " WHERE (t1.DateReminder < NOW() OR t1.ValidUntil < NOW())";
			sql += " WHERE (t1.ValidUntil < NOW())";
			
			sql += " AND t1.Status=1";
			sql += " AND t2.Status=1";
 
			sql += " ORDER BY t2.ID";
			//sql += " LIMIT 5";
		
		}
		else if(getID("Mode")==3){
		
			sql = "SELECT DISTINCT t1.*, t2.ID AS OMID, t4.FamilyName, t4.FirstName, t4.Sex, t5.Value AS Email, t1.NotificationMode FROM MemberAd AS t1";
			sql += " INNER JOIN OrganisationMember AS t2 ON t1.OrganisationMemberID=t2.ID";
			sql += " LEFT JOIN Person AS t3 ON t2.Person=t3.ID";
			sql += " LEFT JOIN Identity AS t4 ON t4.PersonID=t3.ID";
			sql += " LEFT JOIN Contact AS t5 ON t5.PersonID=t3.ID AND t5.Type=3";


			//sql += " WHERE (t1.DateReminder < NOW() OR t1.ValidUntil < NOW())";
			sql += " WHERE (t1.ValidUntil < NOW())";
			
			sql += " AND t1.Status=1";
			sql += " AND t2.Status=1";
 
			sql += " ORDER BY t2.ID";
			//sql += " LIMIT 5";
		
		}
		/*
		else if(getID("Mode")==2){
		
			sql = "SELECT DISTINCT t1.*, t2.ID AS OMID, t4.FamilyName, t4.FirstName, t4.Sex, t5.Value AS Email, t1.NotificationMode, t7.DateCreated AS FIRSTREMINDER, t8.DateCreated AS SECONDREMINDER FROM MemberAd AS t1";
			sql += " INNER JOIN OrganisationMember AS t2 ON t1.OrganisationMemberID=t2.ID";
			sql += " LEFT JOIN Person AS t3 ON t2.Person=t3.ID";
			sql += " LEFT JOIN Identity AS t4 ON t4.PersonID=t3.ID";
			sql += " LEFT JOIN Contact AS t5 ON t5.PersonID=t3.ID AND t5.Type=3";
			sql += " LEFT JOIN ActivityObject AS t6 ON t6.MemberAdID=t1.ID";
			sql += " LEFT JOIN Activity AS t7 ON t2.ID=t7.OrganisationMemberID AND t7.Template IN (4,5) AND t7.Context=6 AND t7.ID=t6.ActivityID";
			sql += " LEFT JOIN Activity AS t8 ON t2.ID=t8.OrganisationMemberID AND t8.Template IN (4,5,8) AND t8.Context=7 AND t8.ID=t6.ActivityID";
			sql += " WHERE ((t7.ID IS NOT NULL AND t7.DateCreated < NOW() - interval '30 days') OR t5.Value IS NULL OR CHAR_LENGTH(TRIM(t5.Value))=0)";
			sql += " AND (t1.DateReminder < NOW() OR t1.ValidUntil < NOW())";
			sql += " AND t1.Status=1";
			sql += " AND t2.Status=1";
			sql += " AND t8.ID IS NULL "; 
			sql += " ORDER BY t2.ID";
			//sql += " LIMIT 5";
		
		}
		*/
		/*
		else if(getID("Mode")==3){
			sql = "SELECT DISTINCT t1.*, t2.ID AS OMID, t4.FamilyName, t4.FirstName, t4.Sex, t5.Value AS Email, t1.NotificationMode, t7.DateCreated AS FIRSTREMINDER, t8.DateCreated AS SECONDREMINDER FROM MemberAd AS t1";
			sql += " INNER JOIN OrganisationMember AS t2 ON t1.OrganisationMemberID=t2.ID";
			sql += " LEFT JOIN Person AS t3 ON t2.Person=t3.ID";
			sql += " LEFT JOIN Identity AS t4 ON t4.PersonID=t3.ID";
			sql += " LEFT JOIN Contact AS t5 ON t5.PersonID=t3.ID AND t5.Type=3";
			sql += " LEFT JOIN ActivityObject AS t6 ON t6.MemberAdID=t1.ID";
			sql += " LEFT JOIN Activity AS t7 ON t2.ID=t7.OrganisationMemberID AND t7.Template IN (4,5) AND t7.Context=6 AND t7.ID=t6.ActivityID";
			sql += " LEFT JOIN Activity AS t8 ON t2.ID=t8.OrganisationMemberID AND t8.Template IN (4,5,8) AND t8.Context=7 AND t8.ID=t6.ActivityID";
			sql += " WHERE (t8.ID IS NOT NULL AND t8.DateCreated < NOW() - interval '30 days')";
			sql += " AND t1.Status=1";
			sql += " AND t2.Status=1";
			//sql += " AND t7.ID IS NOT NULL "; 
			sql += " ORDER BY t2.ID";
			//sql += " LIMIT 5";
		}
		*/
		if(sql != null){ 
			
			ObjectCollection results2 = new ObjectCollection("Results", "*");
			
			ocs.logAccess(sql);
			
			ocs.queryData(sql, results);
			
			int i = 0;
			int cnt = 0;
			

			
				for(BasicClass record : results.getObjects()){
					
					i++;
					
					String maid = record.getString("ID");
					
					

					
					String datefirstreminder = "";
					String datesecondreminder = "";
					

					results2.removeObjects();					
					sql = " SELECT t1.* From Activity AS t1 JOIN ActivityObject AS t2 ON t2.ActivityID=t1.ID AND t1.Template IN (4,5) AND t1.Context=6 JOIN MemberAd AS t3 ON t2.MemberAdID=t3.ID WHERE t3.ValidUntil < t1.DateCreated AND t2.MemberAdID=" + maid;
					ocs.queryData(sql, results2);
					
					if(results2.getObjects().size() > 0){
						BasicClass record2 = (BasicClass)results2.getObjects().get(0);
						datefirstreminder = record2.getString("DATECREATED");
					}
					
					results2.removeObjects();					
					sql = " SELECT t1.* From Activity AS t1 JOIN ActivityObject AS t2 ON t2.ActivityID=t1.ID AND t1.Template IN (4,5,8) AND t1.Context=7 JOIN MemberAd AS t3 ON t2.MemberAdID=t3.ID WHERE t3.ValidUntil < t1.DateCreated AND t2.MemberAdID=" + maid;
					ocs.queryData(sql, results2);
					
					if(results2.getObjects().size() > 0){
						BasicClass record2 = (BasicClass)results2.getObjects().get(0);
						datesecondreminder = record2.getString("DATECREATED");
					}
					
					MemberAdRenewalNode marn = new MemberAdRenewalNode();
					
					String email = record.getString("EMAIL");
					marn.setProperty("Email", email);
					
					if(getID("Mode")==1){
						if(datefirstreminder.trim().length()==0 && datesecondreminder.trim().length()==0 && email.trim().length() > 0){
							addNode(marn);
							
							cnt++;
							if(cnt==1){
								setCurrentNode(marn);	
							}
						}
					}
					else if(getID("Mode")==2){
						java.util.Date date = DateConverter.sqlToDate(datefirstreminder);
						java.util.Date now = new java.util.Date();
						if((date  != null && ((now.getTime() - date.getTime()) / (1000 * 60 * 60 * 24) > 30 )|| email.trim().length() == 0) && datesecondreminder.trim().length()==0 ){
							
							addNode(marn);
							cnt++;
							if(cnt==1){
								setCurrentNode(marn);	
							}

						}
					}
					else if(getID("Mode")==3){
						java.util.Date date = DateConverter.sqlToDate(datesecondreminder);
						java.util.Date now = new java.util.Date();
						if((date  != null && ((now.getTime() - date.getTime()) / (1000 * 60 * 60 * 24) > 29 )) ){
							
							addNode(marn);
							cnt++;
							if(cnt==1){
								setCurrentNode(marn);	
							}

						}
					}
						

		
					
					
					
					marn.setProperty("MAID", record.getString("ID"));
					
					marn.setProperty("Number", cnt + "/");
					
					marn.setProperty("OMID", record.getString("OMID") + ";" + record.getString("FIRSTNAME") + " " + record.getString("FAMILYNAME"));
					
					marn.setProperty("MemberName", record.getString("FIRSTNAME") + " " + record.getString("FAMILYNAME"));
		
					marn.setProperty("DateReminder", record.getString("DateReminder"));
					marn.setProperty("ValidFrom", record.getString("VALIDFROM"));
					marn.setProperty("ValidUntil", record.getString("VALIDUNTIL"));
					
					marn.setProperty("Title", record.getString("TITLE"));
					marn.setProperty("Description", record.getString("DESCRIPTION"));
					

					
					marn.setProperty("FirstReminder", datefirstreminder);
					
					//if(datefirstreminder.length() > 0){
					
					if(getID("Mode")==1){
						
						marn.getProperty("SendFirstReminder").setEditable(true);
						marn.getProperty("SendSecondReminder").setEditable(false);
						
					}
					else if(getID("Mode")==2){
						
						marn.getProperty("SendFirstReminder").setEditable(false);
						marn.getProperty("SendSecondReminder").setEditable(true);
						
					}
					else if(getID("Mode")==3){
						
						marn.getProperty("SendFirstReminder").setEditable(false);
						marn.getProperty("SendSecondReminder").setEditable(false);
						
					}
					marn.setProperty("SecondReminder", datesecondreminder);
					
					String sex = record.getString("SEX");
					String addressation = "";
					if(sex.equals("1")){
						addressation = "Sehr geehrter Herr " + record.getString("FAMILYNAME");
					}
					else if(sex.equals("2")){
						addressation = "Sehr geehrte Frau " + record.getString("FAMILYNAME");
					}
					marn.setProperty("Addressation", addressation);
					
					
					

					

					
					
				}
			if(cnt == 0){
				node1 = addNode();
				node1.setComment("Keine Inserate gefunden");
				setCurrentNode(node1);
			}
			else{
				for(BasicClass node : getObjects("BasicProcessNode")){
					node.setProperty("Number", node.getString("Number") + cnt);	
				}
			}
		
		}
		templib = (PDFTemplateLibrary)ocs.getObjectByName("PDFTemplateLibrary", "1");
	}
	public void finish(ProcessResult result, ApplicationContext context){
	
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		
		Hashtable firstReminders = new Hashtable();
		Hashtable secondReminders = new Hashtable();
		Hashtable nodes = new Hashtable();
		
		for(BasicClass  node : getObjects("BasicProcessNode")){
			
			if(node instanceof MemberAdRenewalNode){
				
				MemberAdRenewalNode marn = (MemberAdRenewalNode)node;
				
				String omid = marn.getString("OMID");
				String maid = marn.getString("MAID");
				
				if(marn.getBoolean("DeleteAd")){
					
					ocs.executeCommand("UPDATE MemberAd SET Status=3 WHERE ID=" + maid);
				}
				else if(marn.getBoolean("SendFirstReminder")){
					
					if(marn.getString("Email").length() > 0){
					
						Vector<String> maids = (Vector<String>)firstReminders.get(omid);
						
						
						
						if(maids==null){
							maids = new Vector<String>();
							firstReminders.put(omid, maids);
						}
						
						if(maids.indexOf(maid)==-1){
							maids.add(maid);	
						}
						
						nodes.put(omid, marn);
						
					}
					
				}
				else if(marn.getBoolean("SendSecondReminder")){
					
						Vector<String> maids = (Vector<String>)secondReminders.get(omid);
						
						
						
						if(maids==null){
							maids = new Vector<String>();
							secondReminders.put(omid, maids);
						}
						
						if(maids.indexOf(maid)==-1){
							maids.add(maid);	
						}
						
						nodes.put(omid, marn);
					
					
				}
				
				
				
				
			}
			
			
		}
		
		
		
		for(Object key : firstReminders.keySet()){
			
			String omid = (String)key;
			
			String mailcontent = ocs.getTextblockContent("8", true);
			

			
			
			
			Vector<String> maids = (Vector<String>)firstReminders.get(omid);
			if(maids != null && maids.size() > 0){
				
				String args[] = omid.split(";");
				
				MemberAdRenewalNode marn = (MemberAdRenewalNode)nodes.get(omid);
				
				String omid2 = args[0];
				
				//String registrationcode = ocs.createPassword(20);
				
				//String link = "\n\n" + ocs.getString("hostname") + "/servlet.srv?action=confirmregistration&registrationcode=" + registrationcode;
				
				String code = ocs.createLoginCode(omid2, context);
								
				String link = "\n\n" + ocs.getString("hostname") + "/servlet.srv?action=onetimelogin&redirect=profile&sectionid=inserate&code=" + code;
			
				mailcontent = mailcontent.replace("<@addressation>", marn.getString("Addressation"));
			
				mailcontent = mailcontent.replace("<@link>", link);
				
				mailcontent = mailcontent.replace("<br />", "");
				
				
				
				//ocs.executeCommand("UPDATE Login SET RegistrationCode=\'" + registrationcode + "\' WHERE OrganisationMemberID=" + omid2);
				

				
				String activityid = ocs.createActivity("4", omid2, "Anfrage zur Inserateverlängerung", context, 6, "21", mailcontent);
				
				for(String maid : maids){
					
					ActivityObject ao = new ActivityObject();
					ao.addProperty("ActivityID", "String", activityid);
					ao.setProperty("MemberAdID" , maid);
					ocs.insertObject(ao);
					
				}
				
				
			}
			
			
		}
		if(secondReminders.keySet().size() > 0){
			
			String batchid = null;
			BatchActivity ba = (BatchActivity)ocs.createObject("ch.opencommunity.base.BatchActivity", null, context);	
			ba.setProperty("Context", 7);
			ba.setProperty("TextBlockID", "18");
			ba.setProperty("Title", "Schriftliche Anfrage zur Inserateverlängerung");
			ba.setProperty("DateCreated", DateConverter.dateToSQL(new java.util.Date(), true));
			//String xml = getXMLString(false);
			//ba.setProperty("Parameters", xml);
			batchid = ocs.insertObject(ba);
			
			String content = null;
			String subject = null;
			
			String content_alt = null;
			
			TextBlock tb = ocs.getTextblock("18");
			if(tb != null){
				content = tb.getString("Content");
				content = BatchActivityCreate.cleanContent(content);
				subject = tb.getString("Subject");
			}
			tb = ocs.getTextblock("26"); //Benutzer ohne Email
			if(tb != null){
				content_alt = tb.getString("Content");
				content_alt = BatchActivityCreate.cleanContent(content_alt);
			}
			
			
			for(Object key : secondReminders.keySet()){
				
				String omid = (String)key;			
				
				Vector<String> maids = (Vector<String>)secondReminders.get(omid);
				
				if(maids != null && maids.size() > 0){
					
					String args[] = omid.split(";");
					
					MemberAdRenewalNode marn = (MemberAdRenewalNode)nodes.get(omid);
					
					String omid2 = args[0];
					
					/*
					
					String registrationcode = ocs.createPassword(20);
					
					String link = "\n\n" + ocs.getString("hostname") + "/servlet.srv?action=confirmregistration&registrationcode=" + registrationcode;
				
					mailcontent = mailcontent.replace("<@addressation>", marn.getString("Addressation"));
				
					mailcontent = mailcontent.replace("<@link>", link);
					
					mailcontent = mailcontent.replace("<br />", "");
						
					ocs.executeCommand("UPDATE Login SET RegistrationCode=\'" + registrationcode + "\' WHERE OrganisationMemberID=" + omid2);
					
					*/
					
					String activityid = ocs.createActivity("8", omid2, "Anfrage zur Inserateverlängerung", context, 7, "27", content, batchid, 1);
					
					for(String maid : maids){
						
						ActivityObject ao = new ActivityObject();
						ao.addProperty("ActivityID", "String", activityid);
						ao.setProperty("MemberAdID" , maid);
						ocs.insertObject(ao);
						
					}
					
					
				}
				
				
			}
			
			String sql = "SELECT t1.ID, t2.ID AS ActivityID from organisationmember as t1 join activity as t2 on t2.organisationmemberid=t1.id and t2.batchactivityid=" + batchid; 
			
			ObjectCollection results = new ObjectCollection("Results", "*");
			
			BatchActivityCreate.prepareLetters(ocs, sql, results, content, subject, content_alt);

			String filename = ocs.createPassword(8);
			
			String path = ocs.getRootpath() + "/temp/" + filename + ".pdf";
			
			PDFWriter pdfWriter = new PDFWriter();
			
			pdfWriter.createPDF(path, templib, "2", results.getObjects());
			
			result.setParam("download", "/temp/" + filename + ".pdf");
		}
		ocs.sendAllPendingMails();
		
	}
	class MemberAdRenewalNode extends BatchProcessNode{
		
		public MemberAdRenewalNode(){
			
			Property p = addProperty("MAID", "String", "", true, "Inseraten-ID");
			
			p = addProperty("Number", "String", "", false, " ");
			p.setEditable(false);
			
			addProperty("OMID", "String", "", false, "Eigentümer");
			
			p = addProperty("MemberName", "String", "", false, "Name");
			p.setEditable(false);
			
			p = addProperty("Addressation", "String", "", false, "Anrede");
			p.setEditable(false);
			
			p = addProperty("Email", "String", "", false, "Email");
			p.setEditable(false);
			
			p = addProperty("Title", "String", "", false, "Bezeichung");
			p.setEditable(false);
			
			p = addProperty("Description", "Text", "", false, "Beschreibung");
			p.setEditable(false);
			
			p = addProperty("ValidFrom", "String", "", false, "Gültig von");
			p.setEditable(false);
			
			p = addProperty("ValidUntil", "String", "", false, "Gültig bis");
			p.setEditable(false);
			
			p = addProperty("DateReminder", "String", "", false, "Mahndatum");
			p.setEditable(false);
			
			p = addProperty("FirstReminder", "String", "", false, "1. Mahnung gesendet");
			p.setEditable(false);
			
			p = addProperty("SecondReminder", "String", "", false, "2. Mahnung gesendet");
			p.setEditable(false);
			
			addProperty("SendFirstReminder", "Boolean", "false", false, "1. Mahnung senden");
			p = addProperty("SendSecondReminder", "Boolean", "false", false, "2. Mahnung senden");
			p.setEditable(false);
			
			addProperty("DeleteAd", "Boolean", "false", false, "Inserat löschen");
			
			addButtonDefinition("Vorz. Abschliessen", "cancelProcess(\'finish\')");
			
		}
		public boolean validate(ApplicationContext context){

			if(context.hasProperty("OMID") && context.getString("OMID").length() > 0){
				
				OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
				
				Hashtable params = new Hashtable();
				params.put("OMID", context.getString("OMID"));
				BasicProcess subprocess = ocs.createProcess("ch.opencommunity.process.OrganisationMemberEdit", this, params, context);
				subprocess.setParent(getProcess());
				subprocess.setStatus("started");

				getProcess().setSubprocess(subprocess);
				
				return false;
			}
			else{
				saveProperties(context);
				return true;
			}
		}
		
		
	}
	
}