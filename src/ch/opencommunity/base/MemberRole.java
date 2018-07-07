package ch.opencommunity.base;

import org.kubiki.database.Record;


public class MemberRole extends BasicOCObject{
	public MemberRole(){
		setTablename("MemberRole");
		
		addProperty("Role", "Integer", "");
		//addObjectCollection("Identity", "ch.opencommunity.base.Identity");

	}

}  
