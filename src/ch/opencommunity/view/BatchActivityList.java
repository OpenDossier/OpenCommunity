package ch.opencommunity.view;

import ch.opencommunity.common.OpenCommunityUserSession;

import org.kubiki.base.ObjectCollection;
import org.kubiki.base.BasicClass;
import org.kubiki.application.Application;
import org.kubiki.application.ApplicationContext;
import org.kubiki.util.DateConverter;


public class BatchActivityList{
		
	public static String getBatchActivityList(Application application, ApplicationContext context, OpenCommunityUserSession userSession){
			
		StringBuilder html = new StringBuilder();
		
		ObjectCollection results = new ObjectCollection("Results", "*");
		String sql = "SELECT * FROM BatchActivity AS t1";
		
		sql += " ORDER BY t1.DateCreated DESC";
		
		application.queryData(sql, results);
		
		html.append("<table>");
		
		html.append("<tr><th>ID</th><th>Datum</th><th>Kontext</th><th>Betreff</th>");
		
		boolean even = false;
		
		for(BasicClass record : results.getObjects()){
			if(even){
				html.append("<tr class=\"even\">");
			}
			else{
				html.append("<tr class=\"odd\">");
			}
			even = !even;
			
			html.append("<td>" + record.getString("ID") + "</td>");
			html.append("<td>" + record.getString("DATECREATED") + "</td>");
			html.append("<td>" + record.getID("CONTEXT") + "</td>");
			html.append("<td>" + record.getString("TITLE") + "</td>");
			
			html.append("<td><a href=\"javascript:createProcess('ch.opencommunity.process.BatchActivityCreate','BAID=" + record.getString("ID") + "')\">Details</a></td>");
			
			html.append("</tr>");
			
			
		}
		
		html.append("</table>");
		
		return html.toString();
		
	}
		
	
}