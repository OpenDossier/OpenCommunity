package ch.opencommunity.view;

import ch.opencommunity.server.*;

import org.kubiki.servlet.HtmlFieldWidget;
import org.kubiki.application.ApplicationContext;
import org.kubiki.base.BasicClass;
import org.kubiki.base.Property;
import org.kubiki.base.ObjectCollection;

public class RecipientList extends HtmlFieldWidget{
	
	String[] channels = {"", "Email", "Brief"};
	
	public RecipientList(){
		
	}
	
	@Override
	public String renderWidget(BasicClass bc, Property p, boolean isEditable, ApplicationContext context, String prefix){
		StringBuilder html = new StringBuilder();
		
		html.append("<tr><td colspan=\"2\">");
		
		html.append("<table>");
		
		html.append("<tr><th>Name</th><th>Vorname</th><th>Email</th><th>Benachrichtigungsart</th></tr>");
		
		ObjectCollection oc = bc.getParent().getObjectCollection("Recipients");
		int even = 1;
		for(BasicClass o : oc.getObjects()){
			if(even==1){
				html.append("<tr class=\"even\">");
				even = -even;
			}
			else{
				html.append("<tr class=\"odd\">");
				even = -even;				
			}
			html.append("<td>" + o.getString("FAMILYNAME")  + "</td>");
			html.append("<td>" + o.getString("FIRSTNAME")  + "</td>");
			html.append("<td>" + o.getString("VALUE")  + "</td>");
			int notificationmode = o.getInt("NOTIFICATIONMODE");
			if(notificationmode==-1) notificationmode=1;
			html.append("<td>" + channels[notificationmode]  + "</td>");
			html.append("</tr>");
		}
		html.append("<table>");
		html.append("</td>");
		return html.toString();
	}
}