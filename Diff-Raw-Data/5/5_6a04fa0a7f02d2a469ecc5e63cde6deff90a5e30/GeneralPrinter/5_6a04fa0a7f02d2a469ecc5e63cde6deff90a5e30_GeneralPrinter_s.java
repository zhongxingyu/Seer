 package de.unisiegen.tpml.ui;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Graphics2D;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.util.ArrayList;
 import java.util.LinkedList;
 
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 import com.lowagie.text.Document;
 import com.lowagie.text.PageSize;
 import com.lowagie.text.Rectangle;
 import com.lowagie.text.pdf.PRAcroForm;
 import com.lowagie.text.pdf.PdfContentByte;
 import com.lowagie.text.pdf.PdfCopy;
 import com.lowagie.text.pdf.PdfImportedPage;
 import com.lowagie.text.pdf.PdfReader;
 import com.lowagie.text.pdf.PdfWriter;
 
 import de.unisiegen.tpml.graphics.AbstractProofComponent;
 
 public class GeneralPrinter {
     private Document document;
 
     private JPanel caller;
 
     private AbstractProofComponent comp;
 
     private Rectangle pageFormat;
 
     private java.awt.Graphics2D g2;
 
     LinkedList<LinkedList<Component>> pages;
 
     private double scale = .5;
 
     private int right = 40;
 
     private int above = 40;
 
     private int naturalright = 20;
 
     private int naturalabove = 20;
     
     private String tmpdir;
 
     public GeneralPrinter(JPanel caller) {
 	// document = new Document(PageSize.A4);
 	// document = new Document(PageSize.A4.rotate());
 	this.caller = caller;
     }
 
     public boolean print(AbstractProofComponent icomp) {
 	JOptionPane.showMessageDialog(caller, "Put Layoutchooser and Filechooser here.");
 	pageFormat = PageSize.A4.rotate();
 	this.comp = icomp;
 
 	try {
 	    // creating a temporary file to wirte to:
 	    tmpdir = System.getProperty("java.io.tmpdir");
 	    System.out.println(tmpdir);
 
 	    // Number Of Pages
 	    int nop = -1;
 	    int i = 0;
 
 	    do {
 		document = new Document(pageFormat);
 		PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(tmpdir+"/tmp" + i + ".pdf"));
 		document.open();
 		PdfContentByte cb = writer.getDirectContent();
 		// do not use the scale factor in the next one!
 		java.awt.Graphics2D g1 = cb.createGraphicsShapes(pageFormat.getWidth(), pageFormat.getHeight());
 		g1.scale(scale, scale);
 		// g2.setClip(right, above,
 		// g2.getClipBounds().width,g2.getClipBounds().height);
 		g2 = (Graphics2D) g1.create(right, above, g1.getClipBounds().width - 2 * right, g1.getClipBounds().height - 2 * above);
 		JPanel j1 = new JPanel();
 		j1.setSize(g2.getClipBounds().width, g2.getClipBounds().height);
 
 		j1.setBackground(new Color(255, 255, 255));
 		if (nop == -1) {
 		    nop = (comp.getHeight() / j1.getHeight() + 1);
 		    comp.setAvailableWidth(g2.getClipBounds().width);
 		    comp.setAvailableHeight(g2.getClipBounds().height);
 		    comp.validate();
 		    comp.doLayout();
 		}
 
 		j1.add(comp);
 		// on the first page we will have to eliminate the natural top spacing
 		if (i == 0) {
 		    comp.setBounds(-naturalright, -naturalabove - i * j1.getHeight(), comp.getWidth(), comp.getHeight());
 		} else {
 		    comp.setBounds(-naturalright, -i * j1.getHeight(), comp.getWidth(), comp.getHeight());
 		}
 		j1.paint(g2);
 		g2.dispose();
 		g1.dispose();
 		document.close();
 		i++;
 	    } while (i < nop);
 
 	    // concatenate the temporary pages
 	    this.concatenatePages(nop);
 
 	    // remove the temporary pages now
 	    this.deleteFiles(nop);
 
 	    JOptionPane.showMessageDialog(caller, "Document has been printed!");
 
 	} catch (Exception de) {
 	    de.printStackTrace();
 	}
 	return true;
     }
 
     private void concatenatePages(int nop) {
 	// concatenate temporary files here
 	try {
 	    int pageOffset = 0;
 	    ArrayList master = new ArrayList();
 	    int f = 0;
 	    String outFile = "out.pdf";
 	    Document document = null;
 	    PdfCopy writer = null;
 	    while (f < nop) {
 		// we create a reader for a certain document
		PdfReader reader = new PdfReader(tmpdir+"tmp" + f + ".pdf");
 		reader.consolidateNamedDestinations();
 		// we retrieve the total number of pages
 		int n = reader.getNumberOfPages();
 
 		pageOffset += n;
 
 		if (f == 0) {
 		    // step 1: creation of a document-object
 		    document = new Document(reader.getPageSizeWithRotation(1));
 		    // step 2: we create a writer that listens to the
 		    // document
 		    writer = new PdfCopy(document, new FileOutputStream(outFile));
 		    // step 3: we open the document
 		    document.open();
 		}
 		// step 4: we add content
 		PdfImportedPage page;
 		for (int i = 0; i < n;) {
 		    ++i;
 		    page = writer.getImportedPage(reader, i);
 		    writer.addPage(page);
 		}
 		PRAcroForm form = reader.getAcroForm();
 		if (form != null)
 		    writer.copyAcroForm(reader);
 		f++;
 	    }
 	    if (!master.isEmpty())
 		writer.setOutlines(master);
 	    // step 5: we close the document
 	    document.close();
 	} catch (Exception e) {
 	    e.printStackTrace();
 	}
     }
 
     private void deleteFiles(int nop) {
 	for (int i = 0; i < nop; i++) {
	    File f = new File(tmpdir+"tmp" + i + ".pdf");
 	    f.delete();
 	}
     }
 
 }
