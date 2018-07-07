package ch.opencommunity.news;


import ch.opencommunity.base.*;
import ch.opencommunity.server.*;
import ch.opencommunity.common.*;

import org.kubiki.base.*;
import org.kubiki.application.*;
import org.kubiki.cms.CMSServer;
import org.kubiki.cms.WebPageElementInterface;
import org.kubiki.servlet.WebApplication;
import org.kubiki.application.server.WebApplicationContext;
import org.kubiki.database.Record;
import org.kubiki.util.DateConverter;

import java.util.Map;
import java.util.Vector;
import java.util.Random;
import java.util.List;



import javax.servlet.http.*;


public class NewsAdministration extends BasicOCObject implements WebPageElementInterface{
	
	
	
	public NewsAdministration(){
		
		addProperty("CurrentNewsMessage", "Integer", "", false, "Aktuell");
		addObjectCollection("NewsMessage", "ch.opencommunity.news.NewsMessage");		
		
	}
	public void initObjectLocal(){
		getProperty("CurrentNewsMessage").setSelection(getObjects("NewsMessage"));	
		
		getObjectCollection("NewsMessage").sort("DateStart", "String", false);
		//filter = null;
	}
	public String getAdminForm(ApplicationContext context){
		return "";
	}
	public String toHTML(ApplicationContext context){
		
		StringBuilder html = new StringBuilder();
		
		html.append("<p class=\"displaytitle\">Aktuelles und Agenda</p>");
		html.append("<p class=\"displaybody\">");
		for(BasicClass bc : getObjects("NewsMessage")){
			if(bc.getID("Status")==0 && (bc.getID("Type")==1 || bc.getID("Type")==2)){
				html.append("<div class=\"newsmessage\">");
				html.append("<b>" + DateConverter.sqlToShortDisplay(bc.getString("DateStart"), true) + " " + bc.getString("Title") + "</b>");
				html.append("<br>" + bc.getString("Description"));
				String url = bc.getString("URL");
				if(url.length() > 0){
					if(bc.getString("URLTitle").length() > 0){
						html.append("<p class=\"newsurl\"><a  class=\"newsurl\" href=\"" + url + "\" target=\"_blank\">" + bc.getString("URLTitle") + "</a></p>");
					}
					else{
						html.append("<p class=\"newsurl\"><a  class=\"newsurl\" href=\"" + url + "\" target=\"_blank\">" + url + "</a></p>");
					}
				}

				html.append("</div>");
			}
		}
		
		
		return html.toString();	
	}
	public String toHTML(ApplicationContext context, List parameters){
		
		StringBuilder html = new StringBuilder();
		
		String title = "Aktuelles und Agenda";
		
		int status = 0;
		
		Vector<Object[]> filter = null;
		
		//if(filter == null){
			for(int i = 0; i < parameters.size(); i++){
				String[] args = (String[])parameters.get(i);
				if(args[0].equals("Status")){
					status = Integer.parseInt(args[1]);	
				}
				else if(args[0].equals("Filter")){
					//status = Integer.parseInt(args[1]);
					if(filter == null){
						filter = new Vector<Object[]>();
					}
					Object[] criterium = new Object[2];
					String[] args2 = args[1].split(":");
					
					if(args2.length==2){
						criterium[0] = args2[0];
						criterium[1] = args2[1].split(",");
						filter.add(criterium);
					}
				}
				else if(args[0].equals("Title")){
					title = args[1];
				}
			}
		//}
		html.append("<p class=\"displaytitle\">" + title + "</p>");
		html.append("<p class=\"displaybody\">");
		for(BasicClass bc : getObjects("NewsMessage")){

				boolean include = false;
				if(filter == null){
					include = true;
				}
				else{				
					for(Object[] criterium : filter){
						String[] args = (String[])criterium[1];
						boolean matches = false;
						for(String arg : args){
							if(bc.getString((String)criterium[0]).equals(arg)){
								matches = true;
							}
						}
						if(matches){
							include = true;	
						}
						else{
							include = false;	
							break;
						}
					}				
				}
				/*
				else if(bc.getID("Type")==3){
					include = true;
				}
				*/
				if(include){
					html.append("<div class=\"newsmessage\">");
					html.append("<b>" + DateConverter.sqlToShortDisplay(bc.getString("DateStart"), true) + " " + bc.getString("Title") + "</b>");
					html.append("<br>" + bc.getString("Description"));
					String url = bc.getString("URL");
					if(bc.getString("URLTitle").length() > 0){
						html.append("<p class=\"newsurl\"><a  class=\"newsurl\"href=\"" + url + "\" target=\"_blank\">" + bc.getString("URLTitle") + "</a></p>");
					}
					else{
						html.append("<p class=\"newsurl\"><a  class=\"newsurl\" href=\"" + url + "\" target=\"_blank\">" + url + "</a></p>");
					}
					
					//html.append(bc.getObjects("FileObjectData").size());
					
					if(bc.getObjects("FileObjectData").size() > 0){
						BasicClass attachment = (BasicClass)bc.getObjectByIndex("FileObjectData", 0);
						html.append("<p class=\"newsattachment\">Angehängte Datei : " + "<a href=\"javascript:downloadFile(" + attachment.getName() + ")\">" + attachment.getString("FileName") + "</a></p>");
					}
					html.append("</div>");
				}
			
		}
		
		
		return html.toString();	
	}
	
}