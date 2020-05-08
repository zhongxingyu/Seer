 package swfapp.workflow_worker.impl;
 
 import swfapp.activity_worker.AWSClient;
 import swfapp.activity_worker.AWSClientImpl;
 import swfapp.activity_worker.JPLDatacenterClient;
 import swfapp.activity_worker.JPLDatacenterClientImpl;
 import swfapp.activity_worker.data.CombineResult;
 import swfapp.activity_worker.data.ProcessingResultA;
 import swfapp.activity_worker.data.ProcessingResultB;
 import swfapp.activity_worker.data.ProcessingResultC;
 import swfapp.activity_worker.data.ProcessingResultD;
 import swfapp.workflow_worker.Decider;
 import util.Log;
 
 import com.amazonaws.services.simpleworkflow.flow.annotations.Asynchronous;
 import com.amazonaws.services.simpleworkflow.flow.core.Promise;
 
 public class DeciderImpl implements Decider {
 
 	private AWSClient aws = new AWSClientImpl();
 	private JPLDatacenterClient jpl = new JPLDatacenterClientImpl();
 
 	@Override
 	public void processStart(final String srcImagePath) {
 		Log.log("[DECIDER]decision making");
 		// send image to S3
 		Promise<String> s3ImagePath = jpl.fileTransfer(srcImagePath);
 
		// branch processing
 		Promise<ProcessingResultA> resultA = aws.dataProcessingA(s3ImagePath);
 		Promise<ProcessingResultB> resultB = aws.dataProcessingB(s3ImagePath);
 		Promise<ProcessingResultC> resultC = aws.dataProcessingC(s3ImagePath);
		
 		Promise<ProcessingResultD> resultD = aws.dataProcessingD(resultC);
 
 		// join result
 		Promise<CombineResult> combineResult = jpl.combineResult(resultA,
 				resultB, resultD);
 
 		// show result
 		Promise<Void> endResult = jpl.showResult(combineResult);
 
 		printEnd(endResult);
 	}
 
 	@Asynchronous
 	private void printEnd(Promise<Void> endResult) {
 		Log.log("[DECIDER]Finish workflow");
 	}
 }
