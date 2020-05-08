 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.uniluebeck.itm.ep5.client;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.DecoratorPanel;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.InlineHTML;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.user.datepicker.client.DateBox;
 import de.uniluebeck.itm.ep5.poll.domain.IOption;
 import de.uniluebeck.itm.ep5.poll.domain.XODateOption;
 import de.uniluebeck.itm.ep5.poll.domain.XOOptionList;
 import de.uniluebeck.itm.ep5.poll.domain.XOTextOption;
 import de.uniluebeck.itm.ep5.poll.domain.xoPoll;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 /**
  * Main entry point.
  *
  * @author hoschi
  */
 public class MainEntryPoint implements EntryPoint {
 
 	private PollServiceGwtAsync service = (PollServiceGwtAsync) GWT.create(
 			PollServiceGwt.class);
 	private Panel showPollListPanel;
 	private Panel addPollPanel;
 	private Panel myRootPanel;
 	private Button addOptionListButton;
 	private Button validateAndSaveNewPollButton;
 	private Grid addPollFormGrid;
 	private TextBox username;
 
 	/**
 	 * Creates a new instance of MainEntryPoint
 	 */
 	public MainEntryPoint() {
 		addPollPanel = new VerticalPanel();
 		showPollListPanel = new VerticalPanel();
 		myRootPanel = new VerticalPanel();
 	}
 
 	/**
 	 * The entry point method, called automatically by loading a module
 	 * that declares an implementing class as an entry-point
 	 */
 	@Override
 	public void onModuleLoad() {
 		createAddPollForm();
 		createPollList();
 
 		myRootPanel.addStyleName("main");
 		RootPanel.get().add(myRootPanel);
 		myRootPanel.add(showPollListPanel);
 		myRootPanel.add(addPollPanel);
 	}
 
 	/*
 	 * create the "add new poll" form
 	 */
 	private void createAddPollForm() {
 		addPollPanel.clear();
 		addPollFormGrid = new Grid(4, 2);
 
 		addPollPanel.add(new InlineHTML("<h1>Add a new one</h1>"));
 
 		addPollFormGrid.setWidget(0, 0, new Label("title"));
 		TextBox titleBox = new TextBox();
 		addPollFormGrid.setWidget(0, 1, titleBox);
 
 		addPollFormGrid.setWidget(1, 0, new Label("is public"));
 		CheckBox isPublicBox = new CheckBox();
 		isPublicBox.setValue(Boolean.TRUE);
 		addPollFormGrid.setWidget(1, 1, isPublicBox);
 
 		addPollFormGrid.setWidget(2, 0, new Label("start date"));
 		DateBox startDateBox = new DateBox();
 		addPollFormGrid.setWidget(2, 1, startDateBox);
 
 		addPollFormGrid.setWidget(3, 0, new Label("end date"));
 		DateBox endDateBox = new DateBox();
 		addPollFormGrid.setWidget(3, 1, endDateBox);
 
 		addPollPanel.add(addPollFormGrid);
 
 		addEmptyRow(addPollPanel);
 
 		addOptionListButton = new Button("add an option list");
 		addOptionListButton.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				addOptionList();
 			}
 		});
 		addPollPanel.add(addOptionListButton);
 
 		addEmptyRow(addPollPanel);
 
 		validateAndSaveNewPollButton = new Button("save");
 		validateAndSaveNewPollButton.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				saveNewPoll();
 			}
 		});
 		addPollPanel.add(validateAndSaveNewPollButton);
 
 	}
 
 	/*
 	 * add an option list to the "add new poll" form
 	 */
 	private void addOptionList() {
 		throw new UnsupportedOperationException("Not yet implemented");
 	}
 
 	/*
 	 * save a new poll with the form data
 	 */
 	private void saveNewPoll() {
 		TextBox titleBox =
 				(TextBox) addPollFormGrid.getWidget(0, 1);
 		CheckBox isPublicBox =
 				(CheckBox) addPollFormGrid.getWidget(1, 1);
 		DateBox startDateBox =
 				(DateBox) addPollFormGrid.getWidget(2, 1);
 		DateBox endDateBox =
 				(DateBox) addPollFormGrid.getWidget(3, 1);
 
 		// check null
 		if (titleBox == null || isPublicBox == null || startDateBox == null || endDateBox ==
 				null) {
 			throw new RuntimeException("widget is null");
 		}
 
 		// validate
 		if (titleBox.getValue() == null || titleBox.getValue().equals("")) {
 			Window.alert("title can't be empty");
 			return;
 		}
 
 		xoPoll poll = new xoPoll();
 		poll.setEndDate(endDateBox.getValue());
 		poll.setPublic(isPublicBox.getValue());
 		poll.setStartDate(startDateBox.getValue());
 		poll.setTitle(titleBox.getValue());
 
 		this.service.addPoll(poll, new AsyncCallback<Void>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				Window.alert("RPC to addPoll() failed: " + caught.
 						getLocalizedMessage());
 			}
 
 			@Override
 			public void onSuccess(Void result) {
 				createAddPollForm();
 			}
 		});
 
 	}
 
 	/*
 	 * create main poll list
 	 */
 	private void createPollList() {
 		showPollListPanel.add(new InlineHTML("<h1>Poll List</h1>"));
 		// add username box
 		this.username = new TextBox();
 		Panel userpanel = new HorizontalPanel();
 		userpanel.add(new Label("username: "));
 		userpanel.add(this.username);
 		showPollListPanel.add(userpanel);
 
 		// add more lists
 		showPollListPanel.add(new InlineHTML(
 				"<h2>Add polls from other host</h2>"));
 		final TextBox otherHostUrl = new TextBox();
 		otherHostUrl.setWidth("400px");
 		final TextBox otherHostLocale = new TextBox();
 		otherHostLocale.setWidth("50px");
 		Button button = new Button("add");
 		button.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				createPollList(otherHostUrl.getText(), otherHostLocale.getText());
 			}
 		});
 
 
 		Panel addStuff = new HorizontalPanel();
 		addStuff.add(new Label("url: "));
 		addStuff.add(otherHostUrl);
 		addStuff.add(new Label("locale: "));
 		addStuff.add(otherHostLocale);
 		addStuff.add(button);
 		showPollListPanel.add(addStuff);
 
 		// localhost polls
 		createPollList("http://localhost:8080/poll?WSDL", "en");
 	}
 
 	/*
 	 * create a poll list with data, fetched from server
 	 * @url url to WSDL from other server
 	 * @locale string which represents a locale of the server
 	 */
 	private void createPollList(String url, String locale) {
 		final String myUrl = url;
 		final String myLocale = locale;
 		service.getPollTitles(url, locale, new AsyncCallback<List<xoPoll>>() {
 
 			public void onFailure(Throwable caught) {
 				Window.alert("RPC to getPollTitles() failed.");
 			}
 
 			public void onSuccess(List<xoPoll> result) {
 				Panel panel = new VerticalPanel();
 				final DecoratorPanel decorator = new DecoratorPanel();
 				decorator.setWidget(panel);
 
 				panel.add(new InlineHTML("<h2>List from <a href=\"" + myUrl +
 						"\">" + myUrl + "</a> (" + myLocale + ")</h2>"));
 
 				for (xoPoll poll : result) {
 					final xoPoll pollFinal = poll;
 					Panel item = new HorizontalPanel();
 					final Panel showPanel = new VerticalPanel();
 
 					Label label = new Label(poll.getTitle());
 					item.add(label);
 					Button showButton = new Button("show");
 					showButton.addClickHandler(new ClickHandler() {
 
 						@Override
 						public void onClick(ClickEvent event) {
 							showPoll(showPanel, pollFinal, myUrl, myLocale);
 						}
 					});
 					item.add(showButton);
 					item.add(new Button("delete"));
 
 					panel.add(item);
 					panel.add(showPanel);
 				}
 				// add status line
 				addEmptyRow(panel);
 				panel.add(new Label("fetched " + result.size() + " polls"));
 				addEmptyRow(panel);
 
 				// control buttons for list
 				Panel bottom = new HorizontalPanel();
 				Button updateButton = new Button("update");
 				Button removeButton = new Button("remove");
 				removeButton.addClickHandler(new ClickHandler() {
 
 					DecoratorPanel pollPanel = decorator;
 
 					public void onClick(ClickEvent event) {
 						showPollListPanel.remove(this.pollPanel);
 					}
 				});
 				bottom.add(updateButton);
 				bottom.add(removeButton);
 				panel.add(bottom);
 
 
 				showPollListPanel.add(decorator);
 			}
 		});
 	}
 
 	/*
 	 * show details of a partially loaded poll
 	 */
 	private void showPoll(Panel mainPanel, xoPoll pollInfo, String url,
 			String locale) {
 		mainPanel.clear();
 		final Panel mainPanelFinal = mainPanel;
 
 		service.getPoll(url, locale, pollInfo.getId(), new AsyncCallback<xoPoll>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				Window.alert("RPC failure in showPoll");
 			}
 
 			@Override
 			public void onSuccess(xoPoll poll) {
 				String publicString = "";
 				if (poll.isPublic()) {
 					publicString += "is public";
 				}
 				mainPanelFinal.add(new Label(publicString));
 
 				// TODO: test this with local poll which have this dates instead
 				// of web service polls
 				String dates = "";
 				if (poll.getStartDate() != null) {
 					dates += "start date is: " + DateTimeFormat.
 							getShortDateFormat().format(poll.getStartDate()) +
 							". ";
 				}
 				if (poll.getEndDate() != null) {
 					dates += "end date is: " + DateTimeFormat.getShortDateFormat().
 							format(poll.getEndDate()) + ".";
 				}
 				mainPanelFinal.add(new Label(dates));
 
 				mainPanelFinal.add(buildPollGrid(poll));
 
 				addEmptyRow(mainPanelFinal);
 			}
 		});
 
 
 	}
 
 	private Widget buildPollGrid(xoPoll poll) {
 		FlexTable t = new FlexTable();
 		t.setStylePrimaryName("pollgrid");
 		Set votersSet = new HashSet();
		votersSet.add(username.getText());
 
 		if (poll != null) {
 			// create headers
 			int optionListCounter = 1;
 			for (XOOptionList optionList : poll.getOptionLists()) {
 				t.setText(0, optionListCounter, optionList.getTitle());
 				int optionCounter = 0;
 				for (IOption option : optionList.getOptions()) {
 					XOTextOption text;
 					XODateOption date;
 
 					int column;
 
 					// if this is the first entry there is no row 1, so
 					// getCellCount(1) throws out of bounds exception
 					if (t.getRowCount() == 1) {
 						column = 1;
 					} else {
 						column = t.getCellCount(1);
 					}
 
 					// make entry
 					if (option instanceof XOTextOption) {
 						text = (XOTextOption) option;
 						t.setText(1, column, text.getStrings().get(
 								0).getText());
 
 					} else if (option instanceof XODateOption) {
 						date = (XODateOption) option;
						t.setText(1, column, DateTimeFormat.getLongTimeFormat().
 								format(date.getDate()));
 
 					} else {
 						throw new RuntimeException("unknown option type");
 					}
 
 					// add voters to hash set
 					for (String voter : option.getVotes()) {
 						votersSet.add(voter);
 					}
 					optionCounter++;
 				}
 
 
 				t.getFlexCellFormatter().setColSpan(0, optionListCounter,
 						optionCounter);
 				optionListCounter++;
 			}
 			// create checkboxes
 			if (poll.getOptionLists().size() > 0) {
 				Iterator iter = votersSet.iterator();
 				int row = 2;
 				while (iter.hasNext()) {
 					String voter = (String) iter.next();
 					t.setWidget(row, 0, new Label(voter));
 
 					// create checkboxes
 					for (XOOptionList optionList : poll.getOptionLists()) {
 						for (IOption option : optionList.getOptions()) {
 							int column = t.getCellCount(row);
 							VoteBox box = new VoteBox();
 
 							box.setOptionId(option.getId());
 							box.setValue(Boolean.FALSE);
 							box.setEnabled(false);
 							if (option.getVotes().contains(voter)) {
 								// add a checked checkbox
 								box.setValue(Boolean.TRUE);
 							}
 							if (voter.equals(username.getText())) {
 								// current user can delete his votes
 								box.setEnabled(true);
 							}
 							t.setWidget(row, column, box);
 						}
 					}
 
 					// add save button
 					if (voter.equals(username.getText())) {
 						Button saveVotesButton = new Button("save");
 						t.setWidget(row, t.getCellCount(row), saveVotesButton);
 					}
 					row++;
 				}
 			}
 		}
 		return t;
 	}
 
 	/*
 	 * add a empty row to a panel for styling issues
 	 */
 	private void addEmptyRow(Panel panel) {
 		panel.add(new InlineHTML("<br/>"));
 	}
 }
