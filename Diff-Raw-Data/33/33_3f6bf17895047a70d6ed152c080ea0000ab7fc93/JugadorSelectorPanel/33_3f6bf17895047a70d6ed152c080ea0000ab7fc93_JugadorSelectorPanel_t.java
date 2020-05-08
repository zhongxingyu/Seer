 package ar.noxit.ehockey.web.pages.components;
 
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 import ar.noxit.ehockey.model.Club;
 import ar.noxit.ehockey.model.Equipo;
 import ar.noxit.ehockey.model.Jugador;
 import ar.noxit.ehockey.service.IClubService;
 import ar.noxit.ehockey.service.IEquipoService;
 import ar.noxit.ehockey.web.pages.models.ClubModel;
 import ar.noxit.ehockey.web.pages.models.ClubesListModel;
 import ar.noxit.ehockey.web.pages.models.EquipoModel;
 import ar.noxit.ehockey.web.pages.models.EquiposPorClubListModel;
 import ar.noxit.ehockey.web.pages.models.JugadoresPorEquipoListModel;
 import ar.noxit.ehockey.web.pages.renderers.ClubRenderer;
 import ar.noxit.ehockey.web.pages.renderers.EquipoRenderer;
 import ar.noxit.ehockey.web.pages.renderers.JugadorRenderer;
 import ar.noxit.ehockey.web.pages.report.ReportePrinterFriendly;
 
public abstract class JugadorSelectorPanel extends Panel {
 
     @SpringBean
     private IEquipoService equipoService;
     @SpringBean
     private IClubService clubService;
     private IModel<Integer> idClub = new Model<Integer>();
     private IModel<Integer> idEquipo = new Model<Integer>();
 
     private AjaxHybridSingleAndMultipleChoicePanel<Jugador> dropDownJugador;
     private AjaxHybridSingleAndMultipleChoicePanel<Equipo> dropDownEquipo;
 
     public JugadorSelectorPanel(String id, IModel<Jugador> jugador) {
         super(id);
 
         add(new Link<Void>("htmlreporte") {
 
             @Override
             public void onClick() {
                 setResponsePage(new ReportePrinterFriendly());
             }
         });
 
         dropDownJugador = new AjaxHybridSingleAndMultipleChoicePanel<Jugador>("jugador", jugador,
                 new JugadoresPorEquipoListModel(idEquipo, clubService), JugadorRenderer.get()) {
             @Override
             protected void onUpdate(AjaxRequestTarget target) {
                onUpdateJugador(target);
             }
         };
 
         dropDownJugador.setRequired(true);
         dropDownJugador.setOutputMarkupId(true);
         add(dropDownJugador);
 
         dropDownEquipo = new AjaxHybridSingleAndMultipleChoicePanel<Equipo>("equipo", new EquipoModel(idEquipo,
                 equipoService), new EquiposPorClubListModel(idClub, clubService), EquipoRenderer.get()) {
 
             @Override
             protected void onUpdate(AjaxRequestTarget target) {
                 onUpdateEquipo(target);
             }
 
         };
 
         dropDownEquipo.setRequired(true);
         dropDownEquipo.setOutputMarkupId(true);
         add(dropDownEquipo);
 
         add(new AjaxHybridSingleAndMultipleChoicePanel<Club>("club", new ClubModel(idClub, clubService),
                 new ClubesListModel(clubService), new ClubRenderer()) {
 
             @Override
             protected void onUpdate(AjaxRequestTarget target) {
                onUpdateClub(target);
             }
 
         });
     }
 
     protected void onUpdateClub(AjaxRequestTarget target) {
        //idClub.setObject(null);
        target.addComponent(dropDownEquipo);
        target.addComponent(dropDownJugador);
     }
 
     protected void onUpdateEquipo(AjaxRequestTarget target) {
        //idEquipo.setObject(null);
         target.addComponent(dropDownJugador);
     }
 
    protected abstract void onUpdateJugador(AjaxRequestTarget target);
 
     @Override
     protected boolean getStatelessHint() {
         return false;
     }
 }
