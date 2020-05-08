 package uk.ac.cam.signups.forms;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.ws.rs.FormParam;
 import javax.ws.rs.PathParam;
 
 import org.hibernate.Session;
 import org.hibernate.criterion.Restrictions;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.cam.cl.dtg.teaching.api.NotificationApi.NotificationApiWrapper;
 import uk.ac.cam.cl.dtg.teaching.api.NotificationException;
 import uk.ac.cam.cl.dtg.teaching.hibernate.HibernateUtil;
 import uk.ac.cam.signups.models.Event;
 import uk.ac.cam.signups.models.Row;
 import uk.ac.cam.signups.models.Slot;
 import uk.ac.cam.signups.models.Type;
 import uk.ac.cam.signups.models.User;
 import uk.ac.cam.signups.util.Util;
 
 public class FillSlot {
 	@PathParam("obfuscatedId")
 	private String obfuscatedId;
 	@FormParam("slot_crsids[]")
 	private String[] crsids;
 	@FormParam("slot_ids[]")
 	private int[] slotIds;
 	@FormParam("types[]")
 	private int[] typeIds;
 
 	private List<String> errors;
 	private List<Slot> slots;
 	private Set<Integer> ids;
 	private Calendar currentTime;
 	private Event event;
 
 	private Logger logger = LoggerFactory.getLogger(FillSlot.class);
 
 	public void handle(NotificationApiWrapper apiWrapper, User currentUser) {
 		Session session = HibernateUtil.getInstance().getSession();
 
 		int columnsSize = slots.get(0).getRow().getSlots().size();
 
 		Slot slot;
 		Row row;
 		Type type;
 		for (int i = 0; i < slotIds.length; i++) {
 			// Register the user to the slot
 			User owner = User.registerUser(crsids[i]);
 			slot = Util.findById(slots, slotIds[i]);
 			if (slot == null)
 				continue;
 
 			if (owner == null && slot.getOwner() == null)
 				continue;
 
 			if (owner != null && owner.equals(slot.getOwner()))
 				continue;
 
 			// Register notifications
 			registerNotifications(apiWrapper, owner, currentUser, slot);
 
 			// Set it
 			slot.setOwner(owner);
 
 			// Sort out the type of the row if there is only a single type.
 			int typeId;
 			session.update(slot);
 			if ((slot.getRow().getEvent().getTypes().size() != 1)
 					&& ((typeId = typeIds[i / columnsSize]) != 0)) {
 				type = (Type) session
 						.createQuery(
 								"from Type as type where type.id = :type_id AND type.event.obfuscatedId = :obfuscatedId")
 						.setParameter("type_id", typeId)
 						.setParameter("obfuscatedId", obfuscatedId)
 						.uniqueResult();
 				row = slot.getRow();
 				row.setType(type);
 				session.update(row);
 			}
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<String> validate() {
 		Session session = HibernateUtil.getInstance().getSession();
 
 		currentTime = new GregorianCalendar(); // Necessary for checking
 												// datetime related fields.
 
 		event = (Event) session.createCriteria(Event.class)
 				.add(Restrictions.eq("obfuscatedId", obfuscatedId))
 				.uniqueResult();
 
 		ids = new HashSet<Integer>();
 		for (int slotId : slotIds)
 			ids.add(slotId);
 
 		slots = (List<Slot>) session
 				.createQuery(
 						"from Slot as slot where slot.row.event.obfuscatedId = :obfuscatedId "
 								+ "and slot.row.event.expiryDate > :calendar "
 								+ "and (slot.row.calendar > :calendar or slot.row.event.sheetType = 'manual')")
 				.setParameter("obfuscatedId", obfuscatedId)
 				.setParameter("calendar", currentTime).list();
 
 		errors = new ArrayList<String>();
 
 		if (!Util.getIds(slots).equals(ids)) {
 			errors.add("You have tried to perform an unauthorised action.");
 		}
 
 		return errors;
 	}
 
 	// Register notification to the system.
 	private void registerNotifications(NotificationApiWrapper apiWrapper,
 			User owner, User currentUser, Slot slot) {
 		String message;
 		Set<String> relatedCrsids = new HashSet<String>();
 		if (owner == null) {
 			relatedCrsids.add(event.getOwner().getCrsid());
 			relatedCrsids.add(slot.getOwner().getCrsid());
 			relatedCrsids.add(currentUser.getCrsid());
 
 			message = slot.getOwner().getName() + " ("
 					+ slot.getOwner().getCrsid() + ") is deleted from "
 					+ event.getTitle();
 
 			if (currentUser.getCrsid() != slot.getOwner().getCrsid()) {
 				message += " by " + currentUser.getName() + " ("
 						+ currentUser.getCrsid() + ").";
 			} else {
 				message += ".";
 			}
 
 		} else if (slot.getOwner() == null) {
 			relatedCrsids.add(event.getOwner().getCrsid());
 			relatedCrsids.add(owner.getCrsid());
 			relatedCrsids.add(currentUser.getCrsid());
 
 			message = owner.getName() + " (" + owner.getCrsid()
 					+ ") is signed up to " + event.getTitle();
 			if (currentUser.getCrsid() != owner.getCrsid()) {
 				message += " by " + currentUser.getName() + " ("
 						+ currentUser.getCrsid() + ").";
 			} else {
 				message += ".";
 			}
 
 		} else {
 			relatedCrsids.add(event.getOwner().getCrsid());
 			relatedCrsids.add(slot.getOwner().getCrsid());
 			relatedCrsids.add(owner.getCrsid());
 			relatedCrsids.add(currentUser.getCrsid());
 
 			message = owner.getName() + " (" + owner.getCrsid()
 					+ ") has taken " + slot.getOwner().getName()
 					+ "'s place. Exchange is made by " + currentUser.getName()
 					+ " (" + currentUser.getCrsid() + ").";
 		}
 
 		if (message != null) {
 			try {
 				apiWrapper.createNotificationWithForeignId(message, "signapp",
 						"events/" + obfuscatedId,
 						Util.join(relatedCrsids, ","),
 						"signapp-" + event.getId());
 			} catch (NotificationException e) {
 				logger.error("Notification could not be saved.");
 				logger.error(e.getMessage());
 			}
 		}
 	}
 }
