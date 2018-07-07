 package ch.opencommunity.process;
 
 import ch.opencommunity.server.*;
 import ch.opencommunity.base.*;
 import ch.opencommunity.advertising.*;
 import ch.opencommunity.common.*;
 import ch.opencommunity.view.UserProfileView;
 
 import org.kubiki.ide.*;
 import org.kubiki.base.Property;
 import org.kubiki.base.ProcessResult;
 import org.kubiki.base.ObjectCollection;
 import org.kubiki.base.BasicClass;
 import org.kubiki.base.ConfigValue;
 import org.kubiki.database.Record;
 
import org.kubiki.application.*;
import org.kubiki.application.server.WebApplicationContext;
import org.kubiki.util.DateConverter;
 
import java.util.Vector;  
import java.util.Date;
import java.util.Calendar;
import java.util.HashMap;
 
import javax.servlet.http.*;
 
 
 public class MemberAdCreate extends BasicProcess{
 
	BasicProcessNode node1, node2;
	OrganisationMember om;
	MemberAdCategory mac = null;
	
	String cbimageselected = null;
	String cbimage = null;
	
	MemberAdAdministration maa;
	
	String newmemberadid;
 
	public MemberAdCreate(){
	
		//setTitle("<img src=\"/res/icons&inserieren_aktiv.png\"> Inserat erstellen");
		
		setTitle("Inserat erstellen");
		
		addProperty("Mode", "Integer", "1");
	
		node1 = new MemberAdCreateNode();
		addNode(node1);
		node1.setName("MemberAdCreateNode");
		
		Property p = addProperty("Title", "String", "", false, "Titel");
		node1.addProperty(p);
				
		p = addProperty("Type", "Integer", "-1", true, "Angebot/Nachfrage");
		node1.addProperty(p);
		
		p = addProperty("Description", "Text", "", false, "Beschreibung");
		node1.addProperty(p);
		
		p = addProperty("Category", "String", "");
		//node1.addProperty(p);
		
		p = addProperty("Location", "String", "", true, "Durchführungsort");
		//node1.addProperty(p);
		
		
		p = addProperty("CreateActivity", "Boolean", "true", true, "Bestätigung erstellen");
		node1.addProperty(p);
		
		p = addProperty("Status", "Integer", "0", true, "Status");
		node1.addProperty(p);
		//node1.addProperty(p);
		
		//node2 = addNode();
		//node2.setName("FeedbackNode2");
	
		setCurrentNode(node1);
		
		addProperty("errors", "Object", "");
	}
	public void initProcess(ApplicationContext context){
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		
		maa = ocs.getMemberAdAdministration();
		getProperty("Category").setSelection(maa.getObjects("MemberAdCategory"));
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
		
		
		if(userSession != null){
			om = userSession.getOrganisationMember();	
			
			if(om != null && om.getActiveRelationship() != null){
				om = om.getActiveRelationship();	 
			}
		}
		
		
		
		/*
		if(om != null){
			addNode(node2);
			setCurrentNode(node2);			
		}
		else{
			addNode(node1);
			addNode(node2);
			setCurrentNode(node1);		
		}
		*/
		if(getID("Mode")==1){
			node2 = addNode();
			node2.setName("FeedbackNode2");			
		}
		

		Vector status = new Vector();
		status.add(new ConfigValue("0", "0", "erfasst"));
		status.add(new ConfigValue("1", "1", "freigeschaltet"));
		getProperty("Status").setSelection(status);	
		
		if(getParent() instanceof BasicProcess){
			//getProperty("CreateActivity").setHidden(false);
			getProperty("Status").setHidden(false);
			setProperty("Status", 1);
		}
		else if(getParent() instanceof OrganisationMemberController){
			getProperty("CreateActivity").setHidden(false);
			getProperty("Status").setHidden(false);
			setProperty("Status", 1);
			om = ((OrganisationMemberController)getParent()).getOrganisationMember();
		}
		
		//getProperty("Location").setSelection(ocs.getGeoObjects());
		

	}
	public void setOrganisationMember(OrganisationMember om){
		this.om = om;	
	}
	public void finish(ProcessResult result, ApplicationContext context){
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		
		if(getParent() instanceof BasicProcess){
			((BasicProcess)getParent()).setSubprocess(null);
			if(getBoolean("CreateActivity")){
				result.setParam("exec", "getNextNode('createnotification=true~newmemberadid=" + newmemberadid + "')");	
			}
		}
		else if(getParent() instanceof OrganisationMemberController){
			OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
			OrganisationMemberController omc = (OrganisationMemberController)getParent();
			result.setParam("openprofile", getParent().getName());
			if(getBoolean("CreateActivity")){
				omc.createNotification(context, newmemberadid);
			}
			result.setData(UserProfileView.getUserProfileView(userSession, (OrganisationMemberController)getParent(), context));
		}
		else{			
			result.setParam("refresh", "profile");
		}
	}
	public String getFeedbackForm(ApplicationContext context){
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		String feedback = ocs.getTextblockContent("24", false);
		
		StringBuilder html = new StringBuilder();
		//html.append("Besten Dank für das Inserat, Sie erhalten eine Bestätigung, sobald es <br>freigeschaltet wurde");
		html.append(feedback);
		html.append("<p><input type=\"button\" class=\"nodebutton\" value=\"OK\" onclick=\"getNextNode('createmember=true')\"><br><br><br><br><br>");
		return html.toString();
	}	

	
	public String getMemberAdCreateForm(ApplicationContext context){
		
		MemberRegistration mr = null;
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		WebApplicationContext webcontext= (WebApplicationContext)context;
		HttpServletRequest request = webcontext.getRequest();
		
		HashMap errors = null;
		
		if(getProperty("errors") != null && getObject("errors") instanceof HashMap){
			errors = (HashMap)getObject("errors");
		}
		
		cbimageselected = "images/cbimageselected.png";
		cbimage = "images/cbimage.png";
		
		HTMLForm form = ocs.getFormFactory();
		StringBuilder html = new StringBuilder();
		html.append("<form action=\"servlet\" id=\"processNodeForm\">");
		html.append("<table style=\"width : 540px;\"");
		if(node1.getComment().length() > 0){
			html.append("<tr><td colspan=\"2\" class=\"comment\">");
			html.append(node1.getComment());
			html.append("</td></tr>");
		}
		
		//MemberAdAdministration maa = (MemberAdAdministration)getParent();
		getProperty("Category").setSelection(maa.getObjects("MemberAdCategory"));
		html.append("<tr><td class=\"inputlabel\">Rubrik</td><td><select class=\"select_yellow\" onchange=\"getNextNode(\'setcategory=true&categoryid=\' + this.value)\">");
		html.append("<option value=\"\"></option>");
		for(BasicClass bc : maa.getObjects("MemberAdCategory")){
			if(mac != null && mac.equals(bc)){
				html.append("<option value=\"" + bc.getName() + "\" SELECTED>" + bc + "</option>");
			}
			else{
				html.append("<option value=\"" + bc.getName() + "\">" + bc + "</option>");
			}
		}
		html.append("</select></td></tr>");
		
		if(mac != null){
			if(!mac.getBoolean("RequestOnly")){
				addTableRow(html, getProperty("Type"), "Art des Inserates", 2, "getNextNode(\'settype=true&type=\' + this.value)", 0, errors);
			}
			
			int type = getID("Type");
			
			if(type > -1 || mac.getBoolean("RequestOnly")){
				
				if(mac.getName().equals("60")){
				
					FieldDefinition fd1 = (FieldDefinition)mac.getObjectByIndex("FieldDefinition", 0);
					FieldDefinition fd2 = (FieldDefinition)mac.getObjectByIndex("FieldDefinition", 1);
					
					if(type==0 || type==2){
					
						html.append("<tr><td>Angebot : Ich unterrichte/ biete:</td><td>");
						html.append("<select id=\"field_7\"  name=\"field_7\">");
						for(Object o : fd1.getCodeList()){	
							ConfigValue cv = (ConfigValue)o;
							html.append("<option value=\"" + cv.getValue() + "\">" + cv.getLabel() + "</option>");
							
						}					
						
						html.append("</select>");					
						html.append("</td></tr>");

					
					}
					
					if(type==1 || type==2){
					
						html.append("<tr><td>Nachfrage: Ich möchte lernen/ brauche Hilfe:</td><td>");

						html.append("<select id=\"field_11\" name=\"field_11\">");
						for(Object o : fd2.getCodeList()){	
							ConfigValue cv = (ConfigValue)o;
							html.append("<option value=\"" + cv.getValue() + "\">" + cv.getLabel() + "</option>");
							
						}
					
					
						html.append("</select>");					
						html.append("</td></tr>");
						
					}
					
				}
				else{
			
					for(BasicClass bc : mac.getObjects("FieldDefinition")){
						
						ch.opencommunity.base.FieldDefinition fd = (ch.opencommunity.base.FieldDefinition)bc;
						
						//if((getID("Type") < 2  && fd.getBoolean("TandemOnly")==false ) || getID("Type")==2 ){
						
						
							Property p = getProperty("field_" + fd.getName());
							
							if(p == null){
								
								ocs.logAccess("adding property " + fd.getName());
							
								p = addProperty("field_" + fd.getName(), "String", "");
								//node1.addProperty(p);
								
								if(fd.getCodeList().size() > 0){
									p.setSelection(fd.getCodeList());
								}
								
							}
							
							if(!p.isHidden()){
							
								String title = fd.getString("Title");
								String alttitle = fd.getString("AltTitle");
								if(alttitle.length() > 0){
									title = alttitle;	
								}
								
								if(fd.getID("Type")==1){
		
									addTableRow(html, p, title, 2, null, 0, errors);
								}
								else if(fd.getID("Type")==2){
		
									addTableRow(html, p, title, 4, null, 0,errors);
								}
								else if(fd.getID("Type")==3){
		
									addTableRow(html, p, title, 5,null, 0, errors);
									
								}
							}
						//}
	
						
					}
					
				}
				
				if(getParent() instanceof BasicProcess && !(getParent() instanceof MemberAdRequestActivityAdd)){ //Admin-Modus
					//addTableRow(html, getProperty("CreateActivity"), "Aktivität erstellen", 3, null, 0,errors);
					addTableRow(html, getProperty("Status"), "Initialer Status", 2, null, 0,errors);
				}
				form.getFormBody(html, node1, true, context, "", 1, errors);
				addTableRow(html, getProperty("Location"), "Alternative PLZ", 1, "checkZipCode(this.id, this.value)", 9, errors);
			}

	
		}
		html.append("</form>");
		html.append("</table>");
		html.append("<input class=\"rightButton\" type=\"button\" onclick=\"getNextNode()\" value=\"Inserat absenden\">");
		
		return html.toString();		
		
	}
	public static void addHelpButton(StringBuilder html, int helpitemid){
		html.append(" <img src=\"images/help.png\" onmouseover=\"showHelp(event, " + helpitemid + ")\" onmouseout=\"hideHelp()\" style=\"height : 12px;\">");	
	}
	public void addTableRow(StringBuilder html, Property p, String label){
		addTableRow(html, p, label, 1);
	}
	public void addTableRow(StringBuilder html, Property p, String label, int type){
		addTableRow(html, p, label, type, "");
	}
	public void addTableRow(StringBuilder html, Property p, String label, int type, String onchange){
		addTableRow(html, p, label, type, onchange, 0);		
	}
	public void addTableRow(StringBuilder html, Property p, String label, int type, String onchange, int helpitemid){
		addTableRow(html, p, label, type, onchange, helpitemid, null);			
	}
	public void addTableRow(StringBuilder html, Property p, String label, int type, String onchange, int helpitemid, HashMap errors){
		
		String error = "";
				
		if(errors != null){
			if(errors.get(p) != null){
				error = "<span class=\"errorfield\">" + errors.get(p) + "</span><br>";
			}
		}

		
		html.append("<tr>");
		if(helpitemid > 0){
			html.append("<td class=\"inputlabel\">" + label );
			addHelpButton(html, helpitemid);
			html.append("</td>");
		}
		else{
			html.append("<td class=\"inputlabel\">" + label + "</td>");
		}
		if(type==1){
			html.append("<td>" + error + HTMLForm.getTextField(p , true, "", null, null, onchange) + "</td>");
			//Property p, boolean isEditable, String prefix, String classid, String style, String onchange
		}
		else if(type==2){
			html.append("<td>" + error + HTMLForm.getSelection(p , p.getName(), "select_yellow", true, true, false, "",onchange) + "</td>");	
			//Property p, String id, String className, boolean isEditable, boolean selectCurrent, boolean onlyActive, String prefix, String onchange
		}
		else if(type==3){
			html.append("<td>" + error + HTMLForm.getRadioButton(p, true, "")  + "</td>");
		}
		else if(type==4){
			html.append("<td>" + error + HTMLForm.getListField(p , true, "") + "</td>");
		}
		else if(type==5){
			html.append("<td>"  + error);
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
					html.append(HTMLForm.getCustomCheckbox( id, "false" , false, cbimageselected, cbimage, cvlabel, null, "label_yellow") + "<br>");
					//String id, String value, boolean selected, String imageselected, String image, String label
				}
			}
			html.append("</td>");
		}
		html.append("</tr>");
		
	}
	class MemberAdCreateNode extends BasicProcessNode{
		public boolean validate(ApplicationContext context){
			
			setComment("");
			OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
			if(context.hasProperty("setcategory")){
				String categoryid = context.getString("categoryid");
				mac = (MemberAdCategory)ocs.getMemberAdAdministration().getObjectByName("MemberAdCategory", categoryid);
				
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
				
				ocs.logAccess("MAC: " + mac);
				
				for(BasicClass bc : mac.getObjects("FieldDefinition")){						
					ch.opencommunity.base.FieldDefinition fd = (ch.opencommunity.base.FieldDefinition)bc;

						
						
					Property p = getProperty("field_" + fd.getName());							
					if(p == null){
								

							
						p = getParent().addProperty("field_" + fd.getName(), "String", "");

								
						if(fd.getCodeList().size() > 0){
							p.setSelection(fd.getCodeList());
						}
								
					}
					
				}
				return false;
			}
			else if(context.hasProperty("settype")){
				String type = context.getString("type");
				ocs.logAccess("neuer Typ: " + type);
				setProperty("Type", type);
				if(mac.getName().equals("6")){
					if(type.equals("0")){
						
						getParent().getProperty("field_7").setHidden(false);
						getParent().getProperty("field_11").setHidden(true);	
						
					}
					else if(type.equals("1")){
						
						getParent().getProperty("field_7").setHidden(true);
						getParent().getProperty("field_11").setHidden(false);	
						
					}
					else if(type.equals("2")){
						
						getParent().getProperty("field_7").setHidden(false);
						getParent().getProperty("field_11").setHidden(false);	
						
					}
					
				}
				return false;
			}
			else{
				if(mac == null){
					return false;
				}
				else{	

						
					HashMap errors = new HashMap();
						boolean hasErrors = false;

						if(getString("Title").length() < 5){
							//setComment("Geben Sie bitte einen Titel ein!");
							errors.put(getProperty("Title"), "Geben Sie bitte einen Titel ein!");
							hasErrors = true;
						}
						if(getString("Description").length() < 10){
							//setComment("Geben Sie bitte eine Beschreibung ein!");
							errors.put(getProperty("Description"), "Geben Sie bitte eine Beschreibung ein!");
							hasErrors = true;
						}
						boolean hasAllParameters = true;
						
						ocs.logAccess("Tandem allowed : " + mac.getBoolean("TandemAllowed"));
						
						if(mac.getBoolean("TandemAllowed") == false){ //keine Ueberprüfung bei Tandems
						
							for(BasicClass bc : mac.getObjects("FieldDefinition")){
								
								
								FieldDefinition fd = (FieldDefinition)bc;
								if(fd.getInt("Type")==1){
									if(getParent().getString("field_" + fd.getName()).length()==0){
										errors.put(getParent().getProperty("field_" + fd.getName()), "Geben zu allen Parametern mindestens einen Wert ein!");
										hasAllParameters = false;
									}
								}
								else if(fd.getInt("Type")==2){
									boolean hasValue = false;
									String[] values = getParent().getString("field_" + fd.getName()).split("#");
									for(String value : values){
										if(value.length() > 0){
											hasValue = true;
										}
									}							
									hasAllParameters = hasValue;	
									if(!hasValue){
										errors.put(getParent().getProperty("field_" + fd.getName()), "Geben zu allen Parametern mindestens einen Wert ein!");
									}
								}
								else if(fd.getInt("Type")==3){
									boolean hasValue = false;
									for(Object o : fd.getCodeList()){
										ConfigValue cv = (ConfigValue)o;
										String propertyid = "field_" + fd.getName() + "_" + cv.getValue();
										if(getParent().hasProperty(propertyid) && getParent().getBoolean(propertyid)){
											hasValue = true;
										}
									}
									if(!hasValue){
										errors.put(getParent().getProperty("field_" + fd.getName()), "Geben zu allen Parametern mindestens einen Wert ein!");
									}
									if(hasAllParameters){
										hasAllParameters = hasValue;	
									}
								}
								
							}
						
						}
						if(!hasAllParameters){
							setComment("Geben zu allen Parametern mindestens einen Wert ein!");
							hasErrors = true;						
						}
						
						if(hasErrors){
							
							getProcess().setProperty("errors", errors);
							return false;
							
						}
						else{
							getProcess().setProperty("errors", "");
							
							MemberAd ma = new MemberAd();
							
							ocs.logEvent("om : " + om);
						
							ma.mergeProperties(this);
							ma.addProperty("OrganisationMemberID", "String", om.getName());
							ma.setProperty("Status", "" + getID("Status"));
							ma.setProperty("Template", mac.getName());
							
							Date now = new Date();
							ma.setProperty("ValidFrom", DateConverter.dateToSQL(now, false)); 
							Calendar c = Calendar.getInstance();
							c.setTime(now);
							int year = c.get(Calendar.YEAR);
							c.set(Calendar.YEAR, year + 1);
							
							Date validuntil = c.getTime();
							
							ma.setProperty("ValidUntil", DateConverter.dateToSQL(validuntil, false)); 
							
							if(getParent() instanceof BasicProcess){
								
							}
							else{
								
							}
							
							if(getParent().getString("Location").length() > 0){
								ma.setProperty("Location", getParent().getString("Location"));
							}
							
							ma.setProperty("DateReminder", DateConverter.dateToSQL(new Date(validuntil.getTime() - (long)(30*24*60*60*1000)), false)); 
							
							if(getParent().getID("Type")==0){
								ma.setProperty("IsOffer", "true");
							}
							else if(getParent().getID("Type")==1){
								ma.setProperty("IsRequest", "true");
							}
							else if(getParent().getID("Type")==2){
								ma.setProperty("IsOffer", "true");
								ma.setProperty("IsRequest", "true");
							}		
							
							//String id = ocs.insertObject(ma);
							
							//newmemberadid = id;
							
							for(BasicClass bc : mac.getObjects("FieldDefinition")){
								FieldDefinition fd = (FieldDefinition)bc;
								if(fd.getInt("Type")==1){
									Parameter parameter = new Parameter();
									//parameter.addProperty("MemberAdID", "String", id);
									parameter.setProperty("Template", fd.getName());
									parameter.setProperty("Title", fd.getString("Title"));
									parameter.setProperty("Value", getParent().getString("field_" + fd.getName()));
									//ocs.insertObject(parameter);
									ma.addSubobject("Parameter", parameter);
								}
								else if(fd.getInt("Type")==2){
									ocs.logAccess("Values : " + getParent().getString("field_" + fd.getName()));
									String[] values = getParent().getString("field_" + fd.getName()).split("#");
									for(String value : values){
										if(value.length() > 0){
											Parameter parameter = new Parameter();
											//parameter.addProperty("MemberAdID", "String", id);
											parameter.setProperty("Template", fd.getName());
											parameter.setProperty("Title", fd.getString("Title"));
											parameter.setProperty("Value", value );
											//ocs.insertObject(parameter);	
											ma.addSubobject("Parameter", parameter);
										}
									}
								}
								else if(fd.getInt("Type")==3){
									for(Object o : fd.getCodeList()){
										ConfigValue cv = (ConfigValue)o;
										String propertyid = "field_" + fd.getName() + "_" + cv.getValue();
										if(getParent().hasProperty(propertyid) && getParent().getBoolean(propertyid)){
											Parameter parameter = new Parameter();
											//parameter.addProperty("MemberAdID", "String", id);
											parameter.setProperty("Template", fd.getName());
											parameter.setProperty("Title", fd.getString("Title"));
											parameter.setProperty("Value", cv.getValue() );
											//ocs.insertObject(parameter);	
											ma.addSubobject("Parameter", parameter);
										}
									}	
								}
							}
							
							if(getParent().getParent() instanceof MemberAdRequestActivityAdd){
								ocs.logEvent("adding memberad ...");
								getParent().getParent().addSubobject("MemberAd", ma);							
							}
							else{
								String id = ocs.insertObject(ma, true);
								
								newmemberadid = id;
								
								if(getParent() instanceof BasicProcess){
									ocs.getObject(om, "MemberAd", "ID", id);
								}
							}
						}
					return true;
					
				}
			}
		}	
	}
	public void setProperty(String name, Object value){
		super.setProperty(name, value);
	}	
}
