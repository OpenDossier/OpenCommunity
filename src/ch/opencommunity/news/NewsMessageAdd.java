package ch.opencommunity.news;
 
import ch.opencommunity.server.*;
import ch.opencommunity.base.*;
import ch.opencommunity.advertising.*;
import ch.opencommunity.common.*;
 
import org.kubiki.ide.*;
import org.kubiki.base.Property;
import org.kubiki.base.ProcessResult;
import org.kubiki.base.ObjectCollection;
import org.kubiki.base.BasicClass;
import org.kubiki.base.ConfigValue;
import org.kubiki.database.Record;
import org.kubiki.util.DateConverter;
 
import org.kubiki.application.*;
 
import java.util.Vector;
 
 
public class NewsMessageAdd extends BasicProcess{
	
	BasicProcessNode node1;
	
	public NewsMessageAdd(){
		node1 = addNode();

		Property p = addProperty("Title", "String", "", false, "Titel");
		node1.addProperty(p);
		p = addProperty("DateStart", "DateTime", "", false, "Datum Von");
		node1.addProperty(p);
		p = addProperty("DateEnd", "DateTime", "", false, "Datum bis");	
		node1.addProperty(p);
		p = addProperty("Type", "Integer", "", false, "Typ");
		node1.addProperty(p);
		p = addProperty("Description", "Text",  "", false, "Beschreibung");
		node1.addProperty(p);
		p = addProperty("URL", "String",  "", false, "Link");	
		node1.addProperty(p);
		
		setCurrentNode(node1);
	}
	public void initProcess(){
		Vector types = new Vector();
		types.add(new ConfigValue("1", "1", "Mitteilung"));
		types.add(new ConfigValue("2", "2", "Veranstaltung"));
		types.add(new ConfigValue("3", "3", "Presse"));
		getProperty("Type").setSelection(types);
	}
	public void finish(ProcessResult result, ApplicationContext context){
		
 	 	OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
 	 	NewsAdministration news = ocs.getNewsAdministration();
 	 	NewsMessage message = (NewsMessage)news.createObject("ch.opencommunity.news.NewsMessage", null, context);
 	 	message.addProperty("NewsAdministrationID", "String", "1");
 	 	message.mergeProperties(this);
 	 	String id = ocs.insertObject(message);
 	 	ocs.getObject(news, "NewsMessage", "ID", id);
 	 	result.setParam("refresh", "calendar");
 	}
	
	
}