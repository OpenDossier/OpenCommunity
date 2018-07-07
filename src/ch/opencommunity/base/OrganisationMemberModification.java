package ch.opencommunity.base;

import ch.opencommunity.server.*;

import org.kubiki.base.ConfigValue;
import org.kubiki.database.Record;

import java.util.Vector;

public class OrganisationMemberModification extends BasicOCObject{

	Person person;

	public OrganisationMemberModification(){
		
		setTablename("OrganisationMemberModification");
		
		addProperty("FamilyName", "String", "", false, "Nachname", 100);
		addProperty("FirstName", "String", "", false, "Vorname", 100);
		addProperty("DateOfBirth", "String", "", false, "Geburtsdatum", 10);
		addProperty("FirstLanguageS", "String", "", false, "Erste Sprache", 50);

		addProperty("AdditionalLine", "String", "", false, "Zusatzzeile", 30);	
		addProperty("Street", "String", "", false, "Strasse", 30);	
		addProperty("Number", "String", "", false, "Nummer", 5);
		addProperty("Zipcode", "String", "", false, "PLZ", 6);
		addProperty("City", "String", "", false, "Ort", 30);		
		
		addProperty("Comment", "Text", "", false, "Bemerkungen");

		addProperty("Email", "String", "", false, "Email", 50);	
		addProperty("TelephonePrivate", "String", "", false, "Email", 50);	
		addProperty("TelephoneBusiness", "String", "", false, "Email", 50);	
		addProperty("TelephoneMobile", "String", "", false, "Email", 50);	
		
	}
}