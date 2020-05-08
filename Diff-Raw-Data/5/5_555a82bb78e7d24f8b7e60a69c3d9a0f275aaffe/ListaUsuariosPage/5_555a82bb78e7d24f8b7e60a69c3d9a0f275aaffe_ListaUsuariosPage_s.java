 package ar.noxit.ehockey.web.pages.usuarios;
 
 import ar.noxit.ehockey.model.Usuario;
 import ar.noxit.ehockey.service.IUsuarioService;
 import ar.noxit.ehockey.web.pages.base.AbstractContentPage;
 import ar.noxit.ehockey.web.pages.base.MensajePage;
 import ar.noxit.ehockey.web.pages.models.UsuarioAdapterModel;
 import ar.noxit.ehockey.web.pages.models.UsuarioModel;
 import ar.noxit.exceptions.NoxitException;
 import ar.noxit.exceptions.NoxitRuntimeException;
 import ar.noxit.web.wicket.provider.DataProvider;
 import java.util.List;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.repeater.Item;
 import org.apache.wicket.markup.repeater.data.DataView;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 public class ListaUsuariosPage extends AbstractUsuariosPage {
 
     @SpringBean
     private IUsuarioService usuarioService;
 
     public ListaUsuariosPage() {
         DataView<Usuario> tabla = new DataView<Usuario>("usuarios", new UsuariosProvider()) {
 
             @Override
             public void populateItem(final Item<Usuario> item) {
                 item.add(new Link<AbstractContentPage>("editarUsuario") {
 
                     @Override
                     public void onClick() {
                         setResponsePage(new EditarUsuarioPage(new UsuarioAdapterModel(item.getModel())));
                     }
                 }.add(new Label("usuario", new PropertyModel<String>(item.getModel(), "user"))));
                 item.add(new Label("nombre", new PropertyModel<String>(item.getModel(), "nombre")));
                 item.add(new Label("apellido", new PropertyModel<String>(item.getModel(), "apellido")));
                 item.add(new Link<MensajePage>("eliminar") {
 
                     @Override
                     public void onClick() {
                         try {
                             usuarioService.remove(item.getModelObject().getUser());
                             setResponsePage(new MensajePage("Baja de usuario", String.format(
                                     "El usuario %s ha sido dado de baja", item.getModelObject().getUser())));
                         } catch (NoxitException e) {
                             setResponsePage(new MensajePage("Baja de usuario",
                                     "No se pudo dar de baja el usuario, ocurrió un error durante la operación"));
                         }
                     }
                 });
             }
         };
         add(tabla);
 
         add(new BookmarkablePageLink<Void>("nuevo", AltaUsuarioPage.class));
     }
 
     public class UsuariosProvider extends DataProvider<Usuario> {
 
         @Override
         protected List<Usuario> loadList() {
             try {
                 return usuarioService.getAll();
             } catch (NoxitException e) {
                 throw new NoxitRuntimeException(e);
             }
         }
 
         @Override
         public IModel<Usuario> model(Usuario object) {
             return new UsuarioModel(new Model<String>(object.getUser()), usuarioService);
         }
 
     }
 }
