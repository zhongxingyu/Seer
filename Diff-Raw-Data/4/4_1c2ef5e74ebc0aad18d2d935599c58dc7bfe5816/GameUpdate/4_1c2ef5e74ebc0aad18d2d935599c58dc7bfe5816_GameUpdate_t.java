 package com.ngdb.web.pages.article.game;
 
 import java.util.Date;
 
 import org.apache.shiro.authz.annotation.RequiresAuthentication;
 import org.apache.tapestry5.SelectModel;
 import org.apache.tapestry5.annotations.InjectPage;
 import org.apache.tapestry5.annotations.Parameter;
 import org.apache.tapestry5.annotations.Property;
 import org.apache.tapestry5.annotations.SetupRender;
 import org.apache.tapestry5.beaneditor.Validate;
 import org.apache.tapestry5.hibernate.annotations.CommitAfter;
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.apache.tapestry5.upload.services.UploadedFile;
 import org.hibernate.Session;
 import org.joda.time.DateTime;
 
 import com.ngdb.entities.History;
 import com.ngdb.entities.article.Game;
 import com.ngdb.entities.article.element.Picture;
 import com.ngdb.entities.reference.Box;
 import com.ngdb.entities.reference.Origin;
 import com.ngdb.entities.reference.Platform;
 import com.ngdb.entities.reference.Publisher;
 import com.ngdb.entities.reference.ReferenceService;
 import com.ngdb.web.model.BoxList;
 import com.ngdb.web.model.GenreList;
 import com.ngdb.web.model.OriginList;
 import com.ngdb.web.model.PlatformList;
 import com.ngdb.web.model.PublisherList;
 import com.ngdb.web.services.infrastructure.CurrentUser;
 import com.ngdb.web.services.infrastructure.PictureService;
 
 @RequiresAuthentication
 public class GameUpdate {
 
 	@Property
 	@Parameter(allowNull = true)
 	private Game game;
 
 	@Property
 	protected UploadedFile mainPicture;
 
 	@Inject
 	protected ReferenceService referenceService;
 
 	@Inject
 	protected PictureService pictureService;
 
 	@Property
 	protected String details;
 
 	@Property
 	@Validate("required")
 	protected Origin origin;
 
 	@Property
 	@Validate("required")
 	protected Date releaseDate;
 
 	@Property
 	@Validate("required")
 	private Publisher publisher;
 
 	@Property
 	@Validate("required")
 	private Platform platform;
 
 	@Property
 	@Validate("required")
 	private Long megaCount;
 
 	@Property
 	@Validate("required")
 	private Box box;
 
 	@Property
 	@Validate("required,maxLength=255")
 	protected String title;
 
 	@InjectPage
 	private GameView gameView;
 
 	@Inject
 	private Session session;
 
 	@Inject
 	private History history;
 
 	@Inject
 	private CurrentUser userSession;
 
 	public void onActivate(Game game) {
 		this.game = game;
 	}
 
 	@SetupRender
 	public void setup() {
 		if (game == null) {
 			this.publisher = null;
 			this.platform = null;
 			this.megaCount = null;
 			this.box = null;
 			this.details = null;
 			this.origin = null;
 			this.releaseDate = new DateTime().withYear(1990).toDate();
 			this.details = null;
 			this.title = null;
 		} else {
 			this.publisher = game.getPublisher();
 			this.platform = game.getPlatform();
 			this.megaCount = game.getMegaCount();
 			this.box = game.getBox();
 			this.details = game.getDetails();
 			this.origin = game.getOrigin();
 			this.releaseDate = game.getReleaseDate();
 			this.details = game.getDetails();
 			this.title = game.getTitle();
 		}
 	}
 
 	@CommitAfter
 	public Object onSuccess() {
 		Game game = new Game();
 		if (isEditMode()) {
 			game = this.game;
 			game.updateModificationDate();
 		}
 		game.setDetails(details);
 		game.setOrigin(origin);
 		game.setReleaseDate(releaseDate);
 		game.setTitle(title);
 		game.setPublisher(publisher);
 		game.setPlatform(platform);
 		game.setMegaCount(megaCount);
 		game.setBox(box);
 		game = (Game) session.merge(game);
 		if (this.mainPicture != null) {
 			Picture picture = pictureService.store(mainPicture, game);
 			game.addPicture(picture);
			if (isEditMode()) {
				session.merge(picture);
			}
 		}
 		gameView.setGame(game);
 		history.add(game, userSession.getUser());
 		return gameView;
 	}
 
 	public boolean isEditMode() {
 		return this.game != null;
 	}
 
 	public SelectModel getPlatforms() {
 		return new PlatformList(referenceService.getPlatforms());
 	}
 
 	public SelectModel getGenres() {
 		return new GenreList(referenceService.getGenres());
 	}
 
 	public SelectModel getPublishers() {
 		return new PublisherList(referenceService.getPublishers());
 	}
 
 	public SelectModel getBoxes() {
 		return new BoxList(referenceService.getBoxes());
 	}
 
 	public SelectModel getOrigins() {
 		return new OriginList(referenceService.getOrigins());
 	}
 
 }
