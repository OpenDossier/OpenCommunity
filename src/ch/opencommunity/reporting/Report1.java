package ch.opencommunity.reporting;

import org.kubiki.reporting.*;

import org.kubiki.base.ActionHandler;
import org.kubiki.base.ActionResult;
import org.kubiki.application.ApplicationContext;

public class Report1 extends BaseReport{
	
	public Report1(){
		
		setProperty("Title", "Angaben zu Benutzern");	
		setProperty("InitialParameters", "useronly=1&new2016=1");
	}
	public void execute(ApplicationContext context, ActionResult result){
		
		boolean useronly = false;
		boolean new2016 = false;
		
		if(context.getString("useronly") != null && context.getString("useronly").equals("1")){
			useronly = true;	
		}
		if(context.getString("new2016") != null && context.getString("new2016").equals("1")){
			new2016 = true;	
		}
		
		StringBuilder html = new StringBuilder();
		
		html.append("<form id=\"reportproperties\">");
		html.append("<input type=\"checkbox\" name=\"useronly\" value=\"1\"");
		if(useronly){
			html.append(" CHECKED");
		}
		html.append(">Nur Benutzer");
		html.append("<input type=\"checkbox\" name=\"new2016\" value=\"1\"");
		if(new2016){
			html.append(" CHECKED");
		}
		html.append(">Nur Neuzugänge 2016");
		html.append("</form>");
		
		html.append("<input type=\"button\" onclick=\"reloadReport('" + getPath() + "')\" value=\"Neu laden\">");
		
		//-------------------------------------------------------------------------------------------------------------------------
		
		html.append("<table>");
		
		html.append("<tr><td class=\"tableheader\">2016</td><td class=\"tableheader\">2017</td></tr>");
		
		html.append("<tr>");
		
		
		html.append("<td valign=\"top\">");
		
		html.append("<p class=\"charttitle\">Geschlecht</p>");
		
		String sql = "SELECT COUNT(t1.ID), SEX";
		sql += " FROM OrganisationMember AS t1 JOIN Person AS t2 ON t1.Person=t2.ID";
		sql += " JOIN Identity AS t3 ON t3.PersonID=t2.ID";
		sql += " JOIN ADDRESS AS t4 ON t4.PersonID=t2.ID";
		sql += " LEFT JOIN MemberRole AS t5 ON t5.OrganisationMemberID=t1.ID AND t5.Role=1";
		sql += " WHERE t1.Status=1";
		
		if(new2016){
			sql += " AND t1.DateCreated >='2016-01-01' AND t1.DateCreated <= '2016-12-31'";
		}
		if(useronly){
			sql += " AND t5.ID IS NOT NULL";
		}
		
		sql += " GROUP BY SEX";
		
		html.append(getQueryTable(sql));
		
		html.append("</td><td valign=\"top\">");
		
		html.append("<p class=\"charttitle\">Geschlecht</p>");
		
		sql = "SELECT COUNT(t1.ID), SEX";
		sql += " FROM OrganisationMember AS t1 JOIN Person AS t2 ON t1.Person=t2.ID";
		sql += " JOIN Identity AS t3 ON t3.PersonID=t2.ID";
		sql += " JOIN ADDRESS AS t4 ON t4.PersonID=t2.ID";
		sql += " LEFT JOIN MemberRole AS t5 ON t5.OrganisationMemberID=t1.ID AND t5.Role=1";
		sql += " WHERE t1.Status=1";
		
		if(new2016){
			sql += " AND t1.DateCreated >='2017-01-01' AND t1.DateCreated <= '2017-12-31'";
		}
		if(useronly){
			sql += " AND t5.ID IS NOT NULL";
		}
		
		sql += " GROUP BY SEX";
		
		html.append(getQueryTable(sql));
		
		html.append("</td>");
		
		
		html.append("</tr><tr>");
		
		//-------------------------------------------------------------------------------------------------------------------------
		
		html.append("<td valign=\"top\">");
		
		html.append("<p class=\"charttitle\">Alter</p>");
		
		sql = "SELECT COUNT(t1.ID), (2017 - (CASE WHEN t3.DateOfBirth ~ E'^\\\\d+$' THEN CAST(t3.DateOfBirth AS Integer) ELSE 0 END)) AS AGE";
		
		//CASE WHEN myfield~E'^\\d+$' THEN myfield::integer ELSE 0 END
		
		sql += " FROM OrganisationMember AS t1 JOIN Person AS t2 ON t1.Person=t2.ID";
		sql += " JOIN Identity AS t3 ON t3.PersonID=t2.ID";
		sql += " JOIN ADDRESS AS t4 ON t4.PersonID=t2.ID";
		sql += " LEFT JOIN MemberRole AS t5 ON t5.OrganisationMemberID=t1.ID AND t5.Role=1";
		sql += " WHERE t1.Status=1";
		
		if(new2016){
			sql += " AND t1.DateCreated >='2016-01-01' AND t1.DateCreated <= '2016-12-31'";
		}
		if(useronly){
			sql += " AND t5.ID IS NOT NULL";
		}
		
		sql += " GROUP BY (2017 - (CASE WHEN t3.DateOfBirth ~ E'^\\\\d+$' THEN CAST(t3.DateOfBirth AS Integer) ELSE 0 END))";
		sql += " ORDER BY 2";
		
		html.append(getQueryTable(sql));
		
		html.append("</td><td valign=\"top\">");
		
		html.append("<p class=\"charttitle\">Alter</p>");
		
		sql = "SELECT COUNT(t1.ID), (2017 - (CASE WHEN t3.DateOfBirth ~ E'^\\\\d+$' THEN CAST(t3.DateOfBirth AS Integer) ELSE 0 END)) AS AGE";
		
		//CASE WHEN myfield~E'^\\d+$' THEN myfield::integer ELSE 0 END
		
		sql += " FROM OrganisationMember AS t1 JOIN Person AS t2 ON t1.Person=t2.ID";
		sql += " JOIN Identity AS t3 ON t3.PersonID=t2.ID";
		sql += " JOIN ADDRESS AS t4 ON t4.PersonID=t2.ID";
		sql += " LEFT JOIN MemberRole AS t5 ON t5.OrganisationMemberID=t1.ID AND t5.Role=1";
		sql += " WHERE t1.Status=1";
		
		if(new2016){
			sql += " AND t1.DateCreated >='2017-01-01' AND t1.DateCreated <= '2017-12-31'";
		}
		if(useronly){
			sql += " AND t5.ID IS NOT NULL";
		}
		
		sql += " GROUP BY (2017 - (CASE WHEN t3.DateOfBirth ~ E'^\\\\d+$' THEN CAST(t3.DateOfBirth AS Integer) ELSE 0 END))";
		sql += " ORDER BY 2";
		
		html.append(getQueryTable(sql));
		
		html.append("</td>");
		
		
		html.append("</tr><tr>");
		
		//-------------------------------------------------------------------------------------------------------------------------
		
		html.append("<td valign=\"top\">");
		
		html.append("<p class=\"charttitle\">PLZ</p>");
		
		sql = "SELECT COUNT(t1.ID), t4.ZIPCODE";
		sql += " FROM OrganisationMember AS t1 JOIN Person AS t2 ON t1.Person=t2.ID";
		sql += " JOIN Identity AS t3 ON t3.PersonID=t2.ID";
		sql += " JOIN ADDRESS AS t4 ON t4.PersonID=t2.ID";
		sql += " LEFT JOIN MemberRole AS t5 ON t5.OrganisationMemberID=t1.ID AND t5.Role=1";
		sql += " WHERE t1.Status=1";
		
		if(new2016){
			sql += " AND t1.DateCreated >='2016-01-01' AND t1.DateCreated <= '2016-12-31'";
		}
		if(useronly){
			sql += " AND t5.ID IS NOT NULL";
		}
		
		sql += " GROUP BY t4.ZIPCODE";
		sql += " ORDER BY 2";
		
		html.append(getQueryTable(sql));
		
		html.append("</td><td valign=\"top\">");
		
		html.append("<p class=\"charttitle\">PLZ</p>");
		
		sql = "SELECT COUNT(t1.ID), t4.ZIPCODE";
		sql += " FROM OrganisationMember AS t1 JOIN Person AS t2 ON t1.Person=t2.ID";
		sql += " JOIN Identity AS t3 ON t3.PersonID=t2.ID";
		sql += " JOIN ADDRESS AS t4 ON t4.PersonID=t2.ID";
		sql += " LEFT JOIN MemberRole AS t5 ON t5.OrganisationMemberID=t1.ID AND t5.Role=1";
		sql += " WHERE t1.Status=1";
		
		if(new2016){
			sql += " AND t1.DateCreated >='2017-01-01' AND t1.DateCreated <= '2017-12-31'";
		}
		if(useronly){
			sql += " AND t5.ID IS NOT NULL";
		}
		
		sql += " GROUP BY t4.ZIPCODE";
		sql += " ORDER BY 2";
		
		html.append(getQueryTable(sql));
		
		html.append("</td>");
		
		
		html.append("</tr><tr>");
		
		//-------------------------------------------------------------------------------------------------------------------------
		
		html.append("<td valign=\"top\">");
		
		html.append("<p class=\"charttitle\">Email Vorhanden</p>");
		
		sql = "SELECT COUNT(t1.ID), CASE WHEN t6.ID IS NOT NULL THEN 'JA' ELSE 'NEIN' END AS EmailVorhanden";
		sql += " FROM OrganisationMember AS t1 JOIN Person AS t2 ON t1.Person=t2.ID";
		sql += " JOIN Identity AS t3 ON t3.PersonID=t2.ID";
		sql += " JOIN ADDRESS AS t4 ON t4.PersonID=t2.ID";
		sql += " LEFT JOIN MemberRole AS t5 ON t5.OrganisationMemberID=t1.ID AND t5.Role=1";
		sql += " LEFT JOIN Contact AS t6 ON t6.PersonID=t2.ID AND t6.Type=3";
		sql += " WHERE t1.Status=1";
		
		if(new2016){
			sql += " AND t1.DateCreated >='2016-01-01' AND t1.DateCreated <= '2016-12-31'";
		}
		if(useronly){
			sql += " AND t5.ID IS NOT NULL";
		}
		
		sql += " GROUP BY CASE WHEN t6.ID IS NOT NULL THEN 'JA' ELSE 'NEIN' END";
		sql += " ORDER BY 2";
		
		html.append(getQueryTable(sql));
		
		html.append("</td><td valign=\"top\">");
		
		html.append("<p class=\"charttitle\">Email Vorhanden</p>");
		
		sql = "SELECT COUNT(t1.ID), CASE WHEN t6.ID IS NOT NULL THEN 'JA' ELSE 'NEIN' END AS EmailVorhanden";
		sql += " FROM OrganisationMember AS t1 JOIN Person AS t2 ON t1.Person=t2.ID";
		sql += " JOIN Identity AS t3 ON t3.PersonID=t2.ID";
		sql += " JOIN ADDRESS AS t4 ON t4.PersonID=t2.ID";
		sql += " LEFT JOIN MemberRole AS t5 ON t5.OrganisationMemberID=t1.ID AND t5.Role=1";
		sql += " LEFT JOIN Contact AS t6 ON t6.PersonID=t2.ID AND t6.Type=3";
		sql += " WHERE t1.Status=1";
		
		if(new2016){
			sql += " AND t1.DateCreated >='2017-01-01' AND t1.DateCreated <= '2017-12-31'";
		}
		if(useronly){
			sql += " AND t5.ID IS NOT NULL";
		}
		
		sql += " GROUP BY CASE WHEN t6.ID IS NOT NULL THEN 'JA' ELSE 'NEIN' END";
		sql += " ORDER BY 2";
		
		html.append(getQueryTable(sql));
		
		html.append("</td>");
		
		
		html.append("</tr><tr>");
		
		//-------------------------------------------------------------------------------------------------------------------------
		
		html.append("<td valign=\"top\">");
		
		html.append("<p class=\"charttitle\">Erstsprache</p>");
			
		sql = "SELECT COUNT(t1.ID), t3.FIRSTLANGUAGES";
		sql += " FROM OrganisationMember AS t1 JOIN Person AS t2 ON t1.Person=t2.ID";
		sql += " JOIN Identity AS t3 ON t3.PersonID=t2.ID";
		sql += " JOIN ADDRESS AS t4 ON t4.PersonID=t2.ID";
		sql += " LEFT JOIN MemberRole AS t5 ON t5.OrganisationMemberID=t1.ID AND t5.Role=1";
		sql += " WHERE t1.Status=1";
		
		if(new2016){
			sql += " AND t1.DateCreated >='2016-01-01' AND t1.DateCreated <= '2016-12-31'";
		}
		if(useronly){
			sql += " AND t5.ID IS NOT NULL";
		}
		
		sql += " GROUP BY t3.FIRSTLANGUAGES";
		sql += " ORDER BY 1";
		
		html.append(getQueryTable(sql));
		
		html.append("</td><td valign=\"top\">");
		
		html.append("<p class=\"charttitle\">Erstsprache</p>");
			
		sql = "SELECT COUNT(t1.ID), t3.FIRSTLANGUAGES";
		sql += " FROM OrganisationMember AS t1 JOIN Person AS t2 ON t1.Person=t2.ID";
		sql += " JOIN Identity AS t3 ON t3.PersonID=t2.ID";
		sql += " JOIN ADDRESS AS t4 ON t4.PersonID=t2.ID";
		sql += " LEFT JOIN MemberRole AS t5 ON t5.OrganisationMemberID=t1.ID AND t5.Role=1";
		sql += " WHERE t1.Status=1";
		
		if(new2016){
			sql += " AND t1.DateCreated >='2017-01-01' AND t1.DateCreated <= '2017-12-31'";
		}
		if(useronly){
			sql += " AND t5.ID IS NOT NULL";
		}
		
		sql += " GROUP BY t3.FIRSTLANGUAGES";
		sql += " ORDER BY 1";
		
		html.append(getQueryTable(sql));
		
		html.append("</td>");
		
		
		html.append("</tr><tr>");
		
		//-------------------------------------------------------------------------------------------------------------------------
		
		html.append("<td valign=\"top\">");
		
		html.append("<p class=\"charttitle\">Zweitsprachen</p>");
			
		sql = "SELECT COUNT(t1.ID), t2.LANGUAGES";
		sql += " FROM OrganisationMember AS t1 JOIN Person AS t2 ON t1.Person=t2.ID";
		sql += " JOIN Identity AS t3 ON t3.PersonID=t2.ID";
		sql += " JOIN ADDRESS AS t4 ON t4.PersonID=t2.ID";
		sql += " LEFT JOIN MemberRole AS t5 ON t5.OrganisationMemberID=t1.ID AND t5.Role=1";
		sql += " WHERE t1.Status=1";
		
		if(new2016){
			sql += " AND t1.DateCreated >='2016-01-01' AND t1.DateCreated <= '2016-12-31'";
		}
		if(useronly){
			sql += " AND t5.ID IS NOT NULL";
		}
		
		sql += " GROUP BY t2.LANGUAGES";
		sql += " ORDER BY 1";
		
		html.append(getQueryTable(sql));
		
		html.append("</td><td valign=\"top\">");
		
		html.append("<p class=\"charttitle\">Zweitsprachen</p>");
			
		sql = "SELECT COUNT(t1.ID), t2.LANGUAGES";
		sql += " FROM OrganisationMember AS t1 JOIN Person AS t2 ON t1.Person=t2.ID";
		sql += " JOIN Identity AS t3 ON t3.PersonID=t2.ID";
		sql += " JOIN ADDRESS AS t4 ON t4.PersonID=t2.ID";
		sql += " LEFT JOIN MemberRole AS t5 ON t5.OrganisationMemberID=t1.ID AND t5.Role=1";
		sql += " WHERE t1.Status=1";
		
		if(new2016){
			sql += " AND t1.DateCreated >='2017-01-01' AND t1.DateCreated <= '2017-12-31'";
		}
		if(useronly){
			sql += " AND t5.ID IS NOT NULL";
		}
		
		sql += " GROUP BY t2.LANGUAGES";
		sql += " ORDER BY 1";
		
		html.append(getQueryTable(sql));
		
		html.append("</td>");
		
		
		html.append("</tr></table>");
		

		
		result.setData(html.toString());
		result.setParam("dataContainer", "editArea");
		
	}
	
	
}