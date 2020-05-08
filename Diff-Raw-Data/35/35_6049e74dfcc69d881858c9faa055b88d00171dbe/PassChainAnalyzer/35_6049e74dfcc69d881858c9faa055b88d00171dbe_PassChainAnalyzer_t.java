 /*
  * $Log: PassChainAnalyzer.java,v $
  * Revision 1.2  2003/01/05 08:29:59  koji
  * *** empty log message ***
  *
  * Revision 1.1  2003/01/04 08:35:36  koji
  * �ѥ���Ϣ�����ѥ��ߥ����ɥ�֥롢���줾�����ײ��Ϥ��ɲ�
  *
  *
  */
 
 package soccerscope.util.analyze;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.TableModel;
 
 import soccerscope.model.GameEvent;
 import soccerscope.model.Scene;
 import soccerscope.model.Team;
 import soccerscope.model.WorldModel;
 import soccerscope.util.analyze.PassAnalyzer.Pass;
 
 import com.jamesmurty.utils.XMLBuilder;
 
 public class PassChainAnalyzer extends SceneAnalyzer implements Xmling {
 
 	public static String NAME = "Pass Chain";
 
 	private List<List<Pass>> passChains = new LinkedList<List<Pass>>();
 
 	int leftChain[] = new int[20];
 	int rightChain[] = new int[20];
 
 	public void init() {
 		super.init();
 		leftChain = new int[20];
 		rightChain = new int[20];
 		passChains.clear();
 	}
 
 	public String getName() {
 		return NAME;
 	}
 
 	public Object getValueAt(int col, int fromTime, int toTime) {
 		count(fromTime, toTime);
 		switch (col) {
 		case ROW_NAME:
 			return getName();
 		case LEFT:
 			if (lcount == -1)
 				return "--";
 			return new Integer(lcount);
 		case RIGHT:
 			if (rcount == -1)
 				return "--";
 			return new Integer(rcount);
 		default:
 			return " ";
 		}
 	}
 
 	public List<List<Pass>> getPassChains() {
		// System.out.println("getting the pass chains!!");
 		// check if we have already calculated the pass chains
 		if (this.passChains.size() > 0) {
 			// System.out.println("pass chains already calculated!!");
 			return this.passChains;
 		}
 
 		int stime = 0;
 		int etime = WorldModel.getInstance().getSceneSet().lastScene().time;
 		// System.out.println("calculating pass chains!! from " + stime + " to"
 		// + etime);
 		for (int time = stime; time < etime; time++) {
 			switch (PassAnalyzer.getPossessionTeam(time)) {
 			case PassAnalyzer.LEFT_SIDE:
 				// System.out.println("LEFT_SIDE detecting pass chain");
 				time = detectPassChain(Team.LEFT_SIDE, time);
 				break;
 			case PassAnalyzer.RIGHT_SIDE:
 				// System.out.println("LEFT_SIDE detecting pass chain");
 				time = detectPassChain(Team.RIGHT_SIDE, time);
 				break;
 			}
 		}
 
 		return this.passChains;
 	}
 
 	/**
 	 * This method detects a pass chain for the team 'side' starting at time
 	 * 'time' and returns the time just after the pass chain has finished. this
 	 * method automatically adds the detected pass chain to the passChains list
 	 * 
 	 * @param side
 	 *            side to detect the pass chain.
 	 * @param time
 	 *            start time of the pass chain.
 	 * @return time of the last pass in the chain + 1
 	 */
 	private int detectPassChain(int side, int time) {
 		int etime = WorldModel.getInstance().getSceneSet().lastScene().time;
 
 		// System.out.println("detecting pass chain for team:" + side
 		// + " starting @ time:" + time);
 
 		int possession_side = PassAnalyzer.PLAY_OFF;
 		if (side == Team.LEFT_SIDE) {
 			possession_side = PassAnalyzer.LEFT_SIDE;
 		} else if (side == Team.RIGHT_SIDE) {
 			possession_side = PassAnalyzer.RIGHT_SIDE;
 		}
 
 		List<Pass> passChain = new ArrayList<Pass>(20);
 		// iterate this entire pass chain
 		while (PassAnalyzer.getPossessionTeam(time) == possession_side
 				&& time <= etime) {
 			Pass pass = PassAnalyzer.getPass(side, time);
 			if (pass != null) {
 				passChain.add(pass); // pass detected, add it to the pass chain
 				System.out.println("pass detected for team(" + side + ") "
 						+ pass.sender.time + "->" + pass.receiver.time);
 				// update the time to continue detecting the chains from the end
 				// of this pass
 				time = pass.receiver.time;
 			}
 			time++;
 		}// finished detecting a pass chain.
 
 		// note: maybe i should also exclude the chains that only have one
 		// pass?
 		if (passChain.size() >= 3) {
 			this.passChains.add(passChain);
 		}
 
 		System.out
 				.println("detecting pass chain for team:" + side + " end time:"
 						+ time + " number of passes:" + passChain.size());
 
 		return time;
 	}
 
 	public void count(int fromTime, int toTime) {
 		lcount = 0;
 		rcount = 0;
 		for (int i = 0; i < 20; i++) {
 			leftChain[i] = 0;
 			rightChain[i] = 0;
 		}
 
 		for (List<Pass> passChain : this.getPassChains()) {
 			Pass firstPass = passChain.get(0);
 			Pass lastPass = passChain.get(passChain.size() - 1);
 			// check if the pass chain is within the requested bounds...
 			if (fromTime <= firstPass.sender.time
 					&& lastPass.receiver.time <= toTime) {
 				if (firstPass.side == Team.LEFT_SIDE) {
 					// increase the number of chains that have
 					// 'passChain.size()' number of
 					// passes
 					leftChain[passChain.size()]++;
 					if (passChain.size() > lcount) {
 						lcount = passChain.size();
 					}
 				} else if (passChain.get(0).side == Team.RIGHT_SIDE) {
 					rightChain[passChain.size()]++;
 					if (passChain.size() > rcount) {
 						rcount = passChain.size();
 					}
 				}
 			}
 		}
 		if (lcount == 0 || lcount == 1)
 			lcount = -1;
 		if (rcount == 0 || lcount == 1)
 			rcount = -1;
 	}
 
 	public void count_old(int fromTime, int toTime) {
 		lcount = 0;
 		rcount = 0;
 		for (int i = 0; i < 20; i++) {
 			leftChain[i] = 0;
 			rightChain[i] = 0;
 		}
 
 		for (int i = fromTime; i <= toTime; i++) {
 			switch (PassAnalyzer.getPossessionTeam(i)) {
 			case PassAnalyzer.LEFT_SIDE:
 				int left = 0;
 				// iterate this entire pass chain
 				while (PassAnalyzer.getPossessionTeam(i) == PassAnalyzer.LEFT_SIDE
 						&& i <= toTime) {
 					Pass pass = PassAnalyzer.getLeftPass(i);
 					if (pass != null) {
 						left++; // increase the number of passes this chain has.
 						i = pass.receiver.time;
 						if (left > lcount)
 							lcount = left; // biggest pass chain?
 					}
 					i++;
 				}
 				// increase the number of chains that have 'left' number of
 				// passes
 				leftChain[left]++;
 				break;
 
 			// this does the same has above
 			case PassAnalyzer.RIGHT_SIDE:
 				int right = 0;
 				while (PassAnalyzer.getPossessionTeam(i) == PassAnalyzer.RIGHT_SIDE
 						&& i <= toTime) {
 					Pass pass = PassAnalyzer.getRightPass(i);
 					if (pass != null) {
 						right++;
 						i = pass.receiver.time;
 						if (right > rcount)
 							rcount = right;
 					}
 					i++;
 				}
 				rightChain[right]++;
 				break;
 
 			default:
 			}
 		}
 		if (lcount == 0 || lcount == 1)
 			lcount = -1;
 		if (rcount == 0 || lcount == 1)
 			rcount = -1;
 	}
 
 	public GameEvent analyze(Scene scene, Scene prev) {
 		return null;
 	}
 
 	public int getLeftPassChain(int chain) {
 		return leftChain[chain];
 	}
 
 	public int getRightPassChain(int chain) {
 		return rightChain[chain];
 	}
 
 	public TableModel getTableModel() {
 		TableModel dataModel = new AbstractTableModel() {
 			/**
 			 * 
 			 */
 			private static final long serialVersionUID = 1L;
 
 			public String getColumnName(int col) {
 				switch (col) {
 				case 0:
 					return " ";
 				case 1:
 					return "Left Team";
 				case 2:
 					return "Right Team";
 				default:
 					return " ";
 				}
 			}
 
 			public int getColumnCount() {
 				return 3;
 			}
 
 			public int getRowCount() {
 				return 10;
 			}
 
 			public Object getValueAt(int row, int col) {
 				count(0, lastTime);
 				switch (col) {
 				case 0:
 					return new String(row + 2 + " passes");
 				case 1:
 					return new Integer(leftChain[row + 2]);
 				case 2:
 					return new Integer(rightChain[row + 2]);
 				default:
 					return " ";
 				}
 			}
 		};
 
 		return dataModel;
 	}
 
 	@Override
 	public void xmlElement(XMLBuilder builder) {
 		int leftChains = nChains(Team.LEFT_SIDE);
 		int rightChains = nChains(Team.RIGHT_SIDE);
 		XMLBuilder passchains = builder.elem("passchains").attr("left",
 				String.valueOf(leftChains)).attr("right",
 				String.valueOf(rightChains));
 		for (List<Pass> passchain : this.getPassChains()) {
 			int side = passchain.get(0).side;
 			int stime = passchain.get(0).sender.time;
 			int etime = passchain.get(passchain.size() - 1).receiver.time;
 			int size = passchain.size();
 			passchains.elem("passchain").attr("team", Team.name(side)).attr(
 					"start", String.valueOf(stime)).attr("end",
 					String.valueOf(etime)).attr("size", String.valueOf(size));
 		}
 	}
 
 	public int nChains(int side) {
 		int count = 0;
 		for (List<Pass> passchain : this.getPassChains()) {
 			if (passchain.get(0).side == side) {
 				count++;
 			}
 		}
 		return count;
 	}
 }
