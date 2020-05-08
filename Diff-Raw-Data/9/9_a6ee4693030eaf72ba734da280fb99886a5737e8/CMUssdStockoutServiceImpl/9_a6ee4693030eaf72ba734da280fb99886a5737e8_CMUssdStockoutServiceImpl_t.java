 package za.org.opengov.ussd.service.stockout.cm;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import za.org.opengov.stockout.entity.Facility;
 import za.org.opengov.stockout.entity.Stockout;
 import za.org.opengov.stockout.entity.StockoutReport;
 import za.org.opengov.stockout.entity.Subject;
 import za.org.opengov.stockout.entity.medical.Product;
 import za.org.opengov.stockout.service.FacilityService;
 import za.org.opengov.stockout.service.StockoutReportService;
 import za.org.opengov.stockout.service.StockoutService;
 import za.org.opengov.stockout.service.medical.ProductService;
 import za.org.opengov.ussd.controller.cm.CMUssdRequest;
 import za.org.opengov.ussd.controller.cm.CMUssdResponse;
 import za.org.opengov.ussd.service.stockout.UssdStockoutDao;
 import za.org.opengov.ussd.util.KeyValueStore;
 
 /**
  * Take note: When a REST call is made, the request will be delegated to the
  * service which matches the name as specified below.
  */
 @Service("cm.stockout")
 public class CMUssdStockoutServiceImpl implements CMUssdStockoutService {
 
 	@Autowired
 	private KeyValueStore keyValueStore;
 
 	@Autowired
 	private UssdStockoutDao stockoutDao;
 
 	@Autowired
 	private StockoutReportService stockoutReportService;
 
 	@Autowired
 	private FacilityService facilityService;
 
 	@Autowired
 	private ProductService productService;
 
 	@Autowired
 	private StockoutService stockoutService;
 
 	@Override
 	public CMUssdResponse createUssdResponse(CMUssdRequest request) {
 
 		CMUssdResponse response = new CMUssdResponse();
 		String displayText = "";
 		String sessionId = "";
 		int menuRequest = 0;
 
 		try {
 			// get the stage at which the user is in,in the ussd session
 			menuRequest = Integer.parseInt(request.getRequestid());
 			// session id identifies session specific data stored in the key
 			// value store
 			sessionId = request.getUssdSessionId();
 
 			switch (menuRequest) {
 
 			case 0: // welcome message..clinic code prompt
 
 				displayText = stockoutDao.getMenu(0);
 				++menuRequest;
 
 				break;
 
 			case 1: // validate clinic and display ussd stock out services
 
 				displayText = request.getRequest();
 				// stockoutDao.checkClinic(request.getRequest());****database
 				// call
 
 				// -----------------------------------------------------------------------------
 				// String clinicName = displayText;
 				// matches the best matching facility
 				Facility facility = facilityService
 						.getClosestMatch(displayText);
 				// -----------------------------------------------------------------------------
 
 				if (facility != null) {
 					// Need to set clinic name so that it can be re-used later
 				keyValueStore.put(
 						"facilityName." + request.getUssdSessionId(),
 						facility);
 				displayText = facility.getLocalName() + " " + facility.getFacilityType().getReadable() + " "
 						+ stockoutDao.getMenu(1);
 				++menuRequest;
 
 				} else {
 					// displayed failed message and redisplay same menu
 					displayText += " " + stockoutDao.getMenu(92);
 					throw new NumberFormatException();
 				}
 
 				break;
 
 			case 2: // process service request,get list of recent medicines
 
 				displayText = stockoutDao.getMenu(91); // set error message for
 														// incorrect string
 														// input and invalid
 														// integer selection
 
 				int requestSelection = Integer.parseInt(request.getRequest());
 
 				if (requestSelection >= 1 && requestSelection <= 3) {
 
 					// -----------------------------------------------------------------------------
 					int limit = 5;
 					// must be facility code, not facility name
 					String facilityCode = ((Facility) (keyValueStore
 							.get("facilityName." + sessionId))).getUid();
 					// facilityCode.
 					List<Stockout> stockouts = stockoutService
 							.getMostCommonlyReportedStockoutsForFacility(
 									facilityCode, limit);
 					if (stockouts.size() > 0) {
 						displayText = stockoutDao.getMenu(21);
 						keyValueStore.put("commonStockouts." + sessionId,
 								stockouts);
 
 						for (int index = 0; index < stockouts.size(); index++) {
 
 							displayText += (index + 1)
 									+ "."
 									+ stockouts.get(index).getProduct()
 											.getName() + " " + stockouts.get(index).getProduct()
 											.getDescription() + "\n";
 						}
 						// or
 
 						// this returns most recent reports, though I could also
 						// just return most recent actual stockouts
 						// since multiple recent reports could be for same
 						// stockouts
 						// List<StockoutReport> recentReports =
 						// stockoutReportService.getRecentlyReportedStockouts(limit);
 
 						// -----------------------------------------------------------------------------
 
 						displayText += stockoutDao.getMenu(22);
 						++menuRequest;
 					} else {
 						displayText = stockoutDao.getMenu(3);
 						menuRequest += 2;
 
 					}
 					keyValueStore.put("service." + sessionId,
 							Integer.toString(requestSelection));
 
 				} else {
 
 					// number greater 3 or less than 1 was chosen
 					throw new NumberFormatException();
 				}
 				break;
 
 			case 3: // process user selection of recent reports or manual
 					// medicine name entry
 
 				displayText = stockoutDao.getMenu(91);
 
 				int requestMedicine = Integer.parseInt(request.getRequest());
 
 				List<Stockout> commonStockouts = (List<Stockout>) keyValueStore
 						.get("commonStockouts." + sessionId);
 
 				if (requestMedicine >= 1 && requestMedicine <= 8
 						&& commonStockouts.size() >= requestMedicine) { // process
 					// medicine
 					// selection
 					// 1-8
 					Product selectedProduct = commonStockouts.get(
 							requestMedicine - 1).getProduct();
 					if (selectedProduct != null) {
 
 						displayText = selectedProduct.getName() + " " + selectedProduct.getDescription();
						
						//put selected product in keyvalue store
 						keyValueStore.put("productName." + sessionId,
 								selectedProduct);
						 //get dosages for selected product
						List<Product> productDosages = productService
								.getAllProductsMatchingName(selectedProduct);
						//put dosages for selected product in key value store
						keyValueStore.put("dosages." + sessionId, productDosages);
						
 						displayText += " " + stockoutDao.getMenu(4);
 						menuRequest += 3;
 					} else {
 						throw new NumberFormatException();
 					}
 
 				} else if (requestMedicine == 9) { // display enter medicine
 													// name prompt
 
 					displayText = stockoutDao.getMenu(3);
 					++menuRequest;
 
 				} else {// user enters a number less than 1 or greater than 8
 
 					throw new NumberFormatException();
 
 				}
 				break;
 
 			case 4: // validate/find nearest match to medicine name+display
 					// appropriate menu as above
 
 				// -----------------------------------------------------------------------------
 				displayText = request.getRequest();
 
 				Product searchProduct = productService
 						.getClosestMatch(displayText);
 
 				displayText = stockoutDao.getMenu(10) + " "
 						+ searchProduct.getName() + "\n";
 
 				List<Product> productsForName = productService
 						.getAllProductsMatchingName(searchProduct);
 
 				for (int k = 0; k < productsForName.size(); k++) {
 
 					displayText += (k + 1)
 							+ "."
 							+ productsForName.get(k).getDescription() + "\n";
 
 				}
 				
 				displayText += stockoutDao.getMenu(11);
 
 				// -----------------------------------------------------------------------------
 
 				if ((searchProduct != null)) { // medicine name found, go
 												// to next menu
 					keyValueStore.put("dosages." + sessionId, productsForName);
 					++menuRequest;
 
 				} else { // medicine name not found
 					displayText += " " + stockoutDao.getMenu(92);
 					throw new NumberFormatException();
 				}
 
 				break;
 
 			case 5:
 
 				displayText = stockoutDao.getMenu(91);
 
 				int requestDosage = Integer.parseInt(request.getRequest());
 
 				List<Product> productDosages = (List<Product>) keyValueStore
 						.get("dosages." + sessionId);
 
 				if (requestDosage >= 1
 						&& requestDosage <= 8 && requestDosage <= productDosages.size()) {
 
 					Product selectedProduct = productDosages.get(requestDosage-1);
 					displayText = selectedProduct.getName() + " "
 							+ selectedProduct.getDescription() + " "
 							+ stockoutDao.getMenu(4);
 
 					keyValueStore.put("productName." + sessionId,
 							selectedProduct);
 					menuRequest++;
 				} 
 				else if (requestDosage==9){
 					menuRequest = 4;
 					displayText = stockoutDao.getMenu(3);
 				}
 				else {
 					throw new NumberFormatException();
 				}
 
 				break;
 
 			case 6: // run methods for each of the different services+display
 					// result.
 
 				String serviceRequest = (String) keyValueStore.get("service."
 						+ sessionId);
 				Product selectedProduct = (Product) keyValueStore
 						.get("productName." + sessionId);
 				Facility selectedFacility = (Facility) keyValueStore
 						.get("facilityName." + sessionId);
 
 				int service = Integer.parseInt(serviceRequest);
 				int requestOption = Integer.parseInt(request.getRequest());
 
 				if (requestOption == 1) {
 					switch (service) {
 					case 1:
 						// StockoutDao.reportStockout(medicineName,facilityName);
 
 						// -----------------------------------------------------------------------------
 						// must be the correct facility and product code
 						String productCode = selectedProduct.getUid();
 						String facilityCode = selectedFacility.getUid();
 						Subject subject = new Subject();
 						subject.setContactNumber(request.getMsisdn());
 						stockoutReportService.submitStockoutReport(productCode,
 								facilityCode, subject, "stockout of product",
 								false);
 						// -----------------------------------------------------------------------------
 
 						displayText = selectedProduct.getName() + " in "
 								+ selectedFacility.getLocalName() + " " + selectedFacility.getFacilityType().getReadable() + " "
 								+ stockoutDao.getMenu(5);
 						break;
 					case 2:
 						// displayText =
 						// StockoutDao.getStatus(MedicineName,facilityName);
 
 						// -----------------------------------------------------------------------------
 						// against must be proper facility code and product code
 						// (no matching is done)
 						String selectedFacilityCode = selectedFacility.getUid();
 						String selectedProductCode = selectedProduct.getUid();
 						Stockout stockout = stockoutService.getStockout(
 								selectedFacilityCode, selectedProductCode);
 						// -----------------------------------------------------------------------------
 						if (stockout != null){
 						displayText = stockoutDao.getMenu(6) + " "
 								+ stockout.getIssue().getState().toString()
 								+ stockoutDao.getMenu(8);
 						} else{
 							displayText = stockoutDao.getMenu(62) + " "
 									+ stockoutDao.getMenu(8);
 						}
 						break;
 					case 3:
 						// displayText =
 						// stockoutDao.findNearestNeighbourWithStock(medicineName,facilityName);
 
 						// -----------------------------------------------------------------------------
 						//System.out.println(selectedFacility.getLocalName());
 						
 						Facility closestFacility = facilityService
 								.getNearestFacilityWithStock(selectedProduct,
 										selectedFacility);
 
 						// -----------------------------------------------------------------------------
 
 						displayText = stockoutDao.getMenu(7) + " "
 								+ closestFacility.getLocalName() + " " + closestFacility.getFacilityType().getReadable() + " "
 								+ stockoutDao.getMenu(8);
 						break;
 					}
 
 					menuRequest = 99;
 
 				} else if (requestOption == 2) {
 
 					displayText = stockoutDao.getMenu(10) + " "
 							+ selectedProduct.getName() + "\n";
 
 					List<Product> productDosage = (List<Product>) keyValueStore
 							.get("dosages." + sessionId);
 
 					for (int k = 0; k < productDosage.size(); k++) {
 
 						displayText += (k + 1)
 								+ "."
 								+ productDosage.get(k).getDescription() + "\n";
 
 					}
 					
 					displayText += stockoutDao.getMenu(11);
 
 					// -----------------------------------------------------------------------------
 						menuRequest =5;
 						
 
 				} else if (requestOption == 3) {
 					displayText = ((Facility) keyValueStore.get("facilityName."
 							+ sessionId)).getLocalName()
 							+ " " + ((Facility) keyValueStore.get("facilityName."
 									+ sessionId)).getFacilityType().getReadable() + " " + stockoutDao.getMenu(1);
 					menuRequest = 2;
 
 				} else {
 
 					displayText = stockoutDao.getMenu(91);
 					throw new NumberFormatException();
 				}
 
 				break;
 
 			case 99:
 
 				keyValueStore.remove("facilityName." + sessionId);
 				keyValueStore.remove("service." + sessionId);
 				keyValueStore.remove("productName." + sessionId);
 				keyValueStore.remove("displayText." + sessionId);
 				keyValueStore.remove("requestId." + sessionId);
 				keyValueStore.remove("commonStockouts." + sessionId);
 				keyValueStore.remove("dosages." + sessionId);
 
 			}
 		} catch (NumberFormatException e) {
 			// reload data from the last request if an error in the users input
 			// was detected
 			// do not call setResponse as there is no need to overwrite
 			// previously saved menu
 			// text with the exact same text. Also avoid concatenating multiple
 			// error messages
 
 			String strMenuRequest = (String) keyValueStore.get("requestId."
 					+ sessionId);
 			displayText += (String) keyValueStore.get("displayText."
 					+ sessionId);
 			response.setDisplayText(displayText);
 			response.setRequestID(strMenuRequest);
 
 			return response;
 		}
 
 		// set response once menu logic processing is complete
 		setResponse(response, displayText, menuRequest, sessionId);
 
 		return response;
 
 	}
 
 	// set the response object with menu text and menu level(request id), store
 	// these variables in
 	// key value store so they can be used for error handling if needed.
 	private void setResponse(CMUssdResponse response, String menuText,
 			int requestId, String sessionId) {
 
 		response.setDisplayText(menuText);
 		response.setRequestID(Integer.toString(requestId));
 		keyValueStore.put("displayText." + sessionId, menuText);
 		keyValueStore
 				.put("requestId." + sessionId, Integer.toString(requestId));
 	}
 
 }
