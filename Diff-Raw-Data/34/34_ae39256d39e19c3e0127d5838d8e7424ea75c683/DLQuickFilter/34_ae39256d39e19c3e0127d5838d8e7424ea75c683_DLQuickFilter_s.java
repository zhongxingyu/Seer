 package cz.datalite.zk.components.list.view;
 
 import cz.datalite.helpers.EqualsHelper;
 import cz.datalite.helpers.ZKBinderHelper;
 import cz.datalite.zk.components.list.controller.DLQuickFilterController;
 import cz.datalite.zk.components.list.model.DLColumnUnitModel;
 import org.zkoss.lang.Strings;
 import org.zkoss.util.resource.Labels;
 import org.zkoss.zk.ui.Component;
 import org.zkoss.zk.ui.WrongValueException;
 import org.zkoss.zk.ui.event.Event;
 import org.zkoss.zk.ui.event.EventListener;
 import org.zkoss.zk.ui.event.Events;
 import org.zkoss.zul.*;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map.Entry;
 
 /**
  * Component for tool which allows user to quickly filter in the listbox
  * @author Karel Čemus <cemus@datalite.cz>
  */
 public class DLQuickFilter extends org.zkoss.zul.Hbox {
 
     // Controller
     protected DLQuickFilterController controller;
     // Model
     protected List<Entry<DLColumnUnitModel, String>> model;
     protected boolean quickFilterAll = true;
     protected String quickFilterDefault;
     // View
     protected final Menupopup popup;
     protected final Textbox textbox;
     protected final Label selector;
     // Constants
     protected static final String CONST_DEFAULT_ICON_PATH = "~./dlzklib/img/";
     protected static final String CONST_IMAGE_SIZE = "20px";
     protected static final String CONST_IMAGE_STYLE = "";
     private static final String CONST_POINTER_STYLE = "cursor: pointer;";
 
     // Variables
     private Hbox parent;
     private Image activateFilter;
 
     public DLQuickFilter() {
         super();
 
         parent = new Hbox();
         parent.setSclass( "datalite-listbox-qfiltr" );
         appendChild( parent );
 
         selector = new Label();
         selector.setSclass( "datalite-listbox-qfiltr-selector" );
         selector.setStyle(CONST_POINTER_STYLE);
         selector.setTooltiptext( Labels.getLabel( "quickFilter.tooltip.filterRange" ) );
         selector.addEventListener( Events.ON_CLICK, new EventListener() {
 
             public void onEvent( final Event event ) {
                 popup.open( selector );
             }
         } );
         parent.appendChild( selector );
 
         popup = new Menupopup();
         popup.setSclass( "datalite-listbox-qfiltr-popup" );
         popup.setStyle( "z-index: 100000 !important;" );
         parent.appendChild( popup );
 
         final Image open = new Image();
         open.setSclass( "datalite-listbox-qfiltr-open" );
         open.setTooltiptext( Labels.getLabel( "quickFilter.tooltip.openFilter" ) );
         open.setSrc( CONST_DEFAULT_ICON_PATH + "open.png" );
         open.setStyle(CONST_POINTER_STYLE);
         open.setWidth("10px");
         open.setHeight(CONST_IMAGE_SIZE);
         open.addEventListener( Events.ON_CLICK, new org.zkoss.zk.ui.event.EventListener() {
 
             public void onEvent( final org.zkoss.zk.ui.event.Event event ) {
                 popup.open( selector );
             }
         } );
         parent.appendChild( open );
 
         textbox = new Textbox();
         textbox.setSclass( "datalite-listbox-qfiltr-textbox" );
         addEventListener( Events.ON_OK, new org.zkoss.zk.ui.event.EventListener() {
 
             public void onEvent( final org.zkoss.zk.ui.event.Event event ) {
                 onQuickFilter();
             }
         } );
         parent.appendChild( textbox );
 
         activateFilter = new Image();
         activateFilter.setSclass( "datalite-listbox-qfiltr-image" );
         activateFilter.setSrc( CONST_DEFAULT_ICON_PATH + "search25x25.png" );
         activateFilter.setStyle( CONST_IMAGE_STYLE );
         activateFilter.setWidth( CONST_IMAGE_SIZE );
         activateFilter.setHeight( CONST_IMAGE_SIZE );
         activateFilter.setStyle(CONST_POINTER_STYLE);
         activateFilter.addEventListener( Events.ON_CLICK, new org.zkoss.zk.ui.event.EventListener()
         {
             public void onEvent( final org.zkoss.zk.ui.event.Event event ) {
                 onQuickFilter();
             }
         } );
         appendChild( activateFilter );
         
     }
 
     public void setController( final DLQuickFilterController controller ) {
         this.controller = controller;
         setAttribute( getUuid() + "_model", controller, Component.COMPONENT_SCOPE);
         ZKBinderHelper.registerAnnotation( textbox, "value", "value", getUuid() + "_model.bindingModel.value" );
     }
 
     public void fireChanges() {
         // odstraníme všechny položky
         while ( popup.getChildren().size() > 0 ) {
             popup.removeChild( popup.getLastChild() );
         }
 
         // znovu vytvoříme položky, podle modelu
         if ( quickFilterAll ) {
             popup.appendChild( new Menuitem( Labels.getLabel( "quickFilter.menu.all" ) ) {
 
                 {
                     setValue( cz.datalite.zk.components.list.filter.QuickFilterModel.CONST_ALL );
                     addEventListener( Events.ON_CLICK, new SelectMenuItemEventListener( this ) );
                 }
             } );
         }
 
         for ( final Entry<DLColumnUnitModel, String> entry : model ) {
             popup.appendChild( new Menuitem( entry.getValue() ) {
 
                 {
                     setAttribute( "model", entry.getKey() );
                     setValue( entry.getKey().getColumn() );
                     addEventListener( Events.ON_CLICK, new SelectMenuItemEventListener( this ) );
                 }
             } );
         }
 
 
         setActiveFilter();
     }
 
     protected void setActiveFilter() {
         // Actual button selection
         final DLColumnUnitModel unit = controller.getBindingModel().getModel();
         for (Entry<DLColumnUnitModel, String> entry : model) {
             if (EqualsHelper.isEqualsNull(entry.getKey(), unit)) {
                 setActiveFilter((Menuitem) popup.getChildren().get(model.indexOf(entry) + (quickFilterAll ? 1 : 0)));
                 return;
             }
         }
         if ( popup.getChildren().size() > 0 ) {
             setActiveFilter( ( Menuitem ) popup.getFirstChild() );
             setVisible( true );
         } else {
             setVisible( false );
         }
     }
 
     protected void setActiveFilter( final Menuitem item ) {
         selector.setValue( item.getLabel() );
         controller.getBindingModel().setKey( item.getValue() );
     }
 
     public void onQuickFilter() {
         if (!controller.validateQuickFilter())
             throw new WrongValueException(textbox, Labels.getLabel("quickFilter.validation.failed"));
 
         controller.onQuickFilter();
     }
 
     public void setModel( final List<Entry<DLColumnUnitModel, String>> model ) {
         for ( final Iterator<Entry<DLColumnUnitModel, String>> it = model.iterator(); it.hasNext(); ) {
             final Entry<DLColumnUnitModel, String> entry = it.next();
             if ( entry.getValue() == null || Strings.isEmpty( entry.getValue() ) ) {
                 it.remove();
             }
         }
         this.model = model;
     }
 
     public void setQuickFilterAll( final boolean quickFilterAll ) {
         this.quickFilterAll = quickFilterAll;
     }
 
     public void setQuickFilterDefault( final String quickFilterDefault ) {
         this.quickFilterDefault = quickFilterDefault;
     }
 
     public String getQuickFilterDefault() {
         return this.quickFilterDefault;
     }
 
     /**
      * Setting focus on Quick Filter means focus on textbox
      * @param focus if true, textbox is selected
      */
     @Override
     public void setFocus(boolean focus)
     {
         textbox.setFocus(focus);
     }
 
 
 
     class SelectMenuItemEventListener implements org.zkoss.zk.ui.event.EventListener {
 
         protected final Menuitem item;
         protected final String column;
         protected final DLColumnUnitModel model;
 
         public SelectMenuItemEventListener( final Menuitem item ) {
             this.item = item;
             this.column = item.getValue();
             this.model = (DLColumnUnitModel) item.getAttribute("model");
         }
 
         public void onEvent( final org.zkoss.zk.ui.event.Event event ) {
             controller.getBindingModel().setKey( column );
             controller.getBindingModel().setModel( model );
             setActiveFilter( item );
         }
     }
 
     @Override
     public String getSclass() {
         // listControll parent may contain default values for class
         if ( getParent() instanceof DLListControl ) {
             return super.getSclass() + " " + (( DLListControl ) getParent()).getQFilterClass();
         } else {
             return super.getSclass();
         }
     }
 
     @Override
     public String getStyle() {
         // listControll parent may contain default values for style
         if ( getParent() instanceof DLListControl ) {
             return super.getStyle() + " " + (( DLListControl ) getParent()).getQFilterStyle();
         } else {
             return super.getStyle();
         }
     }
 
     /**
      * Setting filter button with label = parameter.
      * @param quickFilterButton label for button.
      */
     public void setQuickFilterButton(String quickFilterButton)
     {
         // whether i want filter button
         if(quickFilterButton != null)
         {
             activateFilter.setParent(null);
 
             final Button button = new Button();
             button.setLabel(quickFilterButton);
             button.setImage(CONST_DEFAULT_ICON_PATH + "search25x25.png");
             button.setClass("quick-filter-button");
             button.setStyle(CONST_POINTER_STYLE);
             button.addEventListener( Events.ON_CLICK, new org.zkoss.zk.ui.event.EventListener()
             {
                 public void onEvent( final org.zkoss.zk.ui.event.Event event )
                 {
                     onQuickFilter();
                 }
             } );
             parent.appendChild( button );
 
             ((DLListControl)this.getParent()).setHeight("34px");
         }
     }
     
 }
