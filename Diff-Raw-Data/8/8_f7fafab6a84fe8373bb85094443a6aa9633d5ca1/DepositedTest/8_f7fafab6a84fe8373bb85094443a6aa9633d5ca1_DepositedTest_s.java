 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.mockito.invocation.InvocationOnMock;
 import org.mockito.stubbing.Answer;
 
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Mockito.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: sqv-nbt
  * Date: 6/10/13
  * Time: 2:45 PM
  * To change this template use File | Settings | File Templates.
  */
 public class DepositedTest {
     BankAccountDao mockDao = mock(BankAccountDao.class);
 
     @Before
     public void setUp() {
         reset(mockDao);
         BankAccount.setBankAccountDao(mockDao);
     }
 
     @Test
     public void testDepositedMoneyToAccount() {
         String accountNumber = "1234567890";
         double balance = 0;
         double depositedMoney = 50;
         String log = "deposit 50";
        BankAccount.openAccount(accountNumber);

        BankAccount.deposit(accountNumber,depositedMoney,log);
 
         when(mockDao.getAccount(accountNumber)).thenAnswer(new Answer<Object>() {
             @Override
             public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                 String accountNumber2 = "1234567890";
                 BankAccountDTO answerDTO = new BankAccountDTO(accountNumber2);
                 return answerDTO;
             }
         });
 
         ArgumentCaptor<BankAccountDTO> savedAccount = ArgumentCaptor.forClass(BankAccountDTO.class);
         ArgumentCaptor<String> savedLog = ArgumentCaptor.forClass(String.class);
         verify(mockDao).save(savedAccount.capture(),savedLog.capture());
 
         assertEquals(savedAccount.getValue().getAccountNumber(),accountNumber);
         assertEquals(savedAccount.getValue().getBalance(),depositedMoney,0.001);
 
         assertEquals(savedLog.getValue(),log);
     }
 
 
 
 }
