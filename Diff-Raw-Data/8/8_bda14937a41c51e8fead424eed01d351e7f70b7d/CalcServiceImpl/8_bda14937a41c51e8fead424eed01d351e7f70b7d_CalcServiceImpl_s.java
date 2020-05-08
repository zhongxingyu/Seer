 // ========================================================================
 // Copyright (C) zeroth Project Team. All rights reserved.
 // GNU AFFERO GENERAL PUBLIC LICENSE Version 3, 19 November 2007
 // http://www.gnu.org/licenses/agpl-3.0.txt
 // ========================================================================
 package zeroth.actor.service.app.ws.calc;
import java.util.logging.Logger;
import javax.inject.Inject;
 import javax.jws.WebParam;
 /**
  * Calculator service.
  * @author nilcy
  */
 // @Stateless
 // @WebService(portName = "CalcPort", serviceName = "CalcService", name = "Calc", targetNamespace =
 // "http://zeroth.com/ws/calc", endpointInterface = "zeroth.actor.app.ws.calc.CalcService")
 public class CalcServiceImpl implements CalcService {
    /** ロガー */
    @Inject
    private Logger log;
     /** コンストラクタ */
     public CalcServiceImpl() {
        super();
     }
     /** {@inheritDoc} */
     @Override
     // @WebMethod
     public CalcResponse add(@WebParam(name = "request") final CalcRequest aRequest) {
         // final CalcRequest request = (CalcRequest) aRequest;
         return CalcFactory.createCalcResponse(aRequest.getParam1() + aRequest.getParam2());
     }
 }
