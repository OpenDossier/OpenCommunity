package ch.opencommunity.base;

import org.kubiki.database.Record;

public class AccessLog extends Record{
	
	public AccessLog(){
		
		setTablename("AccessLog");
		addProperty("OrganisationMemberID", "Integer", "");	
		addProperty("DateCreated", "DateTime", "");	
		
	}
	
	
}