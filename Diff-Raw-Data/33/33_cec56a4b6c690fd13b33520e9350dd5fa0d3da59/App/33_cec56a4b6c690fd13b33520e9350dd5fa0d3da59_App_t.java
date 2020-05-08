 package com.arykow.applications.solver;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 public class App {
 
 	public static void main(String[] args) {
 		int index = 0;
 		for (Board board : resolve(new Board(args[0]))) {
 			System.out.println(board);
 			System.out.println("-- " + (++index) + " ----------------------------------------------------------------------------------");
 		}
 	}
 
 	private static Set<Board> resolve(Board board) {
 		Map<Integer, Board> boards = new HashMap<Integer, Board>();
 		boards.put(indexxxxx++, board.cloneBoard(true));
 
 		Set<Board> solutions = new HashSet<Board>();
 
		while (solutions.isEmpty() && !boards.isEmpty()) {
 			updateSolutions(boards, solutions);
 		}
 
 		return solutions;
 	}
 
 	private static int indexxxxx = 0;
 
 	public static void updateSolutions(final Map<Integer, Board> boards, Set<Board> solutions) {
 
 		class Updater {
 
 			private final Collection<Integer> invalids = new ArrayList<Integer>();
 			private final Map<Integer, Map<Integer, Set<Integer>>> values = new HashMap<Integer, Map<Integer, Set<Integer>>>();
 			private final Map<Integer, Board> next = new HashMap<Integer, Board>();
 
 			public void update() {
 				for (Integer index : invalids) {
 					boards.remove(index);
 				}
 				for (Integer index : new ArrayList<Integer>(boards.keySet())) {
					if (next.isEmpty()) {
						Board board = boards.remove(index);
						if (values.containsKey(index)) {
							for (Integer cell : values.get(index).keySet()) {
								for (Integer value : values.get(index).get(cell)) {
									Board aaa = board.cloneBoardAndUpdateValue(true, cell, value);
									if (aaa.isValid()) {
										next.put(indexxxxx++, aaa);
										System.out.println(aaa);
									}
								}
 							}
 						}
					} else {
						break;
 					}
 				}
 				boards.putAll(next);
 			}
 		}
 		final Updater updater = new Updater();
 
 		for (final int index : boards.keySet()) {
 			Board solution = boards.get(index);
 			EmptyInformations informations = EmptyInformationsVisitorBuilder.buildEmptyInformations(solution);
 
 			if (informations.isSolved()) {
 				solutions.add(solution);
 			} else if (!informations.isValid()) {
 				updater.invalids.add(index);
 			} else {
 				final Map<Integer, Set<Integer>> values = new HashMap<Integer, Set<Integer>>();
 				informations.acceptEmptyInformationsVisitor(new EmptyInformationsVisitor() {
 					public void visitInformation(Integer length, Integer indexx, int value) {
 						if (!values.containsKey(indexx)) {
 							values.put(indexx, new TreeSet<Integer>());
 						}
 						values.get(indexx).add(value);
 					}
 				});
 				updater.values.put(index, values);
 			}
 		}
 
 		updater.update();
 
 	}
 
 }
