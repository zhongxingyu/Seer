 package org.apache.struts2.freeroute;
 
 
 import com.google.common.collect.ArrayListMultimap;
 import org.apache.struts2.components.Param;
 import org.apache.struts2.freeroute.annotation.MethodType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.servlet.http.HttpServletRequest;
 import java.util.*;
 import java.util.regex.Pattern;
 
 /**
  * 处理所有的路由信息
  * <p/>
  * 可将 mapping 分成两种，一种是静态的 直接通过 Map 就可以快速找到路由；
  * 另一种是有 pathVariable 的，需要通过正则匹配查找路由
  *
  * @author bastengao
  * @date 12-12-16 22:58
  */
 public class DefaultRouteMappingHandler implements RouteMappingHandler {
     private static final Logger log = LoggerFactory.getLogger(DefaultUnknownHandler.class);
 
     /**
      * 静态路由
      */
     private ArrayListMultimap<String, RouteMapping> staticRoutes = ArrayListMultimap.create();
 
     /**
      * routePath 中包括 pathVariable 中的路由映射
      */
     //private Map<String, RouteMapping> dynamicRoutes = new LinkedHashMap<String, RouteMapping>();
     private ArrayListMultimap<String, RouteMapping> dynamicRoutes = ArrayListMultimap.create();
     private Map<String, Pattern> dynamicRoutesPattern = new HashMap<String, Pattern>();
 
     /**
      * 默认 key 是 @Route.value 的值
      *
      * @param flattedRoutePath
      * @param routeMapping
      */
     @Override
     public void put(String flattedRoutePath, RouteMapping routeMapping) {
         if (routeMapping.hasPathVariables()) {
             // 正则 => 路由
             dynamicRoutes.put(routeMapping.getRoutePathPattern().pattern(), routeMapping);
             dynamicRoutesPattern.put(routeMapping.getRoutePathPattern().pattern(), routeMapping.getRoutePathPattern());
         } else {
             staticRoutes.put(flattedRoutePath, routeMapping);
         }
     }
 
     @Override
     public RouteMapping route(HttpServletRequest request) {
         //三个依据判断是否有对应的路由映射
         //1. servletPath ( 其中可以包括 pathVariable 的匹配 )
         //2. http method
         //3. 特定的 param
 
         String servletPath = request.getServletPath();
 
         // 在静态路由中查找
         if (staticRoutes.containsKey(servletPath)) {
             // 匹配 servletPath 的路由中返回权重最大的路由
             List<RouteMapping> matchPathRouteMapping = staticRoutes.get(servletPath);
             return findMaxWeight(request, matchPathRouteMapping);
         }
 
         // 在动态路由中查找
         for (Map.Entry<String, Pattern> patternEntry : dynamicRoutesPattern.entrySet()) {
             if (patternEntry.getValue().matcher(servletPath).matches()) {
                 // 匹配 servletPath 的路由中返回权重最大的路由
                 List<RouteMapping> matchPathRouteMappings = dynamicRoutes.get(patternEntry.getKey());
                 return findMaxWeight(request, matchPathRouteMappings);
             }
         }
         return null;
     }
 
     /**
      * 根据已经匹配 servletPath 的路由集合找出最匹配的路由
      *
      * @param request
      * @param routeMappings
      * @return
      */
     private RouteMapping findMaxWeight(HttpServletRequest request, List<RouteMapping> routeMappings) {
         int maxWeight = -1;
         RouteMapping maxWeightRoute = null;
         for (RouteMapping routeMapping : routeMappings) {
             int weight = weightOfRoute(request, routeMapping);
             if (weight > 0) {
                 if (weight > maxWeight) {
                     maxWeight = weight;
                     maxWeightRoute = routeMapping;
                 }
             }
         }
         return maxWeightRoute;
     }
 
     /**
      * 返回请求与匹配的路由的权重. 如果不匹配返回小于 0 的值，如果匹配返回权重值。
      * 其中 method 的权重比 param 权重高
      * weight = method(GET, POST, PUT, DELETE) * 2000  +  param * 1
      * weight = method(                  NONE) * 1000  +  param * 1
      *
      * @param request
      * @param routeMapping
      * @return
      */
     private static int weightOfRoute(HttpServletRequest request, RouteMapping routeMapping) {
         // 测试 method 是否匹配，
         int methodWeight = weightOfMethod(request, routeMapping);
         if (methodWeight < 0) {
             return -1;
         }
 
         // 测试是否匹配 param
         int weightOfParams = weightOfParams(request, routeMapping);
         if (weightOfParams < 0) {
             return -1;
         }
 
         // 计算总权重
         if (log.isTraceEnabled()) {
             log.trace("weight: {}", methodWeight + weightOfParams);
         }
         return methodWeight + weightOfParams;
     }
 
     /**
      * 返回 method 的权重，如果不匹配返回 -1
      * MethodType.NONE 权重为 1000, 其他为 2000
      *
      * @param request
      * @param routeMapping
      * @return
      */
     private static int weightOfMethod(HttpServletRequest request, RouteMapping routeMapping) {
        if (routeMapping.getRoute().method() == MethodType.NONE) {
             return 1000;
         }
 
         MethodType methodType = RouteUtil.valueOfMethod(request.getMethod());
         if (methodType == null) {
             return -1;
         }
 
        if (routeMapping.getRoute().method() == methodType) {
            return 2000;
         }
 
         return -1;
     }
 
     /**
      * 返回 params 的权重. 如果不匹配返回 -1
      * 如果匹配返回 params.length
      *
      * @param request
      * @param routeMapping
      * @return
      */
     private static int weightOfParams(HttpServletRequest request, RouteMapping routeMapping) {
         // 如果没有 params 参数, 则直接返回 0
         if (routeMapping.getRoute().params().length == 0) {
             return 0;
         }
 
         Set<String> paramsOfRequest = paramNames(request);
        for(RouteMapping.Param param :routeMapping.getParams()){
            if(!param.match(request, paramsOfRequest)){
                 return -1;
             }
         }
 
         return routeMapping.getParams().size();
     }
 
     /**
      * 返回请求中有的参数名称
      *
      * @param request
      * @return
      */
     private static Set<String> paramNames(HttpServletRequest request) {
         Enumeration<String> nameEnum = request.getParameterNames();
         Set<String> names = new HashSet<String>();
         while (nameEnum.hasMoreElements()) {
             names.add(nameEnum.nextElement());
         }
         return names;
     }
 }
