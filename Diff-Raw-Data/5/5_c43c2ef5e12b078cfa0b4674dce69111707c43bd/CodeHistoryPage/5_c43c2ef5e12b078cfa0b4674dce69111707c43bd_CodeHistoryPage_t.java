 package org.smartsnip.client;
 
 import java.util.List;
 
 import org.smartsnip.shared.ISnippet;
 import org.smartsnip.shared.NoAccessException;
 import org.smartsnip.shared.NotFoundException;
 import org.smartsnip.shared.XCode;
 import org.smartsnip.shared.XSnippet;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.AbsolutePanel;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 /**
  * GWT page for viewing the code history of a given snippet
  * 
  * @author Felix Niederwanger
  * 
  */
 public class CodeHistoryPage extends Composite {
 	/** Associated snippet the code is displayed from */
 	private final XSnippet snippet;
 
 	/** Fetched list of the code history or null, if currently fetching */
 	private List<XCode> history = null;
 
 	/* Controls */
 
 	private final Panel rootPanel = new VerticalPanel();
 
 	private final Label lblTitle = new Label();
 	private final Label lblStatus = new Label();
 
 	private final ScrollPanel scrollHistory = new ScrollPanel();
 	private final Panel codePanel = new AbsolutePanel();
 
 	private PrettyPrint highligter = null;
 
 	/* End of controls */
 
 	public CodeHistoryPage(final XSnippet snippet) {
 		this.snippet = snippet;
 
 		lblTitle.setText("View code history of " + snippet.title);
 
 		rootPanel.add(lblTitle);
 		rootPanel.add(lblStatus);
 		rootPanel.add(scrollHistory);
 		rootPanel.add(codePanel);
 
 		initWidget(rootPanel);
 		applyStyle();
 
 		fetchHistory();
 	}
 
 	private void applyStyle() {
 		lblTitle.setStyleName("h3");
 	}
 
 	/** Updates the component */
 	public void update() {
 		fetchHistory();
 	}
 
 	private void fetchHistory() {
 		history = null;
 		lblStatus.setText("Fetching history ... ");
 		disableControls();
 		ISnippet.Util.getInstance().getCodeHistory(snippet.hash, new AsyncCallback<List<XCode>>() {
 
 			@Override
 			public void onSuccess(List<XCode> result) {
 				if (result == null) {
 					onFailure(new IllegalArgumentException("Null returned"));
 					return;
 				}
 
 				setHistory(result);
 				enableControls();
 				lblStatus.setText("History fetched. " + result.size() + " elements received");
 			}
 
 			@Override
 			public void onFailure(Throwable caught) {
 				lblStatus.setText("Error while fetching: " + caught.getMessage());
 				enableControls();
 			}
 		});
 	}
 
 	/**
 	 * Sets the history
 	 * 
 	 * @param history
 	 *            to be set
 	 */
 	private void setHistory(List<XCode> history) {
 		if (history == null)
 			return;
 		this.history = history;
 
 		scrollHistory.clear();
		final VerticalPanel vrtPanel = new VerticalPanel();
		scrollHistory.add(vrtPanel);
 		for (XCode code : history) {
 			if (code != null)
				vrtPanel.add(createCodePanel(code));
 		}
 	}
 
 	/** Creates the panel for a single code segment */
 	private Panel createCodePanel(final XCode code) {
 		Panel result = new HorizontalPanel();
 
 		final Anchor anch = new Anchor("Version " + code.version);
 		anch.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				selectCode(code);
 			}
 		});
 		result.add(anch);
 
 		if (code.downloadAbleSource) {
 			final Anchor anchDownload = new Anchor("Download source");
 			anchDownload.addClickHandler(new ClickHandler() {
 
 				@Override
 				public void onClick(ClickEvent event) {
 					ISnippet.Util.getInstance().getDownloadSourceTicket(code.id, new AsyncCallback<Long>() {
 
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
 			result.add(anchDownload);
 		}
 
 		return result;
 	}
 
 	/**
 	 * Disable the controls
 	 */
 	private void disableControls() {
 
 	}
 
 	/**
 	 * Enable the controls
 	 */
 	private void enableControls() {
 
 	}
 
 	/** When a code object is selected */
 	private void selectCode(final XCode code) {
 		codePanel.clear();
 		this.highligter = new PrettyPrint(code);
 
 		final Label lblTitle = new Label("Displaying code version: " + code.version);
 
 		codePanel.add(lblTitle);
 		codePanel.add(highligter);
 
 		final Button btnSetCurrent = new Button("Set as current code");
 		btnSetCurrent.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				btnSetCurrent.setEnabled(false);
 				btnSetCurrent.setText("Setting current version ...");
 				lblStatus.setText("Setting current version ...");
 
 				ISnippet.Util.getInstance().editCode(code.snippetId, code.code, new AsyncCallback<Void>() {
 
 					@Override
 					public void onFailure(Throwable caught) {
 						lblStatus.setText("Failed setting current code: " + caught.getMessage());
 						btnSetCurrent.setEnabled(true);
 						btnSetCurrent.setText("Retry set ad current code");
 					}
 
 					@Override
 					public void onSuccess(Void result) {
 						btnSetCurrent.setText("Set as current code");
 						lblStatus.setText("Current code set");
 						update();
 					}
 				});
 			}
 		});
 
 		codePanel.add(btnSetCurrent);
 	}
 }
