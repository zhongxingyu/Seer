 package blink;
 
 import java.awt.Graphics2D;
 import java.awt.event.ActionEvent;
 import java.io.File;
 import java.io.FileOutputStream;
 
 import javax.swing.AbstractAction;
 import javax.swing.JFileChooser;
 import javax.swing.JPanel;
 
 import com.itextpdf.awt.PdfGraphics2D;
 import com.itextpdf.text.Document;
 import com.itextpdf.text.pdf.PdfContentByte;
 import com.itextpdf.text.pdf.PdfTemplate;
 import com.itextpdf.text.pdf.PdfWriter;
 
 import edu.uci.ics.jung.visualization.VisualizationViewer;
 
 public class SavePDF extends AbstractAction {
 
 	/**
 	 * for serialisation
 	 */
 	private static final long serialVersionUID = 979401257782838856L;
 
 	private JPanel _panel;
 	private VisualizationViewer _view;
 
 	public SavePDF(JPanel panel, VisualizationViewer view) {
 		super("PDF");
 		_panel = panel;
 		_view = view;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		hardwork();
 	}
 
 	public void hardwork() {
 		// java.awt.Rectangle dim = _gemViewer.getBounds();
 		// int width = dim.width;
 		// int height = dim.height;
 		// Rectangle dim =
 		// ((MyTutteLayout)_gemViewer.getLayout()).boundingBox();
 		int width = 600;
 		int height = 800;
 		JFileChooser fc = new JFileChooser();
 		fc.setMultiSelectionEnabled(true);
 		String lastPath = App.getProperty("lastSavePDF");
 		if (lastPath != null) {
 			fc.setSelectedFile(new File(lastPath));
 		}
 		int r = fc.showSaveDialog(_panel);
 		if (r == JFileChooser.APPROVE_OPTION) {
 			File selFile = fc.getSelectedFile();
 			App.setProperty("lastSavePDF", selFile.getAbsolutePath());
 			// print the panel to pdf
 			Document document = new Document();
 			try {
 				PdfWriter writer = PdfWriter.getInstance(document,
 						new FileOutputStream(selFile));
 				document.open();
 				PdfContentByte contentByte = writer.getDirectContent();
				PdfTemplate template = contentByte.createTemplate(500, 660);
 //				Graphics2D g2 = template.createGraphics(500, 500);
 				Graphics2D g2 = new PdfGraphics2D(contentByte, width, height);
 				// the idea is that "width" and "height" might change their values
 				// for now a fixed value is being used
 				double scx = (500 / (double) width);
 				double scy = (500 / (double) height);
				g2.scale(scx, scx);
 				if(_view == null) {
 					_panel.print(g2);
 				} else {
 					_view.print(g2);
 				}
 				g2.dispose();
 				contentByte.addTemplate(template, 30, 300);
 				// contentByte.addTemplate(template,
 				// AffineTransform.getScaleInstance(template.getWidth()/dim.width,
 				// template.getHeight()/dim.height));
 			} catch (Exception e) {
 				e.printStackTrace();
 			} finally {
 				if (document.isOpen()) {
 					document.close();
 				}
 			}
 		}
 	}
 
 }
