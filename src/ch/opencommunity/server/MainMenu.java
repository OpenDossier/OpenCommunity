package ch.opencommunity.server;

import ch.opencommunity.common.OpenCommunityUserSession;

import org.kubiki.application.ApplicationContext;
import org.kubiki.application.server.WebApplicationContext;

public class MainMenu{
	
	
	public String toHTML(ApplicationContext context){
		
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
		
		StringBuilder html = new StringBuilder();
		


		html.append("<table><tr>");
		
		if(userSession.isSysadmin()){
			html.append("<td class=\"tab\"><div id=\"home\" class=\"tabcontent\"><a class=\"tab\" href=\"javascript:loadSection('home')\">Uebersicht</a></div></td>");
			//html.append("<td class=\"tab\"><div id=\"user\" class=\"tabcontent\"><a class=\"tab\" href=\"javascript:loadSection('user')\">Mitglieder/Benutzer</a></div></td>");
			html.append("<td class=\"tab\"><div id=\"user3\" class=\"tabcontent\"><a class=\"tab\" href=\"javascript:loadSection('user3')\">Mitglieder/Benutzer</a></div></td>");
			//html.append("<td class=\"tab\"><div id=\"user2\" class=\"tabcontent\"><a class=\"tab\" href=\"javascript:loadSection('user2')\">Mitglieder/Benutzer2</a></div></td>");
			html.append("<td class=\"tab\"><div id=\"memberads\" class=\"tabcontent\"><a class=\"tab\" href=\"javascript:loadSection('memberads')\">Inserate</a></div></td>");
			html.append("<td class=\"tab\"><div id=\"requests\" class=\"tabcontent\"><a class=\"tab\" href=\"javascript:loadSection('requests')\">Adressbestellungen</a></div></td>");
			html.append("<td class=\"tab\"><div id=\"feedback\" class=\"tabcontent\"><a class=\"tab\" href=\"javascript:loadSection('feedback')\">Feedback</a></div></td>");
			html.append("<td class=\"tab\"><div id=\"email\" class=\"tabcontent\"><a class=\"tab\" href=\"javascript:loadSection('imap')\">Email</a></div></td>");
			html.append("<td class=\"tab\"><div id=\"newsletter\" class=\"tabcontent\"><a class=\"tab\" href=\"javascript:loadSection('newsletter')\">Newsletter</a></div></td>");
			html.append("<td class=\"tab\"><div id=\"documents\" class=\"tabcontent\"><a class=\"tab\" href=\"javascript:loadSection('documents')\">Dokumente</a></div></td>");	
			html.append("<td class=\"tab\"><div id=\"statistics\" class=\"tabcontent\"><a class=\"tab\" href=\"javascript:loadSection('statistics')\">Statistik</a></div></td>");	
			html.append("<td class=\"tab\"><div id=\"accounting\" class=\"tabcontent\"><a class=\"tab\" href=\"javascript:loadSection('accounting')\">Zahlungen</a></div></td>");
									
			html.append("<td class=\"tab\"><div id=\"cheques\" class=\"tabcontent\"><a class=\"tab\" href=\"javascript:loadSection('cheques')\">Nachbarcheques</a></div></td>");	
									
			html.append("<td class=\"tab\"><div id=\"calendar\" class=\"tabcontent\"><a class=\"tab\" href=\"javascript:loadSection('calendar')\">Aktuelles und Agenda</a></div></td>");
			
			
			html.append("<td class=\"tab\"><div id=\"cms\" class=\"tabcontent\"><a class=\"tab\" href=\"javascript:loadSection('cms')\">CMS</a></div></td>");
			html.append("<td class=\"tab\"><div id=\"textblocks\" class=\"tabcontent\"><a class=\"tab\" href=\"javascript:loadSection('textblocks')\">Texte</a></div></td>");
			html.append("<td class=\"tab\"><div id=\"files\" class=\"tabcontent\"><a class=\"tab\" href=\"javascript:loadSection('files')\">Dateien</a></div></td>");
			html.append("<td class=\"tab\"><div id=\"memberadcategories\" class=\"tabcontent\"><a class=\"tab\" href=\"javascript:loadSection('memberadcategories')\">Inseratrubriken</a></div></td>");
			//html.append("<td class=\"tab\"><div id=\"commercialads\" class=\"tabcontent\"><a class=\"tab\" href=\"javascript:loadSection('commercialads')\">Bez. Anzeigen</a></div></td>");
			html.append("<td class=\"tab\"><div id=\"batchactivities\" class=\"tabcontent\"><a class=\"tab\" href=\"javascript:loadSection('batchactivities')\">Serienbriefe</a></div></td>");
									
			html.append("<td class=\"tab\"><div id=\"pdf\" class=\"tabcontent\"><a class=\"tab\" href=\"javascript:loadSection('pdf')\">PDF</a></div></td>");
			html.append("<td class=\"tab\"><div id=\"libreoffice\" class=\"tabcontent\"><a class=\"tab\" href=\"javascript:loadSection('libreoffice')\">Libreoffice Vorlagen</a></div></td>");


													
				html.append("<td class=\"tab\"><div id=\"objecttemplates\" class=\"tabcontent\"><a class=\"tab\" href=\"javascript:loadSection('objecttemplates')\">Objektvorlagen</a></div></td>");
										
				html.append("<td class=\"tab\"><div id=\"documenttemplates\" class=\"tabcontent\"><a class=\"tab\" href=\"javascript:loadSection('documenttemplates')\">Dokumentvorlagen</a></div></td>");
		
				//html.append("<td class=\"tab\"><div id=\"email\" class=\"tabcontent\"><a class=\"tab\" href=\"javascript:loadSection('email')\">Pending Mails</a></div></td>");
										
			
			

		}
		else if(userSession.isCMSAdmin()){
			html.append("<td class=\"tab\"><div id=\"cms\" class=\"tabcontent\"><a class=\"tab\" href=\"javascript:loadSection('cms')\">CMS</a></div></td>");			
		}
		html.append("</tr></table>");
		
		return html.toString();
		
	}
	public String getMainMenu(ApplicationContext context, OpenCommunityUserSession userSession){
		
		StringBuilder html = new StringBuilder();
		
		html.append("<div class=\"logo\"><img src=\"images/logo.png\"></div>");
		
		if(userSession.isSysadmin()){
			
			html.append(getMenuButton("home", "loadSection('home')", "&Uuml;bersicht", "dashboard.png"));
			html.append(getMenuButton("user3", "loadSection('user3')", "Mitglieder/Benutzer", "users.png"));
			
			//html.append("<td class=\"tab\"><div id=\"user\" class=\"tabcontent\"><a class=\"tab\" href=\"javascript:loadSection('user')\">Mitglieder/Benutzer</a></div></td>");
			
			//html.append("<div id=\"user3\" class=\"menuitem\"><a class=\"tab\" href=\"javascript:loadSection('user3')\">Mitglieder/Benutzer</a></div>");
			
			//html.append("<td class=\"tab\"><div id=\"user2\" class=\"tabcontent\"><a class=\"tab\" href=\"javascript:loadSection('user2')\">Mitglieder/Benutzer2</a></div></td>");
			
			html.append(getMenuButton("memberads", "loadSection('memberads')", "Inserate", "megaphone.png"));
			
			html.append(getMenuButton("organisations", "loadSection('organisations')", "Stiftungen", "organisations.png"));
			html.append(getMenuButton("projects", "loadSection('projects')", "Projekte", "projects.png"));
				
			html.append("<div id=\"requests\" class=\"menuitem\"><a class=\"tab\" href=\"javascript:loadSection('requests')\">Adressbestellungen</a></div>");
			//html.append("<div id=\"feedback\" class=\"menuitem\"><a class=\"tab\" href=\"javascript:loadSection('feedback')\">Feedback</a></div>");
			
			html.append(getMenuButton("imap", "loadSection('imap')", "Email", "email.png"));
			
			html.append(getMenuButton("newsletter", "loadSection('newsletter')", "Newsletter", "email.png"));

			html.append("<div id=\"documents\" class=\"menuitem\"><a class=\"tab\" href=\"javascript:loadSection('documents')\">Dokumente</a></div>");	

			html.append(getMenuButton("statistics", "loadSection('statistics')", "Statistik", "statistics.png"));
			html.append(getMenuButton("accounting", "loadSection('accounting')", "Zahlungen", "accounting.png"));
									
			html.append(getMenuButton("cheques", "loadSection('cheques')", "Nachbarcheques", "cheques.png"));
			
			html.append(getMenuButton("calendar", "loadSection('calendar')", "Aktuelles und Agenda", "calendar.png"));

			html.append(getMenuButton("cms", "loadSection('cms')", "CMS", "cms.png"));
			

			//html.append("<td class=\"tab\"><div id=\"commercialads\" class=\"tabcontent\"><a class=\"tab\" href=\"javascript:loadSection('commercialads')\">Bez. Anzeigen</a></div></td>");
			html.append("<div id=\"batchactivities\" class=\"menuitem\"><a class=\"tab\" href=\"javascript:loadSection('batchactivities')\">Serienbriefe</a></div>");
									

			
			html.append("<div id=\"administration\" class=\"menuitem\"><a class=\"tab\" href=\"javascript:toggleMenu('administration')\">Verwaltung</a>");
			
			html.append("<div id=\"administration_items\">");
			
			//if(((WebApplicationContext)context).getRequest().getSession().getAttribute("issysadm") != null){
				
				html.append("<div id=\"documenttemplates\" class=\"submenuitem\"><a class=\"tab\" href=\"javascript:loadSection('oma')\">Profileinstellungen</a></div>");
				
				html.append("<div id=\"memberadcategories\" class=\"submenuitem\"><a class=\"tab\" href=\"javascript:loadSection('memberadcategories')\">Inseratrubriken</a></div>");
													
				html.append("<div id=\"objecttemplates\" class=\"submenuitem\"><a class=\"tab\" href=\"javascript:loadSection('objecttemplates')\">Objektvorlagen</a></div>");

				html.append("<div id=\"libreoffice\" class=\"submenuitem\"><a class=\"tab\" href=\"javascript:loadSection('libreoffice')\">LibreOffice Vorlagen</a></div>");
				
				html.append("<div id=\"documenttemplates\" class=\"submenuitem\"><a class=\"tab\" href=\"javascript:loadSection('documenttemplates')\">Dokumentvorlagen</a></div>");
				
				html.append("<div id=\"pdf\" class=\"submenuitem\"><a class=\"tab\" href=\"javascript:loadSection('pdf')\">PDF</a></div>");
				
				html.append("<div id=\"textblocks\" class=\"submenuitem\"><a class=\"tab\" href=\"javascript:loadSection('textblocks')\">Texte</a></div>");
				html.append("<div id=\"files\" class=\"submenuitem\"><a class=\"tab\" href=\"javascript:loadSection('files')\">Dateien</a></div>");

				
	
		
				//html.append("<td class=\"tab\"><div id=\"email\" class=\"tabcontent\"><a class=\"tab\" href=\"javascript:loadSection('email')\">Pending Mails</a></div></td>");
										
			//}
			
			html.append("</div>");
			
			html.append("</div>");
		}
		else if(userSession.isCMSAdmin()){
			html.append("<div id=\"cms\" class=\"tabcontent\"><a class=\"tab\" href=\"javascript:loadSection('cms')\">CMS</a></div>");			
		}

		return html.toString();		
		
	}
	
	public String getMenuButton(String id, String command, String label, String icon){
		String iconstring = "";
		if(icon != null){
			iconstring = "<img class=\"menuicon\" src=\"images/icons/" + icon + "\">";
		}
		String menudef = "<div id=\"" + id + "\" class=\"menuitem\" onclick=\"" + command  + "\">" + iconstring + label + "</div>";
		
		return menudef;
	}
	
	
}