package ch.opencommunity.process;

import ch.opencommunity.dossier.DossierAdministration;
import ch.opencommunity.view.OrganisationMemberList;
import ch.opencommunity.util.ExcelWriter;

import org.kubiki.base.ObjectCollection;
import org.kubiki.base.BasicClass;
import org.kubiki.base.ProcessResult;
import org.kubiki.database.DataStore;
import org.kubiki.ide.BasicProcess;
import org.kubiki.application.ApplicationContext;
import org.kubiki.application.server.ApplicationServer;



import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import java.io.File;

import jxl.*;
import jxl.write.*;

public class ListExport extends BasicProcess{
	
	ApplicationServer server;
	
	DossierAdministration dossierAdministration;
	OrganisationMemberList organisationMemberList;
	
	ObjectCollection results = null;
	
	List availableColumns;
	List selectedColumns;
	Map excludedColumns = null;
	Map selectedColumnsMap = null;
	
	String sql = null;
	
	public ListExport(){
		
		addNode(this);	
		
		addProperty("AvailableColumns", "Text", "");
		addProperty("SelectedColumns", "Text", "");
		
		results = addObjectCollection("Results", "*");
		
		setCurrentNode(this);
		
		availableColumns = new ArrayList();
		selectedColumns = new ArrayList();	
		
		excludedColumns = new HashMap();
		excludedColumns.put("name", "");
		excludedColumns.put("isDeletable", "");
		excludedColumns.put("displaySubelements", "");
		
	}
	public void initProcess(ApplicationContext context){
		
		server = (ApplicationServer)getRoot();
		
		if(getParent() instanceof DossierAdministration){
		
			dossierAdministration = (DossierAdministration)getParent();
			
			sql = dossierAdministration.getSQL(context);
			
		}
		if(getParent() instanceof OrganisationMemberList){
		
			organisationMemberList = (OrganisationMemberList)getParent();
			
			sql = organisationMemberList.getSQL(context);
			sql += organisationMemberList.getFilterString(context);
			server.logAccess(sql);
		}
		
		server.queryData(sql, results);
		
		server.logAccess(results.getObjects().size());
		
		for(int i = 0; i < 1; i++){
			
			BasicClass record = results.getObjects().get(i);
			List<String> propertyNames = record.getPropertySheet().getNames();
			
			for(String propertyName : propertyNames){
				
				if(excludedColumns.get(propertyName)==null){
					
					availableColumns.add(propertyName);
					
				}
				
			}
			
		}
		
		
	}
	public List getAvailableColumns(){
		return availableColumns;	
	}
	public List getSelectedColumns(){
		return selectedColumns;	
	}
	public boolean validate(ApplicationContext context){
		
		if(context.hasProperty("additem") && context.getString("additem").equals("true")){
			String item = context.getString("item");
			
			if(item != null){
				availableColumns.remove(item);
				selectedColumns.add(item);
			}
			return false;
		}
		else if(context.hasProperty("removeitem") && context.getString("removeitem").equals("true")){
			String item = context.getString("item");
			
			if(item != null){
				selectedColumns.remove(item);
				availableColumns.add(item);
			}
			return false;
		}
		else{
			return true;	
		}
		
	}
	public void finish(ProcessResult result, ApplicationContext context){
		try{
			
			DataStore dataStore = server.getDataStore();
			
			selectedColumnsMap = new HashMap();
			for(Object o : selectedColumns){
				selectedColumnsMap.put(o, "");	
			}
				
			WorkbookSettings settings = new WorkbookSettings();
				
			WritableCellFormat times;
			WritableFont times10pt = new WritableFont(WritableFont.TIMES, 10);
			times = new WritableCellFormat(times10pt);
					
			String filename = server.createPassword(8);
							
			String path = server.getRootPath() + "/temp/" + filename + ".xls";
					
			File file = new File(path);
								
			WritableWorkbook workbook = Workbook.createWorkbook(file, settings);	
								
			workbook.createSheet("Spendenliste", 0);
								
			WritableSheet sheet = workbook.getSheet(0);	
			
			if(dossierAdministration != null){
				sql = dossierAdministration.getSQL(context);
			}
			else if(organisationMemberList != null){
				//sql = organisationMemberList.getSQL(context);	
			}
			
			
			//server.queryData(sql, results);
			
			int row = 0;
			int column = 0;
			
			for(int i = 0; i < results.getObjects().size(); i++){
				
				BasicClass record = results.getObjects().get(i);
				List<String> propertyNames = record.getPropertySheet().getNames();
				
				if(i == 0){
					
					for(String propertyName : propertyNames){
						
						if(selectedColumnsMap.get(propertyName)!=null){
							
							String label = dataStore.getFieldLabel(propertyName);
							if(label != null){						
								ExcelWriter.addCell(sheet, column, row, label, times);
							}
							else{
								ExcelWriter.addCell(sheet, column, row, propertyName, times);
							}
							
							column++;
							
						}
						
					}
					
					row++;
				}
				
				column = 0;
				
				for(String propertyName : propertyNames){
					
					if(selectedColumnsMap.get(propertyName)!=null){
						ExcelWriter.addCell(sheet, column, row, record.getString(propertyName), times);

						
						column++;
						
					}
					
				}
				
				row++;
				
			}
			
			workbook.write();
			workbook.close();
					
			result.setParam("download", "/temp/" + filename + ".xls");
			
		}
		catch(java.lang.Exception e){
			server.logException(e);
		}
			
		
	}

	
	
	
}