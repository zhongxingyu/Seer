 package at.ac.tuwien.dse.fairsurgeries.domain;
 
 import java.io.Serializable;
 import java.util.Date;
 
 import javax.persistence.Enumerated;
 import javax.persistence.ManyToOne;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.validation.constraints.NotNull;
 
 import org.springframework.format.annotation.DateTimeFormat;
 import org.springframework.roo.addon.javabean.RooJavaBean;
 import org.springframework.roo.addon.json.RooJson;
 import org.springframework.roo.addon.layers.repository.mongo.RooMongoEntity;
 import org.springframework.roo.addon.tostring.RooToString;
 
 @RooJavaBean
 @RooToString
 @RooMongoEntity
 @RooJson(deepSerialize = true)
 public class Reservation implements Serializable {
 	private static final long serialVersionUID = 1L;
 
 	@Enumerated
     private SurgeryType surgeryType;
 
     @ManyToOne
     private Doctor doctor;
 
     @ManyToOne
     private Patient patient;
 
     private Double radius;
 
     @NotNull
     @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "M-")
     private Date dateFrom;
 
     @NotNull
     @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "M-")
     private Date dateTo;
     
     public boolean isValid() {
 		return this.doctor != null && this.patient != null && this.surgeryType != null && 
 			   this.radius > 0. && this.dateFrom != null && this.dateTo != null;
 	}
 }
