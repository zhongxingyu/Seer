 /* This file is part of SoftwareFm
 /* SoftwareFm is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.*/
 /* SoftwareFm is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
 /* You should have received a copy of the GNU General Public License along with SoftwareFm. If not, see <http://www.gnu.org/licenses/> */
 
 /* This file is part of SoftwareFm
  /* SoftwareFm is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.*/
 /* SoftwareFm is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
 /* You should have received a copy of the GNU General Public License along with SoftwareFm. If not, see <http://www.gnu.org/licenses/> */
 
 package org.softwareFm.explorer.eclipse;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicReference;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.softwareFm.card.card.ICard;
 import org.softwareFm.card.card.ICardChangedListener;
 import org.softwareFm.card.card.ICardFactory;
 import org.softwareFm.card.card.ICardHolder;
 import org.softwareFm.card.configuration.CardConfig;
 import org.softwareFm.card.dataStore.CardAndCollectionDataStoreAdapter;
 import org.softwareFm.card.dataStore.IAfterEditCallback;
 import org.softwareFm.card.dataStore.ICardDataStore;
 import org.softwareFm.card.dataStore.IMutableCardDataStore;
 import org.softwareFm.collections.ICollectionConfigurationFactory;
 import org.softwareFm.collections.explorer.IExplorer;
 import org.softwareFm.collections.explorer.IExplorerListener;
 import org.softwareFm.collections.explorer.IMasterDetailSocial;
 import org.softwareFm.collections.explorer.SnippetFeedConfigurator;
 import org.softwareFm.collections.menu.ICardMenuItemHandler;
 import org.softwareFm.collections.mySoftwareFm.ILoginStrategy;
 import org.softwareFm.display.browser.IBrowserConfigurator;
 import org.softwareFm.display.swt.Swts;
 import org.softwareFm.display.swt.Swts.Buttons;
 import org.softwareFm.display.swt.Swts.Grid;
 import org.softwareFm.display.swt.Swts.Show;
 import org.softwareFm.display.timeline.IPlayListGetter;
 import org.softwareFm.httpClient.api.IHttpClient;
 import org.softwareFm.repositoryFacard.IRepositoryFacard;
 import org.softwareFm.server.GitRepositoryFactory;
 import org.softwareFm.server.ServerConstants;
 import org.softwareFm.utilities.functions.IFunction1;
 import org.softwareFm.utilities.services.IServiceExecutor;
 
 public class ExplorerWithRadioChannel {
 	public static void main(String[] args) {
 		File home = new File(System.getProperty("user.home"));
 		final File localRoot = new File(home, ".sfm");
		boolean local = true;
 		String server = local ? "localhost" : "www.softwarefm.com";
 		String prefix = local ? new File(home, ".sfm_remote").getAbsolutePath() : "git://www.softwarefm.com/";
 		int port = local ? 8080 : 80;
 		final IHttpClient client = IHttpClient.Utils.builder(server, port);
 		final IRepositoryFacard facard = GitRepositoryFactory.gitRepositoryFacard(client, localRoot, prefix);
 		final IServiceExecutor service = IServiceExecutor.Utils.defaultExecutor();
 		try {
 			final List<String> rootUrl = Arrays.asList("/softwareFm/data", "/softwareFm/snippet");
 			final String firstUrl = "/softwareFm/data/activemq/activemq/artifact/activemq-axis";
 
 			Show.display(ExplorerWithRadioChannel.class.getSimpleName(), new IFunction1<Composite, Composite>() {
 				@Override
 				public Composite apply(Composite from) throws Exception {
 					Composite explorerAndButton = Swts.newComposite(from, SWT.NULL, "ExplorerAndButton");
 					explorerAndButton.setLayout(new GridLayout());
 					Composite buttonPanel = Swts.newComposite(explorerAndButton, SWT.NULL, "ButtonPanel");
 					buttonPanel.setLayout(Swts.Row.getHorizonalNoMarginRowLayout());
 
 					final IMutableCardDataStore cardDataStore = ICardDataStore.Utils.repositoryCardDataStore(from, facard);
 					ICardFactory cardFactory = ICardFactory.Utils.cardFactory();
 					final CardConfig cardConfig = ICollectionConfigurationFactory.Utils.softwareFmConfigurator().configure(from.getDisplay(), new CardConfig(cardFactory, cardDataStore));
 					IMasterDetailSocial masterDetailSocial = IMasterDetailSocial.Utils.masterDetailSocial(explorerAndButton);
 					IPlayListGetter playListGetter = new ArtifactPlayListGetter(cardDataStore);
 					ILoginStrategy loginStrategy = ILoginStrategy.Utils.softwareFmLoginStrategy(from.getDisplay(), service, client);
 					final IExplorer explorer = IExplorer.Utils.explorer(masterDetailSocial, cardConfig, rootUrl, playListGetter, service, loginStrategy );
 
 					ICardMenuItemHandler.Utils.addSoftwareFmMenuItemHandlers(explorer);
 					IBrowserConfigurator.Utils.configueWithUrlRssTweet(explorer);
 					SnippetFeedConfigurator.configure(explorer, cardConfig);
 
 					explorer.displayCard(firstUrl, new CardAndCollectionDataStoreAdapter());
 					buttonPanel.setLayoutData(Grid.makeGrabHorizonalAndFillGridData());
 					masterDetailSocial.getComposite().setLayoutData(Grid.makeGrabHorizonalVerticalAndFillGridData());
 
 					Buttons.makePushButton(buttonPanel, null, "Next", false, new Runnable() {
 						@Override
 						public void run() {
 							explorer.next();
 						}
 					});
 					Buttons.makePushButton(buttonPanel, null, "Prev", false, new Runnable() {
 						@Override
 						public void run() {
 							explorer.previous();
 						}
 					});
 					final AtomicReference<String> url = new AtomicReference<String>();
 					explorer.addCardListener(new ICardChangedListener() {
 
 						@Override
 						public void valueChanged(ICard card, String key, Object newValue) {
 
 						}
 
 						@Override
 						public void cardChanged(ICardHolder cardHolder, ICard card) {
 							url.set(card.url());
 						}
 					});
 					Buttons.makePushButton(buttonPanel, null, "Select Artifact", false, new Runnable() {
 						@Override
 						public void run() {
 							explorer.selectAndNext(url.get());
 						}
 					});
 					Buttons.makePushButton(buttonPanel, null, "Refresh", false, new Runnable() {
 						@Override
 						public void run() {
 							explorer.clearCaches();
 							String url = explorer.getCardHolder().getCard().url();
 							explorer.displayCard(url, new CardAndCollectionDataStoreAdapter());
 						}
 					});
 					Buttons.makePushButton(buttonPanel, null, "Unrecognised Jar", false, new Runnable() {
 						@Override
 						public void run() {
 							explorer.displayUnrecognisedJar(new File("a/b/cake-forkjoin-0.1.jar"), "0123439754987345978", "some project name");
 						}
 					});
 					Buttons.makePushButton(buttonPanel, null, "Not A Jar", false, new Runnable() {
 						@Override
 						public void run() {
 							explorer.displayNotAJar();
 						}
 					});
 					Buttons.makePushButton(buttonPanel, null, "Artifact", false, new Runnable() {
 						@Override
 						public void run() {
 							explorer.displayCard(firstUrl, new CardAndCollectionDataStoreAdapter());
 						}
 					});
 					Buttons.makePushButton(buttonPanel, null, "Nuke", false, new Runnable() {
 						@Override
 						public void run() {
 							cardConfig.cardDataStore.delete(explorer.getCardHolder().getCard().url(), new IAfterEditCallback() {
 								@Override
 								public void afterEdit(String url) {
 									explorer.displayCard(url, new CardAndCollectionDataStoreAdapter());
 								}
 							});
 						}
 					});
 					Buttons.makePushButton(buttonPanel, null, "Mysoftwarefm", false, new Runnable() {
 						@Override
 						public void run() {
 							explorer.showMySoftwareFm();
 						}
 					});
 					Swts.Row.setRowDataFor(100, 20, buttonPanel.getChildren());
 					explorer.addExplorerListener(IExplorerListener.Utils.sysout());
 					return explorerAndButton;
 				}
 			});
 		} finally {
 			facard.shutdown();
 			service.shutdownAndAwaitTermination(ServerConstants.clientTimeOut, TimeUnit.SECONDS);
 		}
 
 	}
 }
