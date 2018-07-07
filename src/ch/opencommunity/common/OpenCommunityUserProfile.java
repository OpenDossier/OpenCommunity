package ch.opencommunity.common;


import ch.opencommunity.base.*;
import ch.opencommunity.server.*;
import ch.opencommunity.common.*;
import ch.opencommunity.advertising.*;

import org.kubiki.base.*;
import org.kubiki.application.*;
import org.kubiki.application.server.*;
import org.kubiki.cms.*;
import org.kubiki.servlet.*;
import org.kubiki.util.DateConverter;
import org.kubiki.database.Record;

import java.util.Vector;
import java.util.Map;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.*;


public class OpenCommunityUserProfile extends BasicOCObject implements WebPageElementInterface{
	
	
	
	
	public String toHTML(ApplicationContext context){
		
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
		
		StringBuilder html = new StringBuilder();
		//html.append("<style type=\"text/css\">");
		//html.append("#display{     top : 101px; } ");
		//html.append("</style>");
		html.append("\n<form id=\"profilemenuparams\" name=\"profilemenuparams\">");
		html.append("<div id=\"userprofile\">");
		String sectionid = context.getString("sectionid");
		if(sectionid == null || sectionid.length()==0){
			sectionid = "news";
		}
		html.append(getProfileSection(sectionid, context));
		if(userSession.get("goodbye") != null && userSession.get("goodbye").equals("true")){
			OpenCommunityServer ocs = (OpenCommunityServer)getRoot();

			html.append("<div id=\"welcome\">");
			TextBlockAdministration tba = (TextBlockAdministration)ocs.getObjectByIndex("TextBlockAdministration", 0);
			if(tba != null){
				TextBlock tb = (TextBlock)tba.getObjectByName("TextBlock", "32");
				if(tb != null){
					html.append(tb.getString("Content"));	
				}
			}
			html.append("</div>");
		}
		html.append("</div>");
		html.append("</form>");
		

		

		
		return html.toString();		
	}
	public String getAdminForm(ApplicationContext context){
		return "";
	}
	public String toHTML(ApplicationContext context, List parameters){
		return toHTML(context, null);
	}
	public void getProfileMenu(StringBuilder html, OpenCommunityServer ocs, OrganisationMember om, HttpServletRequest request, Map addpars){
		getProfileMenu(html, ocs, om, request, addpars, null, null);	
	}
	public void getProfileMenu(StringBuilder html, OpenCommunityServer ocs, OrganisationMember om, HttpServletRequest request, Map addpars, Vector criteria1, Vector criteria2){
		getProfileMenu(html, ocs, om, request, addpars, criteria1, criteria2, null);
	}

	public void getProfileMenu(StringBuilder html, OpenCommunityServer ocs, OrganisationMember om, HttpServletRequest request,  Map addpars, Vector criteria1, Vector criteria2, Vector criteria3){
		
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)request.getSession().getAttribute("usersession");
		
		ch.opencommunity.server.HTMLForm formManager = ocs.getFormFactory();
		
		String cbimageselected = "/res/icons/cbimageselected.png";
		String cbimage = "/res/icons/cbimage.png";
		
		String url = request.getRequestURL().toString();
		
		html.append("\n<div id=\"profilemenu\">");
		
		String membericon = "profil-w.png";
		Identity identity = om.getPerson().getIdentity();
		if(identity.getID("Sex")==1){
			membericon = "profil-m.png"; 
		}
		
		
		/* Auf Wunsch des Kunden entfernt
		if(criteria1 != null){
			html.append("\n<div class=\"profilemenuitem\" style=\"background :url(\'" + ocs.getBaseURL(request) + "/opencommunity/websites/1/images/bg_rubriken_top.png'); height : 58px;\">&nbsp;</div>");
			html.append("\n<div class=\"profilemenuitem\"><img src=\"" + ocs.getBaseURL(request) + "/opencommunity/websites/1/images/picto/merken_aktiv.png\">verfeinern/filtern</div>");
		    html.append("\n<div class=\"profilemenuitem\" style=\"padding-top : 5px;\">Rubrik</div>");
			for(Object o : criteria1){
				String value = (String)o;
				html.append("\n<div class=\"profilemenuitem\">");
				if(addpars.get("reload") != null && addpars.get(value)==null){
					html.append(formManager.getCustomCheckbox(value, "false", false, cbimageselected, cbimage, value, "", "catlabel", "reloadProfileSection"));	
				}
				else{
					html.append(formManager.getCustomCheckbox(value, "true", true, cbimageselected, cbimage, value, "", "catlabel", "reloadProfileSection"));
				}
				html.append("</div>");
			}
			if(criteria2 != null){
		        //html.append("<div class=\"profilemenuitem\"><p style=\"background : white; border-bottom : 1px solid #979797;\"></p></div>");
		        html.append("<div style=\"background : white;\"><div style=\"margin-left : 8px; margin-right : 8px; background : white; border-bottom : 1px solid #979797;\"> </div></div>");
		        html.append("\n<div class=\"profilemenuitem\" style=\"padding-top : 5px;\">Ort / Quartier</div>");
				for(Object o : criteria2){					
					String value = (String)o;
					html.append("\n<div class=\"profilemenuitem\">");
					if(addpars.get("reload") != null && addpars.get(value)==null){
						html.append(formManager.getCustomCheckbox(value, "false", false, cbimageselected, cbimage, value, "", "catlabel", "reloadProfileSection"));	
					}
					else{
						html.append(formManager.getCustomCheckbox(value, "true", true, cbimageselected, cbimage, value, "", "catlabel", "reloadProfileSection"));
					}
					html.append("</div>");
				}
			}
			if(criteria3 != null){
				html.append("<div class=\"profilemenuitem\" style=\"border-bottom : 1px solid #979797 \"> </div>");
				for(Object o : criteria3){
					String value = (String)o;
					html.append("\n<div class=\"profilemenuitem\">");
					if(addpars.get("reload") != null && addpars.get(value)==null){
						html.append(formManager.getCustomCheckbox(value, "false", false, cbimageselected, cbimage, value, "", "catlabel", "reloadProfileSection"));	
					}
					else{
						html.append(formManager.getCustomCheckbox(value, "true", true, cbimageselected, cbimage, value, "", "catlabel", "reloadProfileSection"));
					}
					html.append("</div>");
				}				
			}
			html.append("\n<div id=\"profilefooter\" style=\"background :url(\'" + ocs.getBaseURL(request) + "/opencommunity/websites/1/images/bg_rubriken_bottom.png'); height : 28px\">&nbsp;</div>");		
		}
		*/
		
		html.append("\n<div class=\"profilemenuitem\" style=\"background :url(\'images/bg_rubriken_top.png'); height : 58px;\">&nbsp;</div>");
		//html.append("\n<div class=\"profilemenuitem\"><img src=\"" + ocs.getBaseURL(request) + "/opencommunity/websites/1/images/picto/profil-w.png;\">Mein Profil</div>");
		html.append("\n<div class=\"profilemenuitem\" style=\"color : #6CBB16;\"><img class=\"membericon\" src=\"/res/icons/" + membericon + "\">" + om.toString() + "</div>");
		
		html.append("<div style=\"background : white; heigth : 30px;\"><div style=\"margin-left : 8px; margin-right : 8px; background : white; border-bottom : 1px solid black;\"> </div></div>");
		
		
		//if(om.getObjects("OrganisationMemberRelationship").size() > 0){ //AK 20170918
		if(om.getID("Type") > 1){
			html.append("\n<div id=\"profileselection\" class=\"profilemenuitem\">");
			/*
			html.append("<select onchange=\"setActiveProfile(this.value)\" style=\"background : white;\">");
			html.append("<option value=\"" + om.getName() + "\">" + om.toString() + "</option>");
			for(BasicClass bc : om.getObjects("OrganisationMemberRelationship")){
				if(om.getActiveRelationship() != null && om.getActiveRelationship().getName().equals(bc.getString("OrganisationMember"))){
					html.append("<option value=\"" + bc.getID("OrganisationMember") + "\" SELECTED>" + bc.getString("Title") + "</option>");
				}
				else{
					html.append("<option value=\"" + bc.getID("OrganisationMember") + "\">" + bc.getString("Title") + "</option>");
				}
			}
			
			html.append("</select>";
			*/
			html.append("<b>Profil wechseln:</b>");
			
			if(om.getActiveRelationship() == null){
				html.append("<div id=\"" + om.getName() + "\" class=\"profileselection_active\" onclick=\"setActiveProfile(this.id)\">" + om.toString() + "</div>");
			}
			else{
				html.append("<div id=\"" + om.getName() + "\" class=\"profileselection_inactive\" onclick=\"setActiveProfile(this.id)\">" + om.toString() + "</div>");
			}
			
			for(BasicClass bc : om.getObjects("OrganisationMemberRelationship")){
				if(bc.getID("Status")==0){
					if(om.getActiveRelationship() != null && om.getActiveRelationship().getName().equals(bc.getString("OrganisationMember"))){
						html.append("<div id=\"" + bc.getID("OrganisationMember") + "\" class=\"profileselection_active\" onclick=\"setActiveProfile(this.id)\">" + bc.getString("Title") + "</div>");
					}
					else{
						html.append("<div id=\"" + bc.getID("OrganisationMember") + "\" class=\"profileselection_inactive\" onclick=\"setActiveProfile(this.id)\">" + bc.getString("Title") + "</div>");
					}
				}
			}
			html.append("</div>");
			html.append("\n<div class=\"profilemenuitem\"><a class=\"profilemenuitem\" href=\"javascript:createProcess(\'ch.opencommunity.process.MemberRegistration\','parentid=" + om.getName() + "')\"><img src=\"images/home.png\">Weiteres Profil erstellen</a></div>");		
		}
		
		
		//html.append("\n<div class=\"profilemenuitem\"><a class=\"profilemenuitem\" href=\"javascript:loadProfileSection(\'home\')\"><img src=\"" + ocs.getBaseURL(request) + "/opencommunity/websites/1/images/picto/home.png;\">Home</a></div>");
		html.append("\n<div id=\"news\" class=\"profilemenuitem\"><a class=\"profilemenuitem\" href=\"javascript:loadProfileSection(\'news\')\"><img src=\"/res/icons/home.png\">News</a></div>");		
		html.append("\n<div id=\"merkliste\" class=\"profilemenuitem\"><a class=\"profilemenuitem\" href=\"javascript:loadProfileSection(\'merkliste\')\"><img src=\"/res/icons/merkliste.png\">Bestellliste</a></div>");		
		html.append("\n<div id=\"kontakte\" class=\"profilemenuitem\"><a class=\"profilemenuitem\" href=\"javascript:loadProfileSection(\'kontakte\')\"><img src=\"/res/icons/kontakte.png\">Meine Kontakte</a></div>");		
		html.append("\n<div id=\"inserieren\" class=\"profilemenuitem\"><a class=\"profilemenuitem\" href=\"javascript:createMemberAdd()\"><img src=\"/res/icons/inserieren.png\">Inserat erstellen</a></div>");		
		html.append("\n<div id=\"inserate\" class=\"profilemenuitem\"><a class=\"profilemenuitem\" href=\"javascript:loadProfileSection(\'inserate\')\"><img src=\"/res/icons/meine-inserate.png\">Meine Inserate</a></div>");	
	
		//if(url.endsWith("/profile")){
			html.append("\n<div class=\"profilemenuitem\"><a class=\"profilemenuitem\" href=\"" + ocs.getBaseURL(request) + "/pinnwand?mode=start\"><img src=\"/res/icons/inserat-suchen.png\">Inserate suchen</a></div>");	
		//}
		//else{
			//html.append("\n<div class=\"profilemenuitem\"><a class=\"profilemenuitem\" href=\"../pinnwand?mode=start\"><img src=\"" + ocs.getBaseURL(request) + "/opencommunity/websites/1/images/picto/inserat-suchen.png;\">Inserate suchen</a></div>");	
		//}
		//html.append("\n<div class=\"profilemenuitem\"><a class=\"profilemenuitem\" href=\"javascript:feedbackAd()\"><img src=\"" + ocs.getBaseURL(request) + "/opencommunity/websites/1/images/picto/inserat-suchen.png;\">Feedback</a></div>");

		
		//html.append("\n<div class=\"profilemenuitem\"><a class=\"profilemenuitem\" href=\"javascript:createProcess(\'ch.opencommunity.process.FeedbackCreate\',\'Type=2')\"><img src=\"/res/icons/feedback.png\">Feedback</a></div>");
		html.append("\n<div class=\"profilemenuitem\"><a class=\"profilemenuitem\" href=\"javascript:createProcess(\'ch.opencommunity.feedback.FeedbackAdd\',\'Type=2')\"><img src=\"/res/icons/feedback.png\">Feedback</a></div>");
		
		html.append("\n<div class=\"profilemenuitem\"><a class=\"profilemenuitem\" href=\"javascript:createProcess(\'ch.opencommunity.process.ProfileEdit\')\"><img src=\"/res/icons/einstellungen.png\">Einstellungen</a></div>");		
		html.append("\n<div class=\"profilemenuitem\"><a class=\"profilemenuitem\" href=\"javascript:logout()\"><img src=\"/res/icons/logout.png\">abmelden</a></div>");		
		html.append("\n<div id=\"profilefooter\" style=\"background :url(\'images/bg_rubriken_bottom.png'); height : 28px\">&nbsp;</div>");			
		html.append("\n</div>");	
		
		
	}
	public String getProfileSection(String sectionid, ApplicationContext context){
		
		StringBuilder html = new StringBuilder();
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		
		MemberAdAdministration maa = ocs.getMemberAdAdministration();
		
		WebApplicationContext webcontext= (WebApplicationContext)context;
		
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
		
		HttpServletRequest request = webcontext.getRequest();
		
		Map addpars = request.getParameterMap();
		
		OrganisationMember om = userSession.getOrganisationMember();
		OrganisationMember om2 = userSession.getOrganisationMember();
		
		String objectPath = request.getParameter("objectPath");
		
 	 	if(om != null && om.getActiveRelationship() != null){
 	 		om2 = om.getActiveRelationship();	 
 	 	}
 	 	if(objectPath != null){
 	 		
			om = (OrganisationMember)ocs.getObjectByPath(objectPath);

			if(om != null){
				String accesscode = context.getString("accesscode");
				ocs.logAccess(userSession);
				ocs.logAccess(accesscode);
				ocs.logAccess(objectPath);
				if(om.getString("AccessCode").equals(accesscode)){
					ocs.logAccess("found: " + om);
					om2 = om;
					userSession.setOrganisationMember(om);
				}
			}
 	 		
 	 	}

		if(om != null){
			
			String omid = om.getName();
			if(om.getActiveRelationship() != null){
				omid = 	om.getActiveRelationship().getName();
			}
			
			Vector criteria1 = new Vector();
			Vector criteria2 = new Vector();
			Vector criteria3 = new Vector();


			if(sectionid.equals("news")){
				
				getProfileMenu(html, ocs, om, request, addpars);
				
				if(context.hasProperty("welcome")){
					html.append("<div id=\"welcome\">");
					TextBlockAdministration tba = (TextBlockAdministration)ocs.getObjectByIndex("TextBlockAdministration", 0);
					if(tba != null){
						TextBlock tb = (TextBlock)tba.getObjectByName("TextBlock", "1");
						if(tb != null){
							html.append(tb.getString("Content"));	
						}
					}
					html.append("</div>");
				}
				
				html.append("<p class=\"sectionheader\">News");
			
				
				html.append("<p class=\"displaybody\">");
				
				for(BasicClass bc : ocs.getNewsAdministration().getObjects("NewsMessage")){
					if(bc.getID("Status")==0 && (bc.getID("Scope")==2 || bc.getID("Scope")==3) && (bc.getID("Type")==1 || bc.getID("Type")==2)){
						html.append("<div class=\"newsmessage\">");
						html.append("<b>" + DateConverter.sqlToShortDisplay(bc.getString("DateStart"), true) + " " + bc.getString("Title") + "</b>");
						html.append("<br>" + bc.getString("Description"));
						String url = bc.getString("URL");
						if(url.length() > 0){
							html.append("<br><a href=\"" + url + "\" target=\"_blank\">" + url + "</a>");
						}
						html.append("</div>");
					}
				}
			}	
			else if(sectionid.equals("home")){
				
				
				
				getProfileMenu(html, ocs, om, request, addpars);
				//html.append("<div id=\"userprofilecontent\">");	
				if(context.hasProperty("welcome")){
					html.append("<div id=\"welcome\">");
					TextBlockAdministration tba = (TextBlockAdministration)ocs.getObjectByIndex("TextBlockAdministration", 0);
					if(tba != null){
						TextBlock tb = (TextBlock)tba.getObjectByName("TextBlock", "1");
						if(tb != null){
							html.append(tb.getString("Content"));	
						}
					}
					html.append("</div>");
				}

				//html.append("<p class=\"sectionheader\"><img src=\"" + ocs.getBaseURL(request) + "/opencommunity/websites/1/images/picto/merken_aktiv.png;\">News");
				html.append("<p class=\"sectionheader\">News");
				
				//html.append("<p class=\"sectionheader\"><img src=\"" + ocs.getBaseURL(request) + "/opencommunity/websites/1/images/picto/merken_aktiv.png;\">Inserate, die bald auslaufen");
				html.append("<p class=\"sectionheader\">Inserate, die bald auslaufen");
				
				int status = 1;
				ObjectCollection results = new ObjectCollection("Results", "*");
				
				
				String sql = "SELECT t1.*, t6.Title AS Category, t4.DateOfBirth FROM MemberAd AS t1";
				
				sql += " LEFT JOIN OrganisationMember AS t2 ON t1.OrganisationMemberID=t2.ID";
				sql += " LEFT JOIN Person AS t3 ON t2.Person=t3.ID";
				sql += " LEFT JOIN Identity AS t4 ON t4.PersonID=t3.ID";
				sql += " LEFT JOIN Address AS t5 ON t5.PersonID=t3.ID";
					
				sql += " LEFT JOIN MemberAdCategory AS t6 ON t1.Template = t6.ID";
				
				
				sql += " WHERE t1.OrganisationMemberID=" + userSession.getOrganisationMemberID();
				sql += " AND t1.Status = " + status;
				sql += " AND extract(epoch from(t1.ValidUntil - now())) < (30 * 3600 * 24)";
				
				ocs.queryData(sql, results);
				ocs.logAccess(sql);
				
				for(BasicClass record : results.getObjects()){
					
					String validfrom = record.getString("ValidFrom");
					String validuntil = record.getString("ValidUntil");
					
					String dateofbirth = record.getString("DATEOFBIRTH");
					String[] args = dateofbirth.split("\\.");
					String syear = args[args.length-1];
					
					int age = 0;
					int currentyear = DateConverter.getCurrentYear();
					if(syear.length()==4){
						try{
							int year = Integer.parseInt(syear);
							age = currentyear-year;
						}
						catch(java.lang.Exception e){
							ocs.logException(e);
						}
					}
					
					String sex2 = "Mann";
					
					html.append("<div class=\"searchresult3\">");
					html.append("<table>");
					html.append("<tr><td class=\"searchresultcell2\">");
					
					html.append("<span class=\"searchresultfirstline\">" + record.getString("CATEGORY") + "</span>");
					html.append("<br>" + DateConverter.sqlToShortDisplay(record.getString("VALIDFROM"), false) + "-" + DateConverter.sqlToShortDisplay(record.getString("VALIDUNTIL"), false));
					html.append("<br>" + sex2 + " (" + age + ") " + record.getString("TITLE"));
					
					//html.append("</td><td><a class=\"objectaction\" href=\"javascript:createProcess('ch.opencommunity.process.MemberAdEdit','MemberAdID=" + record.getString("ID") + "')\">bearbeiten</a>");
					html.append("</td><td><a class=\"objectaction\" href=\"javascript:feedbackAd(" + record.getString("ID") + ")\">Feedback abgeben</a>");
					html.append("<br><a class=\"objectaction\" href=\"javascript:prolongAd(" + record.getString("ID") + ")\">verlängern</a>");
					//html.append("<br><a class=\"objectaction\" href=\"javascript:deactivateAd(" + record.getString("ID") + ")\">deaktivieren</a>");
					html.append("</tr>");
					html.append("</table>");
					html.append("</div>");
				}
				
				//html.append("<p class=\"sectionheader\"><img src=\"" + ocs.getBaseURL(request) + "/opencommunity/websites/1/images/picto/merken_aktiv.png;\">Feedback zu bestellten Adressen");
				html.append("<p class=\"sectionheader\">Feedback zu bestellten Adressen");
			
				results = new ObjectCollection("Results", "*");
				
				sql = "SELECT t1.*,  t2.Title AS MemberAdTitle, t3.Title AS Category, t6.FamilyName, t6.FirstName, t6.Sex, t6.DateOfBirth, t7.Street, t7.Number, t7.ZipCode, t7.City, t8.ID AS Feedback";
				sql += " FROM MemberAdRequest AS t1";
				sql += " LEFT JOIN MemberAd AS t2 ON t1.MemberAd=t2.ID";
				sql += " LEFT JOIN MemberAdCategory AS t3 ON t2.Template=t3.ID";
				sql += " LEFT JOIN OrganisationMember AS t4 ON t2.OrganisationMemberID=t4.ID";
				sql += " LEFT JOIN Person AS t5 ON t4.Person=t5.ID";
				sql += " LEFT JOIN Identity AS t6 ON t6.PersonID=t5.ID";
				sql += " LEFT JOIN Address AS t7 ON t7.PersonID=t5.ID";
				sql += " LEFT JOIN Feedback AS t8 ON t8.MemberAdRequestID=t1.ID";
				
				//sql += " WHERE t1.OrganisationMemberID=" + userSession.getOrganisationMemberID();
				sql += " WHERE t1.OrganisationMemberID=" + omid;
				
				sql += " AND t8.ID IS NULL";
				
				sql += " AND t1.Status IN (1,2)";
				
				sql += " AND extract(epoch from(t1.ValidUntil - now())) < (30 * 3600 * 24)";
				
				ocs.queryData(sql, results);
				
				for(BasicClass record : results.getObjects()){
					
					String sex = record.getString("SEX");
					String dateofbirth = record.getString("DATEOFBIRTH");
					
					String familyname = record.getString("FAMILYNAME");
					String firstname = record.getString("FIRSTNAME");
					
					String street = record.getString("STREET");
					String number = record.getString("NUMBER");
					String zipcode = record.getString("ZIPCODE");
					String city = record.getString("CITY");
					String category = record.getString("CATEGORY");
					String location = zipcode + " " + city;
					
					if(criteria1.indexOf(category)==-1){
						criteria1.add(category);	
					}
					if(criteria2.indexOf(location)==-1){
						criteria2.add(location);	
					}	
						
					String sex2 = "Mann";
					if(sex.equals("2")){
						sex2 = "Frau";	
					}
					

					int yearofbirth = 1900;
					if(dateofbirth.length() == 4){
						yearofbirth = Integer.parseInt(dateofbirth);
					}	
					int currentyear = DateConverter.getCurrentYear();
					int age = currentyear - yearofbirth;
						
					html.append("<div id=\"" + record.getString("ID") + "\" class=\"searchresult3\" onclick=\"getmemberadrequestdetail(" + record.getString("ID") + ")\">");
					html.append("<table>");
					html.append("<tr>");
					html.append("<td class=\"searchresultcell2\">");
					html.append(record.getString("CATEGORY"));
					html.append("<br>" + firstname + " " + familyname +", " + street + " " + number + ", " + zipcode + " " + city);
					html.append("<br>" + sex2 + " (" + age + ") " + record.getString("MEMBERADTITLE")); 
					html.append("<br>Sichtbar bis " + DateConverter.sqlToShortDisplay(record.getString("VALIDUNTIL"), false));
					html.append("</td><td>");

					html.append("<a class=\"objectaction\" href=\"javascript:createProcess(\'ch.opencommunity.process.FeedbackCreate\',\'MemberAdRequestID=" + record.getString("ID") + "\')\">Feedback abgeben</a>");
					
					html.append("</td>");
					html.append("<td>");
					html.append("</tr>");
					html.append("</table>");
					html.append("</div>");				
				}
				

				//html.append("</div>");
			}
			else if(sectionid.equals("merkliste")){
				
	
				
				//html.append("<div id=\"userprofilecontent\">");
				
				html.append("<p class=\"sectionheader\"><img src=\"images/merken_aktiv.png\">Bestell-Liste Kontakte</p>");
				
				
				
				/* Keine Sortierung
				html.append("<select class=\"selectsmall\" style=\"width : 140px; float : right;\" onchange=\"sortProfileSection(this.value)\">");
				html.append("<option value=\"\">Sortieren</option>");
				html.append("<option value=\"validfrom\">Nach Ablaufdatum</option>");
				html.append("<option value=\"category\">Nach Rubrik</option>");
				html.append("</select></p>");
				*/
				
				
				if(userSession.getMemberAdIDs().size() > 0){
					
					String url = request.getRequestURL().toString();
					
					html.append("<div style=\"height : 30px;\">");
					
					
					
					html.append("<input class=\"orderbutton\" type=\"button\" onclick=\"sendMemberAdRequests()\" value=\"Kontakte bestellen\" style=\"float : right;\">");
					
					html.append(getCategorySelection(maa));
					
					html.append("</div>");
					
					
					//html.append("<input class=\"nodebutton\" type=\"button\" onclick=\"location.href=\'" + ocs.getBaseURL(request) + "/pinnwand?mode=start\'\" value=\"Weitere Adressen suchen\" style=\"width : 190px; float : right; margin-right : 10px;\">&nbsp;");

					
					
					
				
					String adids = "(";
					for(Object adid : userSession.getMemberAdIDs()){
						adids += adid + ",";
					}
					
					adids = adids.substring(0, adids.length()-1) + ")";
					
					ObjectCollection results = new ObjectCollection("Results", "*");
					
					String sql = "SELECT t1.ID, t1.Title, t1.Description, t1.ValidFrom, t1.ValidUntil, t4.Sex, t4.DateOfBirth, t5.ZipCode, t5.City, t6.ID AS CatID, t6.Title AS Category FROM MemberAd AS t1";
					
					sql += " LEFT JOIN OrganisationMember AS t2 ON t1.OrganisationMemberID=t2.ID";
					sql += " LEFT JOIN Person AS t3 ON t2.Person=t3.ID";
					sql += " LEFT JOIN Identity AS t4 ON t4.PersonID=t3.ID";
					sql += " LEFT JOIN Address AS t5 ON t5.PersonID=t3.ID";
					
					sql += " LEFT JOIN MemberAdCategory AS t6 ON t1.Template = t6.ID";
					
					sql += " WHERE t1.ID IN " + adids;
					
					if(addpars.get("criteria") != null){
						String[] criteria = (String[])addpars.get("criteria");
						if(criteria[0].equals("validfrom")){
							sql += " ORDER BY t1.ValidFrom";
						}
						else if(criteria[0].equals("category")){
							sql += " ORDER BY t6.Title";
						}
						else{
							sql += " ORDER BY t5.ZipCode";	
						}
					}
					else{
						sql += " ORDER BY t6.Title, t5.ZipCode";
					}
					
					ocs.queryData(sql, results);
					
					String prevcategory = "";
					
					for(BasicClass record : results.getObjects()){
						
						boolean include = false;
						
						String sex = record.getString("SEX");
						String dateofbirth = record.getString("DATEOFBIRTH");
						
						String sex2 = "Mann";
						if(sex.equals("2")){
							sex2 = "Frau";	
						}
						
						int currentyear = DateConverter.getCurrentYear();
						int yearofbirth = 1968;
						int age = currentyear - yearofbirth;
						

						if(dateofbirth.length() > 3){
							String[] args = dateofbirth.split("\\.");
							String syear = args[args.length-1];
							int year = Integer.parseInt(syear);
							age = currentyear - year;
						}
						
						String zipcode = record.getString("ZIPCODE");
						String city = record.getString("CITY");
						String category = record.getString("CATEGORY");
						String location = zipcode + " " + city;
						
						if(addpars.get("reload")==null){
							include = true;
						}
						else if(addpars.get(category)!=null && addpars.get(location)!=null){
							include = true;						
						}
						
						if(criteria1.indexOf(category)==-1){
							criteria1.add(category);	
						}
						if(criteria2.indexOf(location)==-1){
							criteria2.add(location);	
						}	
						
						
						
						if(include){
							
							if(!category.equals(prevcategory)){
								
								html.append("\n<div class=\"searchresultheader\">");
								html.append("<img src=\"res/icons/" + record.getString("CATID") + "_weiss.png\">" + category);
								html.append("</div>");
								
							}
							
							html.append("<div id=\"" + record.getString("ID") + "\" class=\"searchresult\" style=\"height : auto;\">");
							html.append("<table>");
							//html.append("<tr><td class=\"searchresultcell1\">" + category + ", " + zipcode + " " + city + "</td><td rowspan=\"2\"><a class=\"objectaction\" href=\"javascript:deselectAd(" + record.getString("ID") + ")\"><img src=\"images/delete.png\">löschen</a></td></tr>");
							html.append("<tr><td class=\"searchresultcell1\">" + zipcode + " " + city + "</td><td rowspan=\"2\"><a class=\"objectaction\" href=\"javascript:deselectAd(null, " + record.getString("ID") + ", 1)\"><img src=\"images/delete.png\">löschen</a></td></tr>");
							html.append("<tr><td class=\"searchresultcell2\">" + sex2 + " (" + age + ") " + record.getString("TITLE"));
							html.append("<br>" + record.getString("DESCRIPTION"));
							//html.append("<br><br>Laufzeit Inserat vom " + DateConverter.sqlToShortDisplay(record.getString("VALIDFROM"),false) + " - " + DateConverter.sqlToShortDisplay(record.getString("VALIDUNTIL"),false) + "<br>&nbsp;");
							html.append("</td></tr>");
							html.append("</table>");
							html.append("</div>");
						}
						

						
						prevcategory = category;
					}
					
					html.append("<div>");
					html.append("<input class=\"orderbutton\" type=\"button\" onclick=\"sendMemberAdRequests()\" value=\"Kontakte bestellen\" style=\"float : right;\">");
						
					html.append(getCategorySelection(maa));
					
					html.append("</div>");
					
				}
				else{
					//html.append("<p>Keine Inserate in der Merkliste");
					
					html.append("<input class=\"nodebutton\" type=\"button\" onclick=\"location.href=\'" + ocs.getBaseURL(request) + "/pinnwand?mode=start\'\" value=\"Weitere Adressen suchen\" style=\"width : 190px; float : right; margin-right : 0px\">&nbsp;");
					
					html.append("<p>Bestellte Adressen, die auf Freischaltung warten:");
					
					ObjectCollection results = new ObjectCollection("Results", "*");
					
					String sql = "SELECT t1.*,  t2.Title AS MemberAdTitle, t3.Title AS Category, t6.FamilyName, t6.FirstName, t6.Sex, t6.DateOfBirth, t7.Street, t7.Number, t7.ZipCode, t7.City";
					sql += " FROM MemberAdRequest AS t1";
					sql += " LEFT JOIN MemberAd AS t2 ON t1.MemberAd=t2.ID";
					sql += " LEFT JOIN MemberAdCategory AS t3 ON t2.Template=t3.ID";
					sql += " LEFT JOIN OrganisationMember AS t4 ON t2.OrganisationMemberID=t4.ID";
					sql += " LEFT JOIN Person AS t5 ON t4.Person=t5.ID";
					sql += " LEFT JOIN Identity AS t6 ON t6.PersonID=t5.ID";
					sql += " LEFT JOIN Address AS t7 ON t7.PersonID=t5.ID";
					
					sql += " WHERE t1.OrganisationMemberID=" + omid;
					
					sql += " AND t1.Status IN (0)";
					
					if(addpars.get("criteria") != null){
						String[] criteria = (String[])addpars.get("criteria");
						if(criteria[0].equals("validfrom")){
							sql += " ORDER BY t1.ValidFrom";
						}
						else if(criteria[0].equals("category")){
							sql += " ORDER BY t3.Title";
						}
					}
	
					ocs.queryData(sql, results);
					
					
					
					for(BasicClass record : results.getObjects()){
						
						boolean include = false;
						
						String sex = record.getString("SEX");
						String dateofbirth = record.getString("DATEOFBIRTH");
						
						String familyname = record.getString("FAMILYNAME");
						String firstname = record.getString("FIRSTNAME");
						
						String street = record.getString("STREET");
						String number = record.getString("NUMBER");
						String zipcode = record.getString("ZIPCODE");
						String city = record.getString("CITY");
						String category = record.getString("CATEGORY");
						String location = zipcode + " " + city;
						
						if(addpars.get("reload")==null){
							include = true;
						}
						else if(addpars.get(category)!=null && addpars.get(location)!=null){
							include = true;						
						}
						
						
						if(criteria1.indexOf(category)==-1){
							criteria1.add(category);	
						}
						if(criteria2.indexOf(location)==-1){
							criteria2.add(location);	
						}	
							
						String sex2 = "Mann";
						if(sex.equals("2")){
							sex2 = "Frau";	
						}
						//String dateofbirth = record.getString("DATEOFBIRTH");
						int yearofbirth = 1900;
						if(dateofbirth.length() == 4){
							yearofbirth = Integer.parseInt(dateofbirth);
						}	
						int currentyear = DateConverter.getCurrentYear();
						int age = currentyear - yearofbirth;
						
						if(include){
							html.append("<div id=\"" + record.getString("ID") + "\" class=\"searchresult3\" onclick=\"getmemberadrequestdetail(" + record.getString("ID") + ")\">");
							html.append("<table>");
							html.append("<tr>");
							html.append("<td class=\"searchresultcell2\">");
							html.append(record.getString("CATEGORY"));
							//html.append("<br>" + firstname + " " + familyname +", " + street + " " + number + ", " + zipcode + " " + city);
							html.append("<br>" + sex2 + " (" + age + ") " + record.getString("MEMBERADTITLE")); 
							html.append("<br>Sichtbar bis " + DateConverter.sqlToShortDisplay(record.getString("VALIDUNTIL"), false));
							html.append("</td><td>");
							
							/* Auf Wunsch des Kunden ausgeblendet
							if(record.getInt("STATUS")==1){
								html.append("<a class=\"objectaction\" href=\"javascript:activateContact(" + record.getString("ID") + ")\">auf \"kontaktiert\" setzen</a><br>");
							}
							else if(record.getInt("STATUS")==2){
								html.append("<span style=\"color : green\">kontaktiert</span><br>");
							}
							*/
							//html.append("<a class=\"objectaction\" href=\"javascript:createProcess(\'ch.opencommunity.process.FeedbackCreate\',\'MemberAdRequestID=" + record.getString("ID") + "&Mode=2&DeleteContact=true\')\">löschen</a>");
							html.append("</td>");
							html.append("<td>");
							html.append("</tr>");
							html.append("</table>");
							html.append("</div>");	
						}
					}
					
				}
				//html.append("</div>");		
				getProfileMenu(html, ocs, om, request, addpars, criteria1, criteria2);
			}    
			else if(sectionid.equals("kontakte")){
				
				
				html.append(AddressList.getAddressList(ocs, om, addpars, context));
				
				getProfileMenu(html, ocs, om, request, addpars, criteria1, criteria2);
				
				
			}
			else if(sectionid.equals("inserate")){
				
				getProfileMenu(html, ocs, om, request, addpars);
				
				//html.append("<div id=\"userprofilecontent\">");
				
				int status = 1;
	
				ObjectCollection results = new ObjectCollection("Results", "*");
				
				
				
				//---------------------------------aktive Inserate--------------------------------------------
				
				String sql = "SELECT t1.*, t6.Title AS Category, t4.DateOfBirth, t4.Sex FROM MemberAd AS t1";
				
				sql += " LEFT JOIN OrganisationMember AS t2 ON t1.OrganisationMemberID=t2.ID";
				sql += " LEFT JOIN Person AS t3 ON t2.Person=t3.ID";
				sql += " LEFT JOIN Identity AS t4 ON t4.PersonID=t3.ID";
				sql += " LEFT JOIN Address AS t5 ON t5.PersonID=t3.ID";
					
				sql += " LEFT JOIN MemberAdCategory AS t6 ON t1.Template = t6.ID";
				
				sql += " WHERE t1.OrganisationMemberID=" + omid;
				sql += " AND t1.Status = " + status;
				
				if(addpars.get("criteria") != null){
					String[] criteria = (String[])addpars.get("criteria");
					if(criteria[0].equals("validfrom")){
						sql += " ORDER BY t1.ValidFrom";
					}
					else if(criteria[0].equals("category")){
						sql += " ORDER BY t6.Title";
					}
				}
				
				ocs.queryData(sql, results);
				

				//html.append("<p class=\"sectionheader\"><img src=\"images/merken_aktiv.png\">Bestell-Liste Kontakte</p>");

				
				html.append("\n<div class=\"searchresultheader\" style=\"height : 32px; margin-top : 22px;\"><img src=\"res/icons/meine-inserate_weiss.png\">meine aktiven Inserate</div>");
				
				/*
				html.append("<select class=\"selectsmall\" style=\"width : 140px; float : right;\" onchange=\"sortProfileSection(this.value)\">");
				html.append("<option value=\"\">Sortieren</option>");
				html.append("<option value=\"validfrom\">Nach Ablaufdatum</option>");
				html.append("<option value=\"category\">Nach Rubrik</option>");
				html.append("</select></p>");
				*/
				
				int cnt = 0;
				
				for(BasicClass record : results.getObjects()){
					
					String validfrom = record.getString("ValidFrom");
					String validuntil = record.getString("ValidUntil");
					
					String dateofbirth = record.getString("DATEOFBIRTH");
					String[] args = dateofbirth.split("\\.");
					String syear = args[args.length-1];
					
					int age = 0;
					int currentyear = DateConverter.getCurrentYear();
					if(syear.length()==4){
						try{
							int year = Integer.parseInt(syear);
							age = currentyear-year;
						}
						catch(java.lang.Exception e){
							ocs.logException(e);
						}
					}
					
					String sex = record.getString("SEX");
					
					String sex2 = "Mann";
					
					if(sex.equals("2")){
						sex2 = "Frau";	
					}
					
					if(cnt > 0){
						html.append("<div style=\"border-top : 1px solid black; margin-bottom : 14px; margin-top : 10px;\"></div>");	
					}
					
					html.append("<table style=\"width : 536px; margin-left : 4px; margin-top : 20px;\">");
					
					html.append("<tr><td style=\"font-size : 20px;\">" + record.getString("CATEGORY") + "</td><td></td></tr>");
					html.append("<tr><td style=\"color : #6CBB16;\" colspan=\"2\">" + sex2 + " (" + age + ") " + record.getString("TITLE") + "</td></tr>");
					html.append("<tr><td style=\"color : white;\" colspan=\"2\">" + record.getString("DESCRIPTION") + "</td></tr>");
					html.append("<tr><td>&nbsp;</td></tr>");
					html.append("<tr>");
					
					html.append("<td style=\"width : 400px;\">aufgeschaltet am: " + DateConverter.sqlToShortDisplay(record.getString("VALIDFROM"), false));
					html.append("<br>gültig bis: " + DateConverter.sqlToShortDisplay(record.getString("VALIDUNTIL"), false) + "</td>");
					
					html.append("<td>");
					
					html.append("<a class=\"objectaction\" href=\"javascript:createProcess('ch.opencommunity.process.MemberAdEdit','MemberAdID=" + record.getString("ID") + "')\"><img src=\"images/bearbeiten.png\" style=\"margin-right : 8px;\">bearbeiten</a>");
					html.append("<br><a class=\"objectaction\" href=\"javascript:pauseAd(" + record.getString("ID") + ")\"><img src=\"images/pausieren.png\" style=\"margin-right : 8px;\">pausieren</a>");
					html.append("<br><img src=\"images/delete.png\" style=\"margin-left : -2px;\"><a class=\"objectaction\" href=\"javascript:deactivateAd(" + record.getString("ID") + "," + om.getName() + ")\">löschen</a>");
									
					html.append("</td>");
					
					html.append("</tr>");
					
					
					
					html.append("</table>");
					
					cnt++;
					
					/*
					html.append("<div class=\"searchresult3\">");
					html.append("<table>");
					html.append("<tr><td class=\"searchresultcell2\">");					
					html.append("<span class=\"searchresultfirstline\">" + record.getString("CATEGORY") + "</span>");
					
					html.append("<br>" + DateConverter.sqlToShortDisplay(record.getString("VALIDFROM"), false) + "-" + DateConverter.sqlToShortDisplay(record.getString("VALIDUNTIL"), false));
					html.append("<br>" + sex2 + " (" + age + ") " + record.getString("TITLE"));
					html.append("<p>" + record.getString("DESCRIPTION"));
					
					html.append("</td><td>");
					html.append("<a class=\"objectaction\" href=\"javascript:createProcess('ch.opencommunity.process.MemberAdEdit','MemberAdID=" + record.getString("ID") + "')\"><img src=\"images/bearbeiten.png\">bearbeiten</a>");
					html.append("<br><a class=\"objectaction\" href=\"javascript:pauseAd(" + record.getString("ID") + ")\"><img src=\"images/pausieren.png\">pausieren</a>");
					html.append("<br><img src=\"images/delete.png\"><a class=\"objectaction\" href=\"javascript:deactivateAd(" + record.getString("ID") + "," + om.getName() + ")\">löschen</a>");
					html.append("</tr>");
					html.append("</table>");
					html.append("</div>");
					
					*/
				}	
				
				
				//------------------------------------------------------------Inserate, die bald auslaufen
				
				results = new ObjectCollection("Results", "*");
				
				sql = "SELECT t1.*, t6.Title AS Category, t4.DateOfBirth, t4.Sex FROM MemberAd AS t1";
				
				sql += " LEFT JOIN OrganisationMember AS t2 ON t1.OrganisationMemberID=t2.ID";
				sql += " LEFT JOIN Person AS t3 ON t2.Person=t3.ID";
				sql += " LEFT JOIN Identity AS t4 ON t4.PersonID=t3.ID";
				sql += " LEFT JOIN Address AS t5 ON t5.PersonID=t3.ID";
					
				sql += " LEFT JOIN MemberAdCategory AS t6 ON t1.Template = t6.ID";
				
				
				sql += " WHERE t1.OrganisationMemberID=" + omid;
				sql += " AND t1.Status = " + status;
				sql += " AND extract(epoch from(t1.ValidUntil - now())) < (30 * 3600 * 24)";
				
				//sql += " AND (extract(epoch from(t1.ValidUntil - now())) < (30 * 3600 * 24) OR t1.ValidUntil < now())";
				
				ocs.queryData(sql, results);
				

				
				html.append("\n<div class=\"searchresultheader\" style=\"height : 32px;\"><img src=\"res/icons/verlaengern_weiss.png\">Inserate, die bald auslaufen</div>");
				
				cnt = 0;
				
				for(BasicClass record : results.getObjects()){
					
					String validfrom = record.getString("ValidFrom");
					String validuntil = record.getString("ValidUntil");
					
					String dateofbirth = record.getString("DATEOFBIRTH");
					String[] args = dateofbirth.split("\\.");
					String syear = args[args.length-1];
					
					int age = 0;
					int currentyear = DateConverter.getCurrentYear();
					if(syear.length()==4){
						try{
							int year = Integer.parseInt(syear);
							age = currentyear-year;
						}
						catch(java.lang.Exception e){
							ocs.logException(e);
						}
					}
					
					String sex = record.getString("SEX");
					
					String sex2 = "Mann";
					
					if(sex.equals("2")){
						sex2 = "Frau";	
					}
					
					if(cnt > 0){
						html.append("<div style=\"border-top : 1px solid black; margin-bottom : 14px; margin-top : 10px;\"></div>");	
					}
					
					html.append("<table style=\"width : 536px; margin-left : 4px; margin-top : 20px;\">");
					
					html.append("<tr><td style=\"font-size : 20px;\">" + record.getString("CATEGORY") + "</td><td></td></tr>");
					html.append("<tr><td style=\"color : #6CBB16;\" colspan=\"2\">" + sex2 + " (" + age + ") " + record.getString("TITLE") + "</td></tr>");
					html.append("<tr><td style=\"color : white;\" colspan=\"2\">" + record.getString("DESCRIPTION") + "</td></tr>");
					html.append("<tr><td>&nbsp;</td></tr>");
					html.append("<tr>");
					
					html.append("<td style=\"width : 400px;\">aufgeschaltet am: " + DateConverter.sqlToShortDisplay(record.getString("VALIDFROM"), false));
					html.append("<br>gültig bis: " + DateConverter.sqlToShortDisplay(record.getString("VALIDUNTIL"), false) + "</td>");
					
					html.append("<td>");
					
					
					html.append("<a class=\"objectaction\" href=\"javascript:createProcess('ch.opencommunity.process.MemberAdEdit','MemberAdID=" + record.getString("ID") + "&Mode=prolong')\"><img src=\"res/icons/verlaengern_gelb.png\" style=\"margin-right : 5px;\">verlängern</a>");
					html.append("<br><img src=\"images/delete.png\" style=\"margin-left : -2px;\"><a class=\"objectaction\" href=\"javascript:deactivateAd(" + record.getString("ID") + "," + om.getName() + ")\">löschen</a>");
									
					html.append("</td>");
					
					html.append("</tr>");
					
					
					
					html.append("</table>");
					
					cnt++;

					/*
					html.append("<div class=\"searchresult3\">");
					html.append("<table>");
					html.append("<tr><td class=\"searchresultcell2\">");
					
					html.append("<span class=\"searchresultfirstline\">" + record.getString("CATEGORY") + "</span>");
					html.append("<br>" + DateConverter.sqlToShortDisplay(record.getString("VALIDFROM"), false) + "-" + DateConverter.sqlToShortDisplay(record.getString("VALIDUNTIL"), false)); //AK 20160822
					//html.append("<br>zur Zeit unterbrochen");
					html.append("<br>" + sex2 + " (" + age + ") " + record.getString("TITLE"));
					html.append("<p>" + record.getString("DESCRIPTION"));
					
					html.append("</td><td><a class=\"objectaction\" href=\"javascript:createProcess('ch.opencommunity.process.MemberAdEdit','MemberAdID=" + record.getString("ID") + "&Mode=prolong')\">verlängern</a>");
					html.append("<br><img src=\"images/delete.png\"><a class=\"objectaction\" href=\"javascript:deactivateAd(" + record.getString("ID") + "," + om.getName() + ")\">löschen</a>");
					html.append("</tr>");
					html.append("</table>");
					html.append("</div>");
					*/
				}
				
				
				//------------------------------------------------------------pausiere Inserate------------------------------------------------------
				
				status = 2;
	
				results = new ObjectCollection("Results", "*");
				sql = "SELECT t1.*, t6.Title AS Category, t4.DateOfBirth, t4.Sex FROM MemberAd AS t1";
				
				sql += " LEFT JOIN OrganisationMember AS t2 ON t1.OrganisationMemberID=t2.ID";
				sql += " LEFT JOIN Person AS t3 ON t2.Person=t3.ID";
				sql += " LEFT JOIN Identity AS t4 ON t4.PersonID=t3.ID";
				sql += " LEFT JOIN Address AS t5 ON t5.PersonID=t3.ID";
					
				sql += " LEFT JOIN MemberAdCategory AS t6 ON t1.Template = t6.ID";
				
				sql += " WHERE t1.OrganisationMemberID=" + omid;
				sql += " AND t1.Status = " + status;
				
				if(addpars.get("criteria") != null){
					String[] criteria = (String[])addpars.get("criteria");
					if(criteria[0].equals("validfrom")){
						sql += " ORDER BY t1.ValidFrom";
					}
					else if(criteria[0].equals("category")){
						sql += " ORDER BY t6.Title";
					}
				}
				
				ocs.queryData(sql, results);
				
				html.append("\n<div class=\"searchresultheader\" style=\"height : 32px;\"><img src=\"res/icons/unterbrechen_weiss.png\">Inserate, die Pause machen</div>");
				
				cnt = 0;
				
				for(BasicClass record : results.getObjects()){
					
					String validfrom = record.getString("ValidFrom");
					String validuntil = record.getString("ValidUntil");
					
					String dateofbirth = record.getString("DATEOFBIRTH");
					String[] args = dateofbirth.split("\\.");
					String syear = args[args.length-1];
					
					int age = 0;
					int currentyear = DateConverter.getCurrentYear();
					if(syear.length()==4){
						try{
							int year = Integer.parseInt(syear);
							age = currentyear-year;
						}
						catch(java.lang.Exception e){
							ocs.logException(e);
						}
					}
					
					String sex = record.getString("SEX");
					
					String sex2 = "Mann";
					
					if(sex.equals("2")){
						sex2 = "Frau";	
					}
					
					if(cnt > 0){
						html.append("<div style=\"border-top : 1px solid black; margin-bottom : 14px; margin-top : 10px;\"></div>");	
					}
					
					html.append("<table style=\"width : 536px; margin-left : 4px; margin-top : 20px;\">");
					
					html.append("<tr><td style=\"font-size : 20px;\">" + record.getString("CATEGORY") + "</td><td></td></tr>");
					html.append("<tr><td style=\"color : #6CBB16;\" colspan=\"2\">" + sex2 + " (" + age + ") " + record.getString("TITLE") + "</td></tr>");
					html.append("<tr><td style=\"color : white;\" colspan=\"2\">" + record.getString("DESCRIPTION") + "</td></tr>");
					html.append("<tr><td>&nbsp;</td></tr>");
					html.append("<tr>");
					
					html.append("<td style=\"width : 400px;\">aufgeschaltet am: " + DateConverter.sqlToShortDisplay(record.getString("VALIDFROM"), false));
					html.append("<br>gültig bis: " + DateConverter.sqlToShortDisplay(record.getString("VALIDUNTIL"), false) + "</td>");
					
					html.append("<td>");
					
					
					html.append("<a class=\"objectaction\" href=\"javascript:reactivateAd(" + record.getString("ID") + ")\"><img src=\"res/icons/verlaengern_gelb.png\" style=\"margin-right : 5px;\">aktivieren</a>");
					html.append("<br><img src=\"images/delete.png\" style=\"margin-left : -4px;\"><a class=\"objectaction\" href=\"javascript:deactivateAd(" + record.getString("ID") + "," + om.getName() + ")\">löschen</a>");
									
					html.append("</td>");
					
					html.append("</tr>");
					
					html.append("</table>");
					
					cnt++;
					
					/*
					
					html.append("<div class=\"searchresult3\">");
					html.append("<table>");
					html.append("<tr><td class=\"searchresultcell2\">");
					
					html.append("<span class=\"searchresultfirstline\">" + record.getString("CATEGORY") + "</span>");
					html.append("<br>zur Zeit unterbrochen");
					html.append("<br>" + sex2 + " (" + age + ") " + record.getString("TITLE"));
					html.append("<p>" + record.getString("DESCRIPTION"));
					
					html.append("</td><td><a class=\"objectaction\" href=\"javascript:reactivateAd(" + record.getString("ID") + ")\">aktivieren</a>");
					html.append("<br><img src=\"images/delete.png\"><a class=\"objectaction\" href=\"javascript:deactivateAd(" + record.getString("ID") + "," + om.getName() + ")\">löschen</a>");
					html.append("</tr>");
					html.append("</table>");
					html.append("</div>");
					
					*/
				}
				
				//----------------------------------------------------------gelösche Inserate----------------------------------------------------------------------
				
				
				//------------------------------daektiviert auf Wunsch des Kunden--------------------------------------
				
				/*
				status = 3;
	
				results = new ObjectCollection("Results", "*");
				sql = "SELECT t1.*, t6.Title AS Category, t4.DateOfBirth FROM MemberAd AS t1";
				
				sql += " LEFT JOIN OrganisationMember AS t2 ON t1.OrganisationMemberID=t2.ID";
				sql += " LEFT JOIN Person AS t3 ON t2.Person=t3.ID";
				sql += " LEFT JOIN Identity AS t4 ON t4.PersonID=t3.ID";
				sql += " LEFT JOIN Address AS t5 ON t5.PersonID=t3.ID";
					
				sql += " LEFT JOIN MemberAdCategory AS t6 ON t1.Template = t6.ID";
				
				sql += " WHERE t1.OrganisationMemberID=" + omid;
				sql += " AND t1.Status = " + status;
				
				if(addpars.get("criteria") != null){
					String[] criteria = (String[])addpars.get("criteria");
					if(criteria[0].equals("validfrom")){
						sql += " ORDER BY t1.ValidFrom";
					}
					else if(criteria[0].equals("category")){
						sql += " ORDER BY t6.Title";
					}
				}
				
				ocs.queryData(sql, results);
				
				html.append("<p class=\"sectionheader\"><img src=\"" + ocs.getBaseURL(request) + "/opencommunity/websites/1/images/picto/meine-inserate_aktiv.png\">meine archivierten Inserate</p>");
				
				for(BasicClass record : results.getObjects()){
					
					String validfrom = record.getString("ValidFrom");
					String validuntil = record.getString("ValidUntil");
					
					String sex2 = "Mann";
					String age = "45";
					
					html.append("<div class=\"searchresult3\">");
					html.append("<table>");
					html.append("<tr><td class=\"searchresultcell2\">");
					
					html.append("<span class=\"searchresultfirstline\">" + record.getString("CATEGORY") + "</span>");
					html.append("<br>" + DateConverter.sqlToShortDisplay(record.getString("VALIDFROM"), false) + "-" + DateConverter.sqlToShortDisplay(record.getString("VALIDUNTIL"), false));
					html.append("<br>" + sex2 + " (" + age + ") " + record.getString("TITLE"));
					
					html.append("</td><td><a class=\"objectaction\" href=\"javascript:copyAd(" + record.getString("ID") + ")\">neues Inserat aufschalten</a></td>");
					html.append("</tr>");
					html.append("</table>");
					html.append("</div>");
				}
				
				*/
				
				//html.append("</div>");		
				
				//getProfileMenu(html, ocs, om, request, addpars);
				
			}

		
		}
		else{
			if(context.hasProperty("requestactivated")){	
				
				html.append("Vielen Dank für das Vertrauen. Die Person, die Ihre Adresse bestellt hat, kann jetzt mit Ihnen Kontakt aufnehmen."); 
				
			}
			else if(context.hasProperty("requestdenied")){	
				
				html.append("Danke. Ihre Adresse wurde für die Person, die Ihre Adresse bestellt hat, gesperrt."); 
			}
			//else if(context.hasProperty("registrationconfirmed")){	
			else if(userSession.get("registrationconfirmed") != null && userSession.get("registrationconfirmed").equals("true")){	
				
				html.append("Vielen Dank für die Bestätigung Ihrer E-Mail-Adresse!");
				html.append("<p>Es dauert von Sonntag bis Mittwoch maximal 24 Stunden, bis Ihre Registrierung aktiviert ist und Sie Ihre Zugangsdaten per E-Mail erhalten.");
				html.append("Registrierungen ab Donnerstagnachmittag werden erst am Montag bearbeitet. Wir bitten um Geduld."); 
				html.append("Wenn Sie sich aber jetzt schon in Ihrem Benutzerprofil umsehen, Inserate erstellen oder Adressen zu den Inseraten bestellen wollen,");
				html.append("dann klicken Sie auf den unteren Ein-Mal-Link.");

				html.append("<p><a href=\"" + ocs.getString("hostname") + "/servlet?action=firstlogin\">Ein-Mail-Login</a>");
			
				
				//html.append("Danke für die Bestätigiung Ihrer Email."); 
			}
		}
		
		return html.toString();
	}
    public String getContactList(OpenCommunityServer ocs, String omid, boolean embed, Map addpars, Vector criteria1, Vector criteria2){
    	return getContactList(ocs, omid, embed, addpars, criteria1, criteria2, "Meine Kontakte");
    }
    public String getContactList(OpenCommunityServer ocs, String omid, boolean embed, Map addpars, Vector criteria1, Vector criteria2, String title){
    	
    	StringBuilder html = new StringBuilder();
    	
    	if(!embed){
    		html.append("<b>" + title + "</b>\n");	
    	}

				ObjectCollection results = new ObjectCollection("Results", "*");
				
				String sql = "SELECT t1.*,  t2.Title AS MemberAdTitle, t2.Description, t3.Title AS Category, t6.FamilyName, t6.FirstName, t6.Sex, t6.DateOfBirth, t7.Street, t7.Number, t7.ZipCode, t7.City, t8.Value AS Phonehome, t9.Value AS Email, t10.Value AS Mobile,";
				sql += " t14.FamilyName AS FamilyName2, t14.FirstName AS FirstName2, t14.Sex AS Sex2, t14.DateOfBirth AS DateOfBirth2, t15.Street AS Street2, t15.Number AS Number2, t15.ZipCode AS ZipCode2, t15.City AS City2, t16.Value AS Phonehome2, t17.Value AS Email2, t18.Value AS Mobile2";
				sql += " FROM MemberAdRequest AS t1";
				sql += " LEFT JOIN MemberAd AS t2 ON t1.MemberAd=t2.ID";
				sql += " LEFT JOIN MemberAdCategory AS t3 ON t2.Template=t3.ID";
				sql += " LEFT JOIN OrganisationMember AS t4 ON t2.OrganisationMemberID=t4.ID";
				sql += " LEFT JOIN Person AS t5 ON t4.Person=t5.ID";
				sql += " LEFT JOIN Identity AS t6 ON t6.PersonID=t5.ID";
				sql += " LEFT JOIN Address AS t7 ON t7.PersonID=t5.ID";
				sql += " LEFT JOIN Contact AS t8 ON t8.PersonID=t5.ID AND t8.Type=0";
				sql += " LEFT JOIN Contact AS t9 ON t9.PersonID=t5.ID AND t9.Type=3";
				sql += " LEFT JOIN Contact AS t10 ON t10.PersonID=t5.ID AND t10.Type=2";
				
				sql += " LEFT Join OrganisationMemberRelationship AS t11 ON t11.OrganisationMember=t4.ID";
				sql += " LEFT JOIN OrganisationMember AS t12 ON t11.OrganisationMemberID=t12.ID";
				sql += " LEFT Join Person AS t13 ON t12.Person=t13.ID";
				sql += " LEFT JOIN Identity AS t14 ON t14.PersonID=t13.ID";
				sql += " LEFT JOIN Address AS t15 ON t15.PersonID=t13.ID";
				sql += " LEFT JOIN Contact AS t16 ON t16.PersonID=t13.ID AND t16.Type=0";
				sql += " LEFT JOIN Contact AS t17 ON t17.PersonID=t13.ID AND t17.Type=3";
				sql += " LEFT JOIN Contact AS t18 ON t18.PersonID=t13.ID AND t18.Type=2";
				
				sql += " WHERE t1.OrganisationMemberID=" + omid;
				
				sql += " AND t1.Status IN (1,2)";
				
				sql += " AND t1.ValidUntil >= Now()"; //AK 20161115
				
				sql += " AND t4.Status=1"; //AK 20161205
				
				if(addpars != null && addpars.get("criteria") != null){
					String[] criteria = (String[])addpars.get("criteria");
					if(criteria[0].equals("validfrom")){
						sql += " ORDER BY t1.ValidFrom";
					}
					else if(criteria[0].equals("category")){
						sql += " ORDER BY t3.Title";
					}
				}
				else{
					sql += " ORDER BY t3.Title, t7.Zipcode";
				}

				ocs.queryData(sql, results);
				
				String prevcategory = "";
				String prevzipcode = "";
				
				for(BasicClass record : results.getObjects()){
					
					boolean include = false;
					
					String sex = record.getString("SEX");
					String dateofbirth = record.getString("DATEOFBIRTH");
					
					String familyname = record.getString("FAMILYNAME");
					String firstname = record.getString("FIRSTNAME");
					
					String street = record.getString("STREET");
					String number = record.getString("NUMBER");
					String zipcode = record.getString("ZIPCODE");
					String city = record.getString("CITY");
					String category = record.getString("CATEGORY");
					String location = zipcode + " " + city;
					
					String phonehome = record.getString("PHONEHOME");
					String mobile = record.getString("MOBILE");
					String email = record.getString("EMAIL");
					
					String familyname2 = record.getString("FAMILYNAME2");
					String firstname2 = record.getString("FIRSTNAME2");
					
					String street2 = record.getString("STREET2");
					String number2 = record.getString("NUMBER2");
					String zipcode2 = record.getString("ZIPCODE2");
					String city2 = record.getString("CITY2");
					
					String phonehome2 = record.getString("PHONEHOME2");
					String mobile2 = record.getString("MOBILE2");
					String email2 = record.getString("EMAIL2");
					
					if(addpars==null){
						include = true;
					}
					else if(addpars.get("reload")==null){
						include = true;
					}
					else if(addpars.get(category)!=null && addpars.get(location)!=null){
						include = true;						
					}
					
					
					if(criteria1.indexOf(category)==-1){
						criteria1.add(category);	
					}
					if(criteria2.indexOf(location)==-1){
						criteria2.add(location);	
					}	
						
					String sex2 = "Mann";
					if(sex.equals("2")){
						sex2 = "Frau";	
					}
					//String dateofbirth = record.getString("DATEOFBIRTH");
					int yearofbirth = 1900;
					if(dateofbirth.length() == 4){
						yearofbirth = Integer.parseInt(dateofbirth);
					}		
					int currentyear = DateConverter.getCurrentYear();
					int age = currentyear - yearofbirth;
					
					if(include){
						
						boolean managedaccount = false;
						if(familyname2 != null && familyname2.length() > 0){
							managedaccount = true;
						}
						
						if(!prevcategory.equals(category)){
							
							if(embed){
							
								html.append("\n<div class=\"searchresultheader\">");
								//html2.append(zipcode + " " + record.getString("CITY"));
								html.append("\n" + category);
								
								html.append("</div>");	
							
							}
							else{
								html.append("\n<b>" + category + "</b>");
							}
						}
						if(embed){
							html.append("<div id=\"" + record.getString("ID") + "\" class=\"searchresult3\" onclick=\"getmemberadrequestdetail(" + record.getString("ID") + ")\">");
							html.append("<table>");
							html.append("<tr>");
							html.append("<td class=\"searchresultcell2\">");
						}
						
						if(embed){
							html.append("\n" + record.getString("CATEGORY"));
							html.append("\n<br><span class=\"green\">");
						}
						html.append("\n" + firstname + " " + familyname);
						if(embed){
							html.append("</span>");
						}
						if(!managedaccount){
							html.append(", " + street + " " + number + ", " + zipcode + " " + city);
						}
						html.append("<br>" + sex2 + " (" + age + ") " + record.getString("MEMBERADTITLE")); 
						html.append("<br>" + record.getString("DESCRIPTION"));
						
						if(!managedaccount){
							if(phonehome.length() > 0 || mobile.length() > 0){
								
								if(embed){
									html.append("<br>");
								}
								if(phonehome.length() > 0){
									html.append("\nTelephon(P) : " + phonehome + " ");
								}
								if(mobile.length() > 0){
									html.append("\nTelephon(M) : " + mobile + " ");
								}
							}
							if(email.length() > 0){
								html.append("\n<br>Email : " + email);
							}
						}
						if(embed){
							html.append("<br>Sichtbar bis " + DateConverter.sqlToShortDisplay(record.getString("VALIDUNTIL"), false));
						}
						
						
						if(familyname2 != null && familyname2.length() > 0){ //Betreutes Profil
							html.append("<p>Für die Kontaktaufnahme wenden Sie sich bitte an:");
							html.append("\n<br><b>" + firstname2 + " " + familyname2 + "</b>");
							html.append("\n<br>" + street2 + " " + number2 + "," + zipcode2 + " " + city2);
							
							if(phonehome2.length() > 0 || mobile2.length() > 0){
								html.append("<br>");
								if(phonehome2.length() > 0){
									html.append("Telephon(P) : " + phonehome2 + " ");
								}
								if(mobile2.length() > 0){
									html.append("Telephon(M) : " + mobile2 + " ");
								}
							}
							if(email2.length() > 0){
								html.append("<br>Email : " + email2);
							}
					}
					if(!embed){
						html.append("<br><br>");
					}
					prevcategory = category;
					prevzipcode = zipcode;
			}

		}
    	return html.toString();
    	
    }
	public String getMemberAdList(OpenCommunityServer ocs, String omid, boolean embed, Map addpars, String title, int status){
		StringBuilder html = new StringBuilder();
		
		ObjectCollection results = new ObjectCollection("Results", "*");
		
		String sql = "SELECT t1.*, t6.Title AS Category, t4.DateOfBirth, t4.Sex FROM MemberAd AS t1";
				
		sql += " LEFT JOIN OrganisationMember AS t2 ON t1.OrganisationMemberID=t2.ID";
		sql += " LEFT JOIN Person AS t3 ON t2.Person=t3.ID";
		sql += " LEFT JOIN Identity AS t4 ON t4.PersonID=t3.ID";
		sql += " LEFT JOIN Address AS t5 ON t5.PersonID=t3.ID";
					
		sql += " LEFT JOIN MemberAdCategory AS t6 ON t1.Template = t6.ID";
				
		sql += " WHERE t1.OrganisationMemberID=" + omid;
		sql += " AND t1.Status = " + status;
				
		if(addpars != null && addpars.get("criteria") != null){
			String[] criteria = (String[])addpars.get("criteria");
			if(criteria[0].equals("validfrom")){
				sql += " ORDER BY t1.ValidFrom";
			}
			else if(criteria[0].equals("category")){
				sql += " ORDER BY t6.Title";
			}
		}
				
		ocs.queryData(sql, results);
				
		ocs.logAccess(sql);
		if(embed){		
			html.append("<p class=\"sectionheader\"><img src=\"images/meine-inserate_aktiv.png\">meine aktiven Inserate");
		}
		else{
			html.append("<b>" + title + "</b>\n");	
		}
				

				
		for(BasicClass record : results.getObjects()){
					
			String validfrom = record.getString("ValidFrom");
			String validuntil = record.getString("ValidUntil");
					
			String dateofbirth = record.getString("DATEOFBIRTH");
			String[] args = dateofbirth.split("\\.");
			String syear = args[args.length-1];
					
			int age = 0;
			int currentyear = DateConverter.getCurrentYear();
			if(syear.length()==4){
				try{
					int year = Integer.parseInt(syear);
					age = currentyear-year;
				}
				catch(java.lang.Exception e){
					ocs.logException(e);
				}
			}
					
			String sex = record.getString("SEX");
					
			String sex2 = "Mann";
					
			if(sex.equals("2")){
				sex2 = "Frau";	
			}
			if(embed){		
				html.append("<div class=\"searchresult3\">");
				html.append("<table>");
				html.append("<tr><td class=\"searchresultcell2\">");					
				html.append("<span class=\"searchresultfirstline\">" + record.getString("CATEGORY") + "</span>");
			}
			else{
				html.append("\n\n<b>" + record.getString("CATEGORY") + "</b>");
			}
					
			html.append("<br>" + DateConverter.sqlToShortDisplay(record.getString("VALIDFROM"), false) + "-" + DateConverter.sqlToShortDisplay(record.getString("VALIDUNTIL"), false));
			html.append("<br>" + sex2 + " (" + age + ") " + record.getString("TITLE"));
			html.append("<p>" + record.getString("DESCRIPTION"));
			if(embed){	
				html.append("</td><td>");
				html.append("<a class=\"objectaction\" href=\"javascript:createProcess('ch.opencommunity.process.MemberAdEdit','MemberAdID=" + record.getString("ID") + "')\"><img src=\"images/bearbeiten.png\">bearbeiten</a>");
				html.append("<br><a class=\"objectaction\" href=\"javascript:pauseAd(" + record.getString("ID") + ")\"><img src=\"images/pausieren.png\">pausieren</a>");
				html.append("<br><img src=\"images/delete.png\"><a class=\"objectaction\" href=\"javascript:deactivateAd(" + record.getString("ID") + "," + omid + ")\">löschen</a>");
				html.append("</tr>");
				html.append("</table>");
				html.append("</div>");
			}
		}	
		
		
		return html.toString();
		
	}
	public static List createMemberAdRequests(ApplicationContext context){
		return createMemberAdRequests(context, null, "", null, 0);
	}
	public static List createMemberAdRequests(ApplicationContext context, OrganisationMember om, String margid, Map comments, int status){

		
		WebApplicationContext webcontext= (WebApplicationContext)context;
		
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
		
		OpenCommunityServer ocs = (OpenCommunityServer)userSession.getRoot();
		
		if(om == null){
			om = userSession.getOrganisationMember();	
		}
		
 	 	if(om.getActiveRelationship() != null){
 	 		om = om.getActiveRelationship();	 
 	 	}
		Vector ids = new Vector();
		for(Object o : om.getMemberAdIDs()){
			
			Date now = new Date();
			Calendar cal = Calendar.getInstance();
			cal.setTime(now);
			cal.add(Calendar.DATE, 90); //minus number would decrement the days
			now = cal.getTime();
				
			MemberAdRequest mar = (MemberAdRequest)om.createObject("ch.opencommunity.advertising.MemberAdRequest", null, context);
			
			if(comments != null && comments.get(o) != null){
				mar.setProperty("UserComment", comments.get(o));	
			}
			else{
				String sql = "SELECT Template FROM MemberAd WHERE ID=" + o;
				ObjectCollection results = new ObjectCollection("Results", "*");
				ocs.queryData(sql, results);
				if(results.getObjects().size()==1){
					Record record = (Record)results.getObjects().get(0);
					String commentid = "comment_" + record.getString("TEMPLATE");
					if(userSession.get(commentid) != null){
						mar.setProperty("UserComment", userSession.get(commentid));
					}
				}
			}		

			
			mar.addProperty("OrganisationMemberID", "String", om.getName());
			if(margid.length() > 0){
				mar.setProperty("MemberAdRequestGroupID", margid);
			}
			mar.setProperty("DateCreated", ocs.getNow(true));
			mar.setProperty("MemberAd", o);
			mar.setProperty("ValidUntil", DateConverter.dateToSQL(now, false));
			mar.setProperty("Status", status);
			String id = ocs.insertObject(mar);
			ids.add(id);
			
		}
		
		userSession.removeMemberAdIDs();
		
		return ids;
	}
	public static String getMemoryList(OpenCommunityServer ocs, OpenCommunityUserSession userSession, boolean includeperson){
		
		StringBuilder html = new StringBuilder();
		html.append("<p class=\"sectionheader\"><img src=\"images/merken_aktiv.png\">Merkliste");
				
		html.append("<input class=\"nodebutton\" type=\"button\" onclick=\"getNextNode('overview=true')\" value=\"Abbrechen\" style=\"width : 140px; float : right;\">&nbsp;");
				
				
		if(userSession.getMemberAdIDs().size() > 0){
					
					//String url = request.getRequestURL().toString();
					
					
					
					html.append("<input class=\"nodebutton\" type=\"button\" onclick=\"getNextNode('creatememberadrequests=true')\" value=\"Abschliessen\" style=\"width : 140px; float : right;\">&nbsp;");
					html.append("<input class=\"nodebutton\" type=\"button\" onclick=\"getNextNode('creatememberadrequests=true&email=true')\" value=\"Abschliessen und Email\" style=\"width : 140px; float : right;\">&nbsp;");					
					html.append("<input class=\"nodebutton\" type=\"button\" onclick=\"getNextNode('creatememberadrequests=true&pdf=true')\" value=\"Abschliessen und PDF\" style=\"width : 140px; float : right;\">&nbsp;");
					
					
					//html.append("<input class=\"nodebutton\" type=\"button\" onclick=\"location.href=\'" + ocs.getBaseURL(request) + "/opencommunity/cms/nachbarnet/pinnwand?mode=start\'\" value=\"Weitere Adressen suchen\" style=\"width : 160px; float : right; margin-right : 10px;\">&nbsp;");

					
					
					
				
					String adids = "(";
					for(Object adid : userSession.getMemberAdIDs()){
						adids += adid + ",";
					}
					
					adids = adids.substring(0, adids.length()-1) + ")";
					
					ObjectCollection results = new ObjectCollection("Results", "*");
					
					String sql = "SELECT t1.ID, t1.Title, t1.Description, t1.ValidFrom, t1.ValidUntil, t4.Sex, t4.DateOfBirth, t4.Familyname, t4.Firstname,";
					sql += " t5.ZipCode, t5.Street, t5.Number, t5.City, t6.ID AS MAC, t6.Title AS Category, t7.Value AS Email, t8.Value AS PhoneP, t9.Value AS PhoneM FROM MemberAd AS t1";
					
					sql += " LEFT JOIN OrganisationMember AS t2 ON t1.OrganisationMemberID=t2.ID";
					sql += " LEFT JOIN Person AS t3 ON t2.Person=t3.ID";
					sql += " LEFT JOIN Identity AS t4 ON t4.PersonID=t3.ID";
					sql += " LEFT JOIN Address AS t5 ON t5.PersonID=t3.ID";

					sql += " LEFT JOIN Contact AS t7 ON t7.PersonID=t4.ID AND t7.Type=3";
					sql += " LEFT JOIN Contact AS t8 ON t8.PersonID=t4.ID AND t8.Type=0";
					sql += " LEFT JOIN Contact AS t9 ON t9.PersonID=t4.ID AND t9.Type=2";
					
					sql += " LEFT JOIN MemberAdCategory AS t6 ON t1.Template = t6.ID";
					
					sql += " WHERE t1.ID IN " + adids;
					
					/*
					if(addpars.get("criteria") != null){
						String[] criteria = (String[])addpars.get("criteria");
						if(criteria[0].equals("validfrom")){
							sql += " ORDER BY t1.ValidFrom";
						}
						else if(criteria[0].equals("category")){
							sql += " ORDER BY t6.Title";
						}
						else{
							sql += " ORDER BY t5.ZipCode";	
						}
					}
					else{
					*/
						sql += " ORDER BY t6.Title, t5.ZipCode";
					/*
					}
					*/
					
					ocs.queryData(sql, results);
					
					String prevcategory = "";
					

					
					for(BasicClass record : results.getObjects()){
						
						boolean include = true;
						
						String sex = record.getString("SEX");

						
						String sex2 = "Mann";
						if(sex.equals("2")){
							sex2 = "Frau";	
						}
						
						int currentyear = DateConverter.getCurrentYear();
						int yearofbirth = 1968;
						int age = currentyear - yearofbirth;
						
						String dateofbirth = record.getString("DATEOFBIRTH");
						
						if(dateofbirth.length() > 3){
							String[] args = dateofbirth.split("\\.");
							String syear = args[args.length-1];
							int year = Integer.parseInt(syear);
							age = currentyear - year;
						}
						
						String zipcode = record.getString("ZIPCODE");
						String city = record.getString("CITY");
						String category = record.getString("CATEGORY");
						String location = zipcode + " " + city;
						
						/*
						if(addpars.get("reload")==null){
							include = true;
						}
						else if(addpars.get(category)!=null && addpars.get(location)!=null){
							include = true;						
						}
						
						if(criteria1.indexOf(category)==-1){
							criteria1.add(category);	
						}
						if(criteria2.indexOf(location)==-1){
							criteria2.add(location);	
						}	
						*/
						
						
						
						if(include){
							
							if(!category.equals(prevcategory)){
								if(includeperson){   //Admin-Notizen
									html.append("<div>");
									
									String commentid = "comment_" + record.getString("MAC");
									
									html.append("<textarea id=\"" + commentid + "\" onblur=\"saveComment(this.id, this.value)\">");
									if(userSession.get(commentid) != null){
										html.append(userSession.get(commentid));	
									}
									html.append("</textarea>");
									html.append("</div>");
								}
								html.append("\n<div class=\"searchresultheader\">");
								html.append(category);
								html.append("</div>");
								
							}
							
							html.append("<div id=\"" + record.getString("ID") + "\" class=\"searchresult\" style=\"height : auto;\">");
							html.append("<table>");
							//html.append("<tr><td class=\"searchresultcell1\">" + category + ", " + zipcode + " " + city + "</td><td rowspan=\"2\"><a class=\"objectaction\" href=\"javascript:deselectAd(" + record.getString("ID") + ")\"><img src=\"images/delete.png\">löschen</a></td></tr>");
							html.append("<tr><td class=\"searchresultcell1\">" + zipcode + " " + city + "</td><td rowspan=\"2\"><a class=\"objectaction\" href=\"javascript:deselectAd(" + record.getString("ID") + ", 1)\"><img src=\"images/delete.png\">löschen</a></td></tr>");
							html.append("<tr><td class=\"searchresultcell2\">" + sex2 + " (" + age + ") " + record.getString("TITLE"));
							html.append("<br>" + record.getString("DESCRIPTION"));
							if(includeperson){
								html.append("<br>Name : " + record.getString("FAMILYNAME"));
								html.append("<br>Vorname : " + record.getString("FIRSTNAME"));
								html.append("<br>Adresse : " + record.getString("STREET") + " " + record.getString("NUMBER"));
								html.append("<br>" + record.getString("ZIPCODE") + " " + record.getString("CITY"));
								html.append("<br>Email : " + record.getString("EMAIL"));
								html.append("<br>Tel. p : " + record.getString("PHONEP"));
								html.append("<br>Tel. m : " + record.getString("PHONEM"));
							}
							//html.append("<br><br>Laufzeit Inserat vom " + DateConverter.sqlToShortDisplay(record.getString("VALIDFROM"),false) + " - " + DateConverter.sqlToShortDisplay(record.getString("VALIDUNTIL"),false) + "<br>&nbsp;");
							html.append("</td></tr>");
							html.append("</table>");
							html.append("</div>");
						}
						
						prevcategory = category;
				}
					
		}
		return html.toString();
	}
	public static String getMemoryList2(OpenCommunityServer ocs, OrganisationMemberController omc, OpenCommunityUserSession userSession, boolean includeperson, int mode){
		
		StringBuilder html = new StringBuilder();
		html.append("<p class=\"sectionheader\"><img src=\"images/merken_aktiv.png\">Merkliste");
		
		if(mode==2){
			html.append("<input class=\"nodebutton\" type=\"button\" onclick=\"getNextNode('cancelrequestcreate=true')\" value=\"Abbrechen\" style=\"width : 140px; float : right;\">&nbsp;");
		}
		else{
			html.append("<input class=\"nodebutton\" type=\"button\" onclick=\"onAction('" + omc.getPath() + "','cancelrequestcreate')\" value=\"Abbrechen\" style=\"width : 140px; float : right;\">&nbsp;");
		}	
				
		if(userSession.getMemberAdIDs() != null && userSession.getMemberAdIDs().size() > 0){
					
					//String url = request.getRequestURL().toString();
					
					
					if(mode==2){
						html.append("<input class=\"nodebutton\" type=\"button\" onclick=\"getNextNode('finishrequestcreate=true')\" value=\"Abbschliessen\" style=\"width : 140px; float : right;\">&nbsp;");
					}
					else{
						html.append("<input class=\"nodebutton\" type=\"button\" onclick=\"onAction('" + omc.getPath() + "','creatememebradrequests')\" value=\"Abschliessen\" style=\"width : 140px; float : right;\">&nbsp;");
						html.append("<input class=\"nodebutton\" type=\"button\" onclick=\"onAction('" + omc.getPath() + "','creatememebradrequests', '', 'email=true')\" value=\"Abschliessen und Email\" style=\"width : 140px; float : right;\">&nbsp;");					
						html.append("<input class=\"nodebutton\" type=\"button\" onclick=\"onAction('" + omc.getPath() + "','creatememebradrequests', '', 'createpdf=true')\" value=\"Abschliessen und PDF\" style=\"width : 140px; float : right;\">&nbsp;");
					}
					
					//html.append("<input class=\"nodebutton\" type=\"button\" onclick=\"location.href=\'" + ocs.getBaseURL(request) + "/opencommunity/cms/nachbarnet/pinnwand?mode=start\'\" value=\"Weitere Adressen suchen\" style=\"width : 160px; float : right; margin-right : 10px;\">&nbsp;");

					
					
					
				
					String adids = "(";
					for(Object adid : userSession.getMemberAdIDs()){
						adids += adid + ",";
					}
					
					adids = adids.substring(0, adids.length()-1) + ")";
					
					ObjectCollection results = new ObjectCollection("Results", "*");
					
					String sql = "SELECT t1.ID, t1.Title, t1.Description, t1.ValidFrom, t1.ValidUntil, t4.Sex, t4.DateOfBirth, t4.Familyname, t4.Firstname,";
					sql += " t5.ZipCode, t5.Street, t5.Number, t5.City, t6.ID AS MAC, t6.Title AS Category, t7.Value AS Email, t8.Value AS PhoneP, t9.Value AS PhoneM FROM MemberAd AS t1";
					
					sql += " LEFT JOIN OrganisationMember AS t2 ON t1.OrganisationMemberID=t2.ID";
					sql += " LEFT JOIN Person AS t3 ON t2.Person=t3.ID";
					sql += " LEFT JOIN Identity AS t4 ON t4.PersonID=t3.ID";
					sql += " LEFT JOIN Address AS t5 ON t5.PersonID=t3.ID";

					sql += " LEFT JOIN Contact AS t7 ON t7.PersonID=t4.ID AND t7.Type=3";
					sql += " LEFT JOIN Contact AS t8 ON t8.PersonID=t4.ID AND t8.Type=0";
					sql += " LEFT JOIN Contact AS t9 ON t9.PersonID=t4.ID AND t9.Type=2";
					
					sql += " LEFT JOIN MemberAdCategory AS t6 ON t1.Template = t6.ID";
					
					sql += " WHERE t1.ID IN " + adids;
					
					/*
					if(addpars.get("criteria") != null){
						String[] criteria = (String[])addpars.get("criteria");
						if(criteria[0].equals("validfrom")){
							sql += " ORDER BY t1.ValidFrom";
						}
						else if(criteria[0].equals("category")){
							sql += " ORDER BY t6.Title";
						}
						else{
							sql += " ORDER BY t5.ZipCode";	
						}
					}
					else{
					*/
						sql += " ORDER BY t6.Title, t5.ZipCode";
					/*
					}
					*/
					
					ocs.queryData(sql, results);
					
					String prevcategory = "";
					

					
					for(BasicClass record : results.getObjects()){
						
						boolean include = true;
						
						String sex = record.getString("SEX");

						
						String sex2 = "Mann";
						if(sex.equals("2")){
							sex2 = "Frau";	
						}
						int currentyear = DateConverter.getCurrentYear();
						int yearofbirth = 1968;
						int age = currentyear - yearofbirth;
						
						String dateofbirth = record.getString("DATEOFBIRTH");
						if(dateofbirth.length() > 3){
							String[] args = dateofbirth.split("\\.");
							String syear = args[args.length-1];
							int year = Integer.parseInt(syear);
							age = currentyear - year;
						}
						
						String zipcode = record.getString("ZIPCODE");
						String city = record.getString("CITY");
						String category = record.getString("CATEGORY");
						String location = zipcode + " " + city;
						
						/*
						if(addpars.get("reload")==null){
							include = true;
						}
						else if(addpars.get(category)!=null && addpars.get(location)!=null){
							include = true;						
						}
						
						if(criteria1.indexOf(category)==-1){
							criteria1.add(category);	
						}
						if(criteria2.indexOf(location)==-1){
							criteria2.add(location);	
						}	
						*/
						
						
						
						if(include){
							
							if(!category.equals(prevcategory)){
								if(includeperson){   //Admin-Notizen
									html.append("<div>");
									
									String commentid = "comment_" + record.getString("MAC");
									
									html.append("<textarea id=\"" + commentid + "\" onblur=\"saveComment(this.id, this.value)\" style=\"width : 500px;\">");
									if(userSession.get(commentid) != null){
										html.append(userSession.get(commentid));	
									}
									html.append("</textarea>");
									html.append("</div>");
								}
								html.append("\n<div class=\"searchresultheader\">");
								html.append(category);
								html.append("</div>");
								
							}
							
							html.append("<div id=\"" + record.getString("ID") + "\" class=\"searchresult\" style=\"height : auto;\">");
							html.append("<table>");
							//html.append("<tr><td class=\"searchresultcell1\">" + category + ", " + zipcode + " " + city + "</td><td rowspan=\"2\"><a class=\"objectaction\" href=\"javascript:deselectAd(" + record.getString("ID") + ")\"><img src=\"images/delete.png\">löschen</a></td></tr>");
							html.append("<tr><td class=\"searchresultcell1\">" + zipcode + " " + city + "</td><td rowspan=\"2\"><a class=\"objectaction\" href=\"javascript:deselectAd(" + record.getString("ID") + ", 1)\"><img src=\"images/delete.png\">löschen</a></td></tr>");
							html.append("<tr><td class=\"searchresultcell2\">" + sex2 + " (" + age + ") " + record.getString("TITLE"));
							html.append("<br>" + record.getString("DESCRIPTION"));
							if(includeperson){
								html.append("<br>Name : " + record.getString("FAMILYNAME"));
								html.append("<br>Vorname : " + record.getString("FIRSTNAME"));
								html.append("<br>Adresse : " + record.getString("STREET") + " " + record.getString("NUMBER"));
								html.append("<br>" + record.getString("ZIPCODE") + " " + record.getString("CITY"));
								html.append("<br>Email : " + record.getString("EMAIL"));
								html.append("<br>Tel. p : " + record.getString("PHONEP"));
								html.append("<br>Tel. m : " + record.getString("PHONEM"));
							}
							//html.append("<br><br>Laufzeit Inserat vom " + DateConverter.sqlToShortDisplay(record.getString("VALIDFROM"),false) + " - " + DateConverter.sqlToShortDisplay(record.getString("VALIDUNTIL"),false) + "<br>&nbsp;");
							html.append("</td></tr>");
							html.append("</table>");
							html.append("</div>");
						}
						
						prevcategory = category;
				}
					
		}
		return html.toString();
	}
	
	public static String getMemoryList3(OpenCommunityServer ocs, OrganisationMemberController omc, OpenCommunityUserSession userSession, boolean includeperson, int mode){
		
		StringBuilder html = new StringBuilder();
		
		/*
		html.append("<p class=\"sectionheader\"><img src=\"images/merken_aktiv.png\">Merkliste");
		
		if(mode==2){
			html.append("<input class=\"nodebutton\" type=\"button\" onclick=\"getNextNode('cancelrequestcreate=true')\" value=\"Abbrechen\" style=\"width : 140px; float : right;\">&nbsp;");
		}
		else{
			html.append("<input class=\"nodebutton\" type=\"button\" onclick=\"onAction('" + omc.getPath() + "','cancelrequestcreate')\" value=\"Abbrechen\" style=\"width : 140px; float : right;\">&nbsp;");
		}	
		*/
		
		if(userSession.getMemberAdIDs() != null && userSession.getMemberAdIDs().size() > 0){
					
				
				
					String adids = "(";
					for(Object adid : userSession.getMemberAdIDs()){
						adids += adid + ",";
					}
					
					adids = adids.substring(0, adids.length()-1) + ")";
					
					ObjectCollection results = new ObjectCollection("Results", "*");
					
					String sql = "SELECT t1.ID, t1.Title, t1.Description, t1.ValidFrom, t1.ValidUntil, t4.Sex, t4.DateOfBirth, t4.Familyname, t4.Firstname,";
					sql += " t5.ZipCode, t5.Street, t5.Number, t5.City, t6.ID AS MAC, t6.Title AS Category, t7.Value AS Email, t8.Value AS PhoneP, t9.Value AS PhoneM FROM MemberAd AS t1";
					
					sql += " LEFT JOIN OrganisationMember AS t2 ON t1.OrganisationMemberID=t2.ID";
					sql += " LEFT JOIN Person AS t3 ON t2.Person=t3.ID";
					sql += " LEFT JOIN Identity AS t4 ON t4.PersonID=t3.ID";
					sql += " LEFT JOIN Address AS t5 ON t5.PersonID=t3.ID";

					sql += " LEFT JOIN Contact AS t7 ON t7.PersonID=t4.ID AND t7.Type=3";
					sql += " LEFT JOIN Contact AS t8 ON t8.PersonID=t4.ID AND t8.Type=0";
					sql += " LEFT JOIN Contact AS t9 ON t9.PersonID=t4.ID AND t9.Type=2";
					
					sql += " LEFT JOIN MemberAdCategory AS t6 ON t1.Template = t6.ID";
					
					sql += " WHERE t1.ID IN " + adids;
					
					/*
					if(addpars.get("criteria") != null){
						String[] criteria = (String[])addpars.get("criteria");
						if(criteria[0].equals("validfrom")){
							sql += " ORDER BY t1.ValidFrom";
						}
						else if(criteria[0].equals("category")){
							sql += " ORDER BY t6.Title";
						}
						else{
							sql += " ORDER BY t5.ZipCode";	
						}
					}
					else{
					*/
						sql += " ORDER BY t6.Title, t5.ZipCode";
					/*
					}
					*/
					
					ocs.queryData(sql, results);
					
					String prevcategory = "";
					

					
					for(BasicClass record : results.getObjects()){
						
						boolean include = true;
						
						String sex = record.getString("SEX");

						
						String sex2 = "Mann";
						if(sex.equals("2")){
							sex2 = "Frau";	
						}
						int currentyear = DateConverter.getCurrentYear();
						int yearofbirth = 1968;
						int age = currentyear - yearofbirth;
						
						String dateofbirth = record.getString("DATEOFBIRTH");
						if(dateofbirth.length() > 3){
							String[] args = dateofbirth.split("\\.");
							String syear = args[args.length-1];
							int year = Integer.parseInt(syear);
							age = currentyear - year;
						}
						
						String zipcode = record.getString("ZIPCODE");
						String city = record.getString("CITY");
						String category = record.getString("CATEGORY");
						String location = zipcode + " " + city;
						
						/*
						if(addpars.get("reload")==null){
							include = true;
						}
						else if(addpars.get(category)!=null && addpars.get(location)!=null){
							include = true;						
						}
						
						if(criteria1.indexOf(category)==-1){
							criteria1.add(category);	
						}
						if(criteria2.indexOf(location)==-1){
							criteria2.add(location);	
						}	
						*/
						
						
						
						if(include){
							
							if(!category.equals(prevcategory)){
								if(includeperson){   //Admin-Notizen
									html.append("<div>");
									
									String commentid = "comment_" + record.getString("MAC");
									
									html.append("<textarea id=\"" + commentid + "\" onblur=\"saveComment(this.id, this.value)\" style=\"width : 500px;\">");
									if(userSession.get(commentid) != null){
										html.append(userSession.get(commentid));	
									}
									html.append("</textarea>");
									html.append("</div>");
								}
								html.append("\n<div class=\"searchresultheader\">");
								html.append(category);
								html.append("</div>");
								
							}
							
							html.append("<div id=\"" + record.getString("ID") + "\" class=\"searchresult\" style=\"height : auto;\">");
							html.append("<table>");
							//html.append("<tr><td class=\"searchresultcell1\">" + category + ", " + zipcode + " " + city + "</td><td rowspan=\"2\"><a class=\"objectaction\" href=\"javascript:deselectAd(" + record.getString("ID") + ")\"><img src=\"images/delete.png\">löschen</a></td></tr>");
							html.append("<tr><td class=\"searchresultcell1\">" + zipcode + " " + city + "</td><td rowspan=\"2\"><a class=\"objectaction\" href=\"javascript:deselectAd(" + record.getString("ID") + ", 1)\"><img src=\"images/delete.png\">löschen</a></td></tr>");
							html.append("<tr><td class=\"searchresultcell2\">" + sex2 + " (" + age + ") " + record.getString("TITLE"));
							html.append("<br>" + record.getString("DESCRIPTION"));
							if(includeperson){
								html.append("<br>Name : " + record.getString("FAMILYNAME"));
								html.append("<br>Vorname : " + record.getString("FIRSTNAME"));
								html.append("<br>Adresse : " + record.getString("STREET") + " " + record.getString("NUMBER"));
								html.append("<br>" + record.getString("ZIPCODE") + " " + record.getString("CITY"));
								html.append("<br>Email : " + record.getString("EMAIL"));
								html.append("<br>Tel. p : " + record.getString("PHONEP"));
								html.append("<br>Tel. m : " + record.getString("PHONEM"));
							}
							//html.append("<br><br>Laufzeit Inserat vom " + DateConverter.sqlToShortDisplay(record.getString("VALIDFROM"),false) + " - " + DateConverter.sqlToShortDisplay(record.getString("VALIDUNTIL"),false) + "<br>&nbsp;");
							html.append("</td></tr>");
							html.append("</table>");
							html.append("</div>");
						}
						
						prevcategory = category;
				}
					
		}
		return html.toString();
	}

	public static String getAddressList(OpenCommunityServer ocs, OpenCommunityUserSession userSession){
		
		StringBuilder addresses = new StringBuilder();
		
		
		String adids = "(";
		for(Object adid : userSession.getMemberAdIDs()){
						adids += adid + ",";
					}
					
					adids = adids.substring(0, adids.length()-1) + ")";
					
					ObjectCollection results = new ObjectCollection("Results", "*");
					
					String sql = "SELECT t1.ID, t1.Title, t1.Description, t1.ValidFrom, t1.ValidUntil, t4.Sex, t4.DateOfBirth, t4.Familyname, t4.Firstname,";
					sql += " t5.ZipCode, t5.Street, t5.Number, t5.City, t6.ID AS MAC, t6.Title AS Category, t7.Value AS Email, t8.Value AS PhoneP, t9.Value AS PhoneM FROM MemberAd AS t1";
					
					sql += " LEFT JOIN OrganisationMember AS t2 ON t1.OrganisationMemberID=t2.ID";
					sql += " LEFT JOIN Person AS t3 ON t2.Person=t3.ID";
					sql += " LEFT JOIN Identity AS t4 ON t4.PersonID=t3.ID";
					sql += " LEFT JOIN Address AS t5 ON t5.PersonID=t3.ID";

					sql += " LEFT JOIN Contact AS t7 ON t7.PersonID=t4.ID AND t7.Type=3";
					sql += " LEFT JOIN Contact AS t8 ON t8.PersonID=t4.ID AND t8.Type=0";
					sql += " LEFT JOIN Contact AS t9 ON t9.PersonID=t4.ID AND t9.Type=2";
					
					sql += " LEFT JOIN MemberAdCategory AS t6 ON t1.Template = t6.ID";
					
		sql += " WHERE t1.ID IN " + adids;	
		
		ocs.queryData(sql, results);
					
		String prevcategory = "";
					
		for(BasicClass record : results.getObjects()){	
			
						String sex = record.getString("SEX");

						
						String sex2 = "Mann";
						if(sex.equals("2")){
							sex2 = "Frau";	
						}
						
						int currentyear = DateConverter.getCurrentYear();
						int yearofbirth = 1968;
						int age = currentyear - yearofbirth;
						
						String dateofbirth = record.getString("DATEOFBIRTH");
						if(dateofbirth.length() > 3){
							String[] args = dateofbirth.split("\\.");
							String syear = args[args.length-1];
							int year = Integer.parseInt(syear);
							age = currentyear - year;
						}
						
						String zipcode = record.getString("ZIPCODE");
						String city = record.getString("CITY");
						String category = record.getString("CATEGORY");
						String location = zipcode + " " + city;
						
			addresses.append("\n\n");
			addresses.append("\nName : " + record.getString("FAMILYNAME"));
			addresses.append("\nVorname : " + record.getString("FIRSTNAME"));
			addresses.append("\nAdresse : " + record.getString("STREET") + " " + record.getString("NUMBER"));
			addresses.append("\n" + record.getString("ZIPCODE") + " " + record.getString("CITY"));
			addresses.append("\nEmail : " + record.getString("EMAIL"));
			addresses.append("\nTel. p : " + record.getString("PHONEP"));
			addresses.append("\nTel. m : " + record.getString("PHONEM"));
			
			addresses.append("\n");
			
			addresses.append("\n" + sex2 + " (" + age + ") " + record.getString("TITLE"));
			addresses.append("\n" + record.getString("DESCRIPTION"));
			
		}
					
		return addresses.toString();
		
	}
	
	
	public static String getCategorySelection(MemberAdAdministration maa){
		
		StringBuilder html = new StringBuilder();
		
		html.append("<select class=\"catselect2\" onchange=\"location.href='/servlet.srv?action=searchads&category=' + this.value\" style=\"float : right;\">");
		
		html.append("<option value=\"\">weitersuchen</option>");
		
		for(BasicClass bc :  maa.getObjects("MemberAdCategory")){
			
			html.append("<option value=\"" + bc.getName() + "\">" + bc.getString("Title") + "</option>");
			
		}
		
		
		
		html.append("</select>");
		
		
		return html.toString();
		
	}
}