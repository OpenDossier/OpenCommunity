package ch.opencommunity.server;

import ch.opencommunity.base.*;
import ch.opencommunity.common.*;
import ch.opencommunity.advertising.*;
import ch.opencommunity.office.*;
import ch.opencommunity.base.Document.DocumentFormat;

import ch.opencommunity.process.*;
import ch.opencommunity.news.*;
import ch.opencommunity.statistics.*;
import ch.opencommunity.base.ScriptDefinition;
import ch.opencommunity.mail.*;

import ch.opencommunity.view.OrganisationMemberList;
import ch.opencommunity.view.MemberAdList;
import ch.opencommunity.view.ChequeList;
import ch.opencommunity.view.BatchActivityList;
import ch.opencommunity.view.Dashboard;
import ch.opencommunity.view.UserProfileView;
import ch.opencommunity.view.DossierView;

import ch.opencommunity.dossier.*;

import ch.opencommunity.reporting.*;

import ch.opencommunity.query.QueryDefinition;

import org.kubiki.base.*;
import org.kubiki.base.ActionResult.Status;
import org.kubiki.base.ProcessResult.ProcessStatus;
import org.kubiki.application.server.ApplicationServer;
import org.kubiki.application.server.WebApplicationContext;
import org.kubiki.application.server.WebApplicationModule;
import org.kubiki.database.*;
import org.kubiki.application.*;
import org.kubiki.util.DateConverter;
import org.kubiki.servlet.AppLoader;

import org.kubiki.cms.*;
import org.kubiki.pdf.*;
import org.kubiki.gui.html.*;

import org.kubiki.ide.BasicProcess;
import org.kubiki.ide.BasicProcessNode;

import org.kubiki.util.FileUpload;

import org.kubiki.accounting.*;

import org.kubiki.reporting.*;

import org.kubiki.mail.*;

import org.kubiki.newsletter.*;

import org.kubiki.libreoffice.*;




import java.sql.*;
import java.io.*;
import java.util.*;
import java.awt.image.BufferedImage;

import java.security.CodeSource;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.net.URL;

import javax.servlet.http.*;
import javax.imageio.ImageIO;

import javax.mail.*;
import javax.mail.internet.*;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.DataHandler;

import java.util.Properties;

import java.lang.reflect.Method;

import org.apache.commons.fileupload.servlet.ServletFileUpload;

import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.StringEscapeUtils;


import bsh.Interpreter;

//public class OpenCommunityServer extends CMSServer{
public class OpenCommunityServer extends ApplicationServer{

	private DataStore dataStore = null;
	
	HTMLForm form;
	
	MemberAdAdministration maa; 
	
	OpenCommunityUserProfile ocup;
	
	ObjectTemplateAdministration ota;
	
	Map<String, OpenCommunityUserSession> wordClientSessions;
	
	Accounting accounting;
	
	Reporting reporting;
	
	Vector<ConfigValue> geoobjects;
	Vector<ConfigValue> institutionTypes;
	Map<String, String> institutionTypeMap;
	
	Map<String, String> texts;
	
	Map<String, String> fieldRendererMapping;
	
	Map<String, ScriptDefinition> scripts;
	
	Interpreter interpreter;
	
	NewsAdministration news;
	
	Statistics statistics;
	
	Vector<ConfigValue> languages;
	Vector<ConfigValue> languages2;
	Vector<String> languages3;
	

	
	CMS cms;
	
	Advertising advertising;
	

	
	public Hashtable querylists;
	
	EmailAdministrationForm mailForm = null;
	
	FileUpload fileUpload = null;
	
	MainMenu mainMenu;
	OrganisationMemberList organisationMemberList = null;
	MemberAdList memberAdList = null;
	ChequeList chequeList = null;
	ChequeAdministration chequeAdministration = null;
	
	NewsletterAdministration newsletterAdministration;
	
	public Hashtable yes_no, quality, roleMap = null;
	
	DossierAdministration dossierAdministration;
	ProjectAdministration projectAdministration;
	
	OrganisationMemberAdministration oma;
	
	CodeDefinition functions = null;
	
	Map<String, CodeDefinition> codeDefinitions;
	
	LibreOfficeModule libreOfficeModule;
	
	HashMap<String, BasicUserSession> sessions;

	public OpenCommunityServer(AppLoader loader){
		super(loader);

		addProperty("datastore", "String", "org.kubiki.database.PGSQLDataStore");
		addProperty("dburl", "String", "jdbc:postgresql");
		addProperty("dbname", "String", "opencampus?charSet=\'UTF8\'");
		addProperty("dbuser", "String", "postgres");
		addProperty("dbpw", "String", "Kzk,SAzs.");
		
		addProperty("smtphost", "String", "192.168.0.166");
		
		addProperty("documentFormat", "String", "msword");
		
		addProperty("instancename", "String", "opencommunity");
		addProperty("hostname", "String", "http://www.nachbarnet.net");
		
		addProperty("emailadministration", "String", "org.kubiki.mail.IMAPEmailAdministration");
		addProperty("emailurl", "String", "mail.oxinia.ch");	
		//addProperty("defaultmailuser", "String", "");	
		addProperty("bannerserverurl", "String", "http://192.168.0.166:8080/opendossier_dev/servlet");
		
		addObjectCollection("DocumentTemplateLibrary", "ch.opencommunity.base.DocumentTemplateLibrary");
		addObjectCollection("TextBlockAdministration", "ch.opencommunity.base.TextBlockAdministration");
		addObjectCollection("MemberAdAdministration", "ch.opencommunity.advertising.MemberAdAdministration");
		addObjectCollection("ObjectTemplateAdministration", "ch.opencommunity.base.ObjectTemplateAdministration");
		
		addObjectCollection("NewsAdministration", "ch.opencommunity.news.NewsAdministration");
		
		addObjectCollection("Role", "ch.opencommunity.base.Role");
		addObjectCollection("Document", "ch.opencommunity.base.Document");
		addObjectCollection("DocumentTemplate", "ch.opencommunity.base.DocumentTemplate");
		
		addObjectCollection("ScriptDefinition", "ch.opencommunity.base.ScriptDefinition");
		
		addObjectCollection("PDFTemplateLibrary", "ch.opencommunity.pdf.PDFTemplateLibrary");
		
		addObjectCollection("OrganisationalUnit", "ch.opencommunity.base.OrganisationalUnit");
		
		addObjectCollection("OrganisationMember", "ch.opencommunity.base.OrganisationMember");
		addObjectCollection("OrganisationMemberController", "ch.opencommunity.base.OrganisationMemberController");
		
		addObjectCollection("Dossier", "ch.opencommunity.dossier.Dossier");
		addObjectCollection("QueryDefinition", "ch.opencommunity.query.QueryDefinition");
		
		addObjectCollection("GroupDefinition", "ch.opencommunity.base.GroupDefinition");
		
		addObjectCollection("CodeDefinition", "org.kubiki.application.CodeDefinition");
		
		/*
		cms = new CMS();
		cms.setParent(this);
		cms.initObjectLocal();
		*/
		


		
		wordClientSessions = new Hashtable<String, OpenCommunityUserSession>();

		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		try {
			rootpath = classloader.getResource("desc.txt").getPath();
			rootpath = rootpath.replaceAll("/WEB-INF/classes/desc.txt", "");
			setProperty("rootpath", rootpath);
		} 
			catch (java.lang.Exception e) {
		}
		initLog();
		
		logAccess("init");
		
		try{		

		String conffile = openFile(rootpath + "/conf/opencommunity.conf");
		String[] lines = conffile.split("\r\n|\n|\r");
		for (int i = 0; i < lines.length; i++) {
			if (!lines[i].startsWith("#")) {
				String[] args = lines[i].split("=");
				if (args.length == 2) {
					if (hasProperty(args[0])) {
						setProperty(args[0], args[1].trim());
					}
				}
			}
		}
		
		try {
			Class<?> c = Class.forName(getString("datastore"));
			dataStore = (DataStore) c.newInstance();
			dataStore.setParent(this);

		} catch (java.lang.Exception e) {
			logException(e);

		}
		
		setDataStore(dataStore);
		
		logAccess("DataStore: " + getString("datastore"));
		logAccess("DataStore: " + dataStore.getClass().getName());

		if (dataStore != null) {
			dataStore.registerClass("ch.opencommunity.base.OrganisationalUnit");
			dataStore.registerClass("ch.opencommunity.base.Person");
			dataStore.registerClass("ch.opencommunity.base.Identity");
			dataStore.registerClass("ch.opencommunity.base.Address");
			dataStore.registerClass("ch.opencommunity.base.Contact");
			dataStore.registerClass("ch.opencommunity.base.FieldDefinition");
			dataStore.registerClass("ch.opencommunity.base.StatusDefinition");

			dataStore.registerClass("ch.opencommunity.base.Activity");
			dataStore.registerClass("ch.opencommunity.base.Parameter");
			dataStore.registerClass("ch.opencommunity.base.Note");
			dataStore.registerClass("ch.opencommunity.base.ActivityObject");
			dataStore.registerClass("ch.opencommunity.base.ActivityOrganisationMember");
			dataStore.registerClass("ch.opencommunity.base.ActivityParticipant");
			
			dataStore.registerClass("ch.opencommunity.base.Login");
			dataStore.registerClass("ch.opencommunity.base.BatchActivity");
			
			dataStore.registerClass("ch.opencommunity.base.OrganisationMember");
			dataStore.registerClass("ch.opencommunity.base.OrganisationMemberType");
			dataStore.registerClass("ch.opencommunity.base.Role");
			dataStore.registerClass("ch.opencommunity.base.MemberRole");
			dataStore.registerClass("ch.opencommunity.base.OrganisationMemberRelationship");
			dataStore.registerClass("ch.opencommunity.base.OrganisationMemberModification");
			
			dataStore.registerClass("ch.opencommunity.base.LoginCode");
			
			dataStore.registerClass("ch.opencommunity.base.ObjectTemplateAdministration");
			dataStore.registerClass("ch.opencommunity.base.ObjectTemplate");
			
			dataStore.registerClass("ch.opencommunity.base.DocumentTemplateLibrary");			
			dataStore.registerClass("ch.opencommunity.base.DocumentTemplate");
			dataStore.registerClass("ch.opencommunity.base.DocumentHeaderFooter");
			dataStore.registerClass("ch.opencommunity.base.DocumentTemplateModule");
			
			dataStore.registerClass("ch.opencommunity.base.Document");
			dataStore.registerClass("ch.opencommunity.base.Paragraph");
			
			dataStore.registerClass("ch.opencommunity.base.TextBlockAdministration");			
			dataStore.registerClass("ch.opencommunity.base.TextBlock");
			
			dataStore.registerClass("ch.opencommunity.base.Email");
			dataStore.registerClass("ch.opencommunity.base.FreeTextFeedback");
			
			dataStore.registerClass("ch.opencommunity.base.ExternalDocument");
			
			dataStore.registerClass("ch.opencommunity.base.GroupDefinition");
			dataStore.registerClass("ch.opencommunity.base.GroupMember");
			
			dataStore.registerClass("ch.opencommunity.advertising.MemberAdAdministration");
			dataStore.registerClass("ch.opencommunity.advertising.MemberAd");
			dataStore.registerClass("ch.opencommunity.advertising.MemberAdStatus");
			dataStore.registerClass("ch.opencommunity.advertising.MemberAdCategory");
			dataStore.registerClass("ch.opencommunity.advertising.MemberAdRequest");
			dataStore.registerClass("ch.opencommunity.advertising.MemberAdRequestGroup");
			dataStore.registerClass("ch.opencommunity.advertising.MemberAdModification");
			
			dataStore.registerClass("ch.opencommunity.advertising.Feedback");
			
			dataStore.registerClass("ch.opencommunity.advertising.Advertisement");
						
			dataStore.registerClass("ch.opencommunity.news.NewsAdministration");	
			dataStore.registerClass("ch.opencommunity.news.NewsMessage");
			
			dataStore.registerClass("org.kubiki.application.FileObjectData");
			dataStore.registerClass("org.kubiki.application.ObjectStatus");
			
			dataStore.registerClass("org.kubiki.cms.WebSiteRoot");
			dataStore.registerClass("org.kubiki.cms.WebPage");
			dataStore.registerClass("org.kubiki.cms.WebPageElement");
			dataStore.registerClass("org.kubiki.cms.WebPageLayout");
			dataStore.registerClass("org.kubiki.cms.WebPageLayoutElement");
			dataStore.registerClass("org.kubiki.cms.ImageObject");
			dataStore.registerClass("org.kubiki.cms.FileObject");	
			dataStore.registerClass("org.kubiki.cms.WebForm");
			dataStore.registerClass("org.kubiki.cms.HelpTextAdministration");
			dataStore.registerClass("org.kubiki.cms.HelpText");
			
			dataStore.registerClass("org.kubiki.pdf.PDFTemplateLibrary");
			dataStore.registerClass("org.kubiki.pdf.PDFTemplate");
			dataStore.registerClass("org.kubiki.pdf.PDFTemplateParagraph");
			dataStore.registerClass("org.kubiki.pdf.PDFTemplateImage");
			dataStore.registerClass("org.kubiki.pdf.PDFTemplateFrame");

			dataStore.registerClass("ch.opencommunity.base.AccessLog");
			
			dataStore.registerClass("org.kubiki.accounting.Account");
			dataStore.registerClass("org.kubiki.accounting.AccountMovement");
			dataStore.registerClass("org.kubiki.accounting.Cheque");
			
			dataStore.registerClass("ch.opencommunity.dossier.Dossier");
			dataStore.registerClass("ch.opencommunity.dossier.CaseRecord");
			dataStore.registerClass("ch.opencommunity.dossier.ObjectDetail");
			dataStore.registerClass("ch.opencommunity.dossier.Project");
			
			dataStore.registerClass("ch.opencommunity.query.QueryDefinition");
			
			dataStore.registerClass("ch.opencommunity.feedback.FeedbackRecord");
			
			dataStore.registerClass("org.kubiki.mail.MailMessageInstance");
			
			dataStore.registerClass("org.kubiki.libreoffice.LibreOfficeTemplate");			
			
			
			dataStore.initDatabase();
		}
		
		logAccess("DataStore2: " + dataStore);
		
		initCodes();
		
		cms = new CMS();
		cms.setParent(this);
		cms.setName("cms");
		addSubobject("ApplicationModule", cms);
		cms.initModule();
		
		advertising = new Advertising();
		advertising.setParent(this);
		advertising.setName("Advertising");
		addSubobject("ApplicationModule", advertising);
		
		oma = new OrganisationMemberAdministration();
		oma.setParent(this);
		oma.setName("OrganisationMemberAdministration");
		addSubobject("ApplicationModule", oma);
		oma.initData(dataStore);
		
		libreOfficeModule = new LibreOfficeModule();
		libreOfficeModule.setParent(this);
		libreOfficeModule.setName("libreoffice");
		addSubobject("ApplicationModule", libreOfficeModule);
		libreOfficeModule.initModule();
		
		
		fieldRendererMapping = new Hashtable<String, String>();
		fieldRendererMapping.put("RecipientList", "ch.opencommunity.view.RecipientList");
		fieldRendererMapping.put("memberadid", "ch.opencommunity.view.MemberAdView");
		fieldRendererMapping.put("FileUpload", "ch.opencommunity.view.FileUploadWidget");
		fieldRendererMapping.put("ImageUpload", "ch.opencommunity.view.ImageUploadWidget");
		fieldRendererMapping.put("MemberAdRequestList", "ch.opencommunity.view.MemberAdRequestList");
		fieldRendererMapping.put("EmailMessage", "ch.opencommunity.mail.EmailView");
		fieldRendererMapping.put("OMID", "ch.opencommunity.view.LinkPropertyView");
		fieldRendererMapping.put("ProfileMergeWidget", "ch.opencommunity.view.ProfileMergeWidget");
		form = new HTMLForm(this,  fieldRendererMapping, null);
		
		logAccess("DataStore3: " + dataStore);
		
		try{
			
			
			accounting = new Accounting(this);
			accounting.setParent(this);
			accounting.initObjectLocal();
			
			geoobjects = new Vector<ConfigValue>();
			conffile = openFile(rootpath + "/conf/geoobjects.conf");
			lines = conffile.split("\r\n|\n|\r");
			for(String line : lines){
				String[] args = line.split(";");
				if(args.length==2){
					geoobjects.add(new ConfigValue(args[0], args[0], args[1]));	
				}
			}
			
			institutionTypes = new Vector<ConfigValue>();
			institutionTypeMap = new HashMap<String, String>();
			conffile = openFile(rootpath + "/conf/institutiontypes.conf");
			lines = conffile.split("\r\n|\n|\r");
			for(String line : lines){
				String[] args = line.split(";");
				if(args.length==2){
					institutionTypes.add(new ConfigValue(args[0], args[0], args[1]));	
					institutionTypeMap.put(args[0], args[1]);
				}
			}
			

			
			initData();
			
			
			//maa = (MemberAdAdministration)getObject(this, "MemberAdAdministration", "ID", "1");;
			maa = new MemberAdAdministration();
			maa.setParent(this);
			maa.initModule();
			maa.initData(dataStore);
			addSubobject("ApplicationModule", maa);
			cms.registerSpecialElement("maa", maa);
			
			SearchFrontPage sfp = new SearchFrontPage(maa, this);
			cms.registerSpecialElement("sfp", sfp);

			
			
			
			
			
			ocup = new OpenCommunityUserProfile();
			ocup.setParent(this);
			cms.registerSpecialElement("ocup", ocup);
			

			
			reporting = new Reporting(this);
			reporting.setParent(this);
			reporting.setName("reporting");
			reporting.initObjectLocal();
			addSubobject("ApplicationModule", reporting);
			
			Report1 report1 = new Report1();
			report1.setParent(reporting);
			report1.setName("1");
			report1.initObjectLocal();
			reporting.addSubobject("BaseReport", report1);
			
			Report2 report2 = new Report2();
			report2.setParent(reporting);
			report2.setName("2");
			report2.initObjectLocal();
			reporting.addSubobject("BaseReport", report2);
			
		}
		catch(java.lang.Exception e){
			logException(e);
		}
		
		texts = new Hashtable<String, String>();
		languages = new Vector<ConfigValue>();
		languages2 = new Vector<ConfigValue>();

		try{
			WebPage dir = (WebPage)getObjectByPath("/WebApplication/ApplicationModule:cms/WebSiteRoot:1/WebPage:2/WebPage:9");
			if(dir != null){
				
				int cnt = 0;
				
				for(BasicClass page : dir.getObjects("WebPage")){
					cnt++;
					WebPageElement content = (WebPageElement)page.getObjectByIndex("WebPageElement", 0);
					String title = page.getString("Title");
					if(content != null){
						texts.put(title, content.getString("Content"));
						languages.add(new ConfigValue(title, title, title));
						languages2.add(new ConfigValue("" + cnt, "" + cnt, title));
					}
				}
			}
		}
		catch(java.lang.Exception e){
			logException(e);
		}

		String l = openFile(getRootpath() + "/conf/languages.conf", "iso-8859-1");
		lines = l.split("\r\n|\r|\n");
		for(String line : lines){
			String[] args = line.split(";");
			if(args.length==2){
				languages2.add(new ConfigValue(args[0], args[0], args[1]));
			}
		}
		languages3 = new Vector<String>();
		l = openFile(getRootpath() + "/conf/languages.csv", "UTF-8");
		lines = l.split("\r\n|\r|\n");
		for(String line : lines){
			if(line.length() > 0){
				languages3.add(line);
			}
		}
		
		
		
		
		/*
		File confdir = new File(rootpath + "/conf/texts");
		try{
			File[] files = confdir.listFiles();
			for(File file : files){
				if(file.getName().endsWith(".txt")){
					logAccess(file.getName());
					String filecontent = openFile(file.getAbsolutePath());
					String[] args = file.getName().split("\\.");
					texts.put(args[0], filecontent);
				}
			}
		}
		catch(java.lang.Exception e){
			logException(e);
		}
		*/
		
		
		
		scripts = new Hashtable<String, ScriptDefinition>();
		
		try{
			File scriptdir = new File(rootpath + "/scripts");
			File[] files = scriptdir.listFiles();
			for(File file : files){
				if(file.getName().endsWith(".txt")){
					ScriptDefinition scriptDef = new ScriptDefinition();
					String filecontent = openFile(file.getAbsolutePath());
					String[] args = file.getName().split("\\.");
					scriptDef.setProperty("Title", args[0]);
					scriptDef.setProperty("ScriptBody", filecontent);
					scriptDef.setParent(this);
					scriptDef.initObjectLocal();
					addSubobject("ScriptDefinition", scriptDef);
				}
			}
		}
		catch(java.lang.Exception e){
			logException(e);
		}	
		
		news = (NewsAdministration)getObject( this, "NewsAdministration", "ID", "1");
		news.setParent(this);
		cms.registerSpecialElement("news", news);
		
		statistics = new Statistics(this);
		
		interpreter = new Interpreter();
		
		cms.initObject();
		

		
		

		

		
		//---------------------------------------------------------------------------------------------------------------------------------------------------------------------
		

		

		


		
		

		
		fileUpload = new FileUpload();
		
		//---------------------------------------------------------------------------------
		
		dossierAdministration = new DossierAdministration();
		dossierAdministration.setParent(this);
		dossierAdministration.initObject();
		addSubobject("ApplicationModule", dossierAdministration);
		
		projectAdministration = new ProjectAdministration();
		projectAdministration.setParent(this);
		projectAdministration.initObject();
		addSubobject("ApplicationModule", projectAdministration);
		
		mailForm = new EmailAdministrationForm();
		mailForm.setParent(this);
		mailForm.setName("EmailAdministrationForm");
		addSubobject("ApplicationModule", mailForm);
		mailForm.initModule();
		
		//-------------------------AK-20161022---------------------------------------------
		
		try{
			
			querylists = new Hashtable();
		
			mainMenu = new MainMenu();
			
			organisationMemberList = new OrganisationMemberList(this);
			organisationMemberList.setParent(this);
			addSubobject("View", organisationMemberList);
			
			memberAdList = new MemberAdList(this);
			chequeList = new ChequeList(this);
			
			querylists.put("organisationMemberList", organisationMemberList);
			querylists.put("memberAdList", memberAdList);
			querylists.put("chequeList", chequeList);
			
			chequeAdministration = new ChequeAdministration(this, chequeList);
			chequeAdministration.setParent(this);
			chequeAdministration.setName("ChequeAdministration");
			addSubobject("ApplicationModule", chequeAdministration);
			
			newsletterAdministration = new NewsletterAdministration();
			newsletterAdministration.setParent(this);
			newsletterAdministration.setName("NewsletterAdministration");
			addSubobject("ApplicationModule", newsletterAdministration);
			
		}
		catch(java.lang.Exception e){
			logException(e);
		}	
		
		try{ //icons for cms
			
			CodeSource src = this.getClass().getProtectionDomain().getCodeSource();
			if (src != null) {
			  URL jar = src.getLocation();
			  ZipInputStream zip = new ZipInputStream(jar.openStream());
			  while(true) {
				ZipEntry e = zip.getNextEntry();
				if (e == null)
				  break;
				String name = e.getName();

				if (name.startsWith("res/cms/images/")) {
					logAccess(name);
					
					String[] args = name.split("/");
					String iconname = args[args.length-1];
					ImageObject io = new ImageObject();
					io.setParent(cms);
					io.setProperty("FileName", iconname);
					
					ByteArrayOutputStream output = new ByteArrayOutputStream();
					
					int data = 0;
					while( ( data = zip.read() ) != - 1 )
					{
						output.write( data );
					}
 

					output.close();
					
					io.setCache(output.toByteArray());
					
					
					
					cms.cacheImageObject(iconname, io);
				}
			  }
			  
			  zip.close();
			} 
			else {

			}
			//InputStream input = getClass().getResourceAsStream("/classpath/to/my/file");
			
			functions = new CodeDefinition();
			functions.setProperty("SortBy", 0);
			functions.setParent(this);
			String values = "1;Mitglied Stiftungsrat: Pr�sident\n";
			values += "2;Mitglied Stiftungsrat: Vizepr�sident\n";
			values += "3;Mitglied Stiftungsrat: normales Mitglied\n";
			values += "4;Gesch�ftsf�hrer oder Sekret�r\n";
			values += "5;Andere Funktion";
			functions.setProperty("Values", values);
			
			functions.initObject();
			
			
			maa.initCommercialAds(dataStore);
			
			sessions = new HashMap<String, BasicUserSession>();
			
			
		}
		catch(java.lang.Exception e){
			logException(e);
		}
		
		}
		catch(java.lang.Exception e){
			System.out.println(e);
		}
		
		

	}
	@Override
	public void initApplication(){
		
		HTMLFormManager formManager = new HTMLFormManager(this);
		setFormManager(formManager);
		initGUI();
	}
	public CodeDefinition getCodeDefinition(String name){
		return codeDefinitions.get(name);	
	}
	public void initCodes(){
		
		codeDefinitions = new HashMap<String, CodeDefinition>();
		
		String codes = openFile(getRootpath() + "/conf/codes.conf", "iso-8859-1");
		String[] lines = codes.split("\r\n|\r|\n");
		CodeDefinition codeDefinition = null;
		StringBuilder values = null;
		for(String line : lines){
			if(line.startsWith("[")){
				if(codeDefinition != null){
					addSubobject("CodeDefinition", codeDefinition);	
					codeDefinition.setParent(this);
					codeDefinition.setProperty("Values", values.toString());
					codeDefinition.initObjectLocal();
					
				}
				String title = line;
				line.replace("[|]", "");
				String[] args = line.split(":");
				
				logAccess("adding code : " + args[1]);
				
				codeDefinition = new CodeDefinition();
				codeDefinition.setName(args[0]);
				codeDefinition.setProperty("SortBy", "2");
				codeDefinitions.put(args[1], codeDefinition);
				values = new StringBuilder();
					
			}
			else{
				if(values != null){
					values.append(line + "\n");	
				}
			}
			
		}
		if(codeDefinition != null){
			addSubobject("CodeDefinition", codeDefinition);	
			codeDefinition.setParent(this);
			codeDefinition.setProperty("Values", values.toString());
			codeDefinition.initObjectLocal();			
		}
		
	}
	public void addScript(ScriptDefinition scriptDefinition) {
		scripts.put(scriptDefinition.getTitle(), scriptDefinition);
	}
		
	public ScriptDefinition getScript(String id) {
		return scripts.get(id);
	}
	public Map<String, ScriptDefinition> getScripts(){
		return scripts;
	}
	public CodeDefinition getFunctions(){
		return functions;
	}
	
	public synchronized Object executeScript(String script, BasicClass owner, Map<java.lang.String,java.lang.Object> attrs){
		try{
			interpreter.set("OWNER", owner);
			interpreter.set("ATTRIBUTES", attrs);
			Object output = interpreter.eval(script);
			return output;
		}
		catch(Exception e){
			logException(e);
			return "";
		}

	}
	public synchronized Object executeScript(String script, BasicClass owner, Map<java.lang.String,java.lang.Object> attrs, OpenCommunityUserSession userSession){
		try{
			interpreter.set("OWNER", owner);
			interpreter.set("USERSESSION", userSession);
			if(attrs != null){
				interpreter.set("ATTRIBUTES", attrs);
			}
			Object output = interpreter.eval(script);
			return output;
		}
		catch(Exception e){
			logException(e);
			return "";
		}

	}
	public DataStore getDataStore(){
		return dataStore;	
	}
	public CMS getCMS(){
		return cms;
	}
	public Accounting getAccounting(){
		return accounting;
	}
	public OpenCommunityUserProfile getUserProfile(){
		return ocup;	
	}
	public Vector<ConfigValue> getGeoObjects(){
		return geoobjects;	
	}
	public Vector<ConfigValue> getInstitutionTypes(){
		return institutionTypes;	
	}
	public Map<String, String> getInstitutionTypeMap(){
		return institutionTypeMap;	
	}
	public String doGet(HttpServletRequest request, HttpServletResponse response) {
		

		
		try{
			request.setCharacterEncoding("UTF-8");
		}
		catch(java.lang.Exception e){
			
		}
		handleRequest(request, response);
		return "";
		
	}
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		
		try{
			request.setCharacterEncoding("UTF-8");
		}
		catch(java.lang.Exception e){
			
		}
		logAccess("handling POST request ...");

		HttpSession session = request.getSession();
		OpenCommunityUserSession usersession = (OpenCommunityUserSession)session.getAttribute("usersession");
		logAccess(ServletFileUpload.isMultipartContent(request));
		
		if (ServletFileUpload.isMultipartContent(request)) {
		
			try{
						
				Object result = uploadFile(request, response);
				
				logAccess("upload result " + result);
				
				if (result != null) {
					if(result instanceof String){
						if(!((String)result).isEmpty()){
							response.getWriter().write(result.toString());
						}
					}
					else if(result instanceof ActionResult){

						response.getWriter().write(((ActionResult)result).toXML());

					}
					else{
						
					}
					
				}
			}
			catch(java.lang.Exception e){
				logException(e);
			}			
		}
		else{
			handleRequest(request, response);
		}
		
	}
	public void handleRequest(HttpServletRequest request, HttpServletResponse response){
	
		ActionResult result = null;
		PrintWriter writer = null;
	
		

	
		HttpSession session = request.getSession();
		session.setAttribute("webapp", this);
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)session.getAttribute("usersession");
		
		
		if(userSession==null){
			userSession = new OpenCommunityUserSession();
			session.setAttribute("usersession", userSession);
			userSession.setParent(this);
			userSession.initObject();
			if(fileUpload != null){
				String images = fileUpload.downloadFile(getString("bannerserverurl") + "?action=getadlist","user","password");
				logAccess("imagelist : " + images);
				String[] i = images.split(";");
				String[][] commercialads = new String[i.length][2];
				int cnt = 0;
				for(String image : i){
					String[] args = image.split(":");
					if(args.length==2){
						commercialads[cnt][0] =	args[0];
						commercialads[cnt][1] = args[1];
					}
					cnt++;
				}
				userSession.setCommercialsAds(commercialads);
			}
			userSession.setAdvertisements(advertising.getAdvertisementList());
			logAccess("create new usersession");
			String code = createPassword(8);
			userSession.setName(code);
		}
		
		logAccess("usersession: " + userSession);
		logAccess("organisationmember: " + userSession.getOrganisationMember());
		
		WebApplicationContext context = new WebApplicationContext(request, response);
		
		String objectPath = request.getParameter("objectPath"); 
		//String objectpath = request.getParameter("objectpath");
		
		
		String action = request.getParameter("action");
		
		String path = request.getRequestURI();	
		
		logAccess(path);
		String[] parts = path.split("/");

		if(action != null){
			logAccess(action);
			logAccess(objectPath);
			BasicClass object = null;

			
			String tempID = request.getParameter("tempID");
			String client = request.getParameter("client");	
			logAccess("Client: " + client);
			logAccess("TempID: " + tempID);
			
			//---------------------------cms, auslagern in eigene Klasse-------------------------------
			
			
				

					//redirect = "administration.jsp?action=edit&objectpath=" + bc.getParent().getPath();
					
				
				
				
			//-----------------------------------------------------------------------------------------------------------------------------
			
			String redirect = null;
			//String redirect = cms.handleRequest(action, request, response);
			
			if(redirect != null){
				try{
					response.sendRedirect(redirect);
				}
				catch(java.lang.Exception e){
					logAccess(e);
				}	
			}
			//else{
			
			try {
				
				/**************************************Word-Addin****************************************************/
						
				logAccess("Action: " + action);
				if (tempID != null) {
					// Word authentication with temporary ID
					userSession = wordClientSessions.get(tempID);
					WordServer.onWordClientAction(action, request, response, this, userSession, tempID);
				}
				else if ("wordAdmin".equals(client) && !action.equals("login")) {
							// Word authentication with session cookie
							userSession = (OpenCommunityUserSession)session.getAttribute("usersession");
							logAccess("Objectpath: " + objectPath);
							WordServer.onWordClientAction(action, request, response, this, userSession);
				}	
				
				

				else{
					
			
					if(objectPath != null){
								//object = getObjectByPath(objectPath);
					}
					if(object instanceof BasicOCObject){
								//result = han
					}			

					if(action.equals("login")){
						
						response.setContentType("text/html; charset=UTF-8");
						writer = response.getWriter();	
						
						boolean authenticated = false;
						
						String username = request.getParameter("username");
						String password = request.getParameter("password");
						String referer = request.getParameter("referer");
						
						if(username.equals("sysadm")){
							
							redirect = handleAdminLogin(request, username, password);
							
							logAccess("redirect : " + redirect);
							
							if(redirect.equals("index.jsp")){
								
								userSession = (OpenCommunityUserSession)session.getAttribute("usersession");
								
								session.setAttribute("loggedin", true);
								session.setAttribute("issysadm", true);
								userSession.initObject();;
								userSession.setSysadmin(true);
								userSession.setLoggedIn(true);
							
							
								if(referer != null && referer.equals("null")==false){
									response.sendRedirect(referer);
								}
								else{
									response.sendRedirect("administration2/index.jsp");
								}
								
							}
							else{
								response.sendRedirect("administration2/login.jsp");
							}
										
							
						}
						else{

							try{
								Class c = Class.forName(getString("emailadministration"));
								Method m = c.getMethod("login", String.class, String.class, String.class);
								java.lang.Boolean success = (java.lang.Boolean)m.invoke(null, getString("emailurl"), username, password);
								if(success.booleanValue()==true){
									userSession = new OpenCommunityAdminSession();
									session.setAttribute("usersession", userSession);
									userSession.setParent(this);
									
									AbstractEmailAdministration ea = (AbstractEmailAdministration)c.newInstance();
									ea.setCredentials(username, password);
									ea.setURL(getString("emailurl"));
									session.setAttribute("emailadministration", ea);
									authenticated = true;
									session.setAttribute("loggedin", true);
									//session.setAttribute("issysadm", true);
									
									userSession.initObject();
									userSession.put("emailadministration", ea);
									userSession.setSysadmin(true);
									userSession.setLoggedIn(true);
									
									if(referer != null && referer.equals("null")==false){
										response.sendRedirect(referer);
									}
									else{
										//response.sendRedirect("administration.jsp");
										response.sendRedirect("administration/index.jsp");
									}
								}
							}
							catch(java.lang.Exception e){
								logException(e);
							}
							if(!authenticated){
								if(username.equals("sysadm") && password.equals("nachbardemo")){
									try{
										userSession = new OpenCommunityAdminSession();
										session.setAttribute("usersession", userSession);
										userSession.setParent(this);
										session.setAttribute("loggedin", true);
										session.setAttribute("issysadm", true);
										userSession.initObject();;
										userSession.setSysadmin(true);
										userSession.setLoggedIn(true);

										if(referer != null && referer.equals("null")==false){
											response.sendRedirect(referer);
										}
										else{
											//response.sendRedirect("administration.jsp");
											response.sendRedirect("administration/index.jsp");
										}
									}
									catch(java.lang.Exception e){
										  
									}
										
								}
								else if(username.equals("nachbardemo") && password.equals("nachbardemo")){
									try{
										userSession = new OpenCommunityAdminSession();
										session.setAttribute("usersession", userSession);
										userSession.setParent(this);
										session.setAttribute("loggedin", true);
										userSession.initObject();

										if(referer != null && referer.equals("null")==false){
											response.sendRedirect(referer);
										}
										else{
											response.sendRedirect("administration.jsp");
										}
									}
									catch(java.lang.Exception e){
										  
									}
								}
								else if(username.equals("cmsadmin") && password.equals("x5tz8mlq")){
									
									try{
										userSession = new OpenCommunityAdminSession();
										session.setAttribute("usersession", userSession);
										userSession.setParent(this);
										session.setAttribute("loggedin", true);
										session.setAttribute("issysadm", false);
										userSession.initObject();;
										userSession.setSysadmin(false);
										userSession.setCMSAdmin(true);
										userSession.setLoggedIn(true);

										if(referer != null && referer.equals("null")==false){
											response.sendRedirect(referer);
										}
										else{
											//response.sendRedirect("administration.jsp");
											response.sendRedirect("administration/index.jsp");
										}
									}
									catch(java.lang.Exception e){
										  
									}								
									
								}
								else{
									username = username.trim();
									if(username.indexOf("@")==-1){
										int diff = 8 - username.length();
										for(int i = 0; i < diff; i++){
											username = "0" + username;	
										}
									}
									ObjectCollection results = new ObjectCollection("Results", "*");
									String sql = "";
									if(username.indexOf("@")==-1){
										sql = "SELECT OrganisationMemberID AS ID FROM Login WHERE Username=\'" + username + "\' AND Password=\'" + password + "\'";
									}
									else{
										sql = "SELECT t1.OrganisationMemberID AS ID FROM Login AS t1 JOIN OrganisationMember AS t2 ON t1.OrganisationMemberID=t2.ID";
										sql += " JOIN Person AS t3 ON t2.Person=t3.ID";
										sql += " JOIN Contact AS t4 ON t4.PersonID=t3.ID AND t4.Type=3";
										sql += " WHERE t4.Value=\'" + username.trim() + "\' AND t1.Password=\'" + password + "\'";
									}
									logAccess(sql);
									queryData(sql , results);
									if(results.getObjects().size()==1){
										userSession = new OpenCommunityUserSession();
										userSession.setParent(this);
													//userSession.setLogin(login);
										session.setAttribute("usersession", userSession);					
										if ("wordAdmin".equals(client)) {
											writer.print("OK");
										}
										else{
											Record record = (Record)results.getObjectByIndex(0);
											OrganisationMember om = (OrganisationMember)getObject(null, "OrganisationMember", "ID", record.getString("ID"));
											if(om != null){
												userSession.setOrganisationMember(om);
												userSession.setOrganisationMemberID(new Integer(om.getName()).intValue());
												om.setParent(userSession);
												om.initObjectLocal();
												writer.print("OK");
												
												response.sendRedirect(getBaseURL(request) + "/" + getString("instancename") + "/cms/nachbarnet/profile");
											}
											else{
												writer.print("Login failed");
											}
																			
										}
									}
									else{
										writer.print("Login failed");						
									}
								}
							}
						}
					}
					else if(action.equals("logout")){
						userSession.removeMemberAdIDs();
						userSession.setLoggedIn(false);
						userSession.setSysadmin(false);
						userSession.setOrganisationMember(null);
						response.setContentType("text/html; charset=UTF-8");
						writer = response.getWriter();	
						writer.print("LOGOUT");
					}
					else if (tempID != null) {
						// Word authentication with temporary ID
						userSession = wordClientSessions.get(tempID);
						WordServer.onWordClientAction(action, request, response, this, userSession, tempID);
					}
					else if(action.equals("getmemberadcategoryeditform")){
						String memberadcategoryid = request.getParameter("memberadcategoryid");
						if(memberadcategoryid != null){
							response.setContentType("text/html; charset=UTF-8");
							result = new ActionResult(Status.OK, "Formular geladen");
							result.setParam("dataContainer", "editform");
							result.setData(maa.getMemberAdCategoryEditForm(memberadcategoryid, context));
						}
						else{
							result = new ActionResult(Status.FAILED, "Formular nicht geladen");					
						}
					}		
					else if(action.equals("getfilteredvalues")){
						String fieldid = request.getParameter("fieldid");
						String value = request.getParameter("value");
						if(fieldid != null && value != null){
							value = value.toLowerCase();
							StringBuilder html = new StringBuilder();
							//html.append("<div>");
							for(String language : languages3){
								if(language.toLowerCase().indexOf(value) > -1){
									html.append("<div onclick=\"selectItem(this)\">" + language + "</div>");	
								}
							}
							//html.append("</div>");
							result = new ActionResult(Status.OK, "Liste geladen");
							result.setParam("fieldid", fieldid);
							result.setData(html.toString());
						}
					}
					else if(userSession.hasAction(action)){
						
						result = userSession.onAction(this, action, context);
						
					}
					else if(action.equals("getObjectAction")){
						String command = request.getParameter("command");
						if(command != null){

							String objectpath = request.getParameter("objectPath");
							BasicClass bc = getObjectByPath(objectpath, userSession);
							logAccess("object : " + bc);
							if(bc != null){
								if(bc instanceof ActionHandler){
									logAccess("object : " + bc);
									result = ((ActionHandler)bc).onAction( this, command, context);	
								}
							}
						}
					}
					else if(action.equals("saveObject")){
						String objectpath = request.getParameter("objectpath");
						BasicClass bc = getObjectByPath(objectpath);
						logAccess("object : " + bc);
						if(bc != null){
							List<String> pnames = bc.getPropertySheet().getNames();
							for(String name : pnames){
								Property p = bc.getProperty(name);
								String value = request.getParameter(name);
								if(value != null){
									bc.setProperty(name, value);
	
								}
							  
	
							}
							updateObject(bc);
							bc.initObjectLocal();
						}
					}
					else if(action.equals("savememberadcategoryeditform")){
								String memberadcategoryid = request.getParameter("memberadcategoryid");
								if(memberadcategoryid != null){
									response.setContentType("text/html; charset=UTF-8");
									maa.saveMemberAdCategoryEditForm(memberadcategoryid, context);
									result = new ActionResult(Status.OK, "Formular gespeichert");
									//result.setParam("dataContainer", "editform");
									//result.setData(maa.getMemberAdCategoryEditForm(memberadcategoryid));
								}
					}
					else if(action.equals("orderaddresses")){
						if(userSession.getOrganisationMember()==null){
							result = startProcess("ch.opencommunity.process.MemberRegistration", userSession, request.getParameterMap(), context, this);
						}
						else{
							result = new ActionResult(Status.OK, "Formular geladen");
							result.setParam("loadpage", "../profile~sectionid=merkliste");							
						}
						
					}
					else if(action.equals("selectad")){
						String memberadid = request.getParameter("memberadid");
						if(memberadid != null){
							userSession.addMemberAdID(memberadid);
							result = new ActionResult(Status.OK, "Formular geladen");
							result.setParam("refresh", "currentpage");
						}
					}
					else if(action.equals("deselectad")){
						String memberadid = request.getParameter("memberadid");
						if(memberadid != null){
							userSession.removeMemberAdID(memberadid);
							String mode = request.getParameter("mode");
							
							if(mode != null && mode.equals("admin2")){
								if(userSession.getOrganisationMember() != null){
									OrganisationMemberController omc = (OrganisationMemberController)userSession.getObjectByName("OrganisationMemberController", userSession.getOrganisationMember().getName());
									if(omc != null){
										result = new ActionResult(Status.OK, "Formular geladen");
										result.setParam("dataContainer", "userprofile");
										result.setData(OpenCommunityUserProfile.getMemoryList2(this, omc, userSession, true, 1));
									}
								}
							}
							if(mode != null && mode.equals("admin")){
								result = new ActionResult(Status.OK, "Formular geladen");
								result.setParam("dataContainer", "userprofile");
								result.setData(OpenCommunityUserProfile.getMemoryList(this, userSession, true));
							}
							else{
								//String[] s = new String[1];
								//s[0] = "0";
								//usersession.put("memberadid", s);
								userSession.remove("memberadid");
								result = new ActionResult(Status.OK, "Formular geladen");
								result.setParam("refresh", "currentpage");
							}
						}
					}
					else if(action.equals("reloadmemorylist")){
						for(int i = 1; i < 17; i++){
							if(context.hasProperty("comment_" + i)){
								userSession.put("comment_" + i, context.getString("comment_" + i));									
							}								
						}
						
						if(context.hasProperty("mode") && context.getString("mode").equals("admin2")){
							if(userSession.getOrganisationMember() != null){
								OrganisationMemberController omc = (OrganisationMemberController)userSession.getObjectByName("OrganisationMemberController", userSession.getOrganisationMember().getName());
								if(omc != null){
									result = new ActionResult(Status.OK, "Formular geladen");
									result.setParam("dataContainer", "userprofile");
									result.setData(OpenCommunityUserProfile.getMemoryList2(this, omc, userSession, true, 2));
								}
							}							
						}
						else{
							result = new ActionResult(Status.OK, "Formular geladen");
							result.setParam("dataContainer", "userprofile");
							result.setData(OpenCommunityUserProfile.getMemoryList(this, userSession, true));	
						}
					}
					else if(action.equals("pausead")){
						String memberadid = request.getParameter("memberadid");
						if(memberadid != null){
							try{
								Connection con = getConnection();
								Statement stmt = con.createStatement();
								
								String validFrom = "";
								String validUntil = "";
								
								ResultSet res = stmt.executeQuery("SELECT * FROM MemberAd WHERE ID=" + memberadid);
								
								while(res.next()){
									validFrom = res.getString("ValidFrom");
									validUntil = res.getString("ValidUntil");

								}

								MemberAdModification mam = new MemberAdModification();
								mam.setProperty("ValidFrom", validFrom);
								mam.setProperty("ValidUntil", validUntil);
								mam.addProperty("MemberAdID", "String", memberadid);
								insertSimpleObject(mam);
								stmt.execute("UPDATE MemberAd SET Status=2, ValidUntil=\'" + DateConverter.dateToSQL(new java.util.Date(), false) + "\' WHERE ID=" + memberadid);
								//stmt.e
							}
							catch(java.lang.Exception e){
								logAccess(e);
							}
							result = new ActionResult(Status.OK, "Formular geladen");
							result.setParam("refresh", "currentsection");
						}
					}
					else if(action.equals("deactivatead")){
						String memberadid = request.getParameter("memberadid");
						String omid = request.getParameter("omid");
						if(memberadid != null && omid != null){
							try{
								Connection con = getConnection();
								Statement stmt = con.createStatement();
								stmt.execute("UPDATE MemberAd SET Status=3 WHERE ID=" + memberadid);
								con.close();
								
								String mailcontent = "";
								
								String code = createLoginCode(omid, context);
								
								String link = "\n\n" + getString("hostname") + "/servlet.srv?action=onetimelogin&redirect=profile&sectionid=inserate&code=" + code;
								

								mailcontent = getTextblockContent( "48", true);

								
								mailcontent = mailcontent.replace("<@link>", link);
								mailcontent = mailcontent.replace("<br />", "");
								
								createAndSendEmail(omid, "Geben Sie Ihr Feedback", mailcontent, context);
								
							}
							catch(java.lang.Exception e){
								logAccess(e);
							}
							result = new ActionResult(Status.OK, "Formular geladen");
							result.setParam("refresh", "currentsection");
						}
					}
					else if(action.equals("reactivatead")){
						String memberadid = request.getParameter("memberadid");
						if(memberadid != null){
							try{
								Connection con = getConnection();
								Statement stmt = con.createStatement();
								
								String validFrom = "";
								String validUntil = "";
								String lastValidUntil = null;
								
								ResultSet res = stmt.executeQuery("SELECT * FROM MemberAdModification WHERE MemberAdID=" + memberadid);
								
								while(res.next()){
									validFrom = res.getString("ValidFrom");
									//lastValidUntil = res.getString("ValidUntil");

								}
								
								res = stmt.executeQuery("SELECT * FROM MemberAd WHERE ID=" + memberadid);
								
								while(res.next()){
									validFrom = res.getString("ValidFrom");
									validUntil = res.getString("ValidUntil");

								}

								MemberAdModification mam = new MemberAdModification();
								mam.setProperty("ValidFrom", validFrom);
								mam.setProperty("ValidUntil", validUntil);
								mam.addProperty("MemberAdID", "String", memberadid);
								insertSimpleObject(mam);
								if(lastValidUntil==null){
									logAccess("ValidFrom: " + validFrom);
									java.text.DateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd");
									java.util.Date d =  df.parse(validFrom);  
									Calendar c = Calendar.getInstance();
									c.setTime(d);
									int year = c.get(Calendar.YEAR);
									c.set(Calendar.YEAR, year + 1);
									 
									stmt.execute("UPDATE MemberAd SET Status=1, ValidUntil=\'" + DateConverter.dateToSQL(c.getTime(), false) + "\' WHERE ID=" + memberadid);
								}
								else{
									stmt.execute("UPDATE MemberAd SET Status=1, ValidUntil=\'" + lastValidUntil + "\' WHERE ID=" + memberadid);
								}
							}
							catch(java.lang.Exception e){
								logException(e);
							}
							result = new ActionResult(Status.OK, "Formular geladen");
							result.setParam("refresh", "currentsection");
						}
					}
					else if(action.equals("copyad")){
						String memberadid = request.getParameter("memberadid");
						if(memberadid != null){
							try{
								MemberAd ma = (MemberAd)getObject(null, "MemberAd", "ID", memberadid);
								if(ma != null){
									MemberAd newAd = (MemberAd)ma.clone(context);
									
									newAd.addProperty("OrganisationMemberID", "String", ma.getString("OrganisationMemberID"));
									newAd.setProperty("Status", "0");
									
									java.util.Date now = new java.util.Date();
									newAd.setProperty("ValidFrom", DateConverter.dateToSQL(now, false)); 
									Calendar c = Calendar.getInstance();
									c.setTime(now);
									int year = c.get(Calendar.YEAR);
									c.set(Calendar.YEAR, year + 1);
									newAd.setProperty("ValidUntil", DateConverter.dateToSQL(c.getTime(), false)); 
						
									String newid = insertObject(newAd, true);
									Hashtable pars = new Hashtable();
									pars.put("MemberAdID", newid);
									result = startProcess("ch.opencommunity.process.MemberAdEdit", userSession, pars , context, this);
								}
							}
							catch(java.lang.Exception e){
								logException(e);
							}
						}
						
					}
					/*********************************************2017-09-12-moved-to-usersession*****************************************************************s
					else if(action.equals("prolongad")){
						String memberadid = request.getParameter("memberadid");
						String code = request.getParameter("code");
						if(code!=null){
							String sql = "UPDATE MemberAd SET ValidUntil=(ValidUntil +  INTERVAL \'" + 365 + " days\') WHERE ID=" + memberadid + " AND Code=\'" + code + "\'";
							executeCommand(sql);
							
						}
						else{
							if(memberadid != null){
								result = startProcess("ch.opencommunity.process.MemberAdFinalize", userSession, request.getParameterMap(), context, this);
							}
						}
					}
					*/
					else if(action.equals("deletead")){
						String memberadid = request.getParameter("memberadid");
						String code = request.getParameter("code");
						if(code==null && memberadid != null){
							String sql = "UPDATE MemberAd SET Status=3 WHERE ID=" + memberadid + " AND Code=\'" + code + "\'";
							executeCommand(sql);
						}
					}
					else if(action.equals("feedbackad")){
						String memberadid = request.getParameter("memberadid");
						if(memberadid != null){
							result = startProcess("ch.opencommunity.process.MemberAdFinalize", userSession, request.getParameterMap(), context, this);
						}
						else{
							result = startProcess("ch.opencommunity.process.MemberAdFinalize", userSession, request.getParameterMap(), context, this);
						}
					}
					else if(action.equals("feedbackfinalize")){
						String memberadid = request.getParameter("memberadid");
						if(memberadid != null){
							result = startProcess("ch.opencommunity.process.FeedbackFinalize", userSession, request.getParameterMap(), context, this);
						}
						String feedbackid = request.getParameter("feedbackid");
						if(feedbackid != null){
							result = startProcess("ch.opencommunity.process.FeedbackFinalize", userSession, request.getParameterMap(), context, this);
						}
					}
					else if(action.equals("creatememberadrequests")){
						
							result = startProcess("ch.opencommunity.process.MemberAdRequestGroupCreate", userSession, request.getParameterMap(), context, this);
							
							/*
							ocup.createMemberAdRequests(context);
							result = new ActionResult(Status.OK, "Formular geladen");
							result.setParam("feedback", "Ihre Bestellung wurde registriert. Sobald die Kontatke freigeschaltet worden sind, werden Sie benachrichtigt.");		
							*/				
					}
					else if(action.equals("deactivatecontact")){
						String memberadid = request.getParameter("memberadrequestid");
						if(memberadid != null){
							try{
								Connection con = getConnection();
								Statement stmt = con.createStatement();
								stmt.execute("UPDATE MemberAdRequest SET Status=3 WHERE ID=" + memberadid);
							}
							catch(java.lang.Exception e){
								logAccess(e);
							}
							result = new ActionResult(Status.OK, "Formular geladen");
							result.setParam("refresh", "currentsection");
						}
					}
					else if(action.equals("activatecontact")){
						String memberadid = request.getParameter("memberadrequestid");
						if(memberadid != null){
							try{
								Connection con = getConnection();
								Statement stmt = con.createStatement();
								stmt.execute("UPDATE MemberAdRequest SET Status=2 WHERE ID=" + memberadid);
							}
							catch(java.lang.Exception e){
								logAccess(e);
							}
							result = new ActionResult(Status.OK, "Formular geladen");
							result.setParam("refresh", "currentsection");
						}
					}
					else if(action.equals("activatead")){
							/*
							String memberadid = request.getParameter("memberadid");
							if(memberadid != null){
								result = startProcess("ch.opencommunity.process.MemberAdActivate", usersession, request.getParameterMap(), context, this);
							}
							*/
							String omid = request.getParameter("omid");
							if(omid != null){
								result = startProcess("ch.opencommunity.process.MemberAdActivate", userSession, request.getParameterMap(), context, this);
							}
					}
					else if(action.equals("searchorganisationmember")){
						
							String familyname = request.getParameter("familyname");
							String firstname = request.getParameter("firstname");
							String index = request.getParameter("index");
						
							StringBuilder html = new StringBuilder();
							ObjectCollection results = new ObjectCollection("Result", "*");
							String sql = "SELECT t1.ID, t3.FamilyName, t3.FirstName, t3.DateOfBirth FROM OrganisationMember AS t1";
							sql += " LEFT JOIN Person AS t2 ON t1.Person=t2.ID";
							sql += " LEFT JOIN Identity AS t3 ON t3.PersonID=t2.ID";
							sql += " WHERE t3.FamilyName ILIKE \'%" + familyname + "%\' AND t3.Firstname ILIKE \'%" + firstname + "%\'";
							sql += " ORDER BY t3.FamilyName, t3.Firstname";
							
							logAccess(sql);
							queryData(sql, results);
							
							html.append("<form id=\"organisationmembersearch\" name=\"organisationmembersearch\">");
							html.append("Name: <input name=\"familyname\" value=\"" + familyname + "\">");
							html.append("Vorname: <input name=\"firstname\" value=\"" + firstname + "\">");
							html.append("<input type=\"hidden\" name=\"index\" value=\"" + index + "\">");
							html.append("<input type=\"button\" onclick=\"organisationMemberSearch()\" value=\"Erneut suchen\">");
							html.append("</form>");
							html.append("<table>");
							
							for(BasicClass record : results.getObjects()){
								html.append("<tr>");
								html.append("<td>" + record.getString("ID") + "</td>");
								html.append("<td>" + record.getString("FAMILYNAME") + "</td>");
								html.append("<td>" + record.getString("FIRSTNAME") + "</td>");
								html.append("<td>" + record.getString("DATEOFBIRTH") + "</td>");
								html.append("<td><a href=\"javascript:insertOrganisationMemberID(" + record.getString("ID") + ", " + index + ")\">&Uuml;bernehmen</a></td>");
								html.append("</tr>");
							}

							html.append("</table>");
							
							html.append("<input type=\"button\" onclick=\"hidePopup()\" value=\"Abbrechen\">");
						
							result = new ActionResult(Status.OK, "Formular geladen");
							result.setParam("dataContainer", "popup");	
							result.setParam("dataContainerVisibility", "visible");	
							result.setData(html.toString());
							
					}
					else if(action.equals("addfielddefinition")){
								String memberadcategoryid = request.getParameter("memberadcategoryid");
								if(memberadcategoryid != null){
									MemberAdCategory mac = (MemberAdCategory)maa.getObjectByName("MemberAdCategory", memberadcategoryid);
									if(mac != null){
										ch.opencommunity.base.FieldDefinition fd = new ch.opencommunity.base.FieldDefinition();
										fd.addProperty("MemberAdCategoryID", "String", memberadcategoryid);
										String id = insertSimpleObject(fd);
										getObject(mac, "FieldDefinition", "ID", id);
										result = new ActionResult(Status.OK, "Formular geladen");
										result.setParam("dataContainer", "editform");
										result.setData(maa.getMemberAdCategoryEditForm(memberadcategoryid));	
									}
								}
							}
							else if(action.equals("opendocument")){
								String docid = context.getString("docid");
								//String objectPath = context.getString("objectPath");
								if(objectPath != null){            
									result = openDocument(null, objectPath, userSession);
								}
								else if(docid != null){            
									result = openDocument(docid, null, userSession);
								}
							}
							else if(action.equals("openexternaldocument")){
								String docid = context.getString("docid");
								if(docid != null){
									openExternalDocument(docid, context);
								}
							}
							else if(action.equals("getaccountmovementlist")){

								result = new ActionResult(Status.OK, "Formular geladen");
								result.setParam("dataContainer", "accountmovements");
								//result.setData(accounting.getAccountMovementList(accountid, start));
								result.setData(accounting.getAccountMovementList(context));
							}
							else if(action.equals("createprocess")){
								userSession.put("OMID", request.getParameter("OMID"));
								userSession.put("MAID", request.getParameter("MAID"));
								//result = startProcess(request.getParameter("name"), userSession, request.getParameterMap(), context, this);
								result = startProcess(request.getParameter("name"), userSession, request.getParameterMap(), context, userSession);
							}
							else if(action.equals("creatememberad")){
								result = startProcess("ch.opencommunity.process.MemberAdCreate", userSession, null, context, maa);
							}
							else if(action.equals("creatememberadrequest")){
								result = startProcess("ch.opencommunity.process.MemberAdRequestCreate", userSession, null, context, maa);
					}
					else if(action.equals("getMainMenu")){
						result = new ActionResult(ActionResult.Status.OK, "Menu geladen");

						result.setData(getMainMenu(context, userSession));
						result.setParam("dataContainer", "mainmenu");
					}
					else if(action.equals("getmainmenu")){
						/*
						result = new ActionResult(Status.OK, "Menu geladen");
						result.setParam("dataContainer", "menu");
						StringBuilder html = new StringBuilder();
								
						html.append("<div class=\"logo\"><img src=\"images/logo.gif\"></div>");
						html.append("<div class=\"menutop\">&nbsp;</div>");
						html.append("<div class=\"menuentry\"><a href=\"javascript:loadPage(\'memberadsearchform\')\">Angebote suchen</a></div>");
						html.append("<div class=\"menuentry\"><a href=\"javascript:createMemberAdd()\">Angebot platzieren</a></div>");
						html.append("<div class=\"menubottom\">&nbsp;</div>");
						html.append("<div class=\"login\">");
						if(userSession.getOrganisationMember() != null){
							OrganisationMember om = userSession.getOrganisationMember();
							html.append(om.getPerson().getIdentity().getString("FirstName") + " " + om.getPerson().getIdentity().getString("FamilyName"));
							html.append("<br><input type=\"button\" onclick=\"logout()\" value=\"Abmelden\">");
							html.append("<br><a href=\"javascript:loadPage(\'profile\')\">Mein Profil</a>");
						}
						else{
							html.append("Login");
							html.append("<br>Benutzername<br><input id=\"username\" value=\"hunziker\">");
							html.append("<br>Passwort<br><input type=\"password\" id=\"password\" value=\"1234\">");
							html.append("<br><input type=\"button\" onclick=\"login()\" value=\"Anmelden\">");
						}
						html.append("</div>");
						result.setData(html.toString());
						*/
					}
					else if(action.equals("getadmintabs")){
						
						result = new ActionResult(Status.OK, "Menu geladen");
						result.setParam("dataContainer", "tabs");
						String sectionid = "home";
						if(userSession.get("currentsection") != null){
							sectionid = (String)userSession.get("currentsection");	
						}
						result.setParam("exec", "loadSection('" + sectionid + "');");
						result.setData(mainMenu.toHTML(context));	
						
					}
					else if(action.equals("getusertabs")){
						result = new ActionResult(Status.OK, "Menu geladen");
						result.setParam("dataContainer", "tabs");
						StringBuilder html = new StringBuilder();                                     
						html.append("<table id=\"usertabs\"><tr>");
						for(BasicClass o : userSession.getObjects("OrganisationMemberController")){
							if(userSession.getActiveObject() != null && userSession.getActiveObject().equals(o)){
								html.append("<td class=\"tab\"><div  id=\"tab_" + o.getName() + "\" class=\"tab_active\"><a class=\"tab\" href=\"javascript:editOrganisationMember('" + o.getName() + "')\">" + o + "</a><img src=\"images/close_small.png\" onclick=\"onAction('" + o.getPath() + "','close')\"></div></td>");	
							}
							else{
								html.append("<td class=\"tab\"><div  id=\"tab_" + o.getName() + "\" class=\"tab_inactive\"><a class=\"tab\" href=\"javascript:editOrganisationMember('" + o.getName() + "')\">" + o + "</a><img src=\"images/close_small.png\" onclick=\"onAction('" + o.getPath() + "','close')\"></div></td>");	
							}
						}
						for(BasicClass o : userSession.getObjects("DossierController")){
							if(userSession.getActiveObject() != null && userSession.getActiveObject().equals(o)){
								html.append("<td class=\"tab\"><div  id=\"tab_" + o.getName() + "\" class=\"tab_active\"><a class=\"tab\" href=\"javascript:openDossier('" + o.getName() + "')\">" + o + "</a><img src=\"images/close_small.png\" onclick=\"onAction('" + o.getPath() + "','close')\"></div></td>");	
							}
							else{
								html.append("<td class=\"tab\"><div  id=\"tab_" + o.getName() + "\" class=\"tab_inactive\"><a class=\"tab\" href=\"javascript:openDossier('" + o.getName() + "')\">" + o + "</a><img src=\"images/close_small.png\" onclick=\"onAction('" + o.getPath() + "','close')\"></div></td>");	
							}
						}
						html.append("</tr></table>");
						result.setData(html.toString());							
					}
					else if(action.equals("gettree")){
						String treeRoot = context.getString("treeRoot");
						if(treeRoot != null){

							BasicClass treeRootObject = getObjectByPath(treeRoot);
							if(treeRootObject != null){
								result = new ActionResult(Status.OK, "Baum geladen");		
								result.setData(getFormManager().getObjectTree(treeRootObject));
								result.setParam("dataContainer", "tree");
								
							}
						}
						
					}
					else if(action.equals("searchads")){
						
						for(Object o : request.getParameterMap().keySet()){
							String key = (String)o;
							userSession.put(key , request.getParameterValues(key));
						}
						
						String mode = request.getParameter("mode");
						String category = request.getParameter("category");
						
						String location = request.getParameter("location");
						userSession.put("location", location);
						
						String[] offset = {"0"}; //AK 20170605
						userSession.put("offset", offset); 
						
						//logAccess("category: " + category);
						
						//logAccess("category: " + request.getHeader("referer"));


						if(category != null && category.length() > 0 && category.equals("0")==false){

							
							if(request.getParameter("reload") != null){
								userSession.put("offset", null);	
								userSession.clear();
								for(Object o : request.getParameterMap().keySet()){
									String key = (String)o;
									userSession.put(key , request.getParameterValues(key));
								}
							}
							userSession.put("category", category);
							userSession.put("mode", "searchresults");
							
							String url = request.getHeader("referer").replace("?mode=start", "");
							if(url.indexOf("profile") > -1){
								url = "/pinnwand/searchresults";
							}
							else if(!url.endsWith("/searchresults")){
								url += "/searchresults";
							}
							response.sendRedirect(url);

							//result = new ActionResult(Status.OK, "Menu geladen");
							//result.setParam("refresh", "currentpage");

						}
						else{
							userSession.put("mode", null);
							if(request.getHeader("referer").endsWith("?mode=start")){
								response.sendRedirect(request.getHeader("referer"));
							}
							else{
								response.sendRedirect(request.getHeader("referer") + "?mode=start");
							}
						}						
						
					}
					else if(action.equals("getaddetail")){
						logAccess(action);
						result = new ActionResult(Status.OK, "Menu geladen");
					}
					else if(action.equals("showimpressum")){
						TextBlock impressum = getTextblock("3");
						if(impressum != null){
							result = new ActionResult(Status.OK, "Detailsuche geladen");
							result.setParam("dataContainer", "wizardContent");
							result.setParam("processStatus", "start");
							result.setParam("title", "Impressum");
							result.setData(impressum.getString("Content"));
							
						}
						
					}
					/*/todelete AK 20180414
					else if(action.equals("detailsearch")){
						
						//userSession.clear();
						
						for(Object o : request.getParameterMap().keySet()){
							String key = (String)o;
							userSession.put(key , request.getParameterValues(key));
						}
						
						String memberadid = request.getParameter("memberadid");
						if(memberadid != null){
                                              
							String select = request.getParameter("select");
							if(select.equals("true")){
								userSession.addMemberAdID(memberadid);								
							}
							else{
								userSession.removeMemberAdID(memberadid);
								userSession.remove("memberadid");
							}
						}
						
						result = new ActionResult(Status.OK, "Detailsuche geladen");
						result.setParam("dataContainer", "resultlist");
						
						String embedded = request.getParameter("embedded");
						if(embedded != null && embedded.equals("true")){
							if(userSession.getCurrentProcess() != null  && userSession.getCurrentProcess() instanceof OrganisationMemberEdit){
								result.setData(maa.getSearchResults(context, ((OrganisationMemberEdit)userSession.getCurrentProcess()).getCurrentControler().getOrganisationMember(), true));
							}
							else{
								result.setData(maa.getSearchResults(context, true));
							}
						}
						else{
							result.setData(maa.getSearchResults(context));				
						}
							
						
						String reloadmemorylist = request.getParameter("reloadmemorylist");
						if(reloadmemorylist != null && reloadmemorylist.equals("true")){
							result.setParam("exec", "reloadMemoryList()");
						}
					}
					*/
					else if(action.equals("setdocumentstatus")){
						String documentid = request.getParameter("documentid");
						if(documentid != null && documentid.length() > 0){
							executeCommand("UPDATE Document SET Status=1 WHERE ID=" + documentid);	
						}
						result = new ActionResult(Status.OK, "Menu geladen");
						result.setParam("refresh", "documents");
					}
					/*/todelete:AK 20180414
					else if(action.equals("loadpage")){  // still needed?
						String pageid = request.getParameter("pageid");
						if(pageid != null){
									result = new ActionResult(Status.OK, "Seite geladen");
									result.setParam("dataContainer", "display");
									if(pageid.equals("memberadentryform")){
										result.setData(maa.getMemberAdEntryForm());
									}
									else if(pageid.equals("memberadsearchform")){
										result.setData(maa.getMemberAdSearchForm(context));
									}
									else if(pageid.equals("profile")){
										result.setData(getUserProfile(userSession));
									}
									else if(pageid.equals("feedback")){
										StringBuilder html = new StringBuilder();
										html.append("Feedback");
										result.setData(html.toString());
									}
								
						}
								
					}
					*/
					else if(action.equals("loadprofilesection")){
								String pageid = request.getParameter("pageid");
								if(pageid != null){
									result = new ActionResult(Status.OK, "Seite geladen");
									result.setParam("dataContainer", "userprofile");	
									
									result.setData(ocup.getProfileSection(pageid, context));
								}
					}
					else if(action.equals("getcontactprintlist")){
						

						
						OrganisationMember om = userSession.getOrganisationMember();
						if(om != null){
							PDFWriter pdfWriter = new PDFWriter();
							PDFTemplateLibrary templib = (PDFTemplateLibrary)getObjectByName("PDFTemplateLibrary", "1");
							
							String docname = createPassword(8);	
							
							String filename = getRootpath() + "/temp/" + docname + ".pdf";
							
							Vector instances = new Vector();
							
							Record addresslist = new Record();
							
							Vector criteria1 = new Vector();
							Vector criteria2 = new Vector();
							Vector criteria3 = new Vector();
							
							Map addpars = request.getParameterMap();
							
							String contacts = ocup.getContactList(this, om.getName(), false, addpars, criteria1, criteria2);
							contacts = contacts.replace("<br>", "\n");
							
							addresslist.addProperty("CONTACTLIST", "String" , contacts);
							
							instances.add(addresslist);
						
							pdfWriter.createPDF(filename, templib, "3", instances);	
							
							result = new ActionResult(Status.OK, "Seite geladen");							
							
							result.setParam("download", "/temp/" + docname + ".pdf");
							
						}
						

						
					}
					else if(action.equals("loadsection")){
						String sectionid = request.getParameter("sectionid");
						userSession.put("currentsection", sectionid);
						boolean reload = false;
						String sReload = request.getParameter("reload");
						if(sReload != null && sReload.equals("true")){
							reload = true;	
						}
						if(sectionid != null){
							result = new ActionResult(Status.OK, "Sektion geladen");
							result.setParam("dataContainer", "admindisplay");	
							if(sectionid.equals("home")){
								StringBuilder html = new StringBuilder();
								
								//html.append("<input type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.process.SendEmails\',\'Mode=1\')\" value=\"Emails verschicken\">");
								
								html.append("<h4>Neu registrierte Benutzer</h4>");
								html.append("<div style=\"height : 300px; border : 1px solid black; overflow : auto;\">");
								//html.append(getOrganisationMemberList(context, " WHERE t1.Status IN (0,3,4)", "home", true));
								//html.append(getOrganisationMemberList(context, " WHERE t1.Status IN (0, 3, 4)", "home", true));
								html.append("</div>");
								
								html.append("<h4>Neue Inserate</h4>");
								//html.append("<input type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.process.SendRequestConfirmation\',\'Mode=1\')\" value=\"Inserateinhaber benachrichtigen\">");
								//html.append("<input type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.process.SendRequestConfirmation\',\'Mode=3\')\" value=\"Verl�ngerung anfragen\">");
								html.append("<input type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.process.MemberAdSendRenewalRequest\',\'Mode=1\')\" value=\"Verl�ngerung anfragen per Email\">");
								html.append("<input type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.process.MemberAdSendRenewalRequest\',\'Mode=2\')\" value=\"Verl�ngerung anfragen per Brief\">");
								html.append("<input type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.process.MemberAdSendRenewalRequest\',\'Mode=3\')\" value=\"Abgelaufene Inserate l�schen\">");
								
								
								html.append("<div style=\"height : 300px; border : 1px solid black; overflow : auto;\">");
								context.addProperty("sectionid", "String", "home");
								memberAdList.getDataTable(html, context);
								//html.append(MemberAdList.getMemberAdList(this, context, userSession, " WHERE t1.Status in (0,4)", "home"));
								html.append("</div>");
								
								html.append("<h4>Neue Adressbestellungen</h4>");
								
								html.append("<input type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.process.SendRequestConfirmation\',\'Mode=2\')\" value=\"Adressbesteller benachrichtigen\">");
								html.append("<input type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.process.SendRequestConfirmation\',\'Mode=4\')\" value=\"Feedback anfragen\">");
								
								html.append("<div style=\"height : 300px; border : 1px solid black; overflow : auto;\">");
								html.append(getMemberAdRequestList2(context, userSession, " WHERE t6.Status IN (0, 4)", "home", true));
								html.append("</div>");
								
								/*
								html.append("<table>");
								html.append("<tr><td>Anzahl neu registrierte Benutzer:</td><td></td></tr>");
								html.append("<tr><td>Anzahl nicht freigeschalteter Inserate:</td><td>");
								ObjectCollection results = new ObjectCollection("Results", "*");
								
								queryData("SELECT COUNT(ID) AS CNT FROM MemberAd WHERE Status=0", results);
								if(results.getObjects().size() > 0){
									BasicClass record = (BasicClass)results.getObjects().elementAt(0);
									html.append(record.getString("CNT"));
								}
								html.append("</td></tr>");
								
								html.append("<tr><td>Anzahl zu vermittelnder Adressbestellungen:</td><td>");
								
								results.removeObjects();
								queryData("SELECT COUNT(ID) AS CNT FROM MemberAdRequest WHERE Status=0", results);
								if(results.getObjects().size() > 0){
									BasicClass record = (BasicClass)results.getObjects().elementAt(0);
									html.append(record.getString("CNT"));
								}
								html.append("</td></tr>");
								
								html.append("</table>");	
								*/
								result.setData(Dashboard.toHTML(this, context));
							}
							else if(sectionid.equals("user")){
								
								
								
								userSession.saveFilterProperties(context);
								
								StringBuilder html = new StringBuilder();
								
								if(!reload){
								html.append("<div class=\"toolbar\">");
								
									html.append("<input type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.process.OrganisationMemberAdd\')\" value=\"Neues Mitglied\">");
									
									html.append("<input type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.process.BatchActivityCreate\', 'Mode=1')\" value=\"Brief an Erstspender\">");
									
									html.append("<input type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.process.PDFCreate\')\" value=\"Spendenbest&auml;tigung\">");
									
									html.append("<input type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.process.CallForDonations\')\" value=\"Spendenaufruf\">");
									
									html.append("<input type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.process.BatchActivityCreate\', 'Mode=2')\" value=\"Brief Mitglieder bez.\">");
									
									html.append("<input type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.process.BatchActivityCreate\', 'Mode=3')\" value=\"Brief Mitglieder n. bez.\">");
								
								}
								
								String filterstring = "";
								
								try{
								
								
								
								String familyname = request.getParameter("FamilyName");
								String operator = " WHERE ";
								if(familyname != null && familyname.length() > 0){
									filterstring += operator + "t3.FamilyName ILIKE \'" + familyname + "%\'";
									operator = " AND ";
								}
								else{
									familyname = "";	
								}
								String firstname = request.getParameter("FirstName");
								if(firstname != null && firstname.length() > 0){
									filterstring += operator + "t3.FirstName ILIKE \'" + firstname + "%\'";
									operator = " AND ";
								}
								else{
									firstname = "";	
								}
								String sex = request.getParameter("Sex");
								if(sex != null && sex.length() > 0){
									filterstring += operator + "t3.Sex=" + sex;
									operator = " AND ";
								}
								else{
									sex = "";	
								}
								String dateofbirth = request.getParameter("DateOfBirth");
								if(dateofbirth != null && dateofbirth.length() > 0){
									filterstring += operator + "t3.DateOfBirth ILIKE \'" + dateofbirth + "%\'";
									operator = " AND ";
								}
								else{
									dateofbirth = "";	
								}
								
								String zipcode = request.getParameter("ZipCode");
								if(zipcode != null && zipcode.length() > 0){
									filterstring += operator + "t4.ZipCode ILIKE \'" + zipcode + "%\'";
									operator = " AND ";
								}
								else{
									zipcode = "";	
								}
								
								String email = request.getParameter("Email");
								if(email != null && email.length() > 0){
									filterstring += operator + "t9.Value ILIKE \'" + email + "%\'";
									operator = " AND ";
								}
								else{
									email = "";	
								}
								
								String noemail = request.getParameter("NoEmail");
								if(noemail != null && noemail.length() > 0){
									filterstring += operator + "t9.Value IS NULL";
									operator = " AND ";
								}
								else{
									noemail= "";	
								}
								
								String isuser = request.getParameter("IsUser");
								if(isuser != null && isuser.length() > 0){
									filterstring += operator + "t10.ID > 0";
									operator = " AND ";
								}
								else{
									isuser= "";	
								}
								
								String ismember = request.getParameter("IsMember");
								if(ismember != null && ismember.length() > 0){
									filterstring += operator + "t11.ID > 0";
									operator = " AND ";
								}
								else{
									ismember = "";	
								}
								
								String issponsor = request.getParameter("IsSponsor");
								if(issponsor != null && issponsor.length() > 0){
									filterstring += operator + "t12.ID > 0";
									operator = " AND ";
								}
								else{
									issponsor = "";	
								}
								
								String istodelete = request.getParameter("IsToDelete");
								if(istodelete != null && istodelete.length() > 0){
									//operator = " AND ";
								}
								else{
									filterstring += operator + "t1.Status != 3";
									operator = " AND ";
								}
								
									if(!reload){
										StringBuilder filterdef = new StringBuilder();
										filterdef.append("<form id=\"filter\">Filtern nach : ");
										
										//getTextField(Property p, boolean isEditable, String prefix, String classid, String style, String javascript)
										
										filterdef.append(" Name " + getFormManager().getTextField(userSession.getProperty("FamilyName"), true, "", null, null, "onkeyup=\"filterList('user')\")"));
										filterdef.append(" Vorname " + getFormManager().getTextField(userSession.getProperty("FirstName"), true, ""));
										filterdef.append(" Geschlecht " + getFormManager().getSelection(userSession.getProperty("Sex"), true, ""));
										filterdef.append(" Geburtsjahr " + getFormManager().getTextField(userSession.getProperty("DateOfBirth"), true, ""));
										filterdef.append(" PLZ <input name=\"ZipCode\" value=\"" + "\">");
										filterdef.append(" Email <input name=\"Email\" value=\"" + "\">");
										filterdef.append(" Keine Email <input type=\"checkbox\" name=\"NoEmail\" value=\"1\">");
										filterdef.append(" Benutzer <input type=\"checkbox\" name=\"IsUser\" value=\"1\">");
										filterdef.append(" Mitglied <input type=\"checkbox\" name=\"IsMember\" value=\"1\">");
										filterdef.append(" G�nner <input type=\"checkbox\" name=\"IsSponsor\" value=\"1\">");
										filterdef.append(" Inkl. zu l�schende <input type=\"checkbox\" name=\"IsToDelete\" value=\"1\">");
										filterdef.append(" <input type=\"button\" onclick=\"filterList('user')\"  value=\"Filtern\"></form>");
										
										html.append(filterdef.toString());
									
									}
								}
								catch(java.lang.Exception e){
									logException(e);	
								}
								if(!reload){
									html.append("</div>");
									html.append("<div id=\"user_list\" class=\"objectlist\">");
								}
								html.append(getOrganisationMemberList(context, filterstring, "user", true));
								
								if(!reload){
									html.append("</div>");
								}
								else{
									result.setParam("dataContainer", "user_list");
								}
								result.setData(html.toString());
							}
							else if(sectionid.equals("user2")){
								StringBuilder html = new StringBuilder();
								html.append("<div class=\"toolbar\">");	
								html.append("</div>");
								html.append("<div class=\"objectlist\">");
								html.append(organisationMemberList.toHTML(context));
								html.append("</div>");
								result.setData(html.toString());							
							}
							else if(sectionid.equals("user3")){
								result.setData(organisationMemberList.toHTML(context));							
							}
							else if(sectionid.equals("accounting")){
								StringBuilder html = new StringBuilder();
								html.append("<div class=\"toolbar\">");
								html.append("<input type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.process.AccountAdd\')\" value=\"Neues Konto\">");
								html.append("<input type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.process.AccountMovementAdd\')\" value=\"Neue Buchung\">");
								html.append("<input type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.process.DonationAdd\')\" value=\"Neuer Spendenbeitrag\">");
								html.append("</div>");
								html.append("<div class=\"objectlist\">");
								html.append(accounting.getAccountList(context));
								html.append("</div>");							
								result.setData(html.toString());							
							}
							else if(sectionid.equals("cheques")){
								/*
								StringBuilder html = new StringBuilder();
								html.append("<div class=\"toolbar\">");	
								html.append("</div>");
								html.append("<div class=\"objectlist\">");
								html.append(chequeList.toHTML(context));
								html.append("</div>");
								result.setData(html.toString());	
								*/
								result.setData(chequeAdministration.toHTML(context));	
								
							}
							else if(sectionid.equals("calendar")){
								StringBuilder html = new StringBuilder();
								html.append("<div class=\"toolbar\">");
								html.append("<input type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.news.NewsMessageAdd\')\" value=\"Neuer Eintrag\">");
								html.append("</div>");	
								html.append("<div id=\"tree\">" + getFormManager().getObjectTree(news, "NewsMessage") + "</div>");
								html.append("<div id=\"objectEditArea\" style=\"left : 300px;\"></div>");
								result.setData(html.toString());
							}
							else if(sectionid.equals("statistics")){
								StringBuilder html = new StringBuilder();
								
								html.append(reporting.getMainForm(context));
								
								/*
								html.append("<div class=\"toolbar\">");
								html.append("</div>");
								html.append("<div class=\"objectlist\">");
								html.append(statistics.toHTML());
								html.append("</div>");			
								*/
								
								result.setData(html.toString());							
							}
							else if(sectionid.equals("organisations")){

								result.setData(dossierAdministration.getMainForm(context));		
								
							}
							else if(sectionid.equals("projects")){

								result.setData(projectAdministration.getMainForm(context));		
								
							}
							else if(sectionid.equals("memberadcategories")){
										StringBuilder html = new StringBuilder();
										html.append("<div class=\"toolbar\">");
										html.append("<input type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.process.MemberAdCategoryAdd\')\" value=\"Neue Rubrik\">");
										html.append("</div>");
										html.append("<div class=\"objectlist\">");
										//html.append(getMemberAdCategoryList());
										html.append(getFormManager().getObjectTree(maa, null));
										html.append("</div>");	
										html.append("<div id=\"objectEditArea\" style=\"left : 300px;\"></div>");
										result.setData(html.toString());						
							}
							else if(sectionid.equals("objecttemplates")){
								/*
								StringBuilder html = new StringBuilder();
								html.append("<div class=\"toolbar\">");
								html.append("<input type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.process.ObjectTemplateAdd\')\" value=\"Neue Objektvorlage\">");
								html.append("</div>");
								html.append("<div id=\"tree\">" + HTMLFormManager.getObjectTree(this, "ObjectTemplateAdministration") + "</div>");
								html.append("<div id=\"editArea\"></div>");
								result.setData(html.toString());
								*/
								result.setData(ota.getMainForm(context));
							}
							else if(sectionid.equals("imap")){
								StringBuilder html = new StringBuilder();
								html.append("<div class=\"toolbar\">");
								//html.append("<input type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.news.NewsMessageAdd\')\" value=\"Neuer Eintrag\">");
								html.append("</div>");	
								html.append(mailForm.getMainForm(context));
								result.setData(html.toString());
							}
							else if(sectionid.equals("email")){
								StringBuilder html = new StringBuilder();
										html.append("<div class=\"toolbar\">");
										html.append("<input type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.process.SendEmails\',\'Mode=1\')\" value=\"Emails verschicken\">");
										html.append("</div>");
										html.append("<div class=\"objectlist\">");
										html.append(getActivityList(context));
										html.append("</div>");	
								result.setData(html.toString());
							}
							else if(sectionid.equals("documents")){
								StringBuilder html = new StringBuilder();
										html.append("<div class=\"toolbar\">");
										html.append("<input type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.process.DocumentAdd\')\" value=\"Neues Dokument\">");
										html.append("</div>");
										html.append("<div class=\"objectlist\">");
										html.append(getDocumentList());
										html.append("</div>");	
								result.setData(html.toString());
							}
							else if(sectionid.equals("memberads")){
								
								userSession.saveFilterProperties(context);
								
								StringBuilder html = new StringBuilder();
								
								html.append(memberAdList.toHTML(context));
								
								/*
								if(!reload){
								
									html.append("<div class=\"toolbar\">");
									html.append("<input type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.process.SendRequestConfirmation\',\'Mode=1\')\" value=\"Inserateinhaber benachrichtigen\">");
									html.append("<input type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.process.SendRequestConfirmation\',\'Mode=3\')\" value=\"Verl�ngerung anfragen\">");
									
								}
								String filterstring = "";
								
								String title = request.getParameter("Title");
								String operator = " WHERE ";
								if(title != null && title.length() > 0){
									filterstring += operator + "t1.Title ILIKE \'%" + title + "%\'";
									operator = " AND ";
								}
								else{
									title = "";	
								}
								
								String description = request.getParameter("Description");
								if(description != null && description.length() > 0){
									filterstring += operator + "t1.Description ILIKE \'%" + description + "%\'";
									operator = " AND ";
								}
								else{
									description = "";	
								}
								
								String language = request.getParameter("Language");
								if(language != null && language.length() > 0){
									filterstring += operator + "t4.FirstLangaugeS ILIKE \'%" + language + "%\'";
									operator = " AND ";
								}
								else{
									language = "";	
								}
								
								String category = request.getParameter("Category");
								if(category != null && category.length() > 0){
									filterstring += operator + "t1.Template=" + category;
									operator = " AND ";
								}
								else{
									category = "";	
								}
								
								String type = request.getParameter("Type");
								if(type != null && type.length() > 0){
									
									if(type.equals("0")){
										filterstring += operator + "t1.IsOffer='true'";
										operator = " AND ";
									}
									else if(type.equals("1")){
										filterstring += operator + "t1.IsRequest='true'";
										operator = " AND ";
									}
									else if(type.equals("2")){
										filterstring += operator + "t1.IsOffer='true'";
										filterstring += operator + "t1.IsRequest='true'";
										operator = " AND ";
									}
									
								}
								else{
									type = "";	
								}
								
								String status = request.getParameter("Status");
								if(status != null && status.length() > 0){
									filterstring += operator + "t1.Status=" + status;
									operator = " AND ";
								}
								else{
									status = "";	
								}
								
								String sex = request.getParameter("Sex");
								if(sex != null && sex.length() > 0){
									filterstring += operator + "t4.Sex=" + sex;
									operator = " AND ";
								}
								else{
									sex = "";	
								}
								
								String age = request.getParameter("Age");
								if(age != null && age.length() > 0){
									try{
										int yearofbirth = 2016 - Integer.parseInt(age);
										filterstring += operator + "t4.DateOfBirth ILIKE \'%" + yearofbirth + "%\'";
										operator = " AND ";
									}
									catch(java.lang.Exception e){
										logException(e);	
									}
									
								}
								else{
									age = "";	
								}
								
								String validfrom1 = request.getParameter("ValidFrom1");
								if(validfrom1 != null && validfrom1.length() > 0){
									filterstring += operator + "t1.ValidFrom > '" + validfrom1 + "'";
									operator = " AND ";
								}
								else{
									validfrom1 = "";	
								}
								
								String validfrom2 = request.getParameter("ValidFrom2");
								if(validfrom2 != null && validfrom2.length() > 0){
									filterstring += operator + "t1.ValidFrom < '" + validfrom2 + "'";
									operator = " AND ";
								}
								else{
									validfrom2 = "";	
								}
								
								logAccess(filterstring.toString());
								
								//Stichworten, Bereichen, Angebot / Nachfrage / Tandem, Geschlecht, Alter, Zeitraum, aktiv / inaktiv, Sprache
									
								if(!reload){
									StringBuilder filterdef = new StringBuilder();
									filterdef.append("<form id=\"filter\">Filtern nach : ");
									filterdef.append(" Titel " + getFormManager().getTextField(userSession.getProperty("Title"), true, ""));
									filterdef.append(" Beschreibung " + getFormManager().getTextField(userSession.getProperty("Description"), true, ""));
									filterdef.append(" Sprache " + getFormManager().getTextField(userSession.getProperty("Language"), true, ""));
									filterdef.append(" Rubrik " + getFormManager().getSelection(userSession.getProperty("Category"), true, ""));
									filterdef.append(" Typ " + getFormManager().getSelection(userSession.getProperty("Type"), true, ""));
									filterdef.append(" Status " + getFormManager().getSelection(userSession.getProperty("Status"), true, ""));
									
									filterdef.append(" Geschlecht " + getFormManager().getSelection(userSession.getProperty("Sex"), true, ""));
									filterdef.append(" Alter <input name=\"Age\" value=\"" + "\">");
									
									filterdef.append(" G�ltig von zw. " + getFormManager().getDateField(userSession.getProperty("ValidFrom1"), true, ""));
									filterdef.append(" und " + getFormManager().getDateField(userSession.getProperty("ValidFrom2"), true, ""));
									
									*/
									
									/*
									filterdef.append(" Keine Email <input type=\"checkbox\" name=\"NoEmail\" value=\"1\">");
									filterdef.append(" Benutzer <input type=\"checkbox\" name=\"IsUser\" value=\"1\">");
									filterdef.append(" Mitglied <input type=\"checkbox\" name=\"IsMember\" value=\"1\">");
									filterdef.append(" G�nner <input type=\"checkbox\" name=\"IsSponsor\" value=\"1\">");
									*/
									
									/*
									filterdef.append(" <input type=\"button\" onclick=\"filterList('memberads')\"  value=\"Filtern\"></form>");
									
									
									html.append(filterdef.toString());
											
									html.append("</div>");
									
								
									html.append("<div id=\"memberad_list\" class=\"objectlist\">");
								}
								html.append(MemberAdList.getMemberAdList(this, context, userSession, filterstring));
								if(!reload){
									html.append("</div>");
								}
								else{
									result.setParam("dataContainer", "memberad_list");
								}
								
								*/
								result.setData(html.toString());						
							}
							else if(sectionid.equals("commercialads")){
								
								StringBuilder html = new StringBuilder();
								html.append("<div class=\"toolbar\">");	
								html.append("<input type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.advertising.AdvertisementAdd\')\" value=\"Neue Anzeige\">");
								html.append("</div>");
								html.append("<div class=\"objectlist\">");
								html.append("<div id=\"tree\">" + getFormManager().getObjectTree(advertising, "Advertisement") + "</div>");
								html.append("<div id=\"editArea\"></div>");
								result.setData(html.toString());	
								
							}
							else if(sectionid.equals("batchactivities")){
								StringBuilder html = new StringBuilder();
								html.append("<div class=\"toolbar\">");
								html.append("</div>");
								html.append("<div class=\"objectlist\">");
								html.append(BatchActivityList.getBatchActivityList(this, context, userSession));
								html.append("</div>");
								result.setData(html.toString());
							}
							else if(sectionid.equals("requests")){
								StringBuilder html = new StringBuilder();
											html.append("<div class=\"toolbar\">");
											html.append("<input type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.process.SendRequestConfirmation\',\'Mode=2\')\" value=\"Adressbesteller benachrichtigen\">");
											html.append("<input type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.process.SendRequestConfirmation\',\'Mode=4\')\" value=\"Feedback anfragen\">");
											html.append("</div>");
											html.append("<div class=\"objectlist\">");
											html.append(getMemberAdRequestList(context, userSession, null));
											html.append("</div>");	
								result.setData(html.toString());						
							}
							else if(sectionid.equals("feedback")){
								StringBuilder html = new StringBuilder();
								html.append("<div class=\"toolbar\">");
								html.append("</div>");
								html.append("<div class=\"objectlist\">");
								html.append(getFeedbackList(context, userSession, null));
								html.append("</div>");	
								result.setData(html.toString());
							}
							else if(sectionid.equals("cms")){
								StringBuilder html = new StringBuilder();
								//html.append("<div class=\"toolbar\">");

								//html.append("</div>");	

								html.append("<div id=\"tree\">" + getFormManager().getObjectTree(cms, null, true, 1) + "</div>");
								html.append("<div id=\"editArea\"></div>");
								result.setData(html.toString());
							}
							else if(sectionid.equals("textblocks")){

								TextBlockAdministration  tba = (TextBlockAdministration)getObjectByIndex("TextBlockAdministration", 0);
								if(tba != null){
;									result.setData(tba.toHTML(context));
								}
								
							}
							else if(sectionid.equals("pdf")){
								
							
								StringBuilder html = new StringBuilder();
								html.append("<div class=\"toolbar\">");
								//html.append("<input type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.news.NewsMessageAdd\')\" value=\"Neuer Textblock\">");
								html.append("</div>");	
								PDFTemplateLibrary  pdflib = (PDFTemplateLibrary)getObjectByIndex("PDFTemplateLibrary", 0);
								//html.append(tba);
								if(pdflib != null){
									html.append("<div id=\"tree\">" + getFormManager().getObjectTree(pdflib) + "</div>");
									html.append("<div id=\"objectEditArea\" style=\"left : 300px;\"></div>");
								}
								result.setData(html.toString());
							}
							else if(sectionid.equals("libreoffice")){
								
							
								StringBuilder html = new StringBuilder();

								html.append("<div id=\"tree\">" + getFormManager().getObjectTree(libreOfficeModule, null, true, 1) + "</div>");
								html.append("<div id=\"objectEditArea\" style=\"left : 300px;\">" + libreOfficeModule + "</div>");

								result.setData(html.toString());
							}
							else if(sectionid.equals("files")){
								
								StringBuilder html = new StringBuilder();
								html.append("<div class=\"toolbar\">");	
								//html.append("<input type=\"button\" onclick=\"javascript:createProcess(\'ch.opencommunity.advertising.AdvertisementAdd\')\" value=\"Neue Anzeige\">");
								html.append("</div>");
								html.append("<div id=\"filelist\">");
								WebSiteRoot wsr = (WebSiteRoot)cms.getObjectByIndex("WebSiteRoot", 0);
								if(wsr != null){
									html.append(wsr.getFileList());
								}
								html.append("</div>");
								//html.append("<div id=\"editArea\"></div>");
								result.setData(html.toString());	
								
							}
							else if(sectionid.equals("newsletter")){
								
								result.setData(newsletterAdministration.getMainForm(context));	
								
								
							}
							else if(sectionid.equals("oma")){
								
								result.setData(oma.getMainForm(context));	
								
							}
							
													
						}
					}
					else if(action.equals("sorttable")){
						String tableid = request.getParameter("tableid");
						logAccess("sorting table " + tableid);
						HTMLQueryList querylist = (HTMLQueryList)querylists.get(tableid);
						if(querylist != null){
							
							result = new ActionResult(Status.OK, "Sektion geladen");
							result.setParam("dataContainer", tableid + "_tablearea");	
							StringBuilder html = new StringBuilder();
							querylist.getDataTable(html, context);
							result.setData(html.toString());
						}
					}
					else if(action.equals("editobject")){
						try{
							String objectpath = request.getParameter("objectpath");
							BasicClass bc = getObjectByPath(objectpath);

							if(bc != null){
								
								result = new ActionResult(Status.OK, "Detailsuche geladen");
								result.setParam("dataContainer", "editArea");
								
								if(bc instanceof WebPageElement){
									try{
										String objectid = request.getParameter("objectid");
										
										WebPageElement wpe = (WebPageElement)bc;
										
										StringBuilder html = new StringBuilder();
											
										
										logAccess("edit object : " + wpe.getContentObject());
										
										if(wpe.getContentObject() != null){
											
											logAccess("objectid : " + objectid);
											
											//if(objectid == null){
												//html.append(getFormManager().getEditForm(bc, context));	
											//}
											html.append(wpe.getContentObject().getEditForm(objectid, request));
										}
										else{
											//html.append(form.getEditForm(bc, context));	
											html.append(getFormManager().getEditForm(bc, context));	
										}
			
										result.setData(html.toString());
									}
									catch(java.lang.Exception e){
										logException(e);	
									}
								}
								else if(bc instanceof WebSiteRoot){
									bc.initObject();
									//result.setData(getFormManager().getEditForm(bc, context));	
									result.setData(cms.getWebPageEditForm((WebPage)bc, context, request));
									result.setParam("sidebar", bc.getPath());
								}
								else if(bc instanceof WebPage){
									bc.initObject();
									//result.setData(getFormManager().getEditForm(bc, context));	
									result.setData(cms.getWebPageEditForm((WebPage)bc, context, request));
								}
								else{
									result.setParam("dataContainer", "objectEditArea");
									result.setData(getFormManager().getEditForm(bc, context));	
								}
							}
						}
						catch(java.lang.Exception e){
							logException(e);	
						}
					}
					else if(action.equals("getagb")){
						String language = request.getParameter("language");
						result = new ActionResult(Status.OK, "Detailsuche geladen");
						result.setParam("dataContainer", "agb");
						result.setData(getAGB(language));							
					}
					else if(action.equals("showfeedback")){
						String  feedbackid = request.getParameter("feedbackid");
						Feedback feedback = (Feedback)getObject(null, "Feedback", "ID", feedbackid);
						if(feedback != null){
							OrganisationMemberInfo omi = getOrganisationMemberInfo(feedback.getString("OrganisationMember"));
							feedback.setProperty("OrganisationMember", omi.getString("Title"));
							
							omi = getOrganisationMemberInfo(feedback.getString("OrganisationMemberID"));
							feedback.setProperty("Title", omi.getString("Title"));
							
							StringBuilder html = new StringBuilder();
							html.append(form.getEditForm(feedback, false, null, null, context));
							
							html.append("<input type=\"button\" value=\"Schliessen\" onclick=\"hidePopup()\">");
							
							result = new ActionResult(Status.OK, "Detailsuche geladen");
							result.setParam("dataContainer", "popup");
							result.setParam("dataContainerVisibility", "visible");
							result.setData(html.toString());		
						}
					}
					else if(action.equals("checkfordoubles")){
						
						String familyname = request.getParameter("familyname");
						String firstname = request.getParameter("firstname");
						String dateofbirth = request.getParameter("dateofbirth");
						
						logAccess(familyname);
						
						familyname = familyname.replaceAll("ue", "�");				
						familyname = familyname.replaceAll("�", "(�|u|ue)");
	
						familyname = familyname.replaceAll("ae", "�");		
						familyname = familyname.replaceAll("�", "(�|a|ae)");
	
						familyname = familyname.replaceAll("oe", "�");		
						familyname = familyname.replaceAll("�", "(�|o|oe)");
						
						firstname = firstname.replaceAll("ue", "�");
						firstname = firstname.replaceAll("�", "(�|u|ue)");
	
						firstname = firstname.replaceAll("ae", "�");		
						firstname = firstname.replaceAll("�", "(�|a|ae)");
	
						firstname = firstname.replaceAll("oe", "�");	
						firstname = firstname.replaceAll("�", "(�|o|oe)");
						
						//String sql = "SELECT * FROM Identity WHERE FamilyName ILIKE '" + familyname.trim() + "%' AND FirstName ILIKE '" + firstname.trim() + "%' AND DateOfBirth ILIKE '" + dateofbirth.trim() + "'";
						
						String sql = "SELECT * FROM Identity WHERE FamilyName ~* \'" + familyname.trim() + "\' AND FirstName ~* \'" + firstname.trim() + "\' AND DateOfBirth ILIKE '" + dateofbirth.trim() + "'";
						
						ObjectCollection results = new ObjectCollection("Results", "*");
						queryData(sql, results);
						logAccess(sql);
						if(results.getObjects().size() > 0){
						
							StringBuilder html = new StringBuilder();
							html.append("<div class=\"popupcontent\"><p style=\"color : white;\">Es gibt bereits einen Teilnehmer mit diesem Namen und Geburtsjahr");
							html.append("<p>Falls Sie Ihr Passwort vergessen haben, klicken Sie auf diesen <a href=\"javascript:getNextNode('recovery=true')\"><b>Link</b></a>");
							html.append("<p><input type=\"button\" value=\"Schliessen\" onclick=\"hidePopup()\"></div>");
						
							result = new ActionResult(Status.OK, "Detailsuche geladen");
							result.setParam("dataContainer", "popup");
							result.setParam("dataContainerVisibility", "visible");
							result.setData(html.toString());	
							
						}
						else{
							String mode = request.getParameter("mode");
							if(mode != null && mode.equals("finish")){
								result = new ActionResult(Status.OK, "Detailsuche geladen");
								result.setParam("exec", "getNextNode()");
							}
						}
						
					}
					else if(action.equals("confirmregistration")){
						try{
							String registrationcode = request.getParameter("registrationcode");
							String login = request.getParameter("login");
							Connection con = getConnection();
							Statement stmt = con.createStatement();
							ResultSet res = stmt.executeQuery("SELECT * FROM Login WHERE RegistrationCode=\'" + registrationcode + "\'");
							String organisationMemberID = null;
							while(res.next()){
								organisationMemberID = res.getString("OrganisationMemberID");
							}
							if(organisationMemberID != null){
								
								String codereplacement = createPassword(20);
								stmt.execute("UPDATE OrganisationMember SET Status=4 WHERE Status=0 AND ID=" + organisationMemberID);
								stmt.execute("UPDATE Login SET RegistrationCode='" + codereplacement + "' WHERE OrganisationMemberID=" + organisationMemberID);

								OrganisationMember om = (OrganisationMember)getObject(null, "OrganisationMember", "ID", organisationMemberID, false);

								if(login != null && login.equals("true")){
									                                                                                   
									if(login != null){
										om.setParent(this);
										om.initObjectLocal();
										userSession.setOrganisationMember(om);
										userSession.setOrganisationMemberID(Integer.parseInt(organisationMemberID));
										response.sendRedirect(getBaseURL("", request) + "/profile?welcome=true");
									}
								}
								else{
									userSession.put("registrationconfirmed", "true");
									userSession.put("organisationmember", om);
					
									response.sendRedirect(getBaseURL("", request) + "/profile");
									//response.sendRedirect(getBaseURL("", request) + "/profile?registrationconfirmed=true");
								}
							}
							else{
								response.sendRedirect(getBaseURL("", request) + "/profile?usernotfound=true");
							}
							
						}
						catch(java.lang.Exception e){
							logException(e);	
						}
					}
					else if(action.equals("firstlogin")){
						if(userSession.get("organisationmember") != null){
							
							OrganisationMember om = (OrganisationMember)userSession.get("organisationmember");
							om.setParent(this);
							om.initObjectLocal();
							userSession.setOrganisationMember(om);
							userSession.setOrganisationMemberID(Integer.parseInt(om.getName()));
							
							userSession.put("registrationconfirmed", null);
							userSession.put("organisationmember", null);
							response.sendRedirect(getBaseURL("", request) + "/profile?welcome=true");
							response.sendRedirect(getBaseURL("", request) + "/profile");
						}
						else{
							response.sendRedirect(getBaseURL("", request) + "/profile?usernotfound=true");
						}
						
					}
					else if(action.equals("onetimelogin")){
							String code = request.getParameter("code");	
							if(code != null){
								String omid = handleOneTimeLogin(code);
								if(omid != null){
									
									OrganisationMember om = (OrganisationMember)getObject(null, "OrganisationMember", "ID", omid, false);
									om.setParent(this);
									om.initObjectLocal();
									if(om != null){
										userSession.setOrganisationMember(om);
										userSession.setOrganisationMemberID(Integer.parseInt(omid));
									}
									
									redirect = context.getString("redirect");
									if(redirect != null){
										if(redirect.equals("profile")){
											String sectionid = context.getString("sectionid");
											if(sectionid != null && sectionid.length() > 0){
												response.sendRedirect(getBaseURL("", request) + "/profile?sectionid=" + sectionid);
											}
											else{
												response.sendRedirect(getBaseURL("", request) + "/profile?welcome=true");
											}
										}
									}
									
								}
							}
						
					}
					else if(action.equals("recovery")){
						try{
							String registrationcode = request.getParameter("registrationcode");
							Connection con = getConnection();
							Statement stmt = con.createStatement();
							ResultSet res = stmt.executeQuery("SELECT * FROM Login WHERE RegistrationCode=\'" + registrationcode + "\'");
							String organisationMemberID = null;
							while(res.next()){
								organisationMemberID = res.getString("OrganisationMemberID");
							}
							if(organisationMemberID != null){
								String scramble1 = createPassword(20);
								String scramble2 = createPassword(8);
								//stmt.execute("UPDATE OrganisationMember SET Status=1 WHERE Status=4 AND ID=" + organisationMemberID);
								stmt.execute("UPDATE Login SET Password=RecoveryPassword, RegistrationCode=\'" + scramble1 + "\', RecoveryPassword=\'" + scramble2 + "\' WHERE OrganisationMemberID=" + organisationMemberID);
								OrganisationMember om = (OrganisationMember)getObject(null, "OrganisationMember", "ID", organisationMemberID, false);
								om.setParent(this);
								om.initObjectLocal();
								if(om != null){
									userSession.setOrganisationMember(om);
									userSession.setOrganisationMemberID(Integer.parseInt(organisationMemberID));
								}
								response.sendRedirect(getBaseURL("", request) + "/profile?welcome=true");
							}
							else{
								response.sendRedirect(getBaseURL("", request) + "/profile?usernotfound=true");
							}
							
						}
						catch(java.lang.Exception e){
							logException(e);	
						}
					}
					else if(action.equals("setactiveprofile")){
						OrganisationMember om = userSession.getOrganisationMember();
						if(om != null){
							String id = request.getParameter("id");
							if(id != null){
								if(id.equals(om.getName())){
									om.setActiveRelationship(null);	
									result = new ActionResult(Status.OK, "Profil ge�ndert");
									result.setParam("refresh", "currentsection");										
								}
								else{
									OrganisationMember om2 = (OrganisationMember)getObject(null, "OrganisationMember", "ID", id, false);
									om2.setParent(this);
									om2.initObjectLocal();
									if(om2 != null){
										om.setActiveRelationship(om2);								
										result = new ActionResult(Status.OK, "Profil ge�ndert");
										result.setParam("refresh", "currentsection");
									}
								}
							}
						}
						
						
					}
					else if(action.equals("activaterequest")){
						

						String activationcode = request.getParameter("activationcode");
						if(activationcode != null && activationcode.length()==20){
							
							
							String sql = "SELECT OrganisationMemberID AS omid FROM MemberAdRequest WHERE ActivationCode=\'" + activationcode + "\'";
							
							ObjectCollection results = new ObjectCollection("Results", "*");
							
							queryData(sql, results);
							
							String omid = null;
							OrganisationMemberInfo omi = null;
							
							for(BasicClass bc : results.getObjects()){
								omid = bc.getString("OMID");
								omi = getOrganisationMemberInfo(omid);
							}
							
							if(omid != null && omi != null){
								
								//Todo : in zentraler Funktion zusammmenfassen
								
								String comment = getTextblockContent("4");
								comment = comment.replace("<p>", "\n");
								comment = comment.replace("</p>", "");
								comment = comment.replace("<br />", "");
								comment = comment.trim();
								String content = "";
								String[] lines = comment.split("\r\n|\r|\n");
								for(String line : lines){
									content += "\n" + line.trim();
								}
								
								content = StringEscapeUtils.unescapeHtml4(content);
								
								ObjectTemplate ot = getObjectTemplate("4");
								
								String registrationcode = createPassword(20);
												
								Activity activity = (Activity)ot.createObject("ch.opencommunity.base.Activity", null, context);
								activity.applyTemplate(ot);
								activity.setProperty("Status", "0");
								activity.addProperty("OrganisationMemberID", "String", omid);
								BasicClass note = activity.getFieldByTemplate("21");
													


								String link = "\n\n" + getString("hostname") + "/servlet.srv?action=confirmregistration&registrationcode=" + registrationcode;
								
								content = content.replace("<@addressation>", omi.getAddressation());
								
								content = content.replace("<@link>", link);
								
								note.setProperty("Content", content);
								activity.setProperty("Title", "Benachrichtigung");
								
								String activityid = insertObject(activity, true);
								
								executeCommand("UPDATE Login SET RegistrationCode=\'" + registrationcode + "\' WHERE OrganisationMemberID=" + omid);
							
							
								executeCommand("UPDATE MemberAdRequest SET Status=1, ActivationCode=\'xxxxxxxx\' WHERE ActivationCode=\'" + activationcode + "\'");
								
								
								
								sendAllPendingMails();
								
								response.sendRedirect(getString("hostname") + "/profile?requestactivated=true");
								
							}
						}
					}
					else if(action.equals("denyrequest")){
						

						String activationcode = request.getParameter("activationcode");
						if(activationcode != null && activationcode.length()==20){
							executeCommand("UPDATE MemberAdRequest SET Status=3, ActivationCode=\'xxxxxxxx\' WHERE ActivationCode=\'" + activationcode + "\'");
							response.sendRedirect(getString("hostname") + "/profile?requestdenied=true");
						}
					}
					else if(action.equals("getimageuploadform")){
						logAccess("loading form ...");
						
						String objectpath = request.getParameter("objectPath");
						
						String imageid = context.getString("imageid");
						StringBuilder html = new StringBuilder();
						html.append("<body style=\"margin : 0px; padding : 0px;\"><div style=\"position : absolute; width : 100px;\"><form action=\"servlet.srv\" method=\"POST\" enctype=\"multipart/form-data\">");
						html.append("\n<input type=\"hidden\" name=\"action\" value=\"imageupload\">");
						html.append("<input type=\"hidden\" name=\"imageid\" value=\"" + imageid + "\">");
						String target = context.getString("target");
						if(target != null){
							html.append("<input type=\"hidden\" name=\"target\" value=\"" + target + "\">");
						}
						if(objectpath != null){
							html.append("<input type=\"hidden\" name=\"objectPath\" value=\"" + objectpath + "\">");
						}
						else{
							html.append("<input type=\"hidden\" name=\"objectPath\" value=\"/WebApplication/ApplicationModule:cms/WebSiteRoot:1\">");
						}
						html.append("<input type=\"file\" name=\"imagefile\" style=\"font-size : 9px;\">");
						html.append("<input name=\"title\" style=\"font-size : 10px;\">");
						html.append("<input type=\"submit\" value=\"Bild hochladen\" style=\"font-size : 10px;\">");
						html.append("</form></div></body>");
						
						try{
							response.setContentType("text/html; charset=UTF-8");
							writer = response.getWriter();
							writer.write(html.toString());
							writer.flush();
							writer.close();
						}
						catch(java.lang.Exception e){
							logException(e);
						}
						
						/*
						
						String imageid = context.getString("imageid");
						StringBuilder html = new StringBuilder();
						html.append("<form action=\"servlet.srv\" method=\"POST\" enctype=\"multipart/form-data\">");
						html.append("<input type=\"hidden\" name=\"action\" value=\"imageupload\">");
						html.append("<input type=\"hidden\" name=\"imageid\" value=\"" + imageid + "\">");
						html.append("<input type=\"hidden\" name=\"objectPath\" value=\"/WebApplication/ApplicationModule:cms/WebSiteRoot:1\">");
						html.append("<input type=\"file\" name=\"imagefile\">");
						html.append("<input name=\"title\">");
						html.append("<input type=\"submit\" value=\"Bild hochladen\">");
						html.append("</form>");
						
						try{
							response.setContentType("text/html; charset=UTF-8");
	
							writer = response.getWriter();
							writer.write(html.toString());
							writer.flush();
							writer.close();
						}
						catch(java.lang.Exception e){
							logException(e);
						}
						
						*/
		
					}
					else if(action.equals("getfileuploadform")){
						logAccess("loading form ...");
						
						String omid = context.getString("omid");
						StringBuilder html = new StringBuilder();
						html.append("<form action=\"servlet.srv\" method=\"POST\" enctype=\"multipart/form-data\">");
						html.append("<input type=\"hidden\" name=\"action\" value=\"fileupload\">");
						html.append("<input type=\"hidden\" name=\"omid\" value=\"" + omid + "\">");
						html.append("<input type=\"hidden\" name=\"objectPath\" value=\"/WebApplication/ApplicationModule:cms/WebSiteRoot:1\">");
						html.append("<input type=\"file\" name=\"imagefile\">");
						html.append("<input name=\"title\">");
						html.append("<input type=\"submit\" value=\"Bild hochladen\">");
						html.append("</form>");
						
						try{
							response.setContentType("text/html; charset=UTF-8");
	
							writer = response.getWriter();
							writer.write(html.toString());
							writer.flush();
							writer.close();
						}
						catch(java.lang.Exception e){
							logException(e);
						}
		
					}
					else if(action.equals("gettextblockcontent")){	
						String id = context.getString("id");
						String propertyid = context.getString("propertyid");
						if(id != null){
							TextBlock tb = getTextblock(id);
							String content = getTextblockContent(id);
							
							if(content != null){
								
								content = StringEscapeUtils.unescapeHtml4(content);
								StringBuilder html = new StringBuilder();
								String[] lines = content.split("\r\n|\r|\n");
								for(String line : lines){
									//html.append("\n" + line.trim());	
									html.append(line.trim());	
								}
								
								content = html.toString();
								content = content.replace("<p>", "");
								content = content.replace("</p>", "\n\n");
								content = content.replace("<br />", "\n");
								content = content.trim();

								
								result = new ActionResult(ActionResult.Status.OK, "");
								//result.setData(html.toString());
								result.setData(content);
								result.setParam("propertyid", propertyid);
								int acontext = 9;
								if(id.equals("12")) acontext = 1;
								if(id.equals("14")) acontext = 2;
								if(id.equals("13")) acontext = 3;
								if(id.equals("15")) acontext = 4;
								if(id.equals("16")) acontext = 5;
								if(id.equals("10")) acontext = 8;
								result.setParam("context", "" + acontext);
								result.setParam("title", "" + tb.getString("Title"));
								
								
							}
							
						}
						
					}
					else if(action.equals("gettextblockcontent2")){	
						String id = context.getString("id");
						String propertyid = context.getString("propertyid");
						if(id != null){
							TextBlock tb = getTextblock(id);
							String content = getTextblockContent(id);
							
							if(content != null){
								
								content = StringEscapeUtils.unescapeHtml4(content);
								StringBuilder html = new StringBuilder();
								String[] lines = content.split("\r\n|\r|\n");
								for(String line : lines){
									//html.append("\n" + line.trim());	
									html.append(line.trim());	
								}
								
								content = html.toString();
								content = content.replace("<p>", "");
								content = content.replace("</p>", "\n\n");
								content = content.replace("<br />", "\n");
								content = content.trim();

								
								result = new ActionResult(ActionResult.Status.OK, "");
								//result.setData(html.toString());
								result.setData(content);
								result.setParam("propertyid", propertyid);
								int acontext = 9;
								if(id.equals("12")) acontext = 1;
								if(id.equals("14")) acontext = 2;
								if(id.equals("13")) acontext = 3;
								if(id.equals("15")) acontext = 4;
								if(id.equals("16")) acontext = 5;
								if(id.equals("10")) acontext = 8;
								result.setParam("context2", "" + acontext);
								result.setParam("title", "" + tb.getString("Title"));
								
								
							}
							
						}
						
					}
					else if(action.equals("getfileuploadform")){					
							String srcdoc = "<form action=\"servlet.srv\" method=\"POST\" enctype=\"multipart/form-data\">";
							srcdoc += "<input type=\"file\" name=\"uploadfile\">";
							srcdoc += "Bezeichnung :<input name=\"title\">";
							srcdoc += "<input type=\"hidden\" name=\"action\" value=\"fileupload\">";
							srcdoc += "<input type=\"hidden\" name=\"objectPath\" value=\"/WebApplication/ApplicationModule:cms/WebSiteRoot:1\">";
							srcdoc += "<input type=\"submit\" value=\"Datei hochladen\">";
							srcdoc += "</form>";
							
						try{
							response.setContentType("text/html; charset=UTF-8");
	
							writer = response.getWriter();
							writer.write(srcdoc);
							writer.flush();
							writer.close();
						}
						catch(java.lang.Exception e){
							logException(e);
						}
		
					}
					else if (action.equals("previousnode")) {
								synchronized(userSession) {

									BasicProcess process = userSession.getCurrentProcess();
					
									if(process != null){
										List<String> fields = process.getPropertyNames();
										for(String fieldname : fields){
											String value = request.getParameter(fieldname);
						//					System.out.println(fieldname + ":" + value);
											if(value != null){
												process.setProperty(fieldname, value);
											}
										
										}
										result = new ProcessResult(ProcessStatus.CONTINUE);
										result.setParam("dataContainer", "wizardContent");
										BasicProcessNode node = process.getPreviousNode();
										if (node != null) {
											node.initObjectLocal();
											result.setParam("title", node.getTitle());
											if (!node.getAction().isEmpty()) {
												result.setParam("exec", node.getAction());
											}
							
											// ToDo: verbesstertes Mapping von Klassen und Formularen
											
											/*
											HTMLFormDefinition formdefinition = formdefinitions.get(process.getClassname());
											if(formdefinition == null){
												formdefinition = formdefinitions.get(node.getClassname());	
											}
											if (formdefinition != null) {
												result.setData(form.getForm(formdefinition, node, true, context));							
											}
											else {
											*/
												Property extTemplProperty = node.getProperty("ExternalTemplate");
												if (extTemplProperty != null) {
													String externalTemplate = extTemplProperty.getValue();
													result.setParam("loadDocument", externalTemplate);
												}
												else {
													result.setData(form.getNodeForm(node, context));
												}
											/*
											}
											*/
										}
										else {
											result = new ActionResult(Status.IGNORED, "");
										}
									}
									else {
										result = new ActionResult(Status.IGNORED, "");
									}
								}
							}
					else if (action.equals("nextnode")) {
								synchronized(userSession) {
									BasicProcess process = userSession.getCurrentProcess();
									if (process != null) {
										
										List<String> fields = process.getPropertyNames();
										for(String fieldname : fields){
						
											String[] values = request.getParameterValues(fieldname);
						
											if(values != null){
												String value = "";
												for(int i = 0; i < values.length; i++){
													value += values[i];
													if(i < values.length-1){
														value += ";";
													}
												}
												//logAccess(fieldname + ":" + value);
												process.setProperty(fieldname, value);
												
											}
											
										}	
										BasicProcessNode node;
										if (process.isLast(context)) {
											node = process.getCurrentNode();
											logAccess("last node " + node);
											if(node.validate(context)){
												if(process.getSubprocess() != null){
													result = new ProcessResult(ProcessStatus.CONTINUE);
													result.setParam("title", process.getTitle());
													process.getSubprocess().finish((ProcessResult)result, context);
													node = process.getCurrentNode();
													logAccess("CurrentNode: " + node);
													result.setParam("dataContainer", "wizardContent");
													if(node.getName().equals("MemberAdCreateNode")){
														result.setData(((MemberAdCreate)process.getSubprocess()).getMemberAdCreateForm(context));		
													}
													else if(node.getName().equals("MemberAdRequestActivityAdd")){
														result.setData(((MemberAdRequestActivityAdd)process).getMemberAdRequestAcivityAddForm(context));		
													}	
													else if(node.getName().equals("MemberAdRequestCreateNode")){
														result.setData(((MemberAdRequestCreate)process).getMemberAdRequestCreateForm(context));		
													}
													else if(node.getName().equals("ProfileEditNode")){
														result.setData(((ProfileEdit)process).getProfileEditForm(context));		
													}
													else if(process instanceof OrganisationMemberEdit){
														result.setData(((OrganisationMemberEdit)process).getOrganisationMemberEditForm(context));
													}
													else if(node.getName().equals("OrganisationMemberModificationNode")){
														result.setData(((OrganisationMemberModify)process).getOrganisationMemberModificationForm(context));		
													}
													else if(node.getName().equals("MemberAdEditNode")){
														result.setData(((MemberAdEdit)process).getMemberAdEditForm(context));		
													}
													else{
														result.setData(form.getNodeForm(node, context));
													}	
												}
												else{
													logAccess("finish " + node);
													result = new ProcessResult(ProcessStatus.FINISH);
													process.finish((ProcessResult)result, context);
													userSession.setCurrentProcess(null);
													userSession.setCanceledProcess(process.getClassname(), null);
													String returnTo = process.getString("returnTo");
													//checkReturnTarget(returnTo, result);
												}
											}
											else{
												logAccess("last node validate false");
												if(process.getSubprocess() != null){ //ein neuer Prozess wurde gestartet
													node = process.getCurrentNode();
													result = new ProcessResult(ProcessStatus.CONTINUE);
													result.setParam("dataContainer", "wizardContent");
													result.setParam("title", process.getTitle());
													if(node.getName().equals("MemberAdCreateNode")){
														result.setData(((MemberAdCreate)process.getSubprocess()).getMemberAdCreateForm(context));		
													}
													else if(node.getName().equals("MemberAdRequestCreateNode")){
														result.setData(((MemberAdRequestCreate)process.getSubprocess()).getMemberAdRequestCreateForm(context));		
													}
													else if(node.getName().equals("MemberAdRequestActivityAdd")){
														result.setData(((MemberAdRequestActivityAdd)process).getMemberAdRequestAcivityAddForm(context));		
													}	
													/*
													else if(process instanceof OrganisationMemberEdit){
														result.setData(((OrganisationMemberEdit)process).getOrganisationMemberEditForm(context));
													}
													*/
													else if(process.getSubprocess() instanceof OrganisationMemberEdit){
														result.setData(((OrganisationMemberEdit)process.getSubprocess()).getOrganisationMemberEditForm(context));
													}
													else if(node.getName().equals("OrganisationMemberModificationNode")){
														result.setData(((OrganisationMemberModify)process.getSubprocess()).getOrganisationMemberModificationForm(context));		
													}
													else if(node.getName().equals("MemberAdEditNode")){
														result.setData(((MemberAdEdit)process).getMemberAdEditForm(context));		
													}
													else if(node.getName().equals("ch.opencommunity.process.BatchActivityCreate2")){
														result.setData(((BatchActivityCreate2)process).getBatchActivityCreateForm(context));		
													}
													else{
														result.setData(form.getNodeForm(node, context));
													}
												}
												else{
													
													node = process.getCurrentNode(); //AK 20161205, maybe the validation has inserted a new node
													
													result = new ProcessResult(ProcessStatus.CONTINUE);
													result.setParam("dataContainer", "wizardContent");
													result.setParam("title", process.getTitle());
													//result.setData(form.getNodeForm(node, context));
													if(process instanceof OrganisationMemberEdit){
														result.setData(((OrganisationMemberEdit)process).getOrganisationMemberEditForm(context));
													}
													else if(node.getName().equals("MemberAdDetail")){
														result.setData(((MemberAdDetail)process).getMemberAdDetailForm(context));		
													}
													else if(node.getName().equals("MemberAdCreateNode")){
														result.setData(((MemberAdCreate)process).getMemberAdCreateForm(context));		
													}
													else if(node.getName().equals("MemberAdRequestActivityAdd")){
														result.setData(((MemberAdRequestActivityAdd)process).getMemberAdRequestAcivityAddForm(context));		
													}	
													else if(node.getName().equals("MemberAdRequestCreateNode")){
														result.setData(((MemberAdRequestCreate)process).getMemberAdRequestCreateForm(context));		
													}
													else if(node.getName().equals("LoginNode")){
														result.setData(((MemberRegistration)process).getLoginForm(context));		
													}
													else if(node.getName().equals("AGBNode")){
														result.setData(((MemberRegistration)process).getAGBForm(context));		
													}
													else if(node.getName().equals("FeedbackNode")){
														result.setData(((MemberRegistration)process).getFeedbackForm(context));		
													}
													else if(node.getName().equals("FeedbackNode2")){
														result.setData(((MemberAdCreate)process).getFeedbackForm(context));		
													}
													else if(node.getName().equals("ProfileEditNode")){
														result.setData(((ProfileEdit)process).getProfileEditForm(context));		
													}
													else if(node.getName().equals("MemberAdEditNode")){
														result.setData(((MemberAdEdit)process).getMemberAdEditForm(context));		
													}
													else if(node.getName().equals("ch.opencommunity.process.BatchActivityCreate2")){
														result.setData(((BatchActivityCreate2)process).getBatchActivityCreateForm(context));		
													}
													else if(getFormManager().getObjectView(node.getClass().getName()) != null){
														
														BaseObjectView objectView = (BaseObjectView)getFormManager().getObjectView(node.getClass().getName());
														//BasicUserSession userSession = (BasicUserSession)context.getObject("usersession");
														result.setData(objectView.toHTML(node, context, userSession, true));
														
													}
													else {
														Property extTemplProperty = node.getProperty("ExternalTemplate");
														if (extTemplProperty != null) {
															String externalTemplate = extTemplProperty.getValue();
															result.setParam("loadDocument", externalTemplate);
														}
														else {
															result.setData(form.getNodeForm(node, context));
														}
													}

												}
											}
											//result.setParam("dataContainer", "wizardContent");
										}
										else {
											result = new ProcessResult(ProcessStatus.CONTINUE);
											result.setParam("dataContainer", "wizardContent");
											result.setParam("title", process.getTitle());
											node = process.getNextNode(context);
											node.initObjectLocal();
											logAccess("next node " + node);
											result.setParam("title", node.getTitle());
											
											// ToDo: verbesstertes Mapping von Klassen und Formularen
											/*
											HTMLFormDefinition formdefinition = formdefinitions.get(process.getClassname());
											if(formdefinition == null){
												formdefinition = formdefinitions.get(node.getClassname());	
											}
											if (formdefinition != null) {
												result.setData(form.getForm(formdefinition, node, true, context));							
											}
											else {
											*/
												Property extTemplProperty = node.getProperty("ExternalTemplate");
												if (extTemplProperty != null) {
													String externalTemplate = extTemplProperty.getValue();
													result.setParam("loadDocument", externalTemplate);
												}
												else if(process instanceof OrganisationMemberEdit){
													result.setData(((OrganisationMemberEdit)process).getOrganisationMemberEditForm(context));
												}
												else if(node.getName().equals("MemberAdCreateNode")){
													if(process.getSubprocess() != null){
														result.setData(((MemberAdCreate)process.getSubprocess()).getMemberAdCreateForm(context));
													}
													else{
														result.setData(((MemberAdCreate)process).getMemberAdCreateForm(context));
													}		
												}
												else if(node.getName().equals("MemberAdRequestActivityAdd")){
													result.setData(((MemberAdRequestActivityAdd)process).getMemberAdRequestAcivityAddForm(context));		
												}												
												else if(node.getName().equals("MemberAdRequestCreateNode")){
													result.setData(((MemberAdRequestCreate)process).getMemberAdRequestCreateForm(context));		
												}
												else if(node.getName().equals("LoginNode")){
													result.setData(((MemberRegistration)process).getLoginForm(context));		
												}
												else if(node.getName().equals("AGBNode")){
													result.setData(((MemberRegistration)process).getAGBForm(context));		
												}
												else if(node.getName().equals("FeedbackNode")){
													result.setData(((MemberRegistration)process).getFeedbackForm(context));		
												}
												else if(node.getName().equals("FeedbackNode2")){
													result.setData(((MemberAdCreate)process).getFeedbackForm(context));		
												}
												else if(node.getName().equals("ProfileEditNode")){
													result.setData(((ProfileEdit)process).getProfileEditForm(context));		
												}
												else if(process.getSubprocess() instanceof OrganisationMemberEdit){
													result.setData(((OrganisationMemberEdit)process.getSubprocess()).getOrganisationMemberEditForm(context));
												}
												else if(node.getName().equals("ch.opencommunity.process.BatchActivityCreate2")){
													result.setData(((BatchActivityCreate2)process).getBatchActivityCreateForm(context));		
												}
												else if(getFormManager().getObjectView(node.getClass().getName()) != null){
													
													BaseObjectView objectView = (BaseObjectView)getFormManager().getObjectView(node.getClass().getName());
													//BasicUserSession userSession = (BasicUserSession)context.getObject("usersession");
													result.setData(objectView.toHTML(node, context, userSession, true));
													
												}
												else {
													//Property extTemplProperty = node.getProperty("ExternalTemplate");
													if (extTemplProperty != null) {
														String externalTemplate = extTemplProperty.getValue();
													result.setParam("loadDocument", externalTemplate);
													}
													else {
														result.setData(form.getNodeForm(node, context));
													}
												}
											/*
											}
											*/
										}
										if (!node.getAction().isEmpty() && ((ProcessResult)result).getProcessStatus() != ProcessStatus.FINISH) {
											result.setParam("exec", node.getAction());
										}
										if(node.hasProperty("filename") && node.getString("filename").length() > 0){
											result.setParam("download", node.getString("filename"));
											node.setProperty("filename", "");
										}
									}
									else {
										result = new ActionResult(Status.IGNORED, "");
									}
								}
							}
					else if(action.equals("cancelprocess")){
								synchronized(userSession) {
									BasicProcess process = userSession.getCurrentProcess();
									String mode = request.getParameter("mode");
									if (process != null) {
										if(process.getSubprocess() != null){
											process.setSubprocess(null);
											BasicProcessNode node = process.getCurrentNode();
											result = new ProcessResult(ProcessStatus.CONTINUE);
											result.setParam("dataContainer", "wizardContent");
											result.setData(form.getNodeForm(node, context));
											if(process instanceof OrganisationMemberEdit){
												result.setData(((OrganisationMemberEdit)process).getOrganisationMemberEditForm(context));
											}
											else{
												result.setData(form.getNodeForm(node, context));
											}
										}
										else{
											List<String> fields = process.getPropertyNames();
											for(String fieldname : fields){
								
												String[] values = request.getParameterValues(fieldname);
								
												if(values != null){
													String value = "";
													for(int i = 0; i < values.length; i++){
														value += values[i];
														if(i < values.length-1){
															value += ";";
														}
													}
													process.setProperty(fieldname, value);
														
												}
													
											}
											if(mode != null && mode.equals("finish")){ //the process is canceled before the last node, but its finish-method is called
												result = new ProcessResult(ProcessStatus.FINISH);
												process.finish((ProcessResult)result, context);
												userSession.setCurrentProcess(null);
												userSession.setCanceledProcess(process.getClassname(), null);
												String returnTo = process.getString("returnTo");
												//checkReturnTarget(returnTo, result);
											}
											else if (mode != null && mode.equals("suspend")) {
							//					process.suspend();
												if (userSession.getSuspendedProcess() != null) {
													result = new ProcessResult(ProcessStatus.FAILED);
													result.setStatusMsg("Es kann h�chstens ein Prozess gleichzeitig unterbrochen sein");
												}
												else {
													userSession.setCurrentProcess(null);
													userSession.setSuspendedProcess(process);
													result = new ProcessResult(ProcessStatus.SUSPEND);
													result.setParam("title", process.getTitle());
												}
											}
											
											else{
												process.cancel(context);
												userSession.setCurrentProcess(null);
												userSession.setCanceledProcess(process.getClassname(), process);
												result = new ProcessResult(ProcessStatus.CANCEL);
												result.setParam("refresh", "currentsection");
												
											}
										}
									}
									else { 
										// no process, but process wizard might still be open (dummy node for resuming canceled process),
										// so send "CANCEL" (for closing the window) anyway
										result = new ProcessResult(ProcessStatus.CANCEL);
									}
								}
							}
						else if(action.equals("resumeprocess")){
								synchronized(userSession) {
									String resumeCanceled = request.getParameter("resumeCanceled");
						
									BasicProcess process;
									if (resumeCanceled != null) {
										process = userSession.getCanceledProcess(resumeCanceled);
										userSession.setCanceledProcess(resumeCanceled, null);
									}
									else {
										process = userSession.getSuspendedProcess();
										userSession.setSuspendedProcess(null);
									}
									if (process != null) {
						//				process.resume();
										userSession.setCurrentProcess(process);
										result = new ProcessResult(ProcessStatus.RESUME);
										BasicProcessNode node = process.getCurrentNode();
										result.setParam("title", process.getTitle());
										result.setParam("dataContainer", "wizardContent");
										
										String externalTemplate = node.getString("ExternalTemplate");
										if (!externalTemplate.isEmpty()) {
											result.setParam("loadDocument", externalTemplate);
										}
										else {
											result.setData(form.getNodeForm(node, context));
										}
									}
									else {
										result = new ActionResult(Status.IGNORED, "");
									}
								}
							}
							else if(action.equals("editOrganisationMember")){
								
								if(userSession.getCurrentProcess() != null && userSession.getCurrentProcess() instanceof OrganisationMemberEdit){
									String omid = context.getString("omid");
									userSession.put("lastomid", omid);
									if(omid != null && omid.length() > 0){
										OrganisationMember newom = openOrganisationMember(omid);
										((OrganisationMemberEdit)userSession.getCurrentProcess()).addOrganisationMemberControler(newom);
										result = new ProcessResult(ProcessStatus.CONTINUE);
										result.setParam("dataContainer", "wizardContent");
										result.setData(((OrganisationMemberEdit)userSession.getCurrentProcess()).getOrganisationMemberEditForm(context));
									}
									
								}
								else{
									String omid = context.getString("omid");
									userSession.put("lastomid", omid);
									if(omid != null && omid.length() > 0){
										Hashtable params = new Hashtable();
										params.put("OMID", omid);
										
										result = startProcess("ch.opencommunity.process.OrganisationMemberEdit", userSession, params, context, this);
										
									}									
								}
								
								
							}
							else if(action.equals("editOrganisationMember2")){
								String omid = context.getString("omid");
								if(omid != null){
									
									OrganisationMemberController omc = (OrganisationMemberController)userSession.getObjectByName("OrganisationMemberController", omid);
									
									if(omc==null){
										OrganisationMember newom = openOrganisationMember(omid);
										
										
										omc = new OrganisationMemberController(newom);
										omc.setParent(userSession);
										omc.setName(newom.getName());
										
										userSession.addSubobject("OrganisationMemberController", omc);
										
										
									}
									result = new ActionResult(ActionResult.Status.OK, "Profil geladen");
									result.setData(UserProfileView.getUserProfileView(userSession, omc, context));
									result.setParam("openprofile", omid);
									result.setParam("refresh", "usertabs");
									userSession.setActiveObject(omc);
									
								}
								
							}
							else if(action.equals("openDossier")){
								String dossierid = context.getString("dossierid");
								if(dossierid != null){
									
									DossierController dossierController = (DossierController)userSession.getObjectByName("DossierController", dossierid);
									
									if(dossierController==null){
										
										Dossier dossier = (Dossier)getObject(this, "Dossier", "ID", dossierid, false);
										
										dossier.initObject();
										

										dossierController = new DossierController(dossier);
										dossierController.setParent(userSession);
										dossierController.setName(dossier.getName());
										
										userSession.addSubobject("DossierController", dossierController);
										
									}
									result = new ActionResult(ActionResult.Status.OK, "Dossier geladen");
									result.setData(DossierView.getDossierView(userSession, dossierController, context));
									result.setParam("opendossier", dossierid);
									result.setParam("refresh", "usertabs");
									userSession.setActiveObject(dossierController);
									
								}
								
							}
							else if(action.equals("openOrganisationMember")){
								
							}
							else if(action.equals("openwebsite")){
								objectPath = context.getString("objectPath");
								OrganisationMember om = (OrganisationMember)getObjectByPath(objectPath);

								if(om != null){
									String accesscode = context.getString("accesscode");
									logAccess(userSession);
									logAccess(accesscode);
									logAccess(objectPath);
									if(om.getString("AccessCode").equals(accesscode)){
										logAccess("found: " + om);
										userSession.setOrganisationMember(om);
										response.sendRedirect("profile");
									}
								}
								
							}
							else if(action.equals("savecomment")){
								String id = context.getString("id");
								String value = context.getString("value");
								if(id != null && id.length() > 0){
									userSession.put(id, value);
								}
								result = new ActionResult(ActionResult.Status.OK, "OK");
							}
							else if(action.equals("downloadfile")){
								String fileid = context.getString("fileid");
								logAccess("fileid: " + fileid);
								if(fileid != null){
									FileObjectData fileObjectData = (FileObjectData)getObject(null, "FileObjectData", "ID", fileid);
									logAccess("fileobject: " + fileObjectData);
									if(fileObjectData != null){
										
										String filename = fileObjectData.getString("FileName");

										
										String[] args = filename.split("\\.");
										
										String extension = args[args.length-1].toUpperCase();
										
										String contenttype = "";
										if(extension.equals("DOC")){
											contenttype = "application/msword";
										}
										else if(extension.equals("DOCX")){
											contenttype = "application/vnd.openxmlformats-officedocument. wordprocessingml.document";
										}
										else if(extension.equals("PDF")){
											contenttype = "application/pdf";
										}
										else if(extension.equals("JPG")){
											contenttype = "image/jpeg";
										}
										else if(extension.equals("PNG")){
											contenttype = "image/png";
										}
										
										if(request.getParameter("inline") != null){
											response.setContentType(contenttype);
											response.setHeader("Content-disposition", "inline");
										}
										else{
											response.setContentType(contenttype);

											response.setHeader("Content-disposition", "attachment; filename=\"" + filename + "\"");
										}
										OutputStream out = response.getOutputStream();
										
										byte[] filedata = (byte[])fileObjectData.getObject("FileData");
										
										if(filedata != null){
											logAccess("Filedata: " + filedata.length);
											
					
											out.write(filedata);
											out.flush();
											out.close();
											
										}
										

										
									}
									
								}
								
							}
							else if(action.equals("downloadmailmessage")){
								String messageid = context.getString("messageid");
								logAccess("messageid: " + messageid);
								if(messageid != null){
									MailMessageInstance mailMessageInstance = (MailMessageInstance)getObject(null, "MailMessageInstance", "ID", messageid);
									logAccess("message: " + mailMessageInstance);
									if(mailMessageInstance != null){
										

										
										String contenttype = "message/rfc822 eml";

										
										if(request.getParameter("inline") != null){
											response.setContentType(contenttype);
											response.setHeader("Content-disposition", "inline");
										}
										else{
											response.setContentType(contenttype);

											response.setHeader("Content-disposition", "attachment; filename=\"mailmessage\"");
										}
										OutputStream out = response.getOutputStream();
										
										byte[] filedata = (byte[])mailMessageInstance.getObject("Message");
										
										if(filedata != null){
											logAccess("Filedata: " + filedata.length);
											
					
											out.write(filedata);
											out.flush();
											out.close();
											
										}
										

										
									}
									
								}
								
							}
							else if(action.equals("getmailinglist")){
								
								String mailinglist = libreOfficeModule.getMailingList(context);
								
								try{
									response.setContentType("text/xml; charset=UTF-8");
									writer = response.getWriter();	
									writer.write(mailinglist);
								}
								catch(java.lang.Exception e){
									logException(e);	
								}
							}
							else{
								result = new ActionResult(ActionResult.Status.OK, "OK");
							}
							

					} 						
					
				}
				catch (java.lang.Exception e) {
					logException(e);
				}
					
				//}
			}
			else if(parts.length > 3){
		
			//if(session.getAttribute("loggedin")==null){
			if(parts[parts.length-1].equals("blank.docx")){
				try{
					File file = new File(getRootPath() + "/templates/word/blank.docx");
					if(file.exists()){
						response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
						response.setHeader("content-disposition", "inline; filename=\"blank.docx\"");
						byte[] b = openFileToBytes(file.getPath());
						OutputStream out = response.getOutputStream();
						out.write(b, 0, b.length);
						out.flush();
					}
				}
				catch(java.lang.Exception e){
					logException(e);	
				}
				
			}
			else if(parts[2].equals("libreoffice")){
				
				libreOfficeModule.processRequest(request, response, context, userSession);
				
			}
			else if(parts[2].equals("cms")){
				cms.processRequest(request, response, context, userSession);
			}
			else{
				WebSiteRoot wsr = cms.getWebSiteRoot(parts[3]);

				if(wsr != null){
					try{

						Object page = wsr;
						//BasicCMSObject page = wsr;
						if(parts.length > 4){
							page = wsr.getWebPage(parts);
						}
						//logAccess("Page: " + page);
						if(page instanceof WebPage){
							if(((BasicClass)page).getString("Redirect").length() > 0){
								//String url = request.getRequestURL().toString();
								String url = getBaseURL(null, request);
								String[] args = url.split("\\?");
								String newurl = args[0];
								newurl += "/" + ((WebPage)page).getRelativePath("");
								logAccess("newurl : " + newurl);
								if(newurl.endsWith("/")){
									newurl += ((BasicClass)page).getString("Redirect");
								}
								else{
									newurl += "/" + ((BasicClass)page).getString("Redirect");
								}
								logAccess("newurl2 : " + newurl);
								response.sendRedirect(newurl);
							}
							else{
								response.setContentType("text/html; charset=UTF-8");
								writer = response.getWriter();
								writer.write(((WebPage)page).getPageContent(request));
								writer.flush();
							}
						}
						else if(page instanceof ImageObject){
							/*
							ImageObject img = (ImageObject)page;
							File imagefile = new File(getRootPath() + "/websites/" + wsr.getName() + "/images/" + img.getName());
							if(imagefile.exists()){
							
								String filetype = img.getString("FileExtension");
								
								response.setContentType("image/" + filetype);

								
								BufferedImage bi = ImageIO.read(imagefile);
								OutputStream out = response.getOutputStream();
								ImageIO.write(bi, filetype, out);
								out.close();
							}
							*/
							
								
								ImageObject img = (ImageObject)page;	
								logAccess("cached image : " + img.getCache());
								String filetype = img.getString("FileExtension");										
								response.setContentType("image/" + filetype);
								
								if(img.getCache() == null){
									File imagefile = null;
									if(img.getParent() instanceof ImageGallery){
										imagefile = new File(getRootPath() + "/websites/" + wsr.getName() + "/images/galleries/" + img.getParent().getName() + "/" + img.getString("FileName"));
									}
									else if(img.getParent() instanceof Advertisement){
										imagefile = new File(getRootPath() + "/websites/" + wsr.getName() + "/advertising/" + img.getString("FileName"));
									}
									else{
										//imagefile = new File(getRootPath() + "/websites/" + wsr.getName() + "/images/" + img.getName());
										imagefile = new File(getRootPath() + "/websites/" + wsr.getName() + "/images/" + img.getString("FileName"));
									}
									
									//logAccess("serving image " + imagefile);
									
									if(imagefile != null && imagefile.exists()){
																			
										BufferedImage bi = ImageIO.read(imagefile);
										OutputStream out = response.getOutputStream();
										ImageIO.write(bi, filetype, out);
										out.close();
										
									}
								}
								else{
										OutputStream out = response.getOutputStream();
										out.write(img.getCache());
										//logAccess("serving image " + img.getString("FileName"));
										out.close();
									
								}
						
						}
						else if(page instanceof FileObject){
							/*
							FileObject fo = (FileObject)page;
							File file = new File(getRootPath() + "/websites/" + wsr.getName() + "/files/" + fo.getName());
							if(file.exists()){
							
								String filetype = fo.getString("FileExtension");
								
								response.setContentType("text/" + filetype);
								
								String content = openFile(file.getPath());
								
								writer = response.getWriter();
								writer.write(content);
								writer.flush();
							}
							*/
								FileObject fo = (FileObject)page;
								String filename = fo.getString("FileName");
								if(filename.isEmpty()){
									filename = 	fo.getString("Title");
								}
								File file = new File(getRootPath() + "/websites/" + wsr.getName() + "/files/" + filename);
								
								logAccess("serving file " + file.getPath());
								
								if(file.exists()){
								
									String filetype = fo.getString("FileExtension");
									
									if(filetype.equals("pdf")){
										response.setContentType("application/" + filetype);
										response.setHeader("content-disposition", "inline; filename=\"" + fo.getString("Title") + "\"");
										byte[] b = openFileToBytes(file.getPath());
										OutputStream out = response.getOutputStream();
										out.write(b, 0, b.length);
										out.flush();
									}
									else{
										response.setContentType("text/" + filetype);
										String content = openFile(file.getPath());
										writer = response.getWriter();
										writer.write(content);
										writer.flush();
									}
									
									
									

								}
						
						}
					}
					catch(java.lang.Exception e){
						logException(e);
					}
				
				}
			}
			

		}
		

		
			
		if(result != null){
			try{
				response.setContentType("text/xml; charset=UTF-8");
				writer = response.getWriter();	
				writer.write(result.toXML());
			}
			catch(java.lang.Exception e){
				logException(e);	
			}
			
		}

		
		
	}
	public String getUserProfile(OpenCommunityUserSession usersession){
		StringBuilder html = new StringBuilder();
		html.append("<h4>Benutzerprofil</h4>");
		html.append("<h5>Meine Inserate</h5>");
		return html.toString();
	}
	public String getLoginForm(OpenCommunityUserSession usersession){
	
		StringBuilder html = new StringBuilder();
	
		//html.append("<div class=\"menutop\">&nbsp;</div>");
		//html.append("<div class=\"menuentry\"><a href=\"javascript:loadPage(\'memberadsearchform\')\">Angebote suchen</a></div>");
		//html.append("<div class=\"menuentry\"><a href=\"javascript:createMemberAdd()\">Angebot platzieren</a></div>");
		//html.append("<div class=\"menubottom\">&nbsp;</div>");
		html.append("<div class=\"login\">");
		if(usersession.getOrganisationMember() != null){
			OrganisationMember om = usersession.getOrganisationMember();
			html.append("<span style=\"color : white\">Angemeldet als : " + om.getPerson().getIdentity().getString("FirstName") + " " + om.getPerson().getIdentity().getString("FamilyName") + "</span>");
									html.append("<p><input type=\"button\" onclick=\"logout()\" value=\"Abmelden\">");
									html.append("<p><a class=\"menuentry\" href=\"javascript:loadPage(\'profile\')\">Mein Profil</a>");
		}
		else{
									html.append("<span style=\"color : white\"><b>Login</b>");
									html.append("<br>Benutzername<br><input id=\"username\" value=\"hunziker\">");
									html.append("<br>Passwort<br><input type=\"password\" id=\"password\" value=\"1234\">");
									html.append("<br><input type=\"button\" onclick=\"login()\" value=\"Anmelden\"></span>");
		}
		html.append("</div>");
		return html.toString();
	}
	
	private Object uploadFile(HttpServletRequest request, HttpServletResponse response) {
	
		logAccess("Uploading file ...");
	
		WebApplicationContext context = new WebApplicationContext(request);
		String action = null;
		Object result = null;
		String objectPath = null;
		String title = null;
		String imageid = null;
		String sessionid = null;
		FileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);
		
		logAccess("action : " + action);
		
		try {
			List<FileItem> items = upload.parseRequest(request);
			FileItem fileItem = null;
			String parentObjectPath = null;
			
			String tempID = null;
			String omid = null;

			for (FileItem item : items) {
				if (item.isFormField()) {
					if (item.getFieldName().equals("tempID")) {
						tempID = item.getString();
					}
					else if (item.getFieldName().equals("action")) {
						action = item.getString();
					} 
					else if (item.getFieldName().equals("objectPath")) {
						objectPath = item.getString();
					} 
					else if (item.getFieldName().equals("title")) {
						title = item.getString();
					}
					else if (item.getFieldName().equals("imageid")) {
						imageid = item.getString();
					}
					else if (item.getFieldName().equals("omid")) {
						omid = item.getString();
					}
					else if (item.getFieldName().equals("sessionid")) {
						sessionid = item.getString();
					}
				}
				else {
					fileItem = item;
					logAccess(fileItem.getName());
				}
			}
			logAccess("TempID: " + tempID);
			logAccess(fileItem);
			logAccess(objectPath);
			logAccess(action);
			
			if(action != null && action.equals("uploadimage")){
				
				/*
				
				result = cms.uploadImage(objectPath, title, fileItem);
				try{
					response.sendRedirect(result);
				}
				catch(java.lang.Exception e){
					logException(e);
				}
				
				*/
			
			}
			else if(action != null && action.equals("imageupload")){
				
				
				if(objectPath != null && fileItem != null){
						WebSiteRoot wsr = (WebSiteRoot)getObjectByPath(objectPath);
						
						boolean success = false;
						
						if(wsr != null){
							logAccess(objectPath);
							success = cms.uploadImage(fileItem, title, wsr);
						}
						if(success){
							
							response.setStatus(HttpServletResponse.SC_OK);

							/*
							String html = "<html><script language=\"javascript\">";
							//html += "\nwindow.parent.openImageSelection('" + imageid + "');";
							html += "\nwindow.parent.reloadFormTab('" + wsr.getPath() + "', 'imagelist');";
							html += "\n</script></html>";
							
							PrintWriter writer = response.getWriter();
							response.setContentType("text/html; charset=UTF-8");
							writer.write(html);
							

							
							writer.close();
							writer.flush();
							*/
							
							
						}
						
						

					}

			
			}
			else if(action != null && action.equals("fileupload")){
				
				if(omid != null && omid.equals("null")==false){
					
					
					
					//String templateid, String omid, String title, ApplicationContext context, int activitycontext, String notetemplate, String notecontent, String batchid
					
					ExternalDocument externalDocument = new ExternalDocument();
					externalDocument.setProperty("FileData", fileItem.get());
					externalDocument.setProperty("FileName", fileItem.getName());
					
					logAccess("File length : " + fileItem.get().length);
					String fileid = insertSimpleObject(externalDocument);
					
					String activityid = createActivity("10", omid, "Fileupload", context, 0, "29", "", null);
					
					OrganisationMember om = (OrganisationMember)getObjectByName("OrganisationMember", omid);
					if(om != null){
						Activity activity = (Activity)getObject(om, "Activity", "ID", activityid);
						
						Parameter parameter = (Parameter)activity.getFieldByTemplate("28");
						
						parameter.setProperty("ExternalDocument", fileid);
						parameter.setProperty("Comment", fileItem.getName());
						updateObject(parameter);
					}
					else{
						executeCommand("UPDATE Parameter SET ExternalDocument=" + fileid + ", Comment='" + fileItem.getName() + "' WHERE ActivityID=" + activityid + " AND Template=28");	
					}
					
					
					String html = "<html><script language=\"javascript\">";
					html += "\nwindow.parent.getNextNode('overview=true');";
					html += "\n</script></html>";
							
					PrintWriter writer = response.getWriter();
					response.setContentType("text/html; charset=UTF-8");
					writer.write(html);
							

							
					writer.close();
					writer.flush();	
				
				}
				else if(objectPath != null && fileItem != null){
						WebSiteRoot wsr = (WebSiteRoot)getObjectByPath(objectPath);
						boolean success = false;
						
						if(wsr != null){
							logAccess(objectPath);
							success = cms.uploadFile(fileItem, title, wsr);
						}					
						if(success){

							
							String html = "<html><script language=\"javascript\">";
							html += "\nwindow.parent.loadSection('files');";
							html += "\n</script></html>";
							
							PrintWriter writer = response.getWriter();
							response.setContentType("text/html; charset=UTF-8");
							writer.write(html);
							

							
							writer.close();
							writer.flush();
							
							
						}
						
						
				}
			}
			else if(action != null && action.equals("uploadadvertisement")){

				BasicClass bc = getObjectByPath(objectPath);
				if(bc != null){
					try{
						String filename = fileItem.getName();
						String[] args = filename.split("\\.");
						String extension = args[args.length-1];
						bc.setProperty("FileName", bc.getName() + "." + extension);
						bc.setProperty("FileExtension", extension);
						File file = new File(filename);
						updateObject(bc);
						String path = getRootpath() + "/websites/1/advertising/" + bc.getName() + "." + extension;
						fileItem.write(new File(path));
						/*
						ActionResult actionResult = new ActionResult(ActionResult.Status.OK, "Bild hochgeladen");
						actionResult.setParam("refresh", "currentsection");
						PrintWriter writer = response.getWriter();
						response.setContentType("text/xml; charset=UTF-8");
						writer.write(actionResult.toXML());
						*/
						response.sendRedirect("administration.jsp?section=commercialads");
					}
					catch(java.lang.Exception e){
						logException(e);
					}
					
				}
				
				/*
				result = cms.uploadImage(objectPath, title, fileItem);
				try{
					response.sendRedirect(result);
				}
				catch(java.lang.Exception e){
					logException(e);
				}
				*/
			}
			else if (tempID != null && !tempID.isEmpty()) {
				// Save document that has been edited by client
				OpenCommunityUserSession userSession = wordClientSessions.get(tempID);
				logAccess("TemplID: " + tempID);
				if (userSession != null) {
					String documentPath = userSession.getWordClientDocument(tempID);
					logAccess("Documentpath: " + documentPath);					
					if (documentPath != null) {
						BasicClass bc = getObjectByPath(documentPath);
						logAccess("Document: " + bc);
						if (bc instanceof BasicDocument) {
							BasicDocument doc = (BasicDocument)bc;
							
							logAccess("Document: " + doc);
							
							if (doc instanceof TemplateElement) {
								TemplateElement te = (TemplateElement)doc;
								te.setIsExternal(true);
								te.saveObject((OpenCommunityUserSession)context.getObject("usersession"));
							}
							DocumentFormat documentFormat = DocumentFormat.MSWORD;
							boolean overwrite = true;
							logAccess(fileItem.getContentType());
							if (fileItem.getContentType().equals("application/pdf")) {
								//documentFormat = DocumentFormat.PDF;
								//overwrite = false;
							}
							String filename = doc.getFullFilename(true, documentFormat);
							//String filename = doc.getFullFilename(true);
							logAccess(filename);
							File file = new File(filename);
							if (!overwrite) {
								int counter = 0;
								String filenameWithoutExtension = filename;
								String extension = null;
								int dotPos = filename.lastIndexOf('.');
								if (dotPos != -1) {
									filenameWithoutExtension = filename.substring(0, dotPos);
									extension = filename.substring(dotPos + 1);
								}
								while (file.exists()) {
									counter++;
									String alternativeFilename = filenameWithoutExtension + "-" + counter;
									if (extension != null) {
										alternativeFilename += "." + extension;
									}
									file = new File(alternativeFilename);
								}
							}
							if (!file.getParentFile().exists()) {
								file.getParentFile().mkdirs();
							}
							fileItem.write(file);
							result = "OK";
						}
					}
				}
				else if (tempID.equals("blank")) {
					HttpSession session = request.getSession();
					OpenCommunityUserSession loginSession = (OpenCommunityUserSession)session.getAttribute("usersession");
					if (loginSession != null && loginSession.getLoginID() == 0) { // Hack: only allow admin to overwrite blank.docs // TODO: remove when fixed in Addin
						String docPath = getRootpath();
						docPath += File.separator + "templates";
						docPath += File.separator + "word";
						docPath += File.separator + "blank.docx";
							File file = new File(docPath);
							fileItem.write(file);
							result = "OK";
					}
					else {
						writeError("No permission to overwrite blank.docx");
						result = "FAIL";
					}
				}

				if (result == null) {
					writeError("No temp client document with ID " + tempID + " found");
					result = "FAIL";
				}
			}
			if (fileItem != null && parentObjectPath != null) {
				BasicClass parentObject = getObjectByPath(parentObjectPath);
				if (parentObject instanceof TemplateElement) {
					// Template element upload
					TemplateElement templateElement = (TemplateElement)parentObject;
						templateElement.saveTemplate(fileItem, context);
				}
			}
			if(action==null && objectPath != null){
				
				OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
				
				
				
				if(userSession==null){
					if(sessionid != null){
						
						if(sessions.get(sessionid) != null){
							userSession = (OpenCommunityUserSession)sessions.get(sessionid);
						}
						
					}
				}
				
				BasicClass o = getObjectByPath(objectPath, userSession);
				logAccess("UploadHandler : " + o);
				if(o != null && o instanceof UploadHandler){
					logAccess("UploadHandler : " + o);
					result = ((UploadHandler)o).handleUpload(context, fileItem);	
				}
				
			}
			
			
		}
		catch(java.lang.Exception e){
			logException(e);
		}
		return result;
		
	}
	
	//-------------------------------------------Process-Handling-------------------------------------------------
	
	public ProcessResult startProcess(String name, OpenCommunityUserSession usersession, Map<String, Object> params, ApplicationContext context, BasicClass owner) {
		return startProcess(name, "", false, usersession, params, context, owner);
	}
	
	public ProcessResult startProcess(String name, String returnTo, boolean ignoreCanceled, OpenCommunityUserSession userSession, Map<String, Object> params, ApplicationContext context, BasicClass owner) {
		ProcessResult result = null;
		BasicProcess process = createProcess(name, owner, params, context);
		userSession.setCurrentProcess(process);
		result = new ProcessResult(ProcessStatus.START);
		result.setParam("dataContainer", "wizardContent");
		BasicProcessNode node = process.getCurrentNode();
		result.setParam("title", node.getTitle());
		if(process instanceof OrganisationMemberEdit){
			result.setData(((OrganisationMemberEdit)process).getOrganisationMemberEditForm(context));
		}
		else if(node.getName().equals("MemberAdCreateNode")){
			if(process.getSubprocess() != null){
				result.setData(((MemberAdCreate)process.getSubprocess()).getMemberAdCreateForm(context));
			}
			else{
				result.setData(((MemberAdCreate)process).getMemberAdCreateForm(context));
			}
		}
		else if(node.getName().equals("MemberAdRequestActivityAdd")){
			result.setData(((MemberAdRequestActivityAdd)process).getMemberAdRequestAcivityAddForm(context));		
		}	
		else if(node.getName().equals("MemberAdDetail")){
			result.setData(((MemberAdDetail)process).getMemberAdDetailForm(context));		
		}
		else if(node.getName().equals("MemberAdRequestCreateNode")){
			result.setData(((MemberAdRequestCreate)process).getMemberAdRequestCreateForm(context));		
		}
		else if(node.getName().equals("LoginNode")){
			result.setData(((MemberRegistration)process).getLoginForm(context));		
		}
		else if(node.getName().equals("AGBNode")){
			result.setData(((MemberRegistration)process).getAGBForm(context));		
		}
		else if(node.getName().equals("FeedbackNode")){
			result.setData(((MemberRegistration)process).getFeedbackForm(context));		
		}
		else if(node.getName().equals("FeedbackNode2")){
			result.setData(((MemberAdCreate)process).getFeedbackForm(context));		
		}
		else if(node.getName().equals("RegistrationNode")){
			result.setData(((MemberRegistration)process).getRegistrationForm());		
		}
		else if(node.getName().equals("ProfileEditNode")){
			result.setData(((ProfileEdit)process).getProfileEditForm(context));		
		}
		else if(node.getName().equals("OrganisationMemberModificationNode")){
			result.setData(((OrganisationMemberModify)process).getOrganisationMemberModificationForm(context));		
		}
		else if(node.getName().equals("MemberAdEditNode")){
			result.setData(((MemberAdEdit)process).getMemberAdEditForm(context));		
		}
		else if(node.getName().equals("ch.opencommunity.process.BatchActivityCreate2")){
			result.setData(((BatchActivityCreate2)process).getBatchActivityCreateForm(context));		
		}
		else if(getFormManager().getObjectView(node.getClass().getName()) != null){
													
			BaseObjectView objectView = (BaseObjectView)getFormManager().getObjectView(node.getClass().getName());

			result.setData(objectView.toHTML(node, context, userSession, true));
													
		}									
		else{
			result.setData(form.getNodeForm(node, context));
		}
		return result;
	}
	
	public BasicProcess createProcess(String name){
		
		return createProcess(name, this, null, null);	

	}
	
	public BasicProcess createProcess(String name, BasicClass owner, ApplicationContext context){
		
		return createProcess(name, owner, null, context);	
	
	}	
	
	public BasicProcess createProcess(String name, BasicClass owner, Map<String, Object> params, ApplicationContext context){
		
		BasicProcess bp = null;
		
		try{
			Class<?> c = Class.forName(name);
			bp = (BasicProcess) c.newInstance();
			bp.setParent(owner);

			if (params != null) {
				Object[] names = params.keySet().toArray();
				for (int i = 0; i < names.length; i++) {
					if (!bp.hasProperty(names[i].toString())) {
						bp.addProperty(names[i].toString(), "String", "", true, names[i].toString());
					}
					Object value = params.get(names[i]);
					if (value instanceof String[]) {
						bp.setProperty(names[i].toString(),
								((String[]) value)[0]);
					} 
					else {
						bp.setProperty(names[i].toString(), value);
					}
				}
			}
			bp.initProcess(context);
		}
		catch(Exception e){
			logException(e);
		}
		
		return bp;
				
	}
	
	// ------------------------------------------DataStore--------------------------------------------------------
	public boolean executeCommand(String command){
		return dataStore.executeCommand(command);
	}
	public String insertSimpleObject(BasicClass record) {
		return dataStore.insertSimpleObject(record);
	}

	public String insertObject(BasicClass record) {
		return insertObject(record, false);
	}

	public String insertObject(BasicClass record, boolean recursive) {
		BasicClass newObject = insertAndGetObject(record, recursive);
		return newObject.getName();
	}

	public BasicClass insertAndGetObject(BasicClass record, boolean recursive) {
		return dataStore.insertObject(record, recursive);
	}
 	
	public boolean updateObject(BasicClass record) {
		// System.out.println(".. updating object " + record);
		return dataStore.updateObject(record);
	}

	public BasicClass getObject(BasicClass owner, String type, String keyname, String keyvalue) {

		return getObject(owner, type, keyname, keyvalue, true);

	}

	public BasicClass getObject(BasicClass owner, String type, String keyname, String keyvalue, boolean initialize) {

		return dataStore.getObject(owner, type, keyname, keyvalue, initialize);

	}
	
	public BasicClass getObject(Connection con, BasicClass owner, String type, String keyname, String keyvalue){
				
		return 	getObject(con, owner, type, keyname, keyvalue, true);
		
	}
	
	public BasicClass getObject(Connection con, BasicClass owner, String type, String keyname, String keyvalue, boolean initialize){
	
		return 	dataStore.getObject(con, owner, type, keyname, keyvalue, initialize);
		
	}

	public void loadSubobjects(BasicClass owner, String type, String keyname, String keyvalue) {

		//SQLDataStore sqlDataStore = (SQLDataStore) dataStore;
		//SQLDataStore sqlDataStore = (SQLDataStore2) dataStore;
		Connection con = dataStore.getConnection();
		try {

			//Statement readonlyStmt = sqlDataStore.getReadonlyStatement();
			Statement readonlyStmt = con.createStatement();

			ResultSet res = readonlyStmt.executeQuery("SELECT ID FROM " + type + " WHERE " + keyname + "=" + keyvalue);
			while (res.next()) {
				String sID = res.getString(1);
				getObject(con, owner, type, "ID", sID);
			}
			con.close();

		} catch (java.lang.Exception e) {
			logException(e);
		}

	}

	public ObjectCollection queryData(String searchString, ObjectCollection oc) {
		return dataStore.queryData(searchString, oc);
	}

	public void removeObject(String tablename, String id, boolean recursive) {
		dataStore.removeObject(tablename, id, recursive);
	}
	public BasicClass getObjectByPath(String objectPath){
		return getObjectByPath(objectPath, null);	
	}
	public BasicClass getObjectByPath2(String objectPath, BasicUserSession userSession){
		
		BasicClass bc = this;

		String[] args = objectPath.split("/");
		if(args[0].equals("UserSession") && userSession != null){
			bc = userSession;
		}
		for (int i = 1; i < args.length; i++) {
			if (args[i].equals("UserSession")) {
				bc = userSession;
			}
			if (args[i].equals("usersession")) {
				bc = userSession;
			}
			else if (args[i].equals("currentprocess")) { //AK 20170102
				if(userSession.getCurrentProcess() != null){
					bc = userSession.getCurrentProcess();	
				}
			}
			else if (args[i].equals("email")) {
				bc = mailForm;
			}

			else {
				String[] args2 = args[i].split(":");
				if (args2.length == 2 && args[1].length() > 0) {
					bc = (BasicClass) bc.getObjectByName(args2[0], args2[1]);
				}
			}
			if (bc == null) {
				return null;
			}
		}
		return bc;
	}
	//---------------------------------------------------------------------------------------------------
	
	public synchronized void sendAllPendingMails(){
		
		ObjectCollection recipients = new ObjectCollection("Recipients", "*");

 	 	 String sql = "SELECT DISTINCT t2.ID AS OrganisationMemberID, t4.FamilyName, t4.FirstName, t5.Value, t10.Value AS Emailalt, t1.Template, t1.ID, t1.Title, t1.Attachments, t6.Content,";
 	 	 sql += " t11.Username, t11.Password, ";
 	 	 sql += " CASE WHEN t4.SEX=1 THEN ('Sehr geehrter Herr ' || t4.FamilyName) WHEN t4.SEX=2 THEN ('Sehr geehrte Frau ' || t4.FamilyName) ELSE '' END AS Addressation";   
 	 	 sql += " FROM Activity AS t1";
 	 	 sql += " INNER JOIN OrganisationMember AS t2 ON t1.OrganisationMemberID=t2.ID";
 	 	 sql += " LEFT JOIN Person AS t3 ON t2.Person=t3.ID";
 	 	 sql += " LEFT JOIN Identity AS t4 ON t4.PersonID=t3.ID";
		 sql += " LEFT JOIN Contact AS t5 ON t5.PersonID=t3.ID AND t5.Type=3";
			 //sql += " LEFT JOIN Note AS t6 ON t6.ActivityID=t1.ID AND t6.Template IN (16,21)";
		 sql += " LEFT JOIN Note AS t6 ON t6.ActivityID=t1.ID AND t6.Template IN (16,21, 30, 31)"; //AK 20170201, PDF-Versand
		 sql += " LEFT JOIN OrganisationMemberRelationship AS t7 ON t2.ID=t7.OrganisationMember AND t7.Status=0";
			 sql += " LEFT JOIN OrganisationMember AS t8 ON t8.ID=t7.OrganisationMemberID";			 
			 sql += " LEFT JOIN Person AS t9 ON t8.Person=t9.ID";	
			 sql += " LEFT JOIN Contact AS t10 ON t10.PersonID=t9.ID AND t10.Type=3";
			 sql += " LEFT JOIN LOGIN AS t11 ON t11.OrganisationMemberID=t2.ID";
			 

			 sql += " WHERE t1.Status=0 AND t1.Template IN (1,4, 7, 8)";
			 sql += " ORDER BY t1.ID";
			 queryData(sql, recipients);
			 
			 logAccess(sql);
			 
			 for(BasicClass record : recipients.getObjects()){
			     String content = record.getString("CONTENT");
			     String addressation = record.getString("ADDRESSATION");
			     String username = record.getString("USERNAME");
			     String password = record.getString("PASSWORD");
			     
			     content = content.replace("<@addressation>", addressation);
			     content = content.replace("<@username>", username);	
			     content = content.replace("<@password>", password);
			     record.setProperty("CONTENT", content);
			 }
		
			 sendAllPendingMails(recipients);
			 
		sql = "SELECT t1.*, t6.Content, CASE WHEN t12.Value IS NOT NULL THEN t12.Value WHEN t10.Value IS NOT NULL THEN t10.Value ELSE t5.Value END AS Value";
		sql += " FROM Activity AS t1";
		sql += " JOIN Project AS t2 ON t1.ProjectID=t2.ID";
		sql += " JOIN Dossier AS t3 ON t2.DossierID=t3.ID";
		sql += " JOIN OrganisationalUnit AS t4 ON t3.OrganisationalUnit=t4.ID";
		sql += " LEFT JOIN Contact AS t5 ON t5.OrganisationalUnitID=t4.ID AND t5.Type=3";
		sql += " LEFT JOIN Note AS t6 ON t6.ActivityID=t1.ID AND t6.Template IN (16,21, 30, 31)"; //AK 20170201, PDF-Versand
		sql += " LEFT JOIN ActivityParticipant AS t7 ON t1.ID=t7.ActivityID";
		//sql += " LEFT JOIN ActivityOrganisationMember AS t7 ON t1.ID=t7.ActivityID";
		sql += " LEFT JOIN OrganisationMember AS t8 ON t8.ID=t7.OrganisationMember";			 
		sql += " LEFT JOIN Person AS t9 ON t8.Person=t9.ID";	
		sql += " LEFT JOIN Contact AS t10 ON t10.PersonID=t9.ID AND t10.Type=3";
		
		sql += " LEFT JOIN OrganisationalUnit AS t11 ON t11.ID=t7.OrganisationalUnit";			 
		sql += " LEFT JOIN Contact AS t12 ON t12.OrganisationalUnitID=t11.ID AND t12.Type=3";
		
		sql += " WHERE t1.Status=0 AND t1.Template IN (1,4, 7, 8)";
		sql += " ORDER BY t1.ID";
		
		logEvent(sql);
		
		recipients.removeObjects();
		queryData(sql, recipients);
		
		for(BasicClass record : recipients.getObjects()){
			String content = record.getString("CONTENT");
			record.setProperty("CONTENT", content);
		}
		sendAllPendingMails(recipients);		
		
	}
	
	public synchronized void sendAllPendingMails(ObjectCollection recipients){
		
		for(BasicClass bc : recipients.getObjects()){
			
			String id = bc.getString("ID");
			
			/*
			if(bc.getString("VALUE").length() > 0){
				sendEmail(bc.getString("CONTENT"), bc.getString("TITLE") , bc.getString("VALUE"));
			}
			else{
				sendEmail(bc.getString("CONTENT"), bc.getString("TITLE") + " (" + bc.getString("EMAILALT") + ")" , bc.getString("EMAILALT"));
			}
			*/
			
			String attachments = null;
			
			if(bc.getString("ATTACHMENTS") != null && bc.getString("ATTACHMENTS").length() > 0){
				attachments = bc.getString("ATTACHMENTS");
				attachments = getRootPath() + "/" + attachments;
			}
			
			if(bc.getString("EMAILALT").length() > 0){
				MimeMessage message = sendEmail(bc.getString("CONTENT"), bc.getString("TITLE") , bc.getString("EMAILALT"), attachments);
				if(message != null){
					insertMailMessageInstance(message, null, bc.getString("ID"));
				}
			}
			else{
				MimeMessage message =  sendEmail(bc.getString("CONTENT"), bc.getString("TITLE") , bc.getString("VALUE"), attachments);
				if(message != null){
					insertMailMessageInstance(message, null, bc.getString("ID"));
				}
			}
			executeCommand("UPDATE Activity SET Status=1 WHERE ID=" + id);
			
		}		
		
	}

	public MimeMessage sendEmail(String messagebody, String subject, String recipient, String attachment){
		
		javax.mail.internet.MimeMessage msg = null;
		
		try{
			
			//attachment = getRootPath() + "/newsletter/newsletter.html";
		
			Properties props = new Properties();
			props.put("mail.host", getString("smtphost"));

			Session session = Session.getInstance(props, null);

			msg = new MimeMessage(session);

			InternetAddress addressFrom = new InternetAddress("info@nachbarnet.net");
			msg.setFrom(addressFrom);
			
			InternetAddress[] addressTo = new InternetAddress[1]; 
			
			if(getString("defaultmailuser").trim().length() > 0){
				subject = subject + " (" + recipient + ")";
				addressTo[0] = new InternetAddress(getString("defaultmailuser"));	
			}
			else{
				addressTo[0] = new InternetAddress(recipient);	
			}
		    //addressTo[0] = new InternetAddress("kofler@oxinia.ch");
			msg.setRecipients(javax.mail.Message.RecipientType.TO, addressTo);
			
			InternetAddress[] bcc = new InternetAddress[1]; 
			bcc[0] = new InternetAddress("kofler@meson.ch");
			//bcc[0] = new InternetAddress("info@nachbarnet.net");
			msg.setRecipients(javax.mail.Message.RecipientType.BCC, bcc);
		      

			msg.setSubject(subject, "utf-8");

			//msg.setHeader("Content-Type", "text/plain; charset=\"utf-8\"");
			//msg.setContent(messagebody, "text/plain; charset=\"utf-8\"");
			
			if(attachment==null){
			
			      msg.setHeader("Content-Type", "text/plain; charset=\"utf-8\"");
			      msg.setContent(messagebody, "text/plain; charset=\"utf-8\"");
			
			}
			else{
			      msg.setHeader("Content-Type", "text/html; charset=\"iso-8859-1\"");
			      msg.setHeader("Content-Type", "Content-Type: multipart/alternative; boundary=\"=_7c3eeeca2318900ae18bf0b3c0c23c41\"");
			      //msg.setHeader("Content-Type", "text/plain; charset=\"UTF-8\"");
			      Multipart mp = new MimeMultipart("alternative");

			      MimeBodyPart bodyPart = new MimeBodyPart();

			      bodyPart.setContent(messagebody, "text/plain; charset=\"UTF-8\"");
			      mp.addBodyPart(bodyPart);

			      MimeBodyPart attachPart = new MimeBodyPart();
			      
			      
			      DataSource source = new FileDataSource(attachment);
			      attachPart.setDataHandler(new DataHandler(source));
			      attachPart.setHeader("Content-Type", "text/html; charset=\"iso-8859-1\"");
			      //attachPart.setFileName(new File(attachment).getName());
			      
			      attachPart.setHeader("Content-Disposition", "inline; filename=123456.html");
			      attachPart.setHeader("Content-Transfer-Encoding", "base64");
			      attachPart.setHeader("Content-Transfer-Encoding", "quoted-printable");
			      

			      attachPart.setHeader("Content-ID", "<2348237487b74>");
			      
			      mp.addBodyPart(attachPart);

			      msg.setContent(mp);

			}

			Transport.send(msg);
		
			//return true;
		}
		catch(java.lang.Exception e){
			logException(e);
			//return false;
		}
		return msg;
	}
	
	public String insertMailMessageInstance(javax.mail.internet.MimeMessage message, TransactionHandler transactionHandler, String activityid){
		return insertMailMessageInstance(message, transactionHandler, activityid, null);
	}
	public String insertMailMessageInstance(javax.mail.internet.MimeMessage message, TransactionHandler transactionHandler, String activityid, String projectid){
		
		
	
		
		String id = null;
	
		if(message != null){
					    		
			try{
									
				String now = DateConverter.dateToSQL(new java.util.Date(), true);
										
				MailMessageInstance messageInstance = new MailMessageInstance();
				if(activityid != null){
					messageInstance.addProperty("ActivityID", "String", activityid);
				}
				else if(projectid != null){
					messageInstance.addProperty("ProjectID", "String", projectid);
				}
				messageInstance.setProperty("DateCreated", now);
				messageInstance.setProperty("DateSent", now);
										
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				message.writeTo(baos);
				byte[] bytes = baos.toByteArray(); 
										
				messageInstance.setProperty("Message", bytes);
				messageInstance.setProperty("Subject", message.getSubject());
										
				String recipientString = "";
				javax.mail.Address[] recipientsTO = message.getRecipients(javax.mail.internet.MimeMessage.RecipientType.TO);
										
										
				for(javax.mail.Address recipientTO : recipientsTO){
					recipientString += recipientTO.toString() + ",";	
				}
										
				if(recipientString.length() > 0){
					recipientString = recipientString.substring(0, recipientString.length() - 1);	
				}
										
				messageInstance.setProperty("Recipients", recipientString);
				if(message.getSender() != null){
					messageInstance.setProperty("Sender", message.getSender().toString());
				}
										
				//id = server.insertSimpleObject(transactionHandler, messageInstance);
				if(transactionHandler != null){
					id = insertSimpleObject(transactionHandler, messageInstance);	
				}
				else{
					id = insertSimpleObject(messageInstance);	
				}
			}
			catch(java.lang.Exception e){
				//server.logException(e);	
				logException(e);	
			}
					    		
		}
		
		return id;
	}
	
	//-----------------------------------------------------------------------------------------------------
	
	public String getRootPath(){
		return rootpath;
	}
	public String getToolbar(String sectionid){
		
		StringBuilder html = new StringBuilder();
		
		return html.toString();
		
	}
	public String getMainMenu(WebApplicationContext context, BasicUserSession userSession){
		StringBuilder html = new StringBuilder();
		html.append(mainMenu.getMainMenu(context, (OpenCommunityUserSession)userSession));
		
		/*
		int size = getObjects("ApplicationModule").size();
		List modules = getObjects("ApplicationModule");
		for(int i = 0; i < size; i++){
			Object o = modules.get(i);
			if(o instanceof WebApplicationModule){
							
				WebApplicationModule module = (WebApplicationModule)o;
				html.append(module.getMainMenu(context, userSession));
								
			}
		}	
		*/
		return html.toString();
		
	}
	public String getOrganisationMemberList(ApplicationContext context, String filter){
		return getOrganisationMemberList(context, filter, "user", false);
	}
	public String getOrganisationMemberList(ApplicationContext context, String filter, String sectionid, boolean embedded){
		
		OpenCommunityUserSession usersession = (OpenCommunityUserSession)context.getObject("usersession");
		
		String[] yes_no = {"Nein", "Ja"};
		
		int offset = 0; 
		String soffset = context.getString("offset");
		if(soffset != null){
			try{
				offset = Integer.parseInt(soffset);
			}
			catch(java.lang.Exception e){
				logException(e);	
			}
		}
		
		int limit = 40;
	
		String[] status = {"registriert", "aktiv", "inaktiv", "zu l�schen", "zu kontrollieren"};
	
		StringBuilder html = new StringBuilder();
		html.append("<table>");
		html.append("<tr>");
		html.append("<th class=\"tableheader\">Status</th>");
		html.append("<th class=\"tableheader\">Dat. Registrierung</th>");
		html.append("<th class=\"tableheader\">Zul. ge�ndert</th>");
		html.append("<th class=\"tableheader\">Organisation</th>");
		html.append("<th class=\"tableheader\">Zust. Person</th>");
		html.append("<th class=\"tableheader\">Nachname</th>");
		html.append("<th class=\"tableheader\">Vorname</th>");
		html.append("<th class=\"tableheader\">Strasse</th>");
		html.append("<th class=\"tableheader\">Nummer</th>");
		html.append("<th class=\"tableheader\">PLZ</th>");
		html.append("<th class=\"tableheader\">Ort</th>");
		if(!embedded){
			html.append("<th class=\"tableheader\">Tel. priv.</th>");
			html.append("<th class=\"tableheader\">Tel. gesch.</th>");
			html.append("<th class=\"tableheader\">Tel. mob.</th>");
			html.append("<th class=\"tableheader\">Email</th>");
			html.append("<th class=\"tableheader\">Benutzer</th>");
			html.append("<th class=\"tableheader\">Mitglied</th>");
			html.append("<th class=\"tableheader\">G&ouml;nner</th>");
			html.append("<th class=\"tableheader\">Login</th>");
		}
		//html.append("<th>Datenschutz</th>");
		html.append("<th class=\"tableheader\">Adress�nderung pendent</th>");
		html.append("<th class=\"tableheader\">Kommentar</th>");
		html.append("</tr>");
		ObjectCollection results = new ObjectCollection("Results", "*");
		
		String sql = "SELECT t1.ID AS OMID, t1.Status AS OMStatus, t1.DataProtection, t1.Comment, t1.DateCreated AS Date1, t1.DateModified AS Date2, t3.FamilyName, t3.FirstName, t4.*, t5.ID AS Login,";
 		sql += " t6.Value AS TelP, t7.Value AS TelB,  t8.Value AS TelM, t9.Value AS Email, t10.ID AS Role1, t11.ID AS Role2, t12.ID AS Role3, t13.ID AS ModificationID, t14.Title AS Organisation, t16.ID AS ParentID, CONCAT(t18.Familyname, ' ' , t18.Firstname) AS ParentName";
		sql += " FROM OrganisationMember AS t1";
		sql += " LEFT JOIN Person AS t2 ON t1.Person=t2.ID";
		sql += " LEFT JOIN Identity AS t3 ON t3.PersonID=t2.ID";
		sql += " LEFT JOIN Address AS t4 ON t4.PersonID=t2.ID";
		sql += " LEFT JOIN Login AS t5 ON t5.OrganisationMemberID=t1.ID";
		sql += " LEFT JOIN Contact AS t6 ON t6.PersonID=t2.ID AND t6.Type=0";
		sql += " LEFT JOIN Contact AS t7 ON t7.PersonID=t2.ID AND t7.Type=1";
		sql += " LEFT JOIN Contact AS t8 ON t8.PersonID=t2.ID AND t8.Type=2";
		sql += " LEFT JOIN Contact AS t9 ON t9.PersonID=t2.ID AND t9.Type=3";
		
		sql += " LEFT JOIN MemberRole AS t10 ON t10.OrganisationMemberID=t1.ID AND t10.Role=1 AND t10.Status=0";
		sql += " LEFT JOIN MemberRole AS t11 ON t11.OrganisationMemberID=t1.ID AND t11.Role=2 AND t11.Status=0";
		sql += " LEFT JOIN MemberRole AS t12 ON t12.OrganisationMemberID=t1.ID AND t12.Role=3 AND t12.Status=0";
		
		sql += " LEFT JOIN OrganisationMemberModification AS t13 ON t13.OrganisationMemberID=t1.ID AND t13.Status=0";
		sql += " LEFT JOIN OrganisationalUnit AS t14 ON t1.OrganisationalUnitID=t14.ID";
		
		sql += " LEFT JOIN OrganisationMemberRelationship AS t15 ON t15.OrganisationMember=t1.ID AND t15.Status=0";
		sql += " LEFT JOIN OrganisationMember AS t16 ON t15.OrganisationMemberID=t16.ID";
		sql += " LEFT JOIN Person AS t17 ON t16.Person=t17.ID";
		sql += " LEFT JOIN Identity AS t18 ON t18.PersonID=t17.ID";
		if(filter != null){
			sql += filter;	
		}
		if(embedded){
			sql += " ORDER BY t1.DateModified DESC, t1.ID DESC";
		}
		else{
			sql += " ORDER BY t1.Status, t1.ID, t3.FamilyName, t3.FirstName";
		}
		sql += " LIMIT " + limit + " OFFSET " + offset;
		
		
		queryData(sql, results);
		int odd = -1;
		
		String omid = (String)usersession.get("OMID");
		for(BasicClass bc : results.getObjects()){
			
			if(omid != null && omid.equals(bc.getString("OMID"))){
				html.append("<tr class=\"highlight\">");		
			}
			else if(odd==1){
				html.append("<tr class=\"odd\">");			
			}
			else{
				html.append("<tr class=\"even\">");
			}
			html.append("<td class=\"datacell\">" + status[bc.getInt("OMSTATUS")] + "</td>");
			html.append("<td class=\"datacell\">" + DateConverter.sqlToShortDisplay(bc.getString("DATE1"),false) + "</td>");
			html.append("<td class=\"datacell\">" + DateConverter.sqlToShortDisplay(bc.getString("DATE2"),true) + "</td>");
			html.append("<td class=\"datacell\">" + bc.getString("ORGANISATION") + "</td>");
			if(!bc.getString("PARENTID").isEmpty()){
				html.append("<td class=\"datacell\"><a href=\"javascript:editOrganisationMember(" + bc.getString("PARENTID") + ")\">" + bc.getString("PARENTNAME") + "</a></td>");
				//html.append("<td class=\"datacell\"><a href=\"javascript:createProcess(\'ch.opencommunity.process.OrganisationMemberEdit\', \'OMID=" + bc.getString("PARENTID") + "&sectionid=" + sectionid + "\')\">" + bc.getString("PARENTNAME") + "</a></td>");
			}
			else{
				html.append("<td class=\"datacell\"></td>");
			}
			html.append("<td class=\"datacell\">" + bc.getString("FAMILYNAME") + "</td>");
			html.append("<td class=\"datacell\">" + bc.getString("FIRSTNAME") + "</td>");
			html.append("<td class=\"datacell\">" + bc.getString("STREET") + "</td>");
			html.append("<td class=\"datacell\">" + bc.getString("NUMBER") + "</td>");
			html.append("<td class=\"datacell\">" + bc.getString("ZIPCODE") + "</td>");
			html.append("<td class=\"datacell\">" + bc.getString("CITY") + "</td>");
			if(!embedded){
				html.append("<td class=\"datacell\">" + bc.getString("TELP") + "</td>");
				html.append("<td class=\"datacell\">" + bc.getString("TELB") + "</td>");	
				html.append("<td class=\"datacell\">" + bc.getString("TELM") + "</td>");
				html.append("<td class=\"datacell\">" + bc.getString("EMAIL") + "</td>");
				
				if(bc.getString("ROLE1").length() > 0){
					html.append("<td class=\"datacell\">JA</td>");			
				}
				else{
					html.append("<td class=\"datacell\"></td>");				
				}
				
				if(bc.getString("ROLE2").length() > 0){
					html.append("<td class=\"datacell\">JA</td>");			
				}
				else{
					html.append("<td class=\"datacell\"></td>");				
				}
				
				if(bc.getString("ROLE3").length() > 0){
					html.append("<td class=\"datacell\">JA</td>");			
				}
				else{
					html.append("<td class=\"datacell\"></td>");				
				}
				if(bc.getString("LOGIN").length() > 0){
					html.append("<td>JA</td>");			
				}
				else{
					html.append("<td class=\"datacell\"></td>");				
				}
			}
				//html.append("<td class=\"datacell\">" + yes_no[bc.getInt("DATAPROTECTION")] + "</td>");
			if(bc.getString("MODIFICATIONID").length() > 0){
				html.append("<td class=\"datacell\"><a href=\"javascript:createProcess(\'ch.opencommunity.process.OrganisationMemberModify\', \'OMID=" + bc.getString("OMID") + "\')\">Ja</a></td>");		
			}
			else{
				html.append("<td class=\"datacell\"></td>");				
			}
			html.append("<td class=\"datacell\">" + bc.getString("COMMENT") + "</td>");
			
				
			html.append("<td><a href=\"javascript:editOrganisationMember(" + bc.getString("OMID") + ")\">Bearbeiten</a></td>");
			/*
			html.append("<td><a href=\"javascript:createProcess(\'ch.opencommunity.process.OrganisationMemberEdit\', \'OMID=" + bc.getString("OMID") + "&sectionid=" + sectionid);
			if(!bc.getString("PARENTID").isEmpty()){
				html.append("&PARENT=" + bc.getString("PARENTID"));
			}
			html.append("\')\">Bearbeiten</a></td>");
			*/
			html.append("</tr>");
			odd = -odd;
		}
		html.append("</table>");
		if(offset > 39){
			html.append("<input type=\"button\" onclick=\"loadSection(\'" + sectionid + "\',\'offset=" + (offset - limit) + "\')\" value=\"   <<  \">");
		}
		html.append("<input type=\"button\" onclick=\"loadSection(\'" + sectionid + "\',\'offset=" + (offset + limit) + "\')\" value=\"   >>  \">");
		return html.toString();
	}
	public String getMemberAdCategoryList(){
		StringBuilder html = new StringBuilder();
		try{
			html.append("<table><tr><td valign=\"top\">");
			html.append("<table>");
			html.append("<tr><th>Bezeichnung</th></tr>");
			
			int odd = -1;
			for(BasicClass bc : maa.getObjects("MemberAdCategory")){
				if(odd==1){
					html.append("<tr class=\"odd\">");			
				}
				else{
					html.append("<tr class=\"even\">");
				}
				odd = -odd;
				html.append("<td><a href=\"javascript:getMemberAdCategoryEditForm(" + bc.getName() + ")\">" + bc.getProperty("Title").getValue() + "</a></td>");
				html.append("</tr>");
			}
			
			html.append("</table></td><td><div id=\"editform\"></div></td></tr></table>");
		}
		catch(java.lang.Exception e){
			logException(e);
		}
		return html.toString();
	}

	public String getMemberAdRequestList(ApplicationContext context, OpenCommunityUserSession usersession, String filter){
		return getMemberAdRequestList(context, usersession, filter, "requests");
	}
	public String getMemberAdRequestList(ApplicationContext context, OpenCommunityUserSession usersession, String filter, String sectionid){
		return getMemberAdRequestList(context, usersession, filter, sectionid, false);
	}
	public String getMemberAdRequestList(ApplicationContext context, OpenCommunityUserSession usersession, String filter, String sectionid, boolean embedded){
		
		
		int offset = 0; 
		String soffset = context.getString("offset");
		if(soffset != null){
			try{
				offset = Integer.parseInt(soffset);
			}
			catch(java.lang.Exception e){
				logException(e);	
			}
		}
		
		int limit = 40;
		
		String sortfield = (String)usersession.get("sortfield");
		if(sortfield==null || filter != null){
			sortfield = " 1, 6, 8";	
		}
		else{
			sortfield = " 1";				
		}
		if(context.hasProperty("sortfield")){
			sortfield = context.getString("sortfield");	
		}
		usersession.put("sortfield", sortfield);
		
		StringBuilder html = new StringBuilder();
		String[] status = {"erfasst", "freigeschaltet", "kontaktiert", "inaktiv", "pendent"};
		String[] colors = {"", "orange", "green", "red", "blue"};
		
		ObjectCollection results = new ObjectCollection("Results", "*");
		String sql = "SELECT t6.ID, t6.NotificationStatus, t6.Comment, t1.ID AS MemberAdID, t1.Title, t6.Status, t4.FamilyName, t4.FirstName, t5.Title AS Category, (t12.FamilyName || ' ' || t12.FirstName) as OwnerName,  t13.DateCreated AS EmailDate,";
		sql += " COUNT(t15.ID) AS CNT"; 
		sql += " FROM MemberAd AS t1";
		sql += " INNER JOIN MemberAdRequest AS t6 ON t6.MemberAd=t1.ID";
		sql += " INNER JOIN OrganisationMember AS t2 ON t6.OrganisationMemberID=t2.ID";
		
		sql += " INNER JOIN OrganisationMember AS t10 ON t1.OrganisationMemberID=t10.ID"; //Eigent�mer des Inserates
		sql += " LEFT JOIN Person AS t11 ON t10.Person=t11.ID";
		sql += " LEFT JOIN Identity AS t12 ON t12.PersonID=t11.ID";
		
		sql += " LEFT JOIN Person AS t3 ON t2.Person=t3.ID";
		sql += " LEFT JOIN Identity AS t4 ON t4.PersonID=t3.ID";
		sql += " LEFT JOIN MemberAdCategory AS t5 ON t1.Template=t5.ID";
		
		sql += " LEFT JOIN ActivityObject AS t13 ON t6.ID=t13.MemberAdRequestID";
		sql += " LEFT JOIN Activity AS t14 ON t13.ActivityID=t14.ID AND t14.Template=4";
		sql += " LEFT JOIN Feedback AS t15 ON t15.MemberAdRequestID=t6.ID";
		if(filter != null){
			sql += filter;	
		}
		sql += " GROUP BY t6.ID, t6.NotificationStatus, t6.Comment, t1.ID, t1.Title, t6.Status, t4.FamilyName, t4.FirstName, t5.Title, (t12.FamilyName || ' ' || t12.FirstName),  t13.DateCreated"; 
		
		sql += " ORDER BY " + sortfield;
		
		sql += " LIMIT " + limit + " OFFSET " + offset;
		
		logAccess(sql);
		
		queryData(sql, results);
		
		html.append("<table>");
		html.append("<tr><th>ID</th><th>Status</th>");
		html.append("<th>Nachname</th><th>Vorname</th><th><a href=\"javascript:loadSection(\'requests\',\'sortfield=8')\">Inserateigent�mer</a></th>");
		html.append("<th>Titel</th><th>Rubrik</th><th>Benachrichtigt</th><th>Email versendet</th><th>Feedbacks</th><th>Kommentar</th></tr>");
		
		try{
		
			int odd = -1;
			for(BasicClass record : results.getObjects()){
				if(odd==1){
					html.append("<tr class=\"odd\">");			
				}
				else{
					html.append("<tr class=\"even\">");
				}
				odd = -odd;
				html.append("<td>"+ record.getString("ID") + "</td>");
				//html.append("<td>" + status[record.getInt("STATUS")] + "</td>");
				html.append("<td style=\"background : " + colors[record.getInt("STATUS")] + ";\"> </td>");
				html.append("<td class=\"datacell\">"+ record.getString("FAMILYNAME") + "</td>");
				html.append("<td class=\"datacell\">"+ record.getString("FIRSTNAME") + "</td>");
				html.append("<td class=\"datacell\">" + record.getString("OWNERNAME") + "</td>");
				html.append("<td class=\"datacell\">"+ record.getString("TITLE") + "</td>");
				html.append("<td class=\"datacell\">"+ record.getString("CATEGORY") + "</td>");
				html.append("<td class=\"datacell\">"+ record.getString("NOTIFICATIONSTATUS") + "</td>");
				html.append("<td class=\"datacell\">"+ record.getString("EMAILDATE") + "</td>");
				html.append("<td class=\"datacell\">"+ record.getString("CNT") + "</td>");
				html.append("<td class=\"datacell\">"+ record.getString("COMMENT") + "</td>");
				if(record.getInt("STATUS")==0 && !embedded){
					html.append("<td><a href=\"javascript:createProcess(\'ch.opencommunity.process.MemberAdRequestActivate\','memberadid=" + record.getString("MEMBERADID") + "&memberadrequestid=" + record.getString("ID") + "&sectionid=" + sectionid + "\')\">Freischalten</a></td>");
				}
				html.append("</tr>");
			}
		}
		catch(java.lang.Exception e){
			html.append(e.toString());	
		}
		html.append("</table>");
		
		if(!embedded){
			if(offset > 39){
				html.append("<input type=\"button\" onclick=\"loadSection(\'requests\',\'offset=" + (offset - limit) + "\')\" value=\"   <<  \">");
			}
			html.append("<input type=\"button\" onclick=\"loadSection(\'requests\',\'offset=" + (offset + limit) + "\')\" value=\"   >>  \">");
		}
		return html.toString();	

	}
	
	public String getMemberAdRequestList2(ApplicationContext context, OpenCommunityUserSession usersession, String filter, String sectionid, boolean embedded){
		
		StringBuilder html = new StringBuilder();		
		String sql = "Select min(t1.id), count(t1.id) as cnt, max(t1.Status) as Status, t1.DateCreated, t2.ID, t4.FamilyName, t4.FirstName FROM MemberAdRequest AS t1";
		sql += " INNER JOIN OrganisationMember AS t2 ON t1.OrganisationMemberID=t2.ID";
		sql += " INNER JOIN Person AS t3 ON t3.ID=t2.Person";
		sql += " INNER JOIN Identity AS t4 ON t4.PersonID=t3.ID";
		
		sql += " WHERE t1.Status IN (0,5)";
		sql += " GROUP BY t1.DateCreated, t2.ID, t4.FamilyName, t4.FirstName";
		sql += " ORDER BY min(t1.id)";
		
		ObjectCollection results = new ObjectCollection("Results", "*");
		queryData(sql, results);
		
		html.append("<table>");
		
		try{
		
			int odd = -1;
			for(BasicClass record : results.getObjects()){
				if(odd==1){
					html.append("<tr class=\"odd\">");			
				}
				else{
					html.append("<tr class=\"even\">");
				}
				odd = -odd;
				
				int status = record.getInt("STATUS");
				if(status==5){
					html.append("<td class=\"datacell\" style=\"background : orange;\">"+ record.getString("CNT") + "</td>");
				}
				else{
					html.append("<td class=\"datacell\">"+ record.getString("CNT") + "</td>");
				}
				html.append("<td class=\"datacell\">"+ DateConverter.sqlToShortDisplay(record.getString("DATECREATED"), false) + "</td>");
				html.append("<td class=\"datacell\"><a href=\"javascript:createProcess(\'ch.opencommunity.process.OrganisationMemberEdit\', \'OMID=" + record.getString("ID") + "&sectionid=" + sectionid + "\')\">"+ record.getString("FAMILYNAME") + "</a></td>");
				html.append("<td class=\"datacell\"><a href=\"javascript:createProcess(\'ch.opencommunity.process.OrganisationMemberEdit\', \'OMID=" + record.getString("ID") + "&sectionid=" + sectionid + "\')\">"+ record.getString("FIRSTNAME") + "</a></td>");
				html.append("<td class=\"datacell\"><a href=\"javascript:createProcess(\'ch.opencommunity.process.MemberAdRequestsActivate\','omid=" + record.getString("ID") + "&sectionid=" + sectionid + "\')\">Freischalten</a></td>");
				html.append("</tr>");
				
			}
		}
		catch(java.lang.Exception e){
			logException(e);	
		}
		
		
		html.append("</table>");
		
		return html.toString();
		
	}
	
	public String getFeedbackList(ApplicationContext context, OpenCommunityUserSession usersession, String filter){
		return 	getFeedbackList(context, usersession, filter, "feedback");
	}
	public String getFeedbackList(ApplicationContext context, OpenCommunityUserSession usersession, String filter, String sectionid){
		int offset = 0; 
		String soffset = context.getString("offset");
		if(soffset != null){
			try{
				offset = Integer.parseInt(soffset);
			}
			catch(java.lang.Exception e){
				logException(e);	
			}
		}
		
		int limit = 40;
		
		String sortfield = (String)usersession.get("sortfield");
		if(sortfield==null){
			sortfield = " t1.Status, t4.FamilyName";	
		}
		if(context.hasProperty("sortfield")){
			sortfield = context.getString("sortfield");	
		}
		usersession.put("sortfield", sortfield);
		
		String[] status = {"erfasst", "freigeschaltet", "pausiert", "inaktiv"};
		String[] status2 = {"", "offen", "verarbeitet"};
	
		StringBuilder html = new StringBuilder();	
		
		html.append("<table>");
		html.append("<tr><td class=\"tableheader\">ID</td>");
		html.append("<td class=\"tableheader\"><a href=\"javascript:loadSection(\'memberads\',\'sortfield=t1.Status\')\">Status</a></td>");
		html.append("<td class=\"tableheader\">Nachname</td>");
		html.append("<td class=\"tableheader\">Vorname</td>");
		html.append("<td class=\"tableheader\">Zeit erstellt</td>");
		html.append("<td class=\"tableheader\">Betrifft</td>");	
		html.append("</tr>");
		
		
		
		
		ObjectCollection results = new ObjectCollection("Results", "*");
		
		
		String sql = "SELECT t1.* ,  t4.FamilyName, t4.FirstName, t5.ID AS OM2, t7.FamilyName AS FamilyName2, t7.FirstName AS FirstName2";
		sql += " FROM FeedbackRecord AS t1";
		sql += " LEFT JOIN OrganisationMember AS t2 ON t1.OrganisationMemberID=t2.ID";
		sql += " LEFT JOIN Person AS t3 ON t2.Person=t3.ID";
		sql += " LEFT JOIN Identity AS t4 ON t4.PersonID=t3.ID";
		sql += " LEFT JOIN OrganisationMember AS t5 ON t1.OrganisationMember=t5.ID";
		sql += " LEFT JOIN Person AS t6 ON t5.Person=t6.ID";
		sql += " LEFT JOIN Identity AS t7 ON t7.PersonID=t6.ID";
		sql += " WHERE t1.Status=0";
		sql += " ORDER BY ID DESC";
		
		queryData(sql, results);
		
		try{
			boolean even = false;
			for(BasicClass record : results.getObjects()){
				if(even){
					html.append("<tr class=\"odd\">");			
				}
				else{
					html.append("<tr class=\"even\">");
				}
				even = !even;
				
				html.append("<td class=\"datacell\">"+ record.getString("ID") + "</td>");
				html.append("<td class=\"datacell\">" + status[record.getInt("STATUS")] + "</td>");

				html.append("<td class=\"datacell\">"+ record.getString("FAMILYNAME") + "</td>");
				html.append("<td class=\"datacell\">"+ record.getString("FIRSTNAME") + "</td>");
				html.append("<td class=\"datacell\">"+ DateConverter.sqlToShortDisplay(record.getString("DATECREATED")) + "</td>");

				html.append("<td class=\"datacell\">"+ record.getString("FAMILYNAME2") + " " + record.getString("FIRSTNAME2") + "</td>");


				if(record.getInt("STATUS")==0){
					
					html.append("<td><a href=\"javascript:createProcess('ch.opencommunity.feedback.FeedbackFinalize','feedbackid=" + record.getString("ID") + "&section=feedback')\">Feedback verarbeiten</a></td>");
				}

				html.append("</tr>");
			}
		}
		catch(java.lang.Exception e){
			html.append(e.toString());	
		}
		
		results.removeObjects();
		
		sql = "SELECT t1.* ,  t4.FamilyName, t4.FirstName, t5.ID AS OM2, t7.FamilyName AS FamilyName2, t7.FirstName AS FirstName2";
		sql += " FROM Feedback AS t1";
		sql += " LEFT JOIN OrganisationMember AS t2 ON t1.OrganisationMemberID=t2.ID";
		sql += " LEFT JOIN Person AS t3 ON t2.Person=t3.ID";
		sql += " LEFT JOIN Identity AS t4 ON t4.PersonID=t3.ID";
		sql += " LEFT JOIN OrganisationMember AS t5 ON t1.OrganisationMember=t5.ID";
		sql += " LEFT JOIN Person AS t6 ON t5.Person=t6.ID";
		sql += " LEFT JOIN Identity AS t7 ON t7.PersonID=t6.ID";
		sql += " WHERE t1.Status=0";
		sql += " ORDER BY ID DESC";
		
		/*
		String sql = "SELECT t1.* ,  t4.FamilyName, t4.FirstName FROM FreeTextFeedback AS t1";
		sql += " LEFT JOIN OrganisationMember AS t2 ON t1.OrganisationMemberID=t2.ID";
		sql += " LEFT JOIN Person AS t3 ON t2.Person=t3.ID";
		sql += " LEFT JOIN Identity AS t4 ON t4.PersonID=t3.ID";
		*/
		
		/*
		sql += " LEFT JOIN MemberAdCategory AS t5 ON t1.Template=t5.ID";
		sql += " LEFT JOIN MemberAdRequest AS t6 ON t1.ID=t6.MemberAd";
		sql += " LEFT JOIN ActivityObject AS t7 ON t1.ID=t7.MemberAdID";
		sql += " LEFT JOIN Activity AS t8 ON t7.ActivityID=t8.ID AND t8.Template=4";
		*/
		
		

		
		if(filter != null){
			sql += filter;	
		}
		
		/*
		sql += " GROUP BY t1.ID, t1.Title, t2.Status, t4.FamilyName, t4.FirstName, t5.Title, t1.Status, t1.ValidFrom, t1.ValidUntil, t1.NotificationStatus, t1.FeedbackStatus, t8.DateCreated";

		sql += " ORDER BY " + sortfield;
		*/
		sql += " LIMIT " + limit + " OFFSET " + offset;
		
		logAccess(sql);
		
		queryData(sql, results);
		

		
		try{
			boolean even = false;
			for(BasicClass record : results.getObjects()){
				if(even){
					html.append("<tr class=\"odd\">");			
				}
				else{
					html.append("<tr class=\"even\">");
				}
				even = !even;
				
				html.append("<td class=\"datacell\">"+ record.getString("ID") + "</td>");
				html.append("<td class=\"datacell\">" + status[record.getInt("STATUS")] + "</td>");

				html.append("<td class=\"datacell\">"+ record.getString("FAMILYNAME") + "</td>");
				html.append("<td class=\"datacell\">"+ record.getString("FIRSTNAME") + "</td>");
				html.append("<td class=\"datacell\">"+ DateConverter.sqlToShortDisplay(record.getString("DATECREATED")) + "</td>");

				html.append("<td class=\"datacell\">"+ record.getString("FAMILYNAME2") + " " + record.getString("FIRSTNAME2") + "</td>");


				if(record.getInt("STATUS")==0){
					
					html.append("<td><a href=\"javascript:createProcess('ch.opencommunity.process.FeedbackFinalize','feedbackid=" + record.getString("ID") + "&section=feedback')\">Feedback verarbeiten</a></td>");
				}

				html.append("</tr>");
			}
		}
		catch(java.lang.Exception e){
			html.append(e.toString());	
		}
		html.append("</table>");
		
		if(offset > 39){
			html.append("<input type=\"button\" onclick=\"loadSection(\'memberads\',\'offset=" + (offset - limit) + "\')\" value=\"   <<  \">");
		}
		html.append("<input type=\"button\" onclick=\"loadSection(\'memberads\',\'offset=" + (offset + limit) + "\')\" value=\"   >>  \">");
		
		return html.toString();			
	
	}
	public String getFeedbackList2(ApplicationContext context, OpenCommunityUserSession usersession, String filter){
		return 	getFeedbackList2(context, usersession, filter, "feedback");
	}
	public String getFeedbackList2(ApplicationContext context, OpenCommunityUserSession usersession, String filter, String sectionid){
		int offset = 0; 
		String soffset = context.getString("offset");
		if(soffset != null){
			try{
				offset = Integer.parseInt(soffset);
			}
			catch(java.lang.Exception e){
				logException(e);	
			}
		}
		
		int limit = 40;
		
		String sortfield = (String)usersession.get("sortfield");
		if(sortfield==null){
			sortfield = " t1.Status, t4.FamilyName";	
		}
		if(context.hasProperty("sortfield")){
			sortfield = context.getString("sortfield");	
		}
		usersession.put("sortfield", sortfield);
		
		String[] status = {"erfasst", "freigeschaltet", "pausiert", "inaktiv"};
		String[] status2 = {"", "offen", "verarbeitet"};
	
		StringBuilder html = new StringBuilder();	
		ObjectCollection results = new ObjectCollection("Results", "*");
		String sql = "SELECT t1.ID, t1.Title, t1.Status, t1.NotificationStatus,  t4.FamilyName, t4.FirstName, t5.Title AS TemplateTitle , t1.ValidFrom, t1.ValidUntil, COUNT(t6.ID) AS CNT, t1.FeedbackStatus, t8.DateCreated FROM MemberAd AS t1";
		sql += " LEFT JOIN OrganisationMember AS t2 ON t1.OrganisationMemberID=t2.ID";
		sql += " LEFT JOIN Person AS t3 ON t2.Person=t3.ID";
		sql += " LEFT JOIN Identity AS t4 ON t4.PersonID=t3.ID";
		sql += " LEFT JOIN MemberAdCategory AS t5 ON t1.Template=t5.ID";
		sql += " LEFT JOIN MemberAdRequest AS t6 ON t1.ID=t6.MemberAd";
		sql += " LEFT JOIN ActivityObject AS t7 ON t1.ID=t7.MemberAdID";
		sql += " LEFT JOIN Activity AS t8 ON t7.ActivityID=t8.ID AND t8.Template=4";
		
		sql += " WHERE t1.FeedbackStatus IN (1,2)";
		
		if(filter != null){
			sql += filter;	
		}
		sql += " GROUP BY t1.ID, t1.Title, t2.Status, t4.FamilyName, t4.FirstName, t5.Title, t1.Status, t1.ValidFrom, t1.ValidUntil, t1.NotificationStatus, t1.FeedbackStatus, t8.DateCreated";

		sql += " ORDER BY " + sortfield;
		
		sql += " LIMIT " + limit + " OFFSET " + offset;
		
		queryData(sql, results);
		
		html.append("<table>");
		html.append("<tr><th>ID</th>");
		html.append("<th><a href=\"javascript:loadSection(\'memberads\',\'sortfield=t1.Status\')\">Status</a></th>");
		html.append("<th><a href=\"javascript:loadSection(\'memberads\',\'sortfield=t1.Title\')\">Titel</a></th>");
		html.append("<th><a href=\"javascript:loadSection(\'memberads\',\'sortfield=t5.Title\')\">Rubrik</a></th>");
		html.append("<th>Nachname</th><th>Vorname</th>");
		html.append("<th><a href=\"javascript:loadSection(\'memberads\',\'sortfield=t1.ValidFrom\')\">G�ltig von</a></th>");
		html.append("<th><a href=\"javascript:loadSection(\'memberads\',\'sortfield=t1.ValidUntil\')\">G�ltig bis</a></th>");
		html.append("<th><a href=\"javascript:loadSection(\'memberads\',\'sortfield=t1.NotificationStatus\')\">Benachrichtigt</a></th>");
		html.append("<th>Email versendet</th>");
		html.append("<th><a href=\"javascript:loadSection(\'memberads\',\'sortfield=10\')\">Adressbestellungen</a></th>");
		html.append("<th><a href=\"javascript:loadSection(\'memberads\',\'sortfield=11\')\">Feedbacks</a></th>");
		html.append("</tr>");
		
		try{
			int odd = -1;
			for(BasicClass record : results.getObjects()){
				if(odd==1){
					html.append("<tr class=\"odd\">");			
				}
				else{
					html.append("<tr class=\"even\">");
				}
				odd = -odd;
				html.append("<td>"+ record.getString("ID") + "</td>");
				html.append("<td>" + status[record.getInt("STATUS")] + "</td>");
				html.append("<td>"+ record.getString("TITLE") + "</td>");
				html.append("<td>"+ record.getString("TEMPLATETITLE") + "</td>");
				html.append("<td>"+ record.getString("FAMILYNAME") + "</td>");
				html.append("<td>"+ record.getString("FIRSTNAME") + "</td>");
				html.append("<td>"+ DateConverter.sqlToShortDisplay(record.getString("VALIDFROM")) + "</td>");
				html.append("<td>"+ DateConverter.sqlToShortDisplay(record.getString("VALIDUNTIL")) + "</td>");
				html.append("<td>"+ record.getString("NOTIFICATIONSTATUS") + "</td>");
				html.append("<td>"+ record.getString("DATECREATED") + "</td>");
				html.append("<td>"+ record.getString("CNT") + "</td>");
				html.append("<td>"+ status2[record.getInt("FEEDBACKSTATUS")] + "</td>");
				if(record.getInt("FEEDBACKSTATUS")==1){
					html.append("<td><a href=\"javascript:finalizeFeedback(" + record.getString("ID") + ",\'" + sectionid + "\')\">Feedback verarbeiten</a></td>");
				}
				else{
					//html.append("<td><a href=\"javascript:createProcess(\'ch.opencommunity.process.MemberAdDetail\',\'MAID=" + record.getString("ID") + "\')\">Details</a></td>");
				}
				html.append("</tr>");
			}
		}
		catch(java.lang.Exception e){
			html.append(e.toString());	
		}
		html.append("</table>");
		
		if(offset > 39){
			html.append("<input type=\"button\" onclick=\"loadSection(\'memberads\',\'offset=" + (offset - limit) + "\')\" value=\"   <<  \">");
		}
		html.append("<input type=\"button\" onclick=\"loadSection(\'memberads\',\'offset=" + (offset + limit) + "\')\" value=\"   >>  \">");
		
		return html.toString();			
	}
	public String getDocumentList(){
		
		String[] docstatus = {"offen", "verschickt"};
		
		StringBuilder html = new StringBuilder();
		html.append("<table>");
		html.append("<tr><th>ID</th><th>Aktivit�t</th><th>Vorlage</th><th>Betreff</th><th>Datum</th><th>Name Empf�nger</th><th>Vorname Empf�nger</th><th>Status</th></tr>");
		ObjectCollection results = new ObjectCollection("Results", "*");
		String sql = "SELECT t1.ID, t1.Title AS Subject, t1.DateCreated, t1.Status, t4.FamilyName, t4.FirstName, t5.Title, t8.Title AS Activity FROM Document AS t1 LEFT JOIN OrganisationMember AS t2 ON t1.Recipient=t2.ID LEFT JOIN Person AS t3 ON t2.Person=t3.ID";
		sql += " LEFT JOIN Identity AS t4 ON t4.PersonID=t3.ID";
		sql += " LEFT JOIN DocumentTemplate AS t5 ON t1.Template=t5.ID";
		sql += " LEFT JOIN Parameter AS t6 ON t6.Document=t1.ID";
		sql += " LEFT JOIN Activity AS t7 ON t6.ActivityID=t7.ID";
		sql += " LEFT JOIN ObjectTemplate AS t8 ON t7.Template=t8.ID";
		queryData(sql, results);
		int odd = -1;
		for(BasicClass bc : results.getObjects()){
			if(odd==1){
				html.append("<tr class=\"odd\">");			
			}
			else{
				html.append("<tr class=\"even\">");
			}
			odd=-odd;
			html.append("<td>" + bc.getString("ID") + "</td>");
			html.append("<td>" + bc.getString("ACTIVITY") + "</td>");
			html.append("<td>" + bc.getString("TITLE") + "</td>");
			html.append("<td>" + bc.getString("SUBJECT") + "</td>");
			html.append("<td>" + bc.getString("DATECREATED") + "</td>");
			html.append("<td>" + bc.getString("FAMILYNAME") + "</td>");
			html.append("<td>" + bc.getString("FIRSTNAME") + "</td>");
			html.append("<td>" + docstatus[bc.getID("STATUS")] + "</td>");
			html.append("<td><a href=\"javascript:setDocumentStatus(" + bc.getString("ID") + ")\">Auf \"Verschickt\" setzen</a></td>");
			html.append("<td><a href=\"javascript:openDocument(" + bc.getString("ID") + ")\">Oeffnen</a></td>");
			html.append("</tr>");
		}
		html.append("<tr>");
		html.append("</table>");              
		return html.toString();
	}
	public ActionResult openDocument(String docid, String objectPath, OpenCommunityUserSession userSession){
		
		Document doc = null;
		if(docid != null){
			if(userSession.getOrganisationMember() != null){
				OrganisationMember om = userSession.getOrganisationMember();
				doc = (Document)om.getObjectByName("Document", docid);
			}
		}
		if(doc==null){
			if(objectPath != null){
				doc = (Document)getObjectByPath(objectPath);	
			}
			
		}
		
		if(doc==null){
			doc = (Document)getObject( this, "Document", "ID", docid);
		}
		
		logAccess("Document : " + doc.getName());
		
		ActionResult result = null;
		
		FileObjectData fileObjectData = (FileObjectData)doc.getObjectByIndex("FileObjectData", 0);
		
		sessions.put(userSession.getSessionID(), userSession);
		
			
		if(fileObjectData==null){
			String template = "" + doc.getID("Template");
			
			logAccess("Template : " + template);
			
			LibreOfficeTemplate libreOfficeTemplate = (LibreOfficeTemplate)libreOfficeModule.getObjectByName("LibreOfficeTemplate", template);
			
			logAccess("Template : " + libreOfficeTemplate);
		
			if(libreOfficeTemplate != null){
				fileObjectData = (FileObjectData)libreOfficeTemplate.getObjectByIndex("FileObjectData", 0);
				
				if(fileObjectData != null){
					
					logAccess("Template : " + fileObjectData);
					
					String filename = "oc_" + createPassword(15) + "_" + userSession.getSessionID() + ".odt";
					String path = getRootpath() + "/temp/" + filename;
					saveFile(path, (byte[])fileObjectData.getObject("FileData"));
					
					userSession.put(filename, doc);
					
					result = new ActionResult(ActionResult.Status.OK, "Word-Dokument ge�ffnet");					
					result.setParam("exec", "window.open('/temp/" + filename + "', '_blank')");
				}
				
			}
		}
		else{
			
			logAccess("FileData: " + ((byte[])fileObjectData.getObject("FileData")).length);
			
			String filename = "oc_" + createPassword(15) + "_" + userSession.getSessionID() + ".odt";
			String path = getRootpath() + "/temp/" + filename;
			saveFile(path, (byte[])fileObjectData.getObject("FileData"));
					
			userSession.put(filename, doc);
					
			result = new ActionResult(ActionResult.Status.OK, "Word-Dokument ge�ffnet");					
			result.setParam("exec", "window.open('/temp/" + filename + "', '_blank')");
					
		}
		
		/*

		try {
			File file = new File(getRootPath() + "/docs/"  + doc.getName() + doc.getDocPostfix());
			if (!file.exists()) {
					// Only create once
				File blankFile = new File(getRootpath() + "/templates/word/blank.docx");
				WordDocument.copyFile(blankFile, file);
			}
			result = new ActionResult(ActionResult.Status.OK, "Word-Dokument ge�ffnet");
				// send temporary file to client
			String tempID;
			//if (command.equals("createwordmailinglist")) {
			//	tempID = getTempName("mailinglist");
			//}
			//else {
				tempID = doc.getTempName();
			//}
			File tempFile = new File(getTempPath(true) + tempID + doc.getDocPostfix());
			registerWordClientDocument(doc.getTempName(), userSession, doc.getPath());
			
			logAccess("Document : " + doc.getPath());
			
			WordDocument.copyFile(file, tempFile);
			    
			//String webLink = getTempPath(false) + tempID + doc.getDocPostfix();
			String webLink = "../" + getTempPath(false) + tempID + doc.getDocPostfix();
			result.setParam("exec", "window.open('" + webLink + "', '_blank')");
		}
		catch (IOException e) {
			logException(e);
			result = new ActionResult(ActionResult.Status.FAILED, "�ffnen des Dokuments fehlgeschlagen");
		}	
		
		*/
		return result;
	}
	public void openExternalDocument(String docid, ApplicationContext context){
		
		try{
			ExternalDocument doc = (ExternalDocument)getObject(null, "ExternalDocument", "ID", docid);
			if(doc != null){
				if(doc.getObject("FileData") instanceof byte[]){
					WebApplicationContext webcontext = (WebApplicationContext)context;
					HttpServletResponse response = webcontext.getResponse();
					
					String filename = doc.getString("FileName");
					String contenttype = "image/jpeg";
					if(filename.length()==0){
						filename = "download";
					}
					else{
						String[] args = filename.split("\\.");
						if(args.length > 0){
							String extension = args[args.length-1].toUpperCase(); //Todo : in statische Klasse auslagern
							if(extension.equals("DOC")){
								contenttype = "application/msword";
							}
							else if(extension.equals("DOCX")){
								contenttype = "application/vnd.openxmlformats-officedocument. wordprocessingml.document";
							}
							else if(extension.equals("PDF")){
								contenttype = "application/pdf";
							}
							else if(extension.equals("JPG")){
								contenttype = "image/jpeg";
							}
							else if(extension.equals("PNG")){
								contenttype = "image/png";
							}
						}
					
					}
					response.setContentType(contenttype);
					response.setHeader("Content-disposition", "attachment; filename=" + filename);
					OutputStream out = response.getOutputStream();
					
					byte[] filedata = (byte[])doc.getObject("FileData");
					
					logAccess(filedata.length);
					
					//out.write(filedata, 0, filedata.length);
					out.write(filedata);
					out.flush();
					out.close();
				}
			}
		}
		catch(java.lang.Exception e){
			logException(e);
		}
	}
	public String getActivityList(ApplicationContext context){
		
		int offset = 0; 
		String soffset = context.getString("offset");
		if(soffset != null){
			try{
				offset = Integer.parseInt(soffset);
			}
			catch(java.lang.Exception e){
				logException(e);	
			}
		}
		
		int limit = 40;
		
		String[] activitystatus = {"offen", "verschickt"};		
		StringBuilder html = new StringBuilder();

		
		html.append("<table>");
		html.append("<tr>");
		html.append("<th>ID</th>");
		html.append("<th>Vorlage</th>");
		html.append("<th>Bezeichnung</th>");
		html.append("<th>Name</th>");
		html.append("<th>Vorname</th>");
		html.append("<th>Email</th>");
		html.append("</tr>");
		
		String sql = "SELECT t1.ID, t1.Title, t1.DateCreated, t1.Status, t2.Title AS Template, t5.Familyname, t5.Firstname, t6.Value FROM Activity AS t1";
		sql += " LEFT JOIN ObjectTemplate AS t2 ON t1.Template=t2.ID";
		sql += " LEFT JOIN OrganisationMember AS t3 ON t1.OrganisationMemberID=t3.ID";
		sql += " LEFT JOIN Person AS t4 ON t3.Person=t4.ID";
		sql += " LEFT JOIN Identity AS t5 ON t5.PersonID=t4.ID";
		sql += " LEFT JOIN Contact AS t6 ON t6.PersonID=T4.ID AND t6.Type=3";
		
		sql += " WHERE t1.Template=4";
		sql += " ORDER BY t1.Status";
		
		sql += " LIMIT " + limit + " OFFSET " + offset;
		
		ObjectCollection results = new ObjectCollection("Results", "*");		
		queryData(sql, results);
		int even = 1;
		for(BasicClass bc : results.getObjects()){
			if(even==1){
				html.append("<tr class=\"odd\">");	
			}
			else{
				html.append("<tr class=\"even\">");
			}
			even = -even;
			html.append("<td>" + bc.getString("ID") + "</td>");
			html.append("<td>" + bc.getString("TEMPLATE") + "</td>");
			html.append("<td>" + bc.getString("TITLE") + "</td>");
			html.append("<td>" + bc.getString("FAMILYNAME") + "</td>");
			html.append("<td>" + bc.getString("FIRSTNAME") + "</td>");
			html.append("<td>" + bc.getString("VALUE") + "</td>");
			html.append("<td>" + activitystatus[bc.getInt("STATUS")] + "</td>");
			html.append("</tr>");
		}
		html.append("</table>");
		if(offset > 39){
			html.append("<input type=\"button\" onclick=\"loadSection(\'email\',\'offset=" + (offset - limit) + "\')\" value=\"   <<  \">");
		}
		html.append("<input type=\"button\" onclick=\"loadSection(\'email\',\'offset=" + (offset + limit) + "\')\" value=\"   >>  \">");
		
		return html.toString();
	}
	public DocumentTemplate getDocumentTemplate(int ID) {
		return (DocumentTemplate)getObjectByID("DocumentTemplate", ID);
	}
	public MemberAdAdministration getMemberAdAdministration(){
		return maa;
	}
	public ObjectTemplateAdministration getObjectTemplateAdministration(){
		return ota;
	}
	public NewsAdministration getNewsAdministration(){                   
		return news;	
	}
	public void registerWordClientDocument(String tempID, OpenCommunityUserSession userSession, String objectPath) {
		wordClientSessions.put(tempID, userSession);
		userSession.registerWordClientDocument(tempID, objectPath);
	}
	public String getTempPath(boolean filesystem) {
		String separator = filesystem ? File.separator : "/";
		String tempPath = "";
		if (filesystem) {
			tempPath += getRootpath();
			tempPath += separator;
		}
		//tempPath += "temp" + separator;
		tempPath += "temp" + separator;
		return tempPath;
	}
	public DocumentFormat getDocumentFormat() {
		if (getString("documentFormat").equals("pdf")) {
			return DocumentFormat.PDF;
		}
		else if (getString("documentFormat").equals("msword")) {
			return DocumentFormat.MSWORD;
		}
		// default
		return DocumentFormat.PDF;
	}
	public DocumentTemplateLibrary getTemplib(int ownerID){
		DocumentTemplateLibrary templateLibrary = null;

		Vector<BasicClass> templibs = getObjects("DocumentTemplateLibrary");
		for(BasicClass templib : templibs){
			try{
				if (ownerID == templib.getID("Owner")) {
					templateLibrary = (DocumentTemplateLibrary)templib;
				}
			}
			catch(java.lang.Exception e){
				writeError(e);
			}
		}
		return templateLibrary;	
	}
	public Vector<BasicClass> getTextblocks(int ownerID){

		TextBlockAdministration textBlockAdministration = null;

		Vector<BasicClass> tbas = getObjects("TextBlockAdministration");
		for(BasicClass tba : tbas){

			if(tba.getID("Owner") == ownerID){
				textBlockAdministration = (TextBlockAdministration)tba;
			}
		}
		return textBlockAdministration.getObjects("TextBlock");	
				
	}
	public Vector<BasicClass> getTextblocks(int ownerID, int typeFilter){

		TextBlockAdministration textBlockAdministration = null;
		Vector textBlocks = new Vector();
		textBlocks.add(new ConfigValue("0", "0", ""));
		Vector<BasicClass> tbas = getObjects("TextBlockAdministration");
		for(BasicClass tba : tbas){

			if(tba.getID("Owner") == ownerID){
				textBlockAdministration = (TextBlockAdministration)tba;
			}
		}
		if(textBlockAdministration != null){
			for(BasicClass bc : textBlockAdministration.getObjects("TextBlock")){
				if(bc.getID("Type")==typeFilter){
					textBlocks.add(bc);
				}
			}
		}
		return textBlocks;	
				
	}

	public TextBlock getTextblock(String ID){
		TextBlockAdministration ta = (TextBlockAdministration)getObjectByIndex("TextBlockAdministration", 0);
		if(ta != null){
			return (TextBlock)ta.getObjectByName("TextBlock", ID);	
		}
		else{
			return null; 				
		}	
			
	}
	public String getTextblockContent(String ID){
		return getTextblockContent(ID, false);	
	}
	public String getTextblockContent(String ID, boolean clean){
		TextBlockAdministration ta = (TextBlockAdministration)getObjectByIndex("TextBlockAdministration", 0);
		if(ta != null){
			TextBlock tb = (TextBlock)ta.getObjectByName("TextBlock", ID);	
			if(tb != null){
				String content = tb.getString("Content");
				if(clean){
					content = StringEscapeUtils.unescapeHtml4(content);
					content = content.replace("<p>","\n");	
					content = content.replace("</p>","");	
				}
				return content;
			}
			else{
				return "";
			}
			
		}
		else{
			return ""; 				
		}	
			
	}
	public String getTextblockContentByFunction(String function, boolean clean){
		TextBlockAdministration ta = (TextBlockAdministration)getObjectByIndex("TextBlockAdministration", 0);		
		if(ta != null){
			TextBlock tb = (TextBlock)ta.getTextBlockByFunction(function);	
			if(tb != null){
				String content = tb.getString("Content");
				if(clean){
					content = StringEscapeUtils.unescapeHtml4(content);
					content = content.replace("<p>","\n");	
					content = content.replace("</p>","");	
				}
				return content;
			}
			else{
				return "";
			}
			
		}
		else{
			return ""; 				
		}
		
	}
	public HTMLForm getFormFactory(){
		return form;
	}
	public Connection getConnection(){
		//return ((SQLDataStore)dataStore).getConnection();	
		return dataStore.getConnection();	
	}
	public void initData(){
		try{
			//Statement readonlyStmt = ((SQLDataStore)dataStore).getConnection().createStatement();
			Connection con = dataStore.getConnection();
			Statement readonlyStmt = con.createStatement();
			ResultSet res = readonlyStmt.executeQuery("SELECT ID FROM TextBlockAdministration ORDER BY ID");
			while(res.next()){
				String sID = res.getString(1);
				TextBlockAdministration tba = (TextBlockAdministration)getObject(con, this, "TextBlockAdministration", "ID", sID);	
				//Statement stmt2 = sqlDataStore.getReadonlyStatement();
				//Statement stmt2 = ((SQLDataStore)dataStore).getConnection().createStatement();
				Statement stmt2 = con.createStatement();
				ResultSet res2 = stmt2.executeQuery("SELECT ID FROM TextBlock WHERE Owner=" + tba.getInt("Owner") + " ORDER BY Title");
				while(res2.next()){
					sID = res2.getString(1);
					/*DocumentTemplate dt = (DocumentTemplate)*/getObject(con, tba, "TextBlock", "ID", sID, false);	
				} 
				res2.close();

			}


			res = readonlyStmt.executeQuery("SELECT ID FROM DocumentTemplateLibrary ORDER BY ID");
			while(res.next()){
				String sID = res.getString(1);
				DocumentTemplateLibrary templateLibrary = (DocumentTemplateLibrary)getObject(con, this, "DocumentTemplateLibrary", "ID", sID, false);

				//Statement stmt2 = sqlDataStore.getReadonlyStatement();
				
				
				//Statement stmt2 = ((SQLDataStore)dataStore).getConnection().createStatement();
				Statement stmt2 = con.createStatement();
				ResultSet res2 = stmt2.executeQuery("SELECT ID FROM DocumentTemplate WHERE Owner=" + templateLibrary.getString("Owner") + " ORDER BY Title");
				while(res2.next()){
					sID = res2.getString(1);
					getObject(con, templateLibrary, "DocumentTemplate", "ID", sID, false);	
				} 
				res2 = stmt2.executeQuery("SELECT ID FROM DocumentHeaderFooter WHERE Owner=" + templateLibrary.getInt("Owner") + " ORDER BY Type, Title");
				while(res2.next()){
					sID = res2.getString(1);
					getObject(con, templateLibrary, "DocumentHeaderFooter", "ID", sID, false);	
				} 
				res2.close();
				

			}
			res = readonlyStmt.executeQuery("SELECT ID FROM Role ORDER BY ID");
			while(res.next()){
				String sID = res.getString(1);
				getObject(con, this, "Role", "ID", sID);
			}
			
			res = readonlyStmt.executeQuery("SELECT ID FROM Account ORDER BY AccountNumber");
			while(res.next()){
				String sID = res.getString(1);
				getObject(con, accounting, "Account", "ID", sID);
			}
			res = readonlyStmt.executeQuery("SELECT ID FROM MemberAdAdministration ORDER BY ID");
			while(res.next()){
				String sID = res.getString(1);
				getObject(con, this, "MemberAdAdministration", "ID", sID);
			}
			
			res = readonlyStmt.executeQuery("SELECT ID FROM OrganisationalUnit ORDER BY Title");
			while(res.next()){
				String sID = res.getString(1);
				getObject(con, this, "OrganisationalUnit", "ID", sID);
			}

			res = readonlyStmt.executeQuery("SELECT ID FROM ObjectTemplateAdministration ORDER BY ID");
			while(res.next()){
				String sID = res.getString(1);
				getObject(con, this, "ObjectTemplateAdministration", "ID", sID, false);
			}
			res = readonlyStmt.executeQuery("SELECT ID FROM PDFTemplateLibrary ORDER BY ID");
			while(res.next()){
				String sID = res.getString(1);
				getObject(con, this, "PDFTemplateLibrary", "ID", sID);
			}
			
			Vector<BasicClass> templibs = getObjects("DocumentTemplateLibrary");
			for(BasicClass templib : templibs){
				templib.initObject();
			}
			Vector<BasicClass> tbas = getObjects("TextBlockAdministration");
			for(BasicClass tba : tbas){
				tba.initObject();
			}
			
			
			res = readonlyStmt.executeQuery("SELECT ID FROM WebSiteRoot ORDER BY Title");
			while (res.next()) {
				String sID = res.getString(1);
				getObject(con, cms, "WebSiteRoot", "ID", sID);
			}
			
			res = readonlyStmt.executeQuery("SELECT ID FROM Advertisement ORDER BY ID Desc");
			while (res.next()) {
				String sID = res.getString(1);
				getObject(con, advertising, "Advertisement", "ID", sID);
			}
			
			res = readonlyStmt.executeQuery("SELECT ID FROM QueryDefinition ORDER BY ID Desc");
			while (res.next()) {
				String sID = res.getString(1);
				getObject(con, this, "QueryDefinition", "ID", sID);
			}
			
			res = readonlyStmt.executeQuery("SELECT ID FROM GroupDefinition ORDER BY ID Desc");
			while (res.next()) {
				String sID = res.getString(1);
				getObject(con, this, "GroupDefinition", "ID", sID);
			}
			
			res = readonlyStmt.executeQuery("SELECT ID FROM LibreOfficeTemplate ORDER BY ID Desc");
			while (res.next()) {
				String sID = res.getString(1);
				getObject(con, libreOfficeModule, "LibreOfficeTemplate", "ID", sID);
			}
			
			con.close();
			
			ota = (ObjectTemplateAdministration)getObjectByName("ObjectTemplateAdministration", "1");
			ota.initObject();
		}
		catch(java.lang.Exception e){
			logException(e);
		}
	}
	public Vector getRecipientList(){
		Vector recipients = new Vector();
		ObjectCollection results = new ObjectCollection("Results", "*");
		String sql = "SELECT t1.ID AS OMID, t3.* , t4.Street, t4.Number, t4.ZipCode, t4.City , t5.ID AS MRID2, t6.ID AS MRID3 FROM OrganisationMember AS t1 LEFT JOIN Person AS t2 ON t1.Person=t2.ID LEFT JOIN Identity AS t3 ON t3.PersonID=t2.ID ";
		sql += " LEFT JOIN Address AS t4 ON t4.PersonID=t2.ID";
		sql += " LEFT JOIN MemberRole AS t5 ON t5.OrganisationMemberID=t1.ID AND t5.Status=0 AND t5.Role=2";
		sql += " LEFT JOIN MemberRole AS t6 ON t6.OrganisationMemberID=t1.ID AND t6.Status=0 AND t6.Role=3";
		sql += " WHERE t1.Status!=3 ORDER BY t3.FamilyName";
		queryData(sql, results);
		for(BasicClass bc : results.getObjects()){
			String label = bc.getString("FAMILYNAME") + " " + bc.getString("FIRSTNAME") + ", " + bc.getString("DATEOFBIRTH") + ", " + bc.getString("STREET") + " " + bc.getString("NUMBER") + ", " + bc.getString("ZIPCODE") + " " +  bc.getString("CITY");
			if(bc.getID("MRID2") > 0){
				label += ", Mitglied";
			}
			if(bc.getID("MRID3") > 0){
				label += ", G�nner";
			}
			recipients.add(new ConfigValue(bc.getString("OMID"), bc.getString("OMID"), label));
		}
		return recipients;
	}
	public Vector getRecipientList(String filter){
		Vector recipients = new Vector();
		ObjectCollection results = new ObjectCollection("Results", "*");
		String sql = "SELECT t1.ID AS OMID, t3.* , t4.ZipCode, t4.City FROM OrganisationMember AS t1 LEFT JOIN Person AS t2 ON t1.Person=t2.ID";
		sql += " LEFT JOIN Identity AS t3 ON t3.PersonID=t2.ID LEFT JOIN Address AS t4 ON t4.PersonID=t2.ID";
		sql += " JOIN MemberRole AS t5 ON t5.OrganisationMemberID=t1.ID AND t5.Status=0 AND t5.Role=" + filter;
		sql += " WHERE t1.Status!=3 ORDER BY t3.FamilyName";
		queryData(sql, results);
		for(BasicClass bc : results.getObjects()){
			String label = bc.getString("FAMILYNAME") + " " + bc.getString("FIRSTNAME") + ", " + bc.getString("DATEOFBIRTH") + ", " +  bc.getString("ZIPCODE") + " " +  bc.getString("CITY");
			recipients.add(new ConfigValue(bc.getString("OMID"), bc.getString("OMID"), label));
		}
		return recipients;
	}
	public String replaceParameters(String html, BasicCMSObject caller, HttpServletRequest request){
		StringBuffer url = request.getRequestURL();
		String uri = request.getRequestURI();
		String ctx = request.getContextPath();
		String baseurl = url.substring(0, url.length() - uri.length() + ctx.length()) + "/";
		//String imagebaseurl = baseurl + "websites/" + caller.getWebSiteRoot().getName() + "/images/";
		
		String imagebaseurl = caller.getWebSiteRoot().getBaseURL(request) + "/" + getString("instancename") + "/websites/" + caller.getWebSiteRoot().getName() + "/images";

		html = html.replace("<#baseurl>", baseurl);

		html = html.replace("<#imagebaseurl>", imagebaseurl);

		return html;
	}
	public String getBaseURL(HttpServletRequest request){
		String baseURL = "HTTP";
		baseURL += "://" + request.getServerName();
		baseURL += ":" + request.getServerPort();
		return baseURL;
	}
	//-----------------------------------------------------------------------------------------
	
	public List<ConfigValue> getSupportedLanguages(){
		/*
		Vector<ConfigValue> languages = new Vector<ConfigValue>();  
		languages.add(new ConfigValue("albanian","albanian","Albanisch"));
		languages.add(new ConfigValue("german","german","Deutsch"));
		languages.add(new ConfigValue("english","english","Englisch"));
		languages.add(new ConfigValue("french","french","Franz�sisch"));
		languages.add(new ConfigValue("italian","italian","Italienisch"));
		languages.add(new ConfigValue("portugese","portugese","Portugesisch"));
		languages.add(new ConfigValue("spanish","spanish","Spanisch"));
		languages.add(new ConfigValue("turkish","turkish","T�rkisch"));
		*/
		return languages;
	}
	public List<ConfigValue> getSupportedLanguages2(){
		return languages2;
	}
	public String getAGB(String language){
		if(texts.get(language) != null){
			return texts.get(language);
		}
		else{
			StringBuilder html = new StringBuilder();
			for(int i = 0; i< 10; i++){
				html.append("<p>" + language);
				for(int j = 0; j< 40; j++){
					html.append(" dideldu dudeld�");
				}	
				html.append("</p>");
			}
			return html.toString();
		}
	}
	public static String getNow(boolean withTime) {
		java.util.Date date = new java.util.Date();
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		
		String day = "" + c.get(Calendar.DAY_OF_MONTH);
		if(day.length()==1){
			day = "0" + day;	
		}
		String month = "" + (c.get(Calendar.MONTH)+1);
		if(month.length()==1){
			month = "0" + month;	
		}
		
		String timestring = c.get(Calendar.YEAR) + "-" + month + "-" + day;
		if (withTime) {

			String hour = "" + c.get(Calendar.HOUR_OF_DAY);
			if(hour.length()==1){
				hour = "0" + hour;	
			}

			String minute = "" + c.get(Calendar.MINUTE);
			if(minute.length()==1){
				minute = "0" + minute;	
			}

			String second = "" + c.get(Calendar.SECOND);
			if(second.length()==1){
				second = "0" + second;	
			}
			timestring += " " + hour + ":" + minute + ":" + second;
		}


		return timestring;			
	}
	public ObjectTemplate getObjectTemplate(String id){
		ObjectTemplate ot = null;
		ObjectTemplateAdministration ota = (ObjectTemplateAdministration)getObjectByName("ObjectTemplateAdministration", "1");
		ot = (ObjectTemplate)ota.getObjectByName("ObjectTemplate", id);
		return ot;
	}
	public Vector getObjectTemplates(){
		ObjectTemplateAdministration ota = (ObjectTemplateAdministration)getObjectByName("ObjectTemplateAdministration", "1");
		return ota.getObjects("ObjectTemplate");
	}
	public String getHelpItem(String id){
		return "dqdqwdiqdhqudhwq";
	}
	
	public void setCreationInfo(OpenCommunityUserSession userSession) {
		setProperty("DateCreated", getTimestamp());
		setProperty("DateModified", getTimestamp());
		setProperty("UserCreated", userSession != null ? userSession.getStaffMemberID() : -1);
		setProperty("UserModified", userSession != null ? userSession.getStaffMemberID() : -1);
		setProperty("Owner", userSession != null ? userSession.getOrganisationID() : getParent().getObject("Owner"));
	}
	
	public void setCreationInfo(BasicOCObject other) {
		getProperty("DateCreated").setObject(other.getObject("DateCreated"));
		getProperty("DateModified").setObject(other.getObject("DateModified"));
		getProperty("UserCreated").setObject(other.getObject("UserCreated"));
		getProperty("UserModified").setObject(other.getObject("UserModified"));
		getProperty("Owner").setObject(other.getObject("Owner"));
	}
	public BasicClass createObject(String type, String collectionname, ApplicationContext context){
		
		BasicClass bc = null;
		try{
			Class<?> c = Class.forName(type);
			bc = (BasicClass)c.newInstance();
			bc.setParent(this);
			if (bc instanceof BasicOCObject) {
				((BasicOCObject)bc).setCreationInfo(context != null ? (OpenCommunityUserSession)context.getObject("usersession") : null);
			}
			if(collectionname != null){
				addSubobject(collectionname, bc, "");
			}
		}
		catch(Exception e){
			writeError(e);
		}
		return bc;	
		
	}
	public static String getToday() {
		return getNow(false);
	}
	public static String getTimestamp(){
		return getNow(true);
	}
	//----------------------------------pdf--------------------------------------------
	
	public String createPDF(Activity activity, Document doc){
		
		PDFWriter pdfWriter = new PDFWriter();
		PDFTemplateLibrary templib = (PDFTemplateLibrary)getObjectByName("PDFTemplateLibrary", "1");
		String template = null;
		if(doc != null){
			template = doc.getString("Template");
		}                 
		
		String sql = null;
		
		if(activity.getParent() instanceof DossierController){
			DossierController dossierController = (DossierController)activity.getParent();
			Dossier dossier = dossierController.getDossier();
			if(dossier.getObject("OrganisationalUnit") instanceof OrganisationalUnit){
				
				OrganisationalUnit ou = (OrganisationalUnit)dossier.getObject("OrganisationalUnit");

					
				sql = "SELECT t4.*, t2.Street, t2.Number, t2.Zipcode, t2.City, t2.Country, t2.AdditionalLine, t5.Value AS Email";
				sql += " FROM OrganisationalUnit AS t1";
				sql += " JOIN Address AS t2 ON t2.OrganisationalUnitID=t1.ID";
				sql += " LEFT JOIN Person AS t3 ON t3.ID=t1.MainContactPerson";
				sql += " LEFT JOIN Identity AS t4 ON t4.PersonID=t3.ID";
				sql += " LEFT JOIN Contact AS t5 ON t5.OrganisationalUnitID=t1.ID AND t5.Type=3";
				sql += " WHERE t1.ID=" + ou.getName();
					

			}
			
		}
		else if(activity.getParent() instanceof CaseRecord){

			Dossier dossier = (Dossier)activity.getParent().getParent();
			if(dossier.getObject("OrganisationalUnit") instanceof OrganisationalUnit){
				OrganisationalUnit ou = (OrganisationalUnit)dossier.getObject("OrganisationalUnit");

					
				sql = "SELECT t4.*, t2.Street, t2.Number, t2.Zipcode, t2.City, t2.Country, t2.AdditionalLine, t5.Value AS Email";
				sql += " FROM OrganisationalUnit AS t1";
				sql += " JOIN Address AS t2 ON t2.OrganisationalUnitID=t1.ID";
				sql += " LEFT JOIN Person AS t3 ON t3.ID=t1.MainContactPerson";
				sql += " LEFT JOIN Identity AS t4 ON t4.PersonID=t3.ID";
				sql += " LEFT JOIN Contact AS t5 ON t5.OrganisationalUnitID=t1.ID AND t5.Type=3";
				sql += " WHERE t1.ID=" + ou.getName();
					

			}
			
		}	
		else if(activity.getParent() instanceof Project){

			Dossier dossier = (Dossier)activity.getParent().getParent();
			if(dossier.getObject("OrganisationalUnit") instanceof OrganisationalUnit){
				OrganisationalUnit ou = (OrganisationalUnit)dossier.getObject("OrganisationalUnit");

					
				sql = "SELECT t4.*, t2.Street, t2.Number, t2.Zipcode, t2.City, t2.Country, t2.AdditionalLine, t5.Value AS Email";
				sql += " FROM OrganisationalUnit AS t1";
				sql += " JOIN Address AS t2 ON t2.OrganisationalUnitID=t1.ID";
				sql += " LEFT JOIN Person AS t3 ON t3.ID=t1.MainContactPerson";
				sql += " LEFT JOIN Identity AS t4 ON t4.PersonID=t3.ID";
				sql += " LEFT JOIN Contact AS t5 ON t5.OrganisationalUnitID=t1.ID AND t5.Type=3";
				sql += " WHERE t1.ID=" + ou.getName();
					

			}
			
		}	
		else{
			sql = "SELECT t3.*, t4.Street, t4.Number, t4.Zipcode, t4.City, t4.Country, t4.AdditionalLine, t5.Value AS Email FROM OrganisationMember AS t1";
			sql += " JOIN Person AS t2 On t1.Person=t2.ID";
			sql += " JOIN Identity AS t3 ON t3.PersonID=t2.ID";
			sql += " JOIN Address AS t4 ON t4.PersonID=t2.ID";
			sql += " LEFT JOIN Contact AS t5 ON t5.PersonID=t2.ID AND t5.Type=3";
			
			sql += " LEFT JOIN OrganisationMemberRelationship AS t6 ON t6.OrganisationMember=t1.ID";
			/*
			sql += " JOIN Person AS t2 On t1.Person=t2.ID";
			sql += " JOIN Identity AS t3 ON t3.PersonID=t2.ID";
			sql += " JOIN Address AS t4 ON t4.PersonID=t2.ID";
			sql += " LEFT JOIN Contact AS t5 ON t5.PersonID=t2.ID AND t5.Type=3";
			*/
			
			if(doc != null){
				sql += " WHERE t1.ID=" + doc.getID("Recipient");
			}
			else{
				sql += " WHERE t1.ID=" + activity.getString("OrganisationMemberID");
			}
			
		}
		
		ObjectCollection results = new ObjectCollection("Results", "*");
		queryData(sql, results);
		logAccess("pdf : " + results.getObjects().size() + "\n" + sql);
		
		
		BasicClass note = activity.getFieldByTemplate("22"); //Wie konfigurerbar?
		if(note==null){
			note = activity.getFieldByTemplate("26");
		}
		if(note==null){
			note = activity.getFieldByTemplate("27");
		}
		
		BasicClass res1 = results.getObjectByIndex(0);
		
		StringBuilder addresses = new StringBuilder();
		
		StringBuilder memberads = new StringBuilder();
		
		StringBuilder attachments = new StringBuilder();
		
		attachments.append("Beilagen :");
		
		boolean addattachments = false;
		
		int activitycontext = activity.getID("Context");
		
		if(activitycontext==1 || activitycontext==2 || activitycontext==3 || activitycontext==4){
			
			
			
			String tenrules = "\n#NEWPAGE";
			tenrules += getTextblockContent("19", false);
			tenrules = StringEscapeUtils.unescapeHtml4(tenrules);
			tenrules = tenrules.replace("</p>", "");
			
			
			String tenrules2 = "";
			String[] lines = tenrules.split("\r\n|\r|\n");
			for(String line : lines){
				//tenrules2 += line.trim() + "\n";
				tenrules2 += line.trim();	
			}
			tenrules2 = tenrules2.replace("<br />", "\n");
			
			res1.addProperty("10RULES", "String", tenrules2);
			attachments.append("\n- 10 Regeln");
			addattachments = true;
		}
		else{
			//res1.addProperty("10RULES", "String", "\n#NEWPAGE" + "");
			res1.addProperty("10RULES", "String", "");
		}
		
		
		if(activity.getObjects("ActivityObject").size() > 0)	{
		
			String adids = "(";
			String memberadids = "(";
			for(BasicClass bc  : activity.getObjects("ActivityObject")){
				
				if(bc.getString("MemberAdRequestID").length() > 0){
					
					adids += bc.getString("MemberAdRequestID") + ",";
				}
				
				if(bc.getString("MemberAdID").length() > 0){
					
					memberadids += bc.getString("MemberAdID") + ",";
				}
				
			}
			adids = adids.substring(0, adids.length()-1) + ")";
			memberadids = memberadids.substring(0, memberadids.length()-1) + ")";
			

			
			
			ObjectCollection results2 = new ObjectCollection("Results", "*");
						
			sql = "SELECT t1.ID, t2.title, t2.description, t5.Firstname, t5.Familyname, t5.Sex, t5.DateOfBirth, t6.Street, t6.Number, t6.ZipCode, t6.City, t6.Country, t7.Value AS Email, t8.Value AS PhoneP, t9.Value AS PhoneM,";
			sql += " t14.FamilyName AS FamilyName2, t14.FirstName AS FirstName2, t14.Sex AS Sex2, t14.DateOfBirth AS DateOfBirth2, t15.Street AS Street2, t15.Number AS Number2, t15.ZipCode AS ZipCode2, t15.City AS City2, t16.Value AS Phonehome2, t17.Value AS Email2, t18.Value AS Mobile2";
			sql += " FROM MemberAdRequest AS t1";
			sql += " LEFT JOIN MemberAd AS t2 ON t1.MemberAd=t2.ID";					
			sql += " LEFT JOIN OrganisationMember AS t3 ON t2.OrganisationMemberID=t3.ID";
			sql += " LEFT JOIN Person AS t4 ON t3.Person=t4.ID";
			sql += " LEFT JOIN Identity AS t5 ON t5.PersonID=t4.ID";
			sql += " LEFT JOIN Address AS t6 ON t6.PersonID=t4.ID";
			sql += " LEFT JOIN Contact AS t7 ON t7.PersonID=t4.ID AND t7.Type=3";
			sql += " LEFT JOIN Contact AS t8 ON t8.PersonID=t4.ID AND t8.Type=0";
			sql += " LEFT JOIN Contact AS t9 ON t9.PersonID=t4.ID AND t9.Type=2";
			
			sql += " LEFT Join OrganisationMemberRelationship AS t11 ON t11.OrganisationMember=t4.ID";
			sql += " LEFT JOIN OrganisationMember AS t12 ON t11.OrganisationMemberID=t12.ID";
			sql += " LEFT Join Person AS t13 ON t12.Person=t13.ID";
			sql += " LEFT JOIN Identity AS t14 ON t14.PersonID=t13.ID";
			sql += " LEFT JOIN Address AS t15 ON t15.PersonID=t13.ID";
			sql += " LEFT JOIN Contact AS t16 ON t16.PersonID=t13.ID AND t16.Type=0";
			sql += " LEFT JOIN Contact AS t17 ON t17.PersonID=t13.ID AND t17.Type=3";
			sql += " LEFT JOIN Contact AS t18 ON t18.PersonID=t13.ID AND t18.Type=2";
	
						
			sql += " WHERE t1.ID IN " + adids;
			sql += " AND t1.Status=1";
			
			logAccess(sql);
			
			queryData(sql, results2);
			
			if(results2.getObjects().size() > 0){
				addresses.append("\n#NEWPAGE");
				addresses.append("\n<b>Bestellte Adressen</b>");
				
				attachments.append("\n\u2022 Bestellte Adressen");
				addattachments = true;
			}
			
			
			for(BasicClass bc : results2.getObjects()){
				
				boolean managedaccount = false;
				if(bc.getString("FAMILYNAME2").length() > 0){
					managedaccount = true;
				}
				
				addresses.append("\n\n<b>" + bc.getString("FIRSTNAME") + " " + bc.getString("FAMILYNAME") + "</b>");

				if(!managedaccount){
					addresses.append("\n" + bc.getString("STREET") + " " + bc.getString("NUMBER"));
					addresses.append("\n" + bc.getString("ZIPCODE") + " " + bc.getString("CITY"));
					//addresses.append("\n" + bc.getString("COUNTRY"));
	
					if(bc.getString("EMAIL").length() > 0){
						addresses.append("\nEmail : " + bc.getString("EMAIL"));
					}
					if(bc.getString("PHONEP").length() > 0){
						addresses.append("\nTel. p : " + bc.getString("PHONEP"));
					}
					if(bc.getString("PHONEM").length() > 0){
						addresses.append("\nTel. m : " + bc.getString("PHONEM"));
					}
				}
				addresses.append("\n");
				
				addresses.append("\nInserat : " + bc.getString("TITLE"));
				addresses.append("\n" + bc.getString("DESCRIPTION"));
				
				if(managedaccount){
					addresses.append("\n\nF�r die Kontaktaufnahme wenden Sie sich bitte an:");
					
					addresses.append("\n\n<b>" + bc.getString("FIRSTNAME2") + " " + bc.getString("FAMILYNAME2") + "</b>");
					addresses.append("\n" + bc.getString("STREET2") + " " + bc.getString("NUMBER2"));
					addresses.append("\n" + bc.getString("ZIPCODE2") + " " + bc.getString("CITY2"));
	
					if(bc.getString("EMAIL2").length() > 0){
						addresses.append("\nEmail : " + bc.getString("EMAIL2"));
					}
					if(bc.getString("PHONEP2").length() > 0){
						addresses.append("\nTel. p : " + bc.getString("PHONEP2"));
					}
					if(bc.getString("PHONEM2").length() > 0){
						addresses.append("\nTel. m : " + bc.getString("PHONEM2"));
					}
					
				}
				addresses.append("\n");
			}
			
			//Inserate
			results2.getObjects().clear();
			sql = "SELECT t1.*, t2.Title AS Category FROM MemberAd AS t1 JOIN MemberAdCategory AS t2 ON t1.Template=t2.ID";
			
			sql += " WHERE t1.ID IN " + memberadids;
			
			queryData(sql, results2);
			
			if(results2.getObjects().size() > 0){
				memberads.append("\n#NEWPAGE");
				memberads.append("\n<b>Aufgeschaltete Inserate</b>");
				
				attachments.append("\n\u2022 Aufgeschaltete Inserate");
				addattachments = true;
			}
			
			
			
			for(BasicClass bc : results2.getObjects()){
				memberads.append("\n\n<b>" + bc.getString("CATEGORY") + "</b>");
				memberads.append("\n" + bc.getString("TITLE"));
				memberads.append("\n" + bc.getString("DESCRIPTION"));
				memberads.append("\nAufgeschaltet von " + DateConverter.sqlToShortDisplay(bc.getString("VALIDFROM"), false) + " - " + DateConverter.sqlToShortDisplay(bc.getString("VALIDUNTIL"), false));
			}
			

			
		}
		
		res1.addProperty("SUBJECT", "String", activity.getString("Title"));
		
		res1.addProperty("DATE", "String", DateConverter.dateToShortDisplay(new java.util.Date(), false));
		if(addattachments){
			res1.addProperty("ATTACHMENTS", "String", attachments.toString());
		}
		else{
			res1.addProperty("ATTACHMENTS", "String", "");
		}
		
		
		
		String content = note.getString("Content");
		
		content = StringEscapeUtils.unescapeHtml4(content);
		
		String[] lines = content.split("\r\n|\r|\n");
		
		content = "";
		int cnt = 0;
		for(String line : lines){
			if(cnt > 0){
				content += "\n" + line.trim();	
			}
			else{
				content += line.trim();	
			}
			cnt++;
		}
		
		
		//content = content.replace("<@addresses>", addresses.toString());
		
		if(activity.getParent() != null && activity.getParent() instanceof OrganisationMember){
			OrganisationMember om = (OrganisationMember)activity.getParent();
			//content = content.replace("<@addressation>", om.getAddressation());	
			res1.addProperty("ADDRESSATION", "String", om.getAddressation());
			res1.addProperty("DATE", "String", DateConverter.dateToShortDisplay(new java.util.Date(), false));
		}
		
		res1.addProperty("CONTENT", "String", content);
		
		
		res1.addProperty("ADDRESSES", "String", addresses.toString());
		res1.addProperty("addresses", "String", addresses.toString());
		
		res1.addProperty("MEMBERADS", "String", memberads.toString());

		

		
		String docname = null;
		if(doc != null){
			docname = doc.getName();
		}
		else{
			docname = createPassword(8);	
		}
		
		String filename = getRootpath() + "/temp/" + docname + ".pdf";
		
		pdfWriter.createPDF(filename, templib, "2", results.getObjects());
		
		//result.setParam("download", "temp/test.pdf");
		
		//return "temp/" + docname + ".pdf";
		return "/temp/" + docname + ".pdf";
		
	}
	
	public String createPDF(List activities){
		
		PDFWriter pdfWriter = new PDFWriter();
		PDFTemplateLibrary templib = (PDFTemplateLibrary)getObjectByName("PDFTemplateLibrary", "1");
		
		Vector letters = new Vector();

		for(Object o : activities){
		
			Activity activity = (Activity)o;
		
			String sql = "SELECT t3.*, t4.Street, t4.Number, t4.Zipcode, t4.City, t4.Country, t5.Value AS Email FROM OrganisationMember AS t1";
			sql += " JOIN Person AS t2 On t1.Person=t2.ID";
			sql += " JOIN Identity AS t3 ON t3.PersonID=t2.ID";
			sql += " JOIN Address AS t4 ON t4.PersonID=t2.ID";
			sql += " LEFT JOIN Contact AS t5 ON t5.PersonID=t2.ID AND t5.Type=3";

			sql += " WHERE t1.ID=" + activity.getString("OrganisationMemberID");
			
		
			ObjectCollection results = new ObjectCollection("Results", "*");
			queryData(sql, results);
		
		
			BasicClass note = activity.getFieldByTemplate("22"); //Wie konfigurerbar?
			if(note==null){
				note = activity.getFieldByTemplate("26");
			}
			BasicClass res1 = results.getObjectByIndex(0);
		
		
			res1.addProperty("SUBJECT", "String", activity.getString("Title"));
			res1.addProperty("CONTENT", "String", note.getString("Content"));
			
			letters.add(res1);
			
		}
		
		String docname = createPassword(8);	
		
		String filename = getRootpath() + "/temp/" + docname + ".pdf";
		
		pdfWriter.createPDF(filename, templib, "2", letters);
		
		//result.setParam("download", "temp/test.pdf");
		
		return "/temp/" + docname + ".pdf";
		
	}
	
	//-----------------------------------------------------------------------------------------
	
	public List getMemberRoles(){

		Vector<BasicClass> roles = new Vector<BasicClass>();
		roles.add(new OrganisationMemberRole("1","1","(Ehe)Partnerin", "(Ehe)Partnerin"));
		roles.add(new OrganisationMemberRole("2","2","Mutter | Vater", "Tochter | Sohn"));
		roles.add(new OrganisationMemberRole("3","3","Tochter | Sohn", "Mutter | Vater"));
		roles.add(new OrganisationMemberRole("4","4","Verwandtschaft", "Verwandschaft"));
		roles.add(new OrganisationMemberRole("5","5","Freundin | Bekannte", "Freundin | Bekannte"));
		roles.add(new OrganisationMemberRole("6","6","Nachbar | in", "Nachbar | in"));
		roles.add(new OrganisationMemberRole("7","7","Klient | in", "Betreuer | in"));
		
		roleMap = new Hashtable();
		for(BasicClass bc : roles){
			roleMap.put(bc.getName(), bc.getString("Title"));
		}
		
		return roles;
	}
	
	public OrganisationMemberInfo getOrganisationMemberInfo(String id){
		
		OrganisationMemberInfo omi = new OrganisationMemberInfo();
		
		String sql = "SELECT t3.Firstname, t3.Familyname, t3.Sex, t4.AdditionalLine, t4.Street, t4.Number, t4.Zipcode, t4.City, t4.Country, t5.Value AS Email FROM OrganisationMember AS t1";
		sql += " LEFT JOIN Person AS t2 ON t1.Person=t2.ID";
		sql += " LEFT JOIN Identity AS t3 ON t3.PersonID=t2.ID";
		sql += " LEFT JOIN Address AS t4 ON t4.PersonID=t2.ID";
		sql += " LEFT JOIN Contact AS t5 ON t5.PersonID=t2.ID AND t5.Type=3";
		sql += " WHERE t1.ID=" + id;
		ObjectCollection results = new ObjectCollection("Results", "*");
		queryData(sql, results);
		if(results.getObjects().size() > 0){
			Record record = (Record)results.getObjects().get(0);
			omi.setProperty("Title", record.getString("FIRSTNAME") + " " + record.getString("FAMILYNAME"));
			omi.setProperty("FirstName", record.getString("FIRSTNAME"));
			omi.setProperty("FamilyName", record.getString("FAMILYNAME"));
			omi.setProperty("Sex", record.getString("SEX"));
			omi.setProperty("AdditionalLine", record.getString("ADDITIONALLINE"));
			omi.setProperty("Street", record.getString("STREET"));
			omi.setProperty("Number", record.getString("NUMBER"));
			omi.setProperty("Zipcode", record.getString("ZIPCODE"));
			omi.setProperty("City", record.getString("CITY"));
			omi.setProperty("Country", record.getString("COUNTRY"));
			omi.setProperty("Email", record.getString("EMAIL"));

			if(record.getID("SEX")==1){
				omi.setProperty("Addressation", "Sehr geehrter Herr " + record.getString("FAMILYNAME"));	
			}
			else if(record.getID("SEX")==2){
				omi.setProperty("Addressation", "Sehr geehrte Frau " + record.getString("FAMILYNAME"));	
			}
			
			omi.initObjectLocal();
		}
		else{
			omi.setProperty("Title", "Mitglied nicht gefunden");
		}
		
		return omi;
		
	}
	
	public String getBaseURL(String path,  HttpServletRequest request){
		

		String host = request.getServerName();
		int port = request.getServerPort();

		if(port==80){
			return "http://" + host;	//appliikation hinter apachce, bessere L�sung finden
		}
		else{
			return "http://" + host + ":" + port + "/" + getString("instancename") + "/cms/nachbarnet";
		}
	
	}
	
	public OrganisationMember openOrganisationMember(String omid){
		
		OrganisationMember om = (OrganisationMember)getObject(this, "OrganisationMember", "ID", omid, true);
		logAccess("opening member " + om);
		return om;
		
	}
	
	//---------------------------------------delete-functions------------------------------------------------------
	
	public boolean deleteObject(BasicClass o){
		
		boolean success = false;
		
		try{


			TransactionHandler transactionHandler = null;
			
			if(o instanceof MemberAdRequest){
				//delete feedback
				
				
				
				//if activity has only one request, delete activity
				
				
				
			}
			else if(o instanceof MemberAd){
				
				
				
				
			}
			else if(o instanceof Activity){
				
				try{
				
					transactionHandler = startTransaction();
					dataStore.removeObject(transactionHandler.getConnection(), o.getContainer().toString(), "ID", o.getName(), true);
					transactionHandler.commitTransaction();			
					success = true;
				}
				catch(java.lang.Exception e){
					logException(e);
					transactionHandler.rollbackTransaction();						
				}				
				
				
			}
			else if(o instanceof OrganisationMember){
				
				
				
				
			}
			else if(o instanceof Parameter){
				
				try{
				
					transactionHandler = startTransaction();
					dataStore.removeObject(transactionHandler.getConnection(), o.getContainer().toString(), "ID", o.getName(), true);
					transactionHandler.commitTransaction();			
					success = true;
				}
				catch(java.lang.Exception e){
					transactionHandler.rollbackTransaction();						
				}
				
			}
			else if(o instanceof Note){
				
				try{
				
					transactionHandler = startTransaction();
					dataStore.removeObject(transactionHandler.getConnection(), o.getContainer().toString(), "ID", o.getName(), true);
					transactionHandler.commitTransaction();			
					success = true;
					
				}
				catch(java.lang.Exception e){
					transactionHandler.rollbackTransaction();						
				}
				
			}
			else if(o instanceof ActivityParticipant){
				
				
				try{
				
					transactionHandler = startTransaction();
					dataStore.removeObject(transactionHandler.getConnection(), o.getContainer().toString(), "ID", o.getName(), true);
					o.getParent().deleteElement("ActivityParticipant", o);
					transactionHandler.commitTransaction();			
					success = true;
				}
				catch(java.lang.Exception e){
					transactionHandler.rollbackTransaction();						
				}
			
			}
			else if(o instanceof QueryDefinition){
				
				
				try{
				
					transactionHandler = startTransaction();
					dataStore.removeObject(transactionHandler.getConnection(), o.getContainer().toString(), "ID", o.getName(), true);
					o.getParent().deleteElement("ActivityParticipant", o);
					transactionHandler.commitTransaction();			
					success = true;
				}
				catch(java.lang.Exception e){
					transactionHandler.rollbackTransaction();						
				}
			
			}
			
		}
		catch(java.lang.Exception e){
			
			logException(e);
			success = false;
			
		}
		
		
		return success;
	}
	
	
	//-----------------------------------------------------------------------------------------------------------------------
	
	
	public String createAndSendEmail(String omid, String subject, String content, ApplicationContext context){
		
		String id = createActivity("4", omid, subject, context, 0, "21", content, null, 0);
		sendAllPendingMails();
				
		return id;
		
	}
	
	public String createActivity(String templateid, String omid, String title, ApplicationContext context, int activitycontext, String notetemplate, String notecontent){
			
		return 	createActivity(templateid, omid, title, context, activitycontext, notetemplate, notecontent, null);
	}
	
	public String createActivity(String templateid, String omid, String title, ApplicationContext context, int activitycontext, String notetemplate, String notecontent, String batchid){
			
		return 	createActivity(templateid, omid, title, context, activitycontext, notetemplate, notecontent, batchid, 0);
	}
	
	public String createActivity(String templateid, String omid, String title, ApplicationContext context, int activitycontext, String notetemplate, String notecontent, String batchid, int status){
		
		String activityid = null;
		Activity newactivity = null;
	
		ObjectTemplate ot = getObjectTemplate(templateid);	
		
		if(ot != null){
					
			newactivity = (Activity)ot.createObject("ch.opencommunity.base.Activity", null, context);
			newactivity.applyTemplate(ot);
			newactivity.setProperty("Status", status);
			newactivity.setProperty("Context", activitycontext);
			newactivity.addProperty("OrganisationMemberID", "String", omid);
			if(batchid != null){
				newactivity.setProperty("BatchActivityID", batchid);
			}

			
					
			newactivity.setProperty("Title", title);
			
			if(notetemplate != null){
				Note note = (Note)newactivity.getFieldByTemplate(notetemplate);	
				if(note != null){
					note.setProperty("Content", notecontent);	
				}
				
			}
			
			activityid = insertObject(newactivity, true);
					

			
		}
		//return newactivity;
		return activityid;
	}
	
	public String createLoginCode(String omid, ApplicationContext context){
		
		String code = createPassword(20);
		
		LoginCode loginCode = (LoginCode)createObject("ch.opencommunity.base.LoginCode", null, context);	
		loginCode.addProperty("OrganisationMemberID", "String", omid);
		loginCode.setProperty("Code", code);
		loginCode.setProperty("Status", 0);
		insertObject(loginCode);
		
		return code;
	}
	
	public String handleOneTimeLogin(String code){
		ObjectCollection results = new ObjectCollection("Results", "*");
		String sql = "SELECT ID, OrganisationMemberID AS OMID From LoginCode WHERE Code='" + code + "' AND Status=0";
		queryData(sql, results);
		
		String omid = null;
		String id = null;
		if(results.getObjects().size() == 1){
			for(BasicClass record : results.getObjects()){
				omid = record.getString("OMID");
				id = record.getString("ID");
				executeCommand("UPDATE LoginCode SET Status=1 WHERE ID=" + id);
			}
		}
		return omid;
	}
	
	public Vector getFiles(){
		WebSiteRoot wsr = (WebSiteRoot)cms.getObjectByIndex("WebSiteRoot", 0);
		if(wsr != null){
			return wsr.getObjects("FileObject");
		}	
		else{
			return null;
		}
		
	}
	
	//-----------------------------------------------------------------------------------------------------
	
	public void openOrganisatinMember(String id){
		
		
	}
} 
