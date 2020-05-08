 package controllerLayer;
 
 import java.io.Serializable;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.enterprise.context.SessionScoped;
 import javax.inject.Inject;
 
 import businessLayer.ContractShareTableFiller;
 import businessLayer.Department;
 import daoLayer.FillerDaoBean;
 
 @SessionScoped
 public abstract class FillerFactoryBean implements Serializable {
 	private static final long serialVersionUID = 1L;
 
 	@Inject protected FillerDaoBean fillerDao;
 
 	public ContractShareTableFiller getFiller(Department dep) {
 		List<ContractShareTableFiller> fillers = fillerDao.getAll();
		ContractShareTableFiller currentFiller = createFiller(dep.getRateDirectory());
 		
 		boolean found = false;
 		Iterator<ContractShareTableFiller> it = fillers.iterator();
 		while(!found && it.hasNext()){
 			ContractShareTableFiller f = it.next();
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
 	
 	protected abstract ContractShareTableFiller createFiller(String depDirectory);
 }
