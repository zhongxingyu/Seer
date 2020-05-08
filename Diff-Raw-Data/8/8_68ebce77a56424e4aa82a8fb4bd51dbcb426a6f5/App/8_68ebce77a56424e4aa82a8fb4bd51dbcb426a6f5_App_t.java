 package net.charlespence.get-comments;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.lang3.StringEscapeUtils;
 
 import org.apache.poi.hwpf.HWPFDocument;
 import org.apache.poi.hwpf.usermodel.Range;
 import org.apache.poi.hwpf.usermodel.Paragraph;
 
 import org.apache.poi.POIXMLDocumentPart;
 import org.apache.poi.xwpf.usermodel.XWPFDocument;
 import org.apache.poi.xwpf.usermodel.XWPFRelation;
 import org.apache.poi.xwpf.usermodel.XWPFParagraph;
 import org.openxmlformats.schemas.wordprocessingml.x2006.main.CommentsDocument;
 import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTComment;
 import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
 
 import org.apache.pdfbox.pdfparser.PDFParser;
 import org.apache.pdfbox.exceptions.InvalidPasswordException;
 import org.apache.pdfbox.pdmodel.PDDocument;
 import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
 import org.apache.pdfbox.pdmodel.PDPage;
 import org.apache.pdfbox.pdmodel.common.PDRectangle;
 import org.apache.pdfbox.pdmodel.common.PDTextStream;
 import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
 import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationMarkup;
 
 class AnnotationData {
     float boundsY;
     String content;
 
     public AnnotationData(float boundsY, String content) {
         this.boundsY = boundsY;
         this.content = content;
     }
 
     public float getBoundsY() {
         return boundsY;
     }
 
     public void setBoundsY(float boundsY) {
         this.boundsY = boundsY;
     }
 
     public String getContent() {
         return content;
     }
 
     public void setContent(String content) {
         this.content = content;
     }
 }
 
 class AnnotationDataComparator implements Comparator<AnnotationData> {
     public int compare(AnnotationData o1, AnnotationData o2) {
         float diff = o1.getBoundsY() - o2.getBoundsY();
 
         // Yes, it is intentional that this comparison has opposite sign!  The
         // origin of a PDF page is in the bottom-left corner.
         if (diff < 0.0001 && diff > -0.0001) {
             return 0;
         }
         else if (diff < 0) {
             return 1;
         }
         else {
             return -1;
         }
     }
 }
 
 public class App 
 {
     public static List<String> getDocComments(String fileName)
         throws java.io.FileNotFoundException, java.io.IOException
     {
         File file = null;
         FileInputStream fis = null;
         HWPFDocument document = null;
         Range commentRange = null;
         List<String> ret = new ArrayList<String>();
         
         file = new File(fileName);
         fis = new FileInputStream(file);
         document = new HWPFDocument(fis);
         commentRange = document.getCommentsRange();
         
         int numComments = commentRange.numParagraphs();
         for (int i = 0 ; i < numComments ; i++) {
             String comment = commentRange.getParagraph(i).text().trim();
             if (!comment.equals("")) {
                 ret.add(comment);
             }
         }
         
         return ret;
     }
     
     public static List<String> getDocxComments(String fileName)
         throws java.io.FileNotFoundException, java.io.IOException,
             org.apache.xmlbeans.XmlException
     {
         File file = null;
         FileInputStream fis = null;
         XWPFDocument document = null;
         List<String> ret = new ArrayList<String>();
         
         file = new File(fileName);
         fis = new FileInputStream(file);
         document = new XWPFDocument(fis);
         
         // We want to get the comments ourselves, the current state of the
         // XWPFComment code is really bad.  Get them paragraph by paragraph,
         // mimicing the behavior of the DOC code.
         for (POIXMLDocumentPart part : document.getRelations()) {
             String relation = part.getPackageRelationship().getRelationshipType();
             if (relation.equals(XWPFRelation.COMMENT.getRelation())) {
                 CommentsDocument commentDoc = CommentsDocument.Factory.parse(part.getPackagePart().getInputStream());
                 for (CTComment ctComment : commentDoc.getComments().getCommentList()) {
                     for (CTP ctp : ctComment.getPList()) {
                         XWPFParagraph para = new XWPFParagraph(ctp, document);
                         String comment = para.getText().trim();
                         
                         if (!comment.equals("")) {
                             ret.add(comment);
                         }
                     }
                 }
             }
         }
         
         return ret;
     }
     
     public static List<String> getPDFComments(String fileName)
         throws java.io.FileNotFoundException, java.io.IOException,
         org.apache.pdfbox.exceptions.CryptographyException
     {
         File file = null;
         FileInputStream fis = null;
         PDFParser parser = null;
         PDDocument document = null;
         List<String> ret = new ArrayList<String>();
         
         file = new File(fileName);
         fis = new FileInputStream(file);
         parser = new PDFParser(fis);
         parser.parse();
         
         document = parser.getPDDocument();
         if (document.isEncrypted()) {
             try {
                 document.decrypt("");
             } catch (InvalidPasswordException e) {
                System.err.println("Error: Document is encrypted with a password.");
                 System.exit(1);
             }
         }
         
         List<PDPage> pages = document.getDocumentCatalog().getAllPages();
         for (PDPage page : pages) {
             List<PDAnnotation> annotations = page.getAnnotations();
             List<AnnotationData> annotationData = new ArrayList<AnnotationData>();
             
             for (PDAnnotation annot : annotations) {
                 String contents = null;
                 
                 if (annot instanceof PDAnnotationMarkup) {
                     PDTextStream richContents = ((PDAnnotationMarkup)annot).getRichContents();
                     if (richContents != null) {
                         contents = richContents.getAsString();
                         if (contents != null) {
                             contents = contents.replaceAll("\\<.*?\\>", "");
                             // Normalize Windows CR/LFs to LFs, then any Mac
                             // pure-CRs to LFs.
                             contents = StringEscapeUtils.unescapeHtml4(contents).replaceAll("\r\n", "\n").replaceAll("\r", "\n");
                         }
                     }
                     else {
                         contents = annot.getContents();
                     }
                 }
                 else {
                     contents = annot.getContents();
                 }            
 
                 if (contents != null) {
                     contents = contents.trim();
                     
                     if (!contents.equals("")) {
                         PDRectangle rect = annot.getRectangle();
                         float boundsY = rect.getUpperRightY();
                     
                         annotationData.add(new AnnotationData(boundsY, contents));
                     }
                 }
             }
             
             // Sort these out in the order that they appear on the page, top to
             // bottom
             Collections.sort(annotationData, new AnnotationDataComparator());
             
             for (AnnotationData ad : annotationData) {
                 ret.add(ad.getContent());
             }
         }
         
         return ret;
     }
     
     public static void main(String[] args)
     {
         if (args.length < 1) {
            System.err.println("Error: get-comments <file>");
             System.exit(1);
         }
         
         // Try to take account of file names with spaces
         String fileName = StringUtils.join(args, " ");
         String fileExtension = "";
 
         int i = fileName.lastIndexOf('.');
         if (i > 0) {
             fileExtension = fileName.substring(i + 1);
         }
         
         try {
             List<String> comments;
             if (fileExtension.equals("pdf")) {
                 comments = getPDFComments(fileName);
             }
             else if (fileExtension.equals("doc")) {
                 comments = getDocComments(fileName);
             }
             else {
                 comments = getDocxComments(fileName);
             }
             
             for (i = 0 ; i < comments.size() ; i++) {
                 System.out.println("comment :-  " + comments.get(i));
             }
         } catch (Exception e) {
            System.err.println("Error: Cannot extract comments from file:");
             e.printStackTrace();
         }
     }
 }
