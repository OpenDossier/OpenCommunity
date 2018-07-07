package ch.opencommunity.advertising;

import ch.opencommunity.server.OpenCommunityServer;

import org.kubiki.base.BasicClass;
import org.kubiki.base.ObjectCollection;
import org.kubiki.application.ApplicationModule;
import org.kubiki.application.ApplicationContext;
import org.kubiki.application.server.ApplicationServer;

import java.util.Vector;

public class Advertising extends ApplicationModule{
	
	ApplicationServer ewa;
	
	public Advertising(){

		addObjectCollection("Advertisement", "ch.opencommunity.advertising.Advertisement");
		
	}
	public void initObject(){
		ewa = (ApplicationServer)getRoot();
		super.initObject();
	}
	public String getAdministrationForm(ApplicationContext context){
		StringBuilder html = new StringBuilder();
		
		return html.toString();		
	}
	public Vector getAdvertisementList(){
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();

		String sql = "SELECT ID FROM Advertisement ORDER BY random() LIMIT 3";
		ObjectCollection results = new ObjectCollection("Results", "*");
		ocs.queryData(sql, results);
		

		
		Vector userads = new Vector();
		for(BasicClass o : results.getObjects()){
			Advertisement ad = (Advertisement)getObjectByName("Advertisement", o.getString("ID"));
			userads.add(ad);
		}
		return userads;
		
	}
	
}