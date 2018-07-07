package org.kubiki.accounting;


import ch.opencommunity.base.*;
import ch.opencommunity.server.*;


public class AccountMovement extends BasicOCObject{
	public AccountMovement(){
		setTablename("AccountMovement");
		
		addProperty("Date", "Date", "");
		addProperty("Valuta", "Date", "");
		addProperty("DebitAccount", "Integer", "");
		addProperty("CreditAccount", "Integer", "");	
		addProperty("Description", "Text", "");
		addProperty("Amount", "Double", "");
		addProperty("OrganisationMember", "Integer", "0");
		addProperty("OrganisationalUnit", "Integer", "0");
		addProperty("Comment", "Text", "");
	}

}  
