package ch.opencommunity.view;

import ch.opencommunity.common.OpenCommunityUserSession;
import ch.opencommunity.base.OrganisationalUnit;
import ch.opencommunity.base.Person;
import ch.opencommunity.base.Identity;
import ch.opencommunity.base.Address;
import ch.opencommunity.base.Login;
import ch.opencommunity.base.Note;
import ch.opencommunity.base.Activity;

import ch.opencommunity.dossier.*;

import ch.opencommunity.base.OrganisationMember;
import ch.opencommunity.base.OrganisationMemberController;

import ch.opencommunity.server.HTMLForm;
import ch.opencommunity.server.OpenCommunityServer;
import org.kubiki.application.ApplicationContext;

import org.kubiki.base.Property;
import org.kubiki.base.ObjectCollection;
import org.kubiki.base.BasicClass;
import org.kubiki.util.DateConverter;

import java.util.Hashtable;
import java.util.Map;

public class DossierView extends BaseView{
	
	static String[] contacts = {"Tel. privat","Tel. gesch.","Tel. mobil","Email", "Fax", "Web"};
	static String[] projectTypes ={"Spendengesuch", "Unterstütztes Projekt"};
	
	public static String getDossierView(OpenCommunityUserSession userSession, DossierController dossierController, ApplicationContext context){
		
		Dossier dossier = dossierController.getDossier();
		
		OpenCommunityServer ocs = (OpenCommunityServer)dossier.getRoot();
		
		StringBuilder html = new StringBuilder();
		

		
		html.append("<div id=\"toolbar2\">");
		
		html.append("<img class=\"toolbaricon\" src=\"images/save.png\" onclick=\"saveObject('" + dossier.getPath() + "')\" onmouseover=\"showTooltip(event, 'Speichern')\" onmouseout=\"hideTooltip()\">");
		
		html.append("</div>");
		
		html.append("<div class=\"organisationmemberforml\">");
		
		html.append("<div class=\"organisationmemberdetails\">");
				

		
		html.append("<form action=\"servlet\" id=\"objectEditForm_" + dossier.getPath() + "\">");
		html.append("<table>");
		if(dossier != null){
					
			html.append("<input type=\"hidden\" name=\"action\" value=\"getObjectAction\">");
			html.append("<input type=\"hidden\" name=\"command\" value=\"saveobject\">");
			html.append("<input type=\"hidden\" name=\"objectPath\" value=\"" + dossier.getPath() + "\">");
			
			if(dossier.getObject("OrganisationalUnit") instanceof OrganisationalUnit){
				
				OrganisationalUnit ou = (OrganisationalUnit)dossier.getObject("OrganisationalUnit");
			
				addTableRow(html, ou.getProperty("Title"), "Bezeichnung", "organisation_", 1);
				addTableRow(html, ou.getProperty("Type"), "Typ Organisation", "organisation_", 2);
				//addTableRow(html, ou.getProperty("Description"), "Beschreibung", "organisation_", 3);
				
				String sql = "SELECT t1.ID, t1.Function, t1.Title, t3.Familyname, t3.Firstname FROM OrganisationMember AS t1";
				sql += " LEFT JOIN Person AS t2 ON t2.ID=t1.Person";
				sql += " LEFT JOIN Identity AS t3 ON t3.PersonID=t2.ID";
				sql += " WHERE OrganisationalUnitID=" + ou.getName();
				
				
				
				ObjectCollection results = dossierController.getObjectCollection("organisationmembers");
				results.removeObjects();
				ocs.queryData(sql, results);
				
				

				
				Address address = ou.getAddress();
				if(address != null){
					addTableRow(html, address.getProperty("AdditionalLine"), "Zusatzzeile", "address_", 3);
					addTableRow(html, address.getProperty("POBox"), "Postfach", "address_", 1);
					addTableRow(html, address.getProperty("Street"), "Strasse", "address_", 1);
					addTableRow(html, address.getProperty("Number"), "Nummer", "address_", 1);		
					addTableRow(html, address.getProperty("Zipcode"), "PLZ", "address_", 1);		
					addTableRow(html, address.getProperty("City"), "Ort", "address_", 1);		
				}
				
			
				
		
		
				for(BasicClass bc : ou.getObjects("Contact")){
								//addTableRow(html, getProperty(contacts[bc.getInt("Type")]), contacts[bc.getInt("Type")]);
		
					html.append("<tr><td>" + contacts[bc.getInt("Type")] + "</td>");
					html.append("<td>" + HTMLForm.getTextField(bc.getProperty("Value"), true, "contact_" + bc.getName() + "_") + "</td><td><a href=\"javascript:getNextNode(\'deletecontact=true&contactid=" + bc.getName() + "\')\"><img src=\"images/delete.png\"></a></td></tr>");
				}
						
							
				html.append("<tr><th class=\"inputlabel\">Kontakt hinzufügen</th><td><select onchange=\"onAction('" + dossierController.getPath() + "','contactadd','','contactid=\' + this.value)\">");
				html.append("<option value=\"\"></option>");
				int i = 0;
				for(String contacttype : contacts){
								
					html.append("<option value=\"" + i + "\">" + contacttype + "</option>");
					i++;
							
				}
				html.append("</select></td></tr>");
				
				html.append("<tr><td>Mitglieder</td><td><input type=\"button\" value=\"Neues Mitglied\" onclick=\"onAction('" + dossierController.getPath() + "','organisationmemberadd')\"></td></tr>");
				
				Map functionMap = 	ocs.getFunctions().getCodeMap();	

				for(BasicClass person : results.getObjects()){
					
					
					
					html.append("<tr><td>" + functionMap.get(person.getString("FUNCTION")) + " " + person.getString("TITLE") + "</td>");

					html.append("<td><a href=\"javascript:editOrganisationMember(" + person.getString("ID") + ")\">" + person.getString("FIRSTNAME") + " " + person.getString("FAMILYNAME") + "</a>");
					
					html.append("<input type=\"radio\" name=\"organisation_MainContactPerson\" value=\"" + person.getString("ID") + "\"");
					
					if(ou.getString("MainContactPerson").equals(person.getString("ID"))){
						html.append(" CHECKED");			
					}
					html.append(">");
					
					html.append("</td></tr>");
					
				}
			}
			html.append("<table>");
			ObjectDetail objectDetail = (ObjectDetail)dossier.getObjectByIndex("ObjectDetail", 0);
			if(objectDetail != null){
				html.append(ObjectDetailView.toHTML(dossierController, objectDetail));			
			}
			html.append("</table>");

		}
		
		html.append("</table>");	
		
		html.append("</div>");
		
		
				
		html.append("<div id=\"activityList\" class=\"organisationmemberobjects\">");
		
		/*
		html.append("<div id=\"objecttabs\">");
		html.append("<table><tr>");
		html.append("<td class=\"formtab2\"><div id=\"tab_showobjectlist\" class=\"formtab2\" onclick=\"onAction('" + dossierController.getPath() + "','showobjectlist')\">Projekte</div></td>");
		html.append("<td class=\"formtab2\"><div id=\"tab_showjournal\" class=\"formtab2\" onclick=\"onAction('" + dossierController.getPath() + "','showjournal')\">Journal</div></td>");	
		html.append("</tr></table>");
		html.append("</div>");
		*/
		
		html.append("<div id=\"objectList\" style=\"top : 0px;\">");
				
		html.append(getProjectList(dossierController, dossier, context));
		
		html.append("</div>");
		
		html.append("</div>");
		
		
		
		html.append("<div id=\"objectEditArea\">");
		
		html.append("</div>");
		
		html.append("</div>");	
		
		html.append("\n<script language=\"javascript\">");
		
		html.append("\nalert('e');");
		
		//html.append("\nwindow.addEventListener('input', function(e){");
		//html.append("\nalert(e);");
		//html.append("\n}, false);");
		
		html.append("\n</script>");
		
		return html.toString();
	}
	public static String getProjectList(DossierController dossierController, Dossier dossier, ApplicationContext context){
		StringBuilder html = new StringBuilder();
		
		boolean even = true;
		

		

		html.append("<div class=\"tabletoolbar\" style=\"margin-top : 10px;\">");
		html.append("<input class=\"labelbutton\" type=\"button\" value=\"Projekte\">");			
		html.append("<input class=\"actionbutton\" type=\"button\" value=\"Neues Projekt\" onClick=\"onAction('" + dossierController.getPath() + "','projectadd')\">");
		html.append("</div>");
		
		html.append("<table>");
		
		html.append("<tr><td class=\"tableheader\">Bezeichnung</td><td class=\"tableheader\">Beschreibung</td><td class=\"tableheader\">Typ</td><td class=\"tableheader\">Datum Beginn</td><td class=\"tableheader\">Status</td></tr>");
		
		for(int j = dossier.getObjects("Project").size()-1; j >= 0; j--){
			BasicClass project = dossier.getObjects("Project").get(j);
			if(even){
				html.append("<tr class=\"even\" onClick=\"highlightRow(this)\">");
			}
			else{
				html.append("<tr class=\"odd\" onClick=\"highlightRow(this)\">");						
			}
			even = !even;
			
			html.append("<td class=\"datacell\">" + project.getString("Title") + "</td>");
			html.append("<td class=\"datacell\">" + project.getString("Description") + "</td>");
			html.append("<td class=\"datacell\">" + projectTypes[project.getID("Type")] + "</td>");
			html.append("<td class=\"datacell\">" + DateConverter.sqlToShortDisplay(project.getString("DateStarted"), false) + "</td>");
			html.append("<td class=\"datacell\">" + project.getObject("Status") + "</td>");
			html.append("<td class=\"datacell\"><a href=\"javascript:onAction('" + dossierController.getPath() + "','editproject', '',\'projectid=" + project.getName() + "\')\"><img src=\"images/edit.png\"></a></td>");
			
			html.append("</tr>");
			
			
		}
		
		html.append("</table>");
		
		html.append("<div id=\"projectEditArea\">");
		
		if(dossierController.project != null){
			ProjectView.toHTML(html, dossierController, dossierController.project, context);	
		}
		
		html.append("</div>");
		
		return html.toString();
	}
	public static String getActivityList(DossierController dossierController, Dossier dossier){
		
		StringBuilder html = new StringBuilder();
		
		boolean even = true;
		

		
		CaseRecord caseRecord = (CaseRecord)dossier.getObjectByIndex("CaseRecord", 0);
		
		

		
		if(caseRecord != null){

			html.append("<h4>Journal</h4>");
		
				
			String[] status = {"offen", "erledigt"};
						
			html.append("Neue Aktivität:");
			html.append(HTMLForm.getSelection(dossierController.getProperty("objecttemplate"), "",  "", true, true, true, "", "onAction('" + dossierController.getPath() + "','activityadd', '','objecttemplate=' + this.value )"));
						
			html.append("<div style=\"height : 100%; overflow : auto;\">");
						
			html.append("<table>");
						
			html.append("<tr><th>Datum</th><th>Vorlage</th><th>Betreff</th><th>Status</th></tr>");
						

						
			//for(BasicClass bc : om.getObjects("Activity")){
			for(int j = caseRecord.getObjects("Activity").size()-1; j >= 0; j--){
				BasicClass bc = caseRecord.getObjects("Activity").get(j);
				if(even){
					html.append("<tr class=\"even\">");
				}
				else{
					html.append("<tr class=\"odd\">");						
				}
							
							
				html.append("<td class=\"datacell\">" + DateConverter.sqlToShortDisplay(bc.getString("DateCreated"), false) + "</td>");
				html.append("<td class=\"datacell\">" + bc.getString("Template") + "</td>");
				//html.append("<td class=\"datacell\">" + bc.getString("Date") + "</td>");
				html.append("<td class=\"datacell\">" + bc.getString("Title") + "</td>");
				//html.append("<td class=\"datacell\">" + bc.getString("Title") + "</td>");
				
				if(bc.getProperty("Status").getValues() == null){
					html.append("<td class=\"datacell\">" + status[bc.getID("Status")] + "</td>");
				}
				else{
					html.append("<td class=\"datacell\">" + bc.getString("Status") + "</td>");
				}	
				
				html.append("<td class=\"datacell\"><a href=\"javascript:onAction('" + dossierController.getPath() + "','editactivity', '',\'activityid=" + bc.getName() + "\')\">Details</a></td>");
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
		   html.append("</table>");
		   
		   html.append("</div>");
		}
		
		return html.toString();
		
	}
	
	
}