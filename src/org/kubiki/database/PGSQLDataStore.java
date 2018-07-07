package org.kubiki.database;

import java.sql.*;
import java.util.*;

import org.kubiki.ide.*;
import org.kubiki.base.BasicClass;
import org.kubiki.base.BasicInterface;
import org.kubiki.util.DateConverter;

import java.io.InputStream;

public class PGSQLDataStore extends SQLDataStore{

	private Connection readonlyCon;
	
	public PGSQLDataStore(){

		typeMap.put("DateTime", "timestamp");
		typeMap.put("Double", "float");
		typeMap.put("Code", "text");

	}
	
	public void initDatabase(){
		initLog();
		
		log("initializing database");
		

		try {
			
			Class.forName("org.postgresql.Driver");
			con = DriverManager.getConnection(getParent().getString("dburl") + ":" + getParent().getString("dbname"), getParent().getString("dbuser"), getParent().getString("dbpw"));
			readonlyCon = DriverManager.getConnection(getParent().getString("dburl") + ":" + getParent().getString("dbname"), getParent().getString("dbuser"), getParent().getString("dbpw"));
			try{
			//readonlyCon = DriverManager.getConnection(getParent().getString("dburl") + ":" + getParent().getString("dbname"), getParent().getString("readonlydbuser"), getParent().getString("readonlydbpw"));
			}
			catch(java.lang.Exception e){
				readonlyCon = con;
			}
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

						IndexDefinition index = new IndexDefinition();
						index.setName(std2 + "_" + field);
						index.setProperty("TableName", std2.getName());
						index.setProperty("IndexedFields", field.getName());
						index.setParent(this);
						addSubobject("Indices", index);
						
					}
				}
				
				
			}
			for(int i = 0; i < tables.size(); i++){
				
				SQLTableDefinition std = (SQLTableDefinition)tables.elementAt(i);	
				
				if(existingTables.contains(std.getName().toUpperCase())){
					updateTable(std);
				}
				else{
					String sql = getCreateSQL(std, null);
					try{
					    stmt.execute(sql);
					}
					catch(Exception e){
						log(sql);
					    log(e);
					}
					//stmt.execute("ALTER TABLE " + std + " ALTER COLUMN ID RESTART WITH 1");	
				}
			}

			Vector<BasicClass> indices = getObjects("Indices");
			for(BasicClass index : indices){
				// TODO should only be created if it doesn't already exist
				String sql = "CREATE INDEX " + index.getName() + " ON " + index.getString("TableName") + " (" + index.getString("IndexedFields") + ")";
				try{
					stmt.execute(sql);
				}
				catch(Exception e){
//					log(sql);
//					log(e);
				}
			}
		}
		catch(Exception e){
			log(e);
		}		
	}
	public boolean updateRecord(String tablename, BasicClass record, String fileID, Hashtable addvalues){

		boolean success = true;
		
		if (record != null) {
//			log("Updating Record ... " + record.getName());
		}

   		SQLTableDefinition table = (SQLTableDefinition)getObjectByName("Tables", tablename);
   		
		int cntCols = 0;
		
		String[] preparedData = null;
   		
   		if(tablename != null){
   			
   			Vector fields = null;
   			if(table != null){
   				fields = table.getObjects("fields");
   			}

   			preparedData = new String[fields.size()];   			
   			
   			
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
   					  					
   					//value = value.replace("\'","\'\'");
   					

					
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
					else if(type.equals("timestamp")){
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
   						preparedData[cntCols] = value;
   					}
   					else if(type.equals("boolean")){
   						values += fd.getName() + "=\'" + value + "\',";	
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
//					log(sql);
					PreparedStatement ps = con.prepareStatement(sql);

					for(int i = 0; i < preparedData.length;i++){
						if(preparedData[i] != null){
							ps.setString(i, preparedData[i]);
						}	
					}
					
					ps.executeUpdate();
				}
				catch(Exception e){
					//log(sql);
					log(e);
					success = false;
				}
				
				
			}
//			System.out.println(sql);
   		}
		return success;
	}
	
	// not needed, executeInsert is overwritten instead
	public int getLastInsertedID(String tablename){	
		int ID = -1;
		try {
        	
			Statement stmt = con.createStatement();
			res = stmt.executeQuery("SELECT currval(\'" + tablename + "_id_seq\')");
			if(res.next() != false){ 
				ID = res.getInt(1);	
			}
		}
		catch(Exception e){
			//log(e);	
		}
			
		return ID;
	}
	
	@Override
	protected synchronized int executeInsert(String sql, PreparedStatementData psData, String tablename) throws SQLException {
		sql += " RETURNING ID";
		PreparedStatement ps = con.prepareStatement(sql);
		psData.dataToStatement(ps);
		ResultSet rs = ps.executeQuery();
		if(rs.next()){ 
		      return rs.getInt(1); 
		}
		else {
			log("No ID returned for " + sql);
			return -1;
		}
	}
	

	protected String loadValue(ResultSet res, SQLFieldDefinition sfd) throws SQLException {
		String value = null;
		if(sfd.getType().equals("text")){
			value = res.getString(sfd.getName());
			if (value == null) {
				value = "";	
			}
		}
		else if (sfd.getType().equals("float")) {
			float floatValue = res.getFloat(sfd.getName());
			value = Float.toString(floatValue);
		}
/*		else if (sfd.getType().equals("double")) {
			double doubleValue = res.getDouble(sfd.getName());
			value = Double.toString(doubleValue);
		} */
		else{
			value = res.getString(sfd.getName());
			if(value != null){
				value = value.trim();
			}
		}
		return value;
	}
					
	protected void readField(Record record, int columnIndex, ResultSet res, ResultSetMetaData metaData) throws SQLException {
		if (metaData.getColumnType(columnIndex)==java.sql.Types.DOUBLE) {
			float floatValue = res.getFloat(metaData.getColumnLabel(columnIndex).toUpperCase());
			record.addProperty(metaData.getColumnLabel(columnIndex).toUpperCase(), "Float", Float.toString(floatValue));
		}
		else {
			record.addProperty(metaData.getColumnLabel(columnIndex).toUpperCase(), "String", res.getString(columnIndex));
		}
	}

	
	public void close() {
		log("Closing Datastore");
		try{
			con.close();
			readonlyCon.close();
		} catch (SQLException e) {
			log(e.toString());
		}
		Enumeration<Driver> drivers = DriverManager.getDrivers();
		while (drivers.hasMoreElements()) {
		    Driver driver = drivers.nextElement();
		    try {
			DriverManager.deregisterDriver(driver);
		    } catch (SQLException e) {
			log(e.toString());
		    }

		}
		closeLog();

	}
	
	public Statement getReadonlyStatement() throws SQLException {
		return readonlyCon.createStatement();
	}

	protected PreparedStatementData createPreparedStatementData() {
		return new PGSQLPreparedStatementData();
	}
	
} 
/*
class PGSQLPreparedStatementData implements PreparedStatementData {
	private Object[] preparedData;
	
	@Override
	public void init(int size) {
		preparedData = new Object[size];
	}

	@Override
	public void setData(int column, String data) {
		preparedData[column] = data;	
	}
	@Override
	public void setData(int column, Object data) {
		preparedData[column] = data;
	}

	@Override
	public void dataToStatement(PreparedStatement ps) throws SQLException {
		for (int i = 0; i < preparedData.length;i++) {
			if (preparedData[i] != null) {
				if(preparedData[i] instanceof byte[]){
					ps.setBytes(i, (byte[])preparedData[i]);
				}
				else{
					ps.setString(i, (String)preparedData[i]);
				}
			}	
		}
	}

}
*/