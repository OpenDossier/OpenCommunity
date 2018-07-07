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
import java.util.Hashtable;
 
 
 public class ProfileEdit extends BasicProcess{
 	 
 	 BasicProcessNode node1;
 	 OrganisationMember om;
 	 Person person;
 	 
 	 int edit = 0;
 	 
	 Vector sex;
	 Vector yes_no;
	 
	 boolean updated = false;
	 boolean updatedpassword = false;
	 
	 Hashtable errors = new Hashtable();
 	
	 public ProfileEdit(){
	 	 node1 = new ProfileEditNode();
	 	 addNode(node1);
	 	 node1.setName("ProfileEditNode");
	 	 
	 	 addProperty("validationfeedback", "String", "");
	 	 
	 	 setTitle("Profil bearbeiten");
	 	 
	 	 setCurrentNode(node1);
 	 }
 	 public void initProcess(ApplicationContext context){
 	 	 OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
 	 	 OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
 	 	 if(userSession != null){
 	 	 	 
 	 	 	 om = userSession.getOrganisationMember();
 	 	 	 
 	 	 	 if(om.getActiveRelationship() != null){
 	 	 	 	om = om.getActiveRelationship();	 
 	 	 	 }
 	 	 	 
 	 	 	 person = (Person)ocs.getObject(null, "Person", "ID", om.getString("Person"));
 	 	 	 
 	 	 	 addProperty("Languages", "ListFillIn", person.getString("Languages"));
 	 	 	 addProperty("Languages_b", "ListFillIn", person.getString("Languages"));
 	 	 	 getProperty("Languages").setSelection(ocs.getSupportedLanguages());
 	 	 	 
 	 	 	 //addProperty("Comment", "String", person.getString("Comment"));
 	 	 	 
 	 	 	 Identity identity = person.getIdentity();
 	 	 	 
			 sex = new Vector();
			 sex.add(new ConfigValue("1", "1", "Herr"));
			 sex.add(new ConfigValue("2", "2", "Frau"));
			 identity.getProperty("Sex").setSelection(sex);
 	 	
 	 	 	 for(String name : identity.getPropertySheet().getNames()){
 	 	 	 	 addProperty(name, "String", identity.getString(name));
 	 	 	 	 addProperty(name + "_b", "String", identity.getString(name));
 	 	 	 }
 	 	 	 
  	 	 	 Address address = person.getAddress();	
  	 	 	 for(String name : address.getPropertySheet().getNames()){
 	 	 	 	 addProperty(name, "String", address.getString(name));
 	 	 	 	 addProperty(name + "_b", "String", address.getString(name));
 	 	 	 }
 	 	 	 for(int i = 0; i < 5; i++){
 	 	 	 	 Contact c = person.getContact(i);
 	 	 	 	 if(c != null){
 	 	 	 	 	 addProperty("contact_" + i, "String", c.getString("Value"));
 	 	 	 	 	 addProperty("contact_" + i + "_b", "String", c.getString("Value"));
 	 	 	 	 }
 	 	 	 	 else{
 	 	 	 	 	 addProperty("contact_" + i, "String", "");
 	 	 	 	 	 addProperty("contact_" + i + "_b", "String", "");
 	 	 	 	 }
 	 	 	 }
 	 	 	 addProperty("Password", "Password", "");
 	 	 	 addProperty("PasswordControl", "Password", "");
 	 	 }
 	 }
	public void finish(ProcessResult result, ApplicationContext context){
		
 	 	OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
 	 	for(String name : getPropertySheet().getNames()){
 	 		if(name.endsWith("_b")==false && name.equals("Languages")==false){
 	 			String value = getProperty(name).getObject().toString();
 	 			if(hasProperty(name + "_b")){
 	 				String value_b = getProperty(name + "_b").getObject().toString();
 	 				if(!value.equals(value_b)){
 	 					updated = true;	
 	 				}
 	 			}
 	 				
 	 		}
 	 		
 	 	}
 	 	
		if(updated){
			
			String now = DateConverter.dateToSQL(new java.util.Date(), true);
			OrganisationMemberModification omm = null;
			boolean exists = false;
			for(BasicClass bc : om.getObjects("OrganisationMemberModification")){
			 	if(bc.getID("Status")==0){
			 		omm = (OrganisationMemberModification)bc;	
			 		exists = true;
			 	}
			}
			if(omm == null){
				omm = new OrganisationMemberModification();
				omm.addProperty("OrganisationMemberID", "String", om.getName());
			}	
			ocs.logAccess("omm : " + omm.getName().length());
			//omm.mergeProperties(this);
			
			
			omm.setProperty("FamilyName", getString("FamilyName"));
			omm.setProperty("FirstName", getString("FirstName"));
			omm.setProperty("DateOfBirth", getString("DateOfBirth"));
			omm.setProperty("FirstLanguageS", getString("FirstLanguageS"));

			omm.setProperty("AdditionalLine", getString("AdditionalLine"));
			omm.setProperty("Street", getString("Street"));
			omm.setProperty("Number", getString("Number"));
			omm.setProperty("Zipcode", getString("Zipcode"));
			omm.setProperty("City", getString("City"));
			
			omm.setProperty("TelephonePrivate", getString("contact_0"));
			omm.setProperty("TelephoneBusiness", getString("contact_1"));
			omm.setProperty("TelephoneMobile", getString("contact_2"));
			omm.setProperty("Email", getString("contact_3"));
			omm.setProperty("Status", "0");
			omm.setProperty("DateCreated", DateConverter.dateToSQL(new java.util.Date(), true));
 	 	 	omm.setProperty("UserCreated", om.getName());
 	 	 	if(exists){
 	 	 		ocs.logAccess("updating omm : " + omm.getName());
 	 	 		ocs.updateObject(omm);
 	 	 	}
 	 	 	else{
 	 	 		ocs.logAccess("inserting omm : " + omm);
 	 	 		String id = ocs.insertObject(omm);
 	 	 		ocs.getObject(om, "OrganisationMemberModification", "ID", id);
 	 	 	}
 	 	 	
			
			om.setProperty("DateModified", now);
 	 	 	om.setProperty("UserModified", om.getName());
			om.setProperty("Status", 4);
			ocs.updateObject(om);
		}
		
		
		person.setProperty("Comment", getString("Comment"));
		person.setProperty("Languages", getString("Languages"));
		ocs.updateObject(person);
		
		Login login = om.getLogin();
		if(getString("Password").length() > 0){
			login.setProperty("Password", getString("Password"));
			//updatedpassword = true;	
			ocs.updateObject(login);
		}

		if(updated && updatedpassword){
			result.setParam("newprocess", "ch.opencommunity.process.FeedbackShow");
			result.setParam("newprocessparams", "TextBlockID=38");			
		}
		else if(updated){
			result.setParam("newprocess", "ch.opencommunity.process.FeedbackShow");
			result.setParam("newprocessparams", "TextBlockID=7");			
		}
		else if(updatedpassword){
			result.setParam("newprocess", "ch.opencommunity.process.FeedbackShow");
			result.setParam("newprocessparams", "TextBlockID=37");			
		}
		else{
			result.setParam("refresh", "currentpage");	
		}
		
	}
 	 public String getProfileEditForm(ApplicationContext context){
 	 	 
 	 	 MemberRegistration mr = null;
 	 	 StringBuilder html = new StringBuilder(); 	
 	 	 
 	 	 String prefix = "";
 	 	 
 	 	 int edit = 0;
 	 	 
 	 	 if(person != null){
 	 	 	 
 	 	 	html.append("<form id=\"processNodeForm\">");
 	 	 
 	 	 	html.append("<table style=\"width : 540px;\"");
 	 	 	
			if(getString("validationfeedback").length() > 0){
				html.append("<tr><td colspan=\"2\">" + getString("validationfeedback") + "</td></tr>");
			}
 	 	 	
			Identity identity = (Identity)person.getObjectByIndex("Identity", 0);
			mr.addTableRow(html,  identity.getProperty("FamilyName"), "Nachname", 1, prefix);
			mr.addTableRow(html,  identity.getProperty("FirstName"), "Vorname",  1, prefix);
			
			Address address = (Address)person.getObjectByIndex("Address", 0);
			
			mr.addTableRow(html,  address.getProperty("AdditionalLine"), "Zusatzzeile",  1, prefix);
			
			
			
			int[] length1 = {300, 70};
			mr.addTableRow(html,  address.getProperty("Street"), "Strasse*/Nummer*", 1, prefix, address.getProperty("Number"), length1);
			
			int[] length2 = {70, 300};
			mr.addTableRow(html,  address.getProperty("Zipcode"), "PLZ*/Ort*", 1, prefix, address.getProperty("City"), length2);
			
			html.append("<tr><th class=\"inputlabel\">&nbsp;</th></tr>");
			
			Contact c = (Contact)person.getObjectByIndex("Contact", 3);	
			mr.addTableRow(html,  getProperty("contact_3"), "Email*", 1, prefix);
			
			c = (Contact)person.getObjectByIndex("Contact", 0);	
			mr.addTableRow(html,  getProperty("contact_0"), "Telefon privat*", 1, prefix);
			
			c = (Contact)person.getObjectByIndex("Contact", 1);	
			if(c != null){
				mr.addTableRow(html,  getProperty("contact_1"), "Telefon gesch.*", 1, prefix);
			}
			
			c = (Contact)person.getObjectByIndex("Contact", 2);	
			mr.addTableRow(html,  getProperty("contact_2"), "Telefon mobil", 1, prefix);
			
			html.append("<tr><th class=\"inputlabel\">&nbsp;</th></tr>");
			
			mr.addTableRow(html,  identity.getProperty("DateOfBirth") , "Geburtsjahr", 1, prefix);
			
			mr.addTableRow(html,  identity.getProperty("Sex") , "Anrede", 0, prefix);
			
			mr.addTableRow(html,  identity.getProperty("FirstLanguageS") , "Erstsprache", 1, prefix);
			
			
			//html.append("<tr><td class=\"inputlabel\" style=\"height : 120px;\">Sprachen</td><td>" + HTMLForm.getListField(getProperty("Languages") , true, prefix) + "</td></tr>");
			html.append("<tr><td class=\"inputlabel\" style=\"height : 120px;\">Sprachen</td><td>" + HTMLForm.getMultipleItemWidget(getProperty("Languages") , true, prefix) + "</td></tr>");
			
			//mr.addTableRow(html,  getProperty("Comment") , "Bemerkungen", 4, prefix);
		
			//public static void addTableRow(StringBuilder html, Property p, String label, int type, String prefix, Property p2, int[] length, int helpitemid, Hashtable errors){
			mr.addTableRow(html,  getProperty("Password") , "Passwort", 7, prefix, null, null, 8, errors);
			mr.addTableRow(html,  getProperty("PasswordControl") , "Passwort überprüfen", 5, prefix, null, null, 8, errors); 	 	 
 	 	 
			//html.append("<tr><td colspan=\"2\">* diese Felder müssen ausgefüllt werden <br>** leer lassen, um das bisherige Passwort zu behalten");
			html.append("<tr><td></td><td><input class=\"nodebutton\" type=\"button\" style=\"float : right;\" value=\"Speichern\" onclick=\"getNextNode('createmember=true')\"></td></tr>");
			html.append("<tr><td colspan=\"2\" style=\"height : 40px\"><a href=\"javascript:getNextNode(\'deleteprofile=true\',true)\" style=\"color : #FFCD00; text-decoration : none;\"><img src=\"res/icons/profil-loeschen.png\" style=\"float : left;\">mein Profil löschen</a><br>Es erfolgt nochmals eine Rückfrage durch die Geschäftsstelle, bevor das Profile endgültig gelöscht wird"
			//	+ "<p>WICHTIG: Es dauert ca. 24 Stunden, bis Ihre Registrierung aktiviert ist.</td></tr>");
			
			+ "<p>WICHTIG: Adressänderungen, Namensänderungen etc. müssen von der Administration freigeschaltet werden. Bei glaubwürdigen negativen Feedbacks anderer Personen erlauben wir uns, Profile zu sperren, bis die Vorkommnisse geklärt sind. Vor allem bei Feedbacks kann dies mehrere Tage dauern.</td></tr>");
			
			html.append("</form>");
 	 	 
	     }
 	 	 return html.toString();
 	 }
 	 public String getProfileEditForm2(ApplicationContext context){
 	 	 StringBuilder html = new StringBuilder();
 	 	 
 	 	 /*
 	 	 int top = 0;
 	 	 int left = 160;
 	 	 MemberRegistration mr = null;
 	 	 
				html.append("<style type=\"text/css\">");
				html.append("\n#wizard{ height : " + (900 + top) + "px;} ");
				html.append("\n</style>");
				
 	 	 html.append("<form id=\"processNodeForm\">");
 	 	 html.append("<div style=\"position : absolute; width : 400px; height : 700px;\">");
 	 	 if(person != null){
 	 	 	 
 	 	 	 Identity identity = person.getIdentity();
 	 	 	 
 	 	 	 mr.addLabel( html, "Name", 0, top);
 	 	 	 mr.addLabel( html, identity.getString("FamilyName") , left, top);
 	 	 	 	 
 	 	 	 top = top + 40;
 	 	 	 mr.addLabel( html, "Vorname", 0, top);
 	 	 	 mr.addLabel( html, identity.getString("FirstName") , left, top);
 	 	 	 
 	 	 	 top = top + 40;
 	 	 	 mr.addLabel( html, "Geburtsdatum", 0, top);
 	 	 	 mr.addLabel( html, identity.getString("DateOfBirth") , left, top);
 	 	 	 
  	 	 	 top = top + 40;
 	 	 	 mr.addLabel( html, "Geschlecht", 0, top);
 	 	 	 mr.addLabel( html, identity.getString("Sex") , left, top);
 	 	 	 
  	 	 	 top = top + 40;
  	 	 	 
  	 	 	 html.append("<div style=\"position : absolute; top : " + (top + 20) + "px; width : 400px;\"><a href=\"javascript:getNextNode(\'edit=true\')\">Bearbeiten</a></div>");
  	 	 	 
 	 	 	 Address address = person.getAddress();
 	 	 	 
 	 	 	 top = top + 40;
 	 	 	 mr.addLabel( html, "Strasse", 0, top);
 	 	 	 if(edit){
 	 	 	 	 mr.addTextField(html, getProperty("Street"), "", 1, left, top, 300, 30, "", "");
 	 	 	 }
 	 	 	 else{
 	 	 	 	 mr.addLabel( html, address.getString("Street") , left, top);
 	 	 	 }
 	 	 	 top = top + 40;
 	 	 	 mr.addLabel( html, "Nummer", 0, top);
  	 	 	 if(edit){
 	 	 	 	 mr.addTextField(html, getProperty("Number"), "", 1, left, top, 300, 30, "", "");
 	 	 	 }
 	 	 	 else{
 	 	 	 	 mr.addLabel( html, address.getString("Number") , left, top);
 	 	 	 }
 	 	 	 
  	 	 	 top = top + 40;
 	 	 	 mr.addLabel( html, "PLZ", 0, top);
  	 	 	 if(edit){
 	 	 	 	 mr.addTextField(html, getProperty("Zipcode"), "", 1, left, top, 300, 30, "", "");
 	 	 	 }
 	 	 	 else{
 	 	 	 	 mr.addLabel( html, address.getString("Zipcode") , left, top);
 	 	 	 }
 	 	 	 
 	 	 	 
  	 	 	 top = top + 40;
 	 	 	 mr.addLabel( html, "Ort", 0, top);
  	 	 	 if(edit){
 	 	 	 	 mr.addTextField(html, getProperty("City"), "", 1, left, top, 300, 30, "", "");
 	 	 	 }
 	 	 	 else{
 	 	 	 	 mr.addLabel( html, address.getString("City") , left, top);
 	 	 	 }
 	 	 	 
   	 	 	 top = top + 80;
   	 	 	 
  	 	 	 mr.addLabel( html, "Email", 0, top);
  	 	 	 if(edit){
 	 	 	 	 mr.addTextField(html, getProperty("contact_3"), "", 1, left, top, 300, 30, "", "");
 	 	 	 }
 	 	 	 else{
 	 	 	 	 mr.addLabel( html, getString("contact_3") , left, top);
 	 	 	 }
 	 	 	 
   	 	 	 top = top + 40;   	 	 	 
  	 	 	 mr.addLabel( html, "Tel. privat", 0, top);
  	 	 	 if(edit){
 	 	 	 	 mr.addTextField(html, getProperty("contact_0"), "", 1, left, top, 300, 30, "", "");
 	 	 	 }
 	 	 	 else{
 	 	 	 	 mr.addLabel( html, getString("contact_0") , left, top);
 	 	 	 }
 	 	 	 
    	 	 top = top + 40;   	 	 	 
  	 	 	 mr.addLabel( html, "Tel. mobil", 0, top);
  	 	 	 if(edit){
 	 	 	 	 mr.addTextField(html, getProperty("contact_2"), "", 1, left, top, 300, 30, "", "");
 	 	 	 }
 	 	 	 else{
 	 	 	 	 mr.addLabel( html, getString("contact_2") , left, top);
 	 	 	 }
 	 	 	 
 	 	 	  top = top + 40; 
 	 	 	 html.append("<div style=\"position : absolute; top : " + (top + 20) + "px; width : 400px;\"><input type=\"button\" onclick=\"getNextNode()\" value=\"Fertig\"></div>");
 	 	 	
 	 	 }
 	 	 html.append("</form>");
 	 	 html.append("</div>");
 	 	 
 	 	 */
 	 	 return html.toString();
 	 }
 	 class ProfileEditNode extends BasicProcessNode{
 	 	 public boolean validate(ApplicationContext context){
 	 	 	 errors.clear();
 	 	 	 if(context.hasProperty("edit")){
 	 	 	 	 edit = 1;
 	 	 	 	 return false;	 	 	 	 
 	 	 	 }
 	 	 	 else if(context.hasProperty("deleteprofile")){
 	 	 	 	 OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
 	 	 	 	 
 	 	 	 	 
				String now = DateConverter.dateToSQL(new java.util.Date(), true);
				om.setProperty("DateModified", now);
 	 	 	 	om.setProperty("UserModified", om.getName());
				
 	 	 	 	om.setProperty("Status", 3);
 	 	 	 	
 	 	 	 	
 	 	 	
 	 	 	 	ocs.updateObject(om);
 	 	 	 	
 	 	 	 	OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
 	 	 	 	userSession.setOrganisationMember(null);
 	 	 	 	
 	 	 	 	userSession.put("goodbye", "true");
 	 	 	 	
 	 	 	 	return true;
 	 	 	 }
 	 	 	 else if(context.hasProperty("Password")){
 	 	 	 	 String password = context.getString("Password");
 	 	 	 	 String passwordcontrol = context.getString("PasswordControl");
 	 	 	 	 String feedback = "";
 	 	 	 	 boolean success = true;
 	 	 	 	 if(password.length() > 0){
 	 	 	 	 	 if(!password.equals(passwordcontrol)){
 	 	 	 	 	 	 //feedback += "Passwort und Kontrolle stimmen nicht überein";
 	 	 	 	 	 	 errors.put("Password", "Passwort und Kontrolle stimmen nicht überein");
 	 	 	 	 	 	 errors.put("PasswordControl", "Passwort und Kontrolle stimmen nicht überein");
 	 	 	 	 	 	 success = false;
 	 	 	 	 	 }
 	 	 	 	 	 else if(password.length() < 8){
 	 	 	 	 	 	 //feedback += "Das Passwort muss mindestens 8 Zeichen lang sein";
 	 	 	 	 	 	 errors.put("Password", "Das Passwort muss mindestens 8 Zeichen lang sein");
 	 	 	 	 	 	 success = false;
 	 	 	 	 	 }
 	 	 	 	 	 else if(password.length() > 16){
 	 	 	 	 	 	 //feedback += "Das Passwort muss mindestens 8 Zeichen lang sein";
 	 	 	 	 	 	 errors.put("Password", "Das Passwort darf höchstens 16 Zeichen lang sein");
 	 	 	 	 	 	 success = false;
 	 	 	 	 	 }
 	 	 	 	 }
 	 	 	 	 getParent().setProperty("validationfeedback", feedback);
 	 	 	 	 return success;
 	 	 	 }
 	 	 	 else{
 	 	 	 	 return true;
 	 	 	 }

 	 	 }
 	 }
 }