package ch.opencommunity.process;

import ch.opencommunity.server.OpenCommunityServer;
import ch.opencommunity.common.OpenCommunityUserSession;
import ch.opencommunity.base.OrganisationMember;
import ch.opencommunity.base.OrganisationMemberController;
import ch.opencommunity.base.OrganisationMemberRelationship;
import ch.opencommunity.view.UserProfileView;

import org.kubiki.ide.BasicProcess;
import org.kubiki.ide.BasicProcessNode;
import org.kubiki.pdf.*;

import org.kubiki.base.Property;
import org.kubiki.base.BasicClass;
import org.kubiki.base.ProcessResult;
import org.kubiki.base.ObjectCollection;
 
import org.kubiki.application.*;

import org.kubiki.util.DateConverter;

import java.util.Vector;

public class LinkProfile extends BasicProcess{
	
	OrganisationMemberEdit ome = null;
	OrganisationMemberController owner = null;
	
	public LinkProfile(){
		
		addNode(this);
		
		addProperty("OrganisationMemberLink", "Integer", "", false, "Zu verknüpfendes Profil");
		addProperty("Role", "Integer", "", false, "Rolle");
		
		setCurrentNode(this);
				
		
	}
	public void initProcess(ApplicationContext context){
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		
		Vector profiles = new Vector();
		
		if(getParent() instanceof OrganisationMemberEdit){
			ome = (OrganisationMemberEdit)getParent();
			owner = ome.getCurrentControler();

			
			for(OrganisationMemberController omc : ome.getOrganisationMemberControlers()){
				if(!omc.getOrganisationMember().equals(owner.getOrganisationMember())){
					profiles.add(omc.getOrganisationMember());
				}
				
			}
		}
		else{
			owner = (OrganisationMemberController)getParent();
			OpenCommunityUserSession userSession = (OpenCommunityUserSession)owner.getParent();
			for(BasicClass bc : userSession.getObjects("OrganisationMemberController")){
				OrganisationMemberController omc = (OrganisationMemberController)bc;
				if(!omc.getOrganisationMember().equals(owner.getOrganisationMember())){
					profiles.add(omc.getOrganisationMember());
				}
				
			}
			
		}
		getProperty("OrganisationMemberLink").setSelection(profiles);
		getProperty("Role").setSelection(ocs.getMemberRoles());

		
	}
	public boolean validate(ApplicationContext context){
		if(getID("OrganisationMemberLink") < 1){
			return false;	
		}
		else if(getID("Role") < 1){
			return false;	
		}
		else{
			return true;
		}
	}
	public void finish(ProcessResult result, ApplicationContext context){
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		
		if(getParent() instanceof OrganisationMemberEdit){
			OrganisationMemberRelationship omr = new OrganisationMemberRelationship();
			omr.mergeProperties(this);
			omr.addProperty("OrganisationMemberID", "String", owner.getOrganisationMember().getName());
			omr.setProperty("OrganisationMember", getID("OrganisationMemberLink"));
			omr.setProperty("DateCreated", DateConverter.dateToSQL(new java.util.Date(), true));
			omr.setProperty("Title", getObject("OrganisationMemberLink").toString());
			String id = ocs.insertSimpleObject(omr);
			ocs.getObject(owner.getOrganisationMember(), "OrganisationMemberRelationship", "ID", id);
			if(getObject("OrganisationMemberLink") instanceof OrganisationMember){
				((OrganisationMember)getObject("OrganisationMemberLink")).updateReverseRelationships(ocs);	
			}
			((OrganisationMemberEdit)getParent()).setSubprocess(null);
		}
		else if(getParent() instanceof OrganisationMemberController){
			OrganisationMemberController omc = (OrganisationMemberController)getParent();
			OrganisationMember om = omc.getOrganisationMember();
			OpenCommunityUserSession userSession = (OpenCommunityUserSession)omc.getParent();
			OrganisationMemberRelationship omr = new OrganisationMemberRelationship();
			omr.mergeProperties(this);
			omr.addProperty("OrganisationMemberID", "String", owner.getOrganisationMember().getName());
			omr.setProperty("OrganisationMember", getID("OrganisationMemberLink"));
			omr.setProperty("DateCreated", DateConverter.dateToSQL(new java.util.Date(), true));
			omr.setProperty("Title", getObject("OrganisationMemberLink").toString());
			String id = ocs.insertSimpleObject(omr);
			ocs.getObject( om, "OrganisationMemberRelationship", "ID", id);
			if(getObject("OrganisationMemberLink") instanceof OrganisationMember){
				((OrganisationMember)getObject("OrganisationMemberLink")).updateReverseRelationships(ocs);	
			}
			result.setParam("openprofile", om.getName());
			result.setParam("dataContainer", "editArea");
			result.setData(UserProfileView.getUserProfileView(userSession, omc, context));
			
		}
	}
}