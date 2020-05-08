 package FRC_Score_Sys.InputWindow;
 
 import java.awt.Color;
 import java.awt.GridLayout;
 
 import javax.swing.BorderFactory;
 import javax.swing.JPanel;
 
 import FRC_Score_Sys.SingleMatch;
 
 public class ScorePanel extends JPanel {
 	/**
 	 * 
 	 */
 	private InputWindow myParent;
 	private static final long serialVersionUID = 1;
 	// Standard Objects
 	private int TotalScore = 0;
 	private int FinalScore = 0;
 
 	private String matchID;
 	private String myColor;
 
 	public int my_QS = 0;
 	
 	// Input Objects
 	SingleScoreRow Low;
 	SingleScoreRow Mid;
 	SingleScoreRow Hig;
 	SingleScoreRow Pyr;
 
 	OptRow R1;
 	OptRow R2;
 	OptRow R3;
 
 	PenaltyInput PenRow;
 
 	PanelTotal TotPanel;
 
 	public ScorePanel(InputWindow parent, Color new_color, SingleMatch Match) {
 		myParent = parent;
 		setLayout(new GridLayout(0, 1, 0, 0));
 		setBackground(new_color);
 
 		setBorder(BorderFactory.createLineBorder(Color.BLACK));
 		// Header 1
 		String[] heads1 = { "", "Auto", "Tele", "Total" };
 		SectionHeader Sect1 = new SectionHeader("Disk Points", heads1);
 		this.add(Sect1);
 		matchID = Match.MatchID();
 		myColor = Match.aColor();
 
 		// Score Rows
 		Low = new SingleScoreRow(this, "Low", 1, true, Match.DisksLA, Match.DisksLT);
 		Mid = new SingleScoreRow(this, "Mid", 2, true, Match.DisksMA, Match.DisksMT);
 		Hig = new SingleScoreRow(this, "High", 3, true, Match.DisksHA, Match.DisksHT);
 		Pyr = new SingleScoreRow(this, "Pyramid", 5, false, 0, Match.DisksP);
 
 		Low.setBackground(new_color);
 		Mid.setBackground(new_color);
 		Hig.setBackground(new_color);
 		Pyr.setBackground(new_color);
 
 		this.add(Low);
 		this.add(Mid);
 		this.add(Hig);
 		this.add(Pyr);
 		// DQ/Climb Rows
 		// Header 2
 		String[] heads2 = { "Robot", "Climb", "DQ'ed?" };
 		SectionHeader Sect2 = new SectionHeader("Robot Options", heads2);
 		this.add(Sect2);
 
		R1 = new OptRow(this, String.valueOf(Match.Robot1), Match.Climb1, Match.Dq1);
		R2 = new OptRow(this, String.valueOf(Match.Robot2), Match.Climb2, Match.Dq2);
		R3 = new OptRow(this, String.valueOf(Match.Robot3), Match.Climb3, Match.Dq3);
 		R1.setBackground(new_color);
 		R2.setBackground(new_color);
 		R3.setBackground(new_color);
 
 		this.add(R1);
 		this.add(R2);
 		
 		if(Match.Robot3 != -1){
 			this.add(R3);
 		}
 
 		// Penalties Row
 		// Header 3
 		String[] heads3 = { "Fouls", "T-Fouls" };
 		SectionHeader Sect3 = new SectionHeader("Penalties", heads3);
 		this.add(Sect3);
 		PenRow = new PenaltyInput(this, Match.Foul, Match.TFoul);
 		this.add(PenRow);
 		// Total Rows
 		// Header 4
 		String[] heads4 = { "Score", "Fouls", "Final" };
 		SectionHeader Sect4 = new SectionHeader("Score Roundup", heads4);
 		this.add(Sect4);
 		TotPanel = new PanelTotal();
 		this.add(TotPanel);
 		FinalScore = TotPanel.GetFinal(TotalScore, 0);
 	}
 
 	public int DoRefresh(int Penalties) {
 		TotalScore = Low.GetScore(3) + Mid.GetScore(3) + Hig.GetScore(3) + Pyr.GetScore(3) + R1.GetClimb() + R2.GetClimb() + R3.GetClimb();
 		FinalScore = TotPanel.GetFinal(TotalScore, Penalties);
 		return FinalScore;
 	}
 
 	public void ForwardRefresh() {
 		myParent.DoCalc();
 	}
 
 	public int GetFinalScore() {
 		return FinalScore;
 	}
 
 	public int GetPenalties() {
 		return PenRow.GetPenalties();
 	}
 
 	public SingleMatch GetRawData() {
 		SingleMatch A = new SingleMatch(matchID, myColor);
 		A.DisksLA = Low.GetAutoCount();
 		A.DisksLT = Low.GetTeleCount();
 		A.DisksMA = Mid.GetAutoCount();
 		A.DisksMT = Mid.GetTeleCount();
 		A.DisksHA = Hig.GetAutoCount();
 		A.DisksHT = Hig.GetTeleCount();
 		A.DisksP = Pyr.GetTeleCount();
 		A.Climb1 = R1.GetRawClimb();
 		A.Climb2 = R2.GetRawClimb();
 		A.Climb3 = R3.GetRawClimb();
 		A.Dq1 = R1.isDQ();
 		A.Dq2 = R2.isDQ();
 		A.Dq3 = R3.isDQ();
 		A.Foul = PenRow.GetFoulCount();
 		A.TFoul = PenRow.GetTFoulCount();
 		
 		A.QS = my_QS;
 		
 		A.AP = Low.GetScore(1) + Mid.GetScore(1) + Hig.GetScore(1) + Pyr.GetScore(1);
 		A.TP = Low.GetScore(2) + Mid.GetScore(2) + Hig.GetScore(2) + Pyr.GetScore(2);
 		A.CP = R1.GetClimb() + R2.GetClimb() + R3.GetClimb();
 		
 		A.Score = FinalScore;
 		return A;
 	}
 
 	public void RequestReset() {
 		// TODO: This is where the Panel goes wrong.
 		Low.RequestReset();
 		Mid.RequestReset();
 		Hig.RequestReset();
 		Pyr.RequestReset();
 		R1.RequestReset();
 		R2.RequestReset();
 		R3.RequestReset();
 		PenRow.RequestReset();
 	}
 }
