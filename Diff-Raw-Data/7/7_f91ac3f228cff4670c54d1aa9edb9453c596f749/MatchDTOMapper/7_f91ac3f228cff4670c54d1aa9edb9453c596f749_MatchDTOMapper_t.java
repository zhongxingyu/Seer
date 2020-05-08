 package de.groupon.hcktn.groupong.domain.mappers;
 
 import de.groupon.hcktn.groupong.domain.response.MatchDTO;
 import de.groupon.hcktn.groupong.model.entity.Match;
 import de.groupon.hcktn.groupong.service.StatusService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
import java.util.Calendar;

 @Component
 public class MatchDTOMapper extends BaseMapper {
 
     @Autowired
     private StatusService statusService;
 
     public MatchDTO mapToMatchDTO(final Match match)  {
         final MatchDTO matchDTO = new MatchDTO();
 
         matchDTO.setMatchId(match.getId());
         matchDTO.setUser1Id(match.getUser1Id());
         matchDTO.setUser2Id(match.getUser2Id());
         matchDTO.setScoreUser1(match.getScoreUser1());
         matchDTO.setScoreUser2(match.getScoreUser2());
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(match.getMatchDate());
        matchDTO.setDate(formatTime(c));
         if (match.getStatusId() != null ) {
             matchDTO.setStatus(statusService.fetchStatus(match.getStatusId()).getDescription());
         }
 
         return matchDTO;
     }
 }
