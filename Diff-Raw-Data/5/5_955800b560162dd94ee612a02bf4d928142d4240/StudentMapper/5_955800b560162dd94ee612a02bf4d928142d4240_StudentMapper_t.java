 package com.zhiweiwang.datong.mapper;
 
 import java.util.List;
 import java.util.Map;
 
 import org.apache.ibatis.annotations.Delete;
 import org.apache.ibatis.annotations.Insert;
 import org.apache.ibatis.annotations.Param;
 import org.apache.ibatis.annotations.Select;
 import org.apache.ibatis.annotations.Update;
 
 import com.zhiweiwang.datong.model.User;
 
 public interface StudentMapper {
 
	@Insert("insert into dt_students(id,username,name,pid,sex,policy,birthyear,birthmonth,birthday,healthy,city,address,addcode,phone,cell,gradeschool,gradesection,number,daddy,daddyname,daddyjob,daddyphone,mummy,mummyname,mummyjob,mummyphone,prices,history,reason,yuwem1,yuwem2,shuxue1,shuxue2,yingyu1,yingyu2,wuli1,wuli2,huaxue1,huaxue2,zongfen1,zongfen2,paimin1,paimin2,honors) values(#{id},#{username},#{name},#{pid},#{sex},#{policy},#{birthyear},#{birthmonth},#{birthday},#{healthy},#{city},#{address},#{addcode},#{phone},#{cell},#{gradeschool},#{gradesection},#{number},#{daddy},#{daddyname},#{daddyjob},#{daddyphone},#{mummy},#{mummyname},#{mummyjob},#{mummyphone},#{prices},#{history},#{reason},#{yuwem1},#{yuwem2},#{shuxue1},#{shuxue2},#{yingyu1},#{yingyu2},#{wuli1},#{wuli2},#{huaxue1},#{huaxue2},#{zongfen1},#{zongfen2},#{paimin1},#{paimin2},#{honors})")
 	void insertRow(Map<String, Object> map);
 
	@Update("update dt_students set name=#{name},pid=#{pid},sex=#{sex},policy=#{policy},birthyear=#{birthyear},birthmonth=#{birthmonth},birthday=#{birthday},healthy=#{healthy},city=#{city},address=#{address},addcode=#{addcode},phone=#{phone},cell=#{cell},gradeschool=#{gradeschool},gradesection=#{gradesection},number=#{number},daddy=#{daddy},daddyname=#{daddyname},daddyjob=#{daddyjob},daddyphone=#{daddyphone},mummy=#{mummy},mummyname=#{mummyname},mummyjob=#{mummyjob},mummyphone=#{mummyphone},prices=#{prices},history=#{history},reason=#{reason},yuwem1=#{yuwem1},yuwem2=#{yuwem2},shuxue1=#{shuxue1},shuxue2=#{shuxue2},yingyu1=#{yingyu1},yingyu2=#{yingyu2},wuli1=#{wuli1},wuli2=#{wuli2},huaxue1=#{huaxue1},huaxue2=#{huaxue2},zongfen1=#{zongfen1},zongfen2=#{zongfen2},paimin1=#{paimin1},paimin2=#{paimin2},honors=#{honors} where id=#{id}")
 	void update(Map<String, Object> map);
 
 	@Delete("delete from dt_students where id = #{id}")
 	void deleteStudent(@Param("id") int id);
 
 	@Select("select * FROM dt_students WHERE id = #{id}")
 	Map<?, ?> getStudent(int id);
 	
 	@Select("select * FROM dt_students limit #{start},#{limit}  ")
 	List<Map<?,?>> getStudentsLimit(@Param("start") int start, @Param("limit") int limit);
 }
