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
 
import org.kubiki.application.*;
import org.kubiki.util.DateConverter;

import org.kubiki.gui.html.HTMLFormManager;
 
import java.util.Vector;
import java.util.Date;
import java.util.Calendar;
import java.sql.*;
 
 
 public class MemberAdEdit extends BasicProcess{
 
	BasicProcessNode node1, node2;
	OrganisationMember om;
	MemberAdCategory mac = null;
	
	MemberAd ma;
	
	String omid = "";
	
	String title = "";
	String description = "";
	String location = "";
	int type = 0;
	
	String cbimageselected = "images/cbimageselected.png";
	String cbimage = "images/cbimage.png";
 
	public MemberAdEdit(){
		
		setTitle("Inserat bearbeiten");
		node1 = new BasicProcessNode(){
			public boolean validate(ApplicationContext context){
				
				if(context.hasProperty("settype")){
					return false;					
				}
				else{
					return true;
				}
			}
		};
		addNode(node1);
		node1.setName("MemberAdEditNode");
		
		addProperty("MemberAdID", "String", "");
		addProperty("Title", "String", "");
		addProperty("Type", "Integer", "");
		addProperty("Description", "String", "");	
		addProperty("Location", "String", "");	
		addProperty("ValidUntil", "String", "");	
		addProperty("Mode", "String", "edit");
		addProperty("Time", "String", "365");
		addProperty("Satisfaction", "Integer", "");
		setCurrentNode(node1);
	}
	public void initProcess(){
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		ma = (MemberAd)ocs.getObject(null, "MemberAd", "ID", getString("MemberAdID"), false);
		
		omid = ma.getString("OrganisationMemberID");
		
		ma.setParent(this);
		ma.initObjectLocal();
		setProperty("Type", ma.getID("Type"));
		setProperty("Title", ma.getString("Title"));
		setProperty("Location", ma.getString("Location"));
		setProperty("Description", ma.getString("Description"));
		title = ma.getString("Title");
		description = ma.getString("Description");
		location = ma.getString("Location");
		type = ma.getID("Type");
		
		mac = (MemberAdCategory)ma.getObject("Template");
		
		Vector types = new Vector();
		types.add(new ConfigValue("0","0", "Angebot"));
		types.add(new ConfigValue("1","1", "Nachfrage"));
		if(mac.getBoolean("TandemAllowed")){
			types.add(new ConfigValue("2","2", "Tandem"));
		}
		getProperty("Type").setSelection(types);
		if(mac.getBoolean("RequestOnly")){
			getProperty("Type").setHidden(true);
		}
		
		Property p = null;
		
		if(mac != null){
			for(BasicClass bc  : mac.getObjects("FieldDefinition")){
				FieldDefinition fd = (FieldDefinition)bc;
				if(fd.getID("Type")==1){
					Parameter parameter = ma.getParameterByTemplate(Integer.parseInt(fd.getName()));
					if(parameter != null){
						p = addProperty(fd.getName(), "Integer", "" + (int)parameter.getDouble("Value").doubleValue(), false, fd.getString("Title"));
						node1.addProperty(p);
						p.setSelection(fd.getCodeList());
					}
					else{
						p = addProperty(fd.getName(), "Integer", "" , false, fd.getString("Title"));
						p.setSelection(fd.getCodeList());
						node1.addProperty(p);
					}
				}
				else{
					p = node1.addProperty(fd.getName() + "_label", "String", "" , false, fd.getString("Title"));
					p.setEditable(false);
					for(ConfigValue cv : fd.getCodeList()){
						String label = cv.getLabel();
						String[] args = label.split("/");
						//ocs.logAccess(label + ":" + ma.hasParameter(Integer.parseInt("" + cv.getValue())));
						p = addProperty(fd.getName() + "_" + cv.getValue(), "Boolean", "" + ma.hasParameter(Integer.parseInt("" + cv.getValue())), false, args[0]);
						node1.addProperty(p);
					}
				}
			}	
		}
		if(getString("Mode").equals("prolong")){
			setTitle("Inserat verlängern");
			String validuntil = ma.getString("ValidUntil");
			String[] args = validuntil.split("-");
			if(args.length==3){
				int year = Integer.parseInt(args[0]);
				year++;
				String newdate = year + "-" + args[1] + "-" + args[2];
				//ma.setProperty("ValidUntil", newdate);
			}
			node1.addProperty(getProperty("Time"));
		}
		Vector timespans = new Vector();
		timespans.add(new ConfigValue("30", "30", "Ein Monat"));
		timespans.add(new ConfigValue("91", "91", "Drei Monate"));
		timespans.add(new ConfigValue("182", "182", "Sechs Monate"));
		timespans.add(new ConfigValue("365", "365", "Ein Jahr"));
		getProperty("Time").setSelection(timespans);
		
		Vector satisfaction = new Vector();
		satisfaction.add(new ConfigValue("3", "3", "ja"));
		satisfaction.add(new ConfigValue("2", "2", "es geht so"));
		satisfaction.add(new ConfigValue("1", "1", "nein"));
		getProperty("Satisfaction").setSelection(satisfaction, false);
	}
	public String getMemberAdEditForm(ApplicationContext context){
		StringBuilder html = new StringBuilder();
		int top = 40;
		MemberRegistration mr = null;
		

		
		html.append("<form id=\"processNodeForm\">");
		html.append("<table style=\"width : 540px;\"");
		
		//mr.addLabel(html, "Rubrik", 0,  top);
		//mr.addLabel(html, ma.getObject("Template").toString(), 160,  top);
		
		html.append("<tr><td class=\"inputlabel\">Rubrik</td><td>" +  ma.getObject("Template").toString() + "</td></tr>");
		
		MemberAdCategory mac = (MemberAdCategory)ma.getObject("Template");
		Vector parameters = ma.getObjects("Parameter");
		
		/*
		for(int i = 0; i < parameters.size(); i++){
			Parameter parameter = (Parameter)parameters.elementAt(i);
			FieldDefinition fd = (FieldDefinition)mac.getFieldDefinitionByTemplate("" + parameter.getID("Template"));
			top += 40;
			if(fd != null){
				//mr.addLabel(html, fd.toString(), 0,  top);	
				html.append("<tr><td class=\"inputlabel\">" + fd.toString() + "</td>");
				Double value = parameter.getDouble("Value");
				if(value != null){
					String key = value.intValue() + "";
					//mr.addLabel(html, "" + fd.getCodeMap().get(key), 160,  top);
					String label = fd.getCodeMap().get(key);
					if(label != null){
						String[] args = label.split("/");
						//html.append("<td>" + args[0] + "</td>");
					}
					else{
						html.append("<td></td>");
					}
				}
				html.append("</tr>");
			}
		}
		*/
		
		addTableRow(html, getProperty("Type"), "Angebot/Nachfrage", 2, "getNextNode(\'settype=true&type=\' + this.value)");

		mr.addTableRow(html, getProperty("Title"), "Titel", 1);	

		mr.addTableRow(html, getProperty("Description"), "Beschreibung", 4);	
		
		for(BasicClass bc  : mac.getObjects("FieldDefinition")){
			FieldDefinition fd = (FieldDefinition)bc;
			
			if((getID("Type") < 2  && fd.getBoolean("TandemOnly")==false ) || getID("Type")==2 ){

				if(fd.getID("Type")==1){
					addTableRow(html, getProperty(fd.getName()), fd.getString("Title"), 2);
				}
				else{
	
					html.append("<tr><td>" + bc + "</td>");
					html.append("<td>");
					for(ConfigValue cv : fd.getCodeList()){
		
						if(cv.getLabel().length() > 0){
							String id = fd.getName() + "_" + cv.getValue();
							String cvlabel = cv.getLabel();
							String[] args = cvlabel.split("/");
							if(args.length==2){
								cvlabel = args[0] + " (" + args[1] + ")";	
							}
							if(getBoolean(id)){
								html.append(HTMLForm.getCustomCheckbox( id, "true" , true, cbimageselected, cbimage, cvlabel) + "<br>");
							}
							else{
								html.append(HTMLForm.getCustomCheckbox( id, "false" , false, cbimageselected, cbimage, cvlabel) + "<br>");
							}
						}
					}
					html.append("</td></tr>");
				}
			
			}
			
		}
		
		mr.addTableRow(html, getProperty("Location"), "Alternative PLZ", 1);	
		
		if(getString("Mode").equals("edit")){
			html.append("<tr><td class=\"inputlabel\">Gültig bis</td><td>" +  DateConverter.sqlToShortDisplay(ma.getString("ValidUntil"), false) + "</td></tr>");
		}
		else{
			//html.append("<tr><td class=\"inputlabel\">Verlängert bis</td><td>" +  DateConverter.sqlToShortDisplay(ma.getString("ValidUntil"), false) + "</td></tr>");
			html.append("<tr><td>Verlängern um</td><td>");
				html.append(HTMLForm.getSelection(getProperty("Time"), true, ""));
			html.append("</td></tr>");
			html.append("<tr><td>Sind Sie mit der Wirkung von Ihrem Inserat zufrieden?</td><td>");
				html.append(HTMLFormManager.getRadioButton(getProperty("Satisfaction"), true, ""));
			html.append("</td></tr>");
		}
		//mr.addTextField(html, getProperty("ValidUntil"), "PLZ*/Ort*", 1,  250 , top, 100,30, "inputsmall1", "");
		
		html.append("</table>");
		html.append("<input class=\"nodebutton\" type=\"button\"  value=\"Speichern\" onclick=\"getNextNode()\">");
		html.append("</form>");
		return html.toString();
	}
	public void finish(ProcessResult result, ApplicationContext context){
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();	
		ma.setProperty("Title", getString("Title"));
		ma.setProperty("Description", getString("Description"));
		ma.setProperty("Location", getString("Location"));
		
		if(getString("Mode").equals("prolong")){
			Date validUntil = ma.getDate("ValidUntil");
			Calendar cal = Calendar.getInstance();
			cal.setTime(validUntil);
			cal.add(Calendar.DATE, getInt("Time")); //minus number would decrement the days
			validUntil = cal.getTime();
			ma.setProperty("ValidUntil", DateConverter.dateToSQL(validUntil, false));
			
			int satisfaction = getID("Satisfaction");
			
			if(satisfaction > 0){
				
				
				
				
				String mailcontent = "";
				
				String code = ocs.createLoginCode(omid, context);
				
				String link = "\n\n" + ocs.getString("hostname") + "/servlet.srv?action=onetimelogin&redirect=profile&sectionid=inserate&code=" + code;
				
				if(satisfaction==1 || satisfaction==2){ // nein, es geht
					mailcontent = ocs.getTextblockContent( "47", true);
				}
				else if(satisfaction==3){
					mailcontent = ocs.getTextblockContent("49", true);		
				}
				
				mailcontent = mailcontent.replace("<@link>", link);
				mailcontent = mailcontent.replace("<br />", "");
				
				ocs.createAndSendEmail(omid, "Geben Sie Ihr Feedback", mailcontent, context);
				
			}
			
		}
		
		boolean updated = false;
		
		if(title.equals(getString("Title"))==false || description.equals(getString("Description"))==false || location.equals(getString("Location"))==false || type!=getID("Type")){ //Inserat muss kontrolliert werden
			ma.setProperty("Status", 4);	
			updated = true;
		}
		/*
		if(!getString("ValidUntil").equals(ma.getString("ValidUntil")) && getString("ValidUntil").length() > 0){
				
				MemberAdModification mam = new MemberAdModification();
				mam.setProperty("ValidFrom", ma.getString("ValidFrom"));
				mam.setProperty("ValidUntil", ma.getString("ValidFrom"));
				mam.addProperty("MemberAdID", "String", ma.getName());
				ocs.insertSimpleObject(mam);	
				ma.setProperty("ValidUntil", getString("ValidUntil"));
		}
		*/
		ocs.updateObject(ma);
		/*
		if(getString("Mode").equals("prolong")){
				String sql = "UPDATE MemberAd SET ValidUntil=(ValidUntil +  INTERVAL \'" + getInt("Time") + " days\') WHERE ID=" + ma.getName();
				ocs.executeCommand(sql);
		}
		*/
		try{
				Connection con = ocs.getConnection();
				Statement stmt = con.createStatement();		
				stmt.execute("DELETE FROM Parameter WHERE MemberAdID=" + ma.getName());
				
				ma.getObjects("Parameter").clear();
																																
	
				
				for(BasicClass bc  : mac.getObjects("FieldDefinition")){
					FieldDefinition fd = (FieldDefinition)bc;
					if(fd.getID("Type")==1){
						Parameter parameter = (Parameter)ma.createObject("ch.opencommunity.base.Parameter", null, context);
						parameter.addProperty("MemberAdID", "String", ma.getName());
						parameter.setProperty("Template", fd.getName());
						parameter.setProperty("Title", fd.getString("Title"));
						parameter.setProperty("Value", getString(fd.getName()));	
						String pid = ocs.insertObject(parameter);
						if(getParent() instanceof BasicProcess){
							ocs.getObject(ma, "Parameter", "ID", pid);
						}
					}
					else{
						
						for(ConfigValue cv : fd.getCodeList()){
							String name = bc.getName() + "_" + cv.getValue();
							ocs.logAccess(name);
							ocs.logAccess(getBoolean(name));
							if(getBoolean(name)){
								Parameter parameter = (Parameter)ma.createObject("ch.opencommunity.base.Parameter", null, context);
								parameter.addProperty("MemberAdID", "String", ma.getName());
								parameter.setProperty("Template", fd.getName());
								parameter.setProperty("Title", fd.getString("Title"));
								parameter.setProperty("Value", cv.getValue());
								String pid = ocs.insertObject(parameter);
								if(getParent() instanceof BasicProcess){
									ocs.getObject(ma, "Parameter", "ID", pid);
								}
							}
						}			
						
					
					}
				}
				stmt.close();
				con.close();
		}
		catch(java.lang.Exception e){
			ocs.logException(e);	
		}
		if(updated){
			
			result.setParam("newprocess", "ch.opencommunity.process.FeedbackShow");
			result.setParam("newprocessparams", "TextBlockID=20");	
			
		}
		result.setParam("refresh", "currentsection");
		
		
	}
	public void addTableRow(StringBuilder html, Property p, String label){
		addTableRow(html, p, label, 1);
	}
	public void addTableRow(StringBuilder html, Property p, String label, int type){
		addTableRow(html, p, label, type, "");
	}
	public void addTableRow(StringBuilder html, Property p, String label, int type, String onchange){
		html.append("<tr><td class=\"inputlabel\">" + label + "</td>");
		if(type==1){
			html.append("<td>" + HTMLForm.getTextField(p , true, "") + "</td>");
		}
		else if(type==2){
			html.append("<td>" + HTMLForm.getSelection(p , p.getName(), "", true, true, false, "",onchange) + "</td>");	
			//Property p, String id, String className, boolean isEditable, boolean selectCurrent, boolean onlyActive, String prefix, String onchange
		}
		else if(type==3){
			html.append("<td>" + HTMLForm.getRadioButton(p, true, "")  + "</td>");
		}
		else if(type==4){
			html.append("<td>" + HTMLForm.getListField(p , true, "") + "</td>");
		}
		else if(type==5){
			html.append("<td>");
			for(Object o : p.getValues()){
				ConfigValue cv = (ConfigValue)o;
				if(cv.getLabel().length() > 0){
					String id = p.getName() + "_" + cv.getValue();
					addProperty(id, "String", "false");
					String cvlabel = cv.getLabel();
					String[] args = cvlabel.split("/");
					if(args.length==2){
						cvlabel = args[0] + " (" + args[1] + ")";	
					}
					html.append(HTMLForm.getCustomCheckbox( id, "false" , false, cbimageselected, cbimage, cvlabel) + "<br>");
					//String id, String value, boolean selected, String imageselected, String image, String label
				}
			}
			html.append("</td>");
		}
		html.append("</tr>");
		
	}
 }