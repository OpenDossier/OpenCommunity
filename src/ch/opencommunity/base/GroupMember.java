package ch.opencommunity.base;

import org.kubiki.application.DataObject;

public class GroupMember extends DataObject{
	
	
	public GroupMember(){
		
		setTablename("GroupMember");
		
		addProperty("OrganisationalUnit", "Integer", "");
		
	}
	
	
	
}