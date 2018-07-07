package ch.opencommunity.process;
 
 import ch.opencommunity.server.*;
 import ch.opencommunity.base.*;
 import ch.opencommunity.advertising.*;
 
import org.kubiki.ide.*;
import org.kubiki.base.BasicClass;
import org.kubiki.base.Property;
import org.kubiki.base.ProcessResult;
import org.kubiki.base.ConfigValue;
import org.kubiki.cms.WebForm;
 
 import org.kubiki.application.*;
 
import java.util.Vector;
import java.util.List;
import java.sql.*;
 
 
public class FeedbackForm extends WebForm{
	
	int cnt = 1;
	
	String cbimageselected = null;
	String cbimage = null;
	
	FeedbackPerson fp = null;
	
	public FeedbackForm(){
		addObjectCollection("FeedbackPerson", "*");
	}
	public void initObjectLocal(){
		
		if(!(getParent() instanceof ch.opencommunity.process.FeedbackFinalize)){
		
			fp = new FeedbackPerson();
			fp.setParent(this);
			fp.setName("1");
			addSubobject("FeedbackPerson", fp);
			
			
			
			if(getParent() instanceof FeedbackCreate && getParent().getInt("Mode")==1){
				OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
				fp.getProperty("OrganisationMember").setSelection(ocs.getRecipientList());
			}
			
		}
		
		cbimageselected = "images/cbimageselected.png";
		cbimage = "images/cbimage.png";
	}
	public FeedbackPerson getFeedbackPerson(){
		return fp;	
	}
	public String toHTML(ApplicationContext context){
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		ch.opencommunity.server.HTMLForm formManager = ocs.getFormFactory();
		
		StringBuilder html = new StringBuilder();
		
		//html.append("<tr><td>" + getParent().getInt("Mode") + "</td></tr>");
		//html.append("<tr><td>" + getParent().getInt("Type") + "</td></tr>");
		
		if(getParent() instanceof FeedbackCreate && getParent().getInt("Mode")==1){
			
			if(getParent().getString("Type").equals("2")){
				
				// Feedback durch Admin erfasst
				
				if(getParent().getParent() instanceof OrganisationMemberEdit){
					//html.append("<tr><td class=\"labelColumn\">Mitglieder ID</td><td><input id=\"person_1_OrganisationMember\" name=\"person_1_OrganisationMember\"></td></tr>");
					html.append("<tr><td class=\"labelColumn\">Betrifft</td><td>" + formManager.getSelection(fp.getProperty("OrganisationMember"), true, "person_" + fp.getName() + "_") + "</td></tr>");
				}                                                                                                            
				else{
					html.append("<tr><td class=\"labelColumn\">Nachname</td><td><input name=\"person_1_Familyname\"  onkeyup=\"getSuggestions(event, this.value, this.id)\"></td></tr>");
					html.append("<tr><td class=\"labelColumn\">Vorname</td><td><input name=\"person_1_Firstname\"></td></tr>");
				}
			}
			html.append("<tr>");
			html.append("<td class=\"labelColumn\">Ist ein Einsatz zustandegekommen?i</td>");
			if(getParent().getString("Type").equals("2")){
				html.append("<td><input type=\"radio\" name=\"ContactEstablished\" value=\"true\" CHECKED>Ja <input type=\"radio\" name=\"ContactEstablished\" value=\"false\">Nein</td></tr>");
			}
			else{
				html.append("<td><input type=\"radio\" name=\"ContactEstablished\" value=\"true\" >Ja <input type=\"radio\" name=\"ContactEstablished\" value=\"false\" CHECKED>Nein</td></tr>");
			}
			html.append("</tr>");			
		}
		else if(getParent() instanceof FeedbackCreate && getParent().getInt("Mode")==2){
			html.append("<tr>");
			html.append("<td class=\"labelColumn\">Was war der Grund?</td><td>");
			for(Object o : getParent().getProperty("Reason").getValues()){
				ConfigValue cv = (ConfigValue)o;
				if(cv.getLabel().length() > 0){
					String id = "Reason_" + cv.getValue();
					html.append(HTMLForm.getCustomCheckbox( id, "false" , false, cbimageselected, cbimage, cv.getLabel()) + "<br>");
				}
			}			
			html.append("</td></tr>");
			html.append("<tr><td class=\"columnLabel\">Bemerkungen</td><td>" + formManager.getTextArea(getParent().getProperty("Comment"), true, null, "") + "</td></tr>");
		}
		else if(getParent() instanceof FeedbackCreate && getParent().getInt("Mode")==3){
			
			BasicClass fp = getObjectByIndex("FeedbackPerson", 0);
			html.append("<div style=\"height : 450px; overflow :auto;\">");
			html.append("<br>Wie war der Einsatz?<br>");
			int quality = fp.getInt("Quality");
			String[] labels = {"", "gar nicht gut", "es geht so", "gut", "super"};
			for(int i = 1; i < 5; i++){
				if(i == quality){
					html.append("<input type=\"radio\" name=\"" + "person_" + fp.getName() + "_Quality\" value=\"" + i + "\" CHECKED> " + labels[i]);
				}
				else{
					html.append("<input type=\"radio\" name=\"" + "person_" + fp.getName() + "_Quality\" value=\"" + i + "\"> " + labels[i]);
				}
			}
	
				
			html.append("<br>Was lief besonders gut?<br>");
			html.append(formManager.getTextArea(fp.getProperty("Highlights"), true, null, "person_" + fp.getName() + "_"));
			html.append("<br>Was lief nicht gut?<br>");
			html.append(formManager.getTextArea(fp.getProperty("Problems"), true, null, "person_" + fp.getName()+ "_"));			
			html.append("<br>Bemerkungen?<br>");
			html.append(formManager.getTextArea(fp.getProperty("Comment"), true, null, "person_" + fp.getName()+ "_"));
			
			html.append("</div>");
			html.append("</td></tr>");
			
		}
		else if(getParent() instanceof FeedbackFinalize && ((FeedbackFinalize)getParent()).getFeedback() != null){ //Feedback finalisieren
			Feedback fb = ((FeedbackFinalize)getParent()).getFeedback();
			html.append("<input type=\"checkbox\" value=\"true\" name=\"DeleteFeedback\"> Feedback löschen<br>");
			html.append("Wer ist oder war bei Ihnen im Einsatz?<br>");
			html.append("Name: " + formManager.getTextField(fb.getProperty("Familyname"), true, "person_" + fb.getName() + "_"));
			html.append("Vorname: " + formManager.getTextField(fb.getProperty("Firstname"), true, "person_" + fb.getName() + "_"));
				
			if((getParent() instanceof ch.opencommunity.process.FeedbackFinalize)){
				
				html.append("<input id=\"person_" + fb.getName() + "_OrganisationMember\" name=\"person_" + fb.getName() + "_OrganisationMember\" value=\"" + fb.getString("OrganisationMember") + "\"readonly=\"true\" style=\"width : 50px\">");
				if(fb.getInt("Status")==0){
					html.append("<input type=\"button\" onclick=\"organisationMemberSearch('" + fb.getString("Familyname") + "','" + fb.getString("Firstname") + "', " + fb.getName() + ")\" value=\"In Datenbank suchen\">");
				}
			}
			if(fb.getBoolean("ContactEstablished")){
				html.append("<br>Wie war der Einsatz?<br>");
				int quality = fb.getInt("Quality");
				String[] labels = {"", "gar nicht gut", "es geht so", "gut", "super"};
				for(int i = 1; i < 5; i++){
					if(i == quality){
						html.append("<input type=\"radio\" name=\"" + "person_" + fb.getName() + "_Quality\" value=\"" + i + "\" CHECKED> " + labels[i]);
					}
					else{
						html.append("<input type=\"radio\" name=\"" + "person_" + fb.getName() + "_Quality\" value=\"" + i + "\"> " + labels[i]);
					}
				}
		
					
				html.append("<br>Was lief besonders gut?<br>");
				html.append(formManager.getTextArea(fb.getProperty("Problems"), true, null, "person_" + fb.getName() + "_"));
				html.append("<br>Was lief nicht gut?<br>");
				html.append(formManager.getTextArea(fb.getProperty("Highlights"), true, null, "person_" + fb.getName()+ "_"));			
				html.append("<br>Bemerkungen?<br>");
				html.append(formManager.getTextArea(fb.getProperty("Comment"), true, null, "person_" + fb.getName()+ "_"));	
			}
			else{
				html.append("<tr>");
				html.append("<td class=\"labelColumn\">Einsatz nicht zustandegekommen: Was war der Grund?</td><td>");
				for(Object o : getParent().getProperty("Reason").getValues()){
					ConfigValue cv = (ConfigValue)o;
					if(cv.getLabel().length() > 0){
						/*
						String id = "Reason_" + cv.getValue();
						html.append(HTMLForm.getCustomCheckbox( id, "false" , false, cbimageselected, cbimage, cv.getLabel()) + "<br>");
						*/
						html.append("<input type=\"checkbox\" value=\"true\" name=\"person_" + fb.getName() + "_Reason_" + cv.getValue() + "\"");
						if(fb.getBoolean("Reason_" + cv.getValue())){
							html.append(" CHECKED");	
						}
							
						html.append("> " + cv.getLabel() + "<br>");
					}
				}			
				html.append("</td></tr>");
				html.append("<tr><td class=\"columnLabel\">Bemerkungen</td><td>" + formManager.getTextArea(fb.getProperty("Comment"), true, null, "person_" + fb.getName()+ "_") + "</td></tr>");				
				
			}
		}
		else{
			if((getParent() instanceof ch.opencommunity.process.FeedbackFinalize)){
				html.append("<tr><td colspan=\"2\" width=\"700\">");
			}
			else{
				html.append("<tr><td colspan=\"2\" width=\"550\">");
			}
			if(!(getParent() instanceof ch.opencommunity.process.FeedbackFinalize)){
				html.append("<input type=\"button\" onclick=\"getNextNode('webform=true&formaction=addperson')\" value=\"Person hinzufügen\">");
			}
			html.append("<div style=\"height : 500px; overflow :auto;\">");
			
			int index = 1;
			
			
			for(BasicClass fp : getObjects("FeedbackPerson")){
				html.append("<h4>Person " + fp.getName() + "</h4>");
				html.append("Wer ist oder war bei Ihnen im Einsatz?<br>");
				
				//if(getParent().getParent() instanceof OrganisationMemberEdit){
					//html.append("Mitglieder ID: " + formManager.getTextField(fp.getProperty("OrganisationMember"), true, "person_" + fp.getName() + "_"));
				//}
				
				html.append("Name: " + formManager.getTextField(fp.getProperty("Familyname"), true, "person_" + fp.getName() + "_"));
				html.append("Vorname: " + formManager.getTextField(fp.getProperty("Firstname"), true, "person_" + fp.getName() + "_"));
				
				if((getParent() instanceof ch.opencommunity.process.FeedbackFinalize)){
					
					html.append("<input id=\"person_" + fp.getName() + "_OrganisationMember\" name=\"person_" + fp.getName() + "_OrganisationMember\" value=\"" + fp.getString("OrganisationMember") + "\"readonly=\"true\" style=\"width : 50px\">");
					if(fp.getInt("Status")==0){
						html.append("<input type=\"button\" onclick=\"organisationMemberSearch('" + fp.getString("Familyname") + "','" + fp.getString("Firstname") + "', " + fp.getName() + ")\" value=\"In Datenbank suchen\">");
					}
				}
				html.append("<br>Wie war der Einsatz?<br>");
				int quality = fp.getInt("Quality");
				String[] labels = {"", "gar nicht gut", "es geht so", "gut", "super"};
				for(int i = 1; i < 5; i++){
					if(i == quality){
						html.append("<input type=\"radio\" name=\"" + "person_" + fp.getName() + "_Quality\" value=\"" + i + "\" CHECKED> " + labels[i]);
					}
					else{
						html.append("<input type=\"radio\" name=\"" + "person_" + fp.getName() + "_Quality\" value=\"" + i + "\"> " + labels[i]);
					}
				}
	
				
				html.append("<br>Was lief besonders gut?<br>");
				html.append(formManager.getTextArea(fp.getProperty("Problems"), true, null, "person_" + fp.getName() + "_"));
				html.append("<br>Was lief nicht gut?<br>");
				html.append(formManager.getTextArea(fp.getProperty("Highlights"), true, null, "person_" + fp.getName()+ "_"));			
				html.append("<br>Bemerkungen?<br>");
				html.append(formManager.getTextArea(fp.getProperty("Comment"), true, null, "person_" + fp.getName()+ "_"));			
	            index++;
	
			}
			
	
			
			html.append("</div>");
			html.append("</td></tr>");
			
		}
		return html.toString();	
	}
	public void saveForm(ApplicationContext context){
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		ocs.logAccess(context.hasProperty("formaction"));
		if(context.hasProperty("formaction")){
			ocs.logAccess(context.getString("formaction"));
			if(context.getString("formaction").equals("addperson")){
				cnt++;
				FeedbackPerson fp = new FeedbackPerson();
				fp.setParent(this);
				fp.setName("" + cnt);
				addSubobject("FeedbackPerson", fp);
			}
		}
		for(BasicClass fp : getObjects("FeedbackPerson")){
			String prefix = "person_" + fp.getName() + "_";
			List<String> names = fp.getPropertySheet().getNames();
			for(String name : names){
				ocs.logAccess(prefix + name + ":" + context.hasProperty(prefix + name));
				if(context.hasProperty(prefix + name)){
					ocs.logAccess(context.getString(prefix + name));
					fp.setProperty(name, context.getString(prefix + name));
				}
			}
		}
	
	}

 	 
}