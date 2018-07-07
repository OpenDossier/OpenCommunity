package ch.opencommunity.dossier;

import ch.opencommunity.base.BasicOCObject;
import ch.opencommunity.base.OrganisationalUnit;
import ch.opencommunity.base.Address;

import ch.opencommunity.common.OpenCommunityUserSession;
import ch.opencommunity.server.OpenCommunityServer;

import org.kubiki.base.BasicClass;
import org.kubiki.base.ConfigValue;
import org.kubiki.base.ActionHandler;
import org.kubiki.base.ActionResult;
import org.kubiki.base.Property;
import org.kubiki.base.ObjectCollection;

import org.kubiki.application.ApplicationContext;
import org.kubiki.application.server.WebApplicationContext;
import org.kubiki.application.server.ApplicationServer;
import org.kubiki.application.DataObject;

import java.util.List;
import java.util.Vector;
import java.util.Enumeration;

public class Project extends DataObject{
	
	public Project(){
		
		setTablename("Project");
		
		/*
		addProperty("OrganisationalUnit", "Integer", "");
		
		addObjectCollection("CaseRecord", "ch.opendossier.dossier.CaseRecord");
		*/
		
		addProperty("Description", "Text", "", false, "Beschreibung");
		addProperty("Type", "Integer", "0", false, "Projekttyp");
		addProperty("DateStarted", "Date", "", false, "Startdatum");
		addProperty("DateFinished", "Date", "", false, "Enddatum");
		addObjectCollection("ObjectDetail", "ch.opendossier.dossier.ObjectDetail");
		addObjectCollection("Activity", "ch.opendossier.base.Activity");
		addObjectCollection("Document", "ch.opendossier.base.Document");
		
		addObjectCollection("ObjectStatus", "org.kubiki.application.ObjectStatus");
		
		addObjectCollection("ExternalDocument", "ch.opencommunity.base.ExternalDocument");
		
		ObjectCollection oc = addObjectCollection("MailMessageInstance", "org.kubiki.mail.MailMessageInstance");
		oc.setPreloadObjects(false);
			
	}
	public void initObjectLocal(){
		
		List status = new Vector();
		status.add(new ConfigValue("0","0", "offen"));
		status.add(new ConfigValue("1","1", "abgeschlossen"));		
		status.add(new ConfigValue("2","2", "abgelegt"));	
		getProperty("Status").setSelection(status);
	}
	
	public ActionResult onAction(BasicClass source, String command, ApplicationContext context) {
		
		ActionResult result = null;
		
		if(command.equals("projectfinalize")){
			OpenCommunityServer server = (OpenCommunityServer)getRoot();
			OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
			result = server.startProcess("ch.opencommunity.dossier.ProjectFinalize", userSession, null, context, this);
		}
		
		return result;
		
	}

	
}