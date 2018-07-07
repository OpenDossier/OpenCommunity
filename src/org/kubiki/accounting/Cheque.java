package org.kubiki.accounting;


import ch.opencommunity.base.*;
import ch.opencommunity.server.*;


public class Cheque extends BasicOCObject{
	public Cheque(){
		setTablename("Cheque");
		
		addProperty("DateIssued", "Date", "");
		addProperty("DateCashed", "Date", "");
		addProperty("DateValuta", "Date", "");
		addProperty("SerialNumber", "String", "", false, "Serien-Nummer", 20);	
		addProperty("Description", "Text", "");
		addProperty("Amount", "Double", "");
		addProperty("OrganisationMemberIssued", "Integer", "0");
		addProperty("OrganisationMemberCashed", "Integer", "0");
		addProperty("OrganisationalUnit", "Integer", "0");
		addProperty("Comment", "Text", "");
	}

}                                                                                                                                            