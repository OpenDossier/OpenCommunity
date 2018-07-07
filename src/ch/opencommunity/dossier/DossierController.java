package ch.opencommunity.dossier;

import ch.opencommunity.server.OpenCommunityServer;
import ch.opencommunity.common.OpenCommunityUserSession;
import ch.opencommunity.base.BaseController;
import ch.opencommunity.base.Activity;
import ch.opencommunity.base.ActivityOrganisationMember;
import ch.opencommunity.base.ActivityParticipant;
import ch.opencommunity.base.ObjectTemplate;
import ch.opencommunity.base.FieldDefinition;
import ch.opencommunity.base.Parameter;
import ch.opencommunity.base.Note;
import ch.opencommunity.base.Contact;
import ch.opencommunity.base.OrganisationalUnit;
import ch.opencommunity.base.OrganisationMemberInfo;
import ch.opencommunity.dossier.ObjectDetail;
import ch.opencommunity.view.DossierView;
import ch.opencommunity.view.ActivityView;
import ch.opencommunity.view.ProjectView;

import org.kubiki.base.Property;
import org.kubiki.base.BasicClass;
import org.kubiki.base.ConfigValue;
import org.kubiki.base.ActionResult;
import org.kubiki.base.ActionHandler;
import org.kubiki.base.ObjectCollection;
import org.kubiki.application.ApplicationContext;
import org.kubiki.application.server.WebApplicationContext;

import java.util.Vector;

public class DossierController extends BaseController{
	
	
	Dossier dossier;
	Activity activity = null;
	public Project project;
	
	ObjectCollection organisatonmembers;
	
	int mode = 1;
	
	String activityfilter = null;
	
	public DossierController(Dossier dossier){	
		
		this.dossier = dossier;
		
		addProperty("objecttemplate", "Integer", "");
		
		addObjectCollection("temp", "*");
		organisatonmembers = addObjectCollection("organisationmembers", "*");
			
		OpenCommunityServer ocs = (OpenCommunityServer)dossier.getRoot();
		
		Vector<BasicClass> templates = ocs.getObjectTemplates();
		Vector<BasicClass> templates2 = new Vector<BasicClass>();
		for(BasicClass bc : templates){
			if(bc.getID("Scope") > 1){
				templates2.add(bc);	
			}
		}
		
		getProperty("objecttemplate").setSelection(templates2);
		
	}
	public Dossier getDossier(){
		return dossier;	
	}
	public String toString(){
		return "" + dossier;
	}
	public ActionResult onAction(BasicClass source, String command, ApplicationContext context) {
		
		ActionResult result = null;
		OpenCommunityServer ocs = (OpenCommunityServer)dossier.getRoot();
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
		if(command.equals("activityadd")){
			
			BasicClass parent = null;
			
			if(context.hasProperty("projectid")){
				
				String projectid = context.getString("projectid");
				parent = dossier.getObjectByName("Project", projectid);

			}
			else{
				parent = (CaseRecord)dossier.getObjectByIndex("CaseRecord", 0);
				
			}
			
			if(parent != null){
			
				
				activity = createActivity(parent, context);	
				if(activity != null){
					result = new ActionResult(ActionResult.Status.OK, "Aktivität geladen");
					result.setParam("dataContainer", "objectEditArea");
					StringBuilder html = new StringBuilder();
					ActivityView.toHTML2(html, this, activity, context);
					result.setData(html.toString());
				}
				
			}
		}
		else if(command.equals("saveproject")){

			String projectPath = context.getString("projectPath");	
			
			ocs.logAccess("projectPath : " + projectPath);
			if(projectPath != null){
				Project project = (Project)ocs.getObjectByPath(projectPath, userSession);
				ocs.logAccess("project : " + project);
				if(project != null){
					project.saveObject(context, "");	
					result = new ActionResult(ActionResult.Status.OK, "Projekt gespeichert");	
					result.setParam("objectlist", getPath());
				}
				
			}
			
		}
		else if(command.equals("saveactivity")){
			String activityid = context.getString("activityid");
			String activityPath = context.getString("activityPath");
			result = new ActionResult(ActionResult.Status.OK, "Aktivität geladen");	
			Activity activity = null;
			if(activityid != null && activityPath != null){
				if(activityid.equals("-1")){
					activity = (Activity)getObjectByName("temp", activityid);	
					
					if(context.hasProperty("send") && context.getString("send").equals("true")){
						activity.setProperty("Status", "0");
					}
					
					activity.saveObject(context, "");
					
					activityid = activity.getName();
					
					ocs.logAccess("saving activity " + activityid);
					
					String projectid = activity.getString("ProjectID");
					
					ocs.logAccess("project " + projectid);
					
					if(!projectid.isEmpty()){
						Project project = (Project)dossier.getObjectByName("Project", projectid);
						if(project != null){
							activity = (Activity)project.getObjectByName("Activity", activityid);
						}
					}
					
					if(context.hasProperty("createpdf") && context.getString("createpdf").equals("true")){			
						String filename = ocs.createPDF(activity, null);
						result.setParam("download", filename);
					}
				}
				else{
					activity = (Activity)ocs.getObjectByPath(activityPath, userSession);
					
					/*
					CaseRecord caseRecord = (CaseRecord)dossier.getObjectByIndex("CaseRecord", 0);
					if(caseRecord != null){
						activity = (Activity)caseRecord.getObjectByName("Activity", activityid);
						if(activity != null){
							activity.saveObject(context, "");
						}
					}
					
					if(context.hasProperty("createpdf") && context.getString("createpdf").equals("true")){			
						String filename = ocs.createPDF(activity, null);
						result.setParam("download", filename);
					}
					
					*/
					
					if(activity != null){
						
						if(context.hasProperty("send") && context.getString("send").equals("true")){
							activity.setProperty("Status", "0");
						}
						if(context.hasProperty("createpdf") && context.getString("createpdf").equals("true")){			
							String filename = ocs.createPDF(activity, null);
							result.setParam("download", filename);
						}
						activity.saveObject(context, "");
					}
				}
			}
			StringBuilder html = new StringBuilder();
			ActivityView.toHTML2(html, this, activity, context);
			result.setData(html.toString());
			result.setParam("dataContainer", "objectEditArea");
			if(activity.getID("ProjectID") > 0){
				result.setParam("exec", "onAction('" + getPath() + "','editproject','','projectid=" + activity.getID("ProjectID") + "')");
			}
			else{
				result.setParam("journal", getPath());
			}
			if(context.hasProperty("send") && context.getString("send").equals("true")){
				ocs.sendAllPendingMails();
			}
		}
		else if(command.equals("editactivity")){
			result = new ActionResult(ActionResult.Status.OK, "Aktivität geladen");	
			String activityid = context.getString("activityid");
			if(activityid != null){
				
				BasicClass parent = null;
				
				if(context.hasProperty("projectid")){
					
					String projectid = context.getString("projectid");
					parent = dossier.getObjectByName("Project", projectid);
	
				}
				else{
					parent = (CaseRecord)dossier.getObjectByIndex("CaseRecord", 0);
					
				}

				if(parent != null){
					Activity activity = (Activity)parent.getObjectByName("Activity", activityid);
					if(activity != null){
						StringBuilder html = new StringBuilder();
						ActivityView.toHTML2(html, this, activity, context);
						result.setData(html.toString());
						result.setParam("dataContainer", "objectEditArea");
						activeObject = activity;
					}
				}
			}
		}
		else if(command.equals("recipientadd")){
			
			String activityPath = context.getString("activityPath");
			Activity activity = (Activity)ocs.getObjectByPath(activityPath, userSession);
			if(activity != null){
				
				String omid = context.getString("omid");
				if(omid != null){
					boolean exists = false;
					//for(BasicClass recipient : activity.getObjects("ActivityOrganisationMember")){

					for(BasicClass recipient : activity.getObjects("ActivityParticipant")){
						if(omid.equals("0")){ // Institution selber
							if(recipient.getString("OrganisationalUnit").equals(dossier.getOrganisationalUnit().getName())){
								exists = true;	
							}							
						}
						else{
							if(recipient.getString("OrganisationMember").equals(omid)){
								exists = true;	
							}
						}
					}
					
					if(!exists){
						OrganisationMemberInfo omi = ocs.getOrganisationMemberInfo(omid);
						if(omi != null){
							ActivityParticipant aom = new ActivityParticipant();
							
							if(omid.equals("0")){
								aom.setProperty("OrganisationalUnit", dossier.getOrganisationalUnit());
								aom.setProperty("Title", "Institution allgemein");
							}
							else{
								aom.setProperty("OrganisationMember", omid);
								aom.setProperty("Title", omi.getString("FirstName") + " " + omi.getString("FamilyName"));
							}
							
							if(!activity.getName().equals("-1")){
								aom.addProperty("ActivityID", "String", activity.getName());
								String id = ocs.insertSimpleObject(aom);
								ocs.getObject(activity, "ActivityParticipant", "ID", id);
							}
							else{
								activity.addSubobject("ActivityParticipant", aom);	
							}

							
							result = new ActionResult(ActionResult.Status.OK, "Empfänger hinzugefügt");	
							
							StringBuilder html = new StringBuilder();
							ActivityView.toHTML2(html, this, activity, context);
							result.setData(html.toString());
							result.setParam("dataContainer", "objectEditArea");
						}
					}
					
				}
			}
			
		}
		else if(command.equals("recipientdelete")){	
			String activityPath = context.getString("activityPath");
			Activity activity = (Activity)ocs.getObjectByPath(activityPath, userSession);
			
			ocs.logAccess("deleting ...");
			if(activity != null){
				
				String recipientid = context.getString("recipientid");
				
				ocs.logAccess("deleting ..." + recipientid);
				if(recipientid != null){
					
					ActivityParticipant activityParticipant = (ActivityParticipant)activity.getObjectByName("ActivityParticipant", recipientid);
					
					ocs.logAccess("deleting ..." + activityParticipant);
					if(activityParticipant != null){
						ocs.deleteObject(activityParticipant);	
					}
				}
			}
			result = new ActionResult(ActionResult.Status.OK, "Empfänger gelöscht");	
							
			StringBuilder html = new StringBuilder();
			ActivityView.toHTML2(html, this, activity, context);
			result.setData(html.toString());
			result.setParam("dataContainer", "objectEditArea");
			
		}
		
		else if(command.equals("editproject")){
			result = new ActionResult(ActionResult.Status.OK, "Aktivität geladen");	
			String projectid = context.getString("projectid");	
			if(projectid != null){
				project = (Project)dossier.getObjectByName("Project", projectid);
				if(project != null){
					StringBuilder html = new StringBuilder();		
					ProjectView.toHTML(html, this, project, context);
					result.setData(html.toString());
					result.setParam("dataContainer", "projectEditArea");
					activeObject = project;
					
				}
				
			}
			
		}
		else if(command.equals("showprojectdetails")){
			result = new ActionResult(ActionResult.Status.OK, "Aktivität geladen");	
			String projectid = context.getString("projectid");	
			if(projectid != null){
				project = (Project)dossier.getObjectByName("Project", projectid);
				if(project != null){
					StringBuilder html = new StringBuilder();		
					html.append(ProjectView.getProjectDetails(this, project));
					result.setData(html.toString());
					result.setParam("dataContainer", "project_" + project.getPath());
					activeObject = project;
					
				}
				
			}
				
		}
		else if(command.equals("showprojectemails")){
			result = new ActionResult(ActionResult.Status.OK, "Aktivität geladen");	
			String projectid = context.getString("projectid");	
			if(projectid != null){
				project = (Project)dossier.getObjectByName("Project", projectid);
				if(project != null){
					StringBuilder html = new StringBuilder();		
					html.append(ProjectView.getProjectEmail(ocs, this, project, activityfilter));
					result.setData(html.toString());
					result.setParam("dataContainer", "project_" + project.getPath());
					activeObject = project;
					
				}
				
			}
				
		}
		else if(command.equals("showprojectjournal")){
			result = new ActionResult(ActionResult.Status.OK, "Aktivität geladen");	
			String projectid = context.getString("projectid");	
			if(projectid != null){
				project = (Project)dossier.getObjectByName("Project", projectid);
				if(project != null){
					StringBuilder html = new StringBuilder();		
					html.append(ProjectView.getProjectJournal(this, project, activityfilter));
					result.setData(html.toString());
					result.setParam("dataContainer", "project_" + project.getPath());
					activeObject = project;
					
				}
				
			}
				
		}
		else if(command.equals("activityfilter")){
			result = new ActionResult(ActionResult.Status.OK, "Aktivität geladen");	
			String projectid = context.getString("projectid");	
			String objecttemplate = context.getString("objecttemplate");	
			if(projectid != null && objecttemplate != null){
				project = (Project)dossier.getObjectByName("Project", projectid);
				if(objecttemplate.length() > 0){
					activityfilter = objecttemplate;
				}
				else{
					activityfilter = null;	
				}
				if(project != null){
					StringBuilder html = new StringBuilder();		
					html.append(ProjectView.getProjectJournal(this, project, activityfilter));
					result.setData(html.toString());
					result.setParam("dataContainer", "project_" + project.getPath());
					activeObject = project;
					
				}
				
			}
				
		}
		else if(command.equals("organisationmemberadd")){
			result = ocs.startProcess("ch.opencommunity.process.OrganisationMemberAdd", userSession, null, context, this);
		}
		else if(command.equals("projectadd")){
			result = ocs.startProcess("ch.opencommunity.process.ProjectAdd", userSession, null, context, this);
		}
		else if(command.equals("showobjectlist")){
			
			result = new ActionResult(ActionResult.Status.OK, "Liste geladen");	
			result.setData(DossierView.getProjectList(this, dossier, context));
			result.setParam("dataContainer", "objectList");
			result.setParam("setformtab", command);
			mode = 1;
			
		}
		else if(command.equals("showjournal")){
			
			result = new ActionResult(ActionResult.Status.OK, "Liste geladen");	
			result.setData(DossierView.getActivityList(this, dossier));
			result.setParam("dataContainer", "objectList");
			result.setParam("setformtab", command);
			mode = 2;
			
		}
		else if(command.equals("contactadd")){
			contactAdd(context);
			result = new ActionResult(ActionResult.Status.OK, "Liste geladen");
			result.setParam("opendossier", dossier.getName());
			result.setParam("dataContainer", "editArea");
			result.setData(DossierView.getDossierView(userSession, this, context));
		}
		else if(command.equals("parameteradd")){
			parameterAdd(context);
			result = new ActionResult(ActionResult.Status.OK, "Parameter hinzugefügt");
			result.setParam("opendossier", dossier.getName());
			result.setParam("dataContainer", "editArea");
			result.setData(DossierView.getDossierView(userSession, this, context));
		}
		else if(command.equals("parameterdelete")){
			if(parameterDelete(context)){
				result = new ActionResult(ActionResult.Status.OK, "Parameter gelöscht");
				result.setParam("opendossier", dossier.getName());
				result.setParam("dataContainer", "editArea");
				result.setData(DossierView.getDossierView(userSession, this, context));
			}
			else{
				result = new ActionResult(ActionResult.Status.FAILED, "Problem beim Löschen");				
			}
		}
		else if(command.equals("notedelete")){
			if(noteDelete(context)){
				result = new ActionResult(ActionResult.Status.OK, "Parameter gelöscht");
				result.setParam("opendossier", dossier.getName());
				result.setParam("dataContainer", "editArea");
				result.setData(DossierView.getDossierView(userSession, this, context));
			}
			else{
				result = new ActionResult(ActionResult.Status.FAILED, "Problem beim Löschen");				
			}
		}
		else if(command.equals("close")){
			
			ocs.logAccess("closing profile " + this);
			int index = userSession.getObjects("DossierController").indexOf(this);
			int newindex = -1;
			
			if(index==0 && userSession.getObjects("DossierController").size() > 1){
				newindex = 0;
			}
			else if(index > 0){
				newindex = index-1;
			}
			
			userSession.deleteElement("DossierController", this);
			result = new ActionResult(ActionResult.Status.OK, "Dossier geschlosssen");
			result.setParam("refresh", "usertabs");
			if(newindex > -1){
				DossierController dc = (DossierController)userSession.getObjectByIndex("DossierController", newindex);
				userSession.setActiveObject(dc);
				result.setParam("opendossier", dc.getName());
				result.setData(DossierView.getDossierView(userSession, dc, context));
				
			}
			else{
				result.setParam("exec", "loadSection('home')");
			}  
			
			
		}
		else{
			result = new ActionResult(ActionResult.Status.FAILED, "Befehl nicht gefunden");	
		}
		
		return result;
	}
	public Activity createActivity(BasicClass parent, ApplicationContext context){
		OpenCommunityServer ocs = (OpenCommunityServer)dossier.getRoot();
		
		

		String objecttemplate= context.getString("objecttemplate");
		ObjectTemplate ot = ocs.getObjectTemplate(objecttemplate);
		ocs.logAccess("objecttemplate : " + ot);
		if(ot != null){
			Activity activity = (Activity)ot.createObject("ch.opencommunity.base.Activity", null, context);
			activity.setParent(this);
			activity.setName("-1");
			addSubobject("temp", activity);
			activity.addProperty(parent.getClass().getSimpleName() + "ID", "String", parent.getName());
			activity.setProperty("Status", 1);
			//activity.applyTemplate(ot);
			
			activity.setProperty("Template", ot);
			
			//activity.initObjectLocal();
			activity.initObject();
						
			//addProperty("activity_title", "String", "");
			//addProperty("Attachments", "String", "");
			//int i = 1;

			return activity;
		}	
		return null;
		
	}
	public void contactAdd(ApplicationContext context){
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		try{
			int contactid = context.getID("contactid");
			
			ocs.logAccess("new contact : " + contactid);
			
			if(contactid > -1){
			
				ocs.logAccess("Neue Rolle " + contactid);
				OrganisationalUnit ou = (OrganisationalUnit)dossier.getObject("OrganisationalUnit");
				if(ou != null){
					boolean hasContact = false;
					for(BasicClass bc : ou.getObjects("Contact")){
						if(bc.getID("Type")==contactid){
							hasContact = true;	
						}
					}
	
					if(!hasContact){
						Contact contact = new Contact();
	
						contact.addProperty("OrganisationalUnitID", "String", ou.getName());
						contact.setProperty("Type", contactid);
						String id = ocs.insertObject(contact);
						ocs.getObject(ou, "Contact", "ID", id);
					}
				}
			}
		}
		catch(java.lang.Exception e){
			ocs.logException(e);
		}
	}
	public boolean parameterDelete(ApplicationContext context){
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		String parameterid = context.getString("parameterid");
		boolean success = false;
		if(parameterid != null && parameterid.length() > 0){
			
			ocs.logAccess("deleting parameter " + parameterid);
			
			ObjectDetail objectDetail = (ObjectDetail)dossier.getObjectByIndex("ObjectDetail", 0);
			if(objectDetail != null){
				
				Parameter parameter = (Parameter)objectDetail.getObjectByName("Parameter", parameterid);
				if(parameter != null){
					success = ocs.deleteObject(parameter);	
					if(success){
						objectDetail.deleteElement("Parameter", parameter);
						objectDetail.rebuildList();
					}
				}
				
			}
			
		}
		return success;
		
	}
	public boolean noteDelete(ApplicationContext context){
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		String noteid = context.getString("noteid");
		ocs.logAccess("deleting note " + noteid);
		boolean success = false;
		if(noteid != null && noteid.length() > 0){
			
			ObjectDetail objectDetail = (ObjectDetail)dossier.getObjectByIndex("ObjectDetail", 0);
			if(objectDetail != null){
				
				ocs.logAccess("deleting note " + noteid);
				
				Note note = (Note)objectDetail.getObjectByName("Note", noteid);
				if(note != null){
					success = ocs.deleteObject(note);	
					if(success){
						objectDetail.deleteElement("Note", note);
						objectDetail.rebuildList();
					}
				}
				
			}
			
		}
		return success;
		
	}
	public void parameterAdd(ApplicationContext context){
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		try{
			int fielddefinitionid = context.getID("fielddefinitionid");
;
			
			if(fielddefinitionid > -1){
				ObjectDetail objectDetail = (ObjectDetail)dossier.getObjectByIndex("ObjectDetail", 0);
				ObjectTemplate ot = (ObjectTemplate)objectDetail.getObject("Template");
				FieldDefinition fd = (FieldDefinition)ot.getObjectByName("FieldDefinition", "" + fielddefinitionid);
				if(objectDetail != null && fd != null){
					if(fd.getType()==4){
						
						Note note = (Note)ocs.createObject("ch.opencommunity.base.Note", null, context);
						note.addProperty("ObjectDetailID", "String", objectDetail.getName());
						note.setProperty("Template", fielddefinitionid);
						
						String id = ocs.insertSimpleObject(note);
						

						
						note = (Note)ocs.getObject(objectDetail, "Note", "ID", id);
						note.setProperty("Template", fd);
						

						
						//objectDetail.getObjectCollection("Note").sort("Template");
						
					}
					else{
						Parameter parameter = (Parameter)ocs.createObject("ch.opencommunity.base.Parameter", null, context);
						parameter.addProperty("ObjectDetailID", "String", objectDetail.getName());
						parameter.setProperty("Template", fielddefinitionid);
						
						String id = ocs.insertSimpleObject(parameter);
						
						ocs.logAccess("new parameter " + objectDetail.getObjects("Parameter").size());
						
						parameter = (Parameter)ocs.getObject(objectDetail, "Parameter", "ID", id);
						parameter.setProperty("Template", fd);
						
						ocs.logAccess("new parameter " + objectDetail.getObjects("Parameter").size());
						
						//objectDetail.getObjectCollection("Parameter").sort("Template");
					}
					objectDetail.rebuildList();
					
				}
				
				
			}
			
		}
		catch(java.lang.Exception e){
			ocs.logException(e);
		}
			
		
	}
	public void loadObject(BasicClass object, String id){
		String type = object.getClass().getSimpleName();
		OpenCommunityServer ocs = (OpenCommunityServer)dossier.getRoot();
		if(type.equals("Activity")){
			if(object.getID("ProjectID") > 0){
				
				Project project = (Project)dossier.getObjectByName("Project", "" + object.getID("ProjectID"));
				if(project != null){
					ocs.getObject(project, "Activity", "ID", id);	
				}
				
			}
			else{
				CaseRecord caseRecord = (CaseRecord)dossier.getObjectByIndex("CaseRecord", 0);
				
				if(caseRecord != null){
					ocs.getObject(caseRecord, "Activity", "ID", id);	
				}
			}
			
		}
		else if(type.equals("Project")){
			ocs.getObject(dossier, "Project", "ID", id);	
		}
	}

	
}