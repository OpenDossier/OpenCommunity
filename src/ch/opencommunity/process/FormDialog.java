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
import org.kubiki.cms.*;
 

import java.util.List;
import java.util.Vector;
 
 
public class FormDialog extends BasicProcess{
	
	
	FormNode formNode;
	BasicProcessNode feedbackNode;
	public WebForm form;
	
	private String method = null;
	private String emailaddress = null;
	private List fields;
	

	
	public FormDialog(){
		
		fields = new Vector();
		
		formNode = new FormNode();
		addNode(formNode);
		
		/*
		feedbackNode = addNode();
		feedbackNode.setTitle("Vielen Dank!");
		feedbackNode.setComment("Die Einzahlungsscheine erhalten Sie in den nächsten Tagen per Post zugestellt.");
		*/
		addProperty("formid", "String", "");
		
		setCurrentNode(formNode);	
		

		
	}
	public void initProcess(){
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		CMS cms = (CMS)ocs.getObjectByName("ApplicationModule", "cms");
		if(cms != null){
			WebSiteRoot wsr = (WebSiteRoot)cms.getObjectByIndex("WebSiteRoot", 0);
			if(wsr != null){
				form = wsr.getWebForm(getString("formid"));
				if(form != null){
					String[] lines = form.getString("FormDefinition").split("\r\n|\r|\n");
					for(String line : lines){
						String[] args = line.split(":");
						if(args.length==2){
							if(args[0].equals("Title")){
								formNode.setTitle(args[1]);
								setTitle(args[1]);
							}
							else if(args[0].equals("Method")){
								String[] args2 = args[1].split(",");
								if(args2.length==2){
									this.method = args2[0];
									this.emailaddress = args2[1];
								}
							}
							else if(args[0].equals("Field")){
								String[] args2 = args[1].split(",");
								if(args2.length > 0){
									Property p = addProperty(args2[0], args2[1], "", false, args2[2]);
									formNode.addProperty(p);
									fields.add(p);
								}
							}
							
						}
					}
				}
			}
		}
		
	}
	public void finish(ProcessResult result, ApplicationContext context){
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		
		if(method != null && method.equals("Email")){
			if(emailaddress != null){
				
				StringBuilder message = new StringBuilder();
				for(Object o : fields){
					Property p = (Property)o;
					message.append(p.getLabel() + ":" + p.getValue() + "\n");
				}
				ocs.sendEmail(message.toString(), "Bestellformular", emailaddress);
				
			}
		}
		result.setParam("newprocess", "ch.opencommunity.process.FeedbackShow");
		//result.setParam("newprocessparams", "Title=" + encode("VielenDank!") + "~Text=" + encode("Die Einzahlungsscheine erhalten Sie in den nächsten Tagen per Post zugestellt."));	
		result.setParam("newprocessparams", "Title=" + encode("VielenDank!") + "~Text=" + encode("Das bestellte Material erhalten Sie in den nächsten Tagen per Post zugestellt."));
	}
	class FormNode extends BasicProcessNode{
		
		public boolean validate(ApplicationContext context){
			
			boolean success = true;
			for(Object o : fields){
				Property p = (Property)o;
				if(p.getValue().length() == 0){
					success = false;	
				}
			}
			if(success){
				setComment("");	
			}
			else{
				setComment("Bitte alle Felder ausfüllen!");
			}
			return success;
		}
		

				
	}
	
	
}