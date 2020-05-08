 package no.niths.infrastructure;
 
 import no.niths.domain.Application;
 import no.niths.infrastructure.interfaces.ApplicationRepository;
 
 import org.springframework.stereotype.Repository;
 
 @Repository
 public class ApplicationRepositoryImpl extends AbstractGenericRepositoryImpl<Application>
 		implements ApplicationRepository {
 
 	public ApplicationRepositoryImpl() {
 		super(Application.class);
 	}
 	
 	@Override
 	public Application getByApplicationToken(String token) {
 		String sql = "from " + Application.class.getSimpleName() + " a " +
						"where a.developerToken = :token and a.enabled=true";
 		return (Application) getSession().getCurrentSession().createQuery(sql)
 										.setString("token", token).uniqueResult();
 		
 	}
 
 	@Override
 	public void hibernateDelete(long id) {
 		Application s = new Application();
 		s.setId(id);
 		getSession().getCurrentSession().delete(s);
 	}
 }
