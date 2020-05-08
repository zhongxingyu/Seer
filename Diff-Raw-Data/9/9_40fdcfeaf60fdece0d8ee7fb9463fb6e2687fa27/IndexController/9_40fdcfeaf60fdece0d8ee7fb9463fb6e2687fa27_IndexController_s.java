 package comentesobre.controller;
 
 import java.util.List;
 
 import br.com.caelum.vraptor.Path;
 import br.com.caelum.vraptor.Resource;
 import br.com.caelum.vraptor.Result;
 
 import comentesobre.dao.ComentarioDao;
 import comentesobre.modelo.Comentario;
 
 @Resource
 public class IndexController {
 
 	private ComentarioDao comentarioDao;
 	private Result result;
 	
 	public IndexController(ComentarioDao comentarioDao, Result result) {
 		this.comentarioDao = comentarioDao;
 		this.result = result;
 	}
 	
 	@Path("/")
 	public void index() { 
 		
 	}
 
 	@Path("/{comentario.assunto}")
 	public void form(Comentario comentario) {
 		System.out.println(comentario.getAssunto());
 	}
 	
 	public void salva(Comentario comentario) {
		comentarioDao.salva(comentario);
 		result.redirectTo(IndexController.class).lista();
 	}
 	
 	public void lista() {
 		List<Comentario> comentarios = comentarioDao.lista();
 		result.include("comentarios", comentarios);
 	}
 }
