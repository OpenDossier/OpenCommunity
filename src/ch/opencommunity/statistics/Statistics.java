package ch.opencommunity.statistics;

import ch.opencommunity.server.*;

import org.kubiki.base.BasicClass;

import java.sql.*;

public class Statistics extends BasicClass{
	
	OpenCommunityServer ocs;
	
	String[] months = {"", "Januar", "Februar", "März", "April", "Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Dezember"};
	
	
	public Statistics(OpenCommunityServer ocs){
		this.ocs = ocs;
	}
	
	public String toHTML(){
		
		StringBuilder html = new StringBuilder();
		html.append("<div id=\"statistics\">");
		html.append(getStatistics());
		html.append("</div>");
		return html.toString();
		
	}
	public String getStatistics(){
		
		StringBuilder html = new StringBuilder();
		
		Connection con = ocs.getConnection();
		try{
			Statement stmt = con.createStatement();
			
			html.append("<h4>Inserate</th>");
			
			String sql = "SELECT Count(ID) AS cnt, Extract(Year FROM ValidFrom) AS year, Extract(Month FROM ValidFrom) AS month FROM MemberAd";
			sql += " GROUP BY Extract(Year FROM ValidFrom), Extract(Month FROM ValidFrom)";
			sql += " ORDER BY 2,3";
			
			ResultSet res = stmt.executeQuery(sql);
			
			html.append("<table>");
			
			html.append("<tr><th>Anzahl</th><th>Jahr</th><th>Monat</th>");
			
			while(res.next()){
				
				html.append("<tr>");
				
				html.append("<td>" + res.getInt("CNT") + "</td>");
				html.append("<td>" + res.getInt("YEAR") + "</td>");
				html.append("<td>" + months[res.getInt("MONTH")] + "</td>");
				
				html.append("</tr>");
			}
			html.append("</table>");
			
			//-------------------------------------------------------------------------------------------------------------------------------------
			
			html.append("<h4>Adressbestellungen</th>");
			
			sql = "SELECT Count(ID) AS cnt, Extract(Year FROM DateCreated) AS year, Extract(Month FROM DateCreated) AS month FROM MemberAdRequest";
			sql += " GROUP BY Extract(Year FROM DateCreated), Extract(Month FROM DateCreated)";
			sql += " ORDER BY 2,3";
			
			res = stmt.executeQuery(sql);
			
			html.append("<table>");
			
			html.append("<tr><th>Anzahl</th><th>Jahr</th><th>Monat</th>");
			
			while(res.next()){
				
				html.append("<tr>");
				
				html.append("<td>" + res.getInt("CNT") + "</td>");
				html.append("<td>" + res.getInt("YEAR") + "</td>");
				html.append("<td>" + months[res.getInt("MONTH")] + "</td>");
				
				html.append("</tr>");
			}
			html.append("</table>");
			
		}
		catch(java.lang.Exception e){
			html.append(e.toString());
		}
		try{
			con.close();
		}
		catch(java.lang.Exception e){
			
		}
		
		return html.toString();
		
	}
	
	
}