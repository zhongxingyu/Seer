 package utils.business;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import constants.ClaimType;
 import models.JAffectedMission;
 import models.JClaim;
 import models.JUser;
 import org.apache.commons.lang3.StringUtils;
 import play.libs.F;
 import utils.FormatterUtils;
 import utils.time.TimeUtils;
 
 import javax.annotation.Nullable;
 import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
 
 import static constants.ClaimType.*;
 
 /**
  * @author f.patin
  */
 public class JClaimUtils {
 
 	private static final String totalKey = "Total";
 
 	private static Map<String, Map<ClaimType, String>> formatData(final Map<String, Map<ClaimType, F.Tuple<String, ArrayList<JClaim>>>> data) {
 
 		final Map<String, Map<ClaimType, String>> result = Maps.newTreeMap();
 		// Format data
 		for(String w : data.keySet()) {
 			if(!result.containsKey(w)) {
 				result.put(w, new EnumMap<ClaimType, String>(ClaimType.class));
 			}
 
 			for(ClaimType ct : data.get(w).keySet()) {
 				final String content = data.get(w).get(ct)._1;
 				if("0".equals(content)) {
 					result.get(w).put(ct, "");
 				} else if(w.equals(totalKey)) {
 					result.get(w).put(ct, FormatterUtils.toCurrency(content));
 				} else {
 					switch(ct) {
 						case JOURNEY:
 							result.get(w).put(ct, FormatterUtils.toKm(ClaimUtils.totalKm(data.get(w).get(ct)._2)));
 							break;
 						case ZONE_FEE:
 							result.get(w).put(ct, Integer.toString(data.get(w).get(ct)._2.size()));
 							break;
 						case FIXED_FEE:
 							result.get(w).put(ct, detailFixedFee(data.get(w).get(ct)._2));
 							break;
 						default:
 							result.get(w).put(ct, FormatterUtils.toCurrency(content));
 							break;
 					}
 				}
 
 			}
 		}
 		return result;
 	}
 
 	private static String detailFixedFee(final List<JClaim> claims) {
 		final List<String> s = Lists.newArrayList();
 		final List<JAffectedMission> ams = JUser.affectedMissions(claims.get(0).userId, TimeUtils.getMondayOfDate(claims.get(0).date), TimeUtils.getSundayOfDate(claims.get(0).date));
 		for(final JAffectedMission am : ams) {
 			final Collection<JClaim> cs = Collections2.filter(claims, new Predicate<JClaim>() {
 				@Override
 				public boolean apply(@Nullable final JClaim claim) {
 					return claim.missionId.equals(am.missionId);
 				}
 			});
 
 			if(!cs.isEmpty()) {
 				s.add(String.format("%s * %s", cs.size(), FormatterUtils.toCurrency(am.feeAmount)));
 			}
 		}
 		if(!s.isEmpty()) {
 			return StringUtils.join(s," / ");
 		}
 		return "";
 	}
 
 	public static Map<Integer, Map<ClaimType, List<JClaim>>> transform(final Integer year, final Integer month, final List<JClaim> claims) {
		final List<Integer> weeks = TimeUtils.getWeeks(year, month);
 		final Map<Integer, Map<ClaimType, List<JClaim>>> result = Maps.newTreeMap();
 		for(Integer weekNb : weeks) {
 			result.put(weekNb, new EnumMap<ClaimType, List<JClaim>>(ClaimType.class));
 			for(ClaimType claimType : values()) {
 				if(!TOTAL.equals(claimType)) {
 					result.get(weekNb).put(claimType, new ArrayList<JClaim>());
 				}
 			}
 		}
 
 		for(JClaim claim : claims) {
 			final Integer weekNb = claim.date.getWeekOfWeekyear();
 			final ClaimType claimType = valueOf(claim.claimType);
 			result.get(weekNb).get(claimType).add(claim);
 		}
 		return result;
 	}
 
 	public static Map<String, Map<ClaimType, String>> synthesis(final Integer year, final Integer month, final List<JClaim> claims) {
 		final Map<Integer, Map<ClaimType, List<JClaim>>> synthesis = transform(year, month, claims);
 		final Map<String, Map<ClaimType, F.Tuple<String, ArrayList<JClaim>>>> result = Maps.newTreeMap();
 		for(Integer week : synthesis.keySet()) {
 			final String w = StringUtils.leftPad(week.toString(), 2, '0');
 			result.put(w, new EnumMap<ClaimType, F.Tuple<String, ArrayList<JClaim>>>(ClaimType.class));
 			for(ClaimType claimType : synthesis.get(week).keySet()) {
 				if(!result.get(w).containsKey(claimType)) {
 					result.get(w).put(claimType, F.Tuple(BigDecimal.ZERO.toPlainString(), new ArrayList<JClaim>()));
 				}
 				for(JClaim claim : synthesis.get(week).get(claimType)) {
 					if(JOURNEY.name().equals(claim.claimType)) {
 						result.get(w)
 							.put(claimType, F.Tuple(new BigDecimal(result.get(w).get(claimType)._1).add(claim.kilometerAmount).toPlainString(), Lists.newArrayList(synthesis.get(week).get(claimType))));
 					} else {
 						result.get(w)
 							.put(claimType, F.Tuple(new BigDecimal(result.get(w).get(claimType)._1).add(claim.amount).toPlainString(), Lists.newArrayList(synthesis.get(week).get(claimType))));
 					}
 				}
 			}
 		}
 
 		// Weeks by total
 		for(String w : result.keySet()) {
 			if(!result.get(w).containsKey(TOTAL)) {
 				result.get(w).put(TOTAL, F.Tuple(BigDecimal.ZERO.toPlainString(), new ArrayList<JClaim>()));
 			}
 			for(ClaimType c : result.get(w).keySet()) {
 				if(!TOTAL.equals(c)) {
 					final BigDecimal bd = new BigDecimal(result.get(w).get(c)._1);
 					final BigDecimal total = new BigDecimal(result.get(w).get(TOTAL)._1);
 					result.get(w).put(TOTAL, F.Tuple(total.add(bd).toPlainString(), new ArrayList<JClaim>()));
 				}
 			}
 		}
 
 		// Total by claimType
 
 		result.put(totalKey, new EnumMap<ClaimType, F.Tuple<String, ArrayList<JClaim>>>(ClaimType.class));
 		for(String w : result.keySet()) {
 			if(!totalKey.equals(w)) {
 				for(ClaimType c : result.get(w).keySet()) {
 					if(!result.get(totalKey).containsKey(c)) {
 						result.get(totalKey).put(c, F.Tuple(BigDecimal.ZERO.toPlainString(), new ArrayList<JClaim>()));
 					}
 					result.get(totalKey).put(c, F.Tuple(new BigDecimal(result.get(w).get(c)._1).add(new BigDecimal(result.get(totalKey).get(c)._1)).toPlainString(), result.get(w).get(c)._2));
 				}
 			}
 		}
 
 
 		return formatData(result);
 	}
 }
