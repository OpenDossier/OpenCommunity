package ch.opencommunity.advertising;

import ch.opencommunity.common.OpenCommunityUserSession;
import ch.opencommunity.news.NewsMessage;
import ch.opencommunity.server.OpenCommunityServer;

import org.kubiki.base.BasicClass;
import org.kubiki.base.ConfigValue;
import org.kubiki.base.ObjectCollection;
import org.kubiki.database.Record;
import org.kubiki.application.ApplicationContext;
import org.kubiki.application.server.WebApplicationContext;
import org.kubiki.util.DateConverter;
import org.kubiki.cms.WebPageElementInterface;

import java.util.List;
import java.util.Random;

import javax.servlet.http.*;

public class SearchFrontPage implements WebPageElementInterface{
	
	MemberAdAdministration ma;
	OpenCommunityServer ocs;
	
	Math m;
	Random random;
	
	public SearchFrontPage(MemberAdAdministration ma, OpenCommunityServer ocs){
		
		this.ma = ma;
		this.ocs = ocs;
		
		random = new Random();
		
	}
	public String getAdminForm(ApplicationContext context){
		return "";
	}
	public String toHTML(ApplicationContext context){
		return toHTML(context, null);
	}
	public String toHTML(ApplicationContext context, List parameters){

		WebApplicationContext webcontext= (WebApplicationContext)context;
		HttpServletRequest request = webcontext.getRequest();
		
		
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");

		
		StringBuilder html = new StringBuilder();
		
		for(ConfigValue cv : ocs.getGeoObjects()){ //AK 20160627, gespeicherte Ortsselektion löschen
			String label = cv.getValue() + " " + cv.getLabel();
			label = label.replace("* ", "");
			userSession.put("location_" + cv.getValue(), null);
		}
			
		String location = (String)userSession.get("location");
			
		String cbimageselected = "images/cbimageselected.png";
		String cbimage = "images/cbimage.png";
			
		html.append("\n<style type=\"text/css\">");

		html.append("\n</style>");

		int imageindex = random.nextInt(18 - 1 + 1) + 1;
			

		html.append("<div id=\"teaser1\" style=\"background-image : url(\'/res/background/bg" + imageindex + ".png\');\">");
			

	
			html.append("</div>");
			
			html.append("<div id=\"teaser2\">");
			
			Object o = ocs.getNewsAdministration().getObject("CurrentNewsMessage");
			if(o instanceof NewsMessage){
				
				html.append("<p style=\"margin-bottom : 5px; color : black ; font-size : 18px;\">Aktuell");
				
				NewsMessage currentNewsMessage = (NewsMessage)o;

				html.append("<p style=\"margin-top : 0px; margin-bottom : 0px; color : #6CBB16; font-size : 18px; line-height : 1;\">" + DateConverter.sqlToShortDisplay(currentNewsMessage.getString("DateStart"), true) + " " + currentNewsMessage.getString("Title") + "</b>");
				html.append("<p style=\"margin-bottom : 0px; color : black;\">" + currentNewsMessage.getString("Description"));
				String url = currentNewsMessage.getString("URL");
				if(url.length() > 0){
					html.append("<br><a href=\"" + url + "\" target=\"_blank\" style=\"color : black;\">" + url + "</a>");
				}

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
				
				sql += " WHERE t1.Priority=1 AND t1.DateModified IS NOT NULL ORDER BY t1.DateModified DESC LIMIT 1";
				
				ocs.queryData(sql, result);
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
						age = 2016 - year;
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
			

			
			html.append("\n<div class=\"clearfix\">");
			
			html.append("<form name=\"searchform\" id=\"searchform\">");
			
			int left = 0;
			int top = 0;
			
			int cnt = 0;
			int cnt2 = 1;
			
			for(BasicClass bc :  ma.getObjects("MemberAdCategory")){
			
				html.append("\n<div class=\"category\" style=\"left : " + (left * 245) + "px; top : " + (top * 56) + "px; width 225px;\">");
				
				
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
			

			
			html.append("\n</div>");		
		
		return html.toString();
	}
	
	
}