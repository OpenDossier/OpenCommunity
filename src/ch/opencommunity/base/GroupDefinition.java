package ch.opencommunity.base;

import org.kubiki.application.DataObject;

public class GroupDefinition extends DataObject{
	
	
	public GroupDefinition(){
		
		setTablename("GroupDefinition");
		
		addObjectCollection("GroupMember", "ch.opencommunity.base.GroupMember");
		
	}
	
	
	
}