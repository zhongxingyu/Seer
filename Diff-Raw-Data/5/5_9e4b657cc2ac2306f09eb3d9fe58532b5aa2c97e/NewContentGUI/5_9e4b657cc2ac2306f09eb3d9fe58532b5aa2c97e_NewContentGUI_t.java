 package de.fhkoeln.gm.serientracker.client.gui;
 
 import java.awt.CardLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.UIManager;
 
 import de.fhkoeln.gm.serientracker.jaxb.Country;
 import de.fhkoeln.gm.serientracker.jaxb.Genre;
 import de.fhkoeln.gm.serientracker.jaxb.Network;
 import de.fhkoeln.gm.serientracker.jaxb.Runtime;
 import de.fhkoeln.gm.serientracker.jaxb.Weekday;
 import de.fhkoeln.gm.serientracker.utils.Logger;
 
 import net.miginfocom.swing.MigLayout;
 
 /**
  * Provides the new content GUI for a new series, season and/or episdode
  *
  * @author Dennis Meyer
  */
 public class NewContentGUI extends JFrame implements ActionListener {
 
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * Content Types: SERIES, SEASON, EPISODE
 	 */
 	public enum Context {
 		SERIES, SEASON, EPISODE
 	}
 
 	// Holds the current context
 	private Context context;
 
 	// Holds the card layout
 	private CardLayout newContentCardLayout;
 
 	// GUI components
 	private JPanel newContentPanels;
 	private JPanel newSeriesPanel;
 	private JPanel newSeasonPanel;
 	private JPanel newEpisodePanel;
 
 	private JButton btnSaveSeriesAndAddSeason;
 	private JButton btnSeriesAddImages;
 	private JButton btnSeasonAddImages;
 	private JButton btnSaveSeries;
 	private JButton btnCancelSeries;
 	private JButton btnSaveSeason;
 	private JButton btnCancelSeason;
 	private JButton btnSaveEpisode;
 	private JButton btnCancelEpisode;
 	private JButton btnSaveSeasonAndAddEpisode;
 	private JButton btnEpisodeAddImages;
 
 	public JTextField seasonNumber;
 	public JComboBox seasonSeriesRelation;
 
 	public JTextField seriesTitle;
 	public JTextField seriesYear;
 	public JTextField seriesFirstAired;
 	public JPanel seriesGenreRelations;
 	public JComboBox seriesCountry;
 	public JTextArea seriesOverview;
 	public JComboBox seriesRuntime;
 	public JComboBox seriesNetwork;
 	public JComboBox seriesAirday;
 	public JTextField seriesAirtime;
 
 	public JTextField episodeTitle;
 	public JTextField episodeNumber;
 	public JTextField episodeAirdate;
 	public JTextArea episodeOverview;
 	public JComboBox episodeSeriesRelation;
 	public JComboBox episodeSeasonRelation;
 
 	/**
 	 * Constructor.
 	 * Sets the UI look and context.
 	 *
 	 * @param Context context
 	 */
 	public NewContentGUI( Context context ) {
 		this.context = context;
 
 		try {
 			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
 		} catch ( Exception e ) {
 		}
 
 		initComponents();
 	}
 
 	/**
 	 * Sets up the GUI components.
 	 */
 	private void initComponents() {
 		// Disable resizing
 		setResizable( false );
 
 
 		/********
 		 * PANELS
 		 */
 
 		// Use CardLayout as layout manager
 		newContentCardLayout = new CardLayout();
 		newContentPanels = new JPanel( newContentCardLayout );
 		add( newContentPanels );
 
 		// Get the content panels
 		newContentPanels.add( this.getNewSeriesPanel(), "SERIES" );
 		newContentPanels.add( this.getNewSeasonPanel(), "SEASON" );
 		newContentPanels.add( this.getNewEpisodePanel(), "EPISODE" );
 
 		this.updatePanelDisplay();
 	}
 
 	/**
 	 * Shows the new content panel based on the current context.
 	 */
 	private void updatePanelDisplay() {
 		switch ( this.context ) {
 			case EPISODE:
 				Logger.log( "New episode" );
 
 				// Extend title
 				setTitle( "SERIENTRACKER | NEW CONTENT | EPISODE" );
 
 				// Set frame size
 				setBounds( 0, 0, 600, 400 );
 
 				// Center frame on screen
 				setLocationRelativeTo( null );
 
 				// Show panel
 				newContentCardLayout.show( newContentPanels, "EPISODE" );
 				break;
 
 			case SEASON:
 				Logger.log( "New season" );
 
 				// Extend title
 				setTitle( "SERIENTRACKER | NEW CONTENT | SEASON" );
 
 				// Set frame size
 				setBounds( 0, 0, 600, 200 );
 
 				// Center frame on screen
 				setLocationRelativeTo( null );
 
 				// Show panel
 				newContentCardLayout.show( newContentPanels, "SEASON" );
 				break;
 
 			case SERIES:
 				Logger.log( "New series" );
 
 				// Extend title
 				setTitle( "SERIENTRACKER | NEW CONTENT | SERIES" );
 
 				// Set frame size
 				setBounds( 0, 0, 600, 700 );
 
 				// Center frame on screen
 				setLocationRelativeTo( null );
 
 				// Show panel
 				newContentCardLayout.show( newContentPanels, "SERIES" );
 				break;
 			default:
 				break;
 		}
 	}
 
 	/**
 	 * Returns the panel for a new series.
 	 *
 	 * @return JPanel
 	 */
 	private JPanel getNewSeriesPanel() {
 		// Set up the panel
 		newSeriesPanel = new JPanel( new MigLayout( "", "[150][grow]" ) );
 
 		// Print the labels
 		newSeriesPanel.add( new JLabel( "Title:" ), "cell 0 0" );
 		newSeriesPanel.add( new JLabel( "Genres:" ), "cell 0 1, top" );
 		newSeriesPanel.add( new JLabel( "Year:" ), "cell 0 2" );
 		newSeriesPanel.add( new JLabel( "Firstaired:" ), "cell 0 3" );
 		newSeriesPanel.add( new JLabel( "Country:" ), "cell 0 4" );
 		newSeriesPanel.add( new JLabel( "Overview:" ), "cell 0 5" );
 		newSeriesPanel.add( new JLabel( "Episoderuntime:" ), "cell 0 6" );
 		newSeriesPanel.add( new JLabel( "Network:" ), "cell 0 7, gaptop 5" );
 		newSeriesPanel.add( new JLabel( "Airday:" ), "cell 0 8" );
 		newSeriesPanel.add( new JLabel( "Airtime:" ), "cell 0 9" );
 		newSeriesPanel.add( new JLabel( "Images:" ), "cell 0 10" );
 
 		// Input field for title
 		seriesTitle = new JTextField();
 		newSeriesPanel.add( seriesTitle, "cell 1 0, grow" );
 
 		// Genres
 		seriesGenreRelations = new JPanel( new MigLayout( "ins 0, fill" ) );
 		Genre[] genres = Genre.values();
 		int i = 0;
 		for ( Genre genre : genres ) {
 			JCheckBox genreCheckbox = new JCheckBox();
 			genreCheckbox.setText( genre.value() );
 			genreCheckbox.setName( genre.value() );
 			String constrain = ( ++i % 3 == 0 ) ? "wrap" : "";
 			seriesGenreRelations.add( genreCheckbox, constrain );
 		}
 		newSeriesPanel.add( seriesGenreRelations, "cell 1 1" );
 
 		// Input field for year
 		seriesYear = new JTextField();
 		seriesYear.setText( "2013" );
 		newSeriesPanel.add( seriesYear, "cell 1 2, width 50" );
 
 		// Input field for firstaired
 		seriesFirstAired = new JTextField();
 		newSeriesPanel.add( seriesFirstAired, "cell 1 3, grow" );
 
 		// Dropdown: Country
 		seriesCountry = new JComboBox();
 		Country[] countries = Country.values();
 		for ( Country country : countries )
 			seriesCountry.addItem( country.value() );
 		newSeriesPanel.add( seriesCountry, "cell 1 4" );
 
 		// Input field for overview
 		seriesOverview = new JTextArea();
 		seriesOverview.setRows( 5 );
 		seriesOverview.setLineWrap( true );
 		JScrollPane seriesOverviewScoll = new JScrollPane( seriesOverview );
 		seriesOverviewScoll.setBorder( new JTextField().getBorder() ); // Workaround for same styling
 		newSeriesPanel.add( seriesOverviewScoll, "cell 1 5, growx" );
 
 		// Dropdown: Runtime
 		seriesRuntime = new JComboBox();
 		Runtime[] runtimes = Runtime.values();
 		for ( Runtime runtime : runtimes )
 			seriesRuntime.addItem( runtime.value() );
 		newSeriesPanel.add( seriesRuntime, "cell 1 6" );
 
 		// Dropdown: Network
 		seriesNetwork = new JComboBox();
 		Network[] networks = Network.values();
 		for ( Network network : networks )
 			seriesNetwork.addItem( network.value() );
 		newSeriesPanel.add( seriesNetwork, "cell 1 7" );
 
 		// Dropdown: Airday
 		seriesAirday = new JComboBox();
 		Weekday[] weekdays = Weekday.values();
 		for ( Weekday weekday : weekdays )
 			seriesAirday.addItem( weekday.value() );
 		newSeriesPanel.add( seriesAirday, "cell 1 8" );
 
 		// Input field for airtime
 		seriesAirtime = new JTextField();
 		seriesAirtime.setText( "00:00:00" );
 		newSeriesPanel.add( seriesAirtime, "cell 1 9, width 80" );
 
 
 		/********
 		 * ACTIONS
 		 */
 
 		// Button for add images
 		btnSeriesAddImages = new JButton( "Add Images" );
 		btnSeriesAddImages.addActionListener( this );
 		btnSeriesAddImages.setEnabled( false );
 		newSeriesPanel.add( btnSeriesAddImages, "cell 1 10" );
 
 		// Button for cancel
 		btnCancelSeries = new JButton( "Cancel" );
 		btnCancelSeries.addActionListener( this );
 		newSeriesPanel.add( btnCancelSeries, "cell 0 11, left, gaptop 25" );
 
 		// Button for save
 		btnSaveSeries = new JButton( "Save" );
 		btnSaveSeries.addActionListener( this );
 		newSeriesPanel.add( btnSaveSeries, "cell 1 11, right, gaptop 25" );
 
 		// Button for save and new season
 		btnSaveSeriesAndAddSeason = new JButton( "Save & Add Season" );
 		btnSaveSeriesAndAddSeason.addActionListener( this );
 		newSeriesPanel.add( btnSaveSeriesAndAddSeason, "cell 1 11, right, gaptop 25" );
 
 		return newSeriesPanel;
 	}
 
 	/**
 	 * Returns the panel for a new season.
 	 *
 	 * @return JPanel
 	 */
 	private JPanel getNewSeasonPanel() {
 		// Set up the panel
 		newSeasonPanel = new JPanel( new MigLayout( "gap 0 0", "[150][grow]" ) );
 
 		// Print the labels
 		newSeasonPanel.add( new JLabel( "Series:" ), "cell 0 0" );
 		newSeasonPanel.add( new JLabel( "Seasonnumber:" ), "cell 0 1" );
 		newSeasonPanel.add( new JLabel( "Images:" ), "cell 0 2" );
 
 		// Dropdown: Series
 		seasonSeriesRelation = new JComboBox();
 		newSeasonPanel.add( seasonSeriesRelation, "cell 1 0" );
 
 		// Input field for seasonnumber
 		seasonNumber = new JTextField();
 		newSeasonPanel.add( seasonNumber, "cell 1 1, width 50" );
 
 
 		/********
 		 * ACTIONS
 		 */
 
 		// Button for add images
 		btnSeasonAddImages = new JButton( "Add Images" );
 		btnSeasonAddImages.addActionListener( this );
 		btnSeasonAddImages.setEnabled( false );
 		newSeasonPanel.add( btnSeasonAddImages, "cell 1 2" );
 
 		// Button for cancel
 		btnCancelSeason = new JButton( "Cancel" );
 		btnCancelSeason.addActionListener( this );
 		newSeasonPanel.add( btnCancelSeason, "cell 0 3, left, gaptop 25" );
 
 		// Button for save
 		btnSaveSeason = new JButton( "Save" );
 		btnSaveSeason.addActionListener( this );
 		newSeasonPanel.add( btnSaveSeason, "cell 1 3, right, gaptop 25" );
 
 		// Button for save and add new season
 		btnSaveSeasonAndAddEpisode = new JButton( "Save & Add Episode" );
 		btnSaveSeasonAndAddEpisode.addActionListener( this );
 		newSeasonPanel.add( btnSaveSeasonAndAddEpisode, "cell 1 3, right, gaptop 25" );
 
 		return newSeasonPanel;
 	}
 
 
 	/**
 	 * Returns the panel for a new episode.
 	 *
 	 * @return JPanel
 	 */
 	private JPanel getNewEpisodePanel() {
 		// Set up the panel
 		newEpisodePanel = new JPanel( new MigLayout( "gap 0 0", "[150][grow]" ) );
 
 		// Print the labels
 		newEpisodePanel.add( new JLabel( "Serie:" ), "cell 0 0" );
 		newEpisodePanel.add( new JLabel( "Season:" ), "cell 0 1" );
 		newEpisodePanel.add( new JLabel( "Title:" ), "cell 0 2" );
 		newEpisodePanel.add( new JLabel( "Number:" ), "cell 0 3" );
 		newEpisodePanel.add( new JLabel( "Airdate:" ), "cell 0 4" );
 		newEpisodePanel.add( new JLabel( "Overview:" ), "cell 0 5" );
 		newEpisodePanel.add( new JLabel( "Images:" ), "cell 0 6" );
 
 		// Dropdown: Series
 		episodeSeriesRelation = new JComboBox();
		newEpisodePanel.add( episodeSeriesRelation, "cell 1 0" );
 
 		// Dropdown: Seasons
 		episodeSeasonRelation = new JComboBox();
		newEpisodePanel.add( episodeSeasonRelation, "cell 1 1" );
 
 		// Input field for title
 		episodeTitle = new JTextField();
 		newEpisodePanel.add( episodeTitle, "cell 1 2, grow" );
 
 		// Input field for episodenumber
 		episodeNumber = new JTextField();
 		newEpisodePanel.add( episodeNumber, "cell 1 3, grow" );
 
 		// Input field for airdate
 		episodeAirdate = new JTextField();
 		newEpisodePanel.add( episodeAirdate, "cell 1 4, grow" );
 
 		// Input field for about overview
 		episodeOverview = new JTextArea();
 		episodeOverview.setRows( 5 );
 		episodeOverview.setLineWrap( true );
 		JScrollPane episodeOverviewScoll = new JScrollPane( episodeOverview );
 		episodeOverviewScoll.setBorder( new JTextField().getBorder() ); // Workaround for same styling
 		newEpisodePanel.add( episodeOverviewScoll, "cell 1 5, growx" );
 
 
 		/********
 		 * ACTIONS
 		 */
 
 		// Button for add images
 		btnEpisodeAddImages = new JButton( "Add Images" );
 		btnEpisodeAddImages.addActionListener( this );
 		btnEpisodeAddImages.setEnabled( false );
 		newEpisodePanel.add( btnEpisodeAddImages, "cell 1 6" );
 
 		// Button for cancel
 		btnCancelEpisode = new JButton( "Cancel" );
 		btnCancelEpisode.addActionListener( this );
 		newEpisodePanel.add( btnCancelEpisode, "cell 0 7, left, gaptop 25" );
 
 		// Button for save
 		btnSaveEpisode = new JButton( "Save" );
 		btnSaveEpisode.addActionListener( this );
 		newEpisodePanel.add( btnSaveEpisode, "cell 1 7, right, gaptop 25" );
 
 		return newEpisodePanel;
 	}
 
 	/**
 	 * Sets the context.
 	 *
 	 * @param Context context
 	 */
 	public void setContext( Context context ) {
 		this.context = context;
 	}
 
 	/**
 	 * Action handler for button actions.
 	 */
 	@Override
 	public void actionPerformed( ActionEvent e ) {
 		NewContentController controller = new NewContentController( this );
 
 		if ( e.getSource() == btnSaveSeries ) {
 			Logger.log( "SAVE SERIES" );
 			controller.saveSeries();
 		}
 		else if ( e.getSource() == btnCancelSeries ) {
 			this.dispose();
 		}
 		else if ( e.getSource() == btnSaveSeason ) {
 			Logger.log( "SAVE SEASON" );
 			controller.saveSeason();
 		}
 		else if ( e.getSource() == btnCancelSeason ) {
 			this.dispose();
 		}
 		else if ( e.getSource() == btnSaveEpisode ) {
 			Logger.log( "SAVE EPISODE" );
 			controller.saveSeason();
 		}
 		else if ( e.getSource() == btnCancelEpisode ) {
 			this.dispose();
 		}
 		else if ( e.getSource() == btnSaveSeriesAndAddSeason ) {
 			// Goto Season
 			Logger.log( "Add Season clicked" );
 			this.context = Context.SEASON;
 			this.updatePanelDisplay();
 		}
 		else if ( e.getSource() == btnSaveSeasonAndAddEpisode ) {
 			// Goto Episode
 			Logger.log( "Add Episode clicked" );
 			this.context = Context.EPISODE;
 			this.updatePanelDisplay();
 		}
 	}
 
 
 }
