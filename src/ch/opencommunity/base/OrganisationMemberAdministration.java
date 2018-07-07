package ch.opencommunity.base;

import org.kubiki.base.BasicClass;
import org.kubiki.base.ObjectCollection;
import org.kubiki.base.ActionResult;
import org.kubiki.application.server.WebApplicationModule;
import org.kubiki.application.server.ApplicationServer;
import org.kubiki.application.ApplicationContext;
import org.kubiki.database.DataStore;
import org.kubiki.gui.html.HTMLFormManager;

public class OrganisationMemberAdministration extends WebApplicationModule{
	
	ApplicationServer server;
	
	HTMLFormManager formManager;
	
	
	public OrganisationMemberAdministration(){
	
		addObjectCollection("OrganisationMemberType", "ch.opencommunity.base.OrganisationMemberType");
		
		addContextMenuEntry("organisationmembertypeadd");
		
	
	}
	@Override
	public void initData(DataStore dataStore){
		
		server = (ApplicationServer)getRoot();	
		formManager = server.getFormManager();
		
		ObjectCollection results = new ObjectCollection("Results", "*");
		server.queryData("SELECT t1.ID FROM OrganisationMemberType AS t1 ORDER BY t1.SortOrder", results);
		for(BasicClass record : results.getObjects()){
			String id = record.getString("ID");
			server.getObject(this, "OrganisationMemberType", "ID", id);
		}
		

		
		
		
		
	}
	@Override
	public ActionResult onAction(BasicClass src, String command, ApplicationContext context){
		
		ActionResult result = null;
		
		if(command.equals("organisationmembertypeadd")){
			
			OrganisationMemberType omt = new OrganisationMemberType();
			String id = server.insertObject(omt);
			server.getObject(this, "OrganisationMemberType", "ID", id);
			
			result = new ActionResult(ActionResult.Status.OK, "Objekt erstellt");
			result.setParam("dataContainer", "tree");
			result.setData(formManager.getObjectTree(this, null));
			
		}
		
		return result;
		
	}
	
	@Override
	public String getMainForm(ApplicationContext context){
		
		StringBuilder html = new StringBuilder();
		
		html.append(formManager.getToolbar(this, null, getPath(), context, true));
		
		html.append("<div id=\"tree\">");
		
		html.append(formManager.getObjectTree(this, null));
		
		html.append("</div>");
		
		html.append("<div id=\"objectEditArea\" style=\"left : 300px;\">");
		
		html.append("</div>");
		
		
		return html.toString();
		
	}
	public Person createPerson(){
		Person person = new Person();
		person.setName(server.createPassword(8));
		
		Identity identity = new Identity();
		identity.setParent(person);
		identity.setName(server.createPassword(8));
		person.addSubobject("Identity", identity);
		
		Address address = new Address();
		address.setParent(person);
		address.setName(server.createPassword(8));
		person.addSubobject("Address", address);
		
		return person;
		
	}
	
}