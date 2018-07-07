package ch.opencommunity.feedback;

import ch.opencommunity.base.OrganisationMember;
import ch.opencommunity.common.OpenCommunityUserSession;

import org.kubiki.ide.BasicProcess;
import org.kubiki.base.ProcessResult;
import org.kubiki.application.ApplicationContext;
import org.kubiki.application.server.ApplicationServer;
import org.kubiki.util.DateConverter;

import java.util.List;
import java.util.ArrayList;


public class FeedbackAdd extends BasicProcess{
	
	int page = 1;
	ApplicationServer server = null;
	
	OpenCommunityUserSession userSession = null;
	
	public FeedbackAdd(){
		
		setTitle("Feedback");
		
		addNode(this);	
		
		addProperty("Familyname", "String", "");
		addProperty("Firstname", "String", "");		
		addProperty("Street", "String", "");	
		addProperty("City", "String", "");	
		addProperty("ContactEstablished", "Boolean", "");	
		addProperty("ContactNotEstablishedReason", "Integer", "");
		addProperty("ContactNotEstablishedReasonDetail", "IntegerArray", "");
		
		addProperty("ContactQuality", "Integer", "");
		addProperty("ContactDescription", "Text", "");
		addProperty("Comment", "Text", "");
		
		setCurrentNode(this);
		
	}
	public void initProcess(ApplicationContext context){
		
		server = (ApplicationServer)getRoot();
		userSession = (OpenCommunityUserSession)context.getObject("usersession");
		
	}
	public boolean validate(ApplicationContext context){
		
		setComment("");
		if(page==1){		
			if(getString("Familyname").length() < 3 || getString("Firstname").length() < 2){
				setComment("Beschreiben Sie bitten den Namen der Person an!");
				return false;			
			}
			else if(context.hasProperty("ContactEstablished") && context.getString("ContactEstablished").equals("true")){
				page = 2;
				return false;				
			}
			else if(context.hasProperty("ContactEstablished") && context.getString("ContactEstablished").equals("false")){
				page = 3;
				return false;			
			}
			else{
				return false;	
			}
		}
		else if(page==2){
			if(getString("ContactDescription").length() < 10){
				setComment("Beschreiben Sie bitten den Einsatz!");
				return false;				
			}
			else{
				return true;
			}			
			
		}
		else if(page==3 && context.hasProperty("ContactNotEstablishedReason") && context.hasProperty("ContactNotEstablishedReasonDetail")){
			
			String reason = context.getString("ContactNotEstablishedReason");
			setProperty("ContactNotEstablishedReason", reason);
			
			String[] values = context.getStringArray("ContactNotEstablishedReasonDetail");
			
			List<Integer> valueList = new ArrayList<Integer>();
			
			for(String value : values){
				if(value.startsWith(reason)){
					server.logAccess("value : " + value);	
					try{
						valueList.add(new Integer(value));
					}
					catch(java.lang.Exception e){
						server.logException(e);	
					}
				}
			}
						
			Integer[] integerarray = valueList.toArray(new Integer[valueList.size()]);
			setProperty("ContactNotEstablishedReasonDetail", integerarray);

			
			//return true;
			
			if(getString("Comment").length() < 10){
				setComment("Geben Sie bitte einen Kommentar an!");
				return false;				
			}
			else{
				return true;
			}
		}
		else{
			return true;	
		}
		
		
	}
	public int getPage(){
		return page;	
	}
	public void finish(ProcessResult result, ApplicationContext context){
		
		if(userSession != null && userSession.getOrganisationMember() != null){
			
			String now = DateConverter.dateToSQL(new java.util.Date(), true);
		
			FeedbackRecord feedbackRecord = new FeedbackRecord();
			
			feedbackRecord.addProperty("OrganisationMemberID", "String", userSession.getOrganisationMember().getName());
			
			feedbackRecord.setProperty("DateCreated", now);
			
			feedbackRecord.setProperty("Familyname", getObject("Familyname"));
			feedbackRecord.setProperty("Firstname", getObject("Firstname"));
			feedbackRecord.setProperty("Street", getObject("Street"));
			feedbackRecord.setProperty("City", getObject("City"));
			
			feedbackRecord.setProperty("Comment", getObject("Comment"));
			feedbackRecord.setProperty("ContactEstablished", getObject("ContactEstablished"));
			
			if(page==2){
				feedbackRecord.setProperty("ContactQuality", getObject("ContactQuality"));	
				feedbackRecord.setProperty("ContactDescription", getObject("ContactDescription"));			
				feedbackRecord.setProperty("ContactNotEstablishedReasonDetail", new Integer[0]);			
			}
			else if(page==3){
				feedbackRecord.setProperty("ContactNotEstablishedReason", getID("ContactNotEstablishedReason"));			
				feedbackRecord.setProperty("ContactNotEstablishedReasonDetail", getObject("ContactNotEstablishedReasonDetail"));
			
			}
			String id= server.insertObject(feedbackRecord);
			
			feedbackRecord = (FeedbackRecord)server.getObject(null, "FeedbackRecord", "ID", id);
			if(feedbackRecord != null){
				server.logAccess(feedbackRecord.getObject("ContactNotEstablishedReasonDetail").getClass().getName());	
			}
			
		}
		
	}
	
	
}