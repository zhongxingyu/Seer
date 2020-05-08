 package info.plagiatsjaeger;
 
 import info.plagiatsjaeger.enums.FileType;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.nio.charset.Charset;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.log4j.Logger;
 import org.apache.pdfbox.cos.COSDocument;
 import org.apache.pdfbox.pdfparser.PDFParser;
 import org.apache.pdfbox.pdmodel.PDDocument;
 import org.apache.pdfbox.util.PDFTextStripper;
 import org.apache.poi.hwpf.HWPFDocument;
 import org.apache.poi.hwpf.extractor.WordExtractor;
 import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
 import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
 import org.apache.poi.poifs.filesystem.POIFSFileSystem;
 import org.apache.xmlbeans.XmlException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 
 /**
  * @author Christian
  */
 public class FileParser
 {
 	private File				_file;
 	private static final Logger	_logger	= Logger.getLogger(FileParser.class.getName());
 
 	/**
 	 * startet die Konvertierung der Datei
 	 * 
 	 * @param dId
 	 *            DocumentId, welches Dokument soll geparsed werden
 	 * @param fileType
 	 *            bereits ermittelter Dateityp
 	 * @return boolean result, gibt bei Erfolg true zurueck
 	 */
 	public boolean parseFile(int dId)
 	{
 		_logger.info("Start parsing: " + dId);
 
 		boolean result = false;
 		// Http liegen nicht als Datei vor
 
 		try
 		{
 			FileType fileType = new MySqlDatabaseHelper().loadFileType(dId);
 			if (fileType != FileType.HTML)
 			{
 				// Zusammenbau Dateipfad
 				_file = new File(Control.ROOT_FILES + dId + "." + fileType.toString().toLowerCase());
 				_logger.info("File loadet");
 			}
 			result = fileToPjt(dId, fileType);
 
 		}
 		catch (InvalidFormatException e)
 		{
 			_logger.fatal(e.getMessage(), e);
 			e.printStackTrace();
 			result = false;
 		}
 		catch (OpenXML4JException e)
 		{
 			_logger.fatal(e.getMessage(), e);
 			e.printStackTrace();
 			result = false;
 		}
 		catch (XmlException e)
 		{
 			_logger.fatal(e.getMessage(), e);
 			e.printStackTrace();
 			result = false;
 		}
 		catch (IOException e)
 		{
 			_logger.fatal(e.getMessage(), e);
 			e.printStackTrace();
 			result = false;
 		}
 		catch (SAXException e)
 		{
 			_logger.fatal(e.getMessage(), e);
 			e.printStackTrace();
 			result = false;
 		}
 		catch (ParserConfigurationException e)
 		{
 			_logger.fatal(e.getMessage(), e);
 			e.printStackTrace();
 			result = false;
 		}
 		catch (Exception e)
 		{
 			_logger.fatal(e.getMessage(), e);
 			e.printStackTrace();
 			result = false;
 		}
 
 		return result;
 
 	}
 
 	/**
 	 * Liesst die gegebende Datei ein und speichert den beinhalteten Text in
 	 * einer pjt File unter dem selben Namen und Speicherort ab.
 	 * 
 	 * @return boolean result; true falls Konvertierung erfolgreich
 	 * @throws InvalidFormatException
 	 *             ; Falls eine Datei mit falscher Endung abgespeichert war
 	 * @throws OpenXML4JException
 	 *             ; Falls docx auslesen Fehlschlaegt
 	 * @throws XmlException
 	 *             ; Falls es Probleme mit docx gibt
 	 * @throws IOException
 	 *             ; falls auf die Dateien nicht zugegriffen werden kann
 	 * @throws ParserConfigurationException
 	 *             : ; Bei Schwierigkeiten mit der Docx
 	 * @throws SAXException
 	 *             ; Bei Schwierigkeiten mit der Docx
 	 */
 	private boolean fileToPjt(int dId, FileType fileTyp) throws InvalidFormatException, OpenXML4JException, XmlException, IOException, SAXException, ParserConfigurationException
 	{
 
 		boolean result = false;
 		String strText = "";
 
 		// Auswahl Methode je nach Dateityp
 		switch (fileTyp)
 		{
 			case DOC:
 			{
 				strText = parseDoc();
 				result = true;
 				break;
 			}
 			case DOCX:
 			{
 				strText = parseDocx();
 				result = true;
 				break;
 			}
 			case PDF:
 			{
 				strText = parsePDF();
 				result = true;
 				break;
 			}
 			case HTML:
 			{
 				strText = parseHTML(dId);
 				result = true;
 				break;
 			}
 			case TXT:
 			{
 				strText = parseTXT(dId);
 				result = true;
 				break;
 			}
 			default:
 			{
 				break;
 			}
 
 		}
 		BufferedWriter bufferedWriter = null;
 		try
 		{
 
 			File file = new File(Control.ROOT_FILES + dId + ".pjt");
 
 			// if file doesnt exists, then create it
 			if (!file.exists())
 			{
 				file.createNewFile();
 			}
 
 			FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
 			bufferedWriter = new BufferedWriter(fileWriter);
 			bufferedWriter.write(strText);
 			bufferedWriter.close();
 		}
 		catch (IOException e)
 		{
 			_logger.info(e.getMessage(), e);
 		}
 		finally
 		{
 			// writer soll auf jeden Fall geschlossen werden
 			if (bufferedWriter != null)
 			{
 				bufferedWriter.close();
 			}
 		}
 
 		return result;
 	}
 
 	private String parseTXT(int dId) throws UnsupportedEncodingException
 	{
		return SourceLoader.loadFile(Control.ROOT_FILES + dId + ".txt");
 		// TODO check for SQL Injection
 	}
 
 	/**
 	 * ließt mit Hilfe der Methode loadURL des SourceLoaders den inhalt einer
 	 * Internetseite aus
 	 * 
 	 * @param dId
 	 *            Die DocumentId, mit deren Hilfe die URL ermittelt werden kann
 	 * @return String result, gibt den ermittelten Inhalt zurueck
 	 */
 	private String parseHTML(int dId)
 	{
 		String result = "";
 		_logger.info("Filetype = HTML");
 		MySqlDatabaseHelper databaseHelper = new MySqlDatabaseHelper();
 		result = (SourceLoader.loadURL(databaseHelper.loadDocumentURL(dId)));
 		return result;
 	}
 
 	/**
 	 * liesst den Inhalt einer Doc Datei aus
 	 * 
 	 * @return String result, der Text der Doc
 	 * @throws FileNotFoundException
 	 *             Falls die auszulesene Datei nicht gefunden wird
 	 * @throws IOException
 	 *             Falls beim auslesen Fehler auftreten
 	 */
 	private String parseDoc() throws FileNotFoundException, IOException
 	{
 		String result = "";
 
 		_logger.info("Filetype = DOC");
 		POIFSFileSystem poifsFileSystem = new POIFSFileSystem(new FileInputStream(_file));
 		HWPFDocument hwpfDoc = new HWPFDocument(poifsFileSystem);
 		WordExtractor we = new WordExtractor(hwpfDoc);
 		// Geht alle Absaetze durch und schreibt diese in txt
 		String[] strParagraphs = we.getParagraphText();
 		for (String line : strParagraphs)
 		{
 			if (line != null && !line.trim().isEmpty())
 			{
 				result = result + (line.trim() + "\n");
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Ließt den Inhalt einer Docx aus
 	 * 
 	 * @return String result:Der ermittelte Text der Datei
 	 * @throws IOException
 	 *             : Falls Fehler beim auslesen Auftreten
 	 * @throws SAXException
 	 *             : Falls Fehler beim parsen Auftreten
 	 * @throws ParserConfigurationException
 	 *             : Falls Fehler mit dem Parser auftreten
 	 */
 	private String parseDocx() throws IOException, SAXException, ParserConfigurationException
 	{
 		String result = "";
 
 		_logger.info("Filetype = DOCX");
 		ZipFile zipFile = null;
 
 		try
 		{
 			// Entzippt die Docx, parst die XML
 			zipFile = new ZipFile(_file);
 			ZipEntry zipEntryXML = zipFile.getEntry("word/document.xml");
 			InputStream inputStreamXMLIS = zipFile.getInputStream(zipEntryXML);
 			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 			Document doc = dbf.newDocumentBuilder().parse(inputStreamXMLIS);
 
 			Element element = doc.getDocumentElement();
 			NodeList n = (NodeList) element.getElementsByTagName("w:p");
 			// schreibt alle Absaetze in das result
 			for (int j = 0; j < n.getLength(); j++)
 			{
 				Node nChild = n.item(j);
 				String strLine = nChild.getTextContent();
 				if (strLine != null && !strLine.trim().isEmpty())
 				{
 					result = result + (strLine.trim() + "\n");
 				}
 			}
 
 		}
 
 		finally
 		{
 			// Die Zip sollte wieder geschlossen werden
 			if (zipFile != null)
 			{
 				zipFile.close();
 			}
 		}
 
 		return result;
 	}
 
 	/**
 	 * ließt den Inhalt einer PDF aus
 	 * 
 	 * @return String result: Der Inhalt der PDF
 	 * @throws FileNotFoundException
 	 *             : Falls die PDF nicht gefunden wird
 	 * @throws IOException
 	 *             : Falls es Probleme beim auslesen gibt
 	 */
 	private String parsePDF() throws FileNotFoundException, IOException
 	{
 		String result = "";
 		_logger.info("Filetype = PDF");
 		PDFParser pdfParser = null;
 
 		PDFTextStripper pdfStripper;
 		PDDocument pdDoc = null;
 		COSDocument cosDoc = null;
 		// PDDocumentInformation pdDocInfo;
 
 		try
 		{
 			// Der PDF parse Vorgang
 			pdfParser = new PDFParser(new FileInputStream(_file));
 			pdfParser.parse();
 			cosDoc = pdfParser.getDocument();
 			pdfStripper = new PDFTextStripper();
 			pdDoc = new PDDocument(cosDoc);
 			result = pdfStripper.getText(pdDoc);
 
 		}
 		finally
 		{
 			// Alle Dokumente werden geschlossen
 			try
 			{
 				if (cosDoc != null) cosDoc.close();
 				if (pdDoc != null) pdDoc.close();
 			}
 			catch (IOException e)
 			{
 				_logger.fatal(e.getMessage(), e);
 				e.printStackTrace();
 			}
 		}
 		return result;
 	}
 }
