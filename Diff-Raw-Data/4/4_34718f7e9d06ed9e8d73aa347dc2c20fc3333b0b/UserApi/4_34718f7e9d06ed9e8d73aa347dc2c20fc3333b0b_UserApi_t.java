 package com.osastudio.newshub.net;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONObject;
 
 import android.content.Context;
 
 import com.osastudio.newshub.data.NewsResult;
 import com.osastudio.newshub.data.user.CityDistrictList;
 import com.osastudio.newshub.data.user.CityList;
 import com.osastudio.newshub.data.user.QualificationList;
 import com.osastudio.newshub.data.user.RegisterParameters;
 import com.osastudio.newshub.data.user.SchoolClasslist;
 import com.osastudio.newshub.data.user.SchoolList;
 import com.osastudio.newshub.data.user.SchoolTypeList;
 import com.osastudio.newshub.data.user.SchoolYearlist;
 import com.osastudio.newshub.data.user.UserInfoList;
 import com.osastudio.newshub.data.user.ValidateResult;
 import com.osastudio.newshub.library.ChecksumHelper;
 
 public class UserApi extends NewsBaseApi {
 
    private static final String TAG = "UserApi";
 
    private static final String KEY_BIRTHDAY = "birthday";
    private static final String KEY_CITY_ID = "cityID";
    private static final String KEY_CLASS_ID = "classID";
    private static final String KEY_DISTRICT_ID = "areaID";
    private static final String KEY_GENDER = "sex";
    private static final String KEY_QUALIFICATION = "xueli";
    private static final String KEY_SCHOOL_ID = "schoolID";
    private static final String KEY_SCHOOL_TYPE = "school_class";
    private static final String KEY_USER_NAME = "studentName";
    private static final String KEY_VALIDATE_CODE = "serial";
    private static final String KEY_YEAR_ID = "yearID";
 
    public static ValidateResult getValidateStatus(Context context) {
       List<NameValuePair> params = new ArrayList<NameValuePair>();
       params.add(new BasicNameValuePair(KEY_DEVICE_ID, getDeviceId(context)));
       JSONObject jsonObject = getJsonObject(getValidateStatusService(), params);
       return (jsonObject != null) ? new ValidateResult(jsonObject) : null;
    }
 
    public static ValidateResult validate(Context context, String validateCode) {
       List<NameValuePair> params = new ArrayList<NameValuePair>();
       params.add(new BasicNameValuePair(KEY_DEVICE_ID, getDeviceId(context)));
       params.add(new BasicNameValuePair(KEY_DEVICE_TYPE, getDeviceType()));
       params.add(new BasicNameValuePair(KEY_VALIDATE_CODE, ChecksumHelper
             .toMD5(validateCode)));
       JSONObject jsonObject = getJsonObject(validateService(), params);
       return (jsonObject != null) ? new ValidateResult(jsonObject) : null;
    }
 
    public static UserInfoList getUserInfoList(Context context) {
       List<NameValuePair> params = new ArrayList<NameValuePair>();
       params.add(new BasicNameValuePair(KEY_DEVICE_ID, getDeviceId(context)));
       JSONObject jsonObject = getJsonObject(getUserInfoListService(), params);
       return (jsonObject != null) ? new UserInfoList(jsonObject) : null;
    }
 
    public static NewsResult registerUser(Context context,
          RegisterParameters register) {
       List<NameValuePair> params = new ArrayList<NameValuePair>();
       params.add(new BasicNameValuePair(KEY_DEVICE_ID, getDeviceId(context)));
       params.add(new BasicNameValuePair(KEY_SCHOOL_ID, register.schoolId));
       params.add(new BasicNameValuePair(KEY_YEAR_ID, register.yearId));
       params.add(new BasicNameValuePair(KEY_CLASS_ID, register.classId));
       params.add(new BasicNameValuePair(KEY_USER_NAME, register.userName));
       params.add(new BasicNameValuePair(KEY_GENDER, register.gender));
       params.add(new BasicNameValuePair(KEY_BIRTHDAY, register.birthday));
       params.add(new BasicNameValuePair(KEY_QUALIFICATION,
             register.qualification));
       JSONObject jsonObject = getJsonObject(registerService(), params);
       return (jsonObject != null) ? new NewsResult(jsonObject) : null;
    }
 
    public static NewsResult addUser(Context context, RegisterParameters register) {
       List<NameValuePair> params = new ArrayList<NameValuePair>();
       params.add(new BasicNameValuePair(KEY_DEVICE_ID, getDeviceId(context)));
       params.add(new BasicNameValuePair(KEY_SCHOOL_ID, register.schoolId));
       params.add(new BasicNameValuePair(KEY_YEAR_ID, register.yearId));
       params.add(new BasicNameValuePair(KEY_CLASS_ID, register.classId));
       params.add(new BasicNameValuePair(KEY_USER_NAME, register.userName));
       params.add(new BasicNameValuePair(KEY_GENDER, register.gender));
       params.add(new BasicNameValuePair(KEY_BIRTHDAY, register.birthday));
       JSONObject jsonObject = getJsonObject(registerService(), params);
       return (jsonObject != null) ? new NewsResult(jsonObject) : null;
    }
 
    public static CityList getCityList(Context context) {
       JSONObject jsonObject = getJsonObject(getCityListService(), null);
       return (jsonObject != null) ? new CityList(jsonObject) : null;
    }
 
    public static CityDistrictList getCityDistrictList(Context context,
          String cityId) {
       List<NameValuePair> params = new ArrayList<NameValuePair>();
       params.add(new BasicNameValuePair(KEY_CITY_ID, cityId));
       JSONObject jsonObject = getJsonObject(getCityDistrictListService(),
             params);
       return (jsonObject != null) ? new CityDistrictList(jsonObject) : null;
    }
 
    public static SchoolTypeList getSchoolTypeList(Context context) {
       JSONObject jsonObject = getJsonObject(getSchoolTypeListService(), null);
       return (jsonObject != null) ? new SchoolTypeList(jsonObject) : null;
    }
 
    public static SchoolList getSchoolList(Context context, String districtId,
          String schoolType) {
       List<NameValuePair> params = new ArrayList<NameValuePair>();
       params.add(new BasicNameValuePair(KEY_DISTRICT_ID, districtId));
       params.add(new BasicNameValuePair(KEY_SCHOOL_TYPE, schoolType));
       JSONObject jsonObject = getJsonObject(getSchoolListService(), params);
       return (jsonObject != null) ? new SchoolList(jsonObject) : null;
    }
 
    public static SchoolYearlist getSchoolYearList(Context context,
          String schoolId) {
       List<NameValuePair> params = new ArrayList<NameValuePair>();
       params.add(new BasicNameValuePair(KEY_SCHOOL_ID, schoolId));
       JSONObject jsonObject = getJsonObject(getSchoolYearListService(), params);
       return (jsonObject != null) ? new SchoolYearlist(jsonObject) : null;
    }
 
    public static SchoolClasslist getSchoolClassList(Context context,
          String yearId) {
       List<NameValuePair> params = new ArrayList<NameValuePair>();
       params.add(new BasicNameValuePair(KEY_YEAR_ID, yearId));
       JSONObject jsonObject = getJsonObject(getSchoolClassListService(), params);
       return (jsonObject != null) ? new SchoolClasslist(jsonObject) : null;
    }
 
   public static QualificationList getQualificationList(Context context) {
      JSONObject jsonObject = getJsonObject(getQualificationListService(), null);
       return (jsonObject != null) ? new QualificationList(jsonObject) : null;
    }
 
 }
