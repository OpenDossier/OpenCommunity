package ch.opencommunity.feedback;

import org.kubiki.base.BasicClass;
import org.kubiki.ide.BasicProcess;
import org.kubiki.gui.html.BaseObjectView;
import org.kubiki.gui.html.HTMLFormManager;
import org.kubiki.application.BasicUserSession;
import org.kubiki.application.ApplicationContext;
import org.kubiki.application.server.ApplicationServer;

import java.util.Map;

public class FeedbackFinalizeForm extends BaseObjectView{
	
	
	int mode = 1;
	
	
	public FeedbackFinalizeForm(){
		

		
	}
	public String toHTML(BasicClass bc, ApplicationContext context, BasicUserSession userSession, boolean isEditable){
		
		ApplicationServer server = (ApplicationServer)bc.getRoot();
		
		HTMLFormManager formManager = server.getFormManager();
		
		FeedbackFinalize process = (FeedbackFinalize)bc;
		
		StringBuilder html = new StringBuilder();
				
		html.append("<form id=\"processNodeForm\">");
		
		html.append("<table>");
		
		if(process.getOrganisationMemberInfo() != null){
			html.append("<tr><td>Beurteilter Benutzer : </td><td>" + process.getOrganisationMemberInfo().getInfoString() + "</td></tr>");
		}
		
		html.append("<tr><td>Nachname</td><td><input name=\"Familyname\" value=\"" + process.getString("Familyname") + "\" onkeyup=\"searchFeedbackTarget()\"></td></tr>");
		html.append("<tr><td>Vorname</td><td><input name=\"Firstname\" value=\"" + process.getString("Firstname") + "\" onkeyup=\"searchFeedbackTarget()\"></td></tr>");		
		html.append("<tr><td>Strasse</td><td><input name=\"Street\" value=\"" + process.getString("Street") + "\" onkeyup=\"searchFeedbackTarget()\"></td></tr>");	
		html.append("<tr><td>Ort</td><td><input name=\"City\" value=\"" + process.getString("City") + "\" onkeyup=\"searchFeedbackTarget()\"></td></tr>");	
		
		html.append("<tr><td colspan=\"2\"><div id=\"searchResults\">");
		html.append(process.getSearchResults(process.getString("Familyname"), process.getString("Firstname"), process.getString("Street"), process.getString("City"), process.getString("OrganisationMember")));
		html.append("</div></td></tr>");
		
		formManager.addFormTableRow(html, process.getProperty("ContactEstablished"), false, "", "Einsatz zustandegekommen", 5);
		
		if(process.getBoolean("ContactEstablished")){
			
			html.append("<tr><td>Wie war der Einsatz?</td></tr>");
			
			html.append("<tr><td colspan=\"2\">");
			
			int selectedValue = process.getID("ContactQuality");
			
			html.append("<input type=\"radio\" name=\"ContactQuality\" value=\"1\"" + isChecked(selectedValue, 1) + " DISABLED>gar nicht gut &nbsp;");
			html.append("<input type=\"radio\" name=\"ContactQuality\" value=\"2\"" + isChecked(selectedValue, 2) + " DISABLED>es geht so &nbsp;");
			html.append("<input type=\"radio\" name=\"ContactQuality\" value=\"3\"" + isChecked(selectedValue, 3) + " DISABLED>gut &nbsp;");
			
			html.append("</td></tr>");
			
			html.append("<tr><td colspan=\"2\">");
			
			html.append("Bitte beschreiben Sie möglichst objektiv den Einsatz.");

			
			html.append("</td></tr>");
			
			html.append("<tr><td colspan=\"2\">");
			
			html.append(formManager.getTextArea(process.getProperty("ContactDescription"), false, ""));
			
			html.append("</td></tr>");			
			
			
		}
		else{
			
			html.append("<tr><td>Grund</td><td>");
			
			Map reasons = FeedbackRecord.getReason();
			Map reasonDetails = FeedbackRecord.getReasonDetails();
			
			String contactNotEstablishedReason = process.getString("ContactNotEstablishedReason");
			Integer[] contactNotEstablishedReasonDetail = (Integer[])process.getObject("ContactNotEstablishedReasonDetail");
			
			html.append("" + reasons.get(contactNotEstablishedReason));
			for(Integer i : contactNotEstablishedReasonDetail){
				html.append("<li>" + reasonDetails.get(i.toString()));
			}
			html.append("</td></tr>");			
			html.append("<tr><td colspan=\"2\">");
			
			html.append("Bemerkungen");

			
			html.append("</td></tr>");
			
			html.append("<tr><td colspan=\"2\">");
			
			html.append(formManager.getTextArea(process.getProperty("Comment"), true, ""));
			
			html.append("</td></tr>");
			
			formManager.addFormTableRow(html, process.getProperty("FollowupNeeded"), true, "", "Muss überprüft werden", 5);
			
			
			
		}
			
		html.append("</table>");
		
		html.append("</form>");
		
		html.append("<input type=\"button\" value=\"Abschliessen\" onclick=\"getNextNode()\">");		
		
		return html.toString();
		
		
		
	}
	private String isChecked(int selectedValue, int value){
		if(selectedValue==value){
			return " CHECKED";	
		}
		else{
			return "";	
		}
	}
	
}