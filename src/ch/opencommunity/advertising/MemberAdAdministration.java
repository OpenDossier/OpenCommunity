package ch.opencommunity.advertising;


import ch.opencommunity.base.*;
import ch.opencommunity.server.*;
import ch.opencommunity.common.*;
import ch.opencommunity.news.*;
import ch.opencommunity.process.MemberRegistration;
import ch.opencommunity.process.OrganisationMemberEdit;

import org.kubiki.base.*;
import org.kubiki.application.*;
import org.kubiki.application.server.ApplicationServer;
import org.kubiki.application.server.WebApplicationModule;
import org.kubiki.cms.CMSServer;
import org.kubiki.cms.WebPageElementInterface;
import org.kubiki.cms.ImageObject;
import org.kubiki.servlet.WebApplication;
import org.kubiki.application.server.WebApplicationContext;
import org.kubiki.database.Record;
import org.kubiki.database.DataStore;
import org.kubiki.util.DateConverter;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;

import java.util.Map;
import java.util.Vector;
import java.util.Random;
import java.util.List;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.ArrayList;



import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;


import javax.servlet.http.*;




//public class MemberAdAdministration extends BasicOCObject implements WebPageElementInterface{
public class MemberAdAdministration extends WebApplicationModule implements WebPageElementInterface{
	
	
	Math m;
	Random random;
	
	HashMap<String, MemberAdCategory> categoryMap = null;
	
	OpenCommunityServer ocs = null;
	
	private int nCommercialAds = 0;
	
	public MemberAdAdministration(){
		
		setName("MemberAdAdministration");
		
		//setTablename("MemberAdAdministration");
		addObjectCollection("MemberAdCategory", "ch.opencommunity.advertising.MemberAdCategory");	
		
		addObjectCollection("MemberAd", "ch.opencommunity.advertising.MemberAd");	
		
		random = new Random();
		
		categoryMap = new HashMap<String, MemberAdCategory>();
		

	}
	public List<BasicClass> getCategories(){
		return getObjects("MemberAdCategory");
	}
	public void initModule(){
		
		ocs = (OpenCommunityServer)getRoot();
		
	}
	public void initData(DataStore dataStore){
		
		try{
			
			getObjectCollection("MemberAd").removeObjects();
		
			String sql = "SELECT ID FROM MemberAdCategory ORDER BY ID";
			Connection con = dataStore.getConnection();
			Statement stmt = con.createStatement();
			
			ResultSet res = stmt.executeQuery(sql);
			while(res.next()){
				
				ocs.getObject(this, "MemberAdCategory", "ID", res.getString("ID"));			
				
			}
			
			
			res.close();
			con.close();
			
			for(BasicClass o : getObjects("MemberAdCategory")){
				categoryMap.put(o.getName(), (MemberAdCategory)o);
			}
			
		}
		catch(java.lang.Exception e){
			ocs.logException(e);
		}
		
		
	}
	public void initCommercialAds(DataStore dataStore){
		
		try{
		
			String sql = "SELECT ID FROM MemberAd WHERE Template=0 ORDER BY ID";
			Connection con = dataStore.getConnection();
			Statement stmt = con.createStatement();
			
			ResultSet res = stmt.executeQuery(sql);
			while(res.next()){
				
				MemberAd memberad = (MemberAd)ocs.getObject(this, "MemberAd", "ID", res.getString("ID"));	
				
				ImageObject imageObject = (ImageObject)memberad.getObjectByIndex("ImageObject", 0);
				if(imageObject != null){
					FileObjectData fileObjectData = (FileObjectData)imageObject.getObjectByIndex("FileObjectData", 0);
					
					//ocs.logException("FOD: " + fileObjectData);
					
					if(fileObjectData != null){
						try{
	
							if(fileObjectData.getObject("FileData") instanceof byte[]){
								BufferedImage img = ImageIO.read(new ByteArrayInputStream((byte[])fileObjectData.getObject("FileData")));
								int height = img.getHeight();
								memberad.addProperty("Height", "Integer", "" + height);
							}
							
						}
						catch(java.lang.Exception e){
							ocs.logException(e);	
						}
						
					}
					
				}
				
				
				
			}
			
			nCommercialAds = getObjects("MemberAd").size();
			res.close();
			con.close();
			

			
		}
		catch(java.lang.Exception e){
			ocs.logException(e);
		}
		
		
		
	}
	public void initObjectLocal(){
		
		/*
		for(BasicClass o : getObjects("MemberAdCategory")){
			categoryMap.put(o.getName(), (MemberAdCategory)o);
		}
		*/

	}
	/*/todelete:AK 20180414
	public String getMemberAdEntryForm(){
		StringBuilder html = new StringBuilder();
		html.append("<div id=\"memberaddentryform\">");
		
		html.append("hdwiduwhd");
		
		html.append("</div>");
		
		return html.toString();
	}
	*/
	public ActionResult onAction(BasicClass source, String command, ApplicationContext context){
		ocs.logAccess(command);
		
		ActionResult result = null;
		
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
		
		if(command.equals("detailsearch")){
			
			HttpServletRequest request = ((WebApplicationContext)context).getRequest();
						
						//userSession.clear();
						
			for(Object o : request.getParameterMap().keySet()){
				String key = (String)o;
				userSession.put(key , request.getParameterValues(key));
			}
						
			String memberadid = request.getParameter("memberadid");
			if(memberadid != null){
                                              
				String select = request.getParameter("select");
				if(select.equals("true")){
					userSession.addMemberAdID(memberadid);								
				}
				else{
					userSession.removeMemberAdID(memberadid);
					userSession.remove("memberadid");
				}
			}
						
			result = new ActionResult(ActionResult.Status.OK, "Detailsuche geladen");
			result.setParam("dataContainer", "resultlist");
						
			String embedded = request.getParameter("embedded");
			if(embedded != null && embedded.equals("true")){
				if(userSession.getCurrentProcess() != null  && userSession.getCurrentProcess() instanceof OrganisationMemberEdit){
					result.setData(getSearchResults(context, ((OrganisationMemberEdit)userSession.getCurrentProcess()).getCurrentControler().getOrganisationMember(), true));
				}
			else{
								result.setData(getSearchResults(context, true));
							}
						}
						else{
							result.setData(getSearchResults(context));				
						}
							
						
						String reloadmemorylist = request.getParameter("reloadmemorylist");
						if(reloadmemorylist != null && reloadmemorylist.equals("true")){
							result.setParam("exec", "reloadMemoryList()");
						}
		}
		
		return result;
		
		
	}
	public String getAdminForm(ApplicationContext context){
		return "";
	}
	public String toHTML(ApplicationContext context){
		return getMemberAdSearchForm(context);
		//return "";
	}
	public String toHTML(ApplicationContext context, List parameters){
		return getMemberAdSearchForm(context);
		//return "";
	}
	public String getValueList(FieldDefinition fd, Map parameters){
		String valueList = "(";
		Vector codeList = fd.getCodeList();
		for(int i = 0; i < codeList.size(); i++){
			ConfigValue cv = (ConfigValue)codeList.elementAt(i);
			if(parameters.get(fd.getName() + "_" + cv.getValue()) != null){
				String[] values = (String[])parameters.get(fd.getName() + "_" + cv.getValue());
				if(values.length==1 && values[0].equals("true")){
					valueList += cv.getValue() + ",";
				}
			}
		}
		
		if(valueList.length() > 1){
			valueList = valueList.substring(0,valueList.length()-1);	
		}
		valueList += ")";
		return valueList;
	}
	public boolean hasParameter(FieldDefinition fd, Map<String, String[]> searchparameters){
		boolean hasParameter = false;
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
		return hasParameter;
	}
	public String getMemberAdSearchForm(ApplicationContext context){
		return 	getMemberAdSearchForm(context, null, false);
	}
	public String getSearchMenu(OpenCommunityUserSession userSession, boolean embedded){
		
		return SearchMenu.getSearchMenu(this, ocs, userSession);
		
	}
	public String getSearchMenu2(OpenCommunityUserSession userSession, boolean embedded){
		
		
		ch.opencommunity.server.HTMLForm formManager = ocs.getFormFactory();
		
		StringBuilder html = new StringBuilder();
		
		boolean isoffer = true;
		boolean isrequest = true;
		boolean tandemonly = false;
			
		int cntoffers = 0;
		int cntrequests = 0;
			
		String category = (String)userSession.get("category");
			
		String location = (String)userSession.get("location");
			

			
		MemberAdCategory mac = (MemberAdCategory)getObjectByName("MemberAdCategory", category);
			
		String cbimageselected = "/res/icons/cbimageselected.png";
		String cbimage = "/res/icons/cbimage.png";
		
		cbimageselected = "/res/icons/checkbox-filter_on.png";
		cbimage = "/res/icons/checkbox-filter_off.png";		
						

		/*
		if(embedded){
			html.append("\n<div id=\"criterias\" style=\"\">");
		}
		else{
			html.append("\n<div id=\"criterias\" style=\"max-height : 800px;\">");
		}
		*/
			

			
		html.append("<div id=\"categoryselection\" onclick=\"expandSelectionDiv(event, \'categoryselection2\')\" onmouseover=\"highlightSelection(this.id, true)\" onmouseout=\"highlightSelection(this.id, false)\">");
		html.append("<input type=\"hidden\" id=\"selectedcategorytitle\" value=\"" + mac + "\">");
		if(mac != null){
			html.append("<span id=\"selectedcategory\">" + mac + "</span><br>");
		}
		else{
			html.append("<span id=\"selectedcategory\">Was? Rubrik wählen</span><br>");
		}		

			html.append("<div id=\"categoryselection2\">");
			for(BasicClass bc :  getObjects("MemberAdCategory")){
				if(embedded){
					if(mac != null && mac.getName().equals(bc.getName())){
						html.append("<a href=\"javascript:getNextNode('category=" + bc.getName() + "')\" style=\"color : red\">" + bc + "</a><br>");
					}
					else{
						html.append("<a href=\"javascript:getNextNode('category=" + bc.getName() + "')\" style=\"color : black\">" + bc + "</a><br>");
					}					
				}
				else{
					if(mac != null && mac.getName().equals(bc.getName())){
						html.append("<a href=\"javascript:reloadSearch(" + bc.getName() + ")\" style=\"color : red\">" + bc + "</a><br>");
					}
					else{
						html.append("<a href=\"javascript:reloadSearch(" + bc.getName() + ")\" style=\"color : black\">" + bc + "</a><br>");
					}
				}
			}			
			html.append("</div>");
			html.append("</div>");
			
			
			
			
			
			html.append("\n<div class=\"profilemenuitem\" style=\"background :url(\'/res/website/bg_rubriken_top.png\'); height : 40px;\"></div>");
			
			
			
			html.append("\n<div class=\"profilemenuitem\" style=\"font-size : 16px; font-family : Museo500Regular; height : 40px;\"><img class=\"catpicto2\" src=\"images/" + mac.getName() + "_active.png\">verfeinern</div>");
			
			html.append("<div style=\"background : white;\"><div style=\"margin-left : 8px; margin-right : 8px; background : white; border-bottom : 1px solid black;\"> </div></div>");
			
			if(mac != null){
				
				if(mac.getName().equals("6")){
					
					FieldDefinition fd1 = (FieldDefinition)mac.getObjectByIndex("FieldDefinition", 0);
					FieldDefinition fd2 = (FieldDefinition)mac.getObjectByIndex("FieldDefinition", 0);
					
					html.append("<div class=\"profilemenuitem\">");
					
					html.append("<span id=\"span_languages_tandem\" onclick=\"openMenuItem('languages_tandem')\"><img id=\"icon_languages_tandem\" src=\"/res/icons/weniger_inaktiv.png\" onclick=\"openMenuItem('languages_tandem')\">");
					
					html.append("Tandem</span>");
					html.append("<div id=\"languages_tandem\" style=\"display : none; margin-left : 8px;\">");
					html.append("Du sprichst");
					html.append("<br><select class=\"languageselection\" id=\"select_languages_offer_1\" onchange=\"detailSearch()\">");
					html.append("<option value=\"\"></option>");
					for(Object o : fd1.getCodeList()){	
						ConfigValue cv = (ConfigValue)o;
						html.append("<option value=\"" + cv.getValue() + "\">" + cv.getLabel() + "</option>");
						
					}
					html.append("</select>");
					html.append("<br>Ich spreche");
					html.append("<br><select class=\"languageselection\" id=\"select_languages_request_1\" onchange=\"detailSearch()\">");
					html.append("<option value=\"\"></option>");
					for(Object o : fd2.getCodeList()){	
						ConfigValue cv = (ConfigValue)o;
						html.append("<option value=\"" + cv.getValue() + "\">" + cv.getLabel() + "</option>");
						
					}
					
					
					html.append("</select>");
					html.append("</div>");
					html.append("</div>");
					
					html.append("<div style=\"background : white;\"><div style=\"margin-left : 8px; margin-right : 8px; background : white; border-bottom : 1px solid black;\"> </div></div>");
					
					html.append("<div class=\"profilemenuitem\">");
					html.append("<span id=\"span_languages_offer\" onclick=\"openMenuItem('languages_offer')\"><img id=\"icon_languages_offer\" src=\"/res/icons/weniger_inaktiv.png\">");
					html.append("Angebot: ich spreche</span>");
					html.append("<div id=\"languages_offer\" style=\"display : none; margin-left : 8px;\">");
					html.append("<select class=\"languageselection\" id=\"select_languages_offer_2\" onchange=\"detailSearch()\">");
					html.append("<option value=\"\"></option>");
					for(Object o : fd1.getCodeList()){	
						ConfigValue cv = (ConfigValue)o;
						html.append("<option value=\"" + cv.getValue() + "\">" + cv.getLabel() + "</option>");
						
					}					
					
					html.append("</select>");					
					html.append("</div>");
					html.append("</div>");
					
					html.append("<div style=\"background : white;\"><div style=\"margin-left : 8px; margin-right : 8px; background : white; border-bottom : 1px solid black;\"> </div></div>");
					
					html.append("<div class=\"profilemenuitem\">");
					html.append("<span id=\"span_languages_request\" onclick=\"openMenuItem('languages_request')\"><img id=\"icon_languages_request\" src=\"/res/icons/weniger_inaktiv.png\">");
					html.append("Nachfrage: du sprichst</span>");
					html.append("<div id=\"languages_request\" style=\"display : none; margin-left : 8px;\">");
					html.append("<select class=\"languageselection\" id=\"select_languages_request_2\" onchange=\"detailSearch()\">");
					html.append("<option value=\"\"></option>");
					for(Object o : fd2.getCodeList()){	
						ConfigValue cv = (ConfigValue)o;
						html.append("<option value=\"" + cv.getValue() + "\">" + cv.getLabel() + "</option>");
						
					}
					
					
					html.append("</select>");					
					html.append("</div>");
					html.append("</div>");
					
					html.append("<div style=\"background : white;\"><div style=\"margin-left : 8px; margin-right : 8px; background : white; border-bottom : 1px solid black;\"> </div></div>");
					
					html.append("<form id=\"searchadform\" name=\"searchadform\">");
					
					FieldDefinition fd = (FieldDefinition)mac.getObjectByIndex("FieldDefinition", 2);
					
					for(Object o : fd.getCodeList()){
								html.append("\n<div class=\"profilemenuitem\">");
								ConfigValue cv = (ConfigValue)o;
								String label = cv.getLabel();
								String[] args = label.split("/");
								String helptext = null;
								if(args.length==2){
									label = args[0];
									helptext = args[1];	
								}
								if(userSession.get(fd.getName() + "_" + cv.getValue()) == null){
									
									html.append( formManager.getCustomCheckbox(fd.getName() + "_" + cv.getValue(), "false", false, cbimage, cbimage, label , null, "customcheckbox2", "detailSearch", helptext));	
								}
								else{
								
									if(((String[])userSession.get(fd.getName() + "_" + cv.getValue()))[0].equals("true")){
										html.append( formManager.getCustomCheckbox(fd.getName() + "_" + cv.getValue(), "true", true, cbimageselected, cbimage, label, null, "customcheckbox2", "detailSearch", helptext));	
									}
									else{
										html.append( formManager.getCustomCheckbox(fd.getName() + "_" + cv.getValue(), "false", true, cbimage, cbimage, label, null, "customcheckbox2", "detailSearch", helptext));	
									}
								}
								html.append("</div>");
					}
					
					html.append("<div style=\"background : white;\"><div style=\"margin-left : 8px; margin-right : 8px; background : white; border-bottom : 1px solid black;\"> </div></div>");
					
					
				}
				else{
					
					html.append("<form id=\"searchadform\" name=\"searchadform\">");
					
					if(!mac.getBoolean("RequestOnly")){
						
						html.append("<div class=\"profilemenuitem\">");
						
						html.append("<table style=\"width: 200px;\"><tr>");
						
						if(userSession.get("isoffer") == null || ((String[])userSession.get("isoffer"))[0].equals("true")){
							html.append("<td>" + formManager.getCustomCheckbox("isoffer", "true", true, cbimageselected, cbimage, "Angebot", null, "customcheckbox2", "detailSearch") + "</td>");
						}
						else{
							html.append("<td>" + formManager.getCustomCheckbox("isoffer", "false", false, cbimage, cbimage, "Angebot", null, "customcheckbox2", "detailSearch")+ "</td>");
							isoffer = false;
						}
						if(userSession.get("isrequest") == null || ((String[])userSession.get("isrequest"))[0].equals("true")){
							html.append("<td>" + formManager.getCustomCheckbox("isrequest", "true", true, cbimageselected, cbimage, "Nachfrage", null, "customcheckbox2", "detailSearch")+ "</td>");
						}
						else{
							html.append("<td>" + formManager.getCustomCheckbox("isrequest", "false", false, cbimage , cbimage, "Nachfrage", null, "customcheckbox2", "detailSearch")+ "</td>");
							isrequest = false;
						}
						if(mac.getBoolean("TandemAllowed")){
							if(userSession.get("tandemonly") == null || ((String[])userSession.get("tandemonly"))[0].equals("false")){
								html.append("<td>" + formManager.getCustomCheckbox("tandemonly", "false", false, cbimage , cbimage, "Nur Tandem", null, "customcheckbox2", "detailSearch")+ "</td>");	
							}
							else{
								html.append("<td>" + formManager.getCustomCheckbox("tandemonly", "true", true, cbimageselected , cbimage, "Nur Tandem", null, "customcheckbox2", "detailSearch")+ "</td>");	
								tandemonly = true;
							}
						}
						html.append("</tr></table>");
						html.append("</div>");
						
						html.append("<div style=\"background : white;\"><div style=\"margin-left : 8px; margin-right : 8px; background : white; border-bottom : 1px solid black;\"> </div></div>");
					}
				
					//html.append("<div style=\"max-height : 500px; overflow : auto;\">");
					html.append("<div id=\"searchdetails\">");
					
					String expand = "/res/mehr_aktiv.png";
					
					for(BasicClass bc : mac.getObjects("FieldDefinition")){
											
						FieldDefinition fd = (FieldDefinition)bc;

						
						if(!fd.getBoolean("TandemOnly")){
							
							
							html.append("<div>");
							html.append("\n<div class=\"profilemenuitem\">");
							//html.append( formManager.getCustomCheckbox(fd.getName(), "true", true, cbimageselected, cbimage, fd.toString(), null, "customcheckbox2", "expandDetailMenu", null));
							html.append("<img id=\"icon_" + fd.getName() + "\" src=\"/res/icons/mehr_inaktiv.png\" onclick=\"expandDetailMenu('" + fd.getName() + "')\">");
							
							html.append(fd.getString("Title"));
							
							html.append("</div>");
							html.append("</div>");
							html.append("<div id=\"details_" + fd.getName() + "\" class=\"categorydetails\">");
							for(Object o : fd.getCodeList()){
								
								ConfigValue cv = (ConfigValue)o;
								
								html.append("\n<div id=\"item_" + fd.getName() + "_" + cv.getValue() + "\" class=\"profilemenuitem\">");
								
								String label = cv.getLabel();
								String[] args = label.split("/");
								String helptext = null;
								if(args.length==2){
									label = args[0];
									helptext = args[1];	
								}
								if(userSession.get(fd.getName() + "_" + cv.getValue()) == null){
									//String id, String value, boolean selected, String imageselected, String image, String label, String name, String classname
									//html.append( formManager.getCustomCheckbox(fd.getName() + "_" + cv.getValue(), "true", true, cbimageselected, cbimage, label , null, "customcheckbox2", "detailSearch", helptext));	
									html.append( formManager.getCustomCheckbox(fd.getName() + "_" + cv.getValue(), "false", false, cbimage, cbimage, label , null, "customcheckbox2", "detailSearch", helptext));	
								}
								else{
								
									if(((String[])userSession.get(fd.getName() + "_" + cv.getValue()))[0].equals("true")){
										html.append( formManager.getCustomCheckbox(fd.getName() + "_" + cv.getValue(), "true", true, cbimageselected, cbimage, label, null, "customcheckbox2", "detailSearch", helptext));	
									}
									else{
										html.append( formManager.getCustomCheckbox(fd.getName() + "_" + cv.getValue(), "false", true, cbimage, cbimage, label, null, "customcheckbox2", "detailSearch", helptext));	
									}
								}
								html.append("</div>");
							}
							html.append("</div>");
	
							html.append("<div style=\"background : white;\"><div style=\"margin-left : 8px; margin-right : 8px; background : white; border-bottom : 1px solid black;\"> </div></div>");
						}
						
					}
					
					
					
					html.append("<div id=\"resetfilter\" onclick=\"resetFilter()\" style=\"display : none; background : white;\">");
					
					html.append("<img src=\"/res/icons/closeWizard.png\">");
					
					html.append("<span>Alle Filter zurücksetzen</span>");
					
					html.append("</div>");
					
					html.append("</div>");
					
					
					html.append("\n<script language=\"javascript\">");
					html.append("\ncheckDetailMenu();");
					html.append("\n</script>");

				}
				
			}	
			html.append("\n<div class=\"profilemenuitem\" style=\"background :url(\'images/bg_rubriken_bottom.png\'); height: 28px;\"></div>");

			
			
			html.append("\n<div id=\"locationselection\" onclick=\"expandSelectionDiv(event, \'locationselection2\')\">");
			html.append("Wo? Quartier wählen<br>");

			html.append("\n<div id=\"locationselection2\">");
			
			html.append("\n<table>"); //AK 20160826
			html.append("<tr><td colspan=2>");
			html.append("\n" + formManager.getCustomCheckbox("true", "false", false, cbimageselected, cbimage, "Alle auswählen", null, "customcheckbox",  "checkAllLocations") + "");
			
			//html.append("</td><td>");
			
			html.append("</td></tr><tr><td colspan=2>");
			
			html.append("\n" + formManager.getCustomCheckbox("false", "false", false, cbimageselected, cbimage, "Alle abwählen", null, "customcheckbox",  "checkAllLocations") + "");
			html.append("</td></tr>");
			
			int cnt = 0;
			int cnt2 = 0;
			

			for(ConfigValue cv : ocs.getGeoObjects()){
				

				String label = "" + cv.getValue();
				
				label = label.replace("* ", "");
				
				cnt++;
				cnt2++;
				
				if(cnt2==3){
					
					html.append("</td></tr><tr><td id=\"dc_" + cv.getValue() + "\" class=\"locationcell\" onmouseover=\"showHelp2(event, '" + cv.getLabel() + "')\" onmouseout=\"hideHelp()\">");
				}
				else{
					html.append("</td><td id=\"dc_" + cv.getValue() + "\" class=\"locationcell\" onmouseover=\"showHelp2(event, '" + cv.getLabel() + "')\" onmouseout=\"hideHelp()\">");
				}
				
				if(cnt == 1){
					html.append("<tr><td id=\"dc_" + cv.getValue() + "\" class=\"locationcell\" onmouseover=\"showHelp2(event, '" + cv.getLabel() + "')\" onmouseout=\"hideHelp()\">");
				}

				
				
				if(userSession.get("location_" + cv.getValue()) != null && ((String[])userSession.get("location_" + cv.getValue()))[0].equals("true")){
					//html.append("\n" + formManager.getCustomCheckbox("location_" + cv.getValue(), "true", true, cbimageselected, cbimage, cv.getValue() + " " +  cv.getLabel()) + "<br>");
					html.append("\n" + formManager.getCustomCheckbox("location_" + cv.getValue(), "true", true, cbimageselected, cbimage, label) + "");
				}
				else{
					//html.append("\n" + formManager.getCustomCheckbox("location_" + cv.getValue(), "false", false, cbimage, cbimage, cv.getValue() + " " +  cv.getLabel()) + "<br>");
					html.append("\n" + formManager.getCustomCheckbox("location_" + cv.getValue(), "false", false, cbimage, cbimage, label) + "");
				}
				/*
				if(cnt2==2){
					

					
					html.append("</td></tr><tr><td id=\"dc_" + cv.getValue() + "\" class=\"locationcell\" onmouseover=\"showHelp2(event, '" + cv.getLabel() + "')\" onmouseout=\"hideHelp()\">");
				}
				else{
					html.append("</td><td id=\"dc_" + cv.getValue() + "\" class=\"locationcell\" onmouseover=\"showHelp2(event, '" + cv.getLabel() + "')\" onmouseout=\"hideHelp()\">");
				}
				
				*/
				//if(cnt2==3){
				if(cnt2==3){
					cnt2 = 1;	
				}
			}
			
			html.append("</td></tr>");
			
			html.append("\n<tr><td colspan=2><input class=\"nodebutton\" type=\"button\" value=\"Abschliessen\" onclick=\"shrinkSelectionDiv(event, 'locationselection2', true)\" style=\"width : 219px\"></td></tr>");//AK 20160826
			
			html.append("\n</table>");
			
			//html.append("\n" + formManager.getCustomCheckbox("false", "false", false, cbimageselected, cbimage, "Alle abwählen", null, "customcheckbox",  "checkAllLocations") + "");
			
			//html.append("<input class=\"nodebutton\" type=\"button\" value=\"Fertig\" onclick=\"shrinkSelectionDiv(event, 'locationselection2', true)\" style=\"width : 220px\">");
			
			
			/*
			html.append("<table style=\"float : right;\"><tr><td><a href=\"javascript:checkAllLocations('true')\">Alle auswählen</a></td><td><a href=\"javascript:checkAllLocations('false')\">Alle abwählen</a></td>");
			html.append("<td style=\"color : black;\"><a href=\"javascript:shrinkSelectionDiv(null, 'locationselection2', true)\">Fertig</a></td></tr></table>");
			*/
			html.append("</div>");
			html.append("</div>");
			

			

			
			//html.append("\n<img id=\"map2\" class=\"map\" src=\"" + ocs.getBaseURL(request) + "/opencommunity/websites/1/images/map.png\">");
			html.append("\n<div id=\"map-svg-container\">");
			if(embedded){
				html.append("\n<object id=\"map-svg\" data=\"/res/map.svg\" type=\"image/svg+xml\" onload=\"setMapFillings()\"></object>");
			}
			else{
				html.append("\n<object id=\"map-svg\" data=\"/res/map.svg\" type=\"image/svg+xml\" onload=\"setMapFillings()\"></object>");
			}
			
			
			if(embedded){
				html.append("<div style=\"position : relative; width : 300px; left : -22px; top : 50px;\">");
				html.append("<table>");
				html.append("<tr><td style=\"color : white;\">Nachname</td><td><input name=\"familyname\" onkeyup=\"detailSearch()\"></td></tr>");
				html.append("<tr><td style=\"color : white;\">Vorname</td><td><input name=\"firstname\" onkeyup=\"detailSearch()\"></td></tr>");
				html.append("<tr><td style=\"color : white;\">Inserattitel</td><td><input name=\"title\" onkeyup=\"detailSearch()\"></td></tr>");
				html.append("<tr><td style=\"color : white;\">Beschreibung</td><td><input name=\"description\" onkeyup=\"detailSearch()\"></td></tr>");
				html.append("</table>");
				html.append("</div>");
			}
			
			html.append("\n</form>");
			
			html.append("\n</div>");
			//html.append("\n<img id=\"map2\" width=\225\" class=\"map\" src=\"" + ocs.getBaseURL(request) + "/opencommunity/websites/1/images/map.svg\">");
			
			//html.append("</div>");
			
			//html.append("<div id=\"categorylist2\">");
			
			cnt = 1;
				

			
			//html.append("\n</div>");  //end of criteria
			
			return html.toString();
		
		
	}
	public String getMemberAdSearchForm(ApplicationContext context, OrganisationMember om, boolean embedded){
		StringBuilder html = new StringBuilder();
		
		WebApplicationContext webcontext= (WebApplicationContext)context;
		
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
		
		HttpServletRequest request = webcontext.getRequest();
		
		Object oMode = userSession.get("mode");
		String mode = null;
		if(oMode instanceof String[]){
			if(((String[])oMode).length > 0){
				mode = ((String[])oMode)[0];
			}
		}
		else if(oMode instanceof String){
			mode = (String)oMode;
		}
		if(context.hasProperty("mode")){
			mode = context.getString("mode");
			userSession.put("mode", mode);
		}
		
				
		if(mode != null && mode.equals("searchresults")){
			
			String category = (String)userSession.get("category");
			MemberAdCategory mac = (MemberAdCategory)getObjectByName("MemberAdCategory", category);
			
			boolean isoffer = true;
			boolean isrequest = true;
			boolean tandemonly = false;
		
			html.append("\n<div id=\"resultlist\">");
			
			html.append(getSearchResults(ocs, om, mac, userSession, request, isrequest, isoffer, tandemonly, embedded));
			
			html.append("</div>");
			
		}
		
		return html.toString();
	}
	public String getMemberAdSearchForm2(ApplicationContext context, OrganisationMember om, boolean embedded){

		WebApplicationContext webcontext= (WebApplicationContext)context;
		
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
		
		HttpServletRequest request = webcontext.getRequest();
		
		


		ch.opencommunity.server.HTMLForm formManager = ocs.getFormFactory();

		StringBuilder html = new StringBuilder();
		
		Object oMode = userSession.get("mode");
		String mode = null;
		if(oMode instanceof String[]){
			if(((String[])oMode).length > 0){
				mode = ((String[])oMode)[0];
			}
		}
		else if(oMode instanceof String){
			mode = (String)oMode;
		}
		if(context.hasProperty("mode")){
			mode = context.getString("mode");
			userSession.put("mode", mode);
		}
		
		ocs.logAccess("mode: " + mode);
		
		if(mode != null && mode.equals("searchresults")){
			
			boolean isoffer = true;
			boolean isrequest = true;
			boolean tandemonly = false;
			
			int cntoffers = 0;
			int cntrequests = 0;
			
			String category = (String)userSession.get("category");
			
			String location = (String)userSession.get("location");
			

			
			MemberAdCategory mac = (MemberAdCategory)getObjectByName("MemberAdCategory", category);
			
			String cbimageselected = "/res/icons/cbimageselected.png";
			String cbimage = "/res/icons/cbimage.png";
						


			if(embedded){
				html.append("\n<div id=\"criterias\" style=\"\">");
			}
			else{
				html.append("\n<div id=\"criterias\" style=\"max-height : 800px;\">");
			}
			

			
			html.append("<div id=\"categoryselection\" onclick=\"expandSelectionDiv(event, \'categoryselection2\')\" onmouseover=\"highlightSelection(this.id, true)\" onmouseout=\"highlightSelection(this.id, false)\">");
			html.append("<input type=\"hidden\" id=\"selectedcategorytitle\" value=\"" + mac + "\">");
			if(mac != null){
				html.append("<span id=\"selectedcategory\">" + mac + "</span><br>");
			}
			else{
				html.append("<span id=\"selectedcategory\">Was? Rubrik wählen</span><br>");
			}		

			html.append("<div id=\"categoryselection2\">");
			for(BasicClass bc :  getObjects("MemberAdCategory")){
				if(embedded){
					if(mac != null && mac.getName().equals(bc.getName())){
						html.append("<a href=\"javascript:getNextNode('category=" + bc.getName() + "')\" style=\"color : red\">" + bc + "</a><br>");
					}
					else{
						html.append("<a href=\"javascript:getNextNode('category=" + bc.getName() + "')\" style=\"color : black\">" + bc + "</a><br>");
					}					
				}
				else{
					if(mac != null && mac.getName().equals(bc.getName())){
						html.append("<a href=\"javascript:reloadSearch(" + bc.getName() + ")\" style=\"color : red\">" + bc + "</a><br>");
					}
					else{
						html.append("<a href=\"javascript:reloadSearch(" + bc.getName() + ")\" style=\"color : black\">" + bc + "</a><br>");
					}
				}
			}			
			html.append("</div>");
			html.append("</div>");
			
			
			
			
			html.append("<form id=\"searchadform\" name=\"searchadform\">");
			html.append("\n<div class=\"profilemenuitem\" style=\"background :url(\'" + ocs.getBaseURL(request) + "/opencommunity/websites/1/images/bg_rubriken_top.png\'); height : 40px;\"></div>");
			
			
			
			html.append("\n<div class=\"profilemenuitem\" style=\"font-size : 16px; font-family : Museo500Regular; height : 40px;\"><img class=\"catpicto2\" src=\"images/" + mac.getName() + "_active.png\">verfeinern</div>");
			if(mac != null){
				
				html.append("<div style=\"max-height : 500px; overflow : auto;\">");
				
				for(BasicClass bc : mac.getObjects("FieldDefinition")){
					FieldDefinition fd = (FieldDefinition)bc;
					if(!fd.getBoolean("TandemOnly")){
						for(Object o : fd.getCodeList()){
							html.append("\n<div class=\"profilemenuitem\">");
							ConfigValue cv = (ConfigValue)o;
							String label = cv.getLabel();
							String[] args = label.split("/");
							String helptext = null;
							if(args.length==2){
								label = args[0];
								helptext = args[1];	
							}
							if(userSession.get(fd.getName() + "_" + cv.getValue()) == null){
								//String id, String value, boolean selected, String imageselected, String image, String label, String name, String classname
								//html.append( formManager.getCustomCheckbox(fd.getName() + "_" + cv.getValue(), "true", true, cbimageselected, cbimage, label , null, "customcheckbox2", "detailSearch", helptext));	
								html.append( formManager.getCustomCheckbox(fd.getName() + "_" + cv.getValue(), "false", false, cbimage, cbimage, label , null, "customcheckbox2", "detailSearch", helptext));	
							}
							else{
							
								if(((String[])userSession.get(fd.getName() + "_" + cv.getValue()))[0].equals("true")){
									html.append( formManager.getCustomCheckbox(fd.getName() + "_" + cv.getValue(), "true", true, cbimageselected, cbimage, label, null, "customcheckbox2", "detailSearch", helptext));	
								}
								else{
									html.append( formManager.getCustomCheckbox(fd.getName() + "_" + cv.getValue(), "false", true, cbimage, cbimage, label, null, "customcheckbox2", "detailSearch", helptext));	
								}
							}
							html.append("</div>");
						}

						html.append("<div style=\"background : white;\"><div style=\"margin-left : 8px; margin-right : 8px; background : white; border-bottom : 1px solid #979797;\"> </div></div>");
					}
				}
				html.append("</div>");
				
				if(!mac.getBoolean("RequestOnly")){
					if(userSession.get("isoffer") == null || ((String[])userSession.get("isoffer"))[0].equals("true")){
						html.append( "<div class=\"profilemenuitem\">" + formManager.getCustomCheckbox("isoffer", "true", true, cbimageselected, cbimage, "Angebot", null, "customcheckbox2", "detailSearch") + "</div>");
					}
					else{
						html.append( "<div class=\"profilemenuitem\">" + formManager.getCustomCheckbox("isoffer", "false", false, cbimage, cbimage, "Angebot", null, "customcheckbox2", "detailSearch") + "</div>");
						isoffer = false;
					}
					if(userSession.get("isrequest") == null || ((String[])userSession.get("isrequest"))[0].equals("true")){
						html.append( "<div class=\"profilemenuitem\">" + formManager.getCustomCheckbox("isrequest", "true", true, cbimageselected, cbimage, "Nachfrage", null, "customcheckbox2", "detailSearch") + "</div>");
					}
					else{
						html.append( "<div class=\"profilemenuitem\">" + formManager.getCustomCheckbox("isrequest", "false", false, cbimage , cbimage, "Nachfrage", null, "customcheckbox2", "detailSearch") + "</div>");
						isrequest = false;
					}
					if(mac.getBoolean("TandemAllowed")){
						if(userSession.get("tandemonly") == null || ((String[])userSession.get("tandemonly"))[0].equals("false")){
							html.append( "<div class=\"profilemenuitem\">" + formManager.getCustomCheckbox("tandemonly", "false", false, cbimage , cbimage, "Nur Tandem", null, "customcheckbox2", "detailSearch") + "</div>");	
						}
						else{
							html.append( "<div class=\"profilemenuitem\">" + formManager.getCustomCheckbox("tandemonly", "true", true, cbimageselected , cbimage, "Nur Tandem", null, "customcheckbox2", "detailSearch") + "</div>");	
							tandemonly = true;
						}
					}
				}
				
			}	
			html.append("\n<div class=\"profilemenuitem\" style=\"background :url(\'images/bg_rubriken_bottom.png\'); height: 28px;\"></div>");

			
			
			html.append("<div id=\"locationselection\" onclick=\"expandSelectionDiv(event, \'locationselection2\')\">");
			html.append("Wo? Quartier wählen<br>");

			html.append("<div id=\"locationselection2\">");
			
			html.append("<table>"); //AK 20160826
			html.append("<tr><td colspan=2>");
			html.append("\n" + formManager.getCustomCheckbox("true", "false", false, cbimageselected, cbimage, "Alle auswählen", null, "customcheckbox",  "checkAllLocations") + "");
			
			//html.append("</td><td>");
			
			html.append("</td></tr><tr><td colspan=2>");
			
			html.append("\n" + formManager.getCustomCheckbox("false", "false", false, cbimageselected, cbimage, "Alle abwählen", null, "customcheckbox",  "checkAllLocations") + "");
			html.append("</td></tr>");
			
			int cnt = 0;
			int cnt2 = 0;
			

			for(ConfigValue cv : ocs.getGeoObjects()){
				

				String label = "" + cv.getValue();
				
				label = label.replace("* ", "");
				
				cnt++;
				cnt2++;
				
				if(cnt2==3){
					
					html.append("</td></tr><tr><td id=\"dc_" + cv.getValue() + "\" class=\"locationcell\" onmouseover=\"showHelp2(event, '" + cv.getLabel() + "')\" onmouseout=\"hideHelp()\">");
				}
				else{
					html.append("</td><td id=\"dc_" + cv.getValue() + "\" class=\"locationcell\" onmouseover=\"showHelp2(event, '" + cv.getLabel() + "')\" onmouseout=\"hideHelp()\">");
				}
				
				if(cnt == 1){
					html.append("<tr><td id=\"dc_" + cv.getValue() + "\" class=\"locationcell\" onmouseover=\"showHelp2(event, '" + cv.getLabel() + "')\" onmouseout=\"hideHelp()\">");
				}

				
				
				if(userSession.get("location_" + cv.getValue()) != null && ((String[])userSession.get("location_" + cv.getValue()))[0].equals("true")){
					//html.append("\n" + formManager.getCustomCheckbox("location_" + cv.getValue(), "true", true, cbimageselected, cbimage, cv.getValue() + " " +  cv.getLabel()) + "<br>");
					html.append("\n" + formManager.getCustomCheckbox("location_" + cv.getValue(), "true", true, cbimageselected, cbimage, label) + "");
				}
				else{
					//html.append("\n" + formManager.getCustomCheckbox("location_" + cv.getValue(), "false", false, cbimage, cbimage, cv.getValue() + " " +  cv.getLabel()) + "<br>");
					html.append("\n" + formManager.getCustomCheckbox("location_" + cv.getValue(), "false", false, cbimage, cbimage, label) + "");
				}
				/*
				if(cnt2==2){
					

					
					html.append("</td></tr><tr><td id=\"dc_" + cv.getValue() + "\" class=\"locationcell\" onmouseover=\"showHelp2(event, '" + cv.getLabel() + "')\" onmouseout=\"hideHelp()\">");
				}
				else{
					html.append("</td><td id=\"dc_" + cv.getValue() + "\" class=\"locationcell\" onmouseover=\"showHelp2(event, '" + cv.getLabel() + "')\" onmouseout=\"hideHelp()\">");
				}
				
				*/
				//if(cnt2==3){
				if(cnt2==3){
					cnt2 = 1;	
				}
			}
			
			html.append("</td></tr>");
			
			html.append("<tr><td colspan=2><input class=\"nodebutton\" type=\"button\" value=\"Abschliessen\" onclick=\"shrinkSelectionDiv(event, 'locationselection2', true)\" style=\"width : 219px\"></td></tr>");//AK 20160826
			
			html.append("</table>");
			
			//html.append("\n" + formManager.getCustomCheckbox("false", "false", false, cbimageselected, cbimage, "Alle abwählen", null, "customcheckbox",  "checkAllLocations") + "");
			
			//html.append("<input class=\"nodebutton\" type=\"button\" value=\"Fertig\" onclick=\"shrinkSelectionDiv(event, 'locationselection2', true)\" style=\"width : 220px\">");
			
			
			/*
			html.append("<table style=\"float : right;\"><tr><td><a href=\"javascript:checkAllLocations('true')\">Alle auswählen</a></td><td><a href=\"javascript:checkAllLocations('false')\">Alle abwählen</a></td>");
			html.append("<td style=\"color : black;\"><a href=\"javascript:shrinkSelectionDiv(null, 'locationselection2', true)\">Fertig</a></td></tr></table>");
			*/
			html.append("</div>");
			html.append("</div>");
			

			

			
			//html.append("\n<img id=\"map2\" class=\"map\" src=\"" + ocs.getBaseURL(request) + "/opencommunity/websites/1/images/map.png\">");
			html.append("<div style=\"position : relative; width : 300px; left : -22px; top : -30px;\">");
			if(embedded){
				html.append("<object id=\"map-svg\" style=\"width : 275px;\" data=\"/res/map.svg\" type=\"image/svg+xml\" onload=\"setMapFillings()\"></object>");
			}
			else{
				html.append("<object id=\"map-svg\" style=\"width : 275px;\" data=\"/res/map.svg\" type=\"image/svg+xml\" onload=\"setMapFillings()\"></object>");
			}
			html.append("</div>");
			
			if(embedded){
				html.append("<div style=\"position : relative; width : 300px; left : -22px; top : 50px;\">");
				html.append("<table>");
				html.append("<tr><td style=\"color : white;\">Nachname</td><td><input name=\"familyname\" onkeyup=\"detailSearch()\"></td></tr>");
				html.append("<tr><td style=\"color : white;\">Vorname</td><td><input name=\"firstname\" onkeyup=\"detailSearch()\"></td></tr>");
				html.append("<tr><td style=\"color : white;\">Inserattitel</td><td><input name=\"title\" onkeyup=\"detailSearch()\"></td></tr>");
				html.append("<tr><td style=\"color : white;\">Beschreibung</td><td><input name=\"description\" onkeyup=\"detailSearch()\"></td></tr>");
				html.append("</table>");
				html.append("</div>");
			}
			
			html.append("</form>");
			//html.append("\n<img id=\"map2\" width=\225\" class=\"map\" src=\"" + ocs.getBaseURL(request) + "/opencommunity/websites/1/images/map.svg\">");
			
			//html.append("</div>");
			
			//html.append("<div id=\"categorylist2\">");
			
			cnt = 1;
				
			/*
			for(BasicClass bc :  getObjects("MemberAdCategory")){
			
				
				html.append("\n<div>");
				
				html.append("<a class=\"category\" href=\"" + ocs.getBaseURL(request) + "/opencommunity/servlet?action=searchads&category=" + bc.getName() + "\"><img class=\"catpicto\" src=\"" + ocs.getBaseURL(request) + "/opencommunity/websites/1/images/picto/" + cnt + ".png\">");
				
				html.append("<span>" + bc.getString("Title") + "</span></a>");
				
				html.append("</div>");
				
				cnt++;
				
			}	
			*/
			
			html.append("\n</div>");  //end of criteria
			
			if(embedded){
				html.append("\n<div id=\"resultlist\" style=\"position : relative;\">");
			}
			else{
				html.append("\n<div id=\"resultlist\">");
			}
					
			html.append(getSearchResults(ocs, om, mac, userSession, request, isrequest, isoffer, tandemonly, embedded));
			
			html.append("</div>");


		
		}
		else{
			
			for(ConfigValue cv : ocs.getGeoObjects()){ //AK 20160627, gespeicherte Ortsselektion löschen
				String label = cv.getValue() + " " + cv.getLabel();
				label = label.replace("* ", "");
				userSession.put("location_" + cv.getValue(), null);
			}
			
			String location = (String)userSession.get("location");
			
			String cbimageselected = "images/cbimageselected.png";
			String cbimage = "images/cbimage.png";
			
			html.append("\n<style type=\"text/css\">");
			//html.append("\n#footer{ top : 632px }");
			
			//html.append("\n#main{ position : absolute;  width : 960px; left : 50%; margin-left: -512px; padding-left : 32px; padding-right : 32px; top : 0px; height : 900px; background : #363D45; } ");
			html.append("\n</style>");

			int imageindex = random.nextInt(18 - 1 + 1) + 1;
			
			//html.append("<div id=\"teaser1\" style=\"background-image : url(\'" + ocs.getBaseURL(request) + "/opencommunity/websites/1/images/background/" + imageindex + ".png\');\">");
			
			//html.append("<div id=\"teaser1\" style=\"background-image : url(\'images/bg" + imageindex + ".png\');\">"); //AK 20170130. move icons to /res
			html.append("<div id=\"teaser1\" style=\"background-image : url(\'/res/background/bg" + imageindex + ".png\');\">");
			
			//html.append("<img src=\"" + ocs.getBaseURL(request) + "/opencommunity/websites/1/images/searchheader.png\">");
			
			/*
			html.append("<form name=\"searchadform\" action=\"" + ocs.getBaseURL(request) + "/opencommunity/servlet\">");
			html.append("<input type=\"hidden\" name=\"action\" value=\"searchads\">");
			html.append("<div id=\"catselection1\">");
			html.append("<select name=\"category\" id=\"category\" class=\"selectbig\">");
			html.append("<option value=\"0\">Was? Rubrik wählen</option>");
			for(BasicClass bc :  getObjects("MemberAdCategory")){
				html.append("<option value=\"" + bc.getName() + "\">" + bc.getString("Title") + "</option>");			
			}
			html.append("</select>");
			
			//html.append("<br><br><select class=\"selectbig\" name=\"location\" id=\"location\">");
			html.append("<div id=\"locationselection\" style=\"\" onclick=\"expandSelectionDiv(this.id)\">");
			html.append("Wo? Quartier wählen<br>");
			for(ConfigValue cv : ocs.getGeoObjects()){
				if(userSession.get("location_" + cv.getValue()) != null && ((String[])userSession.get("location_" + cv.getValue()))[0].equals("true")){
					//html.append(formManager.getCustomCheckbox("location_" + cv.getValue(), "true", true, cbimageselected, cbimage, cv.getValue() + " " +  cv.getLabel()) + "");
					html.append(formManager.getCustomCheckbox("location_" + cv.getValue(), "false", false, cbimage, cbimage, cv.getValue() + " " +  cv.getLabel()) + "");  //auf Wunsch des Kunden, 
				}
				else{
					html.append(formManager.getCustomCheckbox("location_" + cv.getValue(), "false", false, cbimage, cbimage, cv.getValue() + " " +  cv.getLabel()) + "");
				}
			}
			//html.append("<img src=\"images/closeWizard.png\" style=\"float : right;\">");
			html.append("<table style=\"float : right;\"><tr><td><a href=\"javascript:checkAllLocations('true')\">Alle auswählen</a></td><td><a href=\"javascript:checkAllLocations('false')\">Alle abwählen</a></td>");
			html.append("<td style=\"color : black;\">Schliessen<img src=\"images/delete_small.png\" style=\"float : right;\" onclick=\"shrinkSelectionDiv(event, 'locationselection')\"></td></tr></table>");
			html.append("</div>");
			
			html.append("<table style=\"position : absolute; top: 92px;width : 415px; border-spacing: 0; border-collapse: collapse;\"><tr>");
			
			//String id, String value, boolean selected, String imageselected, String image, String label
			
		
			html.append("<td>" + formManager.getCustomCheckbox("isoffer", "true", true, cbimageselected, cbimage, "Angebot", null, "catlabel2") + "</td>");
			html.append("<td>" + formManager.getCustomCheckbox("isrequest", "true", true, cbimageselected, cbimage, "Nachfrage", null, "catlabel2") + "</td>");

			//html.append("<td><input type=\"submit\" class=\"button1\" value=\"finden!\" onclick=\"findads()\"></td>"); 
			html.append("<td style=\"text-align : right;\"><input type=\"submit\" class=\"button1\" value=\"findenl!\"></td>"); 
			html.append("</tr></table>");
			html.append("</div>");
			html.append("</form>");
			
			*/
	
			html.append("</div>");
			
			html.append("<div id=\"teaser2\">");
			
			Object o = ocs.getNewsAdministration().getObject("CurrentNewsMessage");
			if(o instanceof NewsMessage){
				
				html.append("<p style=\"margin-bottom : 5px; color : black ; font-size : 18px;\">Aktuell");
				
				NewsMessage currentNewsMessage = (NewsMessage)o;
				//html.append("<div class=\"newsmessage\">");
				html.append("<p style=\"margin-top : 0px; margin-bottom : 0px; color : #6CBB16; font-size : 18px; line-height : 1;\">" + DateConverter.sqlToShortDisplay(currentNewsMessage.getString("DateStart"), true) + " " + currentNewsMessage.getString("Title") + "</b>");
				html.append("<p style=\"margin-bottom : 0px; color : black;\">" + currentNewsMessage.getString("Description"));
				String url = currentNewsMessage.getString("URL");
				if(url.length() > 0){
					html.append("<br><a href=\"" + url + "\" target=\"_blank\" style=\"color : black;\">" + url + "</a>");
				}
				//html.append("</div>");
			}
			else{
				ObjectCollection result = new ObjectCollection("Result", "*");
				
				String sql = "SELECT t1.*, t4.ZipCode, t4.City, t5.DateOfBirth, t5.Sex, t6.ID AS MAC, t6.Title AS Category, t7.Content AS AdditionalInfo FROM MemberAd AS t1";
				sql += " LEFT JOIN OrganisationMember AS t2 ON t1.OrganisationMemberID=t2.ID";
				sql += " LEFT JOIN Person AS t3 ON t2.Person=t3.ID";
				sql += " LEFT JOIN Address AS t4 ON t4.PersonID=t3.ID";
				sql += " LEFT JOIN Identity AS t5 ON t5.PersonID=t3.ID";
				sql += " LEFT JOIN MemberAdCategory AS t6 ON t1.Template=t6.ID";
				sql += " LEFT JOIN Note AS t7 ON t1.ID=t7.MemberAdID";
				
				sql += " WHERE Priority=1 AND DateModified IS NOT NULL ORDER BY DateModified DESC LIMIT 1";
				
				ocs.queryData(sql, result);
				
				int currentyear = DateConverter.getCurrentYear();
				
				if(result.getObjects().size() > 0){
					Record record = (Record)result.getObjectByIndex(0);
					
					String id = record.getString("ID");
					
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
					
					html.append("<p style=\"margin-bottom : 5px; color : black ; font-size : 18px;\">Inserat der Woche");
					html.append("<p style=\"margin-top : 0px; margin-bottom : 0px; color : #6CBB16; font-size : 18px; line-height : 1;\"><img style=\"border : 0px\" src=\"images/" + record.getString("MAC") + "_active.png\">" + record.getString("CATEGORY"));
					html.append("<p style=\"margin-top : 0px; color : #6CBB16; font-size : 16px;\">" + record.getString("ZIPCODE")+ " " + record.getString("CITY"));
					html.append("<br>" + sex2 + " (" + age + ") " +  record.getString("TITLE"));
					html.append("<p style=\"margin-bottom : 0px; color : black;\">");
					if(record.getString("ADDITIONALINFO") != null && record.getString("ADDITIONALINFO").length() > 0){
						html.append(record.getString("ADDITIONALINFO"));
					}
					else{
						html.append(record.getString("DESCRIPTION"));
					}
					html.append("<br><a style=\"color : black;\" href=\"" + ocs.getBaseURL(request) + "/servlet?action=searchads&category=" + record.getString("MAC") + "&memberadid=" + id + "\">(Mehr)</a>");
				}
			}
			
			html.append("</div>");
	
			html.append("\n<div id=\"memberaddentryform\" style=\"position : absolute; left : 32px; top : 344px; height : 330px; width : 960px;\">");
			
			//html.append("<h4>Marktplatz</h4>");
			
			//html.append("Sie können hier jederzeit über unsern Markt schlendern und unverbindlich Angebote und Nachfragen anschauen, bevor Sie sich entscheiden, ob Sie selber auf dem Markt mitmachen wollen. Wenn Sie aktiv mitmachen wollen, müssen Sie sich <a href=\"javascript:createProcess(\'ch.opencommunity.process.MemberRegistration\')\">registrieren</a> und die Regeln des NachbarNET akzeptieren."); 
	
			//html.append("<input type=\"button\" onclick=\"createMemberAdd()\" value=\"Angebot platzieren\"><p>");
			
			//html.append("<h5>Rubriken</h5>");
			
			html.append("\n<div class=\"clearfix\">");
			
			html.append("<form name=\"searchform\" id=\"searchform\">");
			
			int left = 0;
			int top = 0;
			
			int cnt = 0;
			int cnt2 = 1;
			
			for(BasicClass bc :  getObjects("MemberAdCategory")){
			
				html.append("\n<div class=\"category\" style=\"left : " + (left * 245) + "px; top : " + (top * 56) + "px; width 225px;\">");
				
				//html.append("<a class=\"category\" href=\"" + ocs.getBaseURL(request) + "/opencommunity/servlet?action=searchads&category=" + bc.getName() + "\"><img class=\"catpicto\" src=\"" + ocs.getBaseURL(request) + "/opencommunity/websites/1/images/picto/" + cnt2 + ".png\">");
				html.append("<a class=\"category\" href=\"servlet.srv?action=searchads&category=" + bc.getName() + "\"><img class=\"catpicto\" src=\"/res/icons/" + cnt2 + ".png\">");
				
				html.append("<span>" + bc.getString("Title") + "</span></a>");
				
				html.append("</div>");
			
				left++;
				if(left == 4){
					left = 0;
					top++;
				}
				cnt2++;
			}
			
			/*
			html.append("\n<div id=\"map\">");
			html.append("<object id=\"map-svg\" style=\"width : 275px;\" data=\"" + ocs.getBaseURL(request) + "/opencommunity/websites/1/images/map.svg\" type=\"image/svg+xml\" onload=\"setMapFillings()\"></object>");
			html.append("\n</div>");
			*/
			
			html.append("\n</div>");
		
		}
		
		

		

		
		return html.toString();
	}
	public String getMemberAdSearchForm3(ApplicationContext context, OrganisationMemberController omc, OrganisationMember om, boolean embedded){

		WebApplicationContext webcontext= (WebApplicationContext)context;
		
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
		
		HttpServletRequest request = webcontext.getRequest();
		
		


		ch.opencommunity.server.HTMLForm formManager = ocs.getFormFactory();

		StringBuilder html = new StringBuilder();
		
		Object oMode = userSession.get("mode");
		String mode = null;
		if(oMode instanceof String[]){
			if(((String[])oMode).length > 0){
				mode = ((String[])oMode)[0];
			}
		}
		else if(oMode instanceof String){
			mode = (String)oMode;
		}
		if(context.hasProperty("mode")){
			mode = context.getString("mode");
			userSession.put("mode", mode);
		}
		
		ocs.logAccess("mode: " + mode);
		
		if(mode != null && mode.equals("searchresults")){
			
			boolean isoffer = true;
			boolean isrequest = true;
			boolean tandemonly = false;
			
			int cntoffers = 0;
			int cntrequests = 0;
			
			String category = (String)userSession.get("category");
			
			String location = (String)userSession.get("location");
			

			
			MemberAdCategory mac = (MemberAdCategory)getObjectByName("MemberAdCategory", category);
			
			String cbimageselected = "/res/icons/cbimageselected.png";
			String cbimage = "/res/icons/cbimage.png";
						


			if(embedded){
				html.append("\n<div id=\"criterias\" style=\"\">");
			}
			else{
				html.append("\n<div id=\"criterias\" style=\"max-height : 800px;\">");
			}
			

			
			html.append("<div id=\"categoryselection\" onclick=\"expandSelectionDiv(event, \'categoryselection2\')\" onmouseover=\"highlightSelection(this.id, true)\" onmouseout=\"highlightSelection(this.id, false)\">");
			html.append("<input type=\"hidden\" id=\"selectedcategorytitle\" value=\"" + mac + "\">");
			if(mac != null){
				html.append("<span id=\"selectedcategory\">" + mac + "</span><br>");
			}
			else{
				html.append("<span id=\"selectedcategory\">Was? Rubrik wählen</span><br>");
			}		

			html.append("<div id=\"categoryselection2\">");
			for(BasicClass bc :  getObjects("MemberAdCategory")){
				if(embedded){
					if(mac != null && mac.getName().equals(bc.getName())){
						html.append("<a href=\"javascript:onAction('" + omc.getPath() + "','setcategory','','category=" + bc.getName() + "')\" style=\"color : red\">" + bc + "</a><br>");
					}
					else{
						html.append("<a href=\"javascript:onAction('" + omc.getPath() + "','setcategory','','category=" + bc.getName() + "')\" style=\"color : black\">" + bc + "</a><br>");
					}					
				}
				else{
					if(mac != null && mac.getName().equals(bc.getName())){
						html.append("<a href=\"javascript:onAction('" + omc.getPath() + "','setcategory','','category=" + bc.getName() + "')\" style=\"color : red\">" + bc + "</a><br>");
					}
					else{
						html.append("<a href=\"javascript:reloadSearch(" + bc.getName() + ")\" style=\"color : black\">" + bc + "</a><br>");
					}
				}
			}			
			html.append("</div>");
			html.append("</div>");
			
			
			
			
			html.append("<form id=\"searchadform\" name=\"searchadform\">");
			html.append("\n<div class=\"profilemenuitem\" style=\"background :url(\'" + ocs.getBaseURL(request) + "/opencommunity/websites/1/images/bg_rubriken_top.png\'); height : 40px;\"></div>");
			
			
			
			html.append("\n<div class=\"profilemenuitem\" style=\"font-size : 16px; font-family : Museo500Regular; height : 40px;\"><img class=\"catpicto2\" src=\"images/" + mac.getName() + "_active.png\">verfeinern</div>");
			if(mac != null){
				
				html.append("<div style=\"max-height : 500px; overflow : auto;\">");
				
				for(BasicClass bc : mac.getObjects("FieldDefinition")){
					FieldDefinition fd = (FieldDefinition)bc;
					if(!fd.getBoolean("TandemOnly")){
						for(Object o : fd.getCodeList()){
							html.append("\n<div class=\"profilemenuitem\">");
							ConfigValue cv = (ConfigValue)o;
							String label = cv.getLabel();
							String[] args = label.split("/");
							String helptext = null;
							if(args.length==2){
								label = args[0];
								helptext = args[1];	
							}
							if(userSession.get(fd.getName() + "_" + cv.getValue()) == null){
								//String id, String value, boolean selected, String imageselected, String image, String label, String name, String classname
								//html.append( formManager.getCustomCheckbox(fd.getName() + "_" + cv.getValue(), "true", true, cbimageselected, cbimage, label , null, "customcheckbox2", "detailSearch", helptext));	
								html.append( formManager.getCustomCheckbox(fd.getName() + "_" + cv.getValue(), "false", false, cbimage, cbimage, label , null, "customcheckbox2", "detailSearch", helptext));	
							}
							else{
							
								if(((String[])userSession.get(fd.getName() + "_" + cv.getValue()))[0].equals("true")){
									html.append( formManager.getCustomCheckbox(fd.getName() + "_" + cv.getValue(), "true", true, cbimageselected, cbimage, label, null, "customcheckbox2", "detailSearch", helptext));	
								}
								else{
									html.append( formManager.getCustomCheckbox(fd.getName() + "_" + cv.getValue(), "false", true, cbimage, cbimage, label, null, "customcheckbox2", "detailSearch", helptext));	
								}
							}
							html.append("</div>");
						}

						html.append("<div style=\"background : white;\"><div style=\"margin-left : 8px; margin-right : 8px; background : white; border-bottom : 1px solid #979797;\"> </div></div>");
					}
				}
				html.append("</div>");
				
				if(!mac.getBoolean("RequestOnly")){
					if(userSession.get("isoffer") == null || ((String[])userSession.get("isoffer"))[0].equals("true")){
						html.append( "<div class=\"profilemenuitem\">" + formManager.getCustomCheckbox("isoffer", "true", true, cbimageselected, cbimage, "Angebot", null, "customcheckbox2", "detailSearch") + "</div>");
					}
					else{
						html.append( "<div class=\"profilemenuitem\">" + formManager.getCustomCheckbox("isoffer", "false", false, cbimage, cbimage, "Angebot", null, "customcheckbox2", "detailSearch") + "</div>");
						isoffer = false;
					}
					if(userSession.get("isrequest") == null || ((String[])userSession.get("isrequest"))[0].equals("true")){
						html.append( "<div class=\"profilemenuitem\">" + formManager.getCustomCheckbox("isrequest", "true", true, cbimageselected, cbimage, "Nachfrage", null, "customcheckbox2", "detailSearch") + "</div>");
					}
					else{
						html.append( "<div class=\"profilemenuitem\">" + formManager.getCustomCheckbox("isrequest", "false", false, cbimage , cbimage, "Nachfrage", null, "customcheckbox2", "detailSearch") + "</div>");
						isrequest = false;
					}
					if(mac.getBoolean("TandemAllowed")){
						if(userSession.get("tandemonly") == null || ((String[])userSession.get("tandemonly"))[0].equals("false")){
							html.append( "<div class=\"profilemenuitem\">" + formManager.getCustomCheckbox("tandemonly", "false", false, cbimage , cbimage, "Nur Tandem", null, "customcheckbox2", "detailSearch") + "</div>");	
						}
						else{
							html.append( "<div class=\"profilemenuitem\">" + formManager.getCustomCheckbox("tandemonly", "true", true, cbimageselected , cbimage, "Nur Tandem", null, "customcheckbox2", "detailSearch") + "</div>");	
							tandemonly = true;
						}
					}
				}
				
			}	
			html.append("\n<div class=\"profilemenuitem\" style=\"background :url(\'images/bg_rubriken_bottom.png\'); height: 28px;\"></div>");

			
			
			html.append("<div id=\"locationselection\" onclick=\"expandSelectionDiv(event, \'locationselection2\')\">");
			html.append("Wo? Quartier wählen<br>");

			html.append("<div id=\"locationselection2\">");
			
			html.append("<table>"); //AK 20160826
			html.append("<tr><td colspan=2>");
			html.append("\n" + formManager.getCustomCheckbox("true", "false", false, cbimageselected, cbimage, "Alle auswählen", null, "customcheckbox",  "checkAllLocations") + "");
			
			//html.append("</td><td>");
			
			html.append("</td></tr><tr><td colspan=2>");
			
			html.append("\n" + formManager.getCustomCheckbox("false", "false", false, cbimageselected, cbimage, "Alle abwählen", null, "customcheckbox",  "checkAllLocations") + "");
			html.append("</td></tr>");
			
			int cnt = 0;
			int cnt2 = 0;
			

			for(ConfigValue cv : ocs.getGeoObjects()){
				

				String label = "" + cv.getValue();
				
				label = label.replace("* ", "");
				
				cnt++;
				cnt2++;
				
				if(cnt2==3){
					
					html.append("</td></tr><tr><td id=\"dc_" + cv.getValue() + "\" class=\"locationcell\" onmouseover=\"showHelp2(event, '" + cv.getLabel() + "')\" onmouseout=\"hideHelp()\">");
				}
				else{
					html.append("</td><td id=\"dc_" + cv.getValue() + "\" class=\"locationcell\" onmouseover=\"showHelp2(event, '" + cv.getLabel() + "')\" onmouseout=\"hideHelp()\">");
				}
				
				if(cnt == 1){
					html.append("<tr><td id=\"dc_" + cv.getValue() + "\" class=\"locationcell\" onmouseover=\"showHelp2(event, '" + cv.getLabel() + "')\" onmouseout=\"hideHelp()\">");
				}

				
				
				if(userSession.get("location_" + cv.getValue()) != null && ((String[])userSession.get("location_" + cv.getValue()))[0].equals("true")){
					//html.append("\n" + formManager.getCustomCheckbox("location_" + cv.getValue(), "true", true, cbimageselected, cbimage, cv.getValue() + " " +  cv.getLabel()) + "<br>");
					html.append("\n" + formManager.getCustomCheckbox("location_" + cv.getValue(), "true", true, cbimageselected, cbimage, label) + "");
				}
				else{
					//html.append("\n" + formManager.getCustomCheckbox("location_" + cv.getValue(), "false", false, cbimage, cbimage, cv.getValue() + " " +  cv.getLabel()) + "<br>");
					html.append("\n" + formManager.getCustomCheckbox("location_" + cv.getValue(), "false", false, cbimage, cbimage, label) + "");
				}
				/*
				if(cnt2==2){
					

					
					html.append("</td></tr><tr><td id=\"dc_" + cv.getValue() + "\" class=\"locationcell\" onmouseover=\"showHelp2(event, '" + cv.getLabel() + "')\" onmouseout=\"hideHelp()\">");
				}
				else{
					html.append("</td><td id=\"dc_" + cv.getValue() + "\" class=\"locationcell\" onmouseover=\"showHelp2(event, '" + cv.getLabel() + "')\" onmouseout=\"hideHelp()\">");
				}
				
				*/
				//if(cnt2==3){
				if(cnt2==3){
					cnt2 = 1;	
				}
			}
			
			html.append("</td></tr>");
			
			html.append("<tr><td colspan=2><input class=\"nodebutton\" type=\"button\" value=\"Abschliessen\" onclick=\"shrinkSelectionDiv(event, 'locationselection2', true)\" style=\"width : 219px\"></td></tr>");//AK 20160826
			
			html.append("</table>");
			
			//html.append("\n" + formManager.getCustomCheckbox("false", "false", false, cbimageselected, cbimage, "Alle abwählen", null, "customcheckbox",  "checkAllLocations") + "");
			
			//html.append("<input class=\"nodebutton\" type=\"button\" value=\"Fertig\" onclick=\"shrinkSelectionDiv(event, 'locationselection2', true)\" style=\"width : 220px\">");
			
			
			/*
			html.append("<table style=\"float : right;\"><tr><td><a href=\"javascript:checkAllLocations('true')\">Alle auswählen</a></td><td><a href=\"javascript:checkAllLocations('false')\">Alle abwählen</a></td>");
			html.append("<td style=\"color : black;\"><a href=\"javascript:shrinkSelectionDiv(null, 'locationselection2', true)\">Fertig</a></td></tr></table>");
			*/
			html.append("</div>");
			html.append("</div>");
			

			

			
			//html.append("\n<img id=\"map2\" class=\"map\" src=\"" + ocs.getBaseURL(request) + "/opencommunity/websites/1/images/map.png\">");
			html.append("<div style=\"position : relative; width : 300px; left : -22px; top : -30px;\">");
			if(embedded){
				html.append("<object id=\"map-svg\" style=\"width : 275px;\" data=\"/res/map.svg\" type=\"image/svg+xml\" onload=\"setMapFillings()\"></object>");
			}
			else{
				html.append("<object id=\"map-svg\" style=\"width : 275px;\" data=\"/res/map.svg\" type=\"image/svg+xml\" onload=\"setMapFillings()\"></object>");
			}
			html.append("</div>");
			
			if(embedded){
				html.append("<div style=\"position : relative; width : 300px; left : -22px; top : 50px;\">");
				html.append("<table>");
				html.append("<tr><td style=\"color : white;\">Nachname</td><td><input name=\"familyname\" onkeyup=\"detailSearch()\"></td></tr>");
				html.append("<tr><td style=\"color : white;\">Vorname</td><td><input name=\"firstname\" onkeyup=\"detailSearch()\"></td></tr>");
				html.append("<tr><td style=\"color : white;\">Inserattitel</td><td><input name=\"title\" onkeyup=\"detailSearch()\"></td></tr>");
				html.append("<tr><td style=\"color : white;\">Beschreibung</td><td><input name=\"description\" onkeyup=\"detailSearch()\"></td></tr>");
				html.append("</table>");
				html.append("</div>");
			}
			
			html.append("</form>");
			//html.append("\n<img id=\"map2\" width=\225\" class=\"map\" src=\"" + ocs.getBaseURL(request) + "/opencommunity/websites/1/images/map.svg\">");
			
			//html.append("</div>");
			
			//html.append("<div id=\"categorylist2\">");
			
			cnt = 1;
				
			/*
			for(BasicClass bc :  getObjects("MemberAdCategory")){
			
				
				html.append("\n<div>");
				
				html.append("<a class=\"category\" href=\"" + ocs.getBaseURL(request) + "/opencommunity/servlet?action=searchads&category=" + bc.getName() + "\"><img class=\"catpicto\" src=\"" + ocs.getBaseURL(request) + "/opencommunity/websites/1/images/picto/" + cnt + ".png\">");
				
				html.append("<span>" + bc.getString("Title") + "</span></a>");
				
				html.append("</div>");
				
				cnt++;
				
			}	
			*/
			
			html.append("\n</div>");  //end of criteria
			
			if(embedded){
				html.append("\n<div id=\"resultlist\" style=\"position : relative;\">");
			}
			else{
				html.append("\n<div id=\"resultlist\">");
			}
					
			html.append(getSearchResults(ocs, om, mac, userSession, request, isrequest, isoffer, tandemonly, embedded));
			
			html.append("</div>");


		
		}
		else{
			
			for(ConfigValue cv : ocs.getGeoObjects()){ //AK 20160627, gespeicherte Ortsselektion löschen
				String label = cv.getValue() + " " + cv.getLabel();
				label = label.replace("* ", "");
				userSession.put("location_" + cv.getValue(), null);
			}
			
			String location = (String)userSession.get("location");
			
			String cbimageselected = "images/cbimageselected.png";
			String cbimage = "images/cbimage.png";
			
			html.append("\n<style type=\"text/css\">");
			//html.append("\n#footer{ top : 632px }");
			
			//html.append("\n#main{ position : absolute;  width : 960px; left : 50%; margin-left: -512px; padding-left : 32px; padding-right : 32px; top : 0px; height : 900px; background : #363D45; } ");
			html.append("\n</style>");

			int imageindex = random.nextInt(18 - 1 + 1) + 1;
			
			//html.append("<div id=\"teaser1\" style=\"background-image : url(\'" + ocs.getBaseURL(request) + "/opencommunity/websites/1/images/background/" + imageindex + ".png\');\">");
			
			//html.append("<div id=\"teaser1\" style=\"background-image : url(\'images/bg" + imageindex + ".png\');\">"); //AK 20170130. move icons to /res
			html.append("<div id=\"teaser1\" style=\"background-image : url(\'/res/background/bg" + imageindex + ".png\');\">");
			
			//html.append("<img src=\"" + ocs.getBaseURL(request) + "/opencommunity/websites/1/images/searchheader.png\">");
			
			/*
			html.append("<form name=\"searchadform\" action=\"" + ocs.getBaseURL(request) + "/opencommunity/servlet\">");
			html.append("<input type=\"hidden\" name=\"action\" value=\"searchads\">");
			html.append("<div id=\"catselection1\">");
			html.append("<select name=\"category\" id=\"category\" class=\"selectbig\">");
			html.append("<option value=\"0\">Was? Rubrik wählen</option>");
			for(BasicClass bc :  getObjects("MemberAdCategory")){
				html.append("<option value=\"" + bc.getName() + "\">" + bc.getString("Title") + "</option>");			
			}
			html.append("</select>");
			
			//html.append("<br><br><select class=\"selectbig\" name=\"location\" id=\"location\">");
			html.append("<div id=\"locationselection\" style=\"\" onclick=\"expandSelectionDiv(this.id)\">");
			html.append("Wo? Quartier wählen<br>");
			for(ConfigValue cv : ocs.getGeoObjects()){
				if(userSession.get("location_" + cv.getValue()) != null && ((String[])userSession.get("location_" + cv.getValue()))[0].equals("true")){
					//html.append(formManager.getCustomCheckbox("location_" + cv.getValue(), "true", true, cbimageselected, cbimage, cv.getValue() + " " +  cv.getLabel()) + "");
					html.append(formManager.getCustomCheckbox("location_" + cv.getValue(), "false", false, cbimage, cbimage, cv.getValue() + " " +  cv.getLabel()) + "");  //auf Wunsch des Kunden, 
				}
				else{
					html.append(formManager.getCustomCheckbox("location_" + cv.getValue(), "false", false, cbimage, cbimage, cv.getValue() + " " +  cv.getLabel()) + "");
				}
			}
			//html.append("<img src=\"images/closeWizard.png\" style=\"float : right;\">");
			html.append("<table style=\"float : right;\"><tr><td><a href=\"javascript:checkAllLocations('true')\">Alle auswählen</a></td><td><a href=\"javascript:checkAllLocations('false')\">Alle abwählen</a></td>");
			html.append("<td style=\"color : black;\">Schliessen<img src=\"images/delete_small.png\" style=\"float : right;\" onclick=\"shrinkSelectionDiv(event, 'locationselection')\"></td></tr></table>");
			html.append("</div>");
			
			html.append("<table style=\"position : absolute; top: 92px;width : 415px; border-spacing: 0; border-collapse: collapse;\"><tr>");
			
			//String id, String value, boolean selected, String imageselected, String image, String label
			
		
			html.append("<td>" + formManager.getCustomCheckbox("isoffer", "true", true, cbimageselected, cbimage, "Angebot", null, "catlabel2") + "</td>");
			html.append("<td>" + formManager.getCustomCheckbox("isrequest", "true", true, cbimageselected, cbimage, "Nachfrage", null, "catlabel2") + "</td>");

			//html.append("<td><input type=\"submit\" class=\"button1\" value=\"finden!\" onclick=\"findads()\"></td>"); 
			html.append("<td style=\"text-align : right;\"><input type=\"submit\" class=\"button1\" value=\"findenl!\"></td>"); 
			html.append("</tr></table>");
			html.append("</div>");
			html.append("</form>");
			
			*/
	
			html.append("</div>");
			
			html.append("<div id=\"teaser2\">");
			
			Object o = ocs.getNewsAdministration().getObject("CurrentNewsMessage");
			if(o instanceof NewsMessage){
				
				html.append("<p style=\"margin-bottom : 5px; color : black ; font-size : 18px;\">Aktuell");
				
				NewsMessage currentNewsMessage = (NewsMessage)o;
				//html.append("<div class=\"newsmessage\">");
				html.append("<p style=\"margin-top : 0px; margin-bottom : 0px; color : #6CBB16; font-size : 18px; line-height : 1;\">" + DateConverter.sqlToShortDisplay(currentNewsMessage.getString("DateStart"), true) + " " + currentNewsMessage.getString("Title") + "</b>");
				html.append("<p style=\"margin-bottom : 0px; color : black;\">" + currentNewsMessage.getString("Description"));
				String url = currentNewsMessage.getString("URL");
				if(url.length() > 0){
					html.append("<br><a href=\"" + url + "\" target=\"_blank\" style=\"color : black;\">" + url + "</a>");
				}
				//html.append("</div>");
			}
			else{
				ObjectCollection result = new ObjectCollection("Result", "*");
				
				String sql = "SELECT t1.*, t4.ZipCode, t4.City, t5.DateOfBirth, t5.Sex, t6.ID AS MAC, t6.Title AS Category, t7.Content AS AdditionalInfo FROM MemberAd AS t1";
				sql += " LEFT JOIN OrganisationMember AS t2 ON t1.OrganisationMemberID=t2.ID";
				sql += " LEFT JOIN Person AS t3 ON t2.Person=t3.ID";
				sql += " LEFT JOIN Address AS t4 ON t4.PersonID=t3.ID";
				sql += " LEFT JOIN Identity AS t5 ON t5.PersonID=t3.ID";
				sql += " LEFT JOIN MemberAdCategory AS t6 ON t1.Template=t6.ID";
				sql += " LEFT JOIN Note AS t7 ON t1.ID=t7.MemberAdID";
				
				sql += " WHERE Priority=1 AND DateModified IS NOT NULL ORDER BY DateModified DESC LIMIT 1";
				
				ocs.queryData(sql, result);
				
				int currentyear = DateConverter.getCurrentYear();
				
				if(result.getObjects().size() > 0){
					Record record = (Record)result.getObjectByIndex(0);
					
					String id = record.getString("ID");
					
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
					
					html.append("<p style=\"margin-bottom : 5px; color : black ; font-size : 18px;\">Inserat der Woche");
					html.append("<p style=\"margin-top : 0px; margin-bottom : 0px; color : #6CBB16; font-size : 18px; line-height : 1;\"><img style=\"border : 0px\" src=\"images/" + record.getString("MAC") + "_active.png\">" + record.getString("CATEGORY"));
					html.append("<p style=\"margin-top : 0px; color : #6CBB16; font-size : 16px;\">" + record.getString("ZIPCODE")+ " " + record.getString("CITY"));
					html.append("<br>" + sex2 + " (" + age + ") " +  record.getString("TITLE"));
					html.append("<p style=\"margin-bottom : 0px; color : black;\">");
					if(record.getString("ADDITIONALINFO") != null && record.getString("ADDITIONALINFO").length() > 0){
						html.append(record.getString("ADDITIONALINFO"));
					}
					else{
						html.append(record.getString("DESCRIPTION"));
					}
					html.append("<br><a style=\"color : black;\" href=\"" + ocs.getBaseURL(request) + "/servlet?action=searchads&category=" + record.getString("MAC") + "&memberadid=" + id + "\">(Mehr)</a>");
				}
			}
			
			html.append("</div>");
	
			html.append("\n<div id=\"memberaddentryform\" style=\"position : absolute; left : 32px; top : 344px; height : 330px; width : 960px;\">");
			
			//html.append("<h4>Marktplatz</h4>");
			
			//html.append("Sie können hier jederzeit über unsern Markt schlendern und unverbindlich Angebote und Nachfragen anschauen, bevor Sie sich entscheiden, ob Sie selber auf dem Markt mitmachen wollen. Wenn Sie aktiv mitmachen wollen, müssen Sie sich <a href=\"javascript:createProcess(\'ch.opencommunity.process.MemberRegistration\')\">registrieren</a> und die Regeln des NachbarNET akzeptieren."); 
	
			//html.append("<input type=\"button\" onclick=\"createMemberAdd()\" value=\"Angebot platzieren\"><p>");
			
			//html.append("<h5>Rubriken</h5>");
			
			html.append("\n<div class=\"clearfix\">");
			
			html.append("<form name=\"searchform\" id=\"searchform\">");
			
			int left = 0;
			int top = 0;
			
			int cnt = 0;
			int cnt2 = 1;
			
			for(BasicClass bc :  getObjects("MemberAdCategory")){
			
				html.append("\n<div class=\"category\" style=\"left : " + (left * 245) + "px; top : " + (top * 56) + "px; width 225px;\">");
				
				//html.append("<a class=\"category\" href=\"" + ocs.getBaseURL(request) + "/opencommunity/servlet?action=searchads&category=" + bc.getName() + "\"><img class=\"catpicto\" src=\"" + ocs.getBaseURL(request) + "/opencommunity/websites/1/images/picto/" + cnt2 + ".png\">");
				html.append("<a class=\"category\" href=\"servlet.srv?action=searchads&category=" + bc.getName() + "\"><img class=\"catpicto\" src=\"/res/icons/" + cnt2 + ".png\">");
				
				html.append("<span>" + bc.getString("Title") + "</span></a>");
				
				html.append("</div>");
			
				left++;
				if(left == 4){
					left = 0;
					top++;
				}
				cnt2++;
			}
			
			/*
			html.append("\n<div id=\"map\">");
			html.append("<object id=\"map-svg\" style=\"width : 275px;\" data=\"" + ocs.getBaseURL(request) + "/opencommunity/websites/1/images/map.svg\" type=\"image/svg+xml\" onload=\"setMapFillings()\"></object>");
			html.append("\n</div>");
			*/
			
			html.append("\n</div>");
		
		}
		
		

		

		
		return html.toString();
	}
	public String getSearchResults(ApplicationContext context){
		return getSearchResults(context, false);
	}
	public String getSearchResults(ApplicationContext context, boolean embedded){
		return getSearchResults(context, null, embedded);
	}
	public String getSearchResults(ApplicationContext context, OrganisationMember om, boolean embedded){
		
		WebApplicationContext webcontext= (WebApplicationContext)context;
		
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
		
		
		HttpServletRequest request = webcontext.getRequest();
				
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		
		ocs.logAccess("embedded : " + embedded);
		
		String category = (String)userSession.get("category");
			
		String location = (String)userSession.get("location");
			
		MemberAdCategory mac = (MemberAdCategory)getObjectByName("MemberAdCategory", category);
		
		boolean isoffer = true;
		boolean isrequest = true;
		boolean tandemonly = false;
		

		
		
		
		if(userSession.get("isoffer") == null || ((String[])userSession.get("isoffer"))[0].equals("true")){
					
		}
		else{

			isoffer = false;
		}
		if(userSession.get("isrequest") == null || ((String[])userSession.get("isrequest"))[0].equals("true")){
					
		}
		else{
					
			isrequest = false;
		}
		if(mac.getBoolean("TandemAllowed")){
			if(userSession.get("tandemonly") == null || ((String[])userSession.get("tandemonly"))[0].equals("false")){
						
			}
			else{
							
				tandemonly = true;
			}
		}
		/*
		ocs.logAccess("isrequest : " + userSession.get("isrequest"));			
		ocs.logAccess("isrequest : " + isrequest);		

		ocs.logAccess("isoffer : " + userSession.get("isoffer"));			
		ocs.logAccess("isoffer : " + isoffer);	
		*/
		
		
		return getSearchResults(ocs, om, mac, userSession, request, isrequest, isoffer, tandemonly, embedded);
	}
	public String getSearchResults(OpenCommunityServer ocs, OrganisationMember om, MemberAdCategory mac, OpenCommunityUserSession userSession, HttpServletRequest request, boolean isrequest, boolean isoffer, boolean tandemonly, boolean embedded){
		
		int cntrequests = 0;
		int cntoffers = 0;
		
		int limit = 20;
		
		String category = (String)userSession.get("category");
			
		String location = (String)userSession.get("location");
		
		String[] memberadid = (String[])userSession.get("memberadid");
		
		String[] familyname = null;
		String[] firstname = null;
		String[] title = null;
		String[] description = null;
		if(embedded){
			familyname	= (String[])userSession.get("familyname");	
			firstname	= (String[])userSession.get("firstname");	
			title = (String[])userSession.get("title");	
			description = (String[])userSession.get("description");	
		}
		
		int offset = 0;
		String[] soffset = (String[])userSession.get("offset");
		if(soffset != null && soffset.length==1){
			offset = Integer.parseInt(soffset[0]);
		}
		
		StringBuilder html = new StringBuilder();
		


		if(om==null){
			om = userSession.getOrganisationMember();	
		}
			
			
			
			ObjectCollection results = new ObjectCollection("Results", "*");
			
			StringBuilder html2 = new StringBuilder();
			
			for(Object o : userSession.getParameters().keySet()){
				//ocs.logAccess(o);
			}
			
			
			String fieldlist = "SELECT DISTINCT t1.ID, t1.Title, t1.Description, t1.ValidFrom, t1.ValidUntil, t1.IsOffer, t1.IsRequest, t1.Priority, t2.ID AS OMID, t3.Languages, t4.ZipCode, t4.City, t5.Sex, t5.DateOfBirth, t5.FirstLanguageS, t5.Familyname, t5.Firstname,";
			fieldlist += " (CASE WHEN (t1.Location IS NULL OR t1.Location = '') THEN t4.Zipcode ELSE t1.Location END) AS Location, count(t1.ID) OVER() AS total_count";
			String sql = " FROM MemberAd AS t1";			
			sql += " LEFT JOIN OrganisationMember AS t2 ON t1.OrganisationMemberID=t2.ID";
			sql += " LEFT JOIN Person AS t3 ON t2.Person=t3.ID";
			sql += " LEFT JOIN Address AS t4 ON t4.PersonID=t3.ID";
			sql += " LEFT JOIN Identity AS t5 ON t5.PersonID=t3.ID";
			//sql += " LEFT JOIN Parameter AS t6 ON t6.PersonID=t3.ID";

			
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
			
			sql += " WHERE t1.template=" + category + " AND t1.Status=1";
			
			sql += " AND t1.ValidUntil >= Now() ";
			
			sql += " AND t2.Status=1"; //AK 20160605, nur aktive Benutzer
			

			
			String locationcriteria_alt = "(";
			String locationcriteria = "(";
			for(ConfigValue cv : ocs.getGeoObjects()){
				if(userSession.get("location_" + cv.getValue()) != null && ((String[])userSession.get("location_" + cv.getValue()))[0].equals("true")){
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
			    	
					//sql += " AND (t4.Zipcode IN " + locationcriteria;	
			    	//sql += " OR t4.Zipcode NOT IN " + locationcriteria_alt + ")";		
			    	
					sql += " AND ((char_length(t1.location) > 0 AND t1.location IN " + locationcriteria + " OR  char_length(t1.location) = 0 AND t4.ZipCode IN " + locationcriteria + ")";
			    	sql += " OR ((char_length(t1.Location) = 0 AND t4.Zipcode NOT IN " + locationcriteria_alt + ") OR (char_length(t1.Location) > 0 AND t1.Location NOT IN " + locationcriteria_alt + ")))";
			    	
			    }
			    else{
			    	//alle PLZ, die nicht aufgelistet sind
			    	//sql += " AND t4.Zipcode NOT IN " + locationcriteria_alt;
			    	sql += " AND ((char_length(t1.Location) = 0 AND t4.Zipcode NOT IN " + locationcriteria_alt + ") OR (char_length(t1.Location) > 0 AND t1.Location NOT IN " + locationcriteria_alt + "))";
			    }
					
			}
			else{
				if(locationcriteria.length() > 1){
					locationcriteria = locationcriteria.substring(0, locationcriteria.length()-1) + ")";
					//sql += " AND t4.Zipcode IN " + locationcriteria;	
					sql += " AND (char_length(t1.location) > 0 AND t1.location IN " + locationcriteria + " OR  char_length(t1.location) = 0 AND t4.ZipCode IN " + locationcriteria + ")";
				}				
			}
			
			
			for(i = 0; i <  fieldDefinitions.size(); i++ ){
				FieldDefinition fd = (FieldDefinition)fieldDefinitions.elementAt(i);
				if(hasParameter(fd, userSession.getParameters())){
						//if(i==0){  ToDo: korrekte OR-Verknüpfung
						//	sql += " AND (";
						//}
						String valuelist = getValueList(fd, userSession.getParameters());
						if(fd.getInt("RefersTo") > 0){                             
							sql += " AND (t" + (7+i) + ".Value IN " + valuelist + " OR t" + (7+i+1) + ".Value IN " + valuelist + ")";
						}
						else{
							//sql += " AND t" + (7+i) + ".Value IN " + valuelist;
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
            
            if(embedded && familyname != null && familyname.length > 0 && familyname[0].length() > 0){
            	sql += " AND t5.FamilyName ilike '" + familyname[0] + "%'";
            }
            if(embedded && firstname != null && firstname.length > 0 && firstname[0].length() > 0){
            	sql += " AND t5.FirstName ilike '" + firstname[0] + "%'";
            }
            if(embedded && title != null && title.length > 0 && title[0].length() > 0){
            	sql += " AND t1.Title ilike '%" + title[0] + "%'";
            }
            if(embedded && description != null && description.length > 0 && description[0].length() > 0){
            	sql += " AND t1.Description ilike '%" + description[0] + "%'";
            }

			//sql += " ORDER BY t1.Priority DESC, t4.ZipCode";
			sql += " ORDER BY t1.Priority DESC, Location";
			
			//sql += " LIMIT " + limit + " OFFSET " + offset;
			
			
			sql = fieldlist + sql;
			
			ocs.logAccess(sql);
			
			ocs.queryData(sql, results);
			
			
			
			String prevzipcode = "";
			
			int total_count = results.getObjects().size();
			
			int lowerlimit = offset - 1;
			int upperlimit = offset + limit - 1;
			
			
			
			for(i = 0; i < results.getObjects().size(); i++){
				
				if(i >=lowerlimit && i <= upperlimit){
				
				BasicClass record = results.getObjects().elementAt(i);
				
				if(i == 0){
					//total_count = record.getInt("TOTAL_COUNT");	
				}
				
				//String zipcode = record.getString("ZIPCODE");
				String zipcode = record.getString("LOCATION").trim();
				
				String sex2 = "Mann";
				String sex = record.getString("SEX");
				if(sex.equals("2")){
					sex2 = "Frau";	
				}
				
				int age = 48;
				String dateofbirth = record.getString("DATEOFBIRTH");
				int currentyear = DateConverter.getCurrentYear();
				if(dateofbirth.length() > 3){
					try{
						String[] args = dateofbirth.split("\\.");
						String syear = args[args.length-1];
						int year = Integer.parseInt(syear);
						age = currentyear - year;
					}
					catch(java.lang.Exception e){}
				}
				
				
				if(!zipcode.equals(prevzipcode)){
					if(i==0){
						html2.append("\n<div class=\"searchresultheader\" style=\"margin-top : 10px;\">");						
					}
					else{
						html2.append("\n<div class=\"searchresultheader\">");
					}
					html2.append(zipcode + " " + record.getString("CITY"));
					html2.append("</div>");
				}
				
				
				
				html2.append("<div id=\"" + record.getString("ID") + "\" class=\"searchresult\">");
				

				if(om != null){ //nur angemeldete Benutzer
					if(!record.getString("OMID").equals(om.getName())){
						if(record.getString("REQUESTSTATUS").length()==0){  //Inserate nur einmal bestellen
							if(userSession.hasMemberAdID(record.getString("ID"))){
								//html2.append("<img class=\"merken\" src=\"" + ocs.getBaseURL(request) + "/opencommunity/websites/1/images/picto/merken_aktiv.png\"  onclick=\"deselectAd(" + record.getString("ID") + ")\" onmouseover=\"showHelp(event, 4)\" onmouseout=\"hideHelp()\">");
								html2.append("<img id=\"img_" + record.getString("ID") + "\" class=\"merken\" src=\"/res/icons/merken_aktiv.png\"  onclick=\"deselectAd(event, " + record.getString("ID") + ")\">");
							}	
							else{
								//html2.append("<img class=\"merken\" src=\"" + ocs.getBaseURL(request) + "/opencommunity/websites/1/images/picto/merken.png\"  onclick=\"selectAd(" + record.getString("ID") + ")\" onmouseover=\"showHelp(event, 4)\" onmouseout=\"hideHelp()\">");
								html2.append("<img id=\"img_" + record.getString("ID") + "\" class=\"merken\" src=\"/res/icons/merken.png\"  onclick=\"selectAd(event, " + record.getString("ID") + ")\" onmouseover=\"swapImage(this.id, true)\"  onmouseout=\"swapImage(this.id, false)\">");
							}
						}
						else{
							if(userSession.hasMemberAdID(record.getString("ID"))){
								//html2.append("<img class=\"merken\" src=\"" + ocs.getBaseURL(request) + "/opencommunity/websites/1/images/picto/merken_aktiv.png\"  onclick=\"deselectAd(" + record.getString("ID") + ")\" onmouseover=\"showHelp(event, 4)\" onmouseout=\"hideHelp()\">");
								html2.append("<img id=\"img_" + record.getString("ID") + "\" class=\"merken\" src=\"/res/icons/merken_aktiv.png\"  onclick=\"deselectAd(event, " + record.getString("ID") + ")\">");
							}	
							else{
								if(record.getString("REQUESTSTATUS").equals("1")){ //Kontakte ist in "meine Kontakte"
									if(record.getInt("REQUESTDATESTATUS") == 1){ //abgelaufen
										html2.append("<img  id=\"img_" + record.getString("ID") + "\" class=\"merken\" src=\"/res/icons/kontakte_grau.png\")\"  onclick=\"selectAd(event, " + record.getString("ID") + ", 1)\" >");	
									}
									else{
										html2.append("<img  id=\"img_" + record.getString("ID") + "\" class=\"merken\" src=\"/res/icons/kontakte_gruen.png\">");	
									}
								}
								else if(record.getString("REQUESTSTATUS").equals("3")){
									html2.append("<img  id=\"img_" + record.getString("ID") + "\" class=\"merken\" src=\"/res/icons/kontakte_orange.png\" onclick=\"selectAd(event, " + record.getString("ID") + ", 2)\" >");		
								}
							}
							
						}
					}
				}

				

				String persondescription = sex2 + " (" + age + ") ";
				if(embedded){
					persondescription = record.getString("FAMILYNAME") + " " + record.getString("FIRSTNAME") + ", " + age + ": ";
				}

				if(memberadid != null && memberadid[0].equals(record.getString("ID"))){
										
					html2.append("<p style=\"margin-top : 0px;\" onmouseover=\"highlightaddetail('" + record.getString("ID") + "', true)\" onmouseout=\"highlightaddetail('" + record.getString("ID") + "', false)\"><a href=\"javascript:showaddetail('" + record.getString("ID") + "')\" style=\"color : #6CBB16;\"><span id=\"" + record.getString("ID") + "_title\">" +  persondescription + record.getString("TITLE") + "</span> <span id=\"" + record.getString("ID") + "_link\"\"></span></a>");
					html2.append("<div id=\"" + record.getString("ID") + "_detail\" class=\"searchresultdetails\" style=\"display : block; height : auto;\">");	
				}
				else{
					html2.append("<p style=\"margin-top : 0px;\" onmouseover=\"highlightaddetail('" + record.getString("ID") + "', true)\" onmouseout=\"highlightaddetail('" + record.getString("ID") + "', false)\"><a href=\"javascript:showaddetail('" + record.getString("ID") + "')\" style=\"color : white;\"><span id=\"" + record.getString("ID") + "_title\">" +  persondescription + record.getString("TITLE") + "</span> <span id=\"" + record.getString("ID") + "_link\"\"> ... </span></a>");
					html2.append("<div id=\"" + record.getString("ID") + "_detail\" class=\"searchresultdetails\">");
				}
					
				html2.append(record.getString("DESCRIPTION"));
					

				
				if(mac.getName().equals("6")==false){
					html2.append("<p>Sprachen: " + record.getString("FIRSTLANGUAGES") + ", " + record.getString("LANGUAGES").replace("#", ", "));
				}
				else{
					html2.append("<br>&nbsp;");	
				}
				html2.append("</div>");
				
				html2.append("</div>");
				
				if(record.getBoolean("ISOFFER")){
					cntoffers++;
				}
				if(record.getBoolean("ISREQUEST")){
					cntrequests++;
				}
				
				prevzipcode = zipcode;
				
			}
			}
			
			
			

			

			double numBatches = ((double)total_count / (double)limit);
			

			for(i = 0; i< numBatches; i++){
				html2.append("<a href=\"javascript:detailSearch(null, null, false, " + ((i * limit) + 1) + ")\">[" + (i * limit + 1) + "-" + ((i + 1) * limit) + "]</a>");
			}
			
			
			html.append("<p class=\"sectionheader\"><img class=\"catpicto3\" src=\"images/" + mac.getName() + "_active.png\">" + mac.getString("Title") + " - Resultate");
			html.append(" " + MemberRegistration.getHelpButton(7));
			
			if(userSession.getMemberAdIDs() != null && userSession.getMemberAdIDs().size() > 0 && !embedded){
				
				html.append("<div><input class=\"addressorderbutton\" type=\"button\" onclick=\"orderAddresses()\" value=\"zur Bestell-Liste\">");
				html.append("</div>");
			}
			
			
			html.append(html2.toString());
			
			if(userSession.getMemberAdIDs() != null && userSession.getMemberAdIDs().size() > 0 && !embedded){
				html.append("<p><input class=\"addressorderbutton\" type=\"button\" onclick=\"orderAddresses()\" value=\"zur Bestell-Liste\">");
			}
			
			return html.toString();
		
		
		
	}
	public String getSearchResultse(ApplicationContext context){
	
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
		
		StringBuilder html = new StringBuilder();
		
		String macid = context.getString("macid");
		
		MemberAdCategory mac = null;
		
		if(macid != null){
		
			mac = (MemberAdCategory)getObjectByName("MemberAdCategory", macid);
			userSession.setMemberAdCategory(mac);
			
		}
		else{
			mac = userSession.getMemberAdCategory();
		}
			
		if(mac != null){
	
		
			html.append("<table>");
			html.append("<tr><td><h4>Ergebnis der Suche</h4></td><td width=\"400px\"><a href=\"javascript:showMap()\">Als Karte anzeigen</a></td><td><input type=\"button\" onclick=\"createMemberAdRequest()\" value=\"Anmelden\"></td></tr>");
			
			String sql = "SELECT t1.ID, t1.Title, t1.Type, t4.Street,";
			int i = 5;
			for(BasicClass bc : mac.getObjects("FieldDefinition")){
				sql += " t" + i + ".Value AS Value" + i + ",";
				i++;
			}
			sql += " t1.Description,";
			sql += " count(*) OVER() AS total_count";
			sql += " FROM MemberAd AS t1";
			sql += " LEFT JOIN OrganisationMember AS t2 ON t1.OrganisationMemberID=t2.ID";
			sql += " LEFT JOIN Person AS t3 ON t2.Person=t3.ID";
			sql += " LEFT JOIN Address AS t4 ON t4.PersonID=t3.ID";
				
			i = 5;
			for(BasicClass bc : mac.getObjects("FieldDefinition")){
				//sql += " LEFT JOIN Parameter AS t" + i + " ON t1.ID=t" + i + ".MemberAdID AND t" + i + ".template=" + bc.getName();
				sql += " LEFT JOIN Parameter AS t" + i + " ON t1.ID=t" + i + ".MemberAdID";
				i++;
			}
			sql += " WHERE t1.Template=" + mac.getName();
			ObjectCollection results = new ObjectCollection("Results", "*");
			ocs.queryData(sql, results);
			for(BasicClass record : results.getObjects()){
					html.append("<tr>");
					html.append("<th>Inseratentyp</th>");
					if(record.getString("TYPE").equals("0")){
						html.append("<td>Angebot</td>");
					}
					else{
						html.append("<td>Nachfrage</td>");
					}
					html.append("</tr>");
					
					i = 5;
					for(BasicClass bc : mac.getObjects("FieldDefinition")){
						FieldDefinition fd = (FieldDefinition)bc;
						String value = record.getString("VALUE" + i);
						String[] args = value.split("\\."); 
						html.append("<tr><th>" + bc.getString("Title") + "</th><td>" + fd.getCodeMap().get(args[0]) + "</td></tr>");
						i++;
					}
					html.append("<tr><th>Strasse</th><td>" + record.getString("STREET") + "</td></tr>");
					html.append("<tr><th>Betreff</th><td>" + record.getString("TITLE") + "</td></tr>");
					html.append("<tr><th>Beschreibung</th><td>" + record.getString("DESCRIPTION") + "</td></tr>");;
					html.append("<tr class=\"separator\"><td></td><td></td>");
					if(userSession.hasMemberAdID(record.getString("ID"))){
						html.append("<td style=\"background : red;\"><input type=\"button\" onclick=\"removeAd(" + record.getString("ID") + ")\" value=\"Entfernen\"></td>");
					}	
					else{
						html.append("<td><input type=\"button\" onclick=\"selectAd(" + record.getString("ID") + ")\" value=\"Merken\"></td>");
					}
					html.append("</tr>");
					
				}
				
			html.append("</table>");
			
		}
		
		return html.toString();
	}
	public String getMemberAdCategoryEditForm(String memberadcategoryid, ApplicationContext context){
		
		boolean isEditable = true;
		
		StringBuilder html = new StringBuilder();
		
		MemberAdCategory mac = (MemberAdCategory)getObjectByName("MemberAdCategory", memberadcategoryid);
		
		String returnTo = null;
		
		html.append(HTMLForm.getToolbar(mac, null, returnTo, context, isEditable));
	
		html.append("<div>\n<form action=\"main\" id=\"objectEditForm_" + mac.getPath("") + "\">");
		
		
		
		if(mac instanceof ActionHandler){
			html.append("\n<input type=\"hidden\" name=\"action\" value=\"getObjectAction\">");	
			html.append("\n<input type=\"hidden\" name=\"command\" value=\"saveobject\">");			
		}
		else{		
			html.append("\n<input type=\"hidden\" name=\"action\" value=\"saveObject\">");			
		}
		
		html.append("\n<input type=\"hidden\" name=\"objectPath\" value=\"" + mac.getPath("") + "\">");
		
		html.append("<table>");
		
		html.append("<tr><th>Bezeichnung</th><td><input name=\"Title\" value=\"" + mac.getString("Title") + "\"></td></tr>");

		html.append("<tr><th>Tandem</th><td>" + HTMLForm.getRadioButton(mac.getProperty("TandemAllowed"), true, "") + "</td></tr>");
		html.append("<tr><th>Geschützt</th><td>" + HTMLForm.getRadioButton(mac.getProperty("Protected"), true, "") + "</td></tr>");
		html.append("<tr><th>Nur Nachfragen</th><td>" + HTMLForm.getRadioButton(mac.getProperty("RequestOnly"), true, "") + "</td></tr>");
		for(BasicClass bc : mac.getObjects("FieldDefinition")){
			html.append("<tr><th>Bezeichnung</th><td><input name=\"title_" + bc.getName() + "\" value=\"" + bc.getString("Title") + "\"></td></tr>");
			html.append("<tr><th>Alternative Bezeichnung</th><td><input name=\"alttitle_" + bc.getName() + "\" value=\"" + bc.getString("AltTitle") + "\"></td></tr>");
			html.append("<tr><th>Typ</th><td><input name=\"type_" + bc.getName() + "\" value=\"" + bc.getString("Type") + "\"></td></tr>");
			html.append("<tr><th>Sortierung</th><td><input name=\"sortorder_" + bc.getName() + "\" value=\"" + bc.getString("SortOrder") + "\"></td></tr>");

			html.append("<tr><th>Codes</th><td><textarea name=\"codelist_" + bc.getName() + "\">" + bc.getString("CodeList") + "</textarea></td></tr>");
		}
		
		html.append("<tr><td>");
		if (isEditable) {
//			html.append(getSaveButton(o, returnTo, isEditable));
		}
		html.append("</td></tr>");
		
		html.append("</table></form><div>");
		
		return html.toString();
	}
	
	public String getMemberAdCategoryEditForm(String memberadcategoryid){
		
		StringBuilder html = new StringBuilder();
		html.append("<input type=\"button\" onclick=\"addFieldDefinition(" + memberadcategoryid + ")\" value=\"Neue Felddefinition\">");
		MemberAdCategory mac = (MemberAdCategory)getObjectByName("MemberAdCategory", memberadcategoryid);
		html.append("<form id=\"memberadcategoryeditform\">");
		html.append("<table>");
		html.append("<tr><th>Bezeichnung</th><td><input name=\"Title\" value=\"" + mac.getString("Title") + "\"></td></tr>");

		html.append("<tr><th>Tandem</th><td>" + HTMLForm.getRadioButton(mac.getProperty("TandemAllowed"), true, "") + "</td></tr>");
		html.append("<tr><th>Geschützt</th><td>" + HTMLForm.getRadioButton(mac.getProperty("Protected"), true, "") + "</td></tr>");
		html.append("<tr><th>Nur Nachfragen</th><td>" + HTMLForm.getRadioButton(mac.getProperty("RequestOnly"), true, "") + "</td></tr>");
		for(BasicClass bc : mac.getObjects("FieldDefinition")){
			html.append("<tr><th>Bezeichnung</th><td><input name=\"title_" + bc.getName() + "\" value=\"" + bc.getString("Title") + "\"></td></tr>");
			html.append("<tr><th>Alternative Bezeichnung</th><td><input name=\"alttitle_" + bc.getName() + "\" value=\"" + bc.getString("AltTitle") + "\"></td></tr>");
			html.append("<tr><th>Type</th><td><input name=\"type_" + bc.getName() + "\" value=\"" + bc.getString("Type") + "\"></td></tr>");
			//html.append("<tr><th>Nur für Tandem</th><td>" + HTMLForm.getRadioButton(bc.getProperty("TandemOnly"), true, "") + "</td></tr>");
			html.append("<tr><th>Codes</th><td><textarea name=\"codelist_" + bc.getName() + "\">" + bc.getString("CodeList") + "</textarea></td></tr>");
		}
		html.append("</table>");		
		html.append("</form>");
		html.append("<input type=\"button\" onclick=\"saveMemberAdCategoryEditForm(" + memberadcategoryid + ")\" value=\"Speichern\">");
		return html.toString();	
		
	}
	public void saveMemberAdCategoryEditForm(String memberadcategoryid, ApplicationContext context){
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		MemberAdCategory mac = (MemberAdCategory)getObjectByName("MemberAdCategory", memberadcategoryid);
		mac.saveObject(context);
		for(BasicClass bc : mac.getObjects("FieldDefinition")){
			if(context.hasProperty("title_" + bc.getName())){
				bc.setProperty("Title", context.getString("title_" + bc.getName()));
			}
			if(context.hasProperty("alttitle_" + bc.getName())){
				bc.setProperty("AltTitle", context.getString("alttitle_" + bc.getName()));
			}
			if(context.hasProperty("type_" + bc.getName())){
				bc.setProperty("Type", context.getString("type_" + bc.getName()));
			}
			if(context.hasProperty("codelist_" + bc.getName())){
				bc.setProperty("CodeList", context.getString("codelist_" + bc.getName()));
			}
			ocs.updateObject(bc);
		}
	
	}
	public String[] getRandomCommercialAd(){
		
		String[] adinfo = null;
		
		int randomNum = ThreadLocalRandom.current().nextInt(0, nCommercialAds);
		
		MemberAd memberad = (MemberAd)getObjectByIndex("MemberAd", randomNum);
		
		if(memberad != null){
			ImageObject imageObject = (ImageObject)memberad.getObjectByIndex("ImageObject", 0);
			if(imageObject != null){
				adinfo = new String[2];
				adinfo[0] = imageObject.getName() + "." + imageObject.getString("FileExtension");
				adinfo[1] = memberad.getString("ExternalLink");
			}
		}
		
		return adinfo;
		
		
	}
	public List<String[]> getRandomCommercialAds(){
		
		ArrayList adinfos = new ArrayList<String[]>();
		
		try{
			HashMap selected = new HashMap();
			
			int maxheight = 700;
			int minheight = 500;
			int i = 0;
			int sum = 0;
			int cnt = 0;
			while(i < 3 && cnt < 10){
				
				//ocs.logException(sum);
				
				int randomNum = ThreadLocalRandom.current().nextInt(0, nCommercialAds);
				MemberAd memberad = (MemberAd)getObjectByIndex("MemberAd", randomNum);
				
				cnt++;
				
				if(memberad != null){
				
					if(selected.get(memberad.getName())==null){
							

						
						int height = memberad.getInt("Height");
						
						if(sum + height < maxheight){
							

							
							if(sum > minheight){
								i = 3;
							}
							else{
								
								ImageObject imageObject = (ImageObject)memberad.getObjectByIndex("ImageObject", 0);
								if(imageObject != null){
									String[] adinfo = new String[2];
									adinfo[0] = imageObject.getName() + "." + imageObject.getString("FileExtension");
									adinfo[1] = memberad.getString("ExternalLink");
									adinfos.add(adinfo);
								}
									
								selected.put(memberad.getName(), memberad);
							
								i++;	
							}
							sum += height;
							
							
						}
					}
					
				}
				
				
			}
			
		 }
		 catch(java.lang.Exception e){
			ocs.logException(e);	 
		 }
		
		

		
		return adinfos;
		
		
	}

}
