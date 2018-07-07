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
 
 
 public class GroupDefinitionAdd extends BasicProcess{
 	 
 	public GroupDefinitionAdd(){
 		
 		addNode(this);
 		
 		
 		setCurrentNode(this);
 		
 	}
 	
	public void finish(ProcessResult result, ApplicationContext context){
	
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		
		
		
		
	}
 	 
 	 
 }