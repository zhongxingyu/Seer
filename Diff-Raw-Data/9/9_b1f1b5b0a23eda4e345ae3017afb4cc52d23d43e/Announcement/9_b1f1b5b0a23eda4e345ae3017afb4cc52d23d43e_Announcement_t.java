 package pt.ist.expenditureTrackingSystem.domain.announcements;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import org.joda.time.DateTime;
 
 import pt.ist.expenditureTrackingSystem.domain.ExpenditureTrackingSystem;
 import pt.ist.expenditureTrackingSystem.domain.organization.Supplier;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 
 public abstract class Announcement extends Announcement_Base {
 
     protected Announcement() {
 	super();
 	setExpenditureTrackingSystem(ExpenditureTrackingSystem.getInstance());
 	setCreationDate(new DateTime());
	setOjbConcreteClass(getClass().getName());
     }
 
     public abstract Set<Unit> getBuyingUnits();
 
     public abstract Supplier getSupplier();
 
     public abstract Unit getRequestingUnit();
 
     public static <T extends Announcement> List<T> getAnnouncements(Class<T> clazz) {
 	List<T> announcements = new ArrayList<T>();
 	for (Announcement announcement : ExpenditureTrackingSystem.getInstance().getAnnouncements()) {
 	    if (clazz.isAssignableFrom(announcement.getClass())) {
 		announcements.add((T) announcement);
 	    }
 	}
 	return announcements;
     }
 }
