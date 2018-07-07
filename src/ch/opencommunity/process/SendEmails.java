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
import org.kubiki.application.server.WebApplicationContext;
 
 import org.kubiki.application.*;
 
 import java.util.Vector;
 import java.util.List;
 
 import javax.servlet.http.*;
 
 
 public class SendEmails extends BasicProcess{
 	 
 	 BasicProcessNode node1;
 	 ObjectCollection recipients;
 	 
 	 
 	 public SendEmails(){
 	 	 
 	 	 node1 = addNode();
 	 	 setCurrentNode(node1);
 	 	 recipients = addObjectCollection("Recipients", "*");

 	 	 Property p = addProperty("Mode", "Integer", "");
 	 	 //node1.addProperty(p);
 	 	 
 	 	 p = addProperty("NumRecipients", "Integer", "");
 	 	 node1.addProperty(p);
 	 	 
 	 	 p = addProperty("RecipientList", "ObjectList", "");
 	 	 node1.addProperty(p);
 	 	 
 	 	 
 	 	 
 	 }
 	 public void initProcess(){
 	 	 
 	 	 OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
 	 	 String sql = "SELECT DISTINCT t2.ID AS OrganisationMemberID, t4.FamilyName, t4.FirstName, t5.Value, t10.Value AS Emailalt, t1.Template, t1.ID, t1.Title, t6.Content FROM Activity AS t1";
 	 	 sql += " INNER JOIN OrganisationMember AS t2 ON t1.OrganisationMemberID=t2.ID";
			 sql += " LEFT JOIN Person AS t3 ON t2.Person=t3.ID";
			 sql += " LEFT JOIN Identity AS t4 ON t4.PersonID=t3.ID";
			 sql += " LEFT JOIN Contact AS t5 ON t5.PersonID=t3.ID AND t5.Type=3";
			 sql += " LEFT JOIN Note AS t6 ON t6.ActivityID=t1.ID AND t6.Template IN (16,21)";
			 sql += " LEFT JOIN OrganisationMemberRelationship AS t7 ON t2.ID=t7.OrganisationMemberID";
			 sql += " LEFT JOIN OrganisationMember AS t8 ON t8.ID=t7.OrganisationMember";			 
			 sql += " LEFT JOIN Person AS t9 ON t8.Person=t9.ID";	
			 sql += " LEFT JOIN Contact AS t10 ON t10.PersonID=t9.ID AND t10.Type=3";
			 sql += " WHERE t1.Status=0 AND t1.Template IN (1,4)";
			 sql += " ORDER BY t1.ID";
			 ocs.queryData(sql, recipients);
			 setProperty("NumRecipients", "" + recipients.getObjects().size());
 	 	 
 	 	 
 	 }
 	public void finish(ProcessResult result, ApplicationContext context){
	
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		
		/*
		HttpServletRequest request = ((WebApplicationContext)context).getRequest();
		for(BasicClass bc : recipients.getObjects()){
			
			String id = bc.getString("ID");
			
			ocs.sendEmail(bc.getString("CONTENT"), bc.getString("TITLE") , bc.getString("VALUE"));
			ocs.executeCommand("UPDATE Activity SET Status=1 WHERE ID=" + id);
			
		}
		*/
		ocs.sendAllPendingMails(recipients);
		result.setParam("refresh", "email");
		
	}
 	 
 }