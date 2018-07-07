package ch.opencommunity.util;

import ch.opencommunity.base.ScriptDefinition;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import org.kubiki.base.BasicClass;
import ch.opencommunity.base.ObjectTemplateAdministration;
//import org.opendossier.dossier.ScriptDefinition;
import ch.opencommunity.server.OpenCommunityServer;

public class TextComponents {
	//private OpenDossierServer ods;
	private OpenCommunityServer ods;
	private Map<String, Component> components;
	
	//public TextComponents(OpenDossierServer application) {
	public TextComponents(OpenCommunityServer application) {
		final String CAT_DOSSIER = "Dossier";
		final String CAT_CASE = "Fall";
		final String CAT_CASEPERSON = "Bezugsperson";
		final String CAT_ACTIVITY = "Aktivität";
		final String CAT_SCRIPT = "Skript";
		final String CAT_DOCUMENT = "Dokument";
		
		this.ods = application;
		
		components = new TreeMap<String, Component>();
		addComponent("amt", "Amt", CAT_DOSSIER);
		addComponent("abteilung", "Abteilung", CAT_DOSSIER);
		addComponent("absender", "Amt Adresse", CAT_DOSSIER);
		addComponent("amt_telefon", "Amt Telefon", CAT_DOSSIER);
		addComponent("amt_telefax", "Amt Fax", CAT_DOSSIER);
		//addComponent("name", "Name", CAT_DOSSIER);
		addComponent("funktion", "Funktion", CAT_DOSSIER);
		addComponent("datum", "Datum", CAT_DOSSIER);
		addComponent("datum_doc", "Datum Dokument", CAT_DOSSIER);

		addComponent("dossier_nummer", "Klientennummer", CAT_DOSSIER);

		addComponent("betreff", "Betreff", CAT_DOSSIER);
		addComponent("dokumentenklasse", "Dokumentenklasse", CAT_DOCUMENT);
		addComponent("aktenband", "Aktenband", CAT_DOSSIER);
		addComponent("verteiler", "Verteiler", CAT_DOCUMENT);


		addComponent("eltern", "Eltern", CAT_DOSSIER);		
		addComponent("identitaet_familienname", "Familienname", CAT_DOSSIER);
		addComponent("identitaet_vorname", "Vorname", CAT_DOSSIER);
		addComponent("identitaet_maedchenname", "Mädchenname", CAT_DOSSIER);
		addComponent("identitaet_geburtsdatum", "Geburtsdatum", CAT_DOSSIER);
		addComponent("identitaet_geschlecht", "Geschlecht", CAT_DOSSIER);
		addComponent("identitaet_nationalitaet", "Nationalität", CAT_DOSSIER);
		addComponent("identitaet_sprache", "Muttersprache", CAT_DOSSIER);
		addComponent("identitaet_ahvnummer", "AHV-Nummer", CAT_DOSSIER);
		addComponent("adresse_typ", "Adresstyp", CAT_DOSSIER);
		addComponent("adresse_strasse", "Strasse", CAT_DOSSIER);
		addComponent("adresse_nummer", "Nr.", CAT_DOSSIER);
		addComponent("adresse_plz", "PLZ", CAT_DOSSIER);
		addComponent("adresse_ort", "Ort", CAT_DOSSIER);
		addComponent("adresse_land", "Land", CAT_DOSSIER);


		addComponent("telp","Tel.P", CAT_DOSSIER);
		addComponent("telg","Tel.G", CAT_DOSSIER);
		addComponent("telm","Tel.M", CAT_DOSSIER);
		addComponent("email","Email", CAT_DOSSIER);
		addComponent("fax","Fax", CAT_DOSSIER);

		addComponent("namen_eltern", "NameVornameVaterMutter", CAT_DOSSIER);
		addComponent("adresse_eltern", "Adresse Eltern", CAT_DOSSIER);		
		
		addComponent("benutzer_name", "Benutzername", CAT_DOSSIER);
		addComponent("benutzer_funktion", "Benutzerfunktion", CAT_DOSSIER);
		addComponent("benutzer_kuerzel", "Benutzerkürzel", CAT_DOSSIER);
		addComponent("benutzer_email", "Benutzer Email", CAT_DOSSIER);
		addComponent("benutzer_tel", "Benutzer Telefon", CAT_DOSSIER);

		addComponent("unterschrift1", "Unterschrift1", CAT_DOSSIER);
		addComponent("unterschrift2", "Unterschrift2", CAT_DOSSIER);

		addComponent("mitarbeiter1", "Mitarbeiter1", CAT_DOSSIER);
		addComponent("anredemitarbeiter1", "Anrede Mitarbeiter1", CAT_DOSSIER);
		addComponent("mitarbeiter2", "Mitarbeiter2", CAT_DOSSIER);
		addComponent("anredemitarbeiter2", "Anrede Mitarbeiter2", CAT_DOSSIER);


		addComponent("fall_nummer", "Fallnummer", CAT_CASE);
		addComponent("fall_bezeichnung", "Fallbezeichnung", CAT_CASE);

		addComponent("anmeldung_datum", "Anmeldedatum", CAT_CASE);
		addComponent("anmeldung_person", "Anmeldende Person", CAT_CASE);
		addComponent("anmeldung_gemeinde", "Zuständige Gemeinde", CAT_CASE);
		addComponent("anmeldung_institution", "Institution", CAT_CASE);
		addComponent("anmeldung_auftraggeber", "Auftraggeber", CAT_CASE);

		addComponent("abschlussdatum", "Abschlussdatum", CAT_CASE);
		addComponent("einschulungsdatum", "Einschulungsdatum", CAT_CASE);
		addComponent("schultraeger", "Schulträger", CAT_CASE);
		addComponent("datumerstuntersuchung", "Datum Erstuntersuchung", CAT_CASE);
		
		ObjectTemplateAdministration ota = ods.getObjectTemplateAdministration();
		if (ota != null) {
			List<BasicClass> objectTemplates = ota.getObjects("ObjectTemplate");
			for (BasicClass objectTemplate : objectTemplates) {
				String objectType = objectTemplate.getString("Type");
				if (objectType.equals("Activity") || objectType.equals("CaseDetail") ||objectType.equals("DossierDetail")) {
					String category;
					if (objectType.equals("Activity")) {
						category = CAT_ACTIVITY;
					}
					else if (objectType.equals("CaseDetail")) {
						category = CAT_CASE;
					}
					else {
						category = CAT_DOSSIER;
					}

					if(objectType.equals("Activity")){
						if(objectTemplate.getID("Status")==0){
							String path = objectType + "$" + objectTemplate.toString() + "_date";
							path = cleanParameterName(path);
							String label = objectTemplate.toString() + "/Datum";
							addComponent(path, label, CAT_ACTIVITY);

							path = objectType + "$" + objectTemplate.toString() + "_dayofweek";
							path = cleanParameterName(path);
							label = objectTemplate.toString() + "/Wochentag";
							addComponent(path, label, CAT_ACTIVITY);

							path = objectType + "$" + objectTemplate.toString() + "_time";
							path = cleanParameterName(path);
							label = objectTemplate.toString() + "/Zeit";
							addComponent(path, label, CAT_ACTIVITY);

							path = objectType + "$" + objectTemplate.toString() + "_place";
							path = cleanParameterName(path);
							label = objectTemplate.toString() + "/Ort";
							addComponent(path, label, CAT_ACTIVITY);
						}
					}
					List<BasicClass> fieldDefinitions = objectTemplate.getObjects("FieldDefinition");
					for (BasicClass fieldDefinition : fieldDefinitions) {
						String fieldType = fieldDefinition.getString("FieldType");
						if (fieldType.equals("note") || fieldType.equals("parameter") || fieldType.equals("date")) {
							String path = objectType + "$" + objectTemplate.toString() + "_" + fieldType + "$" + fieldDefinition.toString();
							path = cleanParameterName(path);
							String label = objectTemplate.toString() + "/" + fieldDefinition.toString();
							addComponent(path, label, category);
						}
					}
				}
				else if (objectType.equals("Relation")){

					String path = objectType + "$" + objectTemplate.toString() + "_Name";
					path = cleanParameterName(path);
					String label = objectTemplate.toString() + "/Name";
					addComponent(path, label, CAT_CASEPERSON);

					path = objectType + "$" + objectTemplate.toString() + "_Vorname";
					path = cleanParameterName(path);
					label = objectTemplate.toString() + "/Vorname";
					addComponent(path, label, CAT_CASEPERSON);

					path = objectType + "$" + objectTemplate.toString() + "_Address";
					path = cleanParameterName(path);
					label = objectTemplate.toString() + "/Address";
					addComponent(path, label, CAT_CASEPERSON);

					path = objectType + "$" + objectTemplate.toString() + "_Anrede";
					path = cleanParameterName(path);
					label = objectTemplate.toString() + "/Anrede";
					addComponent(path, label, CAT_CASEPERSON);

					path = objectType + "$" + objectTemplate.toString() + "_telp";
					path = cleanParameterName(path);
					label = objectTemplate.toString() + "/TelP";
					addComponent(path, label, CAT_CASEPERSON);

					path = objectType + "$" + objectTemplate.toString() + "_telg";
					path = cleanParameterName(path);
					label = objectTemplate.toString() + "/TelG";
					addComponent(path, label, CAT_CASEPERSON);

					path = objectType + "$" + objectTemplate.toString() + "_telm";
					path = cleanParameterName(path);
					label = objectTemplate.toString() + "/TelM";
					addComponent(path, label, CAT_CASEPERSON);

					path = objectType + "$" + objectTemplate.toString() + "_email";
					path = cleanParameterName(path);
					label = objectTemplate.toString() + "/Email";
					addComponent(path, label, CAT_CASEPERSON);

					path = objectType + "$" + objectTemplate.toString() + "_fax";
					path = cleanParameterName(path);
					label = objectTemplate.toString() + "/Fax";
					addComponent(path, label, CAT_CASEPERSON);


				}
				else if (objectType.equals("Function") && objectTemplate.getBoolean("HasFormFields")==true){

					objectType="relation";

					String path = objectType + "$" + objectTemplate.toString() + "_Name";
					path = cleanParameterName(path);
					String label = objectTemplate.toString() + "/Name";
					addComponent(path, label, CAT_CASEPERSON);

					path = objectType + "$" + objectTemplate.toString() + "_Vorname";
					path = cleanParameterName(path);
					label = objectTemplate.toString() + "/Vorname";
					addComponent(path, label, CAT_CASEPERSON);

					path = objectType + "$" + objectTemplate.toString() + "_Address";
					path = cleanParameterName(path);
					label = objectTemplate.toString() + "/Address";
					addComponent(path, label, CAT_CASEPERSON);

					path = objectType + "$" + objectTemplate.toString() + "_Anrede";
					path = cleanParameterName(path);
					label = objectTemplate.toString() + "/Anrede";
					addComponent(path, label, CAT_CASEPERSON);

					path = objectType + "$" + objectTemplate.toString() + "_telp";
					path = cleanParameterName(path);
					label = objectTemplate.toString() + "/TelP";
					addComponent(path, label, CAT_CASEPERSON);

					path = objectType + "$" + objectTemplate.toString() + "_telg";
					path = cleanParameterName(path);
					label = objectTemplate.toString() + "/TelG";
					addComponent(path, label, CAT_CASEPERSON);

					path = objectType + "$" + objectTemplate.toString() + "_telm";
					path = cleanParameterName(path);
					label = objectTemplate.toString() + "/TelM";
					addComponent(path, label, CAT_CASEPERSON);

					path = objectType + "$" + objectTemplate.toString() + "_email";
					path = cleanParameterName(path);
					label = objectTemplate.toString() + "/Email";
					addComponent(path, label, CAT_CASEPERSON);

					path = objectType + "$" + objectTemplate.toString() + "_fax";
					path = cleanParameterName(path);
					label = objectTemplate.toString() + "/Fax";
					addComponent(path, label, CAT_CASEPERSON);

				}
			//---------------------------defaultwerte für parent-aktivität-------------------------

			}

			String objectType = "activity";
					

			String path = objectType + "$" + "default" + "_date";
			path = cleanParameterName(path);
			ods.logAccess(path);
			String label = "Allgemein" + "/Datum";
			addComponent(path, label, CAT_ACTIVITY);

			path = objectType + "$" + "default" + "_dayofweek";
			path = cleanParameterName(path);
			label = "Allgemein" + "/Wochentag";
			addComponent(path, label, CAT_ACTIVITY);

			path = objectType + "$" + "default" + "_time";
			path = cleanParameterName(path);
			label = "Allgemein" + "/Zeit";
			addComponent(path, label, CAT_ACTIVITY);

			path = objectType + "$" + "default" + "_place";
			path = cleanParameterName(path);
			label = "Allgemein" + "/Ort";
			addComponent(path, label, CAT_ACTIVITY);


			path = objectType + "$" + "default" + "_" + "note" + "$" + "Dokumenttext";
			path = cleanParameterName(path);
			label = "Allgemein" + "/" + "Dokumenttext";
			addComponent(path, label, CAT_ACTIVITY);

			path = objectType + "$" + "default" + "_" + "note" + "$" + "Notiz";
			path = cleanParameterName(path);
			label = "Allgemein" + "/" + "Notiz";
			addComponent(path, label, CAT_ACTIVITY);

			path = objectType + "$" + "default" + "_" + "note" + "$" + "Abmachungen";
			path = cleanParameterName(path);
			label = "Allgemein" + "/" + "Abmachungen";
			addComponent(path, label, CAT_ACTIVITY);

			path = objectType + "$" + "default" + "_" + "note" + "$" + "Zusammenfassung";
			path = cleanParameterName(path);
			label = "Allgemein" + "/" + "Zusammenfassung";
			addComponent(path, label, CAT_ACTIVITY);







				//-----------------------------------------------------------------------------------
			
		}
		
		Map <String, ScriptDefinition> scripts = ods.getScripts();
		for (ScriptDefinition script : scripts.values()) {
			if (script.isDocumentScript()) {
				String path = "script_" + script.toString();
				path = cleanParameterName(path); 
				addComponent(path, script.toString(), CAT_SCRIPT);
			}
		}
		
		generateImages();
	}
	
	public static String cleanParameterName(String parameterName) {
		return parameterName.replaceAll("[^\\p{javaLetterOrDigit}$]", "_").toLowerCase();
	}
	
	private void addComponent(String key, String label, String category) {
		components.put(key, new Component(key, label, category));
	}

	public String getJSON() {
		
		StringBuilder builder = new StringBuilder();
		builder.append("{\n");
		
		for (Component component : components.values()) {
			builder.append(component.getKey() + ": '" + component.getLabel() + "',\n");
		}
		builder.append("}");
		return builder.toString();
	}
	
	public String getJSONCategories() {
		
		StringBuilder builder = new StringBuilder();
		builder.append("{\n");
		
		for (Component component : components.values()) {
			builder.append(component.getKey() + ": '" + component.getCategory() + "',\n");
		}
		builder.append("}");
		return builder.toString();
	}
	
	// For Word client
	public String getClientMap() {
		StringBuilder sb = new StringBuilder();
		for (Component component : components.values()) {
			sb.append(component.getKey() + "=>" + component.getLabel() + "\n");
		}
		return sb.toString();
	}
	
	// For Word client
	public String getClientCategoryMap() {
		StringBuilder sb = new StringBuilder();
		for (Component component : components.values()) {
			sb.append(component.getKey() + "=>" + component.getCategory() + "\n");
		}
		return sb.toString();
	}

	
	private void generateImages() {
		try{
			BufferedImage dummyImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
			Graphics2D dummyGraphics = dummyImage.createGraphics();
			FontMetrics fm = dummyGraphics.getFontMetrics();
			RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			BufferedImage buf;
			for (Component component : components.values()) {
				File imgFile = new File(ods.getRootpath() + "/images/textcomponents/" + component.getKey() + ".png");
				if (!imgFile.exists()) {
					Rectangle2D stringBounds = fm.getStringBounds(component.getLabel(), dummyGraphics);
					int width = (int)stringBounds.getWidth() + 3;
					int heigth = 15;
					buf = new BufferedImage(width, heigth, BufferedImage.TYPE_INT_RGB);
					
					Graphics2D g = buf.createGraphics();
					g.addRenderingHints(hints);
										
					g.setColor(Color.WHITE);
					g.fillRect(0, 0, width, heigth);
					g.setColor(Color.BLACK);
					g.drawRect(0, 0, width - 1, heigth - 1);
					g.drawString(component.getLabel(), 1, 11);
					
					FileOutputStream fis = new FileOutputStream(imgFile); 
					ImageIO.write(buf, "png", fis);
				}
			}			
		}

		catch(Exception e){
			ods.writeError(e);
		}
		
	}
}

class Component {
	private String key;
	private String label;
	private String category;
	public Component(String key, String label, String category) {
		this.key = key;
		this.label = label;
		this.category = category;
	}
	
	public String getKey() {
		return key;
	}
	
	public String getLabel() {
		return label;
	}
	
	public String getCategory() {
		return category;
	}
}