 package ch.znerol.pdftickbox;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import org.apache.pdfbox.ImportXFDF;
 import org.apache.pdfbox.exceptions.COSVisitorException;
 import org.apache.pdfbox.pdfparser.PDFParser;
 import org.apache.pdfbox.pdmodel.PDDocument;
 import org.apache.pdfbox.pdmodel.fdf.FDFDocument;
 import org.springframework.stereotype.Component;
 
 @Component
 public class PDFFormFiller implements FormFiller {
 
 	@Override
 	public String getContentType() {
 		return "application/pdf";
 	}
 
 	@Override
 	public void fill(InputStream template, OutputStream result, String values) throws FormFillerException {
 		PDDocument doc;
 		try {
 			PDFParser parser = new PDFParser(template);
 			parser.parse();
 			doc = parser.getPDDocument();
 		} catch (IOException e) {
 			throw new FormFillerException("Failed to read PDF from template", e);
 		}
 
 		FDFDocument fdf;
 		try {
 			fdf = FDFDocument.loadXFDF(
 					new ByteArrayInputStream(values.getBytes("UTF-8")));
 		} catch (IOException e) {
			throw new FormFillerException("Failed load XFDF", e);
 		}
 
 		ImportXFDF stamper;
 		stamper = new ImportXFDF();
 		try {
 			stamper.importFDF(doc, fdf);
 		} catch (IOException e) {
			throw new FormFillerException("Failed load fill PDF template with XFDF values", e);
 		}
 
 		try {
 			doc.save(result);
 		} catch (COSVisitorException e) {
 			throw new FormFillerException("Failed to write PDF document", e);
 		} catch (IOException e) {
 			throw new FormFillerException("Failed to write PDF document", e);
 		}
 
 		try {
 			stamper.close(doc);
 			stamper.close(fdf);
 		} catch (IOException e) {
 			throw new FormFillerException("Failed close document", e);
 		}
 	}
 }
