 package kea.kme.pullpit.client.UI;
 
 import kea.kme.pullpit.client.UI.widgets.ShowWidget;
 import kea.kme.pullpit.client.UI.widgets.TextBoxWidget;
 import kea.kme.pullpit.client.objects.ShowState;
 import kea.kme.pullpit.client.services.FileService;
 import kea.kme.pullpit.client.services.FileServiceAsync;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.CaptionPanel;
 import com.google.gwt.user.client.ui.DockPanel;
 import com.google.gwt.user.client.ui.FileUpload;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.FormPanel;
 import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
 import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 //import kea.kme.pullpit.client.objects.Venue;
 //import kea.kme.pullpit.client.objects.Venue;
 
 public class DisplayItem {
 
 	char type;
 	private PullPitConstants constants = GWT.create(PullPitConstants.class);
 
 	@SuppressWarnings("unused")
 	private Label showIdLabel, bandNameLabel, venueLabel, dateLabel,
 			promoterLabel, bandIdLabel, bandCountryLabel, bandPromoterLabel,
 			bandAgentLabel;
 
 	@SuppressWarnings("unused")
 	private TextBoxWidget stateTextBox, feeTextBox, provisionTextBox,
 			profitSplitTextBox, ticketPriceTextBox, ticketsSoldTextBox,
 			commentsTextBox, kodaPctTextBox, VATTextBox, lastEditTextBox;
 
 	@SuppressWarnings("unused")
 	private Label stateLabel, feeLabel, feeCurrencyLabel, provisionLabel,
 			productionTypeLabel, profitSplitLabel, ticketPriceLabel,
 			ticketsSoldLabel, commentsLabel, provisionCurrencyLabel,
 			kodaPctLabel, VATLabel, lastEditLabel;
 
 	private CaptionPanel showCapPanel;
 	private CaptionPanel bandCapPanel;
 	private CaptionPanel editableCapPanel;
 	private CaptionPanel venuesCapPanel;
 
 	private FlexTable showTable;
 	private FlexTable bandTable;
 	private FlexTable editableTable;
 	private FlexTable venuesTable;
 
 	private DockPanel dockPanel;
 
 	private ShowWidget currentShow;
 
 	private ListBox provisionCurrencyListBox;
 	private ListBox feeCurrencyListBox;
 	private ListBox productionTypeListBox;
 	private FormPanel myForm;
 	private CaptionPanel fileCapPanel;
 	
 	FileServiceAsync fileService = GWT.create(FileService.class);
 
 	public DisplayItem(char type, ShowWidget currentShow) {
 
 		this.currentShow = currentShow;
 		this.type = type;
 
 		if (type == 'D') {
 			displayDocument();
 		} else if(type == 'S') {
 			displayShow();
 		}
 		else {
 			displayDocument();
 			
 		}
 
 	}
 
 	private void displayDocument() {
 
 	}
 
 	private void displayShow() {
 		showCapPanel = new CaptionPanel();
 		showCapPanel.setCaptionHTML(constants.Show());
 		showCapPanel.setStyleName("capPanel");
 
 		bandCapPanel = new CaptionPanel();
 		bandCapPanel.setCaptionHTML(constants.Band());
 		bandCapPanel.setStyleName("capPanel");
 
 		editableCapPanel = new CaptionPanel();
 		editableCapPanel.setCaptionHTML("Variable");
 		editableCapPanel.setStyleName("capPanel");
 		
 		fileCapPanel = new CaptionPanel();
 		fileCapPanel.setCaptionHTML(constants.files());
 		fileCapPanel.setStyleName("capPanel");
 
 		dockPanel = new DockPanel();
 
 		Label header = new Label(currentShow.getShow().getBand().getBandName()
 				+ "(" + currentShow.getShow().getBand().getBandCountry()
 				+ ") - " + currentShow.getShow().getVenuesString() + " D.: "
 				+ currentShow.getShow().getDate());
 		header.setStyleName("header");
 
 		dockPanel.add(header, DockPanel.NORTH);
 		dockPanel.add(makeFileCaptionPanel(),DockPanel.NORTH);
 		dockPanel.add(makeShowCaptionPanel(), DockPanel.WEST);
 		dockPanel.add(makeBandCaptionPanel(), DockPanel.WEST);
 		dockPanel.add(makeEditableCaptionTable(), DockPanel.WEST);
 
 		UIMain.getInstance().changeContentTo(dockPanel);
 	}
 
 	public CaptionPanel makeShowCaptionPanel() {
 		showTable = new FlexTable();
 		showTable.setTitle(constants.Show());
 
 		showTable.setWidget(1, 0, showIdLabel = new Label(constants.ShowID()
 				+ ": " + currentShow.getShow().getShowID()));
 		showTable.setWidget(2, 0, bandNameLabel = new Label(constants.Band()
 				+ ": " + currentShow.getShow().getBand().getBandName()));
 		showTable.setWidget(3, 0, venueLabel = new Label(constants.venue()
 				+ ": " + currentShow.getShow().getVenuesString()));
 		showTable.setWidget(4, 0, dateLabel = new Label(constants.date() + ": "
 				+ currentShow.getShow().getDate()));
 		showTable.setWidget(5, 0,
 				promoterLabel = new Label(constants.promoter() + ": "
 						+ currentShow.getShow().getPromoter().getPromoName()));
 
 		showCapPanel.add(showTable);
 		return showCapPanel;
 	}
 
 	public CaptionPanel makeBandCaptionPanel() {
 		bandTable = new FlexTable();
 		bandTable.setTitle("band");
 		bandTable.setWidget(1, 0, bandIdLabel = new Label(constants.bandID()
 				+ ": " + currentShow.getShow().getBand().getBandID()));
 		bandTable.setWidget(2, 0, bandNameLabel = new Label(constants.Band()
 				+ ": " + currentShow.getShow().getBand().getBandName()));
 		bandTable.setWidget(3, 0,
 				bandCountryLabel = new Label(constants.country() + ": "
 						+ currentShow.getShow().getBand().getBandCountry()));
 		bandTable.setWidget(4, 0,
 				bandPromoterLabel = new Label(constants.promoter()
 						+ ": "
 						+ currentShow.getShow().getBand().getPromoter()
 								.getPromoName()));
 		bandTable.setWidget(5, 0, bandAgentLabel = new Label(constants.agent()
 				+ ": " + currentShow.getShow().getBand().getAgentsToString()));
 		bandCapPanel.add(bandTable);
 		return bandCapPanel;
 	}
 
 	public CaptionPanel makeEditableCaptionTable() {
 		editableTable = new FlexTable();
 		editableTable.setTitle("variable");
 
 		feeCurrencyListBox = new ListBox();
 		feeCurrencyListBox.addItem(constants.dkk());
 		feeCurrencyListBox.addItem(constants.usd());
 		feeCurrencyListBox.addItem(constants.eur());
 		feeCurrencyListBox.addItem(constants.gbp());
 		feeCurrencyListBox.setSelectedIndex(currentShow.getShow()
 				.getFeeCurrency());
 
 		provisionCurrencyListBox = new ListBox();
 		provisionCurrencyListBox.addItem(constants.dkk());
 		provisionCurrencyListBox.addItem(constants.usd());
 		provisionCurrencyListBox.addItem(constants.eur());
 		provisionCurrencyListBox.addItem(constants.gbp());
 		provisionCurrencyListBox.setSelectedIndex(currentShow.getShow()
 				.getProvisionCurrency());
 
 		productionTypeListBox = new ListBox();
 		productionTypeListBox.addItem(constants.sale());
 		productionTypeListBox.addItem(constants.coProduction());
 		productionTypeListBox.addItem(constants.ownProduction());
 		productionTypeListBox.setSelectedIndex(currentShow.getShow()
 				.getProductionType());
 
 		editableTable.setWidget(1, 0, stateLabel = new Label(constants.state()
 				+ ": "));
 		stateTextBox = new TextBoxWidget(
 				ShowState.getShowStateByInt(currentShow.getShow().getState()));
 		stateTextBox.setReadOnly(true);
 		stateTextBox.setStyleName("readOnlyTextBox");
 		editableTable.setWidget(1, 1, stateTextBox);
 
 		editableTable.setWidget(2, 0, feeLabel = new Label(constants.fee()
 				+ ": "));
 		feeTextBox = new TextBoxWidget(currentShow.getShow().getFee() + "");
 		editableTable.setWidget(2, 1, feeTextBox);
 
 		editableTable.setWidget(3, 0,
 				feeCurrencyLabel = new Label(constants.feeCurrency() + ": "));
 		editableTable.setWidget(3, 1, feeCurrencyListBox);
 		editableTable.setWidget(4, 0,
 				provisionLabel = new Label(constants.provision() + ": "));
 		editableTable.setWidget(4, 1, provisionTextBox = new TextBoxWidget(""
 				+ currentShow.getShow().getProvision()));
 		editableTable.setWidget(5, 0, provisionCurrencyLabel = new Label(
 				constants.provisionCurrency() + ": "));
 		editableTable.setWidget(5, 1, provisionCurrencyListBox);
 		editableTable.setWidget(6, 0,
 				productionTypeLabel = new Label(constants.productionType()
 						+ ": "));
 		editableTable.setWidget(6, 1, productionTypeListBox);
 		editableTable.setWidget(7, 0,
 				profitSplitLabel = new Label(constants.profitSplit() + ": "));
 		editableTable.setWidget(7, 1, profitSplitTextBox = new TextBoxWidget(""
 				+ currentShow.getShow().getProfitSplit()));
 		editableTable.setWidget(8, 0,
 				ticketPriceLabel = new Label(constants.ticketPrice() + ": "));
 		editableTable.setWidget(8, 1, ticketPriceTextBox = new TextBoxWidget(""
 				+ currentShow.getShow().getTicketPrice()));
 		editableTable.setWidget(9, 0,
 				kodaPctLabel = new Label(constants.kodaPct() + ": "));
 		editableTable.setWidget(9, 1, kodaPctTextBox = new TextBoxWidget(""
 				+ currentShow.getShow().getKodaPct()));
 		editableTable.setWidget(10, 0, VATLabel = new Label(constants.VAT()
 				+ ": "));
 		editableTable.setWidget(10, 1, VATTextBox = new TextBoxWidget(""
 				+ currentShow.getShow().getVAT()));
 		editableTable.setWidget(11, 0,
 				ticketsSoldLabel = new Label(constants.ticketsSold() + ": "));
 		editableTable.setWidget(11, 1, ticketsSoldTextBox = new TextBoxWidget(
 				"" + currentShow.getShow().getTicketsSold()));
 		editableTable.setWidget(12, 0,
 				commentsLabel = new Label(constants.comments() + ": "));
 		editableTable.setWidget(12, 1, commentsTextBox = new TextBoxWidget(""
 				+ currentShow.getShow().getComments()));
 		editableTable.setWidget(13, 0,
 				lastEditLabel = new Label(constants.lastEdit()));
 		editableTable.setWidget(13, 1, lastEditTextBox = new TextBoxWidget(""
 				+ currentShow.getShow().getLastEdit()));
 
 		ItemActions.addFieldEventsTextBox(currentShow, feeTextBox, feeLabel);
 		ItemActions.addFieldEventsTextBox(currentShow, provisionTextBox,
 				provisionLabel);
 		ItemActions.addFieldEventsTextBox(currentShow, profitSplitTextBox,
 				profitSplitLabel);
 		ItemActions.addFieldEventsTextBox(currentShow, ticketPriceTextBox,
 				ticketPriceLabel);
 		ItemActions.addFieldEventsTextBox(currentShow, kodaPctTextBox,
 				kodaPctLabel);
 		ItemActions.addFieldEventsTextBox(currentShow, VATTextBox, VATLabel);
 		ItemActions.addFieldEventsTextBox(currentShow, ticketsSoldTextBox,
 				ticketsSoldLabel);
 		ItemActions.addFieldEventsTextBox(currentShow, commentsTextBox,
 				commentsLabel);
 		ItemActions.addFieldEventsListBox(currentShow, feeCurrencyListBox,
 				feeCurrencyLabel);
 		ItemActions.addFieldEventsListBox(currentShow,
 				provisionCurrencyListBox, provisionCurrencyLabel);
 		ItemActions.addFieldEventsListBox(currentShow, productionTypeListBox,
 				productionTypeLabel);
 		editableCapPanel.add(editableTable);
 		return editableCapPanel;
 	}
 
 	public CaptionPanel makeVenueCaptionPanel() {
 		venuesTable = new FlexTable();
 		venuesTable.setTitle(constants.band());
 
 		// for (Venue v : currentShow.getShow().getVenues()){
 		//
 		// }
 
 		venuesCapPanel.add(venuesTable);
 		return venuesCapPanel;
 	}
 
 	public CaptionPanel makeFileCaptionPanel() {
 
 		myForm = new FormPanel();
 		myForm.setEncoding(FormPanel.ENCODING_MULTIPART);
 		myForm.setMethod(FormPanel.METHOD_POST);
 
 		// Create a panel to hold all of the form widgets.
 		VerticalPanel panel = new VerticalPanel();
 		myForm.setWidget(panel);
 
 		// Create a ListBox, giving it a name and some values to be associated with its options.
 		ListBox lb = new ListBox();
 		lb.setName("docType");
 		lb.addItem(constants.dealmemo(), "deal-memo");
 		lb.addItem(constants.contract(), "contract");
 		lb.addItem(constants.budget(), "budget");
 		lb.addItem(constants.invoice(), "invoice");
 		panel.add(lb);
 
 		// Create a FileUpload widget.
 		FileUpload upload = new FileUpload();
		upload.setName("uploadFormElement");
 		panel.add(upload);
 
 		// Add a 'submit' button.
 		Button submit = new Button("Submit");
 		panel.add(submit);
 
 		submit.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				fileService.getBlobStoreUploadUrl(new AsyncCallback<String>() {
 					
 					@Override
 					public void onSuccess(String result) {
 						myForm.setAction(result.toString());
 						myForm.submit();
 						myForm.reset();
 					}
 					
 					@Override
 					public void onFailure(Throwable caught) {
 						Window.alert("Fejlede før submit");
 					}
 				});
 			}
 		});
 
 		// Add an event handler to the myForm.
 		myForm.addSubmitCompleteHandler(new SubmitCompleteHandler() {
 			
 			@Override
 			public void onSubmitComplete(SubmitCompleteEvent event) {
 				Window.alert(event.getResults().trim());
 				
 			}
 		});
 			
 		
 		fileCapPanel.add(myForm);
 return	fileCapPanel;
 		}
 }
