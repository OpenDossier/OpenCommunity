package ch.opencommunity.process;

import ch.opencommunity.server.OpenCommunityServer;

import org.kubiki.ide.BasicProcess;
import org.kubiki.base.Property;
import org.kubiki.base.ProcessResult;
import org.kubiki.base.ObjectCollection;
import org.kubiki.database.Record;
import org.kubiki.application.ApplicationContext;

import org.kubiki.util.DateConverter;

import org.kubiki.accounting.Cheque;

public class ChequeCreate extends BasicProcess{
	
	public ChequeCreate(){
		addNode(this);
		
		addProperty("OrganisationMember", "Integer", "", false, "Benutzer/Mitglied/Gönner");
		
		Property p = addProperty("CurrentSerialNumber", "Integer", "", false, "Letzte verwendete Seriennummer");
		p.setEditable(false);
		
		addProperty("DateValuta", "Date", "2018-01-01", false, "Valuta");
		
		addProperty("NumberOfCheques", "Integer", "", false, "Anzahl Cheques");
		
		addProperty("Amount", "Integer", "10", false, "Wert Cheque");
		
		setCurrentNode(this);
		
	}
	
	public void initProcess(){
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();	
		
		getProperty("OrganisationMember").setSelection(ocs.getRecipientList("4"));
		
		ObjectCollection results = new ObjectCollection("Result", "*");
		ocs.queryData("SELECT Max(ID) AS MAXID FROM Cheque", results);
		if(results.getObjects().size()==1){
			Record record = (Record)results.getObjects().get(0);
			setProperty("CurrentSerialNumber", record.getString("MAXID"));
			
		}

	}
	public boolean validate(ApplicationContext context){
		
		if(getID("NumberOfCheques")==0){
			return false;	
		}
		else if(getID("NumberOfCheques") > 54){
			return false;	
		}
		else if(getID("OrganisationMember")==0){
			return false;	
		}
		else{
			return true;	
		}
		
	}
	
	public void finish(ProcessResult result, ApplicationContext context){
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		
		for(int i = 0; i < getID("NumberOfCheques"); i++){
		
			Cheque cheque = (Cheque)ocs.createObject("org.kubiki.accounting.Cheque", null, context);
			cheque.setProperty("OrganisationMemberIssued", getID("OrganisationMember"));
			cheque.setProperty("DateIssued", DateConverter.dateToSQL(new java.util.Date(), true));
			cheque.setProperty("DateValuta", getString("DateValuta"));
			cheque.setProperty("Amount", getString("Amount"));
			String id = ocs.insertObject(cheque);
			ocs.executeCommand("UPDATE Cheque SET SerialNumber= CAST(ID as int8) WHERE ID=" + id); //
		}

		result.setParam("refresh", "cheques");
	}
	
}