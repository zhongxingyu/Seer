 package nl.waisda.repositories;
 
 import java.util.Collections;
 import java.util.List;
 
 import javax.persistence.TypedQuery;
 
 import nl.waisda.domain.MissionVideo;
 import nl.waisda.domain.Video;
 
 import org.apache.log4j.Logger;
 import org.springframework.stereotype.Repository;
 
 
 @Repository
 public class MissionVideoRepository extends AbstractRepository<MissionVideo> {
 
 	private Logger log = Logger.getLogger(MissionVideoRepository.class);
 
 	public MissionVideoRepository() {
 		super(MissionVideo.class);
 	}
 
 	public List<Video> getVideosForMission(int missionId) {
 		TypedQuery<Video> query = getEntityManager().createQuery(
 				"SELECT v FROM MissionVideo mv " +
 				"LEFT JOIN mv.video v " +
 				"WHERE mv.mission.id = :missionId",
 				Video.class);
 		query.setParameter("missionId", missionId);
 		List<Video> videos = query.getResultList();
 		Collections.sort(videos);
		return videos;
 	}
 }
