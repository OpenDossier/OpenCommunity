package ch.opencommunity.process;


import ch.opencommunity.advertising.*;
import ch.opencommunity.common.*;
import ch.opencommunity.base.*;
import ch.opencommunity.server.OpenCommunityServer;
import ch.opencommunity.server.HTMLForm;
import ch.opencommunity.dossier.*;
 
import org.kubiki.ide.BasicProcess;
import org.kubiki.ide.BasicProcessNode;
import org.kubiki.base.Property;
import org.kubiki.base.ProcessResult;
import org.kubiki.base.ObjectCollection;
import org.kubiki.base.BasicClass;
import org.kubiki.database.Record;


import org.kubiki.application.*;

import org.kubiki.libreoffice.*;

import java.util.Vector;
import java.util.List; 
 
public class DocumentAdd extends BasicTemplateMixer{
 
	BasicProcessNode node1, node2;
	OrganisationMember om;
	
	LibreOfficeModule libreOfficeModule; 
 
	public DocumentAdd(){
		setTitle("Dokument erstellen");

		BasicProcessNode node1 = addNode();
		
		node2 = new TemplateMixerNode();
		//addNode(node2);
		
		Property p = addProperty("Template","String","", false, "Vorlage");
		node1.addProperty(p);
		node2.addProperty(p);	
		
		p = addProperty("Title", "String", "", false, "Betreff");
		node1.addProperty(p);
		
		p = addProperty("Recipient", "String", "", false, "Empfänger");
		node1.addProperty(p);
		
		p = addProperty("QueryDefinition", "Integer", "", false, "Abfrage");
		node1.addProperty(p);

	
		addProperty("DocID","String","", true, "");
	
		setCurrentNode(node1);
	
	}
	public void finish(ProcessResult result, ApplicationContext context){
		
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
				
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();

		BasicClass parent = (BasicClass)getParent();
		
		//DocumentTemplate template = (DocumentTemplate)getObject("Template");
		
		LibreOfficeTemplate template = (LibreOfficeTemplate)getObject("Template");
		
		//Document doc = (Document)template.createObject("ch.opencommunity.base.Document", null, context);
		
		Document doc = new Document();
		
		doc.mergeProperties(this);
		
		doc.setProperty("Template", template.getName());
		doc.setProperty("Type", 3);
		
		String sID = "";
		
		if(getParent() instanceof Parameter){
			
			Parameter parameter = (Parameter)getParent();
			
			OrganisationMember om = (OrganisationMember)parameter.getParent("ch.opencommunity.base.OrganisationMember");
			
			if(om != null){
				
				doc.addProperty("OrganisationMemberID", "String", om.getName());
			
				sID = ocs.insertObject(doc);
					
				parameter.setProperty("Document", sID);
					
				ocs.updateObject(parameter);
		
				doc = (Document)ocs.getObject(om, doc.getTablename(), "ID", sID);
				
			}
			
		}
		
	}
	public void finish2(ProcessResult result, ApplicationContext context){

		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
				
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();

		BasicClass parent = (BasicClass)getParent();
		
		DocumentTemplate template = (DocumentTemplate)getObject("Template");
		
		Document doc = (Document)template.createObject("ch.opencommunity.base.Document", null, context);
		
		doc.mergeProperties(this);
		
		doc.setProperty("Template", template.getName());
		
		String[] templateModuleNames = context.getString("templateModules").split(HTMLForm.LIST_WIDGET_SEPARATOR);
		
		String sID = "";
		
		if(getParent() instanceof Parameter){
			Parameter parameter = (Parameter)getParent();
			
			OrganisationMember om = (OrganisationMember)getParent("ch.opencommunity.base.OrganisationMember");
			
			Dossier dossier = (Dossier)getParent("ch.opencommunity.dossier.Dossier");
			CaseRecord caseRecord = (CaseRecord)getParent("ch.opencommunity.dossier.CaseRecord");
			Project project = (Project)getParent("ch.opencommunity.dossier.Project");
			
			if(dossier != null && caseRecord != null){
			
				if (template != null) {
					
					doc.setProperty("Header", template.getID("Header"));
					doc.setProperty("Footer", template.getID("Footer"));
					doc.setProperty("DocumentClass", template.getString("DocumentClass"));
					doc.addProperty("CaseRecordID", "String", caseRecord.getName());
		
					sID = ocs.insertObject(doc);
					
					parameter.setProperty("Document", sID);
					
					ocs.updateObject(parameter);
		
					doc = (Document)ocs.getObject(caseRecord, doc.getTablename(), "ID", sID);
					doc.fillFromTemplate(template, templateModuleNames, userSession);
					
					result.setParam("opendossier", dossier.getName());
					
				}
				
			}
			else if(dossier != null && project != null){
			
				if (template != null) {
					
					doc.setProperty("Header", template.getID("Header"));
					doc.setProperty("Footer", template.getID("Footer"));
					doc.setProperty("DocumentClass", template.getString("DocumentClass"));
					doc.addProperty("ProjectID", "String", project.getName());
		
					sID = ocs.insertObject(doc);
					
					parameter.setProperty("Document", sID);
					
					ocs.updateObject(parameter);
					
					Activity activity = (Activity)parameter.getParent();
		
					doc = (Document)ocs.getObject(project, doc.getTablename(), "ID", sID);
					doc.fillFromTemplate(template, templateModuleNames, userSession);
					doc.setReference(parameter);
					
					result.setParam("exec", "onAction('/usersession/DossierController:" + dossier.getName() + "','editactivity','','projectid=" + project.getName() + "~activityid=" + activity.getName() + "')");
					
				}
				
			}
			
			
		}
		else{
			
		
			if (template != null) {
				doc.setProperty("Header", template.getID("Header"));
				doc.setProperty("Footer", template.getID("Footer"));
				doc.setProperty("DocumentClass", template.getString("DocumentClass"));
	
				sID = ocs.insertObject(doc);
				setProperty("DocID", sID);
	
				doc = (Document)ocs.getObject(parent, doc.getTablename(), "ID", sID);
				doc.fillFromTemplate(template, templateModuleNames, userSession);
				
				result.setParam("refresh", "documents");
			}
			
		}

		
	}
	public void initProcess(ApplicationContext context){
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		libreOfficeModule = (LibreOfficeModule)ocs.getApplicationModule("libreoffice");
		
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
		setOwnerID(userSession.getOrganisationID());
		
		
		getProperty("Template").setSelection(libreOfficeModule.getObjects("LibreOfficeTemplate"));
		
		getProperty("Recipient").setSelection(ocs.getRecipientList());
		
		getProperty("QueryDefinition").setSelection(ocs.getObjects("QueryDefinition"));
	}
	
	public void initProcess2(ApplicationContext context){
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
		setOwnerID(userSession.getOrganisationID());
		getProperty("Template").setSelection(ocs.getTemplib(getOwnerID()).getObjects("DocumentTemplate"));
		
		getProperty("Recipient").setSelection(ocs.getRecipientList());
		
		getProperty("QueryDefinition").setSelection(ocs.getObjects("QueryDefinition"));
	}
	
	@Override
	public List<BasicOCObject> getMandatoryModules() {
		List<DocumentTemplateModule> templateModules = getTemplateModules(true);
		List<BasicOCObject> mandatoryModules = new Vector<BasicOCObject>();
		mandatoryModules.addAll(templateModules);
		return mandatoryModules;
	}


	@Override
	public List<DocumentTemplateModule> getOptionalTemplateModules() {
		return getTemplateModules(false);
	}
	
	public List<DocumentTemplateModule> getTemplateModules(boolean mandatory) {
		List<DocumentTemplateModule> templateModules = new Vector<DocumentTemplateModule>();
		
		Object object = getObject("Template");
		if (object instanceof DocumentTemplate){
		    DocumentTemplate template = (DocumentTemplate)object;
			ObjectCollection oc = template.getObjectCollection("DocumentTemplateModule");
			if (oc != null) {
				Vector<BasicClass> modules = oc.getObjects();
				for(BasicClass bcModule : modules) {
					DocumentTemplateModule module = (DocumentTemplateModule)bcModule;
					if (module.getBoolean("IsMandatory") == mandatory) {
						templateModules.add(module);
					}
				}
			}
		}
		return templateModules;
	}
	
}
