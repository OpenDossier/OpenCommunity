package ch.opencommunity.dossier;

import ch.opencommunity.base.BasicOCObject;
import ch.opencommunity.base.OrganisationalUnit;
import ch.opencommunity.base.Address;
import ch.opencommunity.base.Parameter;
import ch.opencommunity.base.Note;
import ch.opencommunity.common.OpenCommunityUserSession;

import org.kubiki.base.BasicClass;
import org.kubiki.base.ActionHandler;
import org.kubiki.base.ActionResult;
import org.kubiki.base.Property;


import org.kubiki.application.ApplicationContext;
import org.kubiki.application.server.WebApplicationContext;
import org.kubiki.application.server.ApplicationServer;
import org.kubiki.application.DataObject;

import java.util.List;
import java.util.Enumeration;

public class Dossier extends DataObject{
	
	OrganisationalUnit ou = null;
	
	public Dossier(){
		
		setTablename("Dossier");
		
		addProperty("OrganisationalUnit", "Integer", "");
		
		addObjectCollection("CaseRecord", "ch.opendossier.dossier.CaseRecord");
		addObjectCollection("ObjectDetail", "ch.opendossier.dossier.ObjectDetail");
		addObjectCollection("Project", "ch.opendossier.dossier.Project");
		
	}
	public ActionResult onAction(BasicClass source, String command, ApplicationContext context) {
		
		ActionResult result = null;
		
		if(command.equals("saveobject")){
			if(saveObject(context, "")){
			     result = new ActionResult(ActionResult.Status.OK, "Dossier gespeichert");
			}
			else{
				 result = new ActionResult(ActionResult.Status.FAILED, "Problem beim Speichern");
			}
				
			
		}
		
		return result;
		
	}
	public void initObjectLocal(){
		
		ApplicationServer server = (ApplicationServer)getRoot();
		
		server.logAccess("initializing dossier " + this);
		
		if(getID("OrganisationalUnit") > 0){
			ou = (OrganisationalUnit)server.getObjectByName("OrganisationalUnit", "" + getID("OrganisationalUnit"));
			if(ou != null){
				setProperty("OrganisationalUnit", ou);	
			}
		}
		addProperty("objecttemplate", "Integer", "");
	}
	public boolean saveObject(ApplicationContext context, String prefix){
		
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
		List<String> fieldNames = ps.getNames();
		for(String fieldName : fieldNames){
			String value = context.getString(prefix + fieldName);
			if(value != null){
				setProperty(fieldName, value);
			}
		}
		if(getObject("OrganisationalUnit") instanceof OrganisationalUnit){
			
			OrganisationalUnit ou = (OrganisationalUnit)getObject("OrganisationalUnit");
			
			prefix = "organisation_";
			
			fieldNames = ou.getPropertySheet().getNames();
			for(String fieldName : fieldNames){
			
				String value = context.getString(prefix + fieldName);
				if(value != null){
					ou.setProperty(fieldName, value);
				}
			}	
			
			Address address = ou.getAddress();
			if(address != null){
				prefix = "address_";
				fieldNames = address.getPropertySheet().getNames();
				for(String fieldName : fieldNames){
				
					String value = context.getString(prefix + fieldName);
					if(value != null){
						address.setProperty(fieldName, value);
					}
				}	
				
			}
			for(BasicClass contact : ou.getObjects("Contact")){
				if(context.hasProperty("contact_" + contact.getName() + "_Value")){
					contact.setProperty("Value", context.getString("contact_" + contact.getName() + "_Value"));
				}
			}
			for(int i = 0;i < getObjects("ObjectDetail").size(); i++){
				ObjectDetail objectDetail = (ObjectDetail)getObjects("ObjectDetail").get(i);
				for(int j = 0; j < objectDetail.getObjects("Parameter").size(); j++){
					Parameter parameter = (Parameter)objectDetail.getObjects("Parameter").get(j);
					String value = context.getString("parameter_" + parameter.getName());
					if(value != null){
						parameter.setProperty("Value", value);
					}
					value = context.getString("parameter_comment_" + parameter.getName());
					if(value != null){
						parameter.setProperty("Comment", value);
					}
				}
				for(int j = 0; j < objectDetail.getObjects("Note").size(); j++){
					Note note = (Note)objectDetail.getObjects("Note").get(j);
					String value = context.getString("note_" + note.getName());
					if(value != null){
						note.setProperty("Content", value);
					}

				}
				
				
			}
		}
		saveDossier();
		return true;
	}
	public void saveDossier(){
		
		ApplicationServer server = (ApplicationServer)getRoot();
		
		server.logAccess("saving dossier " + getName());
		
		server.updateObject(this);
		
		if(getObject("OrganisationalUnit") instanceof OrganisationalUnit){
			OrganisationalUnit ou = (OrganisationalUnit)getObject("OrganisationalUnit");
			server.updateObject(ou);
			Address address = ou.getAddress();
			if(address != null){
				server.updateObject(address);				
			}
			for(BasicClass contact : ou.getObjects("Contact")){
				server.updateObject(contact);
			}
		}
		for(int i = 0;i < getObjects("ObjectDetail").size(); i++){
				
			ObjectDetail objectDetail = (ObjectDetail)getObjects("ObjectDetail").get(i);
			for(int j = 0; j < objectDetail.getObjects("Parameter").size(); j++){
				Parameter parameter = (Parameter)objectDetail.getObjects("Parameter").get(j);
				server.updateObject(parameter);
			}
			for(int j = 0; j < objectDetail.getObjects("Note").size(); j++){
				Note note = (Note)objectDetail.getObjects("Note").get(j);
				server.updateObject(note);
			}
				
				
		}
		
	}
	public OrganisationalUnit getOrganisationalUnit(){
		return ou;
	}
	
}