package ch.opencommunity.mail;

import org.kubiki.servlet.HtmlFieldWidget;
import org.kubiki.base.BasicClass;
import org.kubiki.base.Property;
import org.kubiki.base.ConfigValue;
import org.kubiki.application.ApplicationContext;

//import org.opendossier.dossier.*;
import ch.opencommunity.common.*;

import org.kubiki.mail.*;
import org.kubiki.groupware.*;

import java.util.Vector;
import java.util.List;

import microsoft.exchange.webservices.data.*;




public class EmailView extends HtmlFieldWidget{
	

	
	public EmailView(){
		

		
	}
	
	public String renderWidget(BasicClass bc, Property p, boolean isEditable, ApplicationContext context, String prefix) {

		StringBuilder html = new StringBuilder(); 

		try{

			//OpenDossierServer ods = (OpenDossierServer)bc.getRoot();

			ImportMail importMail = (ImportMail)bc.getParent();

			OpenCommunityUserSession usersession = (OpenCommunityUserSession)context.getObject("usersession");
			

			MessageWrapper wrapper = importMail.getMessageWrapper();

			if(wrapper != null){

				html.append("<table>");
				html.append("<tr><td colspan=\"2\">Wählen Sie einen Fall mit Hilfe der Fallsuche aus, oder wählen einen Fall aus den geöffneten Fällen</td></tr>");
				html.append("<tr><td>" + importMail.getString("CaseRecordTitle") + importMail.getString("CaseRecordID") + "<input type=\"button\" onclick=\"getNextNode(\'startsubprocess=true\')\" value=\"Fall auswählen\"></td></tr>");
				html.append("<form name=\"processNodeForm\">");
				/*
				for(Dossier dossier : usersession.getOpenDossiers()){
					for(BasicClass caseRecord : dossier.getObjects("CaseRecord")){
						html.append("<tr><td><input name=\"CaseRecordID\" type=\"radio\" value=\"" + dossier.getName() + "_" + caseRecord.getName() + "\">" + dossier + "," + caseRecord + "</td></tr>");

					}
				}
				*/


				html.append("<tr><th>Absender</th><td>" + wrapper.getSender() + "</td></tr>"); 
				html.append("<tr><th>Datum</th><td>" + wrapper.getDate() + "</td></tr>"); 
				html.append("<tr><th>Betreff</th><td>" + wrapper.getSubject() + "</td></tr>"); 
				html.append("<tr><th>Attachments</th><td>");
				int i = 0;
				for(Object o : wrapper.getAttachments()){
					if(o instanceof MailAttachment){
						i++;
						html.append("<input type=\"checkbox\" value=\"true\" name=\"attachment_" + i + "\">" + ((MailAttachment)o).getFileName());
					}
					else if(o instanceof FileAttachment){ //Exchange
						i++;
						html.append("<input type=\"checkbox\" value=\"true\" name=\"attachment_" + i + "\">" + ((FileAttachment)o).getName());
					}
					html.append(" Neuer Titel: <input name=\"attachmenttitle_" + i + "\">  <a href=\"servlet?action=openattachment&attachmentid=" + i + "\" target=\"_blank\">Vorschau</a><br>");
				}
				html.append("</td></tr>"); 
				html.append("<tr><th>Nur Attachment importieren</th><td>");
				if(importMail.getBoolean("AttachmentOnly")){
					html.append("<input type=\"radio\" name=\"AttachmentOnly\" value=\"true\" checked>Ja");
					html.append("<input type=\"radio\" name=\"AttachmentOnly\" value=\"false\">Nein");
				}
				else{
					html.append("<input type=\"radio\" name=\"AttachmentOnly\" value=\"true\">Ja");
					html.append("<input type=\"radio\" name=\"AttachmentOnly\" value=\"false\" checked>Nein");		
				}
				html.append("</td></tr>"); 
				html.append("<tr><th>Aktivi&auml;t Vorlage</th><td>");
				html.append("<select name=\"ActivityTemplate\">");
				for(Object o : importMail.getTemplates()){
					ConfigValue cv = (ConfigValue)o;
					html.append("<option value=\"" + cv.getValue() + "\">" + cv.getLabel() + "</option>");
				}
				html.append("</select>");
				html.append("</td></tr>"); 

				html.append("<tr><th>In Zielordner verschieben</th><td>");
				
				//String emailImportTargetFolder = usersession.getLogin().getSetting("EmailImportTargetFolder");
				String emailImportTargetFolder = "INBOX";

				html.append("<select name=\"TargetFolder\">");
				List folders = importMail.getEmailAdministration().getFolderList();
				if(folders != null){
					for(Object folder : folders){
						//if(folder.equals(ea.getCurrentFolder())){
						//	html.append("<option value\"" + folder + "\" SELECTED>" + folder + "</option>");
						//}
						//else{
							if(emailImportTargetFolder.equals(folder.toString())){
								html.append("<option value\"" + folder + "\" SELECTED>" + folder + "</option>");	
							}
							else{
								html.append("<option value\"" + folder + "\">" + folder + "</option>");
							}
						//}
					}
				}
				html.append("</select>");
				html.append("</td></tr>");

				html.append("</form>");
				html.append("<tr><th>Inhalt</th><td>" + wrapper.getMessageBody().replaceAll("(?s)<[^>]*>(\\s*<[^>]*>)*", " ") + "</td></tr>"); 
				html.append("</table>");
			}
		}
		catch(java.lang.Exception e){
			html.append(e.toString());
		}
		
		return html.toString();

	}

}
