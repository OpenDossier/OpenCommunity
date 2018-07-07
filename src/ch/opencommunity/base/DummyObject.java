package ch.opencommunity.base;

public class DummyObject extends BasicOCObject {
	private String path;
	private boolean isActive;
	
	public DummyObject(String classtype, String path) {
		this.classtype = classtype;
		this.path = path; 
		isActive = true;
	}
	
	public String getPath(String dummy) {
		return path;
	}
	
	public boolean isActive() {
		return isActive;
	}
	
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
}
