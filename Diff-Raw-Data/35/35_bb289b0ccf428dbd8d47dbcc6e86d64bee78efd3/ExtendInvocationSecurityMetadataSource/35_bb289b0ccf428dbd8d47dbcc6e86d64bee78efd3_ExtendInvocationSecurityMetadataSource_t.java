 /**
  * @author hyd 2012-11-30 上午9:13:49 
  * @version 1.0
  */
 package f.rd.paths.web.security.extend.impl;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.hibernate.Criteria;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.security.access.ConfigAttribute;
 import org.springframework.security.access.SecurityConfig;
 import org.springframework.security.web.FilterInvocation;
 import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
 
 import f.rd.paths.web.security.extend.impl.AntUrlPathMatcher;
 import f.rd.paths.web.security.extend.impl.UrlMatcher;
 import f.rd.paths.web.security.model.AuthoResource;
 import f.rd.paths.web.security.model.Authority;
 import f.rd.paths.web.security.model.Resource;
 
 /** 
  * 此类在初始化时，应该取到所有资源及其对应角色的定义
  * <p>
  *  
  */
 public class ExtendInvocationSecurityMetadataSource implements FilterInvocationSecurityMetadataSource {
 	
 	private UrlMatcher urlMatcher = new AntUrlPathMatcher();
 	
 	Session session = null;
 	
 	// 应当是资源为key， 权限为value。 资源通常为url， 权限就是那些以ROLE_为前缀的角色。 一个资源可以由多个权限来访问。
 	private static Map<String, Collection<ConfigAttribute>> resourceMap = null;
 	
 	public ExtendInvocationSecurityMetadataSource() {
 		loadResourceDefine();
 	}
 	
 	private void loadResourceDefine() {
 		// 数据库连接
 		ApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring/root-context.xml");
 		SessionFactory sessionFactory = (SessionFactory) context.getBean("sessionFactory");
 		session = sessionFactory.openSession();
 		
 		resourceMap = new HashMap<String, Collection<ConfigAttribute>>();
 		// 1. 在Web服务器启动时，提取系统中的所有权限。
 		List<Authority> allAuthorities = this.getAllAuthorities();
 		// 2. 循环查找权限对应对的资源
 		for (Authority authority : allAuthorities) {
 			ConfigAttribute ca = new SecurityConfig(authority.getCode());
 			List<Resource> authResources = this.getAuthResources(authority.getId());
 			// 3. 判断资源文件和权限的对应关系，如果已经存在相关的资源url，则要通过该url为key提取出权限集合，将权限增加到权限集合中。
 			for (Resource resource : authResources) {
 				if (resourceMap.containsKey(resource.getUri())) {
 					Collection<ConfigAttribute> value = resourceMap.get(resource.getUri());
 					value.add(ca);
 					resourceMap.put(resource.getUri(), value);
 				} else {
 					Collection<ConfigAttribute> newCA = new ArrayList<ConfigAttribute>();
 					newCA.add(ca);
 					resourceMap.put(resource.getUri(), newCA);
 				}
 			}
 		}
 	}
 
 	// 根据URL，找到相关的权限配置。
 	public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {
 		// object 是一个URL，被用户请求的url。
 		String url = ((FilterInvocation) object).getRequestUrl();
         int firstQuestionMarkIndex = url.indexOf("?");
         if (firstQuestionMarkIndex != -1) {
             url = url.substring(0, firstQuestionMarkIndex);
         }
 		Iterator<String> ite = resourceMap.keySet().iterator();
 		while (ite.hasNext()) {
 			String resURL = ite.next();
 			if (urlMatcher.pathMatchesUrl(url, resURL)) {
 				return resourceMap.get(resURL);
 			}
 		}
 		return null;
 	}
 
 	public Collection<ConfigAttribute> getAllConfigAttributes() {
 		return null;
 	}
 
 	public boolean supports(Class<?> clazz) {
 		return true;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private List<Resource> getAuthResources(Integer authority) {
 		// 对应关联表
 		List<AuthoResource> authResources = this.getAuthoResources(authority);
 		List<Integer> resoIds = new ArrayList<Integer>();
 		for (AuthoResource authoResource : authResources) {
 			resoIds.add(authoResource.getResource());
 		}
 		Criteria criteria = session.createCriteria(Resource.class);
 		criteria.add(Restrictions.in("id", resoIds));
 		return criteria.list();
 	}
 	
 	@SuppressWarnings("unchecked")
 	private List<AuthoResource> getAuthoResources(Integer authority) {
 		Query query = session.createQuery("from AuthoResource a where a.authority=:authority").setInteger("authority", authority);
 		return query.list();
 	}
 	
 	@SuppressWarnings("unchecked")
 	private List<Authority> getAllAuthorities() {
 		Query query = session.createQuery("from Authority a");
 		return query.list();
 	}
 
 }
