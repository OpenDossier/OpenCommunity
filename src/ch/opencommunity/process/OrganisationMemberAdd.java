 package ch.opencommunity.process;
 
 import ch.opencommunity.server.*;
 import ch.opencommunity.base.*;
 import ch.opencommunity.dossier.DossierController;
 import ch.opencommunity.view.OrganisationMemberList;
 
 import org.kubiki.ide.*;
 import org.kubiki.base.Property;
 import org.kubiki.base.ProcessResult;
 import org.kubiki.base.ConfigValue;
 
 import org.kubiki.application.*;
 import org.kubiki.util.DateConverter;
 
 import java.util.Vector;
 
 
 public class OrganisationMemberAdd extends BasicProcess{
 
	BasicProcessNode node1;
 
	public OrganisationMemberAdd(){
		node1 = new BasicProcessNode(){
			public boolean validate(ApplicationContext context){
				
				if(context.hasProperty("setinheritsaddress") && context.getString("setinheritsaddress").equals("true")){
					
					if(getBoolean("InheritsAddress")){

						getProperty("AdditionalLine").setHidden(true);	
						getProperty("POBox").setHidden(true);	
						getProperty("Street").setHidden(true);	
						getProperty("Number").setHidden(true);	
						getProperty("Zipcode").setHidden(true);	
						getProperty("City").setHidden(true);	
						getProperty("TelP").setHidden(true);	
						getProperty("TelB").setHidden(true);
						getProperty("TelM").setHidden(true);
						getProperty("Email").setHidden(true);
						
					}
					else{
						getProperty("AdditionalLine").setHidden(false);	
						getProperty("POBox").setHidden(false);							
						getProperty("Street").setHidden(false);	
						getProperty("Number").setHidden(false);	
						getProperty("Zipcode").setHidden(false);	
						getProperty("City").setHidden(false);	
						getProperty("TelP").setHidden(false);	
						getProperty("TelB").setHidden(false);
						getProperty("TelM").setHidden(false);
						getProperty("Email").setHidden(false);						
						
					}
				
					return false;
					
				}
				else{
					
					return true;
					
				}
				
			}
			
			
		};
		
		
		addNode(node1);
		
		setTitle("Neuen Benutzer erfassen");
		
		Property p = addProperty("Function", "String", "", true, "Funktion");
		node1.addProperty(p);
		
		p = addProperty("Title", "String", "", true, "Bezeichnung");
		node1.addProperty(p);
		
		p = addProperty("FamilyName", "String", "", false, "Nachname");
		node1.addProperty(p);
		
		p = addProperty("FirstName", "String", "", false, "Vorname");
		node1.addProperty(p);
		
		p = addProperty("DateOfBirth", "String", "", false, "Geburtsjahr");
		node1.addProperty(p);
		
		p = addProperty("Sex", "Integer", "", false, "Geschlecht");
		node1.addProperty(p);
		
		p = addProperty("InheritsAddress", "Boolean", "false", true, "Adresse wie Organisation");
		node1.addProperty(p);
		p.setAction("getNextNode('setinheritsaddress=true')");
		
		p = addProperty("AdditionalLine", "String", "", false, "Zusatzzeile");
		node1.addProperty(p);
		
		p = addProperty("POBox", "String", "", false, "Postfach");
		node1.addProperty(p);
		
		p = addProperty("Street", "String", "", false, "Strasse");
		node1.addProperty(p);
		
		p = addProperty("Number", "String", "", false, "Nummer");
		node1.addProperty(p);
		
		p = addProperty("Zipcode", "String", "", false, "PLZ");
		node1.addProperty(p);
		
		p = addProperty("City", "String", "", false, "Ort");
		node1.addProperty(p);
		
		p = addProperty("TelP", "String", "", false, "Telefon privat");
		node1.addProperty(p);
		
		p = addProperty("TelB", "String", "", false, "Telefon geschäftlich");
		node1.addProperty(p);
		
		p = addProperty("TelM", "String", "", false, "Telefon mobil");
		node1.addProperty(p);
		
		p = addProperty("Email", "String", "", false, "Email");
		node1.addProperty(p);
		
		p = addProperty("Organisation", "Integer", "1", true, "Organisation");
		node1.addProperty(p);
		
		p = addProperty("OrganisationNew", "String", "", true, "Organisation neu");
		node1.addProperty(p);
		
		p = addProperty("NotificationMode", "Integer", "1", true, "Benachrichtigung durch");
		node1.addProperty(p);
		

		
		setCurrentNode(node1);
	}
	public void finish(ProcessResult result, ApplicationContext context){
	
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		
		Person person = new Person();
		String id = ocs.insertSimpleObject(person);	
		
		Identity identity = new Identity();
		identity.mergeProperties(this);
		identity.addProperty("PersonID", "String", id);
		ocs.insertSimpleObject(identity);	
		
		Address address = new Address();
		address.mergeProperties(this);
		address.addProperty("PersonID", "String", id);
		ocs.insertSimpleObject(address);	
		
		String organisationid = "1";
		
		if(getString("OrganisationNew").length() > 1){
			OrganisationalUnit ou = new OrganisationalUnit();
			ou.setProperty("Title", getString("OrganisationNew"));
			organisationid = ocs.insertSimpleObject(ou);
		}
		else if(getID("Organisation") > 0){
			organisationid = "" + getID("Organisation");
		}
		
		OrganisationMember om = new OrganisationMember();
		if(getParent() instanceof DossierController){
			DossierController dossierController = (DossierController)getParent();
			om.setProperty("Status", 1);
			om.addProperty("OrganisationalUnitID", "String", "" + dossierController.getDossier().getID("OrganisationalUnit"));
		}
		else if(getParent() instanceof OrganisationMemberList){
			om.setProperty("Status", "4");
		}
		else{
			om.addProperty("OrganisationalUnitID", "String", organisationid);
		}
		om.setProperty("DateCreated", DateConverter.dateToSQL(new java.util.Date(), true));
		om.setProperty("DateModified", DateConverter.dateToSQL(new java.util.Date(), true));
		om.setProperty("Person", id);
		om.setProperty("Title", getString("Title"));
		om.setProperty("Function", getID("Function"));
		om.setProperty("NotificationMode", getString("NotificationMode"));

		om.setProperty("NotificationStatus", "0");

		om.setProperty("InheritsAddress", getBoolean("InheritsAddress"));
		//om.addProperty("OrganisationalUnitID", "String", organisationid);
		String omid = ocs.insertObject(om);
		
		if(getString("TelP").length() > 0){
			Contact c = new Contact();
			c.addProperty("PersonID", "String", id);
			c.setProperty("Type", 0);
			c.setProperty("Value", getString("TelP"));
			ocs.insertSimpleObject(c);
		}
		if(getString("TelB").length() > 0){
			Contact c = new Contact();
			c.addProperty("PersonID", "String", id);
			c.setProperty("Type", 1);
			c.setProperty("Value", getString("TelB"));
			ocs.insertSimpleObject(c);
		}
		if(getString("TelM").length() > 0){
			Contact c = new Contact();
			c.addProperty("PersonID", "String", id);
			c.setProperty("Type", 2);
			c.setProperty("Value", getString("TelM"));
			ocs.insertSimpleObject(c);
		}
		if(getString("Email").length() > 0){
			Contact c = new Contact();
			c.addProperty("PersonID", "String", id);
			c.setProperty("Type", 3);
			c.setProperty("Value", getString("Email"));
			ocs.insertSimpleObject(c);
		}
		
		
		if(getParent() instanceof DossierController){
			result.setParam("exec", "openDossier('" + getParent().getName() + "')");			
		}
		else if(getParent() instanceof OrganisationMemberList){
			result.setParam("exec", "editOrganisationMember(" + omid + ")");				
		}
		else{
			result.setParam("refresh", "user");
			result.setParam("newprocess", "ch.opencommunity.process.OrganisationMemberEdit");
			result.setParam("newprocessparams", "OMID=" + id + "~sectionid=user");
		}
		
	}
	public void initProcess(){
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		getProperty("Organisation").setSelection(ocs.getObjects("OrganisationalUnit"));
		
		Vector channels = new Vector();
		channels.add(new ConfigValue("1","1", "Email"));
		channels.add(new ConfigValue("2","2", "Brieflich"));
		getProperty("NotificationMode").setSelection(channels);
		
		Vector sex = new Vector();
		sex.add(new ConfigValue("0", "0", "Nicht gesetzt"));
		sex.add(new ConfigValue("1", "1", "Herr"));
		sex.add(new ConfigValue("2", "2", "Frau"));
		getProperty("Sex").setSelection(sex);
		
		
		if(getParent() instanceof DossierController){
			/*
			Vector functions = new Vector();
			functions.add(new ConfigValue("1", "1", "StifungspräsidentIn"));
			functions.add(new ConfigValue("2", "2", "Stifungsrat"));
			*/
			getProperty("Function").setSelection(ocs.getFunctions().getCodeList());				
			getProperty("Function").setHidden(false);	
			getProperty("Title").setHidden(false);	
			getProperty("InheritsAddress").setHidden(false);	
			setProperty("InheritsAddress", true);
			getProperty("DateOfBirth").setHidden(true);	
			getProperty("AdditionalLine").setHidden(true);	
			getProperty("POBox").setHidden(true);	
			getProperty("Street").setHidden(true);	
			getProperty("Number").setHidden(true);	
			getProperty("Zipcode").setHidden(true);	
			getProperty("City").setHidden(true);	
			/*
			getProperty("TelP").setHidden(true);	
			getProperty("TelB").setHidden(true);
			getProperty("TelM").setHidden(true);
			getProperty("Email").setHidden(true);
			*/
		}
	}
		
 }