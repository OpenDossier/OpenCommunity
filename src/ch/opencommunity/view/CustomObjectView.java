package ch.opencommunity.view;

import org.kubiki.application.ApplicationContext;
import org.kubiki.base.BasicClass;
import org.kubiki.base.Property;
import ch.opencommunity.server.HTMLForm;

public abstract class CustomObjectView extends ObjectView {
	private boolean useDefaultTabbar;
	private boolean useDefaultToolbar;
	
	public CustomObjectView(String id, String label, boolean useDefaultTabbar, boolean useDefaultToolbar, boolean display) {
		super(id, label, display);
		this.useDefaultTabbar = useDefaultTabbar;
		this.useDefaultToolbar = useDefaultToolbar;
	}
	
	public abstract void getHtml(StringBuilder html, BasicClass bc, boolean isEditable, ApplicationContext context);
	
	public boolean useDefaultTabbar() {
		return useDefaultTabbar;
	}
	public boolean useDefaultToolbar() {
		return useDefaultToolbar;
	}
	
	public String getSaveCode(BasicClass bc, String returnTo) {
		return null;
	}
	
	protected void getRow(StringBuilder html, Property p, boolean isEditable) {
		html.append("<tr><td class=\"labelColumn\">");
		html.append(p.getLabel());
		html.append("</td><td>");
		if (p.getType().equals("Date")) {
			html.append(HTMLForm.getDateField(p, isEditable, ""));
		}
		else {
			html.append(HTMLForm.getTextField(p, isEditable, ""));
		}
		html.append("</td></tr>");

	}
	
}
