package ch.opencommunity.process;
 
import ch.opencommunity.server.*;
import ch.opencommunity.base.*;
import ch.opencommunity.advertising.*;
import ch.opencommunity.common.*;
 
import org.kubiki.ide.*;
import org.kubiki.base.Property;
import org.kubiki.base.ProcessResult;
import org.kubiki.base.ObjectCollection;
import org.kubiki.base.BasicClass;
import org.kubiki.base.ConfigValue;
import org.kubiki.database.Record;
import org.kubiki.util.DateConverter;
 
import org.kubiki.application.*;
 
import java.util.Vector;
 
 
public class OrganisationMemberModify extends BasicProcess{
	
	BasicProcessNode node1;
	
	Person person;
	Address address;
	Login login;
	Identity identity;
	OrganisationMember om;
	OrganisationMemberModification omm = null;

	public OrganisationMemberModify(){
		
		node1 = addNode();
		node1.setName("OrganisationMemberModificationNode");
		setCurrentNode(node1);
		
		addProperty("OMID", "String", "");
		addProperty("modifyidentity", "boolean", "false");
		addProperty("modifyaddress", "boolean", "false");
		addProperty("modifyemail", "boolean", "false");
		addProperty("modifytelephoneprivate", "boolean", "false");
		addProperty("modifytelephonemobile", "boolean", "false");
		
	}
	public void initProcess(){
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		ocs.logAccess(getString("OMID"));
		if(getString("OMID").length() > 0){
			om = (OrganisationMember)ocs.getObject(this, "OrganisationMember", "ID", getString("OMID"));
			initProcess(om);
		}

	}
	public void initProcess(OrganisationMember om){
		
		this.om = om;
		
		if(om != null){
			person = om.getPerson();
			addProperty(om.getProperty("Status"));
			if(person != null){
				identity = person.getIdentity();
				if(identity != null){
					addProperty(identity.getProperty("FamilyName"));
					addProperty(identity.getProperty("FirstName"));
					addProperty(identity.getProperty("DateOfBirth"));
					addProperty(identity.getProperty("Sex"));
					addProperty(identity.getProperty("FirstLanguageS"));
				}
				address = person.getAddress();
				if(address != null){
					addProperty(address.getProperty("AdditionalLine"));	
					addProperty(address.getProperty("Street"));	
					addProperty(address.getProperty("Number"));
					addProperty(address.getProperty("Zipcode"));
					addProperty(address.getProperty("City"));
				}
				
				for(BasicClass bc : om.getObjects("OrganisationMemberModification")){
					//addProperty(contacts[bc.getInt("Type")-1], "String", bc.getString("Value"));
					//addProperty(contacts[bc.getInt("Type")-1], contacts[bc.getInt("Type")-1], "");
					omm = (OrganisationMemberModification)bc;
				}

			
			}
			login = om.getLogin();
			if(login != null){
				addProperty(login.getProperty("Username"));
				addProperty(login.getProperty("Password"));
			}
		}
		
	}
	public String getOrganisationMemberModificationForm(ApplicationContext context){
		StringBuilder html = new StringBuilder();
		if(omm != null){
			html.append("<form id=\"processNodeForm\">");
			html.append("<table><tr><th>Bisher</th><th>Neu</th></tr>");
			
			html.append("<tr><td>" + identity.getString("FamilyName") + "</td><td>" + omm.getString("FamilyName") + "</td></tr>");
			html.append("<tr><td>" + identity.getString("FirstName") + "</td><td>" + omm.getString("FirstName") + "</td></tr>");
			html.append("<tr><td>" + identity.getString("DateOfBirth") + "</td><td>" + omm.getString("DateOfBirth") + "</td></tr>");
			html.append("<tr><td>" + identity.getString("FirstLanguageS") + "</td><td>" + omm.getString("FirstLanguageS") + "</td><td><input type=\"checkbox\" name=\"modifyidentity\" value=\"true\">Personenangaben ändern</td></tr>");
			
			html.append("<tr><td>" + address.getString("AdditionalLine") + "</td><td>" + omm.getString("AdditionalLine") + "</td></tr>");
			html.append("<tr><td>" + address.getString("Street") + "</td><td>" + omm.getString("Street") + "</td></tr>");
			html.append("<tr><td>" + address.getString("Number") + "</td><td>" + omm.getString("Number") + "</td></tr>");
			html.append("<tr><td>" + address.getString("Zipcode") + "</td><td>" + omm.getString("Zipcode") + "</td></tr>");	
			html.append("<tr><td>" + address.getString("City") + "</td><td>" + omm.getString("City") + "</td><td><input type=\"checkbox\" name=\"modifyaddress\" value=\"true\">Adresse ändern</td></tr>");
			Contact c = person.getContact(3);
			if(c != null){
				html.append("<tr><td>" + c.getString("Value") + "</td><td>" + omm.getString("Email") + "</td><td><input type=\"checkbox\" name=\"modifyemail\" value=\"true\">Email ändern</td></tr>");
			}
			else{
				html.append("<tr><td>(kein Wert)</td><td>" + omm.getString("Email") + "</td><td><input type=\"checkbox\" name=\"modifyemail\" value=\"true\">Email ändern</td></tr>");
			}
			c = person.getContact(0);
			if(c != null){
				html.append("<tr><td>" + c.getString("Value") + "</td><td>" + omm.getString("TelephonePrivate") + "</td><td><input type=\"checkbox\" name=\"modifytelephoneprivate\" value=\"true\">Privattelefon ändern</td></tr>");
			}
			else{
				html.append("<tr><td>(kein Wert)</td><td>" + omm.getString("TelephonePrivate") + "</td><td><input type=\"checkbox\" name=\"modifytelephoneprivate\" value=\"true\">Privattelefon ändern</td></tr>");
			}
			c = person.getContact(2);
			if(c != null){
				html.append("<tr><td>" + c.getString("Value") + "</td><td>" + omm.getString("TelephoneMobile") + "</td><td><input type=\"checkbox\" name=\"modifytelephonemobile\" value=\"true\">Mobiltelefon ändern</td></tr>");
			}
			else{
				html.append("<tr><td>(kein Wert)</td><td>" + omm.getString("TelephoneMobile") + "</td><td><input type=\"checkbox\" name=\"modifytelephonemobile\" value=\"true\">Mobiltelefon ändern</td></tr>");
			}
			html.append("</table>");
			
 	 	 	
 	 	 }
 	 	 html.append("</form>");
		html.append("<div style=\"position : absolute; top : " + (300) + "px; width : 400px;\"><input type=\"button\" onclick=\"getNextNode()\" value=\"Fertig\"></div>");
		return html.toString();
	}
	public void finish(ProcessResult result, ApplicationContext context){
 	 	 OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
 	 	 
 	 	 String now = DateConverter.dateToSQL(new java.util.Date(), true);

 	 	 if(getBoolean("modifyidentity")){
  	 	 	 identity.setProperty("FamilyName", omm.getString("FamilyName"));
 	 	 	 identity.setProperty("FirstName", omm.getString("FirstName"));
 	 	 	 identity.setProperty("DateOfBirth", omm.getString("DateOfBirth"));
 	 	 	 identity.setProperty("FirstLanguageS", omm.getString("FirstLanguageS"));
 	 	 	 ocs.updateObject(identity);
 	 	 }
 	 	 if(getBoolean("modifyaddress")){
  	 	 	 address.setProperty("AdditionalLine", omm.getString("AdditionalLine"));
 	 	 	 address.setProperty("Street", omm.getString("Street"));
 	 	 	 address.setProperty("Number", omm.getString("Number"));
 	 	 	 address.setProperty("Zipcode", omm.getString("Zipcode"));
 	 	 	 address.setProperty("City", omm.getString("City"));
 	 	 	 ocs.updateObject(address);
 	 	 }
 	 	 if(getBoolean("modifyemail")){
 	 	 	 Contact c = person.getContact(3);	 
 	 	 	 if(c != null){
 	 	 	 	c.setProperty("Value", 	 omm.getString("Email"));
 	 	 	 	ocs.updateObject(c);
 	 	 	 }
 	 	 	 else{
 	 	 	 	c = new Contact();
 	 	 	 	c.addProperty("PersonID", "String", person.getName());
 	 	 	 	c.setProperty("Type", 3);
 	 	 	 	c.setProperty("Value", omm.getString("Email"));
 	 	 	 	ocs.insertObject(c);
 	 	 	 }
 	 	 }
 	 	 if(getBoolean("modifytelephoneprivate")){
 	 	 	 Contact c = person.getContact(0);	 
 	 	 	 if(c != null){
 	 	 	 	c.setProperty("Value", 	 omm.getString("TelephonePrivate"));
 	 	 	 	ocs.updateObject(c);
 	 	 	 }
 	 	 	 else{
 	 	 	 	c = new Contact();
 	 	 	 	c.addProperty("PersonID", "String", person.getName());
 	 	 	 	c.setProperty("Type", 0);
 	 	 	 	c.setProperty("Value", omm.getString("TelephonePrivate"));
 	 	 	 	ocs.insertObject(c);
 	 	 	 }
 	 	 }
 	 	 if(getBoolean("modifytelephonemobile")){
 	 	 	 Contact c = person.getContact(2);	 
 	 	 	 if(c != null){
 	 	 	 	c.setProperty("Value", 	 omm.getString("TelephoneMobile"));
 	 	 	 	ocs.updateObject(c);
 	 	 	 }
 	 	 	 else{
 	 	 	 	c = new Contact();
 	 	 	 	c.addProperty("PersonID", "String", person.getName());
 	 	 	 	c.setProperty("Type", 2);
 	 	 	 	c.setProperty("Value", omm.getString("TelephoneMobile"));
 	 	 	 	ocs.insertObject(c);
 	 	 	 }
 	 	 }
 	 	 omm.setProperty("Status", "1");
 	 	 omm.setProperty("DateModified", now);
 	 	 ocs.updateObject(omm);
 	 	 
 	 	 om.setProperty("Status", "1");
 	 	 om.setProperty("DateModified", now);
 	 	 ocs.updateObject(om);
 	 	 
		 if(getParent() instanceof BasicProcess){
			((BasicProcess)getParent()).setSubprocess(null);
		 }
		 else{			
			result.setParam("refresh", "currentsection");	 
		 } 	 	 
 	 	 	 
 	 	 
 	}
 	 
}