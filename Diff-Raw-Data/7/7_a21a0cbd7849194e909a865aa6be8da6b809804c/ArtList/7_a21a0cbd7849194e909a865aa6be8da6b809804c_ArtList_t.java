 package com.google.gwt.artvan.client;
 
 import java.util.Arrays;
 import java.util.Date;
 import java.util.Vector;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RadioButton;
 
 public class ArtList {
 
 	private Vector<ArtInformation> artworks;
 	private ArtInformationServiceAsync artInfoService = GWT
 			.create(ArtInformationService.class);
 	private AsyncCallback<ArtInformation[]> callback;
 	private LocationManager locationMgr;
 	private ArtFinder artfinder;
 
 	public ArtList(  ArtFinder artfinder) {
 		this.artfinder = artfinder;
 		final FlexTable artFlexTable = artfinder.getFlexTable();
 		final Label lastUpdatedLabel = artfinder.getLastUpdatedLabel();
 		artworks = new Vector<ArtInformation>();
 		if (locationMgr != null) {
 			locationMgr = new LocationManager();
 		}
 		if (artInfoService == null) {
 			artInfoService = GWT.create(ArtInformationService.class);
 		}
 
 		// Set up the callback object.
 		callback = new AsyncCallback<ArtInformation[]>() {
 			public void onFailure(Throwable caught) {
 				// TODO: Do something with errors.
 				caught.printStackTrace();
 			}
 
 			public void onSuccess(ArtInformation[] result) {
 				// updateTable(result);
				if(result!=null){
					artworks.addAll(Arrays.asList(result));
					updateTable(result);
				}
 			}
 				
 				
 				public void updateTable(ArtInformation[] artInfo) {
 					int existing_rowcount = artFlexTable.getRowCount();
 					
 					for (int i = 0; i < artInfo.length; i++) {
 						updateTable(artInfo[i], i + 1);
 					}
 					// we have fewer rows than before, clear the excess
 					for (int i = artInfo.length; i< existing_rowcount; i++){
 						if(i!=0) //just so we don't remove the header if length = 0
 							artFlexTable.removeRow(i);
 					}
 					lastUpdatedLabel.setText("Last update : "
 							+ DateTimeFormat.getMediumDateTimeFormat().format(new Date()));
 	
 				}
 				
 				private void updateTable(ArtInformation artInfo, int row) {
 	
 					String latlong = " " + artInfo.getLat() + " " + artInfo.getLng();
 					String desc = artInfo.getDescription();
 					//fill row with art info
 					artFlexTable.setText(row, 0, artInfo.getName());
 					artFlexTable.setText(row, 1, latlong);
 					artFlexTable.setText(row, 2, desc);
 					artFlexTable.setText(row, 3, "" + artInfo.getVisits());
 					
 					//add button to add visits
 					final int currentRow = row;
 					Button visitedButton = new Button("+");
 					visitedButton.setSize("20px", "20px");
 					visitedButton.addClickHandler(new ClickHandler(){		
 						@Override
 						public void onClick(ClickEvent event) {
 							//increase visitor count of related art object
 							int numVisits = Integer.parseInt(artFlexTable.getText(currentRow, 3)); //get current number of visits shown in table
 							artFlexTable.setText(currentRow, 3, ""+(++numVisits));
 							//TODO Persist new visit count to server
 						}	
 					});
 					artFlexTable.setWidget(row, 4, visitedButton);
 					
 					//add ratings selector
 					RadioButton rb1 = new RadioButton("Rating", "1");
 				    RadioButton rb2 = new RadioButton("Rating", "2");
 				    RadioButton rb3 = new RadioButton("Rating", "3");
 				    RadioButton rb4 = new RadioButton("Rating", "4");
 				    RadioButton rb5 = new RadioButton("Rating", "5");
 				    FlowPanel ratingsPanel = new FlowPanel();
 				    ratingsPanel.add(rb1);
 				    ratingsPanel.add(rb2);
 				    ratingsPanel.add(rb3);
 				    ratingsPanel.add(rb4);
 				    ratingsPanel.add(rb5);
 				    artFlexTable.setWidget(row, 5, ratingsPanel);
 					
 				}
 			
 
 	
 		};
 	}
 
 
 	Vector<ArtInformation> searchByAddres(String address) {
 		// TODO build the list before returning it
 		return artworks;
 	}
 
 	Vector<ArtInformation> searchByLocation(double lat, double lng) {
 		// updates private artworks member
 		artInfoService.searchByLocation(lat, lng, callback);
 
 		return artworks;
 	}
 
 	Vector<ArtInformation> getTopRated() {
 		// TODO: build the list before returning it
 		return artworks;
 	}
 
 	void updateArtList(Vector<ArtInformation> artList) {
 		// TODO: build the list before returning it
 	}
 
 	Vector<ArtInformation> getArtWorks() {
 		// TODO: build the list before returning it
 		return artworks;
 	}
 
 	Vector<ArtInformation> sortByNUmVisits(Vector<ArtInformation> artList) {
 		// TODO: build the list before returning it
 		return artworks;
 	}
 	
 	void deleteAllArt(){
 		artInfoService.deleteAllArt(callback);
 		
 	}
 
 }
