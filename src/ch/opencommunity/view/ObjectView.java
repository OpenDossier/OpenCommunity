package ch.opencommunity.view;

public abstract class ObjectView {
	private String id;
	private String label;
	private boolean display;
	
	public ObjectView(String id, String label, boolean display) {
		this.id = id;
		this.label = label;
		this.display = display;
	}
	
	public String getId() {
		return id;
	}
	
	public String getLabel() {
		return label;
	}
	
	// Determines whether view is selectable via object tab.
	public boolean display() {
		return display;
	}
}
