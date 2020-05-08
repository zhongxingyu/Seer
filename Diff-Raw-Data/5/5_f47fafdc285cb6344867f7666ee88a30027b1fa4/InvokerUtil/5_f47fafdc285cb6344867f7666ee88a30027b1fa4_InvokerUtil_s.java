 
 /*
  * InvokerUtil.java
  *
  * Created on October 27, 2009, 4:14 PM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 
 package com.rameses.osiris2.client;
 
 import com.rameses.osiris2.SessionContext;
 import com.rameses.osiris2.Folder;
 import com.rameses.rcp.util.ControlSupport;
 import com.rameses.rcp.framework.UIController;
 import com.rameses.rcp.framework.ClientContext;
 import com.rameses.osiris2.Invoker;
 import com.rameses.platform.interfaces.Platform;
 import com.rameses.rcp.framework.ControllerProvider;
 import com.rameses.rcp.framework.UIControllerContext;
 import com.rameses.rcp.framework.UIControllerPanel;
 import com.rameses.common.ExpressionResolver;
 import com.rameses.rcp.common.Action;
 import com.rameses.rcp.common.Opener;
 import com.rameses.rcp.common.Opener.OpenerLoader;
 import com.rameses.util.ExceptionManager;
 import com.rameses.util.ValueUtil;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  *
  * @author elmo
  */
 public final class InvokerUtil {
     
     public static void invoke(Invoker invoker) {
         invoke(invoker,null);
     }
     
     public static void invoke(Invoker invoker, Map params) {
         invoke(invoker, params, null);
     }
     
     public static void invoke(Invoker invoker, Map params, Object caller) 
     {
         try 
         {
             if (params == null) params = new HashMap();
             
             ClientContext ctx = ClientContext.getCurrentContext();
             Platform platform = ctx.getPlatform();
             
             String wuId = invoker.getWorkunitid();
             
             ControllerProvider cp = ctx.getControllerProvider();
             UIController u = cp.getController( wuId, (UIController) caller);
             
             Object callee = u.getCodeBean();
             ControlSupport.injectInvoker(callee, callee.getClass(), invoker);
             if ( caller != null ) 
                 ControlSupport.injectCaller(callee, callee.getClass(), caller);
             
             String action = invoker.getAction();
             u.setId( createInvokerId(invoker) );
             u.setName( wuId );
             u.setTitle( invoker.getCaption() );
             
             Object outcome = u.init(params, action);            
             String windowId = u.getId();
             
             //check if window id already exists
             if ( platform.isWindowExists( windowId )) 
             {
                 platform.activateWindow( windowId );
                 return;
             }
             
             String target = (String)invoker.getProperties().get("target");
             if ( target == null ) target = "_window";
             
             if ((target.endsWith("process")||target.endsWith("action"))) 
             {
                 if ( outcome instanceof Opener ) 
                     invoke( (Opener) outcome, u );
             } 
             else 
             {
                 UIControllerContext uic = new UIControllerContext( u );
                 uic.setId(windowId);
                 
                 if ( !ValueUtil.isEmpty(outcome) ) 
                     uic.setCurrentView(outcome.toString());
 
                 UIControllerPanel panel = new UIControllerPanel( uic );
                 Map winParams = new HashMap();
                 
                 if ( invoker.getProperties() != null ) 
                 {
                     Map props = invoker.getProperties();
                     winParams.putAll( props );
                     
                     if ( props.get("alwaysOnTop") != null ) 
                         winParams.put("alwaysOnTop", Boolean.valueOf( props.get("alwaysOnTop")+"") );
                 }
                 
                 winParams.put("id", windowId);
                 winParams.put("title", uic.getTitle());
                 
                 if ( "_popup".equals(target) || "popup".equals(target) ) 
                     platform.showPopup(null, panel, winParams);
                 else 
                     platform.showWindow(null, panel, winParams);
             }
         } 
         catch(Exception ex) 
         {
             Exception e = ExceptionManager.getOriginal(ex);            
             if ( !ExceptionManager.getInstance().handleError(e) ) 
                 ClientContext.getCurrentContext().getPlatform().showError(null, ex);
         }
     }
     
     public static void invoke( Opener opener ) {
         invoke( opener, null );
     }
     
     public static void invoke( Opener opener, UIController caller ) {
         ControlSupport.initOpener(opener, caller);
         
         UIControllerContext uic = new UIControllerContext( opener.getController() );
         UIControllerPanel panel = new UIControllerPanel( uic );
         
         Map winParams = new HashMap();
         if ( opener.getProperties() != null ) {
             winParams.putAll( opener.getProperties() );
         }
         
         winParams.put("id", uic.getId() );
         winParams.put("title", uic.getTitle() );
         String target = opener.getTarget();
         
         if ( "_popup".equals(target) || "popup".equals(target) ) {
             ClientContext.getCurrentContext().getPlatform().showPopup(null, panel, winParams);
         } else {
             ClientContext.getCurrentContext().getPlatform().showWindow(null, panel, winParams);
         }
     }
     
     public static void showWindow(Invoker invoker, String target, Map winParams) {
         if ( !ValueUtil.isEmpty(target) ) {
             invoker.setType(target);
         }
         if ( winParams != null ) {
             invoker.getProperties().putAll(winParams);
         }
         invoke(invoker, null, null);
     }
     
     
     public static Object invokeAction(InvokerAction action) 
     {
         try 
         {
             Invoker inv = action.getInvoker();            
             InvokerParameter invParam = action.getInvokerParam();
 
             String target = (String) inv.getProperties().get("target");
             if ( target == null ) target = "_window";
             
             Map props = null;
             String caption = null;
             if ( invParam !=null ) 
             {
                 props = invParam.getParams(inv);
                 caption = (String) props.remove( "formTitle" );
             }
             
             action.getProperties().put("Action.Invoker", inv); 
             
             if (caption == null) caption = inv.getCaption(); 
             
             if ((target.endsWith("process") || target.endsWith("action"))) 
             { 
                 invoke( inv, props );
                 return null; 
             } 
             else {
                 return createOpener(inv, props, caption);
             }
         } 
         catch(Exception ex) 
         {
             Exception e = ExceptionManager.getOriginal(ex);            
             if ( !ExceptionManager.getInstance().handleError(e) ) 
                 ClientContext.getCurrentContext().getPlatform().showError(null, ex);
 
             return null;
         }
     }
     
     public static List lookupActions(String type) {
         return lookupActions(type, (InvokerParameter) null, null);
     }
     
     public static List lookupActions(String type, InvokerFilter filter) {
         return lookupActions(type, (InvokerParameter) null, filter);
     }
         
     public static List lookupActions(String type, Map params) {
         final Map p = params;
         InvokerParameter inv = new InvokerParameter() {
             public Map getParams(Invoker inv) {
                 return p;
             }
         };
         
         return lookupActions(type, inv, null);
     }
     
     public static List lookupActions(String type, InvokerParameter param) {
         return lookupActions( type, param, null);
     }
     
     public static List lookupActions(String type, InvokerParameter param, InvokerFilter filter) 
     {
         List actions = new ArrayList();
         List invList = lookup(type, null, filter);
         for (Object o: invList) 
         {
             Invoker inv = (Invoker)  o;
             actions.add( createInvokerAction(inv, param) );
         }
         return actions;
     }
     
     private static Action createInvokerAction(Invoker inv, InvokerParameter param) 
     {
         InvokerAction a = new InvokerAction(inv, param);
         
         Map invProps = new HashMap(inv.getProperties());
         a.setName( inv.getAction() );
         a.setCaption( inv.getCaption() );
         if(inv.getIndex() != null) a.setIndex(inv.getIndex());
 
         a.setIcon((String)invProps.remove("icon"));
         a.setImmediate( "true".equals(invProps.remove("immediate")+"") );
         a.setUpdate( "true".equals(invProps.remove("update")+"") );
         a.setVisibleWhen( (String) invProps.remove("visibleWhen") );
         
         String mnemonic = (String) invProps.remove("mnemonic");
         if ( !ValueUtil.isEmpty(mnemonic) ) a.setMnemonic(mnemonic.charAt(0));
         
         Object tooltip = invProps.remove("tooltip");
         if ( !ValueUtil.isEmpty(tooltip) ) a.setTooltip(tooltip+"");
         
         if ( !invProps.isEmpty() ) a.getProperties().putAll( invProps );
 
         a.getProperties().put("Action.Invoker", inv);
         return a;
     }
     
     public static List lookup(String type) {
         return lookup(type,null,null);
     }
     
      public static List lookup(String type, Object obj) {
         return lookup(type,obj,null);
     }
     /*
      * The object passed will be evaluated by the expression.
      * params = refer to Object parameter passed.
      * context = the invocation context.
      * a sample implementation as follows: #{param.name == context.module.name}
      */
     public static List lookup(String type, Object obj, InvokerFilter filter) 
     {
         SessionContext app = OsirisContext.getSession();
         List list = app.getInvokers(type);
 
         ExpressionResolver er = ExpressionResolver.getInstance();        
         List data = new ArrayList();
         for (Object o : list) 
         {
             Invoker inv = (Invoker)o;
             if (filter != null && !filter.accept(inv)) continue;
 
             if (inv.getProperties().get("eval") != null) 
             {
                 Map params = new HashMap();
                 params.put("param", obj);
                 params.put("context", inv);
 
                 String cond = (String)inv.getProperties().get("eval");
                 if ( cond.trim().length() > 0 ) 
                 {
                     boolean b = false;
                     try {
                         b = ((Boolean)er.eval( cond, params)).booleanValue();
                     } catch(Exception ign){;}
 
                     if (b) data.add(inv);
                 }
             } 
             else {
                 data.add(inv);
             }
         }
         return data;
     }
     
     /**
      * returns a list of invokers from a folder path.
      */
     public static List lookupFolder( String name ) {
         if(name == null ) return null;
         if( !name.startsWith("/")) name = "/" + name;
         List invokers = new ArrayList();
         SessionContext app = OsirisContext.getSession();
         List items = (List) app.getFolders(name);
         if(items!=null) {
             for (Object o : items) {
                 Folder f = (Folder) o;
                 if (f.getInvoker() != null) {
                     Invoker v = f.getInvoker();
                     invokers.add(v);
                 }
             }
         }
         return invokers;
     }
     
     /**
      * returns a the first opener based on the invoker type
      */
     public static Opener lookupOpener( String invType ) {
         return lookupOpener(invType, null); 
     }
     
     public static Opener lookupOpener( String invType, Map params ) {
         List<Invoker> list = lookup( invType );
         if ( list.size() ==0 ) {
             throw new RuntimeException("No invokers found for type [" + invType + "]");
         }
         return createOpener(list.get(0), params);
     }
     
     public static List lookupOpeners( String invType) {
         return lookupOpeners( invType, null );
     }
     
     public static List lookupOpeners( String invType, Map params ) {
         List<Invoker> list = lookup( invType );
         if ( list.size() ==0 ) {
             throw new RuntimeException("No invokers found for type [" + invType + "]");
         }
         List openers = new ArrayList();
         for(Invoker inv: list) {
             Opener opener = createOpener(inv, params);
             openers.add(opener);
         }
         return openers;
     }
     
     public static Object invokeOpener( Opener opener ) {
         return invokeOpener( opener, null );
     }
     
     public static Object invokeOpener( Opener opener, UIController caller ) {
         ControlSupport.initOpener(opener, caller, false);
         return ControlSupport.init(opener.getController().getCodeBean(), opener.getParams(), opener.getAction() );
     }
     
     public static Opener createOpener(Invoker inv) {
         return createOpener(inv, null);
     }
     
     public static Opener createOpener(Invoker inv, Map params) {
         return createOpener(inv, params, null);
     }
     
     public static Opener createOpener(Invoker inv, Map params, String caption ) 
     {
         String target = (String)inv.getProperties().get("target");
         Opener opener = new Opener(inv.getWorkunitid());
         if (caption == null) caption = inv.getCaption();
         if (caption == null) caption = inv.getWorkunitid();
         
         opener.setId( createInvokerId(inv) );
         opener.setCaption(caption);
         opener.setAction(inv.getAction());
         
         if ( target !=null ) target = target.replaceAll("^([^_])", "_$1");
         
         opener.setTarget( target );
         if (params != null) opener.setParams( params );
         
         if (inv.getProperties().size() > 0)
             opener.getProperties().putAll( inv.getProperties() );
 
         opener.getProperties().put("_INVOKER_", inv);
         //set this as loader so it can automatically load the handler
         opener.setLoader( new OpenerLoader() {
             public void load(Opener o) {
                 InvokerUtil.invokeOpener( o );
             }
         });        
         return opener;
     }
     
     private static String createInvokerId(Invoker inv) {
         StringBuffer sb = new StringBuffer();
         sb.append( inv.getWorkunitid() );
         
         String id = (String) inv.getProperties().get("id");
         if( id != null && id.trim().length() > 0 ) {
             sb.append("_" + id);
         } else if( inv.getCaption() != null && inv.getCaption().trim().length() > 0 ) {
             sb.append("_" + inv.getCaption());
         }
         return sb.toString();
     }
     
 }
