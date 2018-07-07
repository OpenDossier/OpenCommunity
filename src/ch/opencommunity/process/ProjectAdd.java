 package ch.opencommunity.process;
 
 import ch.opencommunity.server.*;
 import ch.opencommunity.base.*;
 import ch.opencommunity.dossier.DossierController;
 import ch.opencommunity.dossier.Project;
 
 import org.kubiki.ide.BasicProcess;
 import org.kubiki.base.Property;
 import org.kubiki.base.ProcessResult;
 import org.kubiki.base.ConfigValue;
 
 import org.kubiki.application.*;
 import org.kubiki.util.DateConverter;
 
 import java.util.Vector;
 
 
 public class ProjectAdd extends BasicProcess{

 
	public ProjectAdd(){
		addNode(this);
		
		addProperty("Title", "String", "", false, "Bezeichnung");
		addProperty("DateStarted", "Date", "0", false, "Datum Beginn");
		addProperty("Description", "Text", "", false, "Beschreibung");
		addProperty("Type", "Integer", "0", true, "Projekttyp");
		
		setCurrentNode(this);
		
	}
	public void initProcess(){
		Vector type = new Vector();
		type.add(new ConfigValue("0","0","Spendengesuch"));
		type.add(new ConfigValue("1","1","Unterstütztes Projekt"));		
		getProperty("Type").setSelection(type);
	}
	public void finish(ProcessResult result, ApplicationContext context){
	
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		
		DossierController dossierController = (DossierController)getParent();
		
		Project project = (Project)ocs.createObject("ch.opencommunity.dossier.Project", null, context);
		project.mergeProperties(this);
		project.addProperty("DossierID", "String", dossierController.getDossier().getName());
		String id = ocs.insertSimpleObject(project);	
		

		
		result.setParam("objectlist", dossierController.getPath());
		dossierController.loadObject(project, id);
	}
 }