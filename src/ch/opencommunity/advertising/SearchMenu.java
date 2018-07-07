package ch.opencommunity.advertising;


import ch.opencommunity.common.OpenCommunityUserSession;
import ch.opencommunity.server.OpenCommunityServer;
import ch.opencommunity.base.FieldDefinition;


import org.kubiki.base.ConfigValue;
import org.kubiki.base.BasicClass;

public class SearchMenu{
	
	
	public static String getSearchMenu(MemberAdAdministration maa, OpenCommunityServer ocs, OpenCommunityUserSession userSession){
		
		
		
		ch.opencommunity.server.HTMLForm formManager = ocs.getFormFactory();
		
		StringBuilder html = new StringBuilder();
		
		boolean isoffer = true;
		boolean isrequest = true;
		boolean tandemonly = false;
			
		int cntoffers = 0;
		int cntrequests = 0;
			
		String category = (String)userSession.get("category");
			
		String location = (String)userSession.get("location");
			

			
		MemberAdCategory mac = (MemberAdCategory)maa.getObjectByName("MemberAdCategory", category);
			
		String cbimageselected = "/res/icons/cbimageselected.png";
		String cbimage = "/res/icons/cbimage.png";
		
		cbimageselected = "/res/icons/checkbox-filter_on.png";
		cbimage = "/res/icons/checkbox-filter_off.png";		
						

			

			
		html.append("<div id=\"categoryselection\" onclick=\"expandSelectionDiv(event, \'categoryselection2\')\" onmouseover=\"highlightSelection(this.id, true)\" onmouseout=\"highlightSelection(this.id, false)\">");
		html.append("<input type=\"hidden\" id=\"selectedcategorytitle\" value=\"" + mac + "\">");
		if(mac != null){
			html.append("<span id=\"selectedcategory\">" + mac + "</span><br>");
		}
		else{
			html.append("<span id=\"selectedcategory\">Was? Rubrik wählen</span><br>");
		}		

			html.append("<div id=\"categoryselection2\">");
			for(BasicClass bc :  maa.getObjects("MemberAdCategory")){

				if(mac != null && mac.getName().equals(bc.getName())){
					html.append("<a href=\"javascript:reloadSearch(" + bc.getName() + ")\" style=\"color : red\">" + bc + "</a><br>");
				}
				else{
					html.append("<a href=\"javascript:reloadSearch(" + bc.getName() + ")\" style=\"color : black\">" + bc + "</a><br>");
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
					
					html.append("<span id=\"span_languages_tandem\" onclick=\"openMenuItem2('languages_tandem')\"><img id=\"icon_languages_tandem\" src=\"/res/icons/mehr_inaktiv.png\" onclick=\"openMenuItem('languages_tandem')\">");
					
					html.append("Ich suche Tandem</span>");
					html.append("<div id=\"languages_tandem\" style=\"display : none; margin-left : 8px;\">");
					html.append("Ich suche");
					html.append("<br><select class=\"languageselection\" id=\"select_languages_offer_1\" onchange=\"detailSearch()\">");
					html.append("<option value=\"\"></option>");
					for(Object o : fd1.getCodeList()){	
						ConfigValue cv = (ConfigValue)o;
						html.append("<option value=\"" + cv.getValue() + "\">" + cv.getLabel() + "</option>");
						
					}
					html.append("</select>");
					html.append("<br>Ich biete");
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
					html.append("<span id=\"span_languages_offer\" onclick=\"openMenuItem('languages_offer')\"><img id=\"icon_languages_offer\" src=\"/res/icons/mehr_inaktiv.png\">");
					html.append("Ich suche Lehrer</span>"); // ich = Benutzer der Website
					html.append("<div id=\"languages_offer\" style=\"display : none; margin-left : 8px;\">");
					html.append("ich möchte lernen<br>");
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
					html.append("<span id=\"span_languages_request\" onclick=\"openMenuItem('languages_request')\"><img id=\"icon_languages_request\" src=\"/res/icons/mehr_inaktiv.png\">");
					html.append("Ich suche Schüler</span>"); // ich = Benutzer der Website
					html.append("<div id=\"languages_request\" style=\"display : none; margin-left : 8px;\">");
					html.append("ich biete / unterrichte<br>");
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
				

					html.append("<div id=\"searchdetails\">");
					
					String expand = "/res/mehr_aktiv.png";
					
					for(BasicClass bc : mac.getObjects("FieldDefinition")){
											
						FieldDefinition fd = (FieldDefinition)bc;

						
						if(!fd.getBoolean("TandemOnly")){
							
							
							html.append("<div>");
							html.append("\n<div class=\"profilemenuitem\">");

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

			
			int cnt = 0;
			int cnt2 = 0;
			

			for(ConfigValue cv : ocs.getGeoObjects()){
				

				String label = "" + cv.getValue();
				
				if(label.equals("*")){
					label = "F/D/CH";	
				}
				
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

					html.append("\n" + formManager.getCustomCheckbox("location_" + cv.getValue(), "true", true, cbimageselected, cbimage, label) + "");
				}
				else{

					html.append("\n" + formManager.getCustomCheckbox("location_" + cv.getValue(), "false", false, cbimage, cbimage, label) + "");
				}


				if(cnt2==3){
					cnt2 = 1;	
				}
			}
			
			html.append("</td></tr>");
			
			html.append("<tr><td colspan=2>");
			html.append("\n<a href=\"javascript:checkAllLocations('true')\"><img src=\"/res/icons/auswaehlen-gelb_aktiv.png\"><span style=\"color : #363D45\"> Alle auswählen</span></a>");
			

			
			html.append("</td></tr><tr><td colspan=2>");
			
			html.append("\n<a href=\"javascript:checkAllLocations('false')\"><img src=\"/res/icons/loeschen_orange.png\"><span style=\"color : #363D45\"> Alle abwählen</span></a>");
			html.append("</td></tr>");
			
			html.append("\n<tr><td colspan=2><input class=\"nodebutton\" type=\"button\" value=\"Abschliessen\" onclick=\"shrinkSelectionDiv(event, 'locationselection2', true)\" style=\"width : 180px\"></td></tr>");//AK 20160826
			
			html.append("\n</table>");
			

			
			

			html.append("</div>");
			html.append("</div>");
			

			

			

			html.append("\n<div id=\"map-svg-container\">");

			html.append("\n<object id=\"map-svg\" data=\"/res/map.svg\" type=\"image/svg+xml\" onload=\"setMapFillings()\"></object>");

			
			

			
			html.append("\n</form>");
			
			html.append("\n</div>");

			
			cnt = 1;
				

			
			return html.toString();
	
		
		
		
		
		
		
		
	}
	
	
	
	
}