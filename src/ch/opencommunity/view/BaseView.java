package ch.opencommunity.view;

import ch.opencommunity.common.OpenCommunityUserSession;
import ch.opencommunity.base.Person;
import ch.opencommunity.base.Identity;
import ch.opencommunity.base.Address;
import ch.opencommunity.base.Login;
import ch.opencommunity.base.Note;
import ch.opencommunity.base.Activity;

import ch.opencommunity.base.OrganisationMember;
import ch.opencommunity.base.OrganisationMemberController;

import ch.opencommunity.server.HTMLForm;
import ch.opencommunity.server.OpenCommunityServer;
import org.kubiki.application.ApplicationContext;

import org.kubiki.base.Property;
import org.kubiki.base.ObjectCollection;
import org.kubiki.base.BasicClass;
import org.kubiki.util.DateConverter;

import java.util.Hashtable;

public class BaseView{
	
	public static void addTableRow(StringBuilder html, Property p, String label){
		addTableRow(html, p, label, 1);
	}
	public static void addTableRow(StringBuilder html, Property p, String label, int type){
		addTableRow(html, p, label, "", type);		
	}
	public static void addTableRow(StringBuilder html, Property p, String label, String prefix, int type){
		html.append("<tr><td>" + label + "</td>");
		if(type==1){
			html.append("<td>" + HTMLForm.getTextField(p , true, prefix) + "</td></tr>");
		}
		else if(type==2){
			html.append("<td>" + HTMLForm.getSelection(p , true, prefix) + "</td></tr>");		
		}
		else if(type==3){
			html.append("<td>" + HTMLForm.getTextArea(p , true, "textarea_small", prefix) + "</td></tr>");		
		}
		else if(type==4){
			html.append("<td>" + HTMLForm.getRadioButton(p , true, prefix) + "</td></tr>");		
		}
	}
	
	
	
}