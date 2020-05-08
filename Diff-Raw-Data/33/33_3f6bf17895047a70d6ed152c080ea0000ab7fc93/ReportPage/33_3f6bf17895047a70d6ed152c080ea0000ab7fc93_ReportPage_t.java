 package ar.noxit.ehockey.web.pages.report;
 
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 import ar.noxit.ehockey.service.IJugadorService;
 import ar.noxit.ehockey.web.pages.components.JugadorSelectorPanel;
 import ar.noxit.ehockey.web.pages.models.JugadorModel;
 
 public class ReportPage extends AbstractReportPage {
 
     @SpringBean
     private IJugadorService jugadorService;
     private IModel<Integer> idJugador = new Model<Integer>();
     private JugadorReportePanel jugadorPanel;
 
     public ReportPage() {
         add(new JugadorSelectorPanel("selectorClubEquipoJugador", new JugadorModel(idJugador, jugadorService)) {
 
             @Override
             protected void onUpdateClub(AjaxRequestTarget target) {
                 super.onUpdateClub(target);
                 target.addComponent(jugadorPanel);
             }
 
             @Override
             protected void onUpdateEquipo(AjaxRequestTarget target) {
                 super.onUpdateEquipo(target);
                 target.addComponent(jugadorPanel);
             }
 
             @Override
             protected void onUpdateJugador(AjaxRequestTarget target) {
                //idJugador.setObject(null);
                 target.addComponent(jugadorPanel);
             }
         });

         jugadorPanel = new JugadorReportePanel("jugadorpanel", jugadorService, idJugador);
         jugadorPanel.setOutputMarkupId(true);
         add(jugadorPanel);
     }
 
 }
