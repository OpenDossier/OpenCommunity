package ch.opencommunity.advertising;


import ch.opencommunity.base.*;
import ch.opencommunity.server.*;


public class MemberAdRequestGroup extends BasicOCObject{
	public MemberAdRequestGroup(){
		setTablename("MemberAdRequestGroup");
		addProperty("MemberAd", "Integer", "");
		

		
		addProperty("Comment", "Text", "");
		
	}
}