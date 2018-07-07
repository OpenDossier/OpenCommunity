package ch.opencommunity.util;


import ch.opencommunity.base.*;
import ch.opencommunity.advertising.*;

import org.kubiki.database.*;
import org.kubiki.base.*;
import org.kubiki.util.*;
import org.kubiki.pdf.*;

import java.io.*;
import java.sql.*;
import java.util.*;


public class InvitationLetterCreate extends AbstractApplication{
	
	Connection con1 = null;
	Connection con2 = null;
	
	String rootpath = "";
	
	private DataStore dataStore;
	
	PDFTemplateLibrary templib;
	
	public InvitationLetterCreate(){
	
		addProperty("datastore", "String", "org.kubiki.database.PGSQLDataStore");		
		addProperty("dburl", "String", "jdbc:postgresql");	
		addProperty("dbname", "String", "nachbarnet_20160523");	
		addProperty("dbuser", "String", "postgres");	
		addProperty("dbpw", "String", "AmE,sadS.");	
		addProperty("readonlydbuser", "String", "queryuser");	
		addProperty("readonlydbpw", "String", "AmE,sadS.");	
		addProperty("rootpath", "String", "");	
		
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		try{
			
			File root = new File(".");
			rootpath = root.getAbsolutePath();
			rootpath = rootpath.replaceAll("\\.", "webapp");
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
		
		if(dataStore != null){
			dataStore.registerClass("org.kubiki.pdf.PDFTemplateLibrary");
			dataStore.registerClass("org.kubiki.pdf.PDFTemplate");
			dataStore.registerClass("org.kubiki.pdf.PDFTemplateParagraph");
			dataStore.registerClass("org.kubiki.pdf.PDFTemplateImage");
			dataStore.registerClass("org.kubiki.pdf.PDFTemplateFrame");
			dataStore.initDatabase();
		}
		
			
		con1 = ((SQLDataStore)dataStore).getConnection();
		
		templib = (PDFTemplateLibrary)getObject(null, "PDFTemplateLibrary", "ID", "1", false);
		templib.setParent(this);
		templib.initObjectLocal();
	}
	public void createLetters(){
		try{
			Vector letters = new Vector();
			PDFWriter pdfwriter = new PDFWriter();
			
			Statement stmt1 = con1.createStatement();	
			String sql = "SELECT t2.*, t3.*, t4.* FROM OrganisationMember AS t1";
			sql += " LEFT JOIN Person AS t2 ON t1.Person=t2.ID";
			sql += " LEFT JOIN Identity AS t3 ON t3.PersonID=t2.ID";
			sql += " LEFT JOIN Address AS t4 ON t4.PersonID=t2.ID";
			sql += " WHERE t1.Status=1";
			sql += " ORDER BY t4.Zipcode, t3.Familyname, t3.Firstname";
			ResultSet res = stmt1.executeQuery(sql);
			
			int cnt = 0;
			
			String content = openFile("../spendenaufruf/brieftext.txt", "iso-8859-1");
			
			while(res.next()){
			//while(res.next() && cnt < 10){
				
				Record letter = new Record();
				
				
				letter.addProperty("SUBJECT", "String", "Spendenaufruf");
				
				letter.addProperty("CONTENT", "String", content);
				
				String familyname = res.getString("FamilyName");
				letter.addProperty("FAMILYNAME", "String", familyname);
				
				String firstname = res.getString("FirstName");
				letter.addProperty("FIRSTNAME", "String", firstname);
				
				String street = res.getString("Street");
				letter.addProperty("STREET", "String", street);
				
				String number = res.getString("Number");
				letter.addProperty("NUMBER", "String", number);
				
				String zipcode = res.getString("ZipCode");
				letter.addProperty("ZIPCODE", "String", zipcode);
				
				String city = res.getString("City");
				letter.addProperty("CITY", "String", city);
				
				String sex = res.getString("Sex");
				String addressation = "Sehr geehrter Herr " + familyname;
				if(sex !=null && sex.equals("2")){
					addressation = "Sehr geehrte Frau " + familyname;
				}
				letter.addProperty("ADDRESSATION", "String", addressation);
				if(sex.equals("1") || sex.equals("2")){
					letters.add(letter);
				}
				
				System.out.println(cnt++);
			}
			String path = "../spendenaufruf/spendenaufruf.pdf";
			pdfwriter.createPDF(path, templib, "2", letters);
		}
		catch(java.lang.Exception e){
			e.printStackTrace();	
		}
	}
	
	public BasicClass getObject(BasicClass owner, String type, String keyname, String keyvalue){
				
		return 	getObject(owner, type, keyname, keyvalue, true);
		
	}
	public BasicClass getObject(BasicClass owner, String type, String keyname, String keyvalue, boolean initialize){
				
		return 	dataStore.getObject(owner, type, keyname, keyvalue, initialize);
		
	}
	public String getRootpath(){
		return rootpath;	
	}
	public static void main(String[] args){
		InvitationLetterCreate ic = new InvitationLetterCreate();	
		ic.createLetters();
		
	}
		
}