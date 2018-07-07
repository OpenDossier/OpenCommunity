package ch.opencommunity.advertising;


import ch.opencommunity.base.*;
import ch.opencommunity.server.*;


public class MemberAdModification extends BasicOCObject{
	public MemberAdModification(){
		setTablename("MemberAdModification");
		addProperty("ValidFrom", "DateTime", "");
		addProperty("ValidUntil", "DateTime", "");
		
	}
}