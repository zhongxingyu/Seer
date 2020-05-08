 package ar.noxit.ehockey.web.pages.torneo;
 
 import ar.noxit.ehockey.model.Partido;
 import ar.noxit.ehockey.service.IExceptionConverter;
 import ar.noxit.ehockey.service.IPartidoService;
 import ar.noxit.ehockey.service.transfer.PartidoInfo;
 import ar.noxit.ehockey.web.pages.base.AbstractColorBasePage;
 import ar.noxit.exceptions.NoxitException;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 public class ReprogramacionPartidoPage extends AbstractColorBasePage {
 
     @SpringBean
     private IPartidoService partidoService;
     @SpringBean
     private IExceptionConverter exceptionConverter;
 
     public ReprogramacionPartidoPage(final ModalWindow modalWindow, IModel<Partido> partido) {
         add(new PartidoFormPanel("form", convertFrom(partido)) {
 
             @Override
             protected void onSubmit(AjaxRequestTarget target, IModel<PartidoInfo> partido) {
                 try {
                     PartidoInfo pi = partido.getObject();
                     partidoService.reprogramar(pi);
 
                     modalWindow.close(target);
                 } catch (NoxitException e) {
                     error(exceptionConverter.convert(e));
                 }
             }
         }.setSubmitLabel("Reprogramar")
                .setLocalActivo(false)
                .setVisitanteActivo(false)
                 .setNumeroFechaActivo(false)
                 .setRuedaActivo(false));
     }
 
     private IModel<PartidoInfo> convertFrom(IModel<Partido> partido) {
         Partido p = partido.getObject();
         return Model.of(PartidoInfo.construir(p));
     }
 }
