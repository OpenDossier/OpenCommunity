package ch.opencommunity.base;


import org.kubiki.base.Property;
import org.kubiki.base.BasicClass;
import org.kubiki.base.ConfigValue;
import org.kubiki.base.ActionResult;
import org.kubiki.base.ActionHandler;
import org.kubiki.application.ApplicationContext;
import org.kubiki.application.server.WebApplicationContext;

public class BaseController extends BasicClass implements ActionHandler{
	
	public BaseController(){
		
		
	}
	public ActionResult onAction(BasicClass source, String command, ApplicationContext context) {
		
		return null;
	}
	public void loadObject(String type, String id){

	}
	public void loadObject(BasicClass object, String id){

	}
	
}