 package amu.action;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import amu.database.BookDAO;
 import amu.database.PersonalBookListDAO;
 import amu.database.ReviewDAO;
 import amu.model.Book;
 import amu.model.Customer;
 import amu.model.PersonalBookList;
 
 public class AddBookToPersonalBooklistAction implements Action {
 
 	@Override
 	public ActionResponse execute(HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		// TODO Auto-generated method stub
         HttpSession session = request.getSession(true);
         Customer customer = (Customer) session.getAttribute("customer");
 
         if (customer == null) {
             ActionResponse actionResponse = new ActionResponse(ActionResponseType.REDIRECT, "loginCustomer");
             return actionResponse;
         }
 		
         BookDAO bookDAO = new BookDAO();
         Book book = bookDAO.findByISBN(request.getParameter("isbn"));
         
         PersonalBookListDAO personalBookListDAO = new PersonalBookListDAO();
         PersonalBookList personalBookList = personalBookListDAO.findListByID(Integer.parseInt(request.getParameter("list_id")));
         if(book == null){
         	return new ActionResponse(ActionResponseType.FORWARD, "generalErrorMessage");
         }
         if(personalBookList == null){
         	return new ActionResponse(ActionResponseType.FORWARD, "generalErrorMessage");
         }
         
         personalBookListDAO.addBookToList(personalBookList.getId(), book.getId());
         
 		return new ActionResponse(ActionResponseType.REDIRECT, "viewUserBooklists");
 	}
 
 }
