 import tester.*;
 
 class Examples {
 	
	Queue Q;
	Stack S;
	PriorityQueue P;
 	
 	boolean testQueue(Tester t) {
 		return t.checkExpect(Q.newQ().enqueue(3).dequeue(),
 				Q.newQ());
 	}
 	
 	boolean testQueue2(Tester t) {
 		return t.checkExpect(Q.newQ().enqueue(7).enqueue(4).enqueue(5).dequeue(),
 				Q.newQ().enqueue(4).enqueue(5));
 	}
 	
 	boolean testQueue3(Tester t) {
 		return t.checkExpect(Q.newQ().enqueue(4).enqueue(5).front(),
 				4);
 	}
 	
 	boolean testQueue4(Tester t) {
 		return t.checkExpect(Q.newQ().enqueue(4).enqueue(5).dequeue(),
 				Q.newQ().enqueue(5));
 	}
 	
 // ------------------------------------------------------------------------------------------------
 	
 	boolean testStack(Tester t) {
 		return t.checkExpect(S.newStk().push(45).pop(),
 				S.newStk());
 	}
 	
 	boolean testStack2(Tester t) {
 		return t.checkExpect(S.newStk().push(45).push(768).pop(),
 				S.newStk().push(45));
 	}
 
 	boolean testStack3(Tester t) {
 		return t.checkExpect(S.newStk().push(7).push(4).push(5).pop(),
 				S.newStk().push(7).push(4));
 	}
 
 	boolean testStack4(Tester t) {
 		return t.checkExpect(S.newStk().push(7).push(4).push(5).top(), 5);
 	}
 
 	boolean testStack5(Tester t) {
 		return t.checkExpect(S.newStk().push(7).push(4).push(5).pop().top(), 4);
 	}
 	
 // ------------------------------------------------------------------------------------------------
 	boolean testPQ(Tester t) {
 		return t.checkExpect(P.newPQ().addElt(5).remMinElt(), P.newPQ());
 	}
 	
 	boolean testPQ2(Tester t) {
 		return t.checkExpect(P.newPQ().addElt(5).addElt(3).remMinElt(),
 				P.newPQ().addElt(5));
 	}
 	
 	boolean testPQ3(Tester t) {
 		return t.checkExpect(P.newPQ().addElt(5).addElt(3).getMinElt(), 3);
 	}
 	
 	boolean testPQ4(Tester t) {
 		return t.checkExpect(P.newPQ().addElt(7).addElt(4).addElt(5).remMinElt(),
 				P.newPQ().addElt(7).addElt(5));
 	}
 
 	boolean testPQ5(Tester t) {
 		return t.checkExpect(P.newPQ().addElt(7).addElt(4).addElt(5).remMinElt().getMinElt(),
				4);
 	}
 }
