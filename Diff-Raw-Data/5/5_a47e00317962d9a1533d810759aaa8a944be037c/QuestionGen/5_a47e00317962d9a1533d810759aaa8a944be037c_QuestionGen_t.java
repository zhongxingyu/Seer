 package com.example.math.tutor;
 
 import java.util.Random;
 
 public class QuestionGen {
 	final static int NUM_LIMITER = 4;
 	final static int OP_LIMITER = 2;
 	Random randGen;
 	public int x;
 	public int y;
 	private int chooser;
 	public char operation;
 	public int answer;
 	public String problem;
 	
 	
 	public QuestionGen(){
 		randGen = new Random();
 	}
 	private void genX(){
		x = Math.abs(randGen.nextInt()) % NUM_LIMITER + 1;
 	}
 	private void genY(){
		y = Math.abs(randGen.nextInt()) % x;
 	}
 	private void genOp(){
 		chooser = Math.abs(randGen.nextInt() % OP_LIMITER);
 		if(chooser == 0){
 			operation = '+';
 			answer = x + y;
 		}else{
 			operation = '-';
 			answer = x - y;
 		}
 	}
 	public int getAnswer(){
 		return answer;
 	}
 	public String genProblem(){
 		genX();
 		genY();
 		genOp();
 		problem = ""+ x + operation + y;
 		return problem;
 	}
 	
 
 }
