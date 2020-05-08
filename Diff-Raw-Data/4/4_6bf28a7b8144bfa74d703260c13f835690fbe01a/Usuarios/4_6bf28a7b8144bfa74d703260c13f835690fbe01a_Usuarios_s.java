 package controllers;
 
 import models.Cliente;
 import models.Empleado;
 import models.Pelicula;
 import play.data.Form;
 import play.mvc.Controller;
 import play.mvc.Result;
 import views.html.index;
 import views.html.vistaPelicula;
 
 public class Usuarios extends Controller {
 
 	private static Form<Cliente> formCliente = Form.form(Cliente.class);
 
 	public static Result index() {
 		return ok(index.render(Pelicula.findAll(), formCliente));
 	}
 
 	public static Result login() {
 		Form<Cliente> formularioRecibido = formCliente.bindFromRequest();
 
 		String login = formularioRecibido.field("login").value();
 		String password = formularioRecibido.field("password").value();
 
 		Cliente cliente = Cliente.findByLogin(login);
 
 		if (cliente == null || !password.equals(cliente.getPassword())) {
			return badRequest(index.render(Pelicula.findAll(), formCliente));
 		} else {
 			session().put("usuario", login);
 			return redirect(routes.Usuarios.index());
 		}
 	}
 
 	public static Result verPelicula(Long id) {
 		Pelicula pelicula = Pelicula.findById(id);
 
 		if (pelicula == null) {
 			return badRequest(index.render(Pelicula.findAll(), formCliente));
 		} else {
 			return ok(vistaPelicula.render(pelicula));
 		}
 	}
 
 	public static Result rellenarDb() {
 
 		Cliente cliente = new Cliente();
 		cliente.setPassword("pass");
 		cliente.setLogin("login");
 		cliente.save();
 		Empleado empleado = new Empleado();
 		empleado.setPassword("pass");
 		empleado.setLogin("empleado");
 		empleado.setAdmin(false);
 		empleado.save();
 		Empleado admin = new Empleado();
 		admin.setPassword("pass");
 		admin.setLogin("admin");
 		admin.save();
 		Pelicula peli = new Pelicula();
 		peli.setTitulo("Pelicula 1");
 		peli.setAnio(2013);
 		peli.setGenero("Accion");
 		peli.save();
 		Pelicula peli2 = new Pelicula();
 		peli2.setTitulo("Pelicula 2");
 		peli2.setAnio(2013);
 		peli2.setGenero("Accion");
 		peli2.save();
 
 		return redirect(routes.Usuarios.index());
 	}
 }
