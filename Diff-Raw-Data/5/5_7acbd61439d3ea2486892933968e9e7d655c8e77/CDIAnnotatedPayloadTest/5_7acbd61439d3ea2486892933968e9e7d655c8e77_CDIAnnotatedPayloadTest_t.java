 package li.rudin.rt.core.test.cdi;
 
 import java.util.List;
 
 import javax.inject.Inject;
 
 import li.rudin.rt.api.RTServer;
 import li.rudin.rt.api.annotations.ChangeId;
 import li.rudin.rt.core.client.RTClientImpl;
 import li.rudin.rt.core.container.ObjectContainer;
import li.rudin.rt.core.test.cdi.base.CDITestRunner;
 import li.rudin.rt.core.test.cdi.base.Mock;
 
 import org.junit.Assert;
 import org.junit.Test;
import org.junit.runner.RunWith;
 
@RunWith(CDITestRunner.class)
 public class CDIAnnotatedPayloadTest
 {
 	
 	@Inject @Mock RTClientImpl queue;
 	@Inject RTServer rt;
 	
 	@Test
 	public void test()
 	{
 		queue.copyAndClear();
 		
 		MyBean bean = new MyBean();
 		bean.setX(5);
 		
 		rt.send(bean);
 		
 		List<ObjectContainer> list = queue.copyAndClear();
 		Assert.assertEquals(1, list.size());
 		Assert.assertEquals("myId", list.get(0).getType());
 		Assert.assertNotNull(list.get(0).getData());
 		
 		Object data = list.get(0).getData();
 		
 		Assert.assertTrue(data instanceof MyBean);
 		
 		Assert.assertEquals(5, ((MyBean)data).getX());
 	}
 	
 	@ChangeId("myId")
 	public static class MyBean
 	{
 		private int x;
 
 		public int getX()
 		{
 			return x;
 		}
 
 		public void setX(int x)
 		{
 			this.x = x;
 		}
 	}
 }
