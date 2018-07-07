package ch.opencommunity.view;

import ch.opencommunity.advertising.*;
import ch.opencommunity.base.*;
import ch.opencommunity.process.*;
import ch.opencommunity.server.OpenCommunityServer;
import ch.opencommunity.server.HTMLForm;
import ch.opencommunity.dossier.*;

import org.kubiki.servlet.HtmlFieldWidget;
import org.kubiki.base.BasicClass;
import org.kubiki.base.Property;
import org.kubiki.base.ObjectCollection;
import org.kubiki.application.ApplicationContext;
import org.kubiki.util.DateConverter;
import org.kubiki.cms.FileObject;

import java.util.Vector;

public class ProjectView extends HtmlFieldWidget{
	
	static String[] status = {"offen", "erledigt"};
	
	
	public String renderWidget(BasicClass bc, Property p, boolean isEditable, ApplicationContext context, String prefix){
		StringBuilder html = new StringBuilder();
		
		return html.toString();
	}
	
	
	public static void toHTML(StringBuilder html, DossierController dossierController, Project project, ApplicationContext context){
		
		html.append("<div class=\"tabletoolbar\" style=\"margin-top : 30px;\">");
		html.append("<input class=\"labelbutton\" type=\"button\" value=\"Projekt " + project.getString("Title") + "\">");
		if(project.getNumericID("Status")==0){
			html.append("<input class=\"actionbutton\" type=\"button\" value=\"Projekt abschliessen\" onclick=\"onAction('" + project.getPath() + "','projectfinalize')\">");
		}
		html.append("</div>");
		
		html.append("<div id=\"objecttabs\">");
		html.append("<table><tr>");
		html.append("<td class=\"formtab2\" onclick=\"selectFormTab(this)\"><div id=\"tab_showobjectlist\" class=\"formtab2_active\" onclick=\"onAction('" + dossierController.getPath() + "','showprojectjournal', '', 'projectid=" + project.getName() + "')\">Journal</div></td>");
		html.append("<td class=\"formtab2\" onclick=\"selectFormTab(this)\"><div id=\"tab_showemaillist\" class=\"formtab2\" onclick=\"onAction('" + dossierController.getPath() + "','showprojectemails', '', 'projectid=" + project.getName() + "')\">Email</div></td>");
		html.append("<td class=\"formtab2\" onclick=\"selectFormTab(this)\"><div id=\"tab_showjournal\" class=\"formtab2\" onclick=\"onAction('" + dossierController.getPath() + "','showprojectdetails','','projectid=" + project.getName() + "')\">Details</div></td>");	
		html.append("</tr></table>");
		html.append("</div>");
		
		/*
		html.append("<div class=\"tabletoolbar\" style=\"margin-top : 30px;\">");	
		html.append("<input class=\"labelbutton\" type=\"button\" value=\"Projektdetails\">");
		html.append("<input type=\"button\" class=\"actionbutton\" value=\"Speichern\" onclick=\"saveObject('" + project.getPath() + "')\">");	
		html.append("</div>");
		
		html.append("<form action=\"servlet\" id=\"objectEditForm_" + project.getPath() + "\">");
			
		html.append("\n<input type=\"hidden\" name=\"action\" value=\"getObjectAction\">");	
		html.append("\n<input type=\"hidden\" name=\"command\" value=\"saveproject\">");	
		html.append("\n<input type=\"hidden\" name=\"activityid\" value=\"" + project.getName() + "\">");
		html.append("\n<input type=\"hidden\" name=\"projectPath\" value=\"" + project.getPath("") + "\">");
		html.append("\n<input type=\"hidden\" name=\"objectPath\" value=\"" + dossierController.getPath("") + "\">");
			
			
		html.append("<table>");
			html.append("<tr><th>Bezeichnung</th><td>" + HTMLForm.getTextField(project.getProperty("Title"), true, "") + "</td></tr>");
			html.append("<tr><th>Beschreibung</th><td>" + HTMLForm.getTextArea(project.getProperty("Description"), true, "") + "</td></tr>");
			html.append("<tr><th>Status</th><td>" + HTMLForm.getSelection(project.getProperty("Status"), true, "") + "</td></tr>");
			
		html.append("</table>");	
				
		html.append("</form>");
		
		*/
		
		html.append("<div id=\"project_" + project.getPath() + "\">");
		
		html.append(getProjectJournal(dossierController, project, null));
		
		html.append("</div>");
		
		
	}
	public static String getProjectEmail(OpenCommunityServer ocs, DossierController dossierController, Project project, String filter){
		
		StringBuilder html = new StringBuilder();
		
		ObjectCollection results = new ObjectCollection("Results", "*");
		String sql = "SELECT ID, Sender, Recipients, DateSent, Subject FROM MailMessageInstance WHERE ProjectID=" + project.getName();
						
		ocs.queryData(sql, results);
							

		html.append("<table>");
		
		html.append("<tr><td class=\"tableheader\">Datum</td><td class=\"tableheader\">Betreff</td><td class=\"tableheader\">Absender</td><td class=\"tableheader\">Empfänger</td></tr>");
								
		for(BasicClass record : results.getObjects("Results")){
									
			html.append("<tr>");
									
			html.append("<td>" + DateConverter.sqlToShortDisplay(record.getString("DATESENT"), true ) + "</td>");
			html.append("<td>" + record.getString("SUBJECT") + "</td>");
			html.append("<td>" + record.getString("SENDER") + "</td>");
			html.append("<td>" + record.getString("RECIPIENTS") + "</td>");
			html.append("<td><a href=\"javascript:downloadMailMessage(" + record.getString("ID") + ")\">Oeffnen</a></td>");
									
			html.append("</tr>");
									
		}
								
		html.append("</table>");

	
		return html.toString();
	}	
	public static String getProjectJournal(DossierController dossierController, Project project, String filter){
		
		StringBuilder html = new StringBuilder();
		
		html.append("<div class=\"tabletoolbar\" style=\"margin-top : 30px;\">");
		html.append("<input class=\"labelbutton\" type=\"button\" value=\"Projektjournal\">");
		
		html.append("Filtern nach");
		html.append(HTMLForm.getSelection(dossierController.getProperty("objecttemplate"), "",  "", true, true, true, "", "onAction('" + dossierController.getPath() + "','activityfilter', '','projectid=" + project.getName() + "&objecttemplate=' + this.value )"));
		
		html.append("Neue Aktivität:");
		html.append(HTMLForm.getSelection(dossierController.getProperty("objecttemplate"), "",  "", true, true, true, "", "onAction('" + dossierController.getPath() + "','activityadd', '','projectid=" + project.getName() + "&objecttemplate=' + this.value )"));
		html.append("</div>");	
		
		html.append("<div id=\"projectjournal_" + project.getPath() + "\">");

		html.append("<table>");
						
		html.append("<tr><td class=\"tableheader\">Datum</td><td class=\"tableheader\">Vorlage</td><td class=\"tableheader\">Betreff</td><td class=\"tableheader\">Status</td></tr>");
						

		boolean even = false;
			
		for(int j = project.getObjects("Activity").size()-1; j >= 0; j--){
			BasicClass bc = project.getObjects("Activity").get(j);
				boolean include = true;
				if(filter != null && filter.equals("" + bc.getID("Template"))==false){
					include = false;
				}
				if(include){
					if(even){
						html.append("<tr class=\"even\" onClick=\"highlightRow(this)\">");
					}
					else{
						html.append("<tr class=\"odd\" onClick=\"highlightRow(this)\">");						
					}
								
								
					html.append("<td class=\"datacell\">" + DateConverter.sqlToShortDisplay(bc.getString("DateCreated"), false) + "</td>");
					html.append("<td class=\"datacell\">" + bc.getString("Template") + "</td>");
	
					html.append("<td class=\"datacell\">" + bc.getString("Title") + "</td>");
	
					
					if(bc.getProperty("Status").getValues() == null){
						html.append("<td class=\"datacell\">" + status[bc.getID("Status")] + "</td>");
					}
					else{
						html.append("<td class=\"datacell\">" + bc.getString("Status") + "</td>");
					}	
					
					html.append("<td class=\"datacell\"><a href=\"javascript:onAction('" + dossierController.getPath() + "','editactivity', '',\'projectid=" + project.getName() + "&activityid=" + bc.getName() + "\')\"><img src=\"images/edit.png\"></a></td>");
					html.append("</tr>");
					Note note = (Note)bc.getObjectByIndex("Note", 0);
					if(note != null){
									
						if(even){
							html.append("<tr class=\"even\">");
						}
						else{
							html.append("<tr class=\"odd\">");						
						}
						html.append("<td class=\"datacell\"></td><td colspan=\"3\" class=\"datacell\">");
						String content = note.getString("Content").trim();
						String[] lines = content.split("\r\n|\r|\n");
						int n = 3;
						if(lines.length < n) n = lines.length;
						for(int i = 0; i < n; i++){
							html.append(lines[i] + "<br>");	
						}
									
						html.append("</td></tr>");
									
						}
						even = !even;
						
				}
		   }
		html.append("</table>");
		   
		html.append("</div>");
		   
		return html.toString();
			
			
	}
	public static String getProjectDetails(DossierController dossierController, Project project){
		
		StringBuilder html = new StringBuilder();
		
		html.append("<div class=\"tabletoolbar\" style=\"margin-top : 30px;\">");	
		html.append("<input class=\"labelbutton\" type=\"button\" value=\"Projektdetails\">");
		html.append("<input type=\"button\" class=\"actionbutton\" value=\"Speichern\" onclick=\"saveObject('" + project.getPath() + "')\">");	
		html.append("</div>");
		
		html.append("<form action=\"servlet\" id=\"objectEditForm_" + project.getPath() + "\">");
			
		html.append("\n<input type=\"hidden\" name=\"action\" value=\"getObjectAction\">");	
		html.append("\n<input type=\"hidden\" name=\"command\" value=\"saveproject\">");	
		html.append("\n<input type=\"hidden\" name=\"activityid\" value=\"" + project.getName() + "\">");
		html.append("\n<input type=\"hidden\" name=\"projectPath\" value=\"" + project.getPath("") + "\">");
		html.append("\n<input type=\"hidden\" name=\"objectPath\" value=\"" + dossierController.getPath("") + "\">");
			
			
		html.append("<table>");
		html.append("<tr><th>Bezeichnung</th><td>" + HTMLForm.getTextField(project.getProperty("Title"), true, "") + "</td></tr>");
		html.append("<tr><th>Beschreibung</th><td>" + HTMLForm.getTextArea(project.getProperty("Description"), true, "") + "</td></tr>");
		html.append("<tr><th>Status</th><td>" + HTMLForm.getSelection(project.getProperty("Status"), true, "") + "</td></tr>");
			
		html.append("</table>");
		
				
		return html.toString();
	}
		
}