 package controller;
 
 import manager.*;
 import model.*;
 
 import javax.ejb.EJB;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebInitParam;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import java.io.IOException;
 import java.util.List;
 
 @WebServlet(name = "ResultCalculator", urlPatterns = {"/results"},
         initParams =
                 {
                         @WebInitParam(name = "success", value = "WEB-INF/results.jsp"),
                         @WebInitParam(name = "failure", value = "WEB-INF/aspirations.jsp")})
 public class ResultCalculator extends HttpServlet
 {
     @EJB
     CarManager carManager;
     @EJB
     DebtTypeManager debtTypeManager;
     @EJB
     DegreeManager degreeManager;
     @EJB
     HousingManager housingManager;
     @EJB
     MiscManager miscManager;
     @EJB
     RegionManager regionManager;
 
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
     {
 //        for(String s : new String[]{
 //                "quarter",
 //                "year",
 //                "degree",
 //                "extra-quarter-full",
 //                "extra-quarter-part",
 //                "scholarships",
 //                "interestrate",
 //                "loan-percent"}){
 //            session.setAttribute(s, request.getAttribute(s));
 //        }
         HttpSession session = request.getSession();
 
         //School
         String startMonth = (String)session.getAttribute("quarter");
         Integer startYear = Integer.parseInt((String)session.getAttribute("year"));
         Region region = regionManager.findRegion((String)request.getParameter("region"));
         List<Degree> degrees = degreeManager.getDegrees();
         Degree degree = new Degree();
 
         for(int i=0;i<degrees.size();i++)
         {
             if(degrees.get(i).getDegree().equals(session.getAttribute("degree")) &&
                degrees.get(i).getRegion().equals(request.getParameter("region")))
             {
                 degree = degrees.get(i);
             }
         }
 
         Integer expectedFull = Integer.parseInt((String)session.getAttribute("extra-quarter-full"));
         Integer expectedPart = Integer.parseInt((String)session.getAttribute("extra-quarter-part"));
         Integer loanPercent = Integer.parseInt((String)session.getAttribute("loan-percent"));
         Double scholarships = Double.parseDouble((String)session.getAttribute("scholarships"));
         Integer interestRate = Integer.parseInt((String)session.getAttribute("interestrate"));
         Double debt1 = Double.parseDouble((String)session.getAttribute("debt1"));
         Double debt2 = Double.parseDouble((String)session.getAttribute("debt2"));
         Double debt3 = Double.parseDouble((String)session.getAttribute("debt3"));
 
         //Housing & Lifestyle
         Double rent = Double.parseDouble((String) session.getAttribute("rent"));
         Double utilities = Double.parseDouble((String)session.getAttribute("utilities"));
         Double lunch = Double.parseDouble((String)session.getAttribute("go_out_to_lunch"));
         Double dinner = Double.parseDouble((String)session.getAttribute("go_out_to_dinner"));
         Double entertainment = Double.parseDouble((String)session.getAttribute("spend_on_entertainment"));
 
         //Post-Graduation
         List<Car> allCars = carManager.getCars();
         Car car = new Car();
         for(int i=0;i<allCars.size();i++)
         {
             if(allCars.get(i).getStatus().equals(request.getParameter("car_status")) &&
                allCars.get(i).getQuality().equals(request.getParameter("car_quality")) &&
                allCars.get(i).getMpg().equals(request.getParameter("fuel_economy")))
             {
                 car = allCars.get(i);
             }
         }
         Double regionHousing;
         if(request.getParameter("housing").equals("Own"))
         {
             regionHousing = region.getHousing();
         }
         else if(request.getParameter("housing").equals("Rent"))
         {
             regionHousing = region.getRent();
         }
         else
         {
             regionHousing = 0.0;
         }
 
         Double carInterest = Double.parseDouble((String)request.getParameter("carinterest"));
 
         Double salary;
         Double studentloan;
         Double incometax;
         Double miscexpenses;
         Double carexpenses;
         Double mortgage;
         Double miscloans;
         Double savings;
         Double discretionary;
 
         //Salary
         salary = degree.getSalary() / 12;
 
         //Student loan
         if(loanPercent == 0)
         {
             studentloan = 0.0;
         }
         else
         {
             Double totalprice = degree.getDuration() * 7200.0;
             Double totalrent = rent / 4.34812 * 100;
             Double totalutilities = utilities / 4.34812 * 100;
             totalprice += (totalrent + totalutilities);
             Double totalloan = totalprice * (loanPercent / 100);
             Double annualrate = interestRate / 1200.0;
            Double payment = annualrate + (annualrate / (Math.pow(annualrate + 1, 120) - 1)) * totalloan;
             studentloan = payment;
         }
 
 //        //Income tax
 //        Double firstBracket = 9000 * 0.1;
 //        Double secondBracket = (36000 - 9000) * 0.15;
 //        Double thirdBracket = (degree.getSalary() - 36000) * .25;
 //        incometax = firstBracket + secondBracket + thirdBracket;
 //        incometax = incometax / 12;
 //
 //        // Misc Expenses
 //        Double lunchMonth = lunch * 4.34812;
 //
 //        //Car Expenses High end, Average, Below average
 //        String mpgString = car.getMpg();
 //        Double mpg = 0.0;
 //        if(mpgString.equals("High end"))
 //        {
 //            mpg = 37.0;
 //        }
 //        else if(mpgString.equals("Average"))
 //        {
 //            mpg = 27.5;
 //        }
 //        else
 //        {
 //            mpg = 14.0;
 //        }
 //        Double gasMonth = 1600 / mpg * 3.6;
 //        Double annualCarInterest = carInterest / 1200;
 //        Double carPayment = annualCarInterest + (annualCarInterest / (Math.pow(annualCarInterest + 1, 48) - 1)) * car.getPrice();
 //        carexpenses = gasMonth + carPayment;
 
         //Mortgage/Rent
 //        mortgage = housing.getRent() + housing.getUtilities();
 
         //Misc. Loans
         //to be determined
         miscloans = 0.0;
 
         //Savings Act Contribution
         //to be determined
         savings = 0.0;
 
 //        discretionary = salary - (studentloan + incometax + miscexpenses + carexpenses + mortgage + miscloans + savings);
 
         session.setAttribute("salary", salary);
         session.setAttribute("studentloan", studentloan);
 //        session.setAttribute("incometax", incometax);
 //        session.setAttribute("miscexpenses", miscexpenses);
 //        session.setAttribute("carexpenses", carexpenses);
 //        session.setAttribute("mortgage", mortgage);
 //        session.setAttribute("miscloans", miscloans);
 //        session.setAttribute("savings", savings);
 //        session.setAttribute("discretionary", discretionary);
 
 
 
 
 
 
 //        <body>
 //            Salary.....................${salary}<br>
 //            Student Loan...............${studentloan}<br>
 //            Income Tax.................${incometax}<br>
 //            Misc. Expenses.............${miscexpenses}<br>
 //            Car Expenses...............${carexpenses}<br>
 //            Mortgage/Rent..............${mortgage}<br>
 //            Misc. Loans................${miscloans}<br>
 //            Savings Act Contribution...${savings}<br>
 //        <br>
 //        <br>
 //            Discretionary Income: ${discretionary}
 //        </body>
 
 
 
         RequestDispatcher dispatcher = request.getRequestDispatcher("WEB-INF/results.jsp");
         dispatcher.forward(request, response);
 
     }
 
 }
