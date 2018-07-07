package ch.opencommunity.feedback;

import ch.opencommunity.common.OpenCommunityUserSession;
import ch.opencommunity.server.OpenCommunityServer;
import ch.opencommunity.base.OrganisationMemberInfo;

import org.kubiki.ide.BasicProcess;
import org.kubiki.base.BasicClass;
import org.kubiki.base.ProcessResult;
import org.kubiki.base.ActionHandler;
import org.kubiki.base.ActionResult;
import org.kubiki.base.ObjectCollection;
import org.kubiki.application.ApplicationContext;
import org.kubiki.application.server.ApplicationServer;

import java.util.List;


public class FeedbackFinalize extends BasicProcess implements ActionHandler{
	
	OpenCommunityUserSession userSession = null;
	ApplicationServer server = null;
	FeedbackRecord feedbackRecord;
	ObjectCollection results = null;
	
	OrganisationMemberInfo ominfo = null;
	
	public FeedbackFinalize(){
		
		addNode(this);
		
		addProperty("feedbackid", "String", "");
		addProperty("FollowupNeeded", "Boolean", "");
		
		addObjectCollection("FeedbackRecord", "ch.opencommunity.feedback.FeedbackRecord");
		
		results = addObjectCollection("Results", "*");
		
		setCurrentNode(this);
		
	}
	public void initProcess(ApplicationContext context){
		
		server = (ApplicationServer)getRoot();
		userSession = (OpenCommunityUserSession)context.getObject("usersession");
		if(getID("feedbackid") > 0){
			
			feedbackRecord = (FeedbackRecord)server.getObject(this, "FeedbackRecord", "ID", Integer.toString(getID("feedbackid")));
			if(feedbackRecord != null){
				
				List<String> propertyNames = feedbackRecord.getPropertySheet().getNames();
				for(String propertyName : propertyNames){
					
					if(!propertyName.equals("name")){
						addProperty(feedbackRecord.getProperty(propertyName));
						
					}
					
				}
				
			}
			
			if(getID("OrganisationMember") > 0){
				ominfo = ((OpenCommunityServer)server).getOrganisationMemberInfo(getString("OrganisationMember"));
			}
				
			
		}
		
		
	}
	public boolean validate(ApplicationContext context){
		if(getID("OrganisationMember") < 1){
			if(context.hasProperty("OrganisationMember") && context.getString("OrganisationMember").length()> 0){
				return true;
			}
			else{
				setComment("Kein Benutzer ausgewählt!");
				return false;
			}
		}
		else{
			return true;	
		}
		
	}
	
	public ActionResult onAction(BasicClass src, String command, ApplicationContext context){
		ActionResult result = null;
		server.logAccess(command);
		if(command.equals("searchFeedbackTarget")){
			String familyname = context.getString("Familyname");
			String firstname = context.getString("Firstname");
			String street = context.getString("Street");
			String city = context.getString("City");
			if(familyname.length() > 2 || firstname.length() > 2 || street.length() > 2 || city.length() > 2){

				String searchResults = getSearchResults(familyname, firstname, street, city, getString("OrganisationMember"));
					
				if(searchResults.length() > 0){
					result = new ActionResult(ActionResult.Status.OK, "");
					result.setParam("dataContainer", "searchResults");
					result.setData(searchResults);
				}
				
				
			}
			
		}
		
		return result;
	}
	public String getSearchResults(String familyname, String firstname, String street, String city){
		return getSearchResults(familyname, firstname, street, city, null);
	}
	public String getSearchResults(String familyname, String firstname, String street, String city, String omid){
		
		StringBuilder html = new StringBuilder();
		
		String sql = "SELECT t1.ID, t3.Familyname, t3.Firstname, t4.Street, t4.Number, t4.Zipcode, t4.City";
		sql += " FROM OrganisationMember AS t1";
		sql += " JOIN Person AS t2 ON t1.Person=t2.ID";
		sql += " JOIN Identity AS t3 ON t3.PersonID=t2.ID";
		sql += " JOIN Address AS t4 ON t4.PersonID=t2.ID";
		sql += " WHERE t3.Familyname ILIKE '%" + familyname + "%' AND t3.Firstname ILIKE '%" + firstname + "%' AND t4.Street ILIKE '%" + street + "%' AND t4.City ILIKE '%" + city + "%'";
				
		results.removeObjects();
				
		server.queryData(sql, results);
				
		if(results.getObjects().size() > 0){
					
					
					
			html.append("<table>");
			for(BasicClass record : results.getObjects()){
				html.append("<tr>");	
				String id = record.getString("ID");
				if(omid != null && omid.equals(id)){
					html.append("<td><input type=\"radio\" name=\"OrganisationMember\" value=\"" + record.getString("ID") + "\" CHECKED></td>");
				}
				else{
					html.append("<td><input type=\"radio\" name=\"OrganisationMember\" value=\"" + record.getString("ID") + "\"></td>");	
				}
				html.append("<td>" + record.getString("ID") + "</td>");
				html.append("<td>" + record.getString("FAMILYNAME") + "</td>");
				html.append("<td>" + record.getString("FIRSTNAME") + "</td>");
				html.append("<td>" + record.getString("STREET") + "</td>");
				html.append("<td>" + record.getString("NUMBER") + "</td>");	
				html.append("<td>" + record.getString("ZIPCODE") + "</td>");
				html.append("<td>" + record.getString("CITY") + "</td>");
				html.append("</tr>");							
			}
			html.append("</table>");	
		}
		return html.toString();
		
	}
	@Override
	public void finish(ProcessResult result, ApplicationContext context){
		feedbackRecord.setProperty("Status", 1);
		server.updateObject(feedbackRecord);	
		result.setParam("refresh", "currentsection");
	}
	public String getPath(String path){
		return "/WebApplication/currentprocess";	
	}
	public OrganisationMemberInfo getOrganisationMemberInfo(){
		return ominfo;
	}
	
	
}