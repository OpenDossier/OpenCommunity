package ch.opencommunity.process;

import ch.opencommunity.server.OpenCommunityServer;
import ch.opencommunity.base.ObjectTemplate;
import ch.opencommunity.base.ObjectTemplateAdministration;

import org.kubiki.ide.BasicProcess;
import org.kubiki.ide.BasicProcessNode;
import org.kubiki.base.Property;
import org.kubiki.base.ProcessResult;
import org.kubiki.application.ApplicationContext;

public class ObjectTemplateAdd extends BasicProcess{
	
	BasicProcessNode node1;
	
	public ObjectTemplateAdd(){
		
		node1 = addNode();
		
		Property p = addProperty("Title", "String", "");
		node1.addProperty(p);
		
		p = addProperty("Type", "Integer", "1");
		node1.addProperty(p);
		
		setCurrentNode(node1);
	}
	public void finish(ProcessResult result, ApplicationContext context){
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		ObjectTemplate ot = new ObjectTemplate();
		ot.mergeProperties(this);
		
		ObjectTemplateAdministration ota = (ObjectTemplateAdministration)ocs.getObjectByName("ObjectTemplateAdministration", "1");
		if(ota != null){
			ot.addProperty("ObjectTemplateAdministrationID", "String", "1");
			String id = ocs.insertObject(ot);
			ocs.getObject(ota, "ObjectTemplate", "ID", id);
			result.setParam("refresh", "objecttemplates");
		}
		
	}
}