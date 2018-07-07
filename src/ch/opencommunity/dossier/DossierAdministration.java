package ch.opencommunity.dossier;

import ch.opencommunity.server.OpenCommunityServer;
import ch.opencommunity.common.OpenCommunityUserSession;


import org.kubiki.base.BasicClass;
import org.kubiki.base.ObjectCollection;
import org.kubiki.base.ConfigValue;
import org.kubiki.base.ActionResult;
import org.kubiki.application.ApplicationContext;
import org.kubiki.application.server.WebApplicationModule;
import org.kubiki.application.CodeDefinition;
import org.kubiki.gui.html.HTMLFormManager;
import org.kubiki.util.StringUtilities;

import java.util.Map;
import java.util.Vector;


public class DossierAdministration extends WebApplicationModule{
	
		OpenCommunityServer server = null;
		Map<String, String> institutionTypeMap = null;
		
		Vector<ConfigValue> purpose = null;
	
		public DossierAdministration(){
			
			setName("Organisations");
			
			addContextMenuEntry("organisationadd");
			addContextMenuEntry("groupadd");
			addContextMenuEntry("listexport");
			
			addProperty("OrganisationType", "Integer", "");
			addProperty("Purpose", "Integer", "");
			addProperty("Eligibility", "Integer", "");
			addProperty("OrganisationClass", "Integer", "");
			
			
		}
		public void initObjectLocal(){
			server = (OpenCommunityServer)getRoot();
			institutionTypeMap = server.getInstitutionTypeMap();
			getProperty("OrganisationType").setSelection(server.getInstitutionTypes());
			
			purpose = new Vector<ConfigValue>();
			purpose.add(new ConfigValue("1", "1", "Soziales (allg.)"));
			purpose.add(new ConfigValue("2", "2", "Armut, Arbeitslosigkeit, Not"));
			purpose.add(new ConfigValue("3", "3", "Krankheit, Invalidität"));
			purpose.add(new ConfigValue("4", "4", "Migration, Integration"));
			purpose.add(new ConfigValue("5", "5", "Alter"));
			purpose.add(new ConfigValue("6", "6", "Familie, Kind, Frauen, Männer"));
			purpose.add(new ConfigValue("7", "7", "Bildung, Stipendien"));
			purpose.add(new ConfigValue("8", "8", "anderes"));
			

			getProperty("Purpose").setSelection(purpose);
			
			Vector eligibility = new Vector();
			eligibility.add(new ConfigValue("1", "1", "*"));
			eligibility.add(new ConfigValue("2", "2", "**"));
			eligibility.add(new ConfigValue("3", "3", "***"));
			getProperty("Eligibility").setSelection(eligibility);			
			
		}
		public ActionResult onAction(BasicClass source, String command, ApplicationContext context){
			
			ActionResult result = null;

			OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
			
			
			
			if(command.equals("organisationadd")){

				result = server.startProcess("ch.opencommunity.process.OrganisationAdd", userSession, null, context, this);
			}
			else if(command.equals("filterorganisations")){
				
				userSession.saveFilterProperties(context);

				result = new ActionResult(ActionResult.Status.OK, "Filter angewendet");
				result.setParam("dataContainer", "organisationlist");
				result.setData(getOrganisationList(context));
			}
			else if(command.equals("getdossierinfo")){
				
				String dossierid = context.getString("dossierid");
				if(dossierid != null){
					DossierInfo dossierInfo = getDossierInfo(server, dossierid);
					if(dossierInfo != null){
						
						result = new ActionResult(ActionResult.Status.OK, "Info geladen");
						result.setParam("dataContainer", "popup");
						result.setParam("dataContainerVisibility", "visible");
						result.setData(dossierInfo.toHTML());							
						
					}
					
				}
				
				
			}
			else if(command.equals("listexport")){

				result = server.startProcess("ch.opencommunity.process.ListExport", userSession, null, context, this);
			}
			return result;
			
		}
	
		public String getMainForm(ApplicationContext context){
			
			OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
			
			HTMLFormManager formManager = server.getFormManager();
			
			StringBuilder html = new StringBuilder();
			
			
			
			html.append(formManager.getToolbar(this, null, null, context, true));
			
			html.append("<div id=\"listfilter\" class=\"listfilter\" style=\"position : absolute; left : 0px; width : 300px; top : 70px; bottom : 0px; overflow : auto;\">");
			
			html.append(getFilter(userSession));
			
			html.append("</div>");
			
			html.append("<div id=\"organisationlist\" style=\"top : 70px; left : 300px; bottom : 0px; right : 0px; overflow : auto;\">");
			
			html.append(getOrganisationList(context));
			
			html.append("</div>");
			
			return html.toString();	
			
		}
		public String getFilter(OpenCommunityUserSession userSession){
			
			StringBuilder html = new StringBuilder();
			
			html.append("Abfrage: " + server.getFormManager().getSelection(userSession.getProperty("CurrentQueryDefinition2"), "mode", "", true, true, true, "", "onAction('" + userSession.getPath() + "','setquery2','','queryid=' + this.value)", false));

			
			html.append("<p><b>Filtern nach</b>");
			
			html.append("<form id=\"filter_organisations\">");
			html.append("<table>");
			html.append("<tr><td>Bezeichnung</td><td>" + server.getFormManager().getTextField(userSession.getProperty("OrganisationTitle"), true, "", null, null, "onkeyup=\"filterOrganisations('" + getPath() + "')\")") + "</td></tr>");			
			html.append("<tr><td>Organisationstyp</td><td>" + server.getFormManager().getSelection(userSession.getProperty("OrganisationType"), true, "") + "</td></tr>");
			html.append("<tr><td>Eignung</td><td>" + server.getFormManager().getSelection(userSession.getProperty("Eligibility"), true, "") + "</td></tr>");
			
			html.append("<tr><td>Stiftungszweck</td><td>");
			
			String spurpose = userSession.getString("Purpose");
			html.append(spurpose);
			server.logAccess("spurpose: " + spurpose);
			spurpose = spurpose.replaceAll("[()]","");
			Map valueMap = StringUtilities.arrayToMap(spurpose.split(","));
			
			for(ConfigValue cv : purpose){
				if(valueMap.get(cv.getValue()) != null){
					html.append("<input type=\"checkbox\" name=\"Purpose\" value=\"" + cv.getValue() + "\" CHECKED>" + cv.getLabel() + "<br />");					
				}
				else{
					html.append("<input type=\"checkbox\" name=\"Purpose\" value=\"" + cv.getValue() + "\">" + cv.getLabel() + "<br />");
				}
				
			}
			html.append("</td></tr>");
			
			html.append("<tr><td valign=\"top\">Förderlogik</td><td>");
			
			String ssubtype = userSession.getString("OrganisationSubtype");

			ssubtype = ssubtype.replaceAll("[()]","");
			server.logAccess("subtype : " + ssubtype);
			valueMap = StringUtilities.arrayToMap(ssubtype.split(","));
			
			CodeDefinition subtypes = server.getCodeDefinition("Foerderlogik");
			if(subtypes != null){
				for(ConfigValue cv : subtypes.getCodeList()){

					if(valueMap.get(cv.getValue()) != null){
						html.append("<input type=\"checkbox\" name=\"OrganisationSubtype\" value=\"" + cv.getValue() + "\" SELECTED>" + cv.getLabel() + "<br />");					
					}
					else{
						html.append("<input type=\"checkbox\" name=\"OrganisationSubtype\" value=\"" + cv.getValue() + "\">" + cv.getLabel() + "<br />");
					}
					
					
				}
			}
			html.append("</td></tr>");
			

			
			html.append("</table>");

			html.append("<input type=\"button\" class=\"actionbutton\" value=\"Formular zurücksetzen\" onclick=\"reset()\">");
			html.append("<input type=\"button\" class=\"actionbutton\" value=\"Filter anwenden\" onclick=\"filterOrganisations('" + getPath() + "')\">");
			
			html.append("</form>");
			
			html.append(" <input type=\"button\" class=\"actionbutton\" onclick=\"onAction('" + userSession.getPath() + "','querydefinitionsave','','scope=2')\"  value=\"Aktuelle Abfrage speichern\">");
			html.append(" <input type=\"button\" class=\"actionbutton\" onclick=\"onAction('" + userSession.getPath() + "','querydefinitionadd','','scope=2')\"  value=\"Als neue Abfrage speichern\">");
			html.append(" <input type=\"button\" class=\"actionbutton\" onclick=\"onAction('" + userSession.getPath() + "','querydefinitiondelete','','scope=2')\"  value=\"Abfrage löschen\">");
			
			return html.toString();
			
		}
		public String getOrganisationList(){
			return getOrganisationList(null);
		}
		public String getSQL(ApplicationContext context){
			
			OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
			
			String sql = "SELECT DISTINCT t1.ID, t1.Title, t1.Type, t2.Street, t2.Number, t2.ZipCode, t2.City, t3.ID AS DossierID, t3.Title AS DossierTitle, t4.Value AS TelB, t5.Value AS Email, t6.Value AS Web, t7.ID AS MainContactPerson, t9.Firstname, t9.Familyname";
			sql += " FROM OrganisationalUnit AS t1";
			sql += " LEFT JOIN Address AS t2 ON t1.ID=t2.OrganisationalUnitID";
			sql += " LEFT JOIN Dossier AS t3 ON t1.ID=t3.OrganisationalUnit";
			sql += " LEFT JOIN Contact AS t4 ON t1.ID=t4.OrganisationalUnitID AND t4.Type=1";
			sql += " LEFT JOIN Contact AS t5 ON t1.ID=t5.OrganisationalUnitID AND t5.Type=3";
			sql += " LEFT JOIN Contact AS t6 ON t1.ID=t6.OrganisationalUnitID AND t6.Type=5";
			sql += " LEFT JOIN OrganisationMember AS t7 ON t1.MainContactPerson=t7.ID";
			sql += " LEFT JOIN Person AS t8 ON t7.Person=t8.ID";
			sql += " LEFT JOIN Identity AS t9 ON t9.PersonID=t8.ID";
			sql += " LEFT JOIN ObjectDetail AS t10 ON t10.DossierID=t3.ID";
				
			String conditions = "";
				
				String operator = " WHERE";
				
				String organisationTitle = userSession.getString("OrganisationTitle");
				if(organisationTitle.length() > 1){
					conditions += operator + " t1.Title ilike '%" + organisationTitle + "%'";		
				}
				
				if(context != null){
					
					String spurpose = userSession.getString("Purpose");
					if(spurpose.length() > 2){
						sql += " JOIN Parameter AS t11 ON t11.ObjectDetailID=t10.ID AND t11.Template IN (33)";
						
						operator = " AND";
						
					}
					
					int eligibility = userSession.getID("Eligibility");
					if(eligibility > 0){
						sql += " JOIN Parameter AS t12 ON t12.ObjectDetailID=t10.ID AND t12.Template IN (34)";
						conditions += operator + " t12.Value=" + eligibility;	
						operator = " AND";
						
					}
					
					String ssubtype = userSession.getString("OrganisationSubtype");
					if(ssubtype.length() > 2){
						sql += " JOIN Parameter AS t13 ON t13.ObjectDetailID=t10.ID AND t13.Template IN (32)";
						conditions += operator + " t13.Value IN " + ssubtype;	
						operator = " AND";
						
					}
					
	
				int type = userSession.getID("OrganisationType");
				if(type > 0){
					conditions += operator + " t1.Type=" + type;	
					operator = " AND";
				}
			}
				
			sql += conditions + " ORDER BY t1.Title";
			return sql;	
		}
		public String getOrganisationList(ApplicationContext context){
			
			StringBuilder html = new StringBuilder();
			
			OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
			
			try{
			
				html.append("<table>");
				
				html.append("<tr>");
				html.append("<td class=\"tableheader\">Bezeichnung</td>");
				html.append("<td class=\"tableheader\">Typ</td>");
				html.append("<td class=\"tableheader\">Strasse</td>");
				html.append("<td class=\"tableheader\">Nummer</td>");
				html.append("<td class=\"tableheader\">PLZ</td>");
				html.append("<td class=\"tableheader\">Ort</td>");
				html.append("<td class=\"tableheader\">Telefon</td>");
				html.append("<td class=\"tableheader\">Email</td>");
				html.append("<td class=\"tableheader\">web</td>");
				html.append("<td class=\"tableheader\">Dossier</td>");
				html.append("<td class=\"tableheader\">Hauptkontakt</td>");
				html.append("</tr>");
				
				/*
				String sql = "SELECT DISTINCT t1.ID, t1.Title, t1.Type, t2.Street, t2.Number, t2.ZipCode, t2.City, t3.ID AS DossierID, t3.Title AS DossierTitle, t4.Value AS TelB, t5.Value AS Email, t6.Value AS Web, t7.ID AS MainContactPerson, t9.Firstname, t9.Familyname";
				sql += " FROM OrganisationalUnit AS t1";
				sql += " LEFT JOIN Address AS t2 ON t1.ID=t2.OrganisationalUnitID";
				sql += " LEFT JOIN Dossier AS t3 ON t1.ID=t3.OrganisationalUnit";
				sql += " LEFT JOIN Contact AS t4 ON t1.ID=t4.OrganisationalUnitID AND t4.Type=1";
				sql += " LEFT JOIN Contact AS t5 ON t1.ID=t5.OrganisationalUnitID AND t5.Type=3";
				sql += " LEFT JOIN Contact AS t6 ON t1.ID=t6.OrganisationalUnitID AND t6.Type=5";
				sql += " LEFT JOIN OrganisationMember AS t7 ON t1.MainContactPerson=t7.ID";
				sql += " LEFT JOIN Person AS t8 ON t7.Person=t8.ID";
				sql += " LEFT JOIN Identity AS t9 ON t9.PersonID=t8.ID";
				sql += " LEFT JOIN ObjectDetail AS t10 ON t10.DossierID=t3.ID";
				
				String conditions = "";
				
				String operator = " WHERE";
				
				String organisationTitle = userSession.getString("OrganisationTitle");
				if(organisationTitle.length() > 1){
					conditions += operator + " t1.Title ilike '%" + organisationTitle + "%'";		
				}
				
				if(context != null){
					
					String spurpose = userSession.getString("Purpose");
					if(spurpose.length() > 2){
						sql += " JOIN Parameter AS t11 ON t11.ObjectDetailID=t10.ID AND t11.Template IN (33)";
						
						operator = " AND";
						
					}
					
					int eligibility = userSession.getID("Eligibility");
					if(eligibility > 0){
						sql += " JOIN Parameter AS t12 ON t12.ObjectDetailID=t10.ID AND t12.Template IN (34)";
						conditions += operator + " t12.Value=" + eligibility;	
						operator = " AND";
						
					}
					
					String ssubtype = userSession.getString("OrganisationSubtype");
					if(ssubtype.length() > 2){
						sql += " JOIN Parameter AS t13 ON t13.ObjectDetailID=t10.ID AND t13.Template IN (32)";
						conditions += operator + " t13.Value IN " + ssubtype;	
						operator = " AND";
						
					}
					
	
					int type = userSession.getID("OrganisationType");
					if(type > 0){
						conditions += operator + " t1.Type=" + type;	
						operator = " AND";
					}
				}
				
				sql += conditions + " ORDER BY t1.Title";
				
				*/
				
				String sql = getSQL(context);
				
				server.logAccess(sql);
				
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
					
					html.append("<td class=\"datacell\">" + record.getString("TITLE") + "</td>");
					html.append("<td class=\"datacell\">" + institutionTypeMap.get(record.getString("TYPE")) + "</td>");
					
					html.append("<td class=\"datacell\">" + record.getString("STREET") + "</td>");
					html.append("<td class=\"datacell\">" + record.getString("NUMBER") + "</td>");
					html.append("<td class=\"datacell\">" + record.getString("ZIPCODE") + "</td>");
					html.append("<td class=\"datacell\">" + record.getString("CITY") + "</td>");
					
	
					
					html.append("<td class=\"datacell\">" + record.getString("TELB") + "</td>");
					html.append("<td class=\"datacell\">" + record.getString("EMAIL") + "</td>");
					html.append("<td class=\"datacell\">" + record.getString("WEB") + "</td>");
					
					String dossierid = record.getString("DOSSIERID");
					if(dossierid.length() > 0){
					
						html.append("<td class=\"datacell\"><a href=\"javascript:openDossier(" + dossierid + ")\">" + record.getString("DOSSIERTITLE") + "</a>");
						html.append("<a href=\"javascript:onAction('" + getPath() + "','getdossierinfo','','dossierid=" + dossierid + "')\"><img src=\"images/icons/info.png\"></a>");
						html.append("</td>");
						
					}
					else{
							
					}
					
					String mainContactPerson= record.getString("MAINCONTACTPERSON");
					if(mainContactPerson != null && mainContactPerson.length() > 0){
						html.append("<td class=\"datacell\"><a href=\"javascript:editOrganisationMember(" + mainContactPerson + ")\">" + record.getString("FIRSTNAME") + " " + record.getString("FAMILYNAME") + "</a></td>");
					}
					
					html.append("</tr>");				
				}
				
				html.append("</table>");
				
			}
			catch(java.lang.Exception e){
				server.logException(e);
			}
			
			
			return html.toString();
		}
	public static DossierInfo getDossierInfo(OpenCommunityServer ocs, String dossierid){
		
		String sql = "SELECT t1.Title FROM OrganisationalUnit AS t1";
		sql += " JOIN Dossier AS t2 ON t2.OrganisationalUnit=t1.ID";
		
		ObjectCollection results = new ObjectCollection("Results", "*");
		ocs.queryData(sql, results);
		
		DossierInfo dossierInfo = new DossierInfo();
		
		
		
		for(BasicClass record : results.getObjects()){
			dossierInfo.setProperty("Title", record.getString("TITLE"));
			
		}
		
		sql = "SELECT t1.*, t2.ID AS DossierID, t3.Title As Organisation, t4.Date AS Date1, t5.Date AS Date2, t6.DateCreated AS Date3, t7.Value AS Result";
		sql += " FROM Project AS t1";
		sql += " JOIN Dossier AS t2 ON t1.DossierID=t2.ID";
		sql += " JOIN OrganisationalUnit AS t3 ON t2.OrganisationalUnit=t3.ID";
		sql += " LEFT JOIN Activity AS t4 ON t4.ProjectID=t1.ID AND t4.Template=12";
		sql += " LEFT JOIN Activity AS t5 ON t5.ProjectID=t1.ID AND t5.Template=17";
		sql += " LEFT JOIN ObjectStatus AS t6 ON t6.ProjectID=t1.ID";
		sql += " LEFT JOIN Parameter AS t7 ON t7.ActivityID=t5.ID AND t7.Template=35";
		sql += " WHERE t2.ID=" + dossierid;
		
		ocs.queryData(sql, dossierInfo.getObjectCollection("Projects"));		
		
		return dossierInfo;
		
	}		
	
	
}