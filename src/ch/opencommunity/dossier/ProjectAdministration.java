package ch.opencommunity.dossier;

import ch.opencommunity.server.OpenCommunityServer;
import ch.opencommunity.common.OpenCommunityUserSession;


import org.kubiki.base.BasicClass;
import org.kubiki.base.ObjectCollection;
import org.kubiki.base.ActionResult;
import org.kubiki.base.ConfigValue;
import org.kubiki.application.ApplicationContext;
import org.kubiki.application.server.WebApplicationModule;
import org.kubiki.gui.html.HTMLFormManager;
import org.kubiki.util.DateConverter;

import java.util.Map;
import java.util.Vector;



public class ProjectAdministration extends WebApplicationModule{
	
	OpenCommunityServer server = null;
	
	static String[] statusdef = {"offen", "abgeschlossen", "abgelegt"};
	static String[] projectTypes ={"Spendengesuch", "Unterstütztes Projekt"};
	
	public ProjectAdministration(){
				
		setName("Projects");
		addProperty("Type", "Integer", "");		
		addProperty("Status", "Integer", "");		
		addProperty("DateFrom", "Date", "");		
		
	}
	public void initObjectLocal(){
		server = (OpenCommunityServer)getRoot();
		//institutionTypeMap = server.getInstitutionTypeMap();
		//getProperty("Type").setSelection(server.getInstitutionTypes());
		
		Vector status = new Vector();
		status.add(new ConfigValue("0","0", "offen"));
		status.add(new ConfigValue("1","1", "abgeschlossen"));		
		status.add(new ConfigValue("2","2", "abgelegt"));	
		getProperty("Status").setSelection(status);
		
		Vector type = new Vector();
		type.add(new ConfigValue("0","0","Spendengesuch"));
		type.add(new ConfigValue("1","1","Unterstütztes Projekt"));	
		getProperty("Type").setSelection(type);
		
	}
	public ActionResult onAction(BasicClass source, String command, ApplicationContext context){
			
		ActionResult result = null;
		if(command.equals("filterprojects")){
			
			result = new ActionResult(ActionResult.Status.OK, "Filter ausgeführt");	
			result.setParam("dataContainer", "projectlist");
			result.setData(getProjectList(context));
		}
		
		
		return result;	
	}
	
	public String getMainForm(ApplicationContext context){
			
		HTMLFormManager formManager = server.getFormManager();
			
		StringBuilder html = new StringBuilder();
		
		html.append(formManager.getToolbar(this, null, null, context, true));
			
		html.append("<div id=\"listfilter\" class=\"listfilter\" style=\"position : absolute; left : 0px; width : 300px; top : 70px; bottom : 0px; overflow : auto;\">");
			
		html.append(getFilter());
			
		html.append("</div>");
			
		html.append("<div id=\"projectlist\" style=\" position : absolute; top : 70px; left : 300px; bottom : 0px; right : 0px; overflow : auto;\">");
			
		html.append(getProjectList(context));
			
		html.append("</div>");
		
		
		return html.toString();
		
	}
	public String getFilter(){
			
		StringBuilder html = new StringBuilder();
			
		html.append("<b>Filtern nach</b>");
			
		html.append("<form id=\"filter_organisations\">");
		html.append("<table>");

		html.append("<tr><td>Typ</td><td>" + server.getFormManager().getSelection(getProperty("Type"), true, "") + "</td></tr>");
		html.append("<tr><td>Status</td><td>" + server.getFormManager().getSelection(getProperty("Status"), true, "") + "</td></tr>");

		html.append("<tr><td>Beginn ab</td><td>" + server.getFormManager().getDateField(getProperty("DateFrom"), true, "") + "</td></tr>");
		
		html.append("</table>");
			
		html.append("<input type=\"button\" class=\"actionbutton\" value=\"Filter anwenden\" onclick=\"filterProjects('" + getPath() + "')\">");
			
		html.append("</form>");
			
		return html.toString();
			
	}
	public String getProjectList(){
		return getProjectList(null);
	}
	public String getProjectList(ApplicationContext context){
		StringBuilder html = new StringBuilder();
			
		html.append("<table>");		
		
		html.append("<tr>");
		html.append("<td></td>");
		html.append("<td class=\"tableheader\">Organisation</td>");
		html.append("<td class=\"tableheader\">Bezeichnung</td>");
		html.append("<td class=\"tableheader\">Typ</td>");
		html.append("<td class=\"tableheader\">Datum Beginn</td>");
		html.append("<td class=\"tableheader\">Datum Gesuch</td>");
		html.append("<td class=\"tableheader\">Datum Antwort</td>");
		html.append("<td class=\"tableheader\">Ergebnis</td>");
		html.append("<td class=\"tableheader\">Status</td>");	
		html.append("<td class=\"tableheader\">Datum Abschluss</td>");
		html.append("</tr>");	
		

		
		String sql = "SELECT t1.*, t2.ID AS DossierID, t3.Title As Organisation, t4.Date AS Date1, t5.Date AS Date2, t6.DateCreated AS Date3, t7.Value AS Result";
		sql += " FROM Project AS t1";
		sql += " JOIN Dossier AS t2 ON t1.DossierID=t2.ID";
		sql += " JOIN OrganisationalUnit AS t3 ON t2.OrganisationalUnit=t3.ID";
		sql += " LEFT JOIN Activity AS t4 ON t4.ProjectID=t1.ID AND t4.Template=12";
		sql += " LEFT JOIN Activity AS t5 ON t5.ProjectID=t1.ID AND t5.Template=17";
		sql += " LEFT JOIN ObjectStatus AS t6 ON t6.ProjectID=t1.ID";
		sql += " LEFT JOIN Parameter AS t7 ON t7.ActivityID=t5.ID AND t7.Template=35";
		

		
		String operator = " WHERE";
		
		
		String type = context.getString("Type");
		
		if(type != null && type.length() > 0){
				
			sql += operator + " t1.Type=" + type;
			
			operator = " AND";
		}
		
		
		String status = context.getString("Status");
		
		if(status != null && status.length() > 0){
				
			sql += operator + " t1.Status=" + status;
			
			operator = " AND";
		}
		
		String datefrom = context.getString("DateFrom");
		
		if(datefrom != null && datefrom.length() > 0){
				
			sql += operator + " t1.DateStarted >='" + datefrom + "'";
			
			operator = " AND";
		}
		
		
		ObjectCollection records = new ObjectCollection("Results", "*");
			
		server.queryData(sql, records);
			
		boolean even = false;
			
		for(BasicClass record : records.getObjects()){
			if(even){
				html.append("<tr class=\"even\">");
			}
			else{
				html.append("<tr class=\"odd\">");					
			}
			even = !even;
			html.append("<td class=\"datacell\"  ondragover=\"allowDrop(event)\" ondrop=\"dropMessage(event, " + record.getString("ID") + ")\" onmouseover=\"highlightCell(this, true)\" onmouseout=\"highlightCell(this, false)\">" + record.getString("ID") + "</td>");
			html.append("<td class=\"datacell\"><a href=\"javascript:openDossier(" + record.getString("DOSSIERID") + ")\">" + record.getString("ORGANISATION") + "</a></td>");
			html.append("<td class=\"datacell\">" + record.getString("TITLE") + "</td>");
			html.append("<td class=\"datacell\">" + projectTypes[record.getID("TYPE")] + "</td>");
			html.append("<td class=\"datacell\">" + DateConverter.sqlToShortDisplay(record.getString("DATESTARTED"), false) + "</td>");
			html.append("<td class=\"datacell\">" + DateConverter.sqlToShortDisplay(record.getString("DATE1"), false) + "</td>");
			html.append("<td class=\"datacell\">" + DateConverter.sqlToShortDisplay(record.getString("DATE2"), false) + "</td>");
			html.append("<td class=\"datacell\">" + record.getString("RESULT") + "</td>");
			html.append("<td class=\"datacell\">" + statusdef[record.getID("STATUS")] + "</td>");
			html.append("<td class=\"datacell\">" + DateConverter.sqlToShortDisplay(record.getString("DATE3"), false) + "</td>");
			html.append("</tr>");	
					
		}
		
		html.append("</table>");	
		
		return html.toString();
		
	}
	
}