package ch.opencommunity.util;

import ch.opencommunity.base.*;
import ch.opencommunity.advertising.*;

import org.kubiki.database.*;
import org.kubiki.base.*;
import org.kubiki.util.*;
import org.kubiki.accounting.*;

import java.io.*;
import java.sql.*;
import java.util.*;

import jxl.*; 
import jxl.write.*;

public class PaymentsImport extends AbstractApplication{
	
	
	String rootpath = "";
	
	Connection con;
	
	private DataStore dataStore;
	
	public PaymentsImport(){
		
		addProperty("datastore", "String", "org.kubiki.database.PGSQLDataStore");		
		addProperty("dburl", "String", "jdbc:postgresql");	
		addProperty("dbname", "String", "//127.0.0.1:15432/nachbarnet_20160602");	
		//addProperty("dbname", "String", "//127.0.0.1:5432/nachbarnet_dev_20160809");	
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
			stmt.execute("DROP TABLE AccountMovement");

		}
		catch(java.lang.Exception e){
			e.printStackTrace();			
		}
		
		
		if(dataStore != null){

			dataStore.registerClass("org.kubiki.accounting.Account");
			dataStore.registerClass("org.kubiki.accounting.AccountMovement");
			
			dataStore.initDatabase();	
			
		}
	
	}
	public void importPayments(){
		
		try{			
			WorkbookSettings settings = new WorkbookSettings();
			settings.setEncoding("8859_1");
			
			File datafile = new File("../import/Spendenverzeichnis.xls");
							
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
				String omid = getFieldValue(sheet, 0, cntLines);	
				if(omid==null || omid.trim().length()==0){
					hasMoreLines = false;
				}
				else{
					System.out.println(omid);
					
					String a2013 = getFieldValue(sheet, 12, cntLines);	
					//System.out.println(a2013);
					if(a2013 != null && a2013.trim().length() > 0){
						AccountMovement am = new AccountMovement();
						am.setProperty("DebitAccount", "2");
						am.setProperty("CreditAccount", "1");
						am.setProperty("DateCreated", "2016-08-09");
						am.setProperty("Amount", a2013);
						am.setProperty("Date", "2013-07-01");
						am.setProperty("OrganisationMember", omid);
						am.setProperty("Description", "Spenden 2013");
						dataStore.insertSimpleObject(am);
					}
					
					String a2014 = getFieldValue(sheet, 13, cntLines);	
					//System.out.println(a2014);
					if(a2014 != null && a2014.trim().length() > 0){
						AccountMovement am = new AccountMovement();
						am.setProperty("DebitAccount", "2");
						am.setProperty("CreditAccount", "1");
						am.setProperty("DateCreated", "2016-08-09");
						am.setProperty("Amount", a2014);
						am.setProperty("Date", "2014-07-01");
						am.setProperty("OrganisationMember", omid);
						am.setProperty("Description", "Spenden 2014");
						dataStore.insertSimpleObject(am);
					}
					
					String a2015 = getFieldValue(sheet, 14, cntLines);	
					//System.out.println(a2015);
					if(a2015 != null && a2015.trim().length() > 0){
						AccountMovement am = new AccountMovement();
						am.setProperty("DebitAccount", "2");
						am.setProperty("CreditAccount", "1");
						am.setProperty("DateCreated", "2016-08-09");
						am.setProperty("Amount", a2015);
						am.setProperty("Date", "2015-07-01");
						am.setProperty("OrganisationMember", omid);
						am.setProperty("Description", "Spenden 2015");
						dataStore.insertSimpleObject(am);
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
		
		PaymentsImport pi = new PaymentsImport();	

		pi.importPayments();

		
	}
	
}