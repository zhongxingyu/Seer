 package se.davvs.floorballfighters.jparepositories;
 
 import org.springframework.data.jpa.repository.JpaRepository;
 
 import se.davvs.floorballfighters.models.DayPlayer;
import se.davvs.floorballfighters.models.GameTeamMember;
 
 public interface DayPlayerRepository extends JpaRepository<DayPlayer, Integer> {
 }
