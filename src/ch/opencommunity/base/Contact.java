package ch.opencommunity.base;


public class Contact extends BasicOCObject{

	public Contact(){
		setTablename("Contact");
		addProperty("Type", "Integer", "", false, "Typ");
		addProperty("Value", "String", "", false, "Wert", 255);
		addProperty("ValidFrom", "Date", "", true, "Gültig von");
		addProperty("ValidUntil", "Date", "", false, "Gültig bis");

	}
	public boolean handleCommand(String command){
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
		//setCodeSelection("Type", "code_contacttype");

	}
	public String getValue(){
		return getString("Value");	
	}

}  
