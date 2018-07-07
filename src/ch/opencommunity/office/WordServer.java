package ch.opencommunity.office;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.kubiki.base.BasicClass;
import ch.opencommunity.common.OpenCommunityUserSession;
import ch.opencommunity.server.OpenCommunityServer;
import ch.opencommunity.util.TextComponents;

public class WordServer {
	// Word authentication with temporary ID
	public static void onWordClientAction(String action, HttpServletRequest request, HttpServletResponse response,
			OpenCommunityServer ods, OpenCommunityUserSession userSession, String tempID) throws IOException {
		boolean isHandled = false;
		if (userSession != null) {
			String objectPath = request.getParameter("objectPath");
			if (objectPath == null || objectPath.isEmpty()) {
				objectPath = userSession.getWordClientDocument(tempID);
			}
			if (objectPath != null) {
				BasicClass bc = ods.getObjectByPath(objectPath);
				if (bc instanceof WordServerInterface) {
					WordServerInterface wsi = (WordServerInterface)bc;
					isHandled = wsi.onWordClientAction(action, request, response, userSession);
				}
			}
		}
		if (!isHandled) {
			onGlobalWordClientAction(action, request, response, ods, userSession);
		}
	}
	
	// Word authentication with session cookie
	public static void onWordClientAction(String action, HttpServletRequest request, HttpServletResponse response,
				OpenCommunityServer ods, OpenCommunityUserSession userSession) throws IOException {
		if (userSession != null) {
			boolean isHandled = false;
			String objectPath = request.getParameter("objectPath");
			if (objectPath != null) {
				BasicClass bc = ods.getObjectByPath(objectPath);
				if (bc instanceof WordServerInterface) {
					ods.logAccess(bc);
					WordServerInterface wsi = (WordServerInterface)bc;
					isHandled = wsi.onWordClientAction(action, request, response, userSession);
				}
			}
			if (!isHandled) {
				onGlobalWordClientAction(action, request, response, ods, userSession);
			}
		}
		else {
			response.sendError(401, "Unauthorized");
		}
	}
	
	// The user's credentials might not yet be checked when this function is called, 
	// so it shouldn't return confidential data without checking!
	private static void onGlobalWordClientAction(String action, HttpServletRequest request, HttpServletResponse response,
			OpenCommunityServer ods, OpenCommunityUserSession userSession) throws IOException {
		int organisationID = -1; // no organisation
		if (userSession != null) {
			if (userSession.getLoginID() == 0) {
				organisationID = 0; // admin has permission for all organisations
			}
			else {
				organisationID = userSession.getOrganisationID(); // only access current organisation
			}
		}
		if (action.equals("getTemplateLibraries")) {
			PrintWriter writer = response.getWriter();
			writer.print("<TemplateLibraries>");
			Vector<BasicClass> templibs = ods.getObjects("DocumentTemplateLibrary");
			for (BasicClass templib : templibs) {
				if (organisationID == 0 || organisationID == templib.getID("Owner")) {
					writer.print(templib.toXml());
				}
			}
			writer.print("</TemplateLibraries>");
		}
		else if (action.equals("getTextBlockAdministrations")) {
			PrintWriter writer = response.getWriter();
			writer.print("<TextBlockAdministrations>");
			Vector<BasicClass> tbLibs = ods.getObjects("TextBlockAdministration");
			for (BasicClass tbLib : tbLibs) {
				if (organisationID == 0 || organisationID == tbLib.getID("Owner")) {
					writer.print(tbLib.toXml());
				}
			}
			writer.print("</TextBlockAdministrations>");
		}
		else if (action.equals("getTextComponents")) {
			PrintWriter writer = response.getWriter();
			TextComponents components = new TextComponents(ods);
			writer.print(components.getClientMap());
/*			// Workaround zum Konfigurieren der Dokumentvariablen
			ScriptDefinition sd = (ScriptDefinition)ods.getObjectByName("ScriptDefinition", "2");
			if(sd != null){
				writer.print(sd.getString("ScriptBody"));
			} */
		}			
		else if (action.equals("getTextComponentCategories")) {
			PrintWriter writer = response.getWriter();
			TextComponents components = new TextComponents(ods);
			writer.print(components.getClientCategoryMap());
/*			// Workaround zum Konfigurieren der Dokumentvariablen
			ScriptDefinition sd = (ScriptDefinition)ods.getObjectByName("ScriptDefinition", "2");
			if(sd != null){
				writer.print(sd.getString("ScriptBody"));
			} */
		}			
	}
}
