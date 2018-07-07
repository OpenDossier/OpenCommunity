package ch.opencommunity.view;

import ch.opencommunity.server.OpenCommunityServer;
import ch.opencommunity.server.HTMLForm;
import ch.opencommunity.common.OpenCommunityUserSession;

import org.kubiki.base.ObjectCollection;
import org.kubiki.base.BasicClass;
import org.kubiki.base.ConfigValue;
import org.kubiki.base.ActionHandler;
import org.kubiki.base.ActionResult;
import org.kubiki.application.Application;
import org.kubiki.application.ApplicationContext;
import org.kubiki.application.server.WebApplicationClient;
import org.kubiki.util.DateConverter;

import org.kubiki.servlet.WebApplication;

import org.kubiki.gui.FilterParameters;
import org.kubiki.gui.html.HTMLQueryList;
import org.kubiki.gui.html.HTMLColumnDefinition;
import org.kubiki.gui.html.HTMLQueryListFilter;

import java.util.HashMap;
import java.util.Vector;


public class OrganisationMemberList extends HTMLQueryList{
	
	int mode = 1;
	Vector modes = null;

	
	public OrganisationMemberList(WebApplication webapp){
		super(webapp);
		

		
		highlightCriteria = "lastomid";
		highlightCriteriaRecordValue = "OMID";
		
		defaultOrder = " ORDER BY t1.DateCreated DESC";

		

		
		setID("organisationMemberList");
		//setSQL(sql);
		
		
		HashMap statusCode = new HashMap();
		statusCode.put("0", "registriert");
		statusCode.put("1", "aktiv");
		statusCode.put("2", "inaktiv");
		statusCode.put("3", "zu löschen");
		statusCode.put("4", "zu kontrollieren");
		statusCode.put("5", "anonymisiert");
		statusCode.put("6", "zu anonymisieren");
		
		String[] editCommand = new String[2];
		editCommand[0] = "<a href=\"javascript:editOrganisationMember(";
		editCommand[1] = ")\">Bearbeiten</a>";
		
		
		HTMLColumnDefinition columnDefinition = new HTMLColumnDefinition("OMSTATUS", "Status");
		columnDefinition.setCode(statusCode);
		addColumnDefinition(columnDefinition);
		
		columnDefinition = new HTMLColumnDefinition("DATECREATED", "Dat. registriert"){
			public String getRecordValue(BasicClass record){
				return DateConverter.sqlToShortDisplay(record.getString("DATECREATED"), false);
			}			
			
		};
		addColumnDefinition(columnDefinition);
		
		columnDefinition = new HTMLColumnDefinition("DATEMODIFIED", "Zul. geändert"){
			public String getRecordValue(BasicClass record){
				return DateConverter.sqlToShortDisplay(record.getString("DATECREATED"), true);
			}			
			
		};
		addColumnDefinition(columnDefinition);
		
		columnDefinition = new HTMLColumnDefinition("PARENTID", "Zust. Person"){
			public String getRecordValue(BasicClass record){
				if(record.getID("PARENTID") > 0){
					return "<a href=\"javascript:editOrganisationMember(" + record.getString("PARENTID") + ")\">" + record.getString("PARENTNAME") + "</a>";	
				}
				else{
					return "";
				}
			}			
			
		};
		columnDefinition.setCommand(editCommand);
		addColumnDefinition(columnDefinition);
		

		
		
		addColumnDefinition("FAMILYNAME", "Nachname");
		addColumnDefinition("FIRSTNAME", "Vorname");
		addColumnDefinition("STREET", "Strasse");
		addColumnDefinition("NUMBER", "Nummer");
		addColumnDefinition("ZIPCODE", "PLZ");
		addColumnDefinition("CITY", "Ort");

		/*
		addColumnDefinition("TELP", "Tel.p.");
		addColumnDefinition("TELB", "Tel.g.");
		addColumnDefinition("TELM", "Tel.m.");
		addColumnDefinition("EMAIL", "Email");
		

		
		
		addColumnDefinition(new BooleanColumnDefinition("ROLE1", "Benutzer"));
		addColumnDefinition(new BooleanColumnDefinition("ROLE2", "Mitglied"));
		addColumnDefinition(new BooleanColumnDefinition("ROLE3", "Gönner"));		
		addColumnDefinition(new BooleanColumnDefinition("LOGIN", "Login"));
		*/
		
		columnDefinition = new HTMLColumnDefinition("MODIFICATIONID", "Addr.änderung pendent"){
			public String getRecordValue(BasicClass record){
				if(record.getID("MODIFICATIONID") > 0){
					return "<a href=\"javascript:createProcess('ch.opencommunity.process.OrganisationMemberModify','OMID=" + record.getString("OMID") + "')\">JA</a>";
				}
				else{
					return "";
				}
			}			
			
		};
		addColumnDefinition(columnDefinition);
		
		addColumnDefinition("CNT1", "Anzahl", null, 3);
		addColumnDefinition("SUM1", "Summe", null, 3);
		addColumnDefinition("AVG1", "Durchschn.", null, 3);
		addColumnDefinition("MAX1", "Max.", null, 3);
		
		addColumnDefinition("COMMENT", "Kommentar");
		
		columnDefinition = new HTMLColumnDefinition("OMID", "");
		columnDefinition.setCommand(editCommand);
		addColumnDefinition(columnDefinition);
		
		HTMLQueryListFilter filter = new HTMLQueryListFilter(){
			
			public String toHTML(ApplicationContext context, FilterParameters parameters){
				
				OpenCommunityServer ocs = (OpenCommunityServer)getWebApplication();
				OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
				
				StringBuilder filterdef = new StringBuilder();
				
				filterdef.append("Abfrage: " + ocs.getFormManager().getSelection(userSession.getProperty("CurrentQueryDefinition"), "mode", "", true, true, true, "", "onAction('" + userSession.getPath() + "','setquery','','queryid=' + this.value)", false));
				
				filterdef.append("<br>Filtermodus: " + ocs.getFormManager().getSelection(userSession.getProperty("FilterMode"), "mode", "", true, true, true, "", "onAction('" + getPath() + "','setmode','','mode=' + this.value)", false));
				
				
				filterdef.append("<form name=\"filter_" + getName() + "\" id=\"filter_" + getName() + "\">Filtern nach : ");
				
				filterdef.append("<table>");
				
				filterdef.append("<tr><td>Name</td><td>" + ocs.getFormManager().getTextField(userSession.getProperty("FamilyName"), true, "", null, null, "onkeyup=\"filterList2('" + getName() + "')\")") + "</td></tr>");
				filterdef.append("<tr><td>Vorname</td><td>" + ocs.getFormManager().getTextField(userSession.getProperty("FirstName"), true, "", null, null, "onkeyup=\"filterList2('" + getName() + "')\")") + "</td></tr>");
				filterdef.append("<tr><td>Geschlecht</td><td>" + ocs.getFormManager().getSelection(userSession.getProperty("Sex"), true, "") + "</td></tr>");
				filterdef.append("<tr><td>Geburtsjahr</td><td>" + ocs.getFormManager().getTextField(userSession.getProperty("DateOfBirth"), true, "", null, null, "onkeyup=\"filterList2('" + getName() + "')\")") + "</td></tr>");
				filterdef.append("<tr><td>PLZ</td><td>" + ocs.getFormManager().getTextField(userSession.getProperty("ZipCode"), true, "", null, null, "onkeyup=\"filterList2('" + getName() + "')\")") + "</td></tr>");
				filterdef.append("<tr><td>Email</td><td>" + ocs.getFormManager().getTextField(userSession.getProperty("Email"), true, "", null, null, "onkeyup=\"filterList2('" + getName() + "')\")") + "</td></tr>");
				filterdef.append("<tr><td>Telefon</td><td>" + ocs.getFormManager().getTextField(userSession.getProperty("Telephone"), true, "", null, null, "onkeyup=\"filterList2('" + getName() + "')\")") + "</td></tr>");
				filterdef.append("<tr><td>Keine Email</td><td>" + ocs.getFormManager().getCheckBox(userSession.getProperty("NoEmail"), true, "", "1", null));
				//filterdef.append(" Keine Email <input type=\"checkbox\" name=\"NoEmail\" value=\"1\">");

				filterdef.append("<tr><td>Nur aktive</td><td>" + ocs.getFormManager().getCheckBox(userSession.getProperty("OnlyActive"), true, "", "1", null) + "</td></tr>");
				filterdef.append("<tr><td>Inkl. zu löschende</td><td>" + ocs.getFormManager().getCheckBox(userSession.getProperty("IsToDelete"), true, "", "1", null) + "</td></tr>");
				
				filterdef.append("<tr><td>Registriert seit</td><td>" + ocs.getFormManager().getDateField(userSession.getProperty("RegisteredSince"), true, "")+ "</td></tr>");
				filterdef.append("<tr><td>Registriert vor</td><td>" + ocs.getFormManager().getDateField(userSession.getProperty("RegisteredBefore"), true, "")+ "</td></tr>");
				filterdef.append("<tr><td>Status</td><td>" + ocs.getFormManager().getSelection(userSession.getProperty("OrganisationMemberStatus"), true, "") + "</td></tr>");
				
				if(userSession.getID("FilterMode")==2 || userSession.getID("FilterMode")==4){
					
					filterdef.append("<tr><td>Altersgruppe</td><td>" + ocs.getFormManager().getSelection(userSession.getProperty("AgeGroup"), true, "") + "</td></tr>");
					 
					filterdef.append("<tr><td>Benutzer</td><td>" + ocs.getFormManager().getCheckBox(userSession.getProperty("IsUser"), true, "", "1", null) + "</td></tr>");
					filterdef.append("<tr><td>Mitglied</td><td>" + ocs.getFormManager().getCheckBox(userSession.getProperty("IsMember"), true, "", "1", null) + "</td></tr>");
					filterdef.append("<tr><td>Gönner</td><td>" + ocs.getFormManager().getCheckBox(userSession.getProperty("IsSponsor"), true, "", "1", null) + "</td></tr>");
					filterdef.append("<tr><td>Nachbarcheque</td><td>" + ocs.getFormManager().getCheckBox(userSession.getProperty("IsChequeReceiver"), true, "", "1", null) + "</td></tr>");
					
					filterdef.append("<tr><td valign=\"top\">Inserate</td><td>");
					                                           
					filterdef.append("Inserattyp<br>" + ocs.getFormManager().getSelection(userSession.getProperty("Type1"), true, ""));
					filterdef.append("<br>Rubrik<br>" + ocs.getFormManager().getSelection(userSession.getProperty("Category1"), true, ""));
					filterdef.append("<br>Zeitraum von<br>" + ocs.getFormManager().getDateField(userSession.getProperty("ValidFrom1"), true, ""));					
					filterdef.append("<br>Zeitraum bis<br>" + ocs.getFormManager().getDateField(userSession.getProperty("ValidUntil1"), true, ""));	
					filterdef.append("<br>Status<br>" + ocs.getFormManager().getSelection(userSession.getProperty("Status1"), true, ""));
					
					filterdef.append("<tr><td valign=\"top\">Inseratnachfragen</td><td>");
					                                           
					filterdef.append("Inserattyp<br>" + ocs.getFormManager().getSelection(userSession.getProperty("Type2"), true, ""));
					filterdef.append("<br>Rubrik<br>" + ocs.getFormManager().getSelection(userSession.getProperty("Category2"), true, ""));
					filterdef.append("<br>Zeitraum von<br>" + ocs.getFormManager().getDateField(userSession.getProperty("ValidFrom2"), true, ""));					
					filterdef.append("<br>Zeitraum bis<br>" + ocs.getFormManager().getDateField(userSession.getProperty("ValidUntil2"), true, ""));	
					filterdef.append("<br>Status<br>" + ocs.getFormManager().getSelection(userSession.getProperty("Status2"), true, ""));
					
					filterdef.append("</td></tr>");
					
				}
				
				if(userSession.getID("FilterMode")==3 || userSession.getID("FilterMode")==4){
					filterdef.append("<tr><td>Spende mind.</td><td>" + ocs.getFormManager().getTextField(userSession.getProperty("DonationMin"), true, "") + "</td></tr>");
					filterdef.append("<tr><td>Spende max.</td><td>" + ocs.getFormManager().getTextField(userSession.getProperty("DonationMax"), true, "") + "</td></tr>");
					filterdef.append("<tr><td>Spende von</td><td>" + ocs.getFormManager().getDateField(userSession.getProperty("DonationFrom"), true, "") + "</td></tr>");
					filterdef.append("<tr><td>Spende bis.</td><td>" + ocs.getFormManager().getDateField(userSession.getProperty("DonationUntil"), true, "") + "</td></tr>");
				}
				
				
				
				filterdef.append("</table>");	
				
				filterdef.append("</form>");
				
				filterdef.append(" <input type=\"button\" onclick=\"onAction('" + userSession.getPath() + "','querydefinitionsave','','scope=1')\"  value=\"Aktuelle Abfrage speichern\">");
				filterdef.append(" <input type=\"button\" onclick=\"onAction('" + userSession.getPath() + "','querydefinitionadd','','scope=1')\"  value=\"Neue Abfrage speichern\">");
				filterdef.append(" <input type=\"button\" onclick=\"filterList2('" + getName() + "')\"  value=\"Filtern\">");
				
										
				return filterdef.toString();				
				
				
			}
			public String getFilterString(ApplicationContext context){
				
				OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
				

				String filterstring = "";
								
				try{
								
								
								
					String familyname = userSession.getString("FamilyName");
					
					getWebApplication().logAccess(familyname);
					
					familyname = familyname.replace("ue", "ü");				
					familyname = familyname.replace("ü", "(ü|u|ue)");

					familyname = familyname.replace("ae", "ä");		
					familyname = familyname.replace("ä", "(ä|a|ae)");

					familyname = familyname.replace("oe", "ö");		
					familyname = familyname.replace("ö", "(ö|o|oe)");
					
					String operator = " WHERE ";
					if(familyname != null && familyname.length() > 0){
						filterstring += operator + "t3.FamilyName ~* \'.*" + familyname + ".*\'";
						operator = " AND ";
					}
					else{
						familyname = "";	
					}
					
					
				    String firstname = userSession.getString("FirstName");

					firstname = firstname.replace("ue", "ü");
					firstname = firstname.replace("ü", "(ü|u|ue)");

					firstname = firstname.replace("ae", "ä");		
					firstname = firstname.replace("ä", "(ä|a|ae)");

					firstname = firstname.replace("oe", "ö");	
					firstname = firstname.replace("ö", "(ö|o|oe)");
					
					if(firstname != null && firstname.length() > 0){
						filterstring += operator + "t3.FirstName ~* \'.*" + firstname + ".*\'";
						operator = " AND ";
					}
					else{
						firstname = "";	
					}
					String sex = userSession.getString("Sex");
					if(sex != null && sex.length() > 0){
						filterstring += operator + "t3.Sex=" + sex;
						operator = " AND ";
					}
					else{
						sex = "";	
					}
					String dateofbirth = userSession.getString("DateOfBirth");
					if(dateofbirth != null && dateofbirth.length() > 0){
						filterstring += operator + "t3.DateOfBirth ILIKE \'" + dateofbirth + "%\'";
						operator = " AND ";
					}
					else{
						dateofbirth = "";	
					}
					
					String agegroup = userSession.getString("AgeGroup");
					if(agegroup != null && agegroup.length() > 0){
						String[] args = agegroup.split("-");
						int year = DateConverter.getCurrentYear();
						if(args.length==2){
							
							filterstring += operator + "\n((" + year + " - CAST((CASE WHEN t3.DateOfBirth='Frau' THEN '0' WHEN t3.DateOfBirth='19??' THEN '0' WHEN t3.DateOfBirth='????' THEN '0'  WHEN char_length(t3.DateOfBirth)=4 THEN t3.DateOfBirth ELSE '0' END) AS INTEGER) >=" + args[0] + ")";
							filterstring += " AND (" + year + " - CAST((CASE WHEN t3.DateOfBirth='Frau' THEN '0' WHEN t3.DateOfBirth='19??' THEN '0' WHEN t3.DateOfBirth='????' THEN '0'  WHEN char_length(t3.DateOfBirth)=4 THEN t3.DateOfBirth ELSE '0' END) AS INTEGER) <=" + args[1] + "))";
							//+ " AND DATEDIFF(year, dateofbirth, now()) <=" + args[1] + ")";
							operator = " AND ";								
						}
						
						
					}
								
					String zipcode = userSession.getString("ZipCode");
								if(zipcode != null && zipcode.length() > 0){
									filterstring += operator + "t4.ZipCode ILIKE \'" + zipcode + "%\'";
									operator = " AND ";
								}
								else{
									zipcode = "";	
								}
								
					String email = userSession.getString("Email");
								if(email != null && email.length() > 0){
									filterstring += operator + "t9.Value ILIKE \'" + email + "%\'";
									operator = " AND ";
								}
								else{
									email = "";	
								}
								
					String telephone = userSession.getString("Telephone");
					if(telephone != null && telephone.length() > 0){
						filterstring += operator + "(t6.Value ILIKE \'" + telephone + "%\' OR t7.Value ILIKE \'" + telephone + "%\' OR t8.Value ILIKE \'" + telephone + "%\')";
						operator = " AND ";
					}
					else{
						email = "";	
					}
								
					String noemail = userSession.getString("NoEmail");
								if(noemail != null && noemail.length() > 0){
									filterstring += operator + "t9.Value IS NULL";
									operator = " AND ";
								}
								else{
									noemail= "";	
								}
								
					String isuser = userSession.getString("IsUser");
								if(isuser != null && isuser.length() > 0){
									filterstring += operator + "t10.ID > 0";
									operator = " AND ";
								}
								else{
									isuser= "";	
								}
								
					String ismember = userSession.getString("IsMember");
					
					webapp.logAccess("IsMember2 : " + ismember);
					
								if(ismember != null && ismember.length() > 0){
									filterstring += operator + "t11.ID > 0";
									operator = " AND ";
								}
								else{
									ismember = "";	
								}
								
					String issponsor = userSession.getString("IsSponsor");
					if(issponsor != null && issponsor.length() > 0){
						filterstring += operator + "t12.ID > 0";
						operator = " AND ";
					}
					else{
						issponsor = "";	
					}
					
					String ischequereceiver = userSession.getString("IsChequeReceiver");
					if(ischequereceiver != null && ischequereceiver.length() > 0){
						filterstring += operator + "t19.ID > 0";
						operator = " AND ";
					}
					else{
						issponsor = "";	
					}
					String omstatus = userSession.getString("OrganisationMemberStatus");						
					String onlyactive = userSession.getString("OnlyActive");
					String istodelete = userSession.getString("IsToDelete");

					if(omstatus != null && omstatus.length() > 0){
						filterstring += operator + "t1.Status = " + omstatus;
						operator = " AND ";						
					}
					else if(onlyactive != null && onlyactive.length() > 0){
						filterstring += operator + "t1.Status = 1";
						operator = " AND ";						
					}
					else if(istodelete != null && istodelete.length() > 0){
						//operator = " AND ";
					}
					else{
						//filterstring += operator + "t1.Status != 3";
						filterstring += operator + "t1.Status != 5";
						operator = " AND ";
					}
					
					
					String category1 = "" + userSession.getID("Category1");
					
					if(category1.equals("0")){
						category1 = "";
					}
					
					String type1 = "" + userSession.getID("Type1");
					
					if(type1.equals("0")){
						type1 = "";
					}
					
					String status1 = "" + userSession.getID("Status1");
					
					if(status1.equals("0")){
						status1 = "";
					}
					
					String registeredsince = userSession.getString("RegisteredSince");
					String registeredbefore = userSession.getString("RegisteredBefore");  
					if(registeredsince.length() > 0){
						filterstring += operator + " t1.DateCreated >='" + registeredsince + "'";
						operator = " AND ";
					}
					if(registeredbefore.length() > 0){
						filterstring += operator + " t1.DateCreated <='" + registeredbefore + "'";
						operator = " AND ";
					}
					
					
					String validfrom1 = userSession.getString("ValidFrom1");
					String validuntil1 = userSession.getString("ValidUntil1");  
					
					if(category1.length() > 0 || type1.length() > 0 || validfrom1.length() > 0 || validuntil1.length() > 0 || status1.length() > 0){
						filterstring += operator + "\n(SELECT Count(ID) FROM MemberAd WHERE OrganisationMemberID=t1.ID";
						
						if(category1.length() > 0){
							filterstring += " AND Template=" + category1;
						}
						if(type1.length() > 0){
							filterstring += " AND Type=" + type1;
						}
						if(validfrom1.length() > 0){
							filterstring += " AND ValidFrom>='" + validfrom1 + "'";
						}
						if(validuntil1.length() > 0){
							filterstring += " AND ValidUntil<='" + validuntil1 + "'";
						}
						if(status1.length() > 0){
							filterstring += " AND Status=" + status1;
						}
						filterstring += ")>0";
						
					}
					
					String category2 = "" + userSession.getID("Category2");
					
					if(category2.equals("0")){
						category2 = "";
					}
					
					String type2 = "" + userSession.getID("Type2");
					
					if(type2.equals("0")){
						type2 = "";
					}
					
					String status2 = "" + userSession.getID("Status2");
					
					if(status2.equals("0")){
						status2 = "";
					}
					
					String validfrom2 = userSession.getString("ValidFrom2");
					String validuntil2 = userSession.getString("ValidUntil2");  
					
					if(category2.length() > 0 || type2.length() > 0 || validfrom2.length() > 0 || validuntil2.length() > 0 || status2.length() > 0){
						filterstring += operator + "\n(SELECT Count(MemberAdRequest.ID) FROM MemberAdRequest, MemberAd WHERE MemberAdRequest.OrganisationMemberID=t1.ID";
						filterstring += " AND MemberAdRequest.MemberAd=MemberAd.ID"; 
						
						if(category2.length() > 0){
							filterstring += " AND MemberAd.Template=" + category2;
						}
						if(type2.length() > 0){
							filterstring += " AND MemberAd.Type=" + type2;
						}
						if(validfrom2.length() > 0){
							filterstring += " AND MemberAdRequest.ValidFrom>='" + validfrom2 + "'";
						}
						if(validuntil2.length() > 0){
							filterstring += " AND MemberAdRequest.ValidUntil<='" + validuntil2 + "'";
						}
						if(status1.length() > 0){
							filterstring += " AND MemberAdRequest.Status=" + status2;
						}
						filterstring += ")>0";
						
					}
					
					String donationfrom = userSession.getString("DonationFrom");
					String donationuntil = userSession.getString("DonationUntil");
					String donationmin = userSession.getString("DonationMin");
					String donationmax = userSession.getString("DonationMax");
					if(donationfrom.length() > 0 || donationuntil.length() > 0 || donationmin.length() > 0 || donationmax.length() > 0){
						//String subsql = "(SELECT Count(ID) FROM AccountMovement WHERE OrganisationMember=t1.ID AND DebitAccount=2 ";
						String subsql = "(SELECT SUM(Amount) FROM AccountMovement WHERE OrganisationMember=t1.ID AND DebitAccount IN (2,3) ";
						if(donationfrom.length() > 0){
							subsql += " AND Date>='" + donationfrom + "'";	
						}
						if(donationuntil.length() > 0){
							subsql += " AND Date<='" + donationuntil + "'";	
						}
						if(donationmin.length() > 0){
							//subsql += " AND Amount >=" + donationmin;	
						}
						if(donationmax.length() > 0){
							//subsql += " AND Amount <=" + donationmax;	
						}
						//subsql += ") > 0";
						subsql += ") >= 50"; //schneller hack für die Spendenbescheinigung, bessere Lösung finden!
						filterstring += operator + subsql;
						operator = " AND ";
					}
			     }
			     catch(java.lang.Exception e){
			     		 
			     }
			     

			     
			     getWebApplication().logAccess(filterstring);
				
			     return filterstring;
				
			}
			
			
		};
		
		setFilter(filter);
		
		modes = new Vector();
		modes.add(new ConfigValue("1", "1", "Standard"));
		modes.add(new ConfigValue("2", "2", "Erweitert"));	
		modes.add(new ConfigValue("3", "3", "Spenden"));	
		modes.add(new ConfigValue("4", "4", "Erweitert+Spenden"));	
		getProperty("Mode").setSelection(modes);
			
	}
	@Override
	public String getSQL(ApplicationContext context){
		return getSQL(context, true);	
	}
		
	public String getSQL(ApplicationContext context, boolean addlimit){
		
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
		
		String sql = "SELECT t1.ID, t1.ID AS OMID, t1.Status AS OMStatus, t1.DataProtection, t1.DateCreated, t1.DateModified, t1.Comment, t3.Sex, t3.FamilyName, t3.FirstName, t4.AdditionalLine, t4.Street, t4.Number, t4.Zipcode, t4.City, t5.ID AS Login,";
 		sql += " t6.Value AS TelP, t7.Value AS TelB,  t8.Value AS TelM, t9.Value AS Email, t10.ID AS Role1, t11.ID AS Role2, t12.ID AS Role3, t13.ID AS ModificationID, t14.Title AS Organisation, ";
 		sql += " t16.ID AS ParentID, CONCAT(t18.Familyname, ' ' , t18.Firstname) AS ParentName, count(*) OVER() AS total_count";
 		
 		if(userSession != null && userSession.getID("FilterMode") >=3){
 			
 			sql += ", (SELECT Count(ID) FROM AccountMovement WHERE DebitAccount=2 AND OrganisationMember=t1.ID) AS CNT1";
  			sql += ", (SELECT SUM(Amount) FROM AccountMovement WHERE DebitAccount=2 AND OrganisationMember=t1.ID) AS SUM1";
    		sql += ", (SELECT (SUM(Amount) / Count(ID)) FROM AccountMovement WHERE DebitAccount=2 AND OrganisationMember=t1.ID) AS AVG1";
    		sql += ", COALESCE((SELECT Max(Amount) FROM AccountMovement WHERE DebitAccount=2 AND OrganisationMember=t1.ID),0) AS MAX1";
 		}
 		
		sql += " FROM OrganisationMember AS t1";
		sql += " LEFT JOIN Person AS t2 ON t1.Person=t2.ID";
		sql += " LEFT JOIN Identity AS t3 ON t3.PersonID=t2.ID";
		sql += " LEFT JOIN Address AS t4 ON t4.PersonID=t2.ID";
		sql += " LEFT JOIN Login AS t5 ON t5.OrganisationMemberID=t1.ID";
		sql += " LEFT JOIN Contact AS t6 ON t6.PersonID=t2.ID AND t6.Type=0";
		sql += " LEFT JOIN Contact AS t7 ON t7.PersonID=t2.ID AND t7.Type=1";
		sql += " LEFT JOIN Contact AS t8 ON t8.PersonID=t2.ID AND t8.Type=2";
		sql += " LEFT JOIN Contact AS t9 ON t9.PersonID=t2.ID AND t9.Type=3";
		
		sql += " LEFT JOIN MemberRole AS t10 ON t10.OrganisationMemberID=t1.ID AND t10.Role=1 AND t10.Status=0";
		sql += " LEFT JOIN MemberRole AS t11 ON t11.OrganisationMemberID=t1.ID AND t11.Role=2 AND t11.Status=0";
		sql += " LEFT JOIN MemberRole AS t12 ON t12.OrganisationMemberID=t1.ID AND t12.Role=3 AND t12.Status=0";
		sql += " LEFT JOIN MemberRole AS t19 ON t19.OrganisationMemberID=t1.ID AND t19.Role=4 AND t19.Status=0";
		
		sql += " LEFT JOIN OrganisationMemberModification AS t13 ON t13.OrganisationMemberID=t1.ID AND t13.Status=0";
		sql += " LEFT JOIN OrganisationalUnit AS t14 ON t1.OrganisationalUnitID=t14.ID";
		
		sql += " LEFT JOIN OrganisationMemberRelationship AS t15 ON t15.OrganisationMember=t1.ID AND t15.Status=0";
		sql += " LEFT JOIN OrganisationMember AS t16 ON t15.OrganisationMemberID=t16.ID";
		sql += " LEFT JOIN Person AS t17 ON t16.Person=t17.ID";
		sql += " LEFT JOIN Identity AS t18 ON t18.PersonID=t17.ID";
		
		return sql;
			
	}
	public String getToolbar(){
		
		StringBuilder html = new StringBuilder();
		
		//html.append("<input type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.process.OrganisationMemberAdd\')\" value=\"Neues Mitglied\"><img src=\"images/memberadd.png\">");
		html.append("<input type=\"button\" onclick=\"javascript:onAction('" + getPath() + "','organisationmemberadd')\" value=\"Neues Mitglied\"><img src=\"images/memberadd.png\">");
				
		html.append("<input type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.process.BatchActivityCreate\', 'Mode=1')\" value=\"Brief an Erstspender\">");
									
		//html.append("<input type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.process.PDFCreate\')\" value=\"Spendenbest&auml;tigung\">");
		html.append("<input type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.process.BatchActivityCreate\', 'Mode=6')\" value=\"Spendenbscheinigung\">");
									
		//html.append("<input type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.process.CallForDonations\')\" value=\"Spendenaufruf\">");
		html.append("<input type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.process.BatchActivityCreate\', 'Mode=4')\" value=\"Spendenaufruf\">");
									
		html.append("<input type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.process.BatchActivityCreate\', 'Mode=2')\" value=\"Brief Mitglieder bez.\">");
									
		html.append("<input type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.process.BatchActivityCreate\', 'Mode=3')\" value=\"Brief Mitglieder n. bez.\">");
		
		html.append("<input type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.process.BatchActivityCreate\', 'Mode=5')\" value=\"Brief an aktuelle Auswahl\">");
		
		//html.append("<input type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.process.SponsorListCreate\', 'Mode=4')\" value=\"Spendenaufruf Weihnachten 2016\">");
		//html.append("<input type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.process.SponsorListCreate2\', 'Mode=4')\" value=\"Spendenaufruf Februar 2017\">");
		
		html.append("<input type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.process.BatchActivityCreate2\', 'Mode=4')\" value=\"Spendenaufruf Mai 2017\">");
		
		html.append("<input type=\"button\" onclick=\"javascript:onAction(\'" + getPath() + "\', 'listexport')\" value=\"Aktuelle Auswahl exportieren\">");
		
		return html.toString();
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
			html.append("<div class=\"datatable\"  id=\"" + getName() + "_tablearea\">");
		}
		
		getDataTable(html, context);
		
		html.append("</div>");
		
		
		return html.toString();
		
	}
	public ActionResult onAction(BasicClass source, String command, ApplicationContext context) {
		ActionResult result = null;
		if(command.equals("setmode")){
			WebApplicationClient userSession = (WebApplicationClient)context.getObject("usersession");
			String mode = context.getString("mode");
			userSession.setProperty("FilterMode", mode);
			result = new ActionResult(ActionResult.Status.OK, "Filter geladen");
			result.setParam("dataContainer", "listfilter");
			
			FilterParameters filterParameters = null;
			
			if(userSession.get(this.getName()) != null){
				filterParameters = (FilterParameters)userSession.get(this.getName());
			}
			else{
				filterParameters = new FilterParameters(getFilter());
				userSession.put(this.getName(), filterParameters);
			}
			result.setData(getFilter().toHTML(context, filterParameters));
		}
		else if(command.equals("listexport")){
			OpenCommunityServer server = (OpenCommunityServer)getRoot();
			WebApplicationClient userSession = (WebApplicationClient)context.getObject("usersession");
			result = server.startProcess("ch.opencommunity.process.ListExport", userSession, null, context, this);
			
		}
		else if(command.equals("organisationmemberadd")){
			OpenCommunityServer server = (OpenCommunityServer)getRoot();
			WebApplicationClient userSession = (WebApplicationClient)context.getObject("usersession");
			result = server.startProcess("ch.opencommunity.process.OrganisationMemberAdd", userSession, null, context, this);

		}
		return result;
	}
	
	
	
}
class BooleanColumnDefinition extends HTMLColumnDefinition{
	
	public BooleanColumnDefinition(String dataItem, String columnLabel){
		super(dataItem, columnLabel);
	}
	public String getRecordValue(BasicClass record){
		if(record.getID(getDataItem()) > 0){
			return "JA";	
		}
		else{
			return "";
		}
	}
	
}