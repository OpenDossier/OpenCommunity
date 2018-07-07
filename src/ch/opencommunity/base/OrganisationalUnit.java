package ch.opencommunity.base;

import ch.opencommunity.server.OpenCommunityServer;

import org.kubiki.base.*;

import java.util.Vector;

//import ch.openenterprise.accounting.CostCenter;

public class OrganisationalUnit extends BasicOCObject{

	public OrganisationalUnit(){
		setTablename("OrganisationalUnit");

		addProperty("Type", "Integer", "0", false, "Typ");
		addProperty("Identifier", "String", "", false, "Identifikator", 20);
		addProperty("LongTitle", "String", "", false, "Lange Bezeichnung", 50);

		addProperty("GeographicalUnit", "Integer", "0", false, "Region");
		
		addProperty("Description", "Text", "0", false, "Beschreibung");
		
		addProperty("MainContactPerson", "Integer", "0", false, "Kontaktperson");

		addObjectCollection("Address", "ch.opencommunity.base.Address");
		addObjectCollection("Contact", "ch.opencommunity.base.Contact");
		
		//addObjectCollection("Dossier", "ch.opencommunity.dossier.Dossier");
		
		ObjectCollection oc = addObjectCollection("OrganisationMember", "ch.openenterprise.base.OrganisationMember");
		oc.setPreloadObjects(false);
		
		addObjectCollection("OrganisationalUnit", "ch.openenterprise.base.OrganisationalUnit");
		
		//addObjectCollection("CostCenter", "ch.openenterprise.accounting.CostCenter");
		
		addFunction("contactadd", "Neuer Kontakt", null);
		addFunction("addressadd", "Neuer Adresse", null);
		addFunction("organisationalunitadd", "Neuer Abteilung", null);
		addFunction("personadd", "Neuer Mitarbeiter", null);
	}
	public boolean handleCommand(String command, BasicClass src){
		System.out.println(command);
		/*
		if(command.equals("personadd")){
			createProcess("ch.openenterprise.client.PersonAdd", this);
		}
		else if(command.equals("addressadd")){
			createProcess("ch.openenterprise.base.AddressAdd", this);
		}
		else if(command.equals("contactadd")){
			createProcess("ch.openenterprise.base.ContactAdd", this);
		}
		else if(command.equals("organisationalunitadd")){
			createProcess("ch.openenterprise.base.OrganisationalUnitAdd", this);
		}
		else{
			super.handleCommand(command, src);
		}
		*/
		return true;
	}
	public void initObjectLocal(){
		/*
		OpenEnterpriseInterface oei = (OpenEnterpriseInterface)getRoot();
		CodeDefinition codeDefinition = oei.getCodeDefinition("code_outype");
		if(codeDefinition != null){
			getProperty("Type").setSelection(codeDefinition.getCodeList());
		}
		//setCodeSelection("GeographicalUnit", "code_geographicalunit");
		GeographicalUnit gu = (GeographicalUnit)oei.getObject(null, "GeographicalUnit", "ID", "" + getID("GeographicalUnit"), false);
		if(gu != null){
			gu.setParent(this);
			gu.initObjectLocal();
			setProperty("GeographicalUnit", gu);
		}
		*/
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		getProperty("Type").setSelection(ocs.getInstitutionTypes());
	}
	public Address getAddress(){
		if(getObjects("Address").size() > 0){
			return (Address)getObjectByIndex("Address", 0);	
		}
		else{
			return null;	
		}
	}

} 
