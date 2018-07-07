package ch.opencommunity.base;


import org.kubiki.application.DataObject;

public class ProcessDefinition  extends DataObject{
	
	
	public ProcessDefinition(){
		
		
		addObjectCollection("TextBlock", "ch.opencommunity.base.TextBlock");
		
	}
	
	
}