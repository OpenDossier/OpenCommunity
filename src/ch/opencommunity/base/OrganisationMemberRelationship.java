package ch.opencommunity.base;

import ch.opencommunity.server.OpenCommunityServer;

public class OrganisationMemberRelationship extends BasicOCObject{
	
	public OrganisationMemberRelationship(){
		
		setTablename("OrganisationMemberRelationship");
		
		addProperty("OrganisationMember", "Integer", "0");
		addProperty("Role", "Integer", "0");
	}
	public void initObjectLocal(){
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		getProperty("Role").setSelection(ocs.getMemberRoles());
		
	}
	
}