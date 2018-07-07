package ch.opencommunity.process;

import org.kubiki.base.BasicClass;
import org.kubiki.database.DataStore;
import org.kubiki.application.BasicUserSession;
import org.kubiki.application.ApplicationContext;
import org.kubiki.application.server.ApplicationServer;
import org.kubiki.gui.html.BaseObjectView;
import org.kubiki.gui.html.HTMLFormManager;

public class ListExportForm extends BaseObjectView{
	
	
	public String toHTML(BasicClass bc, ApplicationContext context, BasicUserSession userSession, boolean isEditable){
		
		ListExport listExport = (ListExport)bc;
		
		ApplicationServer server = (ApplicationServer)listExport.getRoot();
		
		DataStore dataStore = server.getDataStore();
		
		HTMLFormManager formManager = server.getFormManager();
		
		StringBuilder html = new StringBuilder();
		
		html.append("<form id=\"processNodeForm\" name=\"processNodeForm\">");
		
		html.append("<table>");
		

		
		html.append("<tr>");
		html.append("<td valign=\"top\">");
		for(Object o : listExport.getAvailableColumns()){
			
			String label = dataStore.getFieldLabel(o.toString());
			if(label != null){
				
				html.append("<div class=\"listitem\" onclick=\"getNextNode('additem=true&item=" + o + "')\">" + label + "</div>");
				
			}
			else{
			
				html.append("<div class=\"listitem\" onclick=\"getNextNode('additem=true&item=" + o + "')\">" + o + "</div>");
				
			}
			
			
		}
		
		html.append("</td>");
		html.append("<td valign=\"top\">");
		
		for(Object o : listExport.getSelectedColumns()){
			
			String label = dataStore.getFieldLabel(o.toString());
			if(label != null){
			
				html.append("<div class=\"listitem\" onclick=\"getNextNode('removeitem=true&item=" + o + "')\">" + label + "</div>");	
				
			}
			else{
				
				html.append("<div class=\"listitem\" onclick=\"getNextNode('removeitem=true&item=" + o + "')\">" + o + "</div>");		
				
			}
			
		}
		
		
		html.append("</td>");
		
		
		html.append("</tr>");
		
		
		html.append("</table>");
		
		html.append("</form>");
		
		html.append("<input type=\"button\" class=\"actionbutton\" onclick=\"cancelProcess()\" value=\"Abbrechen\">");
		html.append("<input type=\"button\" class=\"actionbutton\" onclick=\"getNextNode()\" value=\"Abschliessen\">");
		
		return html.toString();
		
	}
	
}