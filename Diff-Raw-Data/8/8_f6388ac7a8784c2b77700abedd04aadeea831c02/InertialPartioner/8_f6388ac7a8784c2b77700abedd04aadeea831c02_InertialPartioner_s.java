 /**
  * Inertial Partitioning
  * Copyright (C) 2013  Vy Thuy Nguyen
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Library General Public
  * License as published by the Free Software Foundation; either
  * version 2 of the License, or (at your option) any later version.
  * 
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Library General Public License for more details.
  * 
  * You should have received a copy of the GNU Library General Public
  * License along with this library; if not, write to the
  * Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
  * Boston, MA  02110-1301, USA.
  */
 
 
 package api;
 
 import java.util.ArrayList;
 import org.jgrapht.graph.DefaultWeightedEdge;
 import org.jgrapht.graph.SimpleWeightedGraph;
 
 /**
  * @author              Vy Thuy Nguyen
  * @version             1.0 Jan 22, 2013
  * Last modified:       
  */
 public class InertialPartioner 
 {
     public static Line getLine(SimpleWeightedGraph<Node, DefaultWeightedEdge> g) throws Exception
     {
         //Compute xbar and ybar
         double xbar = 0, ybar = 0;
        int N = g.vertexSet().size();
         
         for (Node node : g.vertexSet())
         {
             xbar += node.getX();
             ybar += node.getY();
         }
         
         xbar /= N;
         ybar /= N;
         
        //Compute sume of squares of distance
         double sumXsq = 0, sumYsq = 0, sumXY = 0;
         double xDif = 0, yDif = 0;
         for (Node node : g.vertexSet())
         {
             xDif = node.getX() - xbar;
             yDif = node.getY() - ybar;
             sumXsq += xDif * xDif;
             sumYsq += yDif * yDif;
             sumXY += xDif * yDif;
         }
         
         //Compute a and b
         double a, b, lambda;
         //Compute lambda
         //The eigenvector of smallest eigenvalue of A = [X1 X2 X2 X3]
         //Let lambda be the eigenvalue, I be the 2x2 identity matrix,
         //X1 = sumXsq, X3 = sumYsq, X2 = sumXY
         //Then (A - lambda * I)[a b] = 0
         //=> det(A - lambda * I) = 0
         //<=> (X1 - lambda)(X3 - lambda) - X2 * X2 = 0
         //<=> lambda^2 -(X1 + X3) * lambda + X1 * X3 - X2 * X2
         //Solve this equation and the smallest solution is the eigenvalue we're looking for.
         ArrayList<Double> sols = getSolutions(1, //a
                                               0 - sumXsq - sumYsq, //b
                                               sumXsq * sumYsq - sumXY * sumXY); //c
        if (sols.size() == 0)
             throw new Exception("No eigenvalue found!");
         
         lambda = Math.min(sols.get(0), sols.get(1));
         
         //Compute a, b
         //v = [a b] is the eigenvector such that
         //(A - lambda * I) * v = [0 0]
         //Or
         //a * X1 + b * X2 = lambda * a <=> a * (X1 - lambda) + b * X2 = 0  (1)
         //a * X2 + b * X3 = lambda * b <=> a * (X2 - lambda) + b * X3 = 0  (2)
         //Note that this system's solution can only be one of the following:
         //  1) (a, b) = (0, 0)
         //  2) (a, b) is a vector in the eigenspace whose basis is (-X2 / (X1 - lambda), 1)
         //  3) the system has no solution.
         //Since the first and third cases are irrelevant, we only need to look for one of the
         //vector in the eigenspace described above.
         //The easiest one to find is the basis itself.
         if (Double.compare((0 - sumXY) / (sumXsq - lambda), 
                            (0 - sumYsq) / (sumXY - lambda)) != 0) //If the system doesn't have inf. number of solultions
             throw new Exception("The system must have inf. number of solutions. Otherwise, the eigenvector would be [0, 0]");
         
         a = (0 - sumXY) / (sumXsq -  lambda);
         b = 1;
         
         
         return new Line(a, b, xbar, ybar);
     }
  
     
     /**
      * 
      * @param a
      * @param b
      * @param c
      * @return an arraylist container the solution(s) of the quad-eqn
      * aX^2 + bX + c = 0
      */
     public static ArrayList<Double> getSolutions(double a, double b, double c)
     {
         ArrayList<Double> sols = new ArrayList<Double>();
         
         double delta = b * b - 4 * a * c;
         
         //If there're solution
         if (delta >= 0)
         {
             //Sqrt of delta
             delta = Math.sqrt(delta);
          
             //The two solutions 
             sols.add(((0 - b) + delta) / (2 * a));
             sols.add(((0 - b) - delta) / (2 * a));
         }
         
         return sols;
     }
    
     
    
 }
