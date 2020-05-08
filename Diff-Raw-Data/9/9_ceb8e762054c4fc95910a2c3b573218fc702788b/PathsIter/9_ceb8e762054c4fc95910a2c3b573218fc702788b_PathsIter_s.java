 package com.strategy.havannah.logic;
 
 import net.sf.javabdd.BDD;
 import net.sf.javabdd.BDDFactory;
 
 import com.strategy.api.board.Board;
 import com.strategy.api.logic.PathCalculator;
 import com.strategy.api.logic.Position;
 import com.strategy.util.Debug;
 import com.strategy.util.StoneColor;
 import com.strategy.util.operation.Logging;
 import com.strategy.util.preferences.Preferences;
 
 /**
  * TODO matrices are symmetrical - can be exploited to save memory
  * 
  * @author Ralph Dürig
  */
 public class PathsIter implements PathCalculator {
 	private BDDFactory fac;
 	private Board board;
 
 	private Logging logPandQ = Logging.create("iterative: p and q");
 	private Logging logNPandNQ = Logging.create("iterative: not p and not q");
 	private Logging logPMandMQ = Logging.create("iterative: pm and mq");
 	private Logging logPQorPMMQ = Logging
 			.create("iterative: pq or (pm and mq)");
 	private BDD[] reachWhite;
 	private BDD[] reachBlack;
 
 	private int rec;
 
 	/**
 	 * 
 	 */
 	public PathsIter(BDDFactory fac, Board board) {
 		this.fac = fac;
 		this.board = board;
 		this.rec = 0;
 		Debug initlog = Debug.create("iteratively creating reachability");
 		initReachability();
 		initlog.log();
 	}
 
 	public BDD getPath(Position p, Position q, StoneColor color) {
 		Integer indexP = board.getField(p.getRow(), p.getCol()).getIndex();
 		Integer indexQ = board.getField(q.getRow(), q.getCol()).getIndex();
 		int V = board.getRows() * board.getColumns();
 		BDD path;
 		if (color.equals(StoneColor.WHITE)) {
 			path = reachWhite[V * indexP + indexQ].id();
 		} else {
 			path = reachBlack[V * indexP + indexQ].id();
 		}
 		return path;
 
 	}
 
 	public void log() {
 		if (null != Preferences.getInstance().getOut()) {
 			Preferences.getInstance().getOut()
 					.println("all iterations: " + rec);
 		}
 		logPandQ.log();
 		logNPandNQ.log();
 		logPMandMQ.log();
 		logPQorPMMQ.log();
 	}
 
 	public void done() {
 		freeMatrix(reachWhite);
 		freeMatrix(reachBlack);
 	}
 
 	// ************************************************************************
 
 	private void initReachability() {
 		// number of vertices
 		int V = board.getColumns() * board.getRows();
 
 		// adjacency matrix with bdd style
 		reachWhite = new BDD[V * V];
 		reachBlack = new BDD[V * V];
 
 		for (int p = 0; p < V; p++) {
 			if (!board.isValidField(p)) {
 				continue;
 			}
 			for (int q = 0; q < V; q++) {
 				if (!board.isValidField(q)) {
 					continue;
 				}
 
 				Position posP = board.getField(p).getPosition();
 				Position posQ = board.getField(q).getPosition();
 				if (posP.isNeighbour(posQ)) {
 					reachWhite[V * p + q] = logPandQ.andLog(fac.ithVar(p),
 							fac.ithVar(q));
 					reachBlack[V * p + q] = logNPandNQ.andLog(fac.nithVar(p),
 							fac.nithVar(q));
 				} else {
					reachWhite[V * p + q] = fac.zero();
					reachBlack[V * p + q] = fac.zero();
 				}
 			}
 		}
 
 		// printMatrix(reachWhite);
 
 		// reachability matrix with floyd-warshall-algorithm and bdd style also
 		for (int k = 0; k < V; k++) {
 			if (!board.isValidField(k)) {
 				continue;
 			}
 			// Pick all vertices as source one by one
 			for (int i = 0; i < V; i++) {
 				if (!board.isValidField(i) || i == k) {
 					continue;
 				}
 				// Pick all vertices as destination for the
 				// above picked source
 				for (int j = 0; j < V; j++) {
 					if (!board.isValidField(j) || j == k || j == i) {
 						continue;
 					}
 
 					rec++;
 
 					// If vertex k is on a path from i to j,
 					// then make sure that the value of reach[i][j] is 1
 					doReach(reachWhite, k, i, j, board);
 					doReach(reachBlack, k, i, j, board);
 
 				}
 			}
 		}
 	}
 
 	private void doReach(BDD[] todo, int m, int p, int q, Board b) {
 		int V = board.getRows() * board.getColumns();
 		BDD reachpq = todo[V * p + q];
 		BDD reachpm = todo[V * p + m];
 		BDD reachmq = todo[V * m + q];
 
 		// todo[p][q] = reachpq.or(reachpm.and(reachmq));
 		// System.out.println("p=" + p + ", q=" + q + ", m=" + m);
 		todo[V * p + q] = logPQorPMMQ.orLog(reachpq,
 				logPMandMQ.andLog(reachpm.id(), reachmq.id()));
 	}
 
 	private void freeMatrix(BDD[] reach) {
 		for (int i = 0; i < reach.length; i++) {
 			if (null != reach[i]) {
 				reach[i].free();
 			}
 		}
 
 	}
 
 	private void printMatrix(BDD[] reach) {
 		System.out
 				.println("Following matrix is transitive closure of the given graph");
 		int limit = board.getRows() * board.getColumns();
 		for (int i = 0; i < reach.length; i++) {
 			if (i % limit == 0) {
 				System.out.println();
 			}
 			if (null != reach[i]) {
 				System.out.print(i / limit + "x" + i % limit + " O ");
 			} else {
 				System.out.print(" null ");
 			}
 		}
 		System.out.println();
 
 	}
 
 }
