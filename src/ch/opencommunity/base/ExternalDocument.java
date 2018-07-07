package ch.opencommunity.base;



public class ExternalDocument extends BasicOCObject{
	
	
	public ExternalDocument(){
		
		setTablename("ExternalDocument");
		addProperty("FileName", "String", "", false, "Dateiname", 255);	
		addProperty("FileData", "Bytea", "");
		
	}
	
	
}