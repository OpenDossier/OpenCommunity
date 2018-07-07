package ch.opencommunity.advertising;


import ch.opencommunity.base.*;
import ch.opencommunity.server.*;
import ch.opencommunity.view.MemberAdView;

import org.kubiki.base.Property;
import org.kubiki.base.BasicClass;
import org.kubiki.base.ConfigValue;
import org.kubiki.application.ApplicationContext;
import org.kubiki.base.ActionResult;

import org.kubiki.util.DateConverter;


import java.util.Vector;

import java.sql.*;

public class MemberAdController extends BasicClass{
	
	MemberAd memberAd = null;
	public MemberAdCategory mac;
	
	public MemberAdController(MemberAd memberAd, String template){
		this.memberAd = memberAd;
		
		OpenCommunityServer ocs = (OpenCommunityServer)memberAd.getRoot();
		
		setName(memberAd.getName());
		
		
		


		//addProperty("Template", "Integer", "" + memberAd.getID("Template"));
		addProperty(memberAd.getProperty("Template"));
		addProperty(memberAd.getProperty("Title"));
		addProperty(memberAd.getProperty("Type"));
		addProperty(memberAd.getProperty("Description"));
		addProperty(memberAd.getProperty("ValidFrom"));
		addProperty(memberAd.getProperty("ValidUntil"));
		addProperty(memberAd.getProperty("Status"));
		addProperty(memberAd.getProperty("Location"));
		addProperty(memberAd.getProperty("ExternalLink"));
		
		Vector status = new Vector();
		status.add(new ConfigValue("0","0","erfasst"));
		status.add(new ConfigValue("1","1","freigeschaltet"));
		status.add(new ConfigValue("3","3","inaktiv"));
		
		getProperty("Status").setSelection(status);
		
		if(memberAd.getInt("Priority") > 0){
			addProperty("AdOfTheWeek", "Boolean", "true", false, "Inserat der Woche");

		}
		else{
			addProperty("AdOfTheWeek", "Boolean", "false", false, "Inserat der Woche");
		}
		
		
		
		if(template != null && template.length() > 0){
			if(!("" + memberAd.getID("Template")).equals(template)){
				setProperty("Template", template);	
			}
		}
		
		getProperty("Template").setSelection(ocs.getMemberAdAdministration().getObjects("MemberAdCategory"));
		
		mac = (MemberAdCategory)getObject("Template");
			
		Property p = null;
			
		if(mac != null){

			for(BasicClass bc  : mac.getObjects("FieldDefinition")){
				FieldDefinition fd = (FieldDefinition)bc;
				if(fd.getID("Type")==1){
					Parameter parameter = memberAd.getParameterByTemplate(Integer.parseInt(fd.getName()));
					if(parameter != null){
						p = addProperty(fd.getName(), "Integer", "" + (int)parameter.getDouble("Value").doubleValue(), false, fd.getString("Title"));
						p.setSelection(fd.getCodeList());
					}
					else{
						p = addProperty(fd.getName(), "Integer", "" , false, fd.getString("Title"));
						p.setSelection(fd.getCodeList());

					}
				}
				else{
					p = addProperty(fd.getName() + "_label", "String", "" , false, fd.getString("Title"));
					p.setEditable(false);
					for(ConfigValue cv : fd.getCodeList()){
						String label = cv.getLabel();
						String[] args = label.split("/");
							
						String name = bc.getName() + "_" + cv.getValue();
							
						ocs.logAccess(name + ":" + memberAd.hasParameter(fd.getID(), Integer.parseInt("" + cv.getValue())));
							
						p = addProperty(name , "Boolean", "" + memberAd.hasParameter(fd.getID(),Integer.parseInt("" + cv.getValue())), false, args[0]);

					}
				}
			}			
		}
		
		Note note = (Note)memberAd.getObjectByIndex("Note", 0);
		if(note != null){
			addProperty("AdditionalInfo", "Text", note.getString("Content"), false, "Zusatzinfo");	
		}
		
		
	}
	public MemberAd getMemberAd(){
		return memberAd;
	}
	public static void saveMemberAd(MemberAd memberAd, ApplicationContext context){
		OpenCommunityServer ocs = (OpenCommunityServer)memberAd.getRoot();
		try{
			Connection con = ocs.getConnection();
			Statement stmt = con.createStatement();		
			stmt.execute("DELETE FROM Parameter WHERE MemberAdID=" + memberAd.getName());
			
			stmt.close();
			con.close();
				
			memberAd.setProperty("Title", context.getString("Title"));
			
			memberAd.setProperty("Description", context.getString("Description"));
			memberAd.setProperty("Type", context.getString("Type"));
			if(memberAd.getID("Type")==0){
				memberAd.setProperty("IsOffer", "true");
				memberAd.setProperty("IsRequest", "false");
			}
			else if(memberAd.getID("Type")==1){
				memberAd.setProperty("IsOffer", "false");
				memberAd.setProperty("IsRequest", "true");
			}
			else if(memberAd.getID("Type")==2){
				memberAd.setProperty("IsOffer", "true");
				memberAd.setProperty("IsRequest", "true");
			}
			memberAd.setProperty("ValidFrom", context.getString("ValidFrom"));
			memberAd.setProperty("ValidUntil", context.getString("ValidUntil"));
			memberAd.setProperty("Status", context.getString("Status"));
			memberAd.setProperty("Location", context.getString("Location"));
			memberAd.setProperty("ExternalLink", context.getString("ExternalLink"));
			memberAd.setProperty("DateModified", DateConverter.dateToSQL(new java.util.Date(), true));
			if(context.hasProperty("AdOfTheWeek") && context.getString("AdOfTheWeek").equals("true")){
				memberAd.setProperty("Priority", 1);	
			}
			else{
				memberAd.setProperty("Priority", 0);	
			}

			
			ocs.updateObject(memberAd);
			
			if(context.hasProperty("AdditionalInfo")){
			
				Note note = (Note)memberAd.getObjectByIndex("Note", 0);
				if(note != null){
					note.setProperty("Content", context.getString("AdditionalInfo"));
					ocs.updateObject(note);
				}
				
			}
			
			memberAd.getObjects("Parameter").clear();
																																
			MemberAdCategory mac = (MemberAdCategory)memberAd.getObject("Template");
				
			for(BasicClass bc  : mac.getObjects("FieldDefinition")){
				FieldDefinition fd = (FieldDefinition)bc;
				if(fd.getID("Type")==1){
					Parameter parameter = (Parameter)memberAd.createObject("ch.opencommunity.base.Parameter", null, context);
					parameter.addProperty("MemberAdID", "String", memberAd.getName());
					parameter.setProperty("Template", fd.getName());
					parameter.setProperty("Title", fd.getString("Title"));
					parameter.setProperty("Value", context.getString(fd.getName()));	
					String pid = ocs.insertObject(parameter);

					ocs.getObject(memberAd, "Parameter", "ID", pid);
				
				}
				else{
						
					for(ConfigValue cv : fd.getCodeList()){
						String name = bc.getName() + "_" + cv.getValue();
						ocs.logAccess(name);
						ocs.logAccess(context.getString(name));
						if(context.getString(name) != null && context.getString(name).equals("true")){
							Parameter parameter = (Parameter)memberAd.createObject("ch.opencommunity.base.Parameter", null, context);
							parameter.addProperty("MemberAdID", "String", memberAd.getName());
							parameter.setProperty("Template", fd.getName());
							parameter.setProperty("Title", fd.getString("Title"));
							parameter.setProperty("Value", cv.getValue());
							String pid = ocs.insertObject(parameter);
	
							ocs.getObject(memberAd, "Parameter", "ID", pid);
				
							
						
						}
					}
				}
	
				
			}
			

			
		}
		catch(java.lang.Exception e){
			ocs.logException(e);	
		}
		
	}
	
	
}