package ch.opencommunity.dossier;

import org.kubiki.base.BasicClass;
import org.kubiki.base.ActionHandler;
import org.kubiki.base.ActionResult;
import org.kubiki.application.ApplicationContext;
import org.kubiki.application.DataObject;

public class CaseRecord extends DataObject{
	
	public CaseRecord(){
		setTablename("CaseRecord");
		
		addObjectCollection("ObjectDetail", "ch.opendossier.dossier.ObjectDetail");
		addObjectCollection("Activity", "ch.opendossier.base.Activity");
		addObjectCollection("Document", "ch.opendossier.base.Document");
		addObjectCollection("ExternalDocument", "ch.opencommunity.base.ExternalDocument");
		
	}
	public ActionResult onAction(BasicClass source, String command, ApplicationContext context) {
		
		ActionResult result = null;
		
		return result;
		
	}
	
}