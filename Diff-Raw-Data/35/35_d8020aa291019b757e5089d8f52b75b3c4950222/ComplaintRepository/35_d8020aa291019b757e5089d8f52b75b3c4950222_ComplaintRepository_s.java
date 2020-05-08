 /**
  * Written by Tien Nguyen <lilylnx@users.sf.net>
  * FREE FOR ALL BUT DOES NOT MEAN THERE IS NO PRICE.
  */
 package mantech.repository;
 
 import java.util.Date;
 import java.util.List;
 
 import net.lilylnx.springnet.repository.Repository;
 
 import mantech.domain.Complaint;
 
 /**
  * 
  * @author Tien Nguyen
  * @version $Id: ComplaintRepository.java,v 1.0 2011/09/07 1:57:39 lilylnx Exp $
  */
 public interface ComplaintRepository extends Repository<Complaint> {
   
   List<Complaint> sort(String fieldName, boolean order, int[] range);
   List<Complaint> getByWeekly(Date begin, Date end);
   List<Complaint> getByDepartment(byte id);
   List<Complaint> getByPriority(byte id);
  List<Complaint> getComplaintByAssignment();
  List<Complaint> getWithoutAssignment();
   List<Complaint> searchByFName(String name);
   List<Complaint> searchStartWithFName(String name);
   List<Complaint> searchStartWithLName(String name);
   List<Complaint> searchByDate(Date date);
   List<Complaint> searchByDate(Date from, Date to);
   List<Complaint> searchByYear(int year);
   boolean isExist(int id);
   boolean hasAssignmentId(int id);
   int insert(Complaint complaint);
   
 }
