package ch.opencommunity.base;

import java.io.IOException;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.kubiki.application.ApplicationContext;
import org.kubiki.base.ActionResult;
import org.kubiki.base.BasicClass;
import org.kubiki.base.ConfigValue;


import ch.opencommunity.common.OpenCommunityUserSession;
import ch.opencommunity.server.OpenCommunityServer;

public class DocumentHeaderFooter extends TemplateElement {
	
	public enum HeaderFooterType { HEADER, FOOTER }

	public DocumentHeaderFooter () {
		setTablename("DocumentHeaderFooter");
	
		addProperty("Type", "Integer", Integer.toString(HeaderFooterType.HEADER.ordinal()), false, "Typ");
		addProperty("Content", "FormattedText", "", false, "Inhalt");	
	}
	
	public void initObjectLocal(){
		super.initObjectLocal();

		Vector<ConfigValue> types = new Vector<ConfigValue>();
		String headerKey = Integer.toString(HeaderFooterType.HEADER.ordinal());
		String footerKey = Integer.toString(HeaderFooterType.FOOTER.ordinal());
		types.add(new ConfigValue(headerKey, headerKey, "Kopfzeilen"));
		types.add(new ConfigValue(footerKey, footerKey, "Fusszeilen"));
		getProperty("Type").setSelection(types, false);
	}	
	
	public ActionResult onAction(BasicClass source, String command, ApplicationContext context) {
		
		ActionResult result = null;
		OpenCommunityServer ods = (OpenCommunityServer)getRoot();
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
		
		if (command.equals("uploadword")) {
			result = ods.startProcess("org.opendossier.process.HeaderFooterUpload", getPath(), true, userSession, null, context, this);
		}
		else {
			return super.onAction(source, command, context);	
		}
		return result;
	}
	
	public HeaderFooterType getType() {
		int type = getID("Type");
		return HeaderFooterType.values()[type];
	}

	@Override
	protected String getDirectoryName() {
		if (getType() == HeaderFooterType.HEADER) {
			return "headers";
		}
		else {
			return "footers";
		}
	}
	
	@Override
	public boolean onWordClientAction(String action, HttpServletRequest request, HttpServletResponse response, 
			OpenCommunityUserSession userSession) throws IOException {
		boolean isHandled = false;
		if (action.equals("getHeaderFooter")) {
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
		return "hf";
	}	
	
	@Override
	public void setProperty(String name, Object value) {
		super.setProperty(name, value);
		if (name.equals("Title") || name.equals("Type")) {
			if (getParent() instanceof DocumentTemplateLibrary) {
				((DocumentTemplateLibrary)getParent()).sortHeaderFooter();
			}
		}
	}
	
}
