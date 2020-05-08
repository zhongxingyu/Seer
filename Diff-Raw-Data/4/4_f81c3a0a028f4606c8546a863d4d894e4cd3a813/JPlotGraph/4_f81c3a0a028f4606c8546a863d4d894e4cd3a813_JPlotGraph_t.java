 import org.jfree.data.xy.XYSeries;
 import org.jfree.data.xy.XYSeriesCollection;
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.chart.JFreeChart;
 import org.apache.batik.dom.GenericDOMImplementation;
 import org.w3c.dom.DOMImplementation;
 import org.w3c.dom.Document;
 import java.awt.geom.Rectangle2D;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 import org.apache.batik.svggen.SVGGraphics2D;
 import org.apache.batik.svggen.SVGGraphics2DIOException;
 
 /**
  * A line graph generator
  * input(command line argument): a file with all path+names of data files
  * output corresponding svg graph in the same directory
  * @author JJ
  *
  */
 public class JPlotGraph {
 
 	/**
 	 * a simple java application to display graphs
 	 * 
 	 * @param arg
 	 *            [0] the file contains all the file path and name of datasets
 	 */
 	public static void main(String[] args) {
 		ArrayList<String> filePath = new ArrayList<String>();
 		BufferedReader br = null;
 		// read in the list of files for generating graphs
 		try {
 			String sCurrentLine;
 			br = new BufferedReader(new FileReader(args[0]));
 			while ((sCurrentLine = br.readLine()) != null) {
 				System.out.println(sCurrentLine);
 				filePath.add(sCurrentLine);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (br != null)
 					br.close();
 			} catch (IOException ex) {
 				ex.printStackTrace();
 			}
 		}
 
 		for (String fileS : filePath) {
 			// if not empty or null,then create a plot file
 			if (!((fileS == null) || (fileS.isEmpty()))) {
 				File dataFile = new File(fileS);
 				// the whole dataset from file
 				ArrayList<float[]> dataSet = new ArrayList<float[]>();
 				// read the file into dataset
 				Scanner mScanner=null;
 				try {
 					 mScanner= new Scanner(dataFile);
 					while (mScanner.hasNextLine()) {
 						// only read in lines starting with a float number
 						if (mScanner.hasNextFloat()) {
 							// data Entry for each line
 							float[] dataEntry = new float[3];
 							dataEntry[0] = mScanner.nextFloat();
 							dataEntry[1] = mScanner.nextFloat();
 							dataEntry[2] = mScanner.nextFloat();
 							dataSet.add(dataEntry);
 							//inc to next line
 							mScanner.nextLine();
 						} else
 							mScanner.nextLine();
 					}
 				} catch (FileNotFoundException e) {
 					System.out.println("dataFile doesn't exist!");
 				} finally{
 					if (mScanner != null)
 						mScanner.close();
 				}
 				// create data sample series
 				XYSeries x_data = new XYSeries("X axis");
 				XYSeries y_data = new XYSeries("Y axis");
 				XYSeries z_data = new XYSeries("Z axis");
 				/* Define some XY Data series for the SVG chart */
 				for (int index = 0; index < dataSet.size(); index++) {
 					x_data.add(index, dataSet.get(index)[0]);
 					y_data.add(index, dataSet.get(index)[1]);
 					z_data.add(index, dataSet.get(index)[2]);
 				}
 				/* Add all XYSeries to XYSeriesCollection */
 				// XYSeriesCollection implements XYDataset
 				XYSeriesCollection svgXYDataSeries = new XYSeriesCollection();
 				// add series using addSeries method
 				svgXYDataSeries.addSeries(x_data);
 				svgXYDataSeries.addSeries(y_data);
 				svgXYDataSeries.addSeries(z_data);
 
				String chartTitle=fileS+"readings";
 				// Use createXYLineChart to create the chart
 				JFreeChart XYLineChart = ChartFactory.createXYLineChart(
						chartTitle, "time", "m/s^2",
 						svgXYDataSeries, PlotOrientation.VERTICAL, true, true,
 						false);
 
 				/* Write Chart output as SVG */
 				/* Get DOM Implementation */
 				DOMImplementation mySVGDOM = GenericDOMImplementation
 						.getDOMImplementation();
 				/* create Document object */
 				Document document = mySVGDOM.createDocument(null, "svg", null);
 				/* Create SVG Generator */
 				SVGGraphics2D my_svg_generator = new SVGGraphics2D(document);
 				/* Render chart as SVG 2D Graphics object */
 				XYLineChart.draw(my_svg_generator, new Rectangle2D.Double(0, 0,
 						1200, 600), null);
 				/* Write output to file */
 				try {
 					String outputFileName =fileS+"_graph"+".svg";
 					my_svg_generator.stream(outputFileName);
 				} catch (SVGGraphics2DIOException e) {
 					System.out.println("fail to create svg file");
 				}
 			}
 		}
 	}
 }
