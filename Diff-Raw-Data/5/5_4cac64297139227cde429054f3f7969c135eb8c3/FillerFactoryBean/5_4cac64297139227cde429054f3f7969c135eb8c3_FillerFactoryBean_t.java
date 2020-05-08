 package controllerLayer;
 
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.enterprise.context.SessionScoped;
 import javax.inject.Inject;
 
 import businessLayer.AgreementShareTableFiller;
 import businessLayer.Department;
 import daoLayer.FillerDaoBean;
 
 @SessionScoped
 public abstract class FillerFactoryBean implements Serializable {
 	private static final long serialVersionUID = 1L;
 
 	@Inject protected FillerDaoBean fillerDao;
 
 	protected Map<Department, AgreementShareTableFiller> cache = new HashMap<>();
 
 	public AgreementShareTableFiller getFiller(Department dep) {
 		if (!cache.containsKey(dep)) {
 			cache.put(dep, findFiller(dep));
 		}
 		return cache.get(dep);
 	}
 
 	protected AgreementShareTableFiller findFiller(Department dep) {
 		List<AgreementShareTableFiller> fillers = fillerDao.getAll();
 		AgreementShareTableFiller currentFiller = createFiller(dep.getRateDirectory());
 		
 		boolean found = false;
 		Iterator<AgreementShareTableFiller> it = fillers.iterator();
 		while(!found && it.hasNext()){
			AgreementShareTableFiller f = it.next();
			if(currentFiller.equals(f)){
 				found = true;
				currentFiller = f;
 			}
 		}
 		if(!found){
 			fillerDao.create(currentFiller);
 		}
 		
 		return currentFiller;
 	}
 	
 	protected abstract AgreementShareTableFiller createFiller(String depDirectory);
 }
