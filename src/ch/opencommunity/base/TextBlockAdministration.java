package ch.opencommunity.base;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Vector;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.kubiki.base.*;
import org.kubiki.base.ActionResult.Status;
import org.kubiki.application.ApplicationContext;

import org.kubiki.gui.html.HTMLFormManager;

import ch.opencommunity.common.OpenCommunityUserSession;
import ch.opencommunity.server.OpenCommunityServer;
import ch.opencommunity.server.HTMLForm;



public class TextBlockAdministration extends BasicOCObject{
	
	Vector types = new Vector();
	
	HashMap<String, TextBlock> textBlockByFunction;
	
	public TextBlockAdministration(){
		
		addObjectCollection("TextBlock", "org.opendossier.dossier.TextBlock");
		
		addContextMenuEntry("addtextblock", "Neuer Textbaustein", true);	
		
		textBlockByFunction = new HashMap<String, TextBlock>();
		

	}
	public String getLabel(){
		return getString("Title");	
	}
	public String toHTML(ApplicationContext context){
		
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
		
		StringBuilder html = new StringBuilder();
		
		html.append(HTMLForm.getToolbar(this, null, getPath(), context, true)); 
		
		html.append("Filtern nach Typ <select onchange=\"onAction('" + getPath() + "','filtertypes','', 'textblockfiltertype=' + this.value)\">");
		
		html.append("<option value=\"\"></option>");
		
		String filterType = (String)userSession.get("textBlockFilterType");	
		
		for(Object o : getTypes()){
			ConfigValue cv = (ConfigValue)o;
			String selected = "";
			if(filterType != null && filterType.equals(cv.getValue())){		
				selected = " SELECTED";	
			}
			html.append("<option value=\"" + cv.getValue() + "\"" + selected + ">" + cv.getLabel() + "</option>");
			
		}
		
		html.append("</select>");
		
		//html.append("<div id=\"tree\">" + HTMLFormManager.getObjectTree(this) + "</div>");
		html.append("<div id=\"tree\" style=\"top : 100px;\">" + getTextBlockList(context) + "</div>");
		//html.append("<div id=\"editArea\"></div>");
		
		
		return html.toString();
		
	}
	public String getTextBlockList(ApplicationContext context){ //allgemeine Funktion ObjectList in Basisklasse implementieren
		
			
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
		
		StringBuilder html = new StringBuilder();
		
		
		html.append("<table>");
		
		html.append("<tr><th class=\"tableheader\">Bezeichnung</th><th class=\"tableheader\">Typ</th></tr>");
		
		
		boolean even = false;
		
		String filterType = (String)userSession.get("textBlockFilterType");		
		
		for(BasicClass textblock : getObjects("TextBlock")){
			
			boolean include = true;
			
			if(filterType != null && filterType.equals(textblock.getString("Type"))==false){
				include = false;		
			}
			

			if(include){
				
			
				if(even){
					html.append("<tr class=\"even\">");
				}
				else{
					html.append("<tr class=\"odd\">");
				}
				even = !even;	
				
				html.append("<td>" + textblock + "</td>");
				html.append("<td>" + textblock.getObject("Type") + "</td>");	
				//html.append("<td><a href=\"javascript:editObject('" + textblock.getPath() + "')\"><img src=\"images/edit_small.png\"></a></td>");
				html.append("<td><a href=\"javascript:onAction('" + getPath() + "','textblockedit','','textblockid=" + textblock.getName() + "')\"><img src=\"images/edit_small.png\"></a></td>");
					
				html.append("</tr>");
				
			}
			
			
		}
		html.append("</table>");		
		
		return html.toString();
		
	}
	public void initObjectLocal(){
		super.initObjectLocal();
		//setName("Textbausteinverwaltung");
		sortTextBlocks();
		getProperty("Owner").setEditable(false); // TODO: Editierbar für Admins
		
		types = new Vector();
		types.add(new ConfigValue("1", "1", "Interne Prozesse"));
		types.add(new ConfigValue("2", "2", "Email"));
		types.add(new ConfigValue("3", "3", "PDF"));
		types.add(new ConfigValue("4", "4", "Textbausteine"));
		
		for (BasicClass bc : getObjects("TextBlock")){
			
			TextBlock textBlock = (TextBlock)bc;
			if(!textBlock.getFunction().isEmpty()){
				textBlockByFunction.put(textBlock.getFunction(), textBlock);
				
			}
			
		}
		
		
	}
	
	public void sortTextBlocks() {
		getObjectCollection("TextBlock").sort("Title");
	}
	
	public ActionResult onAction(BasicClass source, String command, ApplicationContext context) {
		OpenCommunityServer ods = (OpenCommunityServer)getRoot();
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
		ActionResult result = null;
		if (command.equals("addtextblock")) {
			
			result = ods.startProcess("ch.opencommunity.base.TextBlockAdd", userSession, null, context, this);
			/*
			TextBlock textBlock = (TextBlock)createObject("ch.opencommunity.base.TextBlock", null, context);
			textBlock.setProperty("Owner", getID("Owner"));
			textBlock.addProperty("TextBlockAdministrationID", "String", getName());

			String sID = ods.insertObject(textBlock);
			textBlock = (TextBlock)ods.getObject(this, "TextBlock", "ID", sID);

			if (textBlock != null) {
			    result = new ActionResult(Status.OK, "Neuer Textblock angelegt");
			    result.setParam("refresh","tree");
			    result.setParam("edit", textBlock.getPath(""));
			    updateTextBlockReferences();
			}	
			*/
		}
		else if(command.equals("textblockedit")){
			String textblockid = context.getString("textblockid");
			if(textblockid != null){
				TextBlock tb = (TextBlock)getObjectByName("TextBlock", textblockid);
				if(tb != null){
					HashMap params = new HashMap();
					params.put("tb", tb);
					result = ods.startProcess("ch.opencommunity.base.TextBlockAdd", userSession, params, context, this);	
				}
				
			}
			
		}
		else if(command.equals("filtertypes")){
			String textblockfiltertype = context.getString("textblockfiltertype");
			if(textblockfiltertype != null && textblockfiltertype.length() > 0){
				userSession.put("textBlockFilterType", textblockfiltertype);
			}
			else{
				userSession.put("textBlockFilterType", null);
			}
			result = new ActionResult(Status.OK, "Filter angewendet");
			result.setParam("dataContainer", "tree");
			result.setData(getTextBlockList(context));
		}
		else{
			return super.onAction(source, command, context);	
		}
		return result;
	}
	
	@Override
	public boolean onWordClientAction(String action, HttpServletRequest request,
			HttpServletResponse response, OpenCommunityUserSession userSession) throws IOException {
		OpenCommunityServer ods = (OpenCommunityServer)getRoot();		
		boolean isHandled = false;
		if (action.equals("addTextBlock")) {
			PrintWriter writer = response.getWriter();
			
			TextBlock textBlock = new TextBlock();
			textBlock.setParent(this);
			textBlock.setCreationInfo(userSession);
			textBlock.setProperty("Owner", getID("Owner"));
			textBlock.addProperty("TextBlockAdministrationID", "String", getName());
			textBlock.setTitle(request.getParameter("title"));
			
			String sID = ods.insertObject(textBlock);
			textBlock = (TextBlock)ods.getObject(this, "TextBlock", "ID", sID);
			sortTextBlocks();
			updateTextBlockReferences();
			
			String tempID = textBlock.getTempName();
			ods.registerWordClientDocument(tempID, userSession, textBlock.getPath());
			textBlock.addProperty("filename", "String", tempID + ".docx", true, "");
			writer.print(textBlock.toXml());
			isHandled = true;
		}
		if (!isHandled) {
			isHandled = super.onWordClientAction(action, request, response, userSession);
		}

		return isHandled;
	}
	

	public void updateTextBlockReferences() {
	    // Update textblock selection in all template modules
		OpenCommunityServer ods = (OpenCommunityServer)getRoot();
		DocumentTemplateLibrary dtl = ods.getTemplib(getID("Owner"));
		List<BasicClass> documentTemplates = dtl.getObjects("DocumentTemplate");
		for (BasicClass template : documentTemplates) {
			List<BasicClass> modules = template.getObjects("DocumentTemplateModule");
			for (BasicClass module : modules) {
				((DocumentTemplateModule)module).initTextBlocks();
			}
		}
    }
    public Vector getTypes(){
    	return types;	
    }
    public TextBlock getTextBlockByFunction(String function){
    	return textBlockByFunction.get(function);
    }
}