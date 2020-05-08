 package com.github.kolorobot.icm.incident;
 
 import java.util.List;
 
 import org.springframework.data.jpa.repository.JpaRepository;
 import org.springframework.data.jpa.repository.Query;
 
 public interface IncidentRepository extends JpaRepository<Incident, Long> {
 
 	@Query("SELECT i FROM Incident i WHERE i.operatorId = ?1 ORDER BY i.status ASC")
 	List<Incident> findAllByOperatorId(String operatorId);
 	
 	@Query("SELECT i FROM Incident i WHERE i.id = ?1 AND i.operatorId = ?2")
 	Incident findOne(Long id, String operatorId);
 	
 	@Query("SELECT i FROM Incident i WHERE i.id = ?1 AND i.creator.id = ?2 AND i.operatorId = ?3")
 	Incident findOneByIdAndCreatorId(Long id, Long accountId, String operatorId);
 
 	@Query("SELECT i FROM Incident i WHERE i.id = ?1 AND i.assignee.id = ?2 AND i.operatorId = ?3")
 	Incident findOneByIdAndAssigneeId(Long id, Long accountId, String operatorId);
 
 }
