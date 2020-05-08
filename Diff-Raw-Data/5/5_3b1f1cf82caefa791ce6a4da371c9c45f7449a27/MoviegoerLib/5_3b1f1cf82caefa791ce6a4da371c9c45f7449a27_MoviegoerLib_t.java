 package Moblima;
 
 import java.util.*;
 import java.io.*;
 
 class MoviegoerLib {
     private LinkedList<Moviegoer> goerLib;
 
     public MoviegoerLib() {
         goerLib = new LinkedList<Moviegoer>();
         String fileAddress = ".\\src\\Moblima\\goerLib.txt";
         try {
             Properties properties = new Properties();
             properties.load(new FileInputStream(fileAddress));
 
            int size = Integer.parseInt(properties.getProperty("libSize"));
            for (int i = 0; i < size; i++) {
                 Moviegoer newGoer = new Moviegoer("a", "b", "c", "d", "e", 0);
                 newGoer.setUsername(properties.getProperty("username_" + i));
                 newGoer.setPassword(properties.getProperty("password_" + i));
                 newGoer.setName(properties.getProperty("name_" + i));
                 newGoer.setMobileNumber(properties.getProperty("mobileNumber_" + i));
                 newGoer.setEmailAddress(properties.getProperty("emailAddress_" + i));
                 newGoer.setAge(Integer.parseInt(properties.getProperty("age_" + i)));
                 goerLib.add(newGoer);
             }
         } catch (Exception e) {
             System.out.println("Unable to process " + fileAddress);
         }
     }
     public static Scanner sc = new Scanner(System.in);
     
     public void backup() {
         String fileAddress = ".\\src\\Moblima\\goerLib.txt";
         try {
             Properties properties = new Properties();
             properties.load(new FileInputStream(fileAddress));
 
             for (int i = 0; i < goerLib.size(); i++) {
                 properties.setProperty("username_" + i, goerLib.get(i).getUsername());
                 properties.setProperty("password_" + i, goerLib.get(i).getPassword());
                 properties.setProperty("name_" + i, goerLib.get(i).getName());
                 properties.setProperty("mobileNumber_" + i, goerLib.get(i).getMobileNumber());
                 properties.setProperty("emailAddress_" + i, goerLib.get(i).getEmailAddress());
                 properties.setProperty("age_" + i, goerLib.get(i).getAge().toString());
             }
            properties.setProperty("libSize", Integer.toString(goerLib.size()));
             properties.store(new FileOutputStream(fileAddress), "");
         } catch (Exception e) {
             System.out.println("Unable to process " + fileAddress);
         }
     }
     
     public Moviegoer add() {
         //String username, String password, String name, Sting mobileNumber, String emailAddress, Integer age
 
         System.out.println("username: ");
         String newUsername = sc.next();
 
         System.out.println("password: ");
         String newPassword  = sc.next();
 
         sc.nextLine();
         System.out.println("name: ");
         String newName = sc.nextLine();
 
         System.out.println("mobile number: ");
         String newMobileNumber = sc.next();
 
         System.out.println("email address: ");
         String newEmailAddress = sc.next();
 
         System.out.println("age: ");
         Integer newAge =sc.nextInt();
 
         System.out.print("confirm? 1/0");
         Moviegoer newGoer;
         if (sc.nextInt() == 1) {
             newGoer = new Moviegoer(
                                         newUsername, 
                                         newPassword, 
                                         newName, 
                                         newMobileNumber,
                                         newEmailAddress,
                                         newAge);
             goerLib.add(newGoer);
         }
         else
             newGoer = null;
         
         return newGoer;
     }
 
     public boolean delete(Moviegoer goer) {
         System.out.println("password: ");
         String tempPassword  = sc.next();
         if (goer.getPassword().compareTo(tempPassword) == 0) {
             int i = goerLib.indexOf(goer);
             if (goerLib.indexOf(goer) != -1) {
                 goerLib.remove(i);
                 return true;
             }
         }
 
         return false;
     }
 
     public static boolean modify(Moviegoer goer, int choice, Cineplex cLib[]) {
         
         switch (choice) {
             case 1:
                 System.out.print("New name: ");
                 goer.setName(sc.nextLine());
                 break;
             case 2:
                 System.out.print("New mobile number: ");
                 goer.setMobileNumber(sc.next());
                 break;
             case 3:
                 System.out.print("New email address: ");
                 goer.setEmailAddress(sc.next());
                 break;
             case 4:
                 System.out.print("New age: ");
                 goer.setAge(sc.nextInt());
                 break;
             default:
                 System.out.println("invalid. again: ");
                 return false;
         }
         return true;
     }
 
     public Moviegoer checkLogin(String nameTry, String pwdTry) {
         ListIterator<Moviegoer> iter = goerLib.listIterator(0);
         Moviegoer temp = null;
         while (iter.hasNext()) {
             temp = iter.next();
             if (temp.getUsername().compareTo(nameTry) == 0)
                 if (temp.getPassword().compareTo(pwdTry) == 0)
                     return temp;
                 else
                     return null;
         }
         return null;
     }
 
     public static boolean pay(Moviegoer goer, int index) {
         Time tempTime = new Time();
         Ticket ti;
 
         System.out.println("1.Visa 2.Master? ");
         int card = sc.nextInt();
 
         ti = goer.getUnpaid().remove(index);       
         ti.setBuyTime(tempTime.storeCurrentTime());
         goer.getPaid().add(ti);
 
         System.out.println("paid!");
         return true;
     }
 
     public static void showHistory(Moviegoer goer, boolean onlyUnpaid) {
         int unpaidSize = goer.getUnpaid().size();
         int paidSize = goer.getPaid().size();
         Ticket ti;
         int pageSize = 5;
         
         int track = 0;
 
         for (int i = 0; i < unpaidSize; i++) {
             ti = goer.getUnpaid().get(i);
 
             Ticket.display(ti);
             System.out.print(i + "Not paid");
         }
         
         if (onlyUnpaid)
             return ;
 
         for (int i = 0; i < paidSize; i++) {
             ti = goer.getPaid().get(i);
             Ticket.display(ti);
             System.out.print("paid");
 
             if (--pageSize == 0 && i < paidSize - 1) {
                 System.out.print("Continue? 1/0");
                 if (sc.nextInt() == 0)
                     break;
                 paidSize = 5;
             }
         }
     }
 
     public static Ticket book(Moviegoer goer, Movie toBook, Cineplex cLib[]) {
         String movieName;
         Cineplex cineplex;
         Cinema cinema;
 
         System.out.println("Select a showtime");
         ArrayList<Time> showtimeList = toBook.getShowtimeList();
         for (int i = 0; i < showtimeList.size(); i++) {
             System.out.print(i + " " + showtimeList.get(i).getStr());
         }
         Time selectedTime = showtimeList.get(sc.nextInt());
         
         for (int i = 0; i < cLib.length; i++) {
             System.out.println(cLib[i].getName());
         }
         
         System.out.print("Select a cineplex: ");
         cineplex = cLib[sc.nextInt()];
         for (int i = 0; i < cLib.length; i++) {
             System.out.println(cineplex.get(i).getNameOfCinema() + " " + cineplex.get(i).getClassOfCinema());
         }
         
         System.out.print("Select a cinema: ");
         cinema = cineplex.get(sc.nextInt());
         
         Cinema.presentSeat(cinema);
 
         System.out.println("Select a seat");
         System.out.print("row:");
         int row = sc.nextInt();
         System.out.print("col:");
         int col = sc.nextInt();
         int ticketID = 1234567890;
         
         Ticket ti = new Ticket(
                             toBook.getMovieName(), 
                             toBook.getTypeOfMovie(), 
                             cinema.getNameOfCinema(), 
                             cinema.getClassOfCinema(), 
                             cineplex.getLocation(), 
                             goer.getTypeOfMoviegoer(), 
                             selectedTime,
                             row,
                             col,
                             ticketID);
 
         goer.setUnpaid(ti);
         return ti;
     }
 
     public LinkedList<Moviegoer> get() {
     	return goerLib;
     }
 }
