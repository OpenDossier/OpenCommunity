package ch.opencommunity.common;

import ch.opencommunity.server.OpenCommunityServer;
import ch.opencommunity.base.OrganisationMember;

import org.kubiki.base.ObjectCollection;
import org.kubiki.base.BasicClass;
import org.kubiki.application.ApplicationContext;
import org.kubiki.util.DateConverter;

import java.util.Map;

public class AddressList{
	
	public static String getAddressList(OpenCommunityServer ocs, OrganisationMember om, Map addpars, ApplicationContext context){
		
		return getAddressList(ocs, om, addpars, context, false);
	}
	
	public static String getAddressList(OpenCommunityServer ocs, OrganisationMember om, Map addpars, ApplicationContext context, boolean ispdf){
		
		StringBuilder html = new StringBuilder();
				

				
		html.append("<p class=\"sectionheader\"><img src=\"images/kontakte_aktiv.png\"></img>meine Kontakte" + "</p>");
		if(!ispdf){	
			html.append("<div style=\"text-align : right;\"><input type=\"button\" onclick=\"getContactPrintList()\" value=\"Kontakte drucken\" style=\"border : 0px; background : #F58423; color : black;\"></div>");
		}
				
				//html.append("<select class=\"selectsmall\" style=\"width : 140px; float : right;\" onchange=\"sortProfileSection(this.value)\">");
				//html.append("<option value=\"\">Sortieren</option>");
				//html.append("<option value=\"validfrom\">Nach Ablaufdatum</option>");
				//html.append("<option value=\"category\">Nach Rubrik</option>");
				//html.append("</select></p>");
				
				ObjectCollection results = new ObjectCollection("Results", "*");
				
				String sql = "SELECT t1.*,  t2.Title AS MemberAdTitle, t2.Description, t3.ID AS MAC, t3.Title AS Category, t3.Protected, t6.FamilyName, t6.FirstName, t6.Sex, t6.DateOfBirth, t7.Street, t7.Number, t7.ZipCode, t7.City, t8.Value AS Phonehome, t9.Value AS Email, t10.Value AS Mobile,";
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
				
				sql += " WHERE t1.OrganisationMemberID=" + om.getName();
				
				sql += " AND t1.Status IN (1,2)";
				
				sql += " AND t1.ValidUntil >= Now()"; //AK 20161115
				
				sql += " AND t4.Status=1"; //AK 20161205
				
				
				if(addpars.get("criteria") != null){
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
				
				int cnt = 0;
				
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
					
					if(addpars.get("reload")==null){
						include = true;
					}
					else if(addpars.get(category)!=null && addpars.get(location)!=null){
						include = true;						
					}
					
					/*
					if(criteria1.indexOf(category)==-1){
						criteria1.add(category);	
					}
					if(criteria2.indexOf(location)==-1){
						criteria2.add(location);	
					}
					*/
						
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
					
					boolean isProtected = record.getBoolean("PROTECTED");
					
					if(include){
						
						boolean managedaccount = false;
						if(familyname2 != null && familyname2.length() > 0){
							managedaccount = true;
						}
						
						
						if(!prevcategory.equals(category)){
							if(cnt==0){
								html.append("\n<div class=\"searchresultheader\" style=\"height : 32px; margin-top : 10px;\">");								
							}
							else{
								html.append("\n<div class=\"searchresultheader\" style=\"height : 32px;\">");
							}
							html.append("<img src=\"res/icons/" + record.getString("MAC") + "_weiss.png\"></img> " + category);
							html.append("</div>");	
							
							cnt++;
						}
						else{
							html.append("<div style=\"border-top : 1px solid black; margin-top : 20px;\"></div>");
						}
						
						if(isProtected){
							
							html.append("\n<table style=\"width : 536px; margin-left: 4px; margin-top : 20px;\">");
							
							String phone = mobile;
							if(phone.isEmpty()){
								phone = phonehome;
							}
							
							html.append("<tr>");						
							html.append("<td style=\" width : 200px;\">" + firstname + "</td>");
							html.append("<td>Tel.: " + phone + "</td>");											
							html.append("</tr>");
							
						}
						else{
							html.append("\n<table style=\"width : 536px; margin-left: 4px; margin-top : 20px;\">");
							
							html.append("<tr>");						
							html.append("<td style=\" width : 200px;\">" + firstname + " " + familyname + "</td>");
							html.append("<td>Tel.P: " + phonehome + "</td>");											
							html.append("</tr>");
							
							html.append("<tr>");
							html.append("<td>" + street + " " + number + "</td>");
							html.append("<td>Tel.M: " + mobile + "</td>");		
							html.append("</tr>");
							
							html.append("<tr>");
							html.append("<td>" + zipcode + " " + city + "</td>");
							html.append("<td>" + email + "</td>");
							html.append("</tr>");
							
						}
						if(!ispdf){
							html.append("<tr><td>&nbsp;</td></tr>");
						}	
						html.append("\n<tr><td  colspan=\"2\" style=\"color : #6CBB16;\">" + sex2 + " (" + age + ") " + record.getString("MEMBERADTITLE") + "</td></tr>");
							
						html.append("\n<tr><td  colspan=\"2\" style=\"color : white;\">" + record.getString("DESCRIPTION") + "</td></tr>");
						
						if(!ispdf){
							html.append("<tr><td>&nbsp;</td></tr>");
						}
							
						html.append("\n<tr>");
							
						html.append("\n<td>Bestellt am: " + DateConverter.sqlToShortDisplay(record.getString("DATECREATED"), false) + "<br/>Sichtbar bis: " +  DateConverter.sqlToShortDisplay(record.getString("VALIDUNTIL"), false) + "</td>");
							
						html.append("<td style=\"text-align : right; vertical-align : bottom;\">");
						
						if(!ispdf){
							html.append("<a class=\"objectaction\" href=\"javascript:createProcess(\'ch.opencommunity.process.FeedbackCreate\',\'MemberAdRequestID=" + record.getString("ID") + "&Mode=1&DeleteContact=true\')\"><img src=\"res/icons/loeschen_orange.png\">löschen</a>");
						}	
						html.append("</td>");
							
						html.append("</tr>");
							
						html.append("</table>");
						
						
						/*
						
						html.append("<div id=\"" + record.getString("ID") + "\" class=\"searchresult3\" onclick=\"getmemberadrequestdetail(" + record.getString("ID") + ")\">");
						html.append("<table>");
						html.append("<tr>");
						html.append("<td class=\"searchresultcell2\">");
						html.append(record.getString("CATEGORY"));
						
						html.append("<br><span class=\"green\">" + firstname + " " + familyname +"</span>");
						
						if(!managedaccount){
							html.append(", " + street + " " + number + ", " + zipcode + " " + city);
						}
						
						html.append("<br>" + sex2 + " (" + age + ") " + record.getString("MEMBERADTITLE")); 
						html.append("<br>" + record.getString("DESCRIPTION"));
						
						if(!managedaccount){

							if(phonehome.length() > 0 || mobile.length() > 0){
								html.append("<br>");
								if(phonehome.length() > 0){
									html.append("Telephon(P) : " + phonehome + " ");
								}
								if(mobile.length() > 0){
									html.append("Telephon(M) : " + mobile + " ");
								}
							}
							if(email.length() > 0){
								html.append("<br>Email : " + email);
							}
							
						}
						html.append("<br>Sichtbar bis " + DateConverter.sqlToShortDisplay(record.getString("VALIDUNTIL"), false));
						
						if(familyname2 != null && familyname2.length() > 0){ //Betreutes Profil
							html.append("<p>Für die Kontaktaufnahme wenden Sie sich bitte an:");
							html.append("<br><b>" + firstname2 + " " + familyname2 + "</b>");
							html.append("<br>" + street2 + " " + number2 + "," + zipcode2 + " " + city2);
							
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
						
						
						html.append("</td><td>");
						
						*/
						
						/* Auf Wunsch des Kunden ausgeblendet
						if(record.getInt("STATUS")==1){
							html.append("<a class=\"objectaction\" href=\"javascript:activateContact(" + record.getString("ID") + ")\">auf \"kontaktiert\" setzen</a><br>");
						}
						else if(record.getInt("STATUS")==2){
							html.append("<span style=\"color : green\">kontaktiert</span><br>");
						}
						*/
						
						
						//html.append("<a class=\"objectaction\" href=\"javascript:createProcess(\'ch.opencommunity.process.FeedbackCreate\',\'MemberAdRequestID=" + record.getString("ID") + "&Mode=2&DeleteContact=true\')\">löschen</a>");
						/*
						html.append("<a class=\"objectaction\" href=\"javascript:createProcess(\'ch.opencommunity.process.FeedbackCreate\',\'MemberAdRequestID=" + record.getString("ID") + "&Mode=1&DeleteContact=true\')\">löschen</a>");
						html.append("</td>");
						html.append("<td>");
						html.append("</tr>");
						html.append("</table>");
						html.append("</div>");	
						*/
						
						prevcategory = category;
						prevzipcode = zipcode;
					}

				}
		html.append("<div style=\"border-top : 1px solid black; margin-bottom : 14px; margin-top : 10px;\"></div>");
		if(!ispdf){
			html.append("<div style=\"text-align : right;\"><input type=\"button\" onclick=\"getContactPrintList()\" value=\"Kontakte drucken\" style=\"border : 0px; background : #F58423; color : black;\"></div>");
		}
		
		return html.toString();
		
		
	}
	
	
	
}