package ch.opencommunity.base;

import org.kubiki.application.ApplicationContext;
import org.kubiki.base.ActionResult;
import org.kubiki.base.BasicClass;
import org.kubiki.base.ConfigValue;

import ch.opencommunity.common.OpenCommunityUserSession;
import ch.opencommunity.base.TemplateElement; 
import ch.opencommunity.server.OpenCommunityServer;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DocumentTemplateModule extends TemplateElement {
	
	enum ModuleType { FreeText, TextBlock };
	
	public DocumentTemplateModule(){
	
		setTablename("DocumentTemplateModule");
		
		addProperty("Type", "Integer", Integer.toString(ModuleType.FreeText.ordinal()), false, "Typ");
		addProperty("IsMandatory", "Boolean", "false", false, "Obligatorisch");
		addProperty("IsMultiple", "Boolean", "false", false, "Mehrfach");
		addProperty("ShowTitle", "Boolean", "true", false, "Überschrift anzeigen");
		addProperty("PageBreak", "Boolean", "false", false, "Neue Seite beginnen");
		addProperty("SortOrder", "Integer", "", false, "Sortierreihenfolge");
		addProperty("TextBlock", "String", "", false, "Textbaustein", 30);	
		addProperty("Content", "FormattedText", "", false, "Inhalt");	
	}

	public void initObjectLocal(){
		super.initObjectLocal();

		Vector<ConfigValue> types = new Vector<ConfigValue>();
		String freeTextKey = Integer.toString(ModuleType.FreeText.ordinal());
		String textBlockKey = Integer.toString(ModuleType.TextBlock.ordinal());
		types.add(new ConfigValue(freeTextKey, freeTextKey, "Freier Text"));
		types.add(new ConfigValue(textBlockKey, textBlockKey, "Textbaustein"));		
		getProperty("Type").setSelection(types, false);
		initTextBlocks();
	}
	
	public void initTextBlocks() {
		OpenCommunityServer ods = (OpenCommunityServer)getRoot();
		BasicClass dtl = getParent("ch.opencommunity.base.DocumentTemplateLibrary");
		getProperty("TextBlock").setSelection(ods.getTextblocks(dtl.getID("Owner")));
	}
	
	public ActionResult onAction(BasicClass source, String command, ApplicationContext context) {
		
		ActionResult result = null;
		OpenCommunityServer ods = (OpenCommunityServer)getRoot();
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
		
		if(command.equals("addtemplate")){
			/*
			DocumentTemplate template = new DocumentTemplate();
			template.setParent(this);

			template.setName("template" + (getObjects("DocumentTemplate").size() + 1));
			addSubobject("DocumentTemplate", template);
			
			result = new ActionResult(ActionResult.Status.OK, "Datensatz erstellt");
			result.setParam("refresh", "tree");			
			result.setParam("edit", template.getPath(""));
			*/
		}
		else if (command.equals("uploadword")) {
			result = ods.startProcess("org.opendossier.process.TemplateUpload", getPath(), true, userSession, null, context, this);
		}
		else {
			return super.onAction(source, command, context);	
		}
		return result;
	}

	@Override
	protected String getDirectoryName() {
		return "documentTemplateModules";
	}

	public String getContent() {
		if (getString("Type").equals("textblock")) {
			TextBlock textBlock = (TextBlock)getObject("TextBlock");
			if (textBlock != null) {
				return textBlock.getString("Content");
			}
		}
		if (getTitle().equals("Verfügungstext")) { //schneller hack für Verfügungen, es braucht eine bessere Lösung
			return getString("DecreeText");
		}
		if (getTitle().equals("Aufforderung")) { //schneller hack für Verfügungen, es braucht eine bessere Lösung
			return getString("DocumentText");
		}
		
		return getString("Content");
	}

	public TemplateElement getContentContainer() {
		if (getID("Type") == ModuleType.TextBlock.ordinal()) {
			if (getObject("TextBlock") instanceof TextBlock) {
				return (TemplateElement)getObject("TextBlock");
			}
		}		
		return this;
	}

	@Override
	public boolean onWordClientAction(String action, HttpServletRequest request, HttpServletResponse response, 
			OpenCommunityUserSession userSession) throws IOException {
		boolean isHandled = false;
		if (action.equals("getModule")) {
			TemplateElement templateElement = getContentContainer();
			templateElement.sendEditableContentToWord(response, userSession);
			isHandled = true;
		}
		else if (action.equals("getModuleInfo")) {
			PrintWriter writer = response.getWriter();
			writer.print(toXml());
			isHandled = true;
		}
		if (!isHandled) {
			isHandled = super.onWordClientAction(action, request, response, userSession);
		}

		return isHandled;
	}

	@Override
	protected String getTempPrefix() {
		return "mod";
	}
	
	@Override
	public void setProperty(String name, Object value) {
		super.setProperty(name, value);
		if (name.equals("SortOrder")) {
			getContainer().sort("SortOrder");
		}
	}

		
}