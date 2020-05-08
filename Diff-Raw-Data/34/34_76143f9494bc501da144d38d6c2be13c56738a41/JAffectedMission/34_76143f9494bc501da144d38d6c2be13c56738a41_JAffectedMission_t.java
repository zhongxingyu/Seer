 package models;
 
 import com.github.jmkgreen.morphia.annotations.Embedded;
 import com.github.jmkgreen.morphia.annotations.PostLoad;
 import com.github.jmkgreen.morphia.annotations.PrePersist;
 import com.github.jmkgreen.morphia.annotations.Transient;
 import com.google.common.base.Function;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Lists;
 import constants.MissionAllowanceType;
 import org.bson.types.ObjectId;
 import org.joda.time.DateTime;
 
 import javax.annotation.Nullable;
 import java.math.BigDecimal;
 import java.util.Date;
 import java.util.List;
 
 /**
  * @author f.patin
  */
 @Embedded
 public class JAffectedMission {
 
 	@Transient
 	public DateTime startDate;
 	@Transient
 	public DateTime endDate;
 	public ObjectId missionId;
 	public String allowanceType;
 	@Transient
 	public BigDecimal feeAmount;
 	private String _feeAmount;
 	private Date _startDate;
 	private Date _endDate;
 
 	public JAffectedMission() {
 	}
 
 	public JAffectedMission(final JMission mission) {
 		this.missionId = mission.id;
 	}
 
 	@SuppressWarnings({"unused"})
 	@PrePersist
 	private void prePersist() {
 		if(startDate != null) {
 			_startDate = startDate.toDate();
 		}
 		if(endDate != null) {
 			_endDate = endDate.toDate();
 		}
 		if(feeAmount != null) {
 			_feeAmount = feeAmount.toPlainString();
 		}
 	}
 
 	@SuppressWarnings({"unused"})
 	@PostLoad
 	private void postLoad() {
 		if(_startDate != null) {
 			startDate = new DateTime(_startDate.getTime());
 		}
 		if(_endDate != null) {
 			endDate = new DateTime(_endDate.getTime());
 		}
 		if(_feeAmount != null) {
 			feeAmount = new BigDecimal(_feeAmount);
 		}
 	}
 
 	public static List<JAffectedMission> genesis() {
 		return Lists.newArrayList(Collections2.transform(JMission.genesisMission(), new Function<JMission, JAffectedMission>() {
 			@Nullable
 			@Override
 			public JAffectedMission apply(@Nullable final JMission mission) {
 				return new JAffectedMission(mission);
 			}
 		}));
 	}
 }
