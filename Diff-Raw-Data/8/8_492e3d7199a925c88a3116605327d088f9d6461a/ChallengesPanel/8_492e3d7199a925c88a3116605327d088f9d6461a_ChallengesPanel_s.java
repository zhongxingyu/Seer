 package de.kuei.metafora.client;
 
 import java.sql.Date;
 import java.util.Vector;
 
 import com.google.gwt.cell.client.Cell.Context;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
 import com.google.gwt.user.cellview.client.TextColumn;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.LayoutPanel;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.view.client.SelectionChangeEvent;
 import com.google.gwt.view.client.SingleSelectionModel;
 
 import de.kuei.metafora.client.rpc.ChallengeModel;
 
 public class ChallengesPanel extends LayoutPanel {
 	
   private ChallengeModel selectedChallenge = null;
   private Vector<ChallengeModel> challenges;
 	private CellTable<ChallengeModel> challengeTable = null;
 	private TextBox tbChallengeName, tbChallengeUrl;
 	private int selectedChallengeID;
 
 	public ChallengesPanel(Vector<ChallengeModel> challenges) {
 		this.challenges = challenges;
 		createChallengeTable();
 		challengeCreatorPanel();
 	}
 		
 	private void challengeCreatorPanel() {
 		LayoutPanel createChallengePanel = new LayoutPanel();
 
 		Label cnLabel = new Label("Challenge Name:");
 		createChallengePanel.add(cnLabel);
 		createChallengePanel.setWidgetTopHeight(cnLabel, 10, Unit.PX, 30, Unit.PX);
 		createChallengePanel.setWidgetLeftWidth(cnLabel, 30, Unit.PX, 150, Unit.PX);
     tbChallengeName = new TextBox();
 //    tbChallengeName.addKeyPressHandler(new KeyPressHandler() {
 //      public void onKeyPress(KeyPressEvent event) {
 //        if (!Character.isLetterOrDigit(event.getCharCode())) {
 //          ((TextBox) event.getSource()).cancelKey();
 //        }
 //      }
 //    });
     createChallengePanel.add(tbChallengeName);
 		createChallengePanel.setWidgetTopHeight(tbChallengeName, 50, Unit.PX, 30, Unit.PX);
 		createChallengePanel.setWidgetLeftWidth(tbChallengeName, 30, Unit.PX, 400, Unit.PX);
 
 		Label cnUrl = new Label("Challenge URL:");
 		createChallengePanel.add(cnUrl);
 		createChallengePanel.setWidgetTopHeight(cnUrl, 90, Unit.PX, 30, Unit.PX);
 		createChallengePanel.setWidgetLeftWidth(cnUrl, 30, Unit.PX, 150, Unit.PX);
     tbChallengeUrl = new TextBox();
 //    tbChallengeUrl.addKeyPressHandler(new KeyPressHandler() {
 //      public void onKeyPress(KeyPressEvent event) {
 //        if (" \"".indexOf(event.getCharCode())>0) {
 //          ((TextBox) event.getSource()).cancelKey();
 //        }
 //      }
 //    });
     createChallengePanel.add(tbChallengeUrl);
 		createChallengePanel.setWidgetTopHeight(tbChallengeUrl, 130, Unit.PX, 30, Unit.PX);
 		createChallengePanel.setWidgetLeftWidth(tbChallengeUrl, 30, Unit.PX, 400, Unit.PX);
 		
 		Button createNewChallengeButton = new Button("create new\n challenge", new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				createNewChallenge(tbChallengeName.getText(), tbChallengeUrl.getText());
 			}
 		});
 		createNewChallengeButton.setSize("140", "50px");
 		createChallengePanel.add(createNewChallengeButton);
 		createChallengePanel.setWidgetTopHeight(createNewChallengeButton, 200, Unit.PX, 50, Unit.PX);
 		createChallengePanel.setWidgetLeftWidth(createNewChallengeButton,  10, Unit.PX, 140, Unit.PX);
 
		Button updateChallengeButton = new Button("update selected\n challenge", new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				updateChallenge(tbChallengeName.getText(), tbChallengeUrl.getText());
 			}
 		});
 		updateChallengeButton.setSize("140", "50px");
 		createChallengePanel.add(updateChallengeButton);
 		createChallengePanel.setWidgetTopHeight(updateChallengeButton, 200, Unit.PX, 50, Unit.PX);
 		createChallengePanel.setWidgetLeftWidth(updateChallengeButton, 160, Unit.PX, 140, Unit.PX);
 
 		Button copyChallengeButton = new Button("copy selected\n challenge", new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				copySelectedChallenge(tbChallengeName.getText(), tbChallengeUrl.getText(), selectedChallenge.getTemplate());
 			}
 		});
 		copyChallengeButton.setSize("140", "50px");
 		createChallengePanel.add(copyChallengeButton);
 		createChallengePanel.setWidgetTopHeight(copyChallengeButton, 200, Unit.PX, 50, Unit.PX);
 		createChallengePanel.setWidgetLeftWidth(copyChallengeButton, 310, Unit.PX, 140, Unit.PX);
 
 		this.add(createChallengePanel);
 		this.setWidgetTopHeight(createChallengePanel, 10, Unit.PCT, 80, Unit.PCT);
 		this.setWidgetRightWidth(createChallengePanel, 5, Unit.PCT, 35, Unit.PCT);
 	}
 	
 	private int challengeExists(String challengeName) {
 		for (ChallengeModel model : challenges) {
 			if (model.getChallengeName().equals(challengeName)) {
 				return model.getChallengeId();
 			}
 		}
 		return -1;
 	}
 
 	protected void createNewChallenge(String name, String url) {
 		int challengeID = challengeExists(name);
 		if (challengeID >= 0) {
 			Window.alert("The new challenge name already exists!");
 		} else {
 			CAVilLag.instance.createNewChallenge(name, url);
 		}
 		CAVilLag.instance.updateChallengeTab();
 //		createChallengeTable();
 	}
 
 	protected void updateChallenge(String name, String url) {
 		int challengeID = challengeExists(name);
 		if ((challengeID >= 0) && (selectedChallengeID != challengeID)) {
 			Window.alert("You cannot rename a challenge into an existing name!");
 		} else if (selectedChallengeID >= 0) {
 			CAVilLag.instance.updateChallengeName(selectedChallengeID, name);
 			CAVilLag.instance.updateChallengeUrl(selectedChallengeID, url);
 		}
 		CAVilLag.instance.updateChallengeTab();
 //		createChallengeTable();
 	}
 	
 	protected void copySelectedChallenge(String name, String url, String template) {
 		String copiedChallengeName = Window.prompt("Please enter name for copy\n of selected challenge:", "copy of"+name);
 		if (copiedChallengeName == null) {
 			return;
 		}
 		int challengeID = challengeExists(copiedChallengeName);
 		if (challengeID >= 0) {
 			Window.alert("The name of the copy already exists!");
 			return;
 		} else {
 			CAVilLag.instance.createNewChallenge(copiedChallengeName, url, template);
 		}
 		CAVilLag.instance.updateChallengeTab();
 //		createChallengeTable();
 	}
 
 	private void createChallengeTable() {
     challengeTable  = new CellTable<ChallengeModel>();
     challengeTable.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
     
     // Add a selection model to handle user selection.
     final SingleSelectionModel<ChallengeModel> selectionModel = new SingleSelectionModel<ChallengeModel>();
     challengeTable.setSelectionModel(selectionModel);
     selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
       public void onSelectionChange(SelectionChangeEvent event) {
       	selectedChallenge = selectionModel.getSelectedObject();
         if (selectedChallenge != null) {
         	tbChallengeName.setText(selectedChallenge.getChallengeName());
         	tbChallengeUrl.setText(selectedChallenge.getChallengeUrl());
         	selectedChallengeID = selectedChallenge.getChallengeId();
           CAVilLag.instance.readTemplate(selectedChallenge.getTemplate());
         }
       }
     });
     
 
     // Add a text column to show the challenge name.
     TextColumn<ChallengeModel> nameColumn = new TextColumn<ChallengeModel>() {
       @Override
       public String getValue(ChallengeModel object) {
         return object.getChallengeName();
       }
 
 			@Override
 			public void render(Context context, ChallengeModel object, SafeHtmlBuilder sb) {
 				if (object == null) {
 					return;
 				}
 				sb.appendHtmlConstant("<a target=\"_blank\" href=https://metafora.ku-eichstaett.de/planningtoolsolo/PlanningTool.html?locale=en" +
 						"&token=CAVILLAGTEST&user=tom&pw=82a772ff0241d89b26f541c6050c7707&pwEncrypted=true&ptMap=Tm9iYmlzTWFw&groupId=Awesome" +
 						"&challengeId=" + object.getChallengeId() +
 						"&challengeName=" + object.getChallengeName().replace(" ", "%20") +
 						"&testServer=true&cavillag=true>");
 				sb.appendEscaped(object.getChallengeName());
 				sb.appendHtmlConstant("</a>");
 //				super.render(context, object, sb);
 			}
       
       
     };
     challengeTable.addColumn(nameColumn, "Challenge Name");
     
     // and the challenge URL
     
     TextColumn<ChallengeModel> urlColumn = new TextColumn<ChallengeModel>() {
       @Override
       public String getValue(ChallengeModel object) {
         return object.getChallengeUrl();
       }
 
 			@Override
 			public void render(Context context, ChallengeModel object, SafeHtmlBuilder sb) {
 				if (object == null) {
 					return;
 				}
 				String url = object.getChallengeUrl();
 				if ((url != null) && (url.length() > 7)) {
 					sb.appendHtmlConstant("<a href="+url+">");
 					sb.appendEscaped("open");
 					sb.appendHtmlConstant("</a>");
 				}
 			}
     };
     challengeTable.addColumn(urlColumn, "Challenge URL");
     
     
     TextColumn<ChallengeModel> vlColumn = new TextColumn<ChallengeModel>() {
 			String timestamp;
 
 			@Override
 			public String getValue(ChallengeModel object) {
 				if ((object.getTemplate() == null) || object.getTemplate().isEmpty()) {
 					return "EMPTY";
 				}
 //			  try {
 //			    // parse the XML document into a DOM
 //			    Document messageDom = XMLParser.parse(object.getTemplate());
 //			    
 //			    // find the sender's display name in an attribute of the <from> tag
 //			    Node templateNode = messageDom.getElementsByTagName("template").item(0);
 //			    if (templateNode != null) {
 ////				    Window.alert("Reading timestamp from "+templateNode.getNodeName());
 //				    timestamp = ((Element)templateNode).getAttribute("time"); 
 //			    }
 //			  } catch (DOMException e) {
 ////			    Window.alert("Could not parse XML document.");
 //			  }				
 				timestamp = CAVilLag.instance.getTimestamp(object.getTemplate());
 			  if ((timestamp == null) || timestamp.isEmpty()) {
 			  	return "created: unknown";
 			  }
 			  Date createdDate = new Date(Long.parseLong(timestamp));
 				return "created: "+ DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss").format(createdDate);
 			}
     	
 		};
     challengeTable.addColumn(vlColumn, "Visual Language");
     
 
     TextColumn<ChallengeModel> lastUsedColumn = new TextColumn<ChallengeModel>() {
 
 			@Override
 			public String getValue(ChallengeModel object) {
 				if ((object.getLastUsed() == null) || object.getLastUsed().isEmpty()) {
 					return "EMPTY";
 				}
 			  Date lastUsedDate = new Date(Long.parseLong(object.getLastUsed()));
 				return DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss").format(lastUsedDate);
 			}
 
     };
     challengeTable.addColumn(lastUsedColumn, "last used");
     
     // Push the data into the widget.
     challengeTable.setRowData(challenges);
     challengeTable.setSize("100%", "100%");
 		ScrollPanel sp = new ScrollPanel();
 		sp.setSize("100%", "100%");
 		sp.add(challengeTable);
     this.add(sp);
 		this.setWidgetTopHeight(sp, 5, Unit.PCT, 90, Unit.PCT);
 		this.setWidgetLeftWidth(sp, 5, Unit.PCT, 55, Unit.PCT);
 	}
 
 	public ChallengeModel getSelectedChallenge() {
 		return selectedChallenge;
 	}
 	
 	public void resetChallengeCreatorPanel() {
 		tbChallengeName.setText("");
 		tbChallengeUrl.setText("");
 	}
 
 	public void updateChallenges(Vector<ChallengeModel> result) {
 		this.challenges = result;
     challengeTable.setRowData(challenges);
 //		challengeTable.setRowData(challenges.size(), challenges);
 //    challengeTable.setVisibleRange(0, challenges.size());
 	}
 	
 }
