package ch.opencommunity.base;


public class Address extends BasicOCObject{

	public Address(){
		setTablename("Address");
		addProperty("Type", "String", "", false, "Typ", 10);
		addProperty("AdditionalLine", "String", "", false, "Zusatzzeile", 250);
		addProperty("POBox", "String", "", false, "Postfach", 100);
		addProperty("Street", "String", "", false, "Strasse", 30);	
		addProperty("Number", "String", "", false, "Nummer", 5);
		addProperty("Zipcode", "String", "", false, "PLZ", 6);
		addProperty("City", "String", "", false, "Ort", 30);
		addProperty("Country", "String", "", false, "Land", 30);
		addProperty("ValidFrom", "Date", "", true, "Gültig von");
		addProperty("ValidUntil", "Date", "", false, "Gültig bis");

	}
	public boolean handleCommand(String command){
		super.handleCommand(command);
		/*
		ERP erp = (ERP)getRoot();
		if(command.equals("save")){
			erp.updateObject(this);
			erp.refresh(this);
		}
		*/
		return true;
	}
	public void initObjectLocal(){
		/*
		OpenEnterpriseInterface oei = (OpenEnterpriseInterface)getRoot();
		CodeDefinition codeDefinition = oei.getCodeDefinition("code_addresstype");
		if(codeDefinition != null){
			getProperty("Type").setSelection(codeDefinition.getCodeList());
		}
		*/
	}

}  
