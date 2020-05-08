 package nardiff.ordered;
 
 import java.util.Deque;
 import java.util.LinkedList;
 
 import nardiff.MersenneTwisterFast;
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
 	int personID = 0;
 
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
 		
 		if (model.random.nextBoolean(model.opinionLeaderChance)) {
 			// Opinion leaders
			person.leader = true;
			person.alpha = model.opinionLeaderAlpha;
 			person.backward = model.random.nextBoolean(model.opinionLeaderChanceBackward);
 			
 		} else {
 			// Everyone else (opinion followers)
			person.leader = false;
 			person.alpha = model.opinionFollowerAlpha;
 			person.backward = model.random.nextBoolean(model.opinionFollowerChanceBackward);
 		}
 
 		person.reorderProb = model.reorderProb;
 
 		return person;
 	}
 
 	private void randomizeAllInfoPieces() {
 		for (int i = 0; i < allInfoPieces.length; i++) {
 			int index = model.random.nextInt(allInfoPieces.length);
 			// Simple swap
 			byte tmp = allInfoPieces[index];
 			allInfoPieces[index] = allInfoPieces[i];
 			allInfoPieces[i] = tmp;
 		}
 
 		// Now fill the unused queue
 		unusedQueue.clear();
 		for (int i = 0; i < allInfoPieces.length; i++) {
 			unusedQueue.add(allInfoPieces[i]);
 		}
 
 	}
 
 	/**
 	 * Just a test routine for debugging...
 	 * 
 	 * @param args
 	 */
 	// public static void main(String[] args) {
 	// MersenneTwisterFast random = new MersenneTwisterFast(1);
 	// OrderedPersonFactory opf = new OrderedPersonFactory(random, (byte) 30,
 	// (byte) 5, 0.5f, 0.5f, 15, 5f);
 	// for (int i = 0; i < 30; i++) {
 	// System.out.println(opf.create());
 	// }
 	// }
 
 }
