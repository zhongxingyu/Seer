 package org.apache.wicket.examples.yatzy.frontend.pages;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.wicket.behavior.AttributeAppender;
 import org.apache.wicket.examples.yatzy.frontend.Highscore;
 import org.apache.wicket.examples.yatzy.frontend.YatzyApplication;
 import org.apache.wicket.examples.yatzy.frontend.panels.BookmarkableMenuItem;
 import org.apache.wicket.examples.yatzy.frontend.panels.IMenuItem;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.model.AbstractReadOnlyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.model.StringResourceModel;
 import org.examples.yatzy.IGame;
 
 public class HighscorePage extends BasePage<Void> {
 
 	private static class GameHighscoresModel extends LoadableDetachableModel<List<Highscore>> {
 		private static final long serialVersionUID = 1L;
 
 		private final IModel<List<Highscore>> highscoreModel;
 		private final Class<? extends IGame> gameType;
 
 		public GameHighscoresModel(IModel<List<Highscore>> highscoreModel,
 				Class<? extends IGame> gameType) {
 			this.highscoreModel = highscoreModel;
 			this.gameType = gameType;
 		}
 
 		@Override
 		protected List<Highscore> load() {
 			List<Highscore> gameHighscores = new ArrayList<Highscore>();
 
 			List<Highscore> highscores = highscoreModel.getObject();
 
 			for (Highscore highscore : highscores) {
 				if (gameHighscores.size() < 10) {
 					if (gameType.equals(highscore.getGameType())) {
 						gameHighscores.add(highscore);
 					}
 				}
 			}
 
 			return gameHighscores;
 		}
 	}
 
 	private static class GameTypesModel extends
 			LoadableDetachableModel<List<Class<? extends IGame>>> {
 		private static final long serialVersionUID = 1L;
 
 		private final IModel<List<Highscore>> highscoreModel;
 
 		public GameTypesModel(IModel<List<Highscore>> highscoreModel) {
 			this.highscoreModel = highscoreModel;
 		}
 
 		@Override
 		protected List<Class<? extends IGame>> load() {
 			Set<Class<? extends IGame>> gameTypes = new HashSet<Class<? extends IGame>>();
 
 			List<Highscore> highscores = highscoreModel.getObject();
 			for (Highscore highscore : highscores) {
 				gameTypes.add(highscore.getGameType());
 			}
 
 			return new ArrayList<Class<? extends IGame>>(gameTypes);
 		}
 	}
 
 	public HighscorePage() {
 		final IModel<List<Highscore>> highscoreModel = new LoadableDetachableModel<List<Highscore>>() {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected List<Highscore> load() {
 				return YatzyApplication.get().getHighscores();
 			}
 		};
 
 		GameTypesModel gameTypesModel = new GameTypesModel(highscoreModel);
 		add(new ListView<Class<? extends IGame>>("gameTypes", gameTypesModel) {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected void populateItem(ListItem<Class<? extends IGame>> item) {
 				Class<? extends IGame> gameType = item.getModelObject();
 
				IModel<String> gameTypeModel = new StringResourceModel("game.${simpleName}", this, new Model<Class<? extends IGame>>(gameType));
 				Label<String> gameTypeLabel = new Label<String>("gameType", gameTypeModel);
 				item.add(gameTypeLabel);
 
 				GameHighscoresModel gameHighscoresModel = new GameHighscoresModel(highscoreModel,
 						gameType);
 
 				item.add(new ListView<Highscore>("highscores", gameHighscoresModel) {
 					private static final long serialVersionUID = 1L;
 
 					@Override
 					protected void populateItem(final ListItem<Highscore> item) {
 						Label<Integer> rank = new Label<Integer>("rank",
 								new AbstractReadOnlyModel<Integer>() {
 									private static final long serialVersionUID = 1L;
 
 									@Override
 									public Integer getObject() {
 										return item.getIndex() + 1;
 									}
 								});
 						IModel<String> topRankModel = new AbstractReadOnlyModel<String>() {
 							private static final long serialVersionUID = 1L;
 
 							@Override
 							public String getObject() {
 								return "r" + (item.getIndex() + 1);
 							}
 						};
 						rank.add(new AttributeAppender("class", topRankModel, " "));
 						item.add(rank);
 
 						item.add(new Label<Integer>("score", new PropertyModel<Integer>(item
 								.getModel(), "score")));
 
 						Link<IGame> gameLink = new Link<IGame>("gameLink",
 								new PropertyModel<IGame>(item.getModel(), "game")) {
 							private static final long serialVersionUID = 1L;
 
 							@Override
 							public void onClick() {
 								getRequestCycle().setResponsePage(new GamePage(getModelObject()));
 							}
 						};
 						item.add(gameLink);
 						gameLink.add(new Label<String>("name", new PropertyModel<String>(item
 								.getModelObject(), "name")));
 					}
 				});
 
 			}
 		});
 	}
 
 	@Override
 	protected IModel<String> getPageTitleModel() {
 		return new StringResourceModel("highscore", this, null);
 	}
 
 	@Override
 	protected void addMenuItems(List<IMenuItem> menuItems) {
 		menuItems.add(new BookmarkableMenuItem(new StringResourceModel("newGame", this, null),
 				NewGamePage.class));
 	}
 
 }
