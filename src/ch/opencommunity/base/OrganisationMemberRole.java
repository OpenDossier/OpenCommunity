package ch.opencommunity.base;

import org.kubiki.base.BasicClass;


public class OrganisationMemberRole extends BasicClass{

	public OrganisationMemberRole(String id, String value, String title, String titlealt){
		setName(id);
		addProperty("Value", "String", value);
		addProperty("Title", "String", title);
		addProperty("Titlealt", "String", titlealt);
	
	}
	public String toString(){
		return getString("Title");	
	}
	public String getLabel(){
		return getString("Title");	
	}
	public Object getValue(){
		return getName();	
	}
}