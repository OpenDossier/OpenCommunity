package ch.opencommunity.advertising;


import ch.opencommunity.base.*;
import ch.opencommunity.server.*;


public class MemberAdRequest extends BasicOCObject{
	public MemberAdRequest(){
		setTablename("MemberAdRequest");
		addProperty("MemberAd", "Integer", "");
		addProperty("MemberAdRequestGroupID", "Integer", "0");
		
		addProperty("ValidFrom", "DateTime", "");
		addProperty("ValidUntil", "DateTime", "");
		
		addProperty("NotificationMode", "Integer", "1"); //1 Email, 2 Brief
		addProperty("EmailID", "Integer", "");
		addProperty("DocumentID", "Integer", "");
		addProperty("NotificationStatus", "Integer", "");
		addProperty("NotificationMode", "Integer", "");
		
		addProperty("ActivationCode", "String", "", false, "Aktivierungscode", 20);
		
		addProperty("Comment", "Text", "");
		addProperty("UserComment", "Text", "");
		//addProperty("IsRepetition", "Boolean", "false");
		

		
		addObjectCollection("Feedback", "ch.opencommunity.advertising.Feedback");
	}
	public String toString(){
		return getName();
	}
}
