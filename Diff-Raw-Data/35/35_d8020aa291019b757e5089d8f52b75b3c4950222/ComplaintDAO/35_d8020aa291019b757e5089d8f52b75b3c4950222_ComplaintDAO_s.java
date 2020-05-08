 /**
  * Written by Tien Nguyen <lilylnx@users.sf.net>
  * FREE FOR ALL BUT DOES NOT MEAN THERE IS NO PRICE.
  */
 package mantech.dao;
 
 import java.util.Date;
 import java.util.List;
 
 import org.hibernate.Criteria;
 import org.hibernate.Query;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 
 import mantech.domain.Complaint;
 import mantech.repository.ComplaintRepository;
 
 import net.lilylnx.springnet.core.hibernate.HibernateGenericDAO;
 import net.lilylnx.springnet.util.SpringUtils;
 
 /**
  * 
  * @author Tien Nguyen
  * @version $Id: ComplaintDAO.java,v 1.0 2011/09/07 2:07:59 lilylnx Exp $
  */
 public class ComplaintDAO extends HibernateGenericDAO<Complaint> implements ComplaintRepository {
 
   public ComplaintDAO(SessionFactory sessionFactory) {
     super(sessionFactory);
   }
 
   @SuppressWarnings("unchecked")
   @Override
   public List<Complaint> getByWeekly(Date begin, Date end) {
     return session().createQuery("from Complaint as u where" +
     		" datediff(day, u.createDate, :end) >= 0 and datediff(day, u.createDate, :end) <= 7")
     		.setDate("end", end).list();
   }
 
   @SuppressWarnings("unchecked")
   @Override
   public List<Complaint> getByDepartment(byte id) {
     return session().createCriteria(persistClass).createCriteria("user").createCriteria("department", "depart")
         .add(Restrictions.eq("depart.id", id)).list();
   }
 
   @SuppressWarnings("unchecked")
   @Override
   public List<Complaint> getByPriority(byte id) {
     return session().createCriteria(persistClass)
         .add(Restrictions.eq("priority.id", id)).list();
   }
 
   @SuppressWarnings("unchecked")
   @Override
   public List<Complaint> searchByFName(String name) {
     return session().createCriteria(persistClass).createCriteria("user", "u")
         .add(Restrictions.ilike("u.firstName", "%" + name + "%"))
         .list();
   }
 
   @SuppressWarnings("unchecked")
   @Override
   public List<Complaint> searchStartWithFName(String name) {
     return session().createCriteria(persistClass).createCriteria("user", "u")
         .add(Restrictions.ilike("u.firstName", name + "%"))
         .list();
   }
 
   @SuppressWarnings("unchecked")
   @Override
   public List<Complaint> searchStartWithLName(String name) {
     return session().createCriteria(persistClass).createCriteria("user", "u")
         .add(Restrictions.ilike("u.lastName", name + "%"))
         .list();
   }
 
   @SuppressWarnings("unchecked")
   @Override
   public List<Complaint> sort(String fieldName, boolean order, int[] range) {
     List<Complaint> listComplaint = null;
     
     if (fieldName.contains(".")) {
       String[] entity = fieldName.split("\\.");
       String _order = !order ? "asc" : "desc";
       Query q = session().createQuery("select c from Complaint c left join "
                 + "c." + entity[0].trim() + " e order by e." + entity[1].trim() + " " + _order);
       if (range != null) {
         q.setMaxResults(range[0]).setFirstResult((range[1]-1)*range[0]);
       }
       listComplaint = q.list();
     }
     else {
       Criteria c = session().createCriteria(persistClass);
       c.setMaxResults(range[0]).setFirstResult((range[1]-1)*range[0]);
       if (range != null) {
         c.addOrder(!order ? Order.asc(fieldName) : Order.asc(fieldName));
       }
       listComplaint = c.list();
     }
 
     return listComplaint;
   }
 
   @Override
   public List<Complaint> searchByDate(Date date) {
     return searchByDate(date, date);
   }
   
   @SuppressWarnings("unchecked")
   @Override
   public List<Complaint> searchByDate(Date from, Date to) {
     return session().createQuery("from Complaint c where c.createDate >= :from and c.createDate < :to")
         .setDate("from", from).setDate("to", SpringUtils.increaseDay(to))
         .list();
   }
 
   @SuppressWarnings("unchecked")
   @Override
   public List<Complaint> searchByYear(int year) {
     return session().createQuery("from Complaint c where datepart(year, c.createDate) = :year")
         .setInteger("year", year).list();
   }
 
   @SuppressWarnings("unchecked")
   @Override
  public List<Complaint> getComplaintByAssignment() {
     Query q = session().createQuery("select c from Complaint c inner join c.assignment");
     return q.list();
   }
 
   @Override
   public int insert(Complaint complaint) {
    return session().createSQLQuery("insert into complaint" +
    		"(userid, equipment_id, title, [content], priority_id) " +
    		"values(:userid, :equipment_id, :title, :content, :priority_id)")
     		.setInteger("userid", complaint.getUser().getId())
     		.setInteger("equipment_id", complaint.getEquipment().getId())
     		.setString("title", complaint.getTitle())
     		.setString("content", complaint.getContent())
     		.setInteger("priority_id", complaint.getPriority().getId())
     		.executeUpdate();
   }
 
   @SuppressWarnings("unchecked")
   @Override
  public List<Complaint> getWithoutAssignment() {
     return session().createQuery("select c from Complaint c where c.id not in (select complaintId from Assignment)")
         .list();
   }
 
   @Override
   public boolean isExist(int id) {
     return ((Long)session().createQuery("select count(id) from Complaint where id = :id")
         .setInteger("id", id).uniqueResult()).longValue() > 0;
   }
   
   @Override
   public boolean hasAssignmentId(int id) {    
     return ((Long)session().createQuery("select count(complaintId) from Assignment where complaintId = :id")
         .setInteger("id", id).uniqueResult()).longValue() > 0;
   }
 
 
   
   
 
 }
