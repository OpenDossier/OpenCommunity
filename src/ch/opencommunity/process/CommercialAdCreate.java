package ch.opencommunity.process;

import ch.opencommunity.advertising.MemberAd;
import ch.opencommunity.base.OrganisationMember;
import ch.opencommunity.base.OrganisationMemberController;

import org.kubiki.ide.BasicProcess;
import org.kubiki.base.ProcessResult;
import org.kubiki.application.ApplicationContext;
import org.kubiki.application.server.ApplicationServer;

public class CommercialAdCreate extends BasicProcess{
	
	
	ApplicationServer server;
	
	OrganisationMember om;
	OrganisationMemberController omc;
	
	
	public CommercialAdCreate(){
		
		addNode(this);
		addProperty("Title", "String", "", false, "Bezeichnung");	
		addProperty("ExternalLink", "String", "", false, "Link");	
		addProperty("ValidFrom", "Date", "", false, "Gültig von");
		addProperty("ValidUntil", "Date", "", false, "Gültig bis");
		
		
		setCurrentNode(this);
		
	}
	public void initProcess(){
		
		server = (ApplicationServer)getRoot();
		omc = (OrganisationMemberController)getParent();
		om = omc.getOrganisationMember();
		
	}
	
	@Override
	public void finish(ProcessResult result, ApplicationContext context){
		
		MemberAd memberad = new MemberAd();
		memberad.mergeProperties(this);
		memberad.addProperty("OrganisationMemberID", "String", om.getName());
		
		String id = server.insertSimpleObject(memberad);
		
		server.getObject(om, "MemberAd", "ID", id);
		
		result.setParam("objectlist", omc.getPath());
	}
	
	
	
}
