package ch.opencommunity.base;

import ch.opencommunity.server.OpenCommunityServer;
import ch.opencommunity.common.OpenCommunityUserSession;
import ch.opencommunity.dossier.CaseRecord;
import ch.opencommunity.dossier.Project;
import ch.opencommunity.base.FieldDefinition;

import org.kubiki.base.BasicClass;
import org.kubiki.base.ActionResult;
import org.kubiki.application.ApplicationContext;
import org.kubiki.application.server.WebApplicationContext;
import org.kubiki.base.UploadHandler;
import org.kubiki.database.TransactionHandler;

import java.io.OutputStream;
                             
import java.util.Vector;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;


public class Parameter extends BasicOCObject implements UploadHandler{
	public Parameter(){
		setTablename("Parameter");
		addProperty("Date", "Date", "");
		addProperty("Value", "Double", "");
		addProperty("Comment", "Text", "");
		addProperty("Document", "Integer", "");
		addProperty("ExternalDocument", "Integer", "");
	}
	public void initObjectLocal(){
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		if(getID("Document") > 0){
			if(getParent() instanceof Activity){
				Activity activity = (Activity)getParent();
				if(activity.getParent() instanceof OrganisationMember){
					OrganisationMember om = (OrganisationMember)activity.getParent();
					Document document = (Document)om.getObjectByName("Document", "" + getID("Document"));
					if(document != null){
						document.setReference(this);	
					}
				}
				else if(activity.getParent() instanceof CaseRecord){
					CaseRecord caseRecord = (CaseRecord)activity.getParent();
					Document document = (Document)caseRecord.getObjectByName("Document", "" + getID("Document"));
					if(document != null){
						document.setReference(this);	
					}
				}
				else if(activity.getParent() instanceof Project){
					Project project = (Project)activity.getParent();
					
					ocs.logAccess("Parent project : " + project);
					
					Document document = (Document)project.getObjectByName("Document", "" + getID("Document"));
					
					ocs.logAccess("Document : " + document);
					if(document != null){
						document.setReference(this);	
					}
				}
			}
		}
		addProperty("SortOrder", "Integer", "0");
	}
	public ActionResult onAction(BasicClass source, String command, ApplicationContext context) {
		
		ActionResult result = null;
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		if(command.equals("documentadd")){
			
			OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
			
			FieldDefinition fd = (FieldDefinition)getObject("Template");
			if(fd.getNumericID("DocumentTemplate") > 0){
				
				BasicClass parent = getParent().getParent(); //CaseRecord or Project
				
				String parentClass = parent.getClass().getSimpleName();
				
				DocumentTemplate template = (DocumentTemplate)fd.getObject("DocumentTemplate");
				

				
				if(template != null){
					
					Vector<String> modules = new Vector();
					for(BasicClass module : template.getObjects("DocumentTemplateModule")){
						//modules.add(module.getPath());	
						modules.add(module.getName());	
					}
					String[] templateModuleNames = modules.toArray(new String[modules.size()]);
					ocs.logAccess(templateModuleNames.length);
					//String[] templateModuleNames = modules.toArray();
				
					Document doc = (Document)template.createObject("ch.opencommunity.base.Document", null, context);
					
					doc.setProperty("Template", template.getName());
					
					doc.setProperty("Header", template.getID("Header"));
					doc.setProperty("Footer", template.getID("Footer"));

					doc.setProperty("DocumentClass", template.getString("DocumentClass"));
					doc.addProperty(parentClass + "ID", "String", parent.getName());
			
					String sID = ocs.insertObject(doc);
					
					setProperty("Document", sID);
					ocs.updateObject(this);
					
					ocs.logAccess(templateModuleNames);
					doc = (Document)ocs.getObject(parent, "Document", "ID", sID);
					doc.fillFromTemplate(template, templateModuleNames, userSession);
					doc.setReference(this);
					
					result = new ActionResult(ActionResult.Status.OK, "Dokument geladen");
					result.setParam("dataContainer", "input_" + getPath());
					result.setData("<input type=\"button\" class=\"actionbutton\" onClick=\"openDocument2('" + doc.getPath() + "')\" value=\"&Ouml;ffnen\">");
					
					
				}
				
			
			}
			else{
				result = ocs.startProcess("ch.opencommunity.process.DocumentAdd", userSession, null, context, this);	
			}
		}
		else if(command.equals("filedownload")){
				
			ocs.logAccess("filedownload : ");
				try{
					int docid = getID("ExternalDocument");
					if(docid >  1){
						Activity activity = (Activity)getParent();
						BasicClass parent = activity.getParent();
						ExternalDocument doc = (ExternalDocument)parent.getObjectByName("ExternalDocument", Integer.toString(docid));
						if(doc != null && doc.getObject("FileData") instanceof byte[]){
							byte[] fileData = (byte[])doc.getObject("FileData");
							WebApplicationContext webcontext = (WebApplicationContext)context;
							HttpServletResponse response = webcontext.getResponse();
	
							String filename = getString("Comment").toUpperCase();
							if(filename.endsWith(".PDF")){
								response.setContentType("application/pdf");
							}
							else if(filename.endsWith(".JPG")){
								response.setContentType("image/jpeg");
							}
							else if(filename.endsWith(".JPEG")){
								response.setContentType("image/jpeg");
							}
							else if(filename.endsWith(".PNG")){
								response.setContentType("image/png");
							}
							else if(filename.endsWith(".DOCX")){
								response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
							}
							OutputStream out = response.getOutputStream();	
							out.write(fileData);
							out.close();
							//writer.
							
						}
						else{
							result = new ActionResult(ActionResult.Status.FAILED, "Dokument nicht gefunden");	
						}
							
					}
					else{
						result = new ActionResult(ActionResult.Status.FAILED, "Dokument nicht gefunden");	
					}
			}
			catch(java.lang.Exception e){
				ocs.logException(e);
				result = new ActionResult(ActionResult.Status.FAILED, "Dokument nicht gefunden");	
			}
				

		}
		return result;
		
	}
	public Object handleUpload(ApplicationContext context, FileItem fileItem){
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		
		ActionResult result = null;
		
		int docid = getID("ExternalDocument");
		
		if(docid <  1){ //Dokument wurde noch nicht hochgeladen
			
			TransactionHandler transactionHandler = null;
			
			try{
				transactionHandler = ocs.startTransaction();
				
				ExternalDocument doc = new ExternalDocument();
				Activity activity = (Activity)getParent();
				BasicClass parent = activity.getParent();
				String parentIdName = parent.getClass().getSimpleName() + "ID";
				
				ocs.logAccess("parentid : " + parentIdName);
				
				doc.addProperty(parentIdName, "String", parent.getName());
				doc.setProperty("FileData", fileItem.get());
				
				String id = ocs.insertSimpleObject(transactionHandler, doc);
				
				setProperty("ExternalDocument", id);
				setProperty("Comment", fileItem.getName());
				ocs.updateObject(transactionHandler, this);
				
				ocs.getObject(transactionHandler.getConnection(), parent, "ExternalDocument", "ID", id);
				
				transactionHandler.commitTransaction();
				
				if(getParent() instanceof Activity){
					
					result = new ActionResult(ActionResult.Status.OK, "Datensatz gespeichert");	
					result.setParam("objectlist", getParent().getParent().getPath());
					result.setParam("dataContainer", "filename_" + getName());
					result.setData(getString("Comment") + " : <a href=\"servlet.srv?objectPath=" + getPath() + "&action=getObjectAction&command=filedownload\" target=\"_blank\">&Ouml;ffnen</a>");
					
				}
				else{
					result = new ActionResult(ActionResult.Status.OK, "Dokument gespeichert");
					result.setParam("dataContainer", "filename_" + getName());
					result.setData(getString("Comment") + " : <a href=\"servlet.srv?objectPath=" + getPath() + "&action=getObjectAction&command=filedownload\" target=\"_blank\">&Ouml;ffnen</a>");
				}
				
			}
			catch(java.lang.Exception e){
				transactionHandler.rollbackTransaction();
				ocs.logException(e);
			}
			
			
			
		}
		else{
			TransactionHandler transactionHandler = null;
			
			try{
				transactionHandler = ocs.startTransaction();	
				
				Activity activity = (Activity)getParent();
				BasicClass parent = activity.getParent();
				ExternalDocument doc = (ExternalDocument)parent.getObjectByName("ExternalDocument", Integer.toString(docid));
				doc.setProperty("FileData", fileItem.get());
				ocs.updateObject(transactionHandler, doc);

				setProperty("Comment", fileItem.getName());
				ocs.updateObject(transactionHandler, this);
				
				transactionHandler.commitTransaction();
				
				if(getParent() instanceof Activity){
					
					result = new ActionResult(ActionResult.Status.OK, "Datensatz gespeichert");	
					result.setParam("objectlist", getParent().getParent().getPath());
					
					result.setParam("dataContainer", "filename_" + getName());
					result.setData(getString("Comment") + " : <a href=\"servlet.srv?objectPath=" + getPath() + "&action=getObjectAction&command=filedownload\" target=\"_blank\">&Ouml;ffnen</a>");
					
				}
				else{
					result = new ActionResult(ActionResult.Status.OK, "Dokument gespeichert");
					result.setParam("dataContainer", "filename_" + getName());
					result.setData(getString("Comment") + " : <a href=\"servlet.srv?objectPath=" + getPath() + "&action=getObjectAction&command=filedownload\" target=\"_blank\">&Ouml;ffnen</a>");
				}
				
			}
			catch(java.lang.Exception e){
				transactionHandler.rollbackTransaction();
				ocs.logException(e);
			}
		}
		
		return result;
		
	}

}