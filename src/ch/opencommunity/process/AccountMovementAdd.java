 package ch.opencommunity.process;
 
 import ch.opencommunity.server.*;
 import ch.opencommunity.base.*;

 
 import org.kubiki.ide.*;
 import org.kubiki.base.Property;
 import org.kubiki.base.ProcessResult;
 
 import org.kubiki.application.*;
 
 import org.kubiki.accounting.*;
 
 
 public class AccountMovementAdd extends BasicProcess{
 
	BasicProcessNode node1;
	AccountMovement accountMovement = null;
 
	public AccountMovementAdd(){
		node1 = addNode();
		
		Property p = addProperty("Date", "Date", "", false, "Datum");
		node1.addProperty(p);
		
		p = addProperty("Valuta", "Date", "", false, "Valuta");
		node1.addProperty(p);
		
		p = addProperty("DebitAccount", "Integer", "", false, "Debit");
		node1.addProperty(p);
		
		p = addProperty("CreditAccount", "Integer", "", false, "Credit");
		node1.addProperty(p);
		
		p = addProperty("Amount", "Double", "", false, "Betrag");
		node1.addProperty(p);
		
		p = addProperty("Description", "Text", "", false, "Buchungstext");
		node1.addProperty(p);
		
		p = addProperty("OrganisationMember", "Integer", "", false, "Benutzer/Mitglied/Gönner");
		node1.addProperty(p);
		
		p = addProperty("ID", "String", "");
		//node1.addProperty(p);
		
		setCurrentNode(node1);		
	}
	
	public void initProcess(){
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		Accounting accounting = ocs.getAccounting();	
		
		getProperty("DebitAccount").setSelection(accounting.getObjects("Account"));	
		getProperty("CreditAccount").setSelection(accounting.getObjects("Account"));
		
		getProperty("OrganisationMember").setSelection(ocs.getRecipientList());
		
		if(getString("ID").length() > 0){
			accountMovement = (AccountMovement)ocs.getObject(null, "AccountMovement", "ID", getString("ID"));
			if(accountMovement != null){
				setProperty("Date", accountMovement.getString("Date"));
				setProperty("Valuta", accountMovement.getString("Valuta"));
				setProperty("DebitAccount", accountMovement.getString("DebitAccount"));
				setProperty("CreditAccount", accountMovement.getString("CreditAccount"));
				setProperty("Amount", accountMovement.getString("Amount"));
				setProperty("Description", accountMovement.getString("Description"));
				setProperty("OrganisationMember", accountMovement.getString("OrganisationMember"));
			}
		}
	}
	
	public void finish(ProcessResult result, ApplicationContext context){
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		Accounting accounting = ocs.getAccounting();
		
		if(accountMovement != null){
			accountMovement.mergeProperties(this);
			accountMovement.setName(getString("ID"));
			ocs.updateObject(accountMovement);			
		}
		else{
		
			accountMovement = new AccountMovement();
			accountMovement.mergeProperties(this);
			String id = ocs.insertObject(accountMovement);
			
		}

		result.setParam("refresh", "accounting");
		
	}
}
