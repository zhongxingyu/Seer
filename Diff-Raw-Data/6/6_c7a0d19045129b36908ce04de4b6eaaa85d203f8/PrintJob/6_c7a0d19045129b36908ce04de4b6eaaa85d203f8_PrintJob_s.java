 package plotter.pdf;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.io.IOUtils;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import plotter.util.Configuration;
 
 import com.lowagie.text.pdf.PdfReader;
 
 public class PrintJob extends File {
 
 	private static final long serialVersionUID = 1578101753406396888L;
 
 	private String filename;
 	private int numberOfPages;
 	private String printSize;
 	private int copies;
 
 	List<File> thumbnails = new ArrayList<File>();
 
 	public PrintJob(String filename, String originalFileName) throws IOException {
 		super(filename);
 
 		this.filename = originalFileName;
 
 		this.setNumberOfPages(getPageCount());
 	}
 
 	/**
 	 * Use iText to retrieve the number of pages of the PDF
 	 * 
 	 * @return the number of pages
 	 * @throws IOException
 	 */
 	private int getPageCount() throws IOException {
 		int pageCount = 0;
 
 		ByteArrayInputStream bais = null;
 		PdfReader reader = null;
 
 		try {
 			reader = new PdfReader(new FileInputStream(this));
 
 			pageCount = reader.getNumberOfPages();
 		} catch (Exception e) {
 			throw new IOException("Could not retrieve page count.", e);
 		} finally {
 			if (reader != null)
 				reader.close();
 			IOUtils.closeQuietly(bais);
 		}
 
 		return pageCount;
 	}
 
 	/**
 	 * Convert PDF to images
 	 * 
 	 * @param resolution
	 *            the resolution in dpo
 	 * @param paperSize
 	 *            the paper size (e.g. a2, a4, a10...)
 	 * @param portrait
 	 *            rotate the images to portrait format
 	 * @return a list of converted image files
 	 * @throws IOException
 	 */
 	private List<File> convertToImages(int resolution, String paperSize, boolean portrait) throws IOException {
 		// Get temporary file names for images
 		File tmp = File.createTempFile("plotter_%d_", ".png");
 
 		ArrayList<String> command = new ArrayList<String>();
 
 		// Build command
 		command.add(Configuration.getProperty("plotter.ghostscript.executable"));
 		command.add("-dQUIET");
 		command.add("-dNOPAUSE");
 		command.add("-dBATCH");
 		command.add("-dSAFER");
		command.add("-r" + resolution);
 		command.add("-sDEVICE=png16m");
 		command.add("-dTextAlphaBits=4");
 		command.add("-dGraphicsAlphaBits=4");
 		command.add("-dPDFFitPage");
 		command.add("-sPAPERSIZE=" + paperSize);
 		if (portrait) {
 			command.add("-dNORANGEPAGESIZE");
 		}
 		command.add("-sOutputFile=" + tmp.getAbsolutePath());
 		command.add(this.getAbsolutePath());
 
 		ProcessBuilder processBuilder = new ProcessBuilder(command);
 		Process proc = processBuilder.start();
 
 		int exitCode;
 		try {
 			exitCode = proc.waitFor();
 		} catch (InterruptedException e) {
 			throw new IOException("PDF to image conversion interrupted.", e);
 		}
 
 		if (exitCode != 0) {
 			String errorMessage = IOUtils.toString(proc.getErrorStream())
 					+ " - " + IOUtils.toString(proc.getInputStream());
 
 			throw new IOException(
 					"PDF could not be converted to images, message: "
 							+ errorMessage);
 		}
 
 		// Retrieve images
 		List<File> images = new ArrayList<File>();
 		for (int i = 1; i <= this.getNumberOfPages(); i++) {
 			String imageFilename = tmp.getAbsolutePath().replace("%d",
 					Integer.toString(i));
 
 			images.add(new File(imageFilename));
 		}
 
 		tmp.delete();
 
 		return images;
 	}
 
 	/**
 	 * Generate 100dpi thumbnails (results in 101x146px)
 	 * 
 	 * @throws IOException
 	 */
 	public void generateThumbnails() throws IOException {
 		thumbnails = convertToImages(100, "a10", false);
 	}
 
 	public String getFilename() {
 		return filename;
 	}
 
 	public List<File> getThumbnails() {
 		return thumbnails;
 	}
 
 	public int getNumberOfPages() {
 		return numberOfPages;
 	}
 
 	public void setNumberOfPages(int numberOfPages) {
 		this.numberOfPages = numberOfPages;
 	}
 
 	public String getPrintSize() {
 		return printSize;
 	}
 
 	public void setPrintSize(String printSize) {
 		this.printSize = printSize;
 	}
 
 	public int getCopies() {
 		return copies;
 	}
 
 	public void setCopies(int copies) {
 		this.copies = copies;
 	}
 
 	public JSONObject toJSON() throws JSONException {
 		return new JSONObject().put("name", this.getFilename()).put("pages",
 				this.getNumberOfPages());
 	}
 }
