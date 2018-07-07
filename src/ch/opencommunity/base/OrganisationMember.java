package ch.opencommunity.base;

import ch.opencommunity.server.*;

import org.kubiki.base.ConfigValue;
import org.kubiki.base.BasicClass;
import org.kubiki.database.Record;

import java.util.Vector;

public class OrganisationMember extends BasicOCObject{

	Person person;
	
	OrganisationMember activeRelationship = null;
	
	private Vector memberadids;

	public OrganisationMember(){
		setTablename("OrganisationMember");
		
		addProperty("Type", "Integer", "1");
		
		addProperty("Identifier", "String", "", false, "Identifier", 30);
		
		addProperty("Person", "Integer", "0");
		
		addProperty("MemberRole", "Integer", "");
		
		addProperty("Function", "Integer", "");
		
		addProperty("DataProtection", "Integer", "0");
		
		addProperty("NotificationMode", "Integer", "1");
		
		addProperty("NotificationStatus", "Integer", "0");
		
		addProperty("RegistrationMode", "Integer", "0"); //20171218: selber oder durch admin
		
		addProperty("Comment", "Text", "", false, "Bemerkungen");
		
		addProperty("InheritsAddress", "Boolean", "false");

		addObjectCollection("MemberRole", "ch.opencommunity.base.MemberRole");
		addObjectCollection("Login", "ch.opencommunity.base.Login");
		
		addObjectCollection("LoginCode", "ch.opencommunity.base.LoginCode");
		
		addObjectCollection("MemberAd", "ch.opencommunity.advertising.MemberAd");
		addObjectCollection("MemberAdRequest", "ch.opencommunity.advertising.MemberAdRequest");

		addObjectCollection("Feedback", "ch.opencommunity.advertising.Feedback");
		addObjectCollection("FreeTextFeedback", "ch.opencommunity.base.FreeTextFeedback");
		
		addObjectCollection("OrganisationMemberRelationship", "ch.opencommunity.base.OrganisationMemberRelationship");
		addObjectCollection("OrganisationMemberModification", "ch.opencommunity.base.OrganisationMemberModification");
		
		addObjectCollection("Activity", "ch.opencommunity.base.Activity");
		addObjectCollection("Document", "ch.opencommunity.base.Document");
		addObjectCollection("ExternalDocument", "ch.opencommunity.base.ExternalDocument");
		
		addObjectCollection("FeedbackRecord", "ch.opencommunity.feedback.FeedbackRecord");
		
		memberadids = new Vector();
	}
	public void initObjectLocal(){
	
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		ocs.logAccess("initializing organisationmember " + getName());
		person = (Person)ocs.getObject(null, "Person", "ID", "" + getID("Person"), false);	
		person.setParent(this);
		person.initObjectLocal();
		Vector status = new Vector();
		status.add(new ConfigValue("0", "0", "registriert"));
		status.add(new ConfigValue("4", "4", "zu kontrollieren"));
		status.add(new ConfigValue("1", "1", "aktiv"));
		status.add(new ConfigValue("3", "3", "zu löschen"));
		status.add(new ConfigValue("2", "2", "inaktiv"));
		status.add(new ConfigValue("6", "6", "zu anonymisieren"));
		status.add(new ConfigValue("5", "5", "anonymisiert"));
		getProperty("Status").setSelection(status);
		
		Vector channels = new Vector();
		channels.add(new ConfigValue("1","1", "Email"));
		channels.add(new ConfigValue("2","2", "Brieflich"));
		getProperty("NotificationMode").setSelection(channels);
		
		getObjectCollection("Activity").sort("DateCreated", true);
		
		addProperty("AccessCode", "String", "", true, "AccessCode");
		setProperty("AccessCode", ocs.createPassword(20));
		
		addObjectCollection("ReverseRelationships", "*");
		updateReverseRelationships(ocs);
		
		Vector types = new Vector();
		
		types.add(new ConfigValue("1", "1", "Standard"));
		types.add(new ConfigValue("2", "2", "Familie"));
		types.add(new ConfigValue("3", "3", "Privat"));
		types.add(new ConfigValue("4", "4", "Institution"));

		getProperty("Type").setSelection(types);
	}
	public void updateReverseRelationships(OpenCommunityServer ocs){
		
		getObjectCollection("ReverseRelationships").removeObjects();
		String sql = "SELECT t1.*, t2.ID AS OMID, t4.Familyname, t4.Firstname FROM OrganisationMemberRelationship as t1 JOIN OrganisationMember AS t2 ON t1.OrganisationMemberID=t2.ID JOIN Person AS t3 ON t2.Person=t3.ID JOIN Identity AS t4 ON t4.PersonID=t3.ID";
		sql += " WHERE OrganisationMember=" + getName();
		ocs.queryData(sql, getObjectCollection("ReverseRelationships"));
				
		for(BasicClass record : getObjects("ReverseRelationships")){
			record.getProperty("ROLE").setSelection(ocs.getMemberRoles());
		}
			
			
	}
	public Person getPerson(){
		return person;
	}
	public Login getLogin(){
		if(getObjects("Login").size() > 0){
			return (Login)getObjectByIndex("Login", getObjects("Login").size()-1);
		}
		else{
			return null;
		}
	}
	public String getLabel(){
		return toString();	
	}
	public String toString(){
		if(person != null){
			Identity identity = person.getIdentity();
			if(identity != null){
				return identity.getString("FirstName") + " " + identity.getString("FamilyName");
			}
			else{
				return "" + person.getName();	
			}
		}
		else{
			return getName();
		}
	}
	public void setActiveRelationship(OrganisationMember om){
		this.activeRelationship = om;	
	}
	public OrganisationMember getActiveRelationship(){
		return activeRelationship;
	}
	public String getAddressation(){
		String addressation = "";
		Person person = getPerson();
		if(person != null){
			Identity identity = person.getIdentity();
			if(identity != null){
				if(identity.getID("Sex")==2){
					addressation = "Sehr geehrte Frau " + identity.getString("FamilyName");
				}
				else{
					addressation = "Sehr geehrter Herr " + identity.getString("FamilyName");
				}
			}
		}
		
		return addressation;
	}
	public Contact getContact(int type){
		if(person != null){
			return person.getContact(type);	
		}
		else{
			return null;	
		}
	}
	public String getContactValue(int type){
		Contact contact = getContact(type);
		if(contact != null){
			return contact.getValue();	
		}
		else{
			return "";	
		}
	}
	
	//-----------------------------AK 20161115, moved from usersession---------------------------------
	
	public void addMemberAdID(String id){
		if(memberadids.indexOf(id)==-1){
			memberadids.add(id);
		}
	}
	public void removeMemberAdID(String id){
		if(memberadids.indexOf(id) > -1){
			memberadids.remove(id);
		}
	}
	public Vector getMemberAdIDs(){
		return memberadids;
	}
	public boolean hasMemberAdID(String id){
		if(memberadids.indexOf(id)==-1){
			return false;
		}	
		else{
			return true;
		}
	}
	public void removeMemberAdIDs(){
		memberadids.clear();	
	}

}  
