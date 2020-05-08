 package controller;
 
 import java.util.List;
 
 import modelo.entidade.Aluno;
 import modelo.entidade.Responsavel;
 import br.com.caelum.vraptor.Delete;
 import br.com.caelum.vraptor.Get;
 import br.com.caelum.vraptor.Path;
 import br.com.caelum.vraptor.Post;
 import br.com.caelum.vraptor.Put;
 import br.com.caelum.vraptor.Resource;
 import br.com.caelum.vraptor.Result;
 import dao.AlunoDao;
 import dao.ResponsavelDao;
 
 @Resource
 public class AlunoController {
 	
 	private final AlunoDao dao;
 	private final ResponsavelDao responsavelDao;
 	private final Result result;
 	private List<Responsavel> respList;
 
 	public AlunoController(AlunoDao dao,ResponsavelDao responsavelDao, Result result){
 		this.dao = dao;
 		this.responsavelDao = responsavelDao;
 		this.result = result;
 	}
 	
 	@Get @Path("/aluno/adicionar")
 	public void adicionar() {
 		respList = responsavelDao.listaTudo(); 
 		result.include("responsavelList", respList);
 	}
 	
 	@Post @Path("/aluno/adicionar")
 	public void adicionar(Aluno aluno) {		
 		dao.salva(aluno);
		result.redirectTo(MatriculaController.class).adicionar();
 	}
 	
 	@Get @Path("/aluno/{id}")
 	public Aluno editar(Long id){
 		respList = responsavelDao.listaTudo();
 		result.include("responsavelList", respList);
 		return dao.carrega(id);
 	}
 	
 	@Put @Path("/aluno/{aluno.id}")
 	public void alterar(Aluno aluno){
 		dao.atualiza(aluno);
 		result.redirectTo(this).listar();
 	}
 	
 	@Get @Path("/aluno/listar")
 	public List<Aluno> listar(){
 		return dao.listaTudo();
 	}
 	
 	@Delete @Path("/aluno/{id}")
 	public void deletar(Long id){
 		dao.deleta(id);
 		result.redirectTo(this).listar();
 	}
 	
 	
 }
