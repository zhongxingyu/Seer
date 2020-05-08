 /**
  * 
  */
 package com.mcamier.apps.elevator;
 
 import static spark.Spark.get;
 import static spark.Spark.setPort;
 import spark.Request;
 import spark.Response;
 import spark.Route;
 
 import com.mcamier.apps.elevator.engine.IElevatorEngine;
 import com.mcamier.apps.elevator.engine.ScanElevatorEngine;
 import com.mcamier.apps.elevator.request.CallRequest;
 import com.mcamier.apps.elevator.request.DestinationRequest;
 import com.mcamier.apps.elevator.utils.Direction;
 
 /**
  * @author fufuuu
  *
  */
 public class Elevator {
 	
 	private static IElevatorEngine engine;
 
 	public static void main(String[] args) {
 		engine = new ScanElevatorEngine(6);
		lauchElevatorServer(8666);
 	}
 	
 	public static void lauchElevatorServer(int port) {
 		setPort(port);
 		
 		get(new Route("/") {
 			@Override
 			public Object handle(Request request, Response response) {
 				response.status(200);
 				return response;
 			}
 		});
 		
 		get(new Route("/reset") {
 			@Override
 			public Object handle(Request request, Response response) {
 				System.out.println("<==== reset");
 				engine.reset();
 				response.status(200);
 				return response;
 			}
 		});
 		
 		get(new Route("/go") {
 			@Override
 			public Object handle(Request request, Response response) {
 				System.out.println("<==== go");
 				engine.haveToGoTo(new DestinationRequest(Integer.parseInt(request.queryParams("floorToGo"))));
 				response.status(200);
 				return response;
 			}
 		});
 		
 		get(new Route("/call") {
 			@Override
 			public Object handle(Request request, Response response) {
				System.out.println("<==== call");
 				int atFloor = Integer.parseInt(request.queryParams("atFloor"));
				Direction toGo = Direction.valueOf(request.queryParams("to")); 
 				engine.call(new CallRequest(atFloor, toGo));
 				response.status(200);
 				return response;
 			}
 		});
 		
 		get(new Route("/userHasEntered") {
 			@Override
 			public Object handle(Request request, Response response) {
 				System.out.println("<==== userHasEntered");
 				response.status(200);
 				return response;
 			}
 		});
 		
 		get(new Route("/userHasExited") {
 			@Override
 			public Object handle(Request request, Response response) {
 				System.out.println("<==== userHasExited");
 				response.status(200);
 				return response;
 			}
 		});
 		
 		get(new Route("/nextCommand") {
 			@Override
 			public Object handle(Request request, Response response) {
 				System.out.println("<==== nextCommand");
 				response.status(200);
 				return engine.getNextCommand();
 			}
 		});
 	}
 }
