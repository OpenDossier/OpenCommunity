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
import org.kubiki.xml.*;
 
import java.util.Vector;
import java.util.List;
 
 
public class FeedbackFinalize extends BasicProcess{
	
	BasicProcessNode node1;
	FeedbackForm feedbackForm;
	MemberAd ma = null;
	FreeTextFeedback ftf = null;
	Feedback fb = null;
	Vector<ConfigValue> reasons;
	
	public FeedbackFinalize(){
		
		setTitle("Feedback bearbeiten");
		
		node1 = new BasicProcessNode(){
			public boolean validate(ApplicationContext context){
				return getProcess().validate(context);	
			}
				
		};
		addNode(node1);
		
		Property p = addProperty("FeedbackForm", "WebForm", "");
		node1.addProperty(p);
		
		addProperty("feedbackid", "String", "");
		
		setCurrentNode(node1);
		addProperty("Reason", "Integer", "");
		
		addProperty("DeleteFeedback", "Boolean", "false");
		
	}
	public void initProcess(){
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		
		reasons = new Vector<ConfigValue>();
		reasons.add(new ConfigValue("1", "1", "Nicht erreicht"));
		reasons.add(new ConfigValue("2", "2", "Unzuverlässig"));
		reasons.add(new ConfigValue("3", "3", "Keine Zeit"));
		reasons.add(new ConfigValue("4", "4", "Passt persönlich nicht"));
		reasons.add(new ConfigValue("5", "5", "Passt fachlich nicht"));
		reasons.add(new ConfigValue("6", "6", "Anderes"));
		getProperty("Reason").setSelection(reasons);
		
		fb = (Feedback)ocs.getObject(null, "Feedback", "ID", getString("feedbackid"));
		
		ocs.logAccess("Feedback : " + fb);
		
		addProperty("person_" + fb.getName() + "_OrganisationMember", "String", "");
		
		feedbackForm = new FeedbackForm();
		feedbackForm.setParent(this);
		feedbackForm.initObjectLocal();
		setProperty("FeedbackForm", feedbackForm);
		
		/*
		
		ma = (MemberAd)ocs.getObject(null, "MemberAd", "ID", getString("memberadid"), false);
		
		
		if(ma != null){
			ma.setParent(this);
			ma.initObjectLocal();
			
			String feedback = ma.getString("FeedbackForm");
			XMLParser parser = ocs.getParser();
			XMLElement xmldoc = parser.parseString(feedback);
			if(xmldoc.getChild(0) != null && xmldoc.getChild(0).getName().equals("ch.opencommunity.process.FeedbackForm")){
			
				feedbackForm = new FeedbackForm();
				feedbackForm.setProperties(xmldoc.getChild(0));
				feedbackForm.parseSubelements(xmldoc.getChild(0));
				feedbackForm.setParent(this);
				feedbackForm.initObjectLocal();
				setProperty("FeedbackForm", feedbackForm);
				
			}
		}
		else{
			ftf = (FreeTextFeedback)ocs.getObject(null, "FreeTextFeedback", "ID", getString("feedbackid"), false);
			if(ftf != null){
				ftf.setParent(this);
				ftf.initObjectLocal();
				
				String feedback = ftf.getString("FeedbackForm");
				XMLParser parser = ocs.getParser();
				XMLElement xmldoc = parser.parseString(feedback);
				if(xmldoc.getChild(0) != null && xmldoc.getChild(0).getName().equals("ch.opencommunity.process.FeedbackForm")){
				
					feedbackForm = new FeedbackForm();
					feedbackForm.setProperties(xmldoc.getChild(0));
					feedbackForm.parseSubelements(xmldoc.getChild(0));
					feedbackForm.setParent(this);
					feedbackForm.initObjectLocal();
					setProperty("FeedbackForm", feedbackForm);
					
				}
				
			}
		}
		*/
		

		

	}
	public boolean validate(ApplicationContext context){
		return true;
	}
	public void setProperty(String name, Object value){
		//if(fb != null && !name.equals("FeedbackForm") && fb.hasProperty(name)){
		
		String altname = null;
		if(fb != null){
			altname= name.replace("person_" + fb.getName() + "_", "");
		}
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		ocs.logAccess(name + ":" + altname);
		
		if(fb != null && altname != null && !name.equals("FeedbackForm") && fb.hasProperty(altname)){
			
			fb.setProperty(altname, value);	
			
		}
		else{
			super.setProperty(name, value);	
		}
	}
	public List<String> getPropertyNames(){
		
		List<String> names = new Vector<String>();
		if(fb != null){
			for(String name : fb.getPropertyNames()){
				
				names.add("person_" + fb.getName() + "_" + name);
					
			}
			
		}
		names.add("DeleteFeedback");
		
		return names;
		
	}
	public Feedback getFeedback(){
		return fb;
	}	
	public void finish(ProcessResult result, ApplicationContext context){
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		
		if(getBoolean("DeleteFeedback")){
			ocs.executeCommand("DELETE FROM Feedback WHERE ID=" + fb.getName());
			result.setParam("refresh", "currentsection");
		}
		else{	
			//if(getString("person_" + fb.getName() + "_OrganisationMember").length() > 0){
			if(fb.getString("OrganisationMember").length() > 0){
				//fb.setProperty("OrganisationMember", getString("person_" + fb.getName() + "_OrganisationMember"));
				fb.setProperty("Status", "1");
				
				result.setParam("refresh", "currentsection");
			}
			ocs.updateObject(fb);
		}
		
		/*
		
		feedbackForm.saveForm(context);
		
		for(BasicClass fp : feedbackForm.getObjects("FeedbackPerson")){
			if(fp.getString("OrganisationMember").length () > 0 && fp.getInt("Status")==0){
				Feedback feedback = new Feedback();
				feedback.mergeProperties(fp);
				if(ma != null){
					feedback.addProperty("MemberAdID", "String", ma.getName());
				}
				else if(ftf != null){
					feedback.addProperty("OrganisationMemberID", "String", ftf.getString("OrganisationMemberID"));
				}
				feedback.setProperty("DateCreated", DateConverter.dateToSQL(new java.util.Date(), true));
				ocs.insertObject(feedback);
				fp.setProperty("Status", "1");
			}
		}


		if(ma != null){
				
			ma.setProperty("FeedbackForm", feedbackForm.getXMLString(true));
			//ma.setProperty("FeedbackStatus", 1);

			ocs.updateObject(ma);
		}
		else if(ftf != null){
				
			ftf.setProperty("FeedbackForm", feedbackForm.getXMLString(true));
			//ma.setProperty("FeedbackStatus", 1);

			ocs.updateObject(ftf);
		}
		*/
		
	}
	
}