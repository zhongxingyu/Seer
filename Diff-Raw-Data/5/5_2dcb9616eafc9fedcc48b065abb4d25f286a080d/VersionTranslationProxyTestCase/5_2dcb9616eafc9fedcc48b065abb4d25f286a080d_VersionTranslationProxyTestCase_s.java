 package org.mule.transport.fix.examples.versiontranslationproxy;
 
 import org.mule.api.MuleMessage;
 import org.mule.module.client.MuleClient;
 import org.mule.tck.FunctionalTestCase;
 
 import quickfix.field.ClOrdID;
 import quickfix.field.ExecTransType;
 import quickfix.field.ExecType;
 import quickfix.field.HandlInst;
 import quickfix.field.OrdStatus;
 import quickfix.field.OrdType;
 import quickfix.field.OrderQty;
 import quickfix.field.Side;
 import quickfix.field.Symbol;
 import quickfix.field.TransactTime;
 
 public class VersionTranslationProxyTestCase extends FunctionalTestCase {
 	private static int nextID = 1;
 	public void testExecutorConfig() throws Exception {
 		MuleClient client = new MuleClient();
 
 		quickfix.fix42.NewOrderSingle newOrderSingle = new quickfix.fix42.NewOrderSingle(
                 new ClOrdID(Long.valueOf(System.currentTimeMillis()+(nextID++)).toString()), new HandlInst('1'), new Symbol("LNX"),
                 new Side(Side.BUY), new TransactTime(), new OrdType(OrdType.MARKET));
         newOrderSingle.set(new OrderQty(1));
 
 		client
 				.dispatch(
 						"in",
 						newOrderSingle, null);
 		MuleMessage result;
 		quickfix.fix42.ExecutionReport report;
		result=client.request("vm://out", 5000);
 		assertNotNull(result);
 		assertNotNull(result.getPayload());
 		assertTrue(result.getPayload() instanceof quickfix.fix42.ExecutionReport);
 		report=(quickfix.fix42.ExecutionReport)result.getPayload();
 		assertEquals(ExecTransType.NEW,report.getExecTransType().getValue());
 		assertEquals(ExecType.FILL,report.getExecType().getValue());
 		//assertEquals(OrdStatus.NEW,report.getOrdStatus().getValue());
 		assertTrue(OrdStatus.NEW==report.getOrdStatus().getValue()||OrdStatus.FILLED==report.getOrdStatus().getValue());
 		
 		result=client.request("vm://out", 5000);
 		assertNotNull(result);
 		assertNotNull(result.getPayload());
 		assertTrue(result.getPayload() instanceof quickfix.fix42.ExecutionReport);
 		report=(quickfix.fix42.ExecutionReport)result.getPayload();
 		assertEquals(ExecTransType.NEW,report.getExecTransType().getValue());
 		assertEquals(ExecType.FILL,report.getExecType().getValue());
 		//assertEquals(OrdStatus.FILLED,report.getOrdStatus().getValue());
 		assertTrue(OrdStatus.NEW==report.getOrdStatus().getValue()||OrdStatus.FILLED==report.getOrdStatus().getValue());
 		
 		result=client.request("vm://out", 1000);
 		assertNull(result);			
 	}
 
 	@Override
 	protected String getConfigResources() {
		return "examples/src/main/resources/versiontranslationproxy/mule-config.xml,examples/src/test/resources/versiontranslationproxy/executor-mule-config.xml,examples/src/test/resources/versiontranslationproxy/vm-as-banzai-mule-config.xml";
 	}
 
 }
