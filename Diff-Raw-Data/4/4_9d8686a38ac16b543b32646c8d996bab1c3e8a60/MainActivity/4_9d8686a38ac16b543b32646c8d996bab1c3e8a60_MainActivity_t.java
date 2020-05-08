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
 
 import ch.laurent.helpers.TeamNameHelper;
 import ch.laurent.scoreManager.ScoreGraph;
 import ch.laurent.scoreManager.ScoreStack;
 
 import com.actionbarsherlock.app.SherlockActivity;
 
 
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.RatingBar;
 import android.widget.RatingBar.OnRatingBarChangeListener;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 
 // TODO: Forcer le choix du rating apres chaque saisie
 public class MainActivity extends SherlockActivity implements
 		OnRatingBarChangeListener {
 	// Valeurs par defaut
 	private final static int sInitRating = 1;
 	private final static int sAllAssetRating = 6;
 	// Position de l'element XX dans le menu (0..n)
 	private final static int sMenuCancelPosition = 0;
 	private final static int sMenuResetPosition = sMenuCancelPosition + 1;
 
 	// Noms des champs de sauvegarde
 	private final static String sSaveScoreStack = "scoreStack";
 	private final static String sSaveRating = "rating";
 
 	// Nombre de points en cas de match
 	private final static int sBonus = 100;
 	private final static int sMatch = 157;
 	private final static int sMatchAllAsset = 253;
 
 	// Score des equipes
 	private ScoreStack mScore = new ScoreStack();
 
 	// Graph des scores
 	private ScoreGraph mGraph;
 	// Coefficient de la partie (P.ex: 2x pour pique double)
 	private int mCoefficient = 1;
 
 	// Elements de controles
 	private RatingBar mRatingBar;
 	private EditText mInputScore1;
 	private EditText mInputScore2;
 
 	/**
 	 * Lorsque l'activite est cree.
 	 */
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		// Gere la bare de "rating"
 		mRatingBar = (RatingBar) findViewById(R.id.multiply);
 		mRatingBar.setOnRatingBarChangeListener(this);
 		mRatingBar.setRating(sInitRating);
 
 		// Recupere les champs de saisie
 		mInputScore1 = (EditText) findViewById(R.id.input_score1);
 		mInputScore2 = (EditText) findViewById(R.id.input_score2);
 
 		// Verification des saisies a la volee
 		mInputScore1.addTextChangedListener(new TextWatcherAdapter());
 		mInputScore2.addTextChangedListener(new TextWatcherAdapter());
 		textChangedListener();
 
 		// Graphique
 		mGraph = new ScoreGraph(this, mScore) {
 			public void update(Observable observable, Object data) {
 				// Lorsque le score change, on met a jour
 				// le score affiche et le graphique
 				graphUpdate();
 				displayScore();
 			}
 		};
 		// Initialement, on met a jour les infos (graph,score)
 		graphUpdate();
 		displayScore();
 	}
 
 	/**
 	 * Mise a jour du graphique
 	 */
 	public void graphUpdate() {
 		// Mise a jour de l'actionBar
 		supportInvalidateOptionsMenu();
 
 		// Recupere le layout du graphique
 		LinearLayout layout = (LinearLayout) findViewById(R.id.layoutGraph);
 
 		// Supprime le graphe
 		layout.removeAllViews();
 		// Ajoute le graphe, si on a un score a afficher
 		if (mScore.isCancellable()) {
 			layout.addView(mGraph.getView());
 		}
 	}
 
 	/**
 	 * Lorsque l'application est interompue, on sauve le score
 	 */
 	public void onPause() {
 		super.onPause();
 
 		SharedPreferences sharedPrefs = PreferenceManager
 				.getDefaultSharedPreferences(getApplicationContext());
 		SharedPreferences.Editor ed = sharedPrefs.edit();
 		// Valeurs a sauver
 		ed.putString(sSaveScoreStack, mScore.saveAsString());
 		ed.putFloat(sSaveRating, mRatingBar.getRating());
 		// Sauve
 		ed.commit();
 	}
 
 	/**
 	 * Lorsque l'application est reprise, on restaure le score
 	 */
 	public void onResume() {
 		super.onResume();
 
 		SharedPreferences sharedPrefs = PreferenceManager
 				.getDefaultSharedPreferences(getApplicationContext());
 		// Valeur a restaurer
 		// 0,0 est equivalent a new ScoreStack().saveAsString();
 		// il s'agit de la valeur par defaut
 		String temp = sharedPrefs.getString(sSaveScoreStack, "0,0");
 		// La pile est remplacee
 		mScore.restoreFromString(temp);
 		mRatingBar.setRating(sharedPrefs.getFloat(sSaveRating, sInitRating));
 
 		// Raffraichit la vue
 		graphUpdate();
 		textChangedListener();
 	}
 
 	/**
 	 * Cree le menu contextuel
 	 * 
 	 * @param le menu
 	 * @return true;
 	 */
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getSupportMenuInflater().inflate(R.menu.activity_main, menu);
 
 		return true;
 	}
 
 	/**
 	 * Active ou desactive les options du menu a la volee
 	 */
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		final boolean enable = mScore.isCancellable();
 		// Les options Annuler et Reset sont active que si l'on a un score.
 		menu.getItem(sMenuCancelPosition).setEnabled(enable).setVisible(enable);
 		menu.getItem(sMenuResetPosition).setEnabled(enable).setVisible(enable);
 		return true;
 	}
 
 	/**
 	 * Lors de la selection d'un element du menu
 	 * 
 	 * @param item L'element selectionne
 	 */
 	public boolean onOptionsItemSelected(MenuItem item) {
 		final int id = item.getItemId();
 		// Reset
 		if(id == R.id.menu_reset){
 			// Confirmation pour reset()
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage(getString(R.string.score_reset_confirm))
 					.setCancelable(true)
 					.setTitle(getString(R.string.score_reset_confirm_title))
 					.setPositiveButton(getString(android.R.string.yes),
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int id) {
 									// Reset
 									reset();
 									dialog.cancel();
 								}
 							})
 					.setNegativeButton(getString(android.R.string.no),
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int id) {
 									// Annule le reset
 									dialog.cancel();
 								}
 							});
 			builder.create().show();
 			return true;
 		}else if(id == R.id.menu_settings){
 			// Lance les parametres
 			startActivity(new Intent(this, SettingsActivity.class));
 			return true;
 		}else if (id == R.id.menu_cancel){
 			// Annule la derniere saisie
 			mScore.cancel();
 			return true;
 		}
 		return false;
 		
 	}
 
 	/**
 	 * Appelee lorsqu'on change le type de jeu. Modifie le coefficient des
 	 * points et mets a jour la legende
 	 * 
 	 * @Override
 	 * @param ratinBar La barre de rating
 	 * @param rating La valeur de la barre de rating
 	 * @param fromUser Indique si la mise a jour est faite par l'utilisateur
 	 */
 	public void onRatingChanged(RatingBar ratingBar, float rating,
 			boolean fromUser) {
 		final TextView txt_legend = (TextView) findViewById(R.id.multiplyLegend);
 		final String[] legends = getResources().getStringArray(
 				R.array.type_array);
 		// Index du coefficient suivant le rating
 		int i = (int) rating - 1;
 		// Force un rating a une etoile minimum
 		if (i < 0) {
 			i = 0;
 			ratingBar.setRating(1);
 		}
 		// Le coefficient se definit suivant le nombre d'etoiles.
 		// Voir dans res/values/integers.xml,
 		mCoefficient = getResources().getIntArray(R.array.coeff)[i];
 		txt_legend.setText(legends[i] + " x" + mCoefficient);
 	}
 
 	/**
 	 * Ajoute des points aux equipes. Passer toutes les valeurs en x1 et ajuster
 	 * le coefficient si besoin
 	 * 
 	 * @param team L'ID de l'equipe
 	 * @param point Points de l'equipe
 	 * @param max Nombre de points maximum de la partie (sans le bonus de match)
 	 * @param coeff Coefficient de la partie (P.ex 2 pour pique double)
 	 */
 	private void addPoints(final int team, int point, int max, final int coeff) {
 		// Multiplie les points suivant le coeficient
 		max *= coeff;
 		point *= coeff;
 
 		// Le reste est pour l'autre equipe
 		int rest = max - point;
 		// Si le reste est negatif (on a un match)
 		if (rest < 0) {
 			rest = 0;
 		}
 		// Si le score est de 0, l'autre equipe a un match
 		if (point == 0) {
 			rest = max + sBonus * coeff;
 		}
 		// Si on a saisit le score de l'equipe 1, l'equipe 2 a le reste
 		if (team == 1) {
 			mScore.add(point, rest);
 		} else { // Et vice-versa
 			mScore.add(rest, point);
 		}
 	}
 
 	/**
 	 * Efface le score et le type de jeu
 	 */
 	private void reset() {
 		mRatingBar.setRating(sInitRating);
 		mScore.reset();
 	}
 
 	/**
 	 * Affiche le score des deux equipes, avec le nom tire des preferences.
 	 */
 	private void displayScore() {
 		final Resources r = getResources();
 
 		int id_label, points;
 
 		// Iterate for team 1 and 2
 		for (int team = 1; team <= 2; team++) {
 			// Score
 			points = mScore.getScore(team);
 
 			// TextView
 			id_label = (team == 1 ? R.id.team1 : R.id.team2);
 
 			// Set score for current team.
 			TextView txt = (TextView) findViewById(id_label);
 			String displayScore = TeamNameHelper.getTeamName(
 					this, team) + " : ";
 			displayScore += r.getQuantityString(R.plurals.points, points,
 					points);
 			txt.setText(displayScore);
 		}
 	}
 
 	/**
 	 * Action lorsqu'on clique sur un des boutons "Annonce" ou "Calculer.."
 	 * 
 	 * @param v Le bouton clique
 	 */
 	public void onClick(View v) {
 		// Recuperer les points de l'equipe
 		int point = 0;
 		// Choix du champ 1 ou 2.
 		final int team = (!mInputScore1.getText().toString().equals("") ? 1 : 2);
 		final EditText input = (team == 1 ? mInputScore1 : mInputScore2);
 		// Parse la saisie en int
 		try {
 			point = Integer.parseInt(input.getText().toString());
 		} catch (NumberFormatException e) {
 			// Erreur (overflow,.. etc)
 			Toast.makeText(getApplicationContext(),
 					getString(R.string.error_format), Toast.LENGTH_LONG).show();
 			mInputScore1.setText("");
 			mInputScore2.setText("");
 			return;
 		}
 
 		/* calculs les points de l'equipe */
 		// Regarde si on a tout atout
 		final boolean isAllAsset = mRatingBar.getRating() == sAllAssetRating;
 
 		// Points maximum
 		// En tout atout, la partie vaut plus de points
 		final int max = (isAllAsset ? sMatchAllAsset : sMatch);
 
 		// Erreur, les points sont trop eleves (accepte le bonus de match)
 		if (v.getId() == R.id.btn_score && point > max && point != max + sBonus) {
 
 			Toast.makeText(getApplicationContext(),
 					getString(R.string.error_high_value), Toast.LENGTH_LONG)
 					.show();
 			return;
 		}
 
 		// Plus d'erreur,... on se charge des saisies
 		mInputScore1.setText("");
 		mInputScore2.setText("");
 
 		/* Ajoute les points comme une annonce */
 		if (v.getId() == R.id.btn_announcement) {
 			mScore.addAnnounce(team, point * mCoefficient);
 			return;
 		}
 		/* Ajoute les points comme score */
 		addPoints(team, point, max, mCoefficient);
 	}
 
 	/**
 	 * Gere les boutons lors de la saisie. (Couleur, Inhibation) S'assure que
 	 * les saisies de score peuvent se faire pour l'equipe 1 ou l'equipe 2.
 	 * Change la couleur du bouton "Annonce" suivant l'equipe.
 	 */
 	public void textChangedListener() {
 		// Si les inputs 1 et 2 sont vides
		final boolean isEmpty1 = mInputScore1.getText().toString().trim().equals("");
		final boolean isEmpty2 = mInputScore2.getText().toString().trim().equals("");
 		// Recupere les boutons
 		final Button btn_score = (Button) findViewById(R.id.btn_score);
 		final Button btn_announcement = (Button) findViewById(R.id.btn_announcement);
 
 		// Active les boutons
 		btn_score.setEnabled(true);
 		btn_announcement.setEnabled(true);
 
 		// Style des boutons par default
 		int style_an = (isEmpty2 ? R.style.Team1 : R.style.Team2);
 		int style_score = R.style.TeamAll;
 
 		// Si les 2 champs de saisie sont vide ou pleins,
 		// on desactive les boutons
 		if ((isEmpty1 && isEmpty2) || (!isEmpty1 && !isEmpty2)) {
 			btn_score.setEnabled(false);
 			btn_announcement.setEnabled(false);
 			style_an = style_score = R.style.TeamNone;
 		}
 		// Active la saisie du score pour une seule equipe a la fois
 		mInputScore1.setEnabled(isEmpty2);
 		mInputScore2.setEnabled(isEmpty1);
 
 		// Applique le style sur les boutons
 		btn_score.setTextAppearance(getApplicationContext(), style_score);
 		btn_announcement.setTextAppearance(getApplicationContext(), style_an);
 
 	}
 
 	/**
 	 * Lors de la saisie des scores, on verifie les boutons a activer.
 	 */
 	class TextWatcherAdapter implements TextWatcher {
 
 		@Override
 		public void afterTextChanged(Editable arg0) {
 			textChangedListener();
 		}
 
 		@Override
 		public void beforeTextChanged(CharSequence s, int start, int count,
 				int after) {
 		}
 
 		@Override
 		public void onTextChanged(CharSequence s, int start, int before,
 				int count) {
 		}
 	}
 }
