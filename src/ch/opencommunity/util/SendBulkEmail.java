package ch.opencommunity.util;

import org.kubiki.base.*;
import org.kubiki.database.*;

import java.sql.*;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

import java.io.File;

public class SendBulkEmail extends AbstractApplication{
	
	Connection con1;
	DataStore dataStore = null;
	String rootpath;
	
	
	public SendBulkEmail(){
		
		addProperty("datastore", "String", "org.kubiki.database.PGSQLDataStore");		
		addProperty("dburl", "String", "jdbc:postgresql");	
		addProperty("dbname", "String", "//127.0.0.1:15432/nachbarnet_20160602");	
		addProperty("dbuser", "String", "postgres");	
		addProperty("dbpw", "String", "AmE,sadS.");	
		addProperty("readonlydbuser", "String", "queryuser");	
		addProperty("readonlydbpw", "String", "AmE,sadS.");	
		addProperty("rootpath", "String", "");	
		addProperty("smtphost", "String", "192.168.0.166");
		
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
		
		
		
	}
	public void sendMails(){
		try{
			Statement stmt = con1.createStatement();
			String sql = "SELECT DISTINCT t1.ID, t3.Value, t4.FamilyName, t4.FirstName, t4.Sex FROM OrganisationMember AS t1 LEFT JOIN Person AS t2 ON t1.Person=t2.ID";
			sql += " LEFT JOIN Contact AS t3 ON t3.PersonID=t2.ID AND t3.Type=3";
			sql += " LEFT JOIN Identity AS t4 ON t4.PersonID=t2.ID";
			sql += " WHERE t1.Status=1";
			sql += " AND t1.ID > 190";
			sql += " ORDER BY t1.ID";
			//sql += " LIMIT 100";
			ResultSet res = stmt.executeQuery(sql);
			String emailtext = openFile("../spendenaufruf/Infomail_Text.txt", "iso-8859-1");
			int cnt1 = 0;
			int cnt2 = 0;
			StringBuilder log = new StringBuilder();
			while(res.next()){
				String id = res.getString("ID");
				String email = res.getString("Value");
				if(email==null || email.trim().length()==0){
					Statement stmt2 = con1.createStatement();
					//stmt2.execute("UPDATE OrganisationMember SET NotificationMode=2 WHERE ID=" + id);
				}
				else{
					String username = res.getString("ID");
					String addressation = "";
					String familyname = res.getString("FamilyName");
					String firstname = res.getString("FirstName");
					int sex = res.getInt("Sex");
					if(sex==1){
						//addressation = "Liebe/r " + firstname + " " + familyname;
						addressation = "Lieber Herr " + familyname;
					}
					else if(sex==2){
						//addressation = "Liebe/r " + firstname + " " + familyname;
						addressation = "Liebe Frau " + familyname;
					}
					else{
						addressation = "Liebe/r " + firstname + " " + familyname;
					}
					if(sex != 0){
						if(email.indexOf("@") > -1){
							String emailtext2 = emailtext.replace("<@addressation>", addressation);
							emailtext2 = emailtext2.replace("<@username>", username);
							log.append(username + ";" + addressation + ";" + email + "\n");
							cnt1++;
							System.out.println("User " + id+ ":" + cnt1 + " " + addressation);
							
							//sendEmail(emailtext2, "Neue Website Nachbarnet", email);

						}
					}
					else{
						cnt2++;
						System.out.println(cnt2 + ":" + addressation);
					}
				}
			}
			saveFile("../spendenaufruf/out.csv", log.toString());
			
		}
		catch(java.lang.Exception e){
			e.printStackTrace();
		}
		
	}
	public boolean sendEmail(String messagebody, String subject, String recipient){
		

		try{
		
			Properties props = new Properties();
			props.put("mail.host", getString("smtphost"));

			Session session = Session.getInstance(props, null);

			javax.mail.internet.MimeMessage msg = new MimeMessage(session);

			InternetAddress addressFrom = new InternetAddress("info@nachbarnet.net");
			msg.setFrom(addressFrom);
			
			InternetAddress[] addressTo = new InternetAddress[1]; 
			
			addressTo[0] = new InternetAddress(recipient);

			
		    			
			msg.setRecipients(javax.mail.Message.RecipientType.TO, addressTo);
		      

			msg.setSubject(subject, "utf-8");

			msg.setHeader("Content-Type", "text/plain; charset=\"utf-8\"");
			msg.setContent(messagebody, "text/plain; charset=\"utf-8\"");

			Transport.send(msg);
		
			return true;
		}
		catch(java.lang.Exception e){
			logException(e);
			return false;
		}
		
	}
	//------------------------------------------------------------------------------------------
	public static void main(String[] args){
		
		SendBulkEmail sbm = new SendBulkEmail();
		sbm.sendMails();
		
	}
	
	
	
	
}