 package edu.gatech.oad.rocket.findmythings.server.spi;
 
 import com.google.api.server.spi.config.Api;
 import com.google.api.server.spi.config.ApiMethod;
 import com.google.api.server.spi.response.CollectionResponse;
 import com.googlecode.objectify.Key;
 import com.googlecode.objectify.cmd.Query;
 import edu.gatech.oad.rocket.findmythings.server.db.DatabaseService;
 import edu.gatech.oad.rocket.findmythings.server.db.model.DBMember;
 import edu.gatech.oad.rocket.findmythings.server.model.AppMember;
 import edu.gatech.oad.rocket.findmythings.server.security.ProfileIniRealm;
 import org.apache.shiro.SecurityUtils;
 import org.apache.shiro.mgt.RealmSecurityManager;
 import org.apache.shiro.realm.Realm;
 
 import javax.annotation.Nullable;
 import javax.inject.Named;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 @Api(name = "fmthings", version = "v1")
 public class MemberV1 extends BaseEndpoint {
 
 	@SuppressWarnings("unchecked")
 	@ApiMethod(name = "members.list", path = "members")
 	public CollectionResponse<AppMember> listMembers(
 			@Nullable @Named("email") String email,
 			@Nullable @Named("cursor") String cursorString,
 			@Nullable @Named("limit") Integer limit) {
 		List<AppMember> list = new ArrayList<>();
 		
 		if (email == null) {
 			int bakedInOffset;
 			if (cursorString != null && cursorString.startsWith("FMTBAKEDIN")) {
 				String[] split = cursorString.split("-");
 				bakedInOffset = split.length == 1 ? -1 : Integer.parseInt(split[1]);
 			} else if (cursorString == null) {
 				bakedInOffset = 0;
 			} else {
 				bakedInOffset = -1;
 			}
 
 			int bakedInIncludeCount = 0;
 			if (bakedInOffset != -1) {
 				List<AppMember> bakedInMembers = new ArrayList<>();
 				RealmSecurityManager manager = (RealmSecurityManager)SecurityUtils.getSecurityManager();
 				for (Realm realm : manager.getRealms()) {
 					if (realm instanceof ProfileIniRealm) {
 						bakedInMembers.addAll(((ProfileIniRealm) realm).getMembers());
 					}
 				}
 
 				list.addAll(bakedInMembers.subList(bakedInOffset, bakedInMembers.size()));
 				bakedInIncludeCount = bakedInMembers.size() - bakedInOffset;
 			}
 
 			if (limit != null && bakedInIncludeCount <= limit) {
 				cursorString = bakedInIncludeCount == limit ? "FMTBAKEDIN" : "FMTBAKEDIN-" + bakedInIncludeCount;
 			} else {
 				Query<DBMember> query = DatabaseService.ofy().load().type(DBMember.class).order("dateRegistered-");
 				List<DBMember> queriedMembers = new ArrayList<>();
 				StringBuilder outCursorString = new StringBuilder();
 				pagedQueryArray(query, cursorString, limit, null, queriedMembers, outCursorString);
 				list.addAll(queriedMembers);
 				cursorString = outCursorString.toString();
 			}
 
 			return CollectionResponse.<AppMember>builder().setItems(list).setNextPageToken(cursorString).build();
 		} else {
 			Map<String, Object> filter = new HashMap<>();
			filter.put("submittingUser >=", email);
			filter.put("submittingUser <=", email+"\ufffd");
 			return (CollectionResponse<AppMember>)pagedQuery(DBMember.class, cursorString, limit, filter);
 		}
 
 
 
 	}
 
 	@ApiMethod(name = "members.get", path = "members/get")
 	public AppMember getMember(@Named("email") String email) {
 		return getMemberWithEmail(email);
 	}
 
 	@ApiMethod(name = "members.update", path = "members/update")
 	public AppMember updateMember(AppMember member) {
 		if (member.isEditable()) {
 			Key<DBMember> result = DatabaseService.ofy().save().entity((DBMember)member).now();
 			return DatabaseService.ofy().load().key(result).get();
 		}
 		return null;
 	}
 }
