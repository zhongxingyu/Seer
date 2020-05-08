 package ru.tpu.heartland;
 
 import java.io.IOException;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 public class LoginServlet extends HttpServlet {
 
     protected void doPost(HttpServletRequest request, 
     		HttpServletResponse response)
             	throws ServletException, IOException {
         response.setContentType("text/html;charset=UTF-8");
         
         // Получаем параметры авторизации
         String username = request.getParameter("user");
         String password = request.getParameter("password");
         
         // Проверяем имя пользователя и пароль
         if (username.equals("someuser") 
         		&& password.equals("somepassword")) {
             // если логин и пароль верны, 
         	// получаем ссылку на текущую сессию
             HttpSession session = request.getSession(true);
             // и устанавливаем атрибут user
             session.setAttribute("user", username);
         }
         // перенаправляем запрос на страницу выбора товаров
         RequestDispatcher dispatcher =
                 getServletContext().getRequestDispatcher(
                 		"/mainpage.jsp");
         dispatcher.forward(request, response);
     }
 }
