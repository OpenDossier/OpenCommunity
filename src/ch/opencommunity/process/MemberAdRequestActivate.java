 package ch.opencommunity.process;
 
 import ch.opencommunity.server.*;
 import ch.opencommunity.base.*;
 import ch.opencommunity.advertising.*;
 
import org.kubiki.ide.*;
import org.kubiki.base.Property;
import org.kubiki.base.BasicClass;
import org.kubiki.base.ObjectCollection;
import org.kubiki.base.ProcessResult;
import org.kubiki.base.ConfigValue;
 
import org.kubiki.application.*;
import org.kubiki.application.server.WebApplicationContext;
 
import java.util.Vector;
import java.sql.*;
 
import javax.servlet.http.*;
 
 
public class MemberAdRequestActivate extends BasicProcess{
 
	BasicProcessNode node1;
	MemberAdRequest mar = null;
	
	OrganisationMember om;
	
	MemberAd ma;
	
	String ownerComment;
 
	public MemberAdRequestActivate(){ 
		
		setTitle("Inserat aktivieren");
		
		node1 = addNode();
		
		
		Property p = addProperty("NotificationMode", "Integer", "1", false, "Informieren über");
		node1.addProperty(p);
		
		p = addProperty("memberadrequestid", "Integer", "", false, "Inserat ID");
		node1.addProperty(p);
		
		addProperty("sectionid", "String", "");
		
		p = addProperty("memberadid", "Object", "");
		node1.addProperty(p);
		
		p = addProperty("Comment", "Text", "", false, "Kommentar");
		node1.addProperty(p);
		
		p = addProperty("UserComment", "Text", "", false, "Kommentar Adressbesteller");
		node1.addProperty(p);
		
		p = addProperty("Delete", "Boolean", "false", false, "Anfrage löschen");
		node1.addProperty(p);
				
		setCurrentNode(node1);
	}
	public void initProcess(){
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();

		ma = (MemberAd)ocs.getObject(null, "MemberAd", "ID", getString("memberadid"), false);
		ma.setParent(this);
		ma.initObjectLocal();
		
		//setProperty("MemberAd", ma);
		
		Vector channels = new Vector();
		channels.add(new ConfigValue("1","1", "Email"));
		channels.add(new ConfigValue("2","2", "Brieflich"));
		getProperty("NotificationMode").setSelection(channels);
		
		mar = (MemberAdRequest)ocs.getObject(null, "MemberAdRequest", "ID", getString("memberadrequestid"), false);
		
		ObjectCollection results = new ObjectCollection("Results", "*");
		String sql = "SELECT Comment FROM PERSON AS t1, OrganisationMember AS t2 WHERE t2.Person=t1.ID AND t2.ID=" + mar.getString("OrganisationMemberID");
		ocs.logAccess(sql);
		ocs.queryData(sql, results);
		for(BasicClass bc : results.getObjects()){
			ownerComment = bc.getString("COMMENT");
		}
		setProperty("UserComment", mar.getString("UserComment"));
	}
	public void finish(ProcessResult result, ApplicationContext context){
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();

		ObjectTemplate ot = ocs.getObjectTemplate("4");
		ObjectTemplate ot2 = ocs.getObjectTemplate("5");
		
		HttpServletRequest request = ((WebApplicationContext)context).getRequest();
		
		if(getBoolean("Delete")){
			
			mar.setProperty("Comment", getString("Comment"));
			mar.setProperty("Status", 3);
			mar.setProperty("NotificationStatus", 0);
			mar.setProperty("NotificationMode", getID("NotificationMode"));
			
			ocs.updateObject(mar);
		}
		else{
		
			try{
				
				MemberAdCategory mac = ma.getCategory();
				
				if(mac != null){
				
					mar.setProperty("Comment", getString("Comment"));
					
					String registrationcode = ocs.createPassword(20);
					
					if(mac.getBoolean("Protected")){
						mar.setProperty("Status", 4);
						mar.setProperty("ActivationCode", registrationcode);
					}
					else{
						mar.setProperty("Status", 1);							
					}
					mar.setProperty("NotificationStatus", 0);
					mar.setProperty("NotificationMode", getID("NotificationMode"));
					
					ocs.updateObject(mar);
					
					//stmt.execute("UPDATE MemberAdRequest SET Status=1, NotificationStatus=0, NotificationMode=" + getID("NotificationMode") + " WHERE ID=" + getString("memberadrequestid"));
					

								
					Activity activity = (Activity)ot.createObject("ch.opencommunity.base.Activity", null, context);
					activity.applyTemplate(ot);
					activity.setProperty("Status", "0");
					activity.addProperty("OrganisationMemberID", "String", ma.getString("OrganisationMemberID"));
					BasicClass note = activity.getFieldByTemplate("21");
									
					//String content = "Ihre Adresse wurde weitergegeben. Sie werden demnächst kontaktiert von einer Person, die sich selber so beschreibt:" + "\n\n" + ownerComment;
					String content = "Ihre Adresse wurde weitergegeben. Sie werden demnächst kontaktiert von einer Person, die folgendes sucht:" + "\n\n" + ownerComment;
					
					activity.setProperty("Title", "Ihre Adresse wurde weitergegeben");
					
					if(mac.getBoolean("Protected")){
						
						content =  "Ihre Adresse wurde bestellt von einer Person, die sich selber so beschreibt:" + "\n\n" + ownerComment;
						
						content += "\n\nWenn Sie enverstanden sind, dass diese Person Ihre Adresse erhält, klicken Sie auf den nachfolgenden Link:";
									
						content += "\n\n" + ocs.getBaseURL(request) + "/opencommunity/servlet.srv?action=activaterequest&activationcode=" + registrationcode;
						
						content += "\n\nWenn Sie dies nicht wollen, klicken Sie auf den nachfolgenden Link:";
									
						content += "\n\n" + ocs.getBaseURL(request) + "/opencommunity/servlet.srv?action=denyrequest&activationcode=" + registrationcode;
						
						activity.setProperty("Title", "Es gibt Interessenten für Ihr Inserat");
						
					}
									
					note.setProperty("Content", content);
					

					String activityid = ocs.insertObject(activity, true);
									
					ocs.executeCommand("UPDATE Login SET RegistrationCode=\'" + registrationcode + "\' WHERE OrganisationMemberID=" + ma.getString("OrganisationMemberID"));
					
				}
			}
			catch(java.lang.Exception e){
				ocs.logException(e);
			}
			
		}
		result.setParam("refresh", getString("sectionid"));
		
	}
	public MemberAd getMemberAd(){
		return ma;
	}
	
}