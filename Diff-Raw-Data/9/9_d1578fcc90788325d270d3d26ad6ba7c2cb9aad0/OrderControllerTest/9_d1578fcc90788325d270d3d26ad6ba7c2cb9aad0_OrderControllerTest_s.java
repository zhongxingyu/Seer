 package com.abudko.reseller.huuto.mvc.order;
 
 import static org.mockito.Mockito.doThrow;
 import static org.mockito.Mockito.reset;
 import static org.mockito.Mockito.verify;
 import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
 
 import javax.annotation.Resource;
 
 import nl.captcha.Captcha;
 import nl.captcha.Captcha.Builder;
 import nl.captcha.text.renderer.ColoredEdgesWordRenderer;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mockito;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.mock.web.MockHttpSession;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.web.WebAppConfiguration;
 import org.springframework.test.web.servlet.MockMvc;
 import org.springframework.test.web.servlet.setup.MockMvcBuilders;
 import org.springframework.web.context.WebApplicationContext;
 
 import com.abudko.reseller.huuto.notification.email.order.OrderConfirmationEmailSender;
 import com.abudko.reseller.huuto.notification.email.order.OrderEmailSender;
 import com.abudko.reseller.huuto.order.ItemOrder;
 import com.abudko.reseller.huuto.query.exception.EmailNotificationException;
 import com.abudko.reseller.huuto.query.html.item.ItemResponse;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @WebAppConfiguration
 @ContextConfiguration(locations = { "classpath:/spring/test-webapp-config.xml" })
 public class OrderControllerTest {
 
     private static final String ORDER_PATH = "/order";
 
     private static final String ORDER_SUCCESS_URL_PATH = "/WEB-INF/views/order/orderSuccess.jsp";
    private static final String ORDER_INVALID_URL_PATH = "/WEB-INF/views/item.jsp";
 
     private static final String CUSTOMER_NAME = "customer_name";
     private static final String CUSTOMER_EMAIL = "customeremail@mail.com";
     private static final String CUSTOMER_PHONE = "123456789";
     private static final String TEXT = "text";
     private static final String ITEM_URL = "itemUrl";
     private static final String IMAGE_BASE_SRC = "imgBaseSrc";
 
     @Autowired
     private WebApplicationContext wac;
 
     private MockMvc mockMvc;
 
     private Captcha captcha;
 
     private MockHttpSession session;
 
     @Resource
     private OrderEmailSender orderEmailSender;
 
     @Resource
     private OrderConfirmationEmailSender orderConfirmationEmailSender;
 
     private ItemResponse itemResponse;
 
     @Before
     public void setup() {
         this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
         this.session = new MockHttpSession();
         setupCaptcha();
 
         itemResponse = new ItemResponse();
         itemResponse.setImgBaseSrc(IMAGE_BASE_SRC);
         itemResponse.setItemUrl(ITEM_URL);
         
         reset(this.orderEmailSender);
         reset(this.orderConfirmationEmailSender);
     }
 
     private void setupCaptcha() {
         Builder builder = new Captcha.Builder(100, 100);
         ColoredEdgesWordRenderer coloredEdgesWordRenderer = new ColoredEdgesWordRenderer();
         builder.addText(coloredEdgesWordRenderer);
         this.captcha = builder.build();
         this.captcha.getImage();
         this.session.setAttribute(Captcha.NAME, this.captcha);
     }
 
     @Test
     public void testOrderPOSTSendOrder() throws Exception {
         this.mockMvc
                 .perform(
                         post(ORDER_PATH).session(session).param("customerName", CUSTOMER_NAME)
                                 .param("customerEmail", CUSTOMER_EMAIL).param("customerPhone", CUSTOMER_PHONE)
                                 .param("text", TEXT).param("captcha", captcha.getAnswer())).andExpect(status().isOk())
                 .andExpect(forwardedUrl(ORDER_SUCCESS_URL_PATH));
 
         verify(orderEmailSender).send(Mockito.any(ItemOrder.class));
     }
 
     @Test
     public void testOrderPOSTSendConfirmation() throws Exception {
         this.mockMvc
                 .perform(
                         post(ORDER_PATH).session(session).param("customerName", CUSTOMER_NAME)
                                 .param("customerEmail", CUSTOMER_EMAIL).param("customerPhone", CUSTOMER_PHONE)
                                 .param("text", TEXT).param("captcha", captcha.getAnswer())).andExpect(status().isOk())
                 .andExpect(forwardedUrl(ORDER_SUCCESS_URL_PATH));
 
         verify(orderConfirmationEmailSender).send(Mockito.any(ItemOrder.class));
     }
 
     @Test
     public void testOrderPOSTInvalidName() throws Exception {
         this.mockMvc
                 .perform(
                         post(ORDER_PATH).session(session).param("customerEmail", CUSTOMER_EMAIL)
                                 .param("customerPhone", CUSTOMER_PHONE).param("text", TEXT)
                                 .param("captcha", captcha.getAnswer())).andExpect(status().isOk())
                 .andExpect(model().hasErrors()).andExpect(forwardedUrl(ORDER_INVALID_URL_PATH));
     }
 
     @Test
     public void testOrderPOSTInvalidEmail() throws Exception {
         this.mockMvc
                 .perform(
                         post(ORDER_PATH).session(session).param("customerName", CUSTOMER_NAME)
                                 .param("customerPhone", CUSTOMER_PHONE).param("text", TEXT)).andExpect(status().isOk())
                 .andExpect(model().hasErrors()).andExpect(forwardedUrl(ORDER_INVALID_URL_PATH));
     }
 
     @Test
     public void testOrderPOSTInvalidCaptcha() throws Exception {
         this.mockMvc
                 .perform(
                         post(ORDER_PATH).session(session).param("customerName", CUSTOMER_NAME)
                                 .param("customerEmail", CUSTOMER_EMAIL).param("customerPhone", CUSTOMER_PHONE)
                                 .param("text", TEXT)).andExpect(status().isOk()).andExpect(model().hasErrors())
                 .andExpect(forwardedUrl(ORDER_INVALID_URL_PATH));
     }
 
     @Test
     public void testSendOrderEmailThrowsException() throws Exception {
         doThrow(new EmailNotificationException("")).when(orderEmailSender).send(Mockito.any(ItemOrder.class));
 
         this.mockMvc
                 .perform(
                         post(ORDER_PATH).session(session).param("customerName", CUSTOMER_NAME)
                                 .param("customerEmail", CUSTOMER_EMAIL).param("customerPhone", CUSTOMER_PHONE)
                                 .param("text", TEXT).param("captcha", captcha.getAnswer())).andExpect(status().isOk())
                 .andExpect(model().hasErrors()).andExpect(forwardedUrl(ORDER_INVALID_URL_PATH));
     }
 }
