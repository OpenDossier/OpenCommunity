package ch.opencommunity.view;

import ch.opencommunity.server.OpenCommunityServer;
import ch.opencommunity.common.OpenCommunityUserSession;

import org.kubiki.base.ObjectCollection;
import org.kubiki.base.BasicClass;
import org.kubiki.application.Application;
import org.kubiki.application.ApplicationContext;
import org.kubiki.util.DateConverter;

import org.kubiki.servlet.WebApplication;

import org.kubiki.gui.FilterParameters;
import org.kubiki.gui.html.HTMLQueryList;
import org.kubiki.gui.html.HTMLColumnDefinition;
import org.kubiki.gui.html.HTMLQueryListFilter;

import java.util.HashMap;
import java.util.Vector;


public class ChequeList extends HTMLQueryList{

	
	public ChequeList(WebApplication webapp){
		
		super(webapp);
		
		limit = 50;
		
		setID("chequeList");
		
		//defaultOrder = " ORDER BY t1.DateCashed DESC, t1.DateIssued DESC";
		defaultOrder = " ORDER BY t1.ID ASC";
		
		String sql = "SELECT t1.ID, t1.DateIssued, t1.DateValuta, t1.DateCashed, (t4.Familyname  || ' ' || t4.FirstName) AS OM1, (t7.Familyname  || ' ' || t7.FirstName) AS OM2";
		sql += " FROM Cheque AS t1";
		sql += " LEFT JOIN OrganisationMember AS t2 ON t1.OrganisationMemberIssued=t2.ID";
		sql += " LEFT JOIN Person AS t3 ON t2.Person=t3.ID";
		sql += " LEFT JOIN Identity AS t4 ON t4.PersonID=t3.ID";
		sql += " LEFT JOIN OrganisationMember AS t5 ON t1.OrganisationMemberCashed=t5.ID";
		sql += " LEFT JOIN Person AS t6 ON t5.Person=t6.ID";
		sql += " LEFT JOIN Identity AS t7 ON t7.PersonID=t6.ID";
		
		
		setSQL(sql);
		
		addColumnDefinition("ID", "ID", null, 0);
		addColumnDefinition("OM1", "Person ausgestellt", null, 0);
		addColumnDefinition("DATEISSUED", "Datum ausgestellt");
		addColumnDefinition("DATEVALUTA", "Valuta");
		addColumnDefinition("OM2", "Person eingelöst");
		addColumnDefinition("DATECASHED", "Datum eingelöst");
		
		String[] editCommand = new String[2];
		editCommand[0] = "<a href=\"javascript:editCheque(";
		editCommand[1] = ")\">Cheque einlösen</a>";
		
		HTMLColumnDefinition columnDefinition = new HTMLColumnDefinition("ID", "");
		columnDefinition.setCommand(editCommand);
		addColumnDefinition(columnDefinition);
		
		HTMLQueryListFilter filter = new HTMLQueryListFilter(){
			
			public String toHTML(ApplicationContext context, FilterParameters parameters){
				
				OpenCommunityServer ocs = (OpenCommunityServer)getWebApplication();
				OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
				
				StringBuilder filterdef = new StringBuilder();
				
				filterdef.append("<form name=\"filter_chequeList\" id=\"filter_chequeList\">Filtern nach : ");
				filterdef.append(" Cheque-Nr. " + ocs.getFormManager().getTextField(userSession.getProperty("ChequeIDs"), true, "", null, null, "onkeyup=\"filterList2('" + getName() + "')\")"));
				filterdef.append(" <input type=\"button\" onclick=\"filterList2('" + getName() + "')\"  value=\"Filtern\">");
				filterdef.append("</form>");

				
				return filterdef.toString();				
			}
			public String getFilterString(ApplicationContext context){
				
				OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");

				String filterstring = "";
				
				OpenCommunityServer ocs = (OpenCommunityServer)getWebApplication();
				
				try{
								
								
								
					String chequeids = userSession.getString("ChequeIDs");
					
					Vector ids = new Vector();
					
					String[] args = chequeids.split(",");
					for(String arg : args){
						if(arg.indexOf("-") > -1){
							try{
								String[] args2 = arg.split("-");
								if(args2.length==2){
									int lim1 = Integer.parseInt(args2[0].trim());	
									int lim2 = Integer.parseInt(args2[1].trim());	
									for(int i = lim1; i <= lim2; i++){
										ids.add(i);	
									}
								}
								
							}
							catch(java.lang.Exception e){
								
							}
						}
						else{
							ids.add(arg);
						}
					}
					
					
					
					String idlist = "";
					for(int i= 0; i < ids.size(); i++){
						if(i < ids.size()-1){
							idlist += ids.get(i) + ",";
						}
						else{
							idlist += ids.get(i);
						}
					}
					
					String operator = " WHERE ";
					if(chequeids != null && chequeids.length() > 0){
						filterstring += operator + "t1.ID IN (" + idlist + ")";
						operator = " AND ";
					}
					else{
						chequeids = "";	
					}
					
					/*
					if(context.hasProperty("OrganisationMemberID")){
						filterstring +=  operator + " OrganisationMemberIssued=" + context.getString("OrganisationMemberID");
						
					}
					*/
					String organisationMemberIssued = userSession.getString("OrganisationMemberIssued");
					if(organisationMemberIssued.length() > 0){
						filterstring +=  operator + " OrganisationMemberIssued=" + organisationMemberIssued;
						operator = " AND ";
						
					}
					
					filterstring += operator + " extract(year from t1.DateValuta)=" + userSession.getSelectedYear();
					
					
					
					ocs.logAccess(filterstring);
					
				
			    }
			    catch(java.lang.Exception e){
			    	ocs.logException(e);
			     		 
			    }
			    

				
			    return filterstring;
				
			}
		};
		
		setFilter(filter);
		
	}
	public String getToolbar(){
		
		StringBuilder html = new StringBuilder();
		
		html.append("<input type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.process.ChequeCreate\')\" value=\"Nachbarcheque erfassen\">");
		html.append("<input type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.process.ChequePrint\')\" value=\"Cheques ausdrucken\">");
		html.append("<input type=\"button\" onclick=\"editCheque()\"  value=\"Selektion einlösen\"></form>");
		
		return html.toString();		
		
	}
	
}