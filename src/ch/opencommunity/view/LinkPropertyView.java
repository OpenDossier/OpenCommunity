package ch.opencommunity.view;

import ch.opencommunity.server.*;

import org.kubiki.servlet.HtmlFieldWidget;
import org.kubiki.application.ApplicationContext;
import org.kubiki.base.BasicClass;
import org.kubiki.base.Property;
import org.kubiki.base.ObjectCollection;

public class LinkPropertyView extends HtmlFieldWidget{
	
	//Ersetzen durch eine neue Property, welche ein value/label-Paar aufnimmt
	
	@Override
	public String renderWidget(BasicClass bc, Property p, boolean isEditable, ApplicationContext context, String prefix){
		StringBuilder html = new StringBuilder();
		
		html.append("<tr><th>" + p.getLabel() + "</td><td>");
		String value =  p.getValue();
		String[] args = value.split(";");
		if(args.length==2){
			html.append("<a href=\"javascript:getNextNode('OMID=" + args[0] + "')\">" + args[1] + "</a>");
		}
		html.append("</td></tr>");
		return html.toString();	
	}
	
}