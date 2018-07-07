package ch.opencommunity.process;

import java.util.List;
import java.util.Vector;

import org.kubiki.ide.BasicProcess;
import org.kubiki.ide.BasicProcessNode;

import ch.opencommunity.base.BasicOCObject;
import ch.opencommunity.base.DocumentTemplateModule;
import ch.opencommunity.server.OpenCommunityServer;
/*
import org.opendossier.dossier.Dossier;
import org.opendossier.dossier.CaseRecord;
import org.opendossier.dossier.DocumentTemplateModule;
import org.opendossier.server.OpenDossierServer;
*/
public abstract class BasicTemplateMixer extends BasicProcess {

	private int ownerID;
	
	public abstract List<BasicOCObject> getMandatoryModules();
	public abstract List<DocumentTemplateModule> getOptionalTemplateModules();
	
	public List<BasicOCObject> getOptionalDossierModules() {
		List<BasicOCObject> dossierModules = new Vector<BasicOCObject>();
		/*
		CaseRecord caseRecord = (CaseRecord)getParent("org.opendossier.dossier.CaseRecord");
		if (caseRecord != null) {

			Dossier dossier = (Dossier)caseRecord.getParent(); //08.07.2013
			if(dossier != null){
				dossierModules.addAll(dossier.getAllNotes());
			}

			dossierModules.addAll(caseRecord.getAllNotes());
		}
		*/
		return dossierModules;
	}
	
	class TemplateMixerNode extends BasicProcessNode {
		
		public TemplateMixerNode(){
			addProperty("ExternalTemplate", "String", "templateMixer.jsp");	
		}
		
		public void initObjectLocal(){
			OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
			getProperty("Template").setSelection(ocs.getTemplib(ownerID).getObjects("DocumentTemplate"));
		}
	}
	/*
	public void initProcess(ApplicationContext context){
		UserSession userSession = (UserSession)context.getObject("usersession");
		setOwner("" + userSession.getOrganisationID());

	}
	*/
	
	public void setOwnerID(int ownerID){
		this.ownerID = ownerID;
	}
	public int getOwnerID(){
		return ownerID;
	}
}
