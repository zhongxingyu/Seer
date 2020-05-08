 package aplikasi.banking.ws.spring;
 
import aplikasi.banking.service.CoreBankingService;
import com.artivisi.banking.SaldoRequest;
import com.artivisi.banking.SaldoResponse;
 import org.springframework.ws.server.endpoint.annotation.Endpoint;
 import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
 import org.springframework.ws.server.endpoint.annotation.RequestPayload;
 import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
 
 @Endpoint
 public class BankingEndpoint {
 	private CoreBankingService coreBankingService = new CoreBankingService();
 	
 	
 	@PayloadRoot(localPart="SaldoRequest", 
 			namespace="http://www.artivisi.com/banking")
 	@ResponsePayload
 	public SaldoResponse saldo(@RequestPayload SaldoRequest saldoRequest){
 		// hanya wrapper saja, business logic tidak disini
 		String nomer = saldoRequest.getNomer();
 		SaldoResponse response = new SaldoResponse();
 		response.setSaldo(coreBankingService.cekSaldo(nomer));
 		return response;
 	}
 }
