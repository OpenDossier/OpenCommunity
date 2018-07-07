package ch.opencommunity.process;
 
import ch.opencommunity.server.*;
import ch.opencommunity.base.*;

 
import org.kubiki.ide.*;
import org.kubiki.base.Property;
import org.kubiki.base.ProcessResult;
 
import org.kubiki.application.*;

import org.kubiki.accounting.*;
 
 
public class AccountAdd extends BasicProcess{
 
	BasicProcessNode node1;
 
	public AccountAdd(){
		node1 = addNode();
		
		Property p = addProperty("AccountNumber", "String", "", false, "Kontennummer");
		node1.addProperty(p);
		
		p = addProperty("Title", "String", "", false, "Kontenbezeichnung");
		node1.addProperty(p);
		
		setCurrentNode(node1);		
	}
	public void finish(ProcessResult result, ApplicationContext context){
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		Accounting accounting = ocs.getAccounting();
		Account account = new Account();
		account.mergeProperties(this);
		String id = ocs.insertObject(account);
		ocs.getObject(accounting, "Account", "ID", id);
		result.setParam("refresh", "accounting");
		
	}
}
