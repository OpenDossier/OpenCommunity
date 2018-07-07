package ch.opencommunity.base;

import ch.opencommunity.server.OpenCommunityServer;
import ch.opencommunity.common.OpenCommunityUserSession;

import org.kubiki.base.BasicClass;
import org.kubiki.base.ActionResult;
import org.kubiki.base.ObjectCollection;
import org.kubiki.application.ApplicationContext;

import java.util.Vector;
import java.util.List;

public class Activity extends BasicOCObject{
	
	ObjectTemplate ot;
	


	public Activity(){
		
		setTablename("Activity");
		
		addProperty("Date", "Date", "");
		
		addProperty("Context", "Integer", "");
		
		addProperty("BatchActivityID", "Integer", "");
		
		addProperty("Attachments", "Text", "");
		
		/*

		1 Registrierung
		2 Registrierung und Inserat
		3 Registrierung und Adressbestellung
		4 Registrierung und Inserat und Adressbestellung
		5 Inserat
		
		8 Adressbestellung
		9 Freier Brief
		
		
		
		*/
		
		
		addObjectCollection("Parameter", "ch.opencommunity.base.Parameter");
		addObjectCollection("Note", "ch.opencommunity.base.Note");
		addObjectCollection("ActivityObject", "ch.opencommunity.base.ActivityObject");
		//addObjectCollection("ActivityOrganisationMember", "ch.opencommunity.base.ActivityOrganisationMember");
		addObjectCollection("ActivityParticipant", "ch.opencommunity.base.ActivityParticipant");
		
		ObjectCollection oc = addObjectCollection("MailMessageInstance", "org.kubiki.mail.MailMessageInstance");
		oc.setPreloadObjects(false);
		
	}
	public void applyTemplate(ObjectTemplate ot){
		setProperty("Template", ot);
		Vector fielddefinitions = ot.getObjects("FieldDefinition");
		for(int i = 0; i < fielddefinitions.size(); i++){
			FieldDefinition fd = (FieldDefinition)fielddefinitions.elementAt(i);
			if(!hasField(fd.getName())){
				if(fd.getID("Type")==4){
					Note note = new Note();
					note.setName("" + (-i));
					note.setParent(this);
					note.addProperty("ActivityID", "String", getName());
					note.setProperty("Title", fd.getString("Title"));
					note.setProperty("Template", fd.getName());
					addSubobject("Note", note);
					note.setProperty("Template", fd);
				}
				else if(fd.getID("Type")==5){
					Parameter parameter = new Parameter();
					parameter.setName("" + (-i));
					parameter.setParent(this);
					parameter.addProperty("ActivityID", "String", getName());
					parameter.setProperty("Title", fd.getString("Title"));
					parameter.setProperty("Template", fd.getName());
					addSubobject("Parameter", parameter);
					parameter.setProperty("Template", fd);
				}
				else if(fd.getID("Type")==6){
					Parameter parameter = new Parameter();
					parameter.setName("" + (-i));
					parameter.setParent(this);
					parameter.addProperty("ActivityID", "String", getName());
					parameter.setProperty("Title", fd.getString("Title"));
					parameter.setProperty("Template", fd.getName());
					addSubobject("Parameter", parameter);
					parameter.setProperty("Template", fd);
				}
				else if(fd.getID("Type")==7){
					Parameter parameter = new Parameter();
					parameter.setName("" + (-i));
					parameter.setParent(this);
					parameter.addProperty("ActivityID", "String", getName());
					parameter.setProperty("Title", fd.getString("Title"));
					parameter.setProperty("Template", fd.getName());
					addSubobject("Parameter", parameter);
					parameter.setProperty("Template", fd);
				}
			}
			else{
				BasicClass field = getFieldByTemplate(fd.getName());
				field.setProperty("Template", fd);
			}
		}
	}
	public void initObjectLocal(){
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		ot = ocs.getObjectTemplate(getID("Template") + "");
		ocs.logAccess("init activity " + ot);
		if(ot != null){
			setProperty("Template", ot);
			applyTemplate(ot);
			ocs.logAccess("init activity " + getObject("Template").getClass().getName());
			if(ot.getObjects("StatusDefinition").size() > 0){
				getProperty("Status").setSelection(ot.getObjects("StatusDefinition"));
			}
		}
		

		
	}
	public boolean hasField(String sTemplate){
		
		try{
		
			int template = Integer.parseInt(sTemplate);
			
			boolean hasField = false;
			
			for(BasicClass bc : getObjects("Parameter")){
				if(bc.getID("Template")==template){
					hasField = true;		
				}
			}
			for(BasicClass bc : getObjects("Note")){
				if(bc.getID("Template")==template){
					hasField = true;		
				}
			}
			return hasField;
			
		}
		catch(java.lang.Exception e){
			return false;	
		}
	}
	public BasicClass getFieldByTemplate(String sTemplate){
		BasicClass field = null;	
		int template = Integer.parseInt(sTemplate);
		for(BasicClass bc : getObjects("Parameter")){
			if(bc.getID("Template")==template){
				field = bc;
			}
		}
		for(BasicClass bc : getObjects("Note")){
			if(bc.getID("Template")==template){
				field = bc;
			}
		}
		return field;
	}
	public String toString(){
		return getName() + "," + getString("DateCreated") + " " + getString("Template");	
	}
	public boolean saveObject(ApplicationContext context, String prefix){
		
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		
		for(BasicClass note : getObjects("Note")){
			if(context.hasProperty("note_" + note.getName() + "_Content")){
				note.setProperty("Content", context.getString("note_" + note.getName() + "_Content"));
				if(!getName().equals("-1")){
					((Note)note).saveObject(userSession);
				}
			}
			
		}
		for(BasicClass parameter : getObjects("Parameter")){
			if(context.hasProperty("parameter_" + parameter.getName() + "_Value")){
				parameter.setProperty("Value", context.getString("parameter_" + parameter.getName() + "_Value"));
				if(!getName().equals("-1")){
					((Parameter)parameter).saveObject(userSession);
				}
			}
			
		}
		List<String> fieldNames = ps.getNames();
		for(String fieldName : fieldNames){
			String value = context.getString(prefix + fieldName);
			if(value != null){
				setProperty(fieldName, value);
			}
		}
		if(getName().equals("-1")){
			String id = ocs.insertObject(this, true);
			if(getParent() instanceof BaseController){
				((BaseController)getParent()).loadObject(this, id);	
			}
			setName(id);
			for(BasicClass note : getObjects("Note")){
				note.setProperty("ActivityID", id);	
			}
			for(BasicClass parameter : getObjects("Parameter")){
				parameter.setProperty("ActivityID", id);	
			}
			return true;
		}
		else{
			return saveObject(userSession);
		}
	}
	public ActionResult onAction(BasicClass source, String command, ApplicationContext context) {
		
		ActionResult result = null;
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		if(command.equals("saveobject")){
			ocs.logAccess("saving object");
			if(saveObject(context)){
				result = new ActionResult(ActionResult.Status.OK, "Datensatz gespeichert");
				//result.setParam("refresh", "currentsection");		
				result.setParam("objectlist", getParent().getPath());	
				//OpenDossierServer.checkReturnTarget(context.getString("returnTo"), result);
			}
			else{
				result = new ActionResult(ActionResult.Status.FAILED, "Fehler beim Speichern");
			}
		}
		else {
			result = new ActionResult(ActionResult.Status.FAILED, "Befehl '" + command + "' nicht gefunden");
		}
		return result;
	}
}