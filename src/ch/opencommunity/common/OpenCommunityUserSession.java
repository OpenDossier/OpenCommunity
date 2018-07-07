package ch.opencommunity.common;

import org.kubiki.base.Property;
import org.kubiki.base.BasicClass;
import org.kubiki.base.ActionResult;
import org.kubiki.base.ObjectCollection;
import org.kubiki.ide.BasicProcess;

import org.kubiki.application.BasicUserSession;
import org.kubiki.application.CodeDefinition;

import org.kubiki.application.server.WebApplicationClient;

import org.kubiki.mail.*;

import org.kubiki.util.StringUtilities;
import org.kubiki.util.DateConverter;

import org.kubiki.application.ApplicationContext;
import org.kubiki.application.server.ApplicationServer;

import ch.opencommunity.base.*;
import ch.opencommunity.advertising.*;
import ch.opencommunity.server.OpenCommunityServer;

import org.kubiki.base.ConfigValue;

import java.util.Map;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;
import java.util.List;

public class OpenCommunityUserSession extends WebApplicationClient{

	int organisationMemberID;
	private BasicProcess currentProcess = null; 
	private Map<String, BasicProcess> canceledProcesses = new HashMap<String, BasicProcess>(); 
	private BasicProcess suspendedProcess = null; 
	private int loginID = 0;
	private Map<String, String> wordClientDocuments;
	private Vector memberadids;
	private HashMap parameters;
	private Vector profiles;
	private Vector advertisements;
	private String[][] commercialads;
	
	MemberAdCategory mac = null;
	
	private OrganisationMember om = null;
	
	IMAPEmailAdministration mailadministration = null;
	
	private HashMap filterParameters = null;
	
	OpenCommunityServer ocs = null;
	
	Map commands = null;
	Map actions = null;
	
	public int selectedYear;
	
	public String sessionid;

	public OpenCommunityUserSession(){
	
		wordClientDocuments = new Hashtable<String, String>();
		memberadids = new Vector();
        parameters = new HashMap();
        filterParameters = new HashMap();
        advertisements = new Vector();
        
        //-------------------Filterproperties-----------------------------
        // auslagern in eigene Klasse "FilterProperties"

        Property p = addProperty("FamilyName", "String", "");
        //p.setAction("filterList('user')");
        
        p = addProperty("FirstName", "String", "");
        //p.setAction("filterList('user')");
        
        p = addProperty("DateOfBirth", "String", "");
        //p.setAction("filterList('user')");
        
        addProperty("Sex", "Integer", "");
        
        addProperty("Title", "String", "");  
        addProperty("Description", "String", ""); 
        addProperty("Language", "String", ""); 
        addProperty("Category", "Integer", "");
        addProperty("Category1", "Integer", "");
        addProperty("Category2", "Integer", "");
        addProperty("Type", "Integer", "");
        addProperty("Type1", "Integer", "");
        addProperty("Type2", "Integer", "");
        addProperty("Status", "Integer", "");
        addProperty("Status1", "Integer", "");
        addProperty("Status2", "Integer", "");
        addProperty("ValidFrom1", "Date", "");
        addProperty("ValidUntil1", "Date", "");
        addProperty("ValidFrom2", "Date", "");
        addProperty("ValidUntil2", "Date", "");
        
        addProperty("RegisteredSince", "Date", "");
        addProperty("RegisteredBefore", "Date", "");
        
        addProperty("ZipCode", "String", "");
        addProperty("Email", "String", "");
        addProperty("Telephone", "String", "");
        
        addProperty("NoEmail", "Integer", "");
        addProperty("IsUser", "Integer", "");
        addProperty("IsMember", "Integer", "");
        addProperty("IsSponsor", "Integer", "");
        addProperty("IsChequeReceiver", "Integer", "");
        addProperty("IsToDelete", "Integer", "");
        addProperty("OnlyActive", "Integer", "");
        addProperty("AgeGroup", "Integer", "");
        
        addProperty("ChequeIDs", "String", ""); 
        addProperty("OrganisationMemberIssued", "String", ""); 
 
        addProperty("DonationMin", "Integer", "");
        addProperty("DonationMax", "Integer", "");
        addProperty("DonationFrom", "Date", "");
        addProperty("DonationUntil", "Date", "");
        
        addProperty("CurrentQueryDefinition", "Integer", "0");
        addProperty("CurrentQueryDefinition2", "Integer", "0");
        
        addProperty("OrganisationMemberStatus", "Integer", "");
        addProperty("FilterMode", "Integer", "1");
        
        addProperty("OrganisationTitle", "String", "");
        addProperty("OrganisationType", "Integer", "");
        addProperty("Purpose", "IntegerArray", "");
        addProperty("Eligibility", "Integer", "");
        addProperty("OrganisationSubtype", "IntegerArray", "");
        
        selectedYear = DateConverter.getCurrentYear();
        
	}
	public void initObject(){
		
		
		
		commands = new HashMap();
		
		
		actions = new HashMap();
		actions.put("prolongad", "");
		
		
		ocs = (OpenCommunityServer)getRoot();
		Vector categories = ocs.getMemberAdAdministration().getObjects("MemberAdCategory");
		getProperty("Category").setSelection(categories);
		getProperty("Category1").setSelection(categories);
		getProperty("Category2").setSelection(categories);
		
		Vector types = new Vector();
		types.add(new ConfigValue("0","0", "Angebot"));
		types.add(new ConfigValue("1","1", "Nachfrage"));
		types.add(new ConfigValue("2","2", "Tandem"));
		getProperty("Type").setSelection(types);
		getProperty("Type1").setSelection(types);
		getProperty("Type2").setSelection(types);
		
		Vector status = new Vector();
		status.add(new ConfigValue("0","0", "erfasst"));
		status.add(new ConfigValue("1","1", "freigeschaltet"));
		status.add(new ConfigValue("2","2", "pausiert"));
		status.add(new ConfigValue("3","3", "inaktiv"));
		status.add(new ConfigValue("4","4", "zu kontrollieren"));
		getProperty("Status").setSelection(status);
		getProperty("Status1").setSelection(status);
		
		getProperty("Status2").setSelection(status);
		
		Vector sex = new Vector();
		sex.add(new ConfigValue("0", "0", "Nicht gesetzt"));
		sex.add(new ConfigValue("1", "1", "Herr"));
		sex.add(new ConfigValue("2", "2", "Frau"));
		getProperty("Sex").setSelection(sex);
		
		Vector agegroups = new Vector();

		agegroups.add(new ConfigValue("0-20", "0-20", "0-20"));	
		agegroups.add(new ConfigValue("21-35", "21-35", "21-35"));	
		agegroups.add(new ConfigValue("36-50", "36-50", "36-50"));
		agegroups.add(new ConfigValue("51-65", "51-65", "51-65"));
		agegroups.add(new ConfigValue("66-200", "66-200", "66+"));
		
		getProperty("AgeGroup").setSelection(agegroups);
		
		Vector queryDefinitions = new Vector();
		for(BasicClass bc : ocs.getObjects("QueryDefinition")){
			if(bc.getID("Scope")==1){
				queryDefinitions.add(bc);	
			}
		}
		getProperty("CurrentQueryDefinition").setSelection(queryDefinitions);
		
		queryDefinitions = new Vector();
		for(BasicClass bc : ocs.getObjects("QueryDefinition")){
			if(bc.getID("Scope")==2){
				queryDefinitions.add(bc);	
			}
		}
		getProperty("CurrentQueryDefinition2").setSelection(queryDefinitions);
		
		Vector modes = new Vector();
		modes.add(new ConfigValue("1", "1", "Standard"));
		modes.add(new ConfigValue("2", "2", "Erweitert"));	
		modes.add(new ConfigValue("3", "3", "Spenden"));	
		modes.add(new ConfigValue("4", "4", "Erweitert+Spenden"));	
		getProperty("FilterMode").setSelection(modes);
		
		status = new Vector();
		status.add(new ConfigValue("0", "0", "registriert"));
		status.add(new ConfigValue("4", "4", "zu kontrollieren"));
		status.add(new ConfigValue("1", "1", "aktiv"));
		status.add(new ConfigValue("3", "3", "zu löschen"));
		status.add(new ConfigValue("2", "2", "inaktiv"));
		status.add(new ConfigValue("6", "6", "zu anonymisieren"));
		status.add(new ConfigValue("5", "5", "anonymisiert"));
		getProperty("OrganisationMemberStatus").setSelection(status);
		
		getProperty("OrganisationType").setSelection(ocs.getInstitutionTypes());
		
		Vector eligibility = new Vector();
		eligibility.add(new ConfigValue("1", "1", "*"));
		eligibility.add(new ConfigValue("2", "2", "**"));
		eligibility.add(new ConfigValue("3", "3", "***"));
		getProperty("Eligibility").setSelection(eligibility);	
		
		CodeDefinition subtype = ocs.getCodeDefinition("Foerderlogik");
		if(subtype != null){
			getProperty("OrganisationSubtype").setSelection(subtype.getCodeList());	
		}
		
		sessionid = ocs.createPassword(10);
		
	}
	
	public String getSessionID(){
		return sessionid;	
	}
	
	public boolean hasAction(String action){
		
		if(actions.get(action) != null){
			return true;	
		}
		else{
			return false;	
		}
			
	}
	public boolean hasCommand(String command){
		
		if(commands.get(command) != null){
			return true;	
		}
		else{
			return false;	
		}
			
	}
	@Override
	public ActionResult onAction(BasicClass owner, String command, ApplicationContext context){
		
		ocs.logAccess("sessionaction : " + command);
		
		ActionResult result = null;
		
		if(command.equals("prolongad")){
			String memberadid = context.getString("memberadid");
			String code = context.getString("code");
			if(code!=null){
				String sql = "UPDATE MemberAd SET ValidUntil=(ValidUntil +  INTERVAL \'" + 365 + " days\') WHERE ID=" + memberadid + " AND Code=\'" + code + "\'";
				ocs.executeCommand(sql);
							
			}
			else{
				if(memberadid != null){
					result = ocs.startProcess("ch.opencommunity.process.MemberAdFinalize", this, context.getParameterMap(), context, this);
				}
			}
		}
		else if(command.equals("updateSelectionInfo")){
			
			result = new ActionResult(ActionResult.Status.OK, "");
			result.setParam("dataContainer", "adselection");
			result.setData(getSelectedAds());
			
		}
		
		
		return result;
	}
	public void saveFilterProperties(ApplicationContext context){
		
		String prefix = "";
		List<String> fieldNames = ps.getNames();
		for(String fieldName : fieldNames){
			Property p = getProperty(fieldName);
			if(p.getType().equals("IntegerArray")){
				String value = StringUtilities.arrayToIntegerList(context.getStringArray(fieldName));
				setProperty(fieldName, value);
			}
			else{
				String value = context.getString(prefix + fieldName);
				if(value != null){
					setProperty(fieldName, value);
				}
				else if(fieldName.equals("IsMember") || fieldName.equals("IsUser") || fieldName.equals("IsSponsor") || fieldName.equals("IsToDelete") || fieldName.equals("NoEmail") || fieldName.equals("IsChequeReceiver")){
					setProperty(fieldName, "");
				}
			}
		}
		
	}
	public void setOrganisationMemberID(int organisationMemberID){
		this.organisationMemberID = organisationMemberID;
	}
	public int getOrganisationMemberID(){
		return organisationMemberID;
	}
	public BasicProcess getCurrentProcess() {
		return currentProcess;	
	}
	public void setCurrentProcess(BasicProcess currentProcess) {
		this.currentProcess = currentProcess;
	}
	public BasicProcess getCanceledProcess(String className) {
		return canceledProcesses.get(className);	
	}
	public void setCanceledProcess(String className, BasicProcess canceledProcess) {
		canceledProcesses.put(className, canceledProcess);
	}

	public BasicProcess getSuspendedProcess() {
		return suspendedProcess;	
	}
	public void setSuspendedProcess(BasicProcess suspendedProcess) {
		this.suspendedProcess = suspendedProcess;
	}
	public boolean hasPermission(Permission permission){
		return true;
	}
	public boolean hasPermission(String command, BasicClass bc) {
		return true;
	}
	public int getLoginID(){
		return loginID;	
	}
	public void setLoginID(int loginID){
		this.loginID = loginID;	
	}
	public int getOrganisationID(){
		return 1;
	}
	public OrganisationMember getOrganisationMember(){
		return om;
	}
	public void setOrganisationMember(OrganisationMember om){
		this.om = om;
	}
	public int getStaffMemberID(){
		return 1;
	}
		

	
	public String getWordClientDocument(String tempID) {
		return wordClientDocuments.get(tempID);
	}
	public void registerWordClientDocument(String tempID, String objectPath) {
		wordClientDocuments.put(tempID, objectPath);
	}
	public void setMemberAdCategory(MemberAdCategory mac){
		this.mac = mac;
	}
	public MemberAdCategory getMemberAdCategory(){
		return mac;
	}
	//--------------------------------------------
	public void addMemberAdID(String id){
		if(getOrganisationMember() != null){
			getOrganisationMember().addMemberAdID(id);
		}
		/*
		if(memberadids.indexOf(id)==-1){
			memberadids.add(id);
		}
		*/
	}
	public void removeMemberAdID(String id){
		if(getOrganisationMember() != null){
			getOrganisationMember().removeMemberAdID(id);
		}
		/*
		if(memberadids.indexOf(id) > -1){
			memberadids.remove(id);
		}
		*/
	}
	public Vector getMemberAdIDs(){
		if(getOrganisationMember() != null){
			return getOrganisationMember().getMemberAdIDs();
		}
		else{
			return null;
		}
		//return memberadids;
	}
	public boolean hasMemberAdID(String id){
		if(getOrganisationMember() != null){
			return getOrganisationMember().hasMemberAdID(id);
		}
		else{
			return false;	
		}
		/*
		if(memberadids.indexOf(id)==-1){
			return false;
		}	
		else{
			return true;
		}
		*/
	}
	public void removeMemberAdIDs(){
		if(getOrganisationMember() != null){
			getOrganisationMember().removeMemberAdIDs();
		}
		//memberadids.clear();	
	}
	//--------------------------------------------------------
	public void clear(){
		parameters.clear();
	}
	public void put(String key, Object value){
		parameters.put(key, value);
	}
	public void remove(String key){
		parameters.remove(key);
	}
	public Object get(String key){
		return parameters.get(key);
	}
	public Map getParameters(){
		return parameters;	
	}
	public void setProfiles(Vector profiles){
		this.profiles = profiles;	
	}
	public Vector getProfiles(){
		return profiles;	
	}
	public Vector getAdvertisements(){
		return advertisements;	
	}
	public void setAdvertisements(Vector advertisements){
		this.advertisements = advertisements;
	}
	public void setCommercialsAds(String[][] commercialads){
		this.commercialads = commercialads;	
	}
	public String[][] getCommercialsAds(){
		return commercialads;
	}
	public String getPath(String path){
		if(getParent() != null){
			return getParent().getPath("/usersession" + path); 	
		}
		else{
			return "/" + getName() + path;
		}
	}
	public int getSelectedYear(){
		return selectedYear;	
	}
	public void setSelectedYear(int selectedYear){
		this.selectedYear = selectedYear;	
	}
	
	//-------------------------Merkliste---------------------------------------------------------------------
	
	public String getSelectedAds(){
		
		StringBuilder html = new StringBuilder();
		
		if(om != null){
		
			int size = getMemberAdIDs().size();
			

			
			ocs.logAccess("size : " + size);
			
			
			
			
			html.append("<table><tr>");
			html.append("<td  class=\"adselection1\">");
			html.append("Bestell-Liste"); 
			html.append("</td>");
			html.append("<td style=\"width : 36px; height : 36px; background-image : url('/res/icons/merken_aktiv.png'); text-align : center; vertical-align : middle;\">");
			html.append(size);
			html.append("</td>");
				
			html.append("</tr></table>");
			
			
			
			if(size > 0){
			
				String adids = "(";
				for(Object adid : getMemberAdIDs()){
					adids += adid + ",";
				}
						
				adids = adids.substring(0, adids.length()-1) + ")";
				
				String sql = "SELECT count(t1.ID) AS num, t2.ID AS Category, t2.Title FROM MemberAD AS t1 JOIN MemberAdCategory AS t2 ON t2.ID=t1.Template WHERE t1.ID IN " + adids + " GROUP BY t2.ID, t2.Title";
				
				ObjectCollection results = new ObjectCollection("Results", "*");
				ocs.queryData(sql, results);
				
				html.append("<div id=\"adselection_dropdown\">");
					
				html.append("<p class=\"dropdownheader1\">");
					
				html.append(size + " Kontakte<br />auf Bestellliste");
				
				html.append("</p>");
				
				html.append("<table>");
				
				for(BasicClass record : results.getObjects()){
					
					html.append("<tr><td><img src=\"/res/icons/" + record.getString("CATEGORY") + ".png\"></td><td class=\"adselection2\">" + record.getString("NUM") + "</td><td class=\"adselection2\">Kontakte</td></tr>");
				}
					
				
					
				html.append("</table>");	
				
				html.append("<input class=\"addressorderbutton\" type=\"button\" onclick=\"orderAddresses()\" value=\"zur Bestell-Liste\" style=\"left : 0px;\">");
					
				html.append("</div>");
				
			}
			
		
        }
		
		
		return html.toString();
		
		
		
	}

} 	
