 package com.br.biblioteca.controller;
 
 import static br.com.caelum.vraptor.view.Results.json;
 
 import java.util.List;
 
 import br.com.caelum.vraptor.Get;
 import br.com.caelum.vraptor.Path;
 import br.com.caelum.vraptor.Post;
 import br.com.caelum.vraptor.Put;
 import br.com.caelum.vraptor.Resource;
 import br.com.caelum.vraptor.Result;
 
 import com.br.biblioteca.dao.AdminSession;
 import com.br.biblioteca.dao.UsuarioDAO;
 import com.br.biblioteca.entitades.Usuario;
 
 @Resource
 public class UsuarioController {
 	
 	private Result result;
 	private UsuarioDAO usuarioDAO;
 	private AdminSession adminSession;
 	
 	public UsuarioController(Result result, UsuarioDAO usuarioDAO, AdminSession adminSession){
 		this.result = result;
 		this.usuarioDAO = usuarioDAO;
 		this.adminSession = adminSession;
 	}
 	
 	@Get
 	@Path("/usuarios")
 	public void index(String nome){
 		List<Usuario> usuarios = usuarioDAO.pesquisa(nome);
 		result.include("usuarios", usuarios);
 		result.include("nome", nome);
 		result.include("usuario", adminSession.getAdministrador().getNome());
 	}
 	
 	@Get
 	@Path("/usuarios/list/{nome}")
 	public void list(String nome){
 		List<Usuario> usuarios = usuarioDAO.pesquisa(nome);
 		result.use(json()).from(usuarios).serialize();
 	}
 	
 	@Get
 	@Path("/usuario/add")
 	public void novo(){
 	}
 	
 	@Post
 	@Path("/usuario/novo")
 	public void novo(Usuario usuario){
 		String message = null;
 		if(usuario.getNome().equals("") || usuario.getEmail().equals("")){
 			message = "Nome ou email nulos";
 		}else{
 			usuarioDAO.adiciona(usuario);
 			message = "\""+ usuario.getNome() + "\" adicionado com sucesso";
 		}
 		result.use(json()).from(message, "message").serialize();
 	}
 	
 	@Put @Post
	@Path("/usuario/atualiza")
 	public void atualiza(Usuario usuario){
 		String message;
 		if(usuario.equals(null)){
 			message = "Erro ao atualizar usuário";
 		}else{
 			usuario.setUsuarioAtivo(true);
 			usuarioDAO.atualiza(usuario);
 			message = "\""+ usuario.getNome() + "\" atualizado com sucesso";
 		}
 		result.use(json()).from(message, "message").serialize();
 	}
 	
 	@Post
 	@Path("/usuario/delete")
 	public void delete(List<Long> idDelete){
 		String message;
 		for (Long id : idDelete) {
 			Usuario usuario = usuarioDAO.pesquisarUsuarioPorId(id);
 			usuario.setUsuarioAtivo(false);
 			usuarioDAO.atualiza(usuario);
 		}
 		message = "Usuario(s) deletado(s) com sucesso";
 		result.use(json()).from(message, "message").serialize();
 	}
 }
