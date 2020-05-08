 /*******************************************************************************
  * Copyright (c) 2013 95A31.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     95A31 - initial API and implementation
  ******************************************************************************/
 package TreeAutomata;
 
 import GraphViz.*;
 import java.io.*;
 import java.util.*;
 
 public class Buchi extends Automata {
 
 	private HashSet<Integer> acceptanceCondition;
 
 	public Buchi(HashSet<Character> a, HashSet<Integer> s, Integer is, HashSet<Integer> ac, HashSet<Transition> tr) {
 		super(a, s, is, tr);
 		acceptanceCondition = ac;
 	}
 
 	public Buchi() {
 		super();
 		acceptanceCondition = new HashSet<Integer>();
 		transitionRelation = new HashSet<Transition>();
 	}
 
 	public static Buchi fromFile(String fileName) throws FileNotFoundException, IOException {
 
 		System.out.print("Parsing input file...\r");
 
 		HashSet<Character> a = new HashSet<Character>();
 		HashSet<Integer> s = new HashSet<Integer>();
 		Integer is = Integer.MIN_VALUE;
 		HashSet<Transition> tr = new HashSet<Transition>();
 		HashSet<Integer> ac = new HashSet<Integer>();
 
 		FileInputStream fis = new FileInputStream(fileName);
 		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
 		long readedFile = 0;
 		int state = Integer.MIN_VALUE;
 		String line;
 		while ((line = br.readLine()) != null) {
 
 			System.out.print("Parsing input file... [" + (int) (((float) (readedFile = readedFile + line.length()) / fis.getChannel().size()) * 100) + "%]\r");
 
 			if (line.length() <= 0 || line.charAt(0) == '#') {
 				continue;
 			} else if (line.equals("[Alphabet]")) {
 				state = 0;
 			} else if (line.equals("[States]")) {
 				state = 1;
 			} else if (line.equals("[Initial State]")) {
 				state = 2;
 			} else if (line.equals("[Transitions]")) {
 				state = 3;
 			} else if (line.equals("[Acceptance Conditions]")) {
 				state = 4;
 			} else {
 				switch (state) {
 					case 0:
 						a.add(line.charAt(0));
 						break;
 
 					case 1:
 						s.add(Integer.parseInt(line));
 						break;
 
 					case 2:
 						is = Integer.parseInt(line);
 						break;
 
 					case 3:
 						String[] ss = line.split(" ");
 						Integer node = Integer.parseInt(ss[0]);
 						Character c = ss[1].charAt(0);
 						Integer leftChildren = Integer.parseInt(ss[3]);
 						Integer rightChildren = Integer.parseInt(ss[4]);
 
 						tr.add(new Transition(node, c, leftChildren, rightChildren));
 						break;
 
 					case 4:
 						ss = line.split(" ");
 						for (String str : ss) {
 							ac.add(Integer.parseInt(str));
 						}
 						break;
 				}
 			}
 		}
 		br.close();
 		System.out.println("Parsing input file... [OK]   ");
 		return new Buchi(a, s, is, ac, tr);
 	}
 
 	public Buchi recognizeEmptyLanguage() {
 		System.out.print("Checking for empty language...\r");
 		HashSet<Transition> tr = (HashSet<Transition>) transitionRelation.clone();
 		HashSet<Integer> ac = (HashSet<Integer>) acceptanceCondition.clone();
 		Integer numberOfStates = states.size();
 
 		for (int i = 0; i < states.size(); i++) {
 			System.out.print("Checking for empty language... [" + (int) ((float) i / (float) states.size() * 100) + "%]\r");
 			HashMap<Integer, HashSet<HashSet<Integer>>> finiteTrees = calculateFiniteTrees(tr, numberOfStates, ac);
 			if (!finiteTrees.containsKey(initialState)) {
 				System.out.println("Checking for empty language... [OK]   ");
 				return new Buchi();
 			} else {
 				HashSet<Integer> statesInSomeTree = new HashSet<Integer>();
 				for (HashSet<HashSet<Integer>> treeSet : finiteTrees.values()) {
 					for (HashSet<Integer> t : treeSet) {
 						statesInSomeTree.addAll(t);
 					}
 				}
 				if (finiteTrees.keySet().equals(statesInSomeTree)) {
 					Buchi tmpBTA = (Buchi) this.clone();
 					tmpBTA.states = new HashSet<Integer>(finiteTrees.keySet());
 					tmpBTA.transitionRelation = tr;
 					System.out.println("Checking for empty language... [OK]   ");
 					return tmpBTA;
 				}
 			}
 			removeIncoerentTransition(tr, finiteTrees.keySet());
 			ac.retainAll(finiteTrees.keySet());
 			numberOfStates = finiteTrees.keySet().size();
 		}
 		System.out.println("Checking for empty language... [OK]   ");
 		return new Buchi();
 	}
 
 	private static void removeIncoerentTransition(HashSet<Transition> tr, Set<Integer> s) {
 		HashSet<Transition> transitionToEliminate = new HashSet<Transition>();
 		for (Transition t : tr) {
 			if (!s.contains(t.node) || !s.contains(t.leftChildren) || !s.contains(t.rightChildren)) {
 				transitionToEliminate.add(t);
 			}
 		}
 		tr.removeAll(transitionToEliminate);
 	}
 
 	public static HashMap<Integer, HashSet<HashSet<Integer>>> calculateFiniteTrees(HashSet<Transition> tr, Integer numberOfStates, HashSet<Integer> ac) {
 		HashMap<Integer, HashSet<HashSet<Integer>>> fts = new HashMap<Integer, HashSet<HashSet<Integer>>>();
 
 		HashSet<Transition> transRelWorkCopy = (HashSet<Transition>) tr.clone();
 		HashSet<Transition> transToEliminate = new HashSet<Transition>();
 
 		for (int i = 0; i < numberOfStates / 2; i++) {
 
 			transRelWorkCopy.removeAll(transToEliminate);
 			transToEliminate.clear();
 
 			for (Transition trans : transRelWorkCopy) {				
 				HashSet<HashSet<Integer>> tmpSetOfStates;
 
 				if (fts.containsKey(trans.node)) {
 					tmpSetOfStates = fts.get(trans.node);
 				} else {
 					tmpSetOfStates = new HashSet<HashSet<Integer>>();
 				}
 				if (ac.contains(trans.rightChildren) && ac.contains(trans.rightChildren)) {
					if(trans.leftChildren.equals(trans.rightChildren) && trans.node.equals(trans.leftChildren)){
						
					}
 					HashSet<Integer> tmpStates = new HashSet<Integer>();
 					tmpStates.add(trans.node);
 					tmpStates.add(trans.rightChildren);
 					tmpStates.add(trans.leftChildren);
 					tmpSetOfStates.add(tmpStates);
 					transToEliminate.add(trans);
 				} else if (fts.containsKey(trans.leftChildren) && ac.contains(trans.rightChildren)) {
 					for (HashSet<Integer> t : fts.get(trans.leftChildren)) {
 						HashSet<Integer> tmpStates = (HashSet<Integer>) t.clone();
 						tmpStates.add(trans.node);
 						tmpStates.add(trans.rightChildren);
 						tmpSetOfStates.add(tmpStates);
 					}
 					transToEliminate.add(trans);
 				} else if (fts.containsKey(trans.rightChildren) && ac.contains(trans.leftChildren)) {
 					for (HashSet<Integer> t : fts.get(trans.rightChildren)) {
 						HashSet<Integer> tmpStates = (HashSet<Integer>) t.clone();
 						tmpStates.add(trans.node);
 						tmpStates.add(trans.leftChildren);
 						tmpSetOfStates.add(tmpStates);
 					}
 					transToEliminate.add(trans);
				} else if (fts.containsKey(trans.leftChildren) && fts.containsKey(trans.rightChildren) && !trans.leftChildren.equals(trans.rightChildren)) {					
 					for (HashSet<Integer> lt : fts.get(trans.leftChildren)) {
 						HashSet<Integer> tmpStates = (HashSet<Integer>) lt.clone();
 						tmpStates.add(trans.node);
 						for (HashSet<Integer> rt : fts.get(trans.rightChildren)) {
 							tmpStates.addAll(rt);
 							tmpSetOfStates.add(tmpStates);
 						}
 					}
 					transToEliminate.add(trans);
 				}
 				if (!fts.containsKey(trans.node) && !tmpSetOfStates.isEmpty()) {
 					fts.put(trans.node, tmpSetOfStates);
 				}
 			}
 		}
 		return fts;
 	}
 
 	@Override
 	public Object clone() {
 		return new Buchi((HashSet<Character>) alphabet.clone(), (HashSet<Integer>) states.clone(), initialState, (HashSet<Integer>) acceptanceCondition.clone(), (HashSet<Transition>) transitionRelation.clone());
 	}
 
 	public void toImageFile(String fileName) throws IOException {
 		System.out.print("Saving a successful run to image file...\r");
 
 		HashSet<Integer> notVistitedStates = (HashSet<Integer>) states.clone();
 		LinkedList<Integer> currentBFSStates;
 		LinkedList<Integer> nextBFSStates = new LinkedList<Integer>();
 		nextBFSStates.add(initialState);
 		HashSet<Transition> transRelWorkCopy = (HashSet<Transition>) transitionRelation.clone();
 		HashSet<Transition> transToEliminate = new HashSet<Transition>();
 
 		GraphViz g = new GraphViz();
 		g.addln(g.start_graph());
 
 		g.addln("\tnode [shape = circle];");
 
 		while (!nextBFSStates.isEmpty()) {
 			System.out.print("Saving a successful run to image file... [" + (int) ((float) (states.size() - notVistitedStates.size()) / (float) states.size() * 100) + "%]\r");
 			currentBFSStates = nextBFSStates;
 			nextBFSStates = new LinkedList<Integer>();
 			System.gc();
 
 			g.add("\n\t{rank = same; ");
 			for (Integer s : currentBFSStates) {
 				g.add(s + " ");
 			}
 			g.addln("}");
 
 			while (!currentBFSStates.isEmpty()) {
 				Integer tmpState = currentBFSStates.pop();
 
 				transRelWorkCopy.removeAll(transToEliminate);
 				transToEliminate.clear();
 
 				for (Transition trans : transRelWorkCopy) {
 					if (trans.node.equals(tmpState)) {
 						if (trans.node == tmpState && (states.contains(trans.node) || trans.node.equals(initialState)) && states.contains(trans.leftChildren) && states.contains(trans.rightChildren)) {
 							if (trans.node.equals(trans.leftChildren) && trans.node.equals(trans.rightChildren) && !acceptanceCondition.contains(trans.node)) {
 								continue;
 							}
 							if (acceptanceCondition.contains(trans.node)) {
 								g.addln("\t" + tmpState + " [style=\"filled\", fillcolor=\"lawngreen\"];");
 							}
 							if (notVistitedStates.contains(trans.leftChildren)) {
 								g.addln("\t" + tmpState + " -> " + trans.leftChildren + " [label=" + trans.character + "];");
 								nextBFSStates.add(trans.leftChildren);
 								notVistitedStates.remove(trans.leftChildren);
 							} else {
 								double newState = Math.random() + trans.leftChildren;
 								g.addln("\t" + newState + " [label=\"" + trans.leftChildren + "\", style=\"dashed\"];");
 								g.addln("\t" + tmpState + " -> " + newState + " [label=" + trans.character + "];");
 							}
 							if (notVistitedStates.contains(trans.rightChildren)) {
 								g.addln("\t" + tmpState + " -> " + trans.rightChildren + " [label=" + trans.character + "];");
 								nextBFSStates.add(trans.rightChildren);
 								notVistitedStates.remove(trans.rightChildren);
 							} else {
 								double newState = Math.random() + trans.rightChildren;
 								g.addln("\t" + newState + " [label=\"" + trans.rightChildren + "\", style=\"dashed\"];");
 								g.addln("\t" + tmpState + " -> " + newState + " [label=" + trans.character + "];");
 							}
 							notVistitedStates.remove(tmpState);
 							transToEliminate.add(trans);
 							break;
 						}
 					}
 				}
 				transRelWorkCopy.removeAll(transToEliminate);
 				transToEliminate.clear();
 			}
 		}
 
 		g.addln(g.end_graph());
 		g.writeGraphToFile(g.getGraph(g.getDotSource(), "png"), fileName);
 		System.out.println("Saving a successful run to image file... [OK]   ");
 	}
 }
