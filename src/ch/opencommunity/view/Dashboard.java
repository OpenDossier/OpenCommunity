package ch.opencommunity.view;

import ch.opencommunity.server.OpenCommunityServer;
import ch.opencommunity.common.OpenCommunityUserSession;

import org.kubiki.application.ApplicationContext;

public class Dashboard{
	
	public static String toHTML(OpenCommunityServer ocs, ApplicationContext context){
		
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
		
		StringBuilder html = new StringBuilder();
								
		html.append("<div class=\"dashboard\">");
		html.append("<div class=\"tabletoolbar\">");					
		html.append("<input class=\"labelbutton\" type=\"button\" value=\"Neu registrierte Benutzer\">");
		html.append("</div>");
		html.append("<div style=\"height : 300px; border : 1px solid black; overflow : auto;\">");
								//html.append(getOrganisationMemberList(context, " WHERE t1.Status IN (0,3,4)", "home", true));
		html.append(ocs.getOrganisationMemberList(context, " WHERE t1.Status IN (0, 3, 4)", "home", true));
		html.append("</div>");
								

		html.append("<div class=\"tabletoolbar\">");					
		html.append("<input class=\"labelbutton\" type=\"button\" value=\"Neue Inserate\">");								
		html.append("<input class=\"actionbutton\" type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.process.MemberAdSendRenewalRequest\',\'Mode=1\')\" value=\"Verlängerung anfragen per Email\">");
		html.append("<input class=\"actionbutton\" type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.process.MemberAdSendRenewalRequest\',\'Mode=2\')\" value=\"Verlängerung anfragen per Brief\">");
		html.append("<input class=\"actionbutton\" type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.process.MemberAdSendRenewalRequest\',\'Mode=3\')\" value=\"Abgelaufene Inserate löschen\">");
		html.append("</div>");		
		
		MemberAdList memberAdList = (MemberAdList)ocs.querylists.get("memberAdList");
		html.append("<div style=\"position : relative; height : 300px; border : 1px solid black; overflow : auto;\">");
		context.addProperty("sectionid", "String", "home");
		context.addProperty("filterstring", "String", " WHERE t1.Status in (0, 4)");
		memberAdList.getDataTable(html, context);
		//html.append(MemberAdList.getMemberAdList(this, context, userSession, " WHERE t1.Status in (0,4)", "home"));
		html.append("</div>");
										

								
		html.append("<div style=\"height : 200px; border : 1px solid black; overflow : auto; width: 49%; float : left;\">");
		html.append("<div class=\"tabletoolbar\">");	
		html.append("<input class=\"labelbutton\" type=\"button\" value=\"Neue Adressbestellungen\">");	
		html.append("<input class=\"actionbutton\" type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.process.SendRequestConfirmation\',\'Mode=2\')\" value=\"Adressbesteller benachrichtigen\">");
		html.append("<input class=\"actionbutton\" type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.process.SendRequestConfirmation\',\'Mode=4\')\" value=\"Feedback anfragen\">");
		html.append("</div>");	
		html.append(ocs.getMemberAdRequestList2(context, userSession, " WHERE t6.Status IN (0, 4)", "home", true));
		html.append("</div>");
		
		html.append("<div style=\"height : 200px; border : 1px solid black; overflow : auto; width: 49%; float : right;\">");
		html.append("<div class=\"tabletoolbar\">");	
		html.append("<input class=\"labelbutton\" type=\"button\" value=\"Neue Feedbacks\">");	
		//html.append("<input class=\"actionbutton\" type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.process.SendRequestConfirmation\',\'Mode=2\')\" value=\"Adressbesteller benachrichtigen\">");
		//html.append("<input class=\"actionbutton\" type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.process.SendRequestConfirmation\',\'Mode=4\')\" value=\"Feedback anfragen\">");
		html.append("</div>");	
		html.append(ocs.getFeedbackList(context, userSession, null));
		html.append("</div>");
		
		//html.append(getFeedbackList(context, userSession, null));


		html.append("</div>");
		
		return html.toString();		
		
	}
	
}