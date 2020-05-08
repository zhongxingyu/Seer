 package nardiff.ordered;
 
 import java.util.List;
 
 import nardiff.Person;
 
 public class OrderedPerson implements Person {
 
 	public byte[] knowledge;
 
 	/**
 	 * @param info
 	 * @return Where in the current knowledge base the requested byte is; null
 	 *         if the byte is not present.
 	 */
 	public Byte getIndexOf(byte info) {
 		/*
 		 * There are two options here; one is storing the locations in a
 		 * different data structure and keeping them updated, and the other is
 		 * just the brute force checking. The former has much better performance
 		 * with large data sets, but these data sets are expected to be much
 		 * smaller. At this point, the choice has been made for simplicity over
 		 * performance.
 		 */
 		for (byte pos = 0; pos < knowledge.length; pos++) {
 			if (knowledge[pos] == info)
 				return pos;
 		}
 
 		return null; // The byte of knowledge was not found
 	}
 
 	/**
 	 * @param info
 	 * @return true if this person has the information byte at all.
 	 */
 	public boolean has(byte info) {
 		Byte pos = getIndexOf(info);
 		if (pos != null)
 			return true;
 		return false;
 	}
 
 	public byte getNumCurrentInversions(byte pos) {
 		byte numInversions = 0;
 		for (int i = 0; i < knowledge.length; i++) {
 			if (i < pos)
 				if (knowledge[i] > knowledge[pos])
 					numInversions++;
 			if (i > pos)
 				if (knowledge[i] < knowledge[pos])
 					numInversions++;
 			// (a knowledge byte cannot be inverted with itself)
 		}
 		return numInversions;
 	}
 
 	/**
 	 * @param pos
 	 *            The candidate position for insertion
 	 * @param info
 	 *            The candidate information for insertion
 	 * @return The number of inversions that would be present if the byte
 	 *         <i>info</i> was inserted into the person's knowledge base at
 	 *         position <i>pos</i>.
 	 */
 	public byte getNumPotentialInversions(byte pos, byte info) {
 		byte numInversions = 0;
 		for (int i = 0; i < knowledge.length; i++) {
 			if (i < pos)
 				if (info < knowledge[i])
 					numInversions++;
 			if (i >= pos)
 				if (info > knowledge[i])
 					numInversions++;
 		}
 		return numInversions;
 	}
 
 	/**
 	 * This function calculates the number of inconsistencies in information
 	 * ordering with a potential insertion with a given neighbor.
 	 * 
 	 * TODO: This routine needs significant verification and testing.
 	 * 
 	 * @param pos
 	 * @param info
 	 * @param neighbor
 	 * @return
 	 */
 	public byte getNumInconsistencies(byte pos, byte info,
 			OrderedPerson neighbor) {
 		// The position of the candidate byte in the neighbor's array
 		Byte neighborPos = neighbor.getIndexOf(info);
 		if (neighborPos == null)
 			return 0; // Cannot have ANY inconsistencies if neighbor doesn't
 						// have this byte
 
 		// My info bytes that will be before the new info byte
 		byte[] myBefore = new byte[pos];
 		System.arraycopy(this.knowledge, 0, myBefore, 0, pos);
 
 		// My info bytes that will be after the new info byte
 		byte[] myAfter = new byte[knowledge.length - pos];
 		System.arraycopy(this.knowledge, pos, myAfter, 0, this.knowledge.length
 				- pos);
 
 		// My neighbor's info bytes that are before that neighbor's
 		// getIndexOf(info)
 		byte[] neighborBefore = new byte[neighborPos];
 		System.arraycopy(neighbor.knowledge, 0, neighborBefore, 0, neighborPos);
 
 		// My neighbor's info bytes that are after that neighbor's
 		// getIndexOf(info)
 		byte[] neighborAfter = new byte[neighbor.knowledge.length - neighborPos];
 		System.arraycopy(neighbor.knowledge, neighborPos, neighborAfter, 0,
 				neighbor.knowledge.length - neighborPos);
 
 		// Now we just need the number of elements in the intersections of the
 		// different segments.
 		byte numInconsistencies = 0;
 
 		// Again, this is horribly inefficient, but we're expecting small n
 		// myBefore and neighborAfter
 		for (int i = 0; i < myBefore.length; i++)
 			for (int j = 0; j < neighborAfter.length; j++)
 				if (myBefore[i] == neighborAfter[j])
 					numInconsistencies++;
 
 		// neighborBefore and myAfter
 		for (int i = 0; i < neighborBefore.length; i++)
 			for (int j = 0; j < myAfter.length; j++)
 				if (neighborBefore[i] == myAfter[j])
 					numInconsistencies++;
 
 		return numInconsistencies;
 	}
 
 	public void diffuse(byte info, List<OrderedPerson> neighbors) {
 		// We need to assign a probability to each possible insertion position
 
 		// Insersion positions are such that 0 inserts *before* the current
 		// item 0, 1 inserts before the current item 1, and so on. The maximum
 		// insertion point is therefore length+1, where it would be added
 
 		// Create an 2d array containing information on the # of inversions
 		// there would be at every point. The first index is for different
 		// people, starting with this
 
 		byte numNeighbors = (byte) neighbors.size();
 		byte potentialLocations = (byte) (knowledge.length + 1);
 
 		byte[] myInversions = new byte[potentialLocations];
 		byte[][] neighborInconsistencies = new byte[numNeighbors][potentialLocations];
 
 		// Calculate my potential inversions
 		for (byte i = 0; i < potentialLocations; i++) {
 			myInversions[i] = getNumPotentialInversions(i, info);
 		}
 
 		// Calculate neighbor inconsistencies
 		for (byte candidatePos = 0; candidatePos < potentialLocations; candidatePos++) {
 			for (byte neighborIndex = 0; neighborIndex < numNeighbors; neighborIndex++) {
 				neighborInconsistencies[neighborIndex][candidatePos] = getNumInconsistencies(
 						candidatePos, info, neighbors.get(neighborIndex));
 			}
 		}
 		
 		// Do something to create probabilities of insertion at each location
 		
 		
 		
 		
 		// Actually do the insertion
 		
 		
 	}
 
 	/**
 	 * TODO: Needs to be tested.
 	 * 
 	 * @param position
 	 * @param info
 	 */
	public void insertAt(byte position, byte info) {
 		byte[] newKnowledge = new byte[knowledge.length + 1];
 
 		// handle edge cases first
 
 		if (position == 0) { // insert at beginning
 			newKnowledge[0] = info;
 			System.arraycopy(knowledge, 0, newKnowledge, 1, knowledge.length);
 		} else if (position == knowledge.length + 1) { // insert at end
 			System.arraycopy(knowledge, 0, newKnowledge, 0, knowledge.length);
 			newKnowledge[position] = info;
 		} else { // insert into middle
			System.arraycopy(knowledge, 0, newKnowledge, 0, position);
 			newKnowledge[position] = info;
 			System.arraycopy(knowledge, position, newKnowledge, position + 1,
					newKnowledge.length - position - 1);
 		}
 
 		this.knowledge = newKnowledge;
 	}
 	
 	
 	public String toString() {
 		StringBuffer sb = new StringBuffer();
 		sb.append("OrderedPerson(");
 		for (int i = 0; i < knowledge.length; i++) {
 			sb.append(knowledge[i]);
 			if (i < (knowledge.length - 1))
 				sb.append(",");
 		}
 		sb.append(")");
 				
 		return sb.toString();
 	}
 	
 
 }
