 package util.genetic;
 
 import java.io.File;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.PriorityQueue;
 import java.util.Queue;
 
 import util.genetic.mutatorV1.MutatorV1;
 import eval.expEvalV3.DefaultEvalWeights;
 import eval.expEvalV3.EvalParameters;
 
 public final class GeneticTrainer {
 	private final static Comparator<GEntity> sortBestFirst = new Comparator<GEntity>() {
 		public int compare(GEntity e1, GEntity e2) {
 			if(e1 == null){
 				return 1;
 			} else if(e2 == null){
 				return -1;
 			}
 			
 			if(e1.score() > e2.score()){
 				return -1;
 			} else if(e1.score() < e2.score()){
 				return 1;
 			} else{
 				return e1.totalGames() > e2.totalGames()? -1: 1;
 			}
 		}
 	};
 	
 	private final static ByteBuffer b = ByteBuffer.allocate(1<<15);
 	
 	public static void main(String[] args) throws Exception{
 
 		final int tests = 1; //games to play per simulation step
 		final int threads = 4;
 		final long time = 30;
 		final int hashSize = 18;
 		final int popSize = 30;
 		final int cullSize = max((int)(popSize*.05+.5), 1); //number of entries to cull
 		final int minGames = 5; //min games before entry can be culled
 		final Mutator m = new MutatorV1();
 		final int mutations = 6;
 		final double gameCutoffPercent = .2; //only play games against the top X percent of solutions
 		final double reproduceCutoffPercent = .4; //only clone and mutate entites in top X percent of solutions
 		
		final File file = new File("genetic-results/genetic-results-v14");
 		if(file.exists()){
 			System.out.println("log file already exists, exiting");
 			System.exit(0);
 		}
 		final GeneticLogger log = new GeneticLogger(file);
 		
 		final GEntity[] population = new GEntity[popSize];
 		final GameQueue q = new GameQueue(threads, time, hashSize);
 		
 		for(int a = 0; a < population.length; a++){
 			population[a] = new GEntity();
 			population[a].p = DefaultEvalWeights.defaultEval();
 			if(a != 0) m.mutate(population[a].p, mutations);
 			log.recordGEntity(population[a]);
 		}
 		
 		for(int i = 0; ; i++){
 			simulate(population, q, tests, gameCutoffPercent);
 			log.recordIteration(i, population);
 			List<Integer> culled = cull(population, cullSize, minGames);
 			generate(culled, population, m, mutations, reproduceCutoffPercent);
 			recordNewEntities(culled, population, log);
 			
 			System.out.println("completed iteration "+i);
 			
 			//record data
 			GEntity e;
 			final Queue<GEntity> tempq = new PriorityQueue<GEntity>(population.length, sortBestFirst);
 			for(int a = 0; a < population.length; a++) tempq.add(population[a]);
 			while(tempq.size() > 0){
 				if((e = tempq.poll()).totalGames() >= minGames){
 					System.out.println("best id="+e.id);
 					System.out.println(e.p);
 					break;
 				}
 			}
 		}
 	}
 	
 	private static void recordNewEntities(final List<Integer> newEntities, final GEntity[] p,
 			final GeneticLogger log) throws IOException{
 		for(int index: newEntities){
 			final GEntity temp = p[index];
 			log.recordGEntity(temp);
 		}
 	}
 	
 	private static int max(final int i1, final int i2){
 		return i1 > i2? i1: i2;
 	}
 	
 	/** runs the simulation, accumulating a score for each entity*/
 	public static void simulate(final GEntity[] population, final GameQueue q, final int tests, final double gameCutoffPercent){
 		final List<GEntity> sorted = new ArrayList<GEntity>();
 		for(int a = 0; a < population.length; a++) sorted.add(population[a]);
 		Collections.sort(sorted, sortBestFirst);
 		
 		for(int a = 0; a < population.length; a++){
 			
 			for(int w = 0; w < tests; w++){
 				int index;
 				while(sorted.get(index = (int)(Math.random()*population.length*gameCutoffPercent)) == population[a]);
 				final GameQueue.Game g = new GameQueue.Game(population[a], sorted.get(index));
 				q.submit(g);
 			}
 		}
 		
 		//wait for queued games to finish
 		final int total = tests*population.length;
 		int prevCompleted = -1;
 		int mod = max((int)(total*.1), 1);
 		while(q.getOutstandingJobs() > 0){
 			try{
 				Thread.sleep(500);
 			} catch(InterruptedException e){}
 			if(total-q.getOutstandingJobs() != prevCompleted){
 				prevCompleted = total-q.getOutstandingJobs();
 				if((prevCompleted % mod) == 0) System.out.println("completed "+prevCompleted+" / "+total);
 			}
 		}
 	}
 	
 	/** culls bad solutions from population, returns list of culled indeces*/
 	public static List<Integer> cull(final GEntity[] population, int cullSize, final int minGames){
 		final Comparator<GEntity> c = new Comparator<GEntity>() {
 			public int compare(GEntity e1, GEntity e2) {
 				if(e1.score() < e2.score()){
 					return -1;
 				} else if(e1.score() > e2.score()){
 					return 1;
 				} else{
 					return e1.totalGames() < e2.totalGames()? -1: 1;
 				}
 			}
 		};
 		final Queue<GEntity> q = new PriorityQueue<GEntity>(population.length, c);
 		for(int a = 0; a < population.length; a++) q.add(population[a]);
 		
 		final boolean print = true;
 		
 		List<Integer> l = new ArrayList<Integer>();
 		while(q.size() > 0 && l.size() < cullSize){
 			//count as cull even if lowest scoring not eligible for cull
 			//(prevents good solutions from being culled if no eligible bad solutions)
 			cullSize--;
 			GEntity e = q.poll();
 			if(print) System.out.print(e);
 			if(e.totalGames() >= minGames){
 				if(print) System.out.println(" -- culled");
 				for(int w = 0; w < population.length; w++){
 					if(population[w] == e){
 						population[w] = null;
 						l.add(w);
 					}
 				}
 			} else{
 				if(print) System.out.println();
 			}
 		}
 		
 		while(q.size() > 0){
 			if(print) System.out.println(q.poll());
 		}
 		
 		return l;
 	}
 	
 	/** generates new solutions from population , replacing culled entries*/
 	public static void generate(final List<Integer> culled, final GEntity[] population, final Mutator m,
 			final int mutations, final double reproduceCutoffPercent){
 		final List<GEntity> sorted = new ArrayList<GEntity>();
 		for(int a = 0; a < population.length; a++) sorted.add(population[a]);
 		Collections.sort(sorted, sortBestFirst);
 		
 		for(int index: culled){
 			int r; //index of entity to clone and mutate
 			while(sorted.get(r = (int)(Math.random()*population.length*reproduceCutoffPercent)) == null);
 			
 			b.clear();
 			sorted.get(r).p.write(b);
 			b.rewind();
 			final EvalParameters p = new EvalParameters();
 			p.read(b);
 			m.mutate(p, mutations); 
 			GEntity temp = new GEntity();
 			temp.p = p;
 			
 			population[index] = temp;
 		}
 	}
 }
