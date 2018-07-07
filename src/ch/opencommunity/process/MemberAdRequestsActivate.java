package ch.opencommunity.process;
 
import ch.opencommunity.server.*;
import ch.opencommunity.base.*;
import ch.opencommunity.advertising.*;
import ch.opencommunity.common.*;
 
import org.kubiki.ide.*;
import org.kubiki.base.Property;
import org.kubiki.base.BasicClass;
import org.kubiki.base.ObjectCollection;
import org.kubiki.base.ProcessResult;
import org.kubiki.base.ConfigValue;
 
import org.kubiki.application.*;
import org.kubiki.application.server.WebApplicationContext;
 
import java.util.Vector;
import java.sql.*;
 
import javax.servlet.http.*;
 
 
public class MemberAdRequestsActivate extends BasicProcess{
	
	OpenCommunityServer ocs;
	OrganisationMember om;
	String ownerComment;
	
	BasicProcessNode lastNode; 
	
	ObjectCollection results; 
	
	public  MemberAdRequestsActivate(){
		
		addProperty("omid", "String", "");
		addProperty("sectionid", "String", "");
		addProperty("NotificationMode", "Integer", "1");
		
		addProperty("MailText", "Text", "", false, "Nachricht");
		addProperty("Content", "Text", "", false, "Nachrichtentext");
		
		setProperty("Content", "Ihre bestellten Adressen wurden freigeschaltet und sind jetzt in ihrem Benutzerprofil einsehbar");
		
	}
	public void initProcess(ApplicationContext context){
		
		ocs = (OpenCommunityServer)getRoot();
		
		try{
		
			String comment = ocs.getTextblockContent("4");
			comment = comment.replace("<p>", "\n");
			comment = comment.replace("</p>", "");
			comment = comment.replace("<br />", "");
			comment = comment.trim();
			String comment2 = "";
			String[] lines = comment.split("\r\n|\r|\n");
			for(String line : lines){
				comment2 += "\n" + line.trim();
			}
			setProperty("MailText", comment2);
			
			OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
			//om = userSession.getOrganisationMember();
			om = (OrganisationMember)ocs.getObject(null, "OrganisationMember", "ID", getString("omid"), false);
			om.setParent(this);
			om.initObjectLocal();
			Person person = om.getPerson();
			Identity identity = person.getIdentity();
			
			
			
			//om = (OrganisationMember)ocs.getObject(null, "OrganisationMember", "ID", getString("omid") , false);
			
			String sql = "SELECT t1.Comment, t2.NotificationMode FROM PERSON AS t1, OrganisationMember AS t2 WHERE t2.Person=t1.ID AND t2.ID=" + getString("omid");
			results = new ObjectCollection("Results", "*");		
			ocs.queryData(sql, results);
			for(BasicClass bc : results.getObjects()){
				ownerComment = bc.getString("COMMENT");
				setProperty("NotificationMode", bc.getString("NOTIFICATIONMODE"));
			}
			
			results = addObjectCollection("Results", "*");
			sql = "SELECT t1.*, t2.Title, t3.ID AS OMID, t5.FamilyName, t5.FirstName, t6.Title AS Category, Count(t7.ID) AS CNT FROM MemberAdRequest AS t1 ";
			sql += " LEFT JOIN MemberAd AS t2 ON t1.MemberAd=t2.ID";
			sql += " LEFT JOIN OrganisationMember AS t3 ON t2.OrganisationMemberID=t3.ID";
			sql += " LEFT JOIN Person AS t4 ON t3.Person=t4.ID";
			sql += " LEFT JOIN Identity AS t5 ON t5.PersonID=t4.ID";
			sql += " LEFT JOIN MemberAdCategory AS t6 ON t6.ID=t2.Template";
			sql += " LEFT JOIN MemberAdRequest AS t7 ON t7.MemberAd=t2.ID AND t7.ID != t1.ID AND t7.OrganisationMemberID=t1.OrganisationMemberID";
			sql += " WHERE t1.OrganisationMemberID=" + getString("omid") + " AND t1.Status IN (0, 5)";
			sql += " GROUP BY t1.ID, t2.ID, t2.Title, t3.ID, t5.FamilyName, t5.FirstName, t6.Title";
			ocs.queryData(sql, results);
			
	
			
			int cnt = 0;
			for(BasicClass bc : results.getObjects()){
	
				cnt++;
				
				MemberAd ma = (MemberAd)ocs.getObject(null, "MemberAd", "ID", bc.getString("MEMBERAD"), false);
				MemberAdRequest mar = (MemberAdRequest)ocs.getObject(null, "MemberAdRequest", "ID", bc.getString("ID"), false);
				
				MemberAdRequestNode marn = new MemberAdRequestNode(ma, mar, bc);
				
				addNode(marn);
				
				if(ma != null){
					ma.setParent(this);
					ma.initObjectLocal();
					marn.setProperty("memberadid", ma);
					marn.setProperty("Comment", bc.getString("COMMENT"));
					marn.setProperty("UserComment", bc.getString("USERCOMMENT"));
					marn.setProperty("Familyname", bc.getString("FAMILYNAME"));
					marn.setProperty("Firstname", bc.getString("FIRSTNAME"));
					marn.setProperty("OMID", bc.getString("OMID"));
					
					String owner = identity.getString("FirstName") + " " + identity.getString("FamilyName");
					String number = cnt + "/" + results.getObjects().size();
					
					marn.setProperty("RequestOwner", owner);
					marn.setProperty("Number", number);
					
					marn.setProperty("PreviousRequests", bc.getID("CNT"));
				}
			}
			
			lastNode = addNode();
			lastNode.addProperty("MemberAdRequestList", "Object", "");
			lastNode.addProperty(getProperty("MailText"));
			
		}
		catch(java.lang.Exception e){
			ocs.logException(e);	
		}
		
		setCurrentNode((BasicProcessNode)getObjectByIndex("BasicProcessNode", 0));
	}
	public void finish(ProcessResult result, ApplicationContext context){
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();

		ObjectTemplate ot = ocs.getObjectTemplate("4");
		ObjectTemplate ot2 = ocs.getObjectTemplate("5");
		
		HttpServletRequest request = ((WebApplicationContext)context).getRequest();
		
		
		String registrationcode = ocs.createPassword(20);
						
		Activity activity = (Activity)ot.createObject("ch.opencommunity.base.Activity", null, context);
		activity.applyTemplate(ot);
		activity.setProperty("Status", "0");
		activity.addProperty("OrganisationMemberID", "String", getString("omid"));
		BasicClass note = activity.getFieldByTemplate("21");
							
		String content = getString("MailText");
		

		//String link = "\n\n" + ocs.getBaseURL("", request) + "/servlet.srv?action=confirmregistration&registrationcode=" + registrationcode;
		String link = "\n\n" + ocs.getString("hostname") + "/servlet.srv?action=confirmregistration&registrationcode=" + registrationcode;
		
		content = content.replace("<@addressation>", om.getAddressation());
		

							
		
							

		activity.setProperty("Title", "Benachrichtigung");

		String activityid = null;
		
		for(BasicClass bc : getObjects("BasicProcessNode")){
			
			
			
			if(bc instanceof MemberAdRequestNode){
			
				MemberAdRequestNode node = (MemberAdRequestNode)bc;
				MemberAd ma = node.getMemberAd();
				MemberAdRequest mar = node.getMemberAdRequest();
					
				ocs.executeCommand("UPDATE MemberAdRequest SET NotificationStatus=1 WHERE ID=" + mar.getName());
				
				if(node.getBoolean("Postpone")){
					
					mar.setProperty("Comment", node.getString("Comment"));
					mar.setProperty("Status", 5);
					mar.setProperty("NotificationStatus", 0);
					mar.setProperty("NotificationMode", node.getID("NotificationMode"));
					
					ocs.updateObject(mar);
				}
				else if(node.getBoolean("Delete")){
					
					mar.setProperty("Comment", node.getString("Comment"));
					mar.setProperty("Status", 3);
					mar.setProperty("NotificationStatus", 0);
					mar.setProperty("NotificationMode", node.getID("NotificationMode"));

					
					
					ocs.updateObject(mar);
				}
				else{
				
					try{
						if(activityid == null){ //Es wurde noch keine Aktivität erstellt
							
							String omid = getString("omid");
							
							String code = ocs.createLoginCode(omid, context);
							
							link = "\n\n" + ocs.getString("hostname") + "/servlet.srv?action=onetimelogin&redirect=profile&sectionid=kontakte&code=" + code;
							
							content = content.replace("<@link>", link);
							
							note.setProperty("Content", content);
							
							activityid = ocs.insertObject(activity, true);
												
							ocs.executeCommand("UPDATE Login SET RegistrationCode=\'" + registrationcode + "\' WHERE OrganisationMemberID=" + omid);
							
							
						}
						
						ActivityObject ao = (ActivityObject)ot.createObject("ch.opencommunity.base.ActivityObject", null, context);
						ao.addProperty("ActivityID", "String", activityid);
						ao.setProperty("MemberAdRequestID", bc.getString("REQUESTID"));
						ocs.insertObject(ao);
						
						
						MemberAdCategory mac = ma.getCategory();
						
						if(mac != null){
						
							mar.setProperty("Comment", node.getString("Comment"));
							
							registrationcode = ocs.createPassword(20);
							
							if(mac.getBoolean("Protected")){
								//mar.setProperty("Status", 4);
								mar.setProperty("Status", 1); // AK 20180618 Keine Rückfrage mehr
								mar.setProperty("ActivationCode", registrationcode);
							}
							else{
								mar.setProperty("Status", 1);							
							}
							mar.setProperty("NotificationStatus", 0);
							mar.setProperty("NotificationMode", getID("NotificationMode"));
							
							ocs.updateObject(mar);
							
							//stmt.execute("UPDATE MemberAdRequest SET Status=1, NotificationStatus=0, NotificationMode=" + getID("NotificationMode") + " WHERE ID=" + getString("memberadrequestid"));
							
		
										
							activity = (Activity)ot.createObject("ch.opencommunity.base.Activity", null, context);
							activity.applyTemplate(ot);
							activity.setProperty("Status", "0");
							activity.addProperty("OrganisationMemberID", "String", ma.getString("OrganisationMemberID"));
							note = activity.getFieldByTemplate("21");
											
							//String content = "Ihre Adresse wurde weitergegeben. Sie werden demnächst kontaktiert von einer Person, die sich selber so beschreibt:" + "\n\n" + ownerComment;
							//content = "Ihre Adresse wurde weitergegeben. Sie werden demnächst kontaktiert von einer Person, die sich selber so beschreibt:" + "\n\n" + mar.getString("UserComment");
							
							content = ocs.getTextblockContentByFunction("INFOADDRESSACTIVATE", true);
							
							Person person = om.getPerson();
							Identity identity = person.getIdentity();
							
							String telephoneHome = om.getContactValue(0);
							String telephoneMobile = om.getContactValue(2);
							
							String phone = telephoneMobile;
							if(phone.isEmpty()){
								phone = telephoneHome;	
							}
							
							String syear = identity.getString("DateOfBirth");
							if(syear.length() > 4){
								syear = syear.substring(0,4);	
							}
							int year = Integer.parseInt(syear);
							int age = 2016-year;
							
							String sex = identity.getObject("Sex").toString();
							if(sex.equals("1")){
								sex = "männlich";	
							}
							if(sex.equals("2")){
								sex = "weiblich";	
							}
							String firstname = identity.getString("Firstname");
							
							content = content.replaceAll("<@SEX>", sex);
							content = content.replaceAll("<@AGE>", "" + age);
							content = content.replaceAll("<@FIRSTNAME>", firstname);	
							content = content.replaceAll("<@PHONE>", phone);
							
							//content += "\nDie Person ist " + age + " Jahre alt und " + sex;
							
							
							activity.setProperty("Title", "Ihre Adresse wurde weitergegeben");
							
							if(mac.getBoolean("Protected")){
								

								
								/*
								

								content =  "Ihre Adresse wurde bestellt von einer Person, die sich selber so beschreibt:" + "\n\n" + mar.getString("UserComment");
								content += "\nDie Person ist " + age + " Jahre alt und " + sex;
								
								content += "\n\nWenn Sie einverstanden sind, dass diese Person Ihre Adresse erhält, klicken Sie auf den nachfolgenden Link:";
											
								content += "\n\n" +  ocs.getString("hostname") + "/servlet.srv?action=activaterequest&activationcode=" + registrationcode;
								
								content += "\n\nWenn Sie dies nicht wollen, klicken Sie auf den nachfolgenden Link:";
											
								content += "\n\n" + ocs.getString("hostname") + "/servlet.srv?action=denyrequest&activationcode=" + registrationcode;
								
								
								
								activity.setProperty("Title", "Es gibt Interessenten für Ihr Inserat");
								
								*/
								
							}
											
							note.setProperty("Content", content);
							
							if(mac.getBoolean("Protected")){
								String activityid2 = ocs.insertObject(activity, true);
							}
											
							ocs.executeCommand("UPDATE Login SET RegistrationCode=\'" + registrationcode + "\' WHERE OrganisationMemberID=" + ma.getString("OrganisationMemberID"));
							
						}
					}
					catch(java.lang.Exception e){
						ocs.logException(e);
					}
				}
			}	
		}
		
		ocs.sendAllPendingMails();
		
		result.setParam("refresh", getString("sectionid"));
	}
	class MemberAdRequestNode extends BasicProcessNode{
		
		MemberAd ma;
		MemberAdRequest mar;
		BasicClass record;
		
		public MemberAdRequestNode(MemberAd ma, MemberAdRequest mar, BasicClass record){
			
			this.ma = ma;
			this.mar = mar;
			this.record = record;
			
			addProperty("memberadid", "String", "");
			
			Property p = addProperty("RequestOwner", "String", "", false, "Adressbesteller");
			p.setEditable(false);
			
			p = addProperty("Comment", "Text", "", false, "Kommentar");
			p = addProperty("UserComment", "Text", "", false, "Kommentar Adressbesteller");
			p = addProperty("PreviousRequests", "Integer", "", false, "Bisherige Bestellungen");
			p = addProperty("Delete", "Boolean", "false", false, "Anfrage löschen");
			p = addProperty("Postpone", "Boolean", "false", false, "Später bearbeiten");
			
			addProperty("Familyname", "String", "", true, "");
			addProperty("Firstname", "String", "", true, "");
			addProperty("Number", "String", "", true, "");
			addProperty("OMID", "String", "", true, "");
		}
		public boolean validate(ApplicationContext context){
			if(context.hasProperty("Comment")){
				setProperty("Comment", context.getString("Comment"));	
			}
			if(context.hasProperty("Delete")){
				setProperty("Delete", context.getString("Delete"));	
				if(context.getString("Delete").equals("true")){
					record.setProperty("STATUS", "3");
				}
				else{
					record.setProperty("STATUS", "0");
				}
			}
			if(context.hasProperty("Postpone")){
				setProperty("Postpone", context.getString("Postpone"));	
			}
			else{
				setProperty("Delete", "false");	
				record.setProperty("STATUS", "0");				
			}
			if(context.hasProperty("editmember")){
				OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
				OrganisationMemberEdit ome = (OrganisationMemberEdit)startSubprocess("ch.opencommunity.process.OrganisationMemberEdit");
				//ome.setParent(getParent());
				ome.initProcess(context, getString("OMID"));
				//getProcess().setSubprocess(ome);
				
				ocs.logAccess(ome + ":" + context.getString("OMID"));
				
				return true;
			}
			return true;
		}
		public MemberAd getMemberAd(){
			return ma;
		}
		public MemberAdRequest getMemberAdRequest(){
			return mar;
		}
		
	}
	
	
}