package ch.opencommunity.process;
 
 import ch.opencommunity.server.*;
 import ch.opencommunity.base.*;
 import ch.opencommunity.advertising.*;
 
import org.kubiki.ide.*;
import org.kubiki.base.BasicClass;
import org.kubiki.base.Property;
import org.kubiki.base.ProcessResult;
import org.kubiki.base.ConfigValue;
import org.kubiki.cms.WebForm;
 
import org.kubiki.application.*;
 
import java.util.Vector;
import java.util.List;
import java.sql.*;


public class FeedbackPerson extends BasicClass{
	public FeedbackPerson(){
		
		addProperty("OrganisationMember", "Integer", "");
		addProperty("Status", "Integer", "0");
		
		addProperty("Familyname", "String", "");
		addProperty("Firstname", "String", "");	
		addProperty("Quality", "Integer", "1");	
		addProperty("Problems", "String", "");	
		addProperty("Highlights", "String", "");
		addProperty("Comment", "String", "");
	}
		
}