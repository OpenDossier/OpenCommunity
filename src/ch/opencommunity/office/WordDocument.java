package ch.opencommunity.office;

import java.io.*;

import javax.servlet.http.HttpServletResponse;

import org.kubiki.base.BasicClass;

public class WordDocument extends BasicClass {
	
	// save XHTML in a way that can be read by Word	
	public static String saveXHTML(String filePath, String rootPath, String content) throws IOException {
		File file = new File(filePath);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
		writer.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
		writer.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n");
		writer.append("<head>\n");
		writer.append("<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" />\n");
		String styleSheetPath = rootPath + File.separator + "templates" + File.separator + "xhtml" + File.separator + "style.css";
		File styleSheet = new File(styleSheetPath);
		if (styleSheet.exists()) {
			BufferedReader reader =  new BufferedReader(new FileReader(styleSheet));
			writer.append("<style type=\"text/css\">\n");
			String line = null;
			while ((line = reader.readLine()) != null) {
				writer.append(line + "\n");
	        }
 			writer.append("</style>\n");
		}
		writer.append("</head>\n");
		writer.append("<body>\n");
		writer.append(content);
		writer.append("</body>\n");
		writer.append("</html>\n");

		writer.close();
		return filePath;
	}

	// Send Word document to client
	public static void writeFileResponse(File file, HttpServletResponse response) throws IOException {
		if (file != null) {
			response.setContentType("application/msword");
			response.setHeader("X-Filename", file.getName());
			OutputStream out = response.getOutputStream();
			FileInputStream in = new FileInputStream(file);
			byte[] buf = new byte[8192];
	        int count = 0;
	        do
	        {
	            count = in.read(buf, 0, buf.length);
	
	            if (count > 0)
	            {
	                out.write(buf, 0, count);
	            }
	        }
	        while (count > 0);
	
	        in.close();
	        out.close();
		}
	}
	
	public static void copyFile(File source, File target) throws IOException {
		target.getParentFile().mkdirs();
		FileInputStream  in = new FileInputStream (source);
	    FileOutputStream out = new FileOutputStream(target);
	    byte[] buf = new byte[1024];
	    int len;
	    while ((len = in.read(buf)) > 0) {
	    	out.write(buf, 0, len);
	    }

	    in.close();
	    out.close();
	}	
}

