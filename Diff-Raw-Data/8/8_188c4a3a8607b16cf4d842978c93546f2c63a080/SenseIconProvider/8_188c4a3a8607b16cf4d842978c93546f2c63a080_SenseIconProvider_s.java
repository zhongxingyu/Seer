 package nl.sense_os.commonsense.client.common.utility;
 
 import java.util.List;
 import java.util.logging.Logger;
 
 import nl.sense_os.commonsense.client.common.constants.Constants;
 import nl.sense_os.commonsense.client.common.models.DeviceModel;
 import nl.sense_os.commonsense.client.common.models.GroupModel;
 import nl.sense_os.commonsense.client.common.models.SensorModel;
 import nl.sense_os.commonsense.client.common.models.UserModel;
 
 import com.extjs.gxt.ui.client.Registry;
 import com.extjs.gxt.ui.client.data.ModelIconProvider;
 import com.extjs.gxt.ui.client.data.TreeModel;
 import com.extjs.gxt.ui.client.util.IconHelper;
 import com.google.gwt.user.client.ui.AbstractImagePrototype;
 
 public class SenseIconProvider<M extends TreeModel> implements ModelIconProvider<M> {
 
     private static final Logger LOGGER = Logger.getLogger(SenseIconProvider.class.getName());
 
    public static final String GXT_ICONS_PATH = "gxt/images/gxt/icons/";
     public static final String SENSE_ICONS_PATH = "img/icons/16/";
 
     public static final AbstractImagePrototype ICON_DEVICE = IconHelper.create(SENSE_ICONS_PATH
             + "phone_Android.png");
     public static final AbstractImagePrototype ICON_GROUP = IconHelper.create(SENSE_ICONS_PATH
             + "group.png");
     public static final AbstractImagePrototype ICON_GOOGLE = IconHelper.create(SENSE_ICONS_PATH
             + "google.gif");
     public static final AbstractImagePrototype ICON_GXT_DONE = IconHelper.create(GXT_ICONS_PATH
             + "done.gif");
     public static final AbstractImagePrototype ICON_GXT_FOLDER = IconHelper.create(GXT_ICONS_PATH
             + "folder.gif");
     public static final AbstractImagePrototype ICON_GXT_LEAF = IconHelper.create(GXT_ICONS_PATH
             + "tabs.gif");
     public static final AbstractImagePrototype ICON_SENSOR_DEVICE = IconHelper
             .create(SenseIconProvider.SENSE_ICONS_PATH + "sense_red-unshared.gif");
     public static final AbstractImagePrototype ICON_SENSOR_DEVICE_SHARED = IconHelper
             .create(SenseIconProvider.SENSE_ICONS_PATH + "sense_red-shared.gif");
     public static final AbstractImagePrototype ICON_SENSOR_ENVIRONMENT = IconHelper
             .create(SenseIconProvider.SENSE_ICONS_PATH + "sense_orange-unshared.gif");
     public static final AbstractImagePrototype ICON_SENSOR_FEED = IconHelper
             .create(SenseIconProvider.SENSE_ICONS_PATH + "sense_orange-unshared.gif");
     public static final AbstractImagePrototype ICON_SENSOR_PUBLIC = IconHelper
             .create(SenseIconProvider.SENSE_ICONS_PATH + "sense_black-unshared.gif");
     public static final AbstractImagePrototype ICON_SENSOR_STATE = IconHelper
             .create(SenseIconProvider.SENSE_ICONS_PATH + "sense_magenta-unshared.gif");
     public static final AbstractImagePrototype ICON_SENSOR_ENVIRONMENT_SHARED = IconHelper
             .create(SenseIconProvider.SENSE_ICONS_PATH + "sense_orange-shared.gif");
     public static final AbstractImagePrototype ICON_SENSOR_FEED_SHARED = IconHelper
             .create(SenseIconProvider.SENSE_ICONS_PATH + "sense_orange-shared.gif");
     public static final AbstractImagePrototype ICON_SENSOR_PUBLIC_SHARED = IconHelper
             .create(SenseIconProvider.SENSE_ICONS_PATH + "sense_black-shared.gif");
     public static final AbstractImagePrototype ICON_SENSOR_STATE_SHARED = IconHelper
             .create(SenseIconProvider.SENSE_ICONS_PATH + "sense_magenta-shared.gif");
     public static final AbstractImagePrototype ICON_USER = IconHelper.create(SENSE_ICONS_PATH
             + "user.png");
     public static final AbstractImagePrototype ICON_USER_ME = IconHelper.create(SENSE_ICONS_PATH
             + "user_zorro.png");
 
     public static final AbstractImagePrototype ICON_LOADING = IconHelper
            .create("gxt/images/gxt/icons/loading.gif");
     public static final AbstractImagePrototype ICON_BUTTON_GO = IconHelper
            .create("gxt/images/gxt/icons/page-next.gif");
 
     @Override
     public AbstractImagePrototype getIcon(M model) {
 
         if (model instanceof GroupModel) {
             return ICON_GROUP;
 
         } else if (model instanceof DeviceModel) {
             return ICON_DEVICE;
 
         } else if (model instanceof SensorModel) {
             SensorModel sensor = (SensorModel) model;
 
             List<UserModel> users = sensor.getUsers();
             UserModel currentUser = Registry.get(Constants.REG_USER);
             int type = sensor.getType();
             if (users != null
                     && (users.size() > 1 || (!users.contains(currentUser) && users.size() > 0))) {
                 switch (type) {
                     case 0 :
                         return ICON_SENSOR_FEED_SHARED;
                     case 1 :
                         return ICON_SENSOR_DEVICE_SHARED;
                     case 2 :
                         return ICON_SENSOR_STATE_SHARED;
                     case 3 :
                         return ICON_SENSOR_PUBLIC_SHARED;
                     case 4 :
                         return ICON_SENSOR_ENVIRONMENT_SHARED;
                     default :
                         return ICON_GXT_LEAF;
                 }
             } else {
                 switch (type) {
                     case 0 :
                         return ICON_SENSOR_FEED;
                     case 1 :
                         return ICON_SENSOR_DEVICE;
                     case 2 :
                         return ICON_SENSOR_STATE;
                     case 3 :
                         return ICON_SENSOR_PUBLIC;
                     case 4 :
                         return ICON_SENSOR_ENVIRONMENT;
                     default :
                         return ICON_GXT_LEAF;
                 }
             }
 
         } else if (model instanceof UserModel) {
             final UserModel me = Registry.<UserModel> get(Constants.REG_USER);
             if (model.equals(me)) {
                 return ICON_USER_ME;
             } else {
                 return ICON_USER;
             }
 
         } else {
             LOGGER.severe("Unexpected model class: " + model);
             return ICON_GXT_DONE;
         }
     }
 }
