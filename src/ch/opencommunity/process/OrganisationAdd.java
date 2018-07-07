package ch.opencommunity.process;

import ch.opencommunity.server.OpenCommunityServer;
import ch.opencommunity.base.OrganisationalUnit;
import ch.opencommunity.base.Address;
import ch.opencommunity.base.Contact;
import ch.opencommunity.base.Note;
import ch.opencommunity.dossier.*;

import org.kubiki.ide.BasicProcess;
import org.kubiki.ide.BasicProcessNode;
import org.kubiki.base.ProcessResult;
import org.kubiki.application.ApplicationContext;
import org.kubiki.database.TransactionHandler;



public class OrganisationAdd extends BasicProcess{
	
	public OrganisationAdd(){
		
		addNode(this);
		
		addProperty("Title", "String", "", false, "Bezeichnung");
		
		addProperty("Type", "Integer", "", false, "Typ");
		
		//addProperty("Description", "Text", "", false, "Beschreibung");
		addProperty("AdditionalLine", "Text", "", false, "Zusatzzeile");
		addProperty("POBox", "String", "", false, "Postfach");
		
		addProperty("Street", "String", "", false, "Strasse");
		addProperty("Number", "String", "", false, "Nummer");
		addProperty("Zipcode", "String", "", false, "PLZ");
		addProperty("City", "String", "", false, "Ort");
		
		addProperty("TelB", "String", "", false, "Telefon");
		addProperty("TelM", "String", "", false, "Telefon mobile");
		addProperty("Email", "String", "", false, "Email");
		//addProperty("Fax", "String", "", false, "Fax");
		addProperty("Web", "String", "", false, "Website");
		
		addProperty("Purpose", "Text", "", true, "Stiftungszweck");
		
		addProperty("CreateDossier", "Boolean", "true", false, "Dossier erstellen");
		
		setCurrentNode(this);
	}
	
	public void initProcess(){
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		getProperty("Type").setSelection(ocs.getInstitutionTypes());
	}
	
	public void finish(ProcessResult result, ApplicationContext context){
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		TransactionHandler transactionHandler = ocs.startTransaction();
		
		try{
			
			OrganisationalUnit ou = (OrganisationalUnit)ocs.createObject("ch.opencommunity.base.OrganisationalUnit", null, context);
			
			ou.setProperty("Title", getString("Title"));
			ou.setProperty("Type", getID("Type"));
			ou.setProperty("Description", getString("Description"));
			
			String ouid = ocs.insertSimpleObject(transactionHandler, ou);
			
			Address address = (Address)ocs.createObject("ch.opencommunity.base.Address", null, context);
			address.addProperty("OrganisationalUnitID", "String", ouid);
			
			address.setProperty("Street", getString("Street"));
			address.setProperty("Number", getString("Number"));
			address.setProperty("POBox", getString("POBox"));
			address.setProperty("AdditionalLine", getString("AdditionalLine"));
			address.setProperty("Zipcode", getString("Zipcode"));
			address.setProperty("City", getString("City"));
			ocs.insertSimpleObject(transactionHandler, address);
			
			String dossierid = null;
			
			if(getBoolean("CreateDossier")){
				Dossier dossier = (Dossier)ocs.createObject("ch.opencommunity.dossier.Dossier", null, context);
				dossier.setProperty("OrganisationalUnit", ouid);
				dossier.setProperty("Title", getString("Title"));
				
				dossierid = ocs.insertSimpleObject(transactionHandler, dossier);
				
				CaseRecord caseRecord = (CaseRecord)ocs.createObject("ch.opencommunity.dossier.CaseRecord", null, context);
				caseRecord.addProperty("DossierID", "String", dossierid);
				ocs.insertSimpleObject(transactionHandler, caseRecord);
				
				ObjectDetail objectDetail = (ObjectDetail)ocs.createObject("ch.opencommunity.dossier.ObjectDetail", null, context);
				objectDetail.addProperty("DossierID", "String", dossierid);
				
				int template = 11;
				if(getID("Type")==2){ 
					template = 13;	
				}
				else if(getID("Type")==3){ 
					template = 14;	
				}
				else if(getID("Type")==4){ 
					template = 15;	
				}
				else if(getID("Type")==5){ 
					template = 16;	
				}
				objectDetail.setProperty("Template", template);
				String detailid = ocs.insertSimpleObject(transactionHandler, objectDetail);
				
				if(template==11){
					Note note = (Note)ocs.createObject("ch.opencommunity.base.Note", null, context);
					note.addProperty("ObjectDetailID", "String", detailid);
					note.setProperty("Template", "33");
					note.setProperty("Content", getString("Purpose"));
					//ocs.insertSimpleObject(note);
				}
				
			}
			
			Contact contact = (Contact)ocs.createObject("ch.opencommunity.base.Contact", null, context);
			contact.addProperty("OrganisationalUnitID", "String", ouid);
			contact.setProperty("Type", 1);
			contact.setProperty("Value", getString("TelB"));
			ocs.insertSimpleObject(transactionHandler, contact);	
			
			contact = (Contact)ocs.createObject("ch.opencommunity.base.Contact", null, context);
			contact.addProperty("OrganisationalUnitID", "String", ouid);
			contact.setProperty("Type", 2);
			contact.setProperty("Value", getString("TelM"));
			ocs.insertSimpleObject(transactionHandler, contact);	
			
			contact = (Contact)ocs.createObject("ch.opencommunity.base.Contact", null, context);
			contact.addProperty("OrganisationalUnitID", "String", ouid);
			contact.setProperty("Type", 3);
			contact.setProperty("Value", getString("Email"));
			ocs.insertSimpleObject(transactionHandler, contact);
			
			/*
			contact = (Contact)ocs.createObject("ch.opencommunity.base.Contact", null, context);
			contact.addProperty("OrganisationalUnitID", "String", ouid);
			contact.setProperty("Type", 4);
			contact.setProperty("Value", getString("Fax"));
			ocs.insertSimpleObject(transactionHandler, contact);
			*/
			
			contact = (Contact)ocs.createObject("ch.opencommunity.base.Contact", null, context);
			contact.addProperty("OrganisationalUnitID", "String", ouid);
			contact.setProperty("Type", 5);
			contact.setProperty("Value", getString("Web"));
			ocs.insertSimpleObject(transactionHandler, contact);
		
			transactionHandler.commitTransaction();
			
			ocs.getObject(ocs, "OrganisationalUnit", "ID", ouid);
			
			if(getParent() instanceof DossierAdministration){
				//result.setParam("dataContainer", "organisationlist");
				//result.setData(((DossierAdministration)getParent()).getOrganisationList());
				if(dossierid != null){
					result.setParam("exec", "openDossier('" + dossierid + "')");
				}
			}
			
		}
		catch(java.lang.Exception e){
			transactionHandler.rollbackTransaction();		
			ocs.logException(e);
		}
		
		
	}


}