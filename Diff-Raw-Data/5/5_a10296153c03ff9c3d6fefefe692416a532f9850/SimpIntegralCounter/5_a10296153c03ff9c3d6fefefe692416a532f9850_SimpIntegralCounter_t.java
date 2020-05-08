 /*
  * Copyright (C) 2013 Man YUAN <epsilon@epsilony.net>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package net.epsilony.mf.process.integrate.aspect;
 
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 
 import net.epsilony.mf.process.MFProcessType;
 import net.epsilony.mf.process.integrate.core.MFIntegratorCore;
 import net.epsilony.mf.process.integrate.unit.MFIntegrateUnit;
 import net.epsilony.tb.synchron.SynchronizedIterator;
 
 import org.aspectj.lang.JoinPoint;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.BeansException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 
 /**
  * @author Man YUAN <epsilon@epsilony.net>
  * 
  */
 
 public class SimpIntegralCounter extends AbstractIntegralAspect implements ApplicationContextAware {
 
     public static final Logger logger = LoggerFactory.getLogger(SimpIntegralCounter.class);
     long lastCountLogTime = 0;
    long logTimeGapInMillis = 500;
 
     private int volCount = 0;
     private int volSize;
     private int neuCount = 0;
     private int neuSize;
     private int diriCount = 0;
     private int diriSize;
     private ApplicationContext context;
 
     @Override
     @SuppressWarnings("unchecked")
     public void integralUnitsInjected(JoinPoint joinPoint) {
         Map<MFProcessType, SynchronizedIterator<MFIntegrateUnit>> unitesGroup = (Map<MFProcessType, SynchronizedIterator<MFIntegrateUnit>>) joinPoint
                 .getArgs()[0];
         volSize = unitesGroup.get(MFProcessType.VOLUME).getEstimatedSize();
         neuSize = unitesGroup.get(MFProcessType.NEUMANN).getEstimatedSize();
         diriSize = unitesGroup.get(MFProcessType.DIRICHLET).getEstimatedSize();
     }
 
     @Override
     public void beforeIntegrate(JoinPoint joinPoint) {
         logger.info("with {} integral threads", context.getBean("threadNum", Integer.class));
         logger.info("integral units numbers (V, N, D) = ({}, {}, {})", volSize, neuSize, diriSize);
     }
 
     @Override
     synchronized public void integratedAUnit(JoinPoint joinPoint) {
         MFIntegratorCore integratorCore = (MFIntegratorCore) joinPoint.getTarget();
         switch (integratorCore.getProcessType()) {
         case VOLUME:
             volCount++;
             break;
         case DIRICHLET:
             diriCount++;
             break;
         case NEUMANN:
             neuCount++;
             break;
         default:
             return;
         }
         logCounts();
     }
 
     private void logCounts() {
         long gap = System.currentTimeMillis() - lastCountLogTime;
         if (gap < logTimeGapInMillis && (volCount < volSize || neuCount < neuSize || diriCount < diriSize)) {
             return;
         }
 
         logger.info("integrated (V, N, D) : {}/{}, {}/{}, {}/{}", volCount, volSize, neuCount, neuSize, diriCount,
                 diriSize);
        lastCountLogTime = System.currentTimeMillis();
     }
 
     @Override
     public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
         this.context = applicationContext;
     }
 
     public void setLoggerTimeGap(long duration, TimeUnit unit) {
         logTimeGapInMillis = unit.toMillis(duration);
     }
 }
