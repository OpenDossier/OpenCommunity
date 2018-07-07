package ch.opencommunity.mail;

import org.kubiki.base.*;
import org.kubiki.base.ActionResult.*;
import org.kubiki.mail.*;
import org.kubiki.application.ApplicationContext;
import org.kubiki.application.server.WebApplicationContext;
import org.kubiki.application.server.WebApplicationModule;

import org.kubiki.groupware.MailAttachment;

import ch.opencommunity.server.*;
import ch.opencommunity.common.*;

import ch.opencommunity.dossier.ProjectAdministration;

import javax.mail.*;

import java.text.*;
import java.util.Hashtable;
import java.util.List;

import javax.mail.internet.*;


import java.io.PrintWriter;
import java.io.OutputStream;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import microsoft.exchange.webservices.data.FileAttachment;


public class EmailAdministrationForm extends WebApplicationModule{

	AbstractEmailAdministration ea;

	DateFormat dateFormat;
	
	OpenCommunityServer ods;
	
	ProjectAdministration projectAdministration;

	public EmailAdministrationForm(){

		dateFormat = new SimpleDateFormat("dd.MM hh:mm");

	}
	public void initModule(){
		ods = (OpenCommunityServer)getParent();
		projectAdministration = (ProjectAdministration)ods.getObjectByName("ApplicationModule", "Projects");
	}
	public ActionResult onAction(BasicClass source, String command, ApplicationContext context) {


		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");

		ea = (AbstractEmailAdministration)context.getObject("emailadministration");

		ActionResult result = null;
		String target = null;

		if(ea == null){
			result = new ActionResult(Status.FAILED, "Keine Emailanbindung gefunden");
		}
		else{

			ods.logAccess(command);
			String redirect = null;
			String mailcommand = context.getString("mailcommand");

			ods.logAccess("mailcommand: " + mailcommand);	
			
			if(command.equals("importemail")){
				String messageid = context.getString("messageid");
				String projectid = context.getString("projectid");
				if(messageid != null && projectid != null){
					Object o = ea.getMessageByID(messageid);
					if(o != null && o instanceof javax.mail.internet.MimeMessage){
						String id = ods.insertMailMessageInstance((javax.mail.internet.MimeMessage)o, null, null, projectid);
						if(id != null && id.length() > 0){
							
							try{
							
								ea.moveMessage((javax.mail.internet.MimeMessage)o, "Archiv");
								
								//ea.clearCurrentFolder();
								
								result = new ActionResult(Status.OK, "Liste geladen");
								result.setParam("dataContainer", "maillistcontent");
								result.setData(getMailListContent(context, target));
								
							}
							catch(java.lang.Exception e){
								ods.logException(e);	
							}
						}
					}
					
				}
				
			}
			else if(mailcommand.equals("importmail")){
				String messageid = context.getString("messageid");
				Object message = ea.getMessageByID(messageid);
				Hashtable properties = new Hashtable();
				properties.put("EmailAdministration", ea); 
				if(message !=null){
					properties.put("EmailMessage", message); 
				}
				String CaseRecordID=context.getString("CaseRecordID");
				String CaseRecordTitle=context.getString("CaseRecordTitle");
				String MeasureID=context.getString("MeasureID");
				String OrganisationID=context.getString("OrganisationID");

				if(CaseRecordID != null && CaseRecordTitle != null && MeasureID != null){
					properties.put("CaseRecordID", CaseRecordID);
					properties.put("CaseRecordTitle", CaseRecordTitle);
					properties.put("MeasureID", MeasureID);
					properties.put("OrganisationID", OrganisationID);
				}

				result = ods.startProcess("ch.opencommunity.mail.ImportMail", "", true, userSession, properties, context, this);
			}
			else{
				if(mailcommand.equals("loadmessages")){
					try{
						target = context.getString("target");
						ea.connect();
						
						ea.loadMessages();
						
					}
					catch(java.lang.Exception e){
						ods.logException(e);
					}
					result = new ActionResult(Status.OK, "Liste geladen");
					result.setParam("dataContainer", "mailform");
					result.setData(getMailList(context, target));

				}
				else if(mailcommand.equals("sortmessages")){
					try{
						String criteria = context.getString("criteria");
						String direction = context.getString("direction");
						ea.sortMessages(criteria, direction);
					}
					catch(java.lang.Exception e){
						ods.logException(e);
					}
					result = new ActionResult(Status.OK, "Liste geladen");
					result.setParam("dataContainer", "mailform");
					result.setData(getMailList(context, target));

				}
				else if(mailcommand.equals("nextpage")){
					ea.nextPage();
					result = new ActionResult(Status.OK, "Liste geladen");
					result.setParam("dataContainer", "maillistcontent");
					result.setData(getMailListContent(context, target));

				}
				else if(mailcommand.equals("previouspage")){
					ea.previousPage();
					result = new ActionResult(Status.OK, "Liste geladen");
					result.setParam("dataContainer", "maillistcontent");
					result.setData(getMailListContent(context, target));

				}
				else if(mailcommand.equals("changefolder")){
					String folder = context.getString("folder");
					if(folder != null){
						try{
							ea.setFolder(folder);
							ea.loadMessages();
						}
						catch(java.lang.Exception e){
							ods.logException(e);
						}
					}
					result = new ActionResult(Status.OK, "Liste geladen");
					result.setParam("dataContainer", "maillistcontent");
					result.setData(getMailListContent(context, target));
				}
				else if(mailcommand.equals("searchemails")){
					try{
						String searchsender = context.getString("searchsender");
						String searchreceiver = context.getString("searchreceiver");
						String searchdate = context.getString("searchdate");
						String searchsubject = context.getString("searchsubject");
						ods.logAccess(searchsender + " " + searchdate + " " + searchsubject);
						ea.setSearchTerms(searchsender, searchreceiver, searchdate, searchsubject);
						ea.loadMessages();
						result = new ActionResult(Status.OK, "Liste geladen");
						result.setParam("dataContainer", "maillistcontent");
						result.setData(getMailListContent(context, target));
					}
					catch(java.lang.Exception e){
						ods.logException(e);	
					}
				}

			}
		}
		return result;
	}
	public void openAttachment(HttpServletRequest request, HttpServletResponse response){

		OpenCommunityServer ods = (OpenCommunityServer)getParent();

		OpenCommunityUserSession userSession = (OpenCommunityUserSession)request.getSession().getAttribute("usersession");

		try{
			ods.logAccess("opening attachment2");

			String attachmentid = request.getParameter("attachmentid");
			ImportMail importMail = (ImportMail)userSession.getCurrentProcess();
			MessageWrapper wrapper = importMail.getMessageWrapper();

			if(wrapper != null){

				int i = 0;

				for(Object o : wrapper.getAttachments()){
					i++;
					if(("" + i).equals(attachmentid)){

						//PrintWriter writer = response.getWriter();

						
						
						if(o instanceof MailAttachment){

							MailAttachment attachment = (MailAttachment)o;

							String title = attachment.getFileName();

							response.setHeader("Content-Disposition", "attachment; filename=\"" + title + "\"" );

							if(title.toLowerCase().endsWith(".pdf")){

								response.setContentType("application/pdf");
								
							}
							else{

								response.setContentType("application/octet-stream");

							}

							OutputStream out = response.getOutputStream();
							InputStream is = attachment.getContent().getInputStream();
							

							int bufferSize = 1024;
							byte[] buffer = new byte[bufferSize];
							int len = 0;
							//while ((len = is.read(buffer)) != -1) {
							//	out.write(buffer, 0, len);
							//}

							int b;
							while((b = is.read()) != -1){
								out.write(b);				
							}
							out.flush();
							out.close();
						}
						else if(o instanceof FileAttachment){
							FileAttachment attachment = (FileAttachment)o;
							attachment.load();
							String title = attachment.getName();

							if(title.toLowerCase().endsWith(".pdf")){

								response.setContentType("application/pdf");
								
							}
							else{

								response.setContentType("application/octet-stream");

							}

							response.setHeader("Content-Disposition", "attachment; filename=\"" + title + "\"" );
							OutputStream out = response.getOutputStream();
							out.write(attachment.getContent());
							out.flush();
							out.close();
						}

					}
				}
			}
		}
		catch(java.lang.Exception e){
			ods.logException(e);
		}

	}
	public String getMailList(ApplicationContext context, String target){

		StringBuilder html = new StringBuilder(); 
		
		IMAPEmailAdministration imap = (IMAPEmailAdministration)ea;
		
		//html.append(imap.getMessages().length);
		//html.append(getPath());
		
		html.append("<table><tr><td valign=\"top\">");

		try{
			

			html.append("Ordner ausw&auml;hlen: <select onchange=\"callEmailAdministration(\'mailcommand=changefolder\', this.value)\">");
			List folders = ea.getFolderList();
			if(folders != null){
				for(Object folder : folders){
					if(folder.equals(ea.getCurrentFolder())){
						html.append("<option value\"" + folder + "\" SELECTED>" + folder + "</option>");
					}
					else{
						html.append("<option value\"" + folder + "\">" + folder + "</option>");
					}
				}
			}
			html.append("</select>");
			
			html.append("<table>");
			html.append("<tr>");
		    html.append("<th>Absender<a href=\"javascript:callEmailAdministration(\'mailcommand=sortmessages&criteria=sender&direction=down\')\"><img src=\"images/sort_down.gif\"></a><a href=\"javascript:callEmailAdministration(\'mailcommand=sortmessages&criteria=sender&direction=up\')\"><img src=\"images/sort_up.gif\"></a>");
		    html.append("<th>Empfänger<a href=\"javascript:callEmailAdministration(\'mailcommand=sortmessages&criteria=receiver&direction=down\')\"><img src=\"images/sort_down.gif\"></a><a href=\"javascript:callEmailAdministration(\'mailcommand=sortmessages&criteria=reciever&direction=up\')\"><img src=\"images/sort_up.gif\"></a>");
			html.append("<th>Datum<a href=\"javascript:callEmailAdministration(\'mailcommand=sortmessages&criteria=date&direction=down\')\"><img src=\"images/sort_down.gif\"></a><a href=\"javascript:callEmailAdministration(\'mailcommand=sortmessages&criteria=date&direction=up\')\"><img src=\"images/sort_up.gif\"></a>");
		    html.append("<th>Betreff<a href=\"javascript:callEmailAdministration(\'mailcommand=sortmessages&criteria=subject&direction=down\')\"><img src=\"images/sort_down.gif\"></a><a href=\"javascript:callEmailAdministration(\'mailcommand=sortmessages&criteria=subject&direction=up\')\"><img src=\"images/sort_up.gif\"></a>");
		    html.append("</tr>");
		    
		    html.append("<form id=\"emailsearchform\" name=\"emailsearchform\">");
			html.append("<tr>");
				
			html.append("<td><input id=\"searchsender\" name=\"searchsender\"></td>");
			html.append("<td><input id=\"searchreceiver\" name=\"searchreceiver\"></td>");
			html.append("<td><input id=\"searchdate\" name=\"searchdate\"></td>");
			html.append("<td><input id=\"searchsubject\" name=\"searchsubject\"></td>");
			html.append("<td></td>");
			html.append("<td><input type=\"button\" value=\"Nachrichten suchen\" onclick=\"searchEmails()\"></td>");
			html.append("</tr>");
			html.append("</form>");
				
			html.append("</table>");
				
			html.append("<div id=\"maillistcontent\">");
			
			html.append(getMailListContent(context, target));

			html.append("</div>");
			
			html.append("</td><td valign=\"top\">");
			html.append("<div style=\"position : relative; height : 600px; width : 100%; overflow :auto;\">");
			
			html.append(getTargetList(context));
			
			html.append("</div>");
			
			html.append("</td></tr></table>");

		}
		catch(java.lang.Exception e){
			//html.append(e.toString());
			OpenCommunityServer ocs = (OpenCommunityServer)getParent();
			ocs.logException(e);
		}
		return html.toString();
	}
	private String getMailListContent(ApplicationContext context, String target){
		
		StringBuilder html = new StringBuilder();
		
		html.append("<table>");
		
		boolean even = false;

		while(ea.nextMessage()){
			
			//if(!ea.isDeleted()){
				boolean isDeleted = ea.isDeleted();
				if(isDeleted){
					html.append("<tr class=\"inactive\">");
				}
				else if(even){
					html.append("<tr class=\"even\">");
				}
				else{
					html.append("<tr class=\"odd\">");
				}
				even = !even;
				//html.append("<td>" + ea.isDeleted() + "</td>");
				//html.append("<td>" + ea.getContentType() + "</td>");
				html.append("<td>" + ea.getSender() + "</td>");
	
				html.append("<td>" + ea.getDate() + "</td>");
				html.append("<td>" + ea.getSubject() + "</td>");
				
				try{
					
					if(ea.hasAttachment()){
						html.append("<td>A</td>");
					}
					else{
						html.append("<td></td>");
					}
					
					
					
				}
				catch(java.lang.Exception e){
					html.append("<td></td>");				
				}
				if(target != null && target.equals("process")){
					html.append("<td><a href=\"javascript:getNextNode('messageid=" + ea.getMessageID() + "\')\">Importieren</a></td>");
				}
				else{
	
					//html.append("<td><a href=\"javascript:getNextNode('importmail=true&messageid=" + ea.getMessageID() + "\')\">Importieren</a></td>");
				}
				if(!isDeleted){
					html.append("<td><div id=\"" + ea.getMessageID() + "\" draggable=\"true\" ondragstart=\"dragMessage(event, this.id)\" class=\"importbutton\">Import</div></td>");
				}
				
				html.append("</tr>");
				
			//}
		}		
		html.append("</table>");
		html.append("<input type=\"button\" value=\"<<\" onclick=\"callEmailAdministration(\'mailcommand=previouspage\')\">");
		html.append("<input type=\"button\" value=\">>\" onclick=\"callEmailAdministration(\'mailcommand=nextpage\')\">");
		return html.toString();
		
	}
	public String getMainForm(ApplicationContext context){
		OpenCommunityServer ods = (OpenCommunityServer)getParent();
		StringBuilder html = new StringBuilder();
		html.append("<p class=\"homeTitle\">Email</p>");
		html.append("<div id=\"mailform\">");



		html.append("</div>");

		html.append("<script language=\"javascript\">");
		html.append("callEmailAdministration(\'mailcommand=loadmessages\');");
		html.append("</script>");

		return html.toString();
	}
	public String getTargetList(ApplicationContext context){
		
		StringBuilder html = new StringBuilder();
		
		html.append(projectAdministration.getProjectList(context));
		
		return html.toString();		
		
	}
	public String toString(){
		return "EmailAdministrationForm";
	}
} 
