package org.kubiki.accounting;


import ch.opencommunity.base.*;
import ch.opencommunity.server.*;


public class Account extends BasicOCObject{
	public Account(){
		setTablename("Account");
		addProperty("AccountNumber", "String", "", false, "Kontennummer", 20);
	}

} 
