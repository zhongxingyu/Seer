 package ar.noxit.ehockey.web.pages.planilla;
 
 import ar.noxit.ehockey.model.Partido;
 import ar.noxit.ehockey.model.PlanillaFinal;
 import ar.noxit.ehockey.service.IDateTimeProvider;
 import ar.noxit.ehockey.web.pages.base.AbstractHeaderPage;
 import ar.noxit.ehockey.web.pages.header.IMenuItem;
 import ar.noxit.ehockey.web.pages.torneo.TorneoPage;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.model.AbstractReadOnlyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 public class PlanillaPage extends AbstractHeaderPage {
 
     @SpringBean
     private IDateTimeProvider dateTimeProvider;
 
     public PlanillaPage(final IModel<Partido> partido) {
         IModel<PlanillaFinal> planillaModel = new PlanillaFinalModel(partido, dateTimeProvider);
         add(new FeedbackPanel("feedback"));
 
         add(new RechazoAceptacionPlanillaForm("acciones_form", planillaModel, partido));
         add(new PlanillaEstadosForm("planillaForm", planillaModel, partido));
         add(new PlanillaPanel("panelPlanilla", planillaModel));
        add(new VerPartidosLink("volver", partido));
 
         crearLabelEstado(partido, planillaModel);
         crearLinkHTML(planillaModel);
     }
 
     private void crearLinkHTML(final IModel<PlanillaFinal> planillaModel) {
         add(new Link<Void>("html_planilla") {
 
             @Override
             public void onClick() {
                 setResponsePage(new PlanillaPrinterFriendly(planillaModel));
             }
         });
     }
 
     private void crearLabelEstado(final IModel<Partido> partido, final IModel<PlanillaFinal> planillaModel) {
         add(new Label("estado", new PropertyModel<String>(planillaModel, "estado")));
         add(new Label("estado_partido", new AbstractReadOnlyModel<String>() {
 
             @Override
             public String getObject() {
                 Partido object = partido.getObject();
                 return object.isJugado() ? "Jugado" : "No jugado";
             }
         }));
         add(new Label("comentario", new PropertyModel<String>(planillaModel, "comentario")) {
 
             @Override
             public boolean isVisible() {
                 return planillaModel.getObject().isRechazada();
             }
         });
     }
 
     @Override
     public boolean shouldBeSelected(IMenuItem menuItem) {
         return menuItem.getPageLink().equals(TorneoPage.class);
     }
 }
