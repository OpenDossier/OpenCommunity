package ch.opencommunity.dossier;

import ch.opencommunity.base.BasicOCObject;
import ch.opencommunity.base.OrganisationalUnit;
import ch.opencommunity.base.Address;
import ch.opencommunity.base.Parameter;
import ch.opencommunity.base.Note;
import ch.opencommunity.common.OpenCommunityUserSession;

import org.kubiki.base.BasicClass;
import org.kubiki.base.ActionHandler;
import org.kubiki.base.ActionResult;
import org.kubiki.base.Property;
import org.kubiki.util.DateConverter;


import org.kubiki.application.ApplicationContext;
import org.kubiki.application.server.WebApplicationContext;
import org.kubiki.application.server.ApplicationServer;
import org.kubiki.application.DataObject;

import java.util.List;
import java.util.Enumeration;

public class DossierInfo extends BasicClass{
	
	static String[] statusdef = {"offen", "abgeschlossen", "abgelegt"};
	static String[] projectTypes ={"Spendengesuch", "Unterstütztes Projekt"};
	
	public DossierInfo(){
	
		addProperty("Title", "String", "", false, "Bezeichnung");
			
		
		addObjectCollection("Projects", "*");
		
	}
	public String toHTML(){
		
		StringBuilder html = new StringBuilder();
		
		html.append("<table>");
		
		html.append("<tr><td>Institution</td><td>" + getString("Title") + "</td></tr>");
		
		
		html.append("</table>");
		
		html.append("<h4>Projekte</h4>");
		
		html.append("<table>");
		
		html.append("<tr>");
		html.append("<td class=\"tableheader\">Organisation</td>");
		html.append("<td class=\"tableheader\">Bezeichnung</td>");
		html.append("<td class=\"tableheader\">Typ</td>");
		html.append("<td class=\"tableheader\">Datum Beginn</td>");
		html.append("<td class=\"tableheader\">Datum Gesuch</td>");
		html.append("<td class=\"tableheader\">Datum Antwort</td>");
		html.append("<td class=\"tableheader\">Ergebnis</td>");
		html.append("<td class=\"tableheader\">Status</td>");	
		html.append("<td class=\"tableheader\">Datum Abschluss</td>");
		html.append("</tr>");	
		
		boolean even = false;
		
		for(BasicClass record : getObjects("Projects")){
			
			if(even){
				html.append("<tr class=\"even\">");
			}
			else{
				html.append("<tr class=\"odd\">");					
			}
			even = !even;

			html.append("<td class=\"datacell\"><a href=\"javascript:openDossier(" + record.getString("DOSSIERID") + ")\">" + record.getString("ORGANISATION") + "</a></td>");
			html.append("<td class=\"datacell\">" + record.getString("TITLE") + "</td>");
			html.append("<td class=\"datacell\">" + projectTypes[record.getID("TYPE")] + "</td>");
			html.append("<td class=\"datacell\">" + DateConverter.sqlToShortDisplay(record.getString("DATESTARTED"), false) + "</td>");
			html.append("<td class=\"datacell\">" + DateConverter.sqlToShortDisplay(record.getString("DATE1"), false) + "</td>");
			html.append("<td class=\"datacell\">" + DateConverter.sqlToShortDisplay(record.getString("DATE2"), false) + "</td>");
			html.append("<td class=\"datacell\">" + record.getString("RESULT") + "</td>");
			html.append("<td class=\"datacell\">" + statusdef[record.getID("STATUS")] + "</td>");
			html.append("<td class=\"datacell\">" + DateConverter.sqlToShortDisplay(record.getString("DATE3"), false) + "</td>");
			html.append("</tr>");	
			
			
		}
		
		
		html.append("</table>");		
		
		html.append("<input type=\"button\" class=\"actionbutton\" onClick=\"hidePopup()\" value=\"Schliessen\">");
		
		return html.toString();
		
	}
	
	
	
}