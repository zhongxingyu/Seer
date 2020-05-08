 package player.gamer.statemachine.eggplant.heuristic;
 
 import java.util.*;
 
 import player.gamer.statemachine.eggplant.heuristic.MobilityHeuristic.BranchingData;
 import util.statemachine.MachineState;
 import util.statemachine.Role;
 import util.statemachine.StateMachine;
 import util.statemachine.Move;
 import util.statemachine.exceptions.MoveDefinitionException;
 import util.statemachine.exceptions.TransitionDefinitionException;
 
 public class OpponentMobilityHeuristic extends MobilityHeuristic {
 
 	public OpponentMobilityHeuristic(MobilityType type, int numPlayers) {
 		this(type, numPlayers, 1);
 	}
 	
 	public OpponentMobilityHeuristic(MobilityType type, int numPlayers, int deptLimit) {
 		super(type, numPlayers, deptLimit);
 	}
 	
 	public int eval(StateMachine machine, MachineState state, Role role, int alpha, int beta, int depth, int absDepth) {
 		try {
 			double avg = 0;
 			switch (type) {
 			case VAR_STEP:
 				BranchingData data = getRelevantOpponentBranchingData(machine, state, role, alpha, beta, getDepthLimit());
 				if (data.samples == 0) return (alpha + beta) / 2;
 				avg = (double) data.total / data.samples;
 				return judgeRelevantMobility(avg);
 			default: 
				avg = machine.getLegalJointMoves(state).size() / machine.getLegalMoves(state, role).size();
 				return judgeMobility(avg, getIndex(depth + absDepth));
 			}
 		} catch (MoveDefinitionException e) {
 			e.printStackTrace();
 		} catch (TransitionDefinitionException e) {
 			e.printStackTrace();
 		}
 		return (alpha + beta) / 2;
 	}
 	
 	private BranchingData getRelevantOpponentBranchingData(StateMachine machine, MachineState state, Role role, 
 			int alpha, int beta, int maxDepth)
 	throws MoveDefinitionException, TransitionDefinitionException {
 		if (machine.isTerminal(state)) return new BranchingData(0, 0);
 		List<Move> moves = machine.getLegalMoves(state, role);
 		List<List<Move>> joints = machine.getLegalJointMoves(state);
		int sumMoves = joints.size(), roleMoves = moves.size(), quo = sumMoves / roleMoves;
		System.out.println("sum " + sumMoves + ", role " + roleMoves);
 		if (relevant(quo)) return new BranchingData(1, quo);
 		else if (maxDepth == 0) return new BranchingData(0, 0);
 		int samples = 0, total = 0;
 		for (List<Move> joint : joints) {
 			MachineState nextState = machine.getNextState(state, joint);
 			BranchingData data = 
 				getRelevantOpponentBranchingData(machine, nextState, role, alpha, beta, maxDepth - 1);
 			samples += data.samples;
 			total += data.total;
 		}
 		return new BranchingData(samples, total);
 	}
 
 	@Override
 	public void update(StateMachine machine, MachineState state, Role role, 
 			int alpha, int beta, int depth, int absDepth) 
 	throws MoveDefinitionException {
 		int pmSize = machine.getLegalMoves(state, role).size();
		int oppSize = machine.getLegalJointMoves(state).size() / pmSize;
 		updateAverage(oppSize, depth + absDepth);
 	}
 
 }
