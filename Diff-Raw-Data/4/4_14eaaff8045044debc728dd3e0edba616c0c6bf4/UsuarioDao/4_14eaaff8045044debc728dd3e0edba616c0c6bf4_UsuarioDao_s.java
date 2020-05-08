 package dao;
 
 import modelo.entidade.Usuario;
 
 import org.hibernate.Session;
 import org.hibernate.criterion.Restrictions;
 
 import br.com.caelum.vraptor.ioc.Component;
 
 @Component
 public class UsuarioDao extends Dao<Usuario> {
 
 	public UsuarioDao(Session session) {
 		super(session);
 	}
 	
 	
 	public boolean existeLoginSenha(Usuario usr) {
		return lista(Restrictions.eq("login", usr.getLogin()), Restrictions.eq("senha", usr.getEncryptedSenha())).size() > 0;
 	}

 }
