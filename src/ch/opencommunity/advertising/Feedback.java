package ch.opencommunity.advertising;


import ch.opencommunity.base.*;
import ch.opencommunity.server.*;


public class Feedback extends BasicOCObject{
	public Feedback(){
		setTablename("Feedback");
		addProperty("OrganisationMember", "Integer", "");
		addProperty("Familyname", "String", "", false, "Nachname", 30);
		addProperty("Firstname", "String", "", false, "Vorname", 30);
		addProperty("Type", "Integer", "");
		addProperty("Reason", "Integer", "");
		addProperty("Reason_1", "Boolean", "false", false, "Nicht erreicht");
		addProperty("Reason_2", "Boolean", "false", false, "Unzuverlässig");
		addProperty("Reason_3", "Boolean", "false", false, "Keine Zeit");
		addProperty("Reason_4", "Boolean", "false", false, "Passt persönlich nicht");
		addProperty("Reason_5", "Boolean", "false", false, "Passt fachlich nicht");
		addProperty("Reason_6", "Boolean", "false", false, "Anderes");
		
		addProperty("ContactEstablished", "Boolean", "false", false, "Kontakt zustandegekommen");
		
		addProperty("Quality", "Integer", "0");	
		addProperty("Problems", "Text", "");	
		addProperty("Highlights", "Text", "");
		
		addProperty("Comment", "Text", "");
		
	}
	public String toString(){
		return getName() + " " + getString("DateCreated");	
	}
	
	
}