 package de.switajski.priebes.flexibleorders.report.itextpdf;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.web.servlet.view.AbstractView;
 
 import com.itextpdf.text.BadElementException;
 import com.itextpdf.text.Chunk;
 import com.itextpdf.text.Document;
 import com.itextpdf.text.DocumentException;
 import com.itextpdf.text.Element;
 import com.itextpdf.text.ExceptionConverter;
 import com.itextpdf.text.Font;
 import com.itextpdf.text.FontFactory;
 import com.itextpdf.text.Image;
 import com.itextpdf.text.PageSize;
 import com.itextpdf.text.Paragraph;
 import com.itextpdf.text.Phrase;
 import com.itextpdf.text.Rectangle;
 import com.itextpdf.text.pdf.BaseFont;
 import com.itextpdf.text.pdf.ColumnText;
 import com.itextpdf.text.pdf.PdfContentByte;
 import com.itextpdf.text.pdf.PdfPCell;
 import com.itextpdf.text.pdf.PdfPTable;
 import com.itextpdf.text.pdf.PdfPageEvent;
 import com.itextpdf.text.pdf.PdfTemplate;
 import com.itextpdf.text.pdf.PdfWriter;
 import com.itextpdf.text.pdf.draw.VerticalPositionMark;
 
 import de.switajski.priebes.flexibleorders.domain.Address;
 
 /**
  * This class generates PDF views and files in DIN A4 and methods to create a letter.
  * @author Marek
  *
  */
 public abstract class PriebesIText5PdfView extends AbstractView implements PdfPageEvent {
 
 	public final DateFormat dateFormat = new SimpleDateFormat("dd.MM.yy");
 	
 	public PriebesIText5PdfView() {
 		setContentType("application/pdf");
 	}
 
 	@Override
 	protected boolean generatesDownloadContent() {
 		return true;
 	}
 
 	@Override
 	protected final void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
 
 		// IE workaround: write into byte array first.
 		ByteArrayOutputStream baos = createTemporaryOutputStream();
 
 		// Apply preferences and build metadata.
         
 		Document document = newDocument();
 		PdfWriter writer = newWriter(document, baos);
 		prepareWriter(model, writer, request);
 		buildPdfMetadata(model, document, request);
 
 		// Build PDF document.
 		document.open();
 		buildPdfDocument(model, document, writer, request, response);
 		document.close();
 
 		// Flush to HTTP response.
 		writeToResponse(response, baos);
 	}
 
 	protected Document newDocument() {
 		return new Document(PageSize.A4);
 	}
 
 	protected PdfWriter newWriter(Document document, OutputStream os) throws DocumentException {
 		return PdfWriter.getInstance(document, os);
 	}
 
 	protected void prepareWriter(Map<String, Object> model, PdfWriter writer, HttpServletRequest request)
 			throws DocumentException {
 		writer.setViewerPreferences(getViewerPreferences());
 		writer.setPageEvent(this);
 	}
 
 	protected int getViewerPreferences() {
 		return PdfWriter.ALLOW_PRINTING | PdfWriter.PageLayoutSinglePage;
 	}
 
 	protected void buildPdfMetadata(Map<String, Object> model, Document document, HttpServletRequest request) {
 	}
 
 	protected abstract void buildPdfDocument(Map<String, Object> model, Document document, PdfWriter writer,
 			HttpServletRequest request, HttpServletResponse response) throws Exception;
 	
 	//TODO: Filemanagement...
 		//prod:
 //		protected static final String LOGO = "/var/lib/tomcat7/webapps/FlexibleOrders/images/LogoGross.jpg";
 		//dev
		protected static final String LOGO = "/var/lib/tomcat7/webapps/FlexibleOrders/images/LogoGross.jpg";
 		public static final String FONT = "Arial";
 		protected static final String UEBER_EMPFAENGERADRESSE = "Maxstrasse1, 71636 Ludwigsburg";
 		protected static final String HEADER_ZEILE1 = "Maxstrasse 1";
 		protected static final String HEADER_ZEILE2 = "71636 Ludwigsburg";
 		protected static final String HEADER_ZEILE3 = "priebes.eu";
 
 
 		/**
 		 * Einfügen des Headers in ein PDF-Document
 		 * @param document
 		 * @throws BadElementException
 		 * @throws MalformedURLException
 		 * @throws DocumentException
 		 * @throws IOException
 		 */
 		public void insertHeader(Document document) throws BadElementException,
 				MalformedURLException, DocumentException, IOException {
 					
 				}
 		
 		/**
 		 * inserts the address
 		 * 
 		 * @param doc
 		 * @param adresse die EmpfängerAdresse
 		 * @throws MalformedURLException
 		 * @throws IOException
 		 * @throws DocumentException
 		 */
 		public void insertAdresse(Document doc, Address adresse)
 				throws MalformedURLException, IOException, DocumentException {
 					insertEmptyLines(doc, 6);
 					Image img = Image.getInstance(LOGO);
 					img.scaleToFit(70,35);
 					img.setAbsolutePosition(50, 693);
 					Chunk tab1 = new Chunk(new VerticalPositionMark(), 90, true);
 					Paragraph p = new Paragraph();
 					p.add(tab1);
 					p.setFont(FontFactory.getFont(FONT,10,Font.NORMAL));
 					p.add(UEBER_EMPFAENGERADRESSE);
 					doc.add(p);
 					doc.add(img);
 					
 					p = new Paragraph();
 					if (adresse == null){
 						insertEmptyLines(doc,4);
 					}
 					else {
 						int verticalSpace = 30;
 						p.add(Chunk.NEWLINE);
 						p.add(new Chunk(new VerticalPositionMark(), verticalSpace, true));
 						p.add(new Phrase(adresse.getName1()+" "+adresse.getName2()));p.add(Chunk.NEWLINE);
 						p.add(new Chunk(new VerticalPositionMark(), verticalSpace, true));
 						p.add(new Phrase(adresse.getStreet()));p.add(Chunk.NEWLINE);
 						p.add(new Chunk(new VerticalPositionMark(), verticalSpace, true));
 						p.add(new Phrase(adresse.getPostalCode() + " " + adresse.getCity()));p.add(Chunk.NEWLINE);
 						p.add(new Chunk(new VerticalPositionMark(), verticalSpace, true));
 //						p.add(new Phrase(adresse.getCountry().toString()));p.add(Chunk.NEWLINE);
 					}
 					doc.add(p);
 					
 				}
 		 
 
 		/**
 		 * Einfgen eines Betreffs / Titels des Dokuments
 		 * @param doc
 		 * @param title Der Dokumententitel wie z.B. "Rechnung"
 		 * @throws MalformedURLException
 		 * @throws IOException
 		 * @throws DocumentException
 		 */
 		public void insertSubject(Document doc, String title)
 				throws MalformedURLException, IOException, DocumentException {
 
 			Paragraph p = new Paragraph();
 
 			p.setFont(FontFactory.getFont(FONT,16,Font.BOLD));
 			insertEmptyLines(doc,3);
 
 			p.add(title);
 			doc.add(p);
 		}
 		
 		public void insertSmallText(Document doc, String text)
 				throws MalformedURLException, IOException, DocumentException {
 
 			Paragraph p = new Paragraph();
 			p.setFont(FontFactory.getFont(FONT,10,Font.NORMAL));
 			p.add(text);
 			doc.add(p);
 		}
 
 		/**
 		 * 
 		 * @param doc
 		 * @param info Der Dokumententitel wie z.B. "Rechnung"
 		 * @throws MalformedURLException
 		 * @throws IOException
 		 * @throws DocumentException
 		 */
 		public void insertInfo(Document doc, String info)
 				throws MalformedURLException, IOException, DocumentException {
 
 			Paragraph p = new Paragraph();
 
 			p.setFont(FontFactory.getFont(FONT,12,Font.NORMAL));
 			insertEmptyLines(doc,1);
 
 			p.add(info);
 			doc.add(p);
 		}
 
 		public void insertEmptyLines(Document doc, int lines) throws DocumentException{
 			Paragraph p = new Paragraph();
 			for (int i=0;i<lines;i++){
 				p.add(Chunk.NEWLINE);
 			}
 			doc.add(p);
 		}
 
 		@Override
 		public void onOpenDocument(PdfWriter writer, Document document) {
 			total = writer.getDirectContent().createTemplate(30, 16);
 		}
 
 		@Override
 		public void onStartPage(PdfWriter writer, Document document) {
 			
 		}
 
 		PdfTemplate total;
 		
 		@Override
 		public void onEndPage(PdfWriter writer, Document document) {
 	        try {
 	        	Image img = Image.getInstance(LOGO);
 	        	img.setAlignment(Image.RIGHT);
 	        	img.scaleToFit(180,75);
 	        	
 	        	PdfPTable table = new PdfPTable(3);
 	        	//Adresse im Header
 	        	Paragraph addresse = new Paragraph("priebes OHG");
 				addresse.setFont(FontFactory.getFont(FONT,10,Font.NORMAL));
 				addresse.setAlignment(Element.ALIGN_RIGHT);
 				addresse.add(Chunk.NEWLINE);
 				addresse.add(HEADER_ZEILE1);addresse.add(Chunk.NEWLINE);
 				addresse.add(HEADER_ZEILE2);addresse.add(Chunk.NEWLINE);
 				addresse.add(HEADER_ZEILE3);addresse.add(Chunk.NEWLINE);
 				PdfPCell headerCell = new PdfPCell();
 				headerCell.setBorder(Rectangle.NO_BORDER);
 				headerCell.addElement(img);
 				headerCell.addElement(addresse);
 				
 				table.setWidths(new int[]{10, 10, 30});
                 table.setTotalWidth(527);
                 table.setLockedWidth(true);
                 table.getDefaultCell().setFixedHeight(20);
                 table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
                 table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                 table.addCell("");
                 table.addCell("");
                 table.addCell(headerCell);
                 table.writeSelectedRows(0, -1, 34, 803, writer.getDirectContent());
 				
 	            /*PdfPTable footer = new PdfPTable(2);
 	            footer.setWidths(new int[]{525, 2});
 	            footer.setTotalWidth(527);
 	            footer.setLockedWidth(true);
 	            footer.getDefaultCell().setFixedHeight(20);
 	            footer.getDefaultCell().setBorder(Rectangle.NO_BORDER);
 	            footer.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
 	            footer.addCell("priebes OHG / Maxstraße 1 / 71636 Ludwigsburg");footer.addCell("");
 	            footer.addCell("www.priebes.eu / info@priebes.eu / 0162 7014338 / 07141 - 9475640 (auch Fax)");footer.addCell("");
 	            footer.addCell("KSK Ludwigsburg BLZ 60450050 - Kto 30055142 / HRA 725747 / Ust-IdNr.: DE275948390");footer.addCell("");
                 footer.writeSelectedRows(0, -1, 34, 100, writer.getDirectContent());*/
 	            
                 PdfPTable footer = new PdfPTable(1);
                 footer.setTotalWidth(527);
                 footer.setLockedWidth(true);
                 footer.setHorizontalAlignment(Element.ALIGN_CENTER);
                 footer.getDefaultCell().setBorder(Rectangle.NO_BORDER);
                 Paragraph fPara = new Paragraph();
                 fPara.setAlignment(Element.ALIGN_CENTER);
                 fPara.setFont(FontFactory.getFont(FONT,10,Font.NORMAL));
                 fPara.setAlignment(Element.ALIGN_CENTER);
                 fPara.add("priebes OHG / Maxstrasse 1 / 71636 Ludwigsburg\n" + 
                 		"www.priebes.eu / info@priebes.eu / 0162 7014338 / 07141 - 9475640 (auch Fax)\n" +
                 		"KSK Ludwigsburg BLZ 60450050 - Kto 30055142 / HRA 725747 / Ust-IdNr.: DE275948390");
                 PdfPCell footerCell = new PdfPCell();
                 footerCell.addElement(fPara);
                 footerCell.setBorder(Rectangle.TOP);
                 footer.addCell(footerCell);
                 footer.writeSelectedRows(0, -1, 34, 75, writer.getDirectContent());
 	            
 	            addPageNumber(writer, document);
 				
 				//Adresse
 				
 				
 	        }
 	        catch(DocumentException de) {
 	            throw new ExceptionConverter(de);
 	        } catch (MalformedURLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 		}
 		
 		private void addPageNumber(PdfWriter writer, Document document) {
             Image img2;
             int x = 550;
             int y = 20;
 			try {
 				img2 = Image.getInstance(total);
 				img2.setAbsolutePosition(x, y);
 				document.add(img2);				
 			} catch (BadElementException e) {
 				e.printStackTrace();
 			} catch (DocumentException e) {
 				e.printStackTrace();
 			}
             absText(writer, String.format("Seite %d von", writer.getPageNumber()), x-60, y+2);
 			
 		}
 
 		private static void absText(PdfWriter writer, String text, int x, int y) {
 		    PdfContentByte cb = writer.getDirectContent();
 		      try {
 				BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
 				cb.saveState();
 				cb.beginText();
 				cb.moveText(x, y);
 				cb.setFontAndSize(bf, 12);
 				cb.showText(text);
 				cb.endText();
 				cb.restoreState();
 			} catch (DocumentException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		  }
 
 		@Override
 		public void onCloseDocument(PdfWriter writer, Document document) {
 			ColumnText.showTextAligned(total, Element.ALIGN_LEFT,
 	                new Phrase(String.valueOf(writer.getPageNumber() - 1)),
 	                2, 2, 0);
 			
 		}
 
 		@Override
 		public void onParagraph(PdfWriter writer, Document document,
 				float paragraphPosition) {
 			
 		}
 
 		@Override
 		public void onParagraphEnd(PdfWriter writer, Document document,
 				float paragraphPosition) {
 			
 		}
 
 		@Override
 		public void onChapter(PdfWriter writer, Document document,
 				float paragraphPosition, Paragraph title) {
 			
 		}
 
 		@Override
 		public void onChapterEnd(PdfWriter writer, Document document,
 				float paragraphPosition) {
 			
 		}
 
 		@Override
 		public void onSection(PdfWriter writer, Document document,
 				float paragraphPosition, int depth, Paragraph title) {
 			
 		}
 
 		@Override
 		public void onSectionEnd(PdfWriter writer, Document document,
 				float paragraphPosition) {
 			
 		}
 
 		@Override
 		public void onGenericTag(PdfWriter writer, Document document,
 				Rectangle rect, String text) {
 			
 		}
 
 
 }
