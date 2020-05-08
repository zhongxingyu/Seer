 package Zegana;
 import java.util.*;
 import java.io.*;
 import java.math.*;
 import magicGame.*;
 
 public class Zegana{
     public static int genSize = 100;
     public static int trials = 100;
     public static char sa = 't';
     public static char xa = '0';
     public static int end = -1;
     public static int deckCount;
     public static boolean fish = false;
     public static boolean verbose = false;
     private static ArrayList<String> LegalCards = new ArrayList<String>();
 
     public static void main(String [] args){
 		
 	if(args.length == 0){
 	    printUsage();
 	    return;
 	}
 	Zegana.parseArguements(args);
 
 	int genCount = 0;
 
 	if(Zegana.verbose)
 	    System.out.println("Reading card list");
 	readCards("Zegana/standard.txt");
 
 	Deck[] currentGen = new Deck[genSize];
 
 	if(Zegana.verbose)
 	    System.out.println("Generating new random decks");
 	
 	for(int i=0; i<genSize; i++){
 	    currentGen[i] = new Deck(LegalCards);
 	    Zegana.useLands(currentGen[i], 0.3);
 	}
 	
 	while(genCount != end){
 	    float[] performance = Zegana.sim(currentGen);
 	    genCount++;
 
 	    if(Zegana.verbose)
 		System.out.println("Done simulating generation "+genCount);
 	    Deck best = currentGen[0];
 	    float bestP = performance[0];
 	    for(int i=1; i<performance.length; i++){
 		if(performance[i]>bestP){
 		    bestP = performance[i];
 		    best = currentGen[i];
 		}
 	    }
 	    System.out.println("New deck:");
 	    System.out.println(best);
 	    System.out.println("Performance: \n"+bestP);
 
 	    currentGen = Zegana.select(currentGen, performance);
 
 	    for(int i=0; i<currentGen.length; i++){
		if(Math.random()>0.2)
 		    Zegana.twiddle(currentGen[i]);
 	    }
 	}
 
 
 	/*
 	Deck a = new Deck(LegalCards);
 	Zegana.useLands(a, 0.31);
 	Deck b = new Deck(LegalCards);
 	Zegana.useLands(b, 0.31);
 	for(int i = 0; i< trials; i++)	
 	    Connect.simulate(a, b);
 	    */
 
     }
     
     /*
     public static void crossover(Deck a, Deck b){
 	int start = 0;
 	int end = Math.max(a.cards.size(), b.cards.size());
 	if(math.Random()>0.5)
 	    start = (int) (Math.random()*end);
 	if(math.Random()>0.5)
 	    end = end - ((int) Math.random()*(end-start));
     */
     
     public static void twiddle(Deck in){
 	for(int i=0; i< in.cards.size(); i++){
 	    while(Math.random()>0.7){
 		int mod = ((int) (Math.random()*3)-1);
 		in.quantity.set(i, in.quantity.get(i)+mod);
 	    }
 	    if(in.quantity.get(i) < 0)
 		in.quantity.set(i, 0);
 	    if(in.quantity.get(i) > 4){
 		if(! CardsInfo.has(in.cards.get(i), "Basic")){
 		    in.quantity.set(i, 4);
 		}
 	    }
 	    if(in.quantity.get(i) == 0){
 		in.cards.remove(i);
 		in.quantity.remove(i);
 	    }
 	}
	while(Math.random()>0.7){
 	    int tries = 10;
 	    while(tries > 0){
 		String newCard = LegalCards.get((int) (Math.random()*LegalCards.size()));
 		if(in.cards.contains(newCard)){
 		    tries--;
 		}else{
 		    in.cards.add(newCard);
 		    in.quantity.add(2);
		    break;
 		}
 	    }
 	}
     }
 
     public static Deck[] select(Deck[] old, float[] perf){
 	float total = 0;
 	Deck[] result = new Deck[old.length];
 	for(int i=0; i<perf.length; i++){
 	    total += perf[i];
 	}
 	for(int i=0; i<old.length; i++){
 	    float target = ((float) Math.random())*total;
 	    float running = 0;
 	    for(int j=0; j<perf.length; j++){
 		running += perf[j];
 		if(running > target){
 		    result[i] = new Deck(old[j]);
 		    break;
 		}
 	    }
 	}
 	return result;
     }
 
 
     //Generates floats representing the performance of each deck.
     //Guarentees only that higher values are better.
     public static float[] sim(Deck[] gen){
 	float[] balance = new float[gen.length];
 	if(fish){
 	    return null;
 	}else{
 	    for(int i = 0; i<gen.length; i++){
 		for(int j = 0; j<trials; j++){
 		    int target = (int) (Math.random() * gen.length);
 		    int result = Connect.simulate(gen[i], gen[target]);
 		    if(result == 0)
 			result = 1;
 		    else
 			result = 0;
 		    balance[i] += result;
 		    //balance[target] += result;
 		}
 	    }
 	    if(Zegana.verbose){
 		System.out.println("Performance array:");
 		for(int i=0; i<balance.length; i++){
 		    System.out.println(balance[i]);
 		}
 	    }
 	    return balance;
 	}
     }
 
     public static void readCards(String fn){
 	if(verbose)
 	    System.out.println("Reading card list from file "+fn);
 	try{
 	    Scanner in = new Scanner(new File(fn));
 	    while(in.hasNext()){
 		LegalCards.add(in.nextLine());
 	    }
 	}catch(Exception e){
 	    System.out.println(e);
 	}
     }
 
     public static void printUsage(){
 	System.out.println("usage: java Zegana.Zegana [command] [-arguements] [input file] [output file]");
 	System.out.println("Valid commands:");
 	System.out.println("  new      (make new decks)");
 	System.out.println("  evolve   (make new decks from a given generation file) (todo)");
 	System.out.println("Valid arguements:");
 	System.out.println("  -s       (generation size)");
 	System.out.println("  -t       (repetitions of each simulation)");
 	System.out.println("  -v       (verbose)");
 	System.out.println("  -f       (one sided games) (todo)");
 	System.out.println("  -e       (number of generations");
 	System.out.println("  -sa (n)  (selection algorithm) (todo)");
 	System.out.println("           Valid algorithms: r (roulette wheel) (todo), t (tournament) (todo)");
 	System.out.println("  -xa (n)  (mutation algorithm) (todo)");
 	System.out.println("           Valid algorithms: err...");
     }
 
     public static void parseArguements(String[] args){
 	for(int position = 1; position < args.length; position++){
 	    if(args[position].equals("-s")){
 		Zegana.genSize = Integer.parseInt(args[position+1]);
 		position++;
 	    }
 	    
 	    else if(args[position].equals("-t")){
 		Zegana.trials = Integer.parseInt(args[position+1]);
 		position++;
 	    }
 
 
 	    else if(args[position].equals("-v")){
 		Zegana.verbose = true;
 	    }
 	    
 	    else if(args[position].equals("-f")){
 		Zegana.fish = true;
 	    }
 
 	    else if(args[position].equals("-sa")){
 		Zegana.sa = args[position+1].charAt(0);
 		position++;
 	    }
 
 	    else if(args[position].equals("-xa")){
 		Zegana.xa = args[position+1].charAt(0);
 		position++;
 	    }
 
 	    else if(args[position].equals("-e")){
 		Zegana.end = Integer.parseInt(args[position+1]);
 		position++;
 	    }
 	}
     }
     
     //Modifies the given deck such that the portion of basic
     //lands is equal to i
     public static void useLands(Deck d, double r){
 	int cards = 0;
 	int lands = 0;
 	int uniqueBasics = 0;
 	for(int i=0; i<d.cards.size(); i++){
 	    cards += d.quantity.get(i);
 	    String s = d.cards.get(i);
 	    if(CardsInfo.has(s, "Basic")){
 		uniqueBasics++;
 		lands += d.quantity.get(i);
 	    }
 	}
 	if(uniqueBasics != 0){
 	    int add = (int) (((cards*r) - lands)/uniqueBasics);
 	    for(int i = 0; i<d.cards.size(); i++){
 		if(CardsInfo.has(d.cards.get(i), "Basic"))
 		    d.quantity.set(i, d.quantity.get(i)+add);
 	    }
 	}else{
 	    int add = (int) ((cards*r) - lands);
 	    d.cards.add("Forest");
 	    d.quantity.add(add);
 	}
     }
 }
 
 class Deck{
     public ArrayList<String> cards;
     public ArrayList<Integer> quantity;
     public String name;
 
     public Deck(ArrayList<String> LegalCards){
 	if(Zegana.verbose)
 	    System.out.println("Generating new deck");
 	cards = new ArrayList<String>();
 	quantity = new ArrayList<Integer>();
 	for(int i=0; i<LegalCards.size(); i++){
 	    int r = (int) (Math.random()*5);
 	    if(r>0){
 		cards.add(LegalCards.get(i));
 		quantity.add(r);
 	    }
 	}
 	name = ""+Zegana.deckCount;
 	if(Zegana.verbose)
 	    System.out.println("Generated deck "+Zegana.deckCount);
 	Zegana.deckCount++;
     }
 
     //copy constructor
     public Deck(Deck in){
 	name = in.name;
 	cards = new ArrayList<String>(in.cards);
 	quantity = new ArrayList<Integer>(in.quantity);
     }
 
     @Override
     public String toString(){
 	String o = "Deck "+name+'\n';
 	int count = 0;
 	for(int i=0; i<cards.size(); i++){
 	    o = o + quantity.get(i) + ' ' + cards.get(i) + '\n';
 	    count += quantity.get(i);
 	}
 	o = o +"Cards: "+count+'\n';
 	return o;
     }
 }
