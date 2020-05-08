 package final_project.view;
 
 import java.awt.*;
 
 import javax.swing.*;
 
 import final_project.control.*;
 import final_project.model.*;
 
 public class ScoreView extends JPanel {
 	TournamentController tournament;
     public final String player1Name, player2Name;
 	/**
 	 * Create the panel.
 	 * @wbp.parser.constructor
 	 */
 	private ScoreView(TournamentController tournament, PlayerResult player1, PlayerResult player2) {
 		this.tournament = tournament;
 		player1Name = tournament.getNameFromId(player1.getPlayerId());
 		player2Name = tournament.getNameFromId(player2.getPlayerId());
 		int player1Score = player1.getPlayerScore();
 		int player2Score = player2.getPlayerScore();
 		
 		setBackground(Color.BLACK);
 		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
 		gbc_panel_1.anchor = GridBagConstraints.NORTHWEST;
 		gbc_panel_1.insets = new Insets(0, 0, 0, 5);
 		gbc_panel_1.fill = GridBagConstraints.HORIZONTAL;
 		gbc_panel_1.gridx = 1;
 		gbc_panel_1.gridy = 0;
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[]{200, 30, 30, 30, 200, 0};
 		gridBagLayout.rowHeights = new int[]{19, 0};
 		gridBagLayout.columnWeights = new double[]{1.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
 		gridBagLayout.rowWeights = new double[]{0.0, Double.MIN_VALUE};
 		setLayout(gridBagLayout);
 		
 		JLabel player1NameLabel = new JLabel(player1Name);
 		GridBagConstraints gbc_player1NameLabel = new GridBagConstraints();
 		gbc_player1NameLabel.anchor = GridBagConstraints.EAST;
 		gbc_player1NameLabel.insets = new Insets(0, 0, 0, 5);
 		gbc_player1NameLabel.gridx = 0;
 		gbc_player1NameLabel.gridy = 0;
 		add(player1NameLabel, gbc_player1NameLabel);
		player1NameLabel.setFont(new Font("Score Board", Font.PLAIN, 18));
 		player1NameLabel.setForeground(Color.WHITE);
 		
 		JLabel player1ScoreLabel = new JLabel("");
 		player1ScoreLabel.setForeground(Color.CYAN);
 		player1ScoreLabel.setFont(new Font("Score Board", Font.PLAIN, 16));
 		if (player1Score != -1)
 			player1ScoreLabel.setText("" + player1Score);
 		GridBagConstraints gbc_player1ScoreLabel = new GridBagConstraints();
 		gbc_player1ScoreLabel.insets = new Insets(0, 0, 0, 5);
 		gbc_player1ScoreLabel.gridx = 1;
 		gbc_player1ScoreLabel.gridy = 0;
 		add(player1ScoreLabel, gbc_player1ScoreLabel);
 		
 		JLabel vs = new JLabel("vs");
 		GridBagConstraints gbc_vs = new GridBagConstraints();
 		gbc_vs.insets = new Insets(0, 0, 0, 5);
 		gbc_vs.gridx = 2;
 		gbc_vs.gridy = 0;
 		add(vs, gbc_vs);
 		vs.setFont(new Font("Score Board", Font.PLAIN, 16));
 		vs.setForeground(Color.RED);
 		
 		JLabel player2ScoreLabel = new JLabel("");
 		player2ScoreLabel.setForeground(Color.ORANGE);
 		player2ScoreLabel.setFont(new Font("Score Board", Font.PLAIN, 16));
 		if (player2Score != -1)
 			player2ScoreLabel.setText("" + player2Score);
 		GridBagConstraints gbc_player2ScoreLabel1 = new GridBagConstraints();
 		gbc_player2ScoreLabel1.insets = new Insets(0, 0, 0, 5);
 		gbc_player2ScoreLabel1.gridx = 3;
 		gbc_player2ScoreLabel1.gridy = 0;
 		add(player2ScoreLabel, gbc_player2ScoreLabel1);
 		
 		JLabel player2NameLabel = new JLabel(player2Name);
 		player2NameLabel.setForeground(Color.WHITE);
		player2NameLabel.setFont(new Font("Score Board", Font.PLAIN, 16));
 		GridBagConstraints gbc_player2NameLabel = new GridBagConstraints();
 		gbc_player2NameLabel.anchor = GridBagConstraints.WEST;
 		gbc_player2NameLabel.gridx = 4;
 		gbc_player2NameLabel.gridy = 0;
 		add(player2NameLabel, gbc_player2NameLabel);
 	}
 
 	public ScoreView(TournamentController tournament, IncompleteResult result) {
 		this(tournament, new PlayerResult(result.getPlayer1(), -1), new PlayerResult(result.getPlayer2(), -1));
 	}
 	
 	public ScoreView(TournamentController tournament, CompleteResult result) {
 		this(tournament, new PlayerResult(result.getWinner(), result.getWinnerScore()), new PlayerResult(result.getLoser(), result.getLoserScore()));
 	}
 }
