package ch.opencommunity.base;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.kubiki.application.ApplicationContext;
import org.kubiki.base.ActionResult;
import org.kubiki.base.BasicClass;
import ch.opencommunity.common.OpenCommunityUserSession;
import ch.opencommunity.server.OpenCommunityServer;

public class TextBlock extends TemplateElement {
	
	public TextBlock(){
		setTablename("TextBlock");
		addProperty("Type", "Integer", "", false, "Typ");
		addProperty("Subject", "String", "", false, "Betreff", 200);
		addProperty("Function", "String", "", false, "Funktion", 20);
		addProperty("Content", "FormattedText", "", false, "");	
	}
	public void initObjectLocal(){
		super.initObjectLocal();
		getProperty("Type").setSelection(((TextBlockAdministration)getParent()).getTypes());
	}
	
	public ActionResult onAction(BasicClass source, String command, ApplicationContext context) {
		
		ActionResult result = null;
		OpenCommunityServer ods = (OpenCommunityServer)getRoot();
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
		
		if (command.equals("uploadword")) {
			result = ods.startProcess("org.opendossier.process.TemplateUpload", getPath(), true, userSession, null, context, this);
		}
		else {
			return super.onAction(source, command, context);	
		}
		return result;
	}

	@Override
	protected String getDirectoryName() {
		return "textBlocks";
	}
	
	@Override
	public boolean onWordClientAction(String action, HttpServletRequest request, HttpServletResponse response, 
			OpenCommunityUserSession userSession) throws IOException {
		boolean isHandled = false;
		if (action.equals("getTextblock")) {
			sendEditableContentToWord(response, userSession);
			isHandled = true;
		}
		if (!isHandled) {
			isHandled = super.onWordClientAction(action, request, response, userSession);
		}

		return isHandled;
	}
	@Override
	protected String getTempPrefix() {
		return "tb";
	}
	
	@Override
	public void setProperty(String name, Object value) {
		super.setProperty(name, value);
		if (name.equals("Title")) {
			if (getParent() instanceof TextBlockAdministration) {
				((TextBlockAdministration)getParent()).sortTextBlocks();
			}
		}
	}	
	public String getFunction(){
		return getString("Function");	
	}

}