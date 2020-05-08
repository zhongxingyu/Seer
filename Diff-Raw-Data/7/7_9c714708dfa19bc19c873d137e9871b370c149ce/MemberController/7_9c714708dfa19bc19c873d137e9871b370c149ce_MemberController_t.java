 package org.yajug.users.api;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 
 import org.apache.commons.lang.StringUtils;
 import org.yajug.users.domain.Member;
 import org.yajug.users.domain.utils.MemberComparator;
 import org.yajug.users.service.DataException;
 import org.yajug.users.service.MemberService;
 import org.yajug.users.vo.GridVo;
 
 import com.google.gson.ExclusionStrategy;
 import com.google.gson.FieldAttributes;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonObject;
 import com.google.inject.Inject;
 
 @Path("member")
 public class MemberController extends RestController {
 
 	@Inject
 	private MemberService memberService;
 	
 	/** cache of member list */
 	private Map<Long, Member> members;
 	
 	@GET
 	@Path("list")
 	@Produces({MediaType.APPLICATION_JSON})
 	public String list( @QueryParam("callback") String callback,
 						@QueryParam("sortName") String sortName,
 						@QueryParam("sortOrder") String sortOrder,
 						@QueryParam("search") String search,
 						@QueryParam("page") int page,
 						@QueryParam("rows") int rows ) {
 		
 		String response = "";
 		
 		try {
 			List<Member> membersList = null;
 			if(StringUtils.isNotBlank(search)){
 				membersList = memberService.findAll(true, search);
 			} else {
 				membersList = getMembersList(getMembers());
 			}
 			
 			//ordering
 			if(StringUtils.isNotBlank(sortName)){
 				if("desc".equalsIgnoreCase(sortOrder)){
 					Collections.sort(membersList, Collections.reverseOrder(new MemberComparator(sortName)));
 				} else {
 					Collections.sort(membersList, new MemberComparator(sortName));
 				}
 			}
 			
 			//pagination
 			if(rows <= 0){
 				rows = 25;
 			}
 			if(page <= 0){
 				page = 1;
 			}
 			int start = Math.min(0, Math.abs(page * rows));
 			int end = Math.min(membersList.size(), Math.abs(page * rows) + rows);
 			
 			if(start > 0 && end != membersList.size()){
 				membersList.subList(0, start).clear();
 				membersList.subList(end, membersList.size()).clear();
 			}
 			
 			//jsonize
 			response = serializeJsonp(new GridVo(membersList), callback);
 			
 		} catch (DataException e) {
 			e.printStackTrace();
 			response = serializeException(e);
 		} 
 		return response;
 	}
 	
 	@GET
 	@Path("getOne")
 	@Produces({MediaType.APPLICATION_JSON})
 	public String getOne(@QueryParam("id") Long id){
 		String response = "";
 		
 		try {
 			
 			if(id == null || id.longValue() <= 0){
 				throw new DataException("Unable to retrieve member from a wrong id");
 			}
 			
 			//check the cache
 			if(getMembers().containsKey(id)){
 				response = getSerializer().toJson(getMembers().get(id));
 			}
 			
 		} catch (DataException e) {
 			e.printStackTrace();
 			response = serializeException(e);
 		} 
 		return response;
 	}
 	
 	@PUT
 	@Path("add")
 	@Produces({MediaType.APPLICATION_JSON})
 	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
 	public String add(@FormParam("member") String member){
 		return saveMember(member);
 	}
 	
 	@POST
 	@Path("update")
 	@Produces({MediaType.APPLICATION_JSON})
 	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
 	public String update(@FormParam("member") String member){
 		return saveMember(member);
 	}
 	
 	/**
 	 * Save a member
 	 * @param memberData
 	 * @return the json response 
 	 */
 	private String saveMember(String memberData){
 		JsonObject response = new JsonObject();
 		boolean saved = false;
 		
 		Member member = getSerializer().fromJson(memberData, Member.class);
 			
 		try {
 			
 			saved = this.memberService.save(member);
 			
 		} catch (DataException e) {
 			response.addProperty("error", e.getLocalizedMessage());
 		} finally {
 			this.clearMembers();
 		}
 		response.addProperty("saved", saved);
 		
 		return getSerializer().toJson(response);
 	}
 	
 	/**
 	 * use a custom serializer for Members
 	 */
 	@Override
 	protected Gson getSerializer() {
 		return new GsonBuilder()
 				.serializeNulls()
 				.setDateFormat("yyyy-MM-dd")
 				.setExclusionStrategies(new ExclusionStrategy() {
 					//prevent circular references serialization of : Member <-> MemberShip <-> Member
 					@Override public boolean shouldSkipField(FieldAttributes f) {
 						return Member.class.equals(f.getDeclaredClass());
 					}
 					
 					@Override public boolean shouldSkipClass(Class<?> clazz) {
 						return false;
 					}
 				})
 				.create();
 	}
 
 	/**
 	 * get the members (from cache or the service layer)
 	 * @return the map of members, with the member's id as key
 	 * @throws DataException
 	 */
 	private Map<Long, Member> getMembers() throws DataException{
 		if(this.members == null){
 			List<Member> membersList = memberService.getAll(true);
 			
 			//needs of thread safety
 			this.members = new ConcurrentHashMap<Long, Member>(membersList.size());
 			for(Member member : membersList){
 				this.members.put(member.getKey(), member);
 			}
 		}
 		return this.members;
 	}
 	
 	/**
 	 * Clear the current member's cache
 	 */
 	private void clearMembers(){
 		this.members = null;
 	}
 	
 	/**
 	 * convert the member's map to a list
 	 * @param membersMap
 	 * @return
 	 */
 	private static List<Member> getMembersList(Map<Long, Member> membersMap){
 		List<Member> membersList = new ArrayList<Member>();
 		if(membersMap != null){
 			for(Long key : membersMap.keySet()){
 				membersList.add(membersMap.get(key));
 			}
 		}
 		return membersList;
 	}
 	
 	public void setMemberService(MemberService memberService) {
 		this.memberService = memberService;
 	}
 }
