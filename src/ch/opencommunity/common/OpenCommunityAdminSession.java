package ch.opencommunity.common;

import ch.opencommunity.server.OpenCommunityServer;
import ch.opencommunity.query.QueryDefinition;
import ch.opencommunity.base.Document;

import org.kubiki.base.ActionResult;
import org.kubiki.base.BasicClass;
import org.kubiki.base.UploadHandler;
import org.kubiki.application.ApplicationContext;
import org.kubiki.application.server.ApplicationServer;
import org.kubiki.application.FileObjectData;

import org.kubiki.xml.XMLParser;
import org.kubiki.xml.XMLElement;
import org.kubiki.xml.XMLWriter;

import java.util.HashMap;
import java.util.Vector;

import org.apache.commons.fileupload.FileItem;


public class OpenCommunityAdminSession extends OpenCommunityUserSession implements UploadHandler{
	
	
	QueryDefinition currentQueryDefinition;
	
	
	public OpenCommunityAdminSession(){
		
		addObjectCollection("OrganisationMemberController", "ch.opencommunity.base.OrganisationMemberController");	
		addObjectCollection("DossierController", "ch.opencommunity.dossier.DossierController");	
		
	}
	public ActionResult onAction(BasicClass source, String command, ApplicationContext context) {
		
		ActionResult result = null;
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		
		ocs.logAccess("executeing " + command);
		
		if(command.equals("querydefinitionadd")){
			
			ocs.logAccess("executeing 2 " + command);
			HashMap params = new HashMap();
			if(context.hasProperty("scope")){
				params.put("Scope", context.getString("scope"));	
			}
			result = ocs.startProcess("ch.opencommunity.process.QueryDefinitionAdd", this, params, context, this);
			
			
		}
		else if(command.equals("setquery")){
			
			String queryid = context.getString("queryid");
			QueryDefinition queryDefinition = (QueryDefinition)ocs.getObjectByName("QueryDefinition", queryid);
			if(queryDefinition != null){
				currentQueryDefinition = queryDefinition;
				String xml = queryDefinition.getString("XML");
				ocs.logAccess(xml);
				XMLElement xmlDoc = XMLParser.parseString(xml);
				if(xmlDoc.getChild("ch.opencommunity.common.OpenCommunityAdminSession") != null){
					XMLParser.parseSubelements(this, xmlDoc.getChild(0));	
					XMLParser.setProperties(this, xmlDoc.getChild(0));
					result = new ActionResult(ActionResult.Status.OK, "Filter geladen");
					result.setParam("refresh", "currentsection");
				}
			}
			
		}
		else if(command.equals("setquery2")){
			
			String queryid = context.getString("queryid");
			QueryDefinition queryDefinition = (QueryDefinition)ocs.getObjectByName("QueryDefinition", queryid);
			ocs.logAccess("queryid : " + queryid);
			if(queryDefinition != null){

				currentQueryDefinition = queryDefinition;
				String xml = queryDefinition.getString("XML");
				ocs.logAccess(xml);
				XMLElement xmlDoc = XMLParser.parseString(xml);
				if(xmlDoc.getChild("ch.opencommunity.common.OpenCommunityAdminSession") != null){
					XMLParser.parseSubelements(this, xmlDoc.getChild(0));	
					XMLParser.setProperties(this, xmlDoc.getChild(0));
					ocs.logAccess("dada: " + getObject("Purpose"));
					result = new ActionResult(ActionResult.Status.OK, "Filter geladen");
					result.setParam("refresh", "currentsection");
				}
				getProperty("CurrentQueryDefinition2").setObject(queryDefinition);
			}

			//loadQueries(ocs);
			//ocs.logAccess(getObject("CurrentQueryDefinition2").getClass().getName());
			
		}
		else if(command.equals("querydefinitionsave")){
			
			if(currentQueryDefinition != null){

				currentQueryDefinition.setProperty("XML", XMLWriter.toXML(this));
				ocs.updateObject(currentQueryDefinition);
				result = new ActionResult(ActionResult.Status.OK, "Filter gespeichert");
				result.setParam("refresh", "currentsection");
				
			}
				
		}
		else if(command.equals("querydefinitiondelete")){
			
			if(currentQueryDefinition != null){
				
				ocs.deleteObject(currentQueryDefinition);
				currentQueryDefinition = null;
				getProperty("CurrentQueryDefinition2").setObject("");
				loadQueries(ocs);
				result = new ActionResult(ActionResult.Status.OK, "Filter gelöscht");
				result.setParam("refresh", "currentsection");				
			}
		}
		else{
			super.onAction(source, command, context);
		}
		
		
		
		/*
		if(command.equals("saveobject")){
			ocs.logAccess("saving object");
			if(saveObject(context)){
				result = new ActionResult(ActionResult.Status.OK, "Datensatz gespeichert");
				result.setParam("refresh", "currentsection");			
				//OpenDossierServer.checkReturnTarget(context.getString("returnTo"), result);
			}
			else{
				result = new ActionResult(ActionResult.Status.FAILED, "Fehler beim Speichern");
			}
		}
		else {
			result = new ActionResult(ActionResult.Status.FAILED, "Befehl '" + command + "' nicht gefunden");
		}
		*/
		return result;
	}
	public String toString(){
		return "adminsession";	
	}
	private void loadQueries(OpenCommunityServer ocs){
		
		Vector queryDefinitions = new Vector();
		for(BasicClass bc : ocs.getObjects("QueryDefinition")){
			if(bc.getID("Scope")==1){
				queryDefinitions.add(bc);	
			}
		}
		getProperty("CurrentQueryDefinition").setSelection(queryDefinitions);
		
		queryDefinitions = new Vector();
		for(BasicClass bc : ocs.getObjects("QueryDefinition")){
			if(bc.getID("Scope")==2){
				queryDefinitions.add(bc);	
			}
		}
		getProperty("CurrentQueryDefinition2").setSelection(queryDefinitions);		
		
		
	}
	public Object handleUpload(ApplicationContext context, FileItem fileItem){
		
		ApplicationServer server = (ApplicationServer)getRoot();
		
		server.logAccess(fileItem);
		
		String filename = fileItem.getName();
		
		Document doc = (Document)get(filename);
		
		if(doc != null){
			
			FileObjectData fileObjectData = (FileObjectData)doc.getObjectByIndex("FileObjectData", 0);
			
			if(fileObjectData == null){
				fileObjectData = new FileObjectData();
				fileObjectData.addProperty("DocumentID", "String", doc.getName());
				fileObjectData.setProperty("FileData", fileItem.get());
				String id = server.insertObject(fileObjectData);
				server.getObject(doc, "FileObjectData", "ID", id);
			}
			else{
				fileObjectData.setProperty("FileData", fileItem.get());				
				server.updateObject(fileObjectData);
			}
			
			return "Document gespeichert";
			
		}
		else{
		
			return "Document nicht gefunden";
			
		}
	}
	
	
	
}