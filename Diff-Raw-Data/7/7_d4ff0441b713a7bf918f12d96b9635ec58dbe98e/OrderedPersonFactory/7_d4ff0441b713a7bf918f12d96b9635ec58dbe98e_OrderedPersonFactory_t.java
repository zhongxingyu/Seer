 package nardiff.ordered;
 
 import java.util.Deque;
 import java.util.LinkedList;
 
 import nardiff.Person;
 
 import org.apache.commons.collections15.Factory;
 
 /**
  * This factory creates OrderedPerson objects having a initial set of numbers
  * representing individual pieces of information. The set is comprised of a
  * fixed (parameterizable) number of bytes the value of which is drawn from a
  * larger set of all pieces of information in the simulation. The exact subset
  * given to each agent is random. In order to ensure that all numbers exist
  * within a given population, the byte values are drawn from a list of all
  * possible byte values within the simulation, the order of which is randomized
  * every pass through.
  * 
  * @author kkoning
  * 
  */
 public class OrderedPersonFactory implements Factory<Person> {
 
 	byte[] allInfoPieces;
 	byte pos = 0;
 	int leaderPos = 0;
 	int followerPos = 0;
 	int personID = 0;
 
 	boolean[] leaderValues;
 	boolean[] leaderBackwardValues;
 	boolean[] followerBackwardValues;
 	
 	/**
 	 * A link back to the model that created us.
 	 */
 	OrderedDiffusion model;
 
 	Deque<Byte> unusedQueue;
 
 	/**
 	 * Note: As a result of this initialization process, information will be
 	 * evenly distributed. That is, no single piece of information is (more than
 	 * 1) more prevalent in the population than any other piece of information.
 	 */
 	public OrderedPersonFactory(OrderedDiffusion model) {
 		this.model = model;
 		
 		int numLeaders = (int) (model.opinionLeaderChance * model.totalPopulation);
 		int numLeadersBackwards = (int) (model.opinionLeaderChanceBackward * numLeaders);
		
		int numFollowers = model.totalPopulation - numLeaders;
		int numFollowersBackwards = (int) (model.opinionFollowerChanceBackward * numFollowers);
 
 		leaderValues = new boolean[model.totalPopulation];
 		leaderBackwardValues = new boolean[numLeaders];
		followerBackwardValues = new boolean[numFollowers];
 
 		
 		for (int i = 0; i < numLeaders; i++)
 			leaderValues[i] = true;
 		for (int i = 0; i < numLeadersBackwards; i++)
 			leaderBackwardValues[i] = true;
 		for (int i = 0; i < numFollowersBackwards; i++)
 			followerBackwardValues[i] = true;
 
 		// Need to do this manually rather than using collections to preserve
 		// consistency from random seed.
 		OrderedDiffusion.shuffleArray(model.random, leaderValues);
 		OrderedDiffusion.shuffleArray(model.random, leaderBackwardValues);
 		OrderedDiffusion.shuffleArray(model.random, followerBackwardValues);
 
 		allInfoPieces = new byte[model.numTotalInfoPieces];
 		for (byte i = 0; i < model.numTotalInfoPieces; i++)
 			allInfoPieces[i] = i;
 
 		// Initialize the unused Queue
 		unusedQueue = new LinkedList<Byte>();
 		randomizeAllInfoPieces();
 	}
 
 	@Override
 	public Person create() {
 		OrderedPerson person = new OrderedPerson();
 
 		person.id = personID++;
 		person.random = model.random;
 		person.knowledge = new byte[model.numInitialInfoPieces];
 		person.flipsToCheck = model.flipsToCheck;
 		person.sigmoidCenter = model.sigmoidCenter;
 
 		/*
 		 * WARNING: This procedure must ensure that no individual has two copies
 		 * of the same piece of information.
 		 */
 		for (int i = 0; i < model.numInitialInfoPieces; i++) {
 
 			boolean good = true;
 			do {
 				if (unusedQueue.isEmpty())
 					randomizeAllInfoPieces();
 				Byte candidateByte = unusedQueue.removeFirst();
 				good = true;
 				for (int j = 0; j < i; j++) {
 					if (person.knowledge[j] == candidateByte)
 						good = false;
 				}
 				if (good)
 					person.knowledge[i] = candidateByte;
 				else
 					unusedQueue.addLast(candidateByte);
 
 			} while (!good);
 
 		}
 		
 		// Opinion leadership and truth assigment.
 		
 		if (this.leaderValues[person.id]) {
 			// Opinion leaders
 			person.leader = true;
 			person.alpha = model.opinionLeaderAlpha;
 			person.backward = this.leaderBackwardValues[this.leaderPos++];
 			
 		} else {
 			// Everyone else (opinion followers)
 			person.leader = false;
 			person.alpha = model.opinionFollowerAlpha;
 			person.backward = this.followerBackwardValues[this.followerPos++];
 		}
 
 		person.reorderProb = model.reorderProb;
 
 		return person;
 	}
 
 	private void randomizeAllInfoPieces() {
 		OrderedDiffusion.shuffleArray(model.random, allInfoPieces);
 
 		// Now fill the unused queue
 		unusedQueue.clear();
 		for (int i = 0; i < allInfoPieces.length; i++) {
 			unusedQueue.add(allInfoPieces[i]);
 		}
 	}
 }
