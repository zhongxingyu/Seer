 package ar.noxit.ehockey.web.pages.torneo;
 
 import ar.noxit.ehockey.service.transfer.PartidoInfo;
 import ar.noxit.ehockey.web.pages.base.AbstractColorBasePage;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
 import org.apache.wicket.model.IModel;
 
 public class AgregarPartidoPage extends AbstractColorBasePage {
 
     public AgregarPartidoPage(final ModalWindow modalWindow, IModel<PartidoInfo> partido) {
         add(new PartidoFormPanel("form", partido) {
 
             @Override
             protected void onSubmit(AjaxRequestTarget target, IModel<PartidoInfo> partido) {
                 modalWindow.close(target);
             }
         }.setSubmitLabel("Asignar fecha")
                 .setNumeroFechaActivo(false)
                 .setRuedaActivo(false));
     }
 }
