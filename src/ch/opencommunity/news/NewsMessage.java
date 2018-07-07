package ch.opencommunity.news;


import ch.opencommunity.base.*;
import ch.opencommunity.server.*;

import org.kubiki.base.ConfigValue;
import org.kubiki.base.ObjectCollection;
import org.kubiki.util.DateConverter;
import org.kubiki.base.BasicClass;
import org.kubiki.base.UploadHandler;
import org.kubiki.base.ActionResult;
import org.kubiki.application.FileObjectData;
import org.kubiki.application.ApplicationContext;

import java.util.Vector;

import org.apache.commons.fileupload.FileItem;

public class NewsMessage extends BasicOCObject implements UploadHandler{
	
	String[] types = {"", "Mitteilung", "Veranstaltung","Presse"};
	
	
	public NewsMessage(){
		
		setTablename("NewsMessage");
		
		addProperty("DateStart", "DateTime", "", false, "Datum Von");
		addProperty("DateEnd", "DateTime", "", false, "Datum bis");	
		addProperty("Type", "Integer", "", false, "Typ");
		addProperty("Scope", "Integer", "", false, "Bereich");
		//addProperty("Description", "Text",  "", false, "Beschreibung");
		addProperty("Description", "FormattedText",  "", false, "Beschreibung");
		addProperty("URL", "String",  "", false, "Link",  255);		
		addProperty("URLTitle", "String",  "", false, "Link Beschreibung",  255);	
		
		ObjectCollection oc = addObjectCollection("FileObjectData", "org.kubiki.application.FileObjectData");
		//oc.setPreloadObjects(false);
	}
	public void initObjectLocal(){
		Vector types = new Vector();
		types.add(new ConfigValue("1", "1", "Mitteilung"));
		types.add(new ConfigValue("2", "2", "Veranstaltung"));
		types.add(new ConfigValue("3", "3", "Presse"));
		getProperty("Type").setSelection(types);
		
		Vector scope = new Vector();
		scope.add(new ConfigValue("1", "1", "Extern"));
		scope.add(new ConfigValue("2", "2", "Intern"));
		scope.add(new ConfigValue("3", "3", "Extern und Intern"));
		getProperty("Scope").setSelection(scope);
		
		Vector status = new Vector();
		status.add(new ConfigValue("0", "0", "Aktuell"));
		status.add(new ConfigValue("1", "1", "Archiviert"));
		status.add(new ConfigValue("2", "2", "Zu löschen"));
		getProperty("Status").setSelection(status);
		
		addProperty("Attachment", "FileUpload", "", false, "Attachment");
	}
	public String getLabel(){
		return types[getID("Type")] + " " + getString("Title") + " " + DateConverter.sqlToShortDisplay(getString("DateStart"), false);	
		//return getName();	
	}
	public ActionResult onAction(BasicClass source, String command, ApplicationContext context) {
		
		ActionResult result = null;
		
		if(command.equals("saveobject")){

			if(saveObject(context)){
				result = new ActionResult(ActionResult.Status.OK, "Datensatz gespeichert");
				
				getParent().getObjectCollection("NewsMessage").sort("DateStart", "String", false);
				
				result.setParam("refresh", "currentsection");		
				result.setParam("exec", "editObject('" + getPath() + "')");				
			}
			else{
				result = new ActionResult(ActionResult.Status.FAILED, "Fehler beim Speichern");
			}
		}
		
		return result;
		
	}
	public Object handleUpload(ApplicationContext context, FileItem fileItem){
		
		OpenCommunityServer server = (OpenCommunityServer)getRoot();
		
		if(fileItem != null){
			FileObjectData fileObjectData = (FileObjectData)getObjectByIndex("FileObjectData", 0);
			if(fileObjectData==null){
				fileObjectData = new FileObjectData();
				fileObjectData.addProperty("NewsMessageID", "String", getName());
				fileObjectData.setProperty("FileName", fileItem.getName());
				fileObjectData.setProperty("FileData", fileItem.get());
				String id = server.insertObject(fileObjectData);
				server.getObject(this, "FileObjectData", "ID", id);
				
				
			}
			else{
				fileObjectData.setProperty("FileName", fileItem.getName());
				fileObjectData.setProperty("FileData", fileItem.get());
				server.updateObject(fileObjectData);				
				
			}
			
			
		}
		
		ActionResult result = new ActionResult(ActionResult.Status.OK, "Datei hochgeladen");
		result.setParam("exec", "editObject('" + getPath() + "')");

		return result;
	}
}