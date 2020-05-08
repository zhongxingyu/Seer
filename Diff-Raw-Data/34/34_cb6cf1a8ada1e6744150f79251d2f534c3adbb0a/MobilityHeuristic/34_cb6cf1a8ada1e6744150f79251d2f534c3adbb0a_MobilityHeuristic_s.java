 package player.gamer.statemachine.eggplant.heuristic;
 
 import java.util.List;
 
 import util.statemachine.MachineState;
 import util.statemachine.Move;
 import util.statemachine.Role;
 import util.statemachine.StateMachine;
 import util.statemachine.exceptions.MoveDefinitionException;
 import util.statemachine.exceptions.TransitionDefinitionException;
 
 public class MobilityHeuristic implements Heuristic {
 
 	private long[] totalBranchingFactor;
 	private int[] samples;
 	private int numPlayers;
 	private int depthLimit;
 	protected final MobilityType type;
 
 	public MobilityHeuristic(MobilityType type, int numPlayers) {
 		this(type, numPlayers, (type == MobilityType.VAR_STEP) ? numPlayers : 1);
 	}
 
 	public MobilityHeuristic(MobilityType type, int numPlayers, int deptLimit) {
 		this.type = type;
 		this.numPlayers = numPlayers;
 		this.depthLimit = deptLimit;
 
 		int n = 0;
 		switch (type) {
 		case VAR_STEP: n = 1;
 		default: n = numPlayers;
 		}
 			
 		totalBranchingFactor = new long[n];
 		samples = new int[n];
 	}
 	
 	public int avgBranchingFactor(int index) {
 		return (int) (totalBranchingFactor[index] / samples[index]);
 	}
 
 	protected class BranchingData {
 		public BranchingData(int samples, int total) {
 			this.samples = samples;
 			this.total = total;
 		}
 		int samples;
 		int total;
 	}
 
 	protected int judgeMobility(double mobility, int index) {
 		if (samples[index] < 1) return -1;
 		double averageBranching = (double) totalBranchingFactor[index] / samples[index];
 		return judge(mobility, averageBranching);
 	}
 	
 	protected int judgeRelevantMobility(double mobility) {
		// System.out.println((this instanceof OpponentMobilityHeuristic ? "opponent " : "player ") + "mobility is " + mobility + " vs. avg " 
		//		+ (double) totalBranchingFactor[0] / samples[0]);
 		return judgeMobility(mobility, 0);
 	}
 	
 	/*
 	 * Uses a scaled logistic function to judge mobility against the average on
 	 * a scale of 0 to 100. With an advantage of (averageBranching / 3) moves
 	 * between the current and average mobility, the evaluator function will
 	 * produce a result of 73.
 	 */
 	private int judge(double mobility, double avg) {
 		return (int) Math
 		.round((1 / (double) (1 + Math.exp(-1.0 / (avg / 3) * (mobility - avg)))) * 100);
 	}
 	
 	protected int evalVarStep(StateMachine machine, MachineState state, Role role, 
 			int alpha, int beta, int properDepth, int evalDepth)
 	throws MoveDefinitionException, TransitionDefinitionException {
 		BranchingData data = getFirstRelevantBranchingData(machine, state, role, alpha, beta, evalDepth);
 		double avg = 0;
 		if (data.samples > 0) avg = data.total / (double) data.samples;
 		else return (alpha + beta) / 2;
 		int ev = judgeRelevantMobility(avg);
 		return (ev >= 0) ? ev : (alpha + beta) / 2;
 	}
 
 	protected int evalNStep(StateMachine machine, MachineState state, Role role, 
 			int alpha, int beta, int properDepth, int evalDepth)
 	throws MoveDefinitionException, TransitionDefinitionException {
 		BranchingData data = getBranchingData(machine, state, role, alpha, beta, evalDepth, false);
 		double avg;
 		if (data.samples > 0) avg = data.total / (double) data.samples;
 		else return (alpha + beta) / 2;
 		int ev = judgeRelevantMobility(avg);
 		return (ev >= 0) ? ev : (alpha + beta) / 2;
 	}
 
 	protected int evalOneStep(StateMachine machine, MachineState state, Role role, 
 			int alpha, int beta, int properDepth)
 	throws MoveDefinitionException, TransitionDefinitionException {
 		return evalNStep(machine, state, role, alpha, beta, properDepth, 0);
 	}
 
 	// Counts the branching factor the current and next depth turns if countCurrent is true,
 	// and over the moves at the nth turn down if countCurrent is false.
 	// N.B. Setting countCurrent to true will usually cause counting over both op-turns and noop-turns.
 	private BranchingData getBranchingData(StateMachine machine, MachineState state, Role role, 
 			int alpha, int beta, int depth, boolean countCurrent)
 	throws MoveDefinitionException, TransitionDefinitionException {
 		if (machine.isTerminal(state)) return new BranchingData(0, 0);
 		List<Move> moves = machine.getLegalMoves(state, role);
 		if (depth == 0) return new BranchingData(1, moves.size());
 		List<List<Move>> joints = machine.getLegalJointMoves(state);
 		int samples = 0, total = 0;
 		for (List<Move> joint : joints) {
 			MachineState nextState = machine.getNextState(state, joint);
 			BranchingData data = getBranchingData(machine, nextState, role, 
 					alpha, beta, depth - 1, countCurrent);
 			samples += data.samples;
 			total += data.total;
 			if (countCurrent) {
 				samples += 1;
 				total += moves.size();
 			}
 		}
 		return new BranchingData(samples, total);
 	}
 	
 	private BranchingData getFirstRelevantBranchingData(StateMachine machine, MachineState state, Role role, 
 			int alpha, int beta, int maxDepth)
 	throws MoveDefinitionException, TransitionDefinitionException {
 		if (machine.isTerminal(state)) return new BranchingData(0, 0);
 		List<Move> moves = machine.getLegalMoves(state, role);
 		if (relevant(moves)) return new BranchingData(1, moves.size());
 		else if (maxDepth == 0) return new BranchingData(0, 0);
 		List<List<Move>> joints = machine.getLegalJointMoves(state);
 		int samples = 0, total = 0;
 		for (List<Move> joint : joints) {
 			MachineState nextState = machine.getNextState(state, joint);
 			BranchingData data = 
 				getFirstRelevantBranchingData(machine, nextState, role, alpha, beta, maxDepth - 1);
 			samples += data.samples;
 			total += data.total;
 		}
 		return new BranchingData(samples, total);
 	}
 	
 	protected boolean relevant(int moves) { return moves > 1; }
 	
 	protected boolean relevant(List<Move> moves) {
 		return moves.size() > 1;
 	}
 
 	@Override
 	public int eval(StateMachine machine, MachineState state, Role role, 
 			int alpha, int beta, int depth, int absDepth) {
 		int properDepth = absDepth + depth;
 		try {
 			switch (type) {
 			case N_STEP:
 				return evalNStep(machine, state, role, alpha, beta, properDepth, depthLimit);
 			case VAR_STEP:
 				return evalVarStep(machine, state, role, alpha, beta, properDepth, depthLimit);
 			default:
 				return evalOneStep(machine, state, role, alpha, beta, properDepth);
 			}
 		} catch (MoveDefinitionException m) {
 			m.printStackTrace();
 		} catch (TransitionDefinitionException t) {
 			t.printStackTrace();
 		}
 		return (alpha + beta) / 2;
 	}
 
 	@Override
 	public void update(StateMachine machine, MachineState state, Role role, 
 			int alpha, int beta, int depth, int absDepth) 
 	throws MoveDefinitionException {
 		updateAverage(machine.getLegalMoves(state, role).size(), depth + absDepth);	
 	}
 
 	protected void updateAverage(int branchingFactor, int properDepth) {
 		int index = 0;
 		if (type != MobilityType.VAR_STEP) index = getIndex(properDepth);
 		else if (branchingFactor <= 1) return;
 		totalBranchingFactor[index] += branchingFactor;
 		samples[index]++;
 	}
 
 	protected int getIndex(long properDepth) {
 		return (int) (properDepth % numPlayers);
 	}
 
 	public int getDepthLimit() {
 		return depthLimit;
 	}
 
 }
