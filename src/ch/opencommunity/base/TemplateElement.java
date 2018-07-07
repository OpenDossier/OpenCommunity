package ch.opencommunity.base;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.kubiki.application.ApplicationContext;
import org.kubiki.base.ActionResult;
import org.kubiki.base.BasicClass;
import org.kubiki.application.server.WebApplicationContext;

import ch.opencommunity.common.OpenCommunityUserSession;
import ch.opencommunity.base.Document.DocumentFormat;
import ch.opencommunity.office.WordDocument;
import ch.opencommunity.server.OpenCommunityServer;

public abstract class TemplateElement extends BasicDocument {
	private DocumentFormat documentFormat = DocumentFormat.PDF;
	
	public TemplateElement() {
		addProperty("IsExternalDocument", "Boolean", "false", true, "Externes Dokument");
	}
	
	public void initObjectLocal(){
		super.initObjectLocal();
		OpenCommunityServer ods = (OpenCommunityServer)getRoot();
		documentFormat = ods.getDocumentFormat();
		
		/*
		if (documentFormat == DocumentFormat.MSWORD) {
			addContextMenuEntry("uploadword", "Word hochladen", false);
			if (isExternal()) {
				getProperty("Content").setHidden(true);
				addContextMenuEntry("wordpreview", "Word öffnen", false);
			}
		}
		*/
		addDeleteContextMenuEntry();
		getProperty("Owner").setEditable(false); // TODO: Editierbar für Admins
	}
	
	public ActionResult onAction(BasicClass source, String command, ApplicationContext context) {
		
		OpenCommunityServer ods = (OpenCommunityServer)getRoot();
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
		ActionResult result = null;
		
		if(command.equals("wordpreview")){
			
			File file = new File(getFullFilename(true));
			
			result = new ActionResult(ActionResult.Status.OK, "Word-Dokument geöffnet");
			// send temporary file to client
			String tempID = getTempName();
			File tempFile = new File(ods.getTempPath(true) + tempID + getDocPostfix());
			ods.registerWordClientDocument(tempID, userSession, getPath());
			try {
				WordDocument.copyFile(file, tempFile);
			    
			    String webLink = ods.getTempPath(false) + getTempName() + getDocPostfix();
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
	

	public void saveTemplate(FileItem fileItem, WebApplicationContext context) throws Exception {
		setIsExternal(true);
		String strPath = getFullFilename(true);
		
		File file = new File(strPath);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		fileItem.write(file);
		
		saveObject((OpenCommunityUserSession)context.getObject("usersession"));
		getProperty("Content").setHidden(true);
		addContextMenuEntry("wordpreview", "Word Vorschau", false);
	}
	
	public void sendContentToWord(HttpServletResponse response) throws IOException {
		String fileName;
		if (isExternal()) {
			fileName = getFullFilename(true);
		}
		else {
			fileName = dumpXHTMLContent();
		}
		WordDocument.writeFileResponse(new File(fileName), response);		
	}
	
	public void sendEditableContentToWord(HttpServletResponse response, OpenCommunityUserSession userSession) throws IOException {
		OpenCommunityServer ods = (OpenCommunityServer)getRoot();
		String tempID = getTempName();
		ods.registerWordClientDocument(tempID, userSession, getPath());
		File tempFile = new File(ods.getTempPath(true) + tempID + getDocPostfix());
		String fileName;
		if (isExternal()) {
			fileName = getFullFilename(true);
		}
		else {
			fileName = dumpXHTMLContent();
		}
		File file = new File(fileName);
		WordDocument.copyFile(file, tempFile);	
		WordDocument.writeFileResponse(tempFile, response);		
	}	
	

	// documentFormat is unused here
	protected String getDocPath(boolean filesystem, DocumentFormat documentFormat) {
		return getDocPath(filesystem);
	}

	protected String getDocPath(boolean filesystem) {
		String separator = filesystem ? File.separator : "/";
		String docPath = "";
		if (filesystem) {
			docPath += ((OpenCommunityServer)getRoot()).getRootpath();
			docPath += separator;
		}
		docPath += "templates" + separator + "word" + separator + getDirectoryName() + separator;
		return docPath;
	}
	
	// documentFormat is unused here
	protected String getDocPostfix(DocumentFormat documentFormat) {
		return getDocPostfix();
	}
	
	protected String getDocPostfix() {
		if (isExternal()) {
			return ".docx";
		}
		else {
			return ".xhtml";
		}
	}	
	
	protected abstract String getDirectoryName();
	
	@Override
	public boolean isExternal() {
		return getBoolean("IsExternalDocument");
	}
	@Override
	public void setIsExternal(boolean isExternal) {
		getProperty("IsExternalDocument").setValue(isExternal);
	}
	
	@Override
	public boolean onWordClientAction(String action, HttpServletRequest request, HttpServletResponse response, 
			OpenCommunityUserSession userSession) throws IOException {
		boolean isHandled = false;
		if (action.equals("setContent")) {
			String content = request.getParameter("content");
			setProperty("Content", content);
			saveObject(userSession);
			isHandled = true;
		}
		if (!isHandled) {
			isHandled = super.onWordClientAction(action, request, response, userSession);
		}

		return isHandled;
	}
}
