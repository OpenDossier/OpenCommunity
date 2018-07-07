package ch.opencommunity.view;

import ch.opencommunity.common.OpenCommunityUserSession;
import ch.opencommunity.base.Person;
import ch.opencommunity.base.Identity;
import ch.opencommunity.base.Address;
import ch.opencommunity.base.Login;
import ch.opencommunity.base.Note;
import ch.opencommunity.base.Activity;

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

public class UserProfileView extends BaseView{
	
	static String[] contacts = {"Tel. privat","Tel. gesch.","Tel. mobil","Email" , "Web"};
	
	
	public static String getUserProfileView(OpenCommunityUserSession userSession, OrganisationMemberController omc, ApplicationContext context){
		

		
		OrganisationMember om = omc.getOrganisationMember();
		
		OpenCommunityServer ocs = (OpenCommunityServer)om.getRoot();
		
		
		Person person = om.getPerson();
		Identity identity = person.getIdentity();
		Address address = person.getAddress();
		Login login = om.getLogin();
		
		StringBuilder html = new StringBuilder();
		
		ocs.getMemberRoles();
				
		html.append("<div id=\"toolbar2\">");
				
		html.append("<img class=\"toolbaricon\" src=\"images/save.png\" onclick=\"saveObject('" + omc.getPath() + "')\" onmouseover=\"showTooltip(event, 'Speichern')\" onmouseout=\"hideTooltip()\">");
		html.append("<img class=\"toolbaricon\" src=\"images/sendmail.png\" onclick=\"onAction('" + omc.getPath() + "','activitycreate2', '','objecttemplate=1')\" onmouseover=\"showTooltip(event, 'Email schreiben')\" onmouseout=\"hideTooltip()\">");
		html.append("<img class=\"toolbaricon\" src=\"images/sendmail.png\" onclick=\"onAction('" + omc.getPath() + "','sendcredentials')\" onmouseover=\"showTooltip(event, 'Zugangsdaten zustellen')\" onmouseout=\"hideTooltip()\">");

		
		html.append("Neues Dokument:");
		html.append(ocs.getFormManager().getSelection(omc.getProperty("documenttemplate"), "",  "", true, true, true, "", "onAction('" + omc.getPath() + "','documentcreate', '','documenttemplate=' + this.value )", false));
		
		//html.append("<img class=\"toolbaricon\" src=\"images/word.png\" onclick=\"onAction('" + omc.getPath() + "','activitycreate2','', 'objecttemplate=2')\" onmouseover=\"showTooltip(event, 'Worddokument erstellen')\" onmouseout=\"hideTooltip()\">");				
		html.append("<img class=\"toolbaricon\" src=\"images/pdf.png\" onclick=\"onAction('" + omc.getPath() + "','activitycreate2', '','objecttemplate=7')\" onmouseover=\"showTooltip(event, 'PDF-Brief erstellen')\" onmouseout=\"hideTooltip()\">");
		html.append("<img class=\"toolbaricon\" src=\"images/pdf.png\" onclick=\"onAction('" + omc.getPath() + "','createnotification', '','objecttemplate=7')\" onmouseover=\"showTooltip(event, 'Mitglied brieflich benachrichtigen')\" onmouseout=\"hideTooltip()\">");
		html.append("<img class=\"toolbaricon\" src=\"images/emailimport.png\" onclick=\"getNextNode('newtab=emailimport')\" onmouseover=\"showTooltip(event, 'Email importieren')\" onmouseout=\"hideTooltip()\">");
				
		html.append("<img class=\"toolbaricon\" src=\"images/www.png\" onclick=\"openWebsite('" + om.getString("AccessCode") + "','" + om.getPath()+ "')\" onmouseover=\"showTooltip(event, 'Benutzer auf Website ansehen')\" onmouseout=\"hideTooltip()\">");
				
		html.append("<img class=\"toolbaricon\" src=\"images/feedback.png\" onclick=\"getNextNode('feedback=true')\" onmouseover=\"showTooltip(event, 'Feedback')\" onmouseout=\"hideTooltip()\">");
		html.append("<img class=\"toolbaricon\" src=\"images/fileupload.png\" onclick=\"getNextNode('fileupload=true')\" onmouseover=\"showTooltip(event, 'Datei hochladen')\" onmouseout=\"hideTooltip()\">");
		html.append("<img class=\"toolbaricon\" src=\"images/merge.png\" onclick=\"onAction('" + omc.getPath() + "', 'mergeprofile')\" onmouseover=\"showTooltip(event, 'Profile zusammenführen')\" onmouseout=\"hideTooltip()\">");
				
		html.append("</div>");
		
		html.append("<div class=\"organisationmemberforml\">");
		
		html.append("<div id=\"popuplarge\"></div>");
		
		//html.append("<table class=\"formtable\">");
				

				
		//html.append("<tr><td valign=\"top\" style=\"border : 1px solid black; padding : 10px; width : 500px; overflow : scroll;\">");
		
		html.append("<div class=\"organisationmemberdetails\">");
		
		int ouid = om.getID("OrganisationalUnitID");

		if(ouid > 1){
			html.append("<input type=\"button\" class=\"actionbutton\" value=\"Zurück\" onclick=\"openDossier(" + ouid + ")\">");	
		}
		
		html.append("<form action=\"servlet\" id=\"objectEditForm_" + omc.getPath() + "\">");
		html.append("<table>");
		if(om != null){
					
					html.append("<input type=\"hidden\" name=\"action\" value=\"getObjectAction\">");
					html.append("<input type=\"hidden\" name=\"command\" value=\"saveobject\">");
					html.append("<input type=\"hidden\" name=\"objectPath\" value=\"" + omc.getPath() + "\">");
					
					omc.addProperty(om.getProperty("MemberRole"));
					//html.append(HTMLForm.getTextField(om.getProperty("MemberRole"), true, ""));
					
					html.append("<tr><td>Status</td><td>" + HTMLForm.getSelection(om.getProperty("Status") , true, "om_"));

					
					if(om.getID("Status")==6){
						html.append("<input type=\"button\" value=\"Daten anonymisieren\" onclick=\"onAction('" + omc.getPath() + "','profiledelete', null, null, true)\">");
					}
					if(om.getID("Status")==4){
						html.append("<input type=\"button\" value=\"Profil freischalten\" onclick=\"activateProfile('" + omc.getPath() + "')\">");
					}
					html.append("</td></tr>");
					html.append("<tr><td>Registriert seit</td><td>" + DateConverter.sqlToShortDisplay(om.getString("DateCreated"), true) + "</td></tr>");
					addTableRow(html, omc.getProperty("Type"), "Profiltyp", "om_", 2);					
					addTableRow(html, omc.getProperty("NotificationMode"), "Informieren über", 2);
					

					if(person != null){
						//html.append(HTMLForm.getTextField(person.getProperty("DateCreated"), true, ""));
		
						if(identity != null){
							addTableRow(html, omc.getProperty("FamilyName"), "Nachname", "identity_", 1);
							addTableRow(html, omc.getProperty("FirstName"), "Vorname", "identity_", 1);
							addTableRow(html, omc.getProperty("DateOfBirth"), "Geburtsdatum", "identity_", 1);
							addTableRow(html, omc.getProperty("Sex"), "Geschlecht", "identity_", 2);
						}
						if(om.getID("OrganisationalUnitID") > 1){
							addTableRow(html, omc.getProperty("Function"), "Funktion", "om_", 2);	
							addTableRow(html, omc.getProperty("Title"), "Beschreibung", "om_", 1);	
							addTableRow(html, omc.getProperty("InheritsAddress"), "Adresse wie Organisation", "om_", 4);		
						}
						if(!omc.getBoolean("InheritsAddress")){
							if(address != null){
								addTableRow(html, omc.getProperty("AdditionalLine"), "Zusatzzeile", "address_", 1);	
								addTableRow(html, omc.getProperty("POBox"), "Postfach", "address_", 1);	
								addTableRow(html, omc.getProperty("Street"), "Strasse", "address_", 1);
								addTableRow(html, omc.getProperty("Number"), "Nummer", "address_", 1);
								addTableRow(html, omc.getProperty("Zipcode"), "PLZ", "address_", 1);
								addTableRow(html, omc.getProperty("City"), "Ort", "address_", 1);
								addTableRow(html, omc.getProperty("Country"), "Land", "address_", 1);
							}
						}
					}
					
					if(ouid < 2){
					
						addTableRow(html, omc.getProperty("FirstLanguageS"), "Erstsprache", "identity_", 1);
						html.append("<tr><td class=\"inputlabel\" style=\"height : 120px;\">Weitere Sprachen</td><td>" + HTMLForm.getListField(person.getProperty("Languages") , true, "person_") + "</td></tr>");
						
						addTableRow(html, omc.getProperty("Comment"), "Kommentar", "om_", 3);
						
						if(om.getObjects("OrganisationMemberModification").size() > 0){
							html.append("<tr><th class=\"inputlabel\">Mutationenen</th><td>");
							for(BasicClass bc : om.getObjects("OrganisationMemberModification")){
								if(bc.getID("Status")==0){
									html.append("<a href=\"javascript:createProcess('ch.opencommunity.process.OrganisationMemberModify','OMID=" + bc.getName() +"')\">" + bc.getString("Title") +  DateConverter.sqlToShortDisplay(bc.getString("DateCreated")) + "</a><br>");
								}
							}
							html.append("</td></tr>");
							
						}
						
						if(login != null){
							addTableRow(html, login.getProperty("Username"), "Benutzername", "login_", 1);
							addTableRow(html, login.getProperty("Password"), "Passwort", "login_", 1);				
						}
						else{
							html.append("<tr><th>Login</th><td><input type=\"button\" value=\"Login erstellen\" onclick=\"onAction('" + omc.getPath() + "','createlogin')\"></td></tr>");
						}
						
					}
					
					if(person != null){	
					
						for(BasicClass contact : person.getObjects("Contact")){
							//addTableRow(html, getProperty(contacts[bc.getInt("Type")]), contacts[bc.getInt("Type")]);
	
							html.append("<tr><td>" + contacts[contact.getID("Type")] + "</td>");
							html.append("<td>" + HTMLForm.getTextField(contact.getProperty("Value"), true, "contact_" + contact.getName() + "_", "inputlarge") + "</td><td><a href=\"javascript:onAction('" + omc.getPath() + "','contactdelete','', 'contactid=" + contact.getName() + "')\"><img src=\"images/delete.png\"></a></td></tr>");
						}
					
						
						html.append("<tr><th class=\"inputlabel\">Kontakt hinzufügen</th><td><select onchange=\"onAction('" + omc.getPath() + "','contactadd','', 'contactid=' + this.value)\">");
						html.append("<option value=\"\"></option>");
						int i = 0;
						for(String contacttype : contacts){
							
							html.append("<option value=\"" + i + "\">" + contacttype + "</option>");
							i++;
						}
					}
					
					if(ouid < 2){
					
					Hashtable roles = ocs.roleMap;
					
					html.append("<tr><td>Rollen:</td><tr>");
					for(BasicClass bc : om.getObjects("MemberRole")){
						if(bc.getID("Status")==0){
							html.append("<tr><td></td><td>");
							html.append(omc.roles.get(bc.getString("Role")) + "</td><td><a href=\"javascript:onAction('" + omc.getPath() + "', 'roledelete','','roleid=" + bc.getName() + "\')\"><img src=\"images/delete.png\"></a></td></tr>");
		
						}
					}
					html.append("</td></tr>");
					
		
					
					html.append("<tr><th class=\"inputlabel\">Rolle hinzufügen</th><td><select onchange=\"onAction('" + omc.getPath() + "','roleadd', '', 'roleid=\' + this.value)\">");
					html.append("<option value=\"\"></option>");
					for(BasicClass bc : ocs.getObjects("Role")){
						html.append("<option value=\"" + bc.getName() + "\">" + bc.getString("Title") + "</option>");
					}
					html.append("</select></td></tr>");
					
					html.append("<tr><td colspan=\"2\"><input type=\"button\" class=\"actionbutton\" value=\"Profil verknüpfen\" onclick=\"onAction('" + omc.getPath() + "','linkprofile')\"></td></tr>");
					
					if(om.getObjects("OrganisationMemberRelationship").size() > 0){
						html.append("<tr><th class=\"inputlabel\">Verknüpfte Profile</th><td>");
						for(BasicClass bc : om.getObjects("OrganisationMemberRelationship")){
							if(bc.getID("Status")==0){
								html.append("<a href=\"javascript:editOrganisationMember('" + bc.getString("OrganisationMember") + "')\">" + bc.getString("Title") + ", " + bc.getObject("Role") + "</a><a href=\"javascript:getNextNode(\'deactivaterelation=true&relationid=" + bc.getName() + "\')\"><img src=\"images/delete.png\"></a><br>");
							}
						}
						html.append("</td></tr>");
						
					}
					if(om.getObjects("ReverseRelationships").size() > 0){
						html.append("<tr><th class=\"inputlabel\">Profil verknüpft mit:</th><td>");
						for(BasicClass bc : om.getObjects("ReverseRelationships")){
							if(bc.getID("STATUS")==0){
								html.append("<a href=\"javascript:editOrganisationMember('" + bc.getString("ORGANISATIONMEMBERID") + "')\">" + bc.getString("FIRSTNAME") + " " +  bc.getString("FAMILYNAME") + ", " + ((BasicClass)bc.getObject("ROLE")).getString("Titlealt") + "</a><a href=\"javascript:getNextNode(\'deactivaterelation=true&relationid=" + bc.getName() + "\')\"><img src=\"images/delete.png\"></a><br>");
							}
						}
						html.append("</td></tr>");
						
					}
					
					
					
				
				
				html.append("</table>");	
				html.append("</form>");
				html.append("</div>");
				
				html.append("<div class=\"organisationmemberobjects\">");

				html.append("<div id=\"objecttabs\">");
				html.append("<table><tr>");
				html.append("<td class=\"formtab2\"><div id=\"tab_showobjectlist\" class=\"formtab2\" onclick=\"onAction('" + omc.getPath() + "','showobjectlist')\">Objekte</div></td>");
				html.append("<td class=\"formtab2\"><div id=\"tab_showjournal\" class=\"formtab2\" onclick=\"onAction('" + omc.getPath() + "','showjournal')\">Journal</div></td>");			
				html.append("<td class=\"formtab2\"><div id=\"tab_showaccountmovements\" class=\"formtab2\" onclick=\"onAction('" + omc.getPath() + "','showaccountmovements')\">Zahlungen</div></td>");	
				html.append("<td class=\"formtab2\"><div id=\"tab_showcheques\" class=\"formtab2\" onclick=\"onAction('" + omc.getPath() + "','showcheques')\">Nachbarcheques</div></td>");	
				html.append("</tr></table>");
				
				html.append("</div>");
		

		
		//html.append("<img class=\"toolbaricon\" src=\"images/journal.png\" onclick=\"onAction('" + omc.getPath() + "','showobjectlist')\" onmouseover=\"showTooltip(event, 'Journal')\" onmouseout=\"hideTooltip()\">");
		//html.append("<img class=\"toolbaricon\" src=\"images/journal.png\" onclick=\"onAction('" + omc.getPath() + "','showjournal')\" onmouseover=\"showTooltip(event, 'Journal')\" onmouseout=\"hideTooltip()\">");
		
				html.append("<div id=\"objectList\">");
				if(omc.mode==2){
					html.append(getActivityList(omc, om));
				}
				else{
					html.append(getObjectList(omc, om));			
				}
				html.append("</div>");
		
				html.append("</div>");
		

			
				html.append("<div id=\"objectEditArea\">");
					
				if(omc.activeObject != null){
					if(omc.activeObject instanceof Activity){
						Activity activity = (Activity)omc.activeObject;
						ActivityView.toHTML2(html, omc, activity, context);	
					}
				}
					
				html.append("</div>");
				
				}
				
		}

		
		html.append("</div>");
		
		return html.toString();
		
	}

	public static String getObjectList(OrganisationMemberController omc, OrganisationMember om){
		
		OpenCommunityServer ocs = (OpenCommunityServer)om.getRoot();
		
		StringBuilder html = new StringBuilder();
		
		html.append("<h4>Feedbacks</h4>");
					
		html.append("<table><tr><td>Abgegebene Feedbacks<td><td>Einsatz zustande gekommen</td><td>");
					
		getFeedbackBar(ocs, html, om.getName(), "", true);
		getFeedbackBar2(ocs, html, om.getName(), "", true);
					
		html.append("</td></tr><tr><td></td><td><td>Einsatz nicht zustande gekommen</td><td>");
					
		getFeedbackBar(ocs, html, om.getName(), "", false);
		getFeedbackBar2(ocs, html, om.getName(), "", false);
					
		html.append("<tr><td>Erhaltene Feedbacks<td><td>Einsatz zustande gekommen</td><td>");
					
		getFeedbackBar(ocs, html, "", om.getName(), true);
		getFeedbackBar2(ocs, html, "", om.getName(), true);
					
		html.append("</td></tr><tr><td></td><td><td>Einsatz nicht zustande gekommen</td><td>");
					
		getFeedbackBar(ocs, html, "", om.getName(), false);
		getFeedbackBar2(ocs, html, "", om.getName(), false);
					
		html.append("</td></tr></table>");
					
		html.append("<hr>");
					
		html.append("<h4>Inserate  <img src=\"images/plus.png\" onclick=\"expandElement('memeberads')\"> <img src=\"images/minus.png\" onclick=\"shrinkElement('memeberads', 150)\"></h4>");
				
		html.append("<input type=\"button\" class=\"actionbutton\" value=\"Neues Inserat\" onclick=\"onAction('" + omc.getPath() + "','memberadcreate')\">");
		html.append("<input type=\"button\" class=\"actionbutton\" value=\"Neues Werbeinserat\" onclick=\"onAction('" + omc.getPath() + "','commercialadcreate')\">");
		html.append("<input type=\"button\" class=\"actionbutton\" value=\"Email mit Link verschicken\" onclick=\"onAction('" + omc.getPath() + "','memberadlistsend','','objecttemplate=4')\">");
		html.append("<input type=\"button\" class=\"actionbutton\" value=\"Liste ausdrucken\" onclick=\"onAction('" + omc.getPath() + "','memberadlistprint')\">");
					
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
						//html.append("<td><a href=\"javascript:getNextNode(\'memberadid=" + bc.getName() + "\')\">Details</a></td>");
						html.append("<td><a href=\"javascript:onAction(\'" + omc.getPath() + "','editmemberad','','memberadid=" + bc.getName() + "\')\">Details</a></td>");
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
					                                                                                                                          
					
					getAddressRequests(ocs, html, omc, om);
					
					
					html.append("<hr>");
					
				//}	 //20160202, alles soll auf eine Seite
				//else if(activetab.equals("memberadrequest")){	
					
					
					html.append("<h4>Adresse wurde weitergegeben <img src=\"images/plus.png\" onclick=\"expandElement('requests2')\"> <img src=\"images/minus.png\" onclick=\"shrinkElement('requests2', 150)\"></h4>");
					
					html.append("<div id=\"requests2\" style=\"height : 150px; overflow : auto;\">");
					
					html.append("<table>");
					
					html.append("<tr><th></th><th>Datum</th><th>Inhaber/Besteller</th><th>PLZ / Ort</th><th>Inserat</th></tr>");
					
					ObjectCollection results = new ObjectCollection("Results", "*");
					
	
					
					String sql = "SELECT CASE WHEN t1.DateCreated IS NULL THEN t1.ValidFrom ELSE t1.DateCreated END AS Date, t1.*, t2.ID AS MemberAdID, t2.Title AS MemberAdTitle, t3.Title AS Category, t4.ID AS OMID2,  t6.FamilyName, t6.FirstName, t6.Sex, t6.DateOfBirth, t7.Street, t7.Number, t7.ZipCode, t7.City, ";
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
						
						//sql += " ORDER BY t1.DateCreated DESC";
						sql += " ORDER BY 1 DESC";
						
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
						
						html.append("<td class=\"datacell\">" + date + "</td>");
						
						html.append("<td class=\"datacell\"><a href=\"javascript:editOrganisationMember(" + bc.getString("OMID2") +")\" style=\"color : " + color + ";\">" + bc.getString("FAMILYNAME") + " " +  bc.getString("FIRSTNAME") + "</a></td>");
						html.append("<td class=\"datacell\">" + bc.getString("ZIPCODE") + " " + bc.getString("CITY") + "</td>");
						html.append("<td class=\"datacell\">" + bc.getString("MEMBERADTITLE") + "</td><td><a href=\"javascript:onAction('"+ omc.getPath() + "','memberaddetail','', 'memberadid=" + bc.getString("MEMBERADID") + "\')\"  style=\"color : " + color + ";\">Details</a></td>");
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
		return html.toString();
	}
	public static String getActivityList(OrganisationMemberController omc, OrganisationMember om){
		return getActivityList(omc, om, null);	
	}
	public static String getActivityList(OrganisationMemberController omc, OrganisationMember om, String filter){
		StringBuilder html = new StringBuilder();
		
		

		//html.append("<h4>Journal</h4>");
				

		
		html.append("Filtern nach");
		html.append(HTMLForm.getSelection(omc.getProperty("activityfilter"), "",  "", true, true, true, "", "onAction('" + omc.getPath() + "','activityfilter', '','objecttemplate=' + this.value )"));
					
		html.append("Neue Aktivität:");
		html.append(HTMLForm.getSelection(omc.getProperty("objecttemplate"), "",  "", true, true, true, "", "onAction('" + omc.getPath() + "','activitycreate2', '','objecttemplate=' + this.value )"));
			
		
		html.append("<div id=\"activityList\">");
		
		html.append(getActivityListContent(omc, om, filter));
		
		html.append("</div>");
		
		return html.toString();
		
	}
		
	public static String getActivityListContent(OrganisationMemberController omc, OrganisationMember om, String filter){
		
		String[] status = {"offen", "erledigt"};
		
		StringBuilder html = new StringBuilder();		
					
		html.append("<table width=\"90%\">");
					
		html.append("<tr><th>Datum</th><th>Vorlage</th><th>Betreff</th><th>Status</th></tr>");
					
		boolean even = true;

		for(int j = om.getObjects("Activity").size()-1; j >= 0; j--){
					
			BasicClass bc = om.getObjects("Activity").get(j);
			
			int template = bc.getID("Template");
			
			if(filter==null || filter.equals("") || filter.equals("" + template)){
			
			if(even){
				html.append("<tr class=\"even\">");
			}
			else{
				html.append("<tr class=\"odd\">");						
			}
						
						
			html.append("<td class=\"datacell\"><b>" + DateConverter.sqlToShortDisplay(bc.getString("DateCreated"), false) + "</b></td>");
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
			
			//html.append("<td class=\"datacell\">" + status[bc.getID("Status")] + "</td>");
			html.append("<td class=\"datacell\"><a href=\"javascript:onAction('" + omc.getPath() + "','deleteactivity', '',\'activityid=" + bc.getName() + "\')\"><img src=\"images/delete_small.png\"></a><a href=\"javascript:onAction('" + omc.getPath() + "','editactivity', '',\'activityid=" + bc.getName() + "\')\"><img src=\"images/edit_small.png\"></a></td>");
			html.append("</tr>");
			Note note = (Note)bc.getObjectByIndex("Note", 0);
			
			int n = 5;
			
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
					if(template==3){
						n = 10;	
					}
					if(lines.length < n) n = lines.length;
					for(int i = 0; i < n; i++){
						if(lines[i].trim().length() > 0){
							html.append(lines[i] + "<br>");	
						}
					}
							
					html.append("</td></tr>");
							
				}
				even = !even;
			}
					
		}
		html.append("</table>");
				
		
		return html.toString();
		
	}
	public static void getFeedbackBar(OpenCommunityServer ocs, StringBuilder html, String owner, String omid, boolean contactestablished){
		
		String sql = "SELECT * FROM Feedback AS t1";
		if(owner.length() > 0){
			sql += " WHERE OrganisationMemberID=" + owner;
		}
		else{
			sql += " WHERE OrganisationMember=" + omid;	
		}
		if(contactestablished){
			sql += " AND ContactEstablished = 'true' ";	
		}
		else{
			sql += " AND ContactEstablished = 'false' ";				
		}
		sql += " ORDER BY DateCreated";
		ObjectCollection results = new ObjectCollection("Results", "*");
		
		html.append("<table><tr style=\"height : 30px;\">");
		
		ocs.queryData(sql, results);
		String style = null;
		for(BasicClass bc : results.getObjects()){
			if(bc.getBoolean("CONTACTESTABLISHED")==true){
				int quality = bc.getInt("QUALITY");
				if(quality == 1){
					style = "border : 1px solid black; background : red;";
				}
				else if(quality == 2){
					style = "border : 1px solid black; background : orange;";
				}
				else{
					style = "border : 1px solid black; background : green;";
				}
				
			}
			else{
				boolean reason_1 = bc.getBoolean("REASON_1");
				boolean reason_2 = bc.getBoolean("REASON_2");
				boolean reason_3 = bc.getBoolean("REASON_3");
				boolean reason_4 = bc.getBoolean("REASON_4");
				boolean reason_5 = bc.getBoolean("REASON_5");
				boolean reason_6 = bc.getBoolean("REASON_6");
				if(reason_1 || reason_2 || reason_4){
					style = "border : 1px solid black; background : red;";					
				}
				else{
					style = "border : 1px solid black; background : orange;";					
				}
				
			}
			html.append("<td width=\"50px\" style=\"" + style + "\" onclick=\"showFeedback(" + bc.getString("ID") + ")\"></td>");
				
		}
		html.append("</tr></table>");
		
	}
	public static void getFeedbackBar2(OpenCommunityServer ocs, StringBuilder html, String owner, String omid, boolean contactestablished){
		
		String sql = "SELECT * FROM FeedbackRecord AS t1";
		if(owner.length() > 0){
			sql += " WHERE OrganisationMemberID=" + owner;
		}
		else{
			sql += " WHERE OrganisationMember=" + omid;	
		}
		if(contactestablished){
			sql += " AND ContactEstablished = 'true' ";	
		}
		else{
			sql += " AND ContactEstablished = 'false' ";				
		}
		sql += " ORDER BY DateCreated";
		ObjectCollection results = new ObjectCollection("Results", "*");
		
		html.append("<table><tr style=\"height : 30px;\">");
		
		ocs.queryData(sql, results);
		String style = null;
		for(BasicClass bc : results.getObjects()){
			if(bc.getBoolean("CONTACTESTABLISHED")==true){
				int quality = bc.getInt("CONTACTQUALITY");
				if(quality == 1){
					style = "border : 1px solid black; background : red;";
				}
				else if(quality == 2){
					style = "border : 1px solid black; background : orange;";
				}
				else{
					style = "border : 1px solid black; background : green;";
				}
				
			}
			else{
				if(bc.getBoolean("FOLLOWUPNEEDED")){
					style = "border : 1px solid black; background : red;";					
				}
				else{
					style = "border : 1px solid black; background : green;";					
				}
				
			}
			html.append("<td width=\"50px\" style=\"" + style + "\" onclick=\"createProcess('ch.opencommunity.feedback.FeedbackFinalize','feedbackid=" + bc.getString("ID") + "')\"></td>");
				
		}
		html.append("</tr></table>");
		
	}
	public static void getAddressRequests(OpenCommunityServer ocs, StringBuilder html,  OrganisationMemberController omc, OrganisationMember om){
		
		html.append("<p>");
		html.append("<hr>");
		html.append("<h4>Adressbestellungen  <img src=\"images/plus.png\" onclick=\"expandElement('requests1')\"> <img src=\"images/minus.png\" onclick=\"shrinkElement('requests1', 150)\"></h4>");
			
		ObjectCollection results = new ObjectCollection("Results", "*");
	
		html.append("<input type=\"button\" class=\"actionbutton\" value=\"Neue Adressbestellung\" onclick=\"onAction('" + omc.getPath() + "','requestcreate')\">");
		
		html.append("<input type=\"button\" class=\"actionbutton\" value=\"Email mit Link verschicken\" onclick=\"onAction('" + omc.getPath() + "','memberadrequestlistsend','','objecttemplate=4')\">");
		
		html.append("<input type=\"button\" class=\"actionbutton\" value=\"Liste ausdrucken\" onclick=\"onAction('" + omc.getPath() + "','memberadrequestlistprint')\">");
				
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
						html.append("<td class=\"datacell\"  style=\"color : " + color + ";\"><a href=\"javascript:editOrganisationMember(" + bc.getString("OMID2") +")\" style=\"color : " + color + ";\">" + bc.getString("FAMILYNAME") + " " + bc.getString("FIRSTNAME") + "</a></td>");
						html.append("<td class=\"datacell\">" + bc.getString("ZIPCODE") + " " + bc.getString("CITY") + "</td>");
						html.append("<td class=\"datacell\" style=\"color : " + color + ";\">" + bc.getString("MEMBERADTITLE") + "</a></td><td><a href=\"javascript:onAction('"+ omc.getPath() + "','memberaddetail','', 'memberadid=" + bc.getString("MEMBERADID") + "\')\"  style=\"color : " + color + ";\">Details</a></td>");
						if(status == 3){
							html.append("<td><a href=\"javascript:onAction('" + omc.getPath() + "','reactivaterequest', '', 'memberadrequestid=" + bc.getString("ID") + "\')\"  style=\"color : " + color + ";\">Wieder aktiv setzen</a></td>");	
						}
						html.append("<tr class=\"" + classname + "\"><td></td><td class=\"datacell\" colspan=\"4\">" + bc.getString("CATEGORY") + ": " + bc.getString("USERCOMMENT") + "</td></tr>");
						

	
						

						html.append("</tr>");
						

						prevdate = date;
					}
					
		 html.append("</table>");
					
		 html.append("</div>");
		
		
	}


}