package ch.opencommunity.base;

import ch.opencommunity.server.*;

import org.kubiki.base.BasicClass;
import org.kubiki.base.ActionResult;



import org.kubiki.application.ApplicationContext;

public class ScriptDefinition extends BasicOCObject{

	public ScriptDefinition(){

		addProperty("ScriptBody", "Code", "", false, "Skript");
		addProperty("OwnerClass", "String", "org.opendossier.dossier.Document", false, "Typ", 80);

	}
	public ActionResult onAction(BasicClass source, String command, ApplicationContext context) {
		
		ActionResult result = null;
		if(command.equals("saveobject")){
		
			saveObject(context);	
			initObjectLocal();
			result = new ActionResult(ActionResult.Status.OK, "Datensatz gespeichert");
			result.setParam("refresh", "tree");			
			//OpenDossierServer.checkReturnTarget(context.getString("returnTo"), result);
		}
		else {
			result = new ActionResult(ActionResult.Status.FAILED, "Befehl '" + command + "' nicht gefunden");
		}
		return result;
	}
	public void initObjectLocal(){
		super.initObjectLocal();
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		ocs.addScript(this);

	}
	
	public boolean isDocumentScript() {
		String ownerClass = getString("OwnerClass"); 
		return ownerClass.equals("Document") || ownerClass.equals("org.opendossier.dossier.Document") ||
				ownerClass.equals("Decree") || ownerClass.equals("org.opendossier.dossier.Decree");
		
	}
	public String getScriptBody(){
		return getString("ScriptBody");
	}

} 
