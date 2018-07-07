package ch.opencommunity.feedback;

import org.kubiki.base.BasicClass;
import org.kubiki.ide.BasicProcess;
import org.kubiki.gui.html.BaseObjectView;
import org.kubiki.gui.html.HTMLFormManager;
import org.kubiki.application.BasicUserSession;
import org.kubiki.application.ApplicationContext;
import org.kubiki.application.server.ApplicationServer;

public class FeedbackAddForm extends BaseObjectView{
	
	
	int mode = 1;
	
	
	public FeedbackAddForm(){
		

		
	}
	public String toHTML(BasicClass bc, ApplicationContext context, BasicUserSession userSession, boolean isEditable){
		
		ApplicationServer server = (ApplicationServer)bc.getRoot();
		
		HTMLFormManager formManager = server.getFormManager();
		
		FeedbackAdd process = (FeedbackAdd)bc;
		
		StringBuilder html = new StringBuilder();
		
		if(process.getPage()==1){
		
			html.append("<form id=\"processNodeForm\">");
			
			html.append("<table>");
			
			html.append("<tr><td colspan=\"2\">" + process.getComment() + "</td></tr>" );
			
			html.append("<tr><td colspan=\"2\">");
			
			
			html.append("Das Feedback gilt einer Person von der man die Adresse via NachbarNet erhalten hat."); 
			html.append("<p>Feedbacks werden nicht veröffentlicht und dienen dem NachbarNet dazu die Daten zu aktualisieren und NachbarNet Nutzerinnen und Nutzer zu verifizieren. In Streitfällen,  um Objektivität zu bewahren, halten wir Rücksprache mit der anderen Person."); 
			html.append("<p>Füllen Sie bitte mindestens 2 Felder aus, damit wir diese Person in unserer Datenbank finden können:");

			
			
			
			html.append("</td></tr>");
			
			formManager.addFormTableRow(html, process.getProperty("Familyname"), true, "", "Nachname", 1);
			formManager.addFormTableRow(html, process.getProperty("Firstname"), true, "", "Vorname", 1);
			formManager.addFormTableRow(html, process.getProperty("Street"), true, "", "Strasse", 1);
			formManager.addFormTableRow(html, process.getProperty("City"), true, "", "Ort", 1);
			formManager.addFormTableRow(html, process.getProperty("ContactEstablished"), true, "", "Ist ein Einsatz zustandegekommen?", 5);
			
			html.append("</table>");
			
			html.append("</form>");
			
			html.append("<input type=\"button\" value=\"Weiter\" onclick=\"getNextNode()\">");
			
		}
		else if(process.getPage()==2){		
			
			html.append("<form id=\"processNodeForm\">");
			
			html.append("<table>");
			
			html.append("<tr><td colspan=\"2\">" + process.getComment() + "</td></tr>" );
			
			html.append("<tr><td>Wie war der Einsatz?</td></tr>");
			
			html.append("<tr><td colspan=\"2\">");
			
			html.append("<input type=\"radio\" name=\"ContactQuality\" value=\"1\"><span class=\"quality1\">gar nicht gut &nbsp;</span>");
			html.append("<input type=\"radio\" name=\"ContactQuality\" value=\"2\"><span class=\"quality2\">es geht so &nbsp;</span>");
			html.append("<input type=\"radio\" name=\"ContactQuality\" value=\"3\"><span class=\"quality3\">gut &nbsp;</span>");
			
			html.append("</td></tr>");
			
			html.append("<tr><td colspan=\"2\">");
			
			html.append("Bitte beschreiben Sie möglichst objektiv den Einsatz.");

			
			html.append("</td></tr>");
			
			html.append("<tr><td colspan=\"2\">");
			
			html.append(formManager.getTextArea(process.getProperty("ContactDescription"), true, ""));
			
			html.append("</td></tr>");
			
			
			html.append("</table>");
			
			html.append("</form>");			
			
			html.append("<input type=\"button\" value=\"Abschliessen\" onclick=\"getNextNode()\">");			
		}
		else if(process.getPage()==3){		
			
			html.append("<form id=\"processNodeForm\">");
			
			html.append("<table>");
			
			html.append("<tr><td colspan=\"2\">" + process.getComment() + "</td></tr>" );
			
			html.append("<tr>");
			html.append("<td>");
			html.append("<input id=\"feedback_radio_1\" type=\"radio\" name=\"ContactNotEstablishedReason\" value=\"1\" onchange=\"toggleFormSubitems(this.value)\">Nicht erreicht");
			html.append("</td>");
			html.append("<td>");
			html.append("<div class=\"subitemcontainer\" id=\"subitems_1\" style=\"display : none;\">");
			html.append("Kontaktversuch per Telefon:");
			html.append("<br><input type=\"checkbox\" name=\"ContactNotEstablishedReasonDetail\" value=\"11\">mehrmals probiert ohne Erfolg");
			html.append("<br><input type=\"checkbox\" name=\"ContactNotEstablishedReasonDetail\" value=\"12\">Combox/ Anrufbeantworter mit Bitte um Rückruf");
			html.append("<br><input type=\"checkbox\" name=\"ContactNotEstablishedReasonDetail\" value=\"13\">Fehlermeldung/ ungültige Rufnummer");
			html.append("<br><input type=\"checkbox\" name=\"ContactNotEstablishedReasonDetail\" value=\"14\">anderes");
			html.append("<br>Kontaktversuch per Email:");
			html.append("<br><input type=\"checkbox\" name=\"ContactNotEstablishedReasonDetail\" value=\"15\">E-Mail ungültig");
			html.append("<br><input type=\"checkbox\" name=\"ContactNotEstablishedReasonDetail\" value=\"16\">Keine Antwort");
			html.append("</div>");
			html.append("</td>");		
			html.append("</tr>");	
			
			html.append("<tr>");
			html.append("<td>");
			html.append("<input id=\"feedback_radio_2\" type=\"radio\" name=\"ContactNotEstablishedReason\" value=\"2\" onchange=\"toggleFormSubitems(this.value)\">Passt nicht");
			html.append("</td>");
			html.append("<td>");
			html.append("<div class=\"subitemcontainer\" id=\"subitems_2\" style=\"display : none;\">");
			html.append("<br><input type=\"checkbox\" name=\"ContactNotEstablishedReasonDetail\" value=\"21\">passt fachlich nicht");
			html.append("<br><input type=\"checkbox\" name=\"ContactNotEstablishedReasonDetail\" value=\"22\">unterschiedliche Persönlichkeiten");
			html.append("<br><input type=\"checkbox\" name=\"ContactNotEstablishedReasonDetail\" value=\"23\">sprachliche Barriere/ Kommunikationsschwierigkeiten");
			html.append("<br><input type=\"checkbox\" name=\"ContactNotEstablishedReasonDetail\" value=\"24\">kein guter Eindruck beim Erstkontakt");
			html.append("<br><input type=\"checkbox\" name=\"ContactNotEstablishedReasonDetail\" value=\"25\">unangepasstes Benehmen");
			html.append("<br><input type=\"checkbox\" name=\"ContactNotEstablishedReasonDetail\" value=\"26\">anderes");
			html.append("</div>");
			html.append("</td>");		
			html.append("</tr>");		
			
			html.append("<tr>");
			html.append("<td>");
			html.append("<input id=\"feedback_radio_3\" type=\"radio\" name=\"ContactNotEstablishedReason\" value=\"3\" onchange=\"toggleFormSubitems(this.value)\">Unzuverlässig");
			html.append("</td>");
			html.append("<td>");
			html.append("<div class=\"subitemcontainer\" id=\"subitems_3\" style=\"display : none;\">");
			html.append("<br><input type=\"checkbox\" name=\"ContactNotEstablishedReasonDetail\" value=\"31\">am vereinbarten Termin nicht erschienen/ nicht Zuhause");
			html.append("<br><input type=\"checkbox\" name=\"ContactNotEstablishedReasonDetail\" value=\"32\">vereinbarten Termin kurzfristig abgesagt");
			html.append("<br><input type=\"checkbox\" name=\"ContactNotEstablishedReasonDetail\" value=\"33\">anderes");
			html.append("</div>");
			html.append("</td>");		
			html.append("</tr>");	
			
			html.append("<tr>");
			html.append("<td>");
			html.append("<input id=\"feedback_radio_4\" type=\"radio\" name=\"ContactNotEstablishedReason\" value=\"4\" onchange=\"toggleFormSubitems(this.value)\">Keine Zeit");
			html.append("</td>");
			html.append("<td>");
			html.append("<div class=\"subitemcontainer\" id=\"subitems_4\" style=\"display : none;\">");
			html.append("<br><input type=\"checkbox\" name=\"ContactNotEstablishedReasonDetail\" value=\"41\">Keine übereinstimmende Termine gefunden");
			html.append("<br><input type=\"checkbox\" name=\"ContactNotEstablishedReasonDetail\" value=\"42\">momentan keine Zeit");
			html.append("<br><input type=\"checkbox\" name=\"ContactNotEstablishedReasonDetail\" value=\"43\">keine Zeit für neue Engagements");
			html.append("<br><input type=\"checkbox\" name=\"ContactNotEstablishedReasonDetail\" value=\"44\">anderes");
			html.append("</div>");
			html.append("</td>");		
			html.append("</tr>");	
			
			html.append("<tr><td colspan=\"2\">");
			
			html.append("Bemerkungen");

			
			html.append("</td></tr>");
			
			html.append("<tr><td colspan=\"2\">");
			
			html.append(formManager.getTextArea(process.getProperty("Comment"), true, ""));
			
			html.append("</td></tr>");
			
			html.append("</table>");
			
			html.append("</form>");					
				
			
			html.append("<input type=\"button\" value=\"Abschliessen\" onclick=\"getNextNode()\">");			
		}
		
		
		return html.toString();
		
	}
	
	
	
}