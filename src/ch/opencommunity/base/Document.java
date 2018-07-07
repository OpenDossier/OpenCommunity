package ch.opencommunity.base;

import ch.opencommunity.server.OpenCommunityServer;
import ch.opencommunity.common.OpenCommunityUserSession;
import ch.opencommunity.base.DocumentHeaderFooter.HeaderFooterType;
import ch.opencommunity.base.ScriptDefinition;
import ch.opencommunity.dossier.*;
import ch.opencommunity.query.*;
import ch.opencommunity.view.OrganisationMemberList;


import ch.opencommunity.util.TextComponents;

import org.kubiki.base.BasicClass;
import org.kubiki.base.ObjectCollection;
import org.kubiki.base.ActionResult;
import org.kubiki.application.ApplicationContext;
import org.kubiki.application.server.WebApplicationContext;
import org.kubiki.xml.XMLParser;
import org.kubiki.xml.XMLElement;
import org.kubiki.util.DateConverter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.math.BigInteger;
import java.util.Random;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;


import java.io.PrintWriter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.ArrayUtils;


public class Document extends BasicDocument{

	public enum DocumentFormat { PDF, MSWORD }
	private DocumentFormat documentFormat = DocumentFormat.MSWORD;
	private int maxParagraphIndex;
	
	private String tempID;
	
	Parameter reference = null;
	
	public Document(){
		setTablename("Document");
		
		addProperty("Type", "Integer", "1", false, "Typ"); //1=Word, 2=Pdf 3=LibreOffice
		addProperty("MainRecipient","Integer","0", true, "Hauptempfänger");	
		addProperty("SignedBy","Integer","-1", true, "Visiert durch");				
		addProperty("MailingList","String","", true, "Verteiler", 300);
		addProperty("Header", "Integer", "", true, "Kopfzeilen");
		addProperty("Footer", "Integer", "", true, "Fusszeilen");
		addProperty("Subject","String","", false, "Betreffzeile", 200);
		addProperty("DocumentClass","String","", true, "Dokumentenklasse", 200);
		// temporary place for storing modules before Word file has been generated
		addProperty("WordModules", "Text", "", true, ""); 
		
		addProperty("Recipient", "Integer", "", true, "Empfänger");
		addProperty("QueryDefinition", "Integer", "", true, "Abfrage");
		
		addObjectCollection("FileObjectData", "org.kubiki.application.FileObjectData");
	
	}
	public void initObjectLocal(){
		super.initObjectLocal();
		OpenCommunityServer ods = (OpenCommunityServer)getRoot();

		ods.logAccess("Format: " + documentFormat);
		


		DocumentTemplate dt = ods.getDocumentTemplate(getID("Template"));

		if(dt != null){
			setProperty("Template", dt);
		}
		/*
		updateContextMenu();

		try{
			getProperty("Status").setSelection(ods.getCode("Dokumentstatus").getCodeList());
			getProperty("Status").setEditable(true);
		}
		catch(Exception e){
			ods.writeError(e);
		}
		addProperty("MailingList2","List","", false, "Verteiler");
		
		getObjectCollection("Paragraph").sort("SortOrder");
		getObjectCollection("CaseStatus").sort("name", "Integer", true);

		script = (ScriptDefinition)ods.getObjectByName("ScriptDefinition", "1");
		*/
	}
	
	public void fillFromTemplate(DocumentTemplate template, String[] templateModuleNames, OpenCommunityUserSession userSession) {
		OpenCommunityServer ods = (OpenCommunityServer)getRoot();
		
		StringBuilder wordModules = new StringBuilder();

		int sortOrder = 0;
		for (String templateModuleName : templateModuleNames) {
			
			ods.logAccess(templateModuleName);
			
			if (templateModuleName.contains("/Note:")) {
				BasicClass bc = ods.getObjectByPath(templateModuleName);
				if (bc instanceof Note) {
					if (documentFormat == DocumentFormat.MSWORD) {
						wordModules.append(bc.getPath());
						wordModules.append("\n");
					}
					else {
						Note note = (Note)bc;
						Paragraph par = addParagraph();
						par.getProperty("SortOrder").setValue(sortOrder++);
						par.fillContent(note.getTitle(), note.getString("Content"));
					}
				}
			}
			else {
				DocumentTemplateModule templateModule = (DocumentTemplateModule)template.getObjectByName("DocumentTemplateModule", templateModuleName);
				
				//DocumentTemplateModule templateModule = (DocumentTemplateModule)templateModules.getObject(templateModuleName);
				if (templateModule != null) {
					if (documentFormat == DocumentFormat.MSWORD) {
						wordModules.append(templateModule.getPath());
						wordModules.append("\n");
					}
					else {
						Paragraph par = addParagraph();
						par.getProperty("SortOrder").setValue(sortOrder++);
						par.getProperty("ShowTitle").setValue(templateModule.getBoolean("ShowTitle"));
						String content = templateModule.getContent();
						par.fillContent(templateModule.getTitle(), content);
					}
				}
			}
		}
		
		ods.logAccess("Format : " + documentFormat + ", " +  wordModules.toString());
		
		if (documentFormat == DocumentFormat.MSWORD) {
			getProperty("WordModules").setValue(wordModules.toString());
			ods.updateObject(this);
		}
	}
	public ActionResult onAction(BasicClass source, String command, ApplicationContext context) {
		return null;
	}
	public boolean onWordClientAction(String action, HttpServletRequest request, HttpServletResponse response, OpenCommunityUserSession userSession) throws IOException {
		
		
		WebApplicationContext context = new WebApplicationContext(request, response);
		
		OpenCommunityServer ods = (OpenCommunityServer)getRoot();
		
		ods.logAccess("Usersession  " + userSession);
		
		ods.logAccess("Wordaction : " + action);
		boolean isHandled = false;
		if (action.equals("getHeader") || action.equals("getFooter")) {
			DocumentTemplate template = getTemplate();
			ods.logAccess("Template : " + template);
			if (template != null) {
				DocumentHeaderFooter headerFooter = null;
				if (action.equals("getHeader")) {
					headerFooter = template.getHeaderFooter(HeaderFooterType.HEADER);
					ods.logAccess("HeaderFooter : " + headerFooter);
				}
				else {
					headerFooter = template.getHeaderFooter(HeaderFooterType.FOOTER);
				}
				if (headerFooter != null) {
					headerFooter.sendContentToWord(response);
					isHandled = true;
				}
			}
		}
		else if (action.equals("getTemplateModules")) {
			PrintWriter writer = response.getWriter();
			
			Object object = getObject("Template");
			if (object instanceof DocumentTemplate){
			    DocumentTemplate template = (DocumentTemplate)object;
				ObjectCollection oc = template.getObjectCollection("DocumentTemplateModule");
				if (oc != null) {
					Vector<BasicClass> modules = oc.getObjects();
					for(BasicClass bcModule : modules) {
						DocumentTemplateModule module = (DocumentTemplateModule)bcModule;
						writer.print(module.getPath() + "@" + module.getTitle() + "\n");
					}
				}
			}
			/*
			CaseRecord caseRecord = (CaseRecord)getParent("org.opendossier.dossier.CaseRecord");
			if (caseRecord != null) {
				List<Note> notes = caseRecord.getAllNotes();
				for (Note note : notes) {
					writer.print(note.getPath() + "@" + note.getParent().getLabel() + " - " + note.getLabel() + "\n");
				}
				Dossier dossier = (Dossier)caseRecord.getParent(); //AK 08.07.2013
				if (dossier != null) {
					notes = dossier.getAllNotes();
					for (Note note : notes) {
						writer.print(note.getPath() + "@" + note.getParent().getLabel() + " - " + note.getLabel() + "\n");
					}
				}
			}
			*/
			isHandled = true;
		}
		else if (action.equals("getModules")) {
			PrintWriter writer = response.getWriter();
			writer.print(getString("WordModules"));
			isHandled = true;
		}
		else if (action.equals("getMailinglist")) {
			PrintWriter writer = response.getWriter();
					
			
			//Vector<DocumentAddress> documentAddresses = getAddressList();
			try{
				List<OrganisationMember> omlist = new ArrayList<OrganisationMember>();
				OrganisationMember om = null;
				OrganisationalUnit ou = null;
				ObjectCollection addresses = null;

				
				String defaultAddressation = null;
				
				String institution = "";
				
				if(getParent() instanceof OrganisationMember){
					om = (OrganisationMember)getParent();	
					omlist.add(om);
				}
				//else if(getString("QueryDefinition").length() > 0){
				else if(1==1){
					
					ods.logEvent("starting mail merge ....");
					
					HashMap ids = new HashMap();
					
					addresses = new ObjectCollection("Addresses", "*");
					
					ObjectCollection results = new ObjectCollection("Resutls", "*");
					
					String sql = "SELECT DISTINCT  t1.ID, t3.Familyname, t3.Firstname, t3.Sex, t4.Street, t4.Number, t4.AdditionalLine, t4.ZipCode, t4.City FROM OrganisationMember AS t1";
					sql += " JOIN Person AS t2 ON t1.Person=t2.ID";
					sql += " JOIN Identity AS t3 On t3.PersonID=t2.ID";
					sql += " JOIN Address AS t4 ON t4.PersonID=t2.ID";
					sql += " JOIN MemberRole AS t5 ON t5.OrganisationMemberID=t1.ID AND t5.Role IN (2,3)";					
					sql += " WHERE t1.Status=1";
					sql += " ORDER BY t3.Familyname, t3.FirstName";
					
					ods.logEvent(sql);
					
					ods.queryData(sql, results);
					
					ods.logEvent("Anzahl  Records1 : " + results.getObjects().size());
					
					for(BasicClass record : results.getObjects()){
						String id = record.getString("ID");
						if(ids.get(id)==null){
							addresses.getObjects().add(record);	
							ids.put(id, "");
						}
					}
					
					ods.logEvent("Anzahl  Records total : " + addresses.getObjects().size());
					
					results.removeObjects();
					
					/*
					sql = "SELECT DISTINCT  t1.ID, t3.Familyname, t3.Firstname, t3.Sex, t4.Street, t4.Number, t4.AdditionalLine, t4.ZipCode, t4.City FROM OrganisationMember AS t1";
					sql += " JOIN Person AS t2 ON t1.Person=t2.ID";
					sql += " JOIN Identity AS t3 On t3.PersonID=t2.ID";
					sql += " JOIN Address AS t4 ON t4.PersonID=t2.ID";
					sql += " JOIN MemberAd AS t5 ON t5.OrganisationMemberID=t1.ID";
					sql += " WHERE (t5.DateCreated >= '2016-01-01' OR t5.ValidFrom >= '2016-01-01')";					
					sql += " AND t1.Status=1";
					sql += " ORDER BY t3.Familyname, t3.FirstName";
					*/
					sql = "SELECT DISTINCT  t1.ID, t3.Familyname, t3.Firstname, t3.Sex, t4.Street, t4.Number, t4.AdditionalLine, t4.ZipCode, t4.City FROM OrganisationMember AS t1";
					sql += " JOIN Person AS t2 ON t1.Person=t2.ID";
					sql += " JOIN Identity AS t3 On t3.PersonID=t2.ID";
					sql += " JOIN Address AS t4 ON t4.PersonID=t2.ID";
					sql += " JOIN MemberRole AS t5 ON t5.OrganisationMemberID=t1.ID AND t5.Role IN (1)";		
					sql += " WHERE t1.DateCreated >= '2016-01-01'";		
					sql += " AND t1.Status=1";
					sql += " ORDER BY t3.Familyname, t3.FirstName";
					
					ods.queryData(sql, results);
					
					ods.logEvent("Anzahl  Records2 : " + results.getObjects().size());
					
					for(BasicClass record : results.getObjects()){
						String id = record.getString("ID");
						if(ids.get(id)==null){
							addresses.getObjects().add(record);	
							ids.put(id, "");
						}
					}
					
					ods.logEvent("Anzahl  Records total : " + addresses.getObjects().size());
					/*
					results.removeObjects();
					
					sql = "SELECT DISTINCT  t1.ID, t3.Familyname, t3.Firstname, t3.Sex, t4.Street, t4.Number, t4.AdditionalLine, t4.ZipCode, t4.City FROM OrganisationMember AS t1";
					sql += " JOIN Person AS t2 ON t1.Person=t2.ID";
					sql += " JOIN Identity AS t3 On t3.PersonID=t2.ID";
					sql += " JOIN Address AS t4 ON t4.PersonID=t2.ID";
					sql += " JOIN MemberAdRequest AS t5 ON t5.OrganisationMemberID=t1.ID";
					sql += " WHERE (t5.DateCreated >= '2016-01-01' OR t5.ValidFrom >= '2016-01-01')";					
					sql += " AND t1.Status=1";
					sql += " ORDER BY t3.Familyname, t3.FirstName";
					
					ods.queryData(sql, results);
					
					ods.logEvent("Anzahl  Records3 : " + results.getObjects().size());
					
					for(BasicClass record : results.getObjects()){
						String id = record.getString("ID");
						if(ids.get(id)==null){
							addresses.getObjects().add(record);	
							ids.put(id, "");
						}
					}
					
					ods.logEvent("Anzahl  Records total : " + addresses.getObjects().size());
					*/
					
					/*
					OrganisationMemberList organisationMemberList = (OrganisationMemberList)ods.querylists.get("organisationMemberList");
					String queryid = getString("QueryDefinition");
					QueryDefinition queryDefinition = (QueryDefinition)ods.getObjectByName("QueryDefinition", "" + queryid);
					if(queryDefinition != null){
						String xml = queryDefinition.getString("XML");
						ods.logAccess(xml);
						XMLElement xmlDoc = XMLParser.parseString(xml);
						if(xmlDoc.getChild("ch.opencommunity.common.OpenCommunityAdminSession") != null){
							XMLParser.parseSubelements(userSession, xmlDoc.getChild(0));	
							XMLParser.setProperties(userSession, xmlDoc.getChild(0));
							
							ods.logAccess("IsMember : " +  userSession.getString("IsMember"));
							
							request.getSession().setAttribute("usersession", userSession);

							String sql = organisationMemberList.getSQL(context);
							sql += organisationMemberList.getFilter().getFilterString(context);
							
							ods.logAccess(sql);
							
							addresses = new ObjectCollection("Addresses", "*");
							
							ods.queryData(sql, addresses);
							
						}
					}
					*/
					
				}
				else if(getParent() instanceof CaseRecord || getParent() instanceof Project){
					
					Dossier dossier = (Dossier)getParent().getParent();
					
					ou = dossier.getOrganisationalUnit();
					ods.logAccess("OU : " + ou);	
					
					ods.logAccess("Dossier : " + dossier);
					ObjectDetail objectDetail = (ObjectDetail)dossier.getObjectByIndex("ObjectDetail", 0);
					if(objectDetail != null){
						BasicClass note = objectDetail.getFieldByVarname("Default Anrede");
						if(note != null){
							defaultAddressation = note.getString("Content");
						}
					}
					
					boolean defaultAddress = true;
					
					if(reference != null){
						Activity activity = (Activity)reference.getParent();
						if(activity != null){
							
							for(BasicClass ap : activity.getObjects("ActivityParticipant")){
								
								String omid = ap.getString("OrganisationMember");
								String ouid = ap.getString("OrganisationalUnit");
								if(omid != null && omid.length() > 0){
									
									om = (OrganisationMember)ods.getObject(null, "OrganisationMember", "ID", omid);
									om.setParent(this);
									om.initObjectLocal();		
									omlist.add(om);								
									
								}
								else if(ou != null && ouid != null && ouid.equals(ou.getName())){
									String maincontact = ou.getString("MainContactPerson");
		
									if(maincontact.length() > 0){
										om = (OrganisationMember)ods.getObject(null, "OrganisationMember", "ID", maincontact);
										om.setParent(this);
										om.initObjectLocal();		
										omlist.add(om);									
											
									}
								
								}
								defaultAddress = false;
							}
							
						}
						
						
					}
					
					ods.logAccess("defaultAddress : " + defaultAddress);
						
					if(defaultAddress){
								
						if(ou != null){
							String maincontact = ou.getString("MainContactPerson");

							if(maincontact.length() > 0){
								om = (OrganisationMember)ods.getObject(null, "OrganisationMember", "ID", maincontact);
								om.setParent(this);
								om.initObjectLocal();		
								omlist.add(om);
							}
						}
						
					}
				}
				else{
					
					om = (OrganisationMember)ods.getObject(null, "OrganisationMember", "ID", getString("Recipient"));
					om.setParent(this);
					om.initObjectLocal();
					omlist.add(om);
				}
				
				if(ou != null){
					institution = ou.getString("Title");	
				}
				
				ods.logAccess("OrganisationMember : " + om);
				
				
				writer.print("<Mailinglist>\n");
				if(addresses != null){
					
					ods.logAccess("Addresses : " + addresses.getObjects().size());
					
					int cnt = 0;
					int cnt2 = 0;
					
					for(BasicClass address : addresses.getObjects()){
						if(cnt < 2000){
						
						cnt2++;
						
						int sex = address.getID("SEX");
						String title = "Herrn";
						if(sex==2){
							title = "Frau";	
						}
						String longTitle = "Sehr geehrter Herr " + address.getString("FAMILYNAME");
						if(sex==2){
							longTitle = "Sehr geehrte Frau " + address.getString("FAMILYNAME");	
						}
						if(cnt2==1){
							ods.logEvent(address.getString("FAMILYNAME") + " " +  address.getString("FIRSTNAME"));
						}
						if(cnt2==100){
							ods.logEvent(address.getString("FAMILYNAME") + " " +  address.getString("FIRSTNAME"));
							cnt2 = 0;
							ods.logEvent(" ");
						}
						
						writer.print("<Address");		
						writer.print(" title=\"" + XMLParser.escapeXml(title) + "\"");
						writer.print(" longTitle=\"" +  XMLParser.escapeXml(longTitle) + "\"");
						writer.print(" function=\"" +  XMLParser.escapeXml("") + "\"");
						writer.print(" organisation=\"" +  XMLParser.escapeXml(institution) + "\"");
						writer.print(" firstName=\"" +  XMLParser.escapeXml(address.getString("FIRSTNAME")) + "\"");
						writer.print(" familyName=\"" +  XMLParser.escapeXml(address.getString("FAMILYNAME")) + "\"");
						writer.print(" additionalLine=\"" +  XMLParser.escapeXml(address.getString("ADDITIONALLINE")) + "\"");
						writer.print(" street=\"" +  XMLParser.escapeXml(address.getString("STREET")) + "\"");
						writer.print(" number=\"" +  XMLParser.escapeXml(address.getString("NUMBER")) + "\"");
						writer.print(" zipcode=\"" +  XMLParser.escapeXml(address.getString("ZIPCODE")) + "\"");
						writer.print(" city=\"" +  XMLParser.escapeXml(address.getString("CITY")) + "\"");
						writer.print(" description=\"\"");		
						writer.print(">\n");
						writer.print("</Address>\n");	
						cnt++;
						}
					}
					
				}
				//else if(om != null){
				else if(omlist.size() > 0){
					
					for(OrganisationMember om2 : omlist){
						
						writer.print("<Address");
						
						Person person = om2.getPerson();
						Identity identity = person.getIdentity();
						Address address = person.getAddress();
						if(om2.getBoolean("InheritsAddress") && ou != null){
							address = ou.getAddress();	
						}
					//for (DocumentAddress address : documentAddresses) {
						
						/*
						if (address.title != null) writer.print(" title=\"" + XMLParser.escapeXml() + "\"");
						if (address.longTitle != null) writer.print(" longTitle=\"" +  XMLParser.escapeXml() + "\"");
						if (address.function != null) writer.print(" function=\"" +  XMLParser.escapeXml() + "\"");
						if (address.organisation != null) writer.print(" organisation=\"" +  XMLParser.escapeXml() + "\"");
						if (address.firstName != null) writer.print(" firstName=\"" +  XMLParser.escapeXml(identity.getString("FirstName")) + "\"");
						if (address.familyName != null) writer.print(" familyName=\"" +  XMLParser.escapeXml(identity.getString("FirstName")) + "\"");
						if (address.additionalLine != null) writer.print(" additionalLine=\"" +  XMLParser.escapeXml() + "\"");
						if (address.street != null) writer.print(" street=\"" +  XMLParser.escapeXml(address.getString("Street")) + "\"");
						if (address.number != null) writer.print(" number=\"" +  XMLParser.escapeXml(address.getString("Number")) + "\"");
						if (address.zipcode != null) writer.print(" zipcode=\"" +  XMLParser.escapeXml(address.getString("Zipcode")) + "\"");
						if (address.city != null) writer.print(" city=\"" +  XMLParser.escapeXml(address.getString("City")) + "\"");
						*/
						writer.print(" title=\"" + XMLParser.escapeXml("") + "\"");
						if(defaultAddressation != null){
							writer.print(" longTitle=\"" +  XMLParser.escapeXml(defaultAddressation) + "\"");
						}
						else{
							writer.print(" longTitle=\"" +  XMLParser.escapeXml(om.getAddressation()) + "\"");
						}
						writer.print(" function=\"" +  XMLParser.escapeXml("") + "\"");
						writer.print(" organisation=\"" +  XMLParser.escapeXml(institution) + "\"");
						writer.print(" firstName=\"" +  XMLParser.escapeXml(identity.getString("FirstName")) + "\"");
						writer.print(" familyName=\"" +  XMLParser.escapeXml(identity.getString("FamilyName")) + "\"");
						writer.print(" additionalLine=\"" +  XMLParser.escapeXml(address.getString("AdditionalLine")) + "\"");
						writer.print(" street=\"" +  XMLParser.escapeXml(address.getString("Street")) + "\"");
						writer.print(" number=\"" +  XMLParser.escapeXml(address.getString("Number")) + "\"");
						writer.print(" zipcode=\"" +  XMLParser.escapeXml(address.getString("Zipcode")) + "\"");
						writer.print(" city=\"" +  XMLParser.escapeXml(address.getString("City")) + "\"");
						writer.print(" description=\"\"");
						
						writer.print(">\n");
						
						writer.print("</Address>\n");
					}
					
				//}
				}
				writer.print("</Mailinglist>\n");
			}
			catch(java.lang.Exception e){
				ods.logException(e);
			}
			isHandled = true;
		}
		else if (action.equals("getReplaceMap")) {

			PrintWriter writer = response.getWriter();
			Map<String, String> replaceMap = getAttributes(userSession);
			for (String key : replaceMap.keySet()) {
				String value = "";
				if (key.startsWith("script_")) {
					
					try {
						value = (String)ods.executeScript(replaceMap.get(key), this, null, userSession);
					}
					catch (Exception e) {
//							ods.logException(e);
						value = "Ungültiges Script";
					}
					
				}
				else {
					value = replaceMap.get(key);
				}
				// remove HTML tags

				value = value.replaceAll("^\\s*<p>(\n\t)?", "");
				value = value.replaceAll("</p>\\s*$", "");
				value = value.replaceAll("</p>\\s*<p>(\n\t)?", "\n");
				value = value.replaceAll("<br />(\n\t)?", "\n");
				value = StringEscapeUtils.unescapeHtml4(value);
				//value = encode(StringEscapeUtils.unescapeHtml4(value)); //ToDo: Komplette URL-Codierung der Feldinhalte
				
				value = value.replace("\n", "\\n");
				writer.print(key + "=>" + value + "\n");
			}
			isHandled = true;
		}
		if (!isHandled) {
			isHandled = super.onWordClientAction(action, request, response, userSession);
		}
		return isHandled;
	}
	public Map<String, String> getAttributes(OpenCommunityUserSession userSession){
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		Hashtable<String, String> attrs = new Hashtable<String, String>();
		for (ScriptDefinition script : ocs.getScripts().values()) {
			if (script.isDocumentScript()) {
				attrs.put("script_" + TextComponents.cleanParameterName(script.getTitle()), script.getString("ScriptBody"));
			}
		}
		if(getParent() instanceof OrganisationMember){
			OrganisationMember om = (OrganisationMember)getParent();
			if(getReference() != null){
				Activity activity = (Activity)getReference().getParent();
				attrs.put("betreff", activity.getString("Title"));
			}
		}
		else if(getParent() instanceof CaseRecord){
			ocs.logAccess("reference : " + getReference());
			//OrganisationMember om = (OrganisationMember)getParent();
			if(getReference() != null){
				Activity activity = (Activity)getReference().getParent();
				attrs.put("betreff", activity.getString("Title"));
			}
		}
		else if(getParent() instanceof Project){
			ocs.logAccess("reference : " + getReference());
			//OrganisationMember om = (OrganisationMember)getParent();
			if(getReference() != null){
				Activity activity = (Activity)getReference().getParent();
				attrs.put("betreff", activity.getString("Title"));
				if(activity.getString("Date").length() > 9){
					
					attrs.put("datum_doc", DateConverter.sqlToShortDisplay(activity.getString("Date")));	
				}
				else{
					attrs.put("datum_doc", DateConverter.dateToShortDisplay(new java.util.Date(), false));	
				}
			}
		}
		return attrs;
	}
	public Paragraph addParagraph() {
		return addParagraph(Integer.toString(maxParagraphIndex++));
	}
	
	public Paragraph addParagraph(String id) {
		Paragraph paragraph = new Paragraph(id);
		addParagraph(paragraph);
		return paragraph;
	}
	
	public void addParagraph(Paragraph paragraph) {
		addSubobject(paragraph.getId(), "Paragraph", paragraph);
		paragraph.setParent(this);
	}
	public String getDocPostfix(DocumentFormat documentFormat) {
		return documentFormat == DocumentFormat.MSWORD ? ".docx" : ".pdf";
	}
	
	public String getDocPostfix() {
		return getDocPostfix(documentFormat);
	}
	public String getTempName() {
		return getTempName(getTempPrefix());
	}
	public String getTempName(String prefix) {
		if (tempID == null) {
			tempID = new BigInteger(50, new Random()).toString();
		}
		return "od_" + prefix + "_" + tempID;
	}
	protected String getTempPrefix() {
		return "doc";
	}
	protected DocumentTemplate getTemplate() {
		if (getObject("Template") instanceof DocumentTemplate){
			return (DocumentTemplate)getObject("Template");
		}
		else {
			return null;
		}
	}
	@Override
	public void setIsExternal(boolean isExternal) {
	}
	@Override
	public boolean isExternal() {
		return true;
	}
	protected String getDocPath(boolean filesystem){
		return ((OpenCommunityServer)getRoot()).getRootpath() + "/docs/";
	}
	protected String getDocPath(boolean filesystem, DocumentFormat documentFormat){
		return ((OpenCommunityServer)getRoot()).getRootpath() + "/docs/";	
	}
	public void setReference(Parameter reference){
		this.reference = reference;
	}
	public Parameter getReference(){
		return reference;
	}
		
}
