 package monitoring;
 
 import elements.FormantSequence;
 import exceptions.FormantNumberexception;
import geneticAlogrithm.GeneticAlgorithmCall;
 import javax.swing.JFrame;
 import org.math.plot.Plot2DPanel;
 import au.com.bytecode.opencsv.CSVReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.List;
 
 public class Curve{
 
 	/**
 	 * function which read the csv file and draw the curve.
 	 * Use the read functions of the opencsv api and the mathPlot 2.0 api to draw the curve
 	 * 
 	 * @param ga
 	 * 		the ga used. We need it to get the target and do the comparison
 	 * @throws FormantNumberexception 
 	 */
 	public Curve(FormantSequence target) throws FormantNumberexception{
 		CSVReader reader;
 		String[] row = null;
 		double[] absis=null; // the same for the two
 		double[] ordoneeF1=null;
 		double[] ordoneeF2=null;
 		double[] ordoneeFitness=null;
 		int compteur=0;
 		double[] F1Value;
 		double[] F2Value;
 		try {
 			/*
 			 * 1st we read the csv file to extract the information and get tzo array representing the absis and the ordonees
 			 * For that purpose we use the opencsv api 
 			 */
 			reader = new CSVReader(new FileReader(System.getProperty("user.dir") + "/results/algoritmProgression.csv"),',',' ' , 1); //cf opencsv api
 			List content = reader.readAll();
 			//init of the lists
 			absis= new double[content.size()];			
 			ordoneeF1 =new double[content.size()];	
 			ordoneeF2 =new double[content.size()];	
 			F1Value=new double[content.size()];	
 			F2Value=new double[content.size()];
 			ordoneeFitness = new double[content.size()];
 			//decomposition in two array, one for the values of the absis and one for the values of the ordonee
 			for (Object object : content) {
 			    row = (String[]) object;
 			    absis[compteur]=Double.parseDouble(row[0].trim());
 			    ordoneeFitness[compteur]=Double.parseDouble(row[1].trim());
 			    ordoneeF1[compteur]=target.getFormantAt(0).getFrequency()-Double.parseDouble(row[2].trim());
 			    ordoneeF2[compteur]=target.getFormantAt(1).getFrequency()-Double.parseDouble(row[3].trim());
 			    compteur++;
 			}
 			reader.close();
 			for(int i=0;i<content.size();i++){
				System.out.println(ordoneeFitness[i]);
				System.out.println(ordoneeF1[i]);
				System.out.println(ordoneeF1[i]);
				System.out.println();
			}
			for(int i=0;i<content.size();i++){
 				//define the const to get a line
 				F1Value[i]=0.1*target.getFormantAt(0).getFrequency();
 				F2Value[i]=0.1*target.getFormantAt(1).getFrequency();
 			}
 			
 			
 			
 			/*
 			 *then the drawing part 
 			 */
 			
 			// create your PlotPanel (you can use it as a JPanel)
 			Plot2DPanel plotDifferenceToTarget = new Plot2DPanel();
 			Plot2DPanel plotFitness = new Plot2DPanel();
 			
 			// define the legend position
 			plotDifferenceToTarget.addLegend("SOUTH");
 			plotFitness.addLegend("SOUTH");
 			
 			// add a line plot to the PlotPanel
 			plotDifferenceToTarget.addLinePlot("F1 of candidate", absis, ordoneeF1);
 			plotDifferenceToTarget.addLinePlot("F2 of candidate",absis,ordoneeF2);
 			plotDifferenceToTarget.addLinePlot("F1 margin",absis,F1Value);
 			plotDifferenceToTarget.addLinePlot("F2 margin",absis,F2Value);
 			plotFitness.addLinePlot("Fitness Curve", absis,ordoneeFitness);
 			
 			// put the PlotPanel in a JFrame like a JPanel
 			JFrame frameDifferenceToTarget = new JFrame("a plot of the difference to the target");
 			frameDifferenceToTarget.setSize(600, 600);
 			frameDifferenceToTarget.setContentPane(plotDifferenceToTarget);
 			frameDifferenceToTarget.setVisible(true);
 			
 			JFrame frameFitness = new JFrame("a plot to show the fitness evolution");
 			frameFitness.setSize(600, 600);
 			frameFitness.setContentPane(plotFitness);
 			frameFitness.setVisible(true);
 			
 			 
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NumberFormatException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 }
