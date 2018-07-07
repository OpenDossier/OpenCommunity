package ch.opencommunity.view;

import ch.opencommunity.advertising.*;
import ch.opencommunity.base.*;
import ch.opencommunity.process.*;
import ch.opencommunity.server.OpenCommunityServer;
import ch.opencommunity.server.HTMLForm;
import ch.opencommunity.dossier.*;

import org.kubiki.servlet.HtmlFieldWidget;
import org.kubiki.base.ObjectCollection;
import org.kubiki.base.BasicClass;
import org.kubiki.base.Property;
import org.kubiki.application.ApplicationContext;
import org.kubiki.util.DateConverter;
import org.kubiki.cms.FileObject;
import org.kubiki.gui.html.HTMLFormManager;

import java.util.Vector;
import java.util.Map;

public class ActivityView extends HtmlFieldWidget{
	
	public String renderWidget(BasicClass bc, Property p, boolean isEditable, ApplicationContext context, String prefix){
		StringBuilder html = new StringBuilder();
		
		return html.toString();
	}
	public static void toHTML(StringBuilder html, OrganisationMemberController omc, Activity activity, ApplicationContext context){
		
		OpenCommunityServer ocs = (OpenCommunityServer)activity.getRoot();
		
		OrganisationMember om = omc.getOrganisationMember();
		
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
		
		ObjectTemplate ot = (ObjectTemplate)activity.getObject("Template");
		
		for(BasicClass note : activity.getObjects("Note")){
					
			FieldDefinition fieldDefinition = null;
			if(note.getObject("Template") instanceof FieldDefinition){
				fieldDefinition = (FieldDefinition)note.getObject("Template");
			}
			if(fieldDefinition == null){
				if(ot != null){
					fieldDefinition = (FieldDefinition)ot.getObjectByName("FieldDefinition", "" + note.getID("Template"));	
				}
			}
					
			Property p = omc.getProperty("activity_note_" + i);
					
			String propertyid = HTMLForm.createId(p);
					
			html.append("<tr><td></td><td><select onchange=\"getTextblockContent(this.value,'" + propertyid + "')\">");
					
			for(BasicClass textblock : ocs.getTextblocks(1, fieldDefinition.getID("Context"))){
				html.append("<option value=\"" + textblock.getName() + "\">" + textblock.getString("Title") + "</option>");	
			}
					
			html.append("</select>" + fieldDefinition + "</td></tr>");
			
			html.append("<tr><td></td><td><select onchange=\"getTextblockContent(this.value,'" + propertyid + "')\">");
					
			for(BasicClass textblock : ocs.getTextblocks(1, fieldDefinition.getID("Context"))){
				html.append("<option value=\"" + textblock.getName() + "\">" + textblock.getString("Title") + "</option>");	
			}
					
			html.append("</select>" + fieldDefinition + "</td></tr>");
			
			html.append("<tr><th>" + note.getString("Title") + "</th><td>" + HTMLForm.getTextArea(p, true) + "</td></tr>");
					
			i++;
	
		}
				try{
					
					if(activity.getID("Template")==1 || activity.getID("Template")==4){ //Email
						
						html.append("<tr><td>Attachment</td><td>");
						html.append("<select name=\"Attachments\">");
						
						html.append("<option value=\"\"></option>");
						
						Vector files = ocs.getFiles();
						if(files != null){
							for(Object o : files){
								FileObject fileObject = (FileObject)o;
								if(fileObject.getID("Type")==2){
									String path = "websites/1/files/" + fileObject.getString("FileName");
									if(path.equals(activity.getString("Attachments"))){
											html.append("<option value=\"" + path + "\" SELECTED>" + fileObject.getString("Title") + "</option>");
									}
									else{
											html.append("<option value=\"" + path + "\">" + fileObject.getString("Title") + "</option>");										
									}
								}
							}
							
						}
						
						
						html.append("</select>");
						
						html.append("</td></tr>");
						
					}

				
					if(activity.getID("Template")==7 || activity.getID("Template")==8){
						
						html.append("<tr><th>Kontext</th><td>" + HTMLForm.getTextField(omc.getProperty("activity_context"), true, "") + "</td></tr>");
						
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
				

				}
				catch(java.lang.Exception e){
					ocs.logException(e);	
				}
				html.append("</table>");	
				
				html.append("<input type=\"button\" value=\"Zur&uuml;ck\" onclick=\"getNextNode('overview=true')\">");
				html.append("<input type=\"button\" value=\"Speichern\" onclick=\"getNextNode('saveactivity=true')\">");
				if(activity.getID("Template")==1 || activity.getID("Template")==4){
					html.append("<input type=\"button\" value=\"Speichern und senden\" onclick=\"getNextNode('saveactivity=true&send=true')\">");
				}
				else if(activity.getID("Template")==7 || activity.getID("Template")==8){
					html.append("<input type=\"button\" value=\"Speichern und PDF senden\" onclick=\"getNextNode('saveactivity=true&sendpdf=true')\">");
				}
	
				html.append("</form>");		
		
		
		
		
	}
	public static void toHTML2(StringBuilder html, BaseController controller, Activity activity, ApplicationContext context){
		
		OpenCommunityServer ocs = (OpenCommunityServer)activity.getRoot();
		
	
		ObjectTemplate ot = (ObjectTemplate)activity.getObject("Template");
		
		int template = activity.getID("Template");

		if(activity.getName().equals("-1")){
			html.append("<div class=\"object_toolbar\" style=\"background : red;\">");
		}
		else{
			html.append("<div class=\"object_toolbar\">");
		}
		
		
		
		html.append("<input type=\"button\" class=\"actionbutton\" value=\"Speichern\" onclick=\"saveObject('" + activity.getPath() + "')\">");
		html.append("<input type=\"button\" class=\"actionbutton\" value=\"Abbrechen\" onclick=\"onAction('" + controller.getPath() + "','cancelactivity')\">");
		if(activity.getID("Template")==1 || activity.getID("Template")==4){
			html.append("<input type=\"button\" class=\"actionbutton\" value=\"Speichern und senden\" onclick=\"saveObject('" + activity.getPath() + "',null, 'false', 'send=true')\">");
		}
		if(ot.getBoolean("IsSendable") && ot.getBoolean("IsPrintable")){
			html.append("<input type=\"button\" class=\"actionbutton\" value=\"Speichern und PDF senden\" onclick=\"saveObject('" + activity.getPath() + "',null, 'false', 'sendpdf=true')\">");
		}
		
		if(activity.getID("Template")==7 || activity.getID("Template")==8){
			html.append("<input type=\"button\" class=\"actionbutton\" value=\"PDF erstellen\" onclick=\"saveObject('" + activity.getPath() + "',null, 'false', 'createpdf=true')\">");	
		}
		
		//html.append(activity.getPath());
		
		html.append("</div>");
		

		html.append("\n<form action=\"servlet\" name=\"objectEditForm_" + activity.getPath() + "\" id=\"objectEditForm_" + activity.getPath() + "\">");
		
		html.append("\n<input type=\"hidden\" name=\"action\" value=\"getObjectAction\">");	
		html.append("\n<input type=\"hidden\" name=\"command\" value=\"saveactivity\">");	
		html.append("\n<input type=\"hidden\" name=\"activityid\" value=\"" + activity.getName() + "\">");
		html.append("\n<input type=\"hidden\" name=\"activityPath\" value=\"" + activity.getPath("") + "\">");
		html.append("\n<input type=\"hidden\" name=\"objectPath\" value=\"" + controller.getPath("") + "\">");

		//if(!activity.getName().equals("-1")){
			//if(template==1 || template==7 || template==8){
			if(ot.getBoolean("IsSendable")){
				if(controller instanceof DossierController){
					
					Map functionMap = 	ocs.getFunctions().getCodeMap();	
					
					for(BasicClass recipient : activity.getObjects("ActivityParticipant")){
						html.append(recipient.getString("Title") + " <a href=\"javascript:onAction('" + controller.getPath() + "','recipientdelete','', 'activityPath=" + activity.getPath() + "&recipientid=" + recipient.getName() + "')\"><img src=\"images/delete_small.png\"></a><br>");
					}
					
					
					
					html.append("Empfänger. hinzufügen <select onchange=\"onAction('" + controller.getPath() + "','recipientadd','','activityPath=" + activity.getPath() + "&omid=' + this.value)\">");
					html.append("<option value=\"\"></option>");
					html.append("<option value=\"0\">Institution allgemein</option>");
					for(BasicClass person : controller.getObjects("organisationmembers")){
						
						html.append("<option value=\"" + person.getString("ID") + "\">" + person.getString("FIRSTNAME") + " " + person.getString("FAMILYNAME") + "</option>");
						
						
					}	
					
					html.append("</select>");
					

		
				}
			}
		//}
		
		html.append("<table>");
		html.append("<tr><th>Bezeichnung</th><td>" + HTMLForm.getTextField(activity.getProperty("Title"), true, "") + "</td></tr>");
		
		//if(ot.getName().equals("12") || ot.getName().equals("17")){
		if(ot.getBoolean("ShowDate")){
			html.append("<tr><th>Datum</th><td>" + HTMLForm.getDateField(activity.getProperty("Date"), true, "") + "</td></tr>");
		}
		
		//if(ot.getObjects("StatusDefinition").size() > 0){
		if(ot.getBoolean("ShowStatus")){
			html.append("<tr><th>Status</th><td>" + HTMLForm.getSelection(activity.getProperty("Status"), true, "") + "</td></tr>");
		}
				
		int i = 1;
		for(BasicClass parameter : activity.getObjects("Parameter")){
			
			FieldDefinition fd = null;
			if(parameter.getObject("Template") instanceof FieldDefinition){
				fd = (FieldDefinition)parameter.getObject("Template");	
			}
			
			if(fd.getID("Status")==0){
			
				Property p = parameter.getProperty("Value");
				if(p.getValues() != null){
					html.append("<tr><th>" + parameter.getString("Title") + "</th><td>" + HTMLForm.getSelection(p, true, "") + "</td></tr>");
				}
				else if(fd != null && fd.getCodeList().size() > 0){
					p.setSelection(fd.getCodeList());
					if(fd != null && parameter.getName().equals("")){
						html.append("<tr><th>" + parameter.getString("Title") + "</th><td>" + HTMLForm.getSelection(p , true, "parameter_" + fd.getName() + "_") + "</td></tr>");
					}
					else{
						html.append("<tr><th>" + parameter.getString("Title") + "</th><td>" + HTMLForm.getSelection(p , true, "parameter_" + parameter.getName() + "_") + "</td></tr>");
					}
				}
				else if(fd.getID("Type")==6){
					if(controller instanceof OrganisationMemberController){
						OrganisationMember om = ((OrganisationMemberController)controller).getOrganisationMember();
						Document doc = (Document)om.getObjectByName("Document", parameter.getString("Document"));
						if(doc != null){
							//html.append("<tr><th>" + parameter.getString("Title") + "</th><td><a href=\"javascript:openDocument(" + parameter.getString("Document") + ")\">Oeffnen</a></td></tr>");
							html.append("<tr><th>" + parameter.getString("Title") + "</th><td><a href=\"javascript:openDocument2('" + doc.getPath() + "')\">Oeffnen</a></td></tr>");
						}
						else{
							html.append("<tr><th>" + parameter.getString("Title") + "</th><td id=\"input_" + parameter.getPath() + "\"><input type=\"button\" class=\"actionbutton\" onClick=\"onAction('" + parameter.getPath() + "','documentadd')\" value=\"Dokument erstellen\"></td></tr>");					
						}
						
					}
					else if(controller instanceof DossierController){
						if(activity.getParent() instanceof CaseRecord){
							Dossier dossier = ((DossierController)controller).getDossier();
							CaseRecord caseRecord = (CaseRecord)activity.getParent();
							Document doc = (Document)caseRecord.getObjectByName("Document", "" + parameter.getID("Document"));
							if(doc != null){
									
								html.append("<tr><th>" + parameter.getString("Title") + "</th><td id=\"input_" + parameter.getPath() + "\"><input type=\"button\" class=\"actionbutton\" onClick=\"openDocument2('" + doc.getPath() + "')\" value=\"&Ouml;ffnen\"></td></tr>");
							}
							else{
								html.append("<tr><th>" + parameter.getString("Title") + "</th><td id=\"input_" + parameter.getPath() + "\"><input type=\"button\" class=\"actionbutton\" onClick=\"onAction('" + parameter.getPath() + "','documentadd')\" value=\"Dokument erstellen\"></td></tr>");						
								
							}
						}
						else if(activity.getParent() instanceof Project){
	
							Document doc = (Document)activity.getParent().getObjectByName("Document", "" + parameter.getID("Document"));
													
							if(doc != null){							
								html.append("<tr><th>" + parameter.getString("Title") + "</th><td id=\"input_" + parameter.getPath() + "\"><input type=\"button\" class=\"actionbutton\" onClick=\"openDocument2('" + doc.getPath() + "')\" value=\"&Ouml;ffnen\"></td></tr>");
							}
							else{
								html.append("<tr><th>" + parameter.getString("Title") + "</th><td id=\"input_" + parameter.getPath() + "\"><input type=\"button\" class=\"actionbutton\" onClick=\"onAction('" + parameter.getPath() + "','documentadd')\" value=\"Dokument erstellen\"></td></tr>");											
								
							}
						}
						
					}
					
				}
				else if(parameter.getString("Title").equals("Dokument")){
					if(controller instanceof OrganisationMemberController){
						OrganisationMember om = ((OrganisationMemberController)controller).getOrganisationMember();
						Document doc = (Document)om.getObjectByName("Document", parameter.getString("Document"));
						if(doc != null){
							//html.append("<tr><th>" + parameter.getString("Title") + "</th><td><a href=\"javascript:openDocument(" + parameter.getString("Document") + ")\">Oeffnen</a></td></tr>");
							html.append("<tr><th>" + parameter.getString("Title") + "</th><td><a href=\"javascript:openDocument2('" + doc.getPath() + "')\">Oeffnen</a></td></tr>");
						}
						
					}
				}
				else if(fd.getID("Type")==7){

					html.append("<tr><th>" + parameter.getString("Title") + "</th><td>" + HTMLFormManager.getFileUpload(parameter, p) + "</td></tr>");
					html.append("<tr><th></th><td id=\"filename_" + parameter.getName() + "\">");
					if(parameter.getID("ExternalDocument") > 0){
						
						html.append(parameter.getString("Comment") + " : <a href=\"servlet.srv?objectPath=" + parameter.getPath() + "&action=getObjectAction&command=filedownload\" target=\"_blank\">&Ouml;ffnen</a>");
					}
					html.append("</td></tr>");
				}
				else if(parameter.getString("Template").equals("28")){
	
					html.append("<tr><th>" + parameter.getString("Title") + " " + parameter.getString("Comment") + "</th><td><a href=\"javascript:openExternalDocument(" + parameter.getString("ExternalDocument") + ")\">Oeffnen</a></td></tr>");
	
				}
				else{
					if(fd != null && parameter.getName().equals("")){
						html.append("<tr><th>" + parameter.getString("Title") + "</th><td>" + HTMLForm.getTextField(p , true, "parameter_" + fd.getName() + "_") + "</td></tr>");
					}
					else{
						html.append("<tr><th>" + parameter.getString("Title") + "</th><td>" + HTMLForm.getTextField(p , true, "parameter_" + parameter.getName() + "_") + "</td></tr>");
					}
				}
						//getParent().addProperty("activity_note_" + i, "String", "");
			}
		}
		i = 1;
		

		
		for(BasicClass note : activity.getObjects("Note")){
					
			FieldDefinition fieldDefinition = null;
			if(note.getObject("Template") instanceof FieldDefinition){
				fieldDefinition = (FieldDefinition)note.getObject("Template");
			}
			if(fieldDefinition == null){
				if(ot != null){
					fieldDefinition = (FieldDefinition)ot.getObjectByName("FieldDefinition", "" + note.getID("Template"));	
				}
			}
			
			if(fieldDefinition.getID("Status")==0){
					
				Property p = note.getProperty("Content");
						
				String propertyid = HTMLForm.createId(p);
	
				
				html.append("<tr><th>" + note.getString("Title"));
				html.append("<select onchange=\"getTextblockContent(this.value,'" + propertyid + "')\" style=\"width: 100px;\">");
						
				for(BasicClass textblock : ocs.getTextblocks(1, fieldDefinition.getID("Context"))){
					html.append("<option value=\"" + textblock.getName() + "\">" + textblock.getString("Title") + "</option>");	
				}
						
				html.append("</select>");		
				
				html.append("<b>Textbausteine</b>");
				
				html.append("<select onchange=\"getTextblockContent2(this.value,'" + propertyid + "')\" style=\"width: 100px;\">");
						
				for(BasicClass textblock : ocs.getTextblocks(1, 4)){
					//if(textblock.getID("Type")==4){
						html.append("<option value=\"" + textblock.getName() + "\">" + textblock.getString("Title") + "</option>");	
					//}
				}
						
				html.append("</select>");
				html.append("</th><td>" + HTMLForm.getTextArea(p, true, "", "note_" + note.getName() + "_") + "</td></tr>");
						
				i++;
				
			}
	
		}
				try{
					
					if(activity.getID("Template")==1 || activity.getID("Template")==4){ //Email
						
						html.append("<tr><td>Attachment</td><td>");
						html.append("<select name=\"Attachments\">");
						
						html.append("<option value=\"\"></option>");
						
						Vector files = ocs.getFiles();
						if(files != null){
							for(Object o : files){
								FileObject fileObject = (FileObject)o;
								if(fileObject.getID("Type")==2){
									String path = "websites/1/files/" + fileObject.getString("FileName");
									if(path.equals(activity.getString("Attachments"))){
											html.append("<option value=\"" + path + "\" SELECTED>" + fileObject.getString("Title") + "</option>");
									}
									else{
											html.append("<option value=\"" + path + "\">" + fileObject.getString("Title") + "</option>");										
									}
								}
							}
							
						}
						
						
						html.append("</select>");
						
						html.append("</td></tr>");
						
					}
				}
				catch(java.lang.Exception e){
					ocs.logException(e);	
				}
				html.append("</table>");	
				html.append("</form>");
				
				//html.append("<tr><th>Kontext</th><td>" + HTMLForm.getTextField(omc.getProperty("activity_context"), true, "") + "</td></tr>");

				
				//if(activity.getID("Template")==7 || activity.getID("Template")==8){
				
				if(ot.getBoolean("HasObjects")){
						

						
					html.append("<table border>");
						
						
						
						html.append("<tr><td>Folgende Objekte sind mit diesem Brief verknüpft:</td></tr>");
						
						if(activity.getObjectCollection("Results")==null){
							activity.addObjectCollection("Results", "*");	
						}
						activity.getObjects("Results").clear();
						
						if(controller instanceof OrganisationMemberController){
							String sql = "SELECT t1.ID, t1.Title, t1.DateCreated , t1.Status FROM MemberAd AS t1 JOIN ActivityObject AS t2  ON t1.ID=t2.MemberAdID WHERE t2.ActivityID=" + activity.getName() + "";
							ocs.queryData(sql, activity.getObjectCollection("Results"));
							for(BasicClass bc : activity.getObjectCollection("Results").getObjects()){
								html.append("<tr><td>Inserat</td><td>" + bc.getString("ID") + " " + bc.getString("TITLE") + " " + DateConverter.sqlToShortDisplay(bc.getString("VALIDFROM"), false) + "</td><td>" + bc.getString("STATUS") + "</td></tr>");	
							}
							
							activity.getObjectCollection("Results").removeObjects();
							
							sql = "SELECT t1.ID, t1.DateCreated, t3.Title, t1.Status FROM MemberAdRequest AS t1 JOIN ActivityObject AS t2 ON t1.ID=t2.MemberAdRequestID LEFT JOIN MemberAd As t3 ON t1.MemberAd=t3.ID WHERE t2.ActivityID=" + activity.getName();
							ocs.queryData(sql, activity.getObjectCollection("Results"));
							for(BasicClass bc : activity.getObjectCollection("Results").getObjects()){
								html.append("<tr><td>Adressbestellung</td><td>" + bc.getString("ID") + " " + DateConverter.sqlToShortDisplay(bc.getString("DATECREATED"), false) + "</td><td>" + bc.getString("TITLE") + "</td><td>" + bc.getString("STATUS") + "</td></tr>");	
							}
						}
						


	
						
						html.append("<tr><td>Folgende Objekte haben noch keine schriftliche Benachrichtigung:</td></tr>");
						
						if(activity.getID("NotificationStatus")==0){
							//html.append("<tr><td>Benutzerregistrierung</td><td><a href=\"javascript:getNextNode('activityobjectadd=true&organisationmemberid=" +  om.getName() + "')\">Hinzufügen</a></td></tr>");
						}
						
						if(activity.getObjectCollection("Results")==null){
							activity.addObjectCollection("Results", "*");	
						}
						activity.getObjects("Results").clear();
						
						if(controller instanceof OrganisationMemberController){
							
							String sql = "SELECT t1.ID, t1.Title, t1.DateCreated , t1.Status FROM MemberAd AS t1 LEFT JOIN ActivityObject AS t2  ON t1.ID=t2.MemberAdID WHERE t1.OrganisationMemberID=" + controller.getName() + " AND t2.ActivityID IS NULL";
							ocs.queryData(sql, activity.getObjectCollection("Results"));
							for(BasicClass bc : activity.getObjectCollection("Results").getObjects()){
								html.append("<tr><td>Inserat</td><td>" + bc.getString("ID") + " " + bc.getString("TITLE") + " " + DateConverter.sqlToShortDisplay(bc.getString("VALIDFROM"), false) + "</td><td>" + bc.getString("STATUS") + "</td><td><a href=\"javascript:onAction('" + controller.getPath() + "','activityobjectadd','activityid=" + activity.getName() + "&memberadid=" +  bc.getString("ID") + "')\">Hinzufügen</a></td></tr>");	
							}
							/*
							String sql = "SELECT ID, Title, DateCreated FROM MemberAd WHERE OrganisationMemberID=" + controller.getName() + " AND NotificationStatus=0";
							ocs.queryData(sql, activity.getObjectCollection("Results"));
							for(BasicClass bc : activity.getObjectCollection("Results").getObjects()){
								html.append("<tr><td>Inserat</td><td>" + bc.getString("TITLE") + " " + DateConverter.sqlToShortDisplay(bc.getString("VALIDFROM"), false) + "</td><td><a href=\"javascript:onAction('" + controller.getPath() + "','activityobjectadd','activityid=" + activity.getName() + "&memberadid=" +  bc.getString("ID") + "')\">Hinzufügen</a></td></tr>");	
							}
							*/
							
							activity.getObjectCollection("Results").removeObjects();
							
							sql = "SELECT t1.ID, t1.DateCreated, t3.Title, t1.Status FROM MemberAdRequest AS t1 LEFT JOIN ActivityObject AS t2 ON t1.ID=t2.MemberAdRequestID LEFT JOIN MemberAd As t3 ON t1.MemberAd=t3.ID WHERE t2.ID IS NULL AND t1.OrganisationMemberID=" + controller.getName();
							ocs.queryData(sql, activity.getObjectCollection("Results"));
							for(BasicClass bc : activity.getObjectCollection("Results").getObjects()){
								html.append("<tr><td>Adressbestellung</td><td>" + bc.getString("ID") + " " + DateConverter.sqlToShortDisplay(bc.getString("DATECREATED"), false) + "</td><td><a href=\"javascript:onAction('" + controller.getPath() + "','activityobjectadd','activityid=" + activity.getName() + "&memberadrequestid=" +  bc.getString("ID") + "')\">Hinzufügen</a></td></tr>");	
							}
						}
						
						html.append("</table>");
						
						

						
					}
					
	
					


				

	

				
				ObjectCollection results = new ObjectCollection("Results", "*");
				String sql = "SELECT ID, Recipients, DateCreated FROM MailMessageInstance WHERE ActivityID=" + activity.getName();
						
				ocs.queryData(sql, results);
				
				if(ot.getBoolean("IsSendable")){							
							
					html.append("<h4>Mail wurde verschickt</h4>");
					html.append("<table>");
								
					for(BasicClass record : results.getObjects("Results")){
									
						html.append("<tr>");
									
						html.append("<td>" + DateConverter.sqlToShortDisplay(record.getString("DATESENT"), true ) + "</td>");
						html.append("<td>" + record.getString("RECIPIENTS") + "</td>");
						html.append("<td><a href=\"javascript:downloadMailMessage(" + record.getString("ID") + ")\">Oeffnen</a></td>");
									
						html.append("</tr>");
									
					}
								
					html.append("</table>");
				
				}
		
		
		
		
	}
	
}