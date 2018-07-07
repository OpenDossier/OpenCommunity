/*
 * Copyright (C) 2009 Andreas Kofler
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package ch.opencommunity.pdf;

import java.io.FileOutputStream;
import java.io.IOException;

import com.lowagie.text.*;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Chunk;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPRow;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPCell;





import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Vector;
import java.util.Date;
import java.text.*;

import org.kubiki.base.*;
import org.kubiki.document.*;
import org.kubiki.servlet.*;
import org.kubiki.ide.*;

public class PDFWriter{

	PdfWriter writer; 
	String rootpath;
	
	Date date;
	String dateString;
	
	public void createPDF(HttpServletRequest request, HttpServletResponse response, String filename, DocumentLibrary templib, Vector instances){

		WebApplication webapp = (WebApplication)templib.getRoot();
		rootpath = webapp.getRootpath();
		
		Paragraph p = null;
		
		date = new Date();
		SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
		dateString = df.format(date);

		
		Document document = new Document(PageSize.A4, milimeterToPoint(templib.getInt("MarginLeft")), milimeterToPoint(templib.getInt("MarginRight")), milimeterToPoint(templib.getInt("MarginTop")), milimeterToPoint(templib.getInt("MarginBottom")));
		try {

			
			response.setContentType("application/pdf");
			writer = PdfWriter.getInstance(document, new FileOutputStream(filename));


			document.open();


			for(int i = 0; i < instances.size(); i++){
				
				Message m = (Message)instances.elementAt(i);
				
				String[][] vars = m.getVars();
				

				BasicDocument template = (BasicDocument)templib.getObjectByName("BasicDocument", m.getString("template"));
				System.out.println(m.getString("template"));
				System.out.println(template);
				if(template != null){
					



			
				Vector pars = template.getObjects("BasicParagraph");
				
					for(int j = 0; j < pars.size(); j++){
						
						BasicClass bc = (BasicClass)pars.elementAt(j);
						
						if(i > 0 && bc.getBoolean("NewPage")==true){
							document.newPage();
						}
						if(bc.getClasstype().equals("org.kubiki.document.BasicParagraph")){
							
							
							
							BasicParagraph par = (BasicParagraph)bc;
							
							
							//Font font = getFont(par);

							String content = getContent(par, vars);	
							
							
							String[] pars2 = content.split("<p>");
							
							for(int k = 0; k < pars2.length; k++){

							
								p = new Paragraph();
								
								BasicParagraphStyle style = (BasicParagraphStyle)templib.getObjectByName("BasicParagraphStyle", par.getProperty("Style").getValue().toString());
								if(style != null){
									
									p.setSpacingBefore((new Float(style.getString("SpacingBefore"))).floatValue());
									p.setSpacingAfter((new Float(style.getString("SpacingAfter"))).floatValue());
									p.setIndentationLeft((new Float(style.getString("IndentationLeft"))).floatValue());	
									p.setIndentationRight((new Float(style.getString("IndentationRight"))).floatValue());	
									
									p.setFirstLineIndent((new Float(style.getString("IndentationFirstLine"))).floatValue());
									p.setLeading((new Float(style.getString("Leading"))).floatValue());	
									
								}	
								
								String[] lines = pars2[k].split("\n");
								
								for(int l = 0; l < lines.length; l++){					
									
									Font font = getFont(par);
									
									if(lines[l].startsWith("<b>")){
										if(font.getStyle()==Font.ITALIC){
											font.setStyle(Font.BOLDITALIC);	
										}
										else{
											font.setStyle(Font.BOLD);		
										}
										lines[l] = lines[l].replace("<b>","").replace("</b>","");
									}

									p.add(new Chunk(lines[l], font));
									p.add(Chunk.NEWLINE);
									
								}
	
								document.add(new Paragraph(p));
							}
							
						}
						else if(bc.getClasstype().equals("org.kubiki.document.BasicTextFrame")){
							BasicTextFrame btf = (BasicTextFrame)bc;	
							insertTextFrame(btf, writer, vars);						
						}
						else if(bc.getClasstype().equals("org.kubiki.document.BasicImage")){
							BasicImage bi = (BasicImage)bc;
							insertImage(bi, document, rootpath);
						}
		
					}
				}
			
			}

		} catch (DocumentException de) {
			System.err.println(de.getMessage());
			
			webapp.writeError(de.toString());
			
		} catch (IOException ioe) {
			System.err.println(ioe.getMessage());
			webapp.writeError(ioe.toString());
		}

		document.close();	
		
	}
	
	public int milimeterToPoint(int mm){
		
		return (int)(mm / 35.2777778);	
	}
	
	public void insertImage(BasicImage bi, Document doc, String rootpath){
		try{
			Image img = Image.getInstance(rootpath + "/images/" + bi.getString("File"));
			if(bi.getString("Alignment").equals("right")){
	        	img.setAlignment(Image.RIGHT);
	        }
	        img.scaleAbsolute(milimeterToPoint(bi.getInt("Width")), milimeterToPoint(bi.getInt("Height")));
	        doc.add(img);
		}
		catch(java.lang.Exception e){}		
		
	}
	public void insertTextFrame(BasicTextFrame btf, PdfWriter writer, String[][] vars){
		try{		
			PdfContentByte cb = writer.getDirectContent();
			PdfPTable table = new PdfPTable(1);
			
			float xPos = (float)milimeterToPoint(btf.getInt("XPos"));
			float yPos = PageSize.getRectangle("A4").getHeight() - (float)milimeterToPoint(btf.getInt("YPos"));
			float width = (float)milimeterToPoint(btf.getInt("Width"));
			
			float[] rows = { width };
			table.setTotalWidth(rows);
			table.getDefaultCell().setBorder(PdfPCell.NO_BORDER);

			
			Font font = getFont(btf);
			String content = getContent((BasicParagraph)btf, vars);
	
			table.addCell(new Phrase(content, font));
		
	
			table.writeSelectedRows(0, 1, xPos, yPos, cb);	
		}
		catch(java.lang.Exception e){}				
		
		
	}
	
	public Font getFont(BasicClass par){
		
		BaseFont bf = null;
		Font font = null;
		
		
		try{
			String fontname = par.getString("FontName");
			float fontsize = (new Float(par.getString("FontSize"))).floatValue();
								
								
			try{
				bf = BaseFont.createFont(rootpath + "/templates/fonts/" + fontname, BaseFont.CP1252 , true);
			}
			catch(java.lang.Exception e){
				
				bf = BaseFont.createFont();
				e.printStackTrace();
			}
			
			
			try{
				if(par.getBoolean("IsBold")==true){
					if(par.getBoolean("IsItalic")==true){
						font = new Font(bf, fontsize, Font.BOLDITALIC);	
					}
					else{
						font = new Font(bf, fontsize, Font.BOLD);
					}
				}
				else{
					if(par.getBoolean("IsItalic")==true){
						font = new Font(bf, fontsize, Font.ITALIC);	
					}
					else{
						font = new Font(bf, fontsize);
					}								
				}		
			}
			catch(java.lang.Exception e){}
		}
		catch(java.lang.Exception e){}
		
		return font;

	}
	public String getContent(BasicParagraph par, String[][] vars){
		
		String content = par.getContent();
							
		/*					
		content = content.replaceAll("Ã¼","ü");
		content = content.replaceAll("Ã¶","ö");
		content = content.replaceAll("Ã¤","ä"); 
		content = content.replaceAll("â","-");
		*/
		for(int k = 0; k < vars.length; k++){
								
			content = content.replaceAll("<#" + vars[k][0] + ">", vars[k][1]);		
								
		}	
		
		content = content.replaceAll("<@Datum>", dateString);
		
		return content;	
		
	}

}