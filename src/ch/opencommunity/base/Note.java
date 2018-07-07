package ch.opencommunity.base;

public class Note extends BasicOCObject{
	public Note(){
		setTablename("Note");
		addProperty("Content", "Text", "");
	}
	public void initObjectLocal(){
		addProperty("SortOrder", "Integer", "0");	
	}

} 
