package ch.opencommunity.server;

import ch.opencommunity.common.*;
import ch.opencommunity.view.*;

import org.kubiki.application.ApplicationContext;
import org.kubiki.application.server.WebApplicationContext;
import org.kubiki.base.*;
import org.kubiki.database.Record;
import org.kubiki.ide.*;
import org.kubiki.util.DateConverter;

import org.kubiki.servlet.*;
import org.kubiki.cms.WebForm;
import org.kubiki.cms.WebPageElement;
/*
import org.opendossier.common.FieldDefinition;
import org.opendossier.common.UserSession;
import org.opendossier.dossier.BasicDossierObject;
import org.opendossier.dossier.BasicDossierRecord;
import org.opendossier.dossier.CaseRecord;
import org.opendossier.dossier.Dossier;
import org.opendossier.dossier.Field;
import org.opendossier.dossier.MeasureService;
import org.opendossier.dossier.OrganisationalUnit;

import org.opendossier.gui.ObjectViewTab;
import org.opendossier.gui.Tab;
import org.opendossier.gui.Tree;
import org.opendossier.view.CustomObjectView;
import org.opendossier.view.ObjectView;
import org.opendossier.view.ObjectViewDefinitions;
*/

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;


public class HTMLForm {
	public static final String LIST_WIDGET_SEPARATOR = "#";
	
	public enum SortType { NONE, CLIENT, SERVER };
	
	Map<String, String> fieldRendererMapping;
	Map<String, ObjectViewDefinitions> objectViewDefinitionsMap;
	private WebApplication webapp;
	
	public HTMLForm(WebApplication webapp, Map<String, String> fieldRendererMapping, Map<String, ObjectViewDefinitions> objectViewDefinitionsMap){
		this.webapp = webapp;
		this.fieldRendererMapping = fieldRendererMapping;
		this.objectViewDefinitionsMap = objectViewDefinitionsMap;
			
	}
	
	public String getEditForm(BasicClass o, ApplicationContext context){
		return getEditForm(o, true, null, null, context);
	} 
	public String getEditForm(BasicClass o, boolean isEditable, String returnTo, String viewName, ApplicationContext context) {
		StringBuilder html = new StringBuilder();

		html.append(getObjectTabbar(o, viewName, returnTo));
		
		html.append(getToolbar(o, null, returnTo, context, isEditable));
		
		html.append(getPathLinks(o, false, context.getString("returnTo")));
		/*
		if (o instanceof BasicDossierRecord) {
			BasicDossierRecord bdr = (BasicDossierRecord)o;
			html.append("<div><b>" + bdr.getTablename() + " \"" + bdr.getTitle() + "\" ");
			html.append(isEditable ? "bearbeiten" : "betrachten");
			html.append("</b></div>");
		}
		*/
		
		html.append("<div>\n<form action=\"main\" id=\"objectEditForm_" + o.getPath("") + "\">");
		
		if(o instanceof ActionHandler){
			html.append("\n<input type=\"hidden\" name=\"action\" value=\"getObjectAction\">");	
			html.append("\n<input type=\"hidden\" name=\"command\" value=\"saveobject\">");			
		}
		else{		
			html.append("\n<input type=\"hidden\" name=\"action\" value=\"saveObject\">");			
		}
		
		html.append("\n<input type=\"hidden\" name=\"objectPath\" value=\"" + o.getPath("") + "\">");
		
		html.append("<table>");
		
		getFormBody(html, o, isEditable, context);
		
		html.append("<tr><td>");
		if (isEditable) {
//			html.append(getSaveButton(o, returnTo, isEditable));
		}
		html.append("</td></tr>");
		
		html.append("</table></form><div>");
		return html.toString();	
	}

	public String getEditForm(BasicClass o, boolean isEditable, String returnTo, String viewName, ApplicationContext context, BasicClass reference) { //AK 20131009
		StringBuilder html = new StringBuilder();

		html.append(getObjectTabbar(reference, viewName, returnTo));
		
		html.append(getToolbar(reference, null, returnTo, context, isEditable));
		
		html.append(getPathLinks(reference, false, context.getString("returnTo")));
		/*
		if (o instanceof BasicDossierRecord) {
			BasicDossierRecord bdr = (BasicDossierRecord)o;
			html.append("<div><b>" + bdr.getTablename() + " \"" + bdr.getTitle() + "\" ");
			html.append(isEditable ? "bearbeiten" : "betrachten");
			html.append("</b></div>");
		}
		*/
		html.append("<div>\n<form action=\"main\" id=\"objectEditForm_" + o.getPath("") + "\">");
		
		if(o instanceof ActionHandler){
			html.append("\n<input type=\"hidden\" name=\"action\" value=\"getObjectAction\">");	
			html.append("\n<input type=\"hidden\" name=\"command\" value=\"saveobject\">");			
		}
		else{		
			html.append("\n<input type=\"hidden\" name=\"action\" value=\"saveObject\">");			
		}
		
		//html.append("\n<input type=\"hidden\" name=\"objectpath\" value=\"" + o.getPath("") + "\">");
		html.append("\n<input type=\"hidden\" name=\"objectPath\" value=\"" + o.getPath("") + "\">");
		
		html.append("<table>");
		
		getFormBody(html, o, isEditable, context);
		
		html.append("<tr><td>");
		if (isEditable) {
//			html.append(getSaveButton(o, returnTo, isEditable));
		}
		html.append("</td></tr>");
		
		html.append("</table></form><div>");
		return html.toString();	
	}
	
	public String getNodeForm(BasicProcessNode node, ApplicationContext context){
		
		String buttonLabel1 = node.getNextButtonLabel();
		String buttonLabel2 = "Zur&uuml;ck";
		
		BasicProcess process = node.getProcess();
		if(process.isLast(node)){
			buttonLabel1 = process.getLastButtonLabel();	
		}
		
		StringBuilder html = new StringBuilder();
		html.append("<div><form action=\"servlet\" id=\"processNodeForm\">");
		//html.append("<input type=\"hidden\" name=\"action\" value=\"nextnode\">");

		int numCols = 1;
		if (node.hasProperty("numCols")) {
			numCols = node.getInt("numCols");
		}
		
		html.append("<table>");
		if(node.getComment().length() > 0){
			html.append("<tr><td class=\"comment\" colspan=\"" + numCols * 2 + "\">" + node.getComment() + "</td></tr>");
		}
		
		if(process.getProperty("errors") != null && process.getObject("errors") instanceof HashMap){
			getFormBody(html, node, true, context, "", numCols, (HashMap)process.getObject("errors"));
		}
		else{
		
			getFormBody(html, node, true, context, "", numCols);
			
		}
		
		html.append("<tr><td colspan=\"" + numCols * 2 + "\">");

		if(!process.isFirst(node)){
		      html.append("<input type=\"button\" value=\"" + buttonLabel2 + "\" onClick=\"getPreviousNode()\">");
		}

		// go through right-aligned buttons in reverse order (right to left) 
		


		if(process.showDefaultButtons){
			html.append("<input class=\"rightButton\" type=\"button\" value=\"" + buttonLabel1 + "\" onClick=\"getNextNode()\">");
		}
		if(process.showDefaultButtons){
			html.append("<input class=\"rightButton\" type=\"button\" value=\"Abbrechen\" onClick=\"cancelProcess()\">");
		}
		Vector<BasicClass> buttons = node.getObjects("ButtonDefinition");
		for (int i = buttons.size() - 1; i >= 0; i--) {
			ButtonDefinition button = (ButtonDefinition)buttons.elementAt(i);
			html.append("<input class=\"rightButton\" type=\"button\" value=\"" + button.getString("label") + "\" onClick=\"" + button.getString("action") + "\">");
		}

		html.append("</td></tr>");
		
		html.append("</table></form><div>");
		return html.toString();	
	}
	public void getFormBody(StringBuilder html, BasicClass o, boolean isEditable, ApplicationContext context){
		getFormBody(html, o, isEditable, context, "", 1);
	}
	public void getFormBody(StringBuilder html, BasicClass o, boolean isEditable, ApplicationContext context, String prefix){
		getFormBody(html, o, isEditable, context, prefix, 1);
	}
	public void getFormBody(StringBuilder html, BasicClass o, boolean isEditable, ApplicationContext context, String prefix, int numCols){
		getFormBody(html, o, isEditable, context, prefix, numCols, null);		
	}		
	public void getFormBody(StringBuilder html, BasicClass o, boolean isEditable, ApplicationContext context, String prefix, int numCols, HashMap errors){
		int currentColumn = 1;
		
		List<String> fields = o.getPropertySheet().getNames();
		
		for(String fieldname : fields){
			Property p = o.getProperty(fieldname);
			if(!p.isHidden()){
				String label = p.getLabel();
				if(label.length()==0){
					label = fieldname;	
				}
				String labelColumn = "<td class=\"labelColumn\">" + label + "</td>";
				String columnStart = getColumnStart(currentColumn);
				String columnEnd = getColumnEnd(currentColumn, numCols);
				
				String error = "";
				
				if(errors != null){
					if(errors.get(p) != null){
						error = "<span class=\"errorfield\">" + errors.get(p) + "</span><br>";
					}
				}
				
				currentColumn++;
				if (currentColumn > numCols) {
					currentColumn = 1;
				}
				if(fieldRendererMapping != null && fieldRendererMapping.get(p.getName()) != null){
					try{
						Class<?> c = Class.forName(fieldRendererMapping.get(p.getName()));
						HtmlFieldWidget widget = (HtmlFieldWidget)c.newInstance();
						html.append(widget.renderWidget(o, p, isEditable, context, prefix));
					}
					catch(Exception e){
						html.append("...");
						webapp.logException(e);	
					}					
				}
				else if(p.getType().equals("List") || p.getType().equals("ListFillIn")){
					html.append(columnStart + labelColumn + "<td>" + error + getListField(p, isEditable) + "</td>" + columnEnd);
				}
/*				else if(p.getType().equals("TreeList")){
					html.append("<tr><td>" + label + "</td><td>" + getTreeListField(p, isEditable) + "</td></tr>");
				} */
				else if(p.getType().equals("SubobjectList")){
					List<BasicClass> subobjects = o.getObjects(p.getValue());
					String noDataText = "Keine " + label + " vorhanden";
					html.append(columnStart + "<td colspan=\"2\"><div><b>" + label + "</b></div>" + getSubobjectList(subobjects, isEditable && p.isEditable(), noDataText) + "</td>" + columnEnd);
				}
				else if(p.getValues() != null){
					html.append(columnStart + labelColumn + "<td>" + error + getSelection(p, isEditable, prefix) + "</td>" + columnEnd);
				}
				else if(p.getType().equals("Boolean")){
					html.append(columnStart + labelColumn + "<td>" + error + getRadioButton(p, isEditable, prefix) + "</td>" + columnEnd);
				}
				else if(p.getType().equals("Text")){
					
					if(o instanceof WebPageElement && p.getName().equals("Content") && o.getID("Type") == 1){ //Spezialfall CMS
						if (isEditable && p.isEditable()) {
							html.append(columnStart + labelColumn + "</tr><tr><td colspan=\"2\">" + error  +  getHTMLEditor(p) + "</td>" + columnEnd);
						}
						else {
							html.append(columnStart + labelColumn + "</tr><tr><td colspan=\"2\">" + error + getHTMLStatic(p) + "</td>" + columnEnd);
						}
						
					}
					else{
						html.append(columnStart + labelColumn + "<td>"  + error + getTextArea(p, isEditable) + "</td>" + columnEnd);
					}
				}
				else if(p.getType().equals("Code")){
					html.append(columnStart + labelColumn + "<td>" + error + getTextArea(p, isEditable, "codeArea") + "</td>" + columnEnd);
				}
				else if(p.getType().equals("LargeText")){
					html.append(columnStart + labelColumn + "<td>" + error + getLargeTextArea(p, isEditable) + "</td>" + columnEnd);
				}
				else if(p.getType().equals("FormattedText")){
					if (isEditable && p.isEditable()) {
						html.append(columnStart + labelColumn + "</tr><tr><td colspan=\"2\">" + error + getHTMLEditor(p) + "</td>" + columnEnd);
					}
					else {
						html.append(columnStart + labelColumn + "</tr><tr><td colspan=\"2\">" + error + getHTMLStatic(p) + "</td>" + columnEnd);
					}
				}
				else if(p.getType().equals("Date")){
					html.append(columnStart + labelColumn + "<td>" + error + getDateField(p, isEditable, prefix) + "</td>" + columnEnd);
				}
				else if(p.getType().equals("DateTime")){
					html.append(columnStart + labelColumn + "<td>" + error + getDateTimeField(p, isEditable, prefix) + "</td>" + columnEnd);
				}
				else if(p.getType().equals("Integer")){
					html.append(columnStart + labelColumn + "<td>" + error + getIntegerField(p, isEditable) + "</td>" + columnEnd);
				}
				else if(p.getType().equals("Double") || p.getType().equals("Float")){
					html.append(columnStart + labelColumn + "<td>" + error + getDoubleField(p, isEditable) + "</td>" + columnEnd);
				}
				else if(p.getType().equals("Password")){
					html.append(columnStart + labelColumn + "<td>" + error + getPassWordField(p, isEditable) + "</td>" + columnEnd);
				}
				else if(p.getType().equals("Hyperlink")){
					html.append(columnStart + labelColumn + "<td>" + error + getHyperlinkField(p) + "</td>" + columnEnd);
				}
				else if(p.getType().equals("WebForm")){
					if(p.getObject() instanceof WebForm){
						html.append(((WebForm)p.getObject()).toHTML(context));
					}
				}
				else if(p.getType().equals("FileUpload")){
					html.append("<tr><td class=\"label\">" + label + "</td>");
					html.append("<td>" + getFileUpload(o, p) + "</td></tr>");
				}
				else{
					html.append(columnStart + labelColumn + "<td>" + error + getTextField(p, isEditable, prefix) + "</td>" + columnEnd);
				}
			}

		}
		
	}
	
	private String getColumnStart(int currentColumn) {
		if (currentColumn == 1) {
			return "<tr>";
		}
		else {
			return "";
		}
	}
	
	private String getColumnEnd(int currentColumn, int colCount) {
		if (currentColumn == colCount) {
			return "</tr>";
		}
		else {
			return "";                                                                    
		}
	}
	
	public static String getFileUpload(BasicClass owner, Property p){
		
		String id = createId(p);
		
		StringBuilder html = new StringBuilder();
		

		html.append("<input type=\"file\" id=\"" + id + "\" name=\"file\"/>");
		html.append("<input type=\"button\" id=\"upload-button\" value=\"hochladen\" onclick=\"uploadFile(event,'" + id + "','" + owner.getPath() + "')\">");
		
		return html.toString();
		
	}

	public static String getTextField(Property p, boolean isEditable, String prefix){
		return getTextField(p, isEditable, prefix, null);
	}
	public static String getTextField(Property p, boolean isEditable, String prefix, String classid){
		return getTextField(p, isEditable, prefix, classid, null);
	}
	public static String getTextField(Property p, boolean isEditable, String prefix, String classid, String style){
		return getTextField(p, isEditable, prefix, classid, style, null);			
	}
		
	public static String getTextField(Property p, boolean isEditable, String prefix, String classid, String style, String onchange){
		if(isEditable && p.isEditable()) {
			String id = createId(p);
			String maxLength = "";
			int length = p.getLength();
			if (length > 0) {
				maxLength = " maxLength=\"" + length + "\"";
			}
			String size = "";
			if (length > 100) {
				size = " size=\"" + length / 4 + "\"";
			}
			String action = "";
			if (!p.getAction().isEmpty()) {
				action = " onchange=\"" + p.getAction() + "\"";
			}
			else if (onchange != null) {
				action = " onkeyup=\"" + onchange + "\"";
			}
			String classdef = "";
			if(classid != null){
				classdef = "class=\"" + classid + "\"";
			}
			String styledef = "";
			if(styledef != null){
				styledef = " style=\"" + style + "\"";
			}
			//prefix + p.getName() + 
			return "<input " + classdef + " id=\"" + id + "\" name=\"" + prefix + p.getName() + "\" value=\"" + getHtmlValue(p) + "\"" + maxLength + size + action + "" + styledef + ">";
		}
		else{
			return 	getHtmlValue(p);
		}
	}

	public static String getTextArea(Property p, boolean isEditable) {
		return getTextArea(p, isEditable, null, null);
	}
	
	public static String getTextArea(Property p, boolean isEditable, String classname) {
		return getTextArea(p, isEditable, classname, null);
	}
	
	public static String getTextArea(Property p, boolean isEditable, String className, String prefix){
		if(isEditable && p.isEditable()) {
			String id = createId(p);
			StringBuilder html = new StringBuilder();
			html.append("<textarea");
			if (className != null) {
				html.append(" class=\"" + className + "\"");
			}
			if(prefix != null){
				html.append(" id=\"" + id + "\" name=\"" + prefix + p.getName() + "\">" + getHtmlValue(p) + "</textarea>");				
			}
			else{
				html.append(" id=\"" + id + "\" name=\"" + p.getName() + "\">" + getHtmlValue(p) + "</textarea>");
			}
			return html.toString();
		}
		else {
			return 	getHtmlValue(p);
		}
	}
	
	public static String getLargeTextArea(Property p, boolean isEditable){
		if(isEditable && p.isEditable()) {
			String id = createId(p);
			StringBuilder html = new StringBuilder();
			html.append("<div class=\"largeTextareaContainerClosed\" onclick=\"enlargeTextarea('" + id + "')\">");
			html.append("<textarea class=\"largeTextareaClosed\" id=\"" + id + "\" name=\"" + p.getName() + "\">" + getHtmlValue(p) + "</textarea>");
			html.append("<div onclick=\"reduceTextarea(event, '" + id + "')\" class=\"largeTextareaCloseButton\">Schliessen</div>");
			html.append("<div class=\"largeTextareaMarker\">...</div>");
			html.append("</div>");
			return html.toString();
		}
		else {
			String text = getHtmlValue(p);
			if (text.length() > 30) {
				text = text.substring(0, 30) + "...";
			}
			int newLine = text.indexOf("\n");
			if (newLine != -1) {
				text = text.substring(0, newLine) + "...";
			}
			return text;
		}
	}
	public static String getPassWordField(Property p, boolean isEditable){
		return getPassWordField(p,isEditable, false);
	}
	public static String getPassWordField(Property p, boolean isEditable, boolean showWarning){
		if(isEditable && p.isEditable()) {
			String id = createId(p);
			if(showWarning){
				return "<input type=\"password\" id=\"" + id + "\" name=\"" + p.getName() + "\" value=\"" + getHtmlValue(p) + "\" onfocus=\"showPasswordWarning()\">";
			}
			else{
				return "<input type=\"password\" id=\"" + id + "\" name=\"" + p.getName() + "\" value=\"" + getHtmlValue(p) + "\">";
			}
		}
		else{
			return 	"********";
		}
	}
	
	public static String getHyperlinkField(Property p){
		return "<a href=\"" + p.getValue()+ "\" target=\"_blank\">Öffnen</a>";
	}

	public static String getDateField(Property p, boolean isEditable, String prefix) {
		String displayDate = DateConverter.dateToShortDisplay(p.getDate(), false);
		if(isEditable && p.isEditable()) {
			String sqlDate = DateConverter.dateToSQL(p.getDate(), false);
			StringBuffer html = new StringBuffer();
			String id = createId(p);
			html.append("<input type=\"hidden\" id=\"" + id + "\" name=\"" + prefix + p.getName() + "\" value=\"" + sqlDate + "\">");
			html.append("<input id=\"editDate_" + id + "\" name=\"editDate_" +  prefix + p.getName() + "\" onblur=\"checkDate(this.value, '" + id + "')\" value=\"" + displayDate + "\">");
			return html.toString();
		}
		else {
			return displayDate;
		}
	}
	
	public static String getDateTimeField(Property p, boolean isEditable, String prefix) {
		String displayDate = DateConverter.dateToShortDisplay(p.getDate(), true);
		if(isEditable && p.isEditable()) {
			String sqlDate = DateConverter.dateToSQL(p.getDate(), true);
			StringBuffer html = new StringBuffer();
			String id = createId(p);
			html.append("<input type=\"hidden\" id=\"" + id + "\" name=\"" +  prefix + p.getName() + "\" value=\"" + sqlDate + "\">");
			html.append("<input id=\"editDate_" + id + "\" name=\"editDate_" +  prefix + p.getName() + "\" onblur=\"checkDateTime(this.value, '" + id + "')\" value=\"" + displayDate + "\">");
			return html.toString();
		}
		else {
			return displayDate;
		}
	}
	
	public static String getIntegerField(Property p, boolean isEditable) {
		if(isEditable && p.isEditable()) {
			StringBuffer html = new StringBuffer();
			String id = createId(p);
			html.append("<input id=\"" + id + "\" name=\"" + p.getName() + "\" onblur=\"checkInteger(this.value, '" + id + "')\" value=\"" + p.getValue() + "\">");
			return html.toString();
		}
		else {
			return p.getValue();
		}
	}

	public static String getDoubleField(Property p, boolean isEditable) {
		if(isEditable && p.isEditable()) {
			StringBuffer html = new StringBuffer();
			String id = createId(p);
			html.append("<input id=\"" + id + "\" name=\"" + p.getName() + "\" onblur=\"checkDouble(this.value, '" + id + "')\" value=\"" + p.getValue() + "\">");
			return html.toString();
		}
		else {
			return p.getValue();
		}
	}
	public static String getSelection(Property p, boolean isEditable, String prefix){
		return getSelection(p, createId(p), null, isEditable, prefix);
	}
	
	public static String getSelection(Property p, String id, String className, boolean isEditable, String prefix){
		return getSelection(p, id, className, isEditable, true, false, prefix, null);
	}
	public static String getSelection(Property p, String id, String className, boolean isEditable, boolean selectCurrent, boolean onlyActive, String prefix, String onchange){
		StringBuilder html = new StringBuilder();
		Object value = p.getValue();
		if (isEditable && p.isEditable()) {
			html.append("<select id=\"" + id + "\" name=\"" + prefix + p.getName() + "\"");
			if (className != null) {
				html.append(" class=\"" + className + "\"");	
			}
			html.append(" onkeypress=\"selectboxSearchItem(this, event)\" onblur=\"selectboxReset('" + id + "')\"");
			/*
			if (onchange == null && p.getOwner() instanceof Field) {
				Field field = (Field)p.getOwner();
				FieldDefinition fd = field.getTemplate();
				if (fd != null) {
					String jsFunction = fd.getString("JSFunction");
					if (jsFunction.length() > 0) {
						onchange = jsFunction + "(\'" + id + "\',this.value)";
					}
				}
			}
			*/
			if (onchange != null) {
				html.append(" onchange=\"" + onchange + "\"");	
			}
			if(p.getAction().length() > 0){
				html.append(" onchange=\"" + p.getAction() + "\"");	
			}
			if(p.getName().equals("Organisation")){  //Todo: Individuelle Styles für Properties
				html.append(" style=\"width: 300px;\">");
			}
			else{
				html.append(">");
			}
	
			for (int j = 0; j < p.getValues().size(); j++) {
				BasicInterface bi = p.getValues().elementAt(j);
				
				if (onlyActive) {
					// Filter inactive elements
					if (bi instanceof BasicClass && ((BasicClass)bi).hasProperty("Status")) {
						if (((BasicClass)bi).getID("Status") != 0) {
							continue;
						}
					}
				}
				
				html.append("\n<option value=\"" + bi.getValue() +	"\"");
				if (selectCurrent) {
					if (value instanceof BasicClass) {
						if (bi.getValue().toString().equals(((BasicInterface)value).getValue().toString())) {
							html.append(" SELECTED");
						}
					}
					else {
						if (bi.getValue().toString().equals(value.toString()) || bi.toString().equals(value.toString())) {
							html.append(" SELECTED");
						}							
					}
				}
				if(p.disableInactive()){ // AK 20131018
					if (bi instanceof ConfigValue){
						ConfigValue configValue = (ConfigValue)bi;
						if(configValue.isDisabled()){
							html.append(" disabled");
						}
					}
					//else if (bi instanceof BasicDossierObject) {
						if(((BasicClass)bi).getID("Status")==1){
							//html.append(" disabled"); //AK 20160919, allgemeinere Lösung finden
						}
					//}
				}
				
				String label = bi.getLabel();
				String[] args = label.split("/");
				
				html.append(">" + args[0] + "</option>");
			
			}
			html.append("\n</select>");
			html.append("<div id=\"" + id + "_out\" style=\"display:none\"></div>");
		}
		else {
			html.append(value);
		}
		return html.toString();
	}	

	public static String getRadioButton(Property p, boolean isEditable, String prefix){
		StringBuilder html = new StringBuilder();
		html.append("<input type=\"radio\" name=\"" + prefix + p.getName() + "\" value=\"true\"");
		if(p.getBoolean())	{
			html.append(" CHECKED");
		}
		if (!isEditable || !p.isEditable()) {
			html.append(" DISABLED");	
		}
		if (!p.getAction().isEmpty()) {
			html.append(" onchange=\"" + p.getAction() + "\"");
		}
		html.append("> Ja");
		html.append("<input type=\"radio\" name=\"" + prefix + p.getName() + "\" value=\"false\"");
		if(!p.getBoolean())	{
			html.append(" CHECKED");
		}
		if (!isEditable || !p.isEditable()) {
			html.append(" DISABLED");	
		}
		if (!p.getAction().isEmpty()) {
			html.append(" onchange=\"" + p.getAction() + "\"");
		}
		html.append("> Nein");
		return html.toString();
	}
	public static String getRadioButtonGroup(Property p, boolean isEditable, String prefix){
		StringBuilder html = new StringBuilder();
		if(p.getValues() != null){
			for(BasicInterface bi : p.getValues()){
				if(bi.getLabel().length() > 0){
					html.append("<input type=\"radio\" name=\"" + prefix + p.getName() + "\" value=\"" + bi.getValue() + "\"");
					Object value = p.getObject();
					if(value instanceof BasicInterface){
						if(((BasicInterface)value).getValue().equals(bi.getValue()))	{
							html.append(" CHECKED");
						}						
					}
					else{
						if(p.getObject().toString().equals(bi.getValue()))	{
							html.append(" CHECKED");
						}
					}
					/*
					if (!isEditable || !p.isEditable()) {
						html.append(" DISABLED");	
					}
					if (!p.getAction().isEmpty()) {
						html.append(" onchange=\"" + p.getAction() + "\"");
					}
					*/
					html.append(">" + bi.getLabel());

				}
			}
		}
		
		return html.toString();
	}
	public static String createId(Property p) {
		String id = p.getName() + p.getOwner().getPath("");
		id = id.replaceAll("[/:]", "");
		return id;
	}
	
	public void insertSelection(StringBuilder html, Property p, boolean isEditable){
		
		Object value = p.getValue();
		
		html.append("<td><select id=\"" + p.getName() + "\" name=\"" + p.getName() + "\"");
		if(isEditable){
			html.append(">");
		}
		else{
			html.append(" DISABLED>");	
		}
		for(int j = 0; j < p.getValues().size(); j++){
			BasicInterface bi = (BasicInterface)p.getValues().elementAt(j);
			html.append("\n<option value=\"" + bi.getValue() +	"\"");
			
			
			if(value instanceof BasicClass){
//				System.out.println(bi.getValue().toString() + ":" + ((BasicInterface)value).getValue());
							
				if(bi.getValue().toString().equals(((BasicInterface)value).getValue().toString())){
					html.append(" SELECTED");
				}
			}
			else{
							
//				System.out.println("... " + bi.toString() + ":" + value.toString());
							
				if(bi.toString().equals(value.toString())){
					html.append(" SELECTED");
				}							
							
			}
			html.append(">" + bi.getLabel() + "</option>");
					
		}
		html.append("\n</select>");
	}

	
	public static String getListField(Property p, boolean isEditable) {
		
		return getListField(p, isEditable, "");
	}
	public static String getListField(Property p, boolean isEditable, String prefix) {
//		OpenDossierServer ods = (OpenDossierServer)p.getOwner().getRoot();
		StringBuffer html = new StringBuffer();
		BasicClass bc = p.getOwner();
		ObjectCollection listCollection = null;
		if (p.getName().endsWith("List")) {
			// A property ending with "List" refrences a collection of subobjects with the same name (without the ending)
			String collectionName = p.getName().substring(0, p.getName().length() - 4);
			listCollection = bc.getObjectCollection(collectionName);
		}
		String id =  bc.getPath() + p.getName();
		String widgetId = "widget_" + id;
		String selectionId = "selection_" + id;
		getHiddenWidgetField(html, p, id, prefix);
		html.append("<table style=\"border-collapse: collapse\">");
		
		if (isEditable && p.isEditable()) { 
			html.append("<tr><td style=\"vertical-align: top;\">");
			String selectionAction;
			if (listCollection == null) {
				selectionAction = "widgetAddFromSelection('" + widgetId + "', '" + selectionId + "')";
			}
			else {
				selectionAction = "widgetAddFromSelectionAndSave('" + widgetId + "', '" + selectionId + "', '" + bc.getPath() + "')";
			}
//			html.append("<img class=\"widgetButton\" src=\"images/left_small.png\" onclick=\"" + action + "\">");
			html.append(getSelection(p, selectionId, null, true, false, true, "selection_", selectionAction));
			
//			String[] quickSelection = null;
			// TODO konfigurierbar
//			if (p.getName().equals("Participants")) {
//				quickSelection = new String[] { "SL", "LE", "EL" };
//			}
		
			if (p.getType().equals("ListFillIn")) {
				String fillInAction;
				if (listCollection == null) {
					fillInAction = "widgetFillIn('" + widgetId + "', this)";
				}
				else {
					fillInAction = "widgetFillInAndSave('" + widgetId + "', this, '" + bc.getPath() + "')";
				}
				html.append("<br><input class=\"widgetFillInField\" id=\"fillInField_" + widgetId + "\" onchange=\"" + fillInAction + "\"></input>");
			}
/*			if (quickSelection != null && quickSelection.length > 0) {
				html.append("<br>");
				for (String qsItem : quickSelection) {
					html.append("<input type=\"button\" value=\"" + qsItem + "\" onclick=\"widgetAdd('" + widgetId + "', '" + qsItem + "', '" + qsItem + "')\">");
				}
			} */
			html.append("</td></tr>");
		}
		
		
		html.append("<tr><td style=\"vertical-align: top; padding: 0;\">");
		if (listCollection == null) {
			getListWidget(html, p, isEditable, widgetId);
		}
		else {
			getAutosaveListWidget(html, p.getLabel(), listCollection, isEditable && p.isEditable(), widgetId);
		}
		html.append("</td>");

		html.append("</tr></table>");
		
		return html.toString();
//		return "<textarea id=\"" + p.getName() + "\" name=\"" + p.getName() + "\">" + p.getValue() + "</textarea>";
	}
	public static String getListField_back(Property p, boolean isEditable, String prefix) {
//		OpenDossierServer ods = (OpenDossierServer)p.getOwner().getRoot();
		StringBuffer html = new StringBuffer();
		BasicClass bc = p.getOwner();
		ObjectCollection listCollection = null;
		if (p.getName().endsWith("List")) {
			// A property ending with "List" refrences a collection of subobjects with the same name (without the ending)
			String collectionName = p.getName().substring(0, p.getName().length() - 4);
			listCollection = bc.getObjectCollection(collectionName);
		}
		String id =  bc.getPath() + p.getName();
		String widgetId = "widget_" + id;
		String selectionId = "selection_" + id;
		getHiddenWidgetField(html, p, id, prefix);
		html.append("<table style=\"border-collapse: collapse\"><tr><td style=\"vertical-align: top; padding: 0;\">");
		if (listCollection == null) {
			getListWidget(html, p, isEditable, widgetId);
		}
		else {
			getAutosaveListWidget(html, p.getLabel(), listCollection, isEditable && p.isEditable(), widgetId);
		}
		html.append("</td>");
		if (isEditable && p.isEditable()) { 
			html.append("<td style=\"vertical-align: top;\">");
			String selectionAction;
			if (listCollection == null) {
				selectionAction = "widgetAddFromSelection('" + widgetId + "', '" + selectionId + "')";
			}
			else {
				selectionAction = "widgetAddFromSelectionAndSave('" + widgetId + "', '" + selectionId + "', '" + bc.getPath() + "')";
			}
//			html.append("<img class=\"widgetButton\" src=\"images/left_small.png\" onclick=\"" + action + "\">");
			html.append(getSelection(p, selectionId, null, true, false, true, "selection_", selectionAction));
			
//			String[] quickSelection = null;
			// TODO konfigurierbar
//			if (p.getName().equals("Participants")) {
//				quickSelection = new String[] { "SL", "LE", "EL" };
//			}
		
			if (p.getType().equals("ListFillIn")) {
				String fillInAction;
				if (listCollection == null) {
					fillInAction = "widgetFillIn('" + widgetId + "', this)";
				}
				else {
					fillInAction = "widgetFillInAndSave('" + widgetId + "', this, '" + bc.getPath() + "')";
				}
				html.append("<br><input class=\"widgetFillInField\" id=\"fillInField_" + widgetId + "\" onchange=\"" + fillInAction + "\"></input>");
			}
/*			if (quickSelection != null && quickSelection.length > 0) {
				html.append("<br>");
				for (String qsItem : quickSelection) {
					html.append("<input type=\"button\" value=\"" + qsItem + "\" onclick=\"widgetAdd('" + widgetId + "', '" + qsItem + "', '" + qsItem + "')\">");
				}
			} */
			html.append("</td>");
		}
		html.append("</tr></table>");
		
		return html.toString();
//		return "<textarea id=\"" + p.getName() + "\" name=\"" + p.getName() + "\">" + p.getValue() + "</textarea>";
	}
/*	
	public static String getTreeListField(Property p, boolean isEditable){
		StringBuffer html = new StringBuffer();
		String id =  p.getOwner().getPath("") + p.getName();
		String widgetId = "widget_" + p.getOwner().getPath("") + p.getName();
		getHiddenWidgetField(html, p, id);
		html.append("<table style=\"border-collapse: collapse\"><tr><td style=\"vertical-align: top; padding: 0;\">");
		getListWidget(html, p, isEditable, widgetId);
		html.append("</td>");
		if (isEditable && p.isEditable) { 
			html.append("<td style=\"vertical-align: top;\">");
		
			BasicClass root = p.getOwner().getRoot();
			String filter = "OrganisationalUnit"; // TODO aus Property lesen	
		
			WidgetTree widgetTree = new WidgetTree(root, filter, widgetId, 2, true);
			html.append(widgetTree.toHtml());
			html.append("</td>");
		}
		html.append("</tr></table>");
		
		return html.toString();
//		return "<textarea id=\"" + p.getName() + "\" name=\"" + p.getName() + "\">" + p.getValue() + "</textarea>";
	}
*/	
	private static void getHiddenWidgetField(StringBuffer html, Property p, String id, String prefix) {
		String inputId = "input_" + id;
		String value;
		if (p.getObject() instanceof BasicClass) {
			value = ((BasicClass)p.getObject()).getName();
		}
		else {
			value = p.getValue();
		}
		String name = prefix + p.getName();
		html.append("<input id=\"" + inputId + "\" type=\"hidden\" name=\"" + name + "\" value=\"" + value + "\">");
		
	}
	
	private static void getListWidget(StringBuffer html, Property p, boolean isEditable, String widgetId) {
		String[] values = p.getValue().split(LIST_WIDGET_SEPARATOR);
		Map<String, String> labels = new HashMap<String, String>();
		if (p.getValues() != null) {
			for (BasicInterface bi : p.getValues()) {
				labels.put(bi.getName(), bi.toString());
			}
		}
		html.append("<table id=\"" + widgetId + "\" class=\"widget\">");
		html.append("<tr class=\"widgetTitle\"><th colspan=2>" + p.getLabel() + "</th></tr>");
		for (String value : values) {
			if (value.length() > 0) {
				String label = value;
				if (labels.get(value) != null) {
					label = labels.get(value);
				}
				String cellId = "cell" + value;
				String deleteId = "delete" + value;
				html.append("<tr id=\"" + cellId + "\" class=\"widgetCell\"><td>");
				html.append(label);
				html.append("</td><td style=\"text-align: right;\">");
				if (isEditable && p.isEditable()) {
					String action = "widgetDelete(\"" + widgetId + "\", \""+cellId+"\")";
					html.append(" <img src='images/delete_small.png' id='" + deleteId + "' class='widgetButton' onclick='" + action + "'>");
				}
				html.append("</td></tr>");
			}
		}
		html.append("</table>");
	}
	
	private static void getAutosaveListWidget(StringBuffer html, String label, ObjectCollection listCollection, boolean isEditable, String widgetId) {
		html.append("<table id=\"" + widgetId + "\" class=\"widget\">");
		html.append("<tr class=\"widgetTitle\"><th colspan=2>" + label + "</th></tr>");
		for (BasicClass bc : listCollection.getObjects()) {
			String value = "";
			/*
			if (bc instanceof BasicDossierRecord) {
				value = ((BasicDossierRecord)bc).getListValue();
			}
			*/
			if (value.isEmpty()) {
				value = bc.getName();
			}
			String cellId = "cell" + value;
			String deleteId = "delete" + value;
			html.append("<tr id=\"" + cellId + "\" class=\"widgetCell\"><td>");
			html.append(bc.toString());
			html.append("</td><td style=\"text-align: right;\">");
			if (isEditable) {
				String action = "deleteObject(\"" + bc.getPath() + "\", \"" + bc.getParent().getPath() + "\")";
				html.append(" <img src='images/delete_small.png' id='" + deleteId + "' class='widgetButton' onclick='" + action + "'>");
			}
			html.append("</td></tr>");
		}
		html.append("</table>");
		
	}	
/*	
	private String getObjectList(BasicClass bc, String collectionName, boolean isEditable, ApplicationContext context) {
		StringBuffer html = new StringBuffer();
		ObjectCollection oc = bc.getObjectCollection(collectionName);
		if (oc != null && oc.getSize() > 0) {
			GridDefinition grid = OpenDossierServer.getGrid(null, collectionName);
			if (grid != null) {
				String id = "table_" + collectionName + "_" + bc.getPath("");
				html.append(getTable(grid, bc, id, collectionName, null, bc.getPath(""), isEditable, false, true, context));
			}
		}
		return html.toString();
	}
*/
	
	public static String getHTMLEditor(Property p){
		StringBuffer html = new StringBuffer();
		String id = createId(p);
		html.append("<textarea id=\"" + id + "\" name=\"" + p.getName() + "\">" + p.getValue() + "</textarea>");
		html.append("<script>");
		html.append("var editor = CKEDITOR.instances." + id + ";\n");
		html.append("if (editor) { CKEDITOR.remove(editor); }\n");
		html.append("CKEDITOR.replace('" + id + "');\n");
		html.append("</script>");
		return html.toString();
	}
	public static String getHTMLStatic(Property p){
		return "<div class=\"htmlArea\">" + p.getValue() + "</div>";
	} 
	
	public String getForm(HTMLFormDefinition form, BasicClass o, boolean isEditable, ApplicationContext context){
		
		StringBuilder html = new StringBuilder();
		
		html.append(getToolbar(o, null, null, context, isEditable));
		
		String objectPath = o.getPath("");
		
		html.append("<div id=\"" + form.getName() + "_" + o.getPath("") + "\" class=\"formContainer\" style=\"\"><form id=\"" + form.getName() + "\" action=\"\" style=\"\">");
		html.append("\n<input type=\"hidden\" id=\"objectPath\" name=\"objectPath\" value=\"" + objectPath + "\">");
		html.append("\n<input type=\"hidden\" name=\"action\" value=\"getObjectAction\">");	
		html.append("\n<input type=\"hidden\" name=\"command\" value=\"saveobject\">");		
		
		Vector<BasicClass> elements = form.getObjects("Subelements");
		for(BasicClass elementObject : elements){
			BasicFormElement element = (BasicFormElement)elementObject;
			
			String name = element.getName();
			String label = element.getLabel();
			int x = element.getInt("x");
			int y = element.getInt("y");
			int width = element.getInt("width");
			int height = element.getInt("height");
			int labelWidth = Math.min((int)(width * 0.75), 120);
			if (!element.getString("labelWidth").isEmpty()) {
				labelWidth = element.getInt("labelWidth");
			}
			
			if(element.getType().equals("customwidget")){
				try{
					Class<?> c = Class.forName(element.getString("class"));
					HtmlObjectWidget widget = (HtmlObjectWidget)c.newInstance();
					html.append(widget.renderWidget(o));
				}
				catch(Exception e){
					webapp.writeError(e);
				}
			}
			else if(element.getClasstype().equals("org.kubiki.ide.BasicFormElement")){
				
				Property p = o.getProperty(element.getString("dataobject"));
				
				if(p != null){
					
					html.append("<label for=\"" + name + "\" class=\"formelement\" style=\"" + getPosStyle(x, y, labelWidth, height) + "\">" + label + "</label>");
					if(p.getValues() != null){	
						html.append("<div style=\"position:absolute;" + getPosStyle(x + labelWidth, y, width - labelWidth, height) + "\">");
						insertSelection(html, p, isEditable);
						html.append("</div>");
					}
					else{	
						html.append("<input class=\"formelement\" style=\"" + getPosStyle(x + labelWidth, y, width - labelWidth, height) + "\" id=\"" + name + "\" name=\"" + name + "\" value=\"" + p.getValue() + "\"");
						if(isEditable){
							html.append(">");
						}
						else{
							html.append(" DISABLED>");							
						}
											
					}
				}
			}
			else if(element.getClasstype().equals("org.kubiki.ide.ButtonDefinition")){
				html.append("<input class=\"formelement\" type=\"button\" id=\"" + name + "\" style=\"" + getPosStyle(x, y, width, height) + "\" onClick=\"" + element.getString("action").replace("<OBJECTPATH>", objectPath) + "\" value=\"" + label + "\">");	
			}
			else if(element.getClasstype().equals("org.kubiki.ide.GridDefinition")){
				GridDefinition grid = (GridDefinition)element;
				String dataobject = grid.getString("dataobject");
				String id = null;
				id = "table_" + dataobject + "_" + o.getPath("");
				html.append(getTable(grid, o, id, dataobject, "position: absolute;" + getPosStyle(x, y, width, height), o.getPath(""), isEditable, true, context));
			}		
		}
		
		html.append("</form></div>");
		return html.toString();	
	}
	
	private String getPosStyle(int x, int y, int width, int height) {
		return "left:" + x + "px;top:" + y + "px;width:" + width + "px;height:" + height + "px;";
	}

	public String getTable(GridDefinition grid, BasicClass bc, String id, String collectionName, String style, 
			               String returnTo,
                           boolean isEditable, boolean canSort, ApplicationContext context) {
		return getTable(grid, bc, id, collectionName, style, null, returnTo, isEditable, canSort, -1, context);
	}

	public String getTable(GridDefinition grid, BasicClass bc, String id, String collectionName, String style, 
						   Map<String, String> filterMap, String returnTo,
			               boolean isEditable, boolean canSort, int loginID, ApplicationContext context) {
		StringBuffer html = new StringBuffer();
		boolean isCollection = returnTo.lastIndexOf(":") < returnTo.lastIndexOf("/");
		if (isCollection) {
			html.append(getToolbar(bc, collectionName, returnTo, context, isEditable));
			html.append(getPathLinks(bc, true, returnTo));
			html.append("<div style=\"height:18px\"></div>"); // make some space for path links above table
		}
		html.append("<div class=\"grid\" id=\"" + id + "\" name=\"" + collectionName + "\"");
		if (style != null) {
			html.append(" style=\"");
			html.append(style);
			html.append("\"");
		}
		if (returnTo != null) {
			html.append(" returnTo=\"");
			html.append(returnTo);
			html.append("\"");
		}
		html.append(">");
		ObjectCollection oc = bc.getObjectCollection(collectionName);
		if (oc != null) {
			SortType sortType = canSort ? SortType.SERVER : SortType.NONE;
			html.append(getTableContent(oc, grid.getFieldNames(), Arrays.asList(grid.getHeaders()), null, null, filterMap, returnTo, 
					                    isEditable, isEditable, sortType, context));
		}
		html.append("</div>");
		return html.toString();
	}
	
	public static String getSubobjectList(List<BasicClass> subobjects, boolean isEditable, String noDataText) {
		StringBuilder html = new StringBuilder();
		if (subobjects != null && !subobjects.isEmpty()) {
			html.append("<table class=\"datatable\"><tr>");
			List<String> propertyNames = subobjects.get(0).getPropertyNames();
			for (String propertyName : propertyNames) {
				Property prop = subobjects.get(0).getProperty(propertyName); 
				if (!prop.isHidden()) {
					html.append("<th>");
					html.append(prop.getLabelOrName());
					html.append("</th>");
				}
			}
			html.append("</tr>");
			for (BasicClass subobj : subobjects) {
				html.append("<tr>");
				for (String propertyName : propertyNames) {
					Property prop = subobj.getProperty(propertyName); 
					if (!prop.isHidden()) {
						if (isEditable && prop.isEditable()) {
							html.append("<td class=\"inputCell\">");
						}
						else {
							html.append("<td>");
						}
						String prefix = "subobject_" + ((Record)subobj).getTablename() + "_" + subobj.getName() + "_";
						if (prop.getValues() != null) {
							html.append(getSelection(prop, isEditable, prefix));
						}
						else if (prop.getType().equals("Date")) {
							html.append(getDateField(prop, isEditable, prefix));
						}
						else {
							html.append(getTextField(prop, isEditable, prefix));
						}
						html.append("</td>");
					}
				}
				html.append("</tr>");
			}
			html.append("</table>");
		}
		else {
			if (noDataText != null) {
				html.append(noDataText);
			}
		}
		return html.toString();
	}


	public static String getTable(ObjectCollection oc, ApplicationContext context) {
		StringBuffer html = new StringBuffer();
		String id = oc.getName();
		html.append("<div class=\"grid\" id=\"" + id + "\" name=\"" + oc.getName() + "\"");
		html.append(">");
		if (oc.getSize() > 0) {
			List<String> fields = new Vector<String>();
			List<String> columnLabels = new Vector<String>();
			BasicClass bc = (BasicClass)oc.getObjectByIndex(0);
			List<String> propertyNames = bc.getPropertyNames();
			for (String propertyName : propertyNames) {
				Property property = bc.getProperty(propertyName);
				if (!property.isHidden() && !propertyName.equals("name")) {
					fields.add(propertyName);
					String label = property.getLabelOrName();
					columnLabels.add(label);
				}
			}
			html.append(getTableContent(oc, fields, columnLabels, null, null, null, null, 
						                    false, false, SortType.NONE, context));
		}
		else {
			html.append("Keine Daten gefunden");
		}
		html.append("</div>");
		return html.toString();
	}
	
	public static String getTableContent(ObjectCollection objectCollection, 
			               List<String> fields, 
			               List<String> columnLabels, 
			               List<String> sortFields,
			               List<String> sortDirections,
			               Map<String, String> filterMap,
			               String returnTo, 
			               boolean canEdit,
			               boolean canDelete,
			               SortType sortType,
			               ApplicationContext context) {
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
		int loginID = userSession.getLoginID();
//		boolean canExecute = objectCollection.getName().equals("QueryDefinition");
		Map<String, String> sortDirectionMap = new HashMap<String, String>();
		if (sortFields != null) {
			for (int i = sortFields.size() - 1; i >= 0; i--) {
				String sortField = sortFields.get(i);
				boolean sortUp = true;
				if (sortDirections != null && sortDirections.size() > i) {
					String sortDirection = sortDirections.get(i);
					if (sortDirection != null && sortDirection.equals("desc")) {
						sortUp = false;
					}
					sortDirectionMap.put(sortField, sortDirection);
				}
				objectCollection.sort(sortField, sortUp);
			}
		}
		
		StringBuilder html = new StringBuilder();
		html.append("<table class=\"datatable\">");
		html.append("<tr>");
//		String name = objectCollection.getName();
		for (int i = 0; i < fields.size(); i++) {
			String field = fields.get(i);
			String label = field;
			if (columnLabels != null && columnLabels.size() > i) {
				label = columnLabels.get(i);
			}
			getTableHeaderCell(html, label, sortType, sortDirectionMap, field);
		}
		if (canEdit) {
			html.append("<th></th>");
		}
		if (canDelete) {
			html.append("<th></th>");
		}
/*		if (canExecute) {
			html.append("<th></th>"); 
		} */
		html.append("</tr>");
		List<BasicClass> objects = null;
		if (filterMap != null) {
			objects = objectCollection.getFilteredObjects(filterMap);
		}
		else {
			objects = objectCollection.getObjects();
		}
		for (BasicClass bc : objects) {
			boolean canEditObject = canEdit;
			boolean canDeleteObject = canDelete;
			boolean isActive = true;
			boolean isVisible = true;
			if (bc instanceof Record) {
				canEditObject &= ((Record)bc).isEditable();
				canDeleteObject &= ((Record)bc).isEditable();
				/*
				if (bc instanceof BasicDossierObject && loginID != -1) {
					canEditObject &= ((BasicDossierObject)bc).isEditable(userSession);
					canDeleteObject &= ((BasicDossierObject)bc).isEditable(userSession);
				}
				*/
			}
			/*
			if (bc instanceof BasicDossierObject) {
				isActive = ((BasicDossierObject)bc).isActive();
				isVisible = ((BasicDossierObject)bc).isVisible(context);
			}
			*/
/*			if (canExecute) {
				if (bc instanceof QueryDefinition) {
					QueryDefinition queryDefinition = (QueryDefinition)bc;
					if (!queryDefinition.isExecutable(userSession)) {
						continue;
					}
				}
			} */
			if (isVisible) {
				if (isActive) {
					html.append("<tr>");
				}
				else {
					html.append("<tr class=\"inactiveRow\">");
				}
				String editCode = "editObject('" + bc.getPath("") + "'";
				if (returnTo != null) {
					editCode += ", '" + returnTo + "'";
				}
				editCode += ")";
				String deleteCode = editCode.replace("editObject", "deleteObject");
				for (String field : fields) {
					html.append("<td ondblclick=\"" + editCode + "\">");
					String type = bc.getProperty(field).getType();
					if (type.equals("DateTime") || type.equals("Date")) {
						html.append(DateConverter.sqlToShortDisplay(bc.getString(field)));
					}
					else if(field.equals("Title")){
						html.append(bc.toString());
					}
					else {
						if (bc.getObject(field) != null) {
							html.append(bc.getObject(field));
						}
					}
					
					html.append("</td>");
				}
				if (canEdit) {
					if (canEditObject) {
						html.append("<td class=\"iconCell\"><a href=\"javascript:" + editCode + "\"><img class=\"tableIcon\" src=\"images/edit_small.png\" alt=\"edit\"></a></td>");
					}
					else {
						html.append("<td></td>");
					}
				}
				if (canDelete) {
					if (canDeleteObject) {
						html.append("<td class=\"iconCell\"><a href=\"javascript:" + deleteCode + "\"><img class=\"tableIcon\" src=\"images/delete_small.png\" alt=\"delete\"></a></td>");
					}
					else {
						html.append("<td></td>");
					}
				}
	/*			if (canExecute) {
					String sql = "";
					if (bc instanceof QueryDefinition) {
						sql = ((QueryDefinition)bc).getSql().replace("'", "\\'");
					}
					String executeCode = "executeQuery('" + sql + "'";
					if (returnTo != null) {
						executeCode += ", '" + returnTo + "'";
					}				
					executeCode += ")";
					html.append("<td class=\"iconCell\"><a href=\"javascript:" + executeCode + "\"><img class=\"tableIcon\" src=\"images/execute_small.png\" alt=\"execute\"></a></td>");
				} */
				html.append("</tr>");
			}
		}
		html.append("</table>");
		return html.toString();	
	}
	
	public static void getTableHeaderCell(StringBuilder html, String label, boolean isSortable) {
		getTableHeaderCell(html, label, isSortable ? SortType.CLIENT : SortType.NONE, null, null);
	}
	
	private static void getTableHeaderCell(StringBuilder html, String label, SortType sortType, 
			Map<String, String> sortDirectionMap, String field) {
		if (sortType != SortType.NONE) {
			String sortIcon = "sort_none.gif";
			if (sortType == SortType.SERVER) {
				String sortDirection = sortDirectionMap.get(field);
				if (sortDirection != null) {
					if (sortDirection.equals("asc")) {
						sortIcon = "sort_up.gif";
					}
					else if (sortDirection.equals("desc")) {
						sortIcon = "sort_down.gif";
					}
				}
				html.append("<th onclick=\"sortTable(this, '" + field + "')\">");
			}
			else {
				html.append("<th onclick=\"clientSideSort(this)\">");
			}
			html.append(label);
			html.append("<img class=\"sortIcon\" src=\"images/" + sortIcon + "\">");
			html.append("</th>");		
		}
		else {
			html.append("<th>" + label + "</th>");
		}
	}	
	
	public static void getSortableDateCell(StringBuilder html, String date) {
		html.append("<td value=\"" + date + "\">");
		html.append(DateConverter.sqlToShortDisplay(date));
		html.append("</td>");
	}
	
	public static String getEditorId(String id) {
		// remove characters that aren't allowed in a JavaScript variable 
		return "editorContent_" + id.replaceAll("[/: -]", "");
	}
	
	public String getContextMenu(BasicClass o, String collectionFilter, WebApplicationContext context, boolean isEditable) {
		Vector<ContextMenuEntry> contextMenu = o.getContextMenu();
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
		StringBuffer html = new StringBuffer();
		for (ContextMenuEntry entry : contextMenu) {
			boolean commandAllowed = prepareCommand(o, entry, null, isEditable, collectionFilter, userSession);
			if (commandAllowed) {
				String command = entry.getCommand();
				html.append("<div class=\"popupMenu\" onmouseover=\"highlightMenu(this, true)\" onmouseout=\"highlightMenu(this, false)\" onclick=\"");
				if (command.startsWith("javascript:")) {
					html.append(command.substring("javascript:".length()));
				}
				else {
					html.append("selectMenu('" + o.getPath("") + "', '" + command + "', null, " + entry.requiresConfirmation() + ")");
				}
				html.append("\">");
				html.append(entry.getLabel());
				html.append("</div>");
			}
		}
		return html.toString();
	}
	public static String getToolbar(BasicClass o, String collectionFilter, String returnTo,
			ApplicationContext context, boolean isEditable) {
		return getToolbar(o, collectionFilter, returnTo, context, isEditable, null);
	}
	
	public static String getToolbar(BasicClass o, String collectionFilter, String returnTo, 
		ApplicationContext context, boolean isEditable, CustomObjectView customView) {
		Vector<ContextMenuEntry> contextMenu = o.getContextMenu();
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
		StringBuffer html = new StringBuffer();
		boolean isEmpty = true;
		html.append("<div class=\"toolbar\">");
		if (isEditable && userSession.hasPermission("saveobject", o) && collectionFilter == null) {
		//if (isEditable &&  collectionFilter == null) {
			String customSaveCode = null;
			if (customView != null) {
				customSaveCode = customView.getSaveCode(o, returnTo);
			}
			/*
			else if (o instanceof BasicDossierRecord) {
				customSaveCode = ((BasicDossierRecord)o).getSaveCode(returnTo);
			}
			*/
			if (customSaveCode == null) {
				html.append(getToolbarSaveButton(o, returnTo));
			}
			else {
				html.append(getToolbarSaveButton(o, returnTo, customSaveCode));
			}
			isEmpty = false;
		}
		if (contextMenu != null) {
			for (ContextMenuEntry entry : contextMenu) {
				boolean commandAllowed = prepareCommand(o, entry, returnTo, isEditable, collectionFilter, userSession);
				
				if (commandAllowed) {
					html.append(getToolbarButton(o, entry, returnTo));
					isEmpty = false;
				}
			}
		}
		html.append("</div>");
		if (isEmpty) {
			return "";
		}
		else {
			return html.toString();
		}
	}
	
	public static String getToolbarButton(BasicClass bc, ContextMenuEntry entry, String returnTo) {
		StringBuilder html = new StringBuilder();
		html.append("<a class=\"toolbarLink\">");
		html.append("<div class=\"toolbarIcon\" onmouseover=\"highlightToolbarIcon(this, true)\" onmouseout=\"highlightToolbarIcon(this, false)\" onclick=\"");
		
		String command = entry.getCommand();
		if (command.startsWith("javascript:")) {
			html.append(command.substring("javascript:".length()));
		}
		else {
			html.append("onAction('");
			html.append(bc.getPath(""));
			html.append("','");
			html.append(command);
			html.append("', '");
			html.append(returnTo);
			html.append("', '', ");
			html.append(entry.requiresConfirmation());
			html.append(")");
		}
		html.append("\">");
		html.append("<img class=\"toolbarIconImage\" src=\"images/" + getToolbarIcon(entry.getCommand()) + "\">");
		html.append("<div class=\"toolbarIconLabel\">");
		html.append(entry.getLabel());
		html.append("</div>");
		html.append("</div>");
		html.append("<span class=\"tooltip\">");
		html.append(entry.getLabel());
		html.append("</span>");
		html.append("</a>");
		return html.toString();
	}
	
	private static String getToolbarIcon(String command) {
		if (command.equals("addcase")) {
			return "addCase.png";
		}
		if (command.equals("addvolume")) {
			return "addVolume.png";
		}
		if (command.equals("closedossier")) {
			return "closeDossier.png";
		}
		if (command.equals("addactivity") || command.equals("addactivitydef")) {
			return "addActivity.png";
		}
		if (command.equals("addcasetypedef")) {
			return "addCaseType.png";
		}
		if (command.equals("addcasedetaildef")) {
			return "addCaseDetail.png";
		}
		if (command.equals("addrelationdef")) {
			return "addRelation.png";
		}
		if (command.equals("addfunctiondef")) {
			return "addFunction.png";
		}
		else if (command.equals("adddocument")) {
			return "addDocument.png";
		}
		else if (command.equals("adddetail")) {
			return "addCaseDetail.png";
		}
		else if (command.equals("adddecree")) {
			return "addDecree.png";
		}
		else if (command.equals("addmeasureservice")) {
			return "addMeasureService.png";
		}
		else if (command.equals("addmeasure")) {
			return "addMeasure.png";
		}
		else if (command.equals("modifymeasure")) {
			return "modifyMeasure.png";
		}
		else if (command.equals("addnote")) {
			return "addNote.png";
		}
		else if (command.equals("addmessage")) {
			return "addMessage.png";
		}
		else if (command.equals("addperson")) {
			return "addPerson.png";
		}
		else if (command.equals("addlogin")) {
			return "addLogin.png";
		}
		else if (command.equals("addpermission") || command.equals("addrolepermission")) {
			return "addPermission.png";
		}
		else if (command.equals("addrole") || command.equals("addloginrole")) {
			return "addRole.png";
		}
		else if (command.equals("addtemplatelibrary")) {
			return "addDocumentTemplateLibrary.png";
		}
		else if (command.equals("addtemplate")) {
			return "addDocumentTemplate.png";
		}
		else if (command.equals("addheaderfooter")) {
			return "addHeaderFooter.png";
		}
		else if (command.equals("uploadheaderfooter")) {
			return "uploadHeaderFooter.png";
		}
		else if (command.equals("addmodule")) {
			return "addDocumentTemplateModule.png";
		}
		else if (command.equals("addtextblockadministration")) {
			return "addTextBlockLibrary.png";
		}
		else if (command.equals("addtextblock")) {
			return "addTextBlock.png";
		}
		else if (command.equals("addaddress")) {
			return "addAddress.png";
		}
		else if (command.equals("changeaddress")) {
			return "changeAddress.png";
		}
		else if (command.equals("addquerydefinition")) {
			return "addQuery.png";
		}
		else if (command.equals("copyquerydefinition")) {
			return "copyQuery.png";
		}
		else if (command.equals("createpdf") || command.equals("pdfpreview")) {
			return "createPDF.png";
		}
		else if (command.equals("createword") || command.equals("wordpreview")) {
			return "createWord.png";
		}
		else if (command.equals("createwordmailinglist")) {
			return "createWordMailingList.png";
		}
		else if (command.equals("uploadword")) {
			return "uploadWord.png";
		}
		else if (command.equals("send")) {
			return "sendDocument.png";
		}
		else if (command.equals("sign")) {
			return "signDocument.png";
		}
		else if (command.equals("insertparagraphs")) {
			return "insertParagraphs.png";
		}
		else if (command.equals("uploaddocument")) {
			return "uploadDocument.png";
		}
		else if (command.equals("deactivate")) {
			return "deactivatePerson.png";
		}
		else if (command.equals("reactivate")) {
			return "reactivatePerson.png";
		}
		else if (command.equals("closecase")) {
			return "lockCase.png";
		}
		else if (command.equals("resumecase")) {
			return "unlockCase.png";
		}
		else if (command.equals("setcasetype")) {
			return "setCaseType.png";
		}
		else if (command.equals("overviewnote")) {
			return "overviewNotes.png";
		}
		else if (command.equals("overviewparameter")) {
			return "overviewParameters.png";
		}
		else if (command.equals("refer")) {
			return "referOrganisation.png";
		}
		else if (command.equals("movecase")) {
			return "moveCase.png";
		}
		else if (command.equals("movetoothercase")) {
			return "moveToOtherCase.png";
		}
		else if (command.equals("refertoperson")) {
			return "referPerson.png";
		}
		else if (command.equals("reply")) {
			return "replyMessage.png";
		}
		else if (command.equals("addquerypermission")) {
			return "addQueryPermission.png";
		}
		else if (command.equals("addqueryperson")) {
			return "addQueryPerson.png";
		}
		else if (command.equals("addqueryorganisation")) {
			return "addQueryOrganisation.png";
		}
		else if (command.equals("casehistory")) {
			return "caseHistory.png";
		}
		else if (command.equals("documenthistory")) {
			return "documentHistory.png";
		}
		else if (command.equals("addquerycolumn")) {
			return "addQueryColumn.png";
		}
		else if (command.equals("addstatusdefinition")) {
			return "addStatus.png";
		}
		else if (command.equals("addstatustransition")) {
			return "addStatusTransition.png";
		}
		else if (command.equals("addorganisationalunit")) {
			return "addOrganisationalUnit.png";
		}
		else if (command.equals("addstaffmember")) {
			return "addStaffMember.png";
		}
		else if (command.equals("changemeasureprice")) {
			return "addPrice.png";
		}
		else if (command.equals("execute")) {
			return "execute.png";
		}
		else if (command.startsWith("javascript:delete")) {
			return "delete_big.png";
		}
		else if (command.startsWith("print")) {
			return "print.png";
		}
		else {
			return "toolbarIcon.png";
		}
	}

	public static String getToolbarSaveButton(BasicClass o, String returnTo) {
		// same Code as in the JavaScript-Function showGrid()
		String saveCode = "saveObject(\'" + o.getPath("") + "\'";
		if (returnTo != null) {
			saveCode += ", '" + returnTo + "'";
		}
		saveCode += ")";
		return getToolbarSaveButton(o, returnTo, saveCode);
	}
	
	private static String getToolbarSaveButton(BasicClass o, String returnTo, String saveCode) {
		StringBuilder html = new StringBuilder();

		html.append("<a class=\"toolbarLink\">");
		String id = "saveButton";
		/*
		Dossier dossier = (Dossier)o.getParent("org.opendossier.dossier.Dossier");
		if (dossier != null) {
			id += "_" + dossier.getName();
		}
		*/
		html.append("<div id=\"" + id + "\"class=\"toolbarIcon\" onClick=\"" + saveCode + "\" onmouseover=\"highlightToolbarIcon(this, true)\" onmouseout=\"highlightToolbarIcon(this, false)\">");
		html.append("<img class=\"toolbarIconImage\" src=\"images/save.png\">");
		html.append("<div class=\"toolbarIconLabel\">Speichern</div>");
		html.append("</div>");
		html.append("<span class=\"tooltip\">Speichern</span></a>");
		return html.toString();
	}
	
	public static String getPathLinks(BasicClass o, boolean isParent, String returnTo) {
		String html = "";
		BasicClass parent = isParent ? o : getBreadcrumbParent(o);
		while (parent instanceof Record) {
			if (!html.isEmpty()) {
				html = " <img src=\"images/pathArrow.png\"> " + html;
			}
			String linkCommand = "editObject('" + parent.getPath() + "')";
			/*
			if (o instanceof CaseRecord && parent instanceof Dossier) {
				// switch to cases grid
				linkCommand = "currentDossierGrid = 'dcases_" + parent.getName() + "';" + linkCommand;
			}
			else if (o instanceof MeasureService && parent instanceof OrganisationalUnit) {
				// TODO: Should not be called in the administration application
				if (parent.getParent() instanceof OrganisationalUnit) {
					linkCommand = "editObject('"+ parent.getPath() +"', '', 'content')";
				}
				else {
					linkCommand = "getDefaultEditArea('userhome', 'organisations')";
				}
			}
			
			else if (o instanceof OrganisationalUnit && returnTo != null && returnTo.equals("home~")) {
				linkCommand = "getDefaultEditArea('userhome', 'schoolregistry')";
			}
			*/

			/*
			if (parent instanceof CaseRecord) { //08072013, Todo: die alternativen Pfade formalisieren
				if(parent.getObject("ParentCaseRecord") instanceof CaseRecord){
					html = getPathLink(parent, linkCommand) + html;
					CaseRecord parentCaseRecord = (CaseRecord)parent.getObject("ParentCaseRecord");
					linkCommand = "editObject('" + parentCaseRecord.getPath() + "')";
					linkCommand = "currentDossierGrid = 'dcases_" + parentCaseRecord.getName() + "';" + linkCommand;
					html = getPathLink(parentCaseRecord, linkCommand) + html;
				}
				else if(returnTo != null){
					CaseRecord parentCaseRecord = (CaseRecord)parent.getParent().getObjectByName("CaseRecord", returnTo);
					if(parentCaseRecord != null){
						linkCommand = "editObject('" + parentCaseRecord.getPath() + "')";
						linkCommand = "currentDossierGrid = 'dcases_" + parentCaseRecord.getName() + "';" + linkCommand;
						html = getPathLink(parentCaseRecord, linkCommand) + html;
					}

					linkCommand = "editObject('" + parent.getPath() + "')";
					linkCommand = "currentDossierGrid = 'dcases_" + parent.getName() + "';" + linkCommand;
					html = getPathLink(parent, linkCommand) + html;
				}
				else{
					html = getPathLink(parent, linkCommand) + html;
				}
			}
			else{
			*/

				html = getPathLink(parent, linkCommand) + html;
			/*
			}
			*/

			parent = getBreadcrumbParent(parent);
		}
		html = "<div class=\"pathLinks\">" + html + "</div>";
		return html;
	}
	private static String getPathLink(BasicClass parent, String linkCommand){

		return "<span class=\"pathLink\"" + 
				   " onclick=\"" + linkCommand + "\"" + 
				   " onmouseover=\"highlightToolbarIcon(this, true)\"" + 
				   " onmouseout=\"highlightToolbarIcon(this, false)\"" + 
				   //">" + Tree.getIcon(parent) + parent.toString() + 
				   ">" + parent.toString() + 
				   "</span>";
	}
	private static BasicClass getBreadcrumbParent(BasicClass bc) {
		/*
		if (bc instanceof BasicDossierRecord) {
			return ((BasicDossierRecord)bc).getBreadcrumbParent();
		}
		else {
		*/
			return bc.getParent();
		/*
		}
		*/
	}
/*	
	public static String getSaveButton(BasicClass o, String returnTo, boolean isEditable) {
		StringBuilder html = new StringBuilder();

		if (o instanceof Activity) {
			FieldContainerContentView.getSaveButton(html, o, isEditable, true, returnTo);
			return html.toString();
		}
		
		boolean hasBackButton = returnTo != null && returnTo.endsWith("~back");
		html.append("<input type=\"button\" onClick=\"saveObject(\'" + o.getPath("") + "\'");
		if (returnTo != null && !hasBackButton) {
			html.append(", '" + returnTo + "'");
		}
		html.append(")\" value=\"Speichern\"");
		if (!isEditable) {
			html.append(" disabled=\"disabled\"");
		}
		html.append(">");
		if (hasBackButton) {
			html.append("&nbsp;");
			String[] returnToArr = returnTo.split("~");
			returnTo = returnToArr[0];
			if (returnTo.equals("home")) {
				String additionalParams = null;
				if (returnToArr.length > 1) {
					additionalParams = returnToArr[1].replace("AND", "&");
				}
				html.append("<input type=\"button\" onClick=\"getDefaultEditArea(section, subsection");
				if (additionalParams != null) {
					html.append(", '" + additionalParams + "'");
				}
				html.append(")\" value=\"Zurück\">");
			}
			else {
				html.append("<input type=\"button\" onClick=\"editObject(\'" + returnTo + "\')\" value=\"Zurück\">");
			}
		}
		return html.toString();
	}	
*/	
		
	public void addObjectView(String classname, ObjectView view){
		/*
		ObjectViewDefinitions objectViewDefinitions = null;
		if(objectViewDefinitionsMap.get(classname) != null){
			objectViewDefinitions = objectViewDefinitionsMap.get(classname);
		}
		else{
			objectViewDefinitions = new ObjectViewDefinitions();
		    objectViewDefinitionsMap.put(classname, objectViewDefinitions);
		}
		objectViewDefinitions.put(view.getId(), view);
		*/
	}

	public String getObjectTabbar(BasicClass o, String selectedView, String returnTo){
		StringBuilder html = new StringBuilder();
		/*
		ObjectViewDefinitions objectViews = objectViewDefinitionsMap.get(o.getClassname());
		boolean hasCustomViews = false;
		if (objectViews != null) {
			for (ObjectView objectView : objectViews.values()) {
				if (!objectView.getId().equals("default") && objectView.display()) {
					hasCustomViews = true;
				}
			}
		}
		if (hasCustomViews) {
			html.append("<div class=\"objectViewTabBar\">");
		    for(ObjectView objectView : objectViews.values()){
		    	Tab tab = new ObjectViewTab(o.getPath("") + "_" + objectView.getId(), objectView.getId(), objectView.getLabel(), returnTo);
		    	boolean selected = objectView.getId().equals(selectedView);
		    	if (objectView.getId().equals("default")) {
		    		selected |= (selectedView == null);
		    	}
	    		tab.setSelected(selected);
		    	tab.setObject(o);
		    	html.append(tab.toHtml());
//		    	html.append("<a href=\"javascript:editObject('" + o.getPath("") + "', null, '" + label + "')\">[" + label + "]</a>");
		    }
			html.append("</div>");
		}
		*/
		return html.toString();
	}
	
	private static boolean prepareCommand(BasicClass o, ContextMenuEntry entry, String returnTo, 
			boolean isEditable, String collectionFilter, OpenCommunityUserSession userSession) {
		String command = entry.getCommand();
		boolean hasPermission = true;
		if (!isEditable && entry.isEditCommand()) {
			hasPermission = false;
		}
		else if (command.startsWith("javascript:")) {
			/*
			if (o instanceof BasicDossierRecord) {
				BasicDossierRecord bdo = (BasicDossierRecord)o;
				hasPermission = bdo.hasPermission(command.substring("javascript:".length()), userSession);
			}
			*/
			if (command.equals("javascript:deleteObject")) {
				String returnToAfterDelete = returnTo;
				if ((returnToAfterDelete == null || returnToAfterDelete.startsWith(o.getPath())) && 
						o.getParent() != null) {
					returnToAfterDelete = o.getParent().getPath();
				}
				/*
				if (o instanceof Dossier) {
					command += "('" + o.getPath() + "', '', true, 'close=true')";
				}
				else {
				*/
					command += "('" + o.getPath() + "', '" + returnToAfterDelete + "')";
				/*
				}
				*/
				entry.setCommand(command);
			}
		}
		else {
			hasPermission = userSession.hasPermission(command, o);
		}
		return hasPermission && (collectionFilter == null || collectionFilter.equals(entry.getCollectionName()));
	}
	
	private static String getHtmlValue(Property p) {
		String value = p.getValue().toString();
		value = value.replace("<", "&lt;");
		value = value.replace(">", "&gt;");
		value = value.replace("\"", "&quot;");
		return value;
	}
	//-----------------------------------------------------------
	public static String getCustomCheckbox(String id, String value, boolean selected, String imageselected, String image, String label){
		return getCustomCheckbox(id, value, selected, imageselected, image, label, null);
	}
	public static String getCustomCheckbox(String id, String value, boolean selected, String imageselected, String image, String label, String name){
		return getCustomCheckbox(id, value, selected, imageselected, image, label, name, "customcheckbox");
	}
	public static String getCustomCheckbox(String id, String value, boolean selected, String imageselected, String image, String label, String name, String classname){
		return getCustomCheckbox(id, value, selected, imageselected, image, label, name, classname, "setCheckboxValue");
	}
	public static String getCustomCheckbox(String id, String value, boolean selected, String imageselected, String image, String label, String name, String classname, String function){
		return getCustomCheckbox(id, value, selected, imageselected, image, label, name, classname, function, null);
	}
	public static String getCustomCheckbox(String id, String value, boolean selected, String imageselected, String image, String label, String name, String classname, String function, String helptext){
		if(name==null){
			name = id;	
		}
		StringBuilder cb = new StringBuilder();
		cb.append("<input type=\"hidden\" id=\"" + id + "\"  name=\"" + name + "\" value=\"" + value + "\">");
		if(selected){
			cb.append("<table cellspacing=0 cellpadding=0><tr><td><img id=\"" + id + "_ccbimg\" class=\"" + classname + "\" src=\"" + imageselected + "\" onclick=\"" + function + "(\'" + id + "\')\">");
		}
		else{
			cb.append("<table cellspacing=0 cellpadding=0><tr><td><img id=\"" + id + "_ccbimg\" class=\"" + classname + "\" src=\"" + image + "\" onclick=\"" + function + "(\'" + id + "\')\">");
		}
		cb.append("</td><td class=\"" + classname + "\"><span class=\"" + classname + "\">" + label + "</span>");
		if(helptext != null){
			cb.append("<img src=\"images/help.png\" onmouseover=\"showHelp2(event, '" + helptext + "')\" onmouseout=\"hideHelp()\" style=\"height : 12px;\">");
		}
		cb.append("</td></tr></table>");
		return cb.toString();
	}
	
	//---------------------------------------------------------------
	
	public static String getMultipleItemWidget(Property p, boolean isEditable, String prefix){
		StringBuffer html = new StringBuffer();
		
		String id = prefix + p.getName();
		
		html.append("<div id=\"multipleitemwidget_" + id + "\">");
		

		String value = p.getValue();
		String[] items = value.split("#");
		int i = 0;
		html.append("<input id=\"input_" + id + "\" type=\"hidden\" name=\"" + id + "\" value=\"" + value + "\">");
		boolean hasitem = false;
		for(String item : items){
			if(item.trim().length() > 0 || i==0){
				html.append("<div class=\"multipleItemWidget\" id=\"item_" + i + "_" + id + "\">");
				html.append("<input name=\"item_" + id + "\" value=\"" + item + "\" onblur=\"updateMultipleItemWidget('" + id + "')\" onkeyup=\"getSuggestions(event, this.value, this)\">");
				html.append("<input type=\"button\" onclick=\"removeItemFromWidget('multipleitemwidget_" + id + "', 'item_" + i + "_" + id + "')\" value=\" - \">");
				html.append("</div>");
				hasitem = true;
			}
			i++;
		}
		if(!hasitem){
			html.append("<div class=\"multipleItemWidget\" name=\"item_" + id + "\" id=\"item_" + 0 + "_" + id + "\">");
			html.append("<input name=\"item_" + id + "\" value=\"\" onblur=\"updateMultipleItemWidget('" + id + "')\">");
			html.append("</div>");
		}
		html.append("<input id=\"addbutton_" + id + "\" style=\"float : left;\" type=\"button\" onclick=\"addItemToWidget('multipleitemwidget_" + id + "')\" value=\" + \">");
		html.append("</div>");
		
		return html.toString();	
	}
	public String getStandardButton(String command, String label){
		
		StringBuilder html = new StringBuilder();
		html.append("<input type=\"button\" class=\"actionbutton\" value=\"" + label + "\" onclick=\"" + command + "\">");
		return html.toString();
	}
	public String getActionButton(BasicClass o, String command, String args, String label){
		
		StringBuilder html = new StringBuilder();
		html.append("<input type=\"button\" class=\"actionbutton\" value=\"" + label + "\" onclick=\"onAction('" + o.getPath() + "','" + command + "'");
		if(args != null){
			html.append(",'" + args + "'");
			
		}
		html.append(")\">");
		return html.toString();
	}
}
