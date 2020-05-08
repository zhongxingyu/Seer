 package ee.itcollege.borderproject.setup;
 
 import javax.annotation.Resource;
 
import org.springframework.stereotype.Component;

 import ee.itcollege.borderproject.dao.BorderStationDao;
 import ee.itcollege.borderproject.dao.GuardDao;
 import ee.itcollege.borderproject.dao.GuardInBorderStationDao;
 import ee.itcollege.borderproject.model.BorderStation;
 import ee.itcollege.borderproject.model.Guard;
 import ee.itcollege.borderproject.model.GuardInBorderStation;
 
@Component
 public class GuardInBorderStationDataInserter {
 	
 	public static final String SYSTEM_USER = "system";
 	
 	@Resource
 	GuardDao guardDao;
 	
 	@Resource
 	BorderStationDao borderStationDao;
 	
 	@Resource
 	GuardInBorderStationDao guardInBorderStationDao;
 
 	public void insertGuardsInBorderStation() {		
 		if (!hasTestDataBeenInserted()) {
 			Guard guard = getFirstGuard();
 			BorderStation borderStation = getFirstBorderStation();
 		}
 	}
 	
 	private Guard getFirstGuard() {
 		return guardDao.getAll().get(0);
 	}
 	
 	private BorderStation getFirstBorderStation() {
 		return borderStationDao.getAll().get(0);
 	}
 	
 	private boolean hasTestDataBeenInserted() {
 		for (GuardInBorderStation guardInBorderStation : guardInBorderStationDao.getAll()) {
 			if (SYSTEM_USER.equals(guardInBorderStation.getCreator()))
 				return true;
 		}
 		
 		return false;
 	}
 	
 }
