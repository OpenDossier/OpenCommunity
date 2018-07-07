package ch.opencommunity.base;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.kubiki.application.ApplicationContext;
import org.kubiki.base.ActionResult;
import org.kubiki.base.BasicClass;
import org.kubiki.base.ActionResult.Status;

import ch.opencommunity.common.OpenCommunityUserSession;
//import org.opendossier.dossier.BasicDossierObject; 
import ch.opencommunity.base.DocumentHeaderFooter.HeaderFooterType;
import ch.opencommunity.server.OpenCommunityServer;

public class DocumentTemplateLibrary extends BasicOCObject {
	
	public DocumentTemplateLibrary(){
		addObjectCollection("DocumentTemplate", "ch.opencommunity.base.DocumentTemplate");
		addObjectCollection("DocumentHeaderFooter", "ch.opencommunity.base.DocumentHeaderFooter");
		addContextMenuEntry("addtemplate", "Neue Vorlage", true);
		addContextMenuEntry("addheaderfooter", "Neue Kopf-/Fusszeilen", true);
	}
	
	public void initObjectLocal() {
		super.initObjectLocal();
		sortTemplates();
		sortHeaderFooter();
		getProperty("Owner").setEditable(false); // TODO: Editierbar für Admins
		OpenCommunityServer ods = (OpenCommunityServer)getRoot();
		//ods.logAccess("Number of Headers : " + getObjects("DocumentHeaderFooter").size());
	}

	public void sortTemplates() {
		getObjectCollection("DocumentTemplate").sort("Title");
	}

	public void sortHeaderFooter() {
		getObjectCollection("DocumentHeaderFooter").sort("Title");
		getObjectCollection("DocumentHeaderFooter").sort("Type");
	}

	public ActionResult onAction(BasicClass source, String command, ApplicationContext context) {
		
		ActionResult result = null;
		OpenCommunityServer ods = (OpenCommunityServer)getRoot();
		
		if(command.equals("addtemplate")){
			DocumentTemplate template = (DocumentTemplate)createObject("org.opendossier.dossier.DocumentTemplate", null, context);
			template.addProperty("DocumentTemplateLibraryID", "String", getName());
			template.setProperty("Owner", getID("Owner"));			
			String sID = ods.insertObject(template);

			template = (DocumentTemplate)ods.getObject(this, "DocumentTemplate", "ID", sID);
			
			if(template != null){
			      result = new ActionResult(Status.OK, "Neue Vorlage angelegt");
			      result.setParam("refresh","tree");
			      result.setParam("edit", template.getPath(""));
			}
		}
		else if(command.equals("addheaderfooter")){
			DocumentHeaderFooter headerFooter = (DocumentHeaderFooter)createObject("org.opendossier.dossier.DocumentHeaderFooter", null, context);
			headerFooter.addProperty("DocumentTemplateLibraryID", "String", getName());
			headerFooter.setProperty("Owner", getID("Owner"));			
			String sID = ods.insertObject(headerFooter);
			
			headerFooter = (DocumentHeaderFooter)ods.getObject(this, "DocumentHeaderFooter", "ID", sID);
			
			if(headerFooter != null){
			    result = new ActionResult(Status.OK, "Neue Kopf-/Fusszeilen angelegt");
			    result.setParam("refresh","tree");
			    result.setParam("edit", headerFooter.getPath(""));
			    updateHeaderFooterReferences();  
			}
		}
		else{
			return super.onAction(source, command, context);	
		}
		return result;
	}
	
	public List<DocumentHeaderFooter> getHeaders() {
	
		OpenCommunityServer ods = (OpenCommunityServer)getRoot();
		//ods.logAccess("Number of Headers 2: " + getObjects("DocumentHeaderFooter").size());
		
		List<DocumentHeaderFooter> headers = new Vector<DocumentHeaderFooter>();
		List<BasicClass> headersFooters = getObjects("DocumentHeaderFooter");
		
		for (BasicClass headerFooter : headersFooters) {
			if (headerFooter instanceof DocumentHeaderFooter && ((DocumentHeaderFooter)headerFooter).getType() == HeaderFooterType.HEADER) {
				headers.add((DocumentHeaderFooter)headerFooter);
			}
		}
		return headers;
		

	}
	
	public List<DocumentHeaderFooter> getFooters() {
		List<DocumentHeaderFooter> footers = new Vector<DocumentHeaderFooter>();
		List<BasicClass> headersFooters = getObjects("DocumentHeaderFooter");
		for (BasicClass headerFooter : headersFooters) {
			if (headerFooter instanceof DocumentHeaderFooter &&
					((DocumentHeaderFooter)headerFooter).getType() == HeaderFooterType.FOOTER) {
				footers.add((DocumentHeaderFooter)headerFooter);
			}
		}
		return footers;
	}
	
	public DocumentHeaderFooter getHeader(int headerID) {
		List<BasicClass> headersFooters = getObjects("DocumentHeaderFooter");
		OpenCommunityServer ods = (OpenCommunityServer)getRoot();
		ods.logAccess("Number of Headers 3: " + headerID + " "+ getObjects("DocumentHeaderFooter").size());
		for (BasicClass headerFooter : headersFooters) {
			if (headerFooter instanceof DocumentHeaderFooter &&
					((DocumentHeaderFooter)headerFooter).getType() == HeaderFooterType.HEADER &&
					headerFooter.getID() == headerID) {
				return (DocumentHeaderFooter)headerFooter;
			}
		}
		return null;
	}
	
	public DocumentHeaderFooter getFooter(int footerID) {
		List<BasicClass> headersFooters = getObjects("DocumentHeaderFooter");
		for (BasicClass headerFooter : headersFooters) {
			if (headerFooter instanceof DocumentHeaderFooter &&
					((DocumentHeaderFooter)headerFooter).getType() == HeaderFooterType.FOOTER &&
					headerFooter.getID() == footerID) {
				return (DocumentHeaderFooter)headerFooter;
			}
		}
		return null;
	}
	
	@Override
	public boolean onWordClientAction(String action, HttpServletRequest request,
			HttpServletResponse response, OpenCommunityUserSession userSession) throws IOException {
		OpenCommunityServer ods = (OpenCommunityServer)getRoot();	
		boolean isHandled = false;
		if (action.equals("addTemplate")) {
			PrintWriter writer = response.getWriter();
			
			DocumentTemplate template = new DocumentTemplate();
			template.setParent(this);
			template.setCreationInfo(userSession);
			template.setProperty("Owner", getID("Owner"));
			template.addProperty("DocumentTemplateLibraryID", "String", getName());
			template.setTitle(request.getParameter("title"));
	
			String sID = ods.insertObject(template);
			template = (DocumentTemplate)ods.getObject(this, "DocumentTemplate", "ID", sID);
			sortTemplates();
			writer.print(template.toXml());
			isHandled = true;
		}
		else if (action.equals("addHeaderFooter")) {
			PrintWriter writer = response.getWriter();
			
			DocumentHeaderFooter headerFooter = new DocumentHeaderFooter();
			headerFooter.setParent(this);
			headerFooter.setCreationInfo(userSession);
			headerFooter.setProperty("Owner", getID("Owner"));
			headerFooter.addProperty("DocumentTemplateLibraryID", "String", getName());
			headerFooter.setTitle(request.getParameter("title"));
			headerFooter.setProperty("Type", request.getParameter("type"));
	
			String sID = ods.insertObject(headerFooter);
			headerFooter = (DocumentHeaderFooter)ods.getObject(this, "DocumentHeaderFooter", "ID", sID);
			sortHeaderFooter();
			
			updateHeaderFooterReferences();
			
			String tempID = headerFooter.getTempName();
			ods.registerWordClientDocument(tempID, userSession, headerFooter.getPath());
			headerFooter.addProperty("filename", "String", tempID + ".docx", true, "");
			writer.print(headerFooter.toXml());
			isHandled = true;
		}
		if (!isHandled) {
			isHandled = super.onWordClientAction(action, request, response, userSession);
		}

		return isHandled;
	}
	
    private void updateHeaderFooterReferences() {  
		// Update header/footer selection in all templates
		List<BasicClass> documentTemplates = getObjects("DocumentTemplate");
		for (BasicClass template : documentTemplates) {
			((DocumentTemplate)template).initHeadersFooters(this);
		}
    }

}