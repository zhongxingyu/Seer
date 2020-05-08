 package paybar.rest;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Random;
 import java.util.logging.Logger;
 
 import javax.enterprise.context.RequestScoped;
 import javax.inject.Inject;
 import javax.naming.NamingException;
 import javax.persistence.NoResultException;
import javax.ws.rs.GET;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.MediaType;
 
 import paybar.data.DetailAccountResource;
 import paybar.data.PartnerResource;
 import paybar.data.PaybarResourceException;
 import paybar.data.PointOfSaleResource;
 import paybar.data.TransactionResource;
 import paybar.model.Coupon;
 import paybar.model.DetailAccount;
 import paybar.model.PointOfSale;
 import at.ac.uibk.paybar.helpers.RN;
 import at.ac.uibk.paybar.messages.Configuration;
 
 /**
  * This can be used to fill the database with test data after startup.
  * 
  * @author alle ;-)
  * 
  */
 @Path("/setup")
 @RequestScoped
 public class SetupDatabase {
 
 	public static final String VALID_POS_ID = "1060";
 	public static final String VALID_TAN_CODE = "21";
 	public static final double CREDIT = 100000d;
 
 	@Inject
 	Logger log;
 
 	@Inject
 	private PartnerResource pr;
 
 	@Inject
 	private PointOfSaleResource posr;
 
 	@Inject
 	private DetailAccountResource dar;
 
 	@Inject
 	private TransactionResource trr;
 
 	/**
 	 * At least the put works. Should probably exchanged by post with a
 	 * structure like this:
 	 * http://stackoverflow.com/questions/2637017/how-do-i-do
 	 * -a-multipart-form-file-upload-with-jax-rs
 	 * 
 	 * @param posId
 	 * @param tanCode
 	 * @param amount
 	 * @return
 	 * @throws NamingException
 	 */
	@GET
 	@Path("/database")
 	@Produces(MediaType.APPLICATION_JSON)
 	public String setup() {
 		String result = null;
 		boolean success = true;
 
 		// setup our database with new data
 		// generate a list of points of sale for the partner
 		// 10 should be sufficient
 		ArrayList<PointOfSale> pointsOfSale = new ArrayList<PointOfSale>(10);
 
 		PointOfSale pos = new PointOfSale("TIROL", Configuration.BankPosName.toString());
 		posr.createNewPointOfSale(pos);
 		pointsOfSale.add(pos);
 
 		// first create a company
 		pr.createNewpartner("TIROL", "6020", "bankingsystem-HyperBank",
 				Configuration.BankName.toString(), "hello123", pointsOfSale, 0l);
 
 		// setup our database with new data
 		// generate a list of points of sale for the partner
 		// 10 should be sufficient
 		pointsOfSale = new ArrayList<PointOfSale>(10);
 		for (int i = 0; i < Coupon.GENERATE_NUM_OF_CUPONS; i++) {
 			pos = new PointOfSale("TIROL", "FILIALE-" + (i + 1));
 			posr.createNewPointOfSale(pos);
 			pointsOfSale.add(pos);
 		}
 		// first create a company
 		pr.createNewpartner("TIROL", "6020", "bankingdata-KPREIS", "kpreis",
 				"blabla", pointsOfSale, 0l);
 
 		Random r = new Random();
 		Date now = new Date();
 		for (int i = 0; i < 10; i++) {
 			DetailAccount da = new DetailAccount();
 
 			da.setCredit(i * 1000);
 			da.setSecurityKey("user-" + i); // TODO: dummy key for early
 											// development for later
 											// stages
 
 			da.setAdress("Birkenweg " + i);
 			da.setFirstName("Hans der " + RN.roman(i + 1));
 			da.setPassword("hallo123");
 			da.setPhoneNumber("0123456789");
 			da.setSureName(" von Mesopotamien");
 			da.setUserName("user-" + i);
 			da.setActive(true);
 			da.setLocationHash("TIROL");
 			dar.createNewDetailAccount(da);
 			dar.regenerateCoupons(da.getUserName());
 			List<Coupon> coupons = new ArrayList<Coupon>();
 			try {
 				da = dar.getUserByName(da.getUserName(), false);
 				coupons = dar.getCouponListByUserName(da.getUserName());
 			} catch (NoResultException e) {
 				throw new WebApplicationException(e, 500);
 			} catch (Exception e) {
 				throw new WebApplicationException(e, 500);
 			}
 			// Create a bunch of transactions for each user.
 			int n = coupons.size() / 2 + r.nextInt(coupons.size() / 2);
 			for (int j = 0; j < n; j++) {
 				try {
 					String posname = pointsOfSale.get(j).getName();
 					String couponCode = coupons.get(0).getCouponCode();
 					long value = java.lang.Math.abs(r.nextLong()) % 2500;
 					trr.createDebitTransaction(value, couponCode,
 							"Dummy transaction " + j, posname, now);
 					coupons.remove(0);
 				} catch (PaybarResourceException p) {
 					j = n;
 					log.info(p.getMessage());
 				}
 
 			}
 			dar.regenerateCoupons(da.getUserName());
 		}
 
 		// TODO: reload the fastcheck cache after this initial setup or even
 		// better use the available methods for notifying it of the new objects
 
 		if (success) {
 			result = new String("SUCCESS");
 		} else {
 			throw new WebApplicationException(404);
 		}
 		return result;
 	}
 
 }
