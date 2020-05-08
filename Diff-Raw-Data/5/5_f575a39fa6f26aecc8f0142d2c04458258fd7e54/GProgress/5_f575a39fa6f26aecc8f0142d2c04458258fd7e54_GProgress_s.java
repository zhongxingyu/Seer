 /*
 
  IGO Software SL  -  info@igosoftware.es
 
  http://www.glob3.org
 
 -------------------------------------------------------------------------------
  Copyright (c) 2010, IGO Software SL
  All rights reserved.
 
  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are met:
      * Redistributions of source code must retain the above copyright
        notice, this list of conditions and the following disclaimer.
      * Redistributions in binary form must reproduce the above copyright
        notice, this list of conditions and the following disclaimer in the
        documentation and/or other materials provided with the distribution.
      * Neither the name of the IGO Software SL nor the
        names of its contributors may be used to endorse or promote products
        derived from this software without specific prior written permission.
 
  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  DISCLAIMED. IN NO EVENT SHALL IGO Software SL BE LIABLE FOR ANY
  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 -------------------------------------------------------------------------------
 
 */
 
 
 package es.igosoftware.util;
 
 import java.text.MessageFormat;
 
 
 public abstract class GProgress {
 
    private static final MessageFormat FORMATTER = new MessageFormat("{0,number,#0.00}%");
 
    private long                       _steps;
    private long                       _done;
    private final long                 _started;
    private long                       _lastInformTime;
    private final long                 _timeToInform;
 
 
    public GProgress(final long steps) {
      this(steps, 10000);
    }
 
 
   public void incrementSteps(final long delta) {
       _steps += delta;
    }
 
 
    public GProgress(final long steps,
                     final long secondsToInform) {
       _steps = steps;
       _done = 0;
 
       final long now = System.currentTimeMillis();
       _started = now;
       _lastInformTime = now;
       _timeToInform = secondsToInform * 1000;
    }
 
 
    public synchronized double getPercent() {
       //return Math.min((double) _done / _steps, 1);
       return (double) _done / _steps;
    }
 
 
    public void stepDone() {
       stepsDone(1);
    }
 
 
    public static String progressString(final double percent,
                                        final long elapsed,
                                        final long estimatedMsToFinish) {
       final int barSteps = 80;
 
       if (percent == 1) {
          return "[" + StringUtils.sharps(barSteps) + "] " + percentString(1) + " [Finished in " + GUtils.getTimeMessage(elapsed)
                 + "]";
       }
 
       final int doneBarsSteps = Math.max(Math.round((float) percent * barSteps), 0);
       final String bar = "[" + StringUtils.sharps(doneBarsSteps) + StringUtils.dashes(Math.max(barSteps - doneBarsSteps, 0))
                          + "]";
 
       if (percent <= 0.005 /* 0.5% */) {
          return bar + " " + percentString(percent);
       }
 
       return bar + " " + percentString(percent) + " (ETF: " + GUtils.getTimeMessage(estimatedMsToFinish) + ")";
    }
 
 
    private static String percentString(final double percent) {
       //return Math.round(100 * percent);
       final String formated = FORMATTER.format(new Object[] { 100 * percent });
       switch (formated.length()) {
          case 5:
             return "  " + formated;
          case 6:
             return " " + formated;
       }
       return formated;
    }
 
 
    public synchronized void stepsDone(final int steps) {
       _done += steps;
       processSteps();
    }
 
 
    public synchronized void finish() {
       _done = _steps;
       processSteps();
    }
 
 
    private void processSteps() {
       final double percent = getPercent();
 
       final long now = System.currentTimeMillis();
       final long elapsedSinceLastInform = now - _lastInformTime;
       if ((percent >= 1) || (elapsedSinceLastInform > _timeToInform)) {
          _lastInformTime = now;
 
          final long elapsed = now - _started;
          final long estimatedMsToFinish = Math.round(elapsed / percent - elapsed);
          informProgress(percent, elapsed, estimatedMsToFinish);
       }
    }
 
 
    public abstract void informProgress(final double percent,
                                        final long elapsed,
                                        final long estimatedMsToFinish);
 
 
    public static void main(final String[] args) {
       System.out.println("GProgress 0.1");
       System.out.println("-------------\n");
 
 
       final int steps = 10000;
       final GProgress progress = new GProgress(steps) {
          @Override
          public void informProgress(final double percent,
                                     final long elapsed,
                                     final long estimatedMsToFinish) {
             System.out.println("Task: " + progressString(percent, elapsed, estimatedMsToFinish));
          }
       };
 
       for (int i = 0; i < steps; i++) {
          GUtils.delay(5);
          progress.stepDone();
       }
 
    }
 
 }
