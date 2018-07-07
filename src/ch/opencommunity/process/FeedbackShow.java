package ch.opencommunity.process;

import ch.opencommunity.server.OpenCommunityServer;

import org.kubiki.ide.BasicProcess;
import org.kubiki.ide.BasicProcessNode;

import org.kubiki.base.ProcessResult;
import org.kubiki.base.ConfigValue;

import org.kubiki.application.ApplicationContext;



public class FeedbackShow extends BasicProcess{
	
	
	
	public FeedbackShow(){
		
		setTitle("");
		
		addNode(this);
		
		addProperty("TextBlockID", "String", "", true, "");
		
		addProperty("Title", "String", "", true, "");
		addProperty("Text", "String", "", true, "");
		
		setCurrentNode(this);
		
		showDefaultButtons = false;
		
	}
	public void initProcess(ApplicationContext context){
	
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		if(getID("TextBlockID") > 0){
			String feedback = ocs.getTextblockContent(getString("TextBlockID"));
		
			setComment(feedback);
		}
		else{
			setTitle(decode(getString("Title")));	
			setComment(decode(getString("Text")));
		}
		
		
		
	}
	public void finish(ProcessResult result, ApplicationContext context){
	
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		result.setParam("refresh", "currentpage");	
		
		
		
	}
	
}