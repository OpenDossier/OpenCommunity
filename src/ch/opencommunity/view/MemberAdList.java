package ch.opencommunity.view;

import ch.opencommunity.server.OpenCommunityServer;
import ch.opencommunity.common.OpenCommunityUserSession;

import org.kubiki.base.ObjectCollection;
import org.kubiki.base.BasicClass;
import org.kubiki.application.Application;
import org.kubiki.application.ApplicationContext;
import org.kubiki.util.DateConverter;

import org.kubiki.servlet.WebApplication;
import org.kubiki.application.server.WebApplicationClient;

import org.kubiki.gui.FilterParameters;
import org.kubiki.gui.html.HTMLQueryList;
import org.kubiki.gui.html.HTMLColumnDefinition;
import org.kubiki.gui.html.HTMLQueryListFilter;


public class MemberAdList extends HTMLQueryList{
	
	public MemberAdList(WebApplication webapp){
		super(webapp);
		
		setID("memberAdList");
		setName("memberAdList");
		
		HTMLQueryListFilter filter = new HTMLQueryListFilter(){
			
			public String toHTML(ApplicationContext context, FilterParameters parameters){
				
				
				
				OpenCommunityServer ocs = (OpenCommunityServer)getWebApplication();
				OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
				
				StringBuilder filterdef = new StringBuilder();
				
				filterdef.append("<form name=\"filter_" + getName() + "\" id=\"filter_" + getName() + "\">Filtern nach : ");
				
				filterdef.append("<table>");
				
				
				filterdef.append("<tr><td>Titel</td><td>" + ocs.getFormManager().getTextField(userSession.getProperty("Title"), true, "", null, null, "onkeyup=\"filterList2('" + getName() + "')\")") + "</td></tr>");
				filterdef.append("<tr><td>Beschreibung</td><td>"+ ocs.getFormManager().getTextField(userSession.getProperty("Description"), true, "", null, null, "onkeyup=\"filterList2('" + getName() + "')\")") + "</td></tr>");
				filterdef.append("<tr><td>Sprache</td><td>" + ocs.getFormManager().getTextField(userSession.getProperty("Language"), true, "", null, null, "onkeyup=\"filterList2('" + getName() + "')\")") + "</td></tr>");
				filterdef.append("<tr><td>Rubrik</td><td>" + ocs.getFormManager().getSelection(userSession.getProperty("Category"), true, "") + "</td></tr>");
				filterdef.append("<tr><td>Typ</td><td>" + ocs.getFormManager().getSelection(userSession.getProperty("Type"), true, "") + "</td></tr>");
				filterdef.append("<tr><td>Status</td><td>" + ocs.getFormManager().getSelection(userSession.getProperty("Status"), true, "") + "</td></tr>");
									
				filterdef.append("<tr><td>Geschlecht</td><td>" + ocs.getFormManager().getSelection(userSession.getProperty("Sex"), true, "") + "</td></tr>");
				filterdef.append("<tr><td>Alter</td><td><input name=\"Age\" value=\"" + "\">" + "</td></tr>");
									
				filterdef.append("<tr><td>Gültig von zw.</td><td>" + ocs.getFormManager().getDateField(userSession.getProperty("ValidFrom1"), true, "") + "</td></tr>");
				filterdef.append("<tr><td>und</td><td>" + ocs.getFormManager().getDateField(userSession.getProperty("ValidFrom2"), true, "") + "</td></tr>");
				/*
				filterdef.append(" Keine Email <input type=\"checkbox\" name=\"NoEmail\" value=\"1\">");
				filterdef.append(" Benutzer <input type=\"checkbox\" name=\"IsUser\" value=\"1\">");
				filterdef.append(" Mitglied <input type=\"checkbox\" name=\"IsMember\" value=\"1\">");
				filterdef.append(" Gönner <input type=\"checkbox\" name=\"IsSponsor\" value=\"1\">");
				*/
				
				filterdef.append("</table>");	
				filterdef.append("</form>");
				filterdef.append("<input type=\"button\" onclick=\"filterList2('" + getName() + "')\"  value=\"Filtern\">");
									

										
				return filterdef.toString();				
				
				
			}
			public String getFilterString(ApplicationContext context){

				String filterstring = "";
								
				String title = context.getString("Title");
				String operator = " WHERE ";
				if(title != null && title.length() > 0){
					filterstring += operator + "t1.Title ILIKE \'%" + title + "%\'";
					operator = " AND ";
				}
				else{
					title = "";	
				}
								
				String description = context.getString("Description");
				if(description != null && description.length() > 0){
					filterstring += operator + "t1.Description ILIKE \'%" + description + "%\'";
					operator = " AND ";
				}
				else{
					description = "";	
				}
								
				String language = context.getString("Language");
				if(language != null && language.length() > 0){
					filterstring += operator + "t4.FirstLangaugeS ILIKE \'%" + language + "%\'";
					operator = " AND ";
				}
				else{
					language = "";	
				}
								
				String category = context.getString("Category");
				if(category != null && category.length() > 0){
					filterstring += operator + "t1.Template=" + category;
					operator = " AND ";
				}
				else{
					category = "";	
				}
								
				String type = context.getString("Type");
				if(type != null && type.length() > 0){
									
					if(type.equals("0")){
						filterstring += operator + "t1.IsOffer='true'";
						operator = " AND ";
					}
					else if(type.equals("1")){
						filterstring += operator + "t1.IsRequest='true'";
						operator = " AND ";
					}
					else if(type.equals("2")){
						filterstring += operator + "t1.IsOffer='true'";
						filterstring += operator + "t1.IsRequest='true'";
						operator = " AND ";
					}
									
				}
				else{
					type = "";	
				}
								
				String status = context.getString("Status");
				if(status != null && status.length() > 0){
					filterstring += operator + "t1.Status=" + status;
					operator = " AND ";
				}
				else{
					status = "";	
				}
								
				String sex = context.getString("Sex");
				if(sex != null && sex.length() > 0){
					filterstring += operator + "t4.Sex=" + sex;
					operator = " AND ";
				}
				else{
					sex = "";	
				}
								
				String age = context.getString("Age");
				if(age != null && age.length() > 0){
					try{
						int yearofbirth = 2016 - Integer.parseInt(age);
						filterstring += operator + "t4.DateOfBirth ILIKE \'%" + yearofbirth + "%\'";
						operator = " AND ";
					}
					catch(java.lang.Exception e){
						//logException(e);	
					}
									
				}
				else{
					age = "";	
				}
								
				String validfrom1 = context.getString("ValidFrom1");
				if(validfrom1 != null && validfrom1.length() > 0){
					filterstring += operator + "t1.ValidFrom > '" + validfrom1 + "'";
					operator = " AND ";
				}
				else{
					validfrom1 = "";	
				}
								
				String validfrom2 = context.getString("ValidFrom2");
				if(validfrom2 != null && validfrom2.length() > 0){
					filterstring += operator + "t1.ValidFrom < '" + validfrom2 + "'";
					operator = " AND ";
				}
				else{
					validfrom2 = "";	
				}
		
				 return filterstring;
			
			}
		};
		
		setFilter(filter);
	}
	


	public static String getMemberAdList(Application application, ApplicationContext context, OpenCommunityUserSession usersession, String filter){
		return  getMemberAdList(application, context, usersession, filter, "memberads");
	}
	public static String getMemberAdList(Application application, ApplicationContext context, OpenCommunityUserSession usersession, String filter, String sectionid){
		return "";
	}
	public void getDataTable(StringBuilder html, ApplicationContext context){
		
		WebApplicationClient userSession = (WebApplicationClient)context.getObject("usersession");
		
		userSession.saveFilterProperties(context);
		
		String sectionid = "memberads";
		if(context.hasProperty("sectionid")){
			sectionid = context.getString("sectionid");
		}
		
		String[] types = {"Angebot", "Nachfrage", "Tandem"};
		
		int offset = 0; 		
		String soffset = context.getString("offset");
		if(soffset != null){
			try{
				offset = Integer.parseInt(soffset);
			}
			catch(java.lang.Exception e){
				
			}
		}

		
		int limit = 18;
		
		String direction = "DESC";
		
		String sortfield = (String)userSession.get("sortfield");
		if(sortfield==null){
			sortfield = " t1.DateCreated DESC, t1.Status, t4.FamilyName";	
		}
		if(context.hasProperty("columnid")){
			
			if(sortfield.equals(context.getString("columnid"))){ //gleiche Spalte zum 2.Mal geklickt
				direction = (String)userSession.get("direction");
				if(direction==null || direction.equals("ASC")){
					direction = "DESC";
				}
				else{
					direction = "ASC";
				}
					
			}
			else{
				direction = "DESC";
			}
			sortfield = context.getString("columnid");	
			userSession.put("direction", direction);
			
		}
		userSession.put("sortfield", sortfield);
		

		
		String[] status = {"erfasst", "freigeschaltet", "pausiert", "inaktiv", "zu kontrollieren"};
	

		ObjectCollection results = new ObjectCollection("Results", "*");
		String sql = "SELECT t1.ID, t1.Title, t1.Status, t1.NotificationStatus, t1.Type, t1.Description, t2.ID AS OMID, t4.FamilyName, t4.FirstName, t5.Title AS TemplateTitle , t1.ValidFrom, t1.ValidUntil, COUNT(t6.ID) AS CNT, t8.DateCreated, count(*) OVER() AS total_count";
		sql += " FROM MemberAd AS t1";
		sql += " LEFT JOIN OrganisationMember AS t2 ON t1.OrganisationMemberID=t2.ID";
		sql += " LEFT JOIN Person AS t3 ON t2.Person=t3.ID";
		sql += " LEFT JOIN Identity AS t4 ON t4.PersonID=t3.ID";
		sql += " LEFT JOIN MemberAdCategory AS t5 ON t1.Template=t5.ID";
		sql += " LEFT JOIN MemberAdRequest AS t6 ON t1.ID=t6.MemberAd";
		sql += " LEFT JOIN ActivityObject AS t7 ON t1.ID=t7.MemberAdID";
		sql += " LEFT JOIN Activity AS t8 ON t7.ActivityID=t8.ID AND t8.Template=4";
		
		if(context.hasProperty("filterstring")){
			sql += context.getString("filterstring");
		}
		else if(getFilter() != null){
		     sql += getFilter().getFilterString(context);
		}
		sql += " GROUP BY t1.ID, t1.Title, t1.Type, t2.Status, t2.ID, t4.FamilyName, t4.FirstName, t5.Title, t1.Status, t1.ValidFrom, t1.ValidUntil, t1.NotificationStatus, t8.DateCreated";

		sql += " ORDER BY " + sortfield + " " + direction;
		
		sql += " LIMIT " + limit + " OFFSET " + offset;
		
		getWebApplication().queryData(sql, results);
		if(sectionid.equals("home")){
			html.append("<div>");
		}
		else{
			html.append("<div class=\"tablearea\">");
		}
		
		html.append("<table>");
		html.append("<tr class=\"tableheader\"><th>ID</th>");

		html.append("<th class=\"tableheader\"><a class=\"tableheader\" href=\"href=\"javascript:sortTable(\'" + getID() + "\',\'t1.Status\')\">Status</a></th>");
		html.append("<th class=\"tableheader\"><a class=\"tableheader\" href=\"javascript:sortTable(\'" + getID() + "\',\'t1.Title\')\">Titel</a></th>");
		html.append("<th class=\"tableheader\"><a class=\"tableheader\" href=\"javascript:sortTable(\'" + getID() + "\',\'t5.Title\')\">Rubrik</a></th>");
		html.append("<th class=\"tableheader\"><a class=\"tableheader\" href=\"javascript:sortTable(\'" + getID() + "\',\'t1.Type\')\">Typ</a></th>");
		html.append("<th class=\"tableheader\">Nachname / Vorname</th>");
		html.append("<th class=\"tableheader\"><a class=\"tableheader\" href=\"javascript:sortTable(\'" + getID() + "\',\'t1.ValidFrom\')\">Gültig von</a></th>");
		html.append("<th class=\"tableheader\"><a class=\"tableheader\" href=\"javascript:sortTable(\'" + getID() + "\',\'t1.ValidUntil\')\">Gültig bis</a></th>");
		html.append("<th class=\"tableheader\"><a class=\"tableheader\" href=\"javascript:sortTable(\'" + getID() + "\',\'t1.NotificationStatus\')\">Benachrichtigt</a></th>");
		html.append("<th class=\"tableheader\">Email versendet</th>");
		html.append("<th class=\"tableheader\"><a class=\"tableheader\" href=\"javascript:sortTable(\'" + getID() + "\',\'10\')\">Adressbestellungen</a></th>");
		html.append("</tr>");
		
		String maid = (String)userSession.get("MAID");
		
		int total_count = 0;

		
		try{
			int odd = -1;
			for(BasicClass record : results.getObjects()){
				
				if(maid != null && maid.equals(record.getString("ID"))){
					html.append("<tr class=\"highlight\">");		
				}	
				else if(odd==1){
					html.append("<tr class=\"odd\">");			
				}
				else{
					html.append("<tr class=\"even\">");
				}
				
				html.append("<td class=\"datacell\">"+ record.getString("ID") + "</td>");
				html.append("<td class=\"datacell\">" + status[record.getInt("STATUS")] + "</td>");
				html.append("<td class=\"datacell\">"+ record.getString("TITLE") + "</td>");
				html.append("<td class=\"datacell\">"+ record.getString("TEMPLATETITLE") + "</td>");
				int type = record.getInt("TYPE");
				if(type > -1){
					html.append("<td class=\"datacell\">"+ types[record.getInt("TYPE")] + "</td>");
				}
				else{
					html.append("<td class=\"datacell\"></td>");
				}
				//html.append("<td class=\"datacell\"><a href=\"javascript:createProcess('ch.opencommunity.process.OrganisationMemberEdit','OMID=" + record.getString("OMID") + "&section=memberads')\">" + record.getString("FAMILYNAME") + " " + record.getString("FIRSTNAME") + "</a></td>");
				html.append("<td class=\"datacell\"><a href=\"javascript:editOrganisationMember(" + record.getString("OMID") + ", 'section=memberads')\">" + record.getString("FAMILYNAME") + " " + record.getString("FIRSTNAME") + "</a></td>");
				//html.append("<td class=\"datacell\">"+ record.getString("FIRSTNAME") + "</td>");
				html.append("<td class=\"datacell\">"+ DateConverter.sqlToShortDisplay(record.getString("VALIDFROM")) + "</td>");
				html.append("<td class=\"datacell\">"+ DateConverter.sqlToShortDisplay(record.getString("VALIDUNTIL")) + "</td>");
				html.append("<td class=\"datacell\">"+ record.getString("NOTIFICATIONSTATUS") + "</td>");
				html.append("<td class=\"datacell\">"+ record.getString("DATECREATED") + "</td>");
				html.append("<td class=\"datacell\">"+ record.getString("CNT") + "</td>");
				
				if(record.getInt("STATUS")==0){
					//html.append("<td><a href=\"javascript:activateAd(" + record.getString("ID") + ",\'" + sectionid + "\')\">Freischalten</a></td>");
					html.append("<td><a href=\"javascript:activateAd(" + record.getString("OMID") + ",\'" + sectionid + "\')\">Freischalten</a></td>");
				}
				else{
					html.append("<td><a href=\"javascript:createProcess(\'ch.opencommunity.process.MemberAdDetail\',\'MAID=" + record.getString("ID") + "\')\">Bearbeiten</a></td>");
				}
				html.append("</tr>");
				
				if(odd==1){
					html.append("<tr class=\"odd\">");			
				}
				else{
					html.append("<tr class=\"even\">");
				}
				html.append("<td class=\"datacell\"></td><td class=\"datacell\" colspan=\"10\">" + record.getString("DESCRIPTION") + "</td></tr>");
				
				odd = -odd;
				
			}
		}
		catch(java.lang.Exception e){
			html.append(e.toString());	
		}
		html.append("</table>");
		
		html.append("</div>");
		
		if(!sectionid.equals("home")){
		
			html.append("<div class=\"datatablenavig\">");
			
			if(offset > limit-1){
				html.append("<input type=\"button\" onclick=\"moveTable(\'" + getID() + "\',\'" + (offset - limit) + "\')\" value=\"   <<  \">");
			}
			else{
				html.append("<input type=\"button\" onclick=\"moveTable(\'" + getID() + "\',\'" + (offset - limit) + "\')\" value=\"   <<  \" DISABLED>");
			}
			html.append("<input type=\"button\" onclick=\"moveTable(\'" + getID() + "\',\'" + (offset + limit) + "\')\" value=\"   >>  \">");
			html.append("<span>" + offset + "/" + total_count + "</span>");
			
			html.append("</div>");
		}
		

	}
	
	@Override
	public String toHTML(ApplicationContext context){
		
		WebApplicationClient userSession = (WebApplicationClient)context.getObject("usersession");
		OpenCommunityServer ocs = (OpenCommunityServer)getWebApplication();
		
		StringBuilder html = new StringBuilder();
		
		boolean hasToolbar = false;
		
		if(getToolbar() != null){ //move toolbar to embedding form/section
			html.append("<div class=\"toolbar2\">");	
			html.append(getToolbar());
			html.append("</div>");
			hasToolbar = true;
		}
		
		
		
		if(getFilter() != null){
			
			FilterParameters filterParameters = null;
			
			if(userSession.get(this.getName()) != null){
				filterParameters = (FilterParameters)userSession.get(this.getName());
			}
			else{
				filterParameters = new FilterParameters(getFilter());
				userSession.put(this.getName(), filterParameters);
			}
			
			
			
			html.append("<div id=\"listfilter\" class=\"listfilter\" style=\"position : absolute; left : 0px; width : 300px; top : 70px; bottom : 0px; overflow : auto;\">");
			
			
			
			html.append(getFilter().toHTML(context, filterParameters));
			
			html.append("</div>");
		}
		
		
		if(hasToolbar){
			html.append("<div class=\"datatable\"  id=\"" + getName() + "_tablearea\" style=\"top : 70px; left : 300px;\">");
		}
		else{
			html.append("<div class=\"datatable\"  id=\"" + getName() + "_tablearea\" style=\"top : 70px; left : 300px;\">");
		}
		
		getDataTable(html, context);
		
		html.append("</div>");
		
		
		return html.toString();
		
	}

		

	
}