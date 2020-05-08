 package com.alexrnl.subtitlecorrector.gui.model;
 
 import java.nio.file.Path;
 import java.util.logging.Logger;
 
 import com.alexrnl.commons.mvc.AbstractModel;
 import com.alexrnl.subtitlecorrector.correctionstrategy.Strategy;
 import com.alexrnl.subtitlecorrector.gui.controller.MainWindowController;
 
 /**
  * The model for the main window.
  * @author Alex
  */
 public class MainWindowModel extends AbstractModel {
 	/** Logger */
 	private static Logger	lg	= Logger.getLogger(MainWindowModel.class.getName());
 	
 	/** The path of the subtitle */
 	private Path			subtitle;
 	/** The name of the strategy to use */
 	private Strategy		strategy;
 	
 	/**
	 * Constructor #1.<br />
 	 */
 	public MainWindowModel () {
 		super();
 	}
 
 	/**
 	 * Return the attribute subtitle.
 	 * @return the attribute subtitle.
 	 */
 	public Path getSubtitle () {
 		return subtitle;
 	}
 	
 	/**
 	 * Set the attribute subtitle.
 	 * @param subtitle
 	 *        the attribute subtitle.
 	 */
 	public void setSubtitle (final Path subtitle) {
 		final Path oldSubtitle = this.subtitle;
 		this.subtitle = subtitle;
 		fireModelChange(MainWindowController.SUBTITLE_PROPERTY, oldSubtitle, subtitle);
 		
 	}
 	
 	/**
 	 * Return the attribute strategy.
 	 * @return the attribute strategy.
 	 */
 	public Strategy getStrategy () {
 		return strategy;
 	}
 	
 	/**
 	 * Set the attribute strategy.
 	 * @param strategy
 	 *        the attribute strategy.
 	 */
 	public void setStrategy (final Strategy strategy) {
 		final Strategy oldStrategyName = this.strategy;
 		this.strategy = strategy;
 		fireModelChange(MainWindowController.STRATEGY_PROPERTY, oldStrategyName, strategy);
 	}
 	
 }
