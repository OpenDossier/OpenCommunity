 package ch.opencommunity.process;
 
 import ch.opencommunity.server.*;
 import ch.opencommunity.base.*;
 import ch.opencommunity.advertising.*;
 
import org.kubiki.ide.*;
import org.kubiki.base.Property;
import org.kubiki.base.ProcessResult;
import org.kubiki.base.ConfigValue;
import org.kubiki.base.BasicClass;
import org.kubiki.base.ObjectCollection;
 
import org.kubiki.application.*;
import org.kubiki.application.server.WebApplicationContext;
 
import java.util.Vector;
import java.sql.*;

import javax.servlet.http.*;
 
 
 public class MemberAdActivate extends BasicProcess{
 
 	MemberAdEditNode node1;
	BasicProcessNode lastNode;
	
	MemberAd ma = null;

	
	OrganisationMember om;
	
	ObjectCollection results; 
 
	public MemberAdActivate(){ 
		
		setTitle("Inserat aktivieren");
		
		Property p = addProperty("NotificationMode", "Integer", "1", false, "Informieren über");
		//node1.addProperty(p);

		
		
		//node1.addProperty(p);
		addProperty("omid", "String", "");
		
		addProperty("sectionid", "String", "");
		
		addProperty("Comment", "Text", "", false, "Nachricht");
		addProperty("Content", "Text", "", false, "Nachrichtentext");
		
		setProperty("Content", "Ihre Inserate wurden freigeschaltet");
		
		setCurrentNode(node1);
	}
	public void initProcess(){
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		
		try{
		
		om = (OrganisationMember)ocs.getObject(null, "OrganisationMember", "ID", getString("omid"), false);
		om.setParent(this);
		om.initObjectLocal();
		
		Vector channels = new Vector();
		channels.add(new ConfigValue("1","1", "Email"));
		channels.add(new ConfigValue("2","2", "Brieflich"));
		getProperty("NotificationMode").setSelection(channels);
		
		Vector status = new Vector();
		status.add(new ConfigValue("0","0","erfasst"));
		status.add(new ConfigValue("1","1","freigeschaltet"));
		status.add(new ConfigValue("3","3","inaktiv"));
		
		String sql = "SELECT * FROM MemberAd WHERE OrganisationMemberID=" + getString("omid") + " AND Status=0";
		
		sql = "SELECT t2.ID, t2.Title, t3.ID AS OMID, t5.FamilyName, t5.FirstName, t6.Title AS Category ";
		sql += " FROM MemberAd AS t2";
		sql += " LEFT JOIN OrganisationMember AS t3 ON t2.OrganisationMemberID=t3.ID";
		sql += " LEFT JOIN Person AS t4 ON t3.Person=t4.ID";
		sql += " LEFT JOIN Identity AS t5 ON t5.PersonID=t4.ID";
		sql += " LEFT JOIN MemberAdCategory AS t6 ON t6.ID=t2.Template";
		sql += " WHERE t2.OrganisationMemberID=" + getString("omid") + " AND t2.Status=0";
		
		results = addObjectCollection("Results", "*");
		ocs.queryData(sql, results);
		
		for(BasicClass o : results.getObjects()){
			

			
			//p = addProperty("NotificationMode", "Integer", "1", false, "Informieren über");
			//node1.addProperty(p);
		
			//ma = (MemberAd)ocs.getObject(null, "MemberAd", "ID", getString("memberadid"));
			ma = (MemberAd)ocs.getObject(null, "MemberAd", "ID", o.getString("ID"), false);
			ma.setParent(this);
			ma.initObjectLocal();
			
			node1 = new MemberAdEditNode(ma);
			addNode(node1);
			
			Property p = null;
			

			
			
			p = ma.getProperty("Template");
			p.setSelection(ocs.getMemberAdAdministration().getObjects("MemberAdCategory"));
			node1.addProperty(p);
			
			p = ma.getProperty("Title");
			node1.addProperty(p);
			
			p = ma.getProperty("Type");
			node1.addProperty(p);
			
			
			p = ma.getProperty("Description");
			node1.addProperty(p);
			
			p = ma.getProperty("ValidFrom");
			//addProperty(p);
			node1.addProperty(p);
			
			p = ma.getProperty("ValidUntil");
			//addProperty(p);
			node1.addProperty(p);
			
			p = ma.getProperty("Location");
			node1.addProperty(p);
			
			MemberAdCategory mac = (MemberAdCategory)ma.getObject("Template");
			
			node1.setMemberAdCategory(mac);
			node1.setInitialMemberAdCategory(mac);
			
			ocs.logAccess("memberad: " + ma.getName());
			
			if(mac != null){

				for(BasicClass bc  : mac.getObjects("FieldDefinition")){
					FieldDefinition fd = (FieldDefinition)bc;
					for(ConfigValue cv : fd.getCodeList()){
						String label = cv.getLabel();
						String[] args = label.split("/");
						p = node1.addProperty(bc.getName() + "_" + cv.getValue(), "Boolean", "" + ma.hasParameter(Integer.parseInt("" + fd.getName()), Integer.parseInt("" + cv.getValue())), false, args[0]);

						//node1.addProperty(p);
					}
				}			
			}
			
	
			
			p = ma.getProperty("Status");
			p.setValue("1");
			p.setSelection(status);
			node1.addProperty(p);
			
			setCurrentNode((BasicProcessNode)getObjectByIndex("BasicProcessNode", 0));
			
			
		}
		}
		catch(java.lang.Exception e){
			ocs.logException(e);
		}
		
		//lastNode = addNode();
	}	
	public BasicProcessNode getLastNode(){
		return lastNode;	
	}
	public void addLastNode(){
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		
		lastNode = new BasicProcessNode();
		lastNode.addProperty("MemberAdRequestList", "Object", "");
		
		String comment = ocs.getTextblockContent("5");

		comment = comment.replace("<p>", "\n");
		comment = comment.replace("</p>", "");
		comment = comment.replace("<br />", "");
		comment = comment.trim();
		String comment2 = "";
		String[] lines = comment.split("\r\n|\r|\n");
		for(String line : lines){
			comment2 += "\n" + line.trim();
		}

		setProperty("Comment", comment2);
		lastNode.addProperty(getProperty("Comment"));
		
		ocs.logAccess("adding node " + lastNode);
		
		addNode(lastNode);
		
		setCurrentNode(lastNode);
		
		
		
	}
	public MemberAd getMemberAd(){
		if(getCurrentNode() instanceof MemberAdEditNode){
			return ((MemberAdEditNode)getCurrentNode()).getMemberAd();
		}
		else{
			return null;
		}
	}
	public void finish(ProcessResult result, ApplicationContext context){
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		
		Vector<MemberAd> activatedAds = new Vector();
		
		try{

			
			for(BasicClass o : getObjects("BasicProcessNode")){
			
				if(o instanceof MemberAdEditNode){
					
					MemberAdEditNode node = (MemberAdEditNode)o;
					
					MemberAd ma = node.getMemberAd();
	
					//ma.setProperty("Title", node.getString("Title"));
					//ma.setProperty("Description", node.getString("Description"));
					
					int status = node.getID("Status");
					
					ma.setProperty("Status", status);
					
					
					if(status==1){
						
						activatedAds.add(ma);
						ma.setProperty("NotificationStatus", 1);
						
					}
					else{
						ma.setProperty("NotificationStatus", 0);
					}
					

					//ma.setProperty("NotificationMode", getID("NotificationMode"));
					
					
					

						
					
					ma.setProperty("NotificationMode", getID("NotificationMode"));
							
					MemberAdCategory mac = node.getMemberAdCategory();
						
					if(!node.getInitialMemberAdCategory().equals(mac)){
							
						ma.setProperty("Template", mac);
							
						ocs.updateObject(ma);
							
							Connection con = ocs.getConnection();
							Statement stmt = con.createStatement();		
							stmt.execute("DELETE FROM Parameter WHERE MemberAdID=" + ma.getName());
																															
							for(BasicClass bc  : mac.getObjects("FieldDefinition")){
								FieldDefinition fd = (FieldDefinition)bc;
								for(ConfigValue cv : fd.getCodeList()){
									String name = bc.getName() + "_" + cv.getValue();
									ocs.logAccess(name);
									ocs.logAccess(getBoolean(name));
									if(node.getBoolean(name)){
										Parameter parameter = (Parameter)ma.createObject("ch.opencommunity.base.Parameter", null, context);
										parameter.addProperty("MemberAdID", "String", ma.getName());
										parameter.setProperty("Template", fd.getName());
										parameter.setProperty("Title", fd.getString("Title"));
										parameter.setProperty("Value", cv.getValue());
										ocs.insertObject(parameter);
									}
								}
						}	
					}
					else{ 
							
						ocs.updateObject(ma);
							
							//The same. Should we try to update single parameters?
							Connection con = ocs.getConnection();
							Statement stmt = con.createStatement();		
							stmt.execute("DELETE FROM Parameter WHERE MemberAdID=" + ma.getName());
																															
							for(BasicClass bc  : mac.getObjects("FieldDefinition")){
								FieldDefinition fd = (FieldDefinition)bc;
								for(ConfigValue cv : fd.getCodeList()){
									String name = bc.getName() + "_" + cv.getValue();
									ocs.logAccess(name);
									ocs.logAccess(getBoolean(name));
									if(node.getBoolean(name)){
										Parameter parameter = (Parameter)ma.createObject("ch.opencommunity.base.Parameter", null, context);
										parameter.addProperty("MemberAdID", "String", ma.getName());
										parameter.setProperty("Template", fd.getName());
										parameter.setProperty("Title", fd.getString("Title"));
										parameter.setProperty("Value", cv.getValue());
										ocs.insertObject(parameter);
									}
								}
							}
					}
					
				}
			}
			
			
			if(activatedAds.size() > 0){
			
				ObjectTemplate ot = ocs.getObjectTemplate("4");
				ObjectTemplate ot2 = ocs.getObjectTemplate("5");
				
				HttpServletRequest request = ((WebApplicationContext)context).getRequest();
				
				
				String registrationcode = ocs.createPassword(20);
								
				Activity activity = (Activity)ot.createObject("ch.opencommunity.base.Activity", null, context);
				activity.applyTemplate(ot);
				activity.setProperty("Status", "0");
				activity.addProperty("OrganisationMemberID", "String", getString("omid"));
				BasicClass note = activity.getFieldByTemplate("21");
									
				String content = getString("Comment");
				
				String code = ocs.createLoginCode(om.getName(), context);
								
				String link = "\n\n" + ocs.getString("hostname") + "/servlet.srv?action=onetimelogin&redirect=profile&sectionid=inserate&code=" + code;
																		
				//String link = "\n\n" + ocs.getString("hostname") + "/servlet.srv?action=confirmregistration&registrationcode=" + registrationcode;
			
				content = content.replace("<@addressation>", om.getAddressation());
			
				content = content.replace("<@link>", link);
				
				content = content.replace("<@comment>", getString("Comment"));
									
				note.setProperty("Content", content);
				activity.setProperty("Title", "Benachrichtigung");
				String activityid = ocs.insertObject(activity, true);
				for(MemberAd ma : activatedAds){
					
					ActivityObject ao = (ActivityObject)ot.createObject("ch.opencommunity.base.ActivityObject", null, context);
					ao.addProperty("ActivityID", "String", activityid);
					ao.setProperty("MemberAdID", ma.getName());
					ocs.insertObject(ao);
					
				}				
				ocs.executeCommand("UPDATE Login SET RegistrationCode=\'" + registrationcode + "\' WHERE OrganisationMemberID=" + getString("omid"));
				ocs.sendAllPendingMails();
			}
		}
		catch(java.lang.Exception e){
			ocs.logException(e);
		}
		result.setParam("refresh", getString("sectionid"));
		
	}

	
}
