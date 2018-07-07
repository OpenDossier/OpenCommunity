package ch.opencommunity.base;


import ch.opencommunity.server.*;
import ch.opencommunity.base.*;
import ch.opencommunity.advertising.*;
 
import org.kubiki.ide.*;
import org.kubiki.base.Property;
import org.kubiki.base.ProcessResult;
import org.kubiki.base.ConfigValue;
 
import org.kubiki.application.*;
 
import java.util.Vector;
import java.sql.*;
 
 
public class FreeTextFeedback extends BasicOCObject{

	public FreeTextFeedback(){
		setTablename("FreeTextFeedback");	
		
		addProperty("FeedbackForm", "Text", "");
		
	}


}