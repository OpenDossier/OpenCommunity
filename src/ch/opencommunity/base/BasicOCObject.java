package ch.opencommunity.base;

import ch.opencommunity.common.*;
import ch.opencommunity.server.*;
import ch.opencommunity.office.WordServerInterface;
import ch.opencommunity.office.WordDocument;

import org.kubiki.base.*;
import org.kubiki.database.Record;
import org.kubiki.application.*;
import org.kubiki.application.server.WebApplicationContext;
import org.kubiki.servlet.*;

import java.util.List;
import java.util.Enumeration;
import java.util.Calendar;
import java.io.IOException;
import java.io.File;

import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BasicOCObject extends Record implements ActionHandler, WordServerInterface{

	private boolean hasChanged;

	public BasicOCObject(){

		addProperty("UserCreated", "Integer", "", true, "Benutzer erstellt");
		addProperty("DateCreated", "DateTime", "", false, "Datum erstellt");
		addProperty("UserModified", "Integer", "", true, "Benutzer mod.");
		addProperty("DateModified", "DateTime", "", true, "Datum mod.");
		addProperty("Owner", "Integer", "0", true, "Eigentümer");
		addProperty("Template", "Integer", "0", true, "Template");
		
		addProperty("Title", "String", "", false, "Bezeichnung", 200);
		addProperty("Status", "Integer", "0");

	}
	public void addFunction(String command, String label, String icon){
	
	}
	public Object getValue(){
		return getName();	
	}
	public String getLabel(){
		if(getString("Title").length() > 0){
			return getString("Title");
		}
		else{
			return getName();	
		}
	}
	public String toString(){
		return getString("Title");
	}
	public String getTitle(){
		return getString("Title");
	}
	public void setTitle(String title){
		setProperty("Title", title);
	}
	public ActionResult onAction(BasicClass source, String command, ApplicationContext context) {
		
		ActionResult result = null;
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		if(command.equals("saveobject")){
			ocs.logAccess("saving object");
			if(saveObject(context)){
				result = new ActionResult(ActionResult.Status.OK, "Datensatz gespeichert");
				result.setParam("refresh", "currentsection");			
				//OpenDossierServer.checkReturnTarget(context.getString("returnTo"), result);
			}
			else{
				result = new ActionResult(ActionResult.Status.FAILED, "Fehler beim Speichern");
			}
		}
		else {
			result = new ActionResult(ActionResult.Status.FAILED, "Befehl '" + command + "' nicht gefunden");
		}
		return result;
	}
	public boolean saveObject(ApplicationContext context){
		return saveObject(context, "");
	}
	public boolean saveObject(ApplicationContext context, String prefix){
		
		Enumeration<String> parameterNames = ((WebApplicationContext)context).getParameterNames();
		while (parameterNames.hasMoreElements()) {
			String parameterName = parameterNames.nextElement();
			if (parameterName.startsWith("subobject_")) {
				String[] nameParts = parameterName.split("_");
				if (nameParts.length >= 4) {
					String collectionName = nameParts[1];
					String subobjectID = nameParts[2];
					String propertyName = nameParts[3];
					BasicClass subObj = getSubobject(subobjectID, collectionName);
					if (subObj instanceof BasicOCObject) {
						Property p = subObj.getProperty(propertyName);
						if (p != null) {
							p.setValue(context.getString(parameterName));
							((BasicOCObject)subObj).saveObject((OpenCommunityUserSession)context.getObject("usersession"));
						}
					}
				}
			}
		}		
		List<String> fieldNames = ps.getNames();
		for(String fieldName : fieldNames){
			String value = context.getString(prefix + fieldName);
			if(value != null){
				setProperty(fieldName, value);
			}
		}
		return saveObject((OpenCommunityUserSession)context.getObject("usersession"));
	}
	public boolean saveObject(OpenCommunityUserSession userSession){
		
		setSaveInfo(userSession);

		initObjectLocal();	
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		ocs.logAccess("saving object " + this);
		if(ocs.updateObject(this)){	
			hasChanged = false;
			return true;
		}
		else{
			return false;
		}
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
	
	public BasicOCObject getCreationInfo() {
	
		BasicOCObject creationInfo = new DummyObject("", "");
		creationInfo.addProperty("DateCreated", "DateTime", "");
		creationInfo.addProperty("DateModified", "DateTime", "");
		creationInfo.addProperty("UserCreated", "Integer", "");
		creationInfo.addProperty("UserModified", "Integer", "");
		creationInfo.addProperty("Owner", "Integer", "");
		creationInfo.setCreationInfo(this);
		return creationInfo;
		
	}
	protected void setSaveInfo(OpenCommunityUserSession userSession) {
	}
	public String dumpXHTMLContent() throws IOException {
		if (hasProperty("Content")) {
			String filePath = getTempFileName(true, "docx");
			String rootPath = ((OpenCommunityServer)getRoot()).getRootpath();
			WordDocument.saveXHTML(filePath, rootPath, getString("Content"));
			return filePath;
		}
		else {
			return null;
		}
	}
	public String getTempFileName(boolean filesystem, String suffix) {
		String separator = filesystem ? File.separator : "/";
		String tempPath = "";
		if (filesystem) {
			tempPath += ((OpenCommunityServer)getRoot()).getRootpath();
			tempPath += separator;
		}
		tempPath += "temp" + separator + getTablename() + separator + getName() + "." + suffix;
		return tempPath;
	}
	@Override
	public boolean onWordClientAction(String action, HttpServletRequest request, HttpServletResponse response,
			OpenCommunityUserSession userSession) throws IOException {
		boolean isHandled = false;
		if (action.equals("setValue")) {
			String name = request.getParameter("name");
			String value = request.getParameter("value");
			if (name != null && value != null) {
				if (hasProperty(name)) {
					setProperty(name, value);
					isHandled = true;
				}
			}
		}
		else  if (action.equals("save")) {
			saveObject(new WebApplicationContext(request));
		}
		return isHandled;
	}
	protected void addDeleteContextMenuEntry() {
		// parameters to JS function are added in HTMLForm.prepareCommand()
		addContextMenuEntry("javascript:deleteObject", "Löschen", true);
	}
	public static String getToday() {
		return getNow(false);
	}
	public static String getTimestamp(){
		return getNow(true);
	}
	private static String getNow(boolean withTime) {
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
	public BasicOCObject createObject(String type){
			
		BasicOCObject bdo = null;
		
		try{
			Class<?> c = Class.forName(type);
			bdo = (BasicOCObject)c.newInstance();
			bdo.setParent(this);
			bdo.initObject();
		}
		catch(Exception e){
			writeError(e);
		}
		
		return bdo;		
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
	// wenn funktionsweise zufriedenstellen, zurück in BasicClass
	public String getXMLString(boolean subobjects){
		collectValues();
		String s = "\n<" + classtype;
		Enumeration<Property> en = ps.elements();
		while(en.hasMoreElements()){
			Property p = en.nextElement();
			String name = p.getName();
			if(p.getType().equals("Integer")){
				s = s + " " + name + "=\"" + getID(name) + "\"";
			}
			else if(p.getObject() != null){

				s = s + " " + name + "=\"" + encode(p.getObject().toString()) + "\"";
			}
			else{
				s = s + " " + name + "=\"\"";	
			}
			
		}
		s = s + ">\n";
		if(subobjects==true){
			
			for(int i = 0; i < collections.size(); i++){
					ObjectCollection oc = collections.elementAt(i);
					Vector<BasicClass> objects = oc.getObjects();
					if(oc.isSaved==true){
					for(int j = 0; j < objects.size(); j++){
						BasicClass bc = objects.elementAt(j);	
						s = s + bc.getXMLString() + "\n";
					}
					}	
				}
			}		
		s = s + "</" + classtype + ">";
		if(hasProperty("isfile")==true){
			if(getBoolean("isfile")){
				saveFile();
			}
		}
		return s;
	}
	public String getXMLString(){
		collectValues();
		String s = "\n<" + classtype;
		Enumeration<Property> en = ps.elements();
		while(en.hasMoreElements()){
			Property p = en.nextElement();
			String name = p.getName();
			if(p.getType().equals("Integer")){
				s = s + " " + name + "=\"" + getID(name) + "\"";
			}
			else if(p.getObject() != null){
				Object o = p.getObject();
				Class<?>[] interfaces = o.getClass().getInterfaces();
				if(interfaces.length > 0){
					if(interfaces[0].equals(BasicInterface.class)){

						BasicInterface bi = (BasicInterface)o;
						s = s + " " + name + "=\"" + encode(bi.getValue().toString()) + "\"";
					}
					else{
						s = s + " " + name + "=\"" + encode(p.getObject().toString()) + "\"";	
					}
				}
				else{
					
					s = s + " " + name + "=\"" + encode(p.getObject().toString()) + "\"";
					
				}
				

			}
			else{
				s = s + " " + name + "=\"\"";	
			}
			
		}
		s = s + ">\n";
		
		for(int i = 0; i < collections.size(); i++){
			ObjectCollection oc = collections.elementAt(i);
			Vector<BasicClass> objects = oc.getObjects();
			//if(oc.isSaved){
			for(int j = 0; j < objects.size(); j++){
				BasicClass bc = objects.elementAt(j);	
				s = s + bc.getXMLString() + "\n";
			}
			//}	
		}		
		s = s + "</" + classtype + ">";
		if(hasProperty("isfile")==true){
			if(getBoolean("isfile")){
				saveFile();
			}
		}
		return s;
	}
}  
