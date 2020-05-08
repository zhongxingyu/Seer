 package springapp.web.sale;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
 import springapp.domain.Sale;
 import springapp.service.SaleService;
 
 @RunWith(MockitoJUnitRunner.class)
 public class CreateSaleControllerTest {
 
 	@Mock
 	private SaleService mockSaleService;
 	@Mock
 	private BindingResult mockResult;
 	@Mock
 	private RedirectAttributes mockRedirectAttributes;
 
 	private CreateSaleController controller;
 
 	@Before
 	public void setup() {
 		controller = new CreateSaleController(mockSaleService);
 
 	}
 
 	@Test
 	public void getNewFormBeanFromController() {
 		assertNotNull(controller.createFormBean());
 	}
 
 	@Test
 	public void testThatWeHaveAController() {
 		CreateSaleController controller = new CreateSaleController(null);
		assertThat("createsale", equalTo(controller.setupForm(null)));
 	}
 
 	@Test
 	public void saleFactoryMethodWorksWithSaleBeans() {
 
 		SaleBean saleBean = new SaleBean();
 		saleBean.setSalesAssistant("Billy");
 		saleBean.setTillId("aTillId");
 		saleBean.setTimeStamp("19 May, 2061");
 		Sale sale = controller.saleFromSaleBean(saleBean);
 		assertThat("Billy", is(equalTo(sale.getSalesAssistant())));
 		assertThat("aTillId", is(equalTo(sale.getTillId())));
 		assertThat("19 May, 2061", is(equalTo(sale.getTimeStamp())));
 	}
 
 	@Test
 	public void onFormSubmitReturnsNullIfResultHasErrors(){
 		when(mockResult.hasErrors()).thenReturn(true);
 		assertNull(controller.processSubmit(new SaleBean(), mockResult, null, mockRedirectAttributes));
 	}
 
 	@Test
 	public void openSaleIsCalledByCreateSaleControllerOnFormSubmit() {
 
 		SaleBean saleBean = new SaleBean();
 		saleBean.setSalesAssistant("Jojo");
 		saleBean.setTillId("AnyIDa");
 		saleBean.setTimeStamp("1 February, 3456");
 
 		controller.processSubmit(saleBean, mockResult, null,
 				mockRedirectAttributes);
 
 		verify(mockSaleService).openSale(any(Sale.class));
 	}
 }
