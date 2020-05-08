 package controllers;
 
 import helpers.BaumgartnerSampleTable;
 import helpers.Timer;
 
 import java.util.ArrayList;
 
 import models.Renderer;
 import play.mvc.Controller;
 import algorithms.cna.CNAlgorithm;
 import algorithms.cna.NecException;
 import datastructures.cna.CNAList;
 import datastructures.cna.CNATable;
 import datastructures.graph.Graph;
 import datastructures.mt.MinimalTheory;
 import datastructures.mt.MinimalTheorySet;
 import datastructures.parser.MTSetToTable;
 import datastructures.random.RandomMTGeneratorHelper;
 import datastructures.random.RandomMTSetGenerator;
 
 public class CNAController extends Controller {
 
     private static Timer timer;
     private static boolean showBundleNumRenderer;
     private static MinimalTheorySet theories;
     private static boolean makeEpi;
 
     public static void setup() {
 	render();
     }
 
     public static void prepare(String layers) {
 	int i = Integer.parseInt(layers);
 	render(i);
     }
 
     public static void generateGraph(ArrayList<Integer> bundles1,
 	    ArrayList<Integer> bundles2, ArrayList<Integer> bundles3,
 	    ArrayList<Integer> alterFactors, String epi, String showBundleNum) {
 	try {
 	    showBundleNumRenderer = (showBundleNum != null);
 	    makeEpi = (epi != null);
 	    RandomMTSetGenerator generator;
 	    ArrayList<ArrayList<Integer>> list;
 	    RandomMTGeneratorHelper input;
 
 	    list = new ArrayList<ArrayList<Integer>>();
 	    list.add(bundles1);
 	    list.add(bundles2);
 	    list.add(bundles3);
 
 	    input = new RandomMTGeneratorHelper(list, alterFactors);
 	    generator = new RandomMTSetGenerator(input.getCompleteList(),
 		    makeEpi);
 	    theories = generator.getMTSet();
 
 	    Graph graph = new Graph(theories);
 	    Renderer renderer = new Renderer();
 	    renderer.setShowEdgeLabels(showBundleNumRenderer);
 	    renderer.config(graph);
 
 	    String generatedGraphSource = renderer.getImageSource();
 	    String generatedGraphString = theories.toString();
 
	    boolean calc = (theories.getAllNames().size() < 7);
 	    System.out.println(calc);
 	    render(calc, generatedGraphSource, generatedGraphString);
 
 	} catch (OutOfMemoryError e) {
 	    flash.error("Phuu! This calculation was to complex! "
 		    + "Server is out of Memory! "
 		    + "Please wait one minute and try again.");
 	    params.flash();
 	    setup();
 	    e.printStackTrace();
 	} catch (IllegalArgumentException e) {
 	    flash.error("You calculated a null graph ;)");
 	    params.flash();
 	    setup();
 	    e.printStackTrace();
 	} catch (IndexOutOfBoundsException e) {
 	    flash.error("Too much data!");
 	    params.flash();
 	    setup();
 	    e.printStackTrace();
 	}
     }
 
     public static void calcCNAGraph(String generatedGraphSource,
 	    String generatedGraphString) {
 	try {
 	    timer = new Timer();
 	    MTSetToTable parser = new MTSetToTable(theories, makeEpi);
 	    CNATable table = parser.getCoincTable();
 	    CNAlgorithm cnaAlgorithm = new CNAlgorithm(table);
 
 	    Graph graph = new Graph(cnaAlgorithm.getMinimalTheorySet());
 	    Renderer renderer = new Renderer();
 	    renderer.setShowEdgeLabels(showBundleNumRenderer);
 	    renderer.config(graph);
 
 	    Long time = timer.timeElapsed();
 
 	    String calcGraphString = cnaAlgorithm.getMinimalTheorySet()
 		    .toString();
 	    String calcGraphSource = renderer.getImageSource();
 	    String elapsedTime = time.toString() + " ms";
 	    String effects = cnaAlgorithm.getEffects().toString();
 	    String sufTable = cnaAlgorithm.getSufTable().toString();
 	    String msufTable = cnaAlgorithm.getMsufTable().toString();
 	    String necList = cnaAlgorithm.getNecList().toString();
 	    String mnecTable = cnaAlgorithm.getMnecTable().toString();
 	    String coincTable = table.toString();
 
 	    render(elapsedTime, calcGraphString, calcGraphSource,
 		    generatedGraphSource, generatedGraphString, effects,
 		    sufTable, msufTable, necList, mnecTable, coincTable);
 	} catch (NecException e) {
 	    flash.error("NEC error.");
 	    params.flash();
 	    setup();
 	}
     }
 
     public static void baumgartnerSample() {
 	timer = new Timer();
 	CNAlgorithm cnaAlgorithm;
 	try {
 	    cnaAlgorithm = new CNAlgorithm(
 		    new BaumgartnerSampleTable().getSampleTable());
 	    MinimalTheorySet theories = cnaAlgorithm.getMinimalTheorySet();
 	    ArrayList<String> stringGraphs = new ArrayList<String>();
 	    String graphPath;
 
 	    Graph graph = new Graph(theories);
 	    Renderer renderer = new Renderer();
 	    renderer.setShowEdgeLabels(showBundleNumRenderer);
 	    renderer.config(graph);
 	    graphPath = renderer.getImageSource();
 
 	    Long time = timer.timeElapsed();
 	    String elapsedTime = time.toString() + " ms";
 	    String effects = cnaAlgorithm.getEffects().toString();
 	    String sufTable = cnaAlgorithm.getSufTable().toString();
 	    String msufTable = cnaAlgorithm.getMsufTable().toString();
 	    String necList = cnaAlgorithm.getNecList().toString();
 	    String mnecTable = cnaAlgorithm.getMnecTable().toString();
 	    render(elapsedTime, graphPath, stringGraphs, effects, sufTable,
 		    msufTable, necList, mnecTable);
 	} catch (NecException e) {
 	    flash.error("NEC error.");
 	    params.flash();
 	    setup();
 	    e.printStackTrace();
 	}
     }
 
     public static void inputTable(String table) {
 
 	CNATable cnatable = new CNATable("\r\n", ",", table);
	if (cnatable.get(0).size() > 7) {
 	    flash.error("Sorry! Not more than 7 factors are allowed.");
 	    params.flash();
 	    setup();
 	}
 	try {
 	    timer = new Timer();
 	    CNAlgorithm cnaAlgorithm = new CNAlgorithm(cnatable);
 	    Graph graph = new Graph(cnaAlgorithm.getMinimalTheorySet());
 	    Renderer renderer = new Renderer();
 	    renderer.setShowEdgeLabels(showBundleNumRenderer);
 	    renderer.config(graph);
 
 	    Long time = timer.timeElapsed();
 
 	    String calcGraphString = cnaAlgorithm.getMinimalTheorySet()
 		    .toString();
 	    String calcGraphSource = renderer.getImageSource();
 	    String elapsedTime = time.toString() + " ms";
 	    String effects = cnaAlgorithm.getEffects().toString();
 	    String sufTable = cnaAlgorithm.getSufTable().toString();
 	    String msufTable = cnaAlgorithm.getMsufTable().toString();
 	    String necList = cnaAlgorithm.getNecList().toString();
 	    String mnecTable = cnaAlgorithm.getMnecTable().toString();
 
 	    render(elapsedTime, calcGraphString, calcGraphSource, effects,
 		    sufTable, msufTable, necList, mnecTable);
 	} catch (NecException e) {
 	    flash.error("NEC error.");
 	    params.flash();
 	    setup();
 	} catch (ArrayIndexOutOfBoundsException e) {
 	    flash.error("Please give us more data!");
 	    params.flash();
 	    setup();
 	} catch (IndexOutOfBoundsException e) {
 	    flash.error("Please give us more data!");
 	    params.flash();
 	    setup();
 	} catch (IllegalArgumentException e) {
 	    flash.error("Sorry, something went very wrong!");
 	    params.flash();
 	    setup();
 	}
     }
 
     public static void inputMT(String mtset) {
 	try {
 	    CNAList list = new CNAList("\r\n", mtset);
 	    CNAList factors;
 	    theories = new MinimalTheorySet();
 	    MinimalTheory theorie;
 	    for (String str : list) {
 		factors = new CNAList();
 		String[] array = str.split("=>");
 		String[] fac = array[0].split("v");
 		for (int i = 0; i < fac.length; i++) {
 		    factors.add(fac[i]);
 		}
 		theorie = new MinimalTheory(factors, array[1]);
 		theories.add(theorie);
 	    }
 	    Graph graph = new Graph(theories);
 	    Renderer renderer = new Renderer();
 	    renderer.setShowEdgeLabels(showBundleNumRenderer);
 	    renderer.config(graph);
 
 	    String generatedGraphSource = renderer.getImageSource();
 	    String generatedGraphString = theories.toString();
	    boolean calc = (theories.getAllNames().size() < 7);
 
 	    render(generatedGraphSource, generatedGraphString, calc);
 	} catch (ArrayIndexOutOfBoundsException e) {
 	    flash.error("Wrong MT Syntax!");
 	    params.flash();
 	    setup();
 	} catch (IndexOutOfBoundsException e) {
 	    flash.error("Please give us more data!");
 	    params.flash();
 	    setup();
 	} catch (IllegalArgumentException e) {
 	    flash.error("Sorry, something went very wrong!");
 	    params.flash();
 	    setup();
 
 	}
     }
 }
