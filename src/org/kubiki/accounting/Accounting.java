package org.kubiki.accounting;


import ch.opencommunity.base.*;
import ch.opencommunity.server.*;

import org.kubiki.base.*;
import org.kubiki.application.ApplicationContext;
import org.kubiki.util.DateConverter;

import java.util.Vector;
import java.util.Date;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Accounting extends BasicClass{

	OpenCommunityServer ocs;
	
	org.kubiki.gui.html.HTMLFormManager form;

	public Accounting(OpenCommunityServer ocs){ 
		this.ocs = ocs;
		
		getProperty("name").setHidden(true);
		
		form = ocs.getFormManager();
		
		addProperty("OrganisationMember", "Integer", "", false, "Mitglied/Benutzer");
		addProperty("Year", "Integer", "", false, "Jahr");
		
		addObjectCollection("Account", "ch.opencommunity.accounting.Account");
	
	}
	public void initObjectLocal(){
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		
		getProperty("OrganisationMember").setSelection(ocs.getRecipientList());
		
		Vector years = new Vector();
		years.add(new ConfigValue("2018", "2018", "2018"));
		years.add(new ConfigValue("2017", "2017", "2017"));
		years.add(new ConfigValue("2016", "2016", "2016"));
		years.add(new ConfigValue("2015", "2015", "2015"));
		years.add(new ConfigValue("2014", "2014", "2014"));
		years.add(new ConfigValue("2013", "2013", "2013"));
		getProperty("Year").setSelection(years);
		
	}
	public String getAccountList(ApplicationContext context){
		
		getProperty("OrganisationMember").setSelection(ocs.getRecipientList());
		
		
		StringBuilder html = new StringBuilder();

		
		html.append("<div id=\"accountlist\">");
		html.append("<b>Konten</b>");	
		html.append("<table><tr><th>Kontennummer</th><th>Bezeichnung</th><tr>");
		
		int odd = -1;
		for(BasicClass account : getObjects("Account")){
			if(odd==1){
				html.append("<tr class=\"odd\">");			
			}
			else{
				html.append("<tr class=\"even\">");
			}
			odd = -odd;
			html.append("<td>" + account.getString("AccountNumber") + "</td>");
			html.append("<td>" + account.getString("Title") + "</td>");
			html.append("<td><a href=\"javascript:getAccountMovementList(" + account.getName() + ")\">Zahlungen anzeigen</a></td>");
		}
		html.append("<tr><td></td><td></td><td><a href=\"javascript:getAccountMovementList()\">Alle Zahlungen anzeigen</a></td></tr>");
		html.append("</table>");
		
		html.append("<b>Filter</b>");	
		
		html.append("<form id=\"accountingfilter\" name=\"accountingfilter\">");
		html.append("<table>");
		
		form.getFormBody(html, this, context);
		
		
		html.append("</table>");
		html.append("</form>");
		
		html.append("</div>");

		html.append("<div id=\"accountmovements\">");
		html.append("<b>Buchungen</b>");		
		html.append(getAccountMovementList(context) + "</div>");
	    
		

		return html.toString();
	}
	public String getAccountMovementList(ApplicationContext context){
	//public String getAccountMovementList(String accountid, int start){
	
		int[] colwidth ={100,100,100,100,100,200,100, 100};
	
		String accountid = context.getString("accountid");
								
		int start = 0;
		String sStart = context.getString("start");
		if(sStart != null){
			start = Integer.parseInt(sStart);	
		}
		
		String omid = context.getString("OrganisationMember");
		String year = context.getString("Year");
		
		StringBuilder html = new StringBuilder();
		
		Vector filter1 = new Vector();
		
		ObjectCollection results = new ObjectCollection("Results", "*");
		String sql = "SELECT t1.ID, t1.Date , t1.Valuta, t1.Description, t1.Amount, t2.Title AS DebitAccount, t3.Title AS CreditAccount, t6.FamilyName, t6.FirstName";
		sql += " FROM AccountMovement AS t1";
		sql += " LEFT JOIN Account AS t2 ON t1.DebitAccount=t2.ID";
		sql += " LEFT JOIN Account AS t3 ON t1.CreditAccount=t3.ID";
		sql += " LEFT JOIN OrganisationMember AS t4 ON t1.OrganisationMember=t4.ID";
		sql += " LEFT JOIN Person AS t5 ON t4.Person=t5.ID";
		sql += " LEFT JOIN Identity AS t6 ON t6.PersonID=t5.ID";
		
		String operator = " WHERE";
		
		if(accountid != null){
			sql += operator + " (t1.DebitAccount=" + accountid + " OR t1.CreditAccount=" + accountid + ")";
			operator = " AND";
		}
		if(year != null && year.length() > 0){
			sql += operator + " extract(year from t1.date)=" + year;
			operator = " AND";
		}
		if(omid != null && omid.length() > 0){
			sql += operator + " t1.OrganisationMember=" + omid;
			operator = " AND";
		}
		
		sql += " ORDER BY t1.Date DESC";
		ocs.queryData(sql , results);
		
		html.append("<div id=\"accountmovementlistheader\">");
		
		html.append("<table><tr>");
		html.append("<th style=\"width : " + colwidth[0] + "px\">Datum</th>");
		html.append("<th style=\"width : " + colwidth[1] + "px\">Valuta</th>");
		html.append("<th style=\"width : " + colwidth[2] + "px\">Debit</th>");
		html.append("<th style=\"width : " + colwidth[3] + "px\">Credit</th>");
		html.append("<th style=\"width : " + colwidth[4] + "px\">Betrag</th>");
		html.append("<th style=\"width : " + colwidth[5] + "px\">Buchungstext</th>");
		html.append("<th style=\"width : " + colwidth[6] + "px\">Name</th>");
		html.append("<th style=\"width : " + colwidth[7] + "px\">Vorname</th>");
		html.append("</tr></table>");
		
		html.append("</div>");
		
		html.append("<div id=\"accountmovementlist\">");
		
		html.append("<table>");

		int odd = -1;
		
		double total = 0;
		
		double totalm = 0;
		
		int cnt = 0;
		
		String date = "";
		String prevdate = "";
		String prevmonth = "";
		String[] args = null;
		
		DateFormat format = new SimpleDateFormat("MMMM yyyy");
		
		for(BasicClass record : results.getObjects()){
			
			date = record.getString("DATE");
			args = date.split("-");
			
			if(cnt > 0){
				
				if(!prevmonth.equals(args[1])){
					
					
			
					
					Date d = DateConverter.sqlToDate(prevdate); 
					
					
					html.append("<tr><td><b>Total " + format.format(d) + "</b></td><td>" + totalm + "</td></tr>");
					
					
					totalm = 0;
				
				
				}
			}
			
			
			if(odd==1){
				html.append("<tr class=\"odd\">");			
			}
			else{
				html.append("<tr class=\"even\">");
			}
			odd = -odd;	
			

			
			html.append("<td style=\"width : " + colwidth[0] + "px\">" + record.getString("DATE") + "</td>");
			html.append("<td style=\"width : " + colwidth[1] + "px\">" + record.getString("VALUTA") + "</td>");
			html.append("<td style=\"width : " + colwidth[2] + "px\">" + record.getString("DEBITACCOUNT") + "</td>");
			html.append("<td style=\"width : " + colwidth[3] + "px\">" + record.getString("CREDITACCOUNT") + "</td>");
			html.append("<td style=\"width : " + colwidth[4] + "px\">" + record.getString("AMOUNT") + "</td>");
			
			total += (record.getDouble("AMOUNT")).doubleValue();
			totalm += (record.getDouble("AMOUNT")).doubleValue();
			
			String description = record.getString("DESCRIPTION");
			html.append("<td style=\"width : " + colwidth[5] + "px\">" + description + "</td>");
			
			
			html.append("<td style=\"width : " + colwidth[6] + "px\">" + record.getString("FAMILYNAME") + "</td>");
			html.append("<td style=\"width : " + colwidth[7] + "px\">" + record.getString("FIRSTNAME") + "</td>");
			html.append("<td><a href=\"javascript:createProcess('ch.opencommunity.process.AccountMovementAdd', 'ID=" + record.getString("ID") + "')\">Bearbeiten</a></td>");
			html.append("</tr>");
		
			prevmonth = args[1];
			prevdate = date;

			
			cnt++;
			
		}
		
		html.append("</table>");
		
		html.append("</div>");
		
		html.append("<div id=\"accountmovementlistfooter\">");
		html.append("Total : " + total);
		html.append("</div>");
		return html.toString();	
	}
}
