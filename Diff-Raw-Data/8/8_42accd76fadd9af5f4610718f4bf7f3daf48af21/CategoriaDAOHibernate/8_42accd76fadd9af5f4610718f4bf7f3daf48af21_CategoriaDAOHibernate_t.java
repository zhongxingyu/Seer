 package financeiro.categoria;
 
 import java.util.List;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 
 import financeiro.usuario.Usuario;
 
 public class CategoriaDAOHibernate implements CategoriaDAO {
 	
 	private Session session;
 	
 	public void setSession(Session session) {
 		this.session = session;
 	}
 	
 	@Override
 	public Categoria salvar(Categoria categoria) {
 		Categoria merged = (Categoria) session.merge(categoria);
 		session.flush();
 		session.clear();
 		return merged;
 	}
 	
 	@Override
 	public void excluir(Categoria categoria) {
 		categoria = (Categoria) this.carregar(categoria.getCodigo());
 		session.delete(categoria);
 		session.flush();
 		session.clear();
 	}
 
 
 	@Override
 	public Categoria carregar(Integer categoria) {
 		return (Categoria) session.get(Categoria.class, categoria);
 	}
 
 	@Override
 	public List<Categoria> listar(Usuario usuario) {
 		String hql = "select c from Categoria c where c.pai is null and c.usuario = :usuario";
 		Query query = session.createQuery(hql);
 		
		query.setInteger("usuario", usuario.getCodigo());
		
 		@SuppressWarnings("unchecked")
 		List<Categoria> lista = query.list();
 		return lista;
 	}
 }
