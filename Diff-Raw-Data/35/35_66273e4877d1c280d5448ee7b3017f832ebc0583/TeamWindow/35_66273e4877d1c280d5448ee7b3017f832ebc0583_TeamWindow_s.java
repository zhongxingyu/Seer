 package FRC_Score_Sys;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.util.List;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.ScrollPaneConstants;
 
 public class TeamWindow extends JFrame {
 	private static final long	serialVersionUID	= 1L;
 	MainMenu myParent;
 	customTableModel TeamTableModel = new customTableModel();
 	JTable TeamTable = new JTable(TeamTableModel);
 	
 	Color color_red = new Color(255, 106, 0);
 	Color color_blue = new Color(30, 144, 255);
 	Color color_yellow = new Color(242, 255, 0);
 	Color color_green = new Color(0, 255, 0);
 	Color color_orange = new Color(255, 166, 0);
 	
 	public TeamWindow(MainMenu parent, int Team){
 		myParent = parent;
 		setAlwaysOnTop(true);
 		setTitle("Matches for team " + Team);
 		
 		TeamTableModel.addColumn("Type");		// 0
 		TeamTableModel.addColumn("Match #");
 		TeamTableModel.addColumn("Spot");
 		TeamTableModel.addColumn("Played");
 		TeamTableModel.addColumn("Result");		
 		
 		List<SingleMatch> matches = myParent.CommHandle.SqlTalk.FetchTeamMatches(Team);
 		
 		for(SingleMatch m : matches){
 			JLabel T = MatchType(m.MatchTypeOnly());
 			JLabel M = StdLabel(m.MatchNumberOnly(), null);
 			JLabel S = FindSpot(m, Team);
 			JLabel P = BoolLabel(m.Played);
 			JLabel R = Result(m, Team);
 			JLabel[] a = {T, M, S, P, R}; 
 			TeamTableModel.addRow(a);
 		}
 		
 		TableRender newRender = new TableRender();
 		for(int i=0; i<TeamTableModel.getColumnCount(); i++){
 			TeamTable.getColumnModel().getColumn(i).setCellRenderer( newRender );
 		}
 		
 		JScrollPane TeamScroller = new JScrollPane(TeamTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 		
 		this.getContentPane().add(TeamScroller,BorderLayout.CENTER);
 		this.pack();
 	}
 	private JLabel FindSpot(SingleMatch in, int id){
 		String str = "";
 		Color clr = null;
		if(in.Robot1 == id) str = in.aColor()+"1";
		if(in.Robot2 == id) str = in.aColor()+"2";
		if(in.Robot3 == id) str = in.aColor()+"3";
 		if(in.aColor()=="R") clr = color_red;
 		if(in.aColor()=="B") clr = color_blue;
 		return StdLabel(str, clr);
 	}
 	
 	private JLabel MatchType(String mType){
 		String str = "";
 		switch(mType){
 			case "QQ": str = "QUALs"; break;
 			case "QF": str = "QUARTERs"; break;
 			case "SF": str = "SEMIs"; break;
 			case "FF": str = "FINALs"; break;
 			default: str = "UNK: "+mType; break;
 		}
 		return StdLabel(str, null);
 	}
 	
 	private JLabel Result(SingleMatch in, int id){
 		String txt = "";
 		
 		// find the spot so we can check if they are DQed
 		boolean dq = false;
 		if(in.Robot1 == id) dq = in.Dq1;
 		if(in.Robot2 == id) dq = in.Dq2;
 		if(in.Robot3 == id) dq = in.Dq3;
 		Color clr = null;
 		if(!in.Played){
 			txt = "QUEUE";
 		} else {
 			if(dq){
 				txt = "DQed!";
 				clr = color_orange;
 			} else {
 				switch(in.QS){
 					case 2:
 						txt = "WIN";
 						if(in.aColor()=="B"){
 							clr = color_blue;
 						} else {
 							clr = color_red;
 						}
 						break;
 					case 1:
 						txt = "TIE";
 						clr = color_yellow;
 						break;
 					case 0:
 						txt = "LOSS";
 						if(in.aColor()=="B"){
 							clr = color_red;
 						} else {
 							clr = color_blue;
 						}
 						break;
 					default:
 						txt = "WHAT? "+in.QS;
 						clr = null;
 				}
 			}
 		}
 		return StdLabel(txt, clr);
 	}
 	private JLabel BoolLabel(boolean boolval){
 		Color clr = color_red;
 		String str = "NO";
 		if(boolval) {
 			clr = color_green;
 			str = "YES"; 
 		}
 		return StdLabel(str, clr);
 	}
 	private JLabel StdLabel(int intval, Color inColor){
 		return StdLabel(String.valueOf(intval), inColor);
 	}
 	private JLabel StdLabel(String str, Color inColor){
 		JLabel ret = new JLabel("");
 		if(inColor != null) ret.setBackground(inColor);
 		ret.setOpaque(true);
 		ret.setText(str);
 		ret.setHorizontalAlignment(JLabel.CENTER);
 		return ret;
 	}
 }
