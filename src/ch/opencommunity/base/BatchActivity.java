package ch.opencommunity.base;

import org.kubiki.application.DataObject;

public class BatchActivity extends DataObject{
	
	public BatchActivity(){
		
		setTablename("BatchActivity");
		
		addProperty("Context", "Integer", "", false, "Kontext");
		
		addProperty("Parameters", "Text", "", false, "Parameter");
		
		addProperty("TextBlockID", "Integer", "", false, "Textblock");	
		
		addProperty("DateFrom", "Date", "", false, "Datum von");
		addProperty("DateTo", "Date", "", false, "Datum bis");		
		
		
	}
	
	
}