package ch.opencommunity.util;

import ch.opencommunity.base.*;
import ch.opencommunity.advertising.*;
import ch.opencommunity.dossier.*;

import org.kubiki.database.*;
import org.kubiki.base.*;
import org.kubiki.util.*;
import org.kubiki.accounting.*;

import java.io.*;
import java.sql.*;
import java.util.*;

import jxl.*; 
import jxl.write.*;

public class OrganisationImport extends AbstractApplication{
	
	
	String rootpath = "";
	
	Connection con;
	
	private DataStore dataStore;
	
	public OrganisationImport(){
		
		addProperty("datastore", "String", "org.kubiki.database.PGSQLDataStore");		
		addProperty("dburl", "String", "jdbc:postgresql");	
		//addProperty("dbname", "String", "//127.0.0.1:15432/nachbarnet_test_20170405");	
		addProperty("dbname", "String", "//127.0.0.1:15432/nachbarnet_20170107");		
		//addProperty("dbname", "String", "//127.0.0.1:5432/nachbarnet_dev_20170405");	
		addProperty("dbuser", "String", "postgres");	
		addProperty("dbpw", "String", "AmE,sadS.");	
		addProperty("readonlydbuser", "String", "queryuser");	
		addProperty("readonlydbpw", "String", "AmE,sadS.");	
		addProperty("rootpath", "String", "");	
		
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		try{
			
			File root = new File(".");
			rootpath = root.getAbsolutePath();
			setProperty("rootpath", rootpath);
		}
		catch(java.lang.Exception e){}
		
		System.out.println(rootpath);
		
		try{
		    Class<?> c = Class.forName(getString("datastore"));
		    dataStore = (DataStore)c.newInstance();
		    dataStore.setParent(this);
		}
		catch(java.lang.Exception e){
			e.printStackTrace();
		}
		System.out.println(dataStore);
		
		dataStore.initDatabase();	
		con = ((SQLDataStore)dataStore).getConnection();
		
		try{
			Statement stmt = con.createStatement();
			
			stmt.execute("DROP TABLE Dossier");
			stmt.execute("DROP TABLE CaseRecord");
			stmt.execute("DROP TABLE ObjectDetail");
			
			stmt.close();
		}
		catch(java.lang.Exception e){
			e.printStackTrace();			
		}
		
		
		if(dataStore != null){

			dataStore.registerClass("ch.opencommunity.base.OrganisationalUnit");
			dataStore.registerClass("ch.opencommunity.base.Address");
			
			dataStore.registerClass("ch.opencommunity.dossier.Dossier");
			dataStore.registerClass("ch.opencommunity.dossier.CaseRecord");
			dataStore.registerClass("ch.opencommunity.dossier.ObjectDetail");
			
			dataStore.initDatabase();	
			
		}
	
	}
	public void importOrganisations(){
		
		try{			
			WorkbookSettings settings = new WorkbookSettings();
			settings.setEncoding("8859_1");
			
			File datafile = new File("../import/Stiftungen.xls");
							
			Workbook workbook = Workbook.getWorkbook(datafile, settings);	

			
			WritableCellFormat times;
			WritableFont times10pt = new WritableFont(WritableFont.TIMES, 10);
			times = new WritableCellFormat(times10pt);
			
			Sheet sheet = workbook.getSheet(0);
			
			boolean hasMoreLines = true;
			int cntLines = 1;
			int cnt = 0;
			Cell cell;
			
			Statement stmt = con.createStatement();
			
			while(hasMoreLines){
				String title = getFieldValue(sheet, 0, cntLines);
				title = title.trim();
				System.out.println(title);
				
				if(title==null || title.trim().length()==0){
					hasMoreLines = false;
				}
				else{
					
					int ouid = cntLines;
					boolean ouexists = false;
					ResultSet res = stmt.executeQuery("SELECT id, title  FROM OrganisationalUnit WHERE ID=" + ouid);
					while(res.next()){
						ouexists = true;
						System.out.println("Title : " + res.getString("Title"));
					}
					res.close();
					
					if(ouexists){
						stmt.execute("UPDATE OrganisationalUnit SET Title='" + title + "' WHERE ID=" + ouid);	
					}
					else{
						stmt.execute("INSERT INTO OrganisationalUnit (Title) Values ('" + title + "')");						
					}
					
					boolean addressexists = false;
					
					res = stmt.executeQuery("SELECT *  FROM Address WHERE OrganisationalUnitID=" + ouid);
					while(res.next()){
						addressexists = true;
					}
					res.close();
					
					System.out.println("address " + addressexists);
					
					if(!addressexists){
						stmt.execute("INSERT INTO Address (OrganisationalUnitID) Values (" + ouid + ")");
					}
					
					boolean dossierexists = false;
					
					res = stmt.executeQuery("SELECT *  FROM Dossier WHERE OrganisationalUnit=" + ouid);
					while(res.next()){
						dossierexists = true;
					}
					res.close();
					
					System.out.println("dossier " + dossierexists);
					
					if(!dossierexists){
						
						Dossier dossier = new Dossier();
						dossier.setProperty("OrganisationalUnit", "" + ouid);
						dossier.setProperty("Title", title);
						
						String dossierid = dataStore.insertSimpleObject(dossier);
						
						CaseRecord caseRecord = new CaseRecord();
						caseRecord.addProperty("DossierID", "String", dossierid);
						dataStore.insertSimpleObject(caseRecord);
						
						ObjectDetail objectDetail = new ObjectDetail();
						objectDetail.addProperty("DossierID", "String", dossierid);
						objectDetail.setProperty("Template", "11");
						dataStore.insertSimpleObject(objectDetail);
						
					}
						
				}
				
				
				
				cntLines++;
			}
		}
		catch(java.lang.Exception e){
			e.printStackTrace();
		}
		
	}
	public String getFieldValue(Sheet sheet, int column, int row){
		String value = null;
		try{	
			Cell cell = sheet.getCell(column, row);
			value = cell.getContents();
			if(value != null){
				value = value.trim();
			}
			
		}
		catch(java.lang.Exception e){
			e.printStackTrace();
		}	
		return value;
		
	}
	public static void main(String[] args){
		
		System.out.println("importing organisations ...");
		
		OrganisationImport oi = new OrganisationImport();	

		oi.importOrganisations();

		
	}
	
}