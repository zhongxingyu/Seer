 package edu.lognet.reputation.experiments;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import edu.lognet.reputation.controller.Reputation;
 import edu.lognet.reputation.model.Interaction;
 import edu.lognet.reputation.model.beans.experience.Experience;
 import edu.lognet.reputation.model.beans.service.Service;
 import edu.lognet.reputation.model.beans.user.IConsumer;
 import edu.lognet.reputation.model.beans.user.IProvider;
 import edu.lognet.reputation.model.beans.user.User;
 
 /**
  * @author lvanni
  */
 public class Experiment1 extends AbstractExperiment {
 
 	/* --------------------------------------------------------- */
 	/* Constructors */
 	/* --------------------------------------------------------- */
 	public Experiment1(int interactionNumber, int serviceNumber,
 			int totalUserNumber, int goodUser, int badUser, int dataLostPercent) {
 		super(interactionNumber, serviceNumber,
 				totalUserNumber, goodUser, badUser, dataLostPercent);
 	}
 
 	/* --------------------------------------------------------- */
 	/* Implements IExperiment */
 	/* --------------------------------------------------------- */
 	/**
 	 * Starting the Experiements
 	 */
 	public void start() {
 		// CREATE THE RANDOM FACTOR
 		Random randomGenerator = new Random();
 
 		// CREATE THE SERVICES
 		List<Service> services = getServiceSet();
 
 		// CREATE THE USERS
 		List<User> users = getUserSet(services);
 
 		// Launch the insteraction set
 		List<Interaction> interactions = new ArrayList<Interaction>();
 		for (int i = 0; i < getInteractionNumber(); i++) {
 			if(AbstractExperiment.LOG_ENABLED == 1) {
 				System.out.println("\nINFO: BEGIN INTRACTION " + (i+1) + "/" + getInteractionNumber());
 			}
 			// CHOOSE A RANDOM CONSUMER (AN USER WHO CONSUME A SERVICE)
 			IConsumer consumer = users.get(randomGenerator.nextInt(users.size()));
 
 			// CHOOSE A RANDOM SERVICE 
 			Service service = services.get(randomGenerator.nextInt(services.size()));
 
 			// GET THE PROVIDER LIST OF THIS SERVICE
 			List<IProvider> providers = service.getProviders();
 
 			// THE CONSUMER CHOOSE A PROVIDER
 			IProvider provider = consumer.chooseProvider(providers, service, getDataLostPercent());
 			
 			if(provider != null) {
 				if(AbstractExperiment.LOG_ENABLED == 1) {
 					System.out.println("INFO: " + ((User)consumer).getName() + " choose provider " + ((User)provider).getName());
 				}
 				
 				// THE CONSUMER GIVE A FEEDBACK
 				double feedback = 0.0;
				while((feedback = randomGenerator.nextDouble()) > provider.getQoS()){
					System.out.println(feedback);
				}
 				if(AbstractExperiment.LOG_ENABLED == 1) {
 					System.out.println("INFO: " + ((User)consumer).getName() + " give the feedback " + feedback);
 				}
 
 				// ADJUST THE CONSUMER EXPERIENCE
 				Experience oldExp = consumer.getConsumerExp(provider, service);
 				Experience newExp = new Experience(feedback);
 				Experience currentExp = Reputation.adjustExperience(oldExp, newExp);
 				consumer.setConsumerExp(provider, service, currentExp);
 
 				// ADJUST CREDIBILITY
 				Reputation.adjustCredibility(service.getRaters(provider), consumer.getCredibilityOfRater(service), currentExp);
 				
 				// UPDATE THE INTERACTION LIST
 				interactions.add(new Interaction(provider, consumer, service, feedback));
 			}
 
 			if(AbstractExperiment.LOG_ENABLED == 1) {
 				System.out.println("INFO: END INTRACTION " + (i+1) + "/" + getInteractionNumber());
 			}
 		}
 		
 		// PRINT ALL THE INTERACTION
 		if(AbstractExperiment.LOG_ENABLED == 1) {
 			System.out.println("INFO: Interaction List");
 			System.out.println("\tService\t\tProvider\tConsumer\tFeedback");
 			for(Interaction interaction : interactions){
 				System.out.println(interaction);
 			}
 		}
 	}
 
 }
