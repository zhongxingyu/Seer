 package utils.onepixelalignmentdrawer;
 
 import java.awt.Color;
 import java.awt.image.BufferedImage;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.List;
 
 import javax.imageio.IIOImage;
 import javax.imageio.ImageIO;
 import javax.imageio.ImageWriteParam;
 import javax.imageio.ImageWriter;
 import javax.imageio.stream.ImageOutputStream;
 
 import utils.color.ColorStrategy;
 import utils.color.DnaColorStrategy;
 import utils.color.ProteinColorStrategy;
 
 import cmdGA.NoOption;
 import cmdGA.Parser;
 import cmdGA.SingleOption;
 import cmdGA.exceptions.IncorrectParameterTypeException;
 import cmdGA.parameterType.InputStreamParameter;
 import cmdGA.parameterType.OutFileParameter;
 import fileformats.fastaIO.FastaMultipleReader;
 import fileformats.fastaIO.Pair;
 
 public class OnePixel {
 
 	public static void main(String[] args) {
 		
 		////////////////////////////////
 		// Create Parser
 		Parser parser = new Parser();
 		
 		////////////////////////////////
 		// Add Parser Options
 		SingleOption inOpt = new SingleOption(parser, System.in, "-infile", InputStreamParameter.getParameter());
 
 		SingleOption outOpt = new SingleOption(parser, null, "-out", OutFileParameter.getParameter());
 		
 		NoOption isprotOpt = new NoOption(parser, "-isprotein");
 		
		
 		////////////////////////////////
 		// Parse Command Line
 		try {
 			
 			parser.parseEx(args);
 		
 		} catch (IncorrectParameterTypeException e) {
 			
 			System.err.println("There was an error trying to parse the command line:" + e.getMessage());
 			
 			System.exit(1);
 			
 		}
 		
 		//////////////////////////////
 		// Get Command Line Values
 		BufferedReader in = new BufferedReader(new InputStreamReader((InputStream) inOpt.getValue()));
 		
 		File out = (File) outOpt.getValue();
 		
		boolean isProtein = isprotOpt.getValue();
 		
 		//////////////////////////////
 		// Checks if output is valid
 		if (out==null) {
 			
 			System.err.println("There Was an error with the selected output file");
 			
 			System.exit(1);
 			
 		}
 		
 		System.err.println("# Output file: " + out.getAbsolutePath());
 
 		System.err.println("# Is Protein : " + String.valueOf(isProtein));
 		
 		//////////////////////////////
 		// Create Fasta Reader
 		FastaMultipleReader fmr = new FastaMultipleReader();
 
 		//////////////////////////////
 		// Read Input Fasta Alignment
 		List<Pair<String, String>> alignment = fmr.readBuffer(in);
 		
 		//////////////////////////////
 		// Create Coloring Scheme
 		ColorStrategy color = (isProtein)?(new ProteinColorStrategy()):(new DnaColorStrategy());
 		
 		//////////////////////////////
 		// Create OnePixel Object
 		OnePixel onePixel = new OnePixel();
 		
 		//////////////////////////////
 		// Create Image
 		BufferedImage onePixelAlignmentImage = onePixel.drawImage(alignment, color);
 		
 		//////////////////////////////
 		// Export Image
 		try {
 			onePixel.writePNG(onePixelAlignmentImage, out);
 		} catch (IOException e) {
 			System.err.println("There was an error trying to write PNG file: "+e.getMessage());
 		}
 		
 	}
 
 
 
 	/////////////////////////////////////
 	// Public Interface
 	/***
 	 * Creates an Image representation of a sequence alignment 
 	 * using one pixel per base/amino acid
 	 * @param alignment
 	 * @param color
 	 * @return
 	 */
 	public BufferedImage drawImage(List<Pair<String, String>> alignment, ColorStrategy color) {
 		
 		int height = alignment.size();
 		
 		int width = alignment.get(0).getSecond().length();
 		
 		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
 		
 //		Graphics2D graphics = (Graphics2D) image.getGraphics();
 
 		for (int i = 0; i< height; i++) {
 			
 			for (int j=0; j<width; j++) {
 				
 				char residue = alignment.get(i).getSecond().charAt(j);
 		
 				Color currentColor;
 				
 				if (residue=='-') {
 					
 					currentColor= new Color(245,245,245);
 					
 				} else {
 				
 					currentColor= color.getColor(residue);
 					
 				}
 				
 				image.setRGB(j, i, currentColor.getRGB());
 				
 			}
 		
 		}
 		
 		return image;
 		
 	}
 	
 	/**
 	 * Writes a BufferedImage into a PNG file.
 	 * @param onePixelAlignmentImage
 	 * @param out
 	 * @throws IOException 
 	 */
 	public void writePNG(BufferedImage onePixelAlignmentImage, File outfile) throws IOException {
 		
 		FileOutputStream out = new FileOutputStream(outfile);
 		
 		ImageWriter imagewriter = ImageIO.getImageWritersByFormatName("png").next();
 		
 		ImageWriteParam writerparam = imagewriter.getDefaultWriteParam();
 		
 		ImageOutputStream ios = ImageIO.createImageOutputStream(out);
 		
 		imagewriter.setOutput(ios);
 		
 		imagewriter.write(null, new IIOImage(onePixelAlignmentImage, null, null), writerparam);
 		
 		imagewriter.dispose();
 		
 	}
 	
 }
