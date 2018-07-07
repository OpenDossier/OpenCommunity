package ch.opencommunity.common;

import ch.opencommunity.base.BasicOCObject; 

public class Permission extends BasicOCObject{
	
	public Permission(){
			
		addProperty("Class","Text","*", false, "Objekttyp");
		addProperty("Command","String","", false, "Befehle", 200);
		
	}
	public String getLabel(){
		return toString();
	}
}