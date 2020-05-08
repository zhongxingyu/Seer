 package com.nclodger.control.action;
 
 import com.nclodger.control.action.access.*;
 import com.nclodger.control.action.admin.*;
 import com.nclodger.control.action.order.OrderFinishAction;
 import com.nclodger.control.action.order.OrderStartAction;
 import com.nclodger.control.action.search.ACDetailsAction;
 import com.nclodger.control.action.search.SearchAction;
 import com.nclodger.control.action.sm.*;
 
 import java.util.HashMap;
 import java.util.Map;
 import javax.servlet.http.HttpServletRequest;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Iaroslav
  * Date: 21.10.13
  * Time: 21:26
  */
 public class ActionFactory {
 
     private static final Map<String,Action> actions;
 
     static{
         actions = new HashMap<String, Action>();
         actions.put("/signup", new SignUpAction());
         actions.put("/signin", new SignInAction());
         actions.put("/signout", new SignOutAction());
         actions.put("/search", new SearchAction());
         actions.put("/smsettings", new SmSettingsAction());
         actions.put("/adsettings", new AdSettingsAction());
         actions.put("/confirmation", new ConfirmationAction());
         actions.put("/smgetallusers", new SmGetAllUsersAction());
         actions.put("/acdetails", new ACDetailsAction());
         actions.put("/orderstart", new OrderStartAction());
         actions.put("/orderfinish", new OrderFinishAction());
         actions.put("/generatepromo", new GeneratePromoAction());
         actions.put("/smsetdiscounts", new SmSetDiscountsAction());
         actions.put("/makevip", new MakeVIPAction());
         actions.put("/makeunvip", new MakeUnvipAction());
         actions.put("/showmostpopularhotel", new PopHotelsAction());
         actions.put("/getallpromocodes", new SmGetAllPromoCodes());
         actions.put("/initialdiscounts", new AdInitialDiscountsAction());
         actions.put("/admingetallusers", new AdminGetAllUsersAction());
         actions.put("/makeblock", new MakeBlockAction());
         actions.put("/changepswd", new ChangePswdAction());
         actions.put("/makeunblock", new MakeUnBlockAction());
         actions.put("/grantsm", new GrantToSMAction());
         actions.put("/dismisssm", new DismissSMAction());
         actions.put("/deleteuser", new AdminDeleteUserAction());
         actions.put("/saveexcel",new SmSaveExcelAction());
         actions.put("/saveaccexcel",new SmSaveAccExcelAction());
         actions.put("/showmostvaluableacc",new SmGetValueOfAccAction());
         actions.put("/viewpastbooking",new ViewPastBookingAction());
         actions.put("/occupyhotel",new OccupyHotelAction());
         actions.put("/disposehotel",new DisposeHotelAction());
         actions.put("/aboutus", new AboutUsAction());
         actions.put("/contacts", new ContactsAction());
         actions.put("/openid", new OpenIDAccessAction());
 
     }
 
     public static Action getAction(HttpServletRequest request) {

       // return actions.get(request.getServletPath());
        String actname = request.getPathInfo();
        return actions.get(actname);
     }
 }
