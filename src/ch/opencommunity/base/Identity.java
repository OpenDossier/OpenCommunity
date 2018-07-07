package ch.opencommunity.base;

import ch.opencommunity.server.OpenCommunityServer;

public class Identity extends BasicOCObject{

	public Identity(){
		setTablename("Identity");

		addProperty("Sex", "Integer", "", false, "Geschlecht");
		addProperty("Addressation", "String", "", false, "Anrede", 30);
		addProperty("FamilyName", "String", "", false, "Nachname", 100);
		addProperty("Patronymic", "String", "", false, "Vatersname", 100);
		addProperty("FirstName", "String", "", false, "Vorname", 100);
		addProperty("DateOfBirth", "String", "", false, "Geburtsdatum", 10);
		addProperty("FirstLanguage", "Integer", "", false, "Erste Sprache");
		addProperty("FirstLanguageS", "String", "", false, "Erste Sprache", 50);

	}
	public void initObjectLocal(){
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		
		getProperty("FirstLanguage").setSelection(ocs.getSupportedLanguages2());
	}

}  
