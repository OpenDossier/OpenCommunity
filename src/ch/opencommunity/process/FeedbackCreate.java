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
import org.kubiki.util.DateConverter;
 
import org.kubiki.application.*;
 
import java.util.Vector;
 
 
public class FeedbackCreate extends BasicProcess{
	
	BasicProcessNode node1;
	FeedbackForm feedbackForm;
	
	Vector reasons; 
	
	OrganisationMember om = null;

	MemberAd ma = null;
	MemberAdRequest mar = null;
	
	public FeedbackCreate(){
		
		setTitle("Feedback abgeben");
		setLastButtonLabel("Weiter");
		
		node1 = new FeedbackNode();	
		node1.setNextButtonLabel("Weiter");
		addNode(node1);
		
		
		addProperty("MemberAdRequestID", "String", "");
		addProperty("Mode", "Integer", "1");
		addProperty("DeleteContact", "Boolean", "false");
		addProperty("ContactEstablished", "Boolean", "true");
		addProperty("Type", "String", "1");
		
		Property p = addProperty("Reason", "Integer", "");
		//node1.addProperty(p);
		
		p = addProperty("Comment", "Text", "");
		//node1.addProperty(p);
		
		p = addProperty("FeedbackForm", "WebForm", "");			
		node1.addProperty(p);
		
		setCurrentNode(node1);	
		
		addProperty("errors", "Object", "");
		
	}
	public void initProcess(ApplicationContext context){
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		
		OpenCommunityUserSession usersession = (OpenCommunityUserSession)context.getObject("usersession");
		om = usersession.getOrganisationMember();
		
		ocs.logAccess("OM: " + getParent().getClass().getName());
		if(getParent() instanceof OrganisationMemberEdit){
			om = ((OrganisationMemberEdit)getParent()).getCurrentControler().getOrganisationMember();
		}
		ocs.logAccess("OM: " + om);
		
		if(getString("MemberAdRequestID").length() > 0){
			mar = (MemberAdRequest)ocs.getObject(null, "MemberAdRequest", "ID", getString("MemberAdRequestID"), false);
			if(mar != null){
				ma = (MemberAd)ocs.getObject(null, "MemberAd", "ID", mar.getString("MemberAd"), false);
			}
		}
				
		reasons = new Vector();	
		reasons.add(new ConfigValue("1", "1", "Nicht erreicht"));
		reasons.add(new ConfigValue("2", "2", "Unzuverlässig"));
		reasons.add(new ConfigValue("3", "3", "Keine Zeit"));
		reasons.add(new ConfigValue("4", "4", "Passt persönlich nicht"));
		reasons.add(new ConfigValue("5", "5", "Passt fachlich nicht"));
		reasons.add(new ConfigValue("6", "6", "Anderes"));

		getProperty("Reason").setSelection(reasons);
		
		for(int i = 0; i < reasons.size(); i++){
			ConfigValue cv = (ConfigValue)reasons.elementAt(i);
			addProperty("Reason_" + cv.getValue(), "Boolean", "false");
		}
		
		
		feedbackForm = new FeedbackForm();
		feedbackForm.setParent(this);
		feedbackForm.initObjectLocal();
		setProperty("FeedbackForm", feedbackForm);
		
	}
	class FeedbackNode extends BasicProcessNode{
		public boolean validate(ApplicationContext context){
			
			feedbackForm.saveForm(context);
			
			boolean success = false;
			if(getParent().getString("Type").equals("2")){
				
				// Feedback durch Admin erfasst
				
				if(getParent().getParent() instanceof OrganisationMemberEdit){
					//html.append("<tr><td class=\"labelColumn\">Mitglieder ID</td><td><input id=\"person_1_OrganisationMember\" name=\"person_1_OrganisationMember\"></td></tr>");
					//html.append("<tr><td class=\"labelColumn\">Betrifft</td><td>" + formManager.getSelection(fp.getProperty("OrganisationMember"), true, "person_" + fp.getName() + "_") + "</td></tr>");
					success = true;
				}                                                                                                            
				else{
					//html.append("<tr><td class=\"labelColumn\">Nachname</td><td><input name=\"person_1_Familyname\"  onkeyup=\"getSuggestions(event, this.value, this.id)\"></td></tr>");
					//html.append("<tr><td class=\"labelColumn\">Vorname</td><td><input name=\"person_1_Firstname\"></td></tr>");
					if(feedbackForm.getFeedbackPerson().getString("Familyname").length() > 3 && feedbackForm.getFeedbackPerson().getString("Firstname").length() > 2){
						success = true;
					}
					else{
						setComment("Geben Sie einen Namen ein!");	
					}
				}
			}
			else{
				success = true;	
			}
			
			if(!success){
				return false;
			}
			else{
				if(getParent().getBoolean("ContactEstablished")==false){
					if(context.hasProperty("Reason_1")){
						success = false;
						for(int i = 0; i < reasons.size(); i++){
							ConfigValue cv = (ConfigValue)reasons.elementAt(i);
							if(getParent().getString("Reason_" + cv.getValue()).equals("true")){
								success = true;
							}
						}
						if(!success){
							setComment("Geben Sie mindestens einen Grund an!");	
						}
						
						return success;
					}
					else{
						getProcess().setLastButtonLabel("Abschliessen");
						getParent().setProperty("Mode", "2");	
	
						return false;
					}
				}
				else if(getParent().getBoolean("ContactEstablished")==true){
					if(getParent().getInt("Mode")==3){
						FeedbackPerson feedbackPerson = (FeedbackPerson)feedbackForm.getObjectByIndex("FeedbackPerson",0);
						if(feedbackPerson == null){
							return false;
						}	
						else{
							
							int quality = feedbackPerson.getID("Quality");
							
							OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
							
							ocs.logAccess(quality);
							ocs.logAccess(feedbackPerson.getString("Problems"));
							ocs.logAccess(feedbackPerson.getString("Highlights"));
							
							if((quality == 1 || quality == 2 ) && feedbackPerson.getString("Problems").length()==0){
								setComment("Geben Sie an, wo es Probleme gab.");
								return false;
							}
							else if((quality == 3 || quality == 4 ) && feedbackPerson.getString("Highlights").length()==0){
								setComment("Geben Sie an, was besonders gut war.");
								return false;								
							}
							else{
								return true;	
							}
						}
					}
					else{
						getProcess().setLastButtonLabel("Abschliessen");
						getParent().setProperty("Mode", "3");	
	
						return false;	
					}
				}
				else{
					return true;	
				}
				
			}
		}
	}
	public void finish(ProcessResult result, ApplicationContext context){
		
 	 	OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
 	 	Feedback feedback = new Feedback();
 	 	if(getString("Type").equals("2")){
 	 		feedback.setProperty("Status", "0");
 	 		feedback.addProperty("OrganisationMemberID", "String", om.getName());
 	 	}
 	 	else{
 	 		feedback.setProperty("Status", "0");
 	 		feedback.addProperty("MemberAdRequestID", "String", getString("MemberAdRequestID"));
 	 		feedback.addProperty("OrganisationMemberID", "String", om.getName());
 	 		feedback.setProperty("OrganisationMember", ma.getString("OrganisationMemberID"));
 	 	}


 	 	feedback.mergeProperties(this);
 	 	//if(getBoolean("ContactEstablished")){
 	 		feedback.mergeProperties(feedbackForm.getObjectByIndex("FeedbackPerson",0));
 	 	//}
 	 	if(!getBoolean("ContactEstablished")){
 	 		feedback.setProperty("Comment", getString("Comment"));
 	 	}
 	 	feedback.setProperty("DateCreated", DateConverter.dateToSQL(new java.util.Date(), true));
 	 	if(getString("Type").equals("1")){
 	 		//feedback.setProperty("Status", "1");
 	 		feedback.setProperty("OrganisationMember", ma.getString("OrganisationMemberID"));
 	 	}
 	 	if(getParent() instanceof OrganisationMemberEdit){
 	 		feedback.setProperty("Status", "1");
 	 	}
 	 	ocs.insertObject(feedback);
 	 	if(getBoolean("DeleteContact")){
 	 		ocs.executeCommand("UPDATE MemberAdRequest SET Status=3 WHERE ID=" + getString("MemberAdRequestID"));
 	 	}
 	 	
 	 	
 	 	
 	 	if(getParent() instanceof OrganisationMemberEdit){
 	 		((BasicProcess)getParent()).setSubprocess(null);
 	 	}
 	 	else{
 	 		result.setParam("refresh", "currentsection");
 	 		result.setParam("newprocess", "ch.opencommunity.process.FeedbackShow");
 	 		result.setParam("newprocessparams", "TextBlockID=6");
 	 		
 	 	}
 	}
	
}