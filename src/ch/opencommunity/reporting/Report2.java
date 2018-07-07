package ch.opencommunity.reporting;

import ch.opencommunity.server.OpenCommunityServer;
import ch.opencommunity.base.FieldDefinition;

import org.kubiki.reporting.*;

import org.kubiki.base.ConfigValue;
import org.kubiki.base.BasicClass;
import org.kubiki.base.ActionHandler;
import org.kubiki.base.ActionResult;
import org.kubiki.application.ApplicationContext;

import java.util.HashMap;
import java.util.Vector;

public class Report2 extends BaseReport{
	
	HashMap labels = null;
	
	public Report2(){
		
		setProperty("Title", "Inserate und Kontaktbestellungen");	
		setProperty("InitialParameters", "useronly=1&new2016=1");
		
		labels = new HashMap();
		
	}
	public void initObjectLocal(){
		
		HashMap catlabels = new HashMap();
		
		OpenCommunityServer ocs = (OpenCommunityServer)getRoot();
		for(BasicClass category : ocs.getMemberAdAdministration().getCategories()){
			for(BasicClass fd : category.getObjects("FieldDefinition")){
				
				Vector codeList = ((FieldDefinition)fd).getCodeList();
				for(int i = 0; i < codeList.size(); i++){
					ConfigValue cv = (ConfigValue)codeList.elementAt(i);
					String key = category.getName() + "_" + fd.getName() + "_" + cv.getValue();
					catlabels.put(key, cv.getLabel());
				}
				
			}
			
			
		}
		labels.put("SUBKATEGORIE", catlabels);
	}
	public void execute(ApplicationContext context, ActionResult result){
		
		
		StringBuilder html = new StringBuilder();
		
		html.append("<table>");
		
		html.append("<tr><td class=\"tableheader\">2016</td><td class=\"tableheader\">2017</td></tr>");
		
		html.append("<tr>");
		
		

		
		
		//-------------------------------------------------------------------------------------------------------------------------
		
		html.append("<td valign=\"top\">");
		
		html.append("<p class=\"charttitle\">Inserate nach Typ</p>");
		
		
		String sql = "SELECT COUNT(t1.ID), CASE WHEN t1.ISOFFER=true AND t1.ISREQUEST=true THEN 'TANDEM' WHEN t1.ISOFFER=true THEN 'ANGEBOT' ELSE 'NACHFRAGE' END AS Typ";
		sql += " FROM MemberAd AS t1";
		sql += " WHERE t1.ValidFrom >= '2016-01-01' AND t1.ValidFrom <= '2016-12-31'";
		sql += " GROUP BY CASE WHEN t1.ISOFFER=true AND t1.ISREQUEST=true THEN 'TANDEM' WHEN t1.ISOFFER=true THEN 'ANGEBOT' ELSE 'NACHFRAGE' END";
		sql += " ORDER BY 1";
		
		html.append(getQueryTable(sql));
		
		html.append("</td><td valign=\"top\">");
		
		html.append("<p class=\"charttitle\">Inserate nach Typ</p>");
		
		
		sql = "SELECT COUNT(t1.ID), CASE WHEN t1.ISOFFER=true AND t1.ISREQUEST=true THEN 'TANDEM' WHEN t1.ISOFFER=true THEN 'ANGEBOT' ELSE 'NACHFRAGE' END AS Typ";
		sql += " FROM MemberAd AS t1";
		sql += " WHERE t1.ValidFrom >= '2017-01-01' AND t1.ValidFrom <= '2017-12-31'";
		sql += " GROUP BY CASE WHEN t1.ISOFFER=true AND t1.ISREQUEST=true THEN 'TANDEM' WHEN t1.ISOFFER=true THEN 'ANGEBOT' ELSE 'NACHFRAGE' END";
		sql += " ORDER BY 1";
		
		html.append(getQueryTable(sql));
		
		html.append("</td></tr><tr>");
		
		//-------------------------------------------------------------------------------------------------------------------------
		
		html.append("<td valign=\"top\">");
		
		html.append("<p class=\"charttitle\">Inserate nach Rubrik</p>");
		
		
		sql = "SELECT COUNT(t1.ID), t2.Title";
		sql += " FROM MemberAd AS t1";
		sql += " JOIN MemberAdCategory AS t2 ON t1.Template=t2.ID";
		sql += " WHERE t1.ValidFrom >= '2016-01-01' AND t1.ValidFrom <= '2016-12-31'";
		sql += " GROUP BY t2.Title";
		sql += " ORDER BY 1";
		
		html.append(getQueryTable(sql));
		
		html.append("</td><td valign=\"top\">");
		
		html.append("<p class=\"charttitle\">Inserate nach Rubrik</p>");
		
		
		sql = "SELECT COUNT(t1.ID), t2.Title";
		sql += " FROM MemberAd AS t1";
		sql += " JOIN MemberAdCategory AS t2 ON t1.Template=t2.ID";
		sql += " WHERE t1.ValidFrom >= '2017-01-01' AND t1.ValidFrom <= '2017-12-31'";
		sql += " GROUP BY t2.Title";
		sql += " ORDER BY 1";
		
		html.append(getQueryTable(sql));
		
		html.append("</td></tr><tr>");
		
		//-------------------------------------------------------------------------------------------------------------------------
		
		html.append("<td valign=\"top\">");
		
		html.append("<p class=\"charttitle\">Inserate nach Rubrik und Typ</p>");
		
		
		sql = "SELECT COUNT(t1.ID), t2.Title, CASE WHEN t1.ISOFFER=true AND t1.ISREQUEST=true THEN 'TANDEM' WHEN t1.ISOFFER=true THEN 'ANGEBOT' ELSE 'NACHFRAGE' END AS Typ";
		sql += " FROM MemberAd AS t1";
		sql += " JOIN MemberAdCategory AS t2 ON t1.Template=t2.ID";
		sql += " WHERE t1.ValidFrom >= '2016-01-01' AND t1.ValidFrom <= '2016-12-31'";
		sql += " GROUP BY t2.Title, CASE WHEN t1.ISOFFER=true AND t1.ISREQUEST=true THEN 'TANDEM' WHEN t1.ISOFFER=true THEN 'ANGEBOT' ELSE 'NACHFRAGE' END";
		sql += " ORDER BY 2";
		
		html.append(getQueryTable(sql));
		
		html.append("</td><td valign=\"top\">");
		
		html.append("<p class=\"charttitle\">Inserate nach Rubrik und Typ</p>");
		
		
		sql = "SELECT COUNT(t1.ID), t2.Title, CASE WHEN t1.ISOFFER=true AND t1.ISREQUEST=true THEN 'TANDEM' WHEN t1.ISOFFER=true THEN 'ANGEBOT' ELSE 'NACHFRAGE' END AS Typ";
		sql += " FROM MemberAd AS t1";
		sql += " JOIN MemberAdCategory AS t2 ON t1.Template=t2.ID";
		sql += " WHERE t1.ValidFrom >= '2017-01-01' AND t1.ValidFrom <= '2017-12-31'";
		sql += " GROUP BY t2.Title, CASE WHEN t1.ISOFFER=true AND t1.ISREQUEST=true THEN 'TANDEM' WHEN t1.ISOFFER=true THEN 'ANGEBOT' ELSE 'NACHFRAGE' END";
		sql += " ORDER BY 2";
		
		html.append(getQueryTable(sql));
		
		html.append("</td></tr><tr>");
		
		//-------------------------------------------------------------------------------------------------------------------------
		
		html.append("<td valign=\"top\">");
		
		html.append("<p class=\"charttitle\">Inserate nach Rubrik und Unterrubrik</p>");
		
		
		sql = "SELECT COUNT(t1.ID), t2.Title, CASE WHEN t1.ISOFFER=true AND t1.ISREQUEST=true THEN 'TANDEM' WHEN t1.ISOFFER=true THEN 'ANGEBOT' ELSE 'NACHFRAGE' END AS Typ,";
		sql += " (t2.ID || '_' || t4.Template || '_' || t4.Value) AS SUBKATEGORIE";
		sql += " FROM MemberAd AS t1";
		sql += " JOIN MemberAdCategory AS t2 ON t1.Template=t2.ID";
		sql += " LEFT JOIN Parameter AS t4 ON t4.MemberAdID=t1.ID";
		sql += " WHERE t1.ValidFrom >= '2016-01-01' AND t1.ValidFrom <= '2016-12-31'";
		sql += " GROUP BY t2.Title, CASE WHEN t1.ISOFFER=true AND t1.ISREQUEST=true THEN 'TANDEM' WHEN t1.ISOFFER=true THEN 'ANGEBOT' ELSE 'NACHFRAGE' END,";
		sql += " (t2.ID || '_' || t4.Template || '_' || t4.Value)";
		sql += " ORDER BY 2";
		
		html.append(getQueryTable(sql, labels));
		
		html.append("</td><td valign=\"top\">");
		
		html.append("<p class=\"charttitle\">Inserate nach Rubrik und Unterrubrik</p>");
		
		
		sql = "SELECT COUNT(t1.ID), t2.Title, CASE WHEN t1.ISOFFER=true AND t1.ISREQUEST=true THEN 'TANDEM' WHEN t1.ISOFFER=true THEN 'ANGEBOT' ELSE 'NACHFRAGE' END AS Typ,";
		sql += " (t2.ID || '_' || t4.Template || '_' || t4.Value) AS SUBKATEGORIE";
		sql += " FROM MemberAd AS t1";
		sql += " JOIN MemberAdCategory AS t2 ON t1.Template=t2.ID";
		sql += " LEFT JOIN Parameter AS t4 ON t4.MemberAdID=t1.ID";
		sql += " WHERE t1.ValidFrom >= '2017-01-01' AND t1.ValidFrom <= '2017-12-31'";
		sql += " GROUP BY t2.Title, CASE WHEN t1.ISOFFER=true AND t1.ISREQUEST=true THEN 'TANDEM' WHEN t1.ISOFFER=true THEN 'ANGEBOT' ELSE 'NACHFRAGE' END,";
		sql += " (t2.ID || '_' || t4.Template || '_' || t4.Value)";
		sql += " ORDER BY 2";
		
		html.append(getQueryTable(sql, labels));
		
		html.append("</td></tr><tr>");
		
		
		//-------------------------------------------------------------------------------------------------------------------------
		
		html.append("<td valign=\"top\">");
		
		html.append("<p class=\"charttitle\">Kontaktbestellungen nach Rubrik</p>");
		
		
		sql = "SELECT COUNT(t3.ID), t2.Title, CASE WHEN t1.ISOFFER=true AND t1.ISREQUEST=true THEN 'TANDEM' WHEN t1.ISOFFER=true THEN 'ANGEBOT' ELSE 'NACHFRAGE' END AS Typ";
		sql += " FROM MemberAd AS t1";
		sql += " JOIN MemberAdCategory AS t2 ON t1.Template=t2.ID";
		sql += " JOIN MemberAdRequest AS t3 ON t3.MemberAd=t1.ID";
		sql += " WHERE (t3.ValidFrom >= '2016-01-01' AND t3.ValidFrom <= '2016-12-31') OR (t3.DateCreated >= '2016-01-01' AND t3.DateCreated <= '2016-12-31')";
		sql += " GROUP BY t2.Title, CASE WHEN t1.ISOFFER=true AND t1.ISREQUEST=true THEN 'TANDEM' WHEN t1.ISOFFER=true THEN 'ANGEBOT' ELSE 'NACHFRAGE' END";
		sql += " ORDER BY 2";
		
		html.append(getQueryTable(sql));
		
		html.append("</td><td valign=\"top\">");
		
		html.append("<p class=\"charttitle\">Kontaktbestellungen nach Rubrik</p>");
		
		
		sql = "SELECT COUNT(t3.ID), t2.Title, CASE WHEN t1.ISOFFER=true AND t1.ISREQUEST=true THEN 'TANDEM' WHEN t1.ISOFFER=true THEN 'ANGEBOT' ELSE 'NACHFRAGE' END AS Typ";
		sql += " FROM MemberAd AS t1";
		sql += " JOIN MemberAdCategory AS t2 ON t1.Template=t2.ID";
		sql += " JOIN MemberAdRequest AS t3 ON t3.MemberAd=t1.ID";
		sql += " WHERE (t3.ValidFrom >= '2017-01-01' AND t3.ValidFrom <= '2017-12-31') OR (t3.DateCreated >= '2017-01-01' AND t3.DateCreated <= '2017-12-31')";
		sql += " GROUP BY t2.Title, CASE WHEN t1.ISOFFER=true AND t1.ISREQUEST=true THEN 'TANDEM' WHEN t1.ISOFFER=true THEN 'ANGEBOT' ELSE 'NACHFRAGE' END";
		sql += " ORDER BY 2";
		
		html.append(getQueryTable(sql));
		
		html.append("</td></tr><tr>");
		
		//-------------------------------------------------------------------------------------------------------------------------
		
		html.append("<td valign=\"top\">");
		
		html.append("<p class=\"charttitle\">Kontaktbestellungen nach Rubrik und  und Unterrubrik</p>");
		
		
		sql = "SELECT COUNT(t3.ID), t2.Title, CASE WHEN t1.ISOFFER=true AND t1.ISREQUEST=true THEN 'TANDEM' WHEN t1.ISOFFER=true THEN 'ANGEBOT' ELSE 'NACHFRAGE' END AS Typ,";
		sql += " (t2.ID || '_' || t4.Template || '_' || t4.Value) AS SUBKATEGORIE";
		sql += " FROM MemberAd AS t1";
		sql += " JOIN MemberAdCategory AS t2 ON t1.Template=t2.ID";
		sql += " JOIN MemberAdRequest AS t3 ON t3.MemberAd=t1.ID";
		sql += " LEFT JOIN Parameter AS t4 ON t4.MemberAdID=t1.ID";
		sql += " WHERE (t3.ValidFrom >= '2016-01-01' AND t3.ValidFrom <= '2016-12-31') OR (t3.DateCreated >= '2016-01-01' AND t3.DateCreated <= '2016-12-31')";
		sql += " GROUP BY t2.Title, CASE WHEN t1.ISOFFER=true AND t1.ISREQUEST=true THEN 'TANDEM' WHEN t1.ISOFFER=true THEN 'ANGEBOT' ELSE 'NACHFRAGE' END,";
		sql += " (t2.ID || '_' || t4.Template || '_' || t4.Value)";
		sql += " ORDER BY 2";
		
		html.append(getQueryTable(sql, labels));
		
		html.append("</td><td valign=\"top\">");
		
		html.append("<p class=\"charttitle\">Kontaktbestellungen nach Rubrik und  und Unterrubrik</p>");
		
		
		sql = "SELECT COUNT(t3.ID), t2.Title, CASE WHEN t1.ISOFFER=true AND t1.ISREQUEST=true THEN 'TANDEM' WHEN t1.ISOFFER=true THEN 'ANGEBOT' ELSE 'NACHFRAGE' END AS Typ,";
		sql += " (t2.ID || '_' || t4.Template || '_' || t4.Value) AS SUBKATEGORIE";
		sql += " FROM MemberAd AS t1";
		sql += " JOIN MemberAdCategory AS t2 ON t1.Template=t2.ID";
		sql += " JOIN MemberAdRequest AS t3 ON t3.MemberAd=t1.ID";
		sql += " LEFT JOIN Parameter AS t4 ON t4.MemberAdID=t1.ID";
		sql += " WHERE (t3.ValidFrom >= '2017-01-01' AND t3.ValidFrom <= '2017-12-31') OR (t3.DateCreated >= '2017-01-01' AND t3.DateCreated <= '2017-12-31')";
		sql += " GROUP BY t2.Title, CASE WHEN t1.ISOFFER=true AND t1.ISREQUEST=true THEN 'TANDEM' WHEN t1.ISOFFER=true THEN 'ANGEBOT' ELSE 'NACHFRAGE' END,";
		sql += " (t2.ID || '_' || t4.Template || '_' || t4.Value)";
		sql += " ORDER BY 2";
		
		html.append(getQueryTable(sql, labels));
		
		html.append("</td></tr></table>");		
		
		
		result.setData(html.toString());
		result.setParam("dataContainer", "editArea");
		
		
	}
}