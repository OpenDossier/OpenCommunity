package ch.opencommunity.base;


public class ActivityOrganisationMember extends BasicOCObject{

	public ActivityOrganisationMember(){
		
		setTablename("ActivityOrganisationMember");
				
		addProperty("OrganisationMember", "Integer", "");
		
	}
	
}