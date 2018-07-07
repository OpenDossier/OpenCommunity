package ch.opencommunity.process;

import ch.opencommunity.server.OpenCommunityServer;
import ch.opencommunity.query.QueryDefinition;

import org.kubiki.ide.BasicProcess;
import org.kubiki.ide.BasicProcessNode;
import org.kubiki.base.ProcessResult;
import org.kubiki.base.BasicClass;
import org.kubiki.application.ApplicationContext;
import org.kubiki.application.server.WebApplicationClient;
import org.kubiki.database.TransactionHandler;
import org.kubiki.xml.XMLWriter;
import org.kubiki.util.DateConverter;

import java.util.Vector;

public class QueryDefinitionAdd extends BasicProcess{
	
	public QueryDefinitionAdd(){
		addNode(this);	
		
		addProperty("Title", "String", "(neue Abfrage)", false, "Bezeichnung");
		addProperty("Scope", "Integer", "1", false, "Scope");
		
		setCurrentNode(this);
	}
	public void finish(ProcessResult result, ApplicationContext context){
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		TransactionHandler transactionHandler = ocs.startTransaction();
		WebApplicationClient client = (WebApplicationClient)context.getObject("usersession");
		
		try{
			
			QueryDefinition queryDefinition = (QueryDefinition)ocs.createObject("ch.opencommunity.query.QueryDefinition", null, context);
			queryDefinition.mergeProperties(this);
			queryDefinition.setProperty("DateCreated", DateConverter.dateToSQL(new java.util.Date(), true));
			queryDefinition.setProperty("XML", XMLWriter.toXML(client));
			String id = ocs.insertObject(queryDefinition);
			
			ocs.getObject(ocs, "QueryDefinition", "ID", id);
			
			Vector queryDefinitions = new Vector();
			for(BasicClass bc : ocs.getObjects("QueryDefinition")){
				if(bc.getID("Scope")==1){
					queryDefinitions.add(bc);	
				}
			}
			client.getProperty("CurrentQueryDefinition").setSelection(queryDefinitions);
			
			queryDefinitions = new Vector();
			for(BasicClass bc : ocs.getObjects("QueryDefinition")){
				if(bc.getID("Scope")==2){
					queryDefinitions.add(bc);	
				}
			}
			client.getProperty("CurrentQueryDefinition2").setSelection(queryDefinitions);
			
			result.setParam("refresh", "currentsection");
		
		}
		catch(java.lang.Exception e){
			transactionHandler.rollbackTransaction();		
			ocs.logException(e);
		}
	}	
	
}