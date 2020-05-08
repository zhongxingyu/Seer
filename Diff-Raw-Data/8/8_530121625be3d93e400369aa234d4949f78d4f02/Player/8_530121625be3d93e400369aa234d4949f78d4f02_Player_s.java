 package oneway.g8;
 
 import oneway.sim.MovingCar;
 import oneway.sim.Parking;
 import java.util.*;
 import java.lang.*;
 
 public class Player extends oneway.sim.Player
 {
     // if the parking lot is almost full
     // it asks the opposite direction to yield
     private static double AlmostFull = 0.8;
 
     //Indicator points right if true and left if not true.
     public boolean indicator = true;
 
     //Number of ticks since the game began
     public int timer;
 
     //Number of ticks until the indicator should change
     public int changeIndicatorTicks = 23;
 
     private boolean[] llights;
     private boolean[] rlights;
     private Parking[] right;
     private Parking[] left;
     MovingCar[] movingCars;
     private int nblocks_sum;
 	boolean useOM;
 
     //lotAssignment[p] is the number of cars moving in the direction of the indicator whose destination is parking lot p
     private int[] assignedLot;
 
     //similiar to assignedLot but for opposite cars. denote the furthest parking lot a car can possible be parked before collision
     private int[] oppositeLot;
 
     //The number of cars moving in the direction of the indicator which have passed parking lot p since the indicator last changed
     private int[] passedCars;
 
     public Player() {}
 
     public void init(int nsegments, int[] nblocks, int[] capacity)
     {
         this.nsegments = nsegments;
         this.nblocks = nblocks;
         this.capacity = capacity.clone();
 
         passedCars = new int[nsegments+1];
 
         nblocks_sum = 0;
         for (int i=0; i<nblocks.length; i++)
             nblocks_sum += nblocks[i];
 
         indicator = true;
         timer = 0;
 
        changeIndicatorTicks = (int)(nblocks_sum*1.3);
         if(changeIndicatorTicks < 5)
             changeIndicatorTicks = 5;
 
         oppositeLot = new int[nsegments+1];
 		useOM = haveOppossiteMovement();
 		
     }
 	
 	
 	//Determines whether or not to use the opposite movement function based on the configuration of the road
 	public boolean haveOppossiteMovement(){
 	
 	
 		if(nsegments == 1)
 			return false;
 			
 		int totalCapacity = 0;
 		
 		for(int i = 1;i<capacity.length-1;i++){
 		
 			System.out.println("capacity["+i+"]:"+capacity[i]+"\n");
 			totalCapacity += capacity[i];
 		
 		}
 		
 		if(totalCapacity==0)
 			return false;
 	
 	
 		return true;
 	}
 
     //Determine if all road segments are the same
     public boolean sameLength(){
         for(int i= 1;i<nblocks.length;i++){
             if(nblocks[i-1]!=nblocks[i])
                 return false;
         }
         return true;
     }
 
     //Reverse the indicator and set the lights accordingly
     public void setLights(){
 
         System.out.println("Indicator points right:"+indicator);
         //Change all of the lights to point in the direction of the indicator
         for(int i = 0;i<llights.length;i++){
             //The indicator points right
             if(indicator){
                 llights[i] = false;
                 rlights[i] = true;
             }
 
             //The indicator points left
             if (!indicator){
                 llights[i] = true;
                 rlights[i] = false;
             }
         }
     }
 
     public void setLights(MovingCar[] movingCars,
             Parking[] left,
             Parking[] right,
             boolean[] llights,
             boolean[] rlights)
     {
         this.movingCars = movingCars;
         this.left = left;
         this.right = right;
         this.llights = llights;
         this.rlights = rlights;
 
         //System.out.println("nblocks_sum:"+nblocks_sum);
         //System.out.println("changeIndicatorTicks:"+changeIndicatorTicks+"\n");
         
         //Change the indicator once changeIndicatorTicks ticks have passed
         if (timer%changeIndicatorTicks == 0) {
             indicator = !indicator;		
         }
 
         assignedLot = new int[nsegments+1];
         for (int i=0; i<assignedLot.length; i++)
             assignedLot[i] = 0;
 
         for(int k = 0;k<nsegments+1;k++)
             passedCars[k] = 0;
 
         assignedLot = assignedLot();
 
         /*
         System.out.println("assignedLot:");
         for(int i = 0;i<assignedLot.length;i++){
             System.out.print(assignedLot[i]+" ");
         }
         System.out.println("");
         */
 
         setLights();
         
         setLightAssignments(assignedLot);
         
 		//System.out.println("useOM:"+useOM);
 		
 		if(useOM)
 			OppositeMovements();
 
         print_lights();
 
         timer++;
     }
 
     private void print_lights() {
         System.out.println("toright direction lights: ");
         for (int i=0; i<rlights.length; i++)
             System.out.print(((rlights[i])?0:1) + " ");
         System.out.print("\n  ");
         System.out.println("toleft direction lights: ");
         System.out.print("  ");
         for (int i=0; i<llights.length; i++)
             System.out.print(((llights[i])?0:1) + " ");
         System.out.println("");
     }
 
     private void OppositeMovements()
     {
         Vector<Car> opposite = new Vector<Car>( ); // to store all cars in opposite direction as indicator
         Vector<Car> forward  = new Vector<Car>( );
         
         // segment, block, dir, startTime
         
         for (MovingCar c : movingCars) {
             if (indicator) {
                 if (c.dir < 0)  opposite.add(new Car(c.segment, c.block, c.dir, c.startTime, nblocks));
                 else            forward.add( new Car(c.segment, c.block, c.dir, c.startTime, nblocks));
                 
             } else {
                 if (c.dir < 0)  forward.add( new Car(c.segment, c.block, c.dir, c.startTime, nblocks));
                 else            opposite.add(new Car(c.segment, c.block, c.dir, c.startTime, nblocks));
             }
         }
         
         int left_parking_num = 0;
         int right_parking_num = 0;
         
         // take care of the cars in parking lots
         if (indicator) {
             if (right!=null)
                 for (int i=1; i<right.length-1; i++) {
                     if (right[i]==null) continue;
                     
                     right_parking_num += right[i].size();
                     for (int j=0; j<right[i].size(); j++)
                         forward.add( new Car(true, i, nblocks) );
                 }
             if (left!=null)
                 for (int i=1; i<left.length-1; i++) {
                     if (left[i]==null) continue;
                     left_parking_num += left[i].size();
                     
                     for (int j=0; j<left[i].size(); j++)
                         opposite.add( new Car(false, i, nblocks) );
                 }
         } else {
             if (right!=null)
                 for (int i=1; i<right.length-1; i++) {
                     if (right[i]==null) continue;
                     
                     right_parking_num += right[i].size();
                     for (int j=0; j<right[i].size(); j++) {
                         opposite.add( new Car(true, i, nblocks) );
                         opposite.get( opposite.size()-1 ).steps--;
                     }
                 }
             if (left!=null)
                 for (int i=1; i<left.length-1; i++) {
                     if (left[i]==null) continue;
                     
                     left_parking_num += left[i].size();
                     for (int j=0; j<left[i].size(); j++)
                         forward.add( new Car(false, i, nblocks) );
                 }
         }
         
         Collections.sort( forward );
         Collections.sort( opposite );
         
         if (forward.size() == 0 || opposite.size() == 0) { // if only have cars in one direction, set all lights green
             for (int i=0; i<llights.length; i++) {
                 llights[i] = true;
                 rlights[i] = true;
             }
             return ;
         }
         
         int fstep = forward.get(0).steps; // get the first car in the forward direction
         oneway.sim.Parking[] L = new oneway.sim.Parking[left.length];
         oneway.sim.Parking[] R = new oneway.sim.Parking[right.length];
         
         for (int i=0; i<left.length; i++) {
             L[i] = new oneway.sim.Parking();
             /*
              if (left[i] != null)
              for (Integer num : left[i])
              L[i].add( num );
              */
         }
         
         for (int i=0; i<right.length; i++) {
             R[i] = new oneway.sim.Parking();
             /*
              if (right[i] != null)
              for (Integer num : right[i])
              R[i].add( num );
              */
         }
         
         if (indicator) {
             for (int i=llights.length/2; i<llights.length; i++)
                 llights[i] = true;
             for (Car c : opposite) {
                 int cstep = (fstep + c.steps) / 2; // collision steps
                 int cseg = get_seg(cstep); // collision segment,FIXME right now all segments are regarded as same length
                 int latest_parking = cseg + 1;
                 c.latest_parking = latest_parking;
                 
                 if (c.steps <= fstep) { // first forward car already passed and the current car is in parking lot
                     int seg = get_seg(c.steps);
                     c.actual_parking = seg;
 						
                     if (seg > 0){
                         L[seg].add(0);
                         llights[seg - 1] = false;
 					}
                     continue;
                 }
                 
                 for (int i=latest_parking; i<L.length; i++) { // starting from the latest_parking to where the car come from
                     
                     if (R[i].size() + L[i].size() + assignedLot[i]  < capacity[i]) {
                         L[i].add(0);
                         c.actual_parking = i;
                         
                         int car_seg = get_seg(c.steps);
                         int car_blk = get_blk(c.steps);
                         if (car_seg == i && car_blk == 0)
                             llights[i-1] = false;
                         break;
                     }
                 }
             }
 
             for (Car c : opposite) {
                 if ( llights[c.seg-1] == false && c.blk == 0 && c.actual_parking < c.seg )
                     llights[c.seg-1] = true;
             }
             
             // control the left direction startpoint
             int cstep = (fstep + nblocks_sum + 1) / 2;
             int cseg = get_seg(cstep);
             int latest_parking = cseg + 1;
             llights[nsegments-1] = false;
             
             for (int i=latest_parking; i<L.length-1; i++) {
                 if (R[i].size() + L[i].size() + assignedLot[i] < capacity[i]) {
                    //L[i].add(0);
                     llights[nsegments-1] = true;
                     break;
                 }
             }
             
             /*
              int cstep = (fstep + nblocks_sum + 1) / 2;
              int cseg = get_seg(cstep);
              if (cseg == nsegments-1) {
              llights[llights.length-1] = false;
              }
              */
         } else {
             for (int i=0; i<(rlights.length+1)/2; i++)
                 rlights[i] = true;
             for (Car c : opposite) {
                 int cstep = (fstep + c.steps + 1) / 2; // collision steps
                 int cseg = get_seg(cstep); // collision segment
                 int latest_parking = cseg;
                 c.latest_parking = latest_parking;
                 
                 if (c.steps >= fstep) { // first forward car already passed and the current car is in parking lot
                     int seg = get_seg(c.steps);
                     c.actual_parking = seg+1;
 					if (seg < rlights.length-1) {
 						R[seg+1].add(0);
 						rlights[seg+1] = false;
 					}
                     continue;
                 }
                 
                 for (int i=latest_parking; i>=0; i--) {
                     if (R[i].size() + L[i].size()  + assignedLot[i]< capacity[i]) {
                         R[i].add(0);
                         
                         c.actual_parking = i;
                         
                         int car_seg = get_seg(c.steps);
                         int car_blk = get_blk(c.steps);
                         
                         /*
                          if (i==0 && c.steps == -1)
                          rlights[i] = false;
                          */
                         
                         if (car_seg == i-1 && car_blk == nblocks[i-1]-1) { //FIXME index
                             //System.out.println("SET RLIGHTS[" + i + "] red by car " + c.steps);
                             rlights[i] = false;
                         }
                         break;
                     }
                 }
             }
             
             for (Car c : opposite)
                 if (c.actual_parking > 0 && c.steps == -1) {
                     rlights[0] = true;
                     break;
                 }
 
             for (Car c : opposite) {
 				if ( c.seg+1 < rlights.length && rlights[c.seg+1] == false && c.blk == nblocks[c.seg]-1 && c.actual_parking > c.seg+1 )
                     rlights[c.seg+1] = true;
             }
             
             // control the right direction startpoint
             int cstep = fstep / 2;
             int cseg = get_seg(cstep);
             int latest_parking = cseg;
             rlights[0] = false;
             
             for (int i=latest_parking; i>=1; i--) {
                 if (R[i].size() + L[i].size() + assignedLot[i]< capacity[i]) {
                    //R[i].add(0);
                     rlights[0] = true;
                     break;
                 }
             }
             
             /*
              int cstep = fstep / 2;
              int cseg = get_seg(cstep);
              if (cseg == 0) {
              rlights[0] = false;
              }
              */
         }
         
         /*
          r0      r1      r2
          ->      ->      ->
          seg0    seg1    seg2
          | - - - | - - - | - - - |
          p0      p1       p2     p3
          <-      <-      <-
          l0       l1     l2
          */
         
         // take care of the following situation:
         // opposite cars shouldn't wait until forward cars arrive at destination, which is the endpoint of the road
         // commented out for now, becuase not sure whether there will be new forward cars appearing in the endpoint of road
         /*
          if (indicator) {
          for (int i=0; i<llights.length; i++)
          if (forward.get(forward.size()-1).seg >= i+1)
          llights[i] = true;
          } else {
          for (int i=0; i<rlights.length; i++)
          if (forward.get(forward.size()-1).seg < i)
          rlights[i] = true;
          }
          */
         
         System.out.println("*************** forward cars******************");
         System.out.println(forward.size() + " cars moving forward");
         for (Car c : forward)
             System.out.println(c.toString());
         System.out.println("*************** opposite cars*****************");
         System.out.println(opposite.size() + " cars moving opposite");
         for (Car c : opposite)
             System.out.println(c.toString());
         System.out.println("**********************************************");
         
         // parking lot information output
         System.out.println("parking lots:");
         for (int i=0; i<right.length; i++)
             System.out.print(right[i]+" ");
         System.out.println("");
         
         for (int i=0; i<R.length; i++)
             System.out.print(R[i]+" ");
         System.out.println("");
         System.out.println("");
         
         
         for (int i=0; i<left.length; i++)
             System.out.print(left[i]+" ");
         System.out.println("");
         for (int i=0; i<L.length; i++)
             System.out.print(L[i]+" ");
         System.out.println("");
         
         System.out.println("capacity");
         for (int i=0; i<capacity.length; i++)
             System.out.print(capacity[i]+" ");
         System.out.println("");
         System.out.println("**********************************************");
 
 
         for (int i=1; i<nsegments; i++)
             oppositeLot[i] = L[i].size() + R[i].size();
 
         System.out.println("assignedLot:");
         for(int i = 0;i<assignedLot.length;i++){
             System.out.print(assignedLot[i]+" ");
         }
         System.out.println("");
 
 
         System.out.println("oppositeLot: ");
         for (int i=0; i<nsegments+1; i++)
             System.out.print(oppositeLot[i] + " ");
         System.out.println("");
 
         System.out.println("indicator : " + ((indicator)? "true":"false"));
     }
     
     // get the segment number according to steps of a car
     int get_seg(int steps) {
         steps++;
         int sum = 0;
         for (int i=0; i<nblocks.length; i++) {
             sum += nblocks[i];
             if (sum >= steps)
                 return i; // guaranteed to execute this return
         }
         return nblocks.length;
     }
     
     // get the block number in a segment according to steps of a car
     int get_blk(int steps) {
         steps++;
         int sum = 0;
         for (int i=0; i<nblocks.length; i++) {
             sum += nblocks[i];
             if (sum >= steps) {
                 sum -= nblocks[i];
                 return steps - sum - 1; // guaranteed to execute this return
             }
         }
         return 0;
     }
     
 	
 	
 
     //Assign for each parking lot the number of cars moving in the direction of the indicator that should be parked there
     //on the amount of time before the indicator changes the capacity of the lots
     private int[] assignedLot(){
 
 
         int carID = 0;
         Vector<Car> forward  = new Vector<Car>( );
 
 		//adjParking[i] == true means that a car is directly in front
 		//of parking lot i in the direction of the indicator
 		boolean [] adjParking = new boolean[nsegments+1];
 		
 		for(int i = 0;i<adjParking.length;i++){
 			adjParking[i] = false;
 		}
 
         // segment, block, dir, startTime
 
         for (MovingCar c : movingCars) {
             if (indicator) {
                 if(c.dir > 0){
 					forward.add( new Car(c.segment, c.block, c.dir, c.startTime, nblocks, indicator, carID));
 					if(c.block==0) adjParking[c.segment] = true;
 				}
                 carID++;
             } else { 
                 if (c.dir < 0) {
 					forward.add( new Car(c.segment, c.block, c.dir, c.startTime, nblocks, indicator, carID));
 	//				System.out.println("c.segment:"+c.segment+"\n");
 					if(c.block==nblocks[c.segment]-1){
 	//					System.out.println("car adjacted to lot:"+(c.segment+1)+"\n");
 						adjParking[c.segment+1] = true;
 					}
 				}
                 carID++;
             }
         }
 
 
         int left_parking_num = 0;
         int right_parking_num = 0;
 
 
         // take care of the cars in parking lots
         if (indicator) {
             if (right!=null)
                 for (int i=0; i<right.length; i++) {
                     if (right[i]==null) continue;
 
                     right_parking_num += right[i].size();
 					int queuePosition = 0;
                     for (int j=0; j<right[i].size(); j++){ 
                         forward.add( new Car(true, i, nblocks, indicator, carID, queuePosition) );
                         carID++;
 						queuePosition++;
                     }
                 }
         } else {
             if (left!=null)
                 for (int i=0; i<left.length; i++) {
                     if (left[i]==null) continue;
 
                     left_parking_num += left[i].size();
 					int queuePosition = 0;
                     for (int j=0; j<left[i].size(); j++){
                         forward.add( new Car(false, i, nblocks, indicator, carID, queuePosition) );
                         carID++;
 						queuePosition++;
                     }
                 }
         }
 
         //lotAssignment[p] is the number of cars moving in the direction of the indicator whose destination is parking lot p
         int[] lotAssignment = new int[nsegments+1];
 
         for(int k = 0;k<lotAssignment.length;k++)
             lotAssignment[k] = 0;
 
         //Number of cars at each parking after all cars are parked when the indicator changes
         int[] lotCars = new int[nsegments+1];
 
         Collections.sort( forward );
         oneway.sim.Parking[] L = left.clone();
         oneway.sim.Parking[] R = right.clone();
 
 //        System.out.println(forward);
 
         //Add the cars in each parking lot going the opposite direction to the array of lots because these cars will not move
         if(timer%changeIndicatorTicks==0){
             for(int lot = 0;lot < nsegments+1;lot++){
                 if(indicator)
                     if(L!=null&&L[lot]!=null)
                         lotCars[lot]=L[lot].size();
                     else
                         lotCars[lot] = 0;
 
                 else
                     if(R!=null&&R[lot]!=null)
                         lotCars[lot]=R[lot].size();
                     else
                         lotCars[lot] = 0;
             }
         }
 
         //Ticks left before the indicator changes
         int ticks = changeIndicatorTicks-(timer%changeIndicatorTicks);
         //System.out.println(ticks+" ticks left \n");
 
         for(int i = 0;i<forward.size();i++){
 
             Car c = forward.get(i);
 
             //Furthest step the car can reach before the indicator changes if it does not park
             int step = 0;
 
             if(indicator){
                 step = c.steps+ticks-c.queuePosition*2;
 				
 				if(c.isParked&&adjParking[c.seg]){
 					step--;
 					//System.out.println("Decrementing possible steps because car adjacent to lot " + c.seg + "\n");
 				}
             }
             else{
                 step = c.steps-ticks+c.queuePosition*2;
 				if(c.isParked) System.out.println("c is parking in lot " + c.seg + "\n");
 				if(c.isParked&&adjParking[c.seg]){
 					step++;
 					//System.out.println("Decrementing possible steps because car adjacent to lot " + c.seg + "\n");
 					//System.out.println("Queue adjustment:" + (c.queuePosition*2+1) + "\n");
 				}
             }
 
             //Furthest parking lot the car can reach before the indicator changes if it does not park
             int pLot = 0;
 
             //System.out.println("Current step, Furthest possible step for car " + c.steps + " " + c.ID + ":" + step + "\n");
 
             if(indicator){
                 for(int seg = 0;seg<nsegments;seg++){
                     step = step - nblocks[seg];
                     //System.out.println(step);
                     if(step<=0){
                         pLot = seg;
                         //System.out.println("Setting destination lot for car " + c.ID + " to " + pLot + "\n");
                         break;
                     }
                     if(seg==nsegments-1&&step>0){
                         pLot = seg+1;
                         //System.out.println("Setting destination lot for car " + c.ID + " to " + pLot + "\n");
                         break;
                     }
                 }
 
                 while(pLot>-1){
 
                     if(lotCars[pLot]+1<=capacity[pLot]-oppositeLot[pLot]){
 
                         //Car c can park in parking lot pLot without causing overcapacity
                         lotCars[pLot] = lotCars[pLot]+1;
                         lotAssignment[pLot]++;
                         break;
                     }
 
                     pLot--;
                 }
 
                 if(pLot==-1)
                     lotAssignment[0]++;
             }
             else{
 
                 //Total number of blocks on the road
                 int blockCount = 0;
 
                 for(int seg = 0;seg<nsegments;seg++){
                     blockCount = blockCount + nblocks[seg];
                 }
 
                 //System.out.println("blockCount:" + blockCount + "\n");
 
                 step = blockCount - step;
                 for(int seg = nsegments-1;seg>-1;seg--){
 
                     step = step - nblocks[seg];
                     //System.out.println("Recalculated step:" + step + "\n");
                     if(step <=0){
                         pLot = seg + 1;
                         //System.out.println("Setting destination lot for car " + c.ID + " to " + pLot + "\n");
                         break;
                     }
                 }
 
                 while(pLot<nsegments+1){
 
 					//System.out.println("lotCars[plot]:" + lotCars[pLot] + "\n");
 				
                     if(lotCars[pLot]+1<=capacity[pLot]-oppositeLot[pLot]){
 
                         //Car c can park in parking lot pLot without causing overcapacity
                         lotCars[pLot] = lotCars[pLot]+1;
                         lotAssignment[pLot] ++;
                         break;
                     }
 					else{
 					
 						//System.out.println("Not enough room for cars at " + pLot + "\n");
 					
 					}
 
                     pLot++;
                 }
 
                 if(pLot==nsegments+1)
                     lotAssignment[nsegments]++;
             }
         }
 
         return lotAssignment;
     }
 
 
     //Set the lights for the cars moving in the direction of the indicator based on the lot assignments for each parking lot
     public void setLightAssignments(int[] assignedLot){
 
         //Count the number of cars ahead of each parking lot which are facing the direction of the indicator
         if(indicator){
 
             //Add moving cars
             for(int i = 0;i<movingCars.length;i++){
                 if(movingCars[i].dir==1){
                     for(int j = 0;j<=movingCars[i].segment;j++)
                         passedCars[j] ++;
                 }
             }
 
             //Add parked cars
             if(right!=null){
                 for(int i = 0;i<right.length;i++){
                     for(int j = 0;j<i;j++){
                         if(right[i]!=null)
                             passedCars[j] += right[i].size();
                     }
                 }
             }
 
 
             //Determine if more cars can move in the direction of the indicator based on the parking lot destinations and passed cars
             for(int lot = 0;lot<nsegments;lot++){
                 int toPass = 0;
 
 //                System.out.println(assignedLot[lot]);
 
 
                 for(int i = lot+1;i<nsegments+1;i++){
                     toPass += assignedLot[i];
                 }
 
                 //System.out.println("toPass for " + lot + ":" + toPass + "\n");
                 //System.out.println("passedCars[lot]:"+passedCars[lot]+"\n");
                 if(passedCars[lot]<toPass)
                     rlights[lot] = true;
                 else
                     rlights[lot] = false;
             }
 
 
 
         }
         else{
             //Add moving cars
             for(int i = 0;i<movingCars.length;i++){
                 if(movingCars[i].dir==-1){
                     for(int j = nsegments;j>movingCars[i].segment;j--)
                         passedCars[j] ++;
                 }
             }
             //Add parked cars
             if(left!=null){
                 for(int i = 0;i<left.length;i++){
                     for(int j = i+1;j<left.length;j++){
                         if(left[i]!=null)
                             passedCars[j] += left[i].size();
                     }
                 }
             }
 
             //Determine if more cars can move in the direction of the indicator based on the parking lot destinations and passed cars
             for(int lot = 1;lot<nsegments+1;lot++){
                 int toPass = 0;
 
                 for(int i = 0;i<lot;i++){
                     toPass += assignedLot[i];
                 }
 
 
                 if(passedCars[lot]<toPass)
                     llights[lot-1] = true;
                 else
                     llights[lot-1] = false;
 
 
             }
 
 
         }
     }
 
     private int nsegments;
     private int[] nblocks;
     private int[] capacity;
 }
