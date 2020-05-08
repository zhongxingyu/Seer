 /**
  * 
  */
 package view.mainwindow;
 
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.LayoutManager;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.sql.SQLException;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.logging.Level;
 
 import javax.swing.BorderFactory;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.border.EtchedBorder;
 
 import logger.MabisLogger;
 import model.entity.AudioAlbum;
 import model.entity.AudioUser;
 import model.entity.Book;
 import model.entity.BookUser;
 import model.entity.Movie;
 import model.entity.MovieUser;
 import model.entity.User;
 import model.enums.TableType;
 import model.utilities.ForeignKeyPair;
 import view.imagePanel.ChoosableImagePanel;
 import controller.SQLStamentType;
 import controller.entity.AudioAlbumSQLFactory;
 import controller.entity.AudioUserSQLFactory;
 import controller.entity.BookSQLFactory;
 import controller.entity.BookUserSQLFactory;
 import controller.entity.MovieSQLFactory;
 import controller.entity.MovieUserSQLFactory;
 
 //TODO add comments
 public class MWCollectionView extends JPanel implements PropertyChangeListener {
 	private static final Dimension THUMBAILSIZE = new Dimension(150, 200);
 	private static final long serialVersionUID = 4037649477948033295L;
 	private final CollectionMediator mediator = new CollectionMediator();
 	private JPopupMenu collectionMenu;
 	private User connectedUserReference;
 	private final TreeMap<Movie, ChoosableImagePanel> movieThumbs = new TreeMap<Movie, ChoosableImagePanel>();
 	private final TreeMap<AudioAlbum, ChoosableImagePanel> audioThumbs = new TreeMap<AudioAlbum, ChoosableImagePanel>();
 	private final TreeMap<Book, ChoosableImagePanel> bookThumbs = new TreeMap<Book, ChoosableImagePanel>();
 	private JPanel thumbailsPanel = null;
 	private JScrollPane scrollPanel = null;
 
 	public MWCollectionView(LayoutManager layout, boolean isDoubleBuffered) {
 		super(layout, isDoubleBuffered);
 		this.initComponents();
 	}
 
 	private void initComponents() {
 		this.setBorder(BorderFactory.createTitledBorder(
 				BorderFactory.createEtchedBorder(EtchedBorder.RAISED),
 				"Collection"));
 
 		this.thumbailsPanel = new JPanel(
 				new FlowLayout(FlowLayout.LEFT, 10, 10));
 		this.scrollPanel = new JScrollPane(this.thumbailsPanel,
 				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
 				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 
 		this.add(this.scrollPanel);
 		initPopupMenu();
 	}
 
 	private void initPopupMenu() {
 		// TODO add listener
 		this.collectionMenu = new JPopupMenu("Collection popup menu");
 		collectionMenu.add(new JMenuItem("Edit"));
 		collectionMenu.add(new JMenuItem("Remove"));
 		collectionMenu.add(new JMenuItem("Publish/Unpublish"));
 		collectionMenu.setSelected(this);
 	}
 
 	private void reprintCollection() {
 		this.thumbailsPanel.removeAll();
 		for (ChoosableImagePanel thumb : this.movieThumbs.values()) {
 
 			thumb.setPreferredSize(MWCollectionView.THUMBAILSIZE);
 			thumb.setMaximumSize(MWCollectionView.THUMBAILSIZE);
 			thumb.setMinimumSize(MWCollectionView.THUMBAILSIZE);
 
 			thumb.setBorder(BorderFactory.createTitledBorder(
 					BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
 					"LOL"));
 			this.thumbailsPanel.add(thumb);
 		}
 		this.thumbailsPanel.validate();
 	}
 
 	/**
 	 * Klasa której głównym zadaniem jest pośredniczenie między kolekcją
 	 * zlokalizowaną w bazie danych (tj. danymi), a ich wizualną reprezentacją
 	 * (tj. widokiem) który prezentowany jest użytkownikowi na
 	 * {@link MWCollectionView}.
 	 * 
 	 * @author kornicameister
 	 * @version 0.1
 	 */
 	private final class CollectionMediator {
 		private String currentView, currentGroup;
 
 		public CollectionMediator() {
 			this.currentView = new String();
 			this.currentGroup = new String();
 		}
 
 		void loadSelected(String display) throws SQLException {
 			if (display.equals(this.currentView)) {
 				return;// THIS IS ALREADY BEING DISPLAYED
 			} else if (display != null) {
 				this.currentView = display;
 			}
 
 			audioThumbs.clear();
 			movieThumbs.clear();
 			bookThumbs.clear();
 
 			if (this.currentView.equals(TableType.BOOK.toString())) {
 				this.loadBooks(); // load and print books only
 			} else if (this.currentView
 					.equals(TableType.AUDIO_ALBUM.toString())) {
 				this.loadAudios();// load and print audio albums only
 			} else if (this.currentView.equals(TableType.MOVIE.toString())) {
 				this.loadMovies(); // load and print movies only
 			} else if (this.currentView.equals("all")) {
 				this.loadAll();
 			}
 
 			// and finally reprinting collection
 			reprintCollection();
 		}
 
 		public void groupViewBy(String viewValue) {
 			if (this.currentGroup.equals(viewValue)) {
 				return;
 			}
 		}
 
 		private void loadAll() throws SQLException {
 			this.loadBooks();
 			this.loadAudios();
 			this.loadMovies();
 		}
 
 		private void loadMovies() throws SQLException {
 			MovieUserSQLFactory musf = new MovieUserSQLFactory(
 					SQLStamentType.SELECT, new MovieUser());
 			musf.addWhereClause("idUser", connectedUserReference
 					.getPrimaryKey().toString());
 			musf.executeSQL(true); // have all pair of keys from movieUser
 			TreeSet<ForeignKeyPair> keys = musf.getMovieUserKeys();
 			if (keys.isEmpty()) {
 				return;
 			}
 			MovieSQLFactory msf = new MovieSQLFactory(SQLStamentType.SELECT,
 					new Movie());
 			for (ForeignKeyPair fkp : keys) {
 				msf.addWhereClause("idMovie", fkp.getKey("idMovie").getValue()
 						.toString());
 
 			}
 			msf.executeSQL(true);
 			TreeSet<Movie> movies = msf.getMovies();
 			MabisLogger.getLogger().log(Level.INFO,
 					"Succesffuly obtained {0} movies for collection view",
 					movies.size());
 			for (Movie movie : movies) {
 				movieThumbs.put(movie, new ChoosableImagePanel(movie.getCover()
 						.getImageFile()));
 			}
 		}
 
 		private void loadAudios() throws SQLException {
 			AudioUserSQLFactory ausf = new AudioUserSQLFactory(
 					SQLStamentType.SELECT, new AudioUser());
 			ausf.addWhereClause("idUser", connectedUserReference
 					.getPrimaryKey().toString());
 			ausf.executeSQL(true); // have all pair of keys from movieUser
 			TreeSet<ForeignKeyPair> keys = ausf.getAudioUserKeys();
 			if (keys.isEmpty()) {
 				return;
 			}
 			AudioAlbumSQLFactory aasf = new AudioAlbumSQLFactory(
 					SQLStamentType.SELECT, new AudioAlbum());
 			for (ForeignKeyPair fkp : keys) {
 				aasf.addWhereClause("idMovie", fkp.getKey("idMovie").getValue()
 						.toString());
 
 			}
 			aasf.executeSQL(true);
 			TreeSet<AudioAlbum> audios = aasf.getValues();
 			MabisLogger
 					.getLogger()
 					.log(Level.INFO,
 							"Succesffuly obtained {0} audio albums for collection view",
 							audios.size());
 			for (AudioAlbum album : audios) {
				audioThumbs.put(album, new ChoosableImagePanel(album
						.getFrontCover().getImageFile()));
 			}
 		}
 
 		private void loadBooks() throws SQLException {
 			BookUserSQLFactory ausf = new BookUserSQLFactory(
 					SQLStamentType.SELECT, new BookUser());
 			ausf.addWhereClause("idUser", connectedUserReference
 					.getPrimaryKey().toString());
 			ausf.executeSQL(true); // have all pair of keys from movieUser
 			TreeSet<ForeignKeyPair> keys = ausf.getBookUserKeys();
 			if (keys.isEmpty()) {
 				return;
 			}
 			BookSQLFactory aasf = new BookSQLFactory(SQLStamentType.SELECT,
 					new Book());
 			for (ForeignKeyPair fkp : keys) {
 				aasf.addWhereClause("idMovie", fkp.getKey("idMovie").getValue()
 						.toString());
 
 			}
 			aasf.executeSQL(true);
 			TreeSet<Book> books = aasf.getBooks();
 			MabisLogger.getLogger().log(Level.INFO,
 					"Succesffuly obtained {0} books for collection view",
 					books.size());
 			for (Book b : books) {
 				bookThumbs.put(b, new ChoosableImagePanel(b.getCover()
 						.getImageFile()));
 			}
 		}
 
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent arg0) {
 		String group = null;
 		String value = null;
 
 		if (arg0.getPropertyName().equals("connectedUser")) {
 			this.connectedUserReference = (User) arg0.getNewValue();
 			MabisLogger
 					.getLogger()
 					.log(Level.INFO,
 							this.getClass().getName()
 									+ ": updating connected user info, connected user login:"
 									+ this.connectedUserReference.getLogin());
 			return;
 		}
 
 		if (arg0.getNewValue() instanceof String
 				&& arg0.getPropertyName() instanceof String) {
 			value = (String) arg0.getNewValue();
 			group = (String) arg0.getPropertyName();
 			if (group.equals("viewAs")) {
 				try {
 					this.mediator.loadSelected(value);
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 				MabisLogger
 						.getLogger()
 						.log(Level.INFO,
 								this.getClass().getName()
 										+ ": updating collection view, current view set to {0}",
 								value);
 			} else {
 				this.mediator.groupViewBy(value);
 				MabisLogger
 						.getLogger()
 						.log(Level.INFO,
 								this.getClass().getName()
 										+ ": updating collection group, current group set to {0}",
 								value);
 			}
 		}
 	}
 
 	/**
 	 * Metoda ładuje lokalną kolecję na widok kolekcji. Pobiera dane do widoku
 	 * kolekcji poprzez mediatora ( {@link CollectionMediator} ). <b>Ważne: </b>
 	 * Metoda do poprawnego działania wymaga połączenie z bazą danych oraz
 	 * zweryfikowanego użytkownika, który jest zalogowany w aplikacji. Metoda
 	 * wywoływana powinna być tylko raz, zaraz po nawiązaniu połączenia i
 	 * zalogowaniu się przez użytownika do programu.
 	 * 
 	 * @throws SQLException
 	 */
 	// TODO dodać ładowania według predefiniowanych ustawień !!!
 	public void loadCollection() throws SQLException {
 		this.mediator.loadSelected("all");
 		this.mediator.groupViewBy(null);
 	}
 }
