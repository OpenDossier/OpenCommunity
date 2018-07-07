package ch.opencommunity.advertising;

import ch.opencommunity.base.*;
import ch.opencommunity.server.*;

import org.kubiki.base.BasicClass;
import org.kubiki.cms.ImageObject;

import org.kubiki.base.ConfigValue;
import org.kubiki.application.ApplicationContext;

import java.util.Vector;

public class Advertisement extends BasicOCObject{
	public Advertisement(){
		
		setTablename("Advertisement");
		addProperty("Type", "Integer", "", false, "Typ");			
		addProperty("OrganisationalUnit", "Integer", "", false, "Organisation");
		addProperty("ImageObject", "Integer", "", false, "Bildobjekt");
		addProperty("ValidFrom", "DateTime", "", false, "Gültig von");
		addProperty("ValidUntil", "DateTime", "", false, "Gültig bis");		
		addProperty("Counter", "Integer", "0", true, "Zähler");		
		
		addProperty("Frequency", "Integer", "", false, "Frequenz");
		addProperty("Format", "Integer", "", false, "Format");
		addProperty("URL", "String", "", false, "Link URL", 255);
		
		addObjectCollection("ImageObject", "org.kubiki.cms.ImageObject");
	}
	public void initObjectLocal(){
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		getProperty("OrganisationalUnit").setSelection(ocs.getObjects("OrganisationalUnit"));	
		
		Vector types = new Vector();
		types.add(new ConfigValue("1", "1", "Bezahlte Anzeige"));
		types.add(new ConfigValue("2", "2", "Sponsor"));
		types.add(new ConfigValue("3", "3", "Befreundete Organisation"));
		types.add(new ConfigValue("4", "4", "Interner Link"));
		getProperty("Type").setSelection(types);	
		
		Vector formats = new Vector();
		formats.add(new ConfigValue("1", "1", "160x160"));
		formats.add(new ConfigValue("2", "2", "160x240"));
		formats.add(new ConfigValue("3", "3", "160x320"));
		formats.add(new ConfigValue("4", "4", "160x600"));
		getProperty("Format").setSelection(formats);	
		
		Vector status = new Vector();
		status.add(new ConfigValue("0", "0", "aktiv"));
		status.add(new ConfigValue("1", "1", "inaktiv"));
		getProperty("Status").setSelection(status);
		
		ImageObject img = (ImageObject)getObjectByIndex("ImageObject", 0);
		if(img != null){
			ocs.getCMS().cacheImageObject(img.getString("FileName"), img);
		}
	}
	public String getLabel(){
		return getString("OrganisationalUnit");	
	}
}