package ch.opencommunity.view;

import ch.opencommunity.advertising.*;
import ch.opencommunity.base.*;
import ch.opencommunity.process.*;
import ch.opencommunity.common.*;
import ch.opencommunity.server.*;

import org.kubiki.servlet.HtmlFieldWidget;
import org.kubiki.base.ConfigValue;
import org.kubiki.base.BasicClass;
import org.kubiki.base.Property;
import org.kubiki.application.ApplicationContext;
import org.kubiki.util.DateConverter;
import org.kubiki.gui.html.HTMLFormManager;
import org.kubiki.cms.ImageObject;

import java.util.Vector;
import java.util.Map;

public class MemberAdView extends HtmlFieldWidget{
	
	public String renderWidget(BasicClass bc, Property p, boolean isEditable, ApplicationContext context, String prefix){
		if(bc.getParent() instanceof MemberAdActivate){
			return "<tr><td style=\"height : 400px;\">" + getMemberAdView(((MemberAdActivate)bc.getParent()).getMemberAd(), true) + "</td></tr>";
		}
		if(bc.getParent() instanceof MemberAdRequestActivate){
			return getMemberAdDetailForm(((MemberAdRequestActivate)bc.getParent()).getMemberAd(), true, context);
		}
		else if(bc.getParent() instanceof MemberAdRequestsActivate){
			return getMemberAdDetailForm((MemberAd)bc.getObject("memberadid"), bc, true, context);
		}
		else{
			return "xxxx";	
		}
	}
	
	public static String getMemberAdView(MemberAd ma, boolean editable){
		
		StringBuilder html = new StringBuilder();
		int top = 40;
		MemberRegistration mr = null;
		
		html.append("<style type=\"text/css\">");
		html.append("\n#wizard{ height : 700px;} ");
		html.append("\n</style>");
		
		html.append("<form id=\"processNodeForm\">");
		
		mr.addLabel(html, "Rubrik", 0,  top);
		mr.addLabel(html, ma.getObject("Template").toString(), 160,  top);
		
		MemberAdCategory mac = (MemberAdCategory)ma.getObject("Template");
		Vector parameters = ma.getObjects("Parameter");
		for(int i = 0; i < parameters.size(); i++){
			Parameter parameter = (Parameter)parameters.elementAt(i);
			FieldDefinition fd = (FieldDefinition)mac.getFieldDefinitionByTemplate("" + parameter.getID("Template"));
			top += 40;
			if(fd != null){
				mr.addLabel(html, fd.toString(), 0,  top);	
				Double value = parameter.getDouble("Value");
				if(value != null){
					String key = value.intValue() + "";
					mr.addLabel(html, "" + fd.getCodeMap().get(key), 160,  top);
				}
			}
		}
		top += 40;
		
		mr.addLabel(html, "Titel", 0,  top);
		mr.addTextField(html, ma.getProperty("Title"), "PLZ*/Ort*", 1,  160 , top, 300,30, "inputsmall1", "");

		
		top += 40;
		mr.addLabel(html, "Beschreibung", 0,  top);
		html.append("<textarea id=\"Description\" name=\"Decription\" style=\" position : absolute; top : " + top + "px; left: 160px; height : 60px;\">" + ma.getString("Description") + "</textarea>");

		
		top += 80;
		mr.addLabel(html, "Gültig bis", 0,  top);
		mr.addLabel(html, DateConverter.sqlToShortDisplay(ma.getString("ValidUntil")), 160,  top);
		if(editable){
			//mr.addTextField(html, ma.getProperty("ValidUntil"), "PLZ*/Ort*", 1,  250 , top, 100,30, "inputsmall1", "");
		}
		if(editable){
			//html.append("<input type=\"button\" style=\"position : absolute; bottom : 20px;\" value=\"Fertig\" onclick=\"getNextNode()\">");
		}
		

		html.append("</form>");
		return html.toString();
	}
	public static String getMemberAdEditForm(MemberAd ma, boolean editable){
		
		StringBuilder html = new StringBuilder();
		int top = 40;
		MemberRegistration mr = null;
		
		html.append("<div style=\"position: relative;\">");
		
		html.append("<form id=\"objectEditForm_" + ma.getPath() + "\">");
		
		mr.addLabel(html, "Rubrik", 0,  top);
		mr.addLabel(html, ma.getObject("Template").toString(), 160,  top);
		
		MemberAdCategory mac = (MemberAdCategory)ma.getObject("Template");
		Vector parameters = ma.getObjects("Parameter");
		for(int i = 0; i < parameters.size(); i++){
			Parameter parameter = (Parameter)parameters.elementAt(i);
			FieldDefinition fd = (FieldDefinition)mac.getFieldDefinitionByTemplate("" + parameter.getID("Template"));
			top += 40;
			if(fd != null){
				mr.addLabel(html, fd.toString(), 0,  top);	
				Double value = parameter.getDouble("Value");
				if(value != null){
					String key = value.intValue() + "";
					mr.addLabel(html, "" + fd.getCodeMap().get(key), 160,  top);
				}
			}
		}
		top += 40;
		
		mr.addLabel(html, "Titel", 0,  top);
		mr.addTextField(html, ma.getProperty("Title"), "PLZ*/Ort*", 1,  160 , top, 300,30, "inputsmall1", "");

		
		top += 40;
		mr.addLabel(html, "Beschreibung", 0,  top);
		html.append("<textarea id=\"Description\" name=\"Decription\" style=\" position : absolute; top : " + top + "px; left: 160px; height : 60px;\">" + ma.getString("Description") + "</textarea>");

		
		top += 80;
		mr.addLabel(html, "Gültig bis", 0,  top);
		mr.addLabel(html, DateConverter.sqlToShortDisplay(ma.getString("ValidUntil")), 160,  top);
		if(editable){
			//mr.addTextField(html, ma.getProperty("ValidUntil"), "PLZ*/Ort*", 1,  250 , top, 100,30, "inputsmall1", "");
		}
		if(editable){
			//html.append("<input type=\"button\" style=\"position : absolute; bottom : 20px;\" value=\"Fertig\" onclick=\"getNextNode()\">");
		}
		html.append("</form>");
		
		html.append("</div>");
		return html.toString();
	}
	public static String getMemberAdEditForm2(OrganisationMemberController omc, MemberAd memberAd, MemberAdController memberAdController){
		
		OpenCommunityServer ocs = (OpenCommunityServer)memberAd.getRoot();
		
		StringBuilder html = new StringBuilder();
		
		html.append(memberAd.getName());
		
		html.append("<form action=\"servlet\" id=\"objectEditForm_" + memberAd.getPath() + "\">");
		
		html.append("\n<input type=\"hidden\" name=\"action\" value=\"getObjectAction\">");	
		html.append("\n<input type=\"hidden\" name=\"command\" value=\"savememberad\">");	
		html.append("\n<input type=\"hidden\" name=\"memberadid\" value=\"" + memberAd.getName() + "\">");
		html.append("\n<input type=\"hidden\" name=\"objectPath\" value=\"" + omc.getPath("") + "\">");
		
		html.append("<table>");

		//BaseView.addTableRow(html, memberAdController.getProperty("Template"), "Rubrik", "", 2);
		
		if(memberAd.getID("Template") > 0){
			html.append("<tr><td class=\"inputlabel\">Rubrik</td><td>" +  ocs.getFormFactory().getSelection(memberAdController.getProperty("Template"), "Template", null, true, true, true, "", "onAction('" + omc.getPath() + "','editmemberad','','memberadid=" + memberAd.getName() + "&template=' + this.value)") + "</td></tr>");
		}
		BaseView.addTableRow(html, memberAdController.getProperty("Title"), "Bezeichnung", "", 1);
		BaseView.addTableRow(html, memberAdController.getProperty("Type"), "Art des Inserates", "", 2);
		BaseView.addTableRow(html, memberAdController.getProperty("Status"), "Status", "", 2);
		BaseView.addTableRow(html, memberAdController.getProperty("Description"), "Bezeichnung", "", 3);	
		BaseView.addTableRow(html, memberAdController.getProperty("ValidFrom"), "Gültig von", "", 1);
		BaseView.addTableRow(html, memberAdController.getProperty("ValidUntil"), "Gültig bis", "", 1);
		BaseView.addTableRow(html, memberAdController.getProperty("Location"), "Alt. PLZ", "", 1);
		BaseView.addTableRow(html, memberAdController.getProperty("AdOfTheWeek"), "Inserat der Woche", "", 4);
		
		MemberAdCategory mac = memberAdController.mac;
		Vector parameters = memberAd.getObjects("Parameter");
		
		html.append("Template: " + mac);
		
		if(mac != null){
			for(BasicClass bc  : mac.getObjects("FieldDefinition")){
	
				FieldDefinition fd = (FieldDefinition)bc;
	
				if(fd != null){
					//mr.addLabel(html, fd.toString(), 0,  top);	
					
					
					
					html.append("<tr><td class=\"inputlabel\">" + fd.toString() + "</td>");
	
					if(fd.getID("Type")==1){
						
						html.append("<tr><td></td><td>" + HTMLForm.getSelection(memberAdController.getProperty(fd.getName()), true, "")+ "</td></tr>");
						/*
						Parameter parameter = memberAd.getParameterByTemplate(Integer.parseInt(fd.getName()));
						if(parameter != null){
							p = addProperty(fd.getName(), "Integer", "" + (int)parameter.getDouble("Value").doubleValue(), false, fd.getString("Title"));
							p.setSelection(fd.getCodeList());
						}
						else{
							p = addProperty(fd.getName(), "Integer", "" , false, fd.getString("Title"));
							p.setSelection(fd.getCodeList());
	
						}
						*/
					}
					else{
						/*
						p = addProperty(fd.getName() + "_label", "String", "" , false, fd.getString("Title"));
						p.setEditable(false);
						*/
						for(ConfigValue cv : fd.getCodeList()){
							
							
							String label = cv.getLabel();
							String[] args = label.split("/");
							/*
							String name = bc.getName() + "_" + cv.getValue();
								
							ocs.logAccess(name + ":" + memberAd.hasParameter(fd.getID(), Integer.parseInt("" + cv.getValue())));
								
							p = addProperty(name , "Boolean", "" + memberAd.hasParameter(fd.getID(),Integer.parseInt("" + cv.getValue())), false, args[0]);
							*/
							html.append("<tr><td>" + args[0] + "</td><td>" + HTMLForm.getRadioButton(memberAdController.getProperty(fd.getName() + "_" + cv.getValue()), true, "") + "</td></tr>");
	
						}
					}
					
					
					html.append("</tr>");
				}
			}
		
		}
		
		html.append("<tr><th>Bilddatei</th>");
		ImageObject io = (ImageObject)memberAd.getObjectByIndex("ImageObject", 0);
		if(io == null){
			html.append("<td id=\"imageobject\"></td></tr>");
		}
		else{
			html.append("<td id=\"imageobject\">" + io.getString("FileName") + "</td></tr>");	
		}
		html.append("<re><td></td><td>" + HTMLFormManager.getFileUpload(memberAd, memberAd.getProperty("ImageUpload")) + "</td></tr>");
		
		BaseView.addTableRow(html, memberAdController.getProperty("ExternalLink"), "Link", "", 1);
		
		if(memberAd.getObjects("Note").size()==0){
			html.append("<tr><td><input type=\"button\" class=\"actionbutton\" value=\"Notizfeld hinzufügen\" onclick=\"onAction('" + omc.getPath() + "','addmemberadinfo','','memberadid=" + memberAd.getName() + "')\"></td></tr>");	
		}
		else{
			Note note = (Note)memberAd.getObjectByIndex("Note", 0);
			if(note != null){
				//node1.addProperty("AdditionalInfo", "Text", note.getString("Content"), false, "Zusatzinfo");	
				BaseView.addTableRow(html, memberAdController.getProperty("AdditionalInfo"), "Zusatzinfo", "", 3);	
			}
		}
		
		
		html.append("</table>");
		
		html.append("</form>");
		
		html.append("<input type=\"button\" value=\"Speichern\" onclick=\"saveObject('" + memberAd.getPath() + "')\">");
		
		

		return html.toString();
	}


	public static String getMemberAdDetailForm(MemberAd ma, boolean embedded, ApplicationContext context){
		OpenCommunityServer ocs = (OpenCommunityServer)ma.getRoot();
		OpenCommunityUserSession usersession = (OpenCommunityUserSession)context.getObject("usersession");
		StringBuilder html = new StringBuilder();
		//html.append("<table>");
		html.append("<tr><td class=\"inputlabel\">Rubrik</td><td>" +  ma.getObject("Template").toString() + "</td></tr>");
		
		MemberAdCategory mac = (MemberAdCategory)ma.getObject("Template");
		Vector parameters = ma.getObjects("Parameter");
		for(int i = 0; i < parameters.size(); i++){
			Parameter parameter = (Parameter)parameters.elementAt(i);
			FieldDefinition fd = (FieldDefinition)mac.getFieldDefinitionByTemplate("" + parameter.getID("Template"));

			if(fd != null){
				//mr.addLabel(html, fd.toString(), 0,  top);	
				html.append("<tr><td class=\"inputlabel\">" + fd.toString() + "</td>");
				Double value = parameter.getDouble("Value");
				if(value != null){
					String key = value.intValue() + "";
					//mr.addLabel(html, "" + fd.getCodeMap().get(key), 160,  top);
					html.append("<td>" + fd.getCodeMap().get(key) + "</td>");
				}
				html.append("</tr>");
			}
		}

		html.append("<tr><td>Titel</td><td>" + ma.getString("Title") + "</td></tr>");
		html.append("<tr><td>Beschreibung</td><td>" + ma.getString("Description") + "</td></tr>");	
			
		
		html.append("<tr><td class=\"inputlabel\">Gültig bis</td><td>" +  DateConverter.sqlToShortDisplay(ma.getString("ValidUntil")) + "</td></tr>");
		
		//html.append("</table>");
		html.append("<tr><td colspan=\"2\">");
		//statt dessen Feedbacks einbauen
		//html.append(ocs.getMemberAdRequestList(context, usersession, " WHERE t1.ID=" + ma.getName(), "requests", true));
		
		html.append("</td></tr>");
		if(!embedded){
			html.append("<br><input type=\"button\" value=\"OK\" onclick=\"getNextNode('overview=true')\">");
		}
		return html.toString();	
	}
	public static String getMemberAdDetailForm(MemberAd ma, BasicClass bc, boolean embedded, ApplicationContext context){
		OpenCommunityServer ocs = (OpenCommunityServer)ma.getRoot();
		OpenCommunityUserSession usersession = (OpenCommunityUserSession)context.getObject("usersession");
		StringBuilder html = new StringBuilder();
		//html.append("<table>");
		
		html.append("<tr><td>Adressbesteller</td><td>" + bc.getString("RequestOwner") + "</td></tr>");
		html.append("<tr><td>Bestellte Adresse</td><td><a href=\"javascript:getNextNode(\'editmember=" + bc.getString("OMID") + "\')\">" + bc.getString("Familyname") + " " + bc.getString("Firstname") + " " + bc.getString("Number") + "</a></td></tr>");
		html.append("<tr><td class=\"inputlabel\">Rubrik</td><td>" +  ma.getObject("Template").toString() + "</td></tr>");
		
		MemberAdCategory mac = (MemberAdCategory)ma.getObject("Template");
		Vector parameters = ma.getObjects("Parameter");
		for(int i = 0; i < parameters.size(); i++){
			Parameter parameter = (Parameter)parameters.elementAt(i);
			FieldDefinition fd = (FieldDefinition)mac.getFieldDefinitionByTemplate("" + parameter.getID("Template"));

			if(fd != null){
				//mr.addLabel(html, fd.toString(), 0,  top);	
				html.append("<tr><td class=\"inputlabel\">" + fd.toString() + "</td>");
				Double value = parameter.getDouble("Value");
				if(value != null){
					String key = value.intValue() + "";
					//mr.addLabel(html, "" + fd.getCodeMap().get(key), 160,  top);
					html.append("<td>" + fd.getCodeMap().get(key) + "</td>");
				}
				html.append("</tr>");
			}
		}
		html.append("<tr><td>Titel</td><td>" + ma.getString("Title") + "</td></tr>");
		html.append("<tr><td>Beschreibung</td><td>" + ma.getString("Description") + "</td></tr>");	
			
		
		html.append("<tr><td class=\"inputlabel\">Gültig bis</td><td>" +  DateConverter.sqlToShortDisplay(ma.getString("ValidUntil")) + "</td></tr>");
		
		//html.append("</table>");
		html.append("<tr><td colspan=\"2\">");
		//statt dessen Feedbacks einbauen
		//html.append(ocs.getMemberAdRequestList(context, usersession, " WHERE t1.ID=" + ma.getName(), "requests", true));
		
		html.append("</td></tr>");
		if(!embedded){
			html.append("<br><input type=\"button\" value=\"OK\" onclick=\"getNextNode('overview=true')\">");
		}
		return html.toString();	
	}
		
}