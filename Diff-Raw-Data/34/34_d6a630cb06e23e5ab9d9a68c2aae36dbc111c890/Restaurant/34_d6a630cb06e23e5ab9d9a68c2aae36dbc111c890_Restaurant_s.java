 import java.io.*;
 import java.util.*;
 
 class Block {
 	int index;
 	int time;
 	int dur;
 	int id;
 	boolean isDone = false;
 }
 public class Restaurant {
 
 
 	public void process_SRTF ( SRTF[] list, int n ) {
 		Arrays.sort( list );
 		ArrayList<Block> blocks = new ArrayList<Block>();
 		boolean isDone[] = new boolean[n];
 		int time = list[0].getArrival_T();
 		int counter = 0;
 		
 		for( int i = 0; i < n; i ++ ) {
 			isDone[i] = false;
 		}
 
 		while ( true ) {
 			//System.out.println( time );
 			int min = Integer.MAX_VALUE;
 			int index = -1;
 			int next = -1;
 			for( int i = 0; i < n; i++ ){
 				SRTF temp = list[i];
 				if( time >= temp.getArrival_T() && min > temp.getBurst_T() && isDone[i] == false ) {
 					//System.out.println( i + " " + isDone[i] );
 					min = temp.getBurst_T();
 					index = i;
 					//System.out.println( index );
 				}
 
 				else if ( time < temp.getArrival_T() ) {
 					next = temp.getArrival_T();
 					break;
 				}
 
 			}
 
 			if( index == -1 ) {
 				//System.out.println( "NEGATIVE ONE YUNG INDEX" );
 				for( int i = 0; i < n; i++ ) {
 					if( isDone[i] == false ) {
 						time = list[i].getArrival_T();					
 					}
 				}
 			}
 
 			else {
 				//System.out.println( index + " " + list[index].getBurst_T() );
 				if( blocks.size() == 0 ) {
 					blocks. add( new Block() );
 					blocks.get(0).index = index;
 					blocks.get(0).time = time;
 					blocks.get(0).dur = 1;
 					blocks.get(0).id = list[index].getProcessIT();
 					list[index].setBurst_T( list[index].getBurst_T() -1 );
 				}
 				else {
 					Block curr = blocks.get( blocks.size() -1 );
 					//System.out.println( curr.index + " " + curr.time + " " + curr.id + " " + curr.dur );
 					if( curr.index == index ) {
 						//System.out.println( index) ;
 						curr.dur = curr.dur + 1;
 						list[index].setBurst_T( list[index].getBurst_T() -1 );
 						if( list[index].getBurst_T() <= 0) {
 							curr.isDone = true;
 							isDone[index] = true;
 							counter ++;
 							//System.out.println( index + " " + isDone[index] );
 						}		
 					}
 					else {
 						//System.out.println( "NEW BLOCK!!!" );
 						blocks.add( new Block() );
 						blocks.get(blocks.size()-1).index = index;
 						blocks.get(blocks.size()-1).time = time;
 						blocks.get(blocks.size()-1).dur = 1;
 						blocks.get(blocks.size()-1).id = list[index].getProcessIT();
 						list[index].setBurst_T( list[index].getBurst_T() -1 );
 					}
 
 				} // end else 48
 
 				time++;			
 			} // end else 40
 			if( counter == n ) break;
 			//if( time == 100 ) break;
 	
 		}// end while 19
 		
 		//System.out.println( "BLOCK SIZE: " +  blocks.size() );
 		for( Block temp : blocks ) {
 			System.out.printf( "%d %d %d", temp.time, temp.id, temp.dur );
 			if( temp.isDone == true ) System.out.println( "X" );
 			else System.out.println();
 		}
 		
 	}// end method
 	
 	public void process_P ( P[] list, int n ) {
 		Arrays.sort( list );
 		ArrayList<Block> blocks = new ArrayList<Block>();
 		boolean isDone[] = new boolean[n];
 		int time = list[0].getArrival_T();
 		int counter = 0;
 		
 		for( int i = 0; i < n; i ++ ) {
 			isDone[i] = false;
 		}
 
 		while ( true ) {
 			//System.out.println( time );
 			int min = Integer.MAX_VALUE;
 			int index = -1;
 			int next = -1;
 			for( int i = 0; i < n; i++ ){
 				P temp = list[i];
 				if( time >= temp.getArrival_T() && min > temp.getPriority() && isDone[i] == false ) {
 					//System.out.println( i + " " + isDone[i] );
 					min = temp.getPriority();
 					index = i;
 					//System.out.println( index );
 				}
 
 				else if ( time < temp.getArrival_T() ) {
 					next = temp.getArrival_T();
 					break;
 				}
 
 			}
 
 			if( index == -1 ) {
 				//System.out.println( "NEGATIVE ONE YUNG INDEX" );
 				for( int i = 0; i < n; i++ ) {
 					if( isDone[i] == false ) {
 						time = list[i].getArrival_T();					
 					}
 				}
 			}
 
 			else {
 				//System.out.println( index + " " + list[index].getBurst_T() );
 				if( blocks.size() == 0 ) {
 					blocks. add( new Block() );
 					blocks.get(0).index = index;
 					blocks.get(0).time = time;
 					blocks.get(0).dur = 1;
 					blocks.get(0).id = list[index].getProcessIT();
 					list[index].setBurst_T( list[index].getBurst_T() -1 );
 				}
 				else {
 					Block curr = blocks.get( blocks.size() -1 );
 					//System.out.println( curr.index + " " + curr.time + " " + curr.id + " " + curr.dur );
 					if( curr.index == index ) {
 						//System.out.println( index) ;
 						curr.dur = curr.dur + 1;
 						list[index].setBurst_T( list[index].getBurst_T() -1 );
 						if( list[index].getBurst_T() <= 0) {
 							curr.isDone = true;
 							isDone[index] = true;
 							counter ++;
 							//System.out.println( index + " " + isDone[index] );
 						}		
 					}
 					else {
 						//System.out.println( "NEW BLOCK!!!" );
 						blocks.add( new Block() );
 						blocks.get(blocks.size()-1).index = index;
 						blocks.get(blocks.size()-1).time = time;
 						blocks.get(blocks.size()-1).dur = 1;
 						blocks.get(blocks.size()-1).id = list[index].getProcessIT();
 						list[index].setBurst_T( list[index].getBurst_T() -1 );
 					}
 
 				} // end else 48
 
 				time++;			
 			} // end else 40
 			if( counter == n ) break;
 			//if( time == 100 ) break;
 	
 		}// end while 19
 		
 		//System.out.println( "BLOCK SIZE: " +  blocks.size() );
 		for( Block temp : blocks ) {
 			System.out.printf( "%d %d %d", temp.time, temp.id, temp.dur );
 			if( temp.isDone == true ) System.out.println( "X" );
 			else System.out.println();
 		}
 		
 	}// end method
 
 	public void process_SJF( SJF[] list, int n ) {
 		Arrays.sort( list );
 		boolean isDone[] = new boolean[n];
 		int time = list[0].getArrival_T();
 		int counter = 0;
 		
 		//initializes all to false. 
 		for ( int i = 0; i < n; i++ ) {
 			isDone[i] = false;
 		}
 
 		while ( true ) {
 			int min = Integer.MAX_VALUE;
 			int index = -1;
 			//looks for shortest job wherein time >= arrivaltime and it is not yet "done"
 			for( int i = 0; i < n; i++ ) {
 				SJF temp = list[i];
 				if( time >= temp.getArrival_T() && min > temp.getBurst_T() && isDone[i] == false ) {
 					min = temp.getBurst_T();
 					index = i;
 				}
 				else if( time < temp.getArrival_T() ) break;
 			}
 
 			// no one has arrived yet after finishing the last process
 			if( index == -1 ) {
 				for( int i = 0; i < n; i++ ) {
 					if( isDone[i] == false ) {
 						time = list[i].getArrival_T();					
 					}
 				}
 			}
 
 			else {
 				System.out.printf( "%d %d %dX\n", time, list[index].getProcessIT(), list[index].getBurst_T() );	
 				time += list[index].getBurst_T();
 				counter ++;
 				isDone[index] = true;
 				if( counter == n ) break;
 			}
 		} // end while loop	
 	} //end method
 
 	public static void main ( String args[] ) {
 		Scanner in = new Scanner( System.in);
 		int testcase = in.nextInt();
 		int tc_index = 0;
 		while( ++tc_index <= testcase ) {
 			System.out.println( tc_index );
 			int n = in.nextInt();
 			String mode = in.next();
 	
 			if( mode.equals( "FCFS") ) {
 				int counter = 0;
 				
 				FCFS[] object_array = new FCFS[n];
 				
 				while (counter < n) {
 					object_array[counter] = new FCFS(in.nextInt(), in.nextInt(), in.nextInt(), counter + 1);
 					counter++;
 				}
 				Arrays.sort(object_array);
 				
 				
 				// fix the gantt chart
 				counter = 0;
 				int time_elapsed = 0;
 				while (counter < n) {
 					if(object_array[counter].getArrival_T() > time_elapsed) {
 						time_elapsed = object_array[counter].getArrival_T() + object_array[counter].getBurst_T();
 					}
 					else {
 						object_array[counter].setArrival_T(time_elapsed);
 						time_elapsed = time_elapsed + object_array[counter].getBurst_T();
 					}
 					counter++;
 					
 				}
 				
 				
 				// just used to display stuff.
 				
 				counter = 0;
 				while (counter < n) {
 					System.out.printf("%d %d %dX\n", object_array[counter].getArrival_T(), object_array[counter].getProcessIT(), object_array[counter].getBurst_T());
 					counter++;
 				}
 
 				
 			}// end FCFS
 
 			else if( mode.equals( "SJF") ) {
 				SJF[] list = new SJF[n];
 				for( int i = 0; i < n; i++ ) {
 					int a_t = in.nextInt();
 					int b_t = in.nextInt();
 					int p = in.nextInt();
 
 					list[i] = new SJF( a_t, b_t, p, i+1 );
 				}
 				
 				(new Restaurant() ).process_SJF( list, n );
 			} // SJF
 
 			else if( mode.equals( "SRTF") ) {
 				SRTF[] list = new SRTF[n];
 				for( int i = 0; i <n; i++ ) {
 					int a_t = in.nextInt();
 					int b_t = in.nextInt();
 					int p = in.nextInt();
 					
 					list[i] = new SRTF( a_t, b_t, p, i+1 );
 				}
 				(new Restaurant() ).process_SRTF( list, n );
 			} // SRTF
 
 			else if( mode.equals( "P") ) {
 				P list[] = new P[n];
 				for( int i = 0; i < n; i++ ) {
 
 					list[i] = new P( in.nextInt(), in.nextInt(), in.nextInt(), i+1 );
 				}
 				( new Restaurant() ).process_P( list, n );
 			} // P
 
 			else if( mode.equals( "RR") ) {
 				// read the time slice value
 				int TIME_SLICE = in.nextInt();
 
 				int counter = 0;
 				int time_elapsed = 0;
 			
 				// create the objects
 				RR[] object_array = new RR[n];
 				while (counter < n) {
 					object_array[counter] = new RR(in.nextInt(), in.nextInt(), in.nextInt(), counter + 1);
 					counter++;
 				}
 				Arrays.sort(object_array);
 				
 				// this is just for displaying stuff
 				/*
 				System.out.println("---    stuff---");
 				counter = 0;
 				while(counter < n)
 				{
 					System.out.printf("%d %d %d\n", object_array[counter].getArrival_T(), object_array[counter].getBurst_T(), object_array[counter].getPriority());
 					counter++;
 				}
 				
 				
 				System.out.println("---end stuff---");
 				*/
 			
 				int negative_count = 0;			
 				counter = 0;
 				boolean[] negative = new boolean[n];
 				while(true) {
 					if(negative_count == n)
 					{
 						break;
 					}
 					if((negative[counter] == true) || (object_array[counter].getArrival_T() > time_elapsed))
 					{
						counter = (counter + 1) % n;
 						continue;
 					}
 					if((object_array[counter].getBurst_T() - TIME_SLICE) > 0){
 						object_array[counter].setBurst_T(object_array[counter].getBurst_T() - TIME_SLICE);
 						System.out.printf("%d %d %d\n", time_elapsed, object_array[counter].getProcessIT(), TIME_SLICE);
 						time_elapsed += TIME_SLICE;
 					}
 					else {
 						if(object_array[counter].getBurst_T() == 0)
 						{
 							System.out.printf("%d %d %dX\n", time_elapsed, object_array[counter].getProcessIT(), TIME_SLICE);
 						}
 						else
 						{
 							System.out.printf("%d %d %dX\n", time_elapsed, object_array[counter].getProcessIT(), object_array[counter].getBurst_T());
 						}
 						time_elapsed += object_array[counter].getBurst_T();
 						negative[counter] = true;
 						negative_count += 1;
 					}
 					counter = (counter + 1) % n;
 				}
 			
 			
 			
 			
 
 			}
 		
 		}
 
 	}
 }
 
