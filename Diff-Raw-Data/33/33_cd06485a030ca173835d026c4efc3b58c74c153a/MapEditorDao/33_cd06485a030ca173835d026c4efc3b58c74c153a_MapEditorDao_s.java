 package dao;
 
 import java.util.Calendar;
 import java.util.List;
 
 import javax.persistence.TypedQuery;
 
import model.entity.FleetEditorModel;
 import model.entity.MapEditorModel;
 
 public class MapEditorDao extends BaseDao<MapEditorModel> {
 
 	private static MapEditorDao instance;
 
 	private MapEditorDao() { }
 
 	public static MapEditorDao getInstance() {
 		if (instance == null) {
 			instance = new MapEditorDao();
 		}
 		return instance;
 	}
 
 	public MapEditorModel saveMap(MapEditorModel mapModel) {
 
 		mapModel.setSaveDate(Calendar.getInstance());
 
 		em.getTransaction().begin();
 		em.persist(mapModel);
 		em.getTransaction().commit();
 
 		return mapModel;
 	}
 	/**
 	 * Find all Maps
 	 * @return
 	 */
 	public List<MapEditorModel> getMaps() {
 
 
 		TypedQuery<MapEditorModel> query = em.createQuery("select m from MapEditorModel m", MapEditorModel.class);
 		List<MapEditorModel> resultList = query.getResultList();
 
 		return resultList;
 	}
 
 	@Override
 	public MapEditorModel getModelById(long id) {
 
 		MapEditorModel map = em.find(MapEditorModel.class, id);
 
 		return map;
 	}
 
 	@Override
 	public void deleteModelById(long id) {
 
 		MapEditorModel map = em.find(MapEditorModel.class, id);
 		em.getTransaction().begin();
 		em.remove(map);
 		em.getTransaction().commit();
 
 	
 	}
 	
 
 }
