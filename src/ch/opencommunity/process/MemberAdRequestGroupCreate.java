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
import org.kubiki.database.Record;
import org.kubiki.util.DateConverter;
import org.kubiki.application.server.WebApplicationContext;
 
import org.kubiki.application.*;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;

import javax.servlet.http.*;
 
 
public class MemberAdRequestGroupCreate extends BasicProcess{
	
	BasicProcessNode node1; 
	ObjectCollection results;
	
	LinkedHashMap comments;
	
	public MemberAdRequestGroupCreate(){
		
		setTitle("Kontaktbestellung abschliessen");
		setLastButtonLabel("Kontaktbestellung absenden");
		
		node1 = new BasicProcessNode(){
			
			public boolean validate(ApplicationContext context){
				return getProcess().validate(context);
			}	
			
		};
		
		addNode(node1);
		setCurrentNode(node1);
		
		results = addObjectCollection("Results", "*");
		
		node1.setComment("Sie haben Adressen zu den untenstehenden Kategorien ausgewählt. <br>Bitte geben Sie zu jeder Kategorie an, was Sie suchen<br><br>");
		
	}
	public boolean validate(ApplicationContext context){
		
		boolean success = true;
		
		for(Object o : comments.keySet()){
			String comment = (String)comments.get(o);
			if(hasProperty(comment)){
				if(getString(comment).trim().length() < 5){
					success = false;
					node1.setComment("Bitte geben Sie zu jeder Kategorie an, was Sie suchen");
				}
			}
		}
		
		return success;
		
	}
 	public void initProcess(ApplicationContext context){
 		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		
		WebApplicationContext webcontext= (WebApplicationContext)context;
		
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
		
		OrganisationMember om = userSession.getOrganisationMember();	
		
		LinkedHashMap categories = new LinkedHashMap();
		comments = new LinkedHashMap();
		
		if(userSession.getMemberAdIDs().size() > 0){
				
			String adids = "(";
			for(Object adid : userSession.getMemberAdIDs()){
				adids += adid + ",";
			}
					
			adids = adids.substring(0, adids.length()-1) + ")";
					
			ObjectCollection results = new ObjectCollection("Results", "*");
					
			String sql = "SELECT t1.ID, t1.Title, t4.Sex, t4.DateOfBirth, t5.ZipCode, t5.City, t6.ID AS MacID, t6.Title AS Category FROM MemberAd AS t1";
					
			sql += " LEFT JOIN OrganisationMember AS t2 ON t1.OrganisationMemberID=t2.ID";
			sql += " LEFT JOIN Person AS t3 ON t2.Person=t3.ID";
			sql += " LEFT JOIN Identity AS t4 ON t4.PersonID=t3.ID";
			sql += " LEFT JOIN Address AS t5 ON t5.PersonID=t3.ID";
					
			sql += " LEFT JOIN MemberAdCategory AS t6 ON t1.Template = t6.ID";
					
			sql += " WHERE t1.ID IN " + adids;
			
			ocs.queryData(sql, results);
			
			for(BasicClass bc : results.getObjects()){
				
				String mac = bc.getString("MACID");
				String category = bc.getString("CATEGORY");
				categories.put(mac, category);
				
				String mar = bc.getString("ID");
				comments.put(mar, category);
			}
			
		}
		for(Object o : categories.keySet()){
			String cat = (String)categories.get(o);
			Property p = addProperty(cat.toString(), "Text", "", false, "<img src=\"res/icons/" + o + "_weiss.png\"> " + cat);
			//Property p = addProperty(cat.toString(), "Text", "", true, cat);
			node1.addProperty(p);
		}
 		
 	}
	public void finish(ProcessResult result, ApplicationContext context){
		
		
		for(Object o : comments.keySet()){
			String comment = (String)comments.get(o);
			if(hasProperty(comment)){
				comments.put(o, getString(comment));
			}
		}
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
		
		OpenCommunityUserProfile ocup = ocs.getUserProfile();
		
		OrganisationMember om = userSession.getOrganisationMember();	
		
		if(om.getActiveRelationship() != null){
			om = om.getActiveRelationship();	 
 	 	}
		
		MemberAdRequestGroup marg = (MemberAdRequestGroup)om.createObject("ch.opencommunity.advertising.MemberAdRequestGroup", null, context);
		String id = ocs.insertObject(marg);
		
		ocup.createMemberAdRequests(context, null, id, comments, 0);
		
		String feedback = ocs.getTextblockContent("25", true);

		//result.setParam("feedback", "Ihre Bestellung wurde registriert. Sobald die Kontakte freigeschaltet worden sind, werden Sie benachrichtigt.");
		
		//result.setParam("feedback", feedback);
		result.setParam("refresh", "merkliste");
		result.setParam("newprocess", "ch.opencommunity.process.FeedbackShow");
		result.setParam("newprocessparams", "TextBlockID=25");	
	}
	
}