package ch.opencommunity.process;

import ch.opencommunity.server.OpenCommunityServer;
import ch.opencommunity.common.OpenCommunityUserSession;

import org.kubiki.ide.BasicProcess;
import org.kubiki.base.Property;
import org.kubiki.base.ProcessResult;
import org.kubiki.base.ObjectCollection;
import org.kubiki.database.Record;
import org.kubiki.application.ApplicationContext;

import org.kubiki.util.DateConverter;

import org.kubiki.accounting.Cheque;

import java.util.Vector;

import java.sql.Connection;
import java.sql.Statement;

public class ChequeCash extends BasicProcess{
	
	Vector<String> ids = null;
	
	public ChequeCash(){
		addNode(this);
		
		Property p = addProperty("ChequeIDs", "String", "", false, "Serien-Nummern");
		p.setEditable(false);
		addProperty("OrganisationMember", "Integer", "", false, "Benutzer/Mitglied/Gönner");
		setCurrentNode(this);
		
		
	}
	
	public void initProcess(ApplicationContext context){
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();	
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");	
		
		if(getString("ChequeIDs").length()==0){
			setProperty("ChequeIDs", userSession.getString("ChequeIDs"));
		}
		
		String chequeids = getString("ChequeIDs");
		
		ids = new Vector<String>();
		
		String[] args = chequeids.split(",");
		for(String arg : args){
			if(arg.indexOf("-") > -1){
				try{
					String[] args2 = arg.split("-");
					if(args2.length==2){
						int lim1 = Integer.parseInt(args2[0].trim());	
						int lim2 = Integer.parseInt(args2[1].trim());	
						for(int i = lim1; i <= lim2; i++){
							ids.add("" + i);	
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
		
		getProperty("OrganisationMember").setSelection(ocs.getRecipientList());
		
		/*
		ObjectCollection results = new ObjectCollection("Result", "*");
		ocs.queryData("SELECT Max(ID) AS MAXID FROM Cheque", results);
		if(results.getObjects().size()==1){
			Record record = (Record)results.getObjects().get(0);
			setProperty("CurrentSerialNumber", record.getString("MAXID"));
			
		}
		*/

	}
	public boolean validate(ApplicationContext context){
		
		if(getID("OrganisationMember")==0){
			return false;	
		}
		else if(getString("ChequeIDs").length()==0){
			return false;
		}
		else{
			return true;	
		}
		
	}
	public void finish(ProcessResult result, ApplicationContext context){
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		try{
			Connection con = ocs.getConnection();
			Statement stmt = con.createStatement();
			for(String id : ids){
				stmt.execute("UPDATE Cheque SET DateCashed= Now(), OrganisationMemberCashed=" + getID("OrganisationMember") + " WHERE (OrganisationMemberCashed IS NULL OR OrganisationMemberCashed=0) AND ID=" + id); //
			}
			stmt.close();
			con.close();
		}
		catch(java.lang.Exception e){
			ocs.logException(e);	
		}
		result.setParam("refresh", "cheques");
	}
}