 package org.weiqi;
 
 import java.util.HashSet;
 import java.util.Set;
 import java.util.Stack;
 
 import org.util.Util;
 import org.weiqi.Weiqi.Array;
 import org.weiqi.Weiqi.Occupation;
 
 public class Board extends Array<Occupation> {
 
 	public Board() {
 		for (Coordinate c : Coordinate.all())
 			set(c, Occupation.EMPTY);
 	}
 
 	public Board(Board board) {
 		super(board);
 	}
 
 	public enum MoveType {
 		PLACEMENT, CAPTURE, INVALID
 	};
 
 	/**
 	 * Plays a move on the Weiqi board. Do not check for repeats in game state
 	 * history since Board do not have them. Use GameSet.moveIfPossible() for
 	 * the rule-accordance version.
 	 * 
 	 * This method do not take use of GroupAnalysis for performance reasons.
 	 */
 	public MoveType playIfSeemsPossible(Coordinate c, Occupation player) {
 		MoveType type;
 		Occupation current = get(c);
 		Occupation opponent = player.opponent();
 
 		if (current == Occupation.EMPTY) {
 			type = MoveType.PLACEMENT;
 			set(c, player);
 
 			for (Coordinate neighbor : c.neighbors())
 				if (get(neighbor) == opponent && killIfDead(neighbor))
 					type = MoveType.CAPTURE;
 
 			if (!hasBreath(c)) {
 				set(c, Occupation.EMPTY);
 				type = MoveType.INVALID;
 			}
 		} else
 			type = MoveType.INVALID;
 
 		return type;
 	}
 
 	private boolean killIfDead(Coordinate c) {
 		boolean isKilled = !hasBreath(c);
 
 		if (isKilled)
 			for (Coordinate c1 : findGroup(c))
 				set(c1, Occupation.EMPTY);
 
 		return isKilled;
 	}
 
 	private boolean hasBreath(Coordinate c) {
 		Set<Coordinate> group = new HashSet<>();
 		Occupation color = get(c);
 		group.add(c);
 
 		Stack<Coordinate> unexplored = new Stack<>();
 		unexplored.push(c);
 
 		while (!unexplored.isEmpty())
 			for (Coordinate c1 : unexplored.pop().neighbors()) {
 				Occupation color1 = get(c1);
 
 				if (color1 == color) {
 					if (group.add(c1))
 						unexplored.push(c1);
 				} else if (color1 == Occupation.EMPTY)
 					return true;
 			}
 
 		return false;
 	}
 
 	protected Set<Coordinate> findGroup(Coordinate c) {
 		Set<Coordinate> group = Util.createHashSet();
 		Occupation color = get(c);
 		group.add(c);
 
 		Stack<Coordinate> unexplored = new Stack<>();
 		unexplored.push(c);
 
 		while (!unexplored.isEmpty())
 			for (Coordinate c1 : unexplored.pop().neighbors())
 				if (get(c1) == color && group.add(c1))
 					unexplored.push(c1);
 
 		return group;
 	}
 
 	/**
 	 * Plays a move on the Weiqi board. Uses group analysis which is slower.
 	 */
 	public void play1(Coordinate c, Occupation player) {
 		Occupation current = get(c);
 		Occupation opponent = player.opponent();
 
 		if (current == Occupation.EMPTY) {
 			set(c, player);
 			GroupAnalysis ga = new GroupAnalysis(this);
 
 			for (Coordinate neighbor : c.neighbors())
 				if (get(neighbor) == opponent)
 					killIfDead1(ga, neighbor);
 
 			if (killIfDead1(ga, c))
 				throw new RuntimeException("Cannot perform suicide move");
 		} else
 			throw new RuntimeException("Cannot move on occupied position");
 	}
 
 	private boolean killIfDead1(GroupAnalysis ga, Coordinate c) {
		Integer groupId = ga.getGroupId(c);
		boolean isKilled = ga.getNumberOfBreathes(groupId) == 0;
 
 		if (isKilled)
			for (Coordinate c1 : ga.getCoords(groupId))
 				set(c1, Occupation.EMPTY);
 
 		return isKilled;
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 
 		for (int x = 0; x < Weiqi.size; x++) {
 			for (int y = 0; y < Weiqi.size; y++) {
 				Coordinate c = Coordinate.c(x, y);
 				sb.append(get(c).display() + " ");
 			}
 			sb.append("\n");
 		}
 
 		return sb.toString();
 	}
 
 }
