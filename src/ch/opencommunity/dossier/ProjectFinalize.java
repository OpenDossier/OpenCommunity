package ch.opencommunity.dossier;

import ch.opencommunity.server.OpenCommunityServer;
import ch.opencommunity.common.OpenCommunityUserSession;
import ch.opencommunity.view.ProjectView;

import org.kubiki.ide.BasicProcess;
import org.kubiki.base.ConfigValue;
import org.kubiki.application.ApplicationContext;
import org.kubiki.application.ObjectStatus;
import org.kubiki.base.ProcessResult;
import org.kubiki.util.DateConverter;


import java.util.Vector;

public class ProjectFinalize extends BasicProcess{
	
	public ProjectFinalize(){
		
		addNode(this);	
		
		addProperty("Context", "Integer", "", false, "Grund");
		addProperty("Comment", "Text", "", false, "Bemerkungen");		
		
		setCurrentNode(this);
		
	}
	public void initProcess(){
		Vector reason = new Vector();	
		reason.add(new ConfigValue("1", "1", "Anwort erhalten"));
		reason.add(new ConfigValue("2", "2", "Keine Anwort erhalten"));
		getProperty("Context").setSelection(reason);
		
	}
	
	public void finish(ProcessResult result, ApplicationContext context){
		
		OpenCommunityServer server = (OpenCommunityServer)getRoot();
		
		Project project = (Project)getParent();
		
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
		
		Dossier dossier = (Dossier)project.getParent();
		DossierController dossierController = (DossierController)userSession.getObjectByName("DossierController", dossier.getName());
		
		ObjectStatus objectStatus  = (ObjectStatus)server.createObject("org.kubiki.application.ObjectStatus", null, context);
		//ObjectStatus objectStatus  = new ObjectStatus();
		objectStatus.addProperty("ProjectID", "String", project.getName());
		objectStatus.setProperty("Comment", getString("Comment"));
		objectStatus.setProperty("Context", "" + getID("Context"));
		objectStatus.setProperty("DateCreated", DateConverter.dateToSQL(new java.util.Date(), true));
		objectStatus.setProperty("Status", 1);
		String id = server.insertSimpleObject(objectStatus);
		server.getObject(project, "ObjectStatus", "ID", id); 
		
		project.setProperty("Status", 1);
		server.updateObject(project);
		if(dossierController != null){
			result.setParam("dataContainer", "projectEditArea");
			StringBuilder html = new StringBuilder();
			ProjectView.toHTML(html, dossierController, project, context);
			result.setData(html.toString());
		}
		
		
	}
	
	
	
	
}