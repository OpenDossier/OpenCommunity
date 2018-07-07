package ch.opencommunity.util;

import org.kubiki.base.BasicClass;
import ch.opencommunity.common.OpenCommunityUserSession;
import ch.opencommunity.server.OpenCommunityServer;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.*;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.fop.apps.*;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PRAcroForm;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.SimpleBookmark;

public class PDFCreator extends BasicClass{
    private FopFactory fopFactory = FopFactory.newInstance();
    private String m_strBasePath;
    private String m_strInput = new String();
    private String m_strStyles = new String();
    private Hashtable<String, String> m_arrTags = new Hashtable<String, String>();
    private String m_strLastOutput;

    private ch.opencommunity.base.Document document = null;


    public PDFCreator() throws
		org.xml.sax.SAXException,
		IOException
    {
		initialize(".", "styles.xsl", "fopconfig.xml");
    }

    public PDFCreator(String strBasePath) throws 
		org.xml.sax.SAXException,
		IOException
    {
		initialize(strBasePath, "styles.xsl", "fopconfig.xml");
    }

    public PDFCreator(String strBasePath, ch.opencommunity.base.Document document) throws 
		org.xml.sax.SAXException,
		IOException
    {
		this.document = document;
		initialize(strBasePath, "styles.xsl", "fopconfig.xml");
    }

    public PDFCreator(String strBasePath, String strStyles) throws 
		org.xml.sax.SAXException,
		IOException
    {
		initialize(strBasePath, strStyles, "fopconfig.xml");
    }

    public PDFCreator(String strBasePath, String strStyles, String strConfig) throws 
		org.xml.sax.SAXException,
		IOException
    {
		initialize(strBasePath, strStyles, strConfig);
    }
    
    private void initialize(String strBasePath, String strStyles, String strConfig) throws
		org.xml.sax.SAXException, 
		IOException
    {
		m_strBasePath = strBasePath;
		m_strStyles = strStyles;

		fopFactory.setUserConfig(new File(m_strBasePath + File.separator + "conf" + File.separator + strConfig));	
    }

    // setTag sets the tag in strKey to strValue. Tags are replaced in the input buffer before the PDF is rendered
    // A tag with the key "TAGNAME" looks like this in the html code: <code>TAGNAME</code>
    
    public void setTag(String strKey, String strValue) {
		m_arrTags.put(strKey, strValue);
    }

    // setTags copies all the stored mappings from the supplied hashtable into the tag table

    public void setTags(Map<String,String> ht) {
		m_arrTags.putAll(ht);
    }

    // resetTags clears all tags in the system

    public void resetTags() {
		m_arrTags.clear();
    }

    // reset sets the input buffer to empty. The PDFCreator object may now be reused for a different input

    public void reset() {
		m_strInput = "";
    }

    // addSnippet adds a snippet of html text to the input buffer

    public void addSnippet(String strSnippet) {
		m_strInput += strSnippet;
    }

    // addTemplate adds a template file (in the directory templates) to the input buffer

    public void addTemplate(String strTemplate) throws IOException {
		String strFile = m_strBasePath+"/templates/"+strTemplate;
		addFile(strFile);
    }

    // addFile adds any file given to the input buffer

    public void addFile(String strFile) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(strFile));
		String text = null;
	
		// repeat until all lines is read
		while ((text = reader.readLine()) != null) {
			m_strInput = m_strInput+text+System.getProperty("line.separator");
		}

		if (reader != null) {
			reader.close();
		}
    }
    
    public void replaceTags(OpenCommunityUserSession userSession) {
    	m_strInput = replaceTags(m_strInput, userSession);	
    }

    private String replaceTags(String strInput, OpenCommunityUserSession userSession) {
		OpenCommunityServer ods = (OpenCommunityServer)getRoot();
		StringBuilder buffer = new StringBuilder();

		int pos = 0;
		Pattern pattern = Pattern.compile("<img .*?src=\"images/textcomponents/(.*?)\\.png\".*? />");
		Matcher matcher = pattern.matcher(strInput);
		while (matcher.find()) {
			String key = matcher.group(1);
			
			String value = "";
			if (key.startsWith("script_")) {
				//value = (String)ods.executeScript(m_arrTags.get(key), document, null, userSession);
			}	
			else {
				value = m_arrTags.get(key);
			}
			buffer.append(strInput.substring(pos, matcher.start()));
			if (value != null) {
				buffer.append(value);
			}
			pos = matcher.end();
		}
		buffer.append(strInput.substring(pos));
		
		String test = "";
		Object[] keys = m_arrTags.keySet().toArray();
		for(int i = 0; i < keys.length; i++){
		      test += "<li>" + keys[i] + "</li>";

		}
		ods.writeError(test);
		return buffer.toString();
    }

    // concatenate appends the second pdf to the first one

    @SuppressWarnings("unchecked")
	private void concatenate(String strPDF1, String strPDF2) throws
		java.io.FileNotFoundException,
		java.io.IOException,
		com.lowagie.text.pdf.BadPdfFormatException,
		com.lowagie.text.DocumentException
    {
		// first rename PDF1
		File pdf1 = new File(strPDF1);
		File tempfile = File.createTempFile("sbrief", ".pdf", pdf1.getParentFile());

		pdf1.renameTo(tempfile);

		String outFile = strPDF1;
		strPDF1 = tempfile.getPath();

		// now write strPDF1 and strPDF1 to strOutput

		int pageOffset = 0;
		ArrayList master = new ArrayList();
		Document tempDocument = null;
		PdfCopy writer = null;

		// the first PDF

		PdfReader reader = new PdfReader(strPDF1);
		reader.consolidateNamedDestinations();
		int n = reader.getNumberOfPages();
		List bookmarks = SimpleBookmark.getBookmark(reader);
		if (bookmarks != null) {
			if (pageOffset != 0) {
				SimpleBookmark.shiftPageNumbers(bookmarks, pageOffset, null);
			}
			master.addAll(bookmarks);
		}
		pageOffset += n;

		tempDocument = new Document(reader.getPageSizeWithRotation(1));
		writer = new PdfCopy(tempDocument, new FileOutputStream(outFile));
		tempDocument.open();

		PdfImportedPage page;
		for (int i = 0; i < n;) {
			++i;
			page = writer.getImportedPage(reader, i);
			writer.addPage(page);
		}
		PRAcroForm form = reader.getAcroForm();
		if (form != null) {
			writer.copyAcroForm(reader);
		}

		// the second PDF

		reader = new PdfReader(strPDF2);
		reader.consolidateNamedDestinations();
		n = reader.getNumberOfPages();
		bookmarks = SimpleBookmark.getBookmark(reader);
		if (bookmarks != null) {
			if (pageOffset != 0) {
				SimpleBookmark.shiftPageNumbers(bookmarks, pageOffset,null);
			}
			master.addAll(bookmarks);
		}
		pageOffset += n;

		for (int i = 0; i < n;) {
			++i;
			page = writer.getImportedPage(reader, i);
			writer.addPage(page);
		}
		form = reader.getAcroForm();
		if (form != null) {
			writer.copyAcroForm(reader);
		}

		if (!master.isEmpty()) {
			writer.setOutlines(master);
		}

		tempDocument.close();

		// now remove the temporary file
	
		tempfile.delete();
    }

    // this version of generate appends the new pdf to the last pdf that was created with this object

    public void generate() throws
		org.apache.fop.apps.FOPException,
		java.io.FileNotFoundException,
		javax.xml.transform.TransformerConfigurationException,
		javax.xml.transform.TransformerException,
		java.io.IOException,
		com.lowagie.text.pdf.BadPdfFormatException,
		com.lowagie.text.DocumentException
    {
		if (m_strLastOutput != null) {
			File tempfile = File.createTempFile("sbrief", ".pdf");

			internalgenerate(tempfile.getPath());

			concatenate(m_strLastOutput, tempfile.getPath());

			tempfile.delete();
		}
    }

    // generate() creates a PDF and remembers its name

    public void generate(String output) throws
		org.apache.fop.apps.FOPException,
		java.io.FileNotFoundException,
		javax.xml.transform.TransformerConfigurationException,
		javax.xml.transform.TransformerException,
		java.io.IOException
    {
		if (output != null) {
			m_strLastOutput = new String(output);

			internalgenerate(output);
			
		}
		else {
			throw new FileNotFoundException("Please specify an output file");
		}
    }

    // internalgenerate() first replaces all tags in the input buffer with the settings in its tag table.
    // The input buffer is hereby left alone (the procedure works on a copy). Next, the input buffer
    // is converted to FO using the styles xsl file. And finally, the document is rendered and saved in 
    // the file provided.

    private void internalgenerate(String output) throws
		org.apache.fop.apps.FOPException,
		java.io.FileNotFoundException,
		javax.xml.transform.TransformerConfigurationException,
		javax.xml.transform.TransformerException,
		java.io.IOException
    {
		OutputStream out = new BufferedOutputStream(new FileOutputStream(new File(output)));
		// Construct fop with PDF as output format


	    
		Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);
		// Setup JAXP using xhtml2fo transformer
		Source xslt = new StreamSource(new File(m_strBasePath+"/conf/"+m_strStyles));
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer(xslt);				  
		    
		// Setup input and output for XSLT transformation
		Source src = new StreamSource(new StringReader(m_strInput));
		    
		// Resulting SAX events (the generated FO) must be piped through to FOP
		Result res = new SAXResult(fop.getDefaultHandler());
		    
		// Start XSLT transformation and FOP processing
		transformer.transform(src, res);

	    
		out.close();
		
    }
}
