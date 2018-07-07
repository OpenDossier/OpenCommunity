package ch.opencommunity.view;

import ch.opencommunity.server.*;
import ch.opencommunity.process.*;

import org.kubiki.servlet.HtmlFieldWidget;
import org.kubiki.application.ApplicationContext;
import org.kubiki.base.BasicClass;
import org.kubiki.base.Property;
import org.kubiki.base.ObjectCollection;

public class MemberAdRequestList extends HtmlFieldWidget{
	

	
	public MemberAdRequestList(){
		
	}
	
	@Override
	public String renderWidget(BasicClass bc, Property p, boolean isEditable, ApplicationContext context, String prefix){
		StringBuilder html = new StringBuilder();
		
		if(bc instanceof MemberAdRequestsActivate){
		
			html.append("<tr><th>Bestellte Adressen</th><td><table>");
			
		}
		else{
			
			html.append("<tr><th>Inserat</th><td><table>");
			
		}
		
		for(BasicClass o : bc.getParent().getObjects("Results")){
			String style = "";
			if(o.hasProperty("STATUS")){
				if(o.getID("STATUS")==3){
					style = "color : red;";	
				}
			}
			html.append("<tr>");
			html.append("<td style=\"" + style + "\">" + o.getString("FIRSTNAME") + " " + o.getString("FAMILYNAME") + "</td>");
			html.append("<td style=\"" + style + "\">" + o.getString("TITLE") + "</td>");
			html.append("<td style=\"" + style + "\">" + o.getString("CATEGORY") + "</td>");
			html.append("</tr>");
		}
		html.append("</table></td></tr>");
		
		return html.toString();
		
	}
}