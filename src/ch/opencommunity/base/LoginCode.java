package ch.opencommunity.base;

public class LoginCode extends BasicOCObject{
	
	public LoginCode(){
		
		setTablename("LoginCode");
		addProperty("Code", "String", "", false, "Code", 20);	
		
	}
	
	
}