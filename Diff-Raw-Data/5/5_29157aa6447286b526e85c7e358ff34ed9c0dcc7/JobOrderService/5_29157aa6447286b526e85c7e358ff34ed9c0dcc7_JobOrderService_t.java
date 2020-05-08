 package service;
 
 import models.contracts.JobOrder;
 import models.contracts.SpecificPrestation;
 import models.school.Prestation;
 import models.school.SoeExam;
 import models.user.User;
 import org.joda.time.LocalDate;
 import pdf.JobOrderPdfGenerator;
 import play.libs.MimeTypes;
 import plugin.s3.model.impl.S3RealBlob;
 
 import javax.persistence.NoResultException;
 import javax.persistence.Query;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.List;
 
 /**
  * @author Lukasz Piliszczuk <lukasz.piliszczuk AT zenika.com>
  */
 public class JobOrderService {
 
     public List<JobOrder> getOrdersForUser(User user) {
 
         Query query = JobOrder.em().createQuery("select o from JobOrder as o where o.user = :user");
         query.setParameter("user", user);
 
         return query.getResultList();
     }
 
     public void createOrder(List<Prestation> prestations, List<SoeExam> soeExams, List<SpecificPrestation> specificPrestations, User user) {
 
         JobOrder order = new JobOrder();
         order.creationDate = new LocalDate();
         order.user = user;
         order.contract = user.contract;
         order.soeExams = soeExams;
         order.realCoursesProfessors = prestations;
         order.specificPrestations = specificPrestations;
 
         order.total = 0;
 
         for (Prestation rcp : prestations) {
             order.total += rcp.getTotal();
         }
 
         for (SoeExam soe : soeExams) {
             order.total += soe.getTotal();
         }
 
         for (SpecificPrestation specificPrestation : specificPrestations) {
             order.total += specificPrestation.getTotal();
         }
 
 
         JobOrder last;
 
         try {
             last = (JobOrder) JobOrder.em().createQuery("select jo from JobOrder jo " +
                     "where jo.user = :user " +
                     "order by jo.jobOrderNumber desc")
                     .setParameter("user", user)
                     .setMaxResults(1)
                     .getSingleResult();
         } catch (NoResultException e) {
             last = null;
         }
 
         if (null == last) {
             order.jobOrderNumber = 1;
         } else {
             order.jobOrderNumber = last.jobOrderNumber + 1;
         }
 
         order.save();
 
         try {
             File folder = new File("pdf/orders");
             folder.mkdirs();
 
             File file = File.createTempFile("order", ".pdf");
             new JobOrderPdfGenerator().generate(order, file);
 
             order.pdf = new S3RealBlob();
             order.pdf.set(new FileInputStream(file), MimeTypes.getContentType(file.getName()));
 
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
 
         order.save();
 
         new PrestationService().addJobOrderToPrestations(order, prestations);
         new SpecificPrestationService().addJobOrderToSpecificPrestations(order, specificPrestations);
     }
 
     public JobOrder getByIdAndUser(long id, User user) {
 
         Query query = JobOrder.em().createQuery("select jo from JobOrder jo where jo.user = :user and jo.id = :id");
         query.setParameter("user", user)
                 .setParameter("id", id);
 
         try {
             return (JobOrder) query.getSingleResult();
         } catch (NoResultException e) {
             return null;
         }
 
     }
 
     public void delete(JobOrder jobOrder) {
 
         Query query = JobOrder.em().createQuery("update Prestation p set p.jobOrder = null " +
                 "where p.jobOrder = :jobOrder");
         query.setParameter("jobOrder", jobOrder);
         query.executeUpdate();
 
        query = JobOrder.em().createQuery("update SpecificPrestation p set p.jobOrder = null " +
                "where p.jobOrder = :jobOrder");
        query.setParameter("jobOrder", jobOrder);
        query.executeUpdate();

         jobOrder.delete();
     }
 }
