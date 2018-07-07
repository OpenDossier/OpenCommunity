package ch.opencommunity.base;

import ch.opencommunity.server.OpenCommunityServer;

import org.kubiki.database.Record;

import java.util.Vector;


public class Person extends BasicOCObject{
	public Person(){
		setTablename("Person");

		addProperty("PrimaryLanguage", "Integer", "0");
		addProperty("Comment", "Text", "");
		addProperty("Languages", "Text", "");
		
		addObjectCollection("Identity", "ch.opencommunity.base.Identity");
		addObjectCollection("Address", "ch.opencommunity.base.Address");
		addObjectCollection("Contact", "ch.opencommunity.base.Contact");
		addObjectCollection("MemberRole", "ch.opencommunity.base.MemberRole");
		addObjectCollection("Parameter", "ch.opencommunity.base.Parameter");
	}
	public void initObjectLocal(){
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		ocs.logAccess("initializing person " + getName());
		if(getObjects("Identity").size() == 0){
			Identity identity = new Identity();
			identity.addProperty("PersonID", "String", getName());
			String id = ocs.insertObject(identity);
			ocs.getObject(this, "Identity", "ID", id);
		}
	}
	public Identity getIdentity(){
		if(getObjects("Identity").size() > 0){
			return (Identity)getObjectByIndex("Identity", getObjects("Identity").size()-1);
		}
		else{
			return null;
		}
	}
	public Address getAddress(){
		if(getObjects("Address").size() > 0){
			return (Address)getObjectByIndex("Address", getObjects("Address").size()-1);
		}
		else{
			return null;
		}
	}
	public Contact getContact(int type){
		Contact contact = null;
		Vector contacts = getObjects("Contact");
		for(int i = 0; i < contacts.size(); i++){
			Contact c = (Contact)contacts.elementAt(i);
			if(c.getID("Type")==type){
				contact = c;	
			}
		}
		return contact;
	}
} 
