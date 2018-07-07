package ch.opencommunity.query;

import org.kubiki.application.DataObject;
import org.kubiki.util.DateConverter;

public class QueryDefinition extends DataObject{
	
	public QueryDefinition(){
		
		setTablename("QueryDefinition");
		
		addProperty("Scope", "Integer", "1"); //1=OrganisationMember, 2=OrganisationalUnit
		
		addProperty("XML", "Text", "");
		
		
		
	}
	public String toString(){
		return getString("Title") + " " + DateConverter.sqlToShortDisplay(getString("DateCreated"), false);	
		
	}
	@Override
	public Object getValue(){
		return getName();	
	}
	
}