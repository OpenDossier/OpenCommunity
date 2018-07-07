package ch.opencommunity.view;

import ch.opencommunity.server.*;
import ch.opencommunity.process.OrganisationMemberEdit;
import ch.opencommunity.process.MergeProfile;
import ch.opencommunity.base.OrganisationMember;
import ch.opencommunity.base.Person;
import ch.opencommunity.base.Identity;
import ch.opencommunity.base.Address;
import ch.opencommunity.base.Contact;
import ch.opencommunity.base.Login;

import org.kubiki.servlet.HtmlFieldWidget;
import org.kubiki.application.ApplicationContext;
import org.kubiki.base.BasicClass;
import org.kubiki.base.Property;
import org.kubiki.base.ObjectCollection;

public class ProfileMergeWidget extends HtmlFieldWidget{
	
	OrganisationMember om1, om2;
	Person person1, person2;
	Identity identity1, identity2;
	Address address1, address2;
	Login login1, login2;
	
	//Ersetzen durch eine neue Property, welche ein value/label-Paar aufnimmt
	
	@Override
	public String renderWidget(BasicClass bc, Property p, boolean isEditable, ApplicationContext context, String prefix){
		
		OpenCommunityServer ocs = (OpenCommunityServer)bc.getRoot();
		StringBuilder html = new StringBuilder();
		
		if(bc.getParent() instanceof OrganisationMemberEdit){
		
			OrganisationMemberEdit ome = (OrganisationMemberEdit)bc.getParent();
			
			om1 = ome.getCurrentControler().getOrganisationMember();
			
		}
		else{
			
			om1 = ((MergeProfile)bc).getOwner().getOrganisationMember();	
		}
	
		
		om2 = null;
		
		if(bc.getObject("OrganisationMemberLink") instanceof OrganisationMember){
			om2 = (OrganisationMember)bc.getObject("OrganisationMemberLink");
		}
		
		html.append("<tr><td colspan=\"2\">");
		
		
		
		if(om1 != null && om2 != null){
		
			html.append("<table>");
			
			html.append("<tr><th class=\"tableheader\">" + om1 + "</th><th class=\"tableheader\">" + om2 + "</th></tr>");
			
			person1 = om1.getPerson();
			person2 = om2.getPerson();
			
			identity1 = person1.getIdentity();
			identity2 = person2.getIdentity();
			
			address1 = person1.getAddress();
			address2 = person2.getAddress();
			
			login1 = om1.getLogin();
			login2 = om2.getLogin();

			addTableRow(html, "DateCreated", om1, om2);
			
			html.append("<tr><th class=\"tableheader\" colspan=2>Personendaten</th></tr>");
			
			addTableRow(html, "FamilyName", identity1, identity2, true);
			addTableRow(html, "FirstName", identity1, identity2, true);	
			addTableRow(html, "DateOfBirth", identity1, identity2, true);
			addTableRow(html, "LanguagesS", identity1, identity2, true);	
			
			
			addTableRow(html, "Languages", person1, person2, true);
			
			html.append("<tr><th class=\"tableheader\" colspan=2>Adresse</th></tr>");
			
			addTableRow(html, "Street", address1, address2, true);
			addTableRow(html, "Number", address1, address2, true);
			addTableRow(html, "ZipCode", address1, address2, true);			
			addTableRow(html, "City", address1, address2, true);		
			
			html.append("<tr><th class=\"tableheader\" colspan=2>Kontaktinfo</th></tr>");
			
			for(int i = 0; i < 4; i++){
				
				Contact c1 = person1.getContact(i);
				Contact c2 = person2.getContact(i);
				
				addTableRow(html, "Value", c1, c2, true);		
					
			}
			
			html.append("<tr><th class=\"tableheader\" colspan=2>Login</th></tr>");
			
			addTableRow(html, "Username", login1, login2);	
			addTableRow(html, "Password", login1, login2);	
			
			html.append("<tr><td></td><td><input type=\"button\" onclick=\"getNextNode('mergelogin=true')\" value=\"Login übernehmen\"></td></tr>");
			
			//-------------------------------------------------------------
			
			html.append("<tr><th class=\"tableheader\" colspan=2>Aktivitäten</th></tr>");
			
			html.append("<tr><td>");
			
			for(BasicClass o : om1.getObjects("Activity")){
				html.append(o + "<br>");	
			}
				
				
			html.append("</td><td>");
			
			for(BasicClass o : om2.getObjects("Activity")){
				html.append(o + "<br>");	
			}
				
			html.append("</td></tr>");
			
			html.append("<tr><td></td><td><input type=\"button\" onclick=\"getNextNode('mergeactivities=true')\" value=\"Aktivitäten übernehmen\"></td></tr>");
			
			//----------------------------------------------------------
			
			html.append("<tr><th class=\"tableheader\" colspan=2>Inserate</th></tr>");
			
			html.append("<tr><td>");
			
			for(BasicClass o : om1.getObjects("MemberAd")){
				html.append(o + "<br>");	
			}
				
				
			html.append("</td><td>");
			
			for(BasicClass o : om2.getObjects("MemberAd")){
				html.append(o + "<br>");	
			}	
			html.append("</td></tr>");
			
			html.append("<tr><td></td><td><input type=\"button\" onclick=\"getNextNode('mergememberads=true')\" value=\"Inserate übernehmen\"></td></tr>");
			
			//----------------------------------------------------------
			
			html.append("<tr><th class=\"tableheader\" colspan=2>Adressbestellungen</th></tr>");
			
			html.append("<tr><td>");
			
			for(BasicClass o : om1.getObjects("MemberAdRequest")){
				html.append(o + "<br>");	
			}
				
				
			html.append("</td><td>");
			
			for(BasicClass o : om2.getObjects("MemberAdRequest")){
				html.append(o + "<br>");	
			}
			
			
			html.append("</td></tr>");
			
			html.append("<tr><td></td><td><input type=\"button\" onclick=\"getNextNode('mergememberadrequests=true')\" value=\"Adressbestellungen übernehmen\"></td></tr>");		
			
			//----------------------------------------------------------
			
			html.append("<tr><th class=\"tableheader\" colspan=2>Buchungen</th></tr>");
			
			html.append("<tr><td>");
			
			ObjectCollection results = new ObjectCollection("Results" , "*");
			String sql = "SELECT * FROM AccountMovement WHERE OrganisationMember=" + om1.getName();
			ocs.queryData(sql, results);
			
			
			for(BasicClass record : results.getObjects()){
				html.append(record.getString("DATE") + "," + record.getString("AMOUNT") + "<br>");	
			}
			
			html.append("</td><td>");
			results.removeObjects();
			sql = "SELECT * FROM AccountMovement WHERE OrganisationMember=" + om2.getName();
			ocs.queryData(sql, results);
			
			for(BasicClass record : results.getObjects()){
				html.append(record.getString("DATE") + "," + record.getString("AMOUNT") + "<br>");	
			}
			
			html.append("</td></tr>");
			
			html.append("<tr><td></td><td><input type=\"button\" onclick=\"getNextNode('accountmovements=true')\" value=\"Buchungen übernehmen\"></td></tr>");	
			
			//----------------------------------------------------------	
			
			html.append("<tr><th class=\"tableheader\" colspan=2>Feedbacks</th></tr>");
			
			html.append("<tr><td>");
			
			for(BasicClass o : om1.getObjects("Feedback")){
				html.append(o + "<br>");	
			}
			
			results = new ObjectCollection("Results" , "*");
			sql = "SELECT * FROM Feedback WHERE OrganisationMember=" + om1.getName();
			ocs.queryData(sql, results);
			
			
			for(BasicClass record : results.getObjects()){
				html.append(record.getString("DATECREATED") + "," + record.getString("ORGANISATIONMEMBERID") + "<br>");	
			}
				
				
			html.append("</td><td>");
			
			for(BasicClass o : om2.getObjects("Feedback")){
				html.append(o + "<br>");	
			}
			
			results = new ObjectCollection("Results" , "*");
			sql = "SELECT * FROM Feedback WHERE OrganisationMember=" + om2.getName();
			ocs.queryData(sql, results);
			
			
			for(BasicClass record : results.getObjects()){
				html.append(record.getString("DATECREATED") + "," + record.getString("ORGANISATIONMEMBERID") + "<br>");	
			}
				
			html.append("</td></tr>");
			
			html.append("<tr><td></td><td><input type=\"button\" onclick=\"getNextNode('feedbacks=true')\" value=\"Feedbacks übernehmen\"></td></tr>");	
			
			
			html.append("</table>");
		
		}
		html.append("</td></tr>");
		
		
		return html.toString();	
	}
	
	private void addCheckBoxRow(StringBuilder html, String propertyName, BasicClass owner){
		html.append("<tr><td colspan=\"2\">");
		
		if(owner.hasProperty(propertyName)){
			Property p = owner.getProperty(propertyName);
			html.append("<input type=\"checkbox\" value=\"true\">" + p.getLabel());	
		}
		
		html.append("</td></tr>");
	}
	private void addTableRow(StringBuilder html, String propertyName, BasicClass o1, BasicClass o2){
		addTableRow(html, propertyName, o1, o2, false);
	}
	private void addTableRow(StringBuilder html, String propertyName, BasicClass o1, BasicClass o2, boolean merge){
		html.append("<tr>");
		if(o1 != null){
			html.append("<td>" + o1.getString(propertyName) + "</td>");
		}
		else{
			html.append("<td></td>");
		}
		
		if(o2 != null){
			html.append("<td>" + o2.getString(propertyName) + "</td>");
		}
		else{
			html.append("<td></td>");
		}
		if(o2 != null && merge){
			if(o2 instanceof Contact){
				html.append("<td><input type=\"button\" onclick=\"getNextNode('mergecontact=true&contacttype=" + o2.getID("Type") + "')\" value=\"&Uuml;bernehmen\"></td>");
			}
			else{
				html.append("<td><input type=\"button\" onclick=\"getNextNode('mergeproperty=true&propertyname=" + propertyName + "')\" value=\"&Uuml;bernehmen\"></td>");
			}
		}
			
		html.append("</tr>");	
	}
	
}