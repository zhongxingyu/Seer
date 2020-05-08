 /*
  * 
  */
 package org.opendarts.ui.x01.defi.service.comp;
 
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.Date;
 
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.forms.widgets.Section;
 import org.opendarts.core.model.game.IGame;
 import org.opendarts.core.model.player.IPlayer;
 import org.opendarts.core.stats.model.IElementStats;
 import org.opendarts.core.stats.model.IStatValue;
 import org.opendarts.core.stats.model.IStats;
 import org.opendarts.core.stats.model.IStatsEntry;
 import org.opendarts.core.stats.service.IStatsService;
 import org.opendarts.core.x01.defi.OpenDartsX01DefiBundle;
 import org.opendarts.core.x01.defi.model.GameX01Defi;
 import org.opendarts.core.x01.defi.model.GameX01DefiDefinition;
 import org.opendarts.core.x01.defi.service.StatsX01DefiService;
 import org.opendarts.ui.utils.OpenDartsFormsToolkit;
 import org.opendarts.ui.x01.utils.comp.GameDetailComposite;
 
 /**
  * The Class SessionDetailComposite.
  */
 public class GameDefiDetailComposite extends Composite {
 	private final GameX01Defi game;
 	private final OpenDartsFormsToolkit toolkit;
 	private final IStatsService statsService;
 
 	/**
 	 * Instantiates a new session detail composite.
 	 *
 	 * @param parent the parent
 	 * @param game2 
 	 */
 	public GameDefiDetailComposite(Composite parent, IGame game) {
 		super(parent, SWT.NONE);
 		this.toolkit = OpenDartsFormsToolkit.getToolkit();
 		this.game = (GameX01Defi) game;
 		this.statsService = OpenDartsX01DefiBundle.getStatsService(this.game);
 		GridLayoutFactory.fillDefaults().applyTo(this);
 		GridDataFactory.fillDefaults().grab(true, true).applyTo(this);
 		this.createContents();
 	}
 
 	/**
 	 * Creates the contents.
 	 */
 	private void createContents() {
 		// Defi info
 		Composite cmpDetail = this.toolkit.createComposite(this);
 		GridDataFactory.fillDefaults().grab(true, false);
 		GridLayoutFactory.fillDefaults().applyTo(cmpDetail);
 
 		// Defi Detail
 		this.createDefiStats(cmpDetail, null);
 
 		// Players Detail
		for (IPlayer player : this.game.getPlayers()) {
 			this.createPlayerDetail(cmpDetail,player);	
 		}
 
 		// Classic Detail
 		this.createClassicDetail(cmpDetail);
 	}
 
 	/**
 	 * Creates the player detail.
 	 *
 	 * @param parent the parent
 	 * @param player the player
 	 */
 	private void createPlayerDetail(Composite parent, IPlayer player) {
 		Section section = this.toolkit.createSection(parent,
 				Section.DESCRIPTION);
 		GridDataFactory.fillDefaults().grab(true, true).applyTo(section);
 		if (player!=null) {
 			section.setText(player.getName());
 		} else {
 
 			section.setText("Defi Statistics");
 		}
 		section.marginWidth = 10;
 
 		this.toolkit.createCompositeSeparator(section);
 
 		Composite client = this.toolkit.createComposite(section, SWT.WRAP);
 		GridLayoutFactory.fillDefaults().applyTo(client);
 
 		// Create client
 		this.createDefiStats(client, player);
 
 		// End Section
 		this.toolkit.paintBordersFor(client);
 		section.setClient(client);}
 
 	/**
 	 * Creates the classic detail.
 	 *
 	 * @param parent the parent
 	 */
 	private void createClassicDetail(Composite parent) {
 		// Classic detail
 		Label label = toolkit.createSeparator(this, SWT.HORIZONTAL);
 		GridDataFactory.fillDefaults().grab(true, false).applyTo(label);
 
 		Composite cmpTable = this.toolkit.createComposite(parent);
 		GridLayoutFactory.fillDefaults().applyTo(cmpTable);
 		GridDataFactory.fillDefaults().applyTo(cmpTable);
 		new GameDetailComposite(cmpTable, this.game);
 	}
 
 	/**
 	 * Creates the defi stats.
 	 *
 	 * @param client the client
 	 */
 	private void createDefiStats(Composite client, IPlayer player) {
 		IStats<IGame> stats = null;
 		if (player !=null) {
 			IElementStats<IGame> gameStats = statsService.getGameStats(this.game);
 			stats = gameStats.getPlayerStats(player);
 		}
 		
 		int scoreDone = this.getScore(stats);
 		int nbDarts = this.getNbDarts(stats);
 		double duration = this.getDuration(stats);
 		double nbThrows = this.getNbThrow(stats);
 
 		Composite cmpDetail = this.toolkit.createComposite(client);
 		GridLayoutFactory.fillDefaults().numColumns(4).applyTo(cmpDetail);
 		GridDataFactory.fillDefaults().grab(true, true).applyTo(cmpDetail);
 
 		Label label;
 
 		// Average
 		label = this.toolkit.createLabel(cmpDetail, "Average score:");
 		GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER)
 				.applyTo(label);
 
 		Text txtAverage = this.toolkit.createText(cmpDetail,
 				this.getAverage(scoreDone, nbDarts));
 		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtAverage);
 
 		// point by Seconds
 		label = this.toolkit.createLabel(cmpDetail, "Point by Seconds:");
 		GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER)
 				.applyTo(label);
 
 		Text txtPointsBySeconds = this.toolkit.createText(cmpDetail,
 				this.getPointsBySecond(scoreDone, duration));
 		GridDataFactory.fillDefaults().grab(true, false)
 				.applyTo(txtPointsBySeconds);
 
 		// nb throw
 		label = this.toolkit.createLabel(cmpDetail, "Nb Throws:");
 		GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER)
 				.applyTo(label);
 
 		Text txtNbThrow = this.toolkit.createText(cmpDetail,
 				this.getNbThrow(nbThrows));
 		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtNbThrow);
 
 		// seconds by throw
 		label = this.toolkit.createLabel(cmpDetail, "Seconds by Throw:");
 		GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER)
 				.applyTo(label);
 
 		Text txtSecondsByThrow = this.toolkit.createText(cmpDetail,
 				this.getSecondsByThrow(nbThrows, duration));
 		GridDataFactory.fillDefaults().grab(true, false)
 				.applyTo(txtSecondsByThrow);
 
 		// Total time
 		label = this.toolkit.createLabel(cmpDetail, "Total time:");
 		GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER)
 				.applyTo(label);
 
 		Text txtTotalTime = this.toolkit.createText(cmpDetail,
 				this.getTotalTime(duration));
 		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtTotalTime);
 
 		// nb Darts
 		label = this.toolkit.createLabel(cmpDetail, "Nb Darts:");
 		GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER)
 				.applyTo(label);
 
 		Text txtNbDarts = this.toolkit.createText(cmpDetail,
 				this.getNbDarts(nbDarts));
 		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtNbDarts);
 	}
 
 	/**
 	 * Gets the nb throw.
 	 *
 	 * @param player the player
 	 * @return the nb throw
 	 */
 	private double getNbThrow(IStats<IGame> stats) {
 		double result;
 		if (stats == null) {
 			result = (double) this.game.getNbDartToFinish()
 					/ (double) (this.game.getPlayers().size() * 3);
 		} else {
 			result = 0;
 			IStatsEntry<Object> entry = stats.getEntry(StatsX01DefiService.GAME_COUNT_DARTS);
 			if (entry!=null) {
 				IStatValue<Object> value = entry.getValue();
 				if (value!=null) {
 					Object v = value.getValue();
 					if (v instanceof Number) {
 						result = ((Number) v).doubleValue() /3;
 					}
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Gets the duration.
 	 *
 	 * @param player the player
 	 * @return the duration
 	 */
 	private double getDuration(IStats<IGame> stats) {
 		double result;
 		if (stats == null) {
 			result = Long.valueOf(this.game.getDuration()).doubleValue();
 		} else {
 			result = 0;
 			IStatsEntry<Object> entry = stats.getEntry(StatsX01DefiService.GAME_TOTAL_TIME);
 			if (entry!=null) {
 				IStatValue<Object> value = entry.getValue();
 				if (value!=null) {
 					Object v = value.getValue();
 					if (v instanceof Number) {
 						result = ((Number) v).doubleValue()*1000;
 					}
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Gets the score.
 	 *
 	 * @param player the player
 	 * @return the score
 	 */
 	private int getScore(IStats<IGame> stats) {
 		int result;
 		if (stats == null) {
 			result = this.game.getScoreToDo();
 		} else {
 			result = 0;
 			IStatsEntry<Object> entry = stats.getEntry(StatsX01DefiService.GAME_TOTAL_SCORE);
 			if (entry!=null) {
 				IStatValue<Object> value = entry.getValue();
 				if (value!=null) {
 					Object v = value.getValue();
 					if (v instanceof Number) {
 						result = ((Number) v).intValue();
 					}
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Gets the nb darts.
 	 *
 	 * @param player the player
 	 * @return the nb darts
 	 */
 	private int getNbDarts(IStats<IGame> stats) {
 		int result;
 		if (stats == null) {
 			result = this.game.getNbDartToFinish();
 		} else {
 			result = 0;
 			IStatsEntry<Object> entry = stats.getEntry(StatsX01DefiService.GAME_COUNT_DARTS);
 			if (entry!=null) {
 				IStatValue<Object> value = entry.getValue();
 				if (value!=null) {
 					Object v = value.getValue();
 					if (v instanceof Number) {
 						result = ((Number) v).intValue();
 					}
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Gets the points by second.
 	 *
 	 * @return the points by second
 	 */
 	private String getPointsBySecond(int score, double duration) {
 		double pointBySec = ((double) score * 1000) / (duration);
 		return DecimalFormat.getNumberInstance().format(pointBySec);
 	}
 
 	/**
 	 * Gets the seconds by throw.
 	 *
 	 * @return the seconds by throw
 	 */
 	private String getSecondsByThrow(double nbThrows, double duration) {
 		double secByThrow = ((double) duration) / (1000 * nbThrows);
 		return DecimalFormat.getNumberInstance().format(secByThrow);
 	}
 
 	/**
 	 * Gets the nb darts.
 	 *
 	 * @return the nb darts
 	 */
 	private String getNbDarts(int nbDarts) {
 		return DecimalFormat.getIntegerInstance().format(nbDarts);
 	}
 
 	/**
 	 * Gets the average.
 	 *
 	 * @return the average
 	 */
 	private String getAverage(int score, int nbDarts) {
 		double avg = ((double) score * 3) / nbDarts;
 		return NumberFormat.getNumberInstance().format(avg);
 	}
 
 	/**
 	 * Gets the nb throw.
 	 *
 	 * @return the nb throw
 	 */
 	private String getNbThrow(double nbThrows) {
 		return DecimalFormat.getIntegerInstance().format(Math.ceil(nbThrows));
 	}
 
 	/**
 	 * Gets the total time.
 	 *
 	 * @return the total time
 	 */
 	private String getTotalTime(double duration) {
 		Date date = new Date(Double.valueOf(duration).longValue());
 		return ((GameX01DefiDefinition) this.game.getParentSet()
 				.getGameDefinition()).getTimeFormatter().format(date);
 	}
 
 }
