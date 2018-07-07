package ch.opencommunity.base;

import org.kubiki.application.DataObject;

public class OrganisationMemberType extends DataObject{
	
	public OrganisationMemberType(){
		
		setTablename("OrganisationMemberType");
		
		addProperty("SortOrder", "Integer", "0");
		
		addProperty("Description", "Text", "");
		
		addProperty("HasSubMembers", "Boolean", "false");
		
	}
	public boolean hasSubMembers(){
		
		return getBoolean("HasSubMembers");
		
	}
	public void initObjectLocal(){
		
	}
		
	
}