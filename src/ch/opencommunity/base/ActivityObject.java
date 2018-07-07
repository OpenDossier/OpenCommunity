package ch.opencommunity.base;


public class ActivityObject extends BasicOCObject{

	public ActivityObject(){
		setTablename("ActivityObject");
		addProperty("MemberAdID", "Integer", "");
		addProperty("MemberAdRequestID", "Integer", "");
		addProperty("OrganisationMemberID", "Integer", "");
	}
}