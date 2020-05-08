 package Perls_Package;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLEncoder;
 
 public class ManagerDB {
     private final String setScriptURL = "http://lolperl.zz.mu/insert_perl_234b_658z_2.php";
 
     public String setDB(String perl, String author){
        HttpURLConnection conn = null;
        String result="";
        String param;
         try {
             param = "perl=" +
                     URLEncoder.encode(perl, "UTF-8") +
                     "&author=" + URLEncoder.encode(author, "UTF-8");
         } catch (UnsupportedEncodingException ex) {
             return "Ошибка преобразования строки о_О";
         }
 
         try {
             URL url = new URL(setScriptURL);
             conn = (HttpURLConnection) url.openConnection();
             conn.setRequestMethod("POST");
             conn.setRequestProperty("User-Agent", "Java bot " + author);
             conn.setUseCaches (false);
             conn.setDoInput(true);
             conn.setDoOutput(true);
 
             try (DataOutputStream wr = new DataOutputStream (
                     conn.getOutputStream ())) {
                 wr.writeBytes (param);
                 wr.flush ();
             } catch(Exception e) {
                 return "Невозможно отправить запрос, Не добавлено!";
             }
 
             // Проверка кода ответа
             int code=conn.getResponseCode();
             if (code==200) {
                 BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                 String inputLine;
                 while ((inputLine = in.readLine()) != null) {
                     result+=inputLine;
                 }
                 in.close();
             } else {
                // Тут обработаем код ошибки серевра
                 return "Ошибка сервера " + code + ". Не добавлено!";
             }
         } catch (IOException e) {
             e.printStackTrace();
             // Тут обрабатЫваем отсутствие инета, недоступность хоста и т.д.
             return "Нет соединения с сервером, не добавлено!";
         } finally {
             if(conn != null) {
                 conn.disconnect();
                 conn = null;
             }
         }
         return result;
     }
 }
