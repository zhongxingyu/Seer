 package openagendamail.file;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import openagendamail.data.AgendaItem;
 import org.apache.pdfbox.exceptions.COSVisitorException;
 import org.apache.pdfbox.pdmodel.PDDocument;
 import org.apache.pdfbox.pdmodel.PDPage;
 import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
 import org.apache.pdfbox.pdmodel.font.PDFont;
 import org.apache.pdfbox.pdmodel.font.PDType1Font;
 
 /**
  * A class that represents a PDF file.  This class aids in assembling a PDF document from the agenda items.
  *
  * @author adam
  * @date Feb 3, 2013
  * Last Updated:  February 5, 2013.
  */
 public class Pdf {
 
     /** The document. */
     private PDDocument m_doc;
 
     /** The current page that this Pdf is writing to. */
     private PDPage m_currentPage;
 
     /** The content stream for the current page. */
     private PDPageContentStream m_contentStream;
 
     /** The X-Coordinate of the location to write the next line of text. */
     private float m_currentX;
 
     /** The Y-Coordinate of the location to write the next line of text. */
     private float m_currentY;
 
     /** The maximum X-value for the page, ie its width. */
     private float m_pgWidth;
 
     /** The maximum Y value for the page, ie its height. */
     private float m_pgHeight;
 
     /**
      * The amount of vertical offset for text using the title font. This value is used to move the 'cursosr'
      * down a row after rendering a line of that text using the title font.
      */
     private float m_titleOffset;
 
     /**
      * The amount of vertical offset for text using the agenda item header font. This value is used to move the
      * 'cursosr' down a row after rendering a line of that text using the title font.
      */
     private float m_headerOffset;
 
     /**
      * The amount of vertical offset for text using the normal, non-header/title font. This value is used to move the
      * 'cursosr' down a row after rendering a line of that text using the title font.
      */
     private float m_normalOffset;
 
     /** The font size of the document title. */
     private static final int TITLE_FONT_SIZE = 16;
 
     /** The font size of the agenda item headers. */
     private static final int HEADER_FONT_SIZE = 12;
 
     /** The font size for the normal, non-header or title font */
     private static final int NORMAL_FONT_SIZE = 9;
 
     /** The width of the side margins. */
     private static final int SIDE_MARGIN = 70;
 
     /** The height of the Top and Bottom Margins. */
     private static final int TOP_BOTTOM_MARGINS = 80;
 
     /** The Bold Font Face used by this document. */
     private static final PDFont BOLD = PDType1Font.HELVETICA_BOLD;
 
     /** The Normal, non-bold font face used by this document. */
     private static final PDFont NORMAL = PDType1Font.HELVETICA;
 
     /** A list to cache words removed when calculating line lengths for word wrap. */
     private List<String> m_tailWordCache = new ArrayList<>();
 
 
     /** Constructs a new, blank PDF document. */
     public Pdf(){
         try {
             // Initialize the Document
             m_doc = new PDDocument();
             m_currentPage = new PDPage();
             m_doc.addPage(m_currentPage);
 
             // Calculate the page height.
             m_pgWidth  = m_currentPage.getTrimBox().getWidth() - (2 * SIDE_MARGIN);
             m_pgHeight = m_currentPage.getTrimBox().getHeight();
 
             // Figure out the page position
             resetCurrentXandY();
 
             // Initialize a Content Stream for writing to the current page.
             m_contentStream = new PDPageContentStream(m_doc, m_currentPage);
             m_contentStream.beginText();
             m_contentStream.moveTextPositionByAmount(m_currentX, m_currentY);
 
             // Calculate the offsets for the fonts.
             m_titleOffset  = -1 * (BOLD.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * TITLE_FONT_SIZE);
             m_headerOffset = -1 * (BOLD.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * HEADER_FONT_SIZE);
             m_normalOffset = -1 * (NORMAL.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * NORMAL_FONT_SIZE);
 
         } catch (IOException ex) {
             LogFile.getLogFile().log("Error initializing PDF document.", ex);
         }
     }
 
     /**
      * Renders the supplied title and subtitle to this document.
      *
      * @param title the title of the agenda.
      * @param subtitle the subtitle of the agenda.
      */
     public void renderTitle(String title, String subtitle){
         try {
             renderLine(title, BOLD, TITLE_FONT_SIZE, m_titleOffset);
             renderLine(subtitle, NORMAL, NORMAL_FONT_SIZE, m_normalOffset);
 
             // Skip a Line
             m_contentStream.moveTextPositionByAmount(0, m_normalOffset * 2);
             m_currentY = m_currentY + m_normalOffset;
             checkPage();
 
         } catch (IOException ex) {
             LogFile.getLogFile().log("Error rendering title to pdf file.", ex);
         }
     }
 
     /**
      * Renders the provided {@link AgendaItem} to the PDF.
      * @param item the item to render to the agenda.
      */
     public void renderAgendaItem(AgendaItem item){
         try {
             // Render the Header Line
             renderLine(item.getTitle(), BOLD, HEADER_FONT_SIZE, m_headerOffset);
 
             // Render From Line
             String fromLine = "Submitted By:  " + item.getUser() + " (" + item.getEmail() +")";
             renderLine(fromLine, NORMAL, NORMAL_FONT_SIZE, m_normalOffset);
 
             // Skip a Line
             m_contentStream.moveTextPositionByAmount(0, m_normalOffset);
             m_currentY = m_currentY + m_normalOffset;
             checkPage();
 
             // Render the Body if any.
             if (!(item.getBody() == null && item.getBody().isEmpty())){
                 renderLine(item.getBody(), NORMAL, NORMAL_FONT_SIZE, m_normalOffset);
             }
 
             // Skip a Line
             m_contentStream.moveTextPositionByAmount(0, m_normalOffset * 2);
             m_currentY = m_currentY + m_normalOffset;
             checkPage();
 
         } catch (IOException ex) {
             LogFile.getLogFile().log("Error rendering an AgendaItem to pdf file.", ex);
         }
 
     }
 
     /**
      * Renders a single line of text to the document.  If necessary this method will wrap the text of the line
      * repeatedly.
      *
      * @param line the String of text to render to the PDF.
      * @param font the font style to render this line in.
      * @param fontSize the size to render the font.
      * @param offset the row height offset calculated for this font.
      *
      * @return true if the call resulted in a line being written to the
      * @throws IOException if an error occurs rendering the line to the pdf file.
      */
     private boolean renderLine(String line, PDFont font, int fontSize, float offset) throws IOException{
         String toRender = line.trim();
         if (getStringWidth(toRender, font, fontSize) > m_pgWidth){
             int index = toRender.lastIndexOf(" ");
             String head = toRender.substring(0, index);
             m_tailWordCache.add(toRender.substring(index).trim());
 
             // Call render on the timmed header.  If this call results in a write, (ie if its short enough), then
             // call renderLine on the tail.
             if (renderLine(head, font, fontSize, offset)){
                 renderLine(getCachedTail(), font, fontSize, offset);
             }
         } else {
             m_contentStream.setFont(font, fontSize);
            m_contentStream.drawString(toRender);
             m_contentStream.moveTextPositionByAmount(0, offset);
            System.out.println("Current Y:  " + m_currentY);
             m_currentY = m_currentY + offset;
             checkPage();
             return true;
         }
 
         return false;
     }
 
     /**
      * Saves the file to disk.
      *
      * @param filename the name of the file that will be created by this save operation
      * .
      * @throws IOException if an error occurs during saving.
      * @throws COSVisitorException if an error occurs during saving.
      */
     public void saveAs(String filename) throws IOException, COSVisitorException{
         m_contentStream.endText();
         m_contentStream.close();
         m_doc.save(filename);
     }
 
     /** Closes this file for modifications. */
     public void close() throws IOException{
         m_doc.close();
     }
 
     /**
      * Checks the current page position vs the bottom margin to see if its time to create a new page.
      * If a new page is required, a new one is initialized and all associated variables updated.
      */
     private void checkPage(){
         if (m_currentY < TOP_BOTTOM_MARGINS * 2){
            System.out.println("New Page!  Y:  " + m_currentY + ", Pg Height:  " + m_pgHeight + ", Margin:  " + TOP_BOTTOM_MARGINS);
             newPage();
         }
     }
 
     /**
      * Returns the width in page units of the string using the font and font size specified.
      *
      * @param str The string to measure the width of.
      * @param font the font style to use.
      * @param fontSize the size of the font, (ie 12pt, 16pt etc).
      *
      * @return The width in page units of the string using the font and font size specified.
      * @throws IOException if an error occurs calculating the width.
      */
     private float getStringWidth(String str, PDFont font, int fontSize) throws IOException{
         return (font.getStringWidth(str) / 1000 * fontSize);
     }
 
     /** Resets the current X, Y text positions to their default values (the upper left corner of the text area). */
     private void resetCurrentXandY(){
         m_currentX = SIDE_MARGIN;
         m_currentY = m_pgHeight - TOP_BOTTOM_MARGINS;
     }
 
     /** Initializes a new page for this PDF document. */
     private void newPage(){
         try {
             // Close out old content stream.
             m_contentStream.endText();
             m_contentStream.close();
 
             // Generate a new page.
             m_currentPage = new PDPage();
             m_doc.addPage(m_currentPage);
             resetCurrentXandY();
 
             // Create new stream.
             m_contentStream = new PDPageContentStream(m_doc, m_currentPage);
             m_contentStream.beginText();
             m_contentStream.moveTextPositionByAmount(m_currentX, m_currentY);
            System.out.println("CurrY:  " + m_currentY);
 
         } catch (IOException ex) {
             LogFile.getLogFile().log("Error initializing content stream for new page.", ex);
         }
     }
 
     /**
      * A helper method that takes the words stored in the tail words cache and assembles them in the proper order as
      * the entire tail.
      *
      * @return the complete tail cache.
      */
     private String getCachedTail(){
         StringBuilder bldr = new StringBuilder();
         Collections.reverse(m_tailWordCache);
         for (String word : m_tailWordCache){
             bldr.append(word);
             bldr.append(" ");
         }
         m_tailWordCache.clear();
 
         return bldr.toString().trim();
     }
 }
