package ch.opencommunity.base;

import ch.opencommunity.advertising.MemberAd;
import ch.opencommunity.advertising.MemberAdController;
import ch.opencommunity.advertising.MemberAdAdministration;
import ch.opencommunity.advertising.MemberAdRequestGroup;
import ch.opencommunity.server.OpenCommunityServer;
import ch.opencommunity.common.OpenCommunityUserSession;
import ch.opencommunity.common.OpenCommunityUserProfile;
import ch.opencommunity.common.AddressList;
import ch.opencommunity.view.ActivityView;
import ch.opencommunity.view.MemberAdView;
import ch.opencommunity.view.UserProfileView;
import ch.opencommunity.process.MemberAdDetail;

import ch.opencommunity.pdf.PDFCreator;

import org.kubiki.base.Property;
import org.kubiki.base.BasicClass;
import org.kubiki.base.ConfigValue;
import org.kubiki.base.ActionResult;
import org.kubiki.base.ActionHandler;
import org.kubiki.base.ObjectCollection;
import org.kubiki.database.Record;
import org.kubiki.application.ApplicationContext;
import org.kubiki.application.server.WebApplicationContext;
import org.kubiki.mail.MessageWrapper;
import org.kubiki.util.DateConverter;
import org.kubiki.pdf.*;

import java.util.Vector;
import java.util.List;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.HashMap;

public class OrganisationMemberController extends BaseController{
	
	OrganisationMember om;
	
	Person person = null;
	Address address = null;
	Identity identity = null;
	Login login = null;
	
	Activity activity;
	
	MessageWrapper messageWrapper = null;
	
	MemberAd ma;
	
	String activetab = "memberad";
	
	boolean createrequest = false;
	boolean fileupload = false;

	public int mode = 1;
	
	static String[] contacts = {"Tel. privat","Tel. gesch.","Tel. mobil","Email"};
	
	public Hashtable yes_no, quality, roles = null;
	
	public OrganisationMemberController(OrganisationMember om){
		
		this.om = om;
		
		OpenCommunityServer ocs = (OpenCommunityServer)om.getRoot();
		
		addProperty("activityfilter", "Integer", "");
		
		person = om.getPerson();
		
		addProperty(om.getProperty("Status"));
		addProperty(om.getProperty("NotificationMode"));
		addProperty(om.getProperty("InheritsAddress"));
		addProperty(om.getProperty("Title"));
		addProperty(om.getProperty("Type"));
		
		Property p = addProperty(om.getProperty("Function"));
		p.setSelection(ocs.getFunctions().getCodeList());

		
		if(person != null){
				
			addProperty("Languages", "ListFillIn", person.getString("Languages"));
			getProperty("Languages").setSelection(ocs.getSupportedLanguages());
			person.getProperty("Languages").setSelection(ocs.getSupportedLanguages());
				 
				 addProperty(om.getProperty("Comment"));
				
				identity = person.getIdentity();
				if(identity != null){
					addProperty(identity.getProperty("FamilyName"));
					addProperty(identity.getProperty("FirstName"));
					addProperty(identity.getProperty("Sex"));
					addProperty(identity.getProperty("DateOfBirth"));
					addProperty(identity.getProperty("FirstLanguageS"));
				}
				
				//getProperty("FirstLanguage").setSelection(ocs.getSupportedLanguages2());
				
				address = person.getAddress();
				if(address != null){
					
					addProperty(address.getProperty("AdditionalLine"));	
					addProperty(address.getProperty("POBox"));	
					addProperty(address.getProperty("Street"));	
					addProperty(address.getProperty("Number"));
					addProperty(address.getProperty("Zipcode"));
					addProperty(address.getProperty("City"));
					addProperty(address.getProperty("Country"));
				}
				
			for(BasicClass bc : person.getObjects("Contact")){
				addProperty(contacts[bc.getInt("Type")], "String", bc.getString("Value"));
			}
				
			Vector sex = new Vector();
			sex.add(new ConfigValue("0", "0", "Nicht gesetzt"));
			sex.add(new ConfigValue("1", "1", "Herr"));
			sex.add(new ConfigValue("2", "2", "Frau"));
			getProperty("Sex").setSelection(sex);

			
		}
		login = om.getLogin();
		if(login != null){
			login.getProperty("Username").setEditable(false);
			addProperty(login.getProperty("Username"));
			addProperty(login.getProperty("Password"));
		}
		
		addProperty("objecttemplate", "String", "");
		getProperty("objecttemplate").setSelection(ocs.getObjectTemplates());
		
		addProperty("documenttemplate", "String", "");
		getProperty("documenttemplate").setSelection(ocs.getTemplib(1).getObjects("DocumentTemplate"));
		
		getProperty("activityfilter").setSelection(ocs.getObjectTemplates());
		
		addObjectCollection("temp", "*");
		
		roles = new Hashtable();
		for(BasicClass bc : ocs.getObjects("Role")){
			roles.put(bc.getName(), bc.getString("Title"));
		}
		
	}
	public String toString(){
		return om.toString();	
	}
	public OrganisationMember getOrganisationMember(){
		
		return om;	
		
	}
	
	public  Activity getCurrentActivity(){
		
		return activity;	
		
	}
	public void setCurrentActivityID(String activityid){
		Activity activity = (Activity)om.getObjectByName("Activity", activityid);
		setCurrentActivity(activity);
	}
	public void setCurrentActivity(Activity activity){
	
		this.activity = activity;
		
		if(activity != null){
		
			addProperty("activity_title", "String", activity.getString("Title"));
			addProperty("activity_context", "String", activity.getString("Context"));
			addProperty("Attachments", "String", "");
			int i = 1;
			for(BasicClass parameter : activity.getObjects("Parameter")){
				if(parameter.getString("Title").equals("Dokument")){
					addProperty("activity_parameter_" + i, "String", parameter.getString("Document"));
				}
				else{
					addProperty("activity_parameter_" + i, "String", parameter.getString("Value"));
				}
			}
			i = 1;
			for(BasicClass note : activity.getObjects("Note")){
				addProperty("activity_note_" + i, "String", note.getString("Content"));
				i++;
			}
		}
		
	}
	public MessageWrapper getMessageWrapper(){
		return messageWrapper;
	}
	public void setMessageWrapper(MessageWrapper messageWrapper){
		this.messageWrapper = messageWrapper;
	}
	
	public void setMemberAd(MemberAd ma){
		this.ma = ma;	
	}
	public MemberAd getMemberAd(){
		return ma;
	}
	
	public void setActiveTab(String activetab){
		this.activetab = activetab;	
	}
	public String getActiveTab(){
		return activetab;
	}
	
	public void setCreateRequest(boolean createrequest){
		this.createrequest = createrequest;	
	}
	public boolean getCreateRequest(){
		return createrequest;	
	}
	public void setFileUpload(boolean fileupload){
		this.fileupload = fileupload;	
	}
	public boolean getFileUpload(){
		return fileupload;	
	}
	public void saveActivity(ApplicationContext context){
		OpenCommunityServer ocs = (OpenCommunityServer)om.getRoot();
					
		activity.setProperty("Title", getString("activity_title"));
		activity.setProperty("Context", getString("activity_context"));
		activity.setProperty("Attachments", getString("Attachments"));
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
			note.setProperty("Content", getString("activity_note_" + i));
			if(note.getID() > 0){
				ocs.updateObject(note);
			}
			else{
				ocs.insertObject(note);
			}
			i++;
		}
					
					
		if(activity.getID() > 0){
			ocs.updateObject(activity);
		}
		else{
			String id = ocs.insertObject(activity, true);
			activity = (Activity)ocs.getObject(om, "Activity", "ID", id);
			
			ocs.logAccess("Context: " + activity.getID("Context"));
			
			if(activity.getID("Context")==10){ //konfigurierbare Lösung finden
				
				Note note = (Note)activity.getObjectByIndex("Note", 0);
				
				if(note != null){
				
					String code = ocs.createLoginCode(id, context);
									
					String link = "\n\n" + ocs.getString("hostname") + "/servlet.srv?action=onetimelogin&redirect=profile&sectionid=inserate&code=" + code;
					
					String content = note.getString("Content");
					
					content = content.replace("<@link>", link);
					
					note.setProperty("Content", content);
					
					ocs.updateObject(note);
				
				}
					
					
			}
		}
		if(context.hasProperty("send") && context.getString("saveactivity").equals("true")){
			

			
			activity.setProperty("Status", 0);
			ocs.updateObject(activity);
			ocs.sendAllPendingMails();
		}
		if(context.hasProperty("createpdf")){
			if(activity != null){
							
				String filename = ocs.createPDF(activity, null);
				getParent().setProperty("filename", filename);
			}
		}
		if(context.hasProperty("sendpdf")){
			if(activity != null){
							
				String filename = ocs.createPDF(activity, null);
				activity.setProperty("Attachments", filename);
				activity.setProperty("Status", 0);
				ocs.updateObject(activity);	
				ocs.sendAllPendingMails();
				this.activity = null;
			}
		}
		if(activity != null){
			if(activity.getID("Template") != 7 && activity.getID("Template") != 8){
				this.activity = null;
			}
		}
	}
	public void createActivity(ApplicationContext context){
		OpenCommunityServer ocs = (OpenCommunityServer)om.getRoot();
		String objecttemplate= context.getString("objecttemplate");
		ObjectTemplate ot = ocs.getObjectTemplate(objecttemplate);
		if(ot != null){
			activity = (Activity)ot.createObject("ch.opencommunity.base.Activity", null, context);
			activity.setParent(this);
			activity.addProperty("OrganisationMemberID", "String", om.getName());
			activity.setProperty("Status", 1);
			activity.applyTemplate(ot);
						
			addProperty("activity_title", "String", "");
			addProperty("Attachments", "String", "");
			int i = 1;
			for(BasicClass parameter : activity.getObjects("Parameter")){
				Property p = addProperty("activity_parameter_" + i, "String", "");
							//if(parameter.getString("Title").equals("Dokument")){
								p.setSelection(ocs.getTemplib(1).getObjects("DocumentTemplate"));
							//}
			}
			i = 1;
			for(BasicClass note : activity.getObjects("Note")){
				addProperty("activity_note_" + i, "String", "");
			}
		}		
		
	}
	public Activity createActivity2(ApplicationContext context){
		
		return createActivity2(context, null, 0);
		
	}
	public Activity createActivity2(ApplicationContext context, String textblockid, int activitycontext){
		OpenCommunityServer ocs = (OpenCommunityServer)om.getRoot();

		String objecttemplate= context.getString("objecttemplate");
		ObjectTemplate ot = ocs.getObjectTemplate(objecttemplate);
		ocs.logAccess("objecttemplate : " + ot);
		if(ot != null){
			Activity activity = (Activity)ot.createObject("ch.opencommunity.base.Activity", null, context);
			activity.setParent(this);
			activity.setName("-1");
			activity.setProperty("Context", activitycontext);
			addSubobject("temp", activity);
			activity.addProperty("OrganisationMemberID", "String", om.getName());
			activity.setProperty("Status", 1);
			activity.applyTemplate(ot);
						
			addProperty("activity_title", "String", "");
			addProperty("Attachments", "String", "");
			
			if(textblockid != null){
				
				String comment = ocs.getTextblockContent(textblockid);
		
				comment = comment.replace("<p>", "\n");
				comment = comment.replace("</p>", "");
				comment = comment.replace("<br />", "");
				comment = comment.trim();
				String comment2 = "";
				String[] lines = comment.split("\r\n|\r|\n");
				for(String line : lines){
					comment2 += "\n" + line.trim();
				}
				Note note = (Note)activity.getObjectByIndex("Note", 0);
				if(note != null){
					note.setProperty("Content", comment2);
				}
				
				
			}
			
			int i = 1;
			/*
			for(BasicClass parameter : activity.getObjects("Parameter")){
				Property p = addProperty("activity_parameter_" + i, "String", "");
							//if(parameter.getString("Title").equals("Dokument")){
								p.setSelection(ocs.getTemplib(1).getObjects("DocumentTemplate"));
							//}
			}
			*/
			return activity;
		}	
		return null;
		
	}
	public Activity createDocumentActivity(ApplicationContext context){
		OpenCommunityServer ocs = (OpenCommunityServer)om.getRoot();

		String objecttemplate= "2";
		ObjectTemplate ot = ocs.getObjectTemplate(objecttemplate);
		String sDocumenttemplate= context.getString("documenttemplate");
		int documenttemplate = Integer.parseInt(sDocumenttemplate);
		ocs.logAccess("objecttemplate : " + ot);
		if(ot != null){
			
			DocumentTemplate dt = ocs.getDocumentTemplate(documenttemplate);
			if(dt != null){
									
				Document document = (Document)dt.createObject("ch.opencommunity.base.Document", null, context);
				document.addProperty("OrganisationMemberID", "String", om.getName());
				document.setProperty("Recipient", om.getName());
				document.setProperty("Template", dt.getName());
				DocumentTemplateModule dtm = (DocumentTemplateModule)dt.getObjectByIndex("DocumentTemplateModule", 0);
				document.setProperty("WordModules", "/WebApplication/DocumentTemplateLibrary:1/DocumentTemplate:" + dt.getName() + "/DocumentTemplateModule:" + dtm.getName());
				String docid = ocs.insertObject(document);
				
				ocs.getObject(om, "Document", "ID", docid);
									
			
				Activity activity = (Activity)ot.createObject("ch.opencommunity.base.Activity", null, context);
				activity.setParent(this);
	
				activity.addProperty("OrganisationMemberID", "String", om.getName());
				activity.setProperty("Status", 1);
				activity.applyTemplate(ot);
				
				Parameter parameter = (Parameter)activity.getFieldByTemplate("19");
				parameter.setProperty("Document", docid);
				
				String id = ocs.insertObject(activity, true);
				activity = (Activity)ocs.getObject(om, "Document", "ID", id);
							
				addProperty("activity_title", "String", "");
				addProperty("Attachments", "String", "");
				int i = 1;
	
				return activity;
			}
		}	
		return null;
		
	}
	public void importEmail(ApplicationContext context){

		if(messageWrapper != null){
			OpenCommunityServer ocs = (OpenCommunityServer)om.getRoot();
			String objecttemplate= "3";
			ObjectTemplate ot = ocs.getObjectTemplate(objecttemplate);
			if(ot != null){
				activity = (Activity)ot.createObject("ch.opencommunity.base.Activity", null, context);
				activity.setParent(this);
				activity.addProperty("OrganisationMemberID", "String", om.getName());
				activity.applyTemplate(ot);
							
				BasicClass note = activity.getFieldByTemplate("20");
							
				activity.setProperty("Title",  messageWrapper.getSubject());
							
				String content = messageWrapper.getSender();
				content += "\n\n" + messageWrapper.getDateString();
				content += "\n\n" + messageWrapper.getSubject();
				content += "\n\n" + messageWrapper.getMessageBody();
							
				note.setProperty("Content", content);

				String activityid = ocs.insertObject(activity, true);
				ocs.getObject(om, "Activity", "ID", activityid);
							
			}
						
						
		}
		activity = null;
		activetab = "emailimport";
		messageWrapper = null;
		
		
		
	}
	public void saveOrganisationMember(){
		
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
			if(contact.getID("Status")==0){
				contact.setProperty("Value", getString(contacts[contact.getID("Type")]));
				ocs.updateObject(contact);		
			}
		}	
		
	}
	public void saveOrganisationMember2(){
		
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
		ocs.updateObject(person);	
						
		for(BasicClass contact : person.getObjects("Contact")){

			ocs.updateObject(contact);		
			
		}	
		
	}
	public boolean saveObject(ApplicationContext context, String prefix){
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		
		Enumeration<String> parameterNames = ((WebApplicationContext)context).getParameterNames();
		while (parameterNames.hasMoreElements()) {
			String parameterName = parameterNames.nextElement();
			if (parameterName.startsWith("subobject_")) {
				String[] nameParts = parameterName.split("_");
				if (nameParts.length >= 4) {
					String collectionName = nameParts[1];
					String subobjectID = nameParts[2];
					String propertyName = nameParts[3];
					BasicClass subObj = getSubobject(subobjectID, collectionName);
					if (subObj instanceof BasicOCObject) {
						Property p = subObj.getProperty(propertyName);
						if (p != null) {
							p.setValue(context.getString(parameterName));
							((BasicOCObject)subObj).saveObject((OpenCommunityUserSession)context.getObject("usersession"));
						}
					}
				}
			}
		}		
		List<String> fieldNames = om.getPropertySheet().getNames();
		
		prefix = "om_"; 
		
		for(String fieldName : fieldNames){
			String value = context.getString(prefix + fieldName);
			if(value != null){
				om.setProperty(fieldName, value);
			}
		}
		
		Person person = om.getPerson();	
		fieldNames = person.getPropertySheet().getNames();
		prefix = "person_"; 
				
		for(String fieldName : fieldNames){
			String value = context.getString(prefix + fieldName);
			if(value != null){
				person.setProperty(fieldName, value);
			}
		}
		
		Identity identity = person.getIdentity();	
		fieldNames = identity.getPropertySheet().getNames();
		prefix = "identity_"; 
				
		for(String fieldName : fieldNames){
			String value = context.getString(prefix + fieldName);
			if(value != null){
				identity.setProperty(fieldName, value);
			}
		}
		
		Address address = person.getAddress();	
		fieldNames = address.getPropertySheet().getNames();
		prefix = "address_"; 
				
		for(String fieldName : fieldNames){
			String value = context.getString(prefix + fieldName);
			if(value != null){
				address.setProperty(fieldName, value);
			}
		}
		
		Login login = om.getLogin();	
		if(login != null){
			fieldNames = login.getPropertySheet().getNames();
			prefix = "login_"; 
					
			for(String fieldName : fieldNames){
				String value = context.getString(prefix + fieldName);
				if(value != null){
					login.setProperty(fieldName, value);
				}
			}
		}
		
		for(BasicClass contact : person.getObjects("Contact")){
			String value = context.getString("contact_" + contact.getName() + "_Value");	
			
			ocs.logAccess("contact_" + contact.getName() + "_Value : " + value);
			
			if(value != null){
				contact.setProperty("Value", value);
			}			
		}
		
		saveOrganisationMember2();
		return true;
	}
	public ActionResult onAction(BasicClass source, String command, ApplicationContext context) {
		
		ActionResult result = null;
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
		
		if(command.equals("saveobject")){
			ocs.logAccess("saving object");
			
			if(saveObject(context, "")){
				result = new ActionResult(ActionResult.Status.OK, "Datensatz gespeichert");
				if(!context.hasProperty("noreload")){
					result.setParam("exec", "editOrganisationMember('" + om.getName() + "')");
				}
			}
			else{
				result = new ActionResult(ActionResult.Status.FAILED, "Fehler beim Speichern");
			}
			
		}
		else if(command.equals("editactivity")){
			result = new ActionResult(ActionResult.Status.OK, "Aktivität geladen");	
			String activityid = context.getString("activityid");
			if(activityid != null){
				Activity activity = (Activity)om.getObjectByName("Activity", activityid);
				if(activity != null){
					StringBuilder html = new StringBuilder();
					ActivityView.toHTML2(html, this, activity, context);
					result.setData(html.toString());
					result.setParam("dataContainer", "objectEditArea");
					activeObject = activity;
				}
			}
		}
		else if(command.equals("deleteactivity")){
			result = new ActionResult(ActionResult.Status.OK, "Aktivität geladen");	
			String activityid = context.getString("activityid");
			if(activityid != null){
				Activity activity = (Activity)om.getObjectByName("Activity", activityid);
				if(activity != null){
					ocs.deleteObject(activity);
					om.deleteElement("Activity", activity);
					
					StringBuilder html = new StringBuilder();

					result.setData(UserProfileView.getActivityListContent(this, getOrganisationMember(), null));
					
					result.setParam("dataContainer", "activityList");
					activeObject = null;
				}
			}
		}
		else if(command.equals("editmemberad")){
			result = new ActionResult(ActionResult.Status.OK, "Aktivität geladen");	
			String memberadid = context.getString("memberadid");
			if(memberadid != null){
				MemberAd memberAd = (MemberAd)om.getObjectByName("MemberAd", memberadid);
				if(memberAd != null){
					String template = context.getString("template");
					MemberAdController memberAdController = new MemberAdController(memberAd, template);
					StringBuilder html = new StringBuilder();
					html.append(MemberAdView.getMemberAdEditForm2(this, memberAd, memberAdController));
					result.setData(html.toString());
					result.setParam("dataContainer", "objectEditArea");
					activeObject = memberAdController;
				}
			}
		}
		else if(command.equals("addmemberadinfo")){
			result = new ActionResult(ActionResult.Status.OK, "Aktivität geladen");	
			String memberadid = context.getString("memberadid");
			if(memberadid != null){
				MemberAd memberAd = (MemberAd)om.getObjectByName("MemberAd", memberadid);
				if(memberAd != null){
					
					String template = "" + memberAd.getID("Template");
					
					Note note = new Note();
					note.addProperty("MemberAdID", "String", memberAd.getName());
					String id = ocs.insertObject(note);
					note = (Note)ocs.getObject(memberAd, "Note", "ID", id);
					
					memberAd.addProperty("AdditionalInfo", "Text", note.getString("Content"), false, "Zusatzinfo");	
					
					MemberAdController memberAdController = new MemberAdController(memberAd, template);
					StringBuilder html = new StringBuilder();
					html.append(MemberAdView.getMemberAdEditForm2(this, memberAd, memberAdController));
					result.setData(html.toString());
					result.setParam("dataContainer", "objectEditArea");
					activeObject = memberAdController;
				}
			}
		}
		else if(command.equals("showobjectlist")){
			result = new ActionResult(ActionResult.Status.OK, "Liste geladen");	
			result.setData(UserProfileView.getObjectList(this, getOrganisationMember()));
			result.setParam("dataContainer", "objectList");
			result.setParam("setformtab", command);
			mode = 1;
		}
		else if(command.equals("showjournal")){
			result = new ActionResult(ActionResult.Status.OK, "Liste geladen");	
			result.setData(UserProfileView.getActivityList(this, getOrganisationMember()));
			
			//result.setData("fifhwe fwehfuwe");
			result.setParam("dataContainer", "objectList");
			result.setParam("setformtab", command);
			mode = 2;
		}
		else if(command.equals("activityfilter")){
			String objecttemplate = context.getString("objecttemplate");
			setProperty("activityfilter", objecttemplate);
			
			result = new ActionResult(ActionResult.Status.OK, "Liste geladen");	
			result.setData(UserProfileView.getActivityListContent(this, getOrganisationMember(), objecttemplate));
			result.setParam("dataContainer", "activityList");

			mode = 2;
		}
		else if(command.equals("showcheques")){
			result = new ActionResult(ActionResult.Status.OK, "Liste geladen");	
			
			
			String sql = "SELECT t1.ID, t1.DateIssued, t1.DateValuta, t1.DateCashed, (t4.Familyname  || ' ' || t4.FirstName) AS OM1, (t7.Familyname  || ' ' || t7.FirstName) AS OM2";
			sql += " FROM Cheque AS t1";
			sql += " LEFT JOIN OrganisationMember AS t2 ON t1.OrganisationMemberIssued=t2.ID";
			sql += " LEFT JOIN Person AS t3 ON t2.Person=t3.ID";
			sql += " LEFT JOIN Identity AS t4 ON t4.PersonID=t3.ID";
			sql += " LEFT JOIN OrganisationMember AS t5 ON t1.OrganisationMemberCashed=t5.ID";
			sql += " LEFT JOIN Person AS t6 ON t5.Person=t6.ID";
			sql += " LEFT JOIN Identity AS t7 ON t7.PersonID=t6.ID";
			sql += " WHERE OrganisationMemberIssued=" + om.getName();
			sql += " ORDER BY ID DESC";
			
			ObjectCollection results = new ObjectCollection("Results", "*");
			ocs.queryData(sql, results);
			
			StringBuilder html = new StringBuilder();
			
			html.append("<b>Erhaltene Cheques</b>");
			
			html.append("<table>");
			
			html.append("<tr><th>Nummer</th><th>Datum ausgestellt</th><th>Valuta</th><th>Datum eingelöst</th><th>durch</th></tr>");
			
			for(BasicClass record : results.getObjects()){
				
				html.append("<tr>");
				html.append("<td class=\"datacell\">" + record.getString("ID") + "</td>");
				html.append("<td class=\"datacell\">" + DateConverter.sqlToShortDisplay(record.getString("DATEISSUED")) + "</td>");
				html.append("<td class=\"datacell\">" + DateConverter.sqlToShortDisplay(record.getString("DATEVALUTA")) + "</td>");
				html.append("<td class=\"datacell\">" + DateConverter.sqlToShortDisplay(record.getString("DATECASHED")) + "</td>");
				html.append("<td class=\"datacell\">" + record.getString("OM2") + "</td>");
				
				html.append("</tr>");				
			}
			
			html.append("</table>");
			

			
			sql = "SELECT t1.ID, t1.DateIssued, t1.DateValuta, t1.DateCashed, (t4.Familyname  || ' ' || t4.FirstName) AS OM1, (t7.Familyname  || ' ' || t7.FirstName) AS OM2";
			sql += " FROM Cheque AS t1";
			sql += " LEFT JOIN OrganisationMember AS t2 ON t1.OrganisationMemberIssued=t2.ID";
			sql += " LEFT JOIN Person AS t3 ON t2.Person=t3.ID";
			sql += " LEFT JOIN Identity AS t4 ON t4.PersonID=t3.ID";
			sql += " LEFT JOIN OrganisationMember AS t5 ON t1.OrganisationMemberCashed=t5.ID";
			sql += " LEFT JOIN Person AS t6 ON t5.Person=t6.ID";
			sql += " LEFT JOIN Identity AS t7 ON t7.PersonID=t6.ID";
			sql += " WHERE OrganisationMemberCashed=" + om.getName();
			sql += " ORDER BY ID DESC";
			
			results = new ObjectCollection("Results", "*");
			ocs.queryData(sql, results);
			

			
			html.append("<b>Eingelöste Cheques</b>");
			
			html.append("<table>");
			
			html.append("<tr><th>Nummer</th><th>Ausgestellt an</th><th>Datum ausgestellt</th><th>Valuta</th><th>Datum eingelöst</th></tr>");
			
			for(BasicClass record : results.getObjects()){
				
				html.append("<tr>");
				html.append("<td class=\"datacell\">" + record.getString("ID") + "</td>");
				html.append("<td class=\"datacell\">" + record.getString("OM1") + "</td>");
				html.append("<td class=\"datacell\">" + DateConverter.sqlToShortDisplay(record.getString("DATEISSUED")) + "</td>");
				html.append("<td class=\"datacell\">" + DateConverter.sqlToShortDisplay(record.getString("DATEVALUTA")) + "</td>");
				html.append("<td class=\"datacell\">" + DateConverter.sqlToShortDisplay(record.getString("DATECASHED")) + "</td>");
				
				
				html.append("</tr>");				
			}
			
			html.append("</table>");
			
			
			result.setData(html.toString());
			
			result.setParam("dataContainer", "objectList");
			result.setParam("setformtab", command);
			mode = 3;
		}
		else if(command.equals("showaccountmovements")){
			result = new ActionResult(ActionResult.Status.OK, "Liste geladen");				
			result.setData("pppppppp");
			result.setParam("dataContainer", "objectList");
			result.setParam("setformtab", command);
			mode = 4;
		}
		else if(command.equals("close")){
			ocs.logAccess("closing profile " + this);
			int index = userSession.getObjects("OrganisationMemberController").indexOf(this);
			int newindex = -1;
			
			if(index==0 && userSession.getObjects("OrganisationMemberController").size() > 1){
				newindex = 0;
			}
			else if(index > 0){
				newindex = index-1;
			}
			
			userSession.deleteElement("OrganisationMemberController", this);
			result = new ActionResult(ActionResult.Status.OK, "Profil geschlosssen");
			result.setParam("refresh", "usertabs");
			if(newindex > -1){
				OrganisationMemberController omc = (OrganisationMemberController)userSession.getObjectByIndex("OrganisationMemberController", newindex);
				userSession.setActiveObject(omc);
				result.setParam("openprofile", omc.getName());
				result.setData(UserProfileView.getUserProfileView(userSession, omc, context));
				
			}
			else{
				result.setParam("exec", "loadSection('home')");
			}                                                                
		}
		else if(command.equals("memberadcreate")){
			//result = ocs.startProcess("ch.opencommunity.process.MemberAdCreate", userSession, null, context, this);
			
			Hashtable params = new Hashtable();
			params.put("initmode", "1");
			result = ocs.startProcess("ch.opencommunity.process.MemberAdRequestActivityAdd", userSession, params, context, this);
			
			//result = ocs.startProcess("ch.opencommunity.process.MemberAdCreate", userSession, null, context, ocs.getMemberAdAdministration());
		}
		else if(command.equals("commercialadcreate")){

			
			Hashtable params = new Hashtable();
			params.put("initmode", "1");
			result = ocs.startProcess("ch.opencommunity.process.CommercialAdCreate", userSession, params, context, this);
			

		}
		else if(command.equals("activitycreate2")){       
			
			Activity activity = createActivity2(context);
			if(activity != null){
				result = new ActionResult(ActionResult.Status.OK, "Aktivität erstellt");	
				StringBuilder html = new StringBuilder();
				ActivityView.toHTML2(html, this, activity, context);
				result.setData(html.toString());
				result.setParam("dataContainer", "objectEditArea");
				activeObject = activity;
			}
		}
		else if(command.equals("documentcreate")){       
			Activity activity = createDocumentActivity(context);
			if(activity != null){
				result = new ActionResult(ActionResult.Status.OK, "Aktivität erstellt");	
				StringBuilder html = new StringBuilder();
				ActivityView.toHTML2(html, this, activity, context);
				result.setData(html.toString());
				result.setParam("dataContainer", "objectEditArea");
				activeObject = activity;
			}
		}
		else if(command.equals("mergeprofile")){   
			
			Hashtable params = new Hashtable();
			result = ocs.startProcess("ch.opencommunity.process.MergeProfile", userSession, null, context, this);

		}
		else if(command.equals("linkprofile")){   
			
			Hashtable params = new Hashtable();
			result = ocs.startProcess("ch.opencommunity.process.LinkProfile", userSession, null, context, this);

		}
		else if(command.equals("saveactivity")){
			String activityid = context.getString("activityid");
			result = new ActionResult(ActionResult.Status.OK, "Aktivität gespeichert");	
			Activity activity = null;
			if(activityid != null){
				if(activityid.equals("-1")){
					activity = (Activity)getObjectByName("temp", activityid);	
					
					if(context.hasProperty("send") && context.getString("send").equals("true")){
						activity.setProperty("Status", "0");
					}
					
					
					activity.saveObject(context, "");
					activity = (Activity)ocs.getObject(om, "Activity", "ID", activity.getName());
						
					if(context.hasProperty("createpdf") && context.getString("createpdf").equals("true")){			
						String filename = ocs.createPDF(activity, null);
						result.setParam("download", filename);
					}
					
					if(activity.getID("Context")==10){ //konfigurierbare Lösung finden
						
						Note note = (Note)activity.getObjectByIndex("Note", 0);
						
						if(note != null){
						
							String code = ocs.createLoginCode(om.getName(), context);
											
							String link = "\n\n" + ocs.getString("hostname") + "/servlet.srv?action=onetimelogin&redirect=profile&sectionid=inserate&code=" + code;
												
							
							String content = note.getString("Content");						
							content = content.replace("<@link>", link);
							
							ocs.logAccess(content);
							
							note.setProperty("Content", content);
							
							ocs.updateObject(note);
						
						}
							
							
					}
					else if(activity.getID("Context")==11){ //konfigurierbare Lösung finden
						
						Note note = (Note)activity.getObjectByIndex("Note", 0);
						
						if(note != null){
						
							String code = ocs.createLoginCode(om.getName(), context);
											
							String link = "\n\n" + ocs.getString("hostname") + "/servlet.srv?action=onetimelogin&redirect=profile&sectionid=kontakte&code=" + code;
												
							
							String content = note.getString("Content");						
							content = content.replace("<@link>", link);
							
							ocs.logAccess(content);
							
							note.setProperty("Content", content);
							
							ocs.updateObject(note);
						
						}
							
							
					}
				}
				else{

					activity = (Activity)om.getObjectByName("Activity", activityid);
					if(activity != null){
						
						if(context.hasProperty("send") && context.getString("send").equals("true")){
							activity.setProperty("Status", "0");
						}
						activity.saveObject(context, "");
					}
					if(context.hasProperty("createpdf") && context.getString("createpdf").equals("true")){			
						String filename = ocs.createPDF(activity, null);
						result.setParam("download", filename);
					}
				}
			}
			if(context.hasProperty("send") && context.getString("send").equals("true")){
				ocs.sendAllPendingMails();
			}
			if(context.hasProperty("sendpdf")){
				if(activity != null){
								
					String filename = ocs.createPDF(activity, null);
					activity.setProperty("Attachments", filename);
					activity.setProperty("Status", 0);
					ocs.updateObject(activity);	
					ocs.sendAllPendingMails();
					this.activity = null;
				}
			}
			StringBuilder html = new StringBuilder();
			ActivityView.toHTML2(html, this, activity, context);
			result.setData(html.toString());
			result.setParam("dataContainer", "objectEditArea");
			result.setParam("journal", getPath());

		}
		else if(command.equals("cancelactivity")){
			deleteElement("temp", activeObject);
			result = new ActionResult(ActionResult.Status.OK, "Aktivität abgebrochen");	
			result.setParam("dataContainer", "objectEditArea");	
			result.setData("");
		}
		else if(command.equals("savememberad")){

			String memberadid = context.getString("memberadid");
			ocs.logAccess("memberadid: " + memberadid);
			if(memberadid != null){
				MemberAd memberAd = (MemberAd)om.getObjectByName("MemberAd", memberadid);
				ocs.logAccess("memebera: " + memberAd);
				if(memberAd != null){
					MemberAdController.saveMemberAd(memberAd, context);
					result = new ActionResult(ActionResult.Status.OK, "Inserat gespeichert");	
					result.setParam("objectlist", getPath());
					
				}
				
				ocs.getMemberAdAdministration().initCommercialAds(ocs.getDataStore());
				
			}
		}
		else if(command.equals("requestcreate")){
			
			Hashtable params = new Hashtable();
			params.put("initmode", "2");
			result = ocs.startProcess("ch.opencommunity.process.MemberAdRequestActivityAdd", userSession, params, context, this);
			
			/*
			
			MemberAdAdministration maa = ocs.getMemberAdAdministration();
			userSession.setOrganisationMember(om);
			userSession.put("mode", "searchresults");
			userSession.put("category", "1");
			setCreateRequest(true);

				
				StringBuilder html = new StringBuilder();
				
				html.append("<div style=\"position : relative; min-height : 4000px; width : 100%; background : #363D45;\">");
				//html.append("<div style=\"position : relative; height : 95%; width : 100%; background : #363D45;\">");
				
				html.append(maa.getMemberAdSearchForm3(context, this, om, true));
				
				html.append("<div id=\"userprofile\" style=\"position : absolute;\">");
				
				html.append(OpenCommunityUserProfile.getMemoryList2(ocs, this, userSession, true));
				
				html.append("</div>");
				
				html.append("</div>");
				
				
				result = new ActionResult(ActionResult.Status.OK, "Formular geladen");	
				
				result.setData(html.toString());
				result.setParam("dataContainer", "popuplarge");
				result.setParam("dataContainerVisibility", "visible");
				
			*/
				//result.setParam("popupclass", "popuplarge");			
		}
		else if(command.equals("cancelrequestcreate")){
				ocs.logAccess("cancelling request creation ...");
				setCreateRequest(false);	
				result = new ActionResult(ActionResult.Status.OK, "Formular geladen");	
				result.setParam("openprofile", getName());
				result.setData(UserProfileView.getUserProfileView(userSession, this, context));
		}
		else if(command.equals("setcategory")){
				String category = context.getString("category");
				if(category != null){
					MemberAdAdministration maa = ocs.getMemberAdAdministration();
					userSession.setOrganisationMember(om);
					userSession.put("mode", "searchresults");
					userSession.put("category", category);
					setCreateRequest(true);
	
					
					StringBuilder html = new StringBuilder();
					
					html.append("<div style=\"position : relative; min-height : 4000px; width : 100%; background : #363D45;\">");
					//html.append("<div style=\"position : relative; height : 95%; width : 100%; background : #363D45;\">");
					
					html.append(maa.getMemberAdSearchForm3(context, this, om, true));
					
					html.append("<div id=\"userprofile\" style=\"position : absolute;\">");
					
					html.append(OpenCommunityUserProfile.getMemoryList2(ocs, this, userSession, true, 2));
					
					html.append("</div>");
					
					html.append("</div>");
					
					
					result = new ActionResult(ActionResult.Status.OK, "Formular geladen");	
					
					result.setData(html.toString());
					result.setParam("dataContainer", "searchform3");
					/*
					result.setParam("dataContainer", "popuplarge");
					result.setParam("dataContainerVisibility", "visible");
					*/
				}
		}
		else if(command.equals("creatememebradrequests")){
			Activity activity = requestsCreate(context, userSession);
			if(activity != null){
				result = new ActionResult(ActionResult.Status.OK, "Aktivität erstellt");	
				StringBuilder html = new StringBuilder();
				ActivityView.toHTML2(html, this, activity, context);
				result.setData(html.toString());
				result.setParam("dataContainer", "objectEditArea");
				activeObject = activity;
			}	
			else{
				
				result = new ActionResult(ActionResult.Status.OK, "Adressbestellung erstellt");	
				//result.setParam("dataContainer", "admindisplay");
				//result.setData(UserProfileView.getUserProfileView(userSession, this, context));
				result.setParam("exec", "editOrganisationMember('" + getName() + "')");
			}
			
		}
		else if(command.equals("createnotification")){
			Activity activity = createNotification(context, null);
			if(activity != null){
				result = new ActionResult(ActionResult.Status.OK, "Aktivität erstellt");	
				StringBuilder html = new StringBuilder();
				ActivityView.toHTML2(html, this, activity, context);
				result.setData(html.toString());
				result.setParam("dataContainer", "objectEditArea");
				activeObject = activity;
			}

		}
		else if(command.equals("activityobjectadd")){
			if(activeObject != null && activeObject instanceof Activity){
				activityObjectAdd(context, (Activity)activeObject);	
				result = new ActionResult(ActionResult.Status.OK, "Aktivität erstellt");	
				StringBuilder html = new StringBuilder();
				ActivityView.toHTML2(html, this, (Activity)activeObject, context);
				result.setData(html.toString());
				result.setParam("dataContainer", "objectEditArea");
				//activeObject = activity;
			}
		}
		else if(command.equals("createlogin")){
			createLogin();
			result = new ActionResult(ActionResult.Status.OK, "Liste geladen");
			result.setParam("openprofile", om.getName());
			result.setParam("dataContainer", "editArea");
			result.setData(UserProfileView.getUserProfileView(userSession, this, context));
		}
		else if(command.equals("activateprofile")){
			
			result = new ActionResult(ActionResult.Status.OK, "Liste geladen");
			String email = getEmailAddress();			
			if(email != null){
				activateProfile(context);
				result.setParam("openprofile", om.getName());
				result.setParam("dataContainer", "editArea");
				result.setData(UserProfileView.getUserProfileView(userSession, this, context));
			}
			else{
				om.setProperty("Status", "1");
				ocs.updateObject(om);
				result.setParam("openprofile", om.getName());
				result.setParam("dataContainer", "editArea");
				result.setData(UserProfileView.getUserProfileView(userSession, this, context));
				result.setParam("exec", "createNotification('" + getPath() + "')");
			}
			

		}
		else if(command.equals("roleadd")){
			roleAdd(context);
			result = new ActionResult(ActionResult.Status.OK, "Liste geladen");
			result.setParam("openprofile", om.getName());
			result.setParam("dataContainer", "editArea");
			result.setData(UserProfileView.getUserProfileView(userSession, this, context));
		}
		else if(command.equals("roledelete")){
			roleDelete(context);
			result = new ActionResult(ActionResult.Status.OK, "Liste geladen");
			result.setParam("openprofile", om.getName());
			result.setParam("dataContainer", "editArea");
			result.setData(UserProfileView.getUserProfileView(userSession, this, context));
		}
		else if(command.equals("contactadd")){
			contactAdd(context);
			result = new ActionResult(ActionResult.Status.OK, "Liste geladen");
			result.setParam("openprofile", om.getName());
			result.setParam("dataContainer", "editArea");
			result.setData(UserProfileView.getUserProfileView(userSession, this, context));
		}
		else if(command.equals("contactdelete")){
			contactDelete(context);
			result = new ActionResult(ActionResult.Status.OK, "Liste geladen");
			result.setParam("openprofile", om.getName());
			result.setParam("dataContainer", "editArea");
			result.setData(UserProfileView.getUserProfileView(userSession, this, context));
		}
		else if(command.equals("sendcredentials")){
					
			if(om.getID("Status")==1){
						

				Login login = om.getLogin();
							
				if(login != null){

								
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
					activity.setName("-1");
					activity.setParent(this);
					addSubobject("temp", activity);
					activity.applyTemplate(ot);
					activity.setProperty("Status", "1");
					activity.setProperty("Title", "Ihre Zugangsdaten für Nachbarnet");
					activity.addProperty("OrganisationMemberID", "String", om.getName());
					BasicClass note = activity.getFieldByTemplate("21");
								
					note.setProperty("Content", comment2);
								

					
					if(activity != null){
						result = new ActionResult(ActionResult.Status.OK, "Aktivität erstellt");	
						StringBuilder html = new StringBuilder();
						ActivityView.toHTML2(html, this, activity, context);
						result.setData(html.toString());
						result.setParam("dataContainer", "objectEditArea");
						activeObject = activity;
					}
				
				}
						
						
			}
		}
		else if(command.equals("memberaddetail")){
			result = new ActionResult(ActionResult.Status.OK, "Inserat geladen");	
			String memberadid = context.getString("memberadid");
			ocs.logAccess("memberadid: " + memberadid);
			if(memberadid != null && memberadid.length() > 0){
				MemberAd ma = (MemberAd)ocs.getObject(null, "MemberAd", "ID", memberadid, false);
				ma.setParent(ocs);
				ma.getProperty("Template").setSelection(ocs.getMemberAdAdministration().getObjects("MemberAdCategory"));
				ma.initObjectLocal();
				if(ma != null){
					
					result.setData(MemberAdDetail.getMemberAdDetailForm(ma, true, context));
					result.setParam("dataContainer", "objectEditArea");
					
				}
			}

		}
		else if(command.equals("reactivaterequest")){

			String memberadrequestid = 	context.getString("memberadrequestid");
			if(memberadrequestid != null && memberadrequestid.length() > 0){
				ocs.executeCommand("UPDATE MemberAdRequest SET Status=1 WHERE ID=" + memberadrequestid);	
				result = new ActionResult(ActionResult.Status.OK, "Liste geladen");	
				result.setData(UserProfileView.getObjectList(this, getOrganisationMember()));
				result.setParam("dataContainer", "objectList");
			}
		}
		else if(command.equals("memberadlistsend")){
			
			Activity activity = createActivity2(context, "5", 10);
			if(activity != null){
				result = new ActionResult(ActionResult.Status.OK, "Aktivität erstellt");	
				StringBuilder html = new StringBuilder();
				ActivityView.toHTML2(html, this, activity, context);
				result.setData(html.toString());
				result.setParam("dataContainer", "objectEditArea");
				activeObject = activity;
			}
			
		}
		else if(command.equals("memberadrequestlistsend")){
			
			Activity activity = createActivity2(context, "5", 11);
			if(activity != null){
				result = new ActionResult(ActionResult.Status.OK, "Aktivität erstellt");	
				StringBuilder html = new StringBuilder();
				ActivityView.toHTML2(html, this, activity, context);
				result.setData(html.toString());
				result.setParam("dataContainer", "objectEditArea");
				activeObject = activity;
			}
			
		}
		else if(command.equals("memberadlistprint")){
			
			PDFWriter pdfWriter = new PDFWriter();
			PDFTemplateLibrary templib = (PDFTemplateLibrary)ocs.getObjectByName("PDFTemplateLibrary", "1");
							
			String docname = ocs.createPassword(8);	
							
			String filename = ocs.getRootpath() + "/temp/" + docname + ".pdf";
							
			Vector instances = new Vector();
							
		    Record addresslist = new Record();
							
			Vector criteria1 = new Vector();
		    Vector criteria2 = new Vector();
			Vector criteria3 = new Vector();
							

							
			//String contacts = ocs.getUserProfile().getContactList(ocs, om.getName(), false, null, criteria1, criteria2, "Kontakte für " + om.toString());
			String memberads = ocs.getUserProfile().getMemberAdList(ocs, om.getName(), false, null, "Inserate für " + om.toString(), 1);
			
			memberads = memberads.replace("<br>", "\n");
							
			addresslist.addProperty("CONTACTLIST", "String" , memberads);
							
			instances.add(addresslist);
						
			pdfWriter.createPDF(filename, templib, "3", instances);	
							
			result = new ActionResult(ActionResult.Status.OK, "Seite geladen");							
							
			result.setParam("download", "../temp/" + docname + ".pdf");			
			
		}
		else if(command.equals("memberadrequestlistprint")){		
			PDFWriter pdfWriter = new PDFWriter();
			PDFTemplateLibrary templib = (PDFTemplateLibrary)ocs.getObjectByName("PDFTemplateLibrary", "1");
							
			String docname = ocs.createPassword(8);	
							
			String filename = ocs.getRootpath() + "/temp/" + docname + ".pdf";
							
			Vector instances = new Vector();
							
		    Record addresslist = new Record();
							
			Vector criteria1 = new Vector();
		    Vector criteria2 = new Vector();
			Vector criteria3 = new Vector();
							
			//Map addpars = request.getParameterMap();
							
			String contacts = ocs.getUserProfile().getContactList(ocs, om.getName(), false, null, criteria1, criteria2, "Kontakte für " + om.toString());
			contacts = contacts.replace("<br>", "\n");
							
			addresslist.addProperty("CONTACTLIST", "String" , contacts);
							
			instances.add(addresslist);
			
			
			/*
						
			pdfWriter.createPDF(filename, templib, "3", instances);
			
			*/
			
			try{
				String rootPath = ((OpenCommunityServer)getRoot()).getRootpath();
				
				PDFCreator app = new PDFCreator(rootPath);
							
				app.setParent(this);
				
				HashMap addpars = new HashMap();
				
				String html = AddressList.getAddressList(ocs, om, addpars, context, true);
				
				html = html.replace("&", "und");
				
				app.addTemplate("xhtml/docHeader.html");

				app.addSnippet(html);
				
				ocs.saveFile(rootPath + "/temp/test.html", html);
				app.addSnippet("<p style=\"page-break-after: always\"></p>");
				app.addTemplate("xhtml/docFooter.html");
				
				app.generate(filename);
				
			}
			catch(java.lang.Exception e){
				ocs.logException(e);
			}
							
			result = new ActionResult(ActionResult.Status.OK, "Seite geladen");							
							
			result.setParam("download", "../temp/" + docname + ".pdf");
							
		}
		else {
			result = new ActionResult(ActionResult.Status.FAILED, "Befehl '" + command + "' nicht gefunden");
		}
		return result;
	}
	public void activateProfile(ApplicationContext context){

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
	public void createLogin(){
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		ocs.logAccess("creating login ...");
		try{
						
			login = new Login();
			login.addProperty("OrganisationMemberID", "String", om.getName());
						
			String username = om.getName();
			while(username.length() < 7){
				username = "0" + username;
			}
						
			login.setProperty("Username", username);
			String password = ocs.createPassword(8);
			login.setProperty("Password", password);
						
			String id = ocs.insertObject(login);
						
			login = (Login)ocs.getObject(om, "Login", "ID", id);

		}
		catch(java.lang.Exception e){
			ocs.logException(e);
		}		
		
	}
	public Activity createNotification(ApplicationContext context, String newmemberadid){
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
					
		ObjectTemplate ot = ocs.getObjectTemplate("8");	
					
		Activity newactivity = (Activity)ot.createObject("ch.opencommunity.base.Activity", null, context);
		newactivity.applyTemplate(ot);
		newactivity.setProperty("Status", "1");
		newactivity.addProperty("OrganisationMemberID", "String", om.getName());

					

					
		newactivity.setProperty("Title", "Benachrichtigung");
		String activityid = ocs.insertObject(newactivity, true);
					
		if(newmemberadid != null){
						
			ActivityObject ao = new ActivityObject();
			ao.setProperty("MemberAdID", newmemberadid);
			ao.addProperty("ActivityID", "String", activityid);
						
			String id = ocs.insertSimpleObject(ao);
						
			ocs.executeCommand("UPDATE MemberAd SET NotificationStatus=1 WHERE ID=" + context.getString("newmemberadid"));
						
		}
					
		newactivity = (Activity)ocs.getObject(om, "Activity", "ID", activityid);
					
		newactivity.addObjectCollection("Results", "*");
			
		return newactivity;
		
	}
	public void activityObjectAdd(ApplicationContext context, Activity activity){

					
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
					
		String memberadid = context.getString("memberadid");
		String memberadrequestid = context.getString("memberadrequestid");
		String organisationmemberid = context.getString("organisationmemberid");
					
		if(memberadid != null && memberadid.length() > 0){
						
			ActivityObject ao = new ActivityObject();
			ao.setProperty("MemberAdID", memberadid);
			ao.addProperty("ActivityID", "String",  activity.getName());
						
			String id = ocs.insertSimpleObject(ao);
						
			ocs.getObject(activity, "ActivityObject", "ID", id);
						
			ocs.executeCommand("UPDATE MemberAd SET NotificationStatus=1 WHERE ID=" + memberadid);
						
		}
		else if(memberadrequestid != null && memberadrequestid.length() > 0){
						
			ActivityObject ao = new ActivityObject();
			ao.setProperty("MemberAdRequestID", memberadrequestid);
			ao.addProperty("ActivityID", "String",  activity.getName());
						
			String id = ocs.insertSimpleObject(ao);
						
			ocs.getObject(activity, "ActivityObject", "ID", id);
						
			ocs.executeCommand("UPDATE MemberAdRequest SET NotificationStatus=1 WHERE ID=" + memberadrequestid);
						
		}
		else if(organisationmemberid != null && organisationmemberid.length() > 0){
			if(organisationmemberid.equals(om.getName())){
				ActivityObject ao = new ActivityObject();
							
				ao.setProperty("OrganisationMemberID", organisationmemberid);
				ao.addProperty("ActivityID", "String", activity.getName());
							
				String id = ocs.insertSimpleObject(ao);
							
				ocs.getObject(activity, "ActivityObject", "ID", id);
							
				om.setProperty("NotificationStatus", "1");
							
				ocs.updateObject(om);
						
			}
						
		}
	}
	public void roleAdd(ApplicationContext context){
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
					mr.addProperty("OrganisationMemberID", "String", om.getName());
					mr.setProperty("Role", roleid);
					String id = ocs.insertObject(mr);
					ocs.getObject(om, "MemberRole", "ID", id);
				}
			}
		}
		catch(java.lang.Exception e){
			ocs.logException(e);
		}	
	}
	public void roleDelete(ApplicationContext context){
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
	
	}
	public void contactAdd(ApplicationContext context){
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		try{
			int contactid = context.getID("contactid");
			
			ocs.logAccess("new contact : " + contactid);
			
			if(contactid > -1){
			
				ocs.logAccess("Neue Rolle " + contactid);
				Person person = om.getPerson();
				boolean hasContact = false;
				for(BasicClass bc : person.getObjects("Contact")){
					if(bc.getID("Type")==contactid){
						hasContact = true;	
					}
				}

				if(!hasContact){
					Contact contact = new Contact();

					contact.addProperty("PersonID", "String", person.getName());
					contact.setProperty("Type", contactid);
					String id = ocs.insertObject(contact);
					ocs.getObject(person, "Contact", "ID", id);
				}
			}
		}
		catch(java.lang.Exception e){
			ocs.logException(e);
		}
	}
	public void contactDelete(ApplicationContext context){
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		try{
			String contactid = context.getString("contactid");
			if(contactid != null && contactid.length() > 0){
				Person person = om.getPerson();
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
	
	}
	public Activity requestsCreate(ApplicationContext context, OpenCommunityUserSession userSession){
		return requestsCreate(context, null, userSession);	
	}
    public Activity requestsCreate(ApplicationContext context, Vector memberads, OpenCommunityUserSession userSession){
    	
    	Activity activity = null;
    	
    	setCreateRequest(false);
					
    	OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
    	
    	List<String> ids = null;
    	if(om.getMemberAdIDs() != null && om.getMemberAdIDs().size() > 0){
			MemberAdRequestGroup marg = (MemberAdRequestGroup)om.createObject("ch.opencommunity.advertising.MemberAdRequestGroup", null, context);
			String gid = ocs.insertObject(marg);
						
			ids = OpenCommunityUserProfile.createMemberAdRequests(context, getOrganisationMember(), gid, null, 1);
		}
		
		List<String> ids2 = null;
		if(memberads != null){
			ids2 = new Vector();
			for(Object o : memberads){
				String id = ocs.insertObject((MemberAd)o, true);
				ids2.add(id);
			}
			
		}
					
					
    	//Brief erstellen
					
		if(context.hasProperty("createpdf") || 	context.hasProperty("email")){		
					
			//ObjectTemplate ot = ocs.getObjectTemplate("7");
			ObjectTemplate ot = ocs.getObjectTemplate("8");
						
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
			newactivity.addProperty("OrganisationMemberID", "String", getOrganisationMember().getName());
	
						
			//BasicClass note = newactivity.getFieldByTemplate("26");
			BasicClass note = newactivity.getFieldByTemplate("27");
						
			if(context.hasProperty("email") && context.getString("email").equals("true")){
				note = newactivity.getFieldByTemplate("16");
			}
											
						
											
											
			note.setProperty("Content", content);
						
	
						
			newactivity.setProperty("Title", "Benachrichtigung");
			String activityid = ocs.insertObject(newactivity, true);
						
	
			if(ids != null){		
				for(String id : ids){
					ActivityObject ao = new ActivityObject();
					ao.addProperty("ActivityID", "String", activityid);
					ao.setProperty("MemberAdRequestID", id);
					ocs.insertObject(ao);
				}
			}
			if(ids2 != null){
				for(String id : ids2){
					ActivityObject ao = new ActivityObject();
					ao.addProperty("ActivityID", "String", activityid);
					ao.setProperty("MemberAdID", id);
					ocs.insertObject(ao);
				}				
			}

			
			activity = (Activity)ocs.getObject(getOrganisationMember(), "Activity", "ID", activityid);
			
		}
		
		if(ids2 != null){
			for(String id : ids2){
				ocs.getObject(om, "MemberAd", "ID", id);
			}				
		}
		
		userSession.removeMemberAdIDs(); // AK 2017-08-30
								
		return activity;
		
	}
	public void loadObject(BasicClass object, String id){
		OpenCommunityServer ocs = (OpenCommunityServer)om.getRoot();
		if(object.getClass().getSimpleName().equals("Activity")){

			ocs.getObject(om, "Activity", "ID", id);	
	
			
		}
	}
	public String getEmailAddress(){
		String email = null;
		if(person != null){
			for(BasicClass contact : person.getObjects("Contact")){
				if(contact.getID("Type")==3){
					email = contact.getString("Value");	
				}
				
			}
		}
		return email;
	}
	
}