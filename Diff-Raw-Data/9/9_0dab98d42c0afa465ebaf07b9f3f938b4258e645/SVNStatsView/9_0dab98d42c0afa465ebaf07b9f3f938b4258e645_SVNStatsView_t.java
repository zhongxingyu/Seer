 package com.redhat.automationportalui.client.pav;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import com.google.gwt.cell.client.Cell;
 import com.google.gwt.cell.client.DatePickerCell;
 import com.google.gwt.cell.client.TextInputCell;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Style.FontWeight;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.PushButton;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.view.client.SingleSelectionModel;
 import com.redhat.automationportalui.client.AutomationPortalUIClientFactory;
 import com.redhat.automationportalui.client.constants.AutomationPortalUIConstants;
 import com.redhat.automationportalui.client.data.AutomationPortalResponseData;
 import com.redhat.automationportalui.client.data.ConfigXMLData;
 import com.redhat.automationportalui.client.resources.APUI_Errors;
 import com.redhat.automationportalui.client.resources.CommonUIStrings;
 import com.redhat.automationportalui.client.resources.SVNStatsUIStrings;
 import com.redhat.automationportalui.client.template.AutomationPortalUITemplate;
 import com.google.gwt.http.client.Request;
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.http.client.RequestException;
 import com.google.gwt.http.client.Response;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
 
 /**
  * A GWT View that represents the SVNStats UI
  */
 public class SVNStatsView
 {
 	private static interface GetValue<C>
 	{
 		C getValue(final ConfigXMLData entry);
 	}
 
 	private final static String REST_ENDPOINT = "rest/SVNStats/get/json/Execute";
 	private final AutomationPortalUITemplate template;
 	private final SVNStatsUIStrings uiStrings;
 	private final CommonUIStrings commonUiStrings;
 	private TextBox message;
 	private TextArea output;
 	private PushButton go;
 	private List<ConfigXMLData> entires = new ArrayList<ConfigXMLData>();
 
 	public SVNStatsView(final AutomationPortalUIClientFactory clientFactory, final AutomationPortalUITemplate template, final CommonUIStrings commonUiStrings, final APUI_Errors apuiErrors)
 	{
 		/* Get the translates log messages */
 		uiStrings = (SVNStatsUIStrings) GWT.create(SVNStatsUIStrings.class);
 		this.commonUiStrings = commonUiStrings;
 		this.template = template;
 	}
 
 	public void display()
 	{
 		template.getSubTitle().setText(commonUiStrings.SVNStats());
 
 		final VerticalPanel topLevelPanel = new VerticalPanel();
 
 		final HTML underConstruction = new HTML(commonUiStrings.UnderConstruction());
 		underConstruction.getElement().getStyle().setColor("red");
 		topLevelPanel.add(underConstruction);
 
 		topLevelPanel.add(new HTML(uiStrings.Description()));
 
 		final HTML descriptionLineTwo = new HTML(uiStrings.DescriptionLineTwo());
 		descriptionLineTwo.getElement().getStyle().setMarginBottom(2, Unit.EM);
 		topLevelPanel.add(descriptionLineTwo);
 
 		final CellTable<ConfigXMLData> table = new CellTable<ConfigXMLData>();
 
 		final DateTimeFormat dateFormat = DateTimeFormat.getFormat(PredefinedFormat.DATE_MEDIUM);
 		table.addColumn(createColumn(new DatePickerCell(dateFormat), new GetValue<Date>()
 		{
 			@Override
 			public Date getValue(final ConfigXMLData entry)
 			{
 				return entry.getFromDate();
 			}
 		}), uiStrings.FromDate());
 
 		table.addColumn(createColumn(new TextInputCell(), new GetValue<String>()
 		{
 			@Override
 			public String getValue(final ConfigXMLData entry)
 			{
 				return entry.getPath();
 			}
 		}), uiStrings.Path());
 
 		table.addColumn(createColumn(new TextInputCell(), new GetValue<String>()
 		{
 			@Override
 			public String getValue(final ConfigXMLData entry)
 			{
 				return entry.getEntry();
 			}
 		}), uiStrings.Entry());
 
 		topLevelPanel.add(table);
 
 		table.setRowData(entires);
 
 		final SingleSelectionModel<ConfigXMLData> selectionModel = new SingleSelectionModel<ConfigXMLData>();
 		table.setSelectionModel(selectionModel);
 		
 		/* create a panel to hold the buttons */
 		final HorizontalPanel tableButtonPanel = new HorizontalPanel();
 		tableButtonPanel.getElement().getStyle().setMarginTop(2, Unit.EM);
 		topLevelPanel.add(tableButtonPanel);
 
 		final Button addButton = new Button(uiStrings.AddEntry(), new ClickHandler()
 		{
			@Override
 			public void onClick(final ClickEvent event)
 			{
 				entires.add(new ConfigXMLData(new Date(), "", ""));
 				table.setRowData(entires);
 				table.setRowCount(entires.size());
 			}
 		});
 		addButton.setWidth("10em");
 		addButton.setHeight("2em");		
 		tableButtonPanel.add(addButton);
 
 		final Button deleteButton = new Button(uiStrings.RemoveEntry());
 		deleteButton.addClickHandler(new ClickHandler()
 		{
 
 			@Override
 			public void onClick(final ClickEvent event)
 			{
 				final ConfigXMLData selected = selectionModel.getSelectedObject();
 				if (selected != null)
 				{
 					entires.remove(selected);
 					table.setRowData(entires);
 					table.setRowCount(entires.size());
 				}
 			}
 		});
 		deleteButton.setWidth("10em");
 		deleteButton.setHeight("2em");	
 		tableButtonPanel.add(deleteButton);
 		
 
 		final Grid grid = new Grid(3, 2);
 		topLevelPanel.add(grid);
 
 		final HTML resultsLabel = new HTML(commonUiStrings.Results());
 		resultsLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
 		resultsLabel.getElement().getStyle().setMarginTop(2, Unit.EM);
 		grid.setWidget(0, 0, resultsLabel);
 
 		message = new TextBox();
 		message.setReadOnly(true);
 		message.setWidth("40em");
 		grid.setWidget(1, 0, new HTML(commonUiStrings.Message()));
 		grid.setWidget(1, 1, message);
 
 		output = new TextArea();
 		output.setReadOnly(true);
 		output.setWidth("40em");
 		output.setHeight("10em");
 		grid.setWidget(2, 0, new HTML(commonUiStrings.Output()));
 		grid.setWidget(2, 1, output);
 
 		go = new PushButton(commonUiStrings.Go());
 		go.setWidth("10em");
 		go.setHeight("2em");
 		go.getElement().getStyle().setProperty("textAlign", "center");
 		go.getElement().getStyle().setProperty("display", "table-cell");
 		go.getElement().getStyle().setProperty("verticalAlign", "middle");
 		go.addClickHandler(new ClickHandler()
 		{
 			@Override
 			public void onClick(final ClickEvent event)
 			{
 				run();
 			}
 		});
 		// topLevelPanel.add(go);
 
 		template.getContentPanel().setWidget(topLevelPanel);
 
 		enableUI(true);
 	}
 
 	private void run()
 	{
 		enableUI(false);
 
 		this.message.setText("");
 		this.output.setText("");
 
 		final String restUrl = AutomationPortalUIConstants.REST_SERVER_URL + REST_ENDPOINT;
 
 		// Send request to server and catch any errors.
 		final RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, restUrl);
 
 		try
 		{
 			builder.sendRequest(null, new RequestCallback()
 			{
 				@Override
 				public void onError(final Request request, final Throwable exception)
 				{
 					enableUI(true);
 				}
 
 				@Override
 				public void onResponseReceived(final Request request, final Response response)
 				{
 					if (200 == response.getStatusCode())
 					{
 						final String jsonResponse = response.getText();
 						final AutomationPortalResponseData responseData = AutomationPortalResponseData.convert(jsonResponse);
 						message.setText(responseData.getMessage());
 						output.setText(responseData.getOutput());
 					}
 					else
 					{
 						// displayError("Couldn't retrieve JSON (" +
 						// response.getStatusText() + ")");
 					}
 
 					enableUI(true);
 				}
 			});
 		}
 		catch (final RequestException ex)
 		{
 			// displayError("Couldn't retrieve JSON");
 			enableUI(true);
 
 		}
 	}
 
 	private void enableUI(final boolean enabled)
 	{
 		template.showLoadingImage(!enabled);
 		go.setEnabled(enabled);
 	}
 
 	private <C> Column<ConfigXMLData, C> createColumn(final Cell<C> cell, final GetValue<C> getter)
 	{
 		Column<ConfigXMLData, C> column = new Column<ConfigXMLData, C>(cell)
 		{
 			@Override
 			public C getValue(final ConfigXMLData object)
 			{
 				return getter.getValue(object);
 			}
 		};
 
 		return column;
 	}
 }
