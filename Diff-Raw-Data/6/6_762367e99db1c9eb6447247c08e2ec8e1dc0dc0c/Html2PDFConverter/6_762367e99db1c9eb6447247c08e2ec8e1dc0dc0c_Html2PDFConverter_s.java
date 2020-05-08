 package br.com.christ.html2pdf.converter;
 
 import br.com.christ.html2pdf.exception.ConversionException;
 import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;
 import org.xhtmlrenderer.pdf.ITextRenderer;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 
 public class Html2PDFConverter {
     public static byte[] convertHtmlToPDF(String htmlContent, String url) throws ConversionException {
         ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();
         Document xhtmlContent = null;
         Tidy tidy = new Tidy();
         try {
             tidy.setFixUri(true);
             tidy.setXHTML(true);
             tidy.setShowWarnings(false);
             tidy.setAsciiChars(true);
             tidy.setNumEntities(true);
             xhtmlContent = tidy.parseDOM(new ByteArrayInputStream(htmlContent.getBytes()),pdfStream);
         } catch(Exception e) {
            e.printStackTrace();
         }
         try {
             ITextRenderer renderer = new ITextRenderer();
             renderer.setDocument(xhtmlContent, url);
             renderer.layout();
             pdfStream.reset();
             renderer.createPDF(pdfStream);
 
         } catch (com.itextpdf.text.DocumentException e) {
             throw new ConversionException(e);
         } catch (IOException e) {
             throw new ConversionException(e);
         }
 
         byte[] bytesPDF = pdfStream.toByteArray();
         return bytesPDF;
     }
 }
