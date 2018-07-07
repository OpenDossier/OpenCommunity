package ch.opencommunity.process;

import ch.opencommunity.server.OpenCommunityServer;
import ch.opencommunity.base.OrganisationMember;
import ch.opencommunity.base.OrganisationMemberController;
import ch.opencommunity.base.OrganisationMemberRelationship;
import ch.opencommunity.base.Contact;
import ch.opencommunity.base.Login;
import ch.opencommunity.common.OpenCommunityUserSession;


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

public class MergeProfile extends BasicProcess{
	
	OrganisationMemberEdit ome = null;
	OrganisationMemberController owner = null;
	OrganisationMemberController source = null;
	
	static String[] contacts = {"Tel. privat","Tel. gesch.","Tel. mobil","Email"};
	
	public MergeProfile(){
		
		addNode(this);
		
		addProperty("OrganisationMemberLink", "Integer", "", false, "Zu verknüpfendes Profil");
		
		addProperty("MergeIdentity", "Boolean", "false", true, "Identität übernehmen");
		addProperty("MergeAddress", "Boolean", "false", true, "Adresse übernehmen");
	
		addProperty("MergePhoneP", "Boolean", "false", true, "Privattelefon übernehmen");
		addProperty("MergePhoneB", "Boolean", "false", true, "Geschäftstelefon übernehmen");
		addProperty("MergePhoneM", "Boolean", "false", true, "Mobiltelefon übernehmen");
		addProperty("MergeEmail", "Boolean", "false", true, "Email übernehmen");
		
		addProperty("MergeActivities", "Boolean", "false", true, "Aktivitäten übernehmen");
		addProperty("MergeMemberAds", "Boolean", "false", true, "Inserate übernehmen");
		addProperty("MergeMemberRequests", "Boolean", "false", true, "Adressbestellungen übernehmen");

		
		setCurrentNode(this);
				
		
	}
	public OrganisationMemberController getOwner(){
		return owner;	
	}
	public void initProcess(){
		
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


		
	}
	public boolean validate(ApplicationContext context){
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		
		ocs.logAccess("merging activities : " + context.hasProperty("mergeactvities"));
		
		if(context.hasProperty("OrganisationMemberLink")){
			
			if(getParent() instanceof OrganisationMemberEdit){
			
				OrganisationMemberEdit ome = (OrganisationMemberEdit)getParent();
				
				
				
				for(OrganisationMemberController omc : ome.getOrganisationMemberControlers()){
					if(omc.getOrganisationMember().getName().equals(context.getString("OrganisationMemberLink"))){
						setProperty("OrganisationMemberLink", omc.getOrganisationMember());
						source = omc;
					}
					
				}
			}
			else{
				
				OpenCommunityUserSession userSession = (OpenCommunityUserSession)owner.getParent();
				for(BasicClass bc : userSession.getObjects("OrganisationMemberController")){
					OrganisationMemberController omc = (OrganisationMemberController)bc;
					if(omc.getOrganisationMember().getName().equals(context.getString("OrganisationMemberLink"))){
						setProperty("OrganisationMemberLink", omc.getOrganisationMember());
						source = omc;
					}
				}
					
			}
			
			addProperty("ProfileMergeWidget", "Object", "");
			
			getProperty("OrganisationMemberLink").setHidden(true);
			
			return false;	
		}
		else if(getID("OrganisationMemberLink") < 1){
			return false;	
		}
		else{
			if(context.hasProperty("mergeactivities") && context.getString("mergeactivities").equals("true")){
					
				ocs.logAccess("merging activities ...");
					
				if(getObject("OrganisationMemberLink") instanceof OrganisationMember){
						
						
						
					OrganisationMember om1 = owner.getOrganisationMember();
					OrganisationMember om2 = (OrganisationMember)getObject("OrganisationMemberLink");
					for(BasicClass activity : om2.getObjects("Activity")){
						activity.setProperty("OrganisationMemberID", om1.getName());	
						ocs.updateObject(activity);
						activity.setParent(om1);
						om1.addSubobject("Activity", activity);
					}
					om2.getObjectCollection("Activity").removeObjects();
				}
				return false;	
			}
			else if(context.hasProperty("mergememberads") && context.getString("mergememberads").equals("true")){
					
				ocs.logAccess("merging memberads ...");
					
				if(getObject("OrganisationMemberLink") instanceof OrganisationMember){
						
						
						
					OrganisationMember om1 = owner.getOrganisationMember();
					OrganisationMember om2 = (OrganisationMember)getObject("OrganisationMemberLink");
					for(BasicClass memberad : om2.getObjects("MemberAd")){
						memberad.setProperty("OrganisationMemberID", om1.getName());	
						ocs.updateObject(memberad);
						memberad.setParent(om1);
						om1.addSubobject("MemberAd", memberad);
					}
					om2.getObjectCollection("MemberAd").removeObjects();
				}
				return false;	
			}
			else if(context.hasProperty("mergememberadrequests") && context.getString("mergememberadrequests").equals("true")){
					
				ocs.logAccess("merging memberadrequests ...");
					
				if(getObject("OrganisationMemberLink") instanceof OrganisationMember){
						
						
						
					OrganisationMember om1 = owner.getOrganisationMember();
					OrganisationMember om2 = (OrganisationMember)getObject("OrganisationMemberLink");
					for(BasicClass memberadrequest : om2.getObjects("MemberAdRequest")){
						memberadrequest.setProperty("OrganisationMemberID", om1.getName());	
						ocs.updateObject(memberadrequest);
						memberadrequest.setParent(om1);
						om1.addSubobject("MemberAdRequest", memberadrequest);
					}
					om2.getObjectCollection("MemberAdRequest").removeObjects();
				}
				return false;	
			}
			else if(context.hasProperty("mergeproperty") && context.getString("mergeproperty").equals("true")){
				if(context.hasProperty("propertyname")){
					
					String propertyname = context.getString("propertyname");
					
					if(getObject("OrganisationMemberLink") instanceof OrganisationMember){			
						OrganisationMember om1 = owner.getOrganisationMember();
						OrganisationMember om2 = (OrganisationMember)getObject("OrganisationMemberLink");
						if(source instanceof OrganisationMemberController){
							owner.setProperty(propertyname, source.getString(propertyname));
							owner.saveOrganisationMember();
						}
					}
					
				}
				return false;
				
			}
			else if(context.hasProperty("mergelogin") && context.getString("mergelogin").equals("true")){
				if(getObject("OrganisationMemberLink") instanceof OrganisationMember){
					
						OrganisationMember om1 = owner.getOrganisationMember();
						OrganisationMember om2 = (OrganisationMember)getObject("OrganisationMemberLink");
						if(source instanceof OrganisationMemberController){
							if(om1.getObjects("Login").size()==1 && om2.getObjects("Login").size()==1){
								Login login1 = (Login)om1.getObjectByIndex("Login", 0);
								Login login2 = (Login)om2.getObjectByIndex("Login", 0);
								login1.setProperty("Username", login2.getString("Username"));
								login1.setProperty("Password", login2.getString("Password"));
								ocs.updateObject(login1);
								login2.setProperty("Username", ocs.createPassword(10));
								login2.setProperty("Password", ocs.createPassword(12));
								ocs.updateObject(login2);
							}
							else if(om1.getObjects("Login").size()==0 && om2.getObjects("Login").size()==1){
								
								
								Login login2 = (Login)om2.getObjectByIndex("Login", 0);
								
								Login login = new Login();
								
								login.addProperty("OrganisationMemberID", "String", om1.getName());

								
								String username = om1.getName();
								
								while(username.length() < 7){
									username = "0" + username;
								}
								
								login.setProperty("Username", username);
								String password = ocs.createPassword(8);
								
								//Copy username of other profile
								login.setProperty("Username", login2.getString("Username")); //Copy password
								//login.setProperty("Password", password);
								login.setProperty("Password", login2.getString("Password")); //Copy password
								
								String id = ocs.insertObject(login);
								
								login = (Login)ocs.getObject(om1, "Login", "ID", id);
								if(login != null){
									owner.addProperty(login.getProperty("Username"));
									owner.addProperty(login.getProperty("Password"));
								}
								
								login2.setProperty("Username", ocs.createPassword(10));
								login2.setProperty("Password", ocs.createPassword(12));
								ocs.updateObject(login2);
								
							}
							
							
						}
				
				}
				return false;
			}
			else if(context.hasProperty("mergecontact") && context.getString("mergecontact").equals("true")){
				
				if(context.hasProperty("contacttype")){
					
					int contacttype = Integer.parseInt(context.getString("contacttype"));
					
					if(getObject("OrganisationMemberLink") instanceof OrganisationMember){			
						OrganisationMember om1 = owner.getOrganisationMember();
						OrganisationMember om2 = (OrganisationMember)getObject("OrganisationMemberLink");
						if(source instanceof OrganisationMemberController){
							
							Contact c2 = om2.getPerson().getContact(contacttype);
							if(c2 != null){
								Contact c1 = om1.getPerson().getContact(contacttype);
								
								if(c1 == null){
									Contact contact = new Contact();
									contact.addProperty("PersonID", "String", om1.getPerson().getName());
									contact.setProperty("Type", contacttype);
									contact.setProperty("Value", c2.getString("Value"));
									String id = ocs.insertSimpleObject(contact);
									ocs.getObject(om1.getPerson(), "Contact", "ID", id);
									owner.addProperty(contacts[contacttype], "String", c2.getString("Value"));
								}
								else{
									owner.setProperty(contacts[contacttype], c2.getString("Value"));
									owner.saveOrganisationMember();
								}
							}
							
						}
						
					}
					
				}
				
				return false;				
			}
			else if(context.hasProperty("accountmovements") && context.getString("accountmovements").equals("true")){
				
				if(getObject("OrganisationMemberLink") instanceof OrganisationMember){			
					OrganisationMember om1 = owner.getOrganisationMember();
					OrganisationMember om2 = (OrganisationMember)getObject("OrganisationMemberLink");
					if(source instanceof OrganisationMemberController){
						String sql = "UPDATE AccountMovement SET OrganisationMember=" + om1.getName() + " WHERE OrganisationMember=" + om2.getName();
						ocs.executeCommand(sql);						
					}
				}
				return false;
			}
			else if(context.hasProperty("feedbacks") && context.getString("feedbacks").equals("true")){
					
				ocs.logAccess("merging feedbacks ...");
					
				if(getObject("OrganisationMemberLink") instanceof OrganisationMember){
						
						
						
					OrganisationMember om1 = owner.getOrganisationMember();
					OrganisationMember om2 = (OrganisationMember)getObject("OrganisationMemberLink");
					for(BasicClass feedback : om2.getObjects("Feedback")){
						feedback.setProperty("OrganisationMemberID", om1.getName());	
						ocs.updateObject(feedback);
						feedback.setParent(om1);
						om1.addSubobject("Feedback", feedback);
					}
					om2.getObjectCollection("Feedback").removeObjects();
					
					ocs.executeCommand("UPDATE Feedback SET OrganisationMember=" + om1.getName() + " WHERE OrganisationMember=" + om2.getName());
				}
				return false;	
			}
			else if(context.hasProperty("finish")){
				return true;
			}
			else{
				return true;
			}
			
		}
	}
	public void finish(ProcessResult result, ApplicationContext context){
		
		/*
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
		*/
		if(getParent() instanceof OrganisationMemberEdit){
			((OrganisationMemberEdit)getParent()).setSubprocess(null);
		}
	}
}