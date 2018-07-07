package ch.opencommunity.base;

import ch.opencommunity.server.OpenCommunityServer;

import org.kubiki.base.*;

import java.util.Vector;
import java.util.Hashtable;

public class FieldDefinition extends BasicOCObject{

	Vector<ConfigValue> codeList = null;
	Hashtable<String, String> codeMap;

	public FieldDefinition(){ 
		
		setTablename("FieldDefinition");
		
		addProperty("Type", "Integer", "");
		addProperty("IsMultiple", "Boolean", "false");
		addProperty("IsMandatory", "Boolean", "true");
		addProperty("TandemOnly", "Boolean", "false");
		addProperty("RefersTo", "Integer", "0");
		addProperty("CodeList", "Text", "");
		addProperty("Context", "Integer", "3");		
		addProperty("SortOrder", "Integer", "0");		
		addProperty("DocumentTemplate", "Integer", "0");	
		addProperty("AltTitle", "String", "", false, "Alternative Bezeichnung", 255);
		
	}
	public void initObjectLocal(){
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		
		codeList = new Vector();
		codeMap = new Hashtable<String, String>();
		String[] lines = getString("CodeList").split("\r\n|\r|\n");
		for(String line : lines){
			String[] args = line.split(";");
			if(args.length==2){
				codeList.add(new ConfigValue(args[0], args[0], args[1]));
				codeMap.put(args[0], args[1]);
			}
		}
		Vector types = new Vector();
		types.add(new ConfigValue("1","1", "Auswahlfeld"));
		types.add(new ConfigValue("2","2", "Mehrfachauswahl"));
		types.add(new ConfigValue("3","3", "Mehrfachauswahl"));
		types.add(new ConfigValue("4","4", "Notizfeld"));
		types.add(new ConfigValue("5","5", "Parameter"));
		types.add(new ConfigValue("6","6", "Dokument"));
		types.add(new ConfigValue("7","7", "Externes Dokument"));
		getProperty("Type").setSelection(types);
		
		getProperty("DocumentTemplate").setSelection(ocs.getTemplib(1).getObjects("DocumentTemplate"));
	}
	public Vector<ConfigValue> getCodeList(){
		return codeList;
	}
	public Hashtable<String, String> getCodeMap(){
		return codeMap;
	}
	public int getType(){
		return getID("Type");	
	}
}