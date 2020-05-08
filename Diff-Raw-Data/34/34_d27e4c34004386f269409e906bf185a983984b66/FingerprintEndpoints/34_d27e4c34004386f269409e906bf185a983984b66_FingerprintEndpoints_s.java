 package ca.idrc.tagin.spi.v1;
 
 import java.util.List;
 
 import javax.inject.Named;
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 
 import ca.idrc.tagin.dao.EMFService;
 import ca.idrc.tagin.model.Fingerprint;
 
 import com.google.api.server.spi.config.Api;
 import com.google.api.server.spi.config.ApiMethod;
 import com.google.api.server.spi.config.ApiMethod.HttpMethod;
 
 @Api(
	name = "tagin-api",
 	version = "v1"
 )
 public class FingerprintEndpoints {
 
 
 	@SuppressWarnings("unchecked")
 	@ApiMethod(
 			name = "fingerprints.list",
 			path = "fingerprints",
 			httpMethod = HttpMethod.GET
 	)
 	public List<Fingerprint> listFingerprints() {
 		EntityManager m = EMFService.createEntityManager();
 		Query query = m.createQuery("select f from Fingerprint f");
 		List<Fingerprint> fingerprints = query.getResultList();
 		m.close();
 		return fingerprints;
 	}
 
 	@ApiMethod(
 			name = "fingerprints.get",
 			path = "fingerprints/{fingerprint_id}",
 			httpMethod = HttpMethod.GET
 	)
 	public Fingerprint getFingerprint(@Named("fingerprint_id") Long id) {
 		EntityManager m = EMFService.createEntityManager();
 		Fingerprint fp = m.find(Fingerprint.class, id);
 		m.close();
 		return fp;
 	}
 	
 }
