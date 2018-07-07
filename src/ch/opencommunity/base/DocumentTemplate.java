package ch.opencommunity.base;

import ch.opencommunity.base.Document.DocumentFormat;
import ch.opencommunity.util.PDFCreator;
import ch.opencommunity.office.WordDocument;
import ch.opencommunity.base.DocumentHeaderFooter.HeaderFooterType;
import ch.opencommunity.server.OpenCommunityServer;
import ch.opencommunity.common.OpenCommunityUserSession;

import org.kubiki.application.ApplicationContext;
import org.kubiki.base.ActionResult;
import org.kubiki.base.BasicClass;
import org.kubiki.base.ActionResult.Status;
import org.kubiki.base.ObjectCollection;

import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Hashtable;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DocumentTemplate extends BasicOCObject{ 
	private DocumentFormat documentFormat = DocumentFormat.PDF;
	private String tempName;
	
	public DocumentTemplate(){
		setTablename("DocumentTemplate");
		
		addObjectCollection("DocumentTemplateModule", "ch.opencommunity.base.DocumentTemplateModule");
		
		addContextMenuEntry("addmodule", "Neues Modul", true);		
		addProperty("Header", "Integer", "", false, "Kopfzeilen");
		addProperty("Footer", "Integer", "", false, "Fusszeilen");
		addProperty("DocumentClass", "String", "", false, "Dokumentenklasse", 120);
		addProperty("SignatureRule","Integer","0", false, "Unterschriftenregelung");
		addProperty("HasAddressFields","Boolean","true", false, "Hat Adressfelder");
	}
	
	public void initObjectLocal(){
		super.initObjectLocal();

		OpenCommunityServer ods = (OpenCommunityServer)getRoot();
		documentFormat = ods.getDocumentFormat();
		if (documentFormat == DocumentFormat.PDF) {
			addContextMenuEntry("pdfpreview", "PDF Vorschau", false);
		}
		else if (documentFormat == DocumentFormat.MSWORD) {
			addContextMenuEntry("wordpreview", "Word Vorschau", false);
		}
		
		BasicClass parent = getParent();
		if (parent instanceof DocumentTemplateLibrary) {
			ods.logAccess("initializing headers ...");
			initHeadersFooters((DocumentTemplateLibrary)parent);
		}
		getRoot().addSubobject("DocumentTemplate", this);
		
		ObjectCollection modules = getObjectCollection("DocumentTemplateModule");
		// Sort
		modules.sort("SortOrder");
		// Normalize sort indexes
		int sortOrder = 1;
		for (BasicClass module : modules.getObjects()) {
			module.setProperty("SortOrder", sortOrder++);
		}
		//getProperty("SignatureRule").setSelection(ods.getCode("Unterschriftenregelung").getCodeList());
		
		addDeleteContextMenuEntry();
		
		getProperty("Owner").setEditable(false); // TODO: Editierbar für Admins
		getProperty("Status").setHidden(false); 
		try{
			//getProperty("Status").setSelection(ods.getCode("Objektstatus").getCodeList());
		}
		catch(java.lang.Exception e){
			ods.logException(e);
		}
	}
	
	public void initHeadersFooters(DocumentTemplateLibrary templateLibrary) {
		OpenCommunityServer ods = (OpenCommunityServer)getRoot();
		//ods.logAccess("Headers: " + templateLibrary.getHeaders().size());
		//ods.logAccess("Footers: " + templateLibrary.getFooters().size());
		getProperty("Header").setSelection(templateLibrary.getHeaders());
		getProperty("Footer").setSelection(templateLibrary.getFooters());
	}
	
	public ActionResult onAction(BasicClass source, String command, ApplicationContext context) {
		
		ActionResult result = null;
		OpenCommunityServer ods = (OpenCommunityServer)getRoot();
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
		
		if(command.equals("addmodule")){
			
			DocumentTemplateModule module = (DocumentTemplateModule)createObject("org.opendossier.dossier.DocumentTemplateModule", "DocumentTemplateModule", context);
			module.addProperty("DocumentTemplateID", "String", getName());
			
			int maxSort = 0;
			BasicClass lastModule = getObjectCollection("DocumentTemplateModule").getLastObject();
			if (lastModule != null) {
				maxSort = lastModule.getInt("SortOrder");
			}
			module.setProperty("SortOrder", maxSort + 1);

			String sID = ods.insertObject(module);
			module = (DocumentTemplateModule)ods.getObject(this, "DocumentTemplateModule", "ID", sID);

			if(module != null){
			      result = new ActionResult(Status.OK, "Neues Modul angelegt");
			      result.setParam("refresh","tree");
			      result.setParam("edit", module.getPath(""));
			}
		}
		else if(command.equals("pdfpreview")){
			
			String pdfPath = toPDF(new Hashtable<String, String>());
			
			result = new ActionResult(ActionResult.Status.OK, "PDF erstellt");
			result.setParam("exec", "window.open('" + pdfPath + "', '_blank')");
		}
		else if(command.equals("wordpreview")){
			String tempID = getTempName();
			File tempFile = new File(ods.getTempPath(true) + tempID + ".docx");
			try {
				File blankFile = new File(((OpenCommunityServer)getRoot()).getRootpath() + "/templates/word/blank.docx");
				WordDocument.copyFile(blankFile, tempFile);
				
				result = new ActionResult(ActionResult.Status.OK, "Word geöffnet");
				ods.registerWordClientDocument(tempID, userSession, getPath());
				// send temporary file to client
			    String webLink = ods.getTempPath(false) + tempID + ".docx";
				result.setParam("exec", "window.open('" + webLink + "', '_blank')");
			}
			catch (IOException e) {
				writeError(e);
				result = new ActionResult(ActionResult.Status.FAILED, "Öffnen des Dokuments fehlgeschlagen");
			}
		}
		else{
			return super.onAction(source, command, context);	
		}
		return result;
	}
	public String toPDF(Map<String, String> ht) {
		OpenCommunityServer ods = (OpenCommunityServer)getRoot();
		String pdfPath = "temp/" + getName() + ".pdf";
		String strPath = ods.getRootpath() + "/temp/" + getName() + ".pdf";
		try {
			File file = new File(strPath);
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}

			String strHTML = toHtml(false);

		
			String rootPath = ((OpenCommunityServer)getRoot()).getRootpath();
			PDFCreator app = new PDFCreator(rootPath);
			app.setParent(this);
			
			String strHeader = getString("Header");
			if (strHeader.isEmpty()) {
				strHeader = "default.html";
			}
			String strFooter = getString("Footer");
			if (strFooter.isEmpty()) {
				strFooter = "default.html";
			}

			// create the document
			app.addTemplate("xhtml/docHeader.html");
			app.addTemplate("xhtml/header/" + strHeader);
			app.addSnippet(strHTML);			
			app.addTemplate("xhtml/footer/" + strFooter);
			app.addTemplate("xhtml/docFooter.html");

			// maybe fill in some blanks

			if (ht != null) {
				app.setTags(ht);
			}

			// now save the PDF in the specified path

			app.generate(strPath);
		}
		catch (Exception e) {
			ods.writeError(e);
		}
		return pdfPath;
	}
	public String toString(){
		return getString("Title");
	}
	public String toHtml(boolean isEditable) {
		StringBuffer html = new StringBuffer();
		Vector<BasicClass> modules = getObjectCollection("DocumentTemplateModule").getObjects();
		html.append("<div id=\"document_" + getPath("") + "\">");
		for(BasicClass bc : modules){
			DocumentTemplateModule templateModule = (DocumentTemplateModule)bc;
			html.append(templateModule.getContent());
		}

		html.append("</div>");
		return html.toString();
	}
	
	private String getTempName() {
		if (tempName == null) {
			tempName = "odtemplate_" + Double.toString(Math.random()).substring(2);
		}
		return tempName;
	}
	
	public DocumentHeaderFooter getHeaderFooter(HeaderFooterType type) {
		DocumentTemplateLibrary dtl = (DocumentTemplateLibrary)getParent("ch.opencommunity.base.DocumentTemplateLibrary");
		if (dtl != null) {
			if (type == HeaderFooterType.HEADER) {
				return dtl.getHeader(getID("Header"));
			}
			else {
				return dtl.getFooter(getID("Footer"));
			}
		}
		return null;
	}
	

	@Override
	public boolean onWordClientAction(String action, HttpServletRequest request, HttpServletResponse response, OpenCommunityUserSession userSession) throws IOException {
		OpenCommunityServer ods = (OpenCommunityServer)getRoot();		
		boolean isHandled = false;
		if (action.equals("getHeader") || action.equals("getFooter")) {
			DocumentHeaderFooter headerFooter = null;
			if (action.equals("getHeader")) {
				headerFooter = getHeaderFooter(HeaderFooterType.HEADER);
			}
			else {
				headerFooter = getHeaderFooter(HeaderFooterType.FOOTER);
			}
			if (headerFooter != null) {
				headerFooter.sendContentToWord(response);
				isHandled = true;
			}
		}
		else if (action.equals("getModules")) {
			PrintWriter writer = response.getWriter();
			List<BasicClass> modules = getObjects("DocumentTemplateModule");
			for (BasicClass module : modules) {
				writer.print(module.getPath());
				writer.print("\n");
			}
			isHandled = true;
		}
		else if (action.equals("getTemplatePreview")) {
			String tempID = getTempName();
			String tempFileName = ods.getTempPath(true) + tempID + ".docx";
			File tempFile = new File(tempFileName);
			try {
				File blankFile = new File(((OpenCommunityServer)getRoot()).getRootpath() + "/templates/word/blank.docx");
				WordDocument.copyFile(blankFile, tempFile);
				ods.registerWordClientDocument(tempID, userSession, getPath());
			}
			catch (IOException e) {
				writeError(e);
			}
			WordDocument.writeFileResponse(tempFile, response);
			isHandled = true;
		}	
		else if(action.equals("addModule")){
			PrintWriter writer = response.getWriter();
			
			DocumentTemplateModule module = new DocumentTemplateModule();
			module.setParent(this);
			module.setCreationInfo(userSession);
			module.setProperty("Owner", getID("Owner"));
			module.addProperty("DocumentTemplateID", "String", getName());
			module.setTitle(request.getParameter("title"));

			String sID = ods.insertObject(module);
			module = (DocumentTemplateModule)ods.getObject(this, "DocumentTemplateModule", "ID", sID);
			String tempID = module.getTempName();
			ods.registerWordClientDocument(tempID, userSession, module.getPath());
			ods.logAccess(module.getPath());
			module.addProperty("filename", "String", tempID + ".docx", true, "");
			writer.print(module.toXml());
			isHandled = true;
		}
		if (!isHandled) {
			isHandled = super.onWordClientAction(action, request, response, userSession);
		}
		
		return isHandled;
	}
	
	@Override
	public void setProperty(String name, Object value) {
		super.setProperty(name, value);
		if (name.equals("Title")) {
			if (getParent() instanceof DocumentTemplateLibrary) {
				((DocumentTemplateLibrary)getParent()).sortTemplates();
			}
		}
	}
	
}
