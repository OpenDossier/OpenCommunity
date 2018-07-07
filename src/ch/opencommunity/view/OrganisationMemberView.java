package ch.opencommunity.view;

import ch.opencommunity.base.OrganisationMember;
import ch.opencommunity.base.OrganisationMemberController;
import ch.opencommunity.process.OrganisationMemberEdit;
import ch.opencommunity.base.Person;
import ch.opencommunity.base.Identity;
import ch.opencommunity.base.Address;
import ch.opencommunity.base.Activity;
import ch.opencommunity.base.Note;
import ch.opencommunity.base.Document;
import ch.opencommunity.base.Login;
import ch.opencommunity.server.HTMLForm;
import ch.opencommunity.server.OpenCommunityServer;
import ch.opencommunity.common.OpenCommunityUserProfile;
import ch.opencommunity.common.OpenCommunityUserSession;
import ch.opencommunity.advertising.MemberAdAdministration;
import ch.opencommunity.process.MemberAdDetail;

import org.kubiki.base.Property;
import org.kubiki.base.BasicClass;
import org.kubiki.base.ObjectCollection;
import org.kubiki.util.DateConverter;
import org.kubiki.application.ApplicationContext;

import java.util.Hashtable;

public class OrganisationMemberView{
	
	static String[] contacts = {"Tel. privat","Tel. gesch.","Tel. mobil","Email"};
	
	static String[] tabids = {"memberad", "feedback"};
	static String[] tablabels = {"&Uuml;bersicht", "Feedback"};

	public static String getOrganisationMemberView(OrganisationMemberEdit omedit, ApplicationContext context){
		
		OrganisationMemberController omc = omedit.getCurrentOrganisationMemberControler();
		OrganisationMember om = omc.getOrganisationMember();
		
		Person person = om.getPerson();
		Identity identity = person.getIdentity();
		Address address = person.getAddress();
		Login login = om.getLogin();
		
		Activity activity = omc.getCurrentActivity();
		
		OpenCommunityServer ocs = (OpenCommunityServer)om.getRoot();
		//HTMLForm formManager = ocs.getFormManager();
		StringBuilder html = new StringBuilder();
		
		

		html.append("<table style=\"width : 100%;\"><tr>");
		for(OrganisationMemberController omc2 : omedit.getOrganisationMemberControlers()){
			
			OrganisationMember om2 = omc2.getOrganisationMember();
			
			if(om2.getName().equals(om.getName())){
				html.append("<td class=\"tab\">");
				html.append("<div class=\"tab_active\">");
				html.append(om2.toString());
				html.append("<img src=\"images/close_small.png\" onclick=\"getNextNode('close=true&omid=" + om2.getName() + "')\">");
				html.append("</div></td>");					
			}
			else{
				html.append("<td class=\"tab\">");
				html.append("<div class=\"tab_inactive\">");
				html.append("<a href=\"javascript:getNextNode('OMID2=" + om2.getName() + "')\">" + om2 + "</a>");
				html.append("<img src=\"images/close_small.png\" onclick=\"getNextNode('close=true&omid=" + om2.getName() + "')\">");
				html.append("</div></td>");
			}
		}
		html.append("<td class=\"processtab_empty\">&nbsp;</td>");
		html.append("</tr></table>");
		
		try{
		
			if(omc.getMemberAd() != null){
				
				html.append("<input type=\"button\" value=\"Zurück\" onclick=\"getNextNode('overview=true')\">");
				html.append(MemberAdDetail.getMemberAdDetailForm(omc.getMemberAd(), true, context));
				
			}
			else if(omc.getCreateRequest()){
				
				MemberAdAdministration maa = ocs.getMemberAdAdministration();
				OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
				
				html.append("<div style=\"position : relative; min-height : 4000px; width : 100%; background : #363D45;\">");
				//html.append("<div style=\"position : relative; height : 95%; width : 100%; background : #363D45;\">");
				
				html.append(maa.getMemberAdSearchForm2(context, om, true));
				
				html.append("<div id=\"userprofile\" style=\"position : absolute;\">");
				
				html.append(OpenCommunityUserProfile.getMemoryList(ocs, userSession, true));
				
				html.append("</div>");
				
				html.append("</div>");
				
				/*
				
				html.append("<br><select name=\"category\" id=\"category\" class=\"selectbig\" onchange=\"reloadSearch(this.value)\">");
				html.append("<option value=\"0\">Was? Rubrik wählen</option>");
				for(BasicClass bc :  ocs.getMemberAdAdministration().getObjects("MemberAdCategory")){
					html.append("<option value=\"" + bc.getName() + "\">" + bc.getString("Title") + "</option>");			
				}
				html.append("</select>");
				
				html.append("<div id=\"searchresults\" style=\"position : absolute; width : 700px; top : 30px;\">");
				
				
				html.append("</div>");
				
				*/
				
			}	
			else if(activity != null){
				ActivityView.toHTML(html, omc, activity, context);
				/*
				if(activity.getID("Template")==7 || activity.getID("Template")==8){
					html.append("<input type=\"button\" value=\"PDF erstellen\" onclick=\"getNextNode('saveactivity=true&createpdf=true')\">");					
				}
				html.append("<form action=\"servlet\" id=\"processNodeForm\">");
				html.append("<table>");
				html.append("<tr><th>Bezeichnung</th><td>" + HTMLForm.getTextField(omc.getProperty("activity_title"), true, "") + "</td></tr>");
				
				int i = 1;
				for(BasicClass parameter : activity.getObjects("Parameter")){
					Property p = omc.getProperty("activity_parameter_" + i);
					if(p.getValues() != null){
						html.append("<tr><th>" + parameter.getString("Title") + "</th><td>" + HTMLForm.getSelection(omc.getProperty("activity_parameter_" + i), true, "") + "</td></tr>");
					}
					else if(parameter.getString("Title").equals("Dokument")){
						Document doc = (Document)om.getObjectByName("Document", p.getValue());
						if(doc != null){
							html.append("<tr><th>" + parameter.getString("Title") + "</th><td><a href=\"javascript:openDocument(" + p.getValue() + ")\">Oeffnen</a></td></tr>");
						}
					}
					else if(parameter.getString("Template").equals("28")){

						html.append("<tr><th>" + parameter.getString("Title") + " " + parameter.getString("Comment") + "</th><td><a href=\"javascript:openExternalDocument(" + parameter.getString("ExternalDocument") + ")\">Oeffnen</a></td></tr>");

					}
					else{
						html.append("<tr><th>" + parameter.getString("Title") + "</th><td>" + HTMLForm.getTextField(omc.getProperty("activity_parameter_" + i), true, "") + "</td></tr>");
					}
					//getParent().addProperty("activity_note_" + i, "String", "");
				}
				i = 1;
				for(BasicClass note : activity.getObjects("Note")){
					
					Property p = omc.getProperty("activity_note_" + i);
					
					String propertyid = HTMLForm.createId(p);
					
					html.append("<tr><td></td><td><select onchange=\"getTextblockContent(this.value,'" + propertyid + "')\">");
					
					for(BasicClass textblock : ocs.getTextblocks(1, 3)){
						html.append("<option value=\"" + textblock.getName() + "\">" + textblock.getString("Title") + "</option>");	
					}
					
					html.append("</select></td></tr>");
					html.append("<tr><th>" + note.getString("Title") + "</th><td>" + HTMLForm.getTextArea(p, true) + "</td></tr>");
	
				}
				try{
				html.append("<tr><td>Folgende Objekte sind mit diesem Brief verknüpft:</td></tr>");
				
				for(BasicClass bc : activity.getObjectCollection("ActivityObject").getObjects()){
					
					
					
					if(bc.getID("MemberAdID") > 0){
						html.append("<tr><td>Inserat " + bc.getID("MemberAdID") + "</td></tr>");	
					}
					else if(bc.getID("MemberAdRequestID") > 0){
						html.append("<tr><td>Adressbestellung " + bc.getID("MemberAdRequestID") + "</td></tr>");
					}
					else if(bc.getID("OrganisationMemberID") > 0){
						html.append("<tr><td>Benutzerprofil " + bc.getID("OrganisationMemberID") + "</td></tr>");	
					}
					
					
				}
				
				if(activity.getID("Template")==7 || activity.getID("Template")==8){
					
					html.append("<tr><th>Kontext</th><td>" + HTMLForm.getTextField(omc.getProperty("activity_context"), true, "") + "</td></tr>");
					
					html.append("<tr><td>Folgende Objekte haben noch keine schriftliche Benachrichtigung:</td></tr>");
					
					if(om.getID("NotificationStatus")==0){
						html.append("<tr><td>Benutzerregistrierung</td><td><a href=\"javascript:getNextNode('activityobjectadd=true&organisationmemberid=" +  om.getName() + "')\">Hinzufügen</a></td></tr>");
					}
					
					if(activity.getObjectCollection("Results")==null){
						activity.addObjectCollection("Results", "*");	
					}
					activity.getObjects("Results").clear();
					
					String sql = "SELECT ID, Title, DateCreated FROM MemberAd WHERE OrganisationMemberID=" + om.getName() + " AND NotificationStatus=0";
					ocs.queryData(sql, activity.getObjectCollection("Results"));
					for(BasicClass bc : activity.getObjectCollection("Results").getObjects()){
						html.append("<tr><td>" + bc.getString("TITLE") + " " + bc.getString("DATECREATED") + "</td><td><a href=\"javascript:getNextNode('activityobjectadd=true&memberadid=" +  bc.getString("ID") + "')\">Hinzufügen</a></td></tr>");	
					}
					
				}
				
				html.append("</table>");	
				}
				catch(java.lang.Exception e){
					ocs.logException(e);	
				}
				
				html.append("<input type=\"button\" value=\"Zur&uuml;ck\" onclick=\"getNextNode('overview=true')\">");
				html.append("<input type=\"button\" value=\"Speichern\" onclick=\"getNextNode('saveactivity=true')\">");
				if(activity.getID("Template")==1 || activity.getID("Template")==4){
					html.append("<input type=\"button\" value=\"Speichern und senden\" onclick=\"getNextNode('saveactivity=true&send=true')\">");
				}
	
				html.append("</form>");
				*/
			}
			else if(omc.getMessageWrapper() != null){
				html.append("<div style=\"position : relative; width : 600px;\">") ;                                                          
				html.append("<div>" + omc.getMessageWrapper().getSender() + "</div>");
				html.append("<div>" + omc.getMessageWrapper().getDateString() + "</div>");	
				html.append("<div>" + omc.getMessageWrapper().getSubject() + "</div>");
				html.append("<div>" + omc.getMessageWrapper().getMessageBody() + "</div>");
				html.append("</div>");
				html.append("<input type=\"button\" value=\"Zur&uuml;ck\" onclick=\"getNextNode('overview=true')\">");
				html.append("<input type=\"button\" value=\"Importieren\" onclick=\"getNextNode('createactivity=true')\">");
				
			}
			else if(omc.getActiveTab() != null && omc.getActiveTab().equals("journal")){
				html.append("<input type=\"button\" value=\"Zur&uuml;ck\" onclick=\"getNextNode('newtab=memberad')\">");
				html.append("<h4>Journal</h4>");
				
					String[] status = {"offen", "erledigt"};
					
					html.append("Neue Aktivität:");
					html.append(HTMLForm.getSelection(omedit.getProperty("objecttemplate"), "",  "", true, true, true, "", "getNextNode('objecttemplate=' + this.value )"));
					
					html.append("<div style=\"height : 100%; overflow : auto;\">");
					
					html.append("<table>");
					
					html.append("<tr><th>Datum</th><th>Vorlage</th><th>Betreff</th><th>Status</th></tr>");
					
					boolean even = true;
					
					//for(BasicClass bc : om.getObjects("Activity")){
					for(int j = om.getObjects("Activity").size()-1; j >= 0; j--){
						BasicClass bc = om.getObjects("Activity").get(j);
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
						html.append("<td class=\"datacell\">" + status[bc.getID("Status")] + "</td>");
						html.append("<td class=\"datacell\"><a href=\"javascript:getNextNode(\'activityid=" + bc.getName() + "\')\">Details</a></td>");
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
			}
			else if(omc.getActiveTab() != null && omc.getActiveTab().equals("emailimport")){
				html.append("<input type=\"button\" value=\"Zur&uuml;ck\" onclick=\"getNextNode('newtab=memberad')\">");
				html.append("<h4>Email Import</h4>");
					html.append("<div id=\"mailform\">");
			
			
			
					html.append("</div>");
			
					html.append("<script language=\"javascript\">");
					html.append("callEmailAdministration(\'mailcommand=loadmessages\');");
					html.append("</script>");				
				
			}
			else if(omc.getFileUpload()){
				html.append("<iframe src=\"servlet?action=getfileuploadform&omid=" + om.getName() + "\"></iframe>");	
				html.append("<input type=\"button\" onclick=\"getNextNode('overview=true')\" value=\"Abbrechen\">");
			}
			else{
				
				/*
				if(history.size() > 0){
					String[] info = (String[]) history.get(0);
					html.append("<a href=\"javascript:getNextNode('back=true')\"> " + info[1] + "<< </a>");	
				}
				*/
				html.append("<div id=\"toolbar\">");
				
				html.append("<img class=\"toolbaricon\" src=\"images/save.png\" onclick=\"getNextNode('save=true')\" onmouseover=\"showTooltip(event, 'Speichern')\" onmouseout=\"hideTooltip()\">");
				html.append("<img class=\"toolbaricon\" src=\"images/journal.png\" onclick=\"getNextNode('newtab=journal')\" onmouseover=\"showTooltip(event, 'Journal')\" onmouseout=\"hideTooltip()\">");
				html.append("<img class=\"toolbaricon\" src=\"images/sendmail.png\" onclick=\"getNextNode('objecttemplate=1')\" onmouseover=\"showTooltip(event, 'Email schreiben')\" onmouseout=\"hideTooltip()\">");
				html.append("<img class=\"toolbaricon\" src=\"images/sendmail.png\" onclick=\"getNextNode('sendcredentials=true')\" onmouseover=\"showTooltip(event, 'Zugangsdaten zustellen')\" onmouseout=\"hideTooltip()\">");
				
				html.append("<img class=\"toolbaricon\" src=\"images/word.png\" onclick=\"getNextNode('objecttemplate=2')\" onmouseover=\"showTooltip(event, 'Worddokument erstellen')\" onmouseout=\"hideTooltip()\">");				
				html.append("<img class=\"toolbaricon\" src=\"images/pdf.png\" onclick=\"getNextNode('objecttemplate=7')\" onmouseover=\"showTooltip(event, 'PDF-Brief erstellen')\" onmouseout=\"hideTooltip()\">");
				html.append("<img class=\"toolbaricon\" src=\"images/pdf.png\" onclick=\"getNextNode('createnotification=true')\" onmouseover=\"showTooltip(event, 'Mitglied brieflich benachrichtigen')\" onmouseout=\"hideTooltip()\">");
				html.append("<img class=\"toolbaricon\" src=\"images/emailimport.png\" onclick=\"getNextNode('newtab=emailimport')\" onmouseover=\"showTooltip(event, 'Email importieren')\" onmouseout=\"hideTooltip()\">");
				
				html.append("<img class=\"toolbaricon\" src=\"images/www.png\" onclick=\"openWebsite('" + om.getString("AccessCode") + "','" + om.getPath()+ "')\" onmouseover=\"showTooltip(event, 'Benutzer auf Website ansehen')\" onmouseout=\"hideTooltip()\">");
				
				html.append("<img class=\"toolbaricon\" src=\"images/feedback.png\" onclick=\"getNextNode('feedback=true')\" onmouseover=\"showTooltip(event, 'Feedback')\" onmouseout=\"hideTooltip()\">");
				html.append("<img class=\"toolbaricon\" src=\"images/fileupload.png\" onclick=\"getNextNode('fileupload=true')\" onmouseover=\"showTooltip(event, 'Datei hochladen')\" onmouseout=\"hideTooltip()\">");
				html.append("<img class=\"toolbaricon\" src=\"images/merge.png\" onclick=\"getNextNode('mergeprofile=true')\" onmouseover=\"showTooltip(event, 'Profile zusammenführen')\" onmouseout=\"hideTooltip()\">");
				
				html.append("</div>");
				html.append("<div class=\"organisationmemberform\">");
				html.append("<table width=\"100%\">");
				
				/*
				html.append("<tr><th style=\"width : 300px;\">Personenangaben</th>");
				for(int i = 0; i < tabids.length; i++){
					if(activetab.equals(tabids[i])){
						html.append("<td class=\"formtabactive\"><a href=\"javascript:getNextNode(\'newtab=" + tabids[i] + "\')\">" + tablabels[i] + "</td>");
					}
					else{
						html.append("<td class=\"formtabinactive\"><a href=\"javascript:getNextNode(\'newtab=" + tabids[i] + "\')\">" + tablabels[i] + "</td>");
					}
				}
				html.append("</tr>");	
				*/
				
				html.append("<tr><td valign=\"top\" style=\"border : 1px solid black; padding : 10px; width : 500px;\">");
				html.append("<form action=\"servlet\" id=\"processNodeForm\">");
				html.append("<table>");
				if(om != null){
					
					html.append("<input type=\"hidden\" name=\"current_omid\" value=\"" + om.getName() + "\">");
					omc.addProperty(om.getProperty("MemberRole"));
					//html.append(HTMLForm.getTextField(om.getProperty("MemberRole"), true, ""));
					
					addTableRow(html, omc.getProperty("Status"), "Status", 2);
					if(om.getID("Status")==3){
						html.append("<tr><td></td><td><input type=\"button\" value=\"Daten anonymisieren\" onclick=\"getNextNode('delete=true',true)\">");
					}
					if(om.getID("Status")==4){
						html.append("<tr><td></td><td><input type=\"button\" value=\"Profil freischalten\" onclick=\"getNextNode('activate=true',false)\">");
					}
					html.append("<tr><td>Registriert seit</td><td>" + DateConverter.sqlToShortDisplay(om.getString("DateCreated"), true) + "</td></tr>");
					
					addTableRow(html, omc.getProperty("NotificationMode"), "Informieren über", 2);
					

					if(person != null){
						//html.append(HTMLForm.getTextField(person.getProperty("DateCreated"), true, ""));
		
						if(identity != null){
							addTableRow(html, omc.getProperty("FamilyName"), "Nachname");
							addTableRow(html, omc.getProperty("FirstName"), "Vorname");
							addTableRow(html, omc.getProperty("DateOfBirth"), "Geburtsdatum");
							addTableRow(html, omc.getProperty("Sex"), "Geschlecht", 2);
						}
						if(address != null){
							addTableRow(html, omc.getProperty("AdditionalLine"), "Zusatzzeile");		
							addTableRow(html, omc.getProperty("Street"), "Strasse");
							addTableRow(html, omc.getProperty("Number"), "Nummer");
							addTableRow(html, omc.getProperty("Zipcode"), "PLZ");
							addTableRow(html, omc.getProperty("City"), "Ort");
							addTableRow(html, omc.getProperty("Country"), "Land");
						}
					}
					addTableRow(html, omc.getProperty("FirstLanguageS"), "Erstsprache");
					html.append("<tr><td class=\"inputlabel\" style=\"height : 120px;\">Weitere Sprachen</td><td>" + HTMLForm.getListField(omc.getProperty("Languages") , true, "") + "</td></tr>");
					
					addTableRow(html, omc.getProperty("Comment"), "Kommentar", 3);
					
					if(om.getObjects("OrganisationMemberModification").size() > 0){
						html.append("<tr><th class=\"inputlabel\">Mutationenen</th><td>");
						for(BasicClass bc : om.getObjects("OrganisationMemberModification")){
							if(bc.getID("Status")==0){
								html.append("<a href=\"javascript:getNextNode('modify=" + bc.getString("ID") +"')\">" + bc.getString("Title") +  DateConverter.sqlToShortDisplay(bc.getString("DateCreated")) + "</a><br>");
							}
						}
						html.append("</td></tr>");
						
					}
					
					if(login != null){
						addTableRow(html, omc.getProperty("Username"), "Benutzername");
						addTableRow(html, omc.getProperty("Password"), "Passwort");				
					}
					else{
						html.append("<tr><th>Login</th><td><input type=\"button\" value=\"Login erstellen\" onclick=\"getNextNode(\'createlogin=true\')\"></td></tr>");
					}
					
					if(person != null){	
					
						for(BasicClass bc : person.getObjects("Contact")){
							//addTableRow(html, getProperty(contacts[bc.getInt("Type")]), contacts[bc.getInt("Type")]);
	
							html.append("<tr><td>" + contacts[bc.getInt("Type")] + "</td>");
							html.append("<td>" + HTMLForm.getTextField(omc.getProperty(contacts[bc.getInt("Type")]), true, "") + "</td><td><a href=\"javascript:getNextNode(\'deletecontact=true&contactid=" + bc.getName() + "\')\"><img src=\"images/delete.png\"></a></td></tr>");
						}
					
						
						html.append("<tr><th class=\"inputlabel\">Kontakt hinzufügen</th><td><select onchange=\"getNextNode(\'addcontact=true&contactid=\' + this.value)\">");
						html.append("<option value=\"\"></option>");
						int i = 0;
						for(String contacttype : contacts){
							
							html.append("<option value=\"" + i + "\">" + contacttype + "</option>");
							i++;
						}
					}
					
					Hashtable roles = omedit.roles;
					
					html.append("<tr><td>Rollen:</td><tr>");
					for(BasicClass bc : om.getObjects("MemberRole")){
						if(bc.getID("Status")==0){
							html.append("<tr><td></td><td>");
							html.append(roles.get(bc.getString("Role")) + "</td><td><a href=\"javascript:getNextNode(\'deleterole=true&roleid=" + bc.getName() + "\')\"><img src=\"images/delete.png\"></a></td></tr>");
		
						}
					}
					html.append("</td></tr>");
					
		
					
					html.append("<tr><th class=\"inputlabel\">Rolle hinzufügen</th><td><select onchange=\"getNextNode(\'addrole=true&roleid=\' + this.value)\">");
					html.append("<option value=\"\"></option>");
					for(BasicClass bc : ocs.getObjects("Role")){
						html.append("<option value=\"" + bc.getName() + "\">" + bc.getString("Title") + "</option>");
					}
					html.append("</select></td></tr>");
					
					html.append("<tr><td colspan=\"2\"><input type=\"button\" value=\"Profil verknüpfen\" onclick=\"getNextNode('linkprofile=true')\"></td></tr>");
					
					if(om.getObjects("OrganisationMemberRelationship").size() > 0){
						html.append("<tr><th class=\"inputlabel\">Verknüpfte Profile</th><td>");
						for(BasicClass bc : om.getObjects("OrganisationMemberRelationship")){
							if(bc.getID("Status")==0){
								html.append("<a href=\"javascript:getNextNode('OMID2=" + bc.getString("OrganisationMember") +"')\">" + bc.getString("Title") + ", " + bc.getObject("Role") + "</a><a href=\"javascript:getNextNode(\'deactivaterelation=true&relationid=" + bc.getName() + "\')\"><img src=\"images/delete.png\"></a><br>");
							}
						}
						html.append("</td></tr>");
						
					}
					if(om.getObjects("ReverseRelationships").size() > 0){
						html.append("<tr><th class=\"inputlabel\">Profile verknüpft mit:</th><td>");
						for(BasicClass bc : om.getObjects("ReverseRelationships")){
							if(bc.getID("STATUS")==0){
								html.append("<a href=\"javascript:getNextNode('OMID2=" + bc.getString("ORGANISATIONMEMBERID") +"')\">" + bc.getString("FIRSTNAME") + " " +  bc.getString("FAMILYNAME") + ", " + ((BasicClass)bc.getObject("ROLE")).getString("Titlealt") + "</a><a href=\"javascript:getNextNode(\'deactivaterelation=true&relationid=" + bc.getName() + "\')\"><img src=\"images/delete.png\"></a><br>");
							}
						}
						html.append("</td></tr>");
						
					}
					
					
					
				}
				
				
				html.append("</table>");

				html.append("</form>");
				
				html.append("</td><td valign=\"top\" colspan=\"4\">");
				
				if(omc.getActiveTab().equals("memberad")){
					
					//html.append("<input type=\"button\" value=\"Email import\" onclick=\"getNextNode('newtab=emailimport')\">");
					//html.append("<input type=\"button\" value=\"Website öffnen\" onclick=\"openWebsite('" + om.getString("AccessCode") + "','" + om.getPath()+ "')\">");
					
					html.append("<h4>Feedbacks</h4>");
					
					html.append("<table><tr><td>Abgegebene Feedbacks<td><td>Einsatz zustande gekommen</td><td>");
					
					omedit.getFeedbackBar(ocs, html, om.getName(), "", true);
					
					html.append("</td></tr><tr><td></td><td><td>Einsatz nicht zustande gekommen</td><td>");
					
					omedit.getFeedbackBar(ocs, html, om.getName(), "", false);
					
					html.append("<tr><td>Erhaltene Feedbacks<td><td>Einsatz zustande gekommen</td><td>");
					
					omedit.getFeedbackBar(ocs, html, "", om.getName(), true);
					
					html.append("</td></tr><tr><td></td><td><td>Einsatz nicht zustande gekommen</td><td>");
					
					omedit.getFeedbackBar(ocs, html, "", om.getName(), false);
					
					html.append("</td></tr></table>");
					
					html.append("<hr>");
					
					html.append("<h4>Inserate  <img src=\"images/plus.png\" onclick=\"expandElement('memeberads')\"> <img src=\"images/minus.png\" onclick=\"shrinkElement('memeberads', 150)\"></h4>");
				
					html.append("<input type=\"button\" value=\"Neues Inserat\" onclick=\"getNextNode(\'memberadcreate=true\')\">");
					
					html.append("<div id=\"memeberads\" style=\"height : 150px; overflow : auto;\">");
					
					html.append("<table>");
					html.append("<tr><th>ID</th><th>Typ</th><th>Rubrik</th><th>Titel</th><th>Gültig von</th><th>Gültig bis</th><th>Status</th></tr>");
					
					boolean even = true;
					om.getObjectCollection("MemberAd").sort("ValidFrom", false);
					for(BasicClass bc : om.getObjects("MemberAd")){
						if(even){
							html.append("<tr class=\"even\">");
						}
						else{
							html.append("<tr class=\"odd\">");						
						}
						
						
						html.append("<td class=\"datacell\">" + bc.getName() + "</td>");
						html.append("<td class=\"datacell\">" + bc.getObject("Type") + "</td>");
						html.append("<td class=\"datacell\">" + bc.getString("Template") + "</td>");
						html.append("<td class=\"datacell\">" + bc.getString("Title") + "</td>");
						html.append("<td class=\"datacell\">" + DateConverter.sqlToShortDisplay(bc.getString("ValidFrom"), false) + "</td>");
						html.append("<td class=\"datacell\">" + DateConverter.sqlToShortDisplay(bc.getString("ValidUntil"), false) + "</td>");
						html.append("<td class=\"datacell\">" + bc.getString("Status") + "</td>");
						html.append("<td><a href=\"javascript:getNextNode(\'memberadid=" + bc.getName() + "\')\">Details</a></td>");
						html.append("</tr>");		
						
						if(even){
							html.append("<tr class=\"even\">");
						}
						else{
							html.append("<tr class=\"odd\">");						
						}
						try{
							html.append("<td></td><td colspan=\"5\" class=\"datacell\">" + bc.getString("Description") + "</td></tr>");
						}
						catch(java.lang.Exception e){
							html.append(e.toString());	
						}
						
						even = !even;
					}
					html.append("</table>");
					html.append("</div>");
					                                                                                                                          
					
					getAddressRequests(ocs, html, om);
					
					
					html.append("<hr>");
					
				//}	 //20160202, alles soll auf eine Seite
				//else if(activetab.equals("memberadrequest")){	
					
					
					html.append("<h4>Adresse wurde weitergegeben <img src=\"images/plus.png\" onclick=\"expandElement('requests2')\"> <img src=\"images/minus.png\" onclick=\"shrinkElement('requests2', 150)\"></h4>");
					
					html.append("<div id=\"requests2\" style=\"height : 150px; overflow : auto;\">");
					
					html.append("<table>");
					
					html.append("<tr><th></th><th>Datum</th><th>Inhaber/Besteller</th><th>PLZ / Ort</th><th>Inserat</th></tr>");
					
					ObjectCollection results = new ObjectCollection("Results", "*");
					
	
					
					String sql = "SELECT t1.*, t2.ID AS MemberAdID, t2.Title AS MemberAdTitle, t3.Title AS Category, t4.ID AS OMID2,  t6.FamilyName, t6.FirstName, t6.Sex, t6.DateOfBirth, t7.Street, t7.Number, t7.ZipCode, t7.City, ";
						sql += " t8.ID AS FeedbackID, t8.Reason_1, t8.Reason_2, t8.Reason_3, t8.Reason_4, t8.Reason_5, t8.Reason_6";
						sql += " FROM MemberAdRequest AS t1";
						sql += " LEFT JOIN MemberAd AS t2 ON t1.MemberAd=t2.ID";
						sql += " LEFT JOIN MemberAdCategory AS t3 ON t2.Template=t3.ID";
						sql += " LEFT JOIN OrganisationMember AS t4 ON t1.OrganisationMemberID=t4.ID";
						sql += " LEFT JOIN Person AS t5 ON t4.Person=t5.ID";
						sql += " LEFT JOIN Identity AS t6 ON t6.PersonID=t5.ID";
						sql += " LEFT JOIN Address AS t7 ON t7.PersonID=t5.ID";
						sql += " LEFT JOIN Feedback AS t8 ON t8.MemberAdRequestID=t1.ID";
						
						sql += " WHERE t2.OrganisationMemberID=" + om.getName();
						
						sql += " ORDER BY DateCreated DESC";
						
					ocs.queryData(sql, results);
					
					
					String classname = "";	
						
					for(BasicClass bc : results.getObjects()){
						
						if(even){
							classname = "even";
						}
						else{
							classname = "odd";			
						}

						even = !even;
						
						int status = bc.getInt("STATUS");
						String color = "black";
						String feedbackcolor = "orange";
						if(status == 3){
							color = "red";	
						}
						
						String date = DateConverter.sqlToShortDisplay(bc.getString("DATECREATED"), false);
						
						if(date.length()==0){
							date = DateConverter.sqlToShortDisplay(bc.getString("VALIDFROM"), false);
						}
						
						html.append("<tr class=\"" + classname + "\">");
						
						
						html.append("<td><a href=\"javascript:getNextNode('deletememberadrequest=true&memberadrequestid=" + bc.getString("ID") +"')\" style=\"color : " + color + ";\"><img src=\"images/delete.png\"></a>");
						
						html.append("<td><a href=\"<td class=\"datacell\">" + date + "</td>");
						
						html.append("<td class=\"datacell\"><a href=\"javascript:getNextNode('OMID2=" + bc.getString("OMID2") +"')\" style=\"color : " + color + ";\">" + bc.getString("FAMILYNAME") + " " +  bc.getString("FIRSTNAME") + "</a></td>");
						html.append("<td class=\"datacell\">" + bc.getString("ZIPCODE") + " " + bc.getString("CITY") + "</td>");
						html.append("<td class=\"datacell\">" + bc.getString("MEMBERADTITLE") + "</td><td><a href=\"javascript:getNextNode(\'memberadid=" + bc.getString("MEMBERADID") + "\')\" style=\"color : " + color + ";\">Details</a></td>");
						//html.append("<td class=\"datacell\">" + bc.getString("CATEGORY") + "</td>");
						
						//html.append("<td class=\"datacell\">" + bc.getString("FIRSTNAME") + "</td>");
						//html.append("<td class=\"datacell\">" + DateConverter.sqlToShortDisplay(bc.getString("VALIDUNTIL"), false) + "</td>");
						//html.append("<td class=\"datacell\" bgcolor=\"" + colors[bc.getInt("STATUS")] + "\"> </td>");
						
						html.append("</tr>");
						html.append("<tr class=\"" + classname + "\">");
						html.append("<td class=\"datacell\"colspan=\"4\">" + bc.getString("USERCOMMENT") + "</td>");
						
						
	
						html.append("</tr>");

					}
					
					html.append("</table>");
					
					html.append("</div>");
					
					results.removeObjects();
					
	


					
				}
				else if(omc.getActiveTab().equals("feedback")){	
					
					Hashtable yes_no = omedit.yes_no;
					Hashtable quality = omedit.quality;
					
					ObjectCollection results = new ObjectCollection("Results", "*");
	
					html.append("<table>");
					
					html.append("<tr><td colspan=\"5\"><b>Erhaltene Feedbacks zu Einsätzen</b></td></tr>");
					
					html.append("<tr>");
					html.append("<th>Datum</th>");
					html.append("<th>Einsatz zust. gekommen</th>");
					html.append("<th>Nicht erreicht</th>");
					html.append("<th>Unzuverlässig</th>");
					html.append("<th>Keine Zeit</th>");
					html.append("<th>Passt persönlich nicht</th>");
					html.append("<th>Anderes</th>");
					html.append("<th>Wie war der Einsatz</th>");
					html.append("<th>Besonders gut</th>");
					html.append("<th>Nicht gut</th>");
					html.append("<th>Kommentar</th>");
					html.append("</tr>");
						
	
					
					String sql = "SELECT t1.* FROM Feedback AS t1 WHERE t1.OrganisationMember=" + om.getName();
					
					ocs.queryData(sql, results);
					
					boolean even = true;
					
					for(BasicClass bc : results.getObjects()){
						
						if(even){
							html.append("<tr class=\"even\">");
						}
						else{
							html.append("<tr class=\"odd\">");
						}
						even = !even;
						
						html.append("<td>" + DateConverter.sqlToShortDisplay(bc.getString("DATECREATED"), false) + "</td>");
						html.append("<td>" + bc.getString("CONTACTESTABLISHED") + "</td>");
						html.append("<td>" + yes_no.get(bc.getString("REASON_1")) + "</td>");
						html.append("<td>" + yes_no.get(bc.getString("REASON_2")) + "</td>");
						html.append("<td>" + yes_no.get(bc.getString("REASON_3")) + "</td>");
						html.append("<td>" + yes_no.get(bc.getString("REASON_4")) + "</td>");
						html.append("<td>" + yes_no.get(bc.getString("REASON_5")) + "</td>");
						html.append("<td>" + quality.get(bc.getString("QUALITY")) + "</td>");
						html.append("<td>" + bc.getString("HIGHLIGHTS") + "</td>");
						html.append("<td>" + bc.getString("PROBLEMS") + "</td>");
						html.append("<td>" + bc.getString("COMMENTS") + "</td>");
						html.append("<tr>");	
					}
					
					
					
					html.append("</table>");
					
					html.append("</div>");
					
				}
				html.append("</td></tr>");
				html.append("</table>");
				
				html.append("</div>");
				
				
				OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
				html.append("<div id=\"statusbar\">" + userSession.get("feedback") + "</div>");
				
			}
		}
		catch(java.lang.Exception e){
			ocs.logException(e);	
		}
		return html.toString();

	}
	public static void getAddressRequests(OpenCommunityServer ocs, StringBuilder html, OrganisationMember om){
		
		html.append("<p>");
		html.append("<hr>");
		html.append("<h4>Adressbestellungen  <img src=\"images/plus.png\" onclick=\"expandElement('requests1')\"> <img src=\"images/minus.png\" onclick=\"shrinkElement('requests1', 150)\"></h4>");
			
		ObjectCollection results = new ObjectCollection("Results", "*");
	
		html.append("<input type=\"button\" value=\"Neue Adressbestellung\" onclick=\"getNextNode('createrequest=true')\">");
				
					String[] colors = {"", "orange", "green", "red", "blue"};
					
					html.append("<div id=\"requests1\" style=\"height : 150px; overflow : auto;\">");
					
					html.append("<table>");
					
					html.append("<tr><td colspan=\"4\"><b>Bestellte Adressen:</b></td></tr>");
					
					//html.append("<tr><th>Inserat</th><th>Rubrik</th><th colspan=\"2\">Inhaber/Besteller</th><th>Sichtbar bis</th><th>Status</th></tr>");
					//html.append("<tr><th>Inserat</th><th>Inhaber/Besteller</th><th>Feedback</th></tr>");
			
					
						
						String sql = "SELECT t1.*,   t2.ID AS MemberAdID, t2.Title AS MemberAdTitle, t3.Title AS Category, t4.ID AS OMID2, t6.FamilyName, t6.FirstName, t6.Sex, t6.DateOfBirth, t7.Street, t7.Number, t7.ZipCode, t7.City, ";
						sql += " t8.ID AS FeedbackID, t8.Reason_1, t8.Reason_2, t8.Reason_3, t8.Reason_4, t8.Reason_5, t8.Reason_6";
						sql += " FROM MemberAdRequest AS t1";
						sql += " LEFT JOIN MemberAd AS t2 ON t1.MemberAd=t2.ID";
						sql += " LEFT JOIN MemberAdCategory AS t3 ON t2.Template=t3.ID";
						sql += " LEFT JOIN OrganisationMember AS t4 ON t2.OrganisationMemberID=t4.ID";
						sql += " LEFT JOIN Person AS t5 ON t4.Person=t5.ID";
						sql += " LEFT JOIN Identity AS t6 ON t6.PersonID=t5.ID";
						sql += " LEFT JOIN Address AS t7 ON t7.PersonID=t5.ID";
						sql += " LEFT JOIN Feedback AS t8 ON t8.MemberAdRequestID=t1.ID";
						
						sql += " WHERE t1.OrganisationMemberID=" + om.getName();
						
						sql += " ORDER BY DateCreated DESC";
						
						//sql += " AND t1.Status IN (1,2)";
						

						
						ocs.queryData(sql, results);
					
					
					
					boolean even = false;	
					String prevdate = "";
					String classname = "even";
					
					for(BasicClass bc : results.getObjects()){
						
						if(even){
							classname = "even";
						}
						else{
							classname = "odd";			
						}
						even = !even;
						
						String date = DateConverter.sqlToShortDisplay(bc.getString("DATECREATED"), false);
						if(date.length()==0){
							date = DateConverter.sqlToShortDisplay(bc.getString("VALIDFROM"), false);
						}
						
						if(!date.equals(prevdate)){
							html.append("<tr><td class=\"highlight\">" + date + "</td></tr>");	
						}
						
						int status = bc.getInt("STATUS");
						String color = "black";
						String feedbackcolor = "orange";
						
						if(status == 3){
							color = "red";	
						}
						
						

						


						html.append("<tr class=\"" + classname + "\"><td></td>");
						html.append("<td><a href=\"javascript:getNextNode('deletememberadrequest=true&memberadrequestid=" + bc.getString("ID") +"')\" style=\"color : " + color + ";\"><img src=\"images/delete.png\"></a>");
						html.append("<td class=\"datacell\"  style=\"color : " + color + ";\"><a href=\"javascript:getNextNode('OMID2=" + bc.getString("OMID2") +"')\" style=\"color : " + color + ";\">" + bc.getString("FAMILYNAME") + " " + bc.getString("FIRSTNAME") + "</a></td>");
						html.append("<td class=\"datacell\">" + bc.getString("ZIPCODE") + " " + bc.getString("CITY") + "</td>");
						html.append("<td class=\"datacell\" style=\"color : " + color + ";\">" + bc.getString("MEMBERADTITLE") + "</a></td><td><a href=\"javascript:getNextNode(\'memberadid=" + bc.getString("MEMBERADID") + "\')\"  style=\"color : " + color + ";\">Details</a></td>");
						if(status == 3){
							html.append("<td><a href=\"javascript:getNextNode(\'reactivaterequest=true&memberadrequestid=" + bc.getString("ID") + "\')\"  style=\"color : " + color + ";\">Wieder aktiv setzen</a></td>");	
						}
						html.append("<tr class=\"" + classname + "\"><td></td><td class=\"datacell\" colspan=\"4\">" + bc.getString("CATEGORY") + ": " + bc.getString("USERCOMMENT") + "</td></tr>");
						

	
						

						html.append("</tr>");
						

						prevdate = date;
					}
					
		 html.append("</table>");
					
		 html.append("</div>");
		
		
	}
	public static void addTableRow(StringBuilder html, Property p, String label){
		addTableRow(html, p, label, 1);
	}
	public static void addTableRow(StringBuilder html, Property p, String label, int type){
		html.append("<tr><td>" + label + "</td>");
		if(type==1){
			html.append("<td>" + HTMLForm.getTextField(p , true, "") + "</td></tr>");
		}
		else if(type==2){
			html.append("<td>" + HTMLForm.getSelection(p , true, "") + "</td></tr>");		
		}
		else if(type==3){
			html.append("<td>" + HTMLForm.getTextArea(p , true, "textarea_small") + "</td></tr>");		
		}
	}



}