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

import javax.servlet.http.*;
 
 
 public class MemberAdRequestCreate extends BasicProcess{ 
	
	BasicProcessNode node1;
	OrganisationMember om;
	
	String mac = null;
	
	public MemberAdRequestCreate(){
		
		node1 = new BasicProcessNode(){
			public boolean validate(ApplicationContext context){
				return getProcess().validate(context);	
			}
		};
		node1.setName("MemberAdRequestCreateNode");
		addNode(node1);
	    setCurrentNode(node1);
	    
	    addProperty("MemberAdIDs", "String", "");
	}
 
 
 	public void initProcess(ApplicationContext context){
		MemberAdAdministration maa = (MemberAdAdministration)getParent();


		/*
		getProperty("Category").setSelection(maa.getObjects("MemberAdCategory"));
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
		if(userSession != null){
			om = userSession.getOrganisationMember();	
		}

		if(om != null){
			addNode(node3);
			setCurrentNode(node3);			
		}
		else{
			addNode(node1);
			addNode(node3);
			setCurrentNode(node1);		
		}
		*/
	}
	public void setOrganisationMember(OrganisationMember om){
		this.om = om;	
	}
	public boolean validate(ApplicationContext context){
		if(context.hasProperty("mac")){
			mac = context.getString("mac");
			return false;
		}
		else{
			return true;	
		}
	}
	public String getMemberAdRequestCreateForm(ApplicationContext context){
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
		WebApplicationContext webcontext= (WebApplicationContext)context;		
		HttpServletRequest request = webcontext.getRequest();
		
		StringBuilder html = new StringBuilder();
		
		ObjectCollection results = new ObjectCollection("Results", "*");
		String prevzipcode = "";
		/*
		
		String sql = "SELECT t1.*,t2.Title AS Templatetitle FROM MemberAd AS t1 LEFT JOIN MemberAdCategory AS t2 ON t1.Template=t2.ID";
		
		String ids = "";
		for(Object o : userSession.getMemberAdIDs()){
			ids += o + ",";
		}
		ocs.logAccess(ids);
		ids = ids.substring(0, ids.length()-1);
		sql += " WHERE t1.ID IN (" + ids + ")";
		ocs.queryData(sql, results);
		

		
		for(BasicClass record : results.getObjects()){
			html.append("<br><b>" + record.getString("TEMPLATETITLE") + "</b>");
			html.append("<br>" + record.getString("TITLE"));
			html.append("<br>" + record.getString("DESCRIPTION"));
			html.append("<hr>");
		}
		
		html.append("<table>");
		
		html.append("</table>");
		
		*/
		
		html.append("<input class=\"rightButton\" type=\"button\" value=\"" + "Abbrechen" + "\" onClick=\"cancelProcess()\">");
		html.append("<input class=\"rightButton\" type=\"button\" value=\"" + "Abschliessen" + "\" onClick=\"getNextNode()\">");		
		
		html.append("<br><select name=\"category\" id=\"category\" class=\"selectbig\" onchange=\"getNextNode(\'mac=\' + this.value)\">");
		html.append("<option value=\"0\">Was? Rubrik wählen</option>");
		for(BasicClass bc :  ocs.getMemberAdAdministration().getObjects("MemberAdCategory")){
			html.append("<option value=\"" + bc.getName() + "\">" + bc.getString("Title") + "</option>");			
		}
		html.append("</select>");
		
		if(mac != null){
			
			html.append("<form id=\"processNodeForm\" name=\"processNodeForm\">");
			String sql = "SELECT DISTINCT t1.ID, t1.Title, t1.Description, t1.ValidFrom, t1.ValidUntil, t1.IsOffer, t1.IsRequest, t4.ZipCode, t4.City, t5.Sex, t5.DateOfBirth FROM MemberAd AS t1";
				
			sql += " LEFT JOIN OrganisationMember AS t2 ON t1.OrganisationMemberID=t2.ID";
			sql += " LEFT JOIN Person AS t3 ON t2.Person=t3.ID";
			sql += " LEFT JOIN Address AS t4 ON t4.PersonID=t3.ID";
			sql += " LEFT JOIN Identity AS t5 ON t5.PersonID=t3.ID";
			sql += " LEFT JOIN Parameter AS t6 ON t6.PersonID=t3.ID";
			
			sql += " WHERE t1.template=" + mac;
			
			ocs.queryData(sql, results);
		
			for(int i = 0; i < results.getObjects().size(); i++){
				
				BasicClass record = results.getObjects().elementAt(i);
				
				String zipcode = record.getString("ZIPCODE");
				
				String sex2 = "Mann";
				String sex = record.getString("SEX");
				if(sex.equals("2")){
					sex2 = "Frau";	
				}
				
				int age = 48;
				String dateofbirth = record.getString("DATEOFBIRTH");
				if(dateofbirth.length() > 3){
					String[] args = dateofbirth.split("\\.");
					String syear = args[args.length-1];
					int year = Integer.parseInt(syear);
					age = 2015 - year;
				}
				
				
				if(!zipcode.equals(prevzipcode)){
					html.append("\n<div class=\"searchresultheader\">");
					html.append(zipcode + " " + record.getString("CITY"));
					html.append("</div>");
				}
				html.append("<div id=\"" + record.getString("ID") + "\" class=\"searchresult\" onclick=\"showaddetail(this.id)\">");
				html.append("<span id=\"" + record.getString("ID") + "_title\">" +  sex2 + " (" + age + ") " + record.getString("TITLE") + "</span>");
					/*
					if(userSession.hasMemberAdID(record.getString("ID"))){
						html.append("<img class=\"merken\" src=\"" + ocs.getBaseURL(request) + "/opencommunity/websites/1/images/picto/merken_aktiv.png\"  onclick=\"selectAd(" + record.getString("ID") + ")\">");
					}	
					else{
						html.append("<img class=\"merken\" src=\"" + ocs.getBaseURL(request) + "/opencommunity/websites/1/images/picto/merken.png\"  onclick=\"selectAd(" + record.getString("ID") + ")\">");
					}
					*/
					html.append("<input type=\"checkbox\" id=\"MemberAdIDs\" name=\"MemberAdIDs\" value=\"" + record.getString("ID") + "\">");
					html.append("<div id=\"" + record.getString("ID") + "_detail\" class=\"searchresultdetails\">");
					
					html.append("<br>" + record.getString("DESCRIPTION"));
					
					html.append("<br><br>Zeitraum vom " + DateConverter.sqlToShortDisplay(record.getString("VALIDFROM"),false) + " - " + DateConverter.sqlToShortDisplay(record.getString("VALIDUNTIL"),false));
					html.append("<br>Sprachen");
					html.append("</div>");
				
				html.append("</div>");
				
			}
			html.append("</form>");
		}

		return html.toString();
	}
	public void finish(ProcessResult result, ApplicationContext context){
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		String[] ids = getString("MemberAdIDs").split(";");

		for(String id : ids){
			if(id.length() > 0){
				
				Date now = new Date();
				
				Calendar cal = Calendar.getInstance();
				cal.setTime(now);
				cal.add(Calendar.DATE, 90); //minus number would decrement the days
				now = cal.getTime();
				//now.setTime(now.getTime() + (90 * 24 * 60 * 60 * 1000));
				
				
				MemberAdRequest mar = (MemberAdRequest)om.createObject("ch.opencommunity.advertising.MemberAdRequest", null, context);
				mar.addProperty("OrganisationMemberID", "String", om.getName());
				mar.setProperty("MemberAd", id);
				mar.setProperty("Status", 1);
				mar.setProperty("NotificationStatus", 0);
				mar.setProperty("NotificationMode", 2);
				mar.setProperty("ValidUntil", DateConverter.dateToSQL(now, false));
				ocs.insertObject(mar);
			}
		}		
		if(getParent() instanceof BasicProcess){
			getProcess().setSubprocess(null);	
		}
	
	}
 
 
 }
