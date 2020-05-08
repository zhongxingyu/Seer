 package net.thumbtack.updateNotifierBackend.database.mappers;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.ibatis.annotations.Delete;
 import org.apache.ibatis.annotations.Insert;
 import org.apache.ibatis.annotations.Options;
 import org.apache.ibatis.annotations.Param;
 import org.apache.ibatis.annotations.Select;
 import org.apache.ibatis.annotations.Update;
 
 import net.thumbtack.updateNotifierBackend.database.entities.Resource;
 
 public interface ResourceMapper {
 
	String ADD = "INSERT INTO resources (user_id, url, name, schedule_code, filter, hash) VALUES (#{userId}, #{url}, #{name}, #{scheduleCode}, #{filter}, #{hash})";
 	String DEL = "DELETE FROM resources WHERE id=#{id} AND user_id=#{userId}";
 	String GET_ALL_FOR_USER = "SELECT * FROM resources WHERE user_id=#{id}";
 	String GET_BY_IDS = "SELECT * FROM resources WHERE id=#{id} AND user_id=#{userId}";
 	String GET_BY_USER_ID_AND_TAGS = "SELECT DISTINCT * FROM resources WHERE NOT EXISTS (SELECT id FROM tags WHERE tags.id IN (${tagIds}) AND NOT EXISTS (SELECT * FROM resource_tag WHERE resource_id=resources.id AND tag_id = tags.id))";
 	String GET_BY_SCHEDULE_CODE = "SELECT * FROM resources WHERE schedule_code=#{scheduleCode}";
 	String DEL_ALL_USER_RESOURCES = "DELETE FROM resources WHERE id=#{id}";
 	String DEL_BY_TAGS = "DELETE FROM resources WHERE user_id=#{userId} AND NOT EXISTS (SELECT id FROM tags WHERE tags.id IN (${tagIds}) AND NOT EXISTS (SELECT * FROM resource_tag WHERE resource_id=resources.id AND tag_id = tags.id))";
 	String UPD = "UPDATE resources SET user_id=#{userId}, url=#{url}, name=#{name}, filter=#{filter}, schedule_code=#{scheduleCode} WHERE id=#{id}";
 	String UPD_AFTER_UPD = "UPDATE resources SET hash=#{hash}, last_update=#{timestamp} WHERE id=#{id}";
 	String LAST_ID = "SELECT LAST_INSERT_ID()";
 	String DEL_ALL_RESOURCES = "DELETE FROM resources";
 	String CHK = "UPDATE resources SET id=id WHERE id=#{id}";
 	String GET_AFTER_UPD = "SELECT id FROM resources WHERE user_id=#{userId} AND last_update >= #{date}";
 
 	@Insert(ADD)
 	@Options(useGeneratedKeys = true)
 	// TODO check that after addition resource have not-null id
 	int add(Resource resource);
 
 	@Delete(DEL)
 	int delete(Resource resource);
 
 	@Select(GET_ALL_FOR_USER)
 	List<Resource> getAllForUser(Long id);
 
 	@Select(GET_BY_USER_ID_AND_TAGS)
 	List<Resource> getByUserIdAndTags(@Param(value = "tagIds") String tagIds);
 
 	@Select(GET_BY_SCHEDULE_CODE)
 	Set<Resource> getByscheduleCode(byte scheduleCode);
 
 	@Delete(DEL_ALL_USER_RESOURCES)
 	int deleteAll(long id);
 
 	@Delete(DEL_BY_TAGS)
 	int deleteByTags(@Param(value = "userId") long userId,
 			@Param(value = "tagIds") String tagIds);
 
 	@Update(UPD)
 	int update(Resource resource);
 
 	@Update(UPD_AFTER_UPD)
 	int updateAfterUpdate(@Param(value = "id") Long id,
 			@Param(value = "hash") Integer hash,
 			@Param(value = "timestamp") Date timestamp);
 
 	@Select(LAST_ID)
 	Long getLastId();
 
 	@Select(GET_BY_IDS)
 	Resource get(@Param(value = "userId") Long userId,
 			@Param(value = "id") Long id);
 
 	@Update(CHK)
 	boolean check(Resource resource);
 
 	@Select(GET_AFTER_UPD)
 	List<Long> getUpdated(@Param(value = "userId") Long userId,
 			@Param(value = "date") Date date);
 
 }
