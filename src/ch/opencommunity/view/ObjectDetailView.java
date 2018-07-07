package ch.opencommunity.view;

import ch.opencommunity.advertising.*;
import ch.opencommunity.base.*;
import ch.opencommunity.process.*;
import ch.opencommunity.server.OpenCommunityServer;
import ch.opencommunity.server.HTMLForm;
import ch.opencommunity.dossier.ObjectDetail;
import ch.opencommunity.dossier.DossierController;

import org.kubiki.servlet.HtmlFieldWidget;
import org.kubiki.base.BasicClass;
import org.kubiki.base.Property;
import org.kubiki.base.ConfigValue;
import org.kubiki.application.ApplicationContext;
import org.kubiki.util.DateConverter;
import org.kubiki.cms.FileObject;

import java.util.Vector;

public class ObjectDetailView{
	
	public static String toHTML(DossierController dossierController, ObjectDetail objectDetail){
		
		StringBuilder html = new StringBuilder();
		
		ObjectTemplate template = objectDetail.getTemplate();
		if(template != null){
			html.append("<tr><td>Feld. hinzufügen</td><td><select onchange=\"onAction('" + dossierController.getPath() + "','parameteradd','','objectdetailid=" + objectDetail.getName() + "&fielddefinitionid=' + this.value, false, true)\">");
			html.append("<option value=\"\"></option>");
			for(BasicClass fd : template.getObjects("FieldDefinition")){
				if(fd.getBoolean("IsMultiple")){
					html.append("<option value=\"" + fd.getName() + "\">" + fd + "</option>");	
				}
			}
			
			html.append("</select></td></tr>");
		}
		
		
		/*
		for(BasicClass bc : objectDetail.getObjects("Parameter")){
			
			Parameter parameter = (Parameter)bc;

			appendParameter(html, dossierController, parameter);			

				
		}
		
		html.append("</table><table>");
		
		for(BasicClass bc : objectDetail.getObjects("Note")){
			Note note = (Note)bc;

			appendNote(html, dossierController, note);
							
		}
		*/
		
		for(BasicClass bc : objectDetail.getObjects("Fields")){
			
			if(bc instanceof Parameter){
				appendParameter(html, dossierController, (Parameter)bc);		
			}
			else if(bc instanceof Note){
				appendNote(html, dossierController, (Note)bc);
			}			
					
		}
		
		return html.toString();	
		
	}
	public static void appendNote(StringBuilder html, DossierController dossierController, Note note){
		
			if(note.getObject("Template") instanceof FieldDefinition){
				FieldDefinition fd = (FieldDefinition)note.getObject("Template");
				
				if(fd.getID("Status")==0){
					html.append("<tr><td class=\"labelColumn\">" + fd.getString("Title") + "</td>");
					html.append("<td>");
					
					html.append("<textarea class=\"textarea_small\" name=\"note_" + note.getName() + "\" id=\"note_" + note.getPath() + "\">" + note.getString("Content") + "</textarea>");
					
					html.append("</td><td>");
					if(fd.getBoolean("IsMultiple")){
						html.append("<td><img src=\"images/delete_small.png\" onclick=\"onAction('" + dossierController.getPath() + "','notedelete','','noteid=" + note.getName() + "')\"></td>");	
					}
				}

			}
			
			
			html.append("</tr>");		
		
	}
	public static void appendParameter(StringBuilder html, DossierController dossierController, Parameter parameter){
		if(parameter.getObject("Template") instanceof FieldDefinition){
			FieldDefinition fd = (FieldDefinition)parameter.getObject("Template");
				html.append("<tr><td class=\"labelColumn\">" + fd.getString("Title") + "</td>");
				html.append("<td>");
				
				if(fd.getCodeList().size() > 0){
				
					html.append("<select name=\"parameter_" + parameter.getName() + "\" id=\"parameter_" + parameter.getPath() + "\" onchange=\"saveParameter('" + parameter.getPath() + "')\">");
					html.append("<option value=\"\"></option>");	
					for(ConfigValue cv : fd.getCodeList()){
						String value = parameter.getString("Value");
						String[] args = value.split("\\.");
						if(cv.getValue().equals(args[0])){
							html.append("<option value=\"" + cv.getValue() + "\" SELECTED>" + cv.getLabel() + "</option>");							
						}
						else{
							html.append("<option value=\"" + cv.getValue() + "\">" + cv.getLabel() + "</option>");	
						}
							
					}
					html.append("</select>");	
					
				}
				else{
					html.append("<input name=\"parameter_" + parameter.getName() + "\" id=\"parameter_" + parameter.getPath() + "\" value=\"" + parameter.getString("Value") + "\">");
				}
				
				html.append("</td>");
				
				//html.append("<td><input id=\"parameter_comment_" + parameter.getName() + "\" name=\"parameter_comment_" + parameter.getName() + "\"value=\"" + parameter.getString("Comment") + "\"></td>");
				
				
				if(fd.getBoolean("IsMultiple")){
					html.append("<td><input type=\"button\" value=\" + \" onclick=\"onAction('" + dossierController.getPath() + "','parameteradd','','objectdetailid=" + parameter.getParent().getName() + "&fielddefinitionid=" + fd.getName() + "')\"></td>");	
				}
				if(fd.getBoolean("IsMultiple")){
					html.append("<td><img src=\"images/delete_small.png\" onclick=\"onAction('" + dossierController.getPath() + "','parameterdelete','','parameterid=" + parameter.getName() + "')\"></td>");	
				}
		}
			
			
		html.append("</tr>");		
		
		
		
		
		
	}
}