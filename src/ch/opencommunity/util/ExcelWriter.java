package ch.opencommunity.util;

import jxl.*;
import jxl.write.*;

public class ExcelWriter{
	
	
	public static void addCell(WritableSheet sheet, int column, int row, String s, WritableCellFormat format){
		try{
			Label label;
			label = new Label(column, row, s, format);
			sheet.addCell(label);
		}
		catch(java.lang.Exception e){
			
		}
	}
	public static void addNumericCell(WritableSheet sheet, int column, int row, String s, WritableCellFormat format){
		try{
			double val = Double.parseDouble(s);
			jxl.write.Number n = new jxl.write.Number(column, row, val);
			sheet.addCell(n);
		}
		catch(java.lang.Exception e){
			
		}
	}
	public static void addFormulaCell(WritableSheet sheet, int column, int row, String formula, WritableCellFormat format){
		try{

			jxl.write.Formula f = new jxl.write.Formula(column, row, formula);
			//jxl.write.Formula f = new jxl.write.Formula(column, row, "(1+1)");
			sheet.addCell(f);
		}
		catch(java.lang.Exception e){
			
		}
	}
	
	
	
}