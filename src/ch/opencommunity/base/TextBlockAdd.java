package ch.opencommunity.base;

import ch.opencommunity.server.OpenCommunityServer;

import org.kubiki.ide.BasicProcess;
import org.kubiki.application.ApplicationContext;
import org.kubiki.base.ProcessResult;
import org.kubiki.base.Property;
import org.kubiki.base.PropertySheet;

import java.util.List;

public class TextBlockAdd extends BasicProcess{
	
	
	TextBlock tb;
	TextBlockAdministration tba;
	
	
	public TextBlockAdd(){
		
		addNode(this);
		
		addProperty("tb", "Object", "");
		
		setCurrentNode(this);
		
	}
	public void initProcess(){
		
		tba = (TextBlockAdministration)getParent();
		
		if(getObject("tb") instanceof TextBlock){
			tb = (TextBlock)getObject("tb");	
		}
		else{
			tb = new TextBlock();
			tb.setName("-1");
			tb.setParent(getParent());
			tb.initObjectLocal();
		}

		
	}
	public PropertySheet getPropertySheet(){
		if(tb != null){			
			return tb.getPropertySheet();	
		}
		else{
			return super.getPropertySheet();	
		}
		
	}
	public List<String> getPropertyNames(){
		if(tb != null){			
			return tb.getPropertyNames();	
		}
		else{
			return super.getPropertyNames();	
		}
		
	}
	public Property getProperty(String propertyName){
		if(tb != null){			
			return tb.getProperty(propertyName);	
		}
		else{
			return super.getProperty(propertyName);	
		}		
		
	}
	public void setProperty(String propertyName, Object value){
		if(tb != null){			
			tb.setProperty(propertyName, value);	
		}
		else{
			super.setProperty(propertyName, value);	
		}		
		
	}
	public void finish(ProcessResult result, ApplicationContext context){
		
		OpenCommunityServer ods = (OpenCommunityServer)getRoot();
		
		if(tb != null){
			
			if(tb.getName().equals("-1")){
				

				tb.setProperty("Owner", tba.getID("Owner"));
				tb.addProperty("TextBlockAdministrationID", "String", tba.getName());

				String sID = ods.insertObject(tb);
				tb = (TextBlock)ods.getObject(tba, "TextBlock", "ID", sID);

				
				
			}
			else{
				
				ods.updateObject(tb);
				
			}
			
			if (tb != null) {
				tba.getObjectCollection("TextBlock").sort("Title");
				result.setParam("dataContainer", "tree");
				result.setData(tba.getTextBlockList(context));
				tba.updateTextBlockReferences();
			}	
			
			
			
			
		}
		
		
	}
	
	
}