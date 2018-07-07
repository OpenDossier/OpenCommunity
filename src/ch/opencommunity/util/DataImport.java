package ch.opencommunity.util;

import ch.opencommunity.base.*;
import ch.opencommunity.advertising.*;

import org.kubiki.database.*;
import org.kubiki.base.*;
import org.kubiki.util.*;

import java.io.*;
import java.sql.*;
import java.util.*;

import jxl.*; 
import jxl.write.*;

public class DataImport extends AbstractApplication{
	
	Connection con1 = null; //Postgres
	Connection con2 = null; //MySQL
	
	String rootpath = "";
	
	private DataStore dataStore;
	public DataImport(){
		
		addProperty("datastore", "String", "org.kubiki.database.PGSQLDataStore");		
		addProperty("dburl", "String", "jdbc:postgresql");	
		//addProperty("dbname", "String", "//127.0.0.1:15432/nachbarnet_20160602");	
		addProperty("dbname", "String", "//127.0.0.1:5432/nachbarnet_20160605");	
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
		con1 = ((SQLDataStore)dataStore).getConnection();
		
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			con2 = DriverManager.getConnection("jdbc:mysql://192.168.0.166:3306/nachbarnet_20160602", "root", "AmE,sadS.");
		}
		catch(java.lang.Exception e){
			e.printStackTrace();
		}
		


		
		/*
		
		try{
			Statement stmt = con1.createStatement();
			stmt.execute("DROP TABLE OrganisationMember");
			stmt.execute("DROP TABLE Person");	
			stmt.execute("DROP TABLE Identity");	
			stmt.execute("DROP TABLE Address");
			stmt.execute("DROP TABLE Login");
			stmt.execute("DROP TABLE MemberRole");
			stmt.execute("DROP TABLE Contact");		
			
			stmt.execute("DROP TABLE MemberAd");
			stmt.execute("DROP TABLE MemberAdRequest");	
			stmt.execute("DROP TABLE Parameter");	
			
			stmt.execute("DROP TABLE Activity");
			stmt.execute("DROP TABLE Note");
			stmt.execute("DROP TABLE OrganisationMemberRelationship");
		}
		catch(java.lang.Exception e){
			
		}
		
		*/
		
		if(dataStore != null){

			dataStore.registerClass("ch.opencommunity.base.OrganisationalUnit");
			dataStore.registerClass("ch.opencommunity.base.OrganisationMember");
			dataStore.registerClass("ch.opencommunity.base.OrganisationMemberRelationship");
			dataStore.registerClass("ch.opencommunity.base.Person");	
			dataStore.registerClass("ch.opencommunity.base.Identity");	
			dataStore.registerClass("ch.opencommunity.base.Address");	
			dataStore.registerClass("ch.opencommunity.base.Login");
			dataStore.registerClass("ch.opencommunity.base.MemberRole");
			dataStore.registerClass("ch.opencommunity.base.Contact");
			dataStore.registerClass("ch.opencommunity.base.Parameter");
			
			dataStore.registerClass("ch.opencommunity.base.Activity");
			dataStore.registerClass("ch.opencommunity.base.Note");
			
			dataStore.registerClass("ch.opencommunity.advertising.MemberAd");
			dataStore.registerClass("ch.opencommunity.advertising.MemberAdRequest");
			
			dataStore.initDatabase();	
		}
		
	}
	public void importMembers(){
		
		try{
			

			java.util.Date now = new java.util.Date();
			
			Statement stmt = con2.createStatement();
			
			String sql = " select distinct t1.* from t_benutzer as t1";
			sql += " left join t_nachfragen as t2 on t2.nachfragen_benutzer_id=t1.benutzer_id";
			sql += " left join t_vermittlungen AS t3 on t3.vermittlungen_benutzer_id=t1.benutzer_id";
			sql += " where  from_unixtime(t2.nachfragen_end_datum) >='2014-01-01' or from_unixtime(t3.vermittlungen_datum) >= '2013-01-01' LIMIT 10000";
			
			ResultSet res = stmt.executeQuery(sql);
			
			int cnt = 0;
			
			//while(res.next() && cnt < 10){
			while(res.next()){
				
				System.out.println(cnt++);
				

				String id = res.getString("benutzer_id");
				
				Person person = new Person();
				String personid = insertSimpleObject(person);
				
				String familyname = res.getString("benutzer_name");
				String firstname = res.getString("benutzer_vorname");
				String dateofbirth = res.getString("benutzer_jahrgang");
				String sex = res.getString("benutzer_geschlecht");
				
				if(sex.equals("w")){
					sex = "2";	
				}
				else{
					sex = "1";	
				}
				
				Identity identity = new Identity();
				identity.addProperty("PersonID", "String", personid);
				identity.setProperty("FamilyName", familyname);
				identity.setProperty("FirstName", firstname);
				identity.setProperty("DateOfBirth", dateofbirth);
				identity.setProperty("Sex", sex);
				insertSimpleObject(identity);
				
				
				
				String street = res.getString("benutzer_strasse");
				String number = res.getString("benutzer_str_nr");
				String zipcode = res.getString("benutzer_plz");
				String city = res.getString("benutzer_ort");
				
				Address address = new Address();
				address.addProperty("PersonID", "String", personid);
				address.setProperty("Street", street);
				address.setProperty("Number", number);
				address.setProperty("Zipcode", zipcode);
				address.setProperty("City", city);
				insertSimpleObject(address);
				

				
				String tel_p = res.getString("benutzer_tel_privat");
				if(tel_p.trim().length() > 0){
					Contact c = new Contact();
					c.addProperty("PersonID", "String", personid);
					c.setProperty("Type", 0);
					c.setProperty("Value", tel_p);
					insertSimpleObject(c);
				}
				
				String tel_g = res.getString("benutzer_tel_firma");
				if(tel_g.trim().length() > 0){
					Contact c = new Contact();
					c.addProperty("PersonID", "String", personid);
					c.setProperty("Type", 1);
					c.setProperty("Value", tel_g);
					insertSimpleObject(c);
				}
				
				String tel_m = res.getString("benutzer_tel_mobil");
				if(tel_m.trim().length() > 0){
					Contact c = new Contact();
					c.addProperty("PersonID", "String", personid);
					c.setProperty("Type", 2);
					c.setProperty("Value", tel_m);
					insertSimpleObject(c);
				}
				
				String email = res.getString("benutzer_email");
				if(email.trim().length() > 0){
					Contact c = new Contact();
					c.addProperty("PersonID", "String", personid);
					c.setProperty("Type", 3);
					c.setProperty("Value", email);
					insertSimpleObject(c);
				}
				
				OrganisationMember om = new OrganisationMember();
				om.addProperty("OrganisationalUnitID", "String", "1");
				om.setProperty("Person", personid);
				om.setProperty("Identifier", id);
				om.setProperty("Status", 1);
				String omid = insertSimpleObject(om);
				
				MemberRole mr = new MemberRole();
				mr.addProperty("OrganisationMemberID",  "String", omid);
				mr.setProperty("Role", 1);
				
				insertSimpleObject(mr);
				
				String password = res.getString("benutzer_passwort");
				String username = omid;
				while(username.length() < 7){
					username = "0" + username;
				}
				Login login = new Login();
				login.addProperty("OrganisationMemberID",  "String", omid);
				login.setProperty("Username", username);
				login.setProperty("Password", password);
				insertSimpleObject(login);
				
				//--------------------------------------------------------------------------------
				


			}
			

			
		}
		catch(java.lang.Exception e){
			e.printStackTrace();
		}
		
		
	}
	public void updateMembers(){
		try{
			

			java.util.Date now = new java.util.Date();
			
			Statement stmt = con2.createStatement();
			
			String sql = " select distinct t1.* from t_benutzer as t1";
			sql += " left join t_nachfragen as t2 on t2.nachfragen_benutzer_id=t1.benutzer_id";
			sql += " left join t_vermittlungen AS t3 on t3.vermittlungen_benutzer_id=t1.benutzer_id";
			sql += " where  from_unixtime(t2.nachfragen_end_datum) >='2014-01-01' or from_unixtime(t3.vermittlungen_datum) >= '2013-01-01' order by t1.benutzer_id LIMIT 10000";
			
			ResultSet res = stmt.executeQuery(sql);
			
			int cnt1 = 0;
			int cnt2 = 0;
			int cnt3 = 0;
			
			//while(res.next() && cnt < 10){
			while(res.next()){
				
				System.out.println(cnt1++);
				

				String id = res.getString("benutzer_id");
				
				boolean  exists = false;
				
				Statement stmt2 = con1.createStatement();
				ResultSet res2 = stmt2.executeQuery("SELECT * FROM OrganisationMember WHERE Identifier=\'" + id + "\'");
				while(res2.next()){
					exists = true;	
				}
				if(exists){
					System.out.println("In database : " + (cnt2++));
				}
				else{
					
					System.out.println("New : " + (cnt3++));
					
					Person person = new Person();
					String personid = insertSimpleObject(person);
					
					String familyname = res.getString("benutzer_name");
					String firstname = res.getString("benutzer_vorname");
					String dateofbirth = res.getString("benutzer_jahrgang");
					String sex = res.getString("benutzer_geschlecht");
					
					if(sex.equals("w")){
						sex = "2";	
					}
					else{
						sex = "1";	
					}
					
					Identity identity = new Identity();
					identity.addProperty("PersonID", "String", personid);
					identity.setProperty("FamilyName", familyname);
					identity.setProperty("FirstName", firstname);
					identity.setProperty("DateOfBirth", dateofbirth);
					identity.setProperty("Sex", sex);
					insertSimpleObject(identity);
					
					
					
					String street = res.getString("benutzer_strasse");
					String number = res.getString("benutzer_str_nr");
					String zipcode = res.getString("benutzer_plz");
					String city = res.getString("benutzer_ort");
					
					Address address = new Address();
					address.addProperty("PersonID", "String", personid);
					address.setProperty("Street", street);
					address.setProperty("Number", number);
					address.setProperty("Zipcode", zipcode);
					address.setProperty("City", city);
					insertSimpleObject(address);
					
	
					
					String tel_p = res.getString("benutzer_tel_privat");
					if(tel_p.trim().length() > 0){
						Contact c = new Contact();
						c.addProperty("PersonID", "String", personid);
						c.setProperty("Type", 0);
						c.setProperty("Value", tel_p);
						insertSimpleObject(c);
					}
					
					String tel_g = res.getString("benutzer_tel_firma");
					if(tel_g.trim().length() > 0){
						Contact c = new Contact();
						c.addProperty("PersonID", "String", personid);
						c.setProperty("Type", 1);
						c.setProperty("Value", tel_g);
						insertSimpleObject(c);
					}
					
					String tel_m = res.getString("benutzer_tel_mobil");
					if(tel_m.trim().length() > 0){
						Contact c = new Contact();
						c.addProperty("PersonID", "String", personid);
						c.setProperty("Type", 2);
						c.setProperty("Value", tel_m);
						insertSimpleObject(c);
					}
					
					String email = res.getString("benutzer_email");
					if(email.trim().length() > 0){
						Contact c = new Contact();
						c.addProperty("PersonID", "String", personid);
						c.setProperty("Type", 3);
						c.setProperty("Value", email);
						insertSimpleObject(c);
					}
					
					OrganisationMember om = new OrganisationMember();
					om.addProperty("OrganisationalUnitID", "String", "1");
					om.setProperty("Person", personid);
					om.setProperty("Identifier", id);
					om.setProperty("Status", 1);
					String omid = insertSimpleObject(om);
					
					MemberRole mr = new MemberRole();
					mr.addProperty("OrganisationMemberID",  "String", omid);
					mr.setProperty("Role", 1);
					
					insertSimpleObject(mr);
					
					String password = res.getString("benutzer_passwort");
					String username = omid;
					while(username.length() < 7){
						username = "0" + username;
					}
					Login login = new Login();
					login.addProperty("OrganisationMemberID",  "String", omid);
					login.setProperty("Username", username);
					login.setProperty("Password", password);
					insertSimpleObject(login);
					
				}
				
				//--------------------------------------------------------------------------------
				


			}
			System.out.println(cnt1 + "; " + cnt2 + "; " + cnt3);

			
		}
		catch(java.lang.Exception e){
			e.printStackTrace();
		}
	}
	public void updateMembers2(){
		
		try{		
			String sql = "SELECT ID, Identifier, Person FROM OrganisationMember";
			Statement stmt = con1.createStatement();
			ResultSet res = stmt.executeQuery(sql);
			while(res.next()){
				
				String omid = res.getString("ID");
				String id = res.getString("Identifier");
				String personid = res.getString("Person");
					
				System.out.println(omid);
				
				
				
				Statement stmt2 = con2.createStatement();
				
				if(id.length() > 0){

					sql = "SELECT  from_unixtime(benutzer_reg_datum) AS DateCreated FROM t_benutzer WHERE benutzer_id=" + id;
					ResultSet res2 = stmt2.executeQuery(sql);
					while(res2.next()){
						
						//String sex = res2.getString("benutzer_geschlecht");
						String datecreated = res2.getString("DateCreated");
						
						System.out.println(datecreated);
						
						/*
						if(sex.equals("w")){
							sex = "2";	
						}
						else{
							sex = "1";	
						}
						*/
						
						Statement stmt3 = con1.createStatement();
						//stmt3.execute("UPDATE Identity SET Sex= " + sex + " WHERE PersonID=" + personid);
						stmt3.execute("UPDATE OrganisationMember SET DateCreated='" + datecreated + "',   DateModified='" + datecreated + "' WHERE ID=" + omid);
						
					}
				
				}
				
			}
		}
		catch(java.lang.Exception e){
			e.printStackTrace();	
		}
		
		
	}
	public void importMemberAds(){
		
		Hashtable importedAds = new Hashtable();
		java.util.Date now = new java.util.Date();
		
		try{
			Statement stmt = con1.createStatement();			
			stmt.execute("DROP TABLE MemberAd");
			stmt.execute("DROP TABLE MemberAdRequest");	
			stmt.execute("DROP TABLE Parameter");	
			
			dataStore.initDatabase();
		}
		catch(java.lang.Exception e){
			
		}
		try{		
			String sql = "SELECT ID, Identifier FROM OrganisationMember ORDER BY ID";
			Statement stmt = con1.createStatement();
			ResultSet res = stmt.executeQuery(sql);
			while(res.next()){
				
				String omid = res.getString("ID");
				String id = res.getString("Identifier");
					
				System.out.println(omid);
			
				if(id.length() > 0){
			
					Statement stmt2 = con2.createStatement();
					sql = "SELECT * , from_unixtime(nachfragen_datum) AS validfrom, from_unixtime(nachfragen_end_datum) AS validuntil FROM t_nachfragen AS t1 WHERE t1.nachfragen_benutzer_id=" + id;
					sql += " AND from_unixtime(nachfragen_end_datum) >= '2013-01-01'";
					ResultSet res2 = stmt2.executeQuery(sql);
					while(res2.next()){
						String oldid = res2.getString("nachfragen_id");
						
						String validfrom = res2.getString("validfrom");
						String validuntil = res2.getString("validuntil");
						
						System.out.println(validuntil);
						
						MemberAd ma = new MemberAd();
						ma.addProperty("OrganisationMemberID", "String", omid);
						ma.setProperty("ValidFrom", validfrom);
						
						validuntil = validuntil.substring(0,10);
						ma.setProperty("ValidUntil", validuntil);
						
						String description = res2.getString("nachfragen_beschreibung");
						ma.setProperty("Description", description);
						
						ma.setProperty("Identifier", oldid);
						
						int bereich = res2.getInt("nachfragen_bereich_id");
						int spez = res2.getInt("nachfragen_spezifizierung_id");
						int[] cat = getCategory(bereich, spez);
						ma.setProperty("Template", cat[0]);
						
						int isoffer = res2.getInt("nachfragen_ist_angebot");
						if(isoffer==1){
							ma.setProperty("IsOffer", "true");	
						}
						else{
							ma.setProperty("IsRequest", "true");	
						}
						
						java.util.Date v2 = DateConverter.sqlToDate(validuntil);
						System.out.println(v2);
						System.out.println(now);
						if(v2.compareTo(now) >= 0){
							ma.setProperty("Status", 1);
						}
						else{
							ma.setProperty("Status", 3);
						}
						
						String maid = insertSimpleObject(ma);
						
						
						importedAds.put(oldid, maid);
						
						
						
	
						//if(v2.compareTo(now) >= 0){
						if(cat[1] > 0){	
							Parameter parameter = new Parameter();
							parameter.addProperty("MemberAdID", "String", maid);
							parameter.setProperty("Title", "Unterkategorie");
							parameter.setProperty("Template", cat[2]);
							parameter.setProperty("Value", cat[1]);
							insertSimpleObject(parameter);
						}	
						//}
					}
				}
			}
		}
		catch(java.lang.Exception e){
			e.printStackTrace();
		}

		
		// 2. Loop für die Nachfragen
		
		try{
			

				
			String sql = "SELECT ID, Identifier FROM OrganisationMember ORDER BY ID";
			Statement stmt = con1.createStatement();
			ResultSet res = stmt.executeQuery(sql);
			while(res.next()){
				
				String omid = res.getString("ID");
				String id = res.getString("Identifier");
					
				System.out.println(omid);
				
				if(id.length() > 0){
					
					Statement stmt2 = con2.createStatement();
					sql = "SELECT vermittlungen_id, vermittlungen_nachfragen_id, from_unixtime(vermittlungen_datum) AS validfrom FROM t_vermittlungen AS t1 WHERE t1.vermittlungen_benutzer_id=" + id;
					ResultSet res2 = stmt2.executeQuery(sql);
					while(res2.next()){
						String adid = res2.getString("vermittlungen_nachfragen_id");
						if(importedAds.get(adid) != null){
							String maid = (String)importedAds.get(adid);
							String validfrom = res2.getString("validfrom");
								
							MemberAdRequest mar = new MemberAdRequest();
							mar.addProperty("OrganisationMemberID", "String", omid);
							mar.setProperty("MemberAd", maid);
							mar.setProperty("Status", 1);
							mar.setProperty("ValidFrom", validfrom);
							insertSimpleObject(mar);
						}
					}
					
				}

			}

		}
		catch(java.lang.Exception e){
			e.printStackTrace();
		}

			
		
		
	}
	public void importNotes(){
		java.util.Date now = new java.util.Date();
		
		try{
			Statement stmt = con1.createStatement();			

			
			stmt.execute("DROP TABLE Activity");
			stmt.execute("DROP TABLE Note");

			
			dataStore.initDatabase();
		}
		catch(java.lang.Exception e){
			
		}
		try{		
			String sql = "SELECT ID, Identifier FROM OrganisationMember ORDER BY ID";
			Statement stmt = con1.createStatement();
			ResultSet res = stmt.executeQuery(sql);
			while(res.next()){
				
				String omid = res.getString("ID");
				String id = res.getString("Identifier");
					
				System.out.println(omid);
				
				if(id.length() > 0){
			
				
					Statement stmt2 = con2.createStatement();
					sql = "SELECT *, from_unixtime(notiz_datum) AS date FROM t_notizen WHERE notiz_benutzer_id=" + id;
					ResultSet res2 = stmt2.executeQuery(sql);
					while(res2.next()){
						String content = res2.getString("notiz_beschreibung");
						String date = res2.getString("date");
						Activity activity = new Activity();
						activity.addProperty("OrganisationMemberID", "String", omid);
						activity.setProperty("DateCreated", date);
						activity.setProperty("Template", "3");
						String activityid = insertSimpleObject(activity);
						Note note = new Note();
						note.addProperty("ActivityID", "String", activityid);
						note.setProperty("Content", content);
						note.setProperty("Template", "20");
						note.setProperty("Title", "Notiz");
						insertSimpleObject(note);
						
					}
				}
			}
		}
		catch(java.lang.Exception e){
			e.printStackTrace();
		}		
		
	}
	public void importDonors(){
		try{			
			WorkbookSettings settings = new WorkbookSettings();
			settings.setEncoding("8859_1");
			
			File datafile = new File("../import/Spendenverzeichnis.xls");
							
			Workbook workbook = Workbook.getWorkbook(datafile, settings);	
			WritableWorkbook copy = Workbook.createWorkbook(new File("../import/Spendenverzeichnis2.xls"), workbook);
			
			WritableCellFormat times;
			WritableFont times10pt = new WritableFont(WritableFont.TIMES, 10);
			times = new WritableCellFormat(times10pt);
			
			Sheet sheet = workbook.getSheet(0);
			WritableSheet sheet2 = copy.getSheet(0);
			
			boolean hasMoreLines = true;
			int cntLines = 0;
			int cnt = 0;
			Cell cell;
			
			Statement stmt = con1.createStatement();
			
			while(hasMoreLines){
				
				String omid = getFieldValue(sheet, 0, cntLines);
				String title = getFieldValue(sheet, 2, cntLines);
				String familyname = getFieldValue(sheet, 6, cntLines);
				String firstname = getFieldValue(sheet, 7, cntLines);
				System.out.println(familyname + " " + firstname);
				
				if(omid==null || omid.trim().length()==0){
				
					String sql = "SELECT t3.ID AS OMID, t1.* FROM Identity AS t1 JOIN Person AS t2 ON t2.ID=t1.PersonID JOIN OrganisationMember AS t3 ON t3.Person=t2.ID WHERE FamilyName ilike '" + familyname + "%' AND Firstname ilike '" + firstname + "%'";
					ResultSet res = stmt.executeQuery(sql);
					boolean exists = false;
					while(res.next()){
						System.out.println("Gefunden : " + res.getString("OMID") + ":" + res.getString("FamilyName") + " " + res.getString("Firstname"));	
						exists = true;
						addCell(sheet2, 0, cntLines, res.getString("OMID"), times);
						
						
						

					}
					if(!exists){
						/*
						String street = getFieldValue(sheet, 8, cntLines);
						String number = getFieldValue(sheet, 9, cntLines);	
						String zipcode = getFieldValue(sheet, 10, cntLines);	
						String city = "Basel";
						
						System.out.println(street + " " + number + ", " + zipcode);
						
						Person  person = new Person();
						String personid = insertSimpleObject(person);
						
						Identity identity = new Identity();
						identity.addProperty("PersonID", "String", personid);
						identity.setProperty("FamilyName", familyname);
						identity.setProperty("FirstName", firstname);
						String sex = "0";
						if(title.startsWith("Herr")){
							sex = "1";	
						}
						else if(title.startsWith("Herr")){
							sex = "2";	
						}
						identity.setProperty("Sex", sex);
						
						insertSimpleObject(identity);
						
						Address address = new Address();
						address.addProperty("PersonID", "String", personid);
						address.setProperty("Street", street);
						address.setProperty("Number", number);
						address.setProperty("Zipcode", zipcode);
						address.setProperty("City", "Basel");
						
						insertSimpleObject(address);
						
						OrganisationMember om = new OrganisationMember();
						om.addProperty("OrganisationalUnitID", "String", "1");
						om.setProperty("Person", personid);
						om.setProperty("Status", 1);
						insertSimpleObject(om);
						
						System.out.println(cnt++);
						*/

					}

					
				}
				if(omid != null && omid.length() > 0){
					/*	
					MemberRole mr = new MemberRole();
					mr.addProperty("OrganisationMemberID",  "String", omid);
					mr.setProperty("Role", 3);
				
					insertSimpleObject(mr);	
					*/
				}
			
				if(familyname == null){
					hasMoreLines = false;	
				}
				cntLines++;
			}	
			copy.write();
			copy.close();
		}
		catch(java.lang.Exception e){
			e.printStackTrace();
		}
	}
	
	private void addCell(WritableSheet sheet, int column, int row, String s, WritableCellFormat format){
		try{
			Label label;
			label = new Label(column, row, s, format);
			sheet.addCell(label);
		}
		catch(java.lang.Exception e){
			
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
	
	private int[] getCategory(int oldcategory, int oldsubcategory){
		int newcategory = -oldcategory;
		int newcsubcategory = -oldsubcategory;
		int template = 0;


		
		if(oldcategory==1){ //Kinder
			newcategory = 4;	
			template = 5;
			if(oldsubcategory==2){
				newcsubcategory = 1;
			}
		}
		else if(oldcategory==2){ //Nachhilfe
			newcategory = 3;	
			newcsubcategory = 0;
		}
		else if(oldcategory==3){ //Administratives
			newcategory = 11;	
			template = 8;
			if(oldsubcategory==6){
				newcsubcategory = 2;
			}
		}
		else if(oldcategory==4){ //Sprachen
			newcategory = 6;	
			template = 7;
			if(oldsubcategory==123){
				newcsubcategory = 33;
			}	
			else if(oldsubcategory==122){
				newcsubcategory = 32;
			}	
			else if(oldsubcategory==121){
				newcsubcategory = 31;
			}	
			else if(oldsubcategory==120){
				newcsubcategory = 30;
			}	
			else if(oldsubcategory==119){
				newcsubcategory = 29;
			}	
			else if(oldsubcategory==117){
				newcsubcategory = 28;
			}	
			else if(oldsubcategory==115){
				newcsubcategory = 27;
			}	
			else if(oldsubcategory==107){
				newcsubcategory = 26;
			}	
			else if(oldsubcategory==97){
				newcsubcategory = 23;
			}	
			else if(oldsubcategory==92){
				newcsubcategory = 22;
			}	
			else if(oldsubcategory==90){
				newcsubcategory = 20;
			}	
			else if(oldsubcategory==88){
				newcsubcategory = 19;
			}	
			else if(oldsubcategory==66){
				newcsubcategory = 17;
			}	
			else if(oldsubcategory==64){
				newcsubcategory = 16;
			}	
			else if(oldsubcategory==63){
				newcsubcategory = 15;
			}	
			else if(oldsubcategory==61){
				newcsubcategory = 14;
			}	
			else if(oldsubcategory==58){
				newcsubcategory = 11;
			}	
			else if(oldsubcategory==57){
				newcsubcategory = 10;
			}	
			else if(oldsubcategory==54){
				newcsubcategory = 8;
			}	
			else if(oldsubcategory==13){
				newcsubcategory = 7;
			}	
			else if(oldsubcategory==12){
				newcsubcategory = 6;
			}	
			else if(oldsubcategory==11){
				newcsubcategory = 5;
			}	
			else if(oldsubcategory==10){
				newcsubcategory = 4;
			}	
			else if(oldsubcategory==9){
				newcsubcategory = 3;
			}	
			else if(oldsubcategory==8){
				newcsubcategory = 2;
			}	
			else if(oldsubcategory==7){
				newcsubcategory = 1;
			}	

		}
		else if(oldcategory==5){ //Soziales
			newcategory = 9;	
			template = 9;
			if(oldsubcategory==15){
				newcsubcategory = 1;
			}	
			if(oldsubcategory==19){ 
				newcsubcategory = 4; //anderes
			}	
			if(oldsubcategory==109){
				newcsubcategory = 4;
			}	
		}
		else if(oldcategory==6){ //Mittagstisch
			newcategory = 4;
			template = 5;
			if(oldsubcategory==24){
				newcsubcategory = 2;
			}
		}
		else if(oldcategory==8){ //Garten
			newcategory = 5;
			template = 6;
			if(oldsubcategory==30){
				newcsubcategory = 4;
			}
			if(oldsubcategory==31){
				newcsubcategory = 0;
			}
			if(oldsubcategory==32){
				newcsubcategory = 4;
			}
			if(oldsubcategory==93){
				newcategory = 10;
				newcsubcategory = 2;
			}
		}
		else if(oldcategory==14){ //Alter
			newcategory = 1;	
			template = 1;
			if(oldsubcategory==77){
				newcsubcategory = 2;
			}
		}
		else if(oldcategory==15){ //Haustier
			newcategory = 7;
			template = 12;
			if(oldsubcategory==78){
				newcsubcategory = 1;
			}
			if(oldsubcategory==79){
				newcsubcategory = 4;
			}
			if(oldsubcategory==114){
				newcsubcategory = 4;
			}
			if(oldsubcategory==113){ //Pflanzen giessen
				newcategory = 5;
				newcsubcategory = 1;
			}
		}
		else if(oldcategory==16){ //Andere Nachbarschaftshilfen
			if(oldsubcategory==81){  //schriftliches
				newcategory = 11;
				template = 8;
				newcsubcategory = 2;
			}
			if(oldsubcategory==85){  //Kleintiere
				newcategory = 7;
				template = 12;
				newcsubcategory = 3;
			}				
		}
		else if(oldcategory==20){ //Computer
			newcategory = 8;
			template = 15;
			if(oldsubcategory==101){
				newcsubcategory = 1;
			}
			if(oldsubcategory==109){
				newcsubcategory = 4;
			}
			
		}
		else if(oldcategory==22){ //Auto
			newcategory = 10;	
			template = 14;
			if(oldsubcategory==105){
				newcsubcategory = 1;
			}
			if(oldsubcategory==106){
				newcsubcategory = 1;
			}
			
		}
		else if(oldcategory==23){ //Zugezogene
			if(oldsubcategory==108){ //Orientierungshilfe
				newcategory = 9; //Soziales
				template = 0;
				newcsubcategory = 0;
			}
		}
		else if(oldcategory==24){ //Garten
			newcategory = 2;
			if(oldsubcategory==109){ //Handwerkliche Arbeiten
				newcategory = 2;
				template = 2;
				newcsubcategory = 2;
			}
			if(oldsubcategory==110){ //Garten
				newcategory = 5;
				template = 0;
				newcsubcategory = 0;
			}
			if(oldsubcategory==116){ //Schneeräumen
				newcategory = 2;
				template = 2;
				newcsubcategory = 4;
			}
		}
		else if(oldcategory==25){ //Nachfragen
			if(oldsubcategory==101){ //Computer
				newcategory = 8;
				template = 15;
				newcsubcategory = 1;
			}
		}
		
		int[] cat = new int[3];
		cat[0] = newcategory;
		cat[1] = newcsubcategory;
		cat[2] = template;
		
		return cat;
	}
	

	
	//-------------------------------------------data-store--------------------------------------
	
	public String insertSimpleObject(BasicClass record) {
		return dataStore.insertSimpleObject(record);
	}

	public String insertObject(BasicClass record) {
		return insertObject(record, false);
	}

	public String insertObject(BasicClass record, boolean recursive) {
		BasicClass newObject = insertAndGetObject(record, recursive);
		return newObject.getName();
	}
	
	public boolean updateObject(BasicClass record){
//		System.out.println(".. updating object " + record);
		return dataStore.updateObject(record);
	}
	
	public BasicClass getObject(BasicClass owner, String type, String keyname, String keyvalue){
				
		return 	getObject(owner, type, keyname, keyvalue, true);
		
	}
	public BasicClass getObject(BasicClass owner, String type, String keyname, String keyvalue, boolean initialize){
				
		return 	dataStore.getObject(owner, type, keyname, keyvalue, initialize);
		
	}
	
	//------------------------------------------------------------------------------------------
	public static void main(String[] args){
		DataImport di = new DataImport();	
		//di.importMembers();
		//di.updateMembers();
		di.updateMembers2();
		//di.importMemberAds();
		//di.importNotes();
		//di.importDonors();
		
	}
	
}