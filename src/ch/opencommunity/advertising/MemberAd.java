package ch.opencommunity.advertising;


import ch.opencommunity.base.*;
import ch.opencommunity.server.*;
import ch.opencommunity.view.MemberAdView;

import org.kubiki.base.BasicClass;
import org.kubiki.base.ConfigValue;
import org.kubiki.application.ApplicationContext;
import org.kubiki.application.FileObjectData;
import org.kubiki.base.ActionResult;
import org.kubiki.base.UploadHandler;
import org.kubiki.database.TransactionHandler;
import org.kubiki.cms.CMS;
import org.kubiki.cms.ImageObject;

import java.util.Vector;

import org.apache.commons.fileupload.FileItem;

public class MemberAd extends BasicOCObject  implements UploadHandler{
	
	
	public MemberAd(){
		setTablename("MemberAd");
		
		addProperty("Identifier", "String", "", false, "Identifier", 30);
		
		addProperty("IsOffer", "Boolean", "false");
		addProperty("IsRequest", "Boolean", "false");
		addProperty("Type", "Integer", "1");
		addProperty("Category", "Integer", "");
		addProperty("Location", "String", "", false, "Location", 8);
		addProperty("Description", "Text", "");
		
		addProperty("Priority", "Integer", "0");

		addProperty("FeedbackStatus", "Integer", "0");
		addProperty("FeedbackForm", "Text", "");
		
		addProperty("ValidFrom", "DateTime", "");
		addProperty("ValidUntil", "DateTime", "");
		addProperty("DateReminder", "DateTime", "");
		
		addProperty("NotificationStatus", "Integer", "0");
		addProperty("NotificationMode", "Integer", "");
		
		addProperty("Code", "String", "", true, "Code", 20);
		
		addProperty("ExternalLink", "String", "", false, "Link", 255);
		
		addObjectCollection("MemberAdModification", "ch.opencommunity.advertising.MemberAdModification");
		addObjectCollection("Parameter", "ch.opencommunity.base.Parameter");
		addObjectCollection("Note", "ch.opencommunity.base.Note");
		addObjectCollection("Feedback", "ch.opencommunity.advertising.Feedback");
		
		addObjectCollection("Feedback", "ch.opencommunity.advertising.Feedback");
		
		addObjectCollection("ImageObject", "org.kubiki.cms.ImageObject");
		
	}
	public void initObjectLocal(){
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		getProperty("Template").setHidden(false);
		setProperty("Template", ocs.getMemberAdAdministration().getObjectByName("MemberAdCategory", "" + getID("Template")));
		
		Vector types = new Vector();
		types.add(new ConfigValue("0","0", "Angebot"));
		types.add(new ConfigValue("1","1", "Nachfrage"));
		types.add(new ConfigValue("2","2", "Tandem"));
		getProperty("Type").setSelection(types);
		
		Note note = (Note)getObjectByIndex("Note", 0);
		if(note != null){
			addProperty("AdditionalInfo", "Text", note.getString("Content"), false, "Zusatzinfo");	
		}
		
		addProperty("ImageUpload", "Object", "");
		
		ImageObject imageObject = (ImageObject)getObjectByIndex("ImageObject", 0);
		if(imageObject != null){
			
			CMS cms = (CMS)ocs.getApplicationModule("cms");
			
			cms.cacheImageObject(imageObject.getName() + "." + imageObject.getString("FileExtension"), imageObject);
			
			if(imageObject.getObjects("FileObjectData").size() > 0){
				FileObjectData fod = (FileObjectData)imageObject.getObjectByIndex("FileObjectData", 0);
				if(fod.getObject("FileData") instanceof byte[]){
					imageObject.setCache((byte[])fod.getObject("FileData"));	
				}
			}
			
			
		}
		
	}
	public String toString(){
		return getName() + "," + getString("DateCreated") + " " + getString("Title") + ", " + getString("Template");	
	}
	public MemberAd clone(ApplicationContext context){
		MemberAd ma = (MemberAd)createObject("ch.opencommunity.advertising.MemberAd", null, context);
		ma.mergeProperties(this);
		for(BasicClass parameter : getObjects("Parameter")){
			Parameter p = new Parameter();
			p.mergeProperties(parameter);
			p.addProperty("MeberAdID", "String", getName());
			ma.addSubobject("Parameter", p);
		}
		return ma;
	}
	public MemberAdCategory getCategory(){
		if(getObject("Template") instanceof MemberAdCategory){
			return (MemberAdCategory)getObject("Template");
		}
		else{
			return null;	
		}
	}
	public boolean hasParameter(int template){
		boolean hasParameter = false;
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		for(BasicClass bc : getObjects("Parameter")){
			
			ocs.logAccess(template + ":" + bc.getDouble("Value") + " size: " + getObjects("Parameter").size());
			if(template == bc.getDouble("Value")){
				hasParameter = true;	
			}
		}
		return hasParameter;	
	}
	public boolean hasParameter(int template, int value){
		boolean hasParameter = false;
		for(BasicClass bc : getObjects("Parameter")){
			if(template == bc.getID("Template")&& value == bc.getDouble("Value")){
				hasParameter = true;	
			}
		}
		return hasParameter;	
	}
	public Parameter getParameterByTemplate(int template){
		Parameter parameter = null;
		for(BasicClass bc : getObjects("Parameter")){
			if(template == bc.getID("Template")){
				parameter = (Parameter)bc;
			}
		}
		return parameter;	
	}
	public ActionResult onAction(BasicClass source, String command, ApplicationContext context) {
		
		ActionResult result = null;
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		if(command.equals("edit")){
			try{
				result = new ActionResult(ActionResult.Status.OK, "Inserat geladen");
				result.setParam("dataContainer", "objectEditArea");
				result.setData(MemberAdView.getMemberAdEditForm(this,true));
			}
			catch(java.lang.Exception e){
				ocs.logException(e);	
			}
			
		}		
		return result;	
	}

	public Object handleUpload(ApplicationContext context, FileItem fileItem){

		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		
		String filename = fileItem.getName();
		String[] args = filename.split("\\.");
		String fileExtension = args[args.length-1];
		
		ocs.logAccess("FileItem " + fileItem);
		
		ImageObject imageObject = (ImageObject)getObjectByIndex("ImageObject", 0);
		
		ActionResult result = null;
		
		if(imageObject == null){
			
			TransactionHandler transactionHandler = null;
			
			try{
				transactionHandler = ocs.startTransaction();
				imageObject = new ImageObject();
				imageObject.addProperty("MemberAdID", "String", getName());
				

				
				imageObject.setProperty("FileName", filename);
				imageObject.setProperty("FileExtension", fileExtension);
				
				String id = ocs.insertObject(imageObject);
				
				FileObjectData fileObjectData = new FileObjectData();
				fileObjectData.addProperty("ImageObjectID", "String", id);
				fileObjectData.setProperty("FileData", fileItem.get());
				fileObjectData.setProperty("FileName", filename);
				
				ocs.insertObject(fileObjectData);
				
				imageObject = (ImageObject)ocs.getObject(this, "ImageObject", "ID", id, false);
				
				transactionHandler.commitTransaction();
				
				ocs.getMemberAdAdministration().initCommercialAds(ocs.getDataStore());
				
				result = new ActionResult(ActionResult.Status.OK, "Bild hochgeladen");
				result.setParam("dataContainer", "imageobject");
				result.setData(filename);
				
				
			}
			catch(java.lang.Exception e){
				transactionHandler.rollbackTransaction();
				ocs.logException(e);				
			}
			
			
			
		}
		else{
			
			TransactionHandler transactionHandler = null;
			
			try{
				FileObjectData fileObjectData = (FileObjectData)imageObject.getObjectByIndex("FileObjectData", 0);
				
				if(fileObjectData != null){
					
					transactionHandler = ocs.startTransaction();
					
					imageObject.setProperty("FileName", filename);
					imageObject.setProperty("FileExtension", fileExtension);
					
					ocs.updateObject(transactionHandler, imageObject);
					
					fileObjectData.setProperty("FileData", fileItem.get());	
					fileObjectData.setProperty("FileName", filename);
					
					ocs.updateObject(transactionHandler, fileObjectData);
					
					transactionHandler.commitTransaction();
					
					ocs.getMemberAdAdministration().initCommercialAds(ocs.getDataStore());
					
					result = new ActionResult(ActionResult.Status.OK, "Bild hochgeladen");
					result.setParam("dataContainer", "imageobject");
					result.setData(filename);
					
				}
			
			}
			catch(java.lang.Exception e){
				transactionHandler.rollbackTransaction();
				ocs.logException(e);				
			}
			
		}
		
		return result;
		
	}
	
}
