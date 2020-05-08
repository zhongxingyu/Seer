 package org.elsys.homework_23;
 import java.io.IOException;
 import java.util.ArrayList;
 
 public class Main {
 
   public static void main(String[] args) throws IOException {
          int seat_num=0;
            int[] one_TwoTogether; 
            one_TwoTogether = new int[300];
            int index=1;
            
            int all=0;
               
            int info=0; 
            int alone=0;
            int seatNum_alone_cp=0; 
            int placeOnlyFor_1=0; 
            int placeOnlyFor_1SeatNum=0; 
               
            for (;all<=162;){
         	   System.in.read();
         	   seat_num+=1;
                 
                ArrayList<Integer> l = new ArrayList<Integer>();
                Passengers newPassenger = new Passengers();
                	int countOfPassengers=0; 
                	
 		            if (newPassenger.passengers==2){
 		                countOfPassengers+=2;
 		                	l.add(newPassenger.passengers);
 		                	l.add(newPassenger.passengers); 
 					} else if (newPassenger.passengers==3){
 		                countOfPassengers+=3;
 							l.add(newPassenger.passengers);
 							l.add(newPassenger.passengers); 
 							l.add(newPassenger.passengers);
 					} else if (newPassenger.passengers==1){
 						countOfPassengers++;
 							l.add(newPassenger.passengers);
 					}
                       
                       if (countOfPassengers==3) {
                           System.out.println("seat N: "+(seat_num)+" "+(seat_num+1)+" "+(seat_num+2));
                           seat_num=seat_num+2;      
                           
                       }else if (countOfPassengers==2){
                           System.out.println("seat N: "+seat_num+" "+ (seat_num+1));
                           info++;  
                           
                           if (info>0){
                               index+=1;
                               one_TwoTogether[index]=seat_num+2;
                           }
                           seat_num=seat_num+2;  
                       
                       }else if (countOfPassengers==1){
                           if (alone==1){
                               System.out.println("seat N: "+(seatNum_alone_cp+1)); 
                               alone=0;
 
                               placeOnlyFor_1++;
                               placeOnlyFor_1SeatNum=seatNum_alone_cp+2;
                               seat_num-=1;
 
                           } else if (info==0 && placeOnlyFor_1!=1){
                                System.out.println("seat N: "+seat_num);
                                
                                seatNum_alone_cp=seat_num;
                                alone++;
                                seat_num+=2;
                                
                            } else if (info>0){
                               System.out.println("seat N: "+one_TwoTogether[index]);
                                  index--;
                                  info--;
                               seat_num-=1;
                               
                           } else if (placeOnlyFor_1==1){
                              System.out.println("seat N: "+placeOnlyFor_1SeatNum);
                               placeOnlyFor_1--;
                               seat_num-=1;
                           }
                       }
 			
                   System.out.println(l+"\n");
 
               if (seat_num>=162){
                   break;
               }
               
          }
 
     System.out.println("Free seats: "+((alone*2)+info+placeOnlyFor_1));
   
           
 }   }
