 package scheduler;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.ArrayList;
 
 /**
  * Beam Search
  *
  */
 public class FavouriteSLSScheduler extends Scheduler {
 	// parameter determining how many parameters to manipulate and how many neighbours to keep
 	// K = 1 means the same as greedy descent, K = N means brute force combo breaker
 	// NOTE: you may change K to whatever value that suites your fancy, be warned, larger K
 	//       slower the game
 	private static int K = 1;
 	private static int NULL = -1; 
 	
 	/**
 	 * @return names of the authors and their student IDs (1 per line).
 	 */
 	public String authorsAndStudentIDs() {
 		String ret = "NAMES OF THE AUTHORS AND THEIR STUDENT IDs (1 PER LINE)\n";
 		ret += "Yi (Armand) Li 61420048\n";
		ret += "Jeffrey Cheung 66994062\n";
 		return ret;
 	}
 
 	@Override
 	public ScheduleChoice[] solve(SchedulingInstance pInstance) throws Exception {
 		ScheduleChoice[] bests = randomAssignment(pInstance);
 		LinkedList<ChangeSet>[] changes = new LinkedList[K];
 		for (int i = 0; i < K; ++i)
 			changes[i] = new LinkedList<ChangeSet>();
 		while (!timeIsUp() && evaluator.violatedConstraints(pInstance, bests) > 0){
 			Selected[] choices = new Selected[K];
 			for (int i = 0; i < K; ++i){
 				choices[i] = new Selected(new ArrayList<Integer>(K), new ArrayList<Integer>(K), new ArrayList<Integer>(K), new ArrayList<Boolean>(K), NULL, null);
 				for (int j = 0; j < K; ++j){
 					choices[i].courses.add(0);
 					choices[i].isRoom.add(false);
 					choices[i].vals.add(0);
 					choices[i].prev_vals.add(0);
 				}
 			}
 			BeamSearch(pInstance, bests, changes, choices);
 			for (int i = 0; i < K; ++i){
 				changes[i] = choices[i].sup;
 				changes[i].add(new ChangeSet(choices[i].courses, choices[i].vals, choices[i].prev_vals, choices[i].isRoom));
 			}
 		}
 		int best = Integer.MAX_VALUE, besti = 0;
 		for (int i = 0; i < K; ++i){
 			applyChangeSet(bests, changes[i]);
 			int temp;
 			if ((temp = evaluator.violatedConstraints(pInstance, bests)) < best){
 				best = temp;
 				besti = i;
 			}
 			unapplyChangeSet(bests, changes[i]);
 		}
 		applyChangeSet(bests, changes[besti]);
 		return bests;
 	}
 	
 	void BeamSearch(SchedulingInstance in, ScheduleChoice[] s, LinkedList<ChangeSet>[] path, Selected[] selected) throws Exception{
 		for (int i = 0; i < K; ++i){
 			applyChangeSet(s, path[i]);
 			Choice c = permSeed(in, s);
 			do {
 				int[] originals = new int[K];
 				for (int j = 0; j < K; ++j)
 					if (c.isRoom.get(j))
 						originals[j] = s[c.courses.get(j)].room;
 					else
 						originals[j] = s[c.courses.get(j)].timeslot;
 				
 				findOptimalAssignment(in, s, c, selected, path[i], originals);
 				
 				applyAssignment(s, c, originals);
 			} while (permutation(in, s, c, K, true));
 			unapplyChangeSet(s, path[i]);
 		}
 	}
 	
 	void findOptimalAssignment(SchedulingInstance in, ScheduleChoice[] s, Choice c, Selected[] selected, LinkedList<ChangeSet> path, int[] pv) throws Exception{
 		int[] assignments = new int[K];
 		do {
 			applyAssignment(s, c, assignments);
 			int eval = evaluator.violatedConstraints(in, s);
 			for (int i = 0; i < K; ++i)
 				if (selected[i].score < 0 || selected[i].score > eval){
 					selected[i].courses = (ArrayList<Integer>) c.courses.clone();
 					selected[i].isRoom = (ArrayList<Boolean>) c.isRoom.clone();
 					for (int j = 0; j < K; ++j){
 						selected[i].vals.set(j, assignments[j]);
 						selected[i].prev_vals.set(j, pv[j]);
 					}
 					selected[i].score = eval;
 					selected[i].sup = cloneChangeSet(path);
 					break;
 				}
 		} while (incrementIterate(in, c, assignments));
 	}
 	
 	ScheduleChoice[] randomAssignment(SchedulingInstance in){
 		ScheduleChoice[] choice = new ScheduleChoice[in.numCourses];
 		for (int i = 0; i < in.numCourses; ++i){
 			choice[i] = new ScheduleChoice();
 			choice[i].room = Math.abs(this.r.nextInt()) % in.numRooms;
 			choice[i].timeslot = Math.abs(this.r.nextInt()) % in.numTimeslots;
 		}
 		return choice;
 	}
 	
 	Choice permSeed(SchedulingInstance in, ScheduleChoice[] s){
 		assert( in.numCourses * 2 <= K );
 		Choice c = new Choice(new ArrayList<Integer>(K), new ArrayList<Boolean>(K));
 		for (int i = 0; i < K; ++i){
 			c.courses.add(IDToCourse(i));
 			c.isRoom.add(IDToRoom(i));
 		}
 		return c;
 	}
 	
 	boolean permutation(SchedulingInstance in, ScheduleChoice[] s, Choice c, int k, boolean move){
 		if (k == 1){
 			if (c.courses.get(K - 1) >= in.numCourses)
 				return false;
 			if (!move)
 				return true;
 			int id = varToID(c.courses.get(K - 1), c.isRoom.get(K - 1)) + 1;
 			c.courses.set(K - 1, IDToCourse(id));
 			c.isRoom.set(K - 1, IDToRoom(id));
 			if (c.courses.get(K - 1) >= in.numCourses)
 				return false;
 			return true;
 		}
 		if (c.courses.get(K - k) == in.numCourses - 1 && !c.isRoom.get(K - k))
 			return false;
 		if (!permutation(in, s, c, k - 1, move))
 			if (c.isRoom.get(K - k)){
 				c.isRoom.set(K - k, false);
 				int id = varToID(c.courses.get(K - k), c.isRoom.get(K - k));
 				for (int i = K - k + 1, count = 1; i < K; ++i, ++count){
 					c.courses.set(i, IDToCourse(id + count));
 					c.isRoom.set(i, IDToRoom(id + count));
 				}
 				return permutation(in, s, c, k - 1, false);
 			} else {
 				c.isRoom.set(K - k, true);
 				c.courses.set(K - k, c.courses.get(K - k) + 1);
 				int id = varToID(c.courses.get(K - k), c.isRoom.get(K - k));
 				for (int i = K - k + 1, count = 1; i < K; ++i, ++count){
 					c.courses.set(i, IDToCourse(id + count));
 					c.isRoom.set(i, IDToRoom(id + count));
 				}
 				return permutation(in, s, c, k - 1, false);
 			}
 		return true;
 	}
 	
 	int varToID(int course, boolean isRoom){
 		return course * 2 + (isRoom ? 0 : 1);
 	}
 	
 	int IDToCourse(int id){
 		return id / 2;
 	}
 	
 	boolean IDToRoom(int id){
 		return (id % 2 == 0 ? true : false);
 	}
 	
 	int maxDom(SchedulingInstance in, Choice c, int index){
 		if (c.isRoom.get(index))
 			return in.numRooms;
 		else
 			return in.numTimeslots;
 	}
 	
 	boolean incrementIterate(SchedulingInstance in, Choice c, int[] nums){
 		int i = 0;
 		int count = 0;
 		while (i < nums.length && nums[i] >= maxDom(in, c, i) - 1){
 			nums[i++] = 0;
 			count++;
 		}
 		if (count == nums.length)
 			return false;
 		nums[i]++;
 		return true;
 	}
 	
 	void applyAssignment(ScheduleChoice[] s, Choice c, int[] assign){
 		for (int i = 0; i < c.courses.size(); ++i)
 			if (c.isRoom.get(i))
 				s[c.courses.get(i)].room = assign[i];
 			else
 				s[c.courses.get(i)].timeslot = assign[i];
 	}
 
 	LinkedList<ChangeSet> cloneChangeSet(LinkedList<ChangeSet> d){
 		LinkedList<ChangeSet> ret = new LinkedList<ChangeSet>();
 		Iterator<ChangeSet> it = d.iterator();
 		while (it.hasNext())
 			ret.add(it.next());
 		return ret;
 	}
 	
 	void applyChangeSet(ScheduleChoice[] schedule, LinkedList<ChangeSet> set){
 		Iterator<ChangeSet> it = set.iterator();
 		while (it.hasNext()){
 			ChangeSet c = it.next();
 			for (int i = 0; i < c.courses.size(); ++i){
 				if (c.isRoom.get(i))
 					schedule[c.courses.get(i)].room = c.vals.get(i);
 				else 
 					schedule[c.courses.get(i)].timeslot = c.vals.get(i);
 			}
 		}
 	}
 	
 	void unapplyChangeSet(ScheduleChoice[] schedule, LinkedList<ChangeSet> set){
 		Iterator<ChangeSet> it = set.descendingIterator();
 		while (it.hasNext()){
 			ChangeSet c = it.next();
 			for (int i = 0; i < c.courses.size(); ++i){
 				if (c.isRoom.get(i))
 					schedule[c.courses.get(i)].room = c.prev_vals.get(i);
 				else 
 					schedule[c.courses.get(i)].timeslot = c.prev_vals.get(i);
 			}
 		}
 	}
 	
 	private class ChangeSet{
 		ArrayList<Integer> courses, vals, prev_vals;
 		ArrayList<Boolean> isRoom;
 		ChangeSet(ArrayList<Integer> c, ArrayList<Integer> v, ArrayList<Integer> pv, ArrayList<Boolean> r){
 			courses = c; vals = v; prev_vals = pv; isRoom = r; 
 		}
 	}
 	
 	private class Selected{
 		ArrayList<Integer> courses, vals, prev_vals;
 		ArrayList<Boolean> isRoom;
 		int score;
 		LinkedList<ChangeSet> sup;
 		Selected(ArrayList<Integer> c, ArrayList<Integer> v, ArrayList<Integer> pv, ArrayList<Boolean> r, int s, LinkedList<ChangeSet> m){
 			courses = c; vals = v; prev_vals = pv; isRoom = r; score = s; sup = m;
 		}
 	}
 	
 	private class Choice{
 		ArrayList<Integer> courses;
 		ArrayList<Boolean> isRoom;
 		Choice(ArrayList<Integer> c, ArrayList<Boolean> r){
 			courses = c; isRoom = r;
 		}
 	}
 }
