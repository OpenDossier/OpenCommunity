package ch.opencommunity.feedback;

import org.kubiki.ide.BasicProcess;
import org.kubiki.application.DataObject;
import org.kubiki.application.ApplicationContext;
import org.kubiki.application.server.ApplicationServer;

import java.util.Map;
import java.util.HashMap;

public class FeedbackRecord extends DataObject{
	
	
	public FeedbackRecord(){
		
		setTablename("FeedbackRecord");
		
		addProperty("OrganisationMember", "Integer", "", false, "Benutzer");
		
		addProperty("Familyname", "String", "", false, "Nachname", 100);
		addProperty("Firstname", "String", "", false, "Vorname", 100);
		addProperty("Street", "String", "", false, "Strasse", 255);
		addProperty("City", "String", "", false, "Ort", 100);

		addProperty("ContactEstablished", "Boolean", "");
		addProperty("ContactNotEstablishedReason", "Integer", "");
		addProperty("ContactNotEstablishedReasonDetail", "IntegerArray", "");

		addProperty("ContactQuality", "Integer", "");
		addProperty("ContactDescription", "Text", "");
		addProperty("Comment", "Text", "");
		
		addProperty("FollowupNeeded", "Boolean", "false");
		
	}
	public static Map getReasonDetails(){
		Map reasonDetails = new HashMap();
		
		reasonDetails.put("11", "mehrmals probiert ohne Erfolg");
		reasonDetails.put("12", "Combox/ Anrufbeantworter mit Bitte um Rückruf");		
		reasonDetails.put("13", "Fehlermeldung/ ungültige Rufnummer");
		reasonDetails.put("14", "anderes");	
		reasonDetails.put("15", "E-Mail ungültig");
		reasonDetails.put("16", "Keine Antwort");
		
		reasonDetails.put("21", "passt fachlich nicht");
		reasonDetails.put("22", "unterschiedliche Persönlichkeiten");		
		reasonDetails.put("23", "prachliche Barriere/ Kommunikationsschwierigkeiten");
		reasonDetails.put("24", "anderes");	
		reasonDetails.put("25", "kein guter Eindruck beim Erstkontaktg");
		reasonDetails.put("26", "anderes");
		
		reasonDetails.put("31", "am vereinbarten Termin nicht erschienen/ nicht Zuhause");
		reasonDetails.put("32", "vereinbarten Termin kurzfristig abgesagt");		
		reasonDetails.put("33", "anderes");
		
		reasonDetails.put("41", "Keine übereinstimmende Termine gefunden");	
		reasonDetails.put("42", "momentan keine Zeit");
		reasonDetails.put("43", "keine Zeit für neue Engagements");
		reasonDetails.put("44", "anderes");
		
		return reasonDetails;
	}
	public static Map getReason(){
		Map reason = new HashMap();
		reason.put("1", "Kontaktversuch per Telefon:");
		reason.put("2", "Passt nicht:");
		reason.put("3", "Unzuverlässig:");
		reason.put("4", "Keine übereinstimmende Termine gefunden:");
		
		return reason;
	}


	
	
	
}