package ch.opencommunity.base;

import org.kubiki.base.BasicClass;

public class OrganisationMemberInfo extends BasicClass{
	
	String infoString = "";
	
	int yearOfBirth = 0;
	
	public OrganisationMemberInfo(){
		
		addProperty("Title", "String", "", false, "Bezeichnung");
		addProperty("FamilyName", "String", "", false, "Nachname");		
		addProperty("FirstName", "String", "", false, "Vorname");	
		addProperty("Sex", "Integer", "", false, "Geschlecht");	
		addProperty("DateOfBirth", "Integer", "", false, "Geschlecht");	
		addProperty("AdditionalLine", "Integer", "", false, "AdditionalLine");	
		addProperty("Street", "Integer", "", false, "Strasse");	
		addProperty("Number", "Integer", "", false, "Nummer");	
		addProperty("Zipcode", "Integer", "", false, "PLZ");	
		addProperty("City", "String", "", false, "Ort");	
		addProperty("Country", "String", "", false, "Land");	
		addProperty("Email", "String", "", false, "Email");		
		addProperty("Addressation", "String", "", false, "Anrede");
		

	}
	public void initObjectLocal(){
		infoString = getString("FamilyName") + " " + getString("FirstName") + ", " + getString("Street") + " " + getString("Number") + " " + getString("Zipcode") + " " + getString("City");	
	}
	public String getAddressation(){
		String addressation = "";

		if(getID("Sex")==2){
			addressation = "Sehr geehrte Frau " + getString("FamilyName");
		}
		else{
			addressation = "Sehr geehrter Herr " + getString("FamilyName");
		}

		
		return addressation;
	}
	public String getInfoString(){
		return infoString;
	}
	
}