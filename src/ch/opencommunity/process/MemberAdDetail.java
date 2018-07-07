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
 
import java.util.Vector;
import java.util.Hashtable;
import java.util.Map;

import java.sql.*;
 
 
 public class MemberAdDetail extends BasicProcess{
 
	MemberAdEditNode node1, node2;
	OrganisationMember om;
	MemberAdCategory mac = null;
	
	MemberAd ma;
	
	Hashtable yes_no, quality, roles = null;
 
	public MemberAdDetail(){
		
		
		//node1.setName("MemberAdDetail");
		
		addProperty("MAID", "String", "");
		
		
		

	}
	public void initProcess(){
		
		Vector status = new Vector();
		status.add(new ConfigValue("0","0","erfasst"));
		status.add(new ConfigValue("1","1","freigeschaltet"));
		status.add(new ConfigValue("2","2","pausiert"));
		status.add(new ConfigValue("3","3","inaktiv"));
		status.add(new ConfigValue("4","4","zu kontrollieren"));
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		
		ocs.logAccess("Parentobject : " + getParent().getClass().getName());
		
		if(getParent() instanceof OrganisationMemberEdit){
			OrganisationMember om = ((OrganisationMemberEdit)getParent()).getOrganisationMember();
			ma = (MemberAd)om.getObjectByName("MemberAd", getString("MAID"));
		}
		if(ma==null){			
			ma = (MemberAd)ocs.getObject(null, "MemberAd", "ID", getString("MAID"), false);
			ma.setParent(this);
			ma.initObjectLocal();
		}

		
		node1 = new MemberAdEditNode(ma);
		

		
		addNode(node1);
		
		setCurrentNode(node1);
		
		Property p = null;
		
			p = ma.getProperty("Template");
			p.setSelection(ocs.getMemberAdAdministration().getObjects("MemberAdCategory"));
			//p.setEditable(false);
			p.setAction("getNextNode('Template=' + this.value)");
			node1.addProperty(p);
			
			p = ma.getProperty("Title");
			node1.addProperty(p);
			
			p = ma.getProperty("Type");
			node1.addProperty(p);
			
			if(ma.getInt("Priority") > 0){
				p = addProperty("AdOfTheWeek", "Boolean", "true", false, "Inserat der Woche");
				node1.addProperty(p);
			}
			else{
				p = addProperty("AdOfTheWeek", "Boolean", "false", false, "Inserat der Woche");
				node1.addProperty(p);
			}
			
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
							
							String name = bc.getName() + "_" + cv.getValue();
							
							ocs.logAccess(name + ":" + ma.hasParameter(fd.getID(), Integer.parseInt("" + cv.getValue())));
							
							p = addProperty(name , "Boolean", "" + ma.hasParameter(fd.getID(),Integer.parseInt("" + cv.getValue())), false, args[0]);
							node1.addProperty(p);
						}
					}
				}			
			}
			
	
			
			p = ma.getProperty("Status");
			//p.setValue("1");
			p.setSelection(status);
			node1.addProperty(p);
			
			if(ma.getObjects("Note").size()==0){
				node1.addButtonDefinition("Notizfeld hinzufügen", "getNextNode('addnote=true')");	
			}
			else{
				Note note = (Note)ma.getObjectByIndex("Note", 0);
				if(note != null){
					node1.addProperty("AdditionalInfo", "Text", note.getString("Content"), false, "Zusatzinfo");	
				}
			}
		
		/*
		addProperty(ma.getProperty("Title"));
		addProperty(ma.getProperty("Description"));
		addProperty(ma.getProperty("ValidUntil"));
		*/

	}
	public String getMemberAdDetailForm(ApplicationContext context){
		return getMemberAdDetailForm(ma, false, context);
	}
	public static String getMemberAdDetailForm(MemberAd ma, boolean embedded, ApplicationContext context){
		
		ma.initObjectLocal();
		
		OpenCommunityServer ocs = (OpenCommunityServer)ma.getRoot();
		OpenCommunityUserSession usersession = (OpenCommunityUserSession)context.getObject("usersession");
		StringBuilder html = new StringBuilder();
		html.append("<form action=\"servlet\" id=\"processNodeForm\">");
		html.append("<table>");
		//html.append("<tr><td class=\"inputlabel\">Rubrik</td><td>" +  ma.getObject("Template").toString() + "</td></tr>");
		html.append("<tr><td class=\"inputlabel\">Rubrik</td><td>" +  ocs.getFormFactory().getSelection(ma.getProperty("Template"), "Template", null, true, true, true, "", "getNextNode('Template=' + this.value)") + "</td></tr>");
		
		//Property p, String id, String className, boolean isEditable, boolean selectCurrent, boolean onlyActive, String prefix, String onchange
		
		MemberAdCategory mac = (MemberAdCategory)ma.getObject("Template");
		Vector parameters = ma.getObjects("Parameter");
		for(int i = 0; i < parameters.size(); i++){
			Parameter parameter = (Parameter)parameters.elementAt(i);
			FieldDefinition fd = (FieldDefinition)mac.getFieldDefinitionByTemplate("" + parameter.getID("Template"));

			if(fd != null){
				//mr.addLabel(html, fd.toString(), 0,  top);	
				html.append("<tr><td class=\"inputlabel\">" + fd.toString() + "</td>");
				Double value = parameter.getDouble("Value");
				if(value != null){
					
					if(fd.getID("Type")==10){
						
					}
					else{
						String key = value.intValue() + "";
						//mr.addLabel(html, "" + fd.getCodeMap().get(key), 160,  top);
						Map codemap = fd.getCodeMap();
						if(codemap != null){
							String label = fd.getCodeMap().get(key);
							if(label != null){
								String[] args = label.split("/");
								html.append("<td>" + args[0] + "</td>");
							}
							else{
								html.append("<td>" + value + "</td>");
							}
						}
						else{
							html.append("<td>" + value + "</td>");
						}
					}
				}
				html.append("</tr>");
			}
		}

		html.append("<tr><td>Titel</td><td><input name=\"Title\" value=\"" + ma.getString("Title") + "\"></td></tr>");
		html.append("<tr><td>Beschreibung</td><td><textarea name=\"Description\">" + ma.getString("Description") + "</textarea></td></tr>");	
			
		
		//html.append("<tr><td class=\"inputlabel\">Gültig bis</td><td>" +  DateConverter.sqlToShortDisplay(ma.getString("ValidUntil")) + "</td></tr>");
		html.append("<tr><td class=\"inputlabel\">Gültig bis</td><td>" +  ocs.getFormFactory().getDateField(ma.getProperty("ValidUntil"), true, "") + "</td></tr>");
		
		html.append("</table>");
		
		html.append("</form>");
		
		/*
		html.append("<h4>Adressbestellungen</h4>");
		
		html.append(ocs.getMemberAdRequestList(context, usersession, " WHERE t1.ID=" + ma.getName(), "requests"));
		
		html.append("<h4>Feedbacks</h4>");
		
		String sql = "SELECT t1.*, t5.FamilyName, t5.FirstName FROM Feedback AS t1";
		sql += " JOIN MemberAd AS t2 ON t1.MemberAdID=t2.ID";
		sql += " JOIN OrganisationMember AS t3 ON t1.OrganisationMember=t3.ID";
		sql += " JOIN Person AS t4 ON t3.Person=t4.ID";	
		sql += " JOIN Identity AS t5 ON t5.PersonID=t4.ID";
		sql += " WHERE t2.ID=" + ma.getName();
		
		*/
		
		html.append("<table>");
		//getFeedback(ocs, html, sql);
		html.append("</table>");
		
		
		if(!embedded){
			html.append("<br><input type=\"button\" value=\"OK\" onclick=\"getNextNode('overview=true')\">");
		}
		return html.toString();	
	}
	public static void getFeedback(OpenCommunityServer ocs, StringBuilder html, String sql){
		
		Hashtable yes_no = new Hashtable();
		yes_no.put("t", "JA");
		yes_no.put("f", "NEIN");
		
		Hashtable quality = new Hashtable();
		quality.put("0", "");
		quality.put("1", "gar nicht gut");
		quality.put("2", "es geht so");
		quality.put("3", "gut");
		quality.put("4", "super");	
		
		boolean even = false;
		
		ObjectCollection results = new ObjectCollection("Results", "*");
		
		//html.append("<tr><td colspan=\"5\"><b>Erhaltene Feedback zu bestellten Adressen</b></td></tr>");
				
		html.append("<tr>");
		html.append("<th>Datum</th>");
		html.append("<th>Einsatz zust. gekommen</th>");
		html.append("<th>Nicht erreicht</th>");
		html.append("<th>Unzuverlässig</th>");
		html.append("<th>Keine Zeit</th>");
		html.append("<th>Passt persönlich nicht</th>");
		html.append("<th>Anderes</th>");
		html.append("<th>Wie war der Einsatz</th>");
		html.append("<th>Besonders gut</th>");
		html.append("<th>Nicht gut</th>");
		html.append("<th>Kommentar</th>");
		html.append("<th>Betroffener Benutzer</th>");
		html.append("</tr>");
				

				
		ocs.queryData(sql, results);
				
		for(BasicClass bc : results.getObjects()){
			if(even){
				html.append("<tr class=\"even\">");
			}
			else{
				html.append("<tr class=\"odd\">");
			}
			even = !even;
					
					html.append("<td>" + DateConverter.sqlToShortDisplay(bc.getString("DATECREATED"), false) + "</td>");
					html.append("<td>" + bc.getString("CONTACTESTABLISHED") + "</td>");
					html.append("<td>" + yes_no.get(bc.getString("REASON_1")) + "</td>");
					html.append("<td>" + yes_no.get(bc.getString("REASON_2")) + "</td>");
					html.append("<td>" + yes_no.get(bc.getString("REASON_3")) + "</td>");
					html.append("<td>" + yes_no.get(bc.getString("REASON_4")) + "</td>");
					html.append("<td>" + yes_no.get(bc.getString("REASON_5")) + "</td>");
					html.append("<td>" + quality.get(bc.getString("QUALITY")) + "</td>");
					html.append("<td>" + bc.getString("HIGHLIGHTS") + "</td>");
					html.append("<td>" + bc.getString("PROBLEMS") + "</td>");
					html.append("<td>" + bc.getString("COMMENTS") + "</td>");
					html.append("<td>" + bc.getString("FAMILYNAME") + " " + bc.getString("FIRSTNAME") + "</td>");
					html.append("<tr>");	
		}
		
		
	}
	public void finish(ProcessResult result, ApplicationContext context){
		//ma.setProperty("Status", 1);
		
		MemberAdCategory mac = (MemberAdCategory)ma.getObject("Template");
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		
		if(mac != null){
			
			try{
				if(ma.getID("Type")==0){
					ma.setProperty("IsOffer", "true");	
					ma.setProperty("IsRequest", "false");
				}
				else if(ma.getID("Type")==1){
					ma.setProperty("IsOffer", "false");	
					ma.setProperty("IsRequest", "true");	
				}
				else if(ma.getID("Type")==2){
					ma.setProperty("IsOffer", "true");	
					ma.setProperty("IsRequest", "true");	
				}
				ocs.updateObject(ma);
				
				Connection con = ocs.getConnection();
				Statement stmt = con.createStatement();		
				stmt.execute("DELETE FROM Parameter WHERE MemberAdID=" + ma.getName());
				
				ma.getObjectCollection("Parameter").removeObjects();
																																
	
				
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
							if(node1.getBoolean(name)){
								Parameter parameter = (Parameter)ma.createObject("ch.opencommunity.base.Parameter", null, context);
								parameter.addProperty("MemberAdID", "String", ma.getName());
								parameter.setProperty("Template", fd.getName());
								parameter.setProperty("Title", fd.getString("Title"));
								parameter.setProperty("Value", cv.getValue());
								String pid = ocs.insertObject(parameter);
								if(getParent() instanceof BasicProcess){
									ocs.getObject(ma, "Parameter", "ID", pid);
								}
								setProperty(name, "true");
							}
							else{
								setProperty(name, "false");	
							}
						}			
						
					
					}
				}
				if(getBoolean("AdOfTheWeek")){
					stmt = con.createStatement();	
					stmt.execute("UPDATE MemberAd SET Priority=0");
					stmt.execute("UPDATE MemberAd SET Priority=1 WHERE ID=" + ma.getName());			
				}
				if(ma.getObjects("Note").size()==1 && node1.hasProperty("AdditionalInfo")){
					
					Note note = (Note)ma.getObjectByIndex("Note", 0);
					note.setProperty("Content", node1.getString("AdditionalInfo"));
					ocs.updateObject(note);
				}
			}
			catch(java.lang.Exception e){
				ocs.logException(e);	
			}

			
			if(getParent() instanceof BasicProcess){
				((BasicProcess)getParent()).setSubprocess(null);
			}
			else{			
				result.setParam("refresh", "currentsection");
			}
			
		}
	}
		
 }