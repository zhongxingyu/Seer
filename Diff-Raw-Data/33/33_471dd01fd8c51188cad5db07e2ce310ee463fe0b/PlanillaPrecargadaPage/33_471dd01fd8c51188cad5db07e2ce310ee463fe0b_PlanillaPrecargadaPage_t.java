 package ar.noxit.ehockey.web.pages.planilla;
 
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.model.AbstractReadOnlyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 import ar.noxit.ehockey.model.Partido;
 import ar.noxit.ehockey.service.IDateTimeProvider;
 import ar.noxit.ehockey.service.IPlanillaService;
 import ar.noxit.ehockey.web.pages.base.AbstractHeaderPage;
 import ar.noxit.ehockey.web.pages.header.IMenuItem;
 import ar.noxit.ehockey.web.pages.torneo.TorneoPage;
 
 public class PlanillaPrecargadaPage extends AbstractHeaderPage {
 
     @SpringBean
     private IDateTimeProvider dateTimeProvider;
     @SpringBean
     private IPlanillaService planillaService;
 
     public PlanillaPrecargadaPage(final IModel<Partido> partido) {
         add(new Link<Void>("html_planilla") {
 
             @Override
             public void onClick() {
                 setResponsePage(new PlanillaPrinterFriendly(new PlanillaPrecargadaModel(partido, dateTimeProvider, planillaService)));
             }
         });
 
        add(new VerPartidosLink("volver", partido));
         add(new PlanillaPanel("panelPlanilla", new PlanillaPrecargadaModel(partido, dateTimeProvider, planillaService)));
 
         add(new Label("estado_partido", new AbstractReadOnlyModel<String>() {
 
             @Override
             public String getObject() {
                 Partido object = partido.getObject();
                 return object.isJugado() ? "Jugado" : "No jugado";
             }
         }));
     }
 
     @Override
     public boolean shouldBeSelected(IMenuItem menuItem) {
         return menuItem.getPageLink().equals(TorneoPage.class);
     }
 }
