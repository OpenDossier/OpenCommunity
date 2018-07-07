package ch.opencommunity.process;
 
import ch.opencommunity.server.*;
import ch.opencommunity.base.*;
import ch.opencommunity.advertising.*;
 
import org.kubiki.ide.*;
import org.kubiki.base.Property;
import org.kubiki.base.ProcessResult;
import org.kubiki.base.ConfigValue;
import org.kubiki.base.BasicClass;
import org.kubiki.base.ObjectCollection;
 
import org.kubiki.application.*;
import org.kubiki.application.server.WebApplicationContext;
 
import java.util.Vector;
import java.sql.*;

import javax.servlet.http.*;


public class MemberAdEditNode extends BasicProcessNode{
		
	MemberAd ma;
	MemberAdCategory mac, initialmac;
		
	public MemberAdEditNode(MemberAd ma){
			this.ma = ma;
			
			//Property p = addProperty("memberadid", "Integer", "", false, "Inserat");
			
			//Property p = addProperty("memberadid", "String", "");
			//setProperty("memberadid", ma);
			
	}
	public void setMemberAd(MemberAd ma){
		this.ma = ma;	
	}
	public MemberAd getMemberAd(){
		return ma;
	}
	public void setMemberAdCategory(MemberAdCategory mac){
		this.mac = mac;
	}
	public MemberAdCategory getMemberAdCategory(){
		return mac;
	}
	public void setInitialMemberAdCategory(MemberAdCategory initialmac){
		this.initialmac = initialmac;
	}
	public MemberAdCategory getInitialMemberAdCategory(){
		return initialmac;
	}
		
	public boolean validate(ApplicationContext context){
			
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		

		
		if(context.hasProperty("addnote")){
			if(ma.getObjects("Note").size()==0){
				Note note = new Note();
				note.addProperty("MemberAdID", "String", ma.getName());
				String id = ocs.insertObject(note);
				ocs.getObject(ma, "Note", "ID", id);
				addProperty("AdditionalInfo", "Text", "", false, "Zusatzinfo");
			}
			return false;
			
		}
		else if(context.hasProperty("Template") && mac != null && !mac.getName().equals(context.getString("Template"))){
				
			for(BasicClass bc  : mac.getObjects("FieldDefinition")){
				FieldDefinition fd = (FieldDefinition)bc;
				for(ConfigValue cv : fd.getCodeList()){
					removeProperty(bc.getName() + "_" + cv.getValue());
					//node1.removeProperty(bc.getName() + "_" + cv.getValue());
				}
			}	
			mac = (MemberAdCategory)ocs.getMemberAdAdministration().getObjectByName("MemberAdCategory", context.getString("Template"));
			if(mac != null){
				ma.setProperty("Template", mac);
					
				if(mac != null){
						setProperty("Template", mac);
						setMemberAdCategory(mac);
						Property p = null;
						for(BasicClass bc  : mac.getObjects("FieldDefinition")){
							FieldDefinition fd = (FieldDefinition)bc;
							for(ConfigValue cv : fd.getCodeList()){
								p = addProperty(bc.getName() + "_" + cv.getValue(), "Boolean", "" + ma.hasParameter(Integer.parseInt("" + cv.getValue())), false, cv.getLabel());
								getParent().addProperty(p);
							}
						}			
				}
			}
				
			return false;		
		}
		else{
			for(String name : getPropertySheet().getNames()){
				if(context.hasProperty(name)){
					setProperty(name, context.getString(name));	
				}
			}
			if(getParent() instanceof MemberAdActivate){
				if(getProcess().isLast(context)){
					
					boolean hasActiveAds = false;
					
					for(BasicClass node : getParent().getObjects("BasicProcessNode")){
						if(node.getInt("Status")==1){
							hasActiveAds = true;
						}
					}
					
					ocs.logAccess(hasActiveAds);
					
					if(hasActiveAds){
						((MemberAdActivate)getParent()).addLastNode();
						return false;
					}
					else{
						return true;	
					}
				}
				
			}	
			return true;
		}

	}
		
		
}