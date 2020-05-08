 package org.rec.planets.jupiter.rule;
 
 import org.springframework.beans.factory.DisposableBean;
 import org.springframework.context.ApplicationListener;
 import org.springframework.context.support.AbstractApplicationContext;
 import org.springframework.context.support.FileSystemXmlApplicationContext;
 
 import com.google.common.collect.HashBasedTable;
 import com.google.common.collect.Table;
 
 /**
  * 以Spring初始化ApplicationContext来实现的规则工厂,提供的资源文件是spring的配置文件和相关脚本.
  * 需要注意的是,spring配置文件中,有且只有一个Rule的bean定义
  * 
  * @author rec
  * 
  */
 public class SpringRuleFactory implements RuleFactory, DisposableBean,
 		ApplicationListener<RuleVersionToDestroyEvent> {
 	/**
 	 * applicationContext表,websiteId为行,version为列
 	 */
 	private Table<Short, Long, AbstractApplicationContext> table = HashBasedTable
 			.create();
 
 	@Override
 	public Rule build(String... resources) throws Exception {
 		AbstractApplicationContext ac = new FileSystemXmlApplicationContext(
 				resources);
 		Rule rule = ac.getBean(Rule.class);
 
 		synchronized (this) {
 			table.put(rule.getWebsiteId(), rule.getVersion(), ac);
 		}
 		return rule;
 	}
 
 	@Override
 	public synchronized void destroy() throws Exception {
 		for (AbstractApplicationContext ac : table.values()) {
 			ac.close();
 		}
 	}
 
 	@Override
 	public synchronized void onApplicationEvent(RuleVersionToDestroyEvent event) {
		AbstractApplicationContext ac = table.remove(event.getWebsiteId(),
 				event.getVersion());
		if (ac != null) {
 			ac.close();
		}
 	}
 
 }
