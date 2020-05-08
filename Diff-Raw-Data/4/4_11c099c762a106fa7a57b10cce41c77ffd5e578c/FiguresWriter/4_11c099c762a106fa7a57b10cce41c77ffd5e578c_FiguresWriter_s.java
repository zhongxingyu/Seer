 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package invenio.pdf.features;
 
 import invenio.common.Images;
 import invenio.common.XmlTools;
 import invenio.pdf.core.ExtractorLogger;
 import invenio.pdf.core.ExtractorParameters;
 import invenio.pdf.core.FeatureNotPresentException;
 import invenio.pdf.core.PDFDocumentManager;
 import invenio.pdf.core.documentProcessing.PDFDocumentTools;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.geom.AffineTransform;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 import java.io.Writer;
 import java.util.LinkedList;
 import java.util.List;
 import org.apache.batik.svggen.SVGGraphics2DIOException;
 import org.json.JSONException;
 import org.w3c.dom.DOMImplementation;
 import org.apache.batik.dom.GenericDOMImplementation;
 import org.apache.batik.svggen.SVGGraphics2D;
 import org.json.JSONWriter;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 /**
  * A class allowing to write plots together with meta-data into files
  * @author piotr
  */
 public class FiguresWriter {
 
     public static void writePlots(PDFDocumentManager document, File outputDirectory, boolean saveAttachments)
             throws FeatureNotPresentException, Exception {
         Figures plots = (Figures) document.getDocumentFeature(Figures.featureName);
         for (FigureCandidate figure : plots.getToplevelPlots()) {
             writePlot(figure, outputDirectory, saveAttachments);
         }
     }
 
     public static void writePlot(FigureCandidate plot, File outputDirectory, boolean saveAttachments) throws FileNotFoundException, Exception {
         // first assure, the output directory exists
 
         ExtractorParameters parameters = ExtractorParameters.getExtractorParameters();
         if (!outputDirectory.exists()) {
             outputDirectory.mkdir();
         }
 
         setFileNames(plot, outputDirectory);
         writePlotMetadataToFileJSON(plot);
         writePlotMetadataToFile(plot);
 
         if (saveAttachments) {
             writePlotPng(plot);
             if (parameters.generateSVG()) {
                 writePlotSvg(plot);
             }
             if (parameters.generatePlotProvenance()) {
                 writePlotAnnotatedPage(plot);
             }
             writePlotCaptionImage(plot);
         }
     }
     //// JSON version of writers
 
     public static void writePlotMetadataToFileJSON(FigureCandidate plot) throws FileNotFoundException, Exception {
         LinkedList<FigureCandidate> plots = new LinkedList<FigureCandidate>();
         plots.add(plot);
         writePlotsMetadataToFileJSON(plots, plot.getFile("metadataJSON"));
 
     }
 
     public static void writePlotsMetadataToFileJSON(List<FigureCandidate> plots, File plotMetadataFile)
             throws FileNotFoundException, Exception {
         StringWriter stringWriter = new StringWriter();
 
         JSONWriter writer = new JSONWriter(stringWriter).array();
 
         for (FigureCandidate plot : plots) {
             writePlotMetadataJSON(writer, plot);
         }
 
         writer = writer.endArray();
 
         FileWriter out = new FileWriter(plotMetadataFile.getAbsolutePath());
         out.write(stringWriter.toString());
         out.close();
     }
 
     public static JSONWriter writePlotMetadataJSON(JSONWriter writer, FigureCandidate plot) throws JSONException {
         JSONWriter w = writer;
         ExtractorParameters parameters = ExtractorParameters.getExtractorParameters();
 
         w = w.object();
 
         w = w.key("identifier").value(plot.getId());
 
         w = w.key("sourceDocument").value(plot.getPageManager().
                 getDocumentManager().getSourceFileName());
         w = w.key("caption").value(plot.getCaption());
         w = w.key("captionFile").value(plot.getFile("captionImage").getAbsolutePath());
         // writing the fiels section
         w = w.key("files");
         w = w.object();
         w = w.key("png");
         w = w.value(plot.getFile("png").getAbsolutePath());
 
         if (parameters.generateSVG()) {
             w = w.key("svg");
             w = w.value(plot.getFile("svg").getAbsolutePath());
         }
 
         w = w.endObject();
 
         // location of the source
 
         w = w.key("location").object();
         w = w.key("pageNum").value(plot.getPageManager().getPageNumber());
 
         Rectangle pb = plot.getPageManager().getPageBoundary();
         if (pb != null) {
             w = w.key("pageResolution").object();
             w = w.key("width").value(pb.width);
             w = w.key("height").value(pb.height);
             w = w.endObject();
         }
         Rectangle bd = plot.getBoundary();
         if (bd != null) {
             w = w.key("boundary").object();
             w = w.key("x").value(bd.x);
             w = w.key("y").value(bd.y);
             w = w.key("width").value(bd.width);
             w = w.key("height").value(bd.height);
             w = w.endObject();
         }
 
         w = w.key("pageScale").value(ExtractorParameters.getExtractorParameters().getPageScale());
         w = w.endObject();
 
         w = w.key("captionLocation").object();
         w = w.key("pageNum").value(plot.getPageManager().getPageNumber());
 
         pb = plot.getPageManager().getPageBoundary();
         if (pb != null) {
             w = w.key("pageResolution").object();
             w = w.key("width").value(pb.width);
             w = w.key("height").value(pb.height);
             w = w.endObject();
         }
         bd = plot.getCaption().boundary;
         if (bd != null) {
             w = w.key("boundary").object();
             w = w.key("x").value(bd.x);
             w = w.key("y").value(bd.y);
             w = w.key("width").value(bd.width);
             w = w.key("height").value(bd.height);
             w = w.endObject();
         }
         w = w.endObject();
 
         w.key("annotatedImage").value(plot.getFile("annotatedImage").getAbsolutePath());
         w = w.endObject();
         return w;
     }
 
     //// XML versions of writers
     public static void writePlotMetadataToFile(FigureCandidate plot) throws FileNotFoundException, Exception {
         LinkedList<FigureCandidate> plots = new LinkedList<FigureCandidate>();
         plots.add(plot);
         writePlotsMetadataToFile(plots, plot.getFile("metadata"));
     }
 
     public static void writePlotsMetadata(Document document, Element containerEl,
             List<FigureCandidate> plots) throws FileNotFoundException, Exception {
 
         Element plotsCollectionElement = document.createElement("plots");
         containerEl.appendChild(plotsCollectionElement);
 
         for (FigureCandidate plot : plots) {
             writePlotMetadata(document, plotsCollectionElement, plot);
         }
     }
 
     public static void writePlotsMetadataToFile(List<FigureCandidate> plots, File plotMetadataFile)
             throws FileNotFoundException, Exception {
 
         Document document = XmlTools.createXmlDocument();
 
         Element rootElement = document.createElement("publication");
         document.appendChild(rootElement);
 
         writePlotsMetadata(document, rootElement, plots);
 
         XmlTools.saveXmlDocument(document, plotMetadataFile);
     }
 
     public static void writePlotMetadata(Document document, Element containerElement, FigureCandidate plot)
             throws FileNotFoundException, Exception {
 
         ExtractorParameters parameters = ExtractorParameters.getExtractorParameters();
         Element rootElement = document.createElement("plot");
         containerElement.appendChild(rootElement);
 
         // plot identifier
         XmlTools.appendElementWithTextNode(document, rootElement, "identifier", plot.getId());
 
         //        ps.println("identifier= " + plot.getId());
 
         // plot  image files
         XmlTools.appendElementWithTextNode(document, rootElement, "png", plot.getFile("png").getAbsolutePath());
         if (parameters.generateSVG()) {
             XmlTools.appendElementWithTextNode(document, rootElement, "svg", plot.getFile("svg").getAbsolutePath());
         }
         // location of the source
 
         Element locationElement = document.createElement("location");
         rootElement.appendChild(locationElement);
         // main document file
         XmlTools.appendElementWithTextNode(document, locationElement, "pdf", plot.getPageManager().getDocumentManager().getSourceFileName());
         // document scale
         XmlTools.appendElementWithTextNode(document, locationElement, "scale", "" + ExtractorParameters.getExtractorParameters().getPageScale());
         // current page resulution
         Rectangle pb = plot.getPageManager().getPageBoundary();
         if (pb != null) {
             Element pageResolution = document.createElement("pageResolution");
 
             locationElement.appendChild(pageResolution);
             XmlTools.appendElementWithTextNode(document, pageResolution, "width", "" + pb.width);
             XmlTools.appendElementWithTextNode(document, pageResolution, "height",
                     "" + pb.height);
         }
         // main document page (indexed from 0)
         XmlTools.appendElementWithTextNode(document, locationElement, "pageNumber",
                 "" + plot.getPageManager().getPageNumber());
 
         // coordinates in the main document
         XmlTools.appendRectangle(document, locationElement, "pageCoordinates",
                 plot.getBoundary());
 
         Element captionEl = document.createElement("caption");
         rootElement.appendChild(captionEl);
         // caption coordinates
         XmlTools.appendRectangle(document, captionEl, "coordinates",
                 plot.getCaption().boundary);
         // caption text
         XmlTools.appendElementWithTextNode(document, captionEl, "captionText",
                 "" + plot.getCaption());
         // caption image
         XmlTools.appendElementWithTextNode(document, rootElement,
                 "captionImage", plot.getFile("captionImage").getAbsolutePath());
         // debug image
         XmlTools.appendElementWithTextNode(document, rootElement,
                 "annotatedImage",
                 plot.getFile("annotatedImage").getAbsolutePath());
     }
 
     /** Prepare and write the image of an annotated plot
      *
      */
     public static void writePlotAnnotatedPage(FigureCandidate plot) throws Exception {
         BufferedImage pageImg = Images.copyBufferedImage(plot.getPageManager().getRenderedPage());
         Graphics2D gr = (Graphics2D) pageImg.getGraphics();
         gr.setTransform(AffineTransform.getTranslateInstance(0, 0));
         gr.setColor(Color.blue);
         Rectangle bd = plot.getBoundary();
         gr.drawRect(bd.x, bd.y, bd.width, bd.height);
         gr.setColor(Color.green);
         bd = plot.getCaption().boundary;
         if (bd != null) {
             gr.drawRect(bd.x, bd.y, bd.width, bd.height);
         }
         Images.writeImageToFile(pageImg, plot.getFile("annotatedImage"));
     }
 
     public static void writePlotPng(FigureCandidate plot) throws IOException {
         Rectangle b = plot.getBoundary();
         if (plot.getPageManager().getRenderedPage() != null) {
            Images.writeImageToFile(plot.getPageManager().getRenderedPage().getSubimage(b.x, b.y, b.width, b.height), plot.getFile("png"));
         }
     }
 
     public static void writePlotCaptionImage(FigureCandidate plot) throws IOException {
         Rectangle b = plot.getCaption().boundary;
         if (plot.getPageManager().getRenderedPage() != null) {
             if (b == null) {
                 return;
             }
             Images.writeImageToFile(plot.getPageManager().getRenderedPage().getSubimage(b.x, b.y, b.width, b.height), plot.getFile("captionImage"));
         }
     }
 
     public static void writePlotSvg(FigureCandidate plot) throws UnsupportedEncodingException, SVGGraphics2DIOException, FileNotFoundException, IOException {
         // Get a DOMImplementation.
         DOMImplementation domImpl =
                 GenericDOMImplementation.getDOMImplementation();
 
         // Create an instance of org.w3c.dom.Document.
         String svgNS = "http://www.w3.org/2000/svg";
         Document document = domImpl.createDocument(svgNS, "svg", null);
 
         // Create an instance of the SVG Generator.
         SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
         // Ask the test to render into the SVG Graphics2D implementation.
 
         // paint
         ExtractorParameters params = ExtractorParameters.getExtractorParameters();
 
         Rectangle plotBd = plot.getBoundary();
         svgGenerator.setClip(0, 0, (int) plotBd.getWidth(),
                 (int) plotBd.getHeight());
         svgGenerator.setTransform(AffineTransform.getTranslateInstance(-plotBd.getX(), -plotBd.getY()));
         svgGenerator.setSVGCanvasSize(new Dimension((int) plotBd.getWidth(),
                 (int) plotBd.getHeight()));
 
         PDFDocumentTools.renderToCanvas(plot.getPageManager(), svgGenerator,
                 params.getPageScale());
 
         // Finally, stream out SVG to the standard output using
         // UTF-8 encoding.
         boolean useCSS = true; // we want to use CSS style attributes
         FileOutputStream fos = new FileOutputStream(plot.getFile("svg"));
         Writer out = new OutputStreamWriter(fos, "UTF-8");
         svgGenerator.stream(out, useCSS);
         out.close();
         fos.close();
     }
 
     /**
      * Calculates names of files where the plot should be saved
      * @param plot
      */
     public static void setFileNames(FigureCandidate plot, File outputDirectory) {
         //TODO Create some more realistic file names
         ExtractorLogger.logMessage(2, "Saving a plot from page "
                 + plot.getPageManager().getPageNumber()
                 + " number of operations: " + plot.getOperations().size());
 
         plot.addFile("metadata", new File(outputDirectory.getPath(),
                 plot.getId() + "_metadata.xml"));
         plot.addFile("metadataJSON", new File(outputDirectory.getPath(),
                 plot.getId() + "_metadata.json"));
         plot.addFile("png", new File(outputDirectory.getPath(),
                 plot.getId() + ".png"));
         plot.addFile("svg", new File(outputDirectory.getPath(),
                 plot.getId() + ".svg"));
         plot.addFile("annotatedImage", new File(outputDirectory.getPath(),
                 plot.getId() + "_annotated.png"));
         plot.addFile("captionImage", new File(outputDirectory.getPath(),
                 plot.getId() + "caption.png"));
 
     }
 }
