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
 
 import org.kubiki.util.DateConverter;
 
 import java.util.Vector;
 import java.util.List;
 import java.util.Hashtable;
 
 import javax.servlet.http.*;
 
 import org.apache.commons.lang3.StringEscapeUtils;
 
 
 public class MemberRegistration extends BasicProcess{
 
	BasicProcessNode node1, node2, node3;

	MemberAdCategory mac = null;

	OrganisationMember om;
    OrganisationMember parent;
	OrganisationMember om1, om2;
	Person person1, person2;
	
	OpenCommunityServer ocs;
	
	boolean registrationsuccessful = false;
	
	Vector sex;
	Vector yes_no;
	
	private Hashtable errors = null;
	
	boolean issubprofile = false;
	
	OrganisationMemberAdministration oma;
 
	public MemberRegistration(){
		
		setTitle("Anmelden");
		
		addProperty("delegatecommunication", "Boolean", "false");
		addProperty("isorganisation", "Boolean", "false");
		addProperty("registration", "Boolean", "false");
		addProperty("recovery", "Boolean", "false");
		addProperty("consentconfirmed", "Boolean", "false");	
		addProperty("validationfeedback", "String", "");
		addProperty("loadpage", "String", "");
		addProperty("parentid", "String", "");
		
		addProperty("Role1", "Integer", "", false, "Rolle/Beziehung");
		addProperty("Role2", "Integer", "", false, "Rolle/Beziehung");

		addProperty("Type", "Integer", "1", false, "Typ");
		addProperty("Comment", "Text", "", false, "Bemerkungen");
		
		addObjectCollection("Person", "ch.opencommunity.base.Person");
		
		//addObjectCollection("Person", "ch.opencommunity.base.Person");
	
		node1 = new LoginNode();
		addNode(node1);
		node1.setName("LoginNode");

		Property p = addProperty("Username", "String", "");
		node1.addProperty(p);
		
		p = addProperty("Password", "Password", "");
		node1.addProperty(p);
		
		//node2 = new OrganisationMemberAddNode();
		
		p = addProperty("Organisation", "String", "", false, "Organisation");
		
		
		node1.addProperty(p);
		

		

		
		setCurrentNode(node1);
		
		errors = new Hashtable();
		

	}
	public void initProcess(ApplicationContext context){
	
		ocs = (OpenCommunityServer)getRoot();
		
		oma = (OrganisationMemberAdministration)ocs.getApplicationModule("OrganisationMemberAdministration");

		addProperty("person1_DataProtection", "Integer", "0");
		
		person1 = new Person();
		person1.setName("person1");
		person1.setParent(this);
		person1.addProperty("Languages", "ListFillIn", "");
		person1.getProperty("Languages").setSelection(ocs.getSupportedLanguages());
		Identity identity = new Identity();
		person1.addSubobject("Identity", identity);
		Address address = new Address();
		person1.addSubobject("Address", address);
		for(int i = 0; i < 4; i++){
			Contact c = new Contact();
			c.setProperty("Type", "" + i);
			person1.addSubobject("Contact", c);
		}
		sex = new Vector();
		sex.add(new ConfigValue("1", "1", "Herr"));
		sex.add(new ConfigValue("2", "2", "Frau"));
		identity.getProperty("Sex").setSelection(sex);
		identity.getProperty("FirstLanguage").setSelection(ocs.getSupportedLanguages2());
		
		yes_no = new Vector();
		yes_no.add(new ConfigValue("1", "1", "Ja"));
		yes_no.add(new ConfigValue("0", "0", "Nein"));
		getProperty("person1_DataProtection").setSelection(yes_no);
		
		identity.getProperty("FamilyName").setAction("checkDoubles(this.id, this.value)");
		identity.getProperty("FirstName").setAction("checkDoubles(this.id, this.value)");
		identity.getProperty("DateOfBirth").setAction("checkDoubles(this.id, this.value)");
		
		if(getString("parentid").length() > 0){
			OpenCommunityUserSession usersession = (OpenCommunityUserSession)context.getObject("usersession");
			if(usersession != null){
				parent = usersession.getOrganisationMember();
				setProperty("registration", "true");
				/*
				if(node2==null){
					node2 = new OrganisationMemberAddNode();
					addNode(node2);
					node2.setName("AGBNode");
				}
				if(node3==null){
					node3 = addNode();
					node3.setName("FeedbackNode");
				}
				*/
				getProcess().setTitle("Registrieren");
				issubprofile = true;
			}
		}
		

		getProperty("Role1").setSelection(ocs.getMemberRoles());
		getProperty("Role2").setSelection(ocs.getMemberRoles());
	}
	
	@Override
	public List<String> getPropertyNames(){
		
		List<String> names = new Vector<String>();
		
		names.add("Username");
		names.add("Password");
		
		names.add("registration");
		names.add("recovery");
		names.add("delegatecommunication");
		names.add("isorganisation");
		names.add("consentconfirmed");
		names.add("parentid");
		
		names.add("Organisation");
		names.add("Role2");

		names.add("person1_DataProtection");
		
		names.add("person1_FamilyName");
		names.add("person1_FirstName");
		names.add("person1_Sex");
		names.add("person1_FirstLanguageS");
		names.add("person1_DateOfBirth");
		names.add("person1_Street");
		names.add("person1_AdditionalLine");
		names.add("person1_Number");
		names.add("person1_Zipcode");
		names.add("person1_City");
		names.add("person1_Country");
		names.add("person1_Comment");
		names.add("person1_0_Value");
		names.add("person1_1_Value");
		names.add("person1_2_Value");
		names.add("person1_3_Value");
		names.add("person1_4_Value");
		
		names.add("person1_Languages");

		names.add("person2_DataProtection");
		
		names.add("person2_FamilyName");
		names.add("person2_FirstName");
		names.add("person2_Sex");
		names.add("person2_FirstLanguageS");
		names.add("person2_DateOfBirth");
		names.add("person2_AdditionalLine");
		names.add("person2_Street");
		names.add("person2_Number");
		names.add("person2_Zipcode");
		names.add("person2_City");
		names.add("person2_Country");
		names.add("person2_Comment");
		names.add("person2_0_Value");
		names.add("person2_1_Value");
		names.add("person2_2_Value");
		names.add("person2_3_Value");
		names.add("person2_4_Value");
		
		names.add("person2_Languages");
		
		names.add("Comment");
		names.add("Type");
		
		for(BasicClass o : getObjects("Person")){
			
			names.add(o.getName() + "_FirstName");	
			names.add(o.getName() + "_Sex");	
			names.add(o.getName() + "_DateOfBirth");	
			
		}
		return names;
		
	}
	public boolean handleLogin(OpenCommunityServer ocs, ApplicationContext context){
		
		String username = getString("Username");
		String password = getString("Password");
			
			
			
		username = username.trim();
		if(username.indexOf("@")==-1){
			int diff = 7 - username.length();
			for(int i = 0; i < diff; i++){
				username = "0" + username;	
			}
		}
		ocs.logAccess("Username: " + username);
		ObjectCollection results = new ObjectCollection("Results", "*");
							String sql = "";
							if(username.indexOf("@")==-1){
								sql = "SELECT t1.OrganisationMemberID AS ID, t2.Status FROM Login AS t1 JOIN OrganisationMember AS t2 ON t1.OrganisationMemberID=t2.ID WHERE t1.Username=\'" + username + "\' AND t1.Password=\'" + password + "\'";
							}
							else{
								sql = "SELECT t1.OrganisationMemberID AS ID, t2.Status FROM Login AS t1 JOIN OrganisationMember AS t2 ON t1.OrganisationMemberID=t2.ID";
								sql += " JOIN Person AS t3 ON t2.Person=t3.ID";
								sql += " JOIN Contact AS t4 ON t4.PersonID=t3.ID AND t4.Type=3";
								sql += " WHERE t4.Value=\'" + username.trim() + "\' AND t1.Password=\'" + password + "\'";
							}
			ocs.logAccess(sql);
			ocs.queryData(sql, results);
			if(results.getObjects().size()==1){
				
				Record record = (Record)results.getObjectByIndex(0);
				int status = record.getInt("STATUS");
				if(status==1){
					ocs.logAccess("login erfolgreich");
					HttpServletRequest request = ((WebApplicationContext)context).getRequest();
					HttpSession session = request.getSession();
					OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
					if(userSession==null){
						userSession = new OpenCommunityUserSession();
					}
					userSession.setParent(ocs);
												//userSession.setLogin(login);
					session.setAttribute("usersession", userSession);					
	
					
					OrganisationMember om = (OrganisationMember)ocs.getObject(null, "OrganisationMember", "ID", record.getString("ID"));
					if(om != null){
						om.setParent(ocs);
						userSession.setOrganisationMember(om);
						userSession.setOrganisationMemberID(new Integer(om.getName()).intValue());
						om.setParent(userSession);
						om.initObjectLocal();
						
						AccessLog accessLog = new AccessLog();
						accessLog.setProperty("OrganisationMemberID", om.getName());
						accessLog.setProperty("DateCreated", DateConverter.dateToSQL(new java.util.Date(), true));
						ocs.insertSimpleObject(accessLog);
					}
					else{
						//writer.print("Login failed");
					}
					return true;
				}
				else{
					setComment("Der Zugang ist vorübergehend deaktiviert");
					return false;
				}
																	
			}
			else{
				setComment("Falscher Benutzername oder falsches Passwort");
				return false;	
			}
		
	}
	public void finish(ProcessResult result, ApplicationContext context){
	
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		
		//if(getString("Username").length() > 0){
		
		if(getBoolean("recovery")){
			
			String username = context.getString("Username");
			
			
			username = username.trim();
			if(username.indexOf("@")==-1){
				int diff = 7 - username.length();
				for(int i = 0; i < diff; i++){
					username = "0" + username;	
				}
			}
			

			String sql = "";
			
			if(username.indexOf("@")==-1){
				sql = "SELECT t1.OrganisationMemberID, t4.Value AS Email FROM Login AS t1 JOIN OrganisationMember AS t2 ON t1.OrganisationMemberID=t2.ID JOIN Person AS t3 ON t2.Person=t3.ID JOIN Contact AS t4 ON t4.PersonID=t3.ID AND t4.Type=3 WHERE Username=\'" + username + "\'";
				sql += " AND t2.Status=1";
			}
			else{
				sql = "SELECT t1.OrganisationMemberID, t2.Status, t4.Value AS Email FROM Login AS t1 JOIN OrganisationMember AS t2 ON t1.OrganisationMemberID=t2.ID";
				sql += " JOIN Person AS t3 ON t2.Person=t3.ID";
				sql += " JOIN Contact AS t4 ON t4.PersonID=t3.ID AND t4.Type=3";
				sql += " WHERE t4.Value=\'" + username.trim() + "\'";
				sql += " AND t2.Status=1";
			}			
			
			ObjectCollection results = new ObjectCollection("Results", "*");
			
			ocs.queryData(sql, results);
			
			String email = null;
			String omid = null;
			if(results.getObjects().size()==1){
				for(BasicClass bc : results.getObjects()){
					email = bc.getString("EMAIL");
					omid = bc.getString("ORGANISATIONMEMBERID");
				}
			}
			if(email != null && omid != null && email.length() > 0 && omid.length() > 0){
				
				HttpServletRequest request = ((WebApplicationContext)context).getRequest();
				
				String password = ocs.createPassword(8);
				String registrationcode = ocs.createPassword(20);
				
				ocs.executeCommand("UPDATE Login SET RecoveryPassword=\'" + password + "\', RegistrationCode=\'" + registrationcode + "\', Status=4 WHERE OrganisationMemberID=" + omid);
			
				String message = "Ein neues Passwort wurde erstellt:";
						
				message += "\n\nIhr neues Passwort : " + password;
						
				message += "\n\nSie müssen den Vorgang abschliessen, indem Sie auf den untenstehenden Link klicken:";
						
				message += "\n\n" + ocs.getBaseURL("", request) + "/servlet.srv?action=recovery&registrationcode=" + registrationcode;		
				
				ocs.logAccess(message);
						
				ocs.sendEmail(message, "Neues Passwort erstellt" , email, null);
				
				result.setParam("newprocess", "ch.opencommunity.process.FeedbackShow");
				result.setParam("newprocessparams", "TextBlockID=11");	
				
			}
			
		}
		else if(!getBoolean("registration")){


			if(getString("loadpage").length() > 0){
				result.setParam("loadpage", getString("loadpage"));
			}
			else{
				//result.setParam("refresh", "currentpage");
				result.setParam("loadpage", "/profile");
			}
			
		}
		else{
			
			result.setParam("refresh", "currentpage");

		}
				
		
	}
	public String getFeedbackForm(ApplicationContext context){
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		String feedback = ocs.getTextblockContent("9");
		feedback = StringEscapeUtils.unescapeHtml4(feedback);
		
		StringBuilder html = new StringBuilder();
		html.append("<div style=\"min-height : 100px;\">" + feedback + "</div>");
		//html.append("<input type=\"button\" value=\"Abschliessen\" onclick=\"getNextNode('createmember=true')\" style=\" position : absolute; bottom: 20px; background : orange;\">");
		return html.toString();
	}
	public String getAGBForm(ApplicationContext context){
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		
		
		StringBuilder html = new StringBuilder();	
		
		html.append("Um die Registrierung abzuschliessen lesen Sie bitte unsere Regeln und akzeptieren Sie diese.");

		
		html.append("<select onchange=\"setAGB(this.value)\">");
		for(ConfigValue language : ocs.getSupportedLanguages()){
			if(language.getValue().equals("Deutsch")){
				html.append("<option value=\"" + language.getValue() + "\" SELECTED>" + language + "</option>");
			}
			else{
				html.append("<option value=\"" + language.getValue() + "\">" + language + "</option>");
			}
		}
		html.append("</select>");
		html.append("<div id=\"agb\">");
		html.append(ocs.getAGB("Deutsch"));
		html.append("</div>");

		html.append("<form id=\"processNodeForm\">");
		html.append("<input type=\"checkbox\" id=\"consentconfirmed\" name=\"consentconfirmed\" value=\"true\" style=\"vertical-align : middle;\">Ich habe die Regeln gelesen und akzeptiert");
		html.append("</form>");

		
		html.append("<br><input class=\"nodebutton\" type=\"button\" value=\"Abschliessen\" onclick=\"getNextNode('createmember=true')\"><br>&nbsp;");
		return html.toString();
	}
	public String getLoginForm(ApplicationContext context){
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		
		StringBuilder html = new StringBuilder();
		
		int top = 0;
		

		
		html.append("<form id=\"processNodeForm\">");

		
		//if(context.hasProperty("registration")){
		
		if(getBoolean("recovery")){
			
	        html.append("<table>");
	        
	        if(getComment().length() > 0){
	        	html.append("<tr><td colspan=\"2\">" 	+ getComment() + "</td></tr>");
	        }

	        html.append("<tr><td colspan=\"2\">Geben Sie Ihnen Benutzernamen oder Ihre Emailadresse ein, Sie erhalten ein neues Passwort an Ihre Emailadresse zugestellt,  mit der Sie sich registriert haben.</td></tr>");

	        addTableRow(html, getProperty("Username"), "Benutzername");	
	        
	        html.append("</table>");
	        
			html.append("<br><input class=\"nodebutton\" type=\"button\" value=\"Abschliessen\" onclick=\"getNextNode()\">");
			
		}
		else if(getBoolean("registration")){
			
			html.append("<table style=\"width : 540px;\"");
			
			if(getString("validationfeedback").length() > 0){
				//html.append("<tr><td colspan=\"2\">" + getString("validationfeedback") + "</td></tr>");
			}
			
			if(getBoolean("delegatecommunication")==false && getBoolean("isorganisation")==false){
				
				if(getString("parentid").length() == 0){
				
					html.append("<tr><td colspan=\"2\">");

					//addCheckBox(html, getProperty("delegatecommunication"), "Benutzerprofil für eine andere Person erstellen", 0, 0, 250, 300, "delegatecommunication=true");
					
					for(BasicClass o : oma.getObjects("OrganisationMemberType")){
						String checked = "";
						if(o.getName().equals(getString("Type"))){
							checked = " CHECKED";		
						}
						//html.append("<br><input type=\"radio\" value=\"" + o.getName() + "\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\"" + checked + ">" + o.getString("Description"));
					}
					
				/*
					
				if(getID("Type")==1){
					html.append("<input type=\"radio\" value=\"1\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\" CHECKED>Ich nutze das NN nur für mich");
					addHelpButton(html, 11);
					html.append("<br><input type=\"radio\" value=\"2\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\">Ich nutze das NN für mich und meine / unsere Kinder");
					addHelpButton(html, 12);
					html.append("<br><input type=\"radio\" value=\"3\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\">Ich nutze das NN für mich und eine weitere Person (Bekannte / Verwandte)");
					addHelpButton(html, 13);
					html.append("<br><input type=\"radio\" value=\"4\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\">Ich nutze das NN für Klienten meiner Institution");
					addHelpButton(html, 14);
					html.append("<br><input type=\"radio\" value=\"5\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\">Ich nutze das NN für einen Verein, Organisation, Gruppe ");
					addHelpButton(html, 15);

				}
				else if(getID("Type")==2){
					html.append("<input type=\"radio\" value=\"1\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\">Ich nutze das NN nur für mich");
					addHelpButton(html, 11);
					html.append("<br><input type=\"radio\" value=\"2\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\" CHECKED>Ich nutze das NN für mich und meine / unsere Kinder");
					addHelpButton(html, 12);
					html.append("<br><input type=\"radio\" value=\"3\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\">Ich nutze das NN für mich und eine weitere Person (Bekannte / Verwandte)");
					addHelpButton(html, 13);
					html.append("<br><input type=\"radio\" value=\"4\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\">Ich nutze das NN für Klienten meiner Institution");
					addHelpButton(html, 14);
					html.append("<br><input type=\"radio\" value=\"5\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\">Ich nutze das NN für einen Verein, Organisation, Gruppe ");
					addHelpButton(html, 15);
				}
				else if(getID("Type")==3){
					html.append("<input type=\"radio\" value=\"1\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\">Ich nutze das NN nur für mich");
					addHelpButton(html, 11);
					html.append("<br><input type=\"radio\" value=\"2\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\">Ich nutze das NN für mich und meine / unsere Kinder");
					addHelpButton(html, 12);
					html.append("<br><input type=\"radio\" value=\"3\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\" CHECKED>Ich nutze das NN für mich und eine weitere Person (Bekannte / Verwandte)");
					addHelpButton(html, 13);
					html.append("<br><input type=\"radio\" value=\"4\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\">Ich nutze das NN für Klienten meiner Institution");
					addHelpButton(html, 14);
					html.append("<br><input type=\"radio\" value=\"5\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\">Ich nutze das NN für einen Verein, Organisation, Gruppe ");
					addHelpButton(html, 15);
				}
				else if(getID("Type")==4){
					html.append("<input type=\"radio\" value=\"1\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\">Ich nutze das NN nur für mich");
					addHelpButton(html, 11);
					html.append("<br><input type=\"radio\" value=\"2\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\">Ich nutze das NN für mich und meine / unsere Kinder");
					addHelpButton(html, 12);
					html.append("<br><input type=\"radio\" value=\"3\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\">Ich nutze das NN für mich und eine weitere Person (Bekannte / Verwandte)");
					addHelpButton(html, 13);
					html.append("<br><input type=\"radio\" value=\"4\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\" CHECKED>Ich nutze das NN für Klienten meiner Institution");
					addHelpButton(html, 14);
					html.append("<br><input type=\"radio\" value=\"5\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\">Ich nutze das NN für einen Verein, Organisation, Gruppe ");
					addHelpButton(html, 15);
				}
				else if(getID("Type")==5){
					html.append("<input type=\"radio\" value=\"1\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\">Ich nutze das NN nur für mich");
					addHelpButton(html, 11);
					html.append("<br><input type=\"radio\" value=\"2\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\">Ich nutze das NN für mich und meine / unsere Kinder");
					addHelpButton(html, 12);
					html.append("<br><input type=\"radio\" value=\"3\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\">Ich nutze das NN für mich und eine weitere Person (Bekannte / Verwandte)");
					addHelpButton(html, 13);
					html.append("<br><input type=\"radio\" value=\"4\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\">Ich nutze das NN für Klienten meiner Institution");
					addHelpButton(html, 14);
					html.append("<br><input type=\"radio\" value=\"5\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\" CHECKED>Ich nutze das NN für einen Verein, Organisation, Gruppe ");
					addHelpButton(html, 15);
				}
				
				*/
					
					//addHelpButton(html, 1);
					

					//addCheckBox(html, getProperty("isorganisation"), "Organisation registrieren", 250, 0, 250, 300, "isorganisation=true");
					
					//addHelpButton(html, 2);
					
					html.append("</td></tr>");
					
					if(getID("Type") > 1){
						
						html.append("<tr><td>Grund?</td><td colspan=\"1\">");
						
						html.append("<textarea name=\"Comment\">" + getString("Comment") + "</textarea>");
						
						html.append("</td></tr>");
						
					}
				
				}
				

				
				getPersonAddForm(html, person1, "person1_", 40 + top, 700);
				
				if(getID("Type")==2){
					
					html.append("<tr><td><input type=\"button\" value=\"Kind hinzufügen\" onclick=\"getNextNode('personadd=true')\"></td></tr>");	
					
					int i = 0; 
					for(BasicClass o : getObjects("Person")){
						
						i++;
						
						Person person = (Person)o;
						
						Identity identity = person.getIdentity();
						
						html.append("<tr><td>Kind " + i + "</td><td><img src=\"images/delete.png\" onclick=\"getNextNode('persondelete=true&objectPath=" + person.getPath() + "')\" style=\"float : right;\"></td></tr>");	
						
						html.append("<tr><td>Vorname</td><td>" + HTMLForm.getTextField(identity.getProperty("FirstName"), true, person.getName() + "_") + "</td></tr>");	
						html.append("<tr><td>Geschlecht</td><td>" + HTMLForm.getSelection(identity.getProperty("Sex"), true, person.getName() + "_") + "</td></tr>");	
						html.append("<tr><td>Geburtsjahr</td><td>" + HTMLForm.getTextField(identity.getProperty("DateOfBirth"), true, person.getName()  + "_") + "</td></tr>");	
						
					}
					
					
				}
	
				html.append("</table>");
				
			
			}
			else if(getBoolean("isorganisation")){
				html.append("<tr><td colspan=\"2\">");				
				addCheckBox(html, getProperty("isorganisation"), "Organisation anmelden", 250, 0, 250, 300, "isorganisation=false");
				html.append("</td></tr>");				

				
				addTableRow(html, getProperty("Organisation"), "Organisation");			
				getPersonAddForm(html, person1, "person1_", 80 + top, 700);
			
			}
			//else if(context.hasProperty("delegatecommunication")){
			else if(getBoolean("delegatecommunication")){
				html.append("<tr><td colspan=\"2\">");
				addCheckBox(html, getProperty("delegatecommunication"), "Benutzerprofil für eine andere Person erstellen", 0, 0, 250, 300, "delegatecommunication=false");
				html.append("</td></tr>");			
						
				addProperty("person2_DataProtection", "Integer", "0");
				getProperty("person1_DataProtection").setSelection(yes_no);
				
				/*
				if(person2==null){
					person2 = new Person();
					person2.setName("person2");
					person2.setParent(this);
					person2.addProperty("Languages", "ListFillIn", "");
					person2.getProperty("Languages").setSelection(ocs.getSupportedLanguages());
					Identity identity = new Identity();
					
					identity.getProperty("Sex").setSelection(sex);
					identity.getProperty("FirstLanguage").setSelection(ocs.getSupportedLanguages2());
					
					
					person2.addSubobject("Identity", identity);
					Address address = new Address();
					person2.addSubobject("Address", address);
					
					for(int i = 0; i < 4; i++){
						Contact c = new Contact();
						c.setProperty("Type", "" + i);
						person2.addSubobject("Contact", c);
					}
				}
				*/
				
				//html.append("<tr><td>In welchem Umfeld wollen Sie Profile verwalten?</td><td colspan=\"1\">");
				if(getID("Type")==1){
					html.append("<input type=\"radio\" value=\"1\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\" CHECKED>Ich nutze das NN nur für mich");
					addHelpButton(html, 11);
					html.append("<br><input type=\"radio\" value=\"2\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\">Ich nutze das NN für mich und meine / unsere Kinder");
					addHelpButton(html, 12);
					html.append("<br><input type=\"radio\" value=\"3\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\">Ich nutze das NN für mich und eine weitere Person (Bekannte / Verwandte)");
					addHelpButton(html, 13);
					html.append("<br><input type=\"radio\" value=\"4\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\">Ich nutze das NN für Klienten meiner Institution");
					addHelpButton(html, 14);
					html.append("<br><input type=\"radio\" value=\"5\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\">Ich nutze das NN für einen Verein, Organisation, Gruppe ");
					addHelpButton(html, 15);

				}
				else if(getID("Type")==2){
					html.append("<input type=\"radio\" value=\"1\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\">Ich nutze das NN nur für mich");
					addHelpButton(html, 11);
					html.append("<br><input type=\"radio\" value=\"2\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\" CHECKED>Ich nutze das NN für mich und meine / unsere Kinder");
					addHelpButton(html, 12);
					html.append("<br><input type=\"radio\" value=\"3\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\">Ich nutze das NN für mich und eine weitere Person (Bekannte / Verwandte)");
					addHelpButton(html, 13);
					html.append("<br><input type=\"radio\" value=\"4\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\">Ich nutze das NN für Klienten meiner Institution");
					addHelpButton(html, 14);
					html.append("<br><input type=\"radio\" value=\"5\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\">Ich nutze das NN für einen Verein, Organisation, Gruppe ");
					addHelpButton(html, 15);
				}
				else if(getID("Type")==3){
					html.append("<input type=\"radio\" value=\"1\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\">Ich nutze das NN nur für mich");
					addHelpButton(html, 11);
					html.append("<br><input type=\"radio\" value=\"2\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\">Ich nutze das NN für mich und meine / unsere Kinder");
					addHelpButton(html, 12);
					html.append("<br><input type=\"radio\" value=\"3\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\" CHECKED>Ich nutze das NN für mich und eine weitere Person (Bekannte / Verwandte)");
					addHelpButton(html, 13);
					html.append("<br><input type=\"radio\" value=\"4\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\">Ich nutze das NN für Klienten meiner Institution");
					addHelpButton(html, 14);
					html.append("<br><input type=\"radio\" value=\"5\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\">Ich nutze das NN für einen Verein, Organisation, Gruppe ");
					addHelpButton(html, 15);
				}
				else if(getID("Type")==4){
					html.append("<input type=\"radio\" value=\"1\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\">Ich nutze das NN nur für mich");
					addHelpButton(html, 11);
					html.append("<br><input type=\"radio\" value=\"2\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\">Ich nutze das NN für mich und meine / unsere Kinder");
					addHelpButton(html, 12);
					html.append("<br><input type=\"radio\" value=\"3\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\">Ich nutze das NN für mich und eine weitere Person (Bekannte / Verwandte)");
					addHelpButton(html, 13);
					html.append("<br><input type=\"radio\" value=\"4\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\" CHECKED>Ich nutze das NN für Klienten meiner Institution");
					addHelpButton(html, 14);
					html.append("<br><input type=\"radio\" value=\"5\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\">Ich nutze das NN für einen Verein, Organisation, Gruppe ");
					addHelpButton(html, 15);
				}
				else if(getID("Type")==5){
					html.append("<input type=\"radio\" value=\"1\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\">Ich nutze das NN nur für mich");
					addHelpButton(html, 11);
					html.append("<br><input type=\"radio\" value=\"2\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\">Ich nutze das NN für mich und meine / unsere Kinder");
					addHelpButton(html, 12);
					html.append("<br><input type=\"radio\" value=\"3\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\">Ich nutze das NN für mich und eine weitere Person (Bekannte / Verwandte)");
					addHelpButton(html, 13);
					html.append("<br><input type=\"radio\" value=\"4\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\">Ich nutze das NN für Klienten meiner Institution");
					addHelpButton(html, 14);
					html.append("<br><input type=\"radio\" value=\"5\" name=\"Type\" onchange=\"getNextNode('Type=' + this.value)\" CHECKED>Ich nutze das NN für einen Verein, Organisation, Gruppe ");
					addHelpButton(html, 15);
				}
				html.append("</td></tr>");
				
				html.append("<tr><td>Grund?</td><td colspan=\"1\">");
				
				html.append("<textarea name=\"Comment\">" + getString("Comment") + "</textarea>");
				
				html.append("</td></tr>");

				
				html.append("<tr><td colspan=\"2\" style=\"height : 40px\">Ihre Angaben</td></tr>");
				getPersonAddForm(html, person1, "person1_", 40 + top, 700);
				
				/*
				html.append("<tr><td colspan=\"2\" style=\"height : 40px\">Angaben der anderen Person</td></tr>");
				getPersonAddForm(html, person1, "person1_", 40 + top, 700);
			
				html.append("<tr><td colspan=\"2\" style=\"height : 40px\">Ihre Angaben</td></tr>");
				getPersonAddForm(html, person2, "person2_", 740 + top, 700);
				*/
				
				
			
			}

			html.append("<tr><td colspan=\"2\">");
			//html.append("* diese Felder müssen ausgefüllt werden ");
			html.append("<input class=\"nodebutton\" type=\"button\" style=\"float : right;\" value=\"Weiter\" onclick=\"confirmRegistration();\"></td></tr>");
			//html.append("<input class=\"nodebutton\" type=\"button\" style=\"float : right;\" value=\"Weiter\" onclick=\"getNextNode('createmember=true')\"></td></tr>");
			
			
			//html.append("<tr><td colspan=\"2\" style=\"height : 40px\">WICHTIG: Dieses Formular geht beim Absenden nur an die Verwaltung des NachbarNET und nicht direkt an andere NachbarNET-Benutzer/innen."
			//	+ "<br>Von Sonntag bis Mittwoch dauert es maximal 24 Stunden, bis Ihre Registrierung aktiviert ist.<b>Registrierungen ab Donnerstag Nachmittag werden erst am Montag bearbeitet.</td></tr>");
			
			html.append("</table>");
		}
		else{
	        html.append("<table>");
	        //html.append("<tr><th>Benutzername</th><td>");
	        if(getComment().length() > 0){
	        	html.append("<tr><td colspan=\"2\">" 	+ getComment() + "</td></tr>");
	        }
	        addTableRow(html, getProperty("Username"), "Benutzername");
	        addTableRow(html, getProperty("Password"), "Passwort", 5);
	        

			
			html.append("<tr><td colspan=\"2\">");
			
			//html.append("<div style=\" position: absolute; top : 80px;\">");
			html.append("<a class=\"popuplink\" href=\"javascript:getNextNode(\'recovery=true\')\" style=\" float : right; color : white\">Passwort vergessen?</a>");
			html.append("<br>");
			html.append("<br><input class=\"nodebutton\" type=\"button\" value=\"Anmelden\" onclick=\"getNextNode()\">");
			html.append("<br>");
			html.append("<br>");
			html.append("<p class=\"sectionheader\">Neu bei Nachbarnet?</p>");
			html.append("<a class=\"popuplink\" href=\"javascript:getNextNode(\'registration=true\')\">Registrieren</a>");
			html.append("<br>");
			html.append("<br>");
			//html.append("</div>");
			html.append("</td></tr></table>");
		}
		
		html.append("</form>");
		return html.toString();
	}
	public void addValidationFeedback(StringBuilder html){
		html.append("<div style=\"position : absolute; top : 40px; height : 100px; color : red;\">");
		html.append(getString("validationfeedback"));
		html.append("</div>");
	}
	public void getPersonAddForm(StringBuilder html, Person person, String prefix, int y, int height){
		
			//addTableRow(StringBuilder html, Property p, String label, int type, String prefix, Property p2, int[] length, int helpitemid, Hashtable errors)
			
			
		
			Identity identity = (Identity)person.getObjectByIndex("Identity", 0);
			Address address = (Address)person.getObjectByIndex("Address", 0);
			
			if(getID("Type")==4){
				addTableRow(html,  address.getProperty("AdditionalLine"), "Institution",  1, prefix, null, null, 10, errors);
			}
			else if(getID("Type")==5){
				addTableRow(html,  address.getProperty("AdditionalLine"), "Name Verein / Gruppe",  1, prefix, null, null, 10, errors);
			}

			
			addTableRow(html,  identity.getProperty("Sex") , "Anrede", 8, prefix, null, null, 0, errors);
			
			addTableRow(html,  identity.getProperty("FamilyName"), "Nachname", 1, prefix, null, null, 0, errors);
			addTableRow(html,  identity.getProperty("FirstName"), "Vorname",  1, prefix, null, null, 0, errors);
			
			
			
			//if(person.equals(person2) && getBoolean("delegatecommunication")){
				//addTableRow(html,  address.getProperty("AdditionalLine"), "Institution",  1, prefix, null, null, 10, errors);
				//addTableRow(html,  getProperty("Role2"), "Rolle/Beziehung",  2, prefix);
			//}
			
			
			

			
			//if(person.equals(person1) && getBoolean("delegatecommunication") || (parent != null && parent instanceof OrganisationMember)){
			if(parent != null && parent instanceof OrganisationMember){  //AK 20170919: Unterprofile werden nur noch nach Registrierung erstellt
				addTableRow(html,  getProperty("Role2"), "Rolle/Beziehung",  2, "", null, null, 6);
			}
			

			
			int[] length1 = {300, 70};
			addTableRow(html,  address.getProperty("Street"), "Strasse/Nummer", 1, prefix, address.getProperty("Number"), length1, 0, errors);
			
			int[] length2 = {70, 300};
			addTableRow(html,  address.getProperty("Zipcode"), "PLZ/Ort", 1, prefix, address.getProperty("City"), length2, 0, errors);
			
			addTableRow(html,  address.getProperty("Country"), "Land", 1, prefix, null, null, 0, errors);
			
			
			
			html.append("<tr><th class=\"inputlabel\">&nbsp;</th></tr>");
			
			Contact c = (Contact)person.getObjectByIndex("Contact", 3);	
			if(person.equals(person1) && getBoolean("delegatecommunication")){
				addTableRow(html,  c.getProperty("Value"), "E-Mail-Adresse", 1, prefix + "3_", null, null, 0, errors);
			}
			else{
				addTableRow(html,  c.getProperty("Value"), "E-Mail-Adresse", 1, prefix + "3_", null, null, 0, errors);				
			}
			
			c = (Contact)person.getObjectByIndex("Contact", 0);	
			addTableRow(html,  c.getProperty("Value"), "Festnetznummer", 1, prefix + "0_", null, null, 0, errors);
			
			c = (Contact)person.getObjectByIndex("Contact", 1);	
			if(person.equals(person2) && getBoolean("delegatecommunication")){
				addTableRow(html,  c.getProperty("Value"), "Telefon gesch.", 1, prefix + "1_", null, null, 0, errors);
			}
			
			c = (Contact)person.getObjectByIndex("Contact", 2);	
			addTableRow(html,  c.getProperty("Value"), "Mobilnummer", 1, prefix + "2_", null, null, 0, errors);
			
			html.append("<tr><th class=\"inputlabel\">&nbsp;</th></tr>");
			
			addTableRow(html,  identity.getProperty("DateOfBirth") , "Geburtsjahr", 1, prefix, null, null, 0, errors);
			
			addTableRow(html,  identity.getProperty("FirstLanguageS") , "Erstsprache", 1, prefix, null, null, 0, errors);
			
			//html.append("<tr><td class=\"inputlabel\" style=\"height : 120px;\">Weitere Sprachen " + getHelpButton(5) + "</td><td>" + HTMLForm.getListField(person.getProperty("Languages") , true, prefix) + "</td></tr>");
			html.append("<tr><td class=\"inputlabel\" style=\"height : 120px;\">Weitere Sprachen " + getHelpButton(5) + "</td><td>" + HTMLForm.getMultipleItemWidget(person.getProperty("Languages") , true, prefix) + "</td></tr>");
			
			/*
			if(!getBoolean("delegatecommunication") || person.equals(person2)){
				//addTableRow(html,  person.getProperty("Comment") , "Ihre Situation", 4, prefix, null, null, 3);
				addTableRow(html,  person.getProperty("Comment") , "Bemerkungen", 4, prefix, null, null, 3);
			}
			*/
			
			
			
	}
	
	public void getPersonAddForm2(StringBuilder html, Person person, String prefix, int y, int height){
		
			int left = 160;
			int top = 0;
			
			html.append("<div style=\"position: absolute; top : " + y + "px; height : " + height + "px;\">");
			
			
			
			Identity identity = (Identity)person.getObjectByIndex("Identity", 0);
			
			addLabel(html, "Name*", 0, top);
			addTextField(html, identity.getProperty("FamilyName"), "Nachname", 1,  left ,top,300,30, "inputbig", prefix);
			
			top += 40;
			addLabel(html, "Vorname*", 0, top);
			addTextField(html, identity.getProperty("FirstName"), "Vorname", 1,  left , top,300,30, "inputbig", prefix);
			
			Address address = (Address)person.getObjectByIndex("Address", 0);
			
			top += 40;
			addLabel(html, "Strasse*/Nummer*", 0, top);
			addTextField(html, address.getProperty("Street"), "Strasse*/Nummber*", 1,  left, top,200,30, "inputbig2", prefix);
			addTextField(html, address.getProperty("Number"), "", 1,  370, top, 60,30, "inputsmall1", prefix);
			
			top += 40;
			addLabel(html, "PLZ*/Ort*", 0,120);
			addTextField(html, address.getProperty("Zipcode"), "PLZ*/Ort*", 1,  left , top, 80,30, "inputsmall1", prefix);
			addTextField(html, address.getProperty("City"), "", 1,  260, top, 150,30, "inputbig2", prefix);
			
			top += 40;

			Contact c = (Contact)person.getObjectByIndex("Contact", 3);	
			top += 40;
			addLabel(html, "Email Adresse*", 0, top);
			addTextField(html, c.getProperty("Value") , "PLZ*/Ort*", 1,  left , top,300,30, "inputbig", prefix + "3_");
			
			c = (Contact)person.getObjectByIndex("Contact", 0);
						top += 40;
			addLabel(html, "Telefon P*/Telefon G", 0, top);
			addTextField(html, c.getProperty("Value"), "PLZ*/Ort*", 1,  left , top, 160,30, "inputmedium", prefix + "0_");
			
			c = (Contact)person.getObjectByIndex("Contact", 1);
			addTextField(html, c.getProperty("Value"), "PLZ*/Ort*", 1,  left + 170 ,  top, 160,30, "inputmedium", prefix + "1_");
			
			c = (Contact)person.getObjectByIndex("Contact", 2);
			top += 40;
			addLabel(html, "Telefon Mobil", 0,280);
			addTextField(html, c.getProperty("Value") , "PLZ*/Ort*", 1,  left , top,300,30, "inputmedium", prefix + "2_");
			
			//c = (Contact)person.getObjectByIndex("Contact", 2);
			top += 40;
			addLabel(html, "Fax", 0,  top);
			//addTextField(html, getProperty("Fax"), "PLZ*/Ort*", 1,  left , top,300,30, "inputmedium");
			
			top += 40;
			addLabel(html, "Jahrgang", 0,  top);
			addTextField(html, identity.getProperty("DateOfBirth"), "PLZ*/Ort*", 1,  left , top,300,30, "inputsmall1", prefix);
			
			top += 40;
			addLabel(html, "Anrede", 0,  top);
			addTextField(html, identity.getProperty("Sex"), "PLZ*/Ort*", 2,  left , top,300,30, "inputsmall1", prefix);
			
			top += 40;
			addLabel(html, "Datenschutz", 0,  top);
			addTextField(html, getProperty(prefix + "DataProtection"), "PLZ*/Ort*", 2,  left , top,300,30, "inputsmall1", "");
			
			top += 40;
			addLabel(html, "Sprachen", 0,  top);
			
			html.append("<div style=\"position : absolute; left : " + left + "px; top : " + top + "px;\">" + HTMLForm.getListField(person.getProperty("Languages") , true, prefix) + "</div>");
			
			top += 120;
			addLabel(html, "Bemerkungen", 0, top);
			
			html.append("</div>");
			
	}
	
	
	
	public String getRegistrationForm(){
		StringBuilder html = new StringBuilder();
		html.append("<form action=\"servlet\" id=\"processNodeForm\">");
		html.append("<table>");
		addTableRow(html, getProperty("Username"), "Benutzername");
		addTableRow(html, getProperty("Password"), "Passwort");	
		html.append("<tr><td><input type=\"button\" onclick=\"getNextNode(\'login=true\')\" value=\"login\"></td></tr>");
		
		addTableRow(html, getProperty("FamilyName"), "Nachname");
		addTableRow(html, getProperty("FirstName"), "Vorname");	
		addTableRow(html, getProperty("Street"), "Strasse");
		addTableRow(html, getProperty("Number"), "Nummer");
		addTableRow(html, getProperty("Zipcode"), "PLZ");
		addTableRow(html, getProperty("City"), "Ort");	
		addTableRow(html, getProperty("TelP"), "Telefon privat");
		addTableRow(html, getProperty("TelB"), "Telefon geschäftlich");	
		addTableRow(html, getProperty("TelM"), "Telefon mobil");
		addTableRow(html, getProperty("Email"), "Email");	
		

		html.append("<tr><td colspan=\"2\"><input type=\"button\" onclick=\"cancel()\" value=\"Abbrechen\"><input type=\"button\" onclick=\"getNextNode(\'insertorganisationmember=true\')\" value=\"Registrieren\"></td></tr>");
		
		html.append("</form>");
		html.append("</table>");
		
		return html.toString();
	}
	
	@Override
	public void setProperty(String fieldname, Object value) {
		if(ocs != null){
			ocs.logAccess(fieldname + ":" + value);
		}
		Person person = null;
		
		String[] args = fieldname.split("_", -1);
		
		if(args.length > 1){
			if(args[0].equals("person1")){
				person = person1;	
			}
			else if(args[0].equals("person2")){
				person = person2;	
			}
			else{
				person = (Person)getObjectByName("Person", args[0]);
			}
		}
		

		if(person != null){

			if(args.length==2){
				Identity identity = (Identity)person.getObjectByIndex("Identity", 0);
				Address address = (Address)person.getObjectByIndex("Address", 0);
				fieldname = args[1];
				if(identity.hasProperty(fieldname)){
					identity.setProperty(fieldname, value);
				}
				else if(address.hasProperty(fieldname)){
					address.setProperty(fieldname, value);
				}
				else if(person.hasProperty(fieldname)){
					person.setProperty(fieldname, value);
				}
			}
			else if(args.length==3){
				int index = (new Integer(args[1])).intValue();
				Contact c = (Contact)person.getObjectByIndex("Contact", index);
				if(c != null){
					c.setProperty("Value", value);
				}
			}
		}
		else{
			if(hasProperty(fieldname)){
				super.setProperty(fieldname, value);	
			}
		}
	}
	public static void addHelpButton(StringBuilder html, int helpitemid){
		html.append(" <img src=\"images/help.png\" onmouseover=\"showHelp(event, " + helpitemid + ")\" onmouseout=\"hideHelp()\" style=\"height : 12px;\">");	
	}
	public static String getHelpButton(int helpitemid){
		return " <img src=\"images/help.png\" onmouseover=\"showHelp(event, " + helpitemid + ")\" onmouseout=\"hideHelp()\" style=\"height : 12px;\">";	
	}
	public static void addTableRow(StringBuilder html, Property p, String label){
		addTableRow(html, p, label, 1);
	}
	public static void addTableRow(StringBuilder html, Property p, String label, int type){
		addTableRow(html, p, label, type, "");		
	}
	public static void addTableRow(StringBuilder html, Property p, String label, int type, String prefix){
		addTableRow(html, p, label, type, prefix, null, null, 0);				
	}	
	public static void addTableRow(StringBuilder html, Property p, String label, int type, String prefix, Property p2, int[] length){
		addTableRow(html, p, label, type, prefix, p2, length, 0);
	}
	public static void addTableRow(StringBuilder html, Property p, String label, int type, String prefix, Property p2, int[] length, int helpitemid){
		addTableRow(html, p, label, type, prefix, p2, length, helpitemid, null);
	}
	public static void addTableRow(StringBuilder html, Property p, String label, int type, String prefix, Property p2, int[] length, int helpitemid, Hashtable errors){
		
		String style = "width : " + 380 + "px;\"";
		
		if(errors != null && errors.get(prefix + p.getName()) != null){
			html.append("<tr style=\"border : 1px solid red;\">");
		}
		else{
			html.append("<tr>");
		}
		
		if(helpitemid > 0){
			html.append("<td class=\"inputlabel\">" + label );
			addHelpButton(html, helpitemid);
			html.append("</td>");
		}
		else{
			html.append("<td class=\"inputlabel\">" + label + "</td>");
		}
		html.append("<td>");
		
		if(errors != null && errors.get(prefix + p.getName()) != null){
			html.append("<span id=\"errors_\"" + p.getName() + "\" style=\" color : red;\">" + errors.get(prefix + p.getName()) + "</span><br>");
		}
		else{
			html.append("<div id=\"errors_" + prefix + p.getName() + "\" style=\" color : red; height : 0px;\"></div>");
		}
		
		if(type==0){
			html.append(HTMLForm.getTextField(p , false, prefix, null, style) + "</td></tr>");
		}
		else if(type==1){
			if(p2 != null){
				style = "width : " + length[0] + "px;\"";
				html.append(HTMLForm.getTextField(p , true, prefix, null, style));
				style = "width : " + length[1] + "px; margin-left : 10px;\"";
				html.append(HTMLForm.getTextField(p2 , true, prefix, null, style) + "</td></tr>");
			}
			else{
				html.append(HTMLForm.getTextField(p , true, prefix, null, style) + "</td></tr>");
			}
		}
		else if(type==2){
			html.append(HTMLForm.getSelection(p , true, prefix) + "</td></tr>");		
		}
		else if(type==3){
			html.append(HTMLForm.getRadioButton(p, true, prefix) + "</td></tr>");
		}
		else if(type==4){
			html.append(HTMLForm.getTextArea(p, true, null, prefix) + "</td></tr>");
		}
		else if(type==5){
			html.append(HTMLForm.getPassWordField(p , true) + "</td></tr>");
		}
		else if(type==6){
			html.append(HTMLForm.getMultipleItemWidget(p , true, prefix) + "</td></tr>");
		}
		else if(type==7){
			html.append(HTMLForm.getPassWordField(p , true, true) + "</td></tr>");
		}
		else if(type==8){
			html.append(HTMLForm.getRadioButtonGroup(p , true, prefix) + "</td></tr>");
		}
	}
	public static void addTextField(StringBuilder html, Property p, String label, int type, int x, int y, int width, int height, String classid){
		addTextField(html, p, label, type, x, y, width, height, classid, "");
	}
	public static void addTextField(StringBuilder html, Property p, String label, int type, int x, int y, int width, int height, String classid, String prefix){
		html.append("<div style=\" position : absolute; left : " + x + "px; top : " + y + "px;\">");
		String style = "width : " + width + "px; height : " + height + "px;";
		
		if(type==0){
			html.append(HTMLForm.getTextField(p , false, prefix, classid, style));
		}
		else if(type==1){
			html.append(HTMLForm.getTextField(p , true, prefix, classid, style));
		}
		else if(type==2){
			html.append(HTMLForm.getSelection(p , true, prefix));		
		}
		else if(type==3){
			html.append(HTMLForm.getRadioButton(p, true, prefix));
		}
		
		html.append("</div>");
	}
	public static void addLabel(StringBuilder html, String label, int x, int y){
		html.append("<div class=\"inputlabel\" style=\" position : absolute; left : " + x + "px; top : " + y + "px;\">" + label + "</div>");
	}
	public static void addCheckBox(StringBuilder html, Property p, String label, int x, int y, int width, int height, String arguments){
		//html.append("<div style=\" position : absolute; left : " + x + "px; top : " + y + "px;\">");
		html.append("<input type=\"checkbox\" onchange=\"getNextNode(\'" + arguments + "\')\"");
		if(p.getValue().equals("true")){
			html.append(" checked");
		}
		html.append(" style=\"vertical-align: middle;\">" + label);
		//html.append("</div>");
	}
	class LoginNode extends BasicProcessNode{
		public boolean validate(ApplicationContext context){
			
			OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
			OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
			
			ocs.logAccess("validating 0 ...");
			
			if(context.hasProperty("recovery")){
				if(node2 != null){
					getParent().getObjects("BasicProcessNode").remove(node2);
					node2 = null;
				}	
				if(node3 != null){
					getParent().getObjects("BasicProcessNode").remove(node3);
					node3 = null;
				}
				return false;
			}	
			else if(getParent().getBoolean("recovery")){
				if(node2 != null){
					getParent().getObjects("BasicProcessNode").remove(node2);
					node2 = null;
				}	
				if(node3 != null){
					getParent().getObjects("BasicProcessNode").remove(node3);
					node3 = null;
				}
				String username = getParent().getString("Username");
				
				username = username.trim();
				if(username.indexOf("@")==-1){
					int diff = 7 - username.length();
					for(int i = 0; i < diff; i++){
						username = "0" + username;	
					}
				}
				
				ocs.logAccess("Username: " + username);
				ObjectCollection results = new ObjectCollection("Results", "*");
				String sql = "";
				if(username.indexOf("@")==-1){
					sql = "SELECT t1.OrganisationMemberID AS ID, t2.Status FROM Login AS t1 JOIN OrganisationMember AS t2 ON t1.OrganisationMemberID=t2.ID WHERE t1.Username=\'" + username + "\'";
					sql += " AND t2.Status=1";
				}
				else{
					sql = "SELECT t1.OrganisationMemberID AS ID, t2.Status FROM Login AS t1 JOIN OrganisationMember AS t2 ON t1.OrganisationMemberID=t2.ID";
					sql += " JOIN Person AS t3 ON t2.Person=t3.ID";
					sql += " JOIN Contact AS t4 ON t4.PersonID=t3.ID AND t4.Type=3";
					sql += " WHERE t4.Value=\'" + username.trim() + "\'";
					sql += " AND t2.Status=1";
				}
				ocs.logAccess(sql);
				ocs.queryData(sql, results);
				
				if(results.getObjects().size() > 1){					
					getProcess().setComment("Es gibt mehrere Profile, die mit dieser Email registriert worden sind. Melden Sie sich bei der Administration von Nachbarnet, um ein neues Passwort zugetstellt zu erhalten.");
					return false;
				}
				if(results.getObjects().size() == 0){
					getProcess().setComment("Kein aktives Profil mit diesem Benutzernamen oder dieser Email gefunden");
					return false;
				}
				else{
				
					return true;	
				
				}
			}
			else if(!getParent().getBoolean("registration") && context.hasProperty("registration")==false){
				return handleLogin(ocs, context);
			}
			else if(context.hasProperty("Type") && (getParent().getString("Type").equals(context.getString("Type"))==false)){
				
				ocs.logAccess("validating 00 ...");
				
				if(getParent().getString("Type").equals(context.getString("Type"))){
					return true;
				}
				else{
					getParent().setProperty("Type", context.getString("Type"));
					
					if(getParent().getID("Type")==2){
						if(getParent().getObjects("Person").size()==0){
								
							Person person = oma.createPerson();
							person.setParent(getParent());
							getParent().addSubobject("Person", person);
							
							Vector sex = new Vector();
							sex.add(new ConfigValue("1", "1", "Herr"));
							sex.add(new ConfigValue("2", "2", "Frau"));
							person.getIdentity().getProperty("Sex").setSelection(sex);
							
							person.initObject();
							
						}
					}
					
					return false;
				}
			}
			else if(context.hasProperty("personadd") && context.getString("personadd").equals("true")){
				
				Person person = oma.createPerson();
				person.setParent(getParent());
				getParent().addSubobject("Person", person);
				
				Vector sex = new Vector();
				sex.add(new ConfigValue("1", "1", "Herr"));
				sex.add(new ConfigValue("2", "2", "Frau"));
				person.getIdentity().getProperty("Sex").setSelection(sex);
							
				person.initObject();
				
				return false;
				
			}
			else if(context.hasProperty("persondelete") && context.getString("persondelete").equals("true")){
				
				String objectPath = context.getString("objectPath");
				if(objectPath != null){
					if(getParent().getObjects("Person").size() > 1){
						Person person = (Person)ocs.getObjectByPath(objectPath, userSession);
						if(person != null){
							
							getParent().deleteElement("Person", person);
						}
					}
				}
				
				return false;
				
			}
			else{

				if(context.hasProperty("registration")){
					if(node2==null){
						node2 = new OrganisationMemberAddNode();
						addNode(node2);
						node2.setName("AGBNode");

					}
					if(node3==null){
						node3 = addNode();
						node3.setName("FeedbackNode");
						node3.setTitle("Besten Dank für die Registrierung!");
					}
					//setCurrentNode(node2);
					getProcess().setTitle("Registrieren");
					return false;	
				}
				else if(context.hasProperty("delegatecommunication")){
					//addNode(node2);
					//setCurrentNode(node2);
					if(context.getString("delegatecommunication").equals("false")){
						getParent().setProperty("Type", 1);	
					}
					getProcess().setTitle("Registrieren");
					return false;	
				}
				else if(context.hasProperty("isorganisation")){
					
					getProcess().setTitle("Registrieren");
					return false;	
					
				}
				else{
					if(getParent().getBoolean("registration")){
						if(issubprofile){
							return createOrgansisationMember(context);
						}
						else{
							String feedback = "";
							errors.clear();
							if(getParent().getBoolean("isorganisation")){
								if(getParent().getString("Organisation").length()==0){
									feedback += "Geben Sie den Namen der Organisaton ein";
								}
							}
	
							//if(c.getString("Value").length() < 11 || c2.getString("Value").length() < 11){
							
							//if(getParent().getBoolean("delegatecommunication") && person2 != null){
							
							ocs.logAccess("validating 1 ...");
							
							if(getParent().getBoolean("delegatecommunication") && person2 != null && 1==2){ //AK: 20170918, zweite Person wird nicht mehr erfasst
								
								Contact c = (Contact)person2.getObjectByIndex("Contact", 3);	
								if(c.getString("Value").length()==0){
									feedback += "<br>Geben Sie eine gültige Email-Adresse an.";
									errors.put("person2_3_Value", "Geben Sie eine gültige Email-Adresse an.");
								}
								c = (Contact)person2.getObjectByIndex("Contact", 0);
								
								Contact c2 = (Contact)person2.getObjectByIndex("Contact", 2);
								Contact c3 = (Contact)person2.getObjectByIndex("Contact", 1);
								if(c.getString("Value").length() < 11 && c2.getString("Value").length() < 11 && c3.getString("Value").length() < 11){
									feedback += "<br>Geben Sie eine gültige Telefonnummer an.";
									errors.put("person2_0_Value", "Geben Sie Ihre Festnetz-, Geschäfts- oder Mobilnummer an");
									errors.put("person2_1_Value", "Geben Sie Ihre Festnetz-, Geschäfts- oder Mobilnummer an");
									errors.put("person2_2_Value", "Geben Sie Ihre Festnetz-, Geschäfts- oder Mobilnummer an");
								}
								
							}
							else{
								
								Contact c = (Contact)person1.getObjectByIndex("Contact", 3);	
								if(c.getString("Value").length()==0){
									feedback += "<br>Geben Sie eine gültige Email-Adresse an.";
									errors.put("person1_3_Value", "Geben Sie eine gültige Email-Adresse an.");
								}
								c = (Contact)person1.getObjectByIndex("Contact", 0);
								Contact c2 = (Contact)person1.getObjectByIndex("Contact", 2);
								if(c.getString("Value").length() < 11 && c2.getString("Value").length() < 11){
									feedback += "<br>Geben Sie eine gültige Telefonnummer an.";
									errors.put("person1_0_Value", "Geben Sie Ihre Festnetz- oder Mobilnummer an");
									errors.put("person1_2_Value", "Geben Sie Ihre Festnetz- oder Mobilnummer an");
								}
							}
							
							ocs.logAccess("validating 2 ...");
							
							if(person1.getIdentity().getID("Sex") < 1){
								feedback += "<br>Geben Sie die Anrede an.";	
								errors.put("person1_Sex", "Geben Sie die Anrede an.");
							}
							if(person1.getIdentity().getString("DateOfBirth").length() != 4){
								feedback += "<br>Geben Sie Ihr Geburtsjahr vierstellig an.";	
								errors.put("person1_DateOfBirth", "Geben Sie Ihr Geburtsjahr an.");
							}
							if(person1.getIdentity().getString("FamilyName").length() < 3){
								feedback += "<br>Geben Sie Ihren Nachnamen an.";	
								errors.put("person1_FamilyName", "Geben Sie Ihren Nachnamen an.");
							}
							if(person1.getIdentity().getString("FirstName").length() < 3){
								feedback += "<br>Geben Sie Ihren Vornamen an.";		
								errors.put("person1_FirstName", "Geben Sie Ihren Vornamen an.");
							}
							if(person1.getAddress().getString("Street").length() < 3){
								feedback += "<br>Geben Sie die Strasse Ihrer Adresse an.";	
								errors.put("person1_Street", "Geben Sie die Strasse Ihrer Adresse an.");
								String errortext = "Geben Sie die Strasse Ihrer Adresse an.";
								if(person1.getAddress().getString("Number").length() < 1){
									feedback += "<br>Geben Sie die Hausnummer Ihrer Adresse an.";	
									errors.put("person1_Number", "Geben Sie die Hausnummer Ihrer Adresse an.");
									errortext = "Geben Sie die Strasse und Nummer Ihrer Adresse an.";
								}
								errors.put("person1_Street", errortext);
							}
							else if(person1.getAddress().getString("Number").length() < 1){
									feedback += "<br>Geben Sie die Hausnummer Ihrer Adresse an.";	
									errors.put("person1_Street", "Geben Sie die Hausnummer Ihrer Adresse an.");
									
							}
							if(person1.getAddress().getString("Zipcode").length() < 3){
								feedback += "<br>Geben Sie die PLZ Ihrer Adresse an.";	
								errors.put("person1_Zipcode", "Geben Sie die PLZ Ihrer Adresse an.");
							}
							if(person1.getAddress().getString("City").length() < 3){
								feedback += "<br>Geben Sie den Ort Ihrer Adresse an.";	
								errors.put("person1_Zipcode", "Geben Sie den Ort Ihrer Adresse an.");
							}
							if(person1.getIdentity().getString("DateOfBirth").length() != 4){
								feedback += "<br>Geben Sie Ihr Genurtsjahr an.";	
								errors.put("person1_DateOfBirth", "Geben Sie Ihr Geburtsjahr an.");
							}
							//----------------------------------------------------------------------------------
							List<ConfigValue> geoobjects = ocs.getGeoObjects();
							boolean ok  = true;
							for(ConfigValue cv : geoobjects){
								if(person1.getAddress().getString("Zipcode").equals(cv.getValue())){
									if(!person1.getAddress().getString("City").equals(cv.getLabel())){
										feedback += "<br>PLZ und Ort stimmen micht überein.";	
										errors.put("person1_Zipcode", "PLZ und Ort stimmen micht überein.");										
										ok = false;
									}
									
								}
								
							}
							boolean found = false;
							if(ok){
								ok = false;
								for(ConfigValue cv : geoobjects){
									if(person1.getAddress().getString("City").equals(cv.getLabel())){
										found = true;
										if(person1.getAddress().getString("Zipcode").equals(cv.getValue())){									
											ok = true;

										}
										
									}
									
								}																
							
								if(!ok && found){
									feedback += "<br>PLZ und Ort stimmen micht überein.";	
									errors.put("person1_Zipcode", "Ort und PLZ stimmen micht überein.");								
									
								}
								else{
									//errors.put("person1_Zipcode", ok + " " + found);	
								}
							}
							//-------------------------------------------------------------------------------
							
							
							getParent().setProperty("validationfeedback", feedback);
							if(feedback.length() > 0){
								return false;
							}
							else{
								/*
								String familyname = person1.getIdentity().getString("FamilyName");
								String firstname = request.getParameter("firstname");
								String dateofbirth = request.getParameter("dateofbirth");
								
								String sql = "SELECT * FROM Identity WHERE FamilyName ILIKE '" + familyname + "%' AND FirstName ILIKE '" + firstname + "%' AND DateOfBirth ILIKE '" + dateofbirth + "'";
								ObjectCollection results = new ObjectCollection("Results", "*");
								ocs.queryData(sql, results);
	
								if(results.getObjects().size() > 0){
									return false;
								}
								else{
								*/
									return true;
								/*
								}
								*/
							}
						}
					}
					else{
						return true;	
					}
				}
			}
			
		}
		
	}
	
	class OrganisationMemberAddNode extends BasicProcessNode{
		public OrganisationMemberAddNode(){
			setName("AGBNode");
		}
		public boolean validate(ApplicationContext context){
			
			return createOrgansisationMember(context);
			
		}
	}
	
	public boolean createOrgansisationMember(ApplicationContext context){
			
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		ocs.logAccess("Consent: " + context.hasProperty("consentconfirmed"));
			
		ocs.logAccess("Dataprotection: " + getParent().getString("person1_DataProtection"));
			
			
		String email = null;
			
		if(!issubprofile && !context.hasProperty("consentconfirmed")){
			return false;
		}
		else{
			
		String parentmemberid = null;
		String organisationid = null;
				
		String parentTitle = "";
				
				/* //AK 20170918
				
				if(getParent().getBoolean("isorganisation")){
					OrganisationalUnit ou = new OrganisationalUnit();
					ou.setProperty("Title", getParent().getString("Organisation"));
					organisationid = ocs.insertSimpleObject(ou);
				}
				
				else if(getParent().getBoolean("delegatecommunication") && person2 != null){
					
						String id = ocs.insertSimpleObject(person2);	
						
						Identity identity = person2.getIdentity();
						identity.addProperty("PersonID", "String", id);
						
						
						
						ocs.insertSimpleObject(identity);	
						
						
								
						Address address = person2.getAddress();
						address.addProperty("PersonID", "String", id);
						ocs.insertSimpleObject(address);
						
						for(BasicClass c : person2.getObjects("Contact")){
							if(c.getString("Value").length() > 0){
								c.addProperty("PersonID", "String", id);
								if(c.getInt("Type")==3){
									email = c.getString("Value");	
								}
								ocs.insertSimpleObject(c);	
							}
						}
						
						om = new OrganisationMember();
						om.addProperty("OrganisationalUnitID", "String", "1");
						
						String now = DateConverter.dateToSQL(new java.util.Date(), true);
						om.setProperty("DateCreated", now);
						om.setProperty("DateModified", now);
						
						om.setProperty("Person", id);
						om.setProperty("Status", "0");
						
						om.setProperty("NotificationStatus", "1");
						
						om.setProperty("DataProtection", getParent().getString("person2_DataProtection"));
						id = ocs.insertSimpleObject(om);	
						om = (OrganisationMember)ocs.getObject(null, "OrganisationMember", "ID", id, false);
						
						parentmemberid = id;
								
						MemberRole mr = new MemberRole();
						mr.setProperty("Role", "1");
						mr.addProperty("OrganisationMemberID", "String", id);
						ocs.insertSimpleObject(mr);
					
				}
				*/
				
				String id = ocs.insertSimpleObject(person1);	
						
				Identity identity = person1.getIdentity();
				identity.addProperty("PersonID", "String", id);
				ocs.insertSimpleObject(identity);	
				
				parentTitle = identity.getString("FirstName") + " " + identity.getString("FamilyName");
						
				Address address = person1.getAddress();
				address.addProperty("PersonID", "String", id);
				ocs.insertSimpleObject(address);
				
				for(BasicClass c : person1.getObjects("Contact")){
					if(c.getString("Value").length() > 0){
						c.addProperty("PersonID", "String", id);
						if(email == null && c.getInt("Type")==3){
							email = c.getString("Value");	
						}
						ocs.insertSimpleObject(c);	
					}
				}
				
				om = new OrganisationMember();
				om.addProperty("OrganisationalUnitID", "String", "1");
				if(parentmemberid != null){
					om.addProperty("OrganisationMemberID", "String", parentmemberid);
				}
				if(organisationid != null){
					om.addProperty("OrganisationalUnitID", "String", organisationid);
				}
				
				String now = DateConverter.dateToSQL(new java.util.Date(), true);
				om.setProperty("DateCreated", now);
				om.setProperty("DateModified", now);
				
				om.setProperty("Person", id);
				om.setProperty("Status", "0");
				//om.setProperty("DataProtection", getParent().getString("person1_DataProtection"));
				om.setProperty("DataProtection", getString("person1_DataProtection"));
				
				//om.setProperty("Type", getParent().getID("Type")); //AK 20170918
				om.setProperty("Type", getID("Type")); //AK 20170918
				
				//om.setProperty("Comment", getParent().getString("Comment")); //AK 20170918
				om.setProperty("Comment", getString("Comment")); //AK 20170918
				
				om.setProperty("NotificationStatus", "1");
				
				id = ocs.insertSimpleObject(om);	
				om = (OrganisationMember)ocs.getObject(null, "OrganisationMember", "ID", id, false);
						
				MemberRole mr = new MemberRole();
				mr.setProperty("Role", "1");
				mr.addProperty("OrganisationMemberID", "String", id);
				ocs.insertSimpleObject(mr);
				
				HttpServletRequest request = ((WebApplicationContext)context).getRequest();
				
				//if(getParent().getString("parentid").length() == 0){
				
				
				for(BasicClass o : getObjects("Person")){
					
					Person person = (Person)o;
					String personid = ocs.insertObject(person, true);
					
					om = new OrganisationMember();
					
					om.setProperty("DateCreated", now);
					om.setProperty("DateModified", now);
					
					
					
					om.setProperty("Person", personid);
					om.setProperty("Status", "0");
					om.setProperty("Type", "0"); // dependent Profile
					
					String omid = ocs.insertSimpleObject(om);	
					
					OrganisationMemberRelationship omr = new OrganisationMemberRelationship();
					omr.setProperty("OrganisationMember", omid);
					omr.setProperty("Title", parentTitle);
					
					if(getID("Type")==2){
					
						omr.setProperty("Role", 3);
				
					}
					omr.addProperty("OrganisationMemberID", "String", id);
					ocs.insertSimpleObject(omr);
					
					
				}
				
				if(getString("parentid").length() == 0){
				
					Login login = new Login();
					String registrationcode = ocs.createPassword(20);
					login.setProperty("RegistrationCode", registrationcode);
					
					String username = id;
					while(username.length() < 7){
						username = "0" + username;
					}
					
					login.setProperty("Username", username);
					String password = ocs.createPassword(8);
					login.setProperty("Password", password);
					
					if(parentmemberid != null){
						login.addProperty("OrganisationMemberID", "String", parentmemberid);
					}
					else{
						login.addProperty("OrganisationMemberID", "String", id);
					}
					
					ocs.insertSimpleObject(login);
				
				
				
					if(parentmemberid != null){
						OrganisationMemberRelationship omr = new OrganisationMemberRelationship();
						omr.setProperty("OrganisationMember", id);
						omr.setProperty("Title", parentTitle);
						omr.setProperty("Role", getParent().getID("Role2"));
						omr.addProperty("OrganisationMemberID", "String", parentmemberid);
						ocs.insertSimpleObject(omr);
					}
					
					String message = "";
					
					TextBlock textblock = ocs.getTextblock("2");
					if(textblock != null){
						
						String sex = identity.getString("Sex");
						String familyname = identity.getString("FamilyName");
						String firstname = identity.getString("FirstName");
						
						String addressation = "Sehr geehrter Herr " + familyname;
						if(sex.equals("2")){
							addressation = "Sehr geehrte Frau " + familyname;
						}
						
						message = textblock.getString("Content");
						message = message.replaceAll("<p>","\n");
						message = message.replaceAll("</p>","");
						message = message.replaceAll("<br>","\n");
						message = message.replaceAll("<br />","\n");
						message = message.replaceAll("&#252;","ü");
						message = message.replaceAll("&#246;","ö");
						message = message.replaceAll("&#228;","ä");
						
						String[] lines = message.split("\r\n|\r|\n");
						StringBuilder text = new StringBuilder();
						for(String line : lines){
							text.append(line.trim() + "\n");	
						}
						
						message = text.toString();
	
						message = message.replace("&#60;@addressation&#62;", addressation);
						
						String credentials = "\n\nIhr Benutzername : " + username + "\n\nIhr Passwort : " + password;
						message = message.replace("&#60;@credentials&#62;", credentials);
						
						String link = "\n\n" + ocs.getBaseURL("", request) + "/servlet.srv?action=confirmregistration&registrationcode=" + registrationcode;	
						String link2 = "\n\n" + ocs.getBaseURL("", request) + "/servlet.srv?action=confirmregistration&login=true&registrationcode=" + registrationcode;
						message = message.replace("&#60;@link&#62;", link);
						message = message.replace("&#60;@link2&#62;", link2);
					}
					else{
						message = "Vielen Dank für die Registrierung bei NachbarNET";
						
						message += "\n\nIhr Benutzername : " + username;
						message += "\n\nIhr Passwort : " + password;
						
						message += "\n\nSie müssen die Registrierung abschliessen, indem Sie auf den untenstehenden Link klicken:";
						
						message += "\n\n" + ocs.getBaseURL(request) + "/opencommunity/servlet.srv?action=confirmregistration&registrationcode=" + registrationcode;					
						
					}
					
	
		
					ocs.sendEmail(message, "Registrierung bei Nachbarnet" , email, null);
					
				}
				else{
					

					OrganisationMemberRelationship omr = new OrganisationMemberRelationship();
					omr.setProperty("OrganisationMember", id);
					omr.setProperty("Title", parentTitle);
					omr.setProperty("Role", getParent().getID("Role2"));
					omr.addProperty("OrganisationMemberID", "String", getParent().getString("parentid"));
					id = ocs.insertSimpleObject(omr);
					ocs.getObject(parent, "OrganisationMemberRelationship", "ID", id);
					
					
				}
				

				setTitle("Besten Dank für die Registrierung!");
				
				return true;
			
			}
		}
	public String getPath(String path){
		return "/currentprocess" + path;
	}

	
	
}
