 package org.jboss.as.quickstarts.helloworld.model;
 
 import java.math.BigInteger;
 
 /**
  * @author <a href="mailto:mstrukel@redhat.com>Marko Strukelj</a>
  */
 public class MasterCardPaymentMethod extends AbstractPaymentMethod
 {
    private MasterCardPaymentMethod() {
       System.out.println("MasterCatPaymentMethod<init>");
    }
 
    @Override
    public void performPayment(BigInteger amount) {
       // MasterCard service . doPayment(name, number, expiresMonth + "/" + expiresYear, amount)
    }
 }
