package ch.opencommunity.mail;

import org.kubiki.ide.BasicProcess;
import org.kubiki.ide.BasicProcessNode;
import org.kubiki.base.Property;
import org.kubiki.base.ProcessResult;
import org.kubiki.base.BasicClass;
import org.kubiki.base.ConfigValue;
import org.kubiki.base.ObjectCollection;
import org.kubiki.application.*;
import org.kubiki.mail.*;
import org.kubiki.groupware.*;


import ch.opencommunity.server.*;
import ch.opencommunity.base.*;
import ch.opencommunity.common.OpenCommunityUserSession;

import java.io.*;
import java.util.Vector;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import microsoft.exchange.webservices.data.*;

public class ImportMail extends BasicProcess{

	BasicProcessNode node1, node2;

	Object message;

	AbstractEmailAdministration ea = null;

	MessageWrapper wrapper = null;

	Vector templates;
	
	OrganisationMember om;

	public ImportMail(){

		node1 = new BasicProcessNode(){
			public boolean validate(ApplicationContext context){

				if(context.hasProperty("messageid")){
					try{
						Object message = ea.getMessageByID(context.getString("messageid"));
						wrapper = ea.getMessageWrapper(message);
						int i = 0;
						for(Object o : wrapper.getAttachments()){
							i++;
							getParent().addProperty("attachment_" + i, "Boolean", "false");
							getParent().addProperty("attachmenttitle_" + i, "String", "false");
						}
						getParent().setProperty("AttachmentOnly", "true");
						return true;
					}
					catch(java.lang.Exception e){
						return false;
					}
				}
				else{
					return false;
				}

			}

		};
		Property p = addProperty("EmailAdministration", "Object", "");
		node1.addProperty(p);
		

		node2 = new ImportMailNode();
		
		p = addProperty("OMID", "String", "");
		node2.addProperty(p);
		
		p =addProperty("EmailMessage", "Object", "");
		node2.addProperty(p);
		addProperty("EmailAdministration", "Object", "");
		addProperty("CaseRecordID", "String", "");
		addProperty("MeasureID", "String", "");
		addProperty("OrganisationID", "String", "");
		addProperty("CaseRecordTitle", "String", "");
		addProperty("ActivityTemplate", "String", "");
		addProperty("AttachmentOnly", "Boolean", "false");

		addProperty("TargetFolder", "String", "");





	}
	public MessageWrapper getMessageWrapper(){
		return wrapper;
	}
	public Vector getTemplates(){
		return templates;
	}
	public AbstractEmailAdministration getEmailAdministration(){
		return ea;
	}
	public void initProcess(){
		OpenCommunityServer ods = (OpenCommunityServer)getRoot();
		String sender = null;
		if(getObject("EmailAdministration") instanceof AbstractEmailAdministration){
			ea = (AbstractEmailAdministration)getObject("EmailAdministration");
			Object message = getObject("EmailMessage");
			if(!(message instanceof String)){
				wrapper = ea.getMessageWrapper(message);
				sender = wrapper.getSender();
				int i = 0;
				for(Object o : wrapper.getAttachments()){
					i++;
					addProperty("attachment_" + i, "Boolean", "false");
					addProperty("attachmenttitle_" + i, "String", "false");
				}
				addNode(node2);
				setCurrentNode(node2);
			}
			else{
				addNode(node1);
				addNode(node2);
				setCurrentNode(node1);				
			}
		}
		String conffile = ods.openFile(ods.getRootpath() + "/conf/sendemail.conf");
		templates = new Vector();
		String[] lines = conffile.split("\r\n|\n|\r");
		for(int i = 0; i < lines.length; i++){
			if(!lines[i].startsWith("#")){
				String[] args = lines[i].split("=");
				if(args.length == 2){
					if(args[0].equals("ActivityTemplate")){
						String[] args2 = args[1].split("\\(|\\)");
						if(args2.length > 1){
							templates.add(new ConfigValue(args2[1], args2[1], args2[0]));
						}
					}
					else{
					      if(hasProperty(args[0])){
						    setProperty(args[0], args[1].trim());
					      }
					}
				}
			}
		}
		getProperty("ActivityTemplate").setSelection(templates);
		
		ObjectCollection results = new ObjectCollection("Results", "*");
		String sql = "SELECT t1.ID, t3.FamilyName, t3.FirstName FROM OrganisationMember AS t1 JOIN Person AS t2 ON t1.Person=t2.ID JOIN Identity AS t3 ON t3.PersonID=t2.ID JOIN Contact AS t4 ON t4.PersonID=t2.ID AND t4.Type=3 WHERE t4.Value='" + sender + "'";
		ods.queryData(sql, results);
		Vector senders = new Vector();
		for(BasicClass o : results.getObjects()){
			senders.add(new ConfigValue(o.getString("ID"), o.getString("ID"), o.getString("FAMILYNAME") + " " + o.getString("FIRSTNAME")));
			
		}
		getProperty("OMID").setSelection(senders);
		
	}
	public void finish(ProcessResult result, ApplicationContext context){

		OpenCommunityServer ods = (OpenCommunityServer)getRoot();
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
		/*
		DateFormat dateFormat= new SimpleDateFormat("yyyy-MM-dd");



		String[] args = getString("CaseRecordID").split("_");
		String[] args2 = getString("ActivityTemplate").split(";");

		CaseRecord caseRecord = null;



		if(args.length==2 && args2.length > 3){

			for(Dossier dossier : userSession.getOpenDossiers()){
				for(BasicClass caseRecord2 : dossier.getObjects("CaseRecord")){
					if(caseRecord2.getName().equals(args[1])){
						caseRecord = (CaseRecord)caseRecord2;
					}

				}
			}

			int i = 0;
			Vector<String> attachmentids = new Vector();
			for(Object o : wrapper.getAttachments()){
				i++;

				if(getBoolean("attachment_" + i)){
					if(o instanceof MailAttachment){

						try{

							MailAttachment attachment = (MailAttachment)o;
							ExternalDocument doc = (ExternalDocument)ods.createObject("org.opendossier.dossier.ExternalDocument", null, context);
							doc.addProperty("CaseRecordID", "String", args[1]);

							String title = attachment.getFileName();

							if(getString("attachmenttitle_" + i).length() > 0){
								
								String[] filenameparts = title.split("\\.");
								title = getString("attachmenttitle_" + i);
								if(filenameparts.length > 1){
									title += "." + filenameparts[filenameparts.length-1];
								}
							}
												
							String exDocId = ods.insertObject(doc); // insert in DB
							ods.logAccess(attachment.getFileName() + " " + args[0] + " " + args[1]);

							File file = doc.initFileData(title, args[0], args[1]);

							FileOutputStream fos = new FileOutputStream(file);
							InputStream is = attachment.getContent().getInputStream();
							

							int bufferSize = 1024;
							byte[] buffer = new byte[bufferSize];
							int len = 0;
							while ((len = is.read(buffer)) != -1) {
								fos.write(buffer, 0, len);
								//fos.write();
							}
							
							fos.close();
							ods.updateObject(doc);

							attachmentids.add(exDocId);
						}
						catch(java.lang.Exception e){
							ods.logException(e);
						}
					}
					else if(o instanceof FileAttachment){
						try{
							FileAttachment attachment = (FileAttachment)o;
							attachment.load();
							ExternalDocument doc = (ExternalDocument)ods.createObject("org.opendossier.dossier.ExternalDocument", null, context);
							doc.addProperty("CaseRecordID", "String", args[1]);

							String title = attachment.getName();

							if(getString("attachmenttitle_" + i).length() > 0){
								
								String[] filenameparts = title.split("\\.");
								title = getString("attachmenttitle_" + i);
								if(filenameparts.length > 1){
									title += "." + filenameparts[filenameparts.length-1];
								}
							}
												
							String exDocId = ods.insertObject(doc); // insert in DB
							File file = doc.initFileData(title, args[0], args[1]);
							FileOutputStream fos = new FileOutputStream(file);
							fos.write(attachment.getContent());
							fos.close();
							ods.updateObject(doc);

							attachmentids.add(exDocId);
						}
						catch(java.lang.Exception e){
							ods.logException(e);
						}
					}
				}

			}
			if(!getBoolean("AttachmentOnly")){
				Activity activity = (Activity)ods.createObject("ch.opencommunity.base.Activity", null, context);
				activity.addProperty("CaseRecordID", "String", args[1]);
				activity.setProperty("Template", args2[0]);
				activity.setProperty("ScheduledFor", dateFormat.format(wrapper.getDate()));
				activity.setProperty("Title", wrapper.getSubject());
				String id = ods.insertSimpleObject(activity);
				
				
				ActivityStaffMember asm = new ActivityStaffMember();
				asm.setParent(activity);
				asm.addProperty("ActivityID", "String", id);
				asm.setMember("" + userSession.getStaffMemberID());
				ods.insertObject(asm);
				

				Note note = (Note)ods.createObject("org.opendossier.dossier.Note", null, context);
				note.addProperty("ActivityID", "String", id);
				note.setProperty("Template", args2[2]);
				note.setProperty("Title", "Eigenschaften");

				String infoString = "Datum der Nachricht: " + wrapper.getDate();
				infoString += "<br>Absender: " + wrapper.getSender();
				for(String cc : wrapper.getCCRecipients()){
					if(cc.trim().length() > 0){
						infoString += "<br>CC: " + cc;
					}
				}

				note.setProperty("Content", infoString);
				ods.insertObject(note);

				note = (Note)ods.createObject("org.opendossier.dossier.Note", null, context);
				note.addProperty("ActivityID", "String", id);
				note.setProperty("Template", args2[1]);
				note.setProperty("Title", "Notiz");
				note.setProperty("Content", wrapper.getMessageBody().replace("\r|\n|\r\n", "<br>"));
				ods.insertObject(note);



				for(String exDocId : attachmentids){
					Parameter parameter = (Parameter)ods.createObject("org.opendossier.dossier.Parameter", null, context);
					parameter.addProperty("ActivityID", "String", id);
					//parameter.setProperty("Template", "206");
					parameter.setProperty("Template", args2[3]);
					parameter.setProperty("Title", "Attachment");
					//parameter.setProperty("Title", "Dokument");
					parameter.setProperty("Document", exDocId);
					ods.insertObject(parameter);
				}

				asm = new ActivityStaffMember();
				asm.setParent(activity);
				asm.addProperty("ActivityID", "String", id);
				asm.setMember(wrapper.getSender());
				ods.insertObject(asm);

				try{
					if(getString("TargetFolder").length() > 0){
						ea.moveMessage(wrapper, getString("TargetFolder"));
						userSession.getLogin().setSetting("EmailImportTargetFolder", getString("TargetFolder"));
					}
				}
				catch(java.lang.Exception e){
					ods.logException(e);
				}

				if(caseRecord != null){


					for(String exDocId : attachmentids){
						ods.getObject(caseRecord, "ExternalDocument", "ID", exDocId);
					}
					activity = (Activity)ods.getObject(caseRecord, "Activity", "ID", id);	
					result.setParam("edit", caseRecord.getPath(""));
				}
			}

		}
		*/

	}
	class ImportMailNode extends BasicProcessNode{

		public boolean validate(ApplicationContext context){

			if(context.hasProperty("startsubprocess")){
				OpenCommunityServer ods = (OpenCommunityServer)getRoot();
				ods.logAccess("start subprocess");
				BasicProcess subprocess = startSubprocess("org.opendossier.process.CaseSearch");
				//subprocess.setProperty("mode", "dossierperson");
 				subprocess.initProcess();
				return false;

			}
			else{
				OpenCommunityServer ods = (OpenCommunityServer)getRoot();
				ods.logAccess("somthing else");
				return true;
			}

		}


	}

} 
