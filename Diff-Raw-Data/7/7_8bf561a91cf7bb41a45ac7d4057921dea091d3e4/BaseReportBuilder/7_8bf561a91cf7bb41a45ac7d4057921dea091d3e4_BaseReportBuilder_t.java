 package export;
 
 
 import com.itextpdf.text.Document;
 import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
 import com.itextpdf.text.Rectangle;
 import com.itextpdf.text.pdf.BaseFont;
 import com.itextpdf.text.pdf.PdfContentByte;
 import com.itextpdf.text.pdf.PdfPageEventHelper;
 import com.itextpdf.text.pdf.PdfTemplate;
 import com.itextpdf.text.pdf.PdfWriter;
 import play.api.Play;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;
 
 /**
  * @author f.patin
  */
 
 public abstract class BaseReportBuilder extends PdfPageEventHelper {
	protected BaseFont baseFont = new Font(Font.FontFamily.HELVETICA, 10f, Font.NORMAL).getCalculatedBaseFont(false);
 	private PdfTemplate totalPages;
 	private float footerTextSize = 8f;
 	private int pageNumberAlignment = Element.ALIGN_CENTER;
 
 	public BaseReportBuilder() {
 		super();
		//baseFont = load("tahoma.ttf");
 	}
 
 	private BaseFont load(String fontname) {
 		try {
 			File f = Play.getFile("public/font/tahoma.ttf", Play.current());
 			InputStream is = new FileInputStream(f);
 
 			ByteArrayOutputStream out = new ByteArrayOutputStream();
 			byte buf[] = new byte[1024];
 
 			while(true) {
 				int size = is.read(buf);
 				if(size < 0)
 					break;
 				out.write(buf, 0, size);
 			}
 			is.close();
 			buf = out.toByteArray();
 			return BaseFont.createFont(fontname, BaseFont.CP1252, true, true, buf, null);
 		} catch(Exception ex) {
 			return null;
 		}
 	}
 
 	@Override
 	public void onOpenDocument(PdfWriter writer, Document document) {
 		totalPages = writer.getDirectContent().createTemplate(100, 100);
 		totalPages.setBoundingBox(new Rectangle(-20, -20, 100, 100));
 	}
 
 	@Override
 	public void onEndPage(PdfWriter writer, Document document) {
 		PdfContentByte cb = writer.getDirectContent();
 		cb.saveState();
 		String text = String.format("Page %s of ", writer.getPageNumber());
 
 		float textBase = document.bottom() - 20;
 		float textSize = baseFont.getWidthPoint(text, footerTextSize);
 
 		cb.beginText();
 		cb.setFontAndSize(baseFont, footerTextSize);
 		if(Element.ALIGN_CENTER == pageNumberAlignment) {
 			cb.setTextMatrix((document.right() / 2), textBase);
 			cb.showText(text);
 			cb.endText();
 			cb.addTemplate(totalPages, (document.right() / 2) + textSize, textBase);
 		} else if(Element.ALIGN_LEFT == pageNumberAlignment) {
 			cb.setTextMatrix(document.left(), textBase);
 			cb.showText(text);
 			cb.endText();
 			cb.addTemplate(totalPages, document.left() + textSize, textBase);
 		} else {
 			float adjust = baseFont.getWidthPoint("0", footerTextSize);
 			cb.setTextMatrix(document.right() - textSize - adjust, textBase);
 			cb.showText(text);
 			cb.endText();
 			cb.addTemplate(totalPages, document.right() - adjust, textBase);
 		}
 		cb.restoreState();
 	}
 
 	@Override
 	public void onCloseDocument(PdfWriter writer, Document document) {
 		totalPages.beginText();
 		totalPages.setFontAndSize(baseFont, footerTextSize);
 		totalPages.setTextMatrix(0, 0);
 		totalPages.showText(String.valueOf(writer.getPageNumber() - 1));
 		totalPages.endText();
 	}
 
 	public void setPageNumberAlignment(int pageNumberAlignment) {
 		this.pageNumberAlignment = pageNumberAlignment;
 	}
 }
