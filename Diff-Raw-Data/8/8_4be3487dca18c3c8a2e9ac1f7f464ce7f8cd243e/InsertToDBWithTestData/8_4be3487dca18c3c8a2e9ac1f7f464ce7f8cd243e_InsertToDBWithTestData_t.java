 package org.young.db;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 
 import java.util.Date;
 import java.util.Random;
 
import org.young.util.DateUtil;

 
 /**
  * 生成向数据库中插入一些数据的SQL
  *
  * @author by Young.ZHU on 2012-6-29
  *
  *         org.young.InsertToDBWithTestData.java
  */
 public class InsertToDBWithTestData {
     //~ Static fields/initializers *********************************************
 
     private static final int  DATA_COUNT     = 100;
 
     /*
      * “姓”的字符数组
      */
     private static String[]   FIRST_NAME_ARR = {
             "赵",
             "钱",
             "孙",
             "李",
             "王",
             "张",
             "慕容",
             "上官",
             "东方",
             "朱"
         };
 
     /*
      * 其他中文数组
      */
     private static String[]   LAST_NAME_ARR  = {
             "燕",
             "岩",
             "艳",
             "明",
             "建国",
             "小刚",
             "小娟",
             "华",
             "天明",
             "洋",
             "盈盈",
             "仁"
         };
 
     /*
      * 性别
      */
     private static String[]   GENDER_ARR     = {
             "男",
             "女"
         };
 
     /*
      * 插入员工表（T_EMPLOYEE）的SQL
      */
     private static String     SQL_EMPLOYEE   = "INSERT INTO T_EMPLOYEE(F_NAME, F_GENDER, F_BIRTHDAY) " +
         "VALUES ('V_NAME', 'V_GENDER', 'V_BIRTHDAY');";
 
     //~ Methods ****************************************************************
 
     public static void generateEmployeeSQL() {
         for (int i = 0; i < DATA_COUNT; i++) {
             String firstName = getRandomCharFromArray(FIRST_NAME_ARR);
             String lastName  = getRandomCharFromArray(LAST_NAME_ARR);
             String name      = firstName + lastName;
 
             String sql = SQL_EMPLOYEE.replace("V_NAME", name);
             sql     = sql.replace("V_GENDER",
                     GENDER_ARR[new Random().nextInt(1000) % 2]);
             sql     = sql.replace("V_BIRTHDAY", getRandomDate());
 
             System.out.println(sql);
         }
     }
 
     /**
      * 随机生成一个比当前日期小的日期
      * 
      * @return （yyyy年MM月dd日）
      */
     private static String getRandomDate() {
         Date date     = new Date();
         long dateMill = date.getTime();
 
         Random random = new Random();
         dateMill = (long) (random.nextDouble() * dateMill);
 
        return DateUtil.getFormatDate("yyyy-MM-dd", new Date(dateMill));
     }
 
 
     private static String getRandomCharFromArray(String[] arr) {
         Random random = new Random();
         int    index  = random.nextInt(arr.length);
 
         return arr[index];
     }
 }
