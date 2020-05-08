/* $Id: Evaluator.java,v 1.5 2011-01-17 16:01:36 hampelratte Exp $
  * 
  * Copyright (c) 2005, Henrik Niehaus & Lazy Bones development team
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * 1. Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright notice, 
  *    this list of conditions and the following disclaimer in the documentation 
  *    and/or other materials provided with the distribution.
  * 3. Neither the name of the project (Lazy Bones) nor the names of its 
  *    contributors may be used to endorse or promote products derived from this 
  *    software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package lazybones.programmanager.evaluation;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import lazybones.Timer;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import devplugin.Program;
 
 public class Evaluator {
     
     private static transient Logger logger = LoggerFactory.getLogger(Evaluator.class);
     
     private List<Criterion> criteria = new ArrayList<Criterion>();
     
     private List<Result> results = new ArrayList<Result>();
     
     public Evaluator() {
         criteria.add(new TitleCriterion());
         criteria.add(new DurationCriterion());
         criteria.add(new StarttimeCriterion());
     }
     
     /**
      * Evaluates a collection of programs concerning their equality to a timer
      * with the help of several criteria
      * @param candidates
      * @param timer
      * @return the most equal program
      */
     public Result evaluate(Collection<Program> candidates, Timer timer) {
         results.clear();
         for (Iterator<Program> iterator = candidates.iterator(); iterator.hasNext();) {
             Program program = iterator.next();
             int factors = 0;
             int total = 0;
             for (Criterion criterion : criteria) {
                 int percentage = Math.max(0, criterion.evaluate(program, timer));
                 total += percentage * criterion.getBoost();
                 factors += criterion.getBoost();
             }
             
             int percentage = total / factors;
             logger.trace("Total percentage for timer {} and prog {}: {}", new Object[] {timer.getTitle(), program.getTitle(), percentage});
             results.add(new Result(program, percentage));
         }
         
         if(results.size() > 0) {
            Collections.sort(results);
             return results.get(results.size() - 1);
         } else {
             return null;
         }
     }
 }
