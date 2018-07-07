 package ch.opencommunity.process;
 
 import ch.opencommunity.server.*;
 import ch.opencommunity.base.*;
 import ch.opencommunity.advertising.*;
 import ch.opencommunity.common.*;
 
 import org.kubiki.ide.*;
 import org.kubiki.base.Property;
 import org.kubiki.base.ProcessResult;
 import org.kubiki.base.ConfigValue;
 
 import org.kubiki.application.*;
 
 import java.util.Vector;
 import java.sql.*;
 
 
 public class MemberAdFinalize extends BasicProcess{
 
	BasicProcessNode node1;
	FeedbackForm feedbackForm;
	
	MemberAd ma = null;
	
	OrganisationMember om = null;
 
	public MemberAdFinalize(){ 
		
		setTitle("Inserat verlängern");
		setTitle("Feedback abgeben");
		
		node1 = new FeedbackNode();	
		addNode(node1);
		
		Property p = addProperty("mode", "Integer", "1");	
		//node1.addProperty(p);
		
		p = addProperty("Time", "Integer", "180", false, "Verlängerung um");	

		addProperty("memberadid", "String", "");
		addProperty("organisationmemberid", "String", "");
		
		p = addProperty("FeedbackForm", "WebForm", "");	
		node1.addProperty(p);
		
		setCurrentNode(node1);		
	}
	public void initProcess(ApplicationContext context){
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		
		ma = (MemberAd)ocs.getObject(null, "MemberAd", "ID", getString("memberadid"), false);
		if(ma != null){
			ma.setParent(this);
			ma.initObjectLocal();
		}
		else{
			/*
			om = (OrganisationMember)ocs.getObject(null, "OrganisationMember", "ID", getString("organisationmemberid"), false);
			if(om != null){
				om.setParent(this);
				om.initObjectLocal();
			}
			*/

			OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
			if(userSession != null){
 	 	 	 
				om = userSession.getOrganisationMember();
				
			}
		}
		
		feedbackForm = new FeedbackForm();
		feedbackForm.setParent(this);
		feedbackForm.initObjectLocal();
		setProperty("FeedbackForm", feedbackForm);
		
		Vector times = new Vector();
		times.add(new ConfigValue("30", "30", "30 Tage"));
		times.add(new ConfigValue("60", "60", "60 Tage"));
		times.add(new ConfigValue("90", "90", "90 Tage"));
		times.add(new ConfigValue("180", "180", "180 Tage"));
		getProperty("Time").setSelection(times);
		if(getID("mode")==1){
			node1.addProperty(getProperty("Time"));
		}
		

	}
	class FeedbackNode extends BasicProcessNode{
		public boolean validate(ApplicationContext context){
			if(context.hasProperty("webform")){
				feedbackForm.saveForm(context);
				return false;
			}
			else{
				return true;	
			}
		}
		
	}
	public void finish(ProcessResult result, ApplicationContext context){
		
		feedbackForm.saveForm(context);
	
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		ocs.logAccess(ma + ":" + getID("mode"));
		
		if(ma != null){
			if(getID("mode")==1){
				
				String sql = "UPDATE MemberAd SET ValidUntil=(ValidUntil +  INTERVAL \'" + getInt("Time") + " days\') WHERE ID=" + ma.getName();
				ocs.executeCommand(sql);
				result.setParam("refresh", "currentpage");	
				
			}
			else{
	
					
				ma.setProperty("FeedbackForm", feedbackForm.getXMLString(true));
				ma.setProperty("FeedbackStatus", 1);
					//ocs.logAccess(feedbackForm.getXMLString());
				ocs.updateObject(ma);
				
			}
		}
		else if(om != null){
			FreeTextFeedback ftfb = new FreeTextFeedback();
			ftfb.addProperty("OrganisationMemberID", "String", om.getName());
			ftfb.setProperty("FeedbackForm", feedbackForm.getXMLString(true));
			ftfb.setProperty("Status", "0");
			ocs.insertObject(ftfb);
		}
	}
	
 }