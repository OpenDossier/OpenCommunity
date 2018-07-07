package ch.opencommunity.advertising;


import ch.opencommunity.base.*;
import ch.opencommunity.server.*;

import org.kubiki.base.BasicClass;
import org.kubiki.base.ActionResult;
import org.kubiki.application.ApplicationContext;

import java.util.Hashtable;

public class MemberAdCategory extends BasicOCObject{
	
	Hashtable<String, BasicClass> fieldMap;
	public MemberAdCategory(){
		setTablename("MemberAdCategory");
		
		addProperty("TandemAllowed", "Boolean", "false");
		addProperty("RequestOnly", "Boolean", "false");
		addProperty("Protected", "Boolean", "false");
		
		addObjectCollection("FieldDefinition", "ch.opencommunity.base.FieldDefinition");
		
		addContextMenuEntry("fielddefinitionadd");
		
		fieldMap = new Hashtable<String, BasicClass>();
	}
	public String toString(){
		return getString("Title");
	}
	public void initObjectLocal(){
		for(BasicClass bc  : getObjects("FieldDefinition")){
			fieldMap.put(bc.getName(), bc);
		}
		getObjectCollection("FieldDefinition").sort("SortOrder");
	}
	public FieldDefinition getFieldDefinitionByTemplate(String name){
		return (FieldDefinition)fieldMap.get(name);
	}
	public ActionResult onAction(BasicClass src, String command, ApplicationContext context){
		ActionResult result = null;
		if(command.equals("fielddefinitionadd")){
			
			OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
			
			FieldDefinition fieldDefinition = new FieldDefinition();
			
			fieldDefinition.addProperty("MemberAdCategoryID", "String", getName());
			
			String id = ocs.insertObject(fieldDefinition);
			
			ocs.getObject(this, "FieldDefiniton", "ID", id);
			
			
		}
		else{
			result = super.onAction(src, command, context);	
		}
		return result;
	}
	public boolean saveObject(ApplicationContext context){
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		super.saveObject(context);
		
		for(BasicClass bc : getObjects("FieldDefinition")){
			if(context.hasProperty("title_" + bc.getName())){
				bc.setProperty("Title", context.getString("title_" + bc.getName()));
			}
			if(context.hasProperty("alttitle_" + bc.getName())){
				bc.setProperty("AltTitle", context.getString("alttitle_" + bc.getName()));
			}
			if(context.hasProperty("type_" + bc.getName())){
				bc.setProperty("Type", context.getString("type_" + bc.getName()));
			}
			if(context.hasProperty("codelist_" + bc.getName())){
				bc.setProperty("CodeList", context.getString("codelist_" + bc.getName()));
			}
			if(context.hasProperty("sortorder_" + bc.getName())){
				bc.setProperty("SortOrder", context.getString("sortorder_" + bc.getName()));
			}
			ocs.updateObject(bc);
		}
		
		
		return true;
	}
} 
