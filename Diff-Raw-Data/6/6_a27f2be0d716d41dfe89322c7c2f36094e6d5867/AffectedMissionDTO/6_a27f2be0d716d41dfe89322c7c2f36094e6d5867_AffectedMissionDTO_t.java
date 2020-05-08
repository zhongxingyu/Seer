 package dto;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Lists;
 import constants.MissionAllowanceType;
 import models.JAffectedMission;
 import models.JMission;
 import models.JParameter;
 import org.joda.time.DateTime;
 
 import javax.annotation.Nullable;
 import java.math.BigDecimal;
 import java.util.List;
 
 /**
  * @author f.patin
  */
 public class AffectedMissionDTO {
 
     public Long startDate;
     public Long endDate;
     public MissionDTO mission;
     public String allowanceType;
     public BigDecimal feeAmount;
 
     @SuppressWarnings({"unused"})
     public AffectedMissionDTO() {
     }
 
 
     public AffectedMissionDTO(final JAffectedMission affectedMission) {
         this.startDate = affectedMission.startDate.getMillis();
         if (affectedMission.endDate != null) {
             this.endDate = affectedMission.endDate.getMillis();
         }
         this.mission = MissionDTO.of(JMission.fetch(affectedMission.missionId));
         this.allowanceType = affectedMission.allowanceType;
        this.feeAmount = affectedMission.feeAmount;
 
     }
 
     public static List<AffectedMissionDTO> of(final List<JAffectedMission> affectedMissions) {
         return Lists.newArrayList(Collections2.transform(affectedMissions, new Function<JAffectedMission, AffectedMissionDTO>() {
             @Nullable
             @Override
             public AffectedMissionDTO apply(@Nullable JAffectedMission affectedMission) {
                 return new AffectedMissionDTO(affectedMission);
             }
         }));
     }
 }
