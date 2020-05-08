 package ar.noxit.ehockey.web.pages.usuarios;
 
 import org.apache.commons.lang.Validate;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
 
 import ar.noxit.ehockey.web.pages.base.AbstractContentPage;
 
 public class EditarUsuarioPage extends AbstractContentPage{
 
     private IModel<UsuarioDTO> usuario;
 
    public EditarUsuarioPage(Model<UsuarioDTO> usuario) {
         Validate.notNull(usuario, "El usuario no puede ser null");
         this.usuario = usuario;
 
         // creo los dos paneles. Uno por cada tipo de alta de usuario
         // los paneles solo se muestan si el usuario es del tipo que corresponde
         FormularioAdministradorPanel adminPanel = new FormularioAdministradorPanel("administradorPanel", usuario, new EditarUsuarioForm(usuario));
         FormularioRepresentantePanel represPanel = new FormularioRepresentantePanel("representantePanel", usuario, new EditarUsuarioForm(usuario));
         this.setOutputMarkupId(true);
 
         // panel para devolver errores de validación
         add(new FeedbackPanel("feedback"));
 
         //hace que no se pueda cambiar el nombre de usuario en el panel
         adminPanel.setUsuarioEditable(false);
         represPanel.setUsuarioEditable(false);
 
         add(adminPanel);
         add(represPanel);
     }
 }
