 package pikater;
 
 import java.util.Random;
 
 import pikater.ontology.messages.Evaluation;
 import pikater.ontology.messages.Option;
 import pikater.ontology.messages.Options;
 import jade.util.leap.ArrayList;
 import jade.util.leap.Iterator;
 import jade.util.leap.List;
 
 public class Agent_SimulatedAnnealing extends Agent_MutationSearch {
 	/*
 	 * Implementation of Simulated Annealing option search
 	 * Options:
 	 * -E float
 	 * minimum error rate (default 0.1)
 	 * 
 	 * -M int 
 	 * maximal number of generations (default 10)
 	 * 
 	 * -T float
 	 * Initial temperature (default 1.0)
 	 * 
 	 * -S float
 	 * Stability of generation of new option - probability of keeping of option (default 0.5)
 	 */
 	private static final long serialVersionUID = -5087231723984887596L;
 	private Random rnd_gen = null;
 	private Options solution = null;
 	private Options new_solution = null;
 	private float evaluation = Float.MAX_VALUE;
 	private double temperature = 0.0;
 	private double stability = 0.5;
 	private int number_of_tries = 0;
 	private int maximum_tries = 50;
 	private double best_error_rate = 1;
 	private double final_error_rate = 0.1;
 	private boolean minimization = true;
 	@Override
 	protected String getAgentType() {
 		return "SimulatedAnnealing";
 	}
 
 	@Override
 	protected void loadSearchOptions() {
 		temperature = 1.0;//?
 		maximum_tries = 50;
 		stability = 0.5;
 		final_error_rate = 0.1;
 		List search_options = getSearch_options();
 		Iterator itr = search_options.iterator();
 		while (itr.hasNext()) {
 			Option next = (Option) itr.next();
 			if (next.getName().equals("E")){
 				final_error_rate = Float.parseFloat(next.getValue()); 
 			}
 			if (next.getName().equals("M")){
 				maximum_tries = Integer.parseInt(next.getValue()); 
 			}
 			if (next.getName().equals("S")){
 				stability = Float.parseFloat(next.getValue()); 
 			}
 			if (next.getName().equals("T")){
 				temperature = Float.parseFloat(next.getValue()); 
 			}
 		}
 	}
 	
 	@Override
 	protected boolean finished() {
 		//n>=nmax
 		if (number_of_tries >= maximum_tries) {
 			return true;
 		}
 		if (best_error_rate < final_error_rate){
 			return true;
 		}
		return true;
 	}
 	
 	@Override
 	protected List generateNewOptions(List options, List evaluations) {
 		if(rnd_gen == null)
 			rnd_gen = new Random();
 		
 		if(evaluations == null){
 			//inicializace
 			solution = null;
 			new_solution = null;
 			evaluation = Float.MAX_VALUE;
 			number_of_tries = 0;
 			best_error_rate = Double.MAX_VALUE;
 		}
 		
 		//create a new solution for evaluation
 		new_solution = Neighbor(solution);
 		
 		number_of_tries++;
 		
 		//List of options to send
 		List options_list = new ArrayList();
 		options_list.add(new_solution);
 		return options_list;
 	}
 	
 	@Override
 	protected void updateFinished(List evaluations) {
 		float new_evaluation;
 		
 		if (evaluations == null){
 			new_evaluation = Float.MAX_VALUE;
 		}
 		else{
 			new_evaluation = ((Evaluation)(evaluations.get(0))).getError_rate();
 		}
 		
 		//Actualize best evaluation
 		if(new_evaluation < best_error_rate){
 			best_error_rate = new_evaluation;
 		}
 		//Acceptance of new options
		if (rnd_gen.nextDouble()<acceptanceProb(new_evaluation-evaluation,temperature)){
 			solution = new_solution;
 			evaluation = new_evaluation;
 		}
 		//Decrease temperature
 		Cooling();
 	}
 	
 	//Neighbor function: Random options in case of beginning, or mutation of existing
 	private Options Neighbor(Options sol){
 		List new_options = new ArrayList();
 		if(solution == null){
 			//Completely new solution
 			Iterator itr = getOptions().iterator();
 			while (itr.hasNext()) {
 				//dont want to change old options
 				Option opt = ((Option) itr.next()).copyOption();
 				opt.setValue(randomOptValue(opt));
 				new_options.add(opt);
 			}
 		}else{
 			//Neighbor function
 			Iterator itr = sol.getList().iterator();
 			while (itr.hasNext()) {
 				Option opt = ((Option) itr.next()).copyOption();
 				opt.setValue(mutateOptValue(opt, 1-stability));
 				new_options.add(opt);
 			}
 		}
 		return new Options(new_options);
 	}
 	
 	/*Acceptance probability of annealed options: 
 	  -the better values are accepted
 	  -the worse with probability exp((e-e_new)/temperature)
 	*/
 	private double acceptanceProb(double delta, double temperature){
 		if(!minimization){/*for max problems*/
 			delta = -delta;
 		}
 		if(delta<0){//it is better
 			return 1.0;
 		}else{
 			return Math.exp(-delta/temperature);
 		}
 	}
 	
 	//Cooling scheme: 20% decrease in each step	
 	private void Cooling(){
 		temperature = 0.8*temperature;
 	}
 
 
 }
