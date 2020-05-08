 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package geneticmusic;
 
 import geneticmusic.fitness.ChoraleFitnessFunction;
 import geneticmusic.genes.ChoraleGene;
 import geneticmusic.genes.NoteGenerator;
 import geneticmusic.jmusic.bridge.ConverterUtil;
 import javax.swing.JFrame;
 import jm.JMC;
 import jm.util.Write;
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.data.xy.DefaultXYDataset;
 import org.jfree.data.xy.XYBarDataset;
 import org.jfree.data.xy.XYDataset;
 import org.jfree.data.xy.XYSeries;
 import org.jfree.data.xy.XYSeriesCollection;
 import org.jgap.Chromosome;
 import org.jgap.Configuration;
 import org.jgap.FitnessFunction;
 import org.jgap.Gene;
 import org.jgap.Genotype;
 import org.jgap.IChromosome;
 import org.jgap.InvalidConfigurationException;
 import org.jgap.Population;
 import org.jgap.UnsupportedRepresentationException;
 import org.jgap.audit.EvolutionMonitor;
 import org.jgap.impl.BestChromosomesSelector;
 import org.jgap.impl.CrossoverOperator;
 import org.jgap.impl.DefaultConfiguration;
 import org.jgap.impl.GABreeder;
 import org.jgap.impl.MutationOperator;
 import org.jgap.impl.TournamentSelector;
 import org.jgap.impl.WeightedRouletteSelector;
 
 /**
  *
  * @author daviden
  */
 public class GeneticMusicChorale implements JMC {
 
     private static final int FIELD1 = 1;
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) throws InvalidConfigurationException, UnsupportedOperationException, UnsupportedRepresentationException {
 
 
 
 
 
         int populationSize = 40;
         int chromossomeSize = 8;
         int numGenerations = 500;
         String selector = "tournament";
         int tournamentk = 3;
         double tournamentp = 0.7;
 
 
 
 
         //PARSE
         if (args.length > 0) {
             populationSize = Integer.parseInt(args[0]);
             chromossomeSize = Integer.parseInt(args[1]);
             numGenerations = Integer.parseInt(args[2]);
             selector = args[3];
 
             if (args.length > 4) {
                 tournamentk = Integer.parseInt(args[4]);
                 tournamentp = Double.parseDouble(args[5]);
             }
         }
 
 
 
 
 
         System.out.println("GA configuration:");
         System.out.println("population size: " + populationSize);
         System.out.println("chromossome size: " + chromossomeSize);
        System.out.println("number of generations: " + numGenerations);
        System.out.println("selection operator: " + selector);
         
         if(selector.equals("tournament")){
              System.out.println("tournament size: "+tournamentk);
              System.out.println("tournament p: "+tournamentp);
         
         }
         
         
         //configuration object
         Configuration cfg = new DefaultConfiguration();
         ChoraleFitnessFunction fitnessF = new ChoraleFitnessFunction();
         cfg.setFitnessFunction(fitnessF);
 
 
 
 
 
 
 
         cfg.removeNaturalSelectors(true);
         // cfg.addNaturalSelector(new WeightedRouletteSelector(cfg), true); 
         //cfg.addNaturalSelector(new BestChromosomesSelector(cfg,0.7), true);
 
         if (selector.equals("tournament")) {
             cfg.addNaturalSelector(new TournamentSelector(cfg, tournamentk, tournamentp), true);
         } else {
             cfg.addNaturalSelector(new BestChromosomesSelector(cfg, 0.7), true);
         }
 
 
 
         cfg.setKeepPopulationSizeConstant(true);
         System.out.println("Selection Operator: " + cfg.getNaturalSelector(true, 0));
 
 
         System.out.println("Crossover: " + cfg.getGeneticOperators().get(0).toString());
 
         //set population size
         cfg.setPopulationSize(populationSize);
 
         //set note generator
         cfg.setRandomGenerator(new NoteGenerator());
 
 
 
 
         //**************create a sample cromossome************************
 
         Gene[] sampleGenes = new Gene[chromossomeSize];
         for (int i = 0; i < sampleGenes.length; i++) {
             sampleGenes[i] = new ChoraleGene(cfg);
         }
 
 
 
 
         Chromosome sampleChromosome = new Chromosome(cfg, sampleGenes);
 
         cfg.setSampleChromosome(sampleChromosome);
 
 
         //construct a population genotype
         Genotype population = Genotype.randomInitialGenotype(cfg);
 
 
 
 
 
         //// evolve and evaluate
         double currentFitness = 0.0;
 
         int i = 0;
         double lastFitness = 0.0;
 
         /***********************PRESENT DATA ON A CHART************************/
         XYSeries fitnessSeries = new XYSeries("Fittest Fitness");
 
 
         XYSeriesCollection dataset = new XYSeriesCollection(fitnessSeries);
         XYSeriesCollection rulesDataset = fitnessF.getRuleDataset();
 
 
         JFreeChart fitnessChart = ChartFactory.createXYLineChart("Average Fitness Evolution",
                 "Generation",
                 "Fitness", dataset,
                 PlotOrientation.VERTICAL,
                 true, //legend
                 true, //tooltips
                 false); //url
 
         JFreeChart rulesChart = ChartFactory.createXYLineChart("Rule Average Evaluation",
                 "Generation",
                 "Fitness", rulesDataset,
                 PlotOrientation.VERTICAL,
                 true, //legend
                 true, //tooltips
                 false); //url
 
 
         //add chart to panel
         ChartPanel chartPanel = new ChartPanel(fitnessChart);
         JFrame chartFrame = new JFrame("Average Fitness");
         chartFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         chartFrame.setContentPane(chartPanel);
         chartFrame.pack();
         chartFrame.setVisible(true);
 
 
         //add chart to panel
         ChartPanel rulesPanel = new ChartPanel(rulesChart);
         JFrame rulesFrame = new JFrame("Rule Fitness");
         rulesFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         rulesFrame.setContentPane(rulesPanel);
         rulesFrame.pack();
         rulesFrame.setVisible(true);
 
         do {
             lastFitness = currentFitness;
             population.evolve();
             currentFitness = population.getFittestChromosome().getFitnessValue();
 
             fitnessSeries.add(i, getAVGFitness(population));//update series
             //System.out.println("Current fitness: "+currentFitness);
             i++;
 
             //System.out.println(i);
         } while (i < numGenerations);
 
 
 
 
 
         IChromosome chm = population.getFittestChromosome();
 
 
         System.out.println(chm.toString());
         Write.midi(ConverterUtil.getChoraleScore(chm), "generatedChorale.mid");
 
 
 
     }
 
     private static double getAVGFitness(Genotype p) {
 
         double sum = 0.0;
         for (IChromosome c : p.getChromosomes()) {
             sum += c.getFitnessValue();
         }
         return (sum / p.getPopulation().size());
 
     }
 }
