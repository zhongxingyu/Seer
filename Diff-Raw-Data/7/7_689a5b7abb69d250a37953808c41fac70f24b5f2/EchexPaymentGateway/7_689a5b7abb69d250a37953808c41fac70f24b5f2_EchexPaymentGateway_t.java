 package com.orangeleap.tangerine.service.payments;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.orangeleap.tangerine.domain.checkservice.Batch;
 import com.orangeleap.tangerine.domain.checkservice.Detail;
 import com.orangeleap.tangerine.domain.checkservice.PaymentProcessorException;
 import com.orangeleap.tangerine.domain.checkservice.Response;
 import com.orangeleap.tangerine.domain.paymentInfo.Gift;
 import com.orangeleap.tangerine.service.CheckService;
 
 public class EchexPaymentGateway implements ACHPaymentGateway {
     private CheckService checkService = null;
 	private static final Log logger = LogFactory.getLog(EchexPaymentGateway.class);
 
     public void setCheckService(CheckService s)
     {
     	checkService = s;
     }
     
     public CheckService getCheckService()
     {
     	return checkService;
     }
 
     // Return a Batch configured for TEST (to be safe)
     private Batch getTestBatch() {
         Batch batch = checkService.createBatch(853,11229,5000,"Orange Leap");
         batch.isTest(true);
         batch.setFileNumber(1);
         return batch;
 
     }
 
 	@Override
 	public void Process(Gift g) {
 			if (g.getPaymentType() != "ACH") return;
 			
 			Batch batch = getTestBatch();
 	        Detail detail = new Detail();
 	        detail.setDateTime(g.getDonationDate());
 	        detail.setAccountNumber(g.getPaymentSource().getAchRoutingNumber()+g.getPaymentSource().getAchAccountNumber());
 	        detail.setCheckAmount(g.getAmount());
 	        detail.setTransactionNumber(g.getId().intValue());
 	        batch.setDetail(detail);
 
 	        Response response = null;
 
 	        try {
 	            response = checkService.sendBatch(batch);
 	        } catch(PaymentProcessorException exception) {
 	        	logger.error(exception.getMessage());
 	        }
 	}
 
 	@Override
 	public void Refund(Gift g) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
