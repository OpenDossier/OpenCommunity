package ch.opencommunity.process;

import ch.opencommunity.server.OpenCommunityServer;
import ch.opencommunity.common.OpenCommunityUserSession;

import org.kubiki.ide.BasicProcess;
import org.kubiki.base.Property;
import org.kubiki.base.BasicClass;
import org.kubiki.base.ProcessResult;
import org.kubiki.base.ObjectCollection;
import org.kubiki.database.Record;
import org.kubiki.application.ApplicationContext;

import org.kubiki.util.DateConverter;

import org.kubiki.accounting.Cheque;

import org.kubiki.pdf.*;

import java.util.Vector;

public class ChequePrint extends BasicProcess{
	
	public ChequePrint(){
		addNode(this);
		
		Property p = addProperty("ChequeIDs", "String", "", false, "Serien-Nummern");
		
		addProperty("Width", "Integer", "16000");
		addProperty("Height", "Integer", "4400");
		addProperty("Margins", "String", "2500,2300,1600,1550");
		addProperty("Positions", "String", "155,52,369,17");


		setCurrentNode(this);
	}
	public void initProcess(ApplicationContext context){
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");	
		
		setProperty("ChequeIDs", userSession.getString("ChequeIDs"));
	}
	public void finish(ProcessResult result, ApplicationContext context){
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		
		OpenCommunityUserSession userSession = (OpenCommunityUserSession)context.getObject("usersession");
		
		String chequeids = getString("ChequeIDs");
					
		Vector ids = new Vector();
					
		String[] args = chequeids.split(",");
		for(String arg : args){
			if(arg.indexOf("-") > -1){
				try{
					String[] args2 = arg.split("-");
					if(args2.length==2){
						int lim1 = Integer.parseInt(args2[0].trim());	
						int lim2 = Integer.parseInt(args2[1].trim());	
						for(int i = lim1; i <= lim2; i++){
							ids.add(i);	
						}
					}
								
				}
				catch(java.lang.Exception e){
								
				}
			}
			else{
				ids.add(arg);
			}
		}
					
		String idlist = "";
		for(int i= 0; i < ids.size(); i++){
			if(i < ids.size()-1){
				idlist += ids.get(i) + ",";
			}
			else{
				idlist += ids.get(i);
			}
		}
		
		ObjectCollection results = new ObjectCollection("Results", "*");
		
		
		String sql = "SELECT t1.ID, t1.DateIssued, t2.ID AS OMID, t4.Familyname, t4.FirstName";
		sql += " FROM Cheque AS t1";
		sql += " LEFT JOIN OrganisationMember AS t2 ON t1.OrganisationMemberIssued=t2.ID";
		sql += " LEFT JOIN Person AS t3 ON t2.Person=t3.ID";
		sql += " LEFT JOIN Identity AS t4 ON t4.PersonID=t3.ID";
		
		if(idlist.length() > 0){
			sql += " WHERE t1.ID IN (" + idlist + ")";
		}
		sql += " ORDER BY t1.ID";
		
		ocs.queryData(sql, results);
		
		for(BasicClass record : results.getObjects()){
			String id = record.getString("ID");	
			int diff = 6 - id.length();
			for(int i = 0; i < diff; i++){
				id = "0" + id;	
			}
			record.setProperty("ID", "Nr. " + id);
		}
		
		PDFWriter pdfWriter = new PDFWriter();
		String filename = ocs.createPassword(10) + ".pdf";
		String path = ocs.getRootpath() + "/temp/" + filename;
		
		int[] margins = {0,0,0,0};
		
		try{
			args = getString("Margins").split(",");
			if(args.length==4){
				for(int i = 0; i < 4; i++){
					margins[i] = Integer.parseInt(args[i].trim());	
					ocs.logAccess(margins[i]);
				}
			}
		}
		catch(java.lang.Exception e){
			ocs.logException(e);
		}
		
		int[] positions = {130,60, 450, 20};
		
		try{
			args = getString("Positions").split(",");
			if(args.length==4){
				for(int i = 0; i < 4; i++){
					positions[i] = Integer.parseInt(args[i].trim());	
					ocs.logAccess(positions[i]);
				}
			}
		}
		catch(java.lang.Exception e){
			ocs.logException(e);			
		}
		
		ocs.logAccess(margins);
		ocs.logAccess(positions);
		
		String imgpath = ocs.getRootPath() + "/res/nachbarcheque_2018.png";
		
		pdfWriter.createLabels(path, this, margins, positions, imgpath, results.getObjects(), 1, 6, getInt("Width"), getInt("Height"));
		
		addProperty("filename", "String", "/temp/" + filename);
		
	}
}