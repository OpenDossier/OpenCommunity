package ch.opencommunity.process;
 
import ch.opencommunity.server.*;
import ch.opencommunity.base.*;
import ch.opencommunity.advertising.*;
import ch.opencommunity.view.*;
import ch.opencommunity.common.*;
 
import org.kubiki.ide.*;
import org.kubiki.base.Property;
import org.kubiki.base.ProcessResult;
import org.kubiki.base.BasicClass;
import org.kubiki.base.ConfigValue;
import org.kubiki.base.ObjectCollection;
import org.kubiki.util.DateConverter;
import org.kubiki.mail.*;
 
import org.kubiki.application.*;
 
import java.util.Hashtable;
import java.util.Vector;
import java.util.List;
import java.sql.*;
 
public class OrganisationMemberEdit extends BasicProcess{
 
	BasicProcessNode node1;
	OrganisationMember om;
	OrganisationMember parent = null;
	
	Person person;
	Identity identity;
	Address address;
	Login login;
	Activity activity;
	
	MemberAd ma;
	
	boolean createrequest = false;
	
	String[] contacts = {"Tel. privat","Tel. gesch.","Tel. mobil","Email"};
	
	//String[] tabids = {"memberad","memberadrequest","activity", "feedback"};
	//String[] tablabels = {"Inserate","Adressanfragen","Aktivitäten", "Feedback"};
	
	String[] tabids = {"memberad", "feedback"};
	String[] tablabels = {"&Uuml;bersicht", "Feedback"};
	
	String activetab = "memberad";
	
	public Hashtable yes_no, quality, roles = null;
	
	Vector history;
	Hashtable oms;

	Vector<OrganisationMember> organisationmembers = new Vector<OrganisationMember>(); //to delete
	Vector<OrganisationMemberController> organisationmembercontrolers = new Vector<OrganisationMemberController>();
	
	OrganisationMemberController currentController;
	
	//String memberadrequestid = "";
	
	MessageWrapper messageWrapper = null;
 
	public OrganisationMemberEdit(){
		node1 = new BasicProcessNode(){
			public boolean validate(ApplicationContext context){
				
				OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
				userSession.put("feedback", "");
				
				if(context.hasProperty("overview")){
					currentController.setMemberAd(null);
					currentController.setCurrentActivity(null);
					currentController.setMessageWrapper(null);
					currentController.setCreateRequest(false);
					currentController.setFileUpload(false);
					userSession.setOrganisationMember(null);

					return false;
				}
				else if(context.hasProperty("save")){
					
					if(context.hasProperty("current_omid") && context.getString("current_omid").equals(currentController.getOrganisationMember().getName())){
					
						currentController.saveOrganisationMember();
						userSession.put("feedback", "Profil gespeichert");
						
					}
					return false;
				}
				else if(context.hasProperty("feedback")){
					
					OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
					Hashtable params = new Hashtable();
					params.put("Type", "2");
					BasicProcess subprocess = ocs.createProcess("ch.opencommunity.process.FeedbackCreate", getParent(), params, context);
					subprocess.setParent(getProcess());
					subprocess.setStatus("started");
					//((FeddbackCreate)subprocess).initProcess(om);
					getProcess().setSubprocess(subprocess);

					return false;
					
				}
				else if(context.hasProperty("close")){
					if(context.hasProperty("omid")){
						OrganisationMemberController closeom = null;
						if(organisationmembercontrolers.size() > 1){
							for(OrganisationMemberController o : organisationmembercontrolers){
								if(o.getOrganisationMember().getName().equals(context.getString("omid"))){
									closeom = o;	
								}
							}
							if(closeom != null){
								if(closeom.equals(currentController)){
									int index = organisationmembercontrolers.indexOf(currentController);
									if(index == 0){
										currentController = organisationmembercontrolers.get(1);
									}
									else if(index == organisationmembercontrolers.size()-1){
										currentController = organisationmembercontrolers.get(organisationmembercontrolers.size()-2);
									}
									else{
										currentController = organisationmembercontrolers.get(index - 1);	
									}
									organisationmembercontrolers.remove(closeom);
								}
								else{
									organisationmembercontrolers.remove(closeom);
								}
							}

						}
					}
					return false;
				}
				else if(context.hasProperty("activate")){
					
					if(currentController.getOrganisationMember().getID("Status")==4){
						
						OrganisationMember om = currentController.getOrganisationMember();
						Login login = om.getLogin();
						
						if(login != null){
					
							OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
							
							String comment = ocs.getTextblockContent("33", true);
							comment = comment.replace("<p>", "\n");
							comment = comment.replace("</p>", "");
							comment = comment.replace("<br />", "");
							comment = comment.trim();
							String comment2 = "";
							String[] lines = comment.split("\r\n|\r|\n");
							for(String line : lines){
								comment2 += "\n" + line.trim();
							}
							
							String username = login.getString("Username");
							String password = login.getString("Password");
							
							String credentials = "\n\nIhr Benutzername : " + username + "\n\nIhr Passwort : " + password;
							comment2 = comment2.replace("<@credentials>", credentials);
							comment2 = comment2.replace("<@addressation>", om.getAddressation());
							
							ObjectTemplate ot = ocs.getObjectTemplate("4");
							
							Activity activity = (Activity)ot.createObject("ch.opencommunity.base.Activity", null, context);
							activity.applyTemplate(ot);
							activity.setProperty("Status", "0");
							activity.setProperty("Title", "Ihr Profil wurde freigeschaltet");
							activity.addProperty("OrganisationMemberID", "String", om.getName());
							BasicClass note = activity.getFieldByTemplate("21");
							
							note.setProperty("Content", comment2);
							
							String id = ocs.insertObject(activity, true);
							
							ocs.getObject(om, "Activity", "ID", id);
							
							om.setProperty("Status", 1);
							ocs.updateObject(om);
							
							ocs.sendAllPendingMails();
						}
						
						
					}
					return false;
				}
				else if(context.hasProperty("sendcredentials")){
					
					if(currentController.getOrganisationMember().getID("Status")==1){
						
						OrganisationMember om = currentController.getOrganisationMember();
						Login login = om.getLogin();
						
						if(login != null){
					
							OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
							
							String comment = ocs.getTextblockContent("39", true);
							comment = comment.replace("<p>", "\n");
							comment = comment.replace("</p>", "");
							comment = comment.replace("<br />", "");
							comment = comment.trim();
							String comment2 = "";
							String[] lines = comment.split("\r\n|\r|\n");
							for(String line : lines){
								comment2 += "\n" + line.trim();
							}
							
							String username = login.getString("Username");
							String password = login.getString("Password");
							
							String credentials = "\n\nIhr Benutzername : " + username + "\n\nIhr Passwort : " + password;
							comment2 = comment2.replace("<@credentials>", credentials);
							comment2 = comment2.replace("<@addressation>", om.getAddressation());
							
							ObjectTemplate ot = ocs.getObjectTemplate("4");
							
							Activity activity = (Activity)ot.createObject("ch.opencommunity.base.Activity", null, context);
							activity.applyTemplate(ot);
							activity.setProperty("Status", "1");
							activity.setProperty("Title", "Ihre Zugangsdaten für Nachbarnet");
							activity.addProperty("OrganisationMemberID", "String", om.getName());
							BasicClass note = activity.getFieldByTemplate("21");
							
							note.setProperty("Content", comment2);
							
							//String id = ocs.insertObject(activity, true);
							
							//ocs.getObject(om, "Activity", "ID", id);
							
							//om.setProperty("Status", 1);
							//ocs.updateObject(om);
							
							//ocs.sendAllPendingMails();
							
							currentController.setCurrentActivity(activity);
						}
						
						
					}
					return false;
				}
				else if(context.hasProperty("delete")){
					if(currentController.getOrganisationMember().getID("Status")==3){
						OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
						Person person = currentController.getOrganisationMember().getPerson();
						if(person != null){
							
							Identity identity = person.getIdentity();	
							identity.setProperty("FamilyName", ocs.createPassword(10));
							identity.setProperty("FirstName", ocs.createPassword(10));
							
							Address address = person.getAddress();
							address.setProperty("Street", ocs.createPassword(10));
							address.setProperty("Number", ocs.createPassword(5));
							ocs.updateObject(identity);
							ocs.updateObject(address);
							
							for(BasicClass contact : person.getObjects("Contact")){
								contact.setProperty("Value", ocs.createPassword(10));	
								ocs.updateObject(contact);
							}
						}
						currentController.getOrganisationMember().setProperty("Status", 5);
						ocs.updateObject(currentController.getOrganisationMember());
							
					}
					return false;
					
				}
				/*
				else if(context.hasProperty("memberadrequestid")){
					memberadrequestid = context.getString("memberadrequestid");
					return false;
				}
				*/
				else if(context.hasProperty("category")){
					
					userSession.put("category", context.getString("category"));
					userSession.put("mode", "searchresults");
					return false;
				}
				else if(context.hasProperty("createrequest")){
					
					currentController.setCreateRequest(true);
					
					userSession.setOrganisationMember(currentController.getOrganisationMember());
					
					currentController.getOrganisationMember().removeMemberAdIDs();
					
					userSession.put("mode", "searchresults");
					userSession.put("category", "1");

					
					/*
					OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
					Hashtable params = new Hashtable();
					params.put("Mode", "2");
					BasicProcess subprocess = ocs.createProcess("ch.opencommunity.process.MemberAdRequestCreate", ocs.getMemberAdAdministration(), params, context);
					subprocess.setParent(getProcess());
					((MemberAdRequestCreate)subprocess).setOrganisationMember(om);
					subprocess.setStatus("started");
					getProcess().setSubprocess(subprocess);
					ocs.logAccess("Neuer Subprozess: " + getProcess().getSubprocess().getClass().getName());
					*/
					return false;
				}
				else if(context.hasProperty("createnotification")){
					
					OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
					
					ObjectTemplate ot = ocs.getObjectTemplate("8");	
					
					Activity newactivity = (Activity)ot.createObject("ch.opencommunity.base.Activity", null, context);
					newactivity.applyTemplate(ot);
					newactivity.setProperty("Status", "1");
					newactivity.addProperty("OrganisationMemberID", "String", om.getName());

					
					//BasicClass note = newactivity.getFieldByTemplate("27");
					
					newactivity.setProperty("Title", "Benachrichtigung");
					String activityid = ocs.insertObject(newactivity, true);
					
					if(context.hasProperty("newmemberadid")){
						
						ActivityObject ao = new ActivityObject();
						ao.setProperty("MemberAdID", context.getString("newmemberadid"));
						ao.addProperty("ActivityID", "String", activityid);
						
						String id = ocs.insertSimpleObject(ao);
						
						ocs.executeCommand("UPDATE MemberAd SET NotificationStatus=1 WHERE ID=" + context.getString("newmemberadid"));
						
					}
					
					newactivity = (Activity)ocs.getObject(om, "Activity", "ID", activityid);
					
					newactivity.addObjectCollection("Results", "*");
					
					currentController.setCurrentActivity(newactivity);
					
					/*
					if(ot != null){
						
						getParent().addProperty("activity_title", "String", "");
						int i = 1;
						for(BasicClass parameter : activity.getObjects("Parameter")){
							Property p = getParent().addProperty("activity_parameter_" + i, "String", "");
							//if(parameter.getString("Title").equals("Dokument")){
								p.setSelection(ocs.getTemplib(1).getObjects("DocumentTemplate"));
							//}
						}
						i = 1;
						for(BasicClass note : activity.getObjects("Note")){
							getParent().addProperty("activity_note_" + i, "String", "");
						}
					}	
					*/
					return false;
					
				}
				else if(context.hasProperty("activityobjectadd")){
					
					OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
					
					String memberadid = context.getString("memberadid");
					String organisationmemberid = context.getString("organisationmemberid");
					
					if(currentController.getCurrentActivity() != null && memberadid != null && memberadid.length() > 0){
						
						ActivityObject ao = new ActivityObject();
						ao.setProperty("MemberAdID", memberadid);
						ao.addProperty("ActivityID", "String", currentController.getCurrentActivity().getName());
						
						String id = ocs.insertSimpleObject(ao);
						
						ocs.getObject(currentController.getCurrentActivity(), "ActivityObject", "ID", id);
						
						ocs.executeCommand("UPDATE MemberAd SET NotificationStatus=1 WHERE ID=" + memberadid);
						
					}
					else if(currentController.getCurrentActivity() != null && organisationmemberid != null && organisationmemberid.length() > 0){
						if(organisationmemberid.equals(om.getName())){
							ActivityObject ao = new ActivityObject();
							
							ao.setProperty("OrganisationMemberID", organisationmemberid);
							ao.addProperty("ActivityID", "String", currentController.getCurrentActivity().getName());
							
							String id = ocs.insertSimpleObject(ao);
							
							ocs.getObject(currentController.getCurrentActivity(), "ActivityObject", "ID", id);
							
							om.setProperty("NotificationStatus", "1");
							
							ocs.updateObject(currentController.getOrganisationMember());
						
						}
						
					}
				
					return false;
				}
				else if(context.hasProperty("creatememberadrequests")){
					
					currentController.setCreateRequest(false);
					
					OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
					

					
					
					//Brief erstellen
					
					
					
					ObjectTemplate ot = ocs.getObjectTemplate("7");
					
					String content = ocs.getTextblockContent("10", true);
					
					if(context.hasProperty("email") && context.getString("email").equals("true")){
						
						
						
						ot = ocs.getObjectTemplate("1");
						
						content = ocs.getTextblockContent("10", true);
						
						content += "\n\n_____________________________________________________________________";
						
						content += "\n";
						
						content += OpenCommunityUserProfile.getAddressList(ocs, userSession);
						
					}
					
					Activity newactivity = (Activity)ot.createObject("ch.opencommunity.base.Activity", null, context);
					newactivity.applyTemplate(ot);
					newactivity.setProperty("Status", "1");
					newactivity.addProperty("OrganisationMemberID", "String", currentController.getOrganisationMember().getName());

					
					BasicClass note = newactivity.getFieldByTemplate("26");
					
					if(context.hasProperty("email") && context.getString("email").equals("true")){
						note = newactivity.getFieldByTemplate("16");
					}
										
					
										
										
					note.setProperty("Content", content);
					
					//BasicClass parameter = activity.getFieldByTemplate("23");
					
					/*
					Document document = (Document)ot.createObject("ch.opencommunity.base.Document", null, context);
					document.setProperty("Recipient", om.getName());
					document.setProperty("Template", "2");
					document.setProperty("Type", "2");
					String docid = ocs.insertObject(document);
					document.setName(docid);
					parameter.setProperty("Document", docid);
					*/
					
					newactivity.setProperty("Title", "Benachrichtigung");
					String activityid = ocs.insertObject(newactivity, true);
					
					MemberAdRequestGroup marg = (MemberAdRequestGroup)om.createObject("ch.opencommunity.advertising.MemberAdRequestGroup", null, context);
					String gid = ocs.insertObject(marg);
					
					List<String> ids = OpenCommunityUserProfile.createMemberAdRequests(context, getCurrentControler().getOrganisationMember(), gid, null, 1);
					
					for(String id : ids){
						ActivityObject ao = new ActivityObject();
						ao.addProperty("ActivityID", "String", activityid);
						ao.setProperty("MemberAdRequestID", id);
						ocs.insertObject(ao);
					}
					
					
					if(context.hasProperty("pdf") && context.getString("pdf").equals("true")){

						activity = (Activity)ocs.getObject(currentController.getOrganisationMember(), "Activity", "ID", activityid);
						
						currentController.setCurrentActivity(activity);
						
					}
					else if(context.hasProperty("email") && context.getString("email").equals("true")){

						activity = (Activity)ocs.getObject(om, "Activity", "ID", activityid);
						
						getParent().addProperty("activity_title", "String", activity.getString("Title"));
						int i = 1;
						for(BasicClass parameter : activity.getObjects("Parameter")){
							if(parameter.getString("Title").equals("Dokument")){
								getParent().addProperty("activity_parameter_" + i, "String", parameter.getString("Document"));
							}
							else{
								getParent().addProperty("activity_parameter_" + i, "String", parameter.getString("Value"));
							}
						}
						i = 1;
						for(BasicClass note1 : activity.getObjects("Note")){
							getParent().addProperty("activity_note_" + i, "String", note1.getString("Content"));
						}
						
						currentController.setCurrentActivity(activity);
						
					}
					else{
						ocs.getObject( currentController.getOrganisationMember(), "Activity", "ID", activityid); // nur laden, aber nicht anzeigen
						currentController.setCurrentActivity(null);
					}

					
					return false;				
				}	
				else if(context.hasProperty("newtab")){
					currentController.setActiveTab(context.getString("newtab"));
					return false;
				}
				else if(context.hasProperty("modify")){
					
					OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
					Hashtable params = new Hashtable();
					//params.put("OMID", om.getName());
					BasicProcess subprocess = ocs.createProcess("ch.opencommunity.process.OrganisationMemberModify", this, params, context);
					subprocess.setParent(getProcess());
					subprocess.setStatus("started");
					((OrganisationMemberModify)subprocess).initProcess(om);
					getProcess().setSubprocess(subprocess);

					return false;
				}
				/*
				else if(context.hasProperty("back")){
					if(history.size() > 0){
						String[] lastid = (String[])history.elementAt(history.size()-1);
						OrganisationMember om2 = (OrganisationMember)oms.get(lastid[0]);
						if(om2 == null){
							om2 = loadOrganisationMember(lastid[0]);
						}
						else{
							loadOrganisationMember("", om2);
						}
						if(om2 != null){
							history.remove(history.size()-1);
							om = om2;	
						}												
					}
					return false;
				}
				*/
				else if(context.hasProperty("OMID2")){

					OrganisationMemberController oc = null;
					for(OrganisationMemberController o : organisationmembercontrolers){
						if(o.getOrganisationMember().getName().equals(context.getString("OMID2"))){
							oc = o;		
						}
					}
					if(oc == null){
						//om2 = loadOrganisationMember(context.getString("OMID2"));
						loadOrganisationMember(context.getString("OMID2"));
						//organisationmembers.add(om2);
					}
					else{
						//loadOrganisationMember("", om2);
						currentController = oc;
					}
					/*
					loadOrganisationMember("", om2);
					if(om2 != null){
						String[] info = new String[2];
						info[0] = om.getName();
						info[1] = om.toString();
						history.add(info);
						oms.put(om.getName(), om);
						om = om2;	
					}
					*/
					return false;
				}
				else if(context.hasProperty("memberadcreate")){
					OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
					Hashtable params = new Hashtable();
					params.put("Mode", "2");
					BasicProcess subprocess = ocs.createProcess("ch.opencommunity.process.MemberAdCreate", ocs.getMemberAdAdministration(), params, context);
					subprocess.setParent(getProcess());
					((MemberAdCreate)subprocess).setOrganisationMember(currentController.getOrganisationMember());
					subprocess.setStatus("started");
					getProcess().setSubprocess(subprocess);
					return false;
				}
				else if(context.hasProperty("objecttemplate")){
					currentController.createActivity(context);
					return false;
				}
				else if(context.hasProperty("activityid")){
					currentController.setCurrentActivityID(context.getString("activityid"));
					/*
						getParent().addProperty("activity_title", "String", activity.getString("Title"));
						int i = 1;
						for(BasicClass parameter : activity.getObjects("Parameter")){
							if(parameter.getString("Title").equals("Dokument")){
								getParent().addProperty("activity_parameter_" + i, "String", parameter.getString("Document"));
							}
							else{
								getParent().addProperty("activity_parameter_" + i, "String", parameter.getString("Value"));
							}
						}
						i = 1;
						for(BasicClass note : activity.getObjects("Note")){
							getParent().addProperty("activity_note_" + i, "String", note.getString("Content"));
						}
					*/
					return false;
				}
				else if(context.hasProperty("saveactivity")){
					currentController.saveActivity(context);
					/*
					OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
					
					activity.setProperty("Title", getParent().getString("activity_title"));
					int i = 1;
					for(BasicClass parameter : activity.getObjects("Parameter")){
							if(activity.getID() < 1){
								DocumentTemplate dt = ocs.getDocumentTemplate(getParent().getID("activity_parameter_" + i));
								if(dt != null){
									
									Document document = (Document)dt.createObject("ch.opencommunity.base.Document", null, context);
									document.addProperty("OrganisationMemberID", "String", om.getName());
									document.setProperty("Recipient", om.getName());
									document.setProperty("Template", dt.getName());
									DocumentTemplateModule dtm = (DocumentTemplateModule)dt.getObjectByIndex("DocumentTemplateModule", 0);
									document.setProperty("WordModules", "/WebApplication/DocumentTemplateLibrary:1/DocumentTemplate:" + dt.getName() + "/DocumentTemplateModule:" + dtm.getName());
									String docid = ocs.insertObject(document);
									parameter.setProperty("Document", docid);
									ocs.getObject(om, "Document", "ID", docid);
									
								}
							}
							
						}
						i = 1;
						for(BasicClass note : activity.getObjects("Note")){
							note.setProperty("Content", getParent().getString("activity_note_" + i));
							if(note.getID() > 0){
								ocs.updateObject(note);
							}
						}
					
					
					if(activity.getID() > 0){
						ocs.updateObject(activity);
					}
					else{
						String id = ocs.insertObject(activity, true);
						activity = (Activity)ocs.getObject(om, "Activity", "ID", id);
					}
					if(context.hasProperty("send") && context.getString("saveactivity").equals("true")){
						ocs.sendAllPendingMails();
					}
					if(context.hasProperty("createpdf")){
						if(activity != null){
							
							String filename = ocs.createPDF(activity, null);
							setProperty("filename", filename);
						}
					}
					if(activity.getID("Template") != 7){
						activity = null;
					}
					*/
					return false;
				}
				else if(context.hasProperty("createactivity")){
					currentController.importEmail(context);
					return false;
				}
				else if(context.hasProperty("createlogin")){
					OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
					ocs.logAccess("creating login ...");
					try{
						
						login = new Login();
						login.addProperty("OrganisationMemberID", "String", currentController.getOrganisationMember().getName());
						
						String username = om.getName();
						while(username.length() < 7){
							username = "0" + username;
						}
						
						login.setProperty("Username", username);
						String password = ocs.createPassword(8);
						login.setProperty("Password", password);
						
						String id = ocs.insertObject(login);
						
						login = (Login)ocs.getObject(currentController.getOrganisationMember(), "Login", "ID", id);
						if(login != null){
							currentController.addProperty(login.getProperty("Username"));
							currentController.addProperty(login.getProperty("Password"));
						}
					}
					catch(java.lang.Exception e){
						ocs.logException(e);
					}
					return false;
				}
				else if(context.hasProperty("addrole")){
					OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
					try{
						String roleid = context.getString("roleid");
						ocs.logAccess("Neue Rolle " + roleid);
						if(roleid != null && roleid.length() > 0){
							MemberRole mr = null;
							for(BasicClass bc : om.getObjects("MemberRole")){
								if(bc.getString("Role").equals(roleid)){
									mr = (MemberRole)bc;	
								}
							}
							if(mr == null){
								mr = new MemberRole();
								mr.addProperty("OrganisationMemberID", "String", currentController.getOrganisationMember().getName());
								mr.setProperty("Role", roleid);
								String id = ocs.insertObject(mr);
								ocs.getObject(currentController.getOrganisationMember(), "MemberRole", "ID", id);
							}
						}
					}
					catch(java.lang.Exception e){
						ocs.logException(e);
					}
					return false;
				}
				else if(context.hasProperty("deleterole")){
					OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
					try{
						String roleid = context.getString("roleid");
						if(roleid != null && roleid.length() > 0){
							MemberRole memberRole = (MemberRole)om.getObjectByName("MemberRole", roleid);
							if(memberRole != null){
								memberRole.setProperty("Status", 1);
								ocs.updateObject(memberRole);
							}
						}
					}
					catch(java.lang.Exception e){
						ocs.logException(e);
					}
					return false;
				}
				else if(context.hasProperty("deactivaterelation")){
					OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
					try{
						String relationid = context.getString("relationid");
						if(relationid != null && relationid.length() > 0){
							OrganisationMemberRelationship relation = (OrganisationMemberRelationship)currentController.getOrganisationMember().getObjectByName("OrganisationMemberRelationship", relationid);
							if(relation != null){
								relation.setProperty("Status", 1);
								ocs.updateObject(relation);
							}
						}
					}
					catch(java.lang.Exception e){
						ocs.logException(e);
					}
					return false;
				}
				else if(context.hasProperty("addcontact")){
					OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
					try{
						String contactid = context.getString("contactid");
						ocs.logAccess("Neue Rolle " + contactid);
						
						if(contactid != null && contactid.length() > 0){
							int iContactid = (new Integer(contactid)).intValue();
							if(!getParent().hasProperty(contacts[iContactid])){
								Contact contact = new Contact();
								Person person = currentController.getOrganisationMember().getPerson();
								contact.addProperty("PersonID", "String", person.getName());
								contact.setProperty("Type", contactid);
								String id = ocs.insertObject(contact);
								ocs.getObject(person, "Contact", "ID", id);
								currentController.addProperty(contacts[iContactid], "String", "");
							}
						}
					}
					catch(java.lang.Exception e){
						ocs.logException(e);
					}
					return false;
				}
				else if(context.hasProperty("deletecontact")){
					OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
					try{
						String contactid = context.getString("contactid");
						if(contactid != null && contactid.length() > 0){
							Person person = currentController.getOrganisationMember().getPerson();
							Contact contact = (Contact)person.getObjectByName("Contact", contactid);
							if(contact != null){
								ocs.getDataStore().removeObject("Contact", contactid, true);
								person.deleteElement(contact);
							}
						}
					}
					catch(java.lang.Exception e){
						ocs.logException(e);
					}
					return false;
				}
				else if(context.hasProperty("memberadid")){
					String memberAdID = context.getString("memberadid");
					
					OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
					Hashtable params = new Hashtable();
					params.put("MAID", memberAdID);
					//BasicProcess subprocess = ocs.createProcess("ch.opencommunity.process.MemberAdDetail", ocs.getMemberAdAdministration(), params, context);
					BasicProcess subprocess = ocs.createProcess("ch.opencommunity.process.MemberAdDetail", getProcess(), params, context);
					subprocess.setParent(getProcess());
					//((MemberAdDetail)subprocess).setOrganisationMember(om);
					subprocess.setStatus("started");
					getProcess().setSubprocess(subprocess);
					return false;
					
					/*
					ma = (MemberAd)om.getObjectByName("MemberAd", memberAdID);
					if(ma == null){
						OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
						ma = (MemberAd)ocs.getObject(null, "MemberAd", "ID", memberAdID, false);
						ma.setParent(this);
						ma.initObjectLocal();
					}
					return false;
					*/
				}
				else if(context.hasProperty("createpdf2")){
					if(currentController.getCurrentActivity() != null){
						OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
						String filename = ocs.createPDF(currentController.getCurrentActivity(), null);
						setProperty("filename", filename);
					}
					return false;
					
				}
				else if(context.hasProperty("importmail")){
					OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
					String messageid = context.getString("messageid");
					ocs.logAccess("MessageID : " + messageid);
					if(messageid != null){
						
						AbstractEmailAdministration ea = (AbstractEmailAdministration)userSession.get("emailadministration");
						ocs.logAccess("EmailAdministration : " + ea);
						if(ea != null){
							Object message = ea.getMessageByID(messageid);
							if(message != null){
								currentController.setMessageWrapper(ea.getMessageWrapper(message));	
							}
							return false;							
						}
						else{
							return false;	
						}
					}
					return false;
				}
				else if(context.hasProperty("deletememberadrequest")){
					OpenCommunityServer ocs = (OpenCommunityServer)getRoot();					
					Connection con = ocs.getConnection();
					String memberadrequestid = context.getString("memberadrequestid");
					if(memberadrequestid.length() > 0){
						try{
							Statement stmt = con.createStatement();
							stmt.execute("DELETE FROM MemberAdRequest WHERE ID=" + memberadrequestid);
							
						}
						catch(java.lang.Exception e){
							ocs.logException(e);	
						}
					}
					
					return false;
				}
				else if(context.hasProperty("linkprofile")){
					
					OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
					Hashtable params = new Hashtable();
					//params.put("MAID", memberAdID);

					BasicProcess subprocess = ocs.createProcess("ch.opencommunity.process.LinkProfile", getProcess(), params, context);
					subprocess.setParent(getProcess());
					subprocess.setStatus("started");
					getProcess().setSubprocess(subprocess);
					return false;					
				}
				else if(context.hasProperty("mergeprofile")){
					
					OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
					Hashtable params = new Hashtable();
					//params.put("MAID", memberAdID);

					BasicProcess subprocess = ocs.createProcess("ch.opencommunity.process.MergeProfile", getProcess(), params, context);
					subprocess.setParent(getProcess());
					subprocess.setStatus("started");
					getProcess().setSubprocess(subprocess);
					return false;					
				}
				else if(context.hasProperty("reactivaterequest")){	
					if(context.getString("reactivaterequest").equals("true")){
						OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
						String memberadrequestid = 	context.getString("memberadrequestid");
						if(memberadrequestid != null && memberadrequestid.length() > 0){
							ocs.executeCommand("UPDATE MemberAdRequest SET Status=1 WHERE ID=" + memberadrequestid);	
						}
					}
					return false;
				}
				else if(context.hasProperty("fileupload")){
					currentController.setFileUpload(true);
					return false;	
				}
				else{
					return true;
				}
			}
		};
		
		Property p = addProperty("filename", "String", "");
		node1.addProperty(p);
		addNode(node1);
		setCurrentNode(node1);	
		
		addProperty("OMID", "String", "");
		addProperty("PARENT", "String", "");
		addProperty("sectionid", "String", "");
		addProperty("objecttemplate", "Integer", "");
		
		addObjectCollection("OrganisationMember", "ch.opencommunity.base.OrganisationMember");
		
		yes_no = new Hashtable();
		yes_no.put("t", "JA");
		yes_no.put("f", "NEIN");
		
		quality = new Hashtable();
		quality.put("0", "");
		quality.put("1", "gar nicht gut");
		quality.put("2", "es geht so");
		quality.put("3", "gut");
		quality.put("4", "super");		
		
		history = new Vector();
		oms = new Hashtable();

		
	}

	public OrganisationMemberController addOrganisationMemberControler(OrganisationMember newom){
		
		OrganisationMemberController newomc = new OrganisationMemberController(newom);
		
		newomc.setParent(this);
		
		organisationmembercontrolers.add(newomc);	
		
		return newomc;
	}
	public OrganisationMemberController addOrganisationMemberControler(OrganisationMemberController newomc){
		newomc.setParent(this);
		organisationmembercontrolers.add(newomc);	
		return newomc;
	}
	public OrganisationMemberController getCurrentOrganisationMemberControler(){
		return currentController;	
	}
	public Vector<OrganisationMemberController> getOrganisationMemberControlers(){
		return organisationmembercontrolers;
	}
	public OrganisationMemberController getCurrentControler(){
		return currentController;	
	}
	public void initProcess(ApplicationContext context){
		if(getString("OMID").length() > 0){
			initProcess(context, getString("OMID"));	
		}
	}
	public void initProcess(ApplicationContext context, String omid){
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		if(getObject("OMID") instanceof OrganisationMember){
			om = (OrganisationMember)getObject("OMID");
			//organisationmembers.add(om);
			currentController = addOrganisationMemberControler(om);
		}
		else{
			om = (OrganisationMember)ocs.getObject(ocs, "OrganisationMember", "ID", omid, false);
			//om.setParent(this);
			om.setParent(ocs);
			//om.initObjectLocal();
			om.initObject();
			//organisationmembers.add(om);
			currentController = addOrganisationMemberControler(om);
		}
		
		/*
		if(om != null){
			Person person = om.getPerson();
			addProperty(om.getProperty("Status"));
			addProperty(om.getProperty("NotificationMode"));
			
			if(person != null){
				
				 addProperty("Languages", "ListFillIn", person.getString("Languages"));
				 getProperty("Languages").setSelection(ocs.getSupportedLanguages());
				 
				 addProperty(om.getProperty("Comment"));
				
				identity = person.getIdentity();
				if(identity != null){
					addProperty(identity.getProperty("FamilyName"));
					addProperty(identity.getProperty("FirstName"));
					addProperty(identity.getProperty("Sex"));
					addProperty(identity.getProperty("DateOfBirth"));
					addProperty(identity.getProperty("FirstLanguageS"));
				}
				
				
				address = person.getAddress();
				if(address != null){
					
					addProperty(address.getProperty("AdditionalLine"));	
					addProperty(address.getProperty("Street"));	
					addProperty(address.getProperty("Number"));
					addProperty(address.getProperty("Zipcode"));
					addProperty(address.getProperty("City"));
					addProperty(address.getProperty("Country"));
				}
				
				for(BasicClass bc : person.getObjects("Contact")){
					addProperty(contacts[bc.getInt("Type")], "String", bc.getString("Value"));
);
				}

			
			}
			login = om.getLogin();
			if(login != null){
				login.getProperty("Username").setEditable(false);
				addProperty(login.getProperty("Username"));
				addProperty(login.getProperty("Password"));
			}
		}
		*/
		getProperty("objecttemplate").setSelection(ocs.getObjectTemplates());
		
		roles = new Hashtable();
		for(BasicClass bc : ocs.getObjects("Role")){
			roles.put(bc.getName(), bc.getString("Title"));
		}
		//node1.setTitle(om.toString());
		
		if(!getString("PARENT").isEmpty()){
			parent = (OrganisationMember)ocs.getObject(this, "OrganisationMember", "ID", getString("PARENT"), false);
			parent.setParent(this);
			parent.initObject();
			String[] info = new String[2];
			info[0] = parent.getName();
			info[1] = parent.toString();
			history.add(info);
		}
		

		
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
		//userSession.setOrganisationMember(om);
		userSession.removeMemberAdIDs();
	}
	public OrganisationMember loadOrganisationMember(String id){
		return loadOrganisationMember(id, null);
	}
	public OrganisationMember loadOrganisationMember(String id, OrganisationMember om){
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		ocs.logAccess(getString("OMID"));
		if(om == null){
			om = (OrganisationMember)ocs.getObject(ocs, "OrganisationMember", "ID", id , false);
			om.setParent(this);
			om.initObjectLocal();
			currentController = addOrganisationMemberControler(om);
		}
		/*
		if(om != null){
			Person person = om.getPerson();
			addProperty(om.getProperty("Status"));
			addProperty(om.getProperty("NotificationMode"));
			if(person != null){
				
				 addProperty("Languages", "ListFillIn", person.getString("Languages"));
				 getProperty("Languages").setSelection(ocs.getSupportedLanguages());
				
				identity = person.getIdentity();
				if(identity != null){
					addProperty(identity.getProperty("FamilyName"));
					addProperty(identity.getProperty("FirstName"));
					addProperty(identity.getProperty("Sex"));
					addProperty(identity.getProperty("DateOfBirth"));
					addProperty(identity.getProperty("FirstLanguageS"));
				}
				
				//getProperty("FirstLanguage").setSelection(ocs.getSupportedLanguages());
				
				address = person.getAddress();
				if(address != null){
					addProperty(address.getProperty("AdditionalLine"));	
					addProperty(address.getProperty("Street"));	
					addProperty(address.getProperty("Number"));
					addProperty(address.getProperty("Zipcode"));
					addProperty(address.getProperty("City"));
					addProperty(address.getProperty("Country"));
				}
				
				for(BasicClass bc : person.getObjects("Contact")){
					addProperty(contacts[bc.getInt("Type")], "String", bc.getString("Value"));
					//addProperty(contacts[bc.getInt("Type")-1], contacts[bc.getInt("Type")-1], "");
				}

			
			}
			login = om.getLogin();
			if(login != null){
				addProperty(login.getProperty("Username"));
				addProperty(login.getProperty("Password"));
			}
			Vector sex = new Vector();
			sex.add(new ConfigValue("0", "0", "Nicht gesetzt"));
			sex.add(new ConfigValue("1", "1", "Herr"));
			sex.add(new ConfigValue("2", "2", "Frau"));
			getProperty("Sex").setSelection(sex);
		}
		*/
		return om;
	}
	public OrganisationMember getOrganisationMember(){
		return om;
	}
	public String getOrganisationMemberEditForm(ApplicationContext context){
		return OrganisationMemberView.getOrganisationMemberView(this, context);	
	}
	public String getOrganisationMemberEditForm2(ApplicationContext context){
		return om.toString();
	}
	public String getOrganisationMemberEditForm3(ApplicationContext context){
	
		
	
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		//HTMLForm formManager = ocs.getFormManager();
		StringBuilder html = new StringBuilder();
		
		html.append("<table style=\"width : 100%;\"><tr>");
		for(OrganisationMember om2 : organisationmembers){
			if(om2.getName().equals(om.getName())){
				html.append("<td class=\"processtab_active\">");
				html.append(om2.toString());
				html.append("<img src=\"images/close_small.png\" onclick=\"getNextNode('close=true&omid=" + om2.getName() + "')\">");
				html.append("</td>");					
			}
			else{
				html.append("<td class=\"processtab\">");
				html.append("<a href=\"javascript:getNextNode('OMID2=" + om2.getName() + "')\">" + om2 + "</a>");
				html.append("<img src=\"images/close_small.png\" onclick=\"getNextNode('close=true&omid=" + om2.getName() + "')\">");
				html.append("</td>");
			}
		}
		html.append("<td class=\"processtab_empty\">&nbsp;</td>");
		html.append("</tr></table>");
		
		try{
		
			if(ma != null){
				
				html.append("<input type=\"button\" value=\"Zurück\" onclick=\"getNextNode('overview=true')\">");
				html.append(MemberAdDetail.getMemberAdDetailForm(ma, true, context));
				
			}
			else if(createrequest){
				
				MemberAdAdministration maa = ocs.getMemberAdAdministration();
				OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
				
				html.append("<div style=\"position : absolute; height : auto; width : 100%; background : #363D45;\">");
				
				html.append(maa.getMemberAdSearchForm(context, null, true));
				
				html.append("<div id=\"userprofile\" style=\"position : absolute;\">");
				
				html.append(OpenCommunityUserProfile.getMemoryList(ocs, userSession, true));
				
				html.append("</div>");
				
				html.append("</div>");
				
				/*
				
				html.append("<br><select name=\"category\" id=\"category\" class=\"selectbig\" onchange=\"reloadSearch(this.value)\">");
				html.append("<option value=\"0\">Was? Rubrik wählen</option>");
				for(BasicClass bc :  ocs.getMemberAdAdministration().getObjects("MemberAdCategory")){
					html.append("<option value=\"" + bc.getName() + "\">" + bc.getString("Title") + "</option>");			
				}
				html.append("</select>");
				
				html.append("<div id=\"searchresults\" style=\"position : absolute; width : 700px; top : 30px;\">");
				
				
				html.append("</div>");
				
				*/
				
			}	
			else if(activity != null){
				
				if(activity.getID("Template")==7 || activity.getID("Template")==8){
					html.append("<input type=\"button\" value=\"PDF erstellen\" onclick=\"getNextNode('saveactivity=true&createpdf=true')\">");					
				}
				html.append("<form action=\"servlet\" id=\"processNodeForm\">");
				html.append("<table>");
				html.append("<tr><th>Bezeichnung</th><td>" + HTMLForm.getTextField(getProperty("activity_title"), true, "") + "</td></tr>");
				
				int i = 1;
				for(BasicClass parameter : activity.getObjects("Parameter")){
					Property p = getProperty("activity_parameter_" + i);
					if(p.getValues() != null){
						html.append("<tr><th>" + parameter.getString("Title") + "</th><td>" + HTMLForm.getSelection(getProperty("activity_parameter_" + i), true, "") + "</td></tr>");
					}
					else if(parameter.getString("Title").equals("Dokument")){
						Document doc = (Document)om.getObjectByName("Document", p.getValue());
						if(doc != null){
							html.append("<tr><th>" + parameter.getString("Title") + "</th><td><a href=\"javascript:openDocument(" + p.getValue() + ")\">Oeffnen</a></td></tr>");
						}
					}
					else{
						html.append("<tr><th>" + parameter.getString("Title") + "</th><td>" + HTMLForm.getTextField(getProperty("activity_parameter_" + i), true, "") + "</td></tr>");
					}
					getParent().addProperty("activity_note_" + i, "String", "");
				}
				i = 1;
				for(BasicClass note : activity.getObjects("Note")){
					
					Property p = getProperty("activity_note_" + i);
					
					String propertyid = HTMLForm.createId(p);
					
					html.append("<tr><td></td><td><select onchange=\"getTextblockContent(this.value,'" + propertyid + "')\">");
					
					for(BasicClass textblock : ocs.getTextblocks(1)){
						html.append("<option value=\"" + textblock.getName() + "\">" + textblock.getString("Title") + "</option>");	
					}
					
					html.append("</select></td></tr>");
					html.append("<tr><th>" + note.getString("Title") + "</th><td>" + HTMLForm.getTextArea(p, true) + "</td></tr>");
	
				}
				
				html.append("<tr><td>Folgende Objekte sind mit diesem Brief verknüpft:</td></tr>");
				
				for(BasicClass bc : activity.getObjectCollection("ActivityObject").getObjects()){
					
					
					
					if(bc.getID("MemberAdID") > 0){
						html.append("<tr><td>Inserat " + bc.getID("MemberAdID") + "</td></tr>");	
					}
					else if(bc.getID("MemberAdRequestID") > 0){
						html.append("<tr><td>Adressbestellung " + bc.getID("MemberAdRequestID") + "</td></tr>");
					}
					else if(bc.getID("OrganisationMemberID") > 0){
						html.append("<tr><td>Benutzerprofil " + bc.getID("OrganisationMemberID") + "</td></tr>");	
					}
					
					
				}
				
				if(activity.getID("Template")==7 || activity.getID("Template")==8){
					
					html.append("<tr><td>Folgende Objekte haben noch keine schriftliche Benachrichtigung:</td></tr>");
					
					if(om.getID("NotificationStatus")==0){
						html.append("<tr><td>Benutzerregistrierung</td><td><a href=\"javascript:getNextNode('activityobjectadd=true&organisationmemberid=" +  om.getName() + "')\">Hinzufügen</a></td></tr>");
					}
					
					if(activity.getObjectCollection("Results")==null){
						activity.addObjectCollection("Results", "*");	
					}
					activity.getObjects("Results").clear();
					
					String sql = "SELECT ID, Title, DateCreated FROM MemberAd WHERE OrganisationMemberID=" + om.getName() + " AND NotificationStatus=0";
					ocs.queryData(sql, activity.getObjectCollection("Results"));
					for(BasicClass bc : activity.getObjectCollection("Results").getObjects()){
						html.append("<tr><td>" + bc.getString("TITLE") + " " + bc.getString("DATECREATED") + "</td><td><a href=\"javascript:getNextNode('activityobjectadd=true&memberadid=" +  bc.getString("ID") + "')\">Hinzufügen</a></td></tr>");	
					}
					
				}
				
				html.append("</table>");	
				
				html.append("<input type=\"button\" value=\"Zur&uuml;ck\" onclick=\"getNextNode('overview=true')\">");
				html.append("<input type=\"button\" value=\"Speichern\" onclick=\"getNextNode('saveactivity=true')\">");
				if(activity.getID("Template")==1){
					html.append("<input type=\"button\" value=\"Speichern und senden\" onclick=\"getNextNode('saveactivity=true&send=true')\">");
				}
	
				html.append("</form>");
				
			}
			else if(messageWrapper != null){
				html.append("<div style=\"position : relative; width : 600px;\">") ;                                                          
				html.append("<div>" + messageWrapper.getSender() + "</div>");
				html.append("<div>" + messageWrapper.getDateString() + "</div>");	
				html.append("<div>" + messageWrapper.getSubject() + "</div>");
				html.append("<div>" + messageWrapper.getMessageBody() + "</div>");
				html.append("</div>");
				html.append("<input type=\"button\" value=\"Zur&uuml;ck\" onclick=\"getNextNode('overview=true')\">");
				html.append("<input type=\"button\" value=\"Importieren\" onclick=\"getNextNode('createactivity=true')\">");
				
			}
			else if(activetab != null && activetab.equals("journal")){
				html.append("<input type=\"button\" value=\"Zur&uuml;ck\" onclick=\"getNextNode('newtab=memberad')\">");
				html.append("<h4>Journal</h4>");
				
					String[] status = {"offen", "erledigt"};
					
					html.append("Neue Aktivität:");
					html.append(HTMLForm.getSelection(getProperty("objecttemplate"), "",  "", true, true, true, "", "getNextNode('objecttemplate=' + this.value )"));
					
					html.append("<div style=\"height : 100%; overflow : auto;\">");
					
					html.append("<table>");
					
					html.append("<tr><th>Datum</th><th>Vorlage</th><th>Betreff</th><th>Status</th></tr>");
					
					boolean even = true;
					
					//for(BasicClass bc : om.getObjects("Activity")){
					for(int j = om.getObjects("Activity").size()-1; j >= 0; j--){
						BasicClass bc = om.getObjects("Activity").get(j);
						if(even){
							html.append("<tr class=\"even\">");
						}
						else{
							html.append("<tr class=\"odd\">");						
						}
						
						
						html.append("<td class=\"datacell\">" + DateConverter.sqlToShortDisplay(bc.getString("DateCreated"), false) + "</td>");
						html.append("<td class=\"datacell\">" + bc.getString("Template") + "</td>");
						//html.append("<td class=\"datacell\">" + bc.getString("Date") + "</td>");
						html.append("<td class=\"datacell\">" + bc.getString("Title") + "</td>");
						//html.append("<td class=\"datacell\">" + bc.getString("Title") + "</td>");
						html.append("<td class=\"datacell\">" + status[bc.getID("Status")] + "</td>");
						html.append("<td class=\"datacell\"><a href=\"javascript:getNextNode(\'activityid=" + bc.getName() + "\')\">Details</a></td>");
						html.append("</tr>");
						Note note = (Note)bc.getObjectByIndex("Note", 0);
						if(note != null){
							
							if(even){
								html.append("<tr class=\"even\">");
							}
							else{
								html.append("<tr class=\"odd\">");						
							}
							html.append("<td class=\"datacell\"></td><td colspan=\"3\" class=\"datacell\">");
							String content = note.getString("Content").trim();
							String[] lines = content.split("\r\n|\r|\n");
							int n = 3;
							if(lines.length < n) n = lines.length;
							for(int i = 0; i < n; i++){
								html.append(lines[i] + "<br>");	
							}
							
							html.append("</td></tr>");
							
						}
						even = !even;
					}
					html.append("</table>");
			}
			else if(activetab != null && activetab.equals("emailimport")){
				html.append("<input type=\"button\" value=\"Zur&uuml;ck\" onclick=\"getNextNode('newtab=memberad')\">");
				html.append("<h4>Email Import</h4>");
					html.append("<div id=\"mailform\">");
			
			
			
					html.append("</div>");
			
					html.append("<script language=\"javascript\">");
					html.append("callEmailAdministration(\'mailcommand=loadmessages\');");
					html.append("</script>");				
				
			}
			else{
				
				/*
				if(history.size() > 0){
					String[] info = (String[]) history.get(0);
					html.append("<a href=\"javascript:getNextNode('back=true')\"> " + info[1] + "<< </a>");	
				}
				*/
				html.append("<div id=\"toolbar\">");
				
				html.append("<img class=\"toolbaricon\" src=\"images/save.png\" onclick=\"getNextNode('save=true')\" onmouseover=\"showTooltip(event, 'Speichern')\" onmouseout=\"hideTooltip()\">");
				html.append("<img class=\"toolbaricon\" src=\"images/journal.png\" onclick=\"getNextNode('newtab=journal')\" onmouseover=\"showTooltip(event, 'Journal')\" onmouseout=\"hideTooltip()\">");
				html.append("<img class=\"toolbaricon\" src=\"images/sendmail.png\" onclick=\"getNextNode('objecttemplate=1')\" onmouseover=\"showTooltip(event, 'Email schreiben')\" onmouseout=\"hideTooltip()\">");
				html.append("<img class=\"toolbaricon\" src=\"images/word.png\" onclick=\"getNextNode('objecttemplate=2')\" onmouseover=\"showTooltip(event, 'Worddokument erstellen')\" onmouseout=\"hideTooltip()\">");				
				html.append("<img class=\"toolbaricon\" src=\"images/pdf.png\" onclick=\"getNextNode('objecttemplate=7')\" onmouseover=\"showTooltip(event, 'PDF-Brief erstellen')\" onmouseout=\"hideTooltip()\">");
				html.append("<img class=\"toolbaricon\" src=\"images/pdf.png\" onclick=\"getNextNode('createnotification=true')\" onmouseover=\"showTooltip(event, 'Mitglied brieflich benachrichtigen')\" onmouseout=\"hideTooltip()\">");
				html.append("<img class=\"toolbaricon\" src=\"images/emailimport.png\" onclick=\"getNextNode('newtab=emailimport')\" onmouseover=\"showTooltip(event, 'Email importieren')\" onmouseout=\"hideTooltip()\">");
				html.append("<img class=\"toolbaricon\" src=\"images/www.png\" onclick=\"openWebsite('" + currentController.getOrganisationMember().getString("AccessCode") + "','" + currentController.getOrganisationMember().getPath()+ "')\" onmouseover=\"showTooltip(event, 'Benutzer auf Website ansehen')\" onmouseout=\"hideTooltip()\">");
				
				html.append("<img class=\"toolbaricon\" src=\"images/feedback.png\" onclick=\"getNextNode('feedback=true')\" onmouseover=\"showTooltip(event, 'Feedback')\" onmouseout=\"hideTooltip()\">");
				
				html.append("</div>");
				html.append("<table width=\"100%\">");
				
				/*
				html.append("<tr><th style=\"width : 300px;\">Personenangaben</th>");
				for(int i = 0; i < tabids.length; i++){
					if(activetab.equals(tabids[i])){
						html.append("<td class=\"formtabactive\"><a href=\"javascript:getNextNode(\'newtab=" + tabids[i] + "\')\">" + tablabels[i] + "</td>");
					}
					else{
						html.append("<td class=\"formtabinactive\"><a href=\"javascript:getNextNode(\'newtab=" + tabids[i] + "\')\">" + tablabels[i] + "</td>");
					}
				}
				html.append("</tr>");	
				*/
				
				html.append("<tr><td valign=\"top\" style=\"border : 1px solid black; padding : 10px; width : 500px;\">");
				html.append("<form action=\"servlet\" id=\"processNodeForm\">");
				html.append("<table>");
				if(om != null){
					
					html.append("<input type=\"hidden\" name=\"current_omid\" value=\"" + om.getName() + "\">");
					addProperty(om.getProperty("MemberRole"));
					//html.append(HTMLForm.getTextField(om.getProperty("MemberRole"), true, ""));
					
					addTableRow(html, getProperty("Status"), "Status", 2);
					if(om.getID("Status")==3){
						html.append("<tr><td></td><td><input type=\"button\" value=\"Daten anonymisieren\" onclick=\"getNextNode('delete=true',true)\">");
					}
					html.append("<tr><td>Registriert seit</td><td>" + DateConverter.sqlToShortDisplay(om.getString("DateCreated"), true) + "</td></tr>");
					
					addTableRow(html, getProperty("NotificationMode"), "Informieren über", 2);
					
					Person person = om.getPerson();
					if(person != null){
						//html.append(HTMLForm.getTextField(person.getProperty("DateCreated"), true, ""));
		
						if(identity != null){
							addTableRow(html, getProperty("FamilyName"), "Nachname");
							addTableRow(html, getProperty("FirstName"), "Vorname");
							addTableRow(html, getProperty("DateOfBirth"), "Geburtsdatum");
							addTableRow(html, getProperty("Sex"), "Geschlecht", 2);
						}
						if(address != null){
							addTableRow(html, getProperty("AdditionalLine"), "Zusatzzeile");		
							addTableRow(html, getProperty("Street"), "Strasse");
							addTableRow(html, getProperty("Number"), "Nummer");
							addTableRow(html, getProperty("Zipcode"), "PLZ");
							addTableRow(html, getProperty("City"), "Ort");
							addTableRow(html, getProperty("Country"), "Land");
						}
					}
					addTableRow(html, getProperty("FirstLanguageS"), "Erstsprache");
					html.append("<tr><td class=\"inputlabel\" style=\"height : 120px;\">Weitere Sprachen</td><td>" + HTMLForm.getListField(getProperty("Languages") , true, "") + "</td></tr>");
					
					addTableRow(html, getProperty("Comment"), "Kommentar", 3);
					
					if(om.getObjects("OrganisationMemberModification").size() > 0){
						html.append("<tr><th class=\"inputlabel\">Mutationenen</th><td>");
						for(BasicClass bc : om.getObjects("OrganisationMemberModification")){
							if(bc.getID("Status")==0){
								html.append("<a href=\"javascript:getNextNode('modify=" + bc.getString("ID") +"')\">" + bc.getString("Title") +  DateConverter.sqlToShortDisplay(bc.getString("DateCreated")) + "</a><br>");
							}
						}
						html.append("</td></tr>");
						
					}
					
					if(login != null){
						addTableRow(html, getProperty("Username"), "Benutzername");
						addTableRow(html, getProperty("Password"), "Passwort");				
					}
					else{
						html.append("<tr><th>Login</th><td><input type=\"button\" value=\"Login erstellen\" onclick=\"getNextNode(\'createlogin=true\')\"></td></tr>");
					}
					
					if(person != null){	
					
						for(BasicClass bc : person.getObjects("Contact")){
							//addTableRow(html, getProperty(contacts[bc.getInt("Type")]), contacts[bc.getInt("Type")]);
	
							html.append("<tr><td>" + contacts[bc.getInt("Type")] + "</td>");
							html.append("<td>" + HTMLForm.getTextField(getProperty(contacts[bc.getInt("Type")]), true, "") + "</td><td><a href=\"javascript:getNextNode(\'deletecontact=true&contactid=" + bc.getName() + "\')\"><img src=\"images/delete.png\"></a></td></tr>");
						}
					
						
						html.append("<tr><th class=\"inputlabel\">Kontakt hinzufügen</th><td><select onchange=\"getNextNode(\'addcontact=true&contactid=\' + this.value)\">");
						html.append("<option value=\"\"></option>");
						int i = 0;
						for(String contacttype : contacts){
							
							html.append("<option value=\"" + i + "\">" + contacttype + "</option>");
							i++;
						}
					}
					
					html.append("<tr><td>Rollen:</td><tr>");
					for(BasicClass bc : om.getObjects("MemberRole")){
						if(bc.getID("Status")==0){
							html.append("<tr><td></td><td>");
							html.append(roles.get(bc.getString("Role")) + "</td><td><a href=\"javascript:getNextNode(\'deleterole=true&roleid=" + bc.getName() + "\')\"><img src=\"images/delete.png\"></a></td></tr>");
		
						}
					}
					html.append("</td></tr>");
					
		
					
					html.append("<tr><th class=\"inputlabel\">Rolle hinzufügen</th><td><select onchange=\"getNextNode(\'addrole=true&roleid=\' + this.value)\">");
					html.append("<option value=\"\"></option>");
					for(BasicClass bc : ocs.getObjects("Role")){
						html.append("<option value=\"" + bc.getName() + "\">" + bc.getString("Title") + "</option>");
					}
					html.append("</select></td></tr>");
					if(om.getObjects("OrganisationMemberRelationship").size() > 0){
						html.append("<tr><th class=\"inputlabel\">Verknüpfte Profile</th><td>");
						for(BasicClass bc : om.getObjects("OrganisationMemberRelationship")){
							if(bc.getID("Status")==0){
								html.append("<a href=\"javascript:getNextNode('OMID2=" + bc.getString("OrganisationMember") +"')\">" + bc.getString("Title") + ", " + bc.getObject("Role") + "</a><a href=\"javascript:getNextNode(\'deactivaterelation=true&relationid=" + bc.getName() + "\')\"><img src=\"images/delete.png\"></a><br>");
							}
						}
						html.append("</td></tr>");
						
					}
					
					
				}
				
				
				html.append("</table>");
				//html.append("<input class=\"rightButton\" type=\"button\" value=\"" + "Abbrechen" + "\" onClick=\"cancelProcess()\">");
				//html.append("<input class=\"rightButton\" type=\"button\" value=\"" + "Speichern" + "\" onClick=\"getNextNode()\">");
				html.append("</form>");
				
				html.append("</td><td valign=\"top\" colspan=\"4\">");
				
				if(activetab.equals("memberad")){
					
					//html.append("<input type=\"button\" value=\"Email import\" onclick=\"getNextNode('newtab=emailimport')\">");
					//html.append("<input type=\"button\" value=\"Website öffnen\" onclick=\"openWebsite('" + om.getString("AccessCode") + "','" + om.getPath()+ "')\">");
					
					html.append("<h4>Feedbacks</h4>");
					
					html.append("<table><tr><td>Abgegebene Feedbacks<td><td>Einsatz zustande gekommen</td><td>");
					
					getFeedbackBar(ocs, html, om.getName(), "", true);
					
					html.append("</td></tr><tr><td></td><td><td>Einsatz nicht zustande gekommen</td><td>");
					
					getFeedbackBar(ocs, html, om.getName(), "", false);
					
					html.append("<tr><td>Erhaltene Feedbacks<td><td>Einsatz zustande gekommen</td><td>");
					
					getFeedbackBar(ocs, html, "", om.getName(), true);
					
					html.append("</td></tr><tr><td></td><td><td>Einsatz nicht zustande gekommen</td><td>");
					
					getFeedbackBar(ocs, html, "", om.getName(), false);
					
					html.append("</td></tr></table>");
					
					html.append("<hr>");
					
					html.append("<h4>Inserate  <img src=\"images/plus.png\" onclick=\"expandElement('memeberads')\"> <img src=\"images/minus.png\" onclick=\"shrinkElement('memeberads', 150)\"></h4>");
				
					html.append("<input type=\"button\" value=\"Neues Inserat\" onclick=\"getNextNode(\'memberadcreate=true\')\">");
					
					html.append("<div id=\"memeberads\" style=\"height : 150px; overflow : auto;\">");
					
					html.append("<table>");
					html.append("<tr><th>ID</th><th>Typ</th><th>Rubrik</th><th>Titel</th><th>Gültig von</th><th>Gültig bis</th><th>Status</th></tr>");
					
					boolean even = true;
					om.getObjectCollection("MemberAd").sort("ValidFrom", false);
					for(BasicClass bc : om.getObjects("MemberAd")){
						if(even){
							html.append("<tr class=\"even\">");
						}
						else{
							html.append("<tr class=\"odd\">");						
						}
						
						
						html.append("<td class=\"datacell\">" + bc.getName() + "</td>");
						html.append("<td class=\"datacell\">" + bc.getObject("Type") + "</td>");
						html.append("<td class=\"datacell\">" + bc.getString("Template") + "</td>");
						html.append("<td class=\"datacell\">" + bc.getString("Title") + "</td>");
						html.append("<td class=\"datacell\">" + DateConverter.sqlToShortDisplay(bc.getString("ValidFrom"), false) + "</td>");
						html.append("<td class=\"datacell\">" + DateConverter.sqlToShortDisplay(bc.getString("ValidUntil"), false) + "</td>");
						html.append("<td class=\"datacell\">" + bc.getString("Status") + "</td>");
						html.append("<td><a href=\"javascript:getNextNode(\'memberadid=" + bc.getName() + "\')\">Details</a></td>");
						html.append("</tr>");		
						
						if(even){
							html.append("<tr class=\"even\">");
						}
						else{
							html.append("<tr class=\"odd\">");						
						}
						try{
							html.append("<td></td><td colspan=\"5\" class=\"datacell\">" + bc.getString("Description") + "</td></tr>");
						}
						catch(java.lang.Exception e){
							html.append(e.toString());	
						}
						
						even = !even;
					}
					html.append("</table>");
					html.append("</div>");
					                                                                                                                          
					
					html.append("<p>");
					html.append("<hr>");
					html.append("<h4>Adressbestellungen  <img src=\"images/plus.png\" onclick=\"expandElement('requests1')\"> <img src=\"images/minus.png\" onclick=\"shrinkElement('requests1', 150)\"></h4>");
					
	
					html.append("<input type=\"button\" value=\"Neue Adressbestellung\" onclick=\"getNextNode('createrequest=true')\">");
				
					String[] colors = {"", "orange", "green", "red", "blue"};
					
					html.append("<div id=\"requests1\" style=\"height : 150px; overflow : auto;\">");
					
					html.append("<table>");
					
					html.append("<tr><td colspan=\"4\"><b>Bestellte Adressen:</b></td></tr>");
					
					//html.append("<tr><th>Inserat</th><th>Rubrik</th><th colspan=\"2\">Inhaber/Besteller</th><th>Sichtbar bis</th><th>Status</th></tr>");
					//html.append("<tr><th>Inserat</th><th>Inhaber/Besteller</th><th>Feedback</th></tr>");
			
					
						
						String sql = "SELECT t1.*,   t2.ID AS MemberAdID, t2.Title AS MemberAdTitle, t3.Title AS Category, t4.ID AS OMID2, t6.FamilyName, t6.FirstName, t6.Sex, t6.DateOfBirth, t7.Street, t7.Number, t7.ZipCode, t7.City, ";
						sql += " t8.ID AS FeedbackID, t8.Reason_1, t8.Reason_2, t8.Reason_3, t8.Reason_4, t8.Reason_5, t8.Reason_6";
						sql += " FROM MemberAdRequest AS t1";
						sql += " LEFT JOIN MemberAd AS t2 ON t1.MemberAd=t2.ID";
						sql += " LEFT JOIN MemberAdCategory AS t3 ON t2.Template=t3.ID";
						sql += " LEFT JOIN OrganisationMember AS t4 ON t2.OrganisationMemberID=t4.ID";
						sql += " LEFT JOIN Person AS t5 ON t4.Person=t5.ID";
						sql += " LEFT JOIN Identity AS t6 ON t6.PersonID=t5.ID";
						sql += " LEFT JOIN Address AS t7 ON t7.PersonID=t5.ID";
						sql += " LEFT JOIN Feedback AS t8 ON t8.MemberAdRequestID=t1.ID";
						
						sql += " WHERE t1.OrganisationMemberID=" + om.getName();
						
						sql += " ORDER BY DateCreated DESC";
						
						//sql += " AND t1.Status IN (1,2)";
						
						ObjectCollection results = new ObjectCollection("Results", "*");
						
						ocs.queryData(sql, results);
					
					
					
						
					String prevdate = "";
					String classname = "even";
					
					for(BasicClass bc : results.getObjects()){
						
						if(even){
							classname = "even";
						}
						else{
							classname = "odd";			
						}
						even = !even;
						
						String date = DateConverter.sqlToShortDisplay(bc.getString("DATECREATED"), false);
						if(date.length()==0){
							date = DateConverter.sqlToShortDisplay(bc.getString("VALIDFROM"), false);
						}
						
						if(!date.equals(prevdate)){
							html.append("<tr><td>" + date + "</td></tr>");	
						}
						
						int status = bc.getInt("STATUS");
						String color = "black";
						String feedbackcolor = "orange";
						
						if(status == 3){
							//color = "red";	
						}
						
						

						


						html.append("<tr class=\"" + classname + "\">");
						html.append("<td><a href=\"javascript:getNextNode('deletememberadrequest=true&memberadrequestid=" + bc.getString("ID") +"')\" style=\"color : " + color + ";\"><img src=\"images/delete.png\"></a>");
						html.append("<td class=\"datacell\"  style=\"color : " + color + ";\"><a href=\"javascript:getNextNode('OMID2=" + bc.getString("OMID2") +"')\" style=\"color : " + color + ";\">" + bc.getString("FAMILYNAME") + " " + bc.getString("FIRSTNAME") + "</a></td>");
						html.append("<td class=\"datacell\">" + bc.getString("ZIPCODE") + " " + bc.getString("CITY") + "</td>");
						html.append("<td class=\"datacell\" style=\"color : " + color + ";\">" + bc.getString("MEMBERADTITLE") + "</a></td><td><a href=\"javascript:getNextNode(\'memberadid=" + bc.getString("MEMBERADID") + "\')\"  style=\"color : " + color + ";\">Details</a></td>");
						html.append("<tr class=\"" + classname + "\"><td class=\"datacell\" colspan=\"4\">" + bc.getString("CATEGORY") + ": " + bc.getString("USERCOMMENT") + "</td></tr>");
						
						//html.append("<td class=\"datacell\">" + bc.getString("FIRSTNAME") + "</td>");
						//html.append("<td class=\"datacell\">" + DateConverter.sqlToShortDisplay(bc.getString("VALIDUNTIL"), false) + "</td>");
						
						//html.append("<td  class=\"datacell\" bgcolor=\"" + colors[bc.getInt("STATUS")] + "\"> </td>");
						
						/*
	
						
						if(feedbackid.length() > 0){
							
							boolean reason_1 = bc.getBoolean("REASON_1");
							boolean reason_2 = bc.getBoolean("REASON_2");
							boolean reason_3 = bc.getBoolean("REASON_3");
							boolean reason_4 = bc.getBoolean("REASON_4");
							boolean reason_5 = bc.getBoolean("REASON_5");
							boolean reason_6 = bc.getBoolean("REASON_6");
							if(reason_1 || reason_2){
								feedbackcolor = "red";			
							}					
							html.append("<td class=\"datacell\"><a href=\"javascript:showFeedback(" + bc.getString("FEEDBACKID") + ")\" style=\"color : " + feedbackcolor + ";\">Feedback</td>");
							
						}
						*/
						html.append("</tr>");
						
						/*
						if(bc.getString("ID").equals(memberadrequestid)){
							html.append("<tr><td colspan=\"8\">");
							
							sql = "SELECT t1.* FROM Feedback AS t1 LEFT JOIN MemberAdRequest AS t2";
							sql += " ON t1.MemberAdRequestID=t2.ID WHERE t2.ID=" + memberadrequestid;
							
							html.append("<h4>Feedback</h4>");
							html.append("<table>");
							getFeedback(ocs, html, sql);
							html.append("</table>");
							html.append("</td></tr>");
							
						}
						*/
						prevdate = date;
					}
					
					html.append("</table>");
					
					html.append("</div>");
					
					
					html.append("<hr>");
					
				//}	 //20160202, alles soll auf eine Seite
				//else if(activetab.equals("memberadrequest")){	
					
					
					html.append("<h4>Adresse wurde weitergegeben <img src=\"images/plus.png\" onclick=\"expandElement('requests2')\"> <img src=\"images/minus.png\" onclick=\"shrinkElement('requests2', 150)\"></h4>");
					
					html.append("<div id=\"requests2\" style=\"height : 150px; overflow : auto;\">");
					
					html.append("<table>");
					
					html.append("<tr><th></th><th>Datum</th><th>Inhaber/Besteller</th><th>PLZ / Ort</th><th>Inserat</th></tr>");
					
					results = new ObjectCollection("Results", "*");
					
	
					
						sql = "SELECT t1.*, t2.ID AS MemberAdID, t2.Title AS MemberAdTitle, t3.Title AS Category, t4.ID AS OMID2,  t6.FamilyName, t6.FirstName, t6.Sex, t6.DateOfBirth, t7.Street, t7.Number, t7.ZipCode, t7.City, ";
						sql += " t8.ID AS FeedbackID, t8.Reason_1, t8.Reason_2, t8.Reason_3, t8.Reason_4, t8.Reason_5, t8.Reason_6";
						sql += " FROM MemberAdRequest AS t1";
						sql += " LEFT JOIN MemberAd AS t2 ON t1.MemberAd=t2.ID";
						sql += " LEFT JOIN MemberAdCategory AS t3 ON t2.Template=t3.ID";
						sql += " LEFT JOIN OrganisationMember AS t4 ON t1.OrganisationMemberID=t4.ID";
						sql += " LEFT JOIN Person AS t5 ON t4.Person=t5.ID";
						sql += " LEFT JOIN Identity AS t6 ON t6.PersonID=t5.ID";
						sql += " LEFT JOIN Address AS t7 ON t7.PersonID=t5.ID";
						sql += " LEFT JOIN Feedback AS t8 ON t8.MemberAdRequestID=t1.ID";
						
						sql += " WHERE t2.OrganisationMemberID=" + om.getName();
						
						sql += " ORDER BY DateCreated DESC";
						
					ocs.queryData(sql, results);
					
					
									
						
					for(BasicClass bc : results.getObjects()){
						
						if(even){
							classname = "even";
						}
						else{
							classname = "odd";			
						}

						even = !even;
						
						int status = bc.getInt("STATUS");
						String color = "black";
						String feedbackcolor = "orange";
						if(status == 3){
							color = "red";	
						}
						
						String date = DateConverter.sqlToShortDisplay(bc.getString("DATECREATED"), false);
						
						if(date.length()==0){
							date = DateConverter.sqlToShortDisplay(bc.getString("VALIDFROM"), false);
						}
						
						html.append("<tr class=\"" + classname + "\">");
						
						
						html.append("<td><a href=\"javascript:getNextNode('deletememberadrequest=true&memberadrequestid=" + bc.getString("ID") +"')\" style=\"color : " + color + ";\"><img src=\"images/delete.png\"></a>");
						
						html.append("<td><a href=\"<td class=\"datacell\">" + date + "</td>");
						
						html.append("<td class=\"datacell\"><a href=\"javascript:getNextNode('OMID2=" + bc.getString("OMID2") +"')\" style=\"color : " + color + ";\">" + bc.getString("FAMILYNAME") + " " +  bc.getString("FIRSTNAME") + "</a></td>");
						html.append("<td class=\"datacell\">" + bc.getString("ZIPCODE") + " " + bc.getString("CITY") + "</td>");
						html.append("<td class=\"datacell\">" + bc.getString("MEMBERADTITLE") + "</td><td><a href=\"javascript:getNextNode(\'memberadid=" + bc.getString("MEMBERADID") + "\')\" style=\"color : " + color + ";\">Details</a></td>");
						//html.append("<td class=\"datacell\">" + bc.getString("CATEGORY") + "</td>");
						
						//html.append("<td class=\"datacell\">" + bc.getString("FIRSTNAME") + "</td>");
						//html.append("<td class=\"datacell\">" + DateConverter.sqlToShortDisplay(bc.getString("VALIDUNTIL"), false) + "</td>");
						//html.append("<td class=\"datacell\" bgcolor=\"" + colors[bc.getInt("STATUS")] + "\"> </td>");
						
						html.append("</tr>");
						html.append("<tr class=\"" + classname + "\">");
						html.append("<td class=\"datacell\"colspan=\"4\">" + bc.getString("USERCOMMENT") + "</td>");
						
						
						/*
						String feedbackid = bc.getString("FEEDBACKID");
						
						if(feedbackid.length() > 0){
							
							boolean reason_1 = bc.getBoolean("REASON_1");
							boolean reason_2 = bc.getBoolean("REASON_2");
							boolean reason_3 = bc.getBoolean("REASON_3");
							boolean reason_4 = bc.getBoolean("REASON_4");
							boolean reason_5 = bc.getBoolean("REASON_5");
							boolean reason_6 = bc.getBoolean("REASON_6");
							if(reason_1 || reason_2){
								feedbackcolor = "red";			
							}					
							html.append("<td class=\"datacell\"><a href=\"javascript:showFeedback(" + bc.getString("FEEDBACKID") + ")\" style=\"color : " + feedbackcolor + ";\">Feedback</td>");
							
						}
						*/
						html.append("</tr>");
						/*
						if(bc.getString("ID").equals(memberadrequestid)){
							html.append("<tr><td colspan=\"8\">");
							
							sql = "SELECT t1.* FROM Feedback AS t1 LEFT JOIN MemberAdRequest AS t2";
							sql += " ON t1.MemberAdRequestID=t2.ID WHERE t2.ID=" + memberadrequestid;
							
							html.append("<h4>Feedback</h4>");
							html.append("<table>");
							getFeedback(ocs, html, sql);
							html.append("</table>");
							html.append("</td></tr>");
							
						}
						*/
					}
					
					html.append("</table>");
					
					html.append("</div>");
					
					results.removeObjects();
					
	

				//} //20160202, alles soll auf eine Seite
				//else if(activetab.equals("activity")){	
				
					/*
					html.append("<p>");
					html.append("<hr>");
					html.append("<h4>Journal</h4>");
					
					
					String[] status = {"offen", "erledigt"};
					
					html.append("Neue Aktivität:");
					html.append(HTMLForm.getSelection(getProperty("objecttemplate"), "",  "", true, true, true, "", "getNextNode('objecttemplate=' + this.value )"));
					
					html.append("<div style=\"height : 150px; overflow : auto;\">");
					
					html.append("<table>");
					
					//boolean even = true;
					
					for(BasicClass bc : om.getObjects("Activity")){
						if(even){
							html.append("<tr class=\"even\">");
						}
						else{
							html.append("<tr class=\"odd\">");						
						}
						even = !even;
						
						html.append("<td class=\"datacell\">" + DateConverter.sqlToShortDisplay(bc.getString("DateCreated"), false) + "</td>");
						html.append("<td class=\"datacell\">" + bc.getString("Template") + "</td>");
						html.append("<td class=\"datacell\">" + bc.getString("Date") + "</td>");
						html.append("<td class=\"datacell\">" + bc.getString("Title") + "</td>");
						html.append("<td class=\"datacell\">" + bc.getString("Title") + "</td>");
						html.append("<td class=\"datacell\">" + status[bc.getID("Status")] + "</td>");
						html.append("<td class=\"datacell\"><a href=\"javascript:getNextNode(\'activityid=" + bc.getName() + "\')\">Details</a></td>");
						html.append("</tr>");
					}
					html.append("</table>");
					*/
					
				}
				else if(activetab.equals("feedback")){	
					
					ObjectCollection results = new ObjectCollection("Results", "*");
	
					html.append("<table>");
					
					html.append("<tr><td colspan=\"5\"><b>Erhaltene Feedbacks zu Einsätzen</b></td></tr>");
					
					html.append("<tr>");
					html.append("<th>Datum</th>");
					html.append("<th>Einsatz zust. gekommen</th>");
					html.append("<th>Nicht erreicht</th>");
					html.append("<th>Unzuverlässig</th>");
					html.append("<th>Keine Zeit</th>");
					html.append("<th>Passt persönlich nicht</th>");
					html.append("<th>Anderes</th>");
					html.append("<th>Wie war der Einsatz</th>");
					html.append("<th>Besonders gut</th>");
					html.append("<th>Nicht gut</th>");
					html.append("<th>Kommentar</th>");
					html.append("</tr>");
						
	
					
					String sql = "SELECT t1.* FROM Feedback AS t1 WHERE t1.OrganisationMember=" + om.getName();
					
					ocs.queryData(sql, results);
					
					boolean even = true;
					
					for(BasicClass bc : results.getObjects()){
						
						if(even){
							html.append("<tr class=\"even\">");
						}
						else{
							html.append("<tr class=\"odd\">");
						}
						even = !even;
						
						html.append("<td>" + DateConverter.sqlToShortDisplay(bc.getString("DATECREATED"), false) + "</td>");
						html.append("<td>" + bc.getString("CONTACTESTABLISHED") + "</td>");
						html.append("<td>" + yes_no.get(bc.getString("REASON_1")) + "</td>");
						html.append("<td>" + yes_no.get(bc.getString("REASON_2")) + "</td>");
						html.append("<td>" + yes_no.get(bc.getString("REASON_3")) + "</td>");
						html.append("<td>" + yes_no.get(bc.getString("REASON_4")) + "</td>");
						html.append("<td>" + yes_no.get(bc.getString("REASON_5")) + "</td>");
						html.append("<td>" + quality.get(bc.getString("QUALITY")) + "</td>");
						html.append("<td>" + bc.getString("HIGHLIGHTS") + "</td>");
						html.append("<td>" + bc.getString("PROBLEMS") + "</td>");
						html.append("<td>" + bc.getString("COMMENTS") + "</td>");
						html.append("<tr>");	
					}
					
					
					
					html.append("</table>");
					
					html.append("</div>");
					
				}
				html.append("</td></tr>");
				html.append("</table>");
				
			}
		}
		catch(java.lang.Exception e){
			ocs.logException(e);	
		}
		return html.toString();
	
	}
	public static void getFeedbackBar(OpenCommunityServer ocs, StringBuilder html, String owner, String omid, boolean contactestablished){
		
		String sql = "SELECT * FROM Feedback AS t1";
		if(owner.length() > 0){
			sql += " WHERE OrganisationMemberID=" + owner;
		}
		else{
			sql += " WHERE OrganisationMember=" + omid;	
		}
		if(contactestablished){
			sql += " AND ContactEstablished = 'true' ";	
		}
		else{
			sql += " AND ContactEstablished = 'false' ";				
		}
		sql += " ORDER BY DateCreated";
		ObjectCollection results = new ObjectCollection("Results", "*");
		
		html.append("<table><tr style=\"height : 30px;\">");
		
		ocs.queryData(sql, results);
		String style = null;
		for(BasicClass bc : results.getObjects()){
			if(bc.getBoolean("CONTACTESTABLISHED")==true){
				int quality = bc.getInt("QUALITY");
				if(quality == 1){
					style = "border : 1px solid black; background : red;";
				}
				else if(quality == 2){
					style = "border : 1px solid black; background : orange;";
				}
				else{
					style = "border : 1px solid black; background : green;";
				}
				
			}
			else{
				boolean reason_1 = bc.getBoolean("REASON_1");
				boolean reason_2 = bc.getBoolean("REASON_2");
				boolean reason_3 = bc.getBoolean("REASON_3");
				boolean reason_4 = bc.getBoolean("REASON_4");
				boolean reason_5 = bc.getBoolean("REASON_5");
				boolean reason_6 = bc.getBoolean("REASON_6");
				if(reason_1 || reason_2 || reason_4){
					style = "border : 1px solid black; background : red;";					
				}
				else{
					style = "border : 1px solid black; background : orange;";					
				}
				
			}
			html.append("<td width=\"50px\" style=\"" + style + "\" onclick=\"showFeedback(" + bc.getString("ID") + ")\"></td>");
				
		}
		html.append("</tr></table>");
		
	}
	public void getFeedback(OpenCommunityServer ocs, StringBuilder html, String sql){
		
		boolean even = false;
		
		ObjectCollection results = new ObjectCollection("Results", "*");
		
		//html.append("<tr><td colspan=\"5\"><b>Erhaltene Feedback zu bestellten Adressen</b></td></tr>");
				
		html.append("<tr>");
		html.append("<th>Datum</th>");
		html.append("<th>Einsatz zust. gekommen</th>");
		html.append("<th>Nicht erreicht</th>");
				html.append("<th>Unzuverlässig</th>");
				html.append("<th>Keine Zeit</th>");
				html.append("<th>Passt persönlich nicht</th>");
				html.append("<th>Anderes</th>");
				html.append("<th>Wie war der Einsatz</th>");
				html.append("<th>Besonders gut</th>");
				html.append("<th>Nicht gut</th>");
		html.append("<th>Kommentar</th>");
		html.append("</tr>");
				

				
		ocs.queryData(sql, results);
				
		for(BasicClass bc : results.getObjects()){
			if(even){
				html.append("<tr class=\"even\">");
			}
			else{
				html.append("<tr class=\"odd\">");
			}
			even = !even;
					
					html.append("<td>" + DateConverter.sqlToShortDisplay(bc.getString("DATECREATED"), false) + "</td>");
					html.append("<td>" + bc.getString("CONTACTESTABLISHED") + "</td>");
					html.append("<td>" + yes_no.get(bc.getString("REASON_1")) + "</td>");
					html.append("<td>" + yes_no.get(bc.getString("REASON_2")) + "</td>");
					html.append("<td>" + yes_no.get(bc.getString("REASON_3")) + "</td>");
					html.append("<td>" + yes_no.get(bc.getString("REASON_4")) + "</td>");
					html.append("<td>" + yes_no.get(bc.getString("REASON_5")) + "</td>");
					html.append("<td>" + quality.get(bc.getString("QUALITY")) + "</td>");
					html.append("<td>" + bc.getString("HIGHLIGHTS") + "</td>");
					html.append("<td>" + bc.getString("PROBLEMS") + "</td>");
					html.append("<td>" + bc.getString("COMMENTS") + "</td>");
					html.append("<tr>");	
		}
		
		
	}
	public void finish(ProcessResult result, ApplicationContext context){
		/*
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		if(om != null){
			ocs.logAccess("Status: " + om.getString("Status"));
			ocs.logAccess("Comment: " + om.getString("Comment"));
			ocs.updateObject(om);
		}
		if(identity != null){
			ocs.updateObject(identity);		
		}
		if(address != null){
			ocs.updateObject(address);		
		}
		if(login != null){
			ocs.updateObject(login);
		}
		Person person = om.getPerson();
		person.setProperty("Languages", getString("Languages"));
		ocs.updateObject(person);	
		
		for(BasicClass contact : person.getObjects("Contact")){
			contact.setProperty("Value", getString(contacts[contact.getID("Type")]));
			ocs.updateObject(contact);		
		}
		*/
		
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
		userSession.setOrganisationMember(null);
		result.setParam("refresh", getString("sectionid"));
	}
	public void cancel(ApplicationContext context){
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
		userSession.setOrganisationMember(null);		
	}
	public void addTableRow(StringBuilder html, Property p, String label){
		addTableRow(html, p, label, 1);
	}
	public void addTableRow(StringBuilder html, Property p, String label, int type){
		html.append("<tr><td>" + label + "</td>");
		if(type==1){
			html.append("<td>" + HTMLForm.getTextField(p , true, "") + "</td></tr>");
		}
		else if(type==2){
			html.append("<td>" + HTMLForm.getSelection(p , true, "") + "</td></tr>");		
		}
		else if(type==3){
			html.append("<td>" + HTMLForm.getTextArea(p , true, "textarea_small") + "</td></tr>");		
		}
	}
	public List<String> getPropertyNames(){
		if(subprocess != null){
			return subprocess.getPropertyNames();	
		}
		else{
			return currentController.getPropertyNames();	
		}
	}
	public void setProperty(String name, Object value){
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		
		ocs.logAccess(name + ":" + value);
		
		if(subprocess != null && subprocess.hasProperty(name)){
			subprocess.setProperty(name, value);
		}
		else if(currentController != null && currentController.hasProperty(name)){
			currentController.setProperty(name, value);
		}
		else{
			super.setProperty(name, value);	
		}
		
	}
	

}
