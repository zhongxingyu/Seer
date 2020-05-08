 package net.vandut.magik;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 
 import com.itextpdf.text.Document;
 import com.itextpdf.text.DocumentException;
 import com.itextpdf.text.Image;
 import com.itextpdf.text.PageSize;
 import com.itextpdf.text.Rectangle;
 import com.itextpdf.text.pdf.PdfContentByte;
 import com.itextpdf.text.pdf.PdfWriter;
 
 public class GeneratePDF {
 
 	public static float mm2p(int mm) {
 		return (float) (mm) / 25.4f * 72.0f;
 	}
 	
 	private static final int MAX_X = 3;
 	private static final int MAX_Y = 3;
 	
 	private static final float CARD_W = mm2p(63);
 	private static final float CARD_H = mm2p(88);
 	
 	private static final Rectangle PAGE_SIZE = PageSize.A4;
 	
 	private static final float MARGIN_W = (PAGE_SIZE.getWidth()-MAX_X*CARD_W)/2.0f;
 	private static final float MARGIN_H = (PAGE_SIZE.getHeight()-MAX_Y*CARD_H)/2.0f;
 	
 	private Document document;
 	private PdfWriter writer;
 	private PdfContentByte cb;
 	private File pdfFile;
 	
 	private int xIndex = 0;
 	private int yIndex = 0;
 	
 	public GeneratePDF(File pdfFile) throws FileNotFoundException, DocumentException {
 		this.pdfFile = pdfFile;
 		document = new Document(PAGE_SIZE);
 		writer = PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
 		document.open();
 		cb = writer.getDirectContent();
 		drawCutLines();
 	}
 	
 	public void close() {
 		document.close();
 	}
 	
 	public void newPage() {
 		document.newPage();
 		xIndex = 0;
 		yIndex = 0;
 		drawCutLines();
 	}
 
 	protected void drawLine(float x, float y, float xx, float yy) {
 		cb.moveTo(x, y);
 		cb.lineTo(xx, yy);
 		cb.stroke();
 	}
 	
 	private void drawOuterCutLines(float x, float y) {
 		drawLine(x-5, y, x-15, y);
 		drawLine(x, y-5, x, y-15);
 		drawLine(x+5, y, x+15, y);
 		drawLine(x, y+5, x, y+15);
 	}
 	
 	private void drawCutLines() {
		for(int x = 0; x < MARGIN_W; x++) {
			for(int y = 0; y < MARGIN_H; y++) {
 				drawOuterCutLines(MARGIN_W+x*CARD_W, MARGIN_H+y*CARD_H);
 			}
 		}
 	}
 	
 	protected void addImage(Image img, float x, float y) throws DocumentException {
 		cb.addImage(img, CARD_W, 0, 0, CARD_H, x+MARGIN_W, PAGE_SIZE.getHeight()-y-CARD_H-MARGIN_H);
 	}
 	
 	protected void addImage(Image img) throws DocumentException {
 		addImage(img, xIndex*CARD_W, yIndex*CARD_H);
 		++xIndex;
 		if(xIndex >= MAX_X) {
 			xIndex = 0;
 			++yIndex;
 		}
 		if(yIndex >= MAX_Y) {
 			newPage();
 		}
 	}
 	
 	public void addImage(String path, int count) throws MalformedURLException, IOException, DocumentException {
 		Image img = Image.getInstance(path);
 		for(int i = 0; i < count; i++) {
 			addImage(img);
 		}
 	}
 
 	public File getPdfFile() {
 		return pdfFile;
 	}
 
 	public static void main(String[] args) throws DocumentException, IOException {
 		File pdfFile = new File(Utils.getAppBaseDirectory(), "generated.pdf");
 		GeneratePDF pdf = new GeneratePDF(pdfFile);
 		pdf.close();
 	}
 }
