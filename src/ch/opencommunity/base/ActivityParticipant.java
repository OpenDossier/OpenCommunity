package ch.opencommunity.base;


public class ActivityParticipant extends BasicOCObject{

	public ActivityParticipant(){
		
		setTablename("ActivityParticipant");

		addProperty("OrganisationalUnit", "Integer", "");
		addProperty("OrganisationMember", "Integer", "");
		
	}
	
}