 package com.rameses.rcp.impl;
 
 import com.rameses.platform.interfaces.Platform;
 import com.rameses.rcp.common.*;
 import com.rameses.rcp.util.ControlSupport;
 import com.rameses.rcp.common.Opener;
 import com.rameses.rcp.framework.*;
 import com.rameses.rcp.ui.UIControl;
 import com.rameses.util.ValueUtil;
 import java.util.*;
 import javax.swing.JComponent;
 
 /**
  * @author jaycverg
  * handles page navigation using the actions outcome
  */
 public class NavigationHandlerImpl implements NavigationHandler {
     
     
     public void navigate(NavigatablePanel panel, UIControl source, Object outcome) {
         if ( panel == null ) return;
         
         JComponent sourceComp = (JComponent) source;
         ClientContext ctx = ClientContext.getCurrentContext();
         Platform platform = ctx.getPlatform();
         
         Stack<UIControllerContext> conStack = panel.getControllers();
         UIControllerContext curController = conStack.peek();
         
         if ( ValueUtil.isEmpty(outcome) ) {
             //if outcome is null or empty just refresh the current view
             curController.getCurrentView().refresh();
             
         }
         else {
             //-- process Opener outcome
             if( outcome instanceof Opener )  {
                 Opener opener = (Opener) outcome;
                 opener = ControlSupport.initOpener( opener, curController.getController() );
                 
                 String opTarget = opener.getTarget()+"";
                if (opTarget.matches("process|_process")) return;
                
                if (opTarget.startsWith("_")) 
                     opTarget = opTarget.substring(1);

                 boolean self = !opTarget.matches("window|popup|floating");
                 String windowId = opener.getController().getId();
 
                 if ( !self && platform.isWindowExists( windowId ) ) {
                     platform.activateWindow( windowId );
                     return;
                 }
                 
                 UIController opCon = opener.getController();
                 String permission = opener.getPermission();
                 String role = opener.getRole();
                 String domain = opener.getDomain();
                 
                 //check permission(if specified) if allowed
                 if ( !ValueUtil.isEmpty(permission) ) {
                     permission = opCon.getName() + "." + permission;
                     if( !ControlSupport.isPermitted(domain, role, permission) ) {
                         MsgBox.err("You don't have permission to perform this transaction.");
                         return;
                     }
                     
                 }
                 
                 UIControllerContext controller = new UIControllerContext(opCon);
                 
                 //check if opener has outcome
                 if ( !ValueUtil.isEmpty(opener.getOutcome()) ) {
                     if ( "_close".equals( opener.getOutcome()) ) {
                         curController.getCurrentView().refresh();
                         return;
                     }
                     //check if controller has a page reolver
                     controller.setCurrentView( opener.getOutcome() );
                 }
                 
                 if ( self ) {
                     conStack.push(controller);
                 } 
                 else {
                     UIControllerPanel uic = new UIControllerPanel(controller);
                     
                     Map props = new HashMap();
                     if ( opener.getProperties().size() > 0 ) {
                         props.putAll( opener.getProperties() );
                     }
                                         
                     props.put("id", windowId);
                     props.put("title", controller.getTitle() );
                     props.put("modal", opener.isModal());
                     
                     if ( "popup".equals(opTarget) ) {
                         platform.showPopup(sourceComp, uic, props);
                     } else if ( opener instanceof FloatingOpener ) {
                         FloatingOpener fo = (FloatingOpener) opener;
                         JComponent owner = (JComponent) source.getBinding().find( fo.getOwner() );
                         if ( !ValueUtil.isEmpty(fo.getOrientation()) ) 
                             props.put("orientation", fo.getOrientation());
                         
                         platform.showFloatingWindow(owner, uic, props);
                     } else {
                         platform.showWindow(sourceComp, uic, props);
                     }
                     return;
                 }
                 
             }
             //-- process String outcome
             else {
                 String out = outcome+"";
                 if ( out.startsWith("_close") ) {
                     if ( !conStack.isEmpty() ) {
                         if ( conStack.size() > 1 ) {
                             conStack.pop();
                             
                             if( out.contains(":") ) {
                                 out = out.substring(out.indexOf(":")+1);
                                 navigate(panel, source, out);
                                 return;
                             }
                             
                         } else {
                             String conId = curController.getId();
                             platform.closeWindow(conId);
                         }
                     }
                     
                 } else if ( out.startsWith("_exit")) {
                     //get the original owner of he window
                     while ( conStack.size() > 1 ) {
                         conStack.pop();
                     }
                     String conId = conStack.peek().getId();
                     platform.closeWindow(conId);
                     
                 } else if ( out.startsWith("_root") ) {
                     //get the original owner of he window
                     while ( conStack.size() > 1 ) {
                         conStack.pop();
                     }
                     if( out.contains(":") ) {
                         out = out.substring(out.indexOf(":")+1);
                         navigate(panel, source, out);
                         return;
                     }
                     
                 } else {
                     if ( out.startsWith("_") ) out = out.substring(1);
                     curController.setCurrentView( out );
                     
                     //update binding injections based on current view
                     curController.getCurrentView().getBinding().reinjectAnnotations();
                 }
             }
             
             //refresh new view
             panel.renderView();
         }
     }
     
 }
