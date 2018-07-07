package ch.opencommunity.advertising;


import ch.opencommunity.base.*;
import ch.opencommunity.server.*;


public class MemberAdStatus extends BasicOCObject{
	public MemberAdStatus(){
		
		addProperty("ValidFrom", "DateTime", "");
		addProperty("ValidUntil", "DateTime", "");
	}
}