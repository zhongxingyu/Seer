 package it.agilis.mens.azzeroCO2.client.components.annoAttivita;
 
 import com.extjs.gxt.ui.client.Style;
 import com.extjs.gxt.ui.client.data.ModelData;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
 import com.extjs.gxt.ui.client.mvc.Dispatcher;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.util.Margins;
 import com.extjs.gxt.ui.client.util.Padding;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.Text;
 import com.extjs.gxt.ui.client.widget.grid.*;
 import com.extjs.gxt.ui.client.widget.layout.HBoxLayoutData;
 import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
 import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.ui.Image;
 import it.agilis.mens.azzeroCO2.client.AzzeroCO2Resources;
 import it.agilis.mens.azzeroCO2.client.mvc.events.UnAnnoDiAttivitaEvents;
 import it.agilis.mens.azzeroCO2.client.services.CalcoliHelper;
 import it.agilis.mens.azzeroCO2.shared.model.OrdineModel;
 import it.agilis.mens.azzeroCO2.shared.model.RiepilogoModel;
 import it.agilis.mens.azzeroCO2.shared.model.pagamento.Esito;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: serenadimaida
  * Date: 06/12/11
  * Time: 23:36
  * To change this template use File | Settings | File Templates.
  */
 public class AnnoWest extends LayoutContainer {
     private Grid<RiepilogoModel> grid;
     private ListStore<RiepilogoModel> store = new ListStore<RiepilogoModel>();
     private Text title = new Text("Anno di attività");
     private final String oggettoDiDefault = "Non hai ancora inserito <br> nessuna attività";
     private Esito esito;
 
     private DateTimeFormat dateFormat = DateTimeFormat.getFormat("dd.MM.y");
 
 
     public AnnoWest() {
         RiepilogoModel model = new RiepilogoModel();
         model.setOggetto(oggettoDiDefault);
         store.add(model);
     }
 
     @Override
     protected void onRender(Element target, int index) {
         super.onRender(target, index);
 
         VBoxLayout layout = new VBoxLayout();
         layout.setPadding(new Padding(5, 0, 0, 0));
         layout.setVBoxLayoutAlign(VBoxLayout.VBoxLayoutAlign.CENTER);
         setLayout(layout);
 
         HBoxLayoutData flex = new HBoxLayoutData(new Margins(0, 0, 0, 0));
         flex.setFlex(1);
 
         add(title, new VBoxLayoutData(new Margins(5, 5, 0, 5)));
 
         ContentPanel panel = new ContentPanel();
         panel.setHeaderVisible(false);
         panel.setBodyStyle("background-Color: #d9dadb;");
         panel.setBorders(false);
         panel.add(createGrid(), new VBoxLayoutData(new Margins(0, 5, 2, 0)));
 
         title.setStyleAttribute("background-Color", "#d9dadb");
         title.setStyleAttribute("color", "black");
         title.setStyleAttribute("font-family", "arial");
         title.setStyleAttribute("text-align", "center");
        title.setStyleAttribute("vertical-align ", "middle");
         title.setWidth(215);
         title.setHeight(70);
         title.setStyleAttribute("font-size", "14px");
 
         //panel.setStyleAttribute("backgroundColor", "#E9E9E9");
         panel.setStyleAttribute("background-Color", "#d9dadb");
         panel.setHeight(300);
         panel.setStyleAttribute("border-top", "none");
        panel.setStyleAttribute("border-top-color", "#d9dadb !important");
         panel.setShadow(true);
         add(panel, flex);
     }
 
     private Grid<RiepilogoModel> createGrid() {
         List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
         ColumnConfig column = new ColumnConfig("img", "img", 24);
         column.setAlignment(Style.HorizontalAlignment.LEFT);
         column.setRenderer(new GridCellRenderer() {
             @Override
             public Object render(ModelData model, String property, ColumnData config, int rowIndex, int colIndex, ListStore listStore, Grid grid) {
                 config.style += "background-color: #d9dadb;";
                 List<RiepilogoModel> r = listStore.getModels();
                 if (r.size() == 1 && r.get(0).getOggetto().equalsIgnoreCase(oggettoDiDefault)) {
                     return null;
                 }
 
                 return new Image(AzzeroCO2Resources.INSTANCE.checkIcon());//new ToolButton("x-tool-pin");
             }
         });
 
         configs.add(column);
 
         column = new ColumnConfig("oggetto", "oggetto", 186);
         column.setAlignment(Style.HorizontalAlignment.LEFT);
         column.setRenderer(new GridCellRenderer() {
             @Override
             public Object render(ModelData model, String property, ColumnData config, int rowIndex, int colIndex, ListStore listStore, Grid grid) {
                 Text text = new Text((String) model.get(property));
                 text.setStyleAttribute("background-Color", "#d9dadb");
                 text.setStyleAttribute("color", "black");
                 text.setStyleAttribute("font-family", "arial");
 
                 config.style += "background-color: #d9dadb;";
 
                 return text;
             }
         });
         configs.add(column);
         ColumnModel cm = new ColumnModel(configs);
 
         grid = new Grid<RiepilogoModel>(store, cm);
         grid.setAutoHeight(true);
         grid.setHeight(400);
         grid.setWidth(210);
         grid.setHideHeaders(true);
         grid.setStyleAttribute("background-color", "#E9E9E9");
         grid.disableTextSelection(true);
         grid.setTrackMouseOver(false);
 
         grid.getSelectionModel().setSelectionMode(Style.SelectionMode.SINGLE);
         grid.getSelectionModel().addListener(Events.SelectionChange,
                 new Listener<SelectionChangedEvent<RiepilogoModel>>() {
                     public void handleEvent(SelectionChangedEvent<RiepilogoModel> be) {
                         if (be.getSelection().size() > 0) {
                             if (!Esito.PAGATO.equals(esito)) {
                                 Dispatcher.forwardEvent(UnAnnoDiAttivitaEvents.ShowStep, be.getSelectedItem());
                             }
                         }
                     }
                 });
 
         grid.setBorders(false);
 
         return grid;
     }
 
 
     public void setInStore(OrdineModel riepilogo, Esito esito) {
         List<RiepilogoModel> model = CalcoliHelper.getListOfRiepilogoModelLazy(riepilogo);
         store.removeAll();
         if (model == null || model.size() == 0) {
             RiepilogoModel m = new RiepilogoModel();
             m.setOggetto("Non hai ancora inserito <br> nessuna attività");
             store.add(m);
         } else {
             this.esito = esito;
             store.add(model);
         }
         setTitle(riepilogo);
     }
 
     public void setTitle(OrdineModel riepilogo) {
         if (riepilogo != null) {
             String nome = riepilogo.getNome() != null ? riepilogo.getNome() : "Un anno di attività";
             String dove = riepilogo.getDove() != null ? riepilogo.getDove() : "";
 
             if (riepilogo.getInizio() != null
                     && riepilogo.getFine() != null) {
                 String dal = "<br>dal " + dateFormat.format(riepilogo.getInizio());
                 String a = " al " + dateFormat.format(riepilogo.getFine());
                 this.title.setText(nome + "<br>" + dove + dal + a);
             } else {
                 this.title.setText(nome + "<br>" + dove);
             }
         }
     }
 
     public void clean() {
         setInStore(null, Esito.IN_PAGAMENTO);
         this.title.setTitle(".....");
     }
 
     public void isInRiepilogo(OrdineModel riepilogo) {
         setTitle(riepilogo);
         store.removeAll();
         RiepilogoModel m = new RiepilogoModel();
         m.setOggetto("Hai terminato il calcolo! <br>" +
                 "Se vuoi modifica i dati inseriti<br>" +
                 " cliccando sulla voce relativa.");
         store.add(m);
     }
 
     public void isScegliProgettoCompensazione(OrdineModel riepilogo) {
         setTitle(riepilogo);
         store.removeAll();
         RiepilogoModel m = new RiepilogoModel();
         m.setOggetto("Scegli un progetto di <br>" +
                 "compensazione.<br>" +
                 "Controlla il preventivo e <br>" +
                 "accedi al sistema di <br>" +
                 "pagamento.");
         store.add(m);
     }
 
     public void isInConferma(OrdineModel riepilogo) {
         setTitle(riepilogo);
         store.removeAll();
         RiepilogoModel m = new RiepilogoModel();
         m.setOggetto("Il Percorso è finito!");
         store.add(m);
     }
 
 }
