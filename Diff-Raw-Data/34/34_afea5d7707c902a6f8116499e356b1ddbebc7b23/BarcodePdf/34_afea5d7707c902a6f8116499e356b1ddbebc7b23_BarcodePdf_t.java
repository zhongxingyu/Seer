 package common;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import org.joda.time.LocalDate;
 
 import model.item.Item;
 
 import com.itextpdf.text.Document;
 import com.itextpdf.text.DocumentException;
 import com.itextpdf.text.Element;
 import com.itextpdf.text.Chunk;
 import com.itextpdf.text.Font;
 import com.itextpdf.text.Image;
 import com.itextpdf.text.Phrase;
 import com.itextpdf.text.pdf.BarcodeEAN;
 import com.itextpdf.text.pdf.PdfContentByte;
 import com.itextpdf.text.pdf.PdfWriter;
 import com.itextpdf.text.pdf.PdfPTable;
 import com.itextpdf.text.pdf.PdfPCell;
 
 
 /**
  * A class for printing out the barcode, description, and dates of items to a PDF on disk.
  * The class should be instantiated, Items added, and then finish() should be called 
  * in order for all of the changes to be written.
  */
 public class BarcodePdf{
 
 	private Document _d;
 	private PdfWriter _writer;
 	private Boolean _open;
 	private PdfContentByte _rawContent;
 	private PdfPTable _table;
 	private int _itemsAdded; // Keeps track of how many items have been added, in case we need to fill up dummy items.
 
 
 	/**
 	 * Constructor. Takes in a string representing where the file should be written to disk.
 	 * @param filepath
 	 * @throws DocumentException 
 	 * @throws FileNotFoundException 
 	 */
 	public BarcodePdf(String fname) throws FileNotFoundException, DocumentException{
 
 		_d = new Document();
 		
 		_writer = PdfWriter.getInstance(_d, new FileOutputStream(fname));
 		
 		_d.setMargins(25, 25, 25, 25);
 		_d.open();
 		
 		_open = true;
 		_rawContent = _writer.getDirectContent();
 		
 		_table = new PdfPTable(4);
 		_table.setHorizontalAlignment(Element.ALIGN_CENTER);
 		
 		_itemsAdded = 0;
 
 	}
 	
 	/**
 	 * Adds an item to the page for printing.
 	 * If the document was not successfully opened, or if the item has an invalid barcode,
 	 * no action will be taken and a negative Result will be returned.
 	 * @param item
 	 */
 	public Result addItem(Item item){
 		if (!_open){
 			return new Result(false, "Cannot add a product to an unopened file.");
 		}
 		if (!item.getBarcode().isValid()){
 			return new Result(false, "Cannot print an invalid barcode.");
 		}
 
 		// Generate the barcode image:
 		BarcodeEAN codeEAN = new BarcodeEAN();
 		codeEAN.setCodeType(com.itextpdf.text.pdf.Barcode.UPCA);
 		codeEAN.setCode(item.getBarcode().toString());
 		Image bcimage = codeEAN.createImageWithBarcode(_rawContent, null, null);
 		
 		// Add some text to a chunk, and the image to a chunk.
 		Font font = new Font(Font.FontFamily.HELVETICA, 8);
 		Chunk c1 = new Chunk(item.getProductDescription() + "\n" + item.getShortEntryDateString() + " -> " + item.getShortExpirationDateString() + "\n\n");
 		c1.setFont(font);
 		Phrase p = new Phrase(c1);
 		Chunk c2 = new Chunk(bcimage, 0, 0);
 		p.add(c2);
 
 		PdfPCell cell = new PdfPCell(p);
 		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 		cell.setPadding(10);
 		cell.setBorder(0);
 		_table.addCell(cell);
 		
 		_itemsAdded++;
 
 		return new Result(true, "Barcode added successfully.");
 	}
 	
 	/**
 	 * Writes out the information and PDF file to disk, and closes the stream.
 	 * Returns a negative Result if there was an error creating the document, and 
 	 * a positive result if the document was successfully written and closed.
 	 */
 	public Result finish(){
 		if (_open){
 			fillNullCells();
 			try {
 				_d.add(_table);
 			} catch (DocumentException e) {
 				return new Result(false, "The document was not successfully constructed.");
 			}
 			_d.close();
 			_open = false;
 		}
 		
 		return new Result(true, "The document is closed.");
 	}
 	
 	/**
 	 * This is a convenience function to make it easy to fill up the remaining table cells.
 	 */
 	private void fillNullCells(){
		int itemsToAdd = 4 - (_itemsAdded % 4);
		while(itemsToAdd > 0){
			PdfPCell c = new PdfPCell();
			c.setBorder(0);
			_table.addCell(c);
			itemsToAdd--;
 		}
 	}
 }
