package org.kubiki.database;


import org.kubiki.base.*;
import org.kubiki.database.Relationship;

import org.kubiki.ide.SQLFieldDefinition;
import org.kubiki.ide.SQLTableDefinition;

import org.kubiki.util.DateConverter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.*;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.hsqldb.Server;

public class SQLDataStore extends DataStore{
	
	public Connection con;
	public Statement stmt;
	public ResultSet res;
	public Map<String, String> typeMap;
	
	private Server hsqlServer = null;
	private BufferedWriter log;

	public SQLDataStore(){
		

		
		addObjectCollection("Tables","org.kubiki.ide.SQLTableDefinition");
		addObjectCollection("Indices","org.kubiki.database.IndexDefinition");
		
		
		addProperty("DataDir","String","");
		addProperty("DBName","String","");	
		
		typeMap = new Hashtable<String, String>();
		typeMap.put("String", "char");
		typeMap.put("DateTime", "datetime");
		typeMap.put("Date", "date");
//		typeMap.put("TreeList", "text");
		typeMap.put("ListFillIn", "text");
		typeMap.put("List", "text");
		typeMap.put("Text", "text");
		typeMap.put("LargeText", "text");
		typeMap.put("Double", "double");
		typeMap.put("Float", "float");
		typeMap.put("FormattedText", "text");
		typeMap.put("Boolean", "boolean");
		typeMap.put("text", "text");
		typeMap.put("Password", "password");
		typeMap.put("Hyperlink", "char");
			
	}
	public Connection getConnection(){
		return con;	
	}
	public void initDatabase(){
		initLog();
		log("initializing database");
		
		try{
			
			hsqlServer = new Server();
			hsqlServer.setLogWriter(null);
            hsqlServer.setSilent(true);
            
			hsqlServer.setDatabaseName(0, "xdb");
            //hsqlServer.setDatabasePath(0, "file:data/epr");
            hsqlServer.start();



			Class.forName("org.hsqldb.jdbcDriver");
			//con = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/xdb", "sa", "");
			con = DriverManager.getConnection("jdbc:hsqldb:file:" + getString("DataDir") +"/" + getString("DBName"), "sa", "");
			stmt = con.createStatement();

			List<String> existingTables = new Vector<String>();

			DatabaseMetaData meta = con.getMetaData();
			ResultSet res = meta.getTables(null, null, null, new String[]{"TABLE"}); 

			while(res.next() != false){
				existingTables.add(res.getString(3).toUpperCase());
			}
			
			Vector tables = getObjects("Tables");
			
			for(int i = 0; i < tables.size(); i++){
				SQLTableDefinition std = (SQLTableDefinition)tables.elementAt(i);				
				Vector relations = std.getObjects("relations");
				
				for(int j = 0; j < relations.size(); j++){
					Relationship rs = (Relationship)relations.elementAt(j);
					String childtable = rs.getString("targetclass");
					SQLTableDefinition std2 = (SQLTableDefinition)getObjectByName("Tables", childtable);
					if(std2 != null){
						SQLFieldDefinition field = new SQLFieldDefinition();
						field.setParent(std2);
						field.setName(rs.getString("targetfield"));
						field.setType("integer");	
						
						SQLFieldDefinition idfield = (SQLFieldDefinition)std2.getObjectByIndex("fields", 0);
						if(std2.getObjectByName("fields", field.getName())==null){											
							std2.addSubobjectAfter(field.getName(), "fields", field, idfield);	
						}
						
					}
				}
				
				
			}
			for(int i = 0; i < tables.size(); i++){
				
				SQLTableDefinition std = (SQLTableDefinition)tables.elementAt(i);	
				
				if(existingTables.contains(std.getName().toUpperCase())){
					updateTable(std);
				}
				else{
					log(std.getCreateSQL());
					stmt.execute(std.getCreateSQL());
					stmt.execute("ALTER TABLE " + std + " ALTER COLUMN ID RESTART WITH 1");	
				}
			}
			
			

			
		}
		catch(Exception e){
			log(e);
		}		
		
	}
	public void close(){
		try{
			stmt = con.createStatement();
			stmt.execute("SHUTDOWN"); 
            stmt.close();
            con.commit();

		}
		catch(Exception e){
			log(e);
		}		
	}

	public void registerClass(String classname){
		try{
			Class c = Class.forName(classname);
			Record record = (Record)c.newInstance();
			
			String[] args = classname.split("\\.");
			String name = args[args.length-1];
			
			SQLTableDefinition table = new SQLTableDefinition();
			table.setName(name);
			table.setProperty("class", classname);
			
			SQLFieldDefinition field = new SQLFieldDefinition();
			field.setName("ID");
			field.setType("integer");
			field.setProperty("key", "primary_auto_increment");
			field.setParent(table);
			table.addSubobject("fields", field);
			
			List<String> names = record.getPropertySheet().getNames();
			for(int i = 3; i < names.size(); i++){
				
				Property p = record.getProperty(names.get(i));
				field = new SQLFieldDefinition();
				
				String type = p.getType();

				if(typeMap.get(type) != null){
				    type = typeMap.get(type);
				}				
				else{
					type = "integer";
				}
				if(type.equals("char") || type.equals("password")){
					field.setLength(p.getLength());
				}
				
				field.setName(p.getName());

				if(p.getValue().toString().length() > 0){

				      field.setDefault(p.getValue().toString());

				}

				field.setLabel(p.getLabel());

				field.setType(type);
				
				field.setParent(table);
				table.addSubobject("fields", field);
				
			}
			
			Vector<ObjectCollection> collections = record.getObjectCollections();
			for(ObjectCollection oc : collections){
				
				String targetclass = (String)oc.getTypes().elementAt(0);
					
				args = targetclass.split("\\.");
				String targettable = args[args.length-1];
				
				Relationship rs = new Relationship();
				rs.setName(targettable);
				rs.setParent(table);
				rs.setPreloadObjects(oc.getPreloadObjects());
				rs.setProperty("sourcefield", "ID");
				rs.setProperty("targetclass", targettable);
				rs.setProperty("targetfield", name + "ID");

				table.addSubobject("relations", rs);				
								
				
			}
			table.setParent(this);
			addSubobject("Tables",table);
				
			
			
		}
		catch(Exception e){
			log(e);
		}		
		
	}
	
	public void updateTable(SQLTableDefinition std){
			
		Hashtable extistingFields = new Hashtable();
		
		try{
			
			Statement stmt = con.createStatement();
			ResultSet res = stmt.executeQuery("SELECT * FROM " + std.getName());
			ResultSetMetaData metaData = res.getMetaData();
			for(int i = 1; i <= metaData.getColumnCount(); i++){
				extistingFields.put(metaData.getColumnName(i).toUpperCase(), ""); 	
			}
			res.close();
			
			Vector fields = std.getObjects("fields");
			
			for(int i = 0; i < fields.size(); i++){
				
				SQLFieldDefinition sfd = (SQLFieldDefinition)fields.elementAt(i);
				String fieldname = sfd.getName().toUpperCase();
				if(extistingFields.get(fieldname)==null){
					try{
//						stmt.execute("ALTER TABLE " + std.getName() + " ADD COLUMN " + sfd.getCreateString().replace("text","VARBINARY"));
						stmt.execute("ALTER TABLE " + std.getName() + " ADD COLUMN " + sfd.getCreateString());
					}
					catch(SQLException e){
						log(e);	
					}
				}
					
			}
		}
		catch(Exception e){
			log(e);
		} 
	}
	
	public String insertSimpleObject(BasicClass object){
		Record record = (Record)object;
		int id = insertRecord(record.getTablename(), record, null, null, null);
		return Integer.toString(id);
	}

	public BasicClass insertObject(BasicClass object, boolean recursive){
		return insertObject(object.getParent(), object, recursive);
	}
	
	protected BasicClass insertObject(BasicClass parent, BasicClass object, boolean recursive){
		Record record = (Record)object;
		int id = insertRecord(record.getTablename(), record, null, null, null);
		String name = Integer.toString(id);
		record.setName(name); // must be set so getObject finds it if it has already been saved with a different ID
		BasicClass newObject = getObject(parent, record.getTablename(), "ID", name, true);
		if (recursive) {
			for (ObjectCollection oc : object.collections) {
				Vector<BasicClass> subobjects = oc.getObjects();
				for (BasicClass subobject : subobjects) {
					BasicClass newSubobject = insertObject(newObject, subobject, true);
					String parentKey = record.getTablename() + "ID";
					if (newSubobject.hasProperty(parentKey)) {
						newSubobject.setProperty(parentKey, name);
						updateObject(newSubobject);
					}
				}
			}
		}
		return newObject;
	}
	
	public boolean updateObject(BasicClass object){
		Record record = (Record)object;	
		if(getObjectByName("Tables",record.getTablename()) != null){
			return updateRecord(record.getTablename(), record, null, null);
		}
		else{
			return false;
		}
	}	

	public void removeObject(String tablename, String id, boolean recursive) {
		removeObject(tablename, "ID", id, recursive);
	}
	
	public void removeObject(String tablename, String keyName, String key, boolean recursive) {
		try {
			if (recursive) {
				SQLTableDefinition std = (SQLTableDefinition)getObjectByName("Tables", tablename);
				
				String sql = "SELECT * FROM " + tablename + " WHERE " + keyName + "=" + key;
				Statement selectStmt = con.createStatement(); // recursive calls need separate statements
				ResultSet res = selectStmt.executeQuery(sql);
				while (res.next()) {
					String ID = res.getString("ID");
					
					Vector relations = std.getObjects("relations");
					for (int i = 0; i < relations.size(); i++) {
						Relationship rs = (Relationship)relations.elementAt(i);
						if (getObjectByName("Tables", rs.getString("targetclass")) != null) {
							removeObject(rs.getString("targetclass"), rs.getString("targetfield"), ID, true);
						}
					}
				}
				res.close();
			}
			
			String sql = "DELETE FROM " + tablename + " WHERE " + keyName + "=" + key;
			log(sql);
			int deleted = stmt.executeUpdate(sql);
//			System.out.println("deleted: " + deleted);
		}
		catch(Exception e){
			log(e);	
		}
	}
	
	public boolean updateRecord(String tablename, Hashtable addvalues) {
		return updateRecord(tablename, null, null, addvalues);
	}
	public boolean executeCommand(String command){
		try{
			Statement stmt = con.createStatement();
			stmt.execute(command);
			stmt.close();
			return true;
		}
		catch(java.lang.Exception e){
			log(e);
			return false;
		}
	}
	public boolean updateRecord(String tablename, BasicClass record, String fileID, Hashtable addvalues){

		boolean success = true;
		
//		if (record != null) {
//			log("Updating Record ... " + record.getName());
//		}

   		SQLTableDefinition table = (SQLTableDefinition)getObjectByName("Tables", tablename);
   		
		int byteaPos = -1;
		int cntCols = 0;
		
		Object[] byteaData = null;
   		
   		if(tablename != null){
   			

   			
   			Vector fields = null;
   			if(table != null){
   				fields = table.getObjects("fields");
   			}

   			byteaData = new Object[fields.size()];   			
   			
   			
   			String sql = "UPDATE " + tablename + " SET ";

   			String values = "";
   			
   			for(int i = 0; i < fields.size(); i++){
   				SQLFieldDefinition fd = (SQLFieldDefinition)fields.elementAt(i);
   				
   				Object valueObject = null;
   				
   				int length = fd.getInt("length");
   				
   				if(record != null && record.hasProperty(fd.getName())){
   					Object o = record.getObject(fd.getName());
					if(o instanceof BasicInterface){
   						BasicClass item = (BasicClass)o;
   						valueObject = item.getValue();
   					}
   					else{
   						valueObject = record.getObject(fd.getName());
   					}
   				}

   				
   				if(addvalues != null){
   					if(addvalues.get(fd.getName()) != null){
   						valueObject = addvalues.get(fd.getName());
   					}	
   				}
   				
   				if(valueObject != null){
   					String value = valueObject.toString();
   					String type = fd.getString("type").toLowerCase();
   					
   					if (valueObject instanceof java.util.Date) {
   						if (type.equals("date")) {
   							value = DateConverter.dateToSQL((java.util.Date)valueObject, false);
   						}
   						else if (type.equals("datetime")) {
   							value = DateConverter.dateToSQL((java.util.Date)valueObject, true);
   						}
   					}
   					  					
   					value = value.replace("\'","\'\'");
   					

					
					if(type.equals("date")){
						if(value.length()==0){
   							values += fd.getName() + "= NULL,";		
   						}
						else if(value.equals("NULL")){
   							values += fd.getName() + "= NULL,";		
   						}
   						else{
   							values += fd.getName() + "=\'" + value + "\',";	
   						}
						
   							
   					}
					else if(type.equals("datetime")){
						if(value.length()==0){
   							values += fd.getName() + "= NULL,";		
   						}
						else if(value.equals("NULL")){
   							values += fd.getName() + "= NULL,";		
   						}
   						else{
   							values += fd.getName() + "=\'" + value + "\',";	
   						}
						
   							
   					}
   					else if(type.equals("char")){
   						if(value.length() > length){
   							log("Value of " + tablename + "." + fd.getName() + " truncated from " + value.length() + " to " + length + " characters.");
   							value = value.substring(0, length);
   						}
   						values += fd.getName() + "=\'" + value + "\',";	
   					}
   					else if(type.equals("password")){
   						values += fd.getName() + "=\'" + value + "\',";	
   					}
   					else if(type.equals("text")){
   						values += fd.getName() + "=?,";	
   						
   						cntCols++;
   						byteaData[cntCols] = (byte[])value.getBytes();

   					}
   					else if(type.equals("timestamp")){
   						values += fd.getName() + "=\'" + value + "\',";	
   					}
   					else{
   						if(value.length()==0){
   							values += fd.getName() + "=NULL,";   							
   						}
   						else{
   							values += fd.getName() + "=" + value + ",";
   						}
   					}
   					
   					
   					
   					
   				}
   			}

   			values = values.substring(0,values.length()-1);
   			
   			
   			String ID = null;
   			
   			if(record != null){
   				ID = record.getName();
   			}
   			
   			if(addvalues != null && addvalues.get("ID") != null){
   				ID = (String)addvalues.get("ID");	
   			}
   			
   			if(ID != null){
				sql = sql +  values + " WHERE ID=" + ID;
				try{
					
					//log(sql);
					PreparedStatement ps = con.prepareStatement(sql);

					for(int i = 0; i < byteaData.length;i++){
						if(byteaData[i] != null){
							ps.setBytes(i, (byte[])byteaData[i]);
						}	
					}
					
					ps.executeUpdate();
					
				}
				catch(Exception e){
					success = false;
					log(sql);
					log(e);
				}
			}
   		}
		return success;
	}
	protected int insertRecord(String tablename, BasicClass record, Hashtable addvalues, 
			String tabledefinition, Vector fields){
		
//		System.out.println("... inserting in " + tablename);
		
		int id = -1;

		SQLTableDefinition table = null;
		PreparedStatementData psData = createPreparedStatementData();
		int cntCols = 0;
		
		if(fields == null){
			if(tabledefinition != null){
	   			table = (SQLTableDefinition)getObjectByName("Tables", tabledefinition);
	   			fields = table.getObjects("fields");			
			}
			else{
//				log("Table: " + tablename);
	   			table = (SQLTableDefinition)getObjectByName("Tables", tablename);
	   			fields = table.getObjects("fields");
	   		}
	   	}
//   		System.out.println(table);
   		   		
   		if(fields != null){
   			String sql = "INSERT INTO " + tablename + " (";
   			String sFields = "";
   			String values = "";
   			
	   		psData.init(fields.size());
   			
   			for(int i = 0; i < fields.size(); i++){
   				
   				SQLFieldDefinition fd = (SQLFieldDefinition)fields.elementAt(i);

   				
   				Object valueObject = null;
   				
   				int length = fd.getInt("length");
   				
   				if(record != null && record.hasProperty(fd.getName())){

   					Object o = record.getObject(fd.getName());
					if (o instanceof BasicClass) {
   						BasicClass item = (BasicClass)o;
   						valueObject = item.getValue();
   					}
					else {
   						valueObject = o;
   					}
   				}
   				
   				if(addvalues != null){
   					if(addvalues.get(fd.getName()) != null){
   						valueObject = addvalues.get(fd.getName());
   					}	
   				}
   				
   				
   				if(valueObject != null){
   					String value = valueObject.toString();
   					String type = fd.getString("type").toLowerCase();
   					
   					if (valueObject instanceof java.util.Date) {
   						if (type.equals("date")) {
   							value = DateConverter.dateToSQL((java.util.Date)valueObject, false);
   						}
   						else if (type.equals("datetime")) {
   							value = DateConverter.dateToSQL((java.util.Date)valueObject, true);
   						}
   					}

   					value = value.replace("\'","\'\'");
   					sFields += fd.getName() + ",";
   					
   					if(type.equals("char")){
    					
    					if(value.equals("NULL")){
   							value = "";	
   						}  						   						
   						else if(value.length() > length){
   							log("Value of " + tablename + "." + fd.getName() + " truncated from " + value.length() + " to " + length + " characters.");
   							value = value.substring(0, length);		
   						}
   						
   						values += "\'" + value + "\',";	
   					}
   					else if(type.equals("password")){
   						values += "\'" + value + "\',";	
   					}
   					else if(type.equals("text")){
   						//values += "\'" + value + "\',";
   						values += "?,";	
   						cntCols++;
   						psData.setData(cntCols, value);
   					}
   					else if(type.equals("bytea")){
   						values += "?,";	
   						cntCols++;
   						psData.setData(cntCols, value);
   					}
   					else if(type.equals("boolean")){
   						values += "\'" + value + "\',";	
   					}
   					else if(type.equals("date")){
   						if(value.length()==0){
   							values += "NULL,";	
   						}
   						else if(value.equals("NULL")){
   							values += "NULL,";	
   						}
   						else{
   							values += "\'" + value + "\',";	
   						}
   					}  
   					else if(type.equals("datetime")){
   						if(value.length()==0){
   							values += "NULL,";	
   						}
   						else if(value.equals("NULL")){
   							values += "NULL,";	
   						}
   						else{
   							values += "\'" + value + "\',";	
   						}
   					}   						
   					else if(type.equals("timestamp")){
   						if(value.length()==0 || value == null){
   							values += "NULL,";	
   						}
   						else{
   							values += "\'" + value + "\',";	
   						}
   					}
   					else{
   						if(value.length()==0){
   							values += "NULL,";	
   						}
   						else if(value.equals("NULL")){
   							values += "NULL,";	
   						}
   						else{
   							values += value + ",";	
   						}
   					}
   				}
   			}
   			sFields = sFields.substring(0,sFields.length()-1);
   			values = values.substring(0,values.length()-1);
   			
			sql = sql + sFields + ") values (" + values + ")";

			try{
				id = executeInsert(sql, psData, tablename);
				con.commit();
			}
			catch(Exception e){
				log(sql);
				log(e);
			}
   		}
		return id;
	}
	
	// Overwrite this to change method of retrieving ID of inserted record
	protected synchronized int executeInsert(String sql, PreparedStatementData psData, String tablename) throws SQLException {
		PreparedStatement ps = con.prepareStatement(sql);
		psData.dataToStatement(ps);
		ps.execute();
		return getLastInsertedID(tablename);
	}
	
	public BasicClass getObject(BasicClass parent, String tablename, String keyname, String keyvalue, boolean initialize){
		
		return loadSubrecord(parent, tablename, keyname, keyvalue, initialize);	
		
	}

	public BasicClass loadSubrecord(BasicClass parent, String tablename, String keyname, String keyvalue, boolean initialize){
		
		Record record = null;
		
		try{
			SQLTableDefinition std = (SQLTableDefinition)getObjectByName("Tables", tablename);
			Vector fields = std.getObjects("fields");
			
			String sql = "SELECT * FROM " + tablename + " WHERE " + keyname + "='" + keyvalue + "'";
			if (!keyname.equals("ID")) {
				sql += " ORDER BY ID";
			}
//System.out.println(sql);
			Statement readonlyStmt = getReadonlyStatement();
			ResultSet res = readonlyStmt.executeQuery(sql);
			while(res.next() != false){
				String ID = res.getString("ID");
				
				if(parent != null){
					Object existing = parent.getSubobject(ID, tablename);
					if (existing instanceof Record) {
						record = (Record)existing;
					}
					else {
						Class<?> c = Class.forName(std.getString("class"));
						
						record = (Record)c.newInstance();
						record.setParent(parent);
						record.setTablename(tablename);
						record.setName(ID);
						parent.addSubobject(tablename, record);
					}
					parent.setActiveObject(record);
				}
				else{
					Class<?> c = Class.forName(std.getString("class"));
						
					record = (Record)c.newInstance();
					record.setTablename(tablename);
					record.setName(ID);

				}
				record.getProperty("name").setEditable(false);
				
				for(int i = 1; i < fields.size(); i++){
					SQLFieldDefinition sfd = (SQLFieldDefinition)fields.elementAt(i);

					String value = loadValue(res, sfd);
					
					String type = "String";
					if(sfd.getType().equals("text")){
						type = "FormattedText";	
					}
					
					Property p = null;
					
					if(record.hasProperty(sfd.getName())){
						p = record.getProperty(sfd.getName());
						
						if(value != null){
							p.setValue(value.trim());
						}
					}
					else{
						if(value != null){
							p = record.addProperty(sfd.getName(), type, value.trim(), false, sfd.getLabel());
						}
						else{
							p = record.addProperty(sfd.getName(), type, "", false, sfd.getLabel());	
						}
					}
					
					if(sfd.getName().indexOf("ID") > 0){
						record.getProperty(sfd.getName()).setHidden(true);
					}
					
					if(sfd.getString("values").length() > 0){
						Vector values = new Vector();
						String[] args = sfd.getString("values").split("\n");
						for(int j = 0; j < args.length; j++){
							values.add(new ConfigValue(args[j]));	
						}
						p.setSelection(values);	
					}
					/*
					else if(sfd.getSource().length() > 0){
						System.out.println(sfd.getName() + ":" + getCode(sfd.getSource()));
						if(getCode(sfd.getSource()) != null){
							p.setSelection(getCode(sfd.getSource()));		
						}															
					}
					*/
				}

				record.initValues();
//				parent.addSubobject(tablename, record);
				
				Vector relations = std.getObjects("relations");
				for(int i = 0; i < relations.size(); i++){

					Relationship rs = (Relationship)relations.elementAt(i);

					ObjectCollection oc = record.getObjectCollection(rs.getString("targetclass"));
					if(oc==null){
						oc = record.addObjectCollection(rs.getString("targetclass"));
					}
					
					
					if(oc.getPreloadObjects()){
						if(getObjectByName("Tables", rs.getString("targetclass")) != null){
							loadSubrecord(record, rs.getString("targetclass"), rs.getString("targetfield"), ID, initialize);
						}
					}
				
				}
				if(initialize){
					record.initObjectLocal();
				}
				
			}
			res.close();
			
				
		}
		catch(Exception e){
			log(e);	
		}
		return record;
	}
	
	protected String loadValue(ResultSet res, SQLFieldDefinition sfd) throws SQLException {
		String value = null;
		if(sfd.getType().equals("text")){
			byte[] b = res.getBytes(sfd.getName());
			if(b != null){
				value = new String(b);	
			}
			else{
				value = "";	
			}
				
		}
		else{
			value = res.getString(sfd.getName());
			if(value != null){
				value = value.trim();
			}
		}
		return value;
	}
	
	//-------------------------------------------------------------------------------------------------------
	
	public int getLastInsertedID(String tablename){	
		int ID = -1;
        try {
        	
			PreparedStatement pstmt = con.prepareStatement("call identity()");
			res = pstmt.executeQuery();
			if(res.next() != false){ 
				ID = res.getInt(1);	
				//System.out.println("ID: " + ID);
			}
		}
		catch(Exception e){
			log(e);
		}
		return ID;
	}
	
	public static String convertDate(String datestring, int mode){
		
		String[] months = {"Januar","Februar","Maerz","April","Mai","Juni","Juli","August","September","Oktober","November","Dezember"};
		
		String newdate = datestring;
		if(mode==1){
			try{
				String[] parts = datestring.split(" ");
				if(parts.length==3){
					String month = 	null;
					for(int i = 0; i < months.length; i++){
						if(parts[1].equals(months[i])){
							month = "" + (i+1);	
						}	
					}
					if(month != null){
						String day = parts[0];
						if(day.charAt(0)=='0'){
							day = day.substring(1);
						}
						newdate = parts[2] + "-" + month + "-" + day;	
					}	
				}
			}
			catch(Exception e){
				newdate = datestring;	
			}
		}
		else if(mode==2){
			String[] parts = datestring.split("-");
			if(parts.length==3){
				String month = 	months[(new Integer(parts[1])).intValue()-1];	
				newdate = parts[2] + ". " + month + " " + parts[0];	
			}
		}
		
		return newdate;
			
	}

	
	public ObjectCollection queryData(String searchString, ObjectCollection oc, String classname){
		try{
			Statement readonlyStmt = getReadonlyStatement();
			ResultSet res = readonlyStmt.executeQuery(searchString);

			ResultSetMetaData metaData = res.getMetaData();
					
			while(res.next()){
				//Record record = new Record();
				Class<?> c = Class.forName(classname);
				Record record = (Record)c.newInstance();
				for(int i = 1; i <= metaData.getColumnCount(); i++){
					readField(record, i, res, metaData);
				}
				oc.addObject(record);
			}
			
			return oc;
		}
		catch(java.lang.Exception e){
			log(searchString);
			log(e);
			return null;
		}		
		
		
	}
	
	
	public ObjectCollection queryData(String searchString, ObjectCollection oc){
		try{
			Statement readonlyStmt = getReadonlyStatement();
			ResultSet res = readonlyStmt.executeQuery(searchString);

			ResultSetMetaData metaData = res.getMetaData();
					
			while(res.next()){
				Record record = new Record();
				for(int i = 1; i <= metaData.getColumnCount(); i++){
					readField(record, i, res, metaData);
				}
				oc.addObject(record);
			}
			
			return oc;
		}
		catch(SQLException e){
			log(searchString);
			log(e);
			return null;
		}
	}
	
	protected void readField(Record record, int columnIndex, ResultSet res, ResultSetMetaData metaData) throws SQLException {
		record.addProperty(metaData.getColumnLabel(columnIndex), "String", res.getString(columnIndex));	
	}
	
	public String getCreateSQL(SQLTableDefinition std, String altname){
		
		//ToDo: Eigene Klasse SQLDialect für die verschiedenen Datenbanksysteme
		
		String type = "";
		
		String sql = "";
		

		if(altname == null){
			//sql = "CREATE TABLE " + getProperty("name").getValue() + " (";
			sql = "CREATE TABLE " + std.getProperty("name").getValue() + " (";
		}
		else{
			sql = "CREATE TABLE " + altname + " (";	
		}
		
		
		Vector fields = std.getObjectCollection("fields").getObjects();
		for(int i = 0; i < fields.size(); i++){
			SQLFieldDefinition vd = (SQLFieldDefinition)fields.elementAt(i);
			type = vd.getProperty("type").getValue();
			if(type.equals("char")){
				//type = "char(" + vd.getProperty("length").getValue() + ")";
				type = "varchar(" + vd.getProperty("length").getValue() + ")";  //??????????????????????
			}
			else if(type.equals("password")){
				//type = "char(" + vd.getProperty("length").getValue() + ")";
				type = "char(" + vd.getProperty("length").getValue() + ")";
			}
			else if(type.equals("text")){
				type = "text ";
				//type = "VARBINARY (100)"; //???????????????????
			}
			else if(type.equals("bytea")){
				type = "bytea ";
			}						
			else if(vd.getProperty("key").getValue().equals("primary_auto_increment")){
				//sql += " PRIMARY KEY DEFAULT nextval(\'serial\')";

				type = "SERIAL PRIMARY KEY ";	
				//type = "IDENTITY ";	 //???????????????????
				
			}
			else if(type.equals("integer")){
				type = "integer ";
			}
			sql += vd.getName() + " " + type;			
			sql += ", ";
		}
		sql = sql.substring(0,sql.length() -2) + ")";

		return sql;		
	}
	
	public Statement getReadonlyStatement() throws SQLException {
		// not supported by HSQL, use regular statement
		return con.createStatement();
	}
	
	protected PreparedStatementData createPreparedStatementData() {
		return new SQLPreparedStatementData();
	}
	
	protected void log(String message){
		System.err.println(message);
		try{
			log.write("\n" + new java.util.Date());
	        log.write(" " + message);
	        log.flush();	
	    }
	    catch(Exception e){
	    	e.printStackTrace();
	    }
		
	}
	protected void log(Exception  e){	
		e.printStackTrace();
		try{
		    StringWriter sw = new StringWriter();
		    PrintWriter pw = new PrintWriter(sw);
		    e.printStackTrace(pw);
			sw.toString();
			log.write("\n" + new java.util.Date());
		    log.write("\n" + sw.toString());
		    log.flush();
	    }
	    catch(Exception ex){
	    	ex.printStackTrace();
	    }

	}	
	
	protected void initLog(){
		try{
			FileWriter fstream = new FileWriter(getParent().getString("rootpath") + "/log/sql.log", true);
	        log = new BufferedWriter(fstream);	
	    }
	    catch(Exception e){
	    	e.printStackTrace();
	    }
	     
	}
	protected void closeLog(){
		try{	
			log.flush();
			log.close();
		}
		catch(Exception e){
			
		}
	}
}

class SQLPreparedStatementData implements PreparedStatementData {
	private Object[] byteaData;

	@Override
	public void init(int size) {
		byteaData = new Object[size];
	}
	
	@Override
	public void setData(int column, String data) {
		byteaData[column] = data.getBytes();
	}	
	
	@Override
	public void setData(int column, Object data) {
		
	}

	@Override
	public void dataToStatement(PreparedStatement ps) throws SQLException {
		for (int i = 0; i < byteaData.length;i++) {
			if (byteaData[i] != null) {
				ps.setBytes(i, (byte[])byteaData[i]);
			}	
		}
	}
}