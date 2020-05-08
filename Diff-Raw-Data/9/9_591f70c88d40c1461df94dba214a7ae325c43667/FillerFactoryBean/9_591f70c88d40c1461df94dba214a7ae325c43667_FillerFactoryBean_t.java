 package controllerLayer;
 
 import java.io.File;
 import java.io.Serializable;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.ejb.Stateful;
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.PersistenceContextType;
 
 import util.Config;
 import businessLayer.ContractShareTableFiller;
 import businessLayer.Department;
 import daoLayer.FillerDaoBean;
 
 @Stateful
 public abstract class FillerFactoryBean implements Serializable {
 	private static final long serialVersionUID = 1L;
 
 	@Inject protected FillerDaoBean fillerDao;
 
 	@PersistenceContext(unitName = "primary", type = PersistenceContextType.EXTENDED)
 	private EntityManager em;
 	
 	public ContractShareTableFiller getFiller(Department dep) {
 		String depDir = Config.depRatesDefaultDir;
		if(null != dep){
			System.out.println("°°°°°°°°°°°°°°Filler factory: dep not null. Dir = " + dep.getRateDirectory());
		}
		if(null != dep && new File(Config.depRatesPath +  dep.getRateDirectory()).exists()){
 			depDir = dep.getRateDirectory() ;
 		}
 		System.out.println("°°°°°°°°°°°°°°Filler factory: depDir = " + depDir);
 		ContractShareTableFiller currentFiller = createFiller(depDir);
 
 		
 		boolean found = false;
 		List<ContractShareTableFiller> fillers = fillerDao.getAll();
 		Iterator<ContractShareTableFiller> it = fillers.iterator();
 		while(!found && it.hasNext()){
 			ContractShareTableFiller f = it.next();
 			if(currentFiller.equals(f)){
 				System.out.println("°°°°°°°°°°°°°°Filler factory: equals ");
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
