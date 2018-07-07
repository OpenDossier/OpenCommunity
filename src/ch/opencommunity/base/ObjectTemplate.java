package ch.opencommunity.base;

import ch.opencommunity.server.OpenCommunityServer;

import org.kubiki.application.ApplicationContext;
import org.kubiki.base.ActionResult;
import org.kubiki.base.BasicClass;

public class ObjectTemplate extends BasicOCObject{ 

	public ObjectTemplate(){
		setTablename("ObjectTemplate");
		
		addProperty("Type", "Integer", "1");
		addProperty("Scope", "Integer", "1");
		addProperty("ShowDate", "Boolean", "false");
		addProperty("ShowStatus", "Boolean", "false");
		addProperty("IsSendable", "Boolean", "false");
		addProperty("IsPrintable", "Boolean", "false");
		addProperty("HasObjects", "Boolean", "false");
		
		addObjectCollection("FieldDefinition", "ch.opencommunity.base.FieldDefinition");
		addObjectCollection("StatusDefinition", "ch.opencommunity.base.StatusDefinition");
		addContextMenuEntry("fielddefinitionadd", "Neues Feld", true);
		addContextMenuEntry("statusdefinitionadd", "Neuer Status", true);
	}
	public ActionResult onAction(BasicClass source, String command, ApplicationContext context) {
		
		ActionResult result = null;
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		if(command.equals("fielddefinitionadd")){
			FieldDefinition fd = new FieldDefinition();
			fd.addProperty("ObjectTemplateID", "String", getName());
			String id = ocs.insertObject(fd);
			ocs.getObject(this, "FieldDefinition", "ID", id);
			result = new ActionResult(ActionResult.Status.OK, "Datensatz gespeichert");
			result.setParam("refresh", "tree");	
			result.setParam("treeObjectRoot", getParent().getPath());	
		}
		else if(command.equals("statusdefinitionadd")){
			StatusDefinition sd = new StatusDefinition();
			sd.addProperty("ObjectTemplateID", "String", getName());
			String id = ocs.insertObject(sd);
			ocs.getObject(this, "StatusDefinition", "ID", id);
			result = new ActionResult(ActionResult.Status.OK, "Datensatz gespeichert");
			result.setParam("refresh", "tree");	
			result.setParam("treeObjectRoot", getParent().getPath());
			
		}
		else{
			result = super.onAction(source, command, context);	
		}
		return result;
	}
	public String toString(){
		return getString("Title");
	}
	public void initObjectLocal(){
		getObjectCollection("FieldDefinition").sort("SortOrder", "Integer", true);	
	}

}
