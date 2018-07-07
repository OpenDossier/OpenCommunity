 package ch.opencommunity.process;
 
 import ch.opencommunity.server.*;
 import ch.opencommunity.base.*;
 
 import org.kubiki.ide.*;
 import org.kubiki.base.Property;
 import org.kubiki.base.ProcessResult;
 import org.kubiki.base.BasicClass;
 import org.kubiki.base.ObjectCollection;
 
 import org.kubiki.application.*;
import org.kubiki.application.server.WebApplicationContext;
 
 import java.util.Vector;
 
 import javax.servlet.http.*;
 
 
 public class SendRequestConfirmation extends BasicProcess{
 	 
 	 BasicProcessNode node1;
 	 ObjectCollection recipients;
 	 
 	 public SendRequestConfirmation(){ 	 
 	 	 node1 = addNode();
 	 	 setCurrentNode(node1);
 	 	 recipients = addObjectCollection("Recipients", "*");

 	 	 Property p = addProperty("Mode", "Integer", "");
 	 	 //node1.addProperty(p);
 	 	 
 	 	 p = addProperty("NumRecipients", "Integer", "");
 	 	 node1.addProperty(p);
 	 	 
 	 	 p = addProperty("RecipientList", "ObjectList", "");
 	 	 node1.addProperty(p);
 	 	 
 	 	 p = addProperty("Content", "Text", "");
 	 	 node1.addProperty(p);
 	 	 
 	 	 p = addProperty("Content2", "Text", "");
 	 	 //node1.addProperty(p);
 	 }
 	 public void initProcess(){
 	 	 OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
 	 	 
 	 	 String sql = "";
 	 	 if(getID("Mode")==1){
 	 	 	 
			 sql = "SELECT DISTINCT t2.ID, t4.FamilyName, t4.FirstName, t5.Value, t1.NotificationMode FROM MemberAd AS t1";
			 sql += " INNER JOIN OrganisationMember AS t2 ON t1.OrganisationMemberID=t2.ID";
			 sql += " LEFT JOIN Person AS t3 ON t2.Person=t3.ID";
			 sql += " LEFT JOIN Identity AS t4 ON t4.PersonID=t3.ID";
			 sql += " LEFT JOIN Contact AS t5 ON t5.PersonID=t3.ID AND t5.Type=3";
			 sql += " WHERE t1.Status=1 AND t1.NotificationStatus=0";
			 ocs.queryData(sql, recipients);
			 setProperty("NumRecipients", "" + recipients.getObjects().size());
			 
			 setProperty("Content", "Ihr(e) Inserat(e) wurden freigeschaltet"); 	 	 
 	 	 	 
 	 	 }
 	 	 else if(getID("Mode")==2){
			 sql = "SELECT DISTINCT t2.ID, t4.FamilyName, t4.FirstName, t5.Value, t1.NotificationMode FROM MemberAdRequest AS t1";
			 sql += " INNER JOIN OrganisationMember AS t2 ON t1.OrganisationMemberID=t2.ID";
			 sql += " LEFT JOIN Person AS t3 ON t2.Person=t3.ID";
			 sql += " LEFT JOIN Identity AS t4 ON t4.PersonID=t3.ID";
			 sql += " LEFT JOIN Contact AS t5 ON t5.PersonID=t3.ID AND t5.Type=3";
			 /*
			 sql += " LEFT JOIN MemberAd AS t6 ON t1.MemberAd=t6.ID";
			 sql += " LEFT JOIN OrganisationMember AS t7 ON t6.OrganisationMemberID=t7.ID";
			 sql += " LEFT JOIN Person AS t8 ON t7.Person=t8.ID";
			 sql += " LEFT JOIN Contact AS t9 ON t9.PersonID=t8.ID AND t9.Type=3";
			 */
			 sql += " WHERE t1.Status=1 AND t1.NotificationStatus=0";
			 
			 ocs.queryData(sql, recipients);
			 setProperty("NumRecipients", "" + recipients.getObjects().size());
			 
			 setProperty("Content", "Ihre bestellten Adressen wurden freigeschaltet und sind jetzt in ihrem Benutzerprofil einsehbar");
			 setProperty("Content2", "Wir haben Ihre Adresse vermittelt. In nächster Zeit könngte sich jemand bei Ihnen melden, der sich in seinem Profile folgendermassen charakterisiert: ");
			 
		 }
 	 	 else if(getID("Mode")==3){ //Inseratverlängerung
 	 	 	 
			 sql = "SELECT DISTINCT t2.ID, t4.FamilyName, t4.FirstName, t5.Value, t1.NotificationMode FROM MemberAd AS t1";
			 sql += " INNER JOIN OrganisationMember AS t2 ON t1.OrganisationMemberID=t2.ID";
			 sql += " LEFT JOIN Person AS t3 ON t2.Person=t3.ID";
			 sql += " LEFT JOIN Identity AS t4 ON t4.PersonID=t3.ID";
			 sql += " LEFT JOIN Contact AS t5 ON t5.PersonID=t3.ID AND t5.Type=3";
			 sql += " LEFT JOIN Activity AS t6 ON t2.ID=t6.OrganisationMemberID AND t6.Template IN (4,5) AND t6.Context=6 AND  t6.DateCreated < NOW() +  interval '30 days'";
			 sql += " WHERE t1.ValidUntil < NOW() +  interval '30 days'";
			 sql += " AND t1.Status=1";
			 sql += " AND t6.ID IS NULL";
			 
			 sql = "SELECT DISTINCT t2.ID, t4.FamilyName, t4.FirstName, t5.Value, t1.NotificationMode, t6.Context AS FIRSTREMINDER, t6.Context AS SECONDREMINDER FROM MemberAd AS t1";
			 sql += " INNER JOIN OrganisationMember AS t2 ON t1.OrganisationMemberID=t2.ID";
			 sql += " LEFT JOIN Person AS t3 ON t2.Person=t3.ID";
			 sql += " LEFT JOIN Identity AS t4 ON t4.PersonID=t3.ID";
			 sql += " LEFT JOIN Contact AS t5 ON t5.PersonID=t3.ID AND t5.Type=3";
			 sql += " LEFT JOIN Activity AS t6 ON t2.ID=t6.OrganisationMemberID AND t6.Template IN (4,5) AND t6.Context=6";
			 sql += " LEFT JOIN Activity AS t7 ON t2.ID=t7.OrganisationMemberID AND t7.Template IN (4,5) AND t7.Context=7";
			 sql += " WHERE (t1.DateReminder < NOW() OR t1.ValidUntil < NOW())";
			 sql += " AND (t6.ID IS NULL OR t7.ID IS NULL)  AND t1.Status=1";
			 
			 ocs.queryData(sql, recipients);
			 setProperty("NumRecipients", "" + recipients.getObjects().size());
			 String content = ocs.getTextblockContent("8");
			 setProperty("Content", content); 	 	 	 
 	 	 	 
 	 	 }
 	 }
	public void finish(ProcessResult result, ApplicationContext context){
	
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		
		HttpServletRequest request = ((WebApplicationContext)context).getRequest();
		
		ObjectTemplate ot1 = ocs.getObjectTemplate("4");
		ObjectTemplate ot2 = ocs.getObjectTemplate("5");
		
		if(getID("Mode")==1){
			
			recipients.removeObjects();
			String sql = "SELECT DISTINCT t1.ID AS MemberAdID, t1.NotificationMode, t2.ID, t4.FamilyName, t4.FirstName, t5.Value, t1.NotificationMode FROM MemberAd AS t1";
			sql += " INNER JOIN OrganisationMember AS t2 ON t1.OrganisationMemberID=t2.ID";
			sql += " LEFT JOIN Person AS t3 ON t2.Person=t3.ID";
			sql += " LEFT JOIN Identity AS t4 ON t4.PersonID=t3.ID";
				 sql += " LEFT JOIN Contact AS t5 ON t5.PersonID=t3.ID AND t5.Type=3 AND t5.Status=0";
				 sql += " WHERE t1.Status=1 AND NotificationStatus=0";
				 sql += " ORDER BY t2.ID, t1.NotificationMode";
				 ocs.queryData(sql, recipients);
	
				String previd = "";
				String activityid = null;
				for(BasicClass bc : recipients.getObjects()){
					
					
				String memberid = bc.getString("ID");
					
				if(!memberid.equals(previd)){
					
					String registrationcode = ocs.createPassword(20);
					
						Activity activity = (Activity)ot1.createObject("ch.opencommunity.base.Activity", null, context);
						activity.applyTemplate(ot1);
						
						activity.setProperty("Status", "0");
						activity.addProperty("OrganisationMemberID", "String", bc.getString("ID"));
						BasicClass note = activity.getFieldByTemplate("21");
						
						String content = getString("Content");
						
						content += "\n\n" + ocs.getBaseURL(request) + "/opencommunity/servlet.srv?action=confirmregistration&registrationcode=" + registrationcode;
						
						note.setProperty("Content", content);
						
						activity.setProperty("Title", "Benachrichtigung");
						activityid = ocs.insertObject(activity, true);
						
						ocs.executeCommand("UPDATE Login SET RegistrationCode=\'" + registrationcode + "\' WHERE OrganisationMemberID=" + bc.getString("ID"));
						
				}
					
				ActivityObject ao = (ActivityObject)ot1.createObject("ch.opencommunity.base.ActivityObject", null, context);
				ao.addProperty("ActivityID", "String", activityid);
				ao.setProperty("MemberAdID", bc.getString("MEMBERADID"));
				ocs.insertObject(ao);
					
				ocs.executeCommand("UPDATE MemberAd SET NotificationStatus=1 WHERE ID=" + bc.getString("MEMBERADID"));
					
				previd = memberid;
				
				
			}
			ocs.sendAllPendingMails();
		}
		else if(getID("Mode")==2){
			

			
			if(ot1 != null){
				
				recipients.removeObjects();
				 //String  sql = "SELECT DISTINCT t2.ID, t7.ID AS ID2, t4.FamilyName, t4.FirstName, t5.Value, t3.Comment , t9.Value AS EmailOwner, t1.NotificationMode, t10.Protected FROM MemberAdRequest AS t1";
				 String  sql = "SELECT DISTINCT t1.ID AS RequestID, t2.ID, t4.FamilyName, t4.FirstName, t5.Value, t1.NotificationMode FROM MemberAdRequest AS t1";
				 sql += " INNER JOIN OrganisationMember AS t2 ON t1.OrganisationMemberID=t2.ID";
				 sql += " LEFT JOIN Person AS t3 ON t2.Person=t3.ID";
				 sql += " LEFT JOIN Identity AS t4 ON t4.PersonID=t3.ID";
				 sql += " LEFT JOIN Contact AS t5 ON t5.PersonID=t3.ID AND t5.Type=3";
				 /*
				 sql += " LEFT JOIN MemberAd AS t6 ON t1.MemberAd=t6.ID";
				 sql += " LEFT JOIN OrganisationMember AS t7 ON t6.OrganisationMemberID=t7.ID";
				 sql += " LEFT JOIN Person AS t8 ON t7.Person=t8.ID";
				 sql += " LEFT JOIN Contact AS t9 ON t9.PersonID=t8.ID AND t9.Type=3";
				 sql += " LEFT JOIN MemberAdCategory AS t10 ON t6.Template=t10.ID";
				 */
				 sql += " WHERE t1.Status=1 AND t1.NotificationStatus=0";
				 sql += " ORDER BY t2.ID, t1.NotificationMode";
				 ocs.queryData(sql, recipients);
	
				String previd = "";
				String activityid = null;
				String notificationmode = "";
				for(BasicClass bc : recipients.getObjects()){
					
					notificationmode = bc.getString("NOTIFICATIONMODE");
					
					String memberid = bc.getString("ID") + "_" + notificationmode;
					
					String memberid2 = bc.getString("ID");
					
					if(!memberid.equals(previd)){
						
						if(notificationmode.equals("1")){
						
							String registrationcode = ocs.createPassword(20);
						
							Activity activity = (Activity)ot1.createObject("ch.opencommunity.base.Activity", null, context);
							activity.applyTemplate(ot1);
							activity.setProperty("Status", "0");
							activity.addProperty("OrganisationMemberID", "String", bc.getString("ID"));
							BasicClass note = activity.getFieldByTemplate("21");
							
							String content = getString("Content");
							
							content += "\n\n" + ocs.getBaseURL(request) + "/opencommunity/servlet.srv?action=confirmregistration&registrationcode=" + registrationcode;
							
							note.setProperty("Content", content);
							activity.setProperty("Title", "Benachrichtigung");
							activityid = ocs.insertObject(activity, true);
							
							ocs.executeCommand("UPDATE Login SET RegistrationCode=\'" + registrationcode + "\' WHERE OrganisationMemberID=" + bc.getString("ID"));
							
							//---------------------------------------------------neuer Modus: der Inserateinhaber wird auch informiert--------------------------------------
							

							
						}
						else{
							
							Document document = (Document)ot1.createObject("ch.opencommunity.base.Document", null, context);
							document.setProperty("Recipient", memberid2);
							document.setProperty("Template", "4");
							document.setProperty("WordModules", "/WebApplication/DocumentTemplateLibrary:1/DocumentTemplate:4/DocumentTemplateModule:4");
							String docid = ocs.insertObject(document);

							
							Activity activity = (Activity)ot1.createObject("ch.opencommunity.base.Activity", null, context);
							activity.applyTemplate(ot2);
							activity.setProperty("Status", "0");
							activity.addProperty("OrganisationMemberID", "String", bc.getString("ID"));
							
							BasicClass parameter = activity.getFieldByTemplate("23");
							parameter.setProperty("Document", docid);
							
							BasicClass note = activity.getFieldByTemplate("22");
							
							String content = getString("Content");
							
							note.setProperty("Content", content);
							activity.setProperty("Title", "Benachrichtigung");
							activityid = ocs.insertObject(activity, true);
							
						}
						
					}
					
					ActivityObject ao = (ActivityObject)ot1.createObject("ch.opencommunity.base.ActivityObject", null, context);
					ao.addProperty("ActivityID", "String", activityid);
					ao.setProperty("MemberAdRequestID", bc.getString("REQUESTID"));
					ocs.insertObject(ao);
					
					ocs.executeCommand("UPDATE MemberAdRequest SET NotificationStatus=1 WHERE ID=" + bc.getString("REQUESTID"));
					
					String registrationcode = ocs.createPassword(20);
					
					
					previd = memberid;
				}
			}
			
			ocs.sendAllPendingMails();
			
			result.setParam("refresh", "currentsection");
		}
		else if(getID("Mode")==3){
			
			 // welche Benutzer haben Inserate die auslaufen / ausgelaufen sind und noch auf status 1?
			 
			 //1. Mahnung
			
			 String sql = "SELECT DISTINCT t2.ID, t4.FamilyName, t4.FirstName, t5.Value, t1.NotificationMode, t6.Context AS FIRSTREMINDER, t6.Context AS SECONDREMINDER FROM MemberAd AS t1";
			 sql += " INNER JOIN OrganisationMember AS t2 ON t1.OrganisationMemberID=t2.ID";
			 sql += " LEFT JOIN Person AS t3 ON t2.Person=t3.ID";
			 sql += " LEFT JOIN Identity AS t4 ON t4.PersonID=t3.ID";
			 sql += " LEFT JOIN Contact AS t5 ON t5.PersonID=t3.ID AND t5.Type=3";
			 sql += " LEFT JOIN Activity AS t6 ON t2.ID=t6.OrganisationMemberID AND t6.Template IN (4,5) AND t6.Context=6";
			 //sql += " JOIN Activity AS t7 ON t2.ID=t7.OrganisationMemberID AND t7.Template IN (4,5) AND t7.Context=7";
			 sql += " WHERE (t1.DateReminder < NOW())";
			 sql += " AND t6.ID IS NULL  AND t1.Status=1";

			 //sql += " AND trim(t5.Value)='kofler@oxinia.ch'";
			 
			 recipients.removeObjects();
			 ocs.logAccess(sql);
			 ocs.queryData(sql, recipients);	
			 
			 for(BasicClass bc : recipients.getObjects()){
			 	 
			 	 String omid = bc.getString("ID");
			 	 
				 sql = "SELECT t1.* FROM MemberAd AS t1";
				 //sql += " LEFT JOIN ActivityObject AS t2 ON t2.MemberAdID=t1.ID";
				 //sql += " LEFT JOIN Activity AS t3 ON t2.ActivityID=t3.ID AND t3.Context=6";
				 sql += " WHERE t1.OrganisationMemberID=" + omid + " AND t1.DateReminder < NOW()";
				 //sql += " AND t3.ID IS NULL";
				 ObjectCollection results2 = new ObjectCollection("Results2", "*");
				 ocs.queryData(sql, results2);	
				 
				 ocs.logAccess(sql);
				 
				 
				 
				 if(results2.getObjects().size() > 0){
				 	 
				 	 String registrationcode = ocs.createPassword(20);
				 	 
					 Activity activity = (Activity)ot1.createObject("ch.opencommunity.base.Activity", null, context);
					 activity.applyTemplate(ot1);
					 activity.setProperty("Status", "0");
					 activity.setProperty("Context", "6");
					 activity.addProperty("OrganisationMemberID", "String", bc.getString("ID"));
					 BasicClass note = activity.getFieldByTemplate("21");
								
					 String content = getString("Content");
				 
					 content += "\n\n" + ocs.getBaseURL(request) + "/opencommunity/servlet.srv?action=confirmregistration&registrationcode=" + registrationcode;
					 
					 note.setProperty("Content", content);
					 activity.setProperty("Title", "Aufforderung zur Verlängerung");
					 String activityid = ocs.insertObject(activity, true);
					 
					 ocs.executeCommand("UPDATE Login SET RegistrationCode=\'" + registrationcode + "\' WHERE OrganisationMemberID=" + omid);
					 
					 
					 for(BasicClass bc2 : results2.getObjects()){

					 	 ActivityObject ao = (ActivityObject)ot1.createObject("ch.opencommunity.base.ActivityObject", null, context);
					 	 ao.addProperty("ActivityID", "String", activityid);
					 	 ao.setProperty("MemberAdID", bc2.getString("ID"));
					 	 ocs.insertObject(ao);

					 }
								

					 
				 }
				 
				 
				 
			 }
			 
			 // das genze wiederholen, diesmal für 2. Mahnung PDF
			
			 sql = "SELECT DISTINCT t2.ID, t4.FamilyName, t4.FirstName, t5.Value, t1.NotificationMode, t6.Context AS FIRSTREMINDER, t6.Context AS SECONDREMINDER FROM MemberAd AS t1";
			 sql += " INNER JOIN OrganisationMember AS t2 ON t1.OrganisationMemberID=t2.ID";
			 sql += " LEFT JOIN Person AS t3 ON t2.Person=t3.ID";
			 sql += " LEFT JOIN Identity AS t4 ON t4.PersonID=t3.ID";
			 sql += " LEFT JOIN Contact AS t5 ON t5.PersonID=t3.ID AND t5.Type=3";
			 sql += " JOIN Activity AS t6 ON t2.ID=t6.OrganisationMemberID AND t6.Template IN (4,5) AND t6.Context=6"; //Eine erste Mahnung muss existieren
			 sql += " LEFT JOIN Activity AS t7 ON t2.ID=t7.OrganisationMemberID AND t7.Template IN (4,5) AND t7.Context=7";
			 sql += " WHERE (t1.ValidUntil < NOW())";
			 sql += " AND t7.ID IS NULL  AND t1.Status=1";
			 
			 Vector letters = new Vector();

			 
			 recipients.removeObjects();
			 ocs.logAccess(sql);
			 ocs.queryData(sql, recipients);	
			 
			 for(BasicClass bc : recipients.getObjects()){
			 	 
			 	 String omid = bc.getString("ID");
			 	 
				 sql = "SELECT t1.*, t3.ID AS ActivityID FROM MemberAd AS t1";
				 sql += " LEFT JOIN ActivityObject AS t2 ON t2.MemberAdID=t1.ID";
				 sql += " LEFT JOIN Activity AS t3 ON t2.ActivityID=t3.ID AND t3.Context=7";
				 sql += " WHERE t1.OrganisationMemberID=" + omid + " AND t1.ValidUntil < NOW() +  interval '30 days'";
				 sql += " AND t3.ID IS NULL";
				 ObjectCollection results2 = new ObjectCollection("Results2", "*");
				 ocs.queryData(sql, results2);	
				 
				 ocs.logAccess(sql);
				 
				 
				 
				 if(results2.getObjects().size() > 0){
				 	 
				 	 String registrationcode = ocs.createPassword(20);
				 	 
					 Activity activity = (Activity)ot1.createObject("ch.opencommunity.base.Activity", null, context);
					 activity.applyTemplate(ot2);
					 activity.setProperty("Status", "0");
					 activity.setProperty("Context", "7");
					 activity.addProperty("OrganisationMemberID", "String", bc.getString("ID"));
					 BasicClass note = activity.getFieldByTemplate("22");
								
					 String content = getString("Content");
				 
					 content += "\n\n" + ocs.getBaseURL(request) + "/opencommunity/servlet.srv?action=confirmregistration&registrationcode=" + registrationcode;
					 
					 note.setProperty("Content", content);
					 activity.setProperty("Title", "Aufforderung zur Verlängerung");
					 String activityid = ocs.insertObject(activity, true);
					 activity.setName(activityid);
					 letters.add(activity);
					 
					 //ocs.executeCommand("UPDATE Login SET RegistrationCode=\'" + registrationcode + "\' WHERE OrganisationMemberID=" + omid);
					 
					 
					 for(BasicClass bc2 : results2.getObjects()){

					 	 ActivityObject ao = (ActivityObject)ot1.createObject("ch.opencommunity.base.ActivityObject", null, context);
					 	 ao.addProperty("ActivityID", "String", activityid);
					 	 ao.setProperty("MemberAdID", bc2.getString("ID"));
					 	 ocs.insertObject(ao);

					 }
								

					 
				 }
				 
				 
				 
			 }
			 
			 if(letters.size() > 0){
			 	 String filename = ocs.createPDF(letters);
			 	 result.setParam("download", filename);
			 }
			
			 ocs.sendAllPendingMails();
			
			 result.setParam("refresh", "currentsection");
			
		}
		
		
	}
 	 
 }