 package ca.idrc.tagin.spi.v1;
 
 import java.util.List;
 
 import javax.inject.Named;
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 
 import ca.idrc.tagin.dao.EMFService;
 import ca.idrc.tagin.model.Pattern;
 
 import com.google.api.server.spi.config.Api;
 import com.google.api.server.spi.config.ApiMethod;
 import com.google.api.server.spi.config.ApiMethod.HttpMethod;
 
 @Api(
	name = "tagin-api",
 	version = "v1"
 )
 public class PatternEndpoints {
 
 	@ApiMethod(
 			name = "patterns.add",
 			path = "patterns",
 			httpMethod = HttpMethod.POST
 	)
 	public Pattern addPattern(Pattern pattern) {
 		EntityManager m = EMFService.createEntityManager();
 		m.persist(pattern);
 		m.close();
 		return pattern;
 	}
 
 	@SuppressWarnings("unchecked")
 	@ApiMethod(
 			name = "patterns.list",
 			path = "patterns",
 			httpMethod = HttpMethod.GET
 	)
 	public List<Pattern> listPatterns() {
 		EntityManager m = EMFService.createEntityManager();
 		Query query = m.createQuery("select p from Pattern p");
 		List<Pattern> patterns = query.getResultList();
		for (Pattern p : patterns) 
 			p.getBeacons(); // Forces eager-loading
 		m.close();
 		return patterns;
 	}
 	
 	@ApiMethod(
 			name = "patterns.get",
 			path = "patterns/{pattern_id}",
 			httpMethod = HttpMethod.GET
 	)
 	public Pattern getPattern(@Named("pattern_id") Long id) {
 		EntityManager m = EMFService.createEntityManager();
 		Pattern p = m.find(Pattern.class, id);
 		if (p != null) 
 			p.getBeacons(); // Forces eager-loading
 		m.close();
 		return p;
 	}
 	
 	@ApiMethod(
 			name = "patterns.remove",
 			path = "patterns/{pattern_id}",
 			httpMethod = HttpMethod.DELETE
 	)
 	public void removePattern(@Named("pattern_id") Long id) {
 		EntityManager m = EMFService.createEntityManager();
 		Pattern p = m.find(Pattern.class, id);
 		m.remove(p);
 		m.close();
 	}
 	
 }
