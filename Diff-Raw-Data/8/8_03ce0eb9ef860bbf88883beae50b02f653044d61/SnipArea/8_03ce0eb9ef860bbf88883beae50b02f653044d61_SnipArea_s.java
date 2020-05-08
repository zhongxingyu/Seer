 package org.smartsnip.client;
 
 import org.smartsnip.shared.ISnippet;
 import org.smartsnip.shared.NoAccessException;
 import org.smartsnip.shared.NotFoundException;
 import org.smartsnip.shared.XSnippet;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 /**
  * 
  * 
  * @author Paul
  * 
  * 
  *         A composed Widget to display the a snippet
  * 
  */
 public class SnipArea extends Composite {
 
 	private final XSnippet snippet;
 
 	private final VerticalPanel vertPanel;
 	private final HorizontalPanel horPanel;
 	private final ScrollPanel scrPanel;
 	private final Grid properties;
 	private final Grid anchorGrid;
 	private final Label title;
 	private final Label description;
 	private final Label language;
 	private final Label lblViewCount;
 	private final Label license;
 	private final Label lblOwner;
 
 	private final Anchor anchUpload;
 	private final Anchor anchDownload;
 	private final Rating rating;
 	private final Label lblAverageRating;
 
 	private final HTMLPanel snipFull;
 
 	private final Button btnFav;
 	private final Button btnEdit;
 	private final Button btnDelete;
 
 	/**
 	 * @see com.google.gwt.user.client.ui.Widget#onLoad()
 	 */
 	@Override
 	protected void onLoad() {
 		Control.prettyPrint();
 	};
 
 	/**
 	 * Initializes the widget
 	 * 
 	 * @param mySnip
 	 *            a XSnippet
 	 */
 	SnipArea(final XSnippet mySnip) {
 
 		snippet = mySnip;
 
 		vertPanel = new VerticalPanel();
 		horPanel = new HorizontalPanel();
 		scrPanel = new ScrollPanel();
 		scrPanel.setWidth("700px");
 		scrPanel.setStyleName("scrollSnippet");
 		anchorGrid = new Grid(3, 1);
 		properties = new Grid(2, 7);
 		properties.setStyleName("properties");
 		title = new Label(mySnip.title);
 		title.setStyleName("h3");
 		description = new Label(mySnip.description);
 		description.setStyleName("desc");
 		language = new Label(mySnip.language);
 		language.setStyleName("txt");
 		lblViewCount = new Label(mySnip.viewcount + "");
 		lblViewCount.setStyleName("txt");
 		license = new Label(mySnip.license);
 		license.setStyleName("txt");
 		lblOwner = new Label(getOwnerText(mySnip));
 		lblOwner.setStyleName("txt");
 
 		snipFull = new HTMLPanel(mySnip.codeHTML);
 		snipFull.setWidth("680px");
 		snipFull.setStyleName("snipHtml");
 		anchUpload = new Anchor("Upload source");
 		anchDownload = new Anchor("Download source");
 		rating = new Rating(5);
 		btnFav = new Button("Favorites");
 		btnEdit = new Button("Edit");
 		btnDelete = new Button("Delete");
 		lblAverageRating = new Label("" + snippet.rating);
 		lblAverageRating.setStyleName("txt");
 
 		anchorGrid.setWidget(0, 0, anchUpload);
 		anchorGrid.setWidget(1, 0, anchDownload);
 		anchorGrid.setWidget(2, 0, rating);
 
 		properties.setWidget(0, 0, new Label("Language"));
 		properties.setWidget(1, 0, language);
 		properties.setWidget(0, 1, new Label("License"));
 		properties.setWidget(1, 1, license);
 		properties.setWidget(0, 2, new Label("View count"));
 		properties.setWidget(1, 2, lblViewCount);
 		properties.setWidget(0, 3, new Label("Average Rating"));
 		properties.setWidget(1, 3, lblAverageRating);
 		properties.setWidget(0, 4, new Label("Owner"));
 		properties.setWidget(1, 4, lblOwner);
 
 		btnFav.setText(snippet.isFavorite ? "Remove favorite" : "Add favorite");
 		btnFav.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				btnFav.setEnabled(false);
 
 				if (snippet.isFavorite) {
 					btnFav.setText("Removing favourite ... ");
 
 					ISnippet.Util.getInstance().removeFavorite(snippet.hash, new AsyncCallback<Void>() {
 
 						@Override
 						public void onSuccess(Void result) {
 							snippet.isFavorite = false;
 							updateFavortiteStatus();
 						}
 
 						@Override
 						public void onFailure(Throwable caught) {
 							Control.myGUI.showErrorPopup("Error removing snippet to favorites", caught);
 							btnFav.setEnabled(true);
 						}
 					});
 				} else {
 					btnFav.setText("Adding favorites ... ");
 
 					ISnippet.Util.getInstance().addToFavorites(snippet.hash, new AsyncCallback<Void>() {
 
 						@Override
 						public void onSuccess(Void result) {
 							snippet.isFavorite = true;
 							updateFavortiteStatus();
 						}
 
 						@Override
 						public void onFailure(Throwable caught) {
 							Control.myGUI.showErrorPopup("Error adding snippet to favorites", caught);
 							btnFav.setEnabled(true);
 						}
 					});
 				}
 			}
 		});
 
 		btnEdit.setVisible(false);
 		btnEdit.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				Control.myGUI.showEditSnippetForm(snippet);
 				update();
 			}
 		});
 		btnDelete.setVisible(false);
 		btnDelete.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				if (Control.myGUI.showConfirmPopup("Really delete this snippet?\nThis action cannot be undone!", "Confirm action") == false)
 					return;
 
 				setDeleted();
 			}
 		});
 
 		rating.setStyleName("toollink");
 		rating.setRatingHandler(new Rating.RatingHandler() {
 
 			@Override
 			public void unrate() {
 				rate(0);
 			}
 
 			@Override
 			public void rate(final int rate) {
 				rating.setEnabled(false);
 
 				ISnippet.Util.getInstance().rateSnippet(snippet.hash, rate, new AsyncCallback<Void>() {
 
 					@Override
 					public void onSuccess(Void result) {
 						rating.setEnabled(true);
 						rating.setRatingStatus(rate);
 						lblAverageRating.setText("Getting rating ... ");
 						update();
 					}
 
 					@Override
 					public void onFailure(Throwable caught) {
 						rating.setEnabled(false);
 						Control.myGUI.showErrorPopup("Rating failed", caught);
 
 						caught.printStackTrace(System.err);
 					}
 				});
 			}
 		});
 		anchUpload.setVisible(false);
 		anchUpload.setStyleName("toollink");
 		anchUpload.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				UploadCode.show(snippet.codeID);
 			}
 		});
 
 		anchDownload.setStyleName("toollink");
 		anchDownload.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				anchDownload.setEnabled(false);
 
 				ISnippet.Util.getInstance().getDownloadSourceTicket(snippet.hash, new AsyncCallback<Long>() {
 
 					@Override
 					public void onSuccess(Long result) {
 						// Got the download link
 						Control.myGUI.showDownloadPopup("Downloadticket received ... \nClick on the following link to download ... ",
 								convertToLink(result));
 					}
 
 					@Override
 					public void onFailure(Throwable caught) {
 						anchDownload.setEnabled(true);
 						if (caught == null)
 							return;
 						if (caught instanceof NoAccessException) {
 							Control.myGUI.showErrorPopup("Access denied", caught);
 						} else if (caught instanceof NotFoundException) {
 							Control.myGUI.showErrorPopup("Snippet hash id not found", caught);
 						} else
 							Control.myGUI.showErrorPopup("Error", caught);
 					}
 
 					/* Converts the download ticket into a link */
 					private String convertToLink(long id) {
 
 						String result = GWT.getHostPageBaseURL();
 						if (!result.endsWith("/"))
 							result += "/";
 
 						result += "downloader?ticket=" + id;
 						return result;
 					}
 				});
 			}
 		});
 
 		anchDownload.setEnabled(false);
 		anchDownload.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				requestDownload();
 			}
 		});
 		ISnippet.Util.getInstance().hasDownloadableSource(snippet.hash, new AsyncCallback<Boolean>() {
 
 			@Override
 			public void onSuccess(Boolean result) {
 				if (result) {
 					anchDownload.setTitle("Click here to request a download ticket");
 					anchDownload.setEnabled(true);
 				} else {
 					anchDownload.setTitle("This snippet does not have any downloadable source");
 					anchDownload.setEnabled(false);
 				}
 			}
 
 			@Override
 			public void onFailure(Throwable caught) {
 				anchDownload.setTitle("Error" + (caught == null ? "" : ": " + caught.getMessage()));
 				anchDownload.setEnabled(false);
 			}
 		});
 
 		ISnippet.Util.getInstance().canEdit(snippet.hash, new AsyncCallback<Boolean>() {
 
 			@Override
 			public void onSuccess(Boolean result) {
 				btnEdit.setVisible(result);
 				btnDelete.setVisible(result);
 				anchUpload.setVisible(result);
 			}
 
 			@Override
 			public void onFailure(Throwable caught) {
 				// Something went wrong ...
 				// TODO: Error handling ...
 			}
 		});
 
 		horPanel.add(btnFav);
 		horPanel.add(btnDelete);
 		horPanel.add(btnEdit);
 		horPanel.add(anchorGrid);
 
 		scrPanel.add(snipFull);
 
 		vertPanel.add(title);
 		vertPanel.add(description);
 		vertPanel.add(properties);
 		vertPanel.add(scrPanel);
 		vertPanel.add(horPanel);
 
 		initWidget(vertPanel);
 		// Give the overall composite a style name.
 		setStyleName("snipArea");
 
 		updateFavortiteStatus();
 	}
 
 	/**
 	 * Reloads the snippet and updates the component
 	 */
 	public void update() {
 		ISnippet.Util.getInstance().getSnippet(snippet.hash, new AsyncCallback<XSnippet>() {
 
 			@Override
 			public void onSuccess(XSnippet result) {
 				if (result == null)
 					return;
 				if (result.hash != snippet.hash)
 					return;
 
 				// Copy properties
 				snippet.title = result.title;
 				snippet.description = result.description;
 				snippet.code = result.code;
 				snippet.category = result.category;
 				snippet.codeHTML = result.codeHTML;
 				snippet.isFavorite = result.isFavorite;
 				snippet.language = result.language;
 				snippet.license = result.license;
 				snippet.owner = result.owner;
 				snippet.viewcount = result.viewcount;
 				snippet.tags = result.tags;
 				snippet.rating = result.rating;
 
 				snippet.canDelete = result.canDelete;
 				snippet.canEdit = result.canEdit;
 				snippet.canRate = result.canRate;
 
 				SnipArea.this.updateComponents();
 			}
 
 			@Override
 			public void onFailure(Throwable caught) {
 				// Ignore it
 
 			}
 		});
 	}
 
 	/**
 	 * Updates all components
 	 */
 	private void updateComponents() {
 		String msgTitle = snippet.title;
 		if (snippet.isOwn)
 			msgTitle += " (own snippet)";
 		title.setText(msgTitle);
 		description.setText(snippet.description);
 		language.setText(snippet.language);
 		license.setText(snippet.license);
 		lblAverageRating.setText("Rating: " + getRating(snippet.rating));
 		lblViewCount.setText(snippet.viewcount + "");
 		rating.setRatingStatus(snippet.myRating);
 		lblOwner.setText(getOwnerText(snippet));
 
 		btnEdit.setVisible(snippet.canEdit);
 		btnDelete.setVisible(snippet.canDelete);
		updateFavortiteStatus();
 		rating.setEnabled(snippet.canRate);
 
 		updateFavortiteStatus();
 
 	}
 
 	/**
 	 * Updates the behaviour depending on favourites status
 	 */
 	private void updateFavortiteStatus() {
 		btnFav.setEnabled(true);
 		if (snippet.isFavorite) {
 			btnFav.setText("Remove favorite");
 		} else {
 			btnFav.setEnabled(true);
 			btnFav.setText("Add favorites");
 		}
 	}
 
 	/**
 	 * Formats the rating
 	 * 
 	 * @param rating
 	 *            to be formatted
 	 * @return formatted string out of a rating
 	 */
 	private String getRating(float rating) {
 		int temp = (int) (rating * 10);
 		rating = (float) (temp / 10.0);
 		return Float.toString(rating);
 	}
 
 	/**
 	 * Gets the owner description based on a xsnippet
 	 * 
 	 * @param mySnip
 	 *            xsnippet the text is created from
 	 * @return description for the owner field
 	 */
 	private String getOwnerText(XSnippet mySnip) {
 		if (mySnip == null)
 			return "";
 		if (mySnip.isOwn)
 			return mySnip.owner + " (You)";
 		else
 			return mySnip.owner;
 	}
 
 	/**
 	 * Disabled the whole control, when the snippet is deleted
 	 */
 	private void setDeleted() {
 		btnDelete.setEnabled(false);
 		btnEdit.setEnabled(false);
 		btnFav.setEnabled(false);
 		anchDownload.setEnabled(false);
 		anchUpload.setEnabled(false);
 		rating.setEnabled(false);
 	}
 
 	/** Requests a download */
 	private void requestDownload() {
 
 	}
 }
