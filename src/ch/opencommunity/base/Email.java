package ch.opencommunity.base;

import org.kubiki.base.*;

import java.util.Vector;
import java.util.Hashtable;

public class Email extends BasicOCObject{
	
	public Email(){
		setTablename("Email");
		addProperty("OrganisationMember", "Integer", "");
		addProperty("Recipient", "String", "", false, "Empfänger", 255);
		addProperty("Content", "Text", "");
		
	}	
	
}