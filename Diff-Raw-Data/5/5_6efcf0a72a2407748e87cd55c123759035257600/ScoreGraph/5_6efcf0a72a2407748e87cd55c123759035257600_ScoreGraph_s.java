 /* LICENSE
  * This work is licensed under the 
  * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License. 
  * To view a copy of this license, visit 
  * http://creativecommons.org/licenses/by-nc-sa/3.0/.
  * 
  * Copyright (c) 2013 by Laurent Constantin <constantin.laurent@gmail.com>
  */
 package ch.laurent.chibre;
 
 import java.util.Observable;
 import java.util.Observer;
 
 import android.view.View;
 
 import ch.laurent.chibre.R;
 import ch.laurent.chibre.ScoreStack.Score;
 
 import com.jjoe64.graphview.GraphView;
 import com.jjoe64.graphview.GraphViewSeries;
 import com.jjoe64.graphview.GraphViewSeries.*;
 
 import com.jjoe64.graphview.LineGraphView;
 import com.jjoe64.graphview.GraphView.GraphViewData;
 
 /**
  * Affiche le score des parties dans un graphique Le score est represente par
  * ScoreStack
  * 
  * @author Laurent Constantin
  * @see https://github.com/jjoe64/GraphView/
  */
 public abstract class ScoreGraph implements Observer {
 	// Le graphique
 	final GraphView graphView;
 	// La pile de score
 	ScoreStack scoreStack;
 	// Le nombre de series presents dans le graphe (2 ou 0)
 	int nbSeries = 0;
 
 	// Couleur des equipes
 	final int TEAM1_COLOR;
 	final int TEAM2_COLOR;
 	// Legendes du graphes
 	final String LEGEND;
 	final String[] TEAM_NAME;
 
 	/**
 	 * Instancie le graphe
 	 * 
 	 * @param main L'activite Main
 	 * @param scoreStack Le score
 	 */
 	public ScoreGraph(MainActivity main, ScoreStack scoreStack) {
 		LEGEND = "";//main.getString(R.string.score_graph_legend);
 
 		graphView = new LineGraphView(main.getBaseContext(), LEGEND);
 		TEAM_NAME = new String[] { main.getTeamName(1), main.getTeamName(2) };
 
 		TEAM1_COLOR = main.getResources().getColor(R.color.red);
 		TEAM2_COLOR = main.getResources().getColor(R.color.blue);
 
 		setStack(scoreStack);
 
 	}
 
 	/**
 	 * Change le ScoreStack du graph
 	 * 
 	 * @param scoreStack
 	 */
 	public void setStack(ScoreStack scoreStack) {
 		// Supprime l'observateur
 		if (this.scoreStack != null) {
 			this.scoreStack.deleteObserver(this);
 		}
 		// Ajout du nouvel observateur
 		this.scoreStack = scoreStack;
 		this.scoreStack.addObserver(this);
 	}
 
 	/**
 	 * Genere le graph
 	 */
 	private void generate() {
 		if (scoreStack == null)
 			return;
 		// Efface les series precedentes
 		while (nbSeries > 0) {
 			graphView.removeSeries(0);
 			nbSeries--;
 		}
 		// Boucle pour afficher les trais de chaque equipe
 		int topScore = 0;
 		for (int team = 1; team < 3; team++) {
 			final int color = (team == 1 ? TEAM1_COLOR : TEAM2_COLOR);
 
 			int i = 0;
 			// Cree les tableaux
 			GraphViewData gfd[] = new GraphViewData[scoreStack.getStack()
 					.size()];
 			String[] gfdString = new String[gfd.length];
 			// On boucle sur la pile des scores
 			for (Score s : scoreStack.getStack()) {
 
 				final int score = (team == 1 ? s.score1 : s.score2);
 				// Sauve le score max pour la legende verticale
 				if (score > topScore)
 					topScore = score;
 				// Insertion des points dans le graph
 				gfd[i] = new GraphViewData(i + 1, score);
 				// Legende horizontales vides
 				gfdString[i] = "";
 				i++;
 			}
 			// Cree les legendes verticales [0....MAX]
			/*String[] verticalString = (String[]) gfdString.clone();
			verticalString[0] = topScore + "";
			verticalString[verticalString.length - 1] = "0";*/
			String[] verticalString = new String[] { topScore + "","","0" };
 			// Applique les legendess
 			graphView.setVerticalLabels(verticalString);
 			graphView.setHorizontalLabels(gfdString);
 			// Ajoute les points dans le graph pour l'equipe courante
 			graphView.addSeries(new GraphViewSeries(TEAM_NAME[team - 1],
 					new GraphViewStyle(color, 3), gfd));
 			// Il y a une serie de plus dans le graphe
 			nbSeries++;
 		}
 	}
 
 	/**
 	 * Calcule et renvoie la vue du graph
 	 * 
 	 * @return Le graphe
 	 */
 	public View getView() {
 		generate();
 		return graphView;
 	}
 
 	/**
 	 * Appele lorsque les scores changent
 	 */
 	public abstract void update(Observable observable, Object data);
 
 }
