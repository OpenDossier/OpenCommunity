package ch.opencommunity.process;

import ch.opencommunity.server.OpenCommunityServer;

import org.kubiki.ide.BasicProcess;
import org.kubiki.ide.BasicProcessNode;
import org.kubiki.pdf.*;

import org.kubiki.base.Property;
import org.kubiki.base.ProcessResult;
import org.kubiki.base.ObjectCollection;
 
import org.kubiki.application.*;

public class PDFCreate extends BasicProcess{
	
	public PDFCreate(){
		
		BasicProcessNode node1 = addNode();
		
		Property p = addProperty("Type", "Integer", "", false, "Typ");
		
		p = addProperty("Year", "Integer", "", false, "Jahr");
		node1.addProperty(p);
		
		setCurrentNode(node1);
		
	}
	public void finish(ProcessResult result, ApplicationContext context){
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		
		PDFWriter pdfWriter = new PDFWriter();
		PDFTemplateLibrary templib = (PDFTemplateLibrary)ocs.getObjectByName("PDFTemplateLibrary", "1");
		
		String sql = "SELECT " + getString("Year") + " AS YEAR, 'CHF 2000' AS Amount, t3.*, t4.Street, t4.Number, t4.Zipcode, t4.City FROM OrganisationMember AS t1";
		sql += " JOIN Person AS t2 On t1.Person=t2.ID";
		sql += " JOIN Identity AS t3 ON t3.PersonID=t2.ID";
		sql += " JOIN Address AS t4 ON t4.PersonID=t2.ID";
		
		ObjectCollection results = new ObjectCollection("Results", "*");
		ocs.queryData(sql, results);
		String filename = ocs.getRootpath() + "/temp/test.pdf";
		
		pdfWriter.createPDF(filename, templib, "1", results.getObjects());
		
		result.setParam("download", "temp/test.pdf");
		
	}
	
	
}