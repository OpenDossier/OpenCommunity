package ch.opencommunity.base;

import ch.opencommunity.server.OpenCommunityServer;
import ch.opencommunity.common.OpenCommunityUserSession;
import ch.opencommunity.view.ChequeList;

import org.kubiki.base.ObjectCollection;
import org.kubiki.base.BasicClass;
import org.kubiki.base.ActionResult;
import org.kubiki.application.ApplicationContext;
import org.kubiki.application.server.WebApplicationModule;
import org.kubiki.util.DateConverter;


public class ChequeAdministration extends WebApplicationModule{
	
	OpenCommunityServer ocs;
	ChequeList  chequeList;
	
	public ChequeAdministration(OpenCommunityServer ocs, ChequeList  chequeList){
		
		this.ocs = ocs;
		this.chequeList = chequeList;
		

		
	}
	public ActionResult onAction(BasicClass source, String command, ApplicationContext context){
		ActionResult result = null;
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
		if(command.equals("setselectedyear")){
			String selectedyear = context.getString("selectedyear");
			if(selectedyear != null){
				userSession.setSelectedYear(Integer.parseInt(selectedyear));
				result = new ActionResult(ActionResult.Status.OK, "Formular geladen");	
				result.setParam("dataContainer", "admindisplay");
				result.setData(toHTML(context));
			}
			
		}
		return result;
		
	}
	
	public String toHTML(ApplicationContext context){
		
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
		
		StringBuilder html = new StringBuilder();
		
		html.append("<div style=\"position : absolute; width : 400px; top : 50px; bottom : 0px; left : 20px; overflow : auto;\">");
		
		int yearStart = 2017;
		int yearEnd = DateConverter.getCurrentYear() + 1;
		html.append("Jahr <select onchange=\"onAction('" + getPath() + "','setselectedyear','', 'selectedyear=' + this.value)\">");
		for(int i = yearStart; i <= yearEnd; i++){
			if(userSession.getSelectedYear()==i){
				html.append("<option value=\"" + i + "\" SELECTED>" + i + "</option>");
			}
			else{
				html.append("<option value=\"" + i + "\">" + i + "</option>");
			}
		}
		html.append("</select>");
		
		html.append(getOverview(context));
			
		html.append("</div>");
		
		html.append("<div style=\"position : absolute; left : 450px; top : 0px; width : 800px; bottom : 0px; border : 1px solid red;\">");
		
		html.append(chequeList.toHTML(context));	
	
		html.append("</div>");
		
		return html.toString();
	}
	
	public String getOverview(ApplicationContext context){
		
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
		
		ObjectCollection results = new ObjectCollection("Results", "*");
		
		//String sql = "SELECT count(t2.id)  AS CNT, t4.FamilyName, t4.FirstName FROM Cheque AS t1 JOIN OrganisationMember AS t2";
		//sql += " ON t1.OrganisationMemberIssued=t2.id AND t1.organisationmembercashed > 0";
		
		String sql = " SELECT t1.ID, t3.FamilyName, t3.FirstName, (SELECT Count(ID) FROM Cheque WHERE OrganisationMemberIssued=t1.ID AND OrganisationMemberCashed=0 AND extract(year from DateValuta)=" + userSession.getSelectedYear() + ") AS CNT1, (SELECT Count(ID) FROM Cheque WHERE OrganisationMemberIssued=t1.ID AND OrganisationMemberCashed>0 AND extract(year from DateValuta)=" + userSession.getSelectedYear() + ") AS CNT2 ";
		sql += " FROM OrganisationMember AS t1";
		sql += " JOIN Person AS t2 ON t1.Person=t2.ID";
		sql += " JOIN Identity AS t3 ON t3.PersonID=t2.ID";
		
		sql += " WHERE (SELECT Count(ID) FROM Cheque WHERE OrganisationMemberIssued=t1.ID) > 0";
		


		sql += " ORDER BY t3.FamilyName, t3.FirstName";
		ocs.queryData(sql, results);
		
		StringBuilder html = new StringBuilder();
		
		html.append("<table>");
		
		html.append("<tr><td class=\"tableheader\">Name</td><td class=\"tableheader\">Vorname</td><td class=\"tableheader\">offen</td><td class=\"tableheader\">eingel.</td><td class=\"tableheader\">total</td><td class=\"datacell\"><a href=\"javascript:filterList2('chequeList','OrganisationMemberIssued=')\">Alle</td></tr>");
		
		int sum1 = 0;
		int sum2 = 0;
		
		boolean even = false;
		
		for(BasicClass record : results.getObjects()){
			if(even){
				html.append("<tr class=\"even\">");
			}
			else{
				html.append("<tr class=\"odd\">");				
			}
			even = !even;
			
			html.append("<td class=\"datacell\">" + record.getString("FAMILYNAME") + "</td>");	
			html.append("<td class=\"datacell\">" + record.getString("FIRSTNAME") + "</td>");	
			
			int cnt1 = record.getInt("CNT1");
			int cnt2 = record.getInt("CNT2");
			
			html.append("<td class=\"datacell\">" + cnt1 + "</td>");
			html.append("<td class=\"datacell\">" + cnt2 + "</td>");
			html.append("<td class=\"datacell\">" + (cnt1 + cnt2) + "</td>");
			html.append("<td class=\"datacell\"><a href=\"javascript:filterList2('chequeList','OrganisationMemberIssued=" + record.getString("ID") + "')\">Anzeigen</td>");
			
			html.append("</tr>");
			
			sum1 = sum1 + cnt1;
			sum2 = sum2 + cnt2;
			
		}
		
		html.append("<tr><td>Total</th><td></td><td>" + sum1 + "</td><td>" + sum2 + "</td><td>" + (sum1 + sum2) + "</td></tr>");
		
		html.append("</table>");
		
		return html.toString();
		
	}
	
	
	
}