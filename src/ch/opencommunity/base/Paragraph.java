package ch.opencommunity.base;

import ch.opencommunity.server.OpenCommunityServer;

import org.kubiki.database.Record;

public class Paragraph extends BasicOCObject{
	
	public Paragraph(){
		setTablename("Paragraph");
		addProperty("Content", "FormattedText", "");
		addProperty("SortOrder", "Integer", "", true, "");
		addProperty("ShowTitle", "Boolean", "true", true, "");
		getProperty("name").setHidden(true);
		setTitle("Neuer Abschnitt");
	
	}
	public Paragraph(String id) {
		this();
		setName(id);
	}
	public String getContent() {
		return getProperty("Content").getValue();
	}
	
	public void setContent(String content) {
		setProperty("Content", content);
	}
	public void fillContent(String title, String content) {
		OpenCommunityServer ods = (OpenCommunityServer)getRoot();
		setTitle(title);
		setContent(content);
		// Foreign key
		Record parent = (Record)getParent();
		addProperty(parent.getTablename() + "ID", "String", parent.getName());
		initObjectLocal();
		String id = ods.insertObject(this);
		setName(id);
	}
	public String getId() {
		return getName();
	}

} 
