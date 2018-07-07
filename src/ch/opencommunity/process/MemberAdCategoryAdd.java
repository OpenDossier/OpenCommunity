 package ch.opencommunity.process;
 
 import ch.opencommunity.server.*;
 import ch.opencommunity.base.*;
 import ch.opencommunity.advertising.*;
 
 import org.kubiki.ide.*;
 import org.kubiki.base.Property;
 import org.kubiki.base.ProcessResult;
 
 import org.kubiki.application.*;
 
 
 public class MemberAdCategoryAdd extends BasicProcess{
 
	BasicProcessNode node1;
 
	public MemberAdCategoryAdd(){
		node1 = addNode();
		Property p = addProperty("Title", "String", "");
		node1.addProperty(p);
		setCurrentNode(node1);	

	}
	public void finish(ProcessResult result, ApplicationContext context){
	
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		
		MemberAdCategory mac = new MemberAdCategory();
		mac.mergeProperties(this);
		mac.addProperty("MemberAdAdministrationID", "String", "1");
		String id = ocs.insertObject(mac);
 		ocs.getObject(ocs.getMemberAdAdministration(), "MemberAdCategory", "ID", id);
		result.setParam("refresh", "memberadcategories");		
	}
	
}
