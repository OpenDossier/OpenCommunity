package ch.opencommunity.process;

import ch.opencommunity.base.OrganisationMember;
import ch.opencommunity.base.OrganisationMemberController;
import ch.opencommunity.base.Activity;
import ch.opencommunity.base.FieldDefinition;

import ch.opencommunity.advertising.*;
import ch.opencommunity.server.OpenCommunityServer;
import ch.opencommunity.common.OpenCommunityUserSession;
import ch.opencommunity.common.OpenCommunityUserProfile;


import org.kubiki.base.BasicClass;
import org.kubiki.base.ProcessResult;
import org.kubiki.base.ActionResult;
import org.kubiki.base.ObjectCollection;
import org.kubiki.base.ConfigValue;
import org.kubiki.ide.BasicProcess;
import org.kubiki.application.ApplicationContext;
import org.kubiki.application.server.ApplicationServer;
import org.kubiki.gui.html.HTMLFormManager;
import org.kubiki.util.DateConverter;

import java.util.List;
import java.util.Vector;
import java.util.Map;

public class MemberAdRequestActivityAdd extends BasicProcess{
	
	ApplicationServer server;
	OrganisationMemberController omc;
	OrganisationMember om;
	
	OpenCommunityServer ocs;
	
	MemberAdCategory mac = null;
	
	MemberAdAdministration maa;
	
	int offset = 0;

	public MemberAdRequestActivityAdd(){
		
		setName("MemberAdRequestActivityAdd");
		
		addObjectCollection("MemberAd", "ch.opencommunity.advertising.MemberAd");
		
		addNode(this);
		
		addProperty("initmode", "Integer", "");
		
		addProperty("MemberAdCategory", "Object", "1");
		
		
		addProperty("familyname", "String", "");
		addProperty("firstname", "String", "");
		addProperty("title", "String", "");
		addProperty("description", "String", "");
		addProperty("type", "Integer", "");
		
		
		setCurrentNode(this);
	}
	public void initProcess(ApplicationContext context){
		
		
		server = (ApplicationServer)getRoot();
		
		ocs = (OpenCommunityServer)server;
		
		maa = ocs.getMemberAdAdministration();
		
		omc = (OrganisationMemberController)getParent();
		om = omc.getOrganisationMember();
		
		server.logEvent(omc + ":" + om);
		
		if(getID("initmode")==1){
			
			subprocess = new MemberAdCreate();
			subprocess.setParent(this);
			subprocess.setProperty("Mode", 2);
			subprocess.initProcess(context);
			((MemberAdCreate)subprocess).setOrganisationMember(om);
			
		}
		
		getProperty("MemberAdCategory").setSelection(maa.getObjects("MemberAdCategory"));
		
		setCategoryProperties();
		
		for(ConfigValue cv : ocs.getGeoObjects()){
			
			addProperty("location_" + cv.getValue(), "Boolean", "false");
			
		}
		
		Vector types = new Vector();
		types.add(new ConfigValue("1", "1", "Angebot"));
		types.add(new ConfigValue("2", "2", "Nachfrage"));
		types.add(new ConfigValue("3", "3", "Nur Tandem"));
		getProperty("type").setSelection(types);
		

		
		
	}
	public boolean validate(ApplicationContext context){
		
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
		
		server.logEvent("hdqwd : " + context.hasProperty("finishrequestcreate"));
		
		if(context.hasProperty("finish")){
			return true;
		}
		else if(context.hasProperty("memberadcreate")){
			
			subprocess = new MemberAdCreate();
			subprocess.setParent(this);
			subprocess.setProperty("Mode", 2);
			subprocess.initProcess(context);
			((MemberAdCreate)subprocess).setOrganisationMember(om);			
			
		}
		else if(context.hasProperty("memberadrequestcreate")){
			setProperty("initmode", 2);			
		}
		else if(context.hasProperty("finishsearch")){
			setProperty("initmode", 0);			
		}
		else if(context.hasProperty("cancelrequestcreate")){
			userSession.getMemberAdIDs().clear();
			setProperty("initmode", 0);
		}
		else if(context.hasProperty("finishrequestcreate")){
			setProperty("initmode", 0);
		}
		return false;
	}
	public void finish(ProcessResult result, ApplicationContext context){
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
		Activity activity = omc.requestsCreate(context, getObjects("MemberAd"), userSession);
		omc.setActiveObject(activity);
		result.setParam("exec", "editOrganisationMember('" + om.getName() + "')");
	}
	public String getMemberAdRequestAcivityAddForm(ApplicationContext context){
		
		StringBuilder html = new StringBuilder();
		
		OpenCommunityServer ocs = (OpenCommunityServer)server;
		
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
		
		html.append("<div style=\"position : absolute; left : 0px; top : 0px; right : 0px; bottom : 0px; background : white;\">");
		
		if(getID("initmode")==2){
			
			html.append(getMemberAdSearchForm(userSession, context));
			
			/*
			
			MemberAdAdministration maa = ocs.getMemberAdAdministration();

			

			userSession.setOrganisationMember(om);
			userSession.put("mode", "searchresults");
			userSession.put("category", "1");
			omc.setCreateRequest(true);
			
			html.append("<div id=\"searchform3\">");
			html.append("<div style=\"position : relative; min-height : 4000px; width : 100%; background : #363D45;\">");
				//html.append("<div style=\"position : relative; height : 95%; width : 100%; background : #363D45;\">");
				
			html.append(maa.getMemberAdSearchForm3(context, omc, om, true));
				
			html.append("<div id=\"userprofile\" style=\"position : absolute;\">");
				
			html.append(OpenCommunityUserProfile.getMemoryList2(ocs, omc, userSession, true, 2));
			
			
			//OpenCommunityServer ocs, OrganisationMemberController omc, OpenCommunityUserSession userSession, boolean includeperson
				
			html.append("</div>");
				
			html.append("</div>");	
			
			html.append("</div>");	
			
			*/
			
		}
		else{
		

			
			html.append("<input type=\"button\" class=\"actionbutton\" value=\"Abbrechen\" onclick=\"cancelProcess()\">");
			html.append("<input type=\"button\" class=\"actionbutton\" value=\"Abschliessen\" onclick=\"getNextNode('finish=true')\">");
			html.append("<input type=\"button\" class=\"actionbutton\" value=\"Abschliessen und PDF erstellen\" onclick=\"getNextNode('finish=true&createpdf=true')\" style=\"width : 200px;\">");
			
			html.append("<table border style=\"position : absolute; left : 10px; top : 50px; width : 1200px; bottom : 10px;\">");
			
			html.append("<tr><td style=\"width : 50%;\">Inserate <input type=\"button\" class=\"actionbutton\" value=\"Inserat hinzufügen\" onclick=\"getNextNode('memberadcreate=true')\" style=\"float : right;\"></td><td>Adressbestellungen <input type=\"button\" class=\"actionbutton\" value=\"Adressbestellung hinzufügen\" onclick=\"getNextNode('memberadrequestcreate=true')\"  style=\"float : right;\"></td></tr>");
			
			html.append("<tr>");
			html.append("<td id=\"memberadlist\" valign=\"top\">");
			
			
			
			html.append(getMemberAdList());
				
			
			html.append("</td>");
			html.append("<td id=\"memorylist\">");
			
			html.append(getMemoryList(ocs, userSession, true));
			
			html.append("</td>");
			html.append("</tr>");
			html.append("</table>");
			
		}
		
		html.append("</div>");	
		
		return html.toString();
		
	}
	public String getMemberAdList(){
		StringBuilder html = new StringBuilder();
		
		int i = 0;
		
		for(BasicClass o : getObjects("MemberAd")){
				
			html.append("<div>");
				
			MemberAd ma = (MemberAd)o;
				
			html.append("<div style=\"font-weight : bold; margin-top : 10px;\">");
				
				
			html.append(ma.getString("Title"));
				
			html.append("</div>");
			html.append("<div>");
			html.append(ma.getString("Category"));
			html.append("<p>");
			html.append(ma.getString("Description"));
				
			html.append("<img src=\"images/delete.png\" onClick=\"onAction('" + getPath() + "','deletememberad','','memberadid=" + i + "')\" style=\"float : right;\">");
				
			html.append("</div>");
			html.append("</div>");
				
				
		}
		
		return html.toString();
		
	}
		
		
		
	public void setCategoryProperties(){
		
		if(getObject("MemberAdCategory") instanceof MemberAdCategory){
			
			MemberAdCategory mac = (MemberAdCategory)getObject("MemberAdCategory");
			
			for(BasicClass o : mac.getObjects("FieldDefinition")){
				
				FieldDefinition fd = (FieldDefinition)o;
				List codeList = fd.getCodeList();
				
				
				if(fd.getID("Type")==100){
					
					//addProperty("
				}
				else{
				
					for(int i = 0; i < codeList.size(); i++){	
						ConfigValue cv = (ConfigValue)codeList.get(i);	
						
						addProperty(fd.getName() + "_" + cv.getValue(), "String", "");
						
						
					}
					
				}
				
				
			}
			
			
			
		}
		
		
		
	}
	public String getMemberAdSearchForm(OpenCommunityUserSession userSession, ApplicationContext context){
		
		StringBuilder html = new StringBuilder();
		

		
		HTMLFormManager formManager = server.getFormManager();

		html.append("<input type=\"button\" class=\"actionbutton\" onclick=\"cancelProcess()\" value=\"Abbrechen\">");
		html.append("<input type=\"button\" class=\"actionbutton\" onclick=\"getNextNode('finishsearch=true')\" value=\"Abschliessen\">");
		html.append("<div id=\"memberadsearchform\" style=\"position : absolute; left : 0px; right : 0px; top : 30px; bottom : 0px; overflow : auto;\">");
		html.append(getMemberAdSearchFormContent(userSession, context));
		html.append("</div>");		
		
		return html.toString();
		
	}
	public String getMemberAdSearchFormContent(OpenCommunityUserSession userSession, ApplicationContext context){
		
		StringBuilder html = new StringBuilder();
		
		HTMLFormManager formManager = server.getFormManager();
		
		html.append("<div id=\"searchparams\" style=\"position : absolute; left : 0px; width : 400px; top : 0px; bottom : 0px; overflow : auto;\">");
		

		
		html.append("<div>" + formManager.getSelection(getProperty("MemberAdCategory"), "MemberAdCategory", "", true, true, true, "", "onAction('" + getPath() + "','setmemberadcategory','','memberadcategory=' + this.value)") + "</div>");
		
		html.append("<form id=\"adminSearchForm\" name=\"adminSearchForm\">");
		
		
		html.append("<div><b>Inserattyp</b><br>" + formManager.getSelection(getProperty("type"), "type", "", true, true, true, "", "adminDetailSearch()") + "</div>");
		
		if(getObject("MemberAdCategory") instanceof MemberAdCategory){
			
			MemberAdCategory mac = (MemberAdCategory)getObject("MemberAdCategory");
			
			for(BasicClass o : mac.getObjects("FieldDefinition")){
						
				FieldDefinition fd = (FieldDefinition)o;
				
				html.append("<div><b>" + fd + "</b></div>");
				
				List codeList = fd.getCodeList();
				for(int i = 0; i < codeList.size(); i++){	
					ConfigValue cv = (ConfigValue)codeList.get(i);	
					
					html.append("<div><input type=\"checkbox\" name=\"" + fd.getName() + "_" + cv.getValue() + "\" value=\"true\" onchange=\"adminDetailSearch()\">" + cv.getLabel() + "</div>");
					
					
				}
				
				
			}
			
			
			
		}
		
		html.append("<div><b>Quartier</b></div>");
		
		for(ConfigValue cv : ocs.getGeoObjects()){
			
			
			html.append("<div><input type=\"checkbox\" name=\"location_" + cv.getValue() + "\" value=\"true\" onchange=\"adminDetailSearch()\">" + cv.getValue() + " " + cv.getLabel() + "</div>");
			
			
		}
		html.append("<div><table>");
		html.append("<tr><td>Nachname : </td><td><input name=\"familyname\" onkeyup=\"adminDetailSearch()\"></td></tr>");
		html.append("<tr><td>Vorname : </td><td><input name=\"firstname\" onkeyup=\"adminDetailSearch()\"></td></tr>");
		html.append("<tr><td>Titel : </td><td><input name=\"title\" onkeyup=\"adminDetailSearch()\"></td></tr>");
		html.append("<tr><td>Beschreibung : </td><td><input name=\"description\" onkeyup=\"adminDetailSearch()\"></td></tr>");	
		html.append("</table></div>");
		
		
		html.append("</form>");
		
		
		html.append("</div>");
		
		html.append("<div id=\"searchresults\" style=\"position : absolute; left : 400px; width : 700px; top : 0px; bottom : 0px; overflow : auto;\">");
		
		html.append(getSearchResults(userSession));
		
		html.append("</div>");
		
		html.append("<div id=\"memorylist\" style=\"position : absolute; left : 1100px; right : 0px; top : 0px; bottom : 0px; overflow : auto;\">");
		
		html.append(getMemoryList(ocs, userSession, true));
		
		html.append("</div>");
		
		
		return html.toString();
		
	}
	public ActionResult onAction(BasicClass src, String command, ApplicationContext context){
		
		ActionResult result = null;	
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
		
		if(command.equals("setmemberadcategory")){
			
			String memberadcategory = context.getString("memberadcategory");
			if(memberadcategory != null && memberadcategory.length() > 0){
				
				setProperty("MemberAdCategory", memberadcategory);
				getProperty("MemberAdCategory").setSelection(maa.getObjects("MemberAdCategory"));
				result = new ActionResult(ActionResult.Status.OK, "Suche ausgeführt");
				result.setParam("dataContainer", "memberadsearchform");
				result.setData(getMemberAdSearchFormContent(userSession, context));
				
				setCategoryProperties();
			}
				
			
		}
		else if(command.equals("selectmemberad")){
			
			String memberadid = context.getString("memberadid");
			if(memberadid != null && memberadid.length() > 0){
				
				om.addMemberAdID(memberadid);
				result = new ActionResult(ActionResult.Status.OK, "Inserat ausgewählt");
				result.setParam("dataContainer", "memorylist");
				result.setData(getMemoryList(ocs, userSession, true));
				result.setParam("exec", "adminDetailSearch()");
				
			}
		
		}
		else if(command.equals("deselectmemberad")){
			
			String memberadid = context.getString("memberadid");
			if(memberadid != null && memberadid.length() > 0){
				
				om.removeMemberAdID(memberadid);
				result = new ActionResult(ActionResult.Status.OK, "Inserat ausgewählt");
				result.setParam("dataContainer", "memorylist");
				result.setData(getMemoryList(ocs, userSession, true));
				result.setParam("exec", "adminDetailSearch()");
				
			}
		
		}
		else if(command.equals("deletememberad")){
			
			String memberadid = context.getString("memberadid");
			if(memberadid != null && memberadid.length() > 0){
				
				MemberAd ma = (MemberAd)getObjectByIndex("MemberAd", Integer.parseInt(memberadid));
				
				if(ma != null){
					deleteElement("MemberAd", ma);
					result = new ActionResult(ActionResult.Status.OK, "Inserat ausgewählt");
					result.setParam("dataContainer", "memberadlist");
					result.setData(getMemberAdList());
					
				}
				
			}
		
		}
		else if(command.equals("searchads")){
			
			if(getObject("MemberAdCategory") instanceof MemberAdCategory){
				
				MemberAdCategory mac = (MemberAdCategory)getObject("MemberAdCategory");
				
				for(BasicClass o : mac.getObjects("FieldDefinition")){
					
					FieldDefinition fd = (FieldDefinition)o;
					List codeList = fd.getCodeList();
					for(int i = 0; i < codeList.size(); i++){	
						ConfigValue cv = (ConfigValue)codeList.get(i);	
						
						String name = fd.getName() + "_" + cv.getValue();
						
						if(hasProperty(name)){
							if(context.hasProperty(name)){
								setProperty(name, true);
							}
							else{
								setProperty(name, false);
							}
							
						}

						
						
					}
					
					
				}
				
				
				
			}
			
			for(ConfigValue cv : ocs.getGeoObjects()){
				String name = "location_" + cv.getValue();
				if(context.hasProperty(name)){
					setProperty(name, true);
				}
				else{
					setProperty(name, false);
				}
				
			}
			if(context.hasProperty("familyname")){
				setProperty("familyname", context.getString("familyname"));	
			}
			if(context.hasProperty("firstname")){
				setProperty("firstname", context.getString("firstname"));	
			}
			if(context.hasProperty("title")){
				setProperty("title", context.getString("title"));	
			}
			if(context.hasProperty("description")){
				setProperty("description", context.getString("description"));	
			}
			if(context.hasProperty("type")){
				setProperty("type", context.getString("type"));	
			}
			
			result = new ActionResult(ActionResult.Status.OK, "Inserat ausgewählt");
			result.setParam("dataContainer", "searchresults");
			result.setData(getSearchResults(userSession));
			
			
		}
		
		
		
		return result;
	}
	public String getPath(){
		return "/WebApplication/currentprocess";	
	}
	private String getSearchResults(OpenCommunityUserSession userSession){
		
		if(getObject("MemberAdCategory") instanceof MemberAdCategory){
			
			MemberAdCategory mac = (MemberAdCategory)getObject("MemberAdCategory");
			
			String familyname = getString("familyname");
			String firstname = getString("firstname");
			String title = getString("title");
			String description = getString("description");
			int type = getID("type");
			
			boolean isoffer = false;
			boolean isrequest = false;
			boolean tandemonly = false;
			if(type==1){
				isoffer = true;	
			}
			if(type==2){
				isrequest = true;	
			}
			if(type==3){
				tandemonly = true;
			}
			
			ObjectCollection results = new ObjectCollection("Results", "*");
		
			String fieldlist = "SELECT DISTINCT t1.ID, t1.Title, t1.Description, t1.ValidFrom, t1.ValidUntil, t1.IsOffer, t1.IsRequest, t1.Priority, t2.ID AS OMID, t3.Languages, t4.ZipCode, t4.City, t5.Sex, t5.DateOfBirth, t5.FirstLanguageS, t5.Familyname, t5.Firstname,";
			fieldlist += " (CASE WHEN (t1.Location IS NULL OR t1.Location = '') THEN t4.Zipcode ELSE t1.Location END) AS Location, count(t1.ID) OVER() AS total_count";
			String sql = " FROM MemberAd AS t1";			
			sql += " LEFT JOIN OrganisationMember AS t2 ON t1.OrganisationMemberID=t2.ID";
			sql += " LEFT JOIN Person AS t3 ON t2.Person=t3.ID";
			sql += " LEFT JOIN Address AS t4 ON t4.PersonID=t3.ID";
			sql += " LEFT JOIN Identity AS t5 ON t5.PersonID=t3.ID";


			
			Vector fieldDefinitions = mac.getObjects("FieldDefinition");
			
			int i = 0;
			for(i = 0; i <  fieldDefinitions.size(); i++ ){
				FieldDefinition fd = (FieldDefinition)fieldDefinitions.elementAt(i);
				sql += " LEFT JOIN Parameter AS t" + (7+i) + " ON t" + (7+i) + ".MemberAdID=t1.ID AND t" + (7+i) + ".Template=" + fd.getName();
			}
			
			if(om != null){ //Inserate sollen nur einmal bestellt werden können
				sql += " LEFT JOIN MemberAdRequest AS t" + (8 + i) + " ON t" + (8 + i)	+ ".MemberAd=t1.ID AND t" + (8 + i)	+ ".OrganisationMemberID=" + om.getName();
				fieldlist += " ,t" + (8 + i) + ".Status AS RequestStatus";
				fieldlist += " ,t" + (8 + i) + ".ValidUntil AS RequestDate";
				fieldlist += " , CASE WHEN t" + (8 + i) + ".ValidUntil < NOW() THEN 1 ELSE 0 END  AS RequestDateStatus";
			}
			
			sql += " WHERE t1.template=" + getID("MemberAdCategory") + " AND t1.Status=1";
			
			sql += " AND t1.ValidUntil >= Now() ";
			
			sql += " AND t2.Status=1"; //AK 20160605, nur aktive Benutzer
			

			
			String locationcriteria_alt = "(";
			String locationcriteria = "(";
			for(ConfigValue cv : ocs.getGeoObjects()){
				if(getBoolean("location_" + cv.getValue())){
					if(!cv.getValue().equals("*")){
						locationcriteria += "\'" + cv.getValue() + "\',";
					}
				}
				locationcriteria_alt += "\'" + cv.getValue() + "\',";
			}
			locationcriteria_alt = locationcriteria_alt.substring(0, locationcriteria_alt.length()-1) + ")";
			
			

			if(userSession.get("location_*") != null && ((String[])userSession.get("location_*"))[0].equals("true")){
				
				//Benutzer hat die Option (*) gewählt = die nicht spezifierten PLZ
				
			    if(locationcriteria.length() > 1){
			    	locationcriteria = locationcriteria.substring(0, locationcriteria.length()-1) + ")";
			    	
	
			    	
					sql += " AND ((char_length(t1.location) > 0 AND t1.location IN " + locationcriteria + " OR  char_length(t1.location) = 0 AND t4.ZipCode IN " + locationcriteria + ")";
			    	sql += " OR ((char_length(t1.Location) = 0 AND t4.Zipcode NOT IN " + locationcriteria_alt + ") OR (char_length(t1.Location) > 0 AND t1.Location NOT IN " + locationcriteria_alt + ")))";
			    	
			    }
			    else{

			    	sql += " AND ((char_length(t1.Location) = 0 AND t4.Zipcode NOT IN " + locationcriteria_alt + ") OR (char_length(t1.Location) > 0 AND t1.Location NOT IN " + locationcriteria_alt + "))";
			    }
					
			}
			else{
				if(locationcriteria.length() > 1){
					locationcriteria = locationcriteria.substring(0, locationcriteria.length()-1) + ")";

					sql += " AND (char_length(t1.location) > 0 AND t1.location IN " + locationcriteria + " OR  char_length(t1.location) = 0 AND t4.ZipCode IN " + locationcriteria + ")";
				}				
			}
			
			
			for(i = 0; i <  fieldDefinitions.size(); i++ ){
				FieldDefinition fd = (FieldDefinition)fieldDefinitions.elementAt(i);
				if(hasParameter(fd, userSession.getParameters())){

					String valuelist = getValueList(fd, userSession.getParameters());
					if(fd.getInt("RefersTo") > 0){                             
						sql += " AND (t" + (7+i) + ".Value IN " + valuelist + " OR t" + (7+i+1) + ".Value IN " + valuelist + ")";
					}
					else{

						String valuelist2 = valuelist.replace("(","");
						valuelist2 = valuelist2.replace(")","");
						sql += " AND ARRAY[" + valuelist2 + "]::float[]  <@ (SELECT ARRAY(SELECT Value FROM Parameter WHERE MemberAdID=t1.ID AND Template=" + fd.getName() + " ORDER BY VALUE))";
					}
				}
			}
			
            if(tandemonly){
            	sql += " AND t1.IsOffer=true AND t1.IsRequest=true";
            }
            else if(!isoffer && isrequest){
            	sql += " AND t1.IsOffer=false AND t1.IsRequest=true";
            }
            else if(isoffer && !isrequest){
            	sql += " AND t1.IsOffer=true AND t1.IsRequest=false";
            }
            
            if(familyname != null && familyname.length() > 0){
            	sql += " AND t5.FamilyName ilike '" + familyname + "%'";
            }
            if(firstname != null && firstname.length() > 0){
            	sql += " AND t5.FirstName ilike '" + firstname + "%'";
            }
            if(title != null && title.length() > 0){
            	sql += " AND t1.Title ilike '%" + title + "%'";
            }
            if(description != null && description.length() > 0){
            	sql += " AND t1.Description ilike '%" + description + "%'";
            }


			//sql += " ORDER BY t1.Priority DESC, Location";
			sql += " ORDER BY Location";
			

			
			
			sql = fieldlist + sql;
			
			ocs.logAccess(sql);
			
			ocs.queryData(sql, results);	
			
			
			StringBuilder html = new StringBuilder();
			
			
			boolean even = false;
			
			String prevzipcode = "";
			
			int currentyear = DateConverter.getCurrentYear();
			
			for(BasicClass record : results.getObjects()){
				
				String id = record.getString("ID");
				
				String zipcode = record.getString("ZIPCODE");
				
				if(!zipcode.equals(prevzipcode)){
					html.append("<div><b>" + zipcode + "</b></div>");	
				}
				
				if(even){
					
					html.append("<div class=\"even\">");
					
				}
				else{
				
					html.append("<div class=\"odd\">");
					
				}
				
				even = !even;
				
				
				String sex2 = "Mann";
				String sex = record.getString("SEX");
				if(sex.equals("2")){
					sex2 = "Frau";	
				}
					
				String dateofbirth = record.getString("DATEOFBIRTH");
				int age = 0;
				if(dateofbirth.length() > 3){
					String[] args = dateofbirth.split("\\.");
					String syear = args[args.length-1];
					int year = Integer.parseInt(syear);
					age = currentyear - year;
				}
				
				
				html.append(record.getString("FIRSTNAME") + " " + record.getString("FAMILYNAME") + " ");
				
				html.append(sex2 + "," + age);
				
				html.append("<br><b>" + record.getString("TITLE") + "</b>");	
				html.append("<br>" + record.getString("DESCRIPTION"));
				//html.append("<br>" + record.getString("REQUESTSTATUS"));
				
				if(!om.hasMemberAdID(id) && record.getString("REQUESTSTATUS").length()==0){
					html.append("<br><input type=\"button\" onclick=\"onAction('" + getPath() + "','selectmemberad','','memberadid=" + record.getString("ID") + "')\" value=\"Auswählen\">");
				}
				
				html.append("</div>");
				
				prevzipcode = zipcode;
			}
			
			
			
			return html.toString();
		
		}
		else{
			return "";	
		}
		
		
	}
	public boolean hasParameter(FieldDefinition fd, Map<String, String[]> searchparameters){
		boolean hasParameter = true;
		/*
		Vector codeList = fd.getCodeList();
		for(int i = 0; i < codeList.size(); i++){
			ConfigValue cv = (ConfigValue)codeList.elementAt(i);
			if(searchparameters.get(fd.getName() + "_" + cv.getValue()) != null){

				//hasParameter = true;	
				
				String[] values = (String[])searchparameters.get(fd.getName() + "_" + cv.getValue()); //nur berücksichtigen, wenn mindestens eine Option ausgewählt
				if(values.length==1 && values[0].equals("true")){
					hasParameter = true;	
				}
			}
		}
		*/
		return hasParameter;
	}
	public String getValueList(FieldDefinition fd, Map parameters){
		String valueList = "(";
		Vector codeList = fd.getCodeList();
		for(int i = 0; i < codeList.size(); i++){
			ConfigValue cv = (ConfigValue)codeList.elementAt(i);
			String name = fd.getName() + "_" + cv.getValue();
			if(hasProperty(name) && getString(name).equals("true")){

				valueList += cv.getValue() + ",";
				
			}
		}
		
		if(valueList.length() > 1){
			valueList = valueList.substring(0,valueList.length()-1);	
		}
		valueList += ")";
		return valueList;
	}
	public String getMemoryList(OpenCommunityServer ocs, OpenCommunityUserSession userSession, boolean includeperson){
		
		StringBuilder html = new StringBuilder();

		ocs.logAccess("selected ads : " + om.getMemberAdIDs().size());

				
		if(om.getMemberAdIDs() != null && om.getMemberAdIDs().size() > 0){
						
				
			String adids = "(";
			for(Object adid : om.getMemberAdIDs()){
				adids += adid + ",";
			}
					
			adids = adids.substring(0, adids.length()-1) + ")";
					
			ObjectCollection results = new ObjectCollection("Results", "*");
					
			String sql = "SELECT t1.ID, t1.Title, t1.Description, t1.ValidFrom, t1.ValidUntil, t4.Sex, t4.DateOfBirth, t4.Familyname, t4.Firstname,";
			sql += " t5.ZipCode, t5.Street, t5.Number, t5.City, t6.ID AS MAC, t6.Title AS Category, t7.Value AS Email, t8.Value AS PhoneP, t9.Value AS PhoneM FROM MemberAd AS t1";
					
			sql += " LEFT JOIN OrganisationMember AS t2 ON t1.OrganisationMemberID=t2.ID";
			sql += " LEFT JOIN Person AS t3 ON t2.Person=t3.ID";
			sql += " LEFT JOIN Identity AS t4 ON t4.PersonID=t3.ID";
					sql += " LEFT JOIN Address AS t5 ON t5.PersonID=t3.ID";

					sql += " LEFT JOIN Contact AS t7 ON t7.PersonID=t4.ID AND t7.Type=3";
					sql += " LEFT JOIN Contact AS t8 ON t8.PersonID=t4.ID AND t8.Type=0";
					sql += " LEFT JOIN Contact AS t9 ON t9.PersonID=t4.ID AND t9.Type=2";
					
					sql += " LEFT JOIN MemberAdCategory AS t6 ON t1.Template = t6.ID";
					
					sql += " WHERE t1.ID IN " + adids;
					
					
					
					ocs.queryData(sql, results);
					
					String prevcategory = "";
					

					
					for(BasicClass record : results.getObjects()){
						
						boolean include = true;
						
						String sex = record.getString("SEX");

						
						String sex2 = "Mann";
						if(sex.equals("2")){
							sex2 = "Frau";	
						}
						int currentyear = DateConverter.getCurrentYear();
						int yearofbirth = 1968;
						int age = currentyear - yearofbirth;
						
						String dateofbirth = record.getString("DATEOFBIRTH");
						if(dateofbirth.length() > 3){
							String[] args = dateofbirth.split("\\.");
							String syear = args[args.length-1];
							int year = Integer.parseInt(syear);
							age = currentyear - year;
						}
						
						String zipcode = record.getString("ZIPCODE");
						String city = record.getString("CITY");
						String category = record.getString("CATEGORY");
						String location = zipcode + " " + city;
						
	
						
						
						
						
						if(include){
							
							if(!category.equals(prevcategory)){
								if(includeperson){   //Admin-Notizen
									html.append("<div>");
									
									String commentid = "comment_" + record.getString("MAC");
									
									html.append("<textarea id=\"" + commentid + "\" onblur=\"saveComment(this.id, this.value)\" style=\"width : 500px;\">");
									if(userSession.get(commentid) != null){
										html.append(userSession.get(commentid));	
									}
									html.append("</textarea>");
									html.append("</div>");
								}
								html.append("\n<div class=\"searchresultheader\">");
								html.append(category);
								html.append("</div>");
								
							}
							
							html.append("<div id=\"" + record.getString("ID") + "\" class=\"searchresult\" style=\"height : auto; color : black;\">");
							html.append("<table>");
							
							html.append("<tr><td class=\"searchresultcell1\" style=\"color : black;\">" + zipcode + " " + city + "</td><td rowspan=\"2\"><a class=\"objectaction\" href=\"javascript:onAction('" + getPath() + "','deselectmemberad','','memberadid=" + record.getString("ID") + "')\"><img src=\"images/delete.png\">löschen</a></td></tr>");
							html.append("<tr><td class=\"searchresultcell2\" style=\"color : black;\">" + sex2 + " (" + age + ") " + record.getString("TITLE"));
							html.append("<br>" + record.getString("DESCRIPTION"));
							if(includeperson){
								html.append("<br>Name : " + record.getString("FAMILYNAME"));
								html.append("<br>Vorname : " + record.getString("FIRSTNAME"));
								html.append("<br>Adresse : " + record.getString("STREET") + " " + record.getString("NUMBER"));
								html.append("<br>" + record.getString("ZIPCODE") + " " + record.getString("CITY"));
								html.append("<br>Email : " + record.getString("EMAIL"));
								html.append("<br>Tel. p : " + record.getString("PHONEP"));
								html.append("<br>Tel. m : " + record.getString("PHONEM"));
							}
							
							html.append("</td></tr>");
							html.append("</table>");
							html.append("</div>");
						}
						
						prevcategory = category;
				}
					
		}
		return html.toString();
	}


}