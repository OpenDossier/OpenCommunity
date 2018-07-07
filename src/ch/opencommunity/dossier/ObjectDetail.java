package ch.opencommunity.dossier;

import ch.opencommunity.server.OpenCommunityServer;
import ch.opencommunity.base.ObjectTemplate;
import ch.opencommunity.base.FieldDefinition;
import ch.opencommunity.base.Note;
import ch.opencommunity.base.Parameter;


import org.kubiki.base.BasicClass;
import org.kubiki.base.ObjectCollection;
import org.kubiki.base.ActionHandler;
import org.kubiki.base.ActionResult;
import org.kubiki.application.ApplicationContext;
import org.kubiki.application.DataObject;

import java.util.Vector;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ObjectDetail extends DataObject{
	
	ObjectTemplate ot = null;
	Map<String, BasicClass> fieldsByVarname = null;
	
	ObjectCollection fields = null;
	
	public ObjectDetail(){
		setTablename("ObjectDetail");

		addObjectCollection("Parameter", "ch.opendossier.base.Parameter");
		addObjectCollection("Note", "ch.opendossier.base.Note");
			
	}
	public ActionResult onAction(BasicClass source, String command, ApplicationContext context) {
		
		ActionResult result = null;
		
		return result;
		
	}
	
	public void initObjectLocal(){
		
		fieldsByVarname = new HashMap<String, BasicClass>();
		
		OpenCommunityServer server = (OpenCommunityServer)getRoot();
		ot = server.getObjectTemplate(getID("Template") + "");
		server.logAccess("init objectdetail " + ot);
		if(ot != null){
			setProperty("Template", ot);
			applyTemplate(ot, server);
			server.logAccess("init activity " + getObject("Template").getClass().getName());
			getObjectCollection("Parameter").sort("Template");
		}
		
		// ersetzen durch spezielle view bzw. controller class
		fields = addObjectCollection("Fields", "*");

		
		rebuildList();
	}
	public void rebuildList(){
		
		fields.removeObjects();
		fields.getObjects().addAll(getObjects("Parameter"));
		fields.getObjects().addAll(getObjects("Note"));	
		for(BasicClass bc : fields.getObjects()){
			if(bc.getObject("Template") instanceof FieldDefinition){
				FieldDefinition fd = (FieldDefinition)bc.getObject("Template");
				if(bc.hasProperty("SortOrder")){					
					bc.setProperty("SortOrder", fd.getString("SortOrder"));
				}
				else{
					bc.addProperty("SortOrder", "Integer", fd.getString("SortOrder"));					
				}
			}
		}	
		fields.sort("SortOrder");
	}
	public void applyTemplate(ObjectTemplate ot, OpenCommunityServer server){
		setProperty("Template", ot);

		Vector fielddefinitions = ot.getObjects("FieldDefinition");
		for(int i = 0; i < fielddefinitions.size(); i++){
			FieldDefinition fd = (FieldDefinition)fielddefinitions.elementAt(i);
			
			
			if(!hasField(fd.getName())){
				if(fd.getID("Type")==4){
					Note note = new Note();
					note.setParent(this);
					note.addProperty("ObjectDetailID", "String", getName());
					note.setProperty("Title", fd.getString("Title"));
					note.setProperty("Template", fd.getName());
					String id = server.insertSimpleObject(note);
					server.getObject(this, "Note", "ID", id);

				}
				else if(fd.getID("Type")==5){
					Parameter parameter = new Parameter();
					parameter.setParent(this);
					parameter.addProperty("ObjectDetailID", "String", getName());
					parameter.setProperty("Title", fd.getString("Title"));
					parameter.setProperty("Template", fd.getName());
					String id = server.insertSimpleObject(parameter);
					server.getObject(this, "Parameter", "ID", id);
				}
				else if(fd.getID("Type")==6){
					Parameter parameter = new Parameter();
					parameter.setParent(this);
					parameter.addProperty("ObjectDetailID", "String", getName());
					parameter.setProperty("Title", fd.getString("Title"));
					parameter.setProperty("Template", fd.getName());
					String id = server.insertSimpleObject(parameter);
					server.getObject(this, "Parameter", "ID", id);
				}
				else if(fd.getID("Type")==7){
					Parameter parameter = new Parameter();
					parameter.setParent(this);
					parameter.addProperty("ObjectDetailID", "String", getName());
					parameter.setProperty("Title", fd.getString("Title"));
					parameter.setProperty("Template", fd.getName());
					String id = server.insertSimpleObject(parameter);
					server.getObject(this, "Parameter", "ID", id);
				}
				BasicClass field = getFieldByTemplate(fd.getName());
				field.setProperty("Template", fd);
				fieldsByVarname.put(fd.getString("Title"), field);
			}
			else{
				BasicClass field = getFieldByTemplate(fd.getName());
				server.logAccess("field " + field);
				field.setProperty("Template", fd);
				
				fieldsByVarname.put(fd.getString("Title"), field);
			}
			if(fd.getBoolean("IsMultiple")){
				for(BasicClass field : getFieldsByTemplate(fd.getName())){
					field.setProperty("Template", fd);		
				}
				
			}
		}
	}
	public BasicClass getFieldByVarname(String varname){
		return fieldsByVarname.get(varname);
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
	public List<BasicClass> getFieldsByTemplate(String sTemplate){
		Vector<BasicClass> fields = new Vector<BasicClass>();
		int template = Integer.parseInt(sTemplate);
		for(BasicClass bc : getObjects("Parameter")){
			if(bc.getID("Template")==template){
				fields.add(bc);
			}
		}		
		
		return fields;
		
	}
	public ObjectTemplate getTemplate(){
		if(getObject("Template") instanceof ObjectTemplate){
			return (ObjectTemplate)getObject("Template");
		}
		else{
			return null;
		}
	}
		
		
	
	
}