 package ch.opencommunity.process;
 
 import ch.opencommunity.server.*;
 import ch.opencommunity.base.*;

 
 import org.kubiki.ide.*;
 import org.kubiki.base.Property;
 import org.kubiki.base.ConfigValue;
 import org.kubiki.base.ProcessResult;
 import org.kubiki.base.ObjectCollection;
 import org.kubiki.util.DateConverter;
 
 import org.kubiki.application.ApplicationContext;
 
 import org.kubiki.accounting.*;
 
 import java.util.Vector;
 import java.util.Date;
 
 
 public class DonationAdd extends BasicProcess{
 
	BasicProcessNode node1;
 
	public DonationAdd(){
		node1 = new BasicProcessNode(){
			public boolean validate(ApplicationContext context){
				return getProcess().validate(context);	
			}
		};
		
		addNode(node1);
		
		Property p = addProperty("Date", "Date", "", false, "Datum");
		node1.addProperty(p);
		
		p = addProperty("OrganisationMember", "Integer", "", false, "Benutzer/Mitglied/Gönner");
		node1.addProperty(p);
		
		p = addProperty("DebitAccount", "Integer", "", false, "Debit");
		//node1.addProperty(p);
		
		p = addProperty("CreditAccount", "Integer", "1", false, "Credit");
		//node1.addProperty(p);
		
		p = addProperty("AmountMembership", "Double", "", false, "Betrag Mitgliedschaft");
		node1.addProperty(p);
		
		p = addProperty("AmountDonation", "Double", "", false, "Betrag Spende");
		node1.addProperty(p);
		
		p = addProperty("Year", "String", "2018", false, "Jahr");
		node1.addProperty(p);
		
		p = addProperty("Comment", "Text", "", false, "Bemerkung");
		node1.addProperty(p);
		
		
		setCurrentNode(node1);		
	}
	
	public boolean validate(ApplicationContext context){
		
		if(getID("OrganisationMember") < 1){
			node1.setComment("Wählen Sie einen Benutzer/Mitglied/Gönner aus");
			return false;
		}		
		else if(getString("AmountMembership").length() > 0){
			
			OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
			ObjectCollection results = new ObjectCollection("Results", "*");
			String sql = "SELECT * FROM AccountMovement WHERE DebitAccount=3 AND OrganisationMember=" + getID("OrganisationMember") + " AND extract(year from date)=" + getString("Year");
			ocs.queryData(sql, results);
			if(results.getObjects().size() > 0){
				node1.setComment("Für dieses Jahr wurde bereits ein Mitgliederbeitrag erfasst");
				return false;
			}
			else{
				return true;	
			}
			
		}
		else{
		
			return true;	
			
		}
	}
	public void initProcess(){
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		Accounting accounting = ocs.getAccounting();	
		
		getProperty("DebitAccount").setSelection(accounting.getObjects("Account"));	
		getProperty("CreditAccount").setSelection(accounting.getObjects("Account"));
		
		getProperty("OrganisationMember").setSelection(ocs.getRecipientList());
		
		Vector years = new Vector();
		years.add(new ConfigValue("2018", "2018", "2018"));
		years.add(new ConfigValue("2017", "2017", "2017"));
		years.add(new ConfigValue("2016", "2016", "2016"));
		years.add(new ConfigValue("2015", "2015", "2015"));
		years.add(new ConfigValue("2014", "2014", "2014"));
		getProperty("Year").setSelection(years);
	}
	
	public void finish(ProcessResult result, ApplicationContext context){
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		Accounting accounting = ocs.getAccounting();
		
		if(getString("AmountMembership").length() > 0){ 
			AccountMovement accountMovement = new AccountMovement();
			accountMovement.mergeProperties(this);
			accountMovement.setProperty("Description", "Mitgliedschaft " + getString("Year"));
			accountMovement.setProperty("DebitAccount", 3);
			accountMovement.setProperty("DateCreated", DateConverter.dateToSQL(new Date(), true));
			accountMovement.setProperty("Amount", getString("AmountMembership"));
			accountMovement.setProperty("Date", getString("Date"));
			accountMovement.setProperty("Valuta", getString("Date"));
			
			String id = ocs.insertObject(accountMovement);
		}
		if(getString("AmountDonation").length() > 0){ //Spenden
			AccountMovement accountMovement = new AccountMovement();
			accountMovement.mergeProperties(this);
			accountMovement.setProperty("Description", "Spenden " + getString("Year"));
			accountMovement.setProperty("DebitAccount", 2);
			accountMovement.setProperty("DateCreated", DateConverter.dateToSQL(new Date(), true));
			accountMovement.setProperty("Amount", getString("AmountDonation"));
			accountMovement.setProperty("Date", getString("Date"));
			accountMovement.setProperty("Valuta", getString("Date"));
			
			String id = ocs.insertObject(accountMovement);
		}

		result.setParam("refresh", "accounting");
		
	}
}
