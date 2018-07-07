package ch.opencommunity.base;

import ch.opencommunity.server.OpenCommunityServer;
import ch.opencommunity.common.OpenCommunityUserSession;


import org.kubiki.base.BasicClass;
import org.kubiki.base.ObjectCollection;
import org.kubiki.base.ActionResult;
import org.kubiki.application.ApplicationContext;
import org.kubiki.application.server.WebApplicationModule;
import org.kubiki.gui.html.HTMLFormManager;




public class ObjectTemplateAdministration extends BasicOCObject{ 
	
	OpenCommunityServer server = null;

	public ObjectTemplateAdministration(){
		setTablename("ObjectTemplateAdministration");
		
		addObjectCollection("ObjectTemplate", "ch.opencommunity.base.ObjectTemplate");
		
		addContextMenuEntry("objecttemplateadd");
	}
	public void initObjectLocal(){
		server = (OpenCommunityServer)getRoot();
	}
	public ActionResult onAction(BasicClass source, String command, ApplicationContext context){
			
		ActionResult result = null;

		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
			
			
			
		if(command.equals("objecttemplateadd")){

			result = server.startProcess("ch.opencommunity.process.ObjectTemplateAdd", userSession, null, context, this);
		}
		return result;
			
	}
	public String getMainForm(ApplicationContext context){
		
		HTMLFormManager formManager = server.getFormManager();
			
		StringBuilder html = new StringBuilder();
		html.append(formManager.getToolbar(this, null, null, context, true));
			
		html.append("<div id=\"tree\">");
			
		html.append(formManager.getObjectTree(server, "ObjectTemplateAdministration"));
			
		html.append("</div>");
		
		html.append("<div id=\"objectEditArea\">");
		
		
		html.append("</div>");
			
		return html.toString();			
		
	}
	
} 
