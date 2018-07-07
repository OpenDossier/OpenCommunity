package ch.opencommunity.view;

import ch.opencommunity.server.*;

import org.kubiki.servlet.HtmlFieldWidget;
import org.kubiki.application.ApplicationContext;
import org.kubiki.base.BasicClass;
import org.kubiki.base.Property;
import org.kubiki.base.ObjectCollection;

public class ImageUploadWidget extends HtmlFieldWidget{
	
	@Override
	public String renderWidget(BasicClass bc, Property p, boolean isEditable, ApplicationContext context, String prefix){
		StringBuilder html = new StringBuilder();
		
		html.append("<tr><th>" + p.getLabel() + "</td>");
		html.append("<td><input type=\"button\" onclick=\"openFileUpload('" + bc.getPath() + "')\" value=\"File hochladen\">");
		html.append("</td></tr>");
		return html.toString();	
	}
	
}