package ch.opencommunity.base;

import org.kubiki.database.Record;


public class Role extends BasicOCObject{
	public Role(){
		setTablename("Role");
		
		//addProperty("Role", "Integer", "");
		//addObjectCollection("Identity", "ch.opencommunity.base.Identity");

	}
	public String toString(){
		return getString("Title");
	}

}  
