package ch.opencommunity.advertising;

import ch.opencommunity.server.*;

import org.kubiki.ide.BasicProcess;
import org.kubiki.ide.BasicProcessNode;
import org.kubiki.base.BasicClass;
import org.kubiki.base.Property;
import org.kubiki.base.ProcessResult;
import org.kubiki.application.ApplicationContext;

import org.kubiki.cms.ImageObject;


public class AdvertisementAdd extends BasicProcess{
	
	
	public AdvertisementAdd(){
		BasicProcessNode node1 = addNode();
		
		Property p = addProperty("OrganisationalUnit", "Integer", "", false, "Organisation");
		node1.addProperty(p);
		
		setCurrentNode(node1);
		
	}
	public void initProcess(){
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		getProperty("OrganisationalUnit").setSelection(ocs.getObjects("OrganisationalUnit"));
	}
	public void finish(ProcessResult result, ApplicationContext context){
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		Advertising advertising = (Advertising)ocs.getObjectByName("ApplicationModule", "Advertising");
		
		Advertisement advertisement = (Advertisement)ocs.createObject("ch.opencommunity.advertising.Advertisement", null, context);
		advertisement.mergeProperties(this);
		String id = ocs.insertObject(advertisement);
		
		ImageObject io = (ImageObject)ocs.createObject("org.kubiki.cms.ImageObject", null, context);
		io.addProperty("AdvertisementID", "String", id);
		ocs.insertObject(io);
		
		ocs.getObject(advertising, "Advertisement", "ID", id);
		
		result.setParam("refresh", "tree");
		
	}
	
	
}