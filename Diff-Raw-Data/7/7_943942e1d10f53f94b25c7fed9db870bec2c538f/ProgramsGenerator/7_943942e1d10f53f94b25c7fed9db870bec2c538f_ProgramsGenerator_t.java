 /* -.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.
 
 * File Name : ProgramsGenerator.java
 
 * Purpose :
 
 * Creation Date : 05-07-2011
 
* Last Modified : Fri 05 Aug 2011 09:01:44 AM EEST
 
 * Created By : Greg Liras <gregliras@gmail.com>
 
 _._._._._._._._._._._._._._._._._._._._._.*/
 
 import java.util.ArrayList;
 
 
 public class ProgramsGenerator
 {
   private int minLim=0;
   private ArrayList<Program> ProgList = null;
   private ArrayList<Program> StartList = null;
   private int progLen;
 
   private Program BuffA = null;
   private Program BuffM = null;
   public ProgramsGenerator(int minLim)
   {
     this.minLim=minLim;
     ProgList = new ArrayList<Program>();
     progLen=0;
   }
   public void fillProgList()
   {
     ProgList.add(new Program(minLim));
     for(int i=0;i<minLim;i++)
     {
       makeMoreProgs();
     }
   }
   public void makeMoreProgs()
   {
     progLen++;
     StartList=new ArrayList<Program>();
     StartList.ensureCapacity(2*ProgList.size());
     for(Program S : ProgList)
     {
       BuffA = (Program) S.clone();
      BuffA.set(S.getMyLength());
       StartList.add(BuffA);
       BuffM = (Program) S.clone();
       BuffM.clear(S.getMyLength());
       StartList.add(BuffM);
     }
     ProgList=StartList;
   }
   public void setProgList(ArrayList<Program> newPLst)
   {
     ProgList=newPLst;
   }
   public ArrayList<Program> getProgList()
   {
     return ProgList;
   }
   public void printProgs()
   {
     for(Program S : ProgList)
     {
       System.out.println(S);
     }
   }
   public void printProgsSize()
   {
     System.out.println(progLen);
   }
 }
 
