 package org.iucn.sis.client.tabs;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.iucn.sis.client.api.caches.AuthorizationCache;
 import org.iucn.sis.client.api.caches.TaxonomyCache;
 import org.iucn.sis.client.api.container.StateManager;
 import org.iucn.sis.client.api.ui.models.taxa.TaxonListElement;
 import org.iucn.sis.client.api.ui.notes.NoteAPI;
 import org.iucn.sis.client.api.ui.notes.NotesWindow;
 import org.iucn.sis.client.api.utils.TaxonPagingLoader;
 import org.iucn.sis.client.api.utils.UriBase;
 import org.iucn.sis.client.container.SimpleSISClient;
 import org.iucn.sis.client.panels.ClientUIContainer;
 import org.iucn.sis.client.panels.assessments.NewAssessmentPanel;
 import org.iucn.sis.client.panels.images.ImageManagerPanel;
 import org.iucn.sis.client.panels.taxa.TaxonAssessmentInformationTab;
 import org.iucn.sis.client.panels.taxa.TaxonHomeGeneralInformationTab;
 import org.iucn.sis.client.panels.taxa.TaxonHomeWorkingSetsTab;
 import org.iucn.sis.client.panels.taxomatic.CreateNewTaxonPanel;
 import org.iucn.sis.client.panels.taxomatic.LateralMove;
 import org.iucn.sis.client.panels.taxomatic.MergePanel;
 import org.iucn.sis.client.panels.taxomatic.MergeUpInfrarank;
 import org.iucn.sis.client.panels.taxomatic.SplitNodePanel;
 import org.iucn.sis.client.panels.taxomatic.TaxomaticAssessmentMover;
 import org.iucn.sis.client.panels.taxomatic.TaxomaticDemotePanel;
 import org.iucn.sis.client.panels.taxomatic.TaxomaticHistoryPanel;
 import org.iucn.sis.client.panels.taxomatic.TaxomaticUtils;
 import org.iucn.sis.client.panels.taxomatic.TaxomaticWindow;
 import org.iucn.sis.client.panels.taxomatic.TaxonBasicEditor;
 import org.iucn.sis.client.panels.taxomatic.TaxonCommonNameEditor;
 import org.iucn.sis.client.panels.taxomatic.TaxonSynonymEditor;
 import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
 import org.iucn.sis.shared.api.acl.feature.AuthorizableFeature;
 import org.iucn.sis.shared.api.citations.Referenceable;
 import org.iucn.sis.shared.api.models.Notes;
 import org.iucn.sis.shared.api.models.Reference;
 import org.iucn.sis.shared.api.models.Synonym;
 import org.iucn.sis.shared.api.models.Taxon;
 import org.iucn.sis.shared.api.models.TaxonLevel;
 
 import com.extjs.gxt.ui.client.Style.LayoutRegion;
 import com.extjs.gxt.ui.client.Style.Scroll;
 import com.extjs.gxt.ui.client.binder.DataListBinder;
 import com.extjs.gxt.ui.client.event.BaseEvent;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.MenuEvent;
 import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
 import com.extjs.gxt.ui.client.event.SelectionChangedListener;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.store.StoreSorter;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.DataList;
 import com.extjs.gxt.ui.client.widget.Info;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.TabItem;
 import com.extjs.gxt.ui.client.widget.TabPanel;
 import com.extjs.gxt.ui.client.widget.Window;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
 import com.extjs.gxt.ui.client.widget.layout.FillLayout;
 import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
 import com.extjs.gxt.ui.client.widget.menu.Menu;
 import com.extjs.gxt.ui.client.widget.menu.MenuItem;
 import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
 import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
 import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.lwxml.shared.NativeDocument;
 import com.solertium.util.events.ComplexListener;
 import com.solertium.util.events.SimpleListener;
 import com.solertium.util.extjs.client.WindowUtils;
 import com.solertium.util.gwt.ui.DrawsLazily;
 import com.solertium.util.gwt.ui.StyledHTML;
 import com.solertium.util.portable.PortableAlphanumericComparator;
 
 public class TaxonHomePageTab extends FeaturedItemContainer<Integer> {
 	
 	private ToolBar toolBar = null;
 	private Button assessmentTools;
 	private Button taxonToolsItem;
 	private Button taxomaticToolItem;
 	private Button goToParent;
 
 	private final boolean ENABLE_TAXOMATIC_FEATURES = true;
  
 	/**
 	 * Defaults to having Style.NONE
 	 */
 	public TaxonHomePageTab() {
 		super();
 
 		toolBar = buildToolBar();
 	}
 	
 	@Override
 	protected void drawBody(DoneDrawingCallback callback) {
 		Taxon taxon = TaxonomyCache.impl.getTaxon(getSelectedItem());
 		//if (bodyContainer.getItemCount() == 0) {
 			bodyContainer.removeAll();
 			
 			goToParent.setText("Up to Parent (" + taxon.getParentName() + ")");
 			goToParent.setVisible(taxon.getTaxonLevel().getLevel() > TaxonLevel.KINGDOM);
 			
 			final TaxonHomeGeneralInformationTab generalContent = 
 				new TaxonHomeGeneralInformationTab();
 			
 			final TabItem general = new TabItem();
 			general.setLayout(new FillLayout());
 			general.setText("General Information");
 			general.addListener(Events.Select, new Listener<BaseEvent>() {
 				public void handleEvent(BaseEvent be) {
 					generalContent.draw(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
 				}
 			});
 			general.add(generalContent);
 			
 			final TaxonAssessmentInformationTab assessmentContent = 
 				new TaxonAssessmentInformationTab();
 			
 			final TabItem assessment = new TabItem();
 			assessment.setLayout(new FillLayout());
 			assessment.setText("Assessments");
 			assessment.addListener(Events.Select, new Listener<BaseEvent>() {
 				public void handleEvent(BaseEvent be) {
 					assessmentContent.draw(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
 				}
 			});
 			assessment.add(assessmentContent);
 			
 			final TaxonHomeWorkingSetsTab workingSetContent = 
 				new TaxonHomeWorkingSetsTab();
 			
 			final TabItem workingSet = new TabItem();
 			workingSet.setLayout(new FillLayout());
 			workingSet.setText("Working Sets");
 			workingSet.addListener(Events.Select, new Listener<BaseEvent>() {
 				public void handleEvent(BaseEvent be) {
 					workingSetContent.draw(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
 				}
 			});
 			workingSet.add(workingSetContent);
 			
 			final TabPanel tabPanel = new TabPanel();
 			tabPanel.add(general);
 			if (TaxonomyCache.impl.getCurrentTaxon().getLevel() >= TaxonLevel.SPECIES) {
 				tabPanel.add(assessment);
 				tabPanel.add(workingSet);
 			}
 			
 			BorderLayoutData toolbarData = new BorderLayoutData(LayoutRegion.NORTH);
 			toolbarData.setSize(25);
 	
 			/*
 			BorderLayoutData summaryData = new BorderLayoutData(LayoutRegion.CENTER, .5f, 100, 500);
 			summaryData.setSize(300);
 			BorderLayoutData browserData = new BorderLayoutData(LayoutRegion.EAST, .5f, 100, 500);
 			browserData.setSize(250);
 	
 			final LayoutContainer container = new LayoutContainer(new BorderLayout());
 			container.add(taxonomicSummaryPanel, summaryData);
 			container.add(toolBar, toolbarData);*/
 			
 			final LayoutContainer container = new LayoutContainer(new BorderLayout());
 			container.add(toolBar, toolbarData);
 			container.add(tabPanel, new BorderLayoutData(LayoutRegion.CENTER));
 	
 			bodyContainer.add(container);
 		//}
 		
 		callback.isDrawn();
 	}
 	
 	@Override
 	protected void drawOptions(final DrawsLazily.DoneDrawingCallback callback) {
 		final Taxon taxon = TaxonomyCache.impl.getTaxon(getSelectedItem());
 		
 		final ContentPanel children = new ContentPanel();
 		children.setLayout(new FillLayout());
 		
 		if (Taxon.getDisplayableLevelCount() > taxon.getLevel() + 1) {
 			children.setHeading(Taxon.getDisplayableLevel(taxon.getLevel() + 1));
 
 			TaxonomyCache.impl.fetchChildren(taxon, new GenericCallback<List<TaxonListElement>>() {
 				public void onFailure(Throwable caught) {
 					children.add(new HTML("No " + Taxon.getDisplayableLevel(taxon.getLevel() + 1) + "."));
 					optionsContainer.removeAll();
 					optionsContainer.add(children);
 					callback.isDrawn();
 				}
 				public void onSuccess(List<TaxonListElement> result) {
 					final TaxonPagingLoader loader = new TaxonPagingLoader();
 					final PagingToolBar bar = new PagingToolBar(30);
 					bar.bind(loader.getPagingLoader());
 					
 					final DataList list = new DataList();
 					//list.setSize((com.google.gwt.user.client.Window.getClientWidth() - 500) / 2, 148);
 					list.setScrollMode(Scroll.AUTOY);
 					
 					final ListStore<TaxonListElement> store = new ListStore<TaxonListElement>(loader.getPagingLoader());
 					store.setStoreSorter(new StoreSorter<TaxonListElement>(new PortableAlphanumericComparator()));
 
 					final DataListBinder<TaxonListElement> binder = new DataListBinder<TaxonListElement>(list, store);
 					binder.setDisplayProperty("name");
 					binder.init();
 					binder.addSelectionChangedListener(new SelectionChangedListener<TaxonListElement>() {
 						public void selectionChanged(SelectionChangedEvent<TaxonListElement> se) {
 							if (se.getSelectedItem() != null) {
 								//TaxonomyCache.impl.setCurrentTaxon(se.getSelectedItem().getNode());
 								StateManager.impl.setState(null, se.getSelectedItem().getNode(), null);
 								//update(se.getSelectedItem().getNode().getId());
 							}
 						}
 					});
 					
 					loader.getFullList().addAll(result);
 					/*ArrayUtils.quicksort(loader.getFullList(), new Comparator<TaxonListElement>() {
 						public int compare(TaxonListElement o1, TaxonListElement o2) {
 							return ((String) o1.get("name")).compareTo((String) o2.get("name"));
 						}
 					});*/
 					
 					final LayoutContainer container = new LayoutContainer(new BorderLayout());
 					container.add(list, new BorderLayoutData(LayoutRegion.CENTER));
 					container.add(bar, new BorderLayoutData(LayoutRegion.SOUTH, 25, 25, 25));
 					
 					children.add(container);
 
 					loader.getPagingLoader().load(0, loader.getPagingLoader().getLimit());
 
 					//children.layout();
 					optionsContainer.removeAll();
 					optionsContainer.add(children);		
 					callback.isDrawn();
 				}
 			});
 		} else {
 			children.setHeading("Not available.");
 			optionsContainer.removeAll();
 			optionsContainer.add(children);	
 			callback.isDrawn();
 		}
 	}
 	
 	@Override
 	protected LayoutContainer updateFeature() {
 		final Taxon node = getTaxon();
 		
 		final Image taxonImage = new Image(UriBase.getInstance().getImageBase() + "/images/view/thumb/" + node.getId() + "/primary?size=100&unique=" + new Date().getTime());
 		/*taxonImage.setWidth("100px");
 		taxonImage.setHeight("100px");*/
 		taxonImage.setStyleName("SIS_taxonSummaryHeader_image");
 		taxonImage.setTitle("Click for Image Viewer");
 		taxonImage.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				final ImageManagerPanel manager = new ImageManagerPanel(node);
 				manager.update(new DrawsLazily.DoneDrawingCallback() {
 					public void isDrawn() {
 						Window window = new Window();
 						window.setHeading("Manage Images");
 						window.add(manager);
 						window.setWidth(600);
 						window.setHeight(300);
 						window.show();
 					}
 				});
 			}
 		});
 		
 		final HorizontalPanel taxonImageWrapper = new HorizontalPanel();
 		taxonImageWrapper.setWidth("100%");
 		taxonImageWrapper.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		taxonImageWrapper.add(taxonImage);
 		
 		LayoutContainer vp = new LayoutContainer(new FlowLayout());
 		vp.add(createSpacer(10));
 		vp.add(taxonImageWrapper);
 		
 		/*final NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
 		doc.get(UriBase.getInstance().getImageBase() + "/images/" + getSelectedItem().getId(), new GenericCallback<String>() {
 			public void onFailure(Throwable caught) {
 				Debug.println("Taxon image loader failed to fetch xml");
 				taxonImage.setUrl("images/unavailable.png");
 			}
 			public void onSuccess(String result) {
 				NativeNodeList list = doc.getDocumentElement().getElementsByTagName("image");
 				boolean found = false;
 				for (int i = 0; i < list.getLength(); i++) {
 					boolean primary = ((NativeElement) list.item(i)).getAttribute("primary").equals("true");
 					if (primary) {
 						String ext = "";
 						if (((NativeElement) list.item(i)).getAttribute("encoding").equals("image/jpeg"))
 							ext = "jpg";
 						if (((NativeElement) list.item(i)).getAttribute("encoding").equals("image/gif"))
 							ext = "gif";
 						if (((NativeElement) list.item(i)).getAttribute("encoding").equals("image/png"))
 							ext = "png";
 
 						taxonImage.setUrl(UriBase.getInstance().getSISBase() + "/raw/images/bin/"
 										+ ((NativeElement) list.item(i)).getAttribute("id") + "." + ext);
 						found = true;
 						break;
 					}
 				}
 				if (!found) {
 					taxonImage.setUrl("images/unavailable.png");
 				}		
 			}
 		});*/
 		vp.add(createSpacer(20));
 		if (node.getLevel() >= TaxonLevel.SPECIES)
 			vp.add(new StyledHTML("<center><i>" + node.getFullName() + "</i></center>", "SIS_taxonSummaryHeader"));
 		else
 			vp.add(new StyledHTML("<center><i>" + node.getName() + "</i></center>", "SIS_taxonSummaryHeader"));
 		
 		return vp;
 	}
 	
 	@Override
 	protected void updateSelection(final Integer selection) {
 		TaxonomyCache.impl.fetchTaxon(selection, new GenericCallback<Taxon>() {
 			public void onFailure(Throwable caught) {
 				WindowUtils.errorAlert("Could not load this taxon. Please try again later.");
 			}
 			public void onSuccess(Taxon result) {
 				if (getSelectedItem() == selection)
 					draw(new DrawsLazily.DoneDrawingCallback() {
 						public void isDrawn() {
 							layout();
 						}
 					});
 				else
 					StateManager.impl.setState(result, null);
 			}
 		});
 	}
 	
 	public Taxon getTaxon() {
 		return TaxonomyCache.impl.getTaxon(getSelectedItem()); 
 	}
 	
 	private ToolBar buildToolBar() {
 		ToolBar toolbar = new ToolBar();
 		
 		goToParent = new Button("Up to Parent");
 		goToParent.setIconStyle("icon-previous");
 		goToParent.addSelectionListener(new SelectionListener<ButtonEvent>() {
 			public void componentSelected(ButtonEvent ce) {
 				TaxonomyCache.impl.fetchTaxon(getTaxon().getParentId(), true, new GenericCallback<Taxon>() {
 					public void onFailure(Throwable caught) {
 						//updatePanel(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
 					}
 					public void onSuccess(Taxon arg0) {
 						/*taxon = arg0;
 						updatePanel(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
 						ClientUIContainer.headerContainer.update();*/
 						//update(taxon.getParentId());
 						//StateManager.impl.setTaxon(arg0);
 						StateManager.impl.setState(null, arg0, null);
 					}
 				});
 			}
 		});
 		
 		toolbar.add(goToParent);
 		toolbar.add(new SeparatorToolItem());
 
 		assessmentTools = new Button();
 		assessmentTools.setText("Assessment Tools");
 		assessmentTools.setIconStyle("icon-preferences-wrench");
 
 		MenuItem mItem = new MenuItem();
 		mItem.setText("Goto Most Recent");
 		mItem.setIconStyle("icon-go-jump");
 		mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
 			public void componentSelected(MenuEvent ce) {
 
 			}
 		});
 
 		mItem = new MenuItem();
 		mItem.setText("Assess Current Taxon");
 		mItem.setIconStyle("icon-new-document");
 
 		mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
 			public void componentSelected(MenuEvent ce) {
 				if (TaxonomyCache.impl.getCurrentTaxon().getFootprint().length < TaxonLevel.SPECIES) {
 					WindowUtils.errorAlert("You must select a species or lower taxa to assess.  "
 							+ "You can select a different taxon using the navigator, the search function, "
 							+ " or the browser.");
 				} else {
 					final NewAssessmentPanel panel = new NewAssessmentPanel();
 					panel.show();
 				}
 			}
 		});
 
 		Menu mainMenu = new Menu();
 		mainMenu.add(mItem);
 		assessmentTools.setMenu(mainMenu);
 
 		toolbar.add(assessmentTools);
 		toolbar.add(new SeparatorToolItem());
 
 		taxonToolsItem = new Button();
 		taxonToolsItem.setText("Taxon Tools");
 		taxonToolsItem.setIconStyle("icon-preferences-wrench-orange");
 
 		mainMenu = new Menu();
 		taxonToolsItem.setMenu(mainMenu);
 
 		// mainMenu.add( mItem );
 
 		mItem = new MenuItem();
 		mItem.setText("View/Attach Note");
 		mItem.setIconStyle("icon-note");
 		mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
 			public void componentSelected(MenuEvent ce) {
 				buildNotePopup();
 			}
 		});
 		mainMenu.add(mItem);
 
 		mItem = new MenuItem();
 		mItem.setText("View/Attach Reference");
 		mItem.setIconStyle("icon-book");
 		mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
 			public void componentSelected(MenuEvent ce) {
 				buildReferencePopup();
 			}
 		});
 		mainMenu.add(mItem);
 
 		toolbar.add(taxonToolsItem);
 		toolbar.add(new SeparatorToolItem());
 
 		// BEGIN TAXOMATIC FEATURES
 		if (ENABLE_TAXOMATIC_FEATURES) {
 			taxomaticToolItem = new Button();
 			taxomaticToolItem.setText("Taxomatic Tools");
 			taxomaticToolItem.setIconStyle("icon-preferences-wrench-green");
 			mainMenu = new Menu();
 			taxomaticToolItem.setMenu(mainMenu);
 
 			mItem = new MenuItem();
 			mItem.setText("Edit Taxon");
 			mItem.setIconStyle("icon-note-edit");
 			mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
 				public void componentSelected(MenuEvent ce) {
 					popupChooser(new TaxonBasicEditor());
 				}
 			});
 			mainMenu.add(mItem);
 
 			mItem = new MenuItem();
 			mItem.setText("Edit Synonyms");
 			mItem.setIconStyle("icon-note-edit");
 			mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
 				public void componentSelected(MenuEvent ce) {
 					popupChooser(new TaxonSynonymEditor());
 				}
 			});
 			mainMenu.add(mItem);
 
 			mItem = new MenuItem();
 			mItem.setText("Edit Common Names");
 			mItem.setIconStyle("icon-note-edit");
 			mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
 				public void componentSelected(MenuEvent ce) {
 					popupChooser(new TaxonCommonNameEditor());
 				}
 			});
 			mainMenu.add(mItem);
 
 			// TODO: Decide if need to guard against deprecated nodes
 			mItem = new MenuItem();
 			mItem.setText("Add New Child Taxon");
 			mItem.setIconStyle("icon-new-document");
 			mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
 				public void componentSelected(MenuEvent ce) {
 					Taxon curNode = TaxonomyCache.impl.getCurrentTaxon();
 
 					if (curNode != null) {
 						if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.CREATE, curNode)) {
 							CreateNewTaxonPanel panel = new CreateNewTaxonPanel(TaxonomyCache.impl.getCurrentTaxon());
 							panel.setHeading("Add New Child Taxon");
 							panel.show();
 						}
 						else
 							WindowUtils.errorAlert("Insufficient Permission", "Sorry. You do not have create permissions for this taxon.");
 					} else {
 						WindowUtils.errorAlert("Please select a taxon to attach to.");
 					}
 				}
 			});
 			mainMenu.add(mItem);
 
 			mItem = new MenuItem();
 			mItem.setText("Lateral Move");
 			mItem.setIconStyle("icon-lateral-move");
 			mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
 				public void componentSelected(MenuEvent ce) {
 					popupChooser(new LateralMove());
 				}
 			});
 			mainMenu.add(mItem);
 
 			mItem = new MenuItem();
 			mItem.setText("Promote Taxon");
 			mItem.setIconStyle("icon-promote");
 			mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
 				public void componentSelected(MenuEvent ce) {
 					final Taxon currentNode = TaxonomyCache.impl.getCurrentTaxon();
 					if (currentNode == null)  //Not possible??
 						WindowUtils.errorAlert("Please first select a taxon");
 					else if (currentNode.getLevel() != TaxonLevel.INFRARANK)
 						WindowUtils.errorAlert("You may only promote infraranks.");
 					else {
 						String message = "<b>Instructions:</b> By promoting " + currentNode.getFullName() + ", "
 							+ currentNode.getFullName() + " will become a species " + " and will have the same parent that "
 							+ currentNode.getParentName() + " has.";
 						
 						WindowUtils.confirmAlert("Confirm", message, new WindowUtils.SimpleMessageBoxListener() {
 							public void onYes() {
 								TaxomaticUtils.impl.performPromotion(currentNode, new GenericCallback<String>() {
 									public void onFailure(Throwable arg0) {
 										//Error message handled via default callback
 									}
 									public void onSuccess(String arg0) {
 										WindowUtils.infoAlert("Success", currentNode.getName() + " has successfully been promoted.");
 									}
 								});
 							}
 						}, "OK", "Cancel");
 					}
 				}
 			});
 			mainMenu.add(mItem);
 
 			mItem = new MenuItem();
 			mItem.setText("Demote Taxon");
 			mItem.setIconStyle("icon-demote");
 			mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
 				public void componentSelected(MenuEvent ce) {
 					Taxon node = TaxonomyCache.impl.getCurrentTaxon();
 					if (node == null || node.getLevel() != TaxonLevel.SPECIES)
 						WindowUtils.infoAlert("Not allowed", "You can only demote a species.");
 					else
 						popupChooser(new TaxomaticDemotePanel());
 				}
 			});
 			mainMenu.add(mItem);
 
 			mItem = new MenuItem();
 			mItem.setIconStyle("icon-merge");
 			mItem.setText("Merge Taxa");
 			mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
 				public void componentSelected(MenuEvent ce) {
 					Taxon node = TaxonomyCache.impl.getCurrentTaxon();
 					if (node != null)
 						popupChooser(new MergePanel());
 				}
 			});
 			mainMenu.add(mItem);
 
 			mItem = new MenuItem();
 			mItem.setIconStyle("icon-merge-up");
 			mItem.setText("Merge Up Subspecies");
 			mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
 
 				@Override
 				public void componentSelected(MenuEvent ce) {
 					final Taxon node = TaxonomyCache.impl.getCurrentTaxon();
 					if (node == null || node.getLevel() != TaxonLevel.SPECIES) {
 						WindowUtils.infoAlert("Not Allowed", "You can only merge subspecies into a species, please "
 								+ "visit the species you wish to merge a subspecies into.");
 					} else {
 						TaxonomyCache.impl.getTaxonChildren(node.getId() + "", new GenericCallback<List<Taxon>>() {
 							public void onFailure(Throwable caught) {
 								WindowUtils.infoAlert("Error", "There was an internal error while trying to "
 										+ "fetch the children of " + node.getFullName());
 							}
 							public void onSuccess(List<Taxon> list) {
 								boolean show = false;
 								for (Taxon childNode : list) {
 									if (childNode.getLevel() == TaxonLevel.INFRARANK) {
 										show = true;
 										break;
 									}
 								}
 								if (show) {
 									popupChooser(new MergeUpInfrarank());
 								} else {
 									WindowUtils.infoAlert("Not Allowed", node.getFullName()
 											+ " does not have any subspecies to promote.  " 
 											+ "You can only merge subspecies with their parent.");
 								}
 
 							}
 						});
 					}
 
 				}
 			});
 			mainMenu.add(mItem);
 
 			mItem = new MenuItem();
 			mItem.setIconStyle("icon-split");
 			mItem.setText("Split Taxon");
 			mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
 				public void componentSelected(MenuEvent ce) {
 					// TODO:
 					//Taxon node = TaxonomyCache.impl.getCurrentTaxon();
 					// if( !node.isDeprecatedStatus() )
 					popupChooser(new SplitNodePanel());
 					// else
 					// WindowUtils.errorAlert("Error",
 					// "Taxon selected for merging is not a valid taxon" +
 					// " (i.e. status is not A or U).");
 
 				}
 			});
 			mainMenu.add(mItem);
 
 			mItem = new MenuItem();
 			mItem.setIconStyle("icon-remove");
 			mItem.setText("Remove Taxon");
 			mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
 				public void componentSelected(MenuEvent ce) {
 					final Taxon node = getTaxon();
 					TaxonomyCache.impl.fetchChildren(node, new GenericCallback<List<TaxonListElement>>() {
 						public void onFailure(Throwable caught) {
 							String msg = "If this taxon has assessments, these will be moved to the trash as well. Move"
 										+ node.generateFullName() + " to the trash?";
 							
 							WindowUtils.confirmAlert("Confirm Delete", msg, new WindowUtils.SimpleMessageBoxListener() {
 								public void onYes() {
 									TaxomaticUtils.impl.deleteTaxon(node, new GenericCallback<String>() {
										public void onSuccess(String result) {
 											TaxonomyCache.impl.clear();
 											TaxonomyCache.impl.evict(node.getParentId() + "," + node.getId());
											TaxonomyCache.impl.fetchTaxon(getTaxon().getParentId(), true,
 													new GenericCallback<Taxon>() {
 												public void onFailure(Throwable caught) {
 													//ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel.update(null);
 													//FIXME: panelManager.recentAssessmentsPanel.update();
 												};
 												public void onSuccess(Taxon result) {
 													updateSelection(result.getId());
 													/*ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel
 														.update(TaxonomyCache.impl.getCurrentTaxon().getId());*/
 													//FIXME: panelManager.recentAssessmentsPanel.update();
 												};
 											});
 										}
 										public void onFailure(Throwable caught) {
 											//close();
 										}
 									});
 								}
 
 							});
 						}
 
 						public void onSuccess(List<TaxonListElement> result) {
 							WindowUtils.infoAlert("You cannot remove this Taxa without first removing its children.");
 							// ((List<ModelData>) result).size();
 
 						}
 					});
 				}
 			});
 			mainMenu.add(mItem);
 
 			// END TAXOMATIC FEATURES
 
 			toolbar.add(taxomaticToolItem);
 			toolbar.add(new SeparatorToolItem());
 
 			mItem = new MenuItem();
 			mItem.setIconStyle("icon-undo");
 			mItem.setText("Undo Taxomatic Operation");
 			mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
 				@Override
 				public void componentSelected(MenuEvent ce) {
 					final NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
 					doc.getAsText(UriBase.getInstance().getSISBase() + "/taxomatic/undo", new GenericCallback<String>() {
 
 						public void onFailure(Throwable caught) {
 							WindowUtils.infoAlert("Unable to undo last operation", "SIS is unable to undo the last "
 									+ "taxomatic operation "
 									+ "because you are not the last user to perform a taxomatic "
 									+ "change, or there has not been a taxomatic operation to undo.");
 
 						}
 
 						public void onSuccess(String result) {
 							WindowUtils.confirmAlert("Undo Last Taxomatic Operation", doc.getText()
 									+ "  Are you sure you want to undo this operation?",
 									new WindowUtils.SimpleMessageBoxListener() {
 								public void onYes() {
 									final NativeDocument postDoc = SimpleSISClient.getHttpBasicNativeDocument();
 									postDoc.post(UriBase.getInstance().getSISBase() +"/taxomatic/undo", "", new GenericCallback<String>() {
 										public void onFailure(Throwable caught) {
 											WindowUtils.errorAlert(
 												"Unable to undo the last operation.  " +
 												"Please undo the operation manually."
 											);
 										}
 										public void onSuccess(String result) {
 											final Taxon currentNode = TaxonomyCache.impl.getCurrentTaxon();
 											TaxonomyCache.impl.clear();
 											TaxonomyCache.impl.fetchTaxon(currentNode.getId(), true,
 													new GenericCallback<Taxon>() {
 												public void onFailure(Throwable caught) {
 													WindowUtils	.infoAlert("Success",
 													"Successfully undid the last taxomatic operation, " +
 													"but was unable to refresh the current taxon.");
 												}
 												public void onSuccess(Taxon result) {
 													//panelManager.taxonomicSummaryPanel.update(currentNode.getId());
 													ClientUIContainer.bodyContainer.refreshBody();
 													WindowUtils.infoAlert("Success",
 													"Successfully undid the last taxomatic operation.");
 												}
 											});
 										}
 									});
 								}
 							});
 						}
 					});
 				}
 			});
 		}
 		mainMenu.add(mItem);
 		
 		mItem = new MenuItem();
 		mItem.setText("View Taxomatic History");
 		mItem.setIconStyle("icon-taxomatic-history");
 		mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
 			public void componentSelected(MenuEvent ce) {
 				TaxomaticHistoryPanel panel = new TaxomaticHistoryPanel(getTaxon());
 				panel.show();
 			}
 		});
 		mainMenu.add(mItem);
 
 		mItem = new MenuItem();
 		mItem.setText("Move Assessments");
 		mItem.setIconStyle("icon-document-move");
 		mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
 			public void componentSelected(MenuEvent ce) {
 				popupChooser(new TaxomaticAssessmentMover(TaxonomyCache.impl.getCurrentTaxon()));
 			}
 		});
 		
 
 		mainMenu.add(mItem);
 
 		return toolbar;
 	}
 	
 	public void buildNotePopup() {
 		final Taxon taxon = getTaxon(); 
 		final NotesWindow window = new NotesWindow(new TaxonNoteAPI(taxon));
 		window.setHeading("Notes for " + taxon.getFullName());
 		window.show();	
 	}
 
 	public void buildReferencePopup() {
 		final Taxon taxon = getTaxon();
 		SimpleSISClient.getInstance().onShowReferenceEditor(
 			"Manage References for " + taxon.getFullName(), 
 			new ReferenceableTaxon(taxon, new SimpleListener() {
 				public void handleEvent() {
 					//update(taxon.getId());
 					//TaxonomyCache.impl.setCurrentTaxon(getSelectedItem());
 					ClientUIContainer.bodyContainer.refreshTaxonPage();
 				}
 			}), 
 			null, null
 		);
 	}
 
 	private void popupChooser(TaxomaticWindow chooser) {
 		chooser.show();
 		//chooser.center();
 	}
 
 	public void setAppropriateRights(Taxon node) {
 		
 		if (!AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, node)) {
 			Info.display("Insufficient Rights", "Notice: You do not have "
 					+ "sufficient permissions to edit this taxon.");
 			taxonToolsItem.hide();
 			taxomaticToolItem.hide();
 		} else if (!AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.USE_FEATURE, AuthorizableFeature.TAXOMATIC_FEATURE)) {
 			taxonToolsItem.show();
 			taxomaticToolItem.hide();
 		} else {
 			taxonToolsItem.show();
 			taxomaticToolItem.show();
 		}
 		
 		layout();
 	}
 	
 	private static class SynonymNoteAPI implements NoteAPI {
 		
 		private final Synonym synonym;
 		private final Taxon taxon;
 		
 		private boolean hasChanged;
 		
 		public SynonymNoteAPI(Taxon taxon, Synonym synonym) {
 			this.synonym = synonym;
 			this.taxon = taxon;
 			
 			hasChanged = false;
 		}
 
 		@Override
 		public void addNote(final Notes note, final GenericCallback<Object> callback) {
 			note.setSynonym(synonym);
 			
 			final NativeDocument document = SimpleSISClient.getHttpBasicNativeDocument();
 			document.put(UriBase.getInstance().getNotesBase() + "/notes/synonym/" + synonym.getId(), note.toXML(), new GenericCallback<String>() {
 				public void onSuccess(String result) {
 					Notes newNote = Notes.fromXML(document.getDocumentElement());
 					
 					note.setEdits(newNote.getEdits());
 					note.setId(newNote.getId());
 					
 					synonym.getNotes().add(note);
 					
 					hasChanged = true;
 					
 					callback.onSuccess(result);
 				}public void onFailure(Throwable caught) {
 					callback.onFailure(caught);
 				}
 			});
 		}
 		
 		@Override
 		public void deleteNote(final Notes note, final GenericCallback<Object> callback) {
 			final NativeDocument document = SimpleSISClient.getHttpBasicNativeDocument();
 			document.delete(UriBase.getInstance().getNotesBase() + "/notes/note/" + note.getId(), new GenericCallback<String>() {
 				public void onSuccess(String result) {
 					synonym.getNotes().remove(note);
 					hasChanged = true;
 					callback.onSuccess(result);
 				}
 				public void onFailure(Throwable caught) {
 					callback.onFailure(caught);
 				}
 			});
 		}
 		
 		@Override
 		public void loadNotes(ComplexListener<Collection<Notes>> listener) {
 			listener.handleEvent(synonym.getNotes());
 		}
 		
 		@Override
 		public void onClose() {
 			if (hasChanged)
 				ClientUIContainer.bodyContainer.refreshTaxonPage();
 				//TaxonomyCache.impl.setCurrentTaxon(taxon);
 				//ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel.update(taxon.getId());			
 		}
 		
 	}
 	
 	public static class ReferenceableTaxon implements Referenceable {
 		
 		private final Taxon taxon;
 		private final SimpleListener afterChangeListener;
 		
 		public ReferenceableTaxon(Taxon taxon, SimpleListener afterChangeListener) {
 			this.taxon = taxon;
 			this.afterChangeListener = afterChangeListener;
 		}
 		
 		public void addReferences(ArrayList<Reference> references, GenericCallback<Object> callback) {
 			taxon.getReference().addAll(references);
 			persist(callback);
 		}
 		
 		public Set<Reference> getReferencesAsList() {
 			return new HashSet<Reference>(taxon.getReference());
 		}
 
 		public void onReferenceChanged(GenericCallback<Object> callback) {
 
 		}
 
 		public void removeReferences(ArrayList<Reference> references, GenericCallback<Object> callback) {
 			taxon.getReference().removeAll(references);
 			persist(callback);
 		}
 		
 		private void persist(final GenericCallback<Object> callback) {
 			TaxonomyCache.impl.saveReferences(taxon, new GenericCallback<String>() {
 				public void onSuccess(String result) {
 					if (afterChangeListener != null)
 						afterChangeListener.handleEvent();
 					
 					callback.onSuccess(result);
 				}
 				public void onFailure(Throwable caught) {
 					callback.onFailure(caught);
 				}
 			});
 		}
 		
 	}
 	
 	public static class TaxonNoteAPI implements NoteAPI {
 		
 		private final Taxon taxon;
 		
 		public TaxonNoteAPI(Taxon taxon) {
 			this.taxon = taxon;
 		}
 		
 		@Override
 		public void addNote(final Notes note, final GenericCallback<Object> callback) {
 			final NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
 			String url = UriBase.getInstance().getNotesBase() + "/notes/taxon/"+ taxon.getId();
 			
 			doc.post(url, note.toXML(), new GenericCallback<String>() {
 				public void onFailure(Throwable caught) {
 					callback.onFailure(caught);							
 				};
 
 				public void onSuccess(String result) {
 					Notes note = Notes.fromXML(doc.getDocumentElement());
 					taxon.getNotes().add(note);
 					callback.onSuccess(result);
 				};
 			});
 		}
 		
 		@Override
 		public void deleteNote(final Notes note, final GenericCallback<Object> callback) {
 			NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
 			String url = UriBase.getInstance().getNotesBase() + "/notes/note/" + note.getId();
 
 			doc.delete(url, new GenericCallback<String>() {
 				public void onFailure(Throwable caught) {
 					callback.onFailure(caught);
 				};
 
 				public void onSuccess(String result) {
 					taxon.getNotes().remove(note);
 					callback.onSuccess(result);
 				};
 			});
 		}
 		
 		@Override
 		public void loadNotes(ComplexListener<Collection<Notes>> listener) {
 			listener.handleEvent(taxon.getNotes());
 		}
 		
 		@Override
 		public void onClose() {
 			ClientUIContainer.bodyContainer.refreshBody();
 		}
 		
 	}
 
 }
