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
 package net.epsilony.mf.process.integrate.core.oned;
 
 import java.util.concurrent.locks.ReentrantLock;
 
 import net.epsilony.mf.model.load.MFLoad;
 import net.epsilony.mf.model.load.SegmentLoad;
 import net.epsilony.mf.process.MFProcessType;
 import net.epsilony.mf.process.integrate.unit.SubLineDomain;
 import net.epsilony.mf.util.LockableHolder;
 import net.epsilony.tb.analysis.Math2D;
 import net.epsilony.tb.solid.Line;
 import net.epsilony.tb.solid.Segment2DUtils;
 
 /**
  * @author Man YUAN <epsilon@epsilony.net>
  * 
  */
 public class SubLineIntegratorCore extends AbstractLineIntegratorCore {
 
     public SubLineIntegratorCore(MFProcessType processType) {
         super(processType);
     }
 
     @Override
     public void integrate() {
         SubLineDomain sublineDomain = (SubLineDomain) integrateUnit;
         Line startLine = (Line) sublineDomain.getStartSegment();
         Line endLine = (Line) sublineDomain.getEndSegment();
         linearQuadratureSupport.setStartEndCoords(
                 Segment2DUtils.chordPoint(startLine, sublineDomain.getStartParameter(), null),
                 Segment2DUtils.chordPoint(endLine, sublineDomain.getEndParameter(), null));
         linearQuadratureSupport.reset();
         Line line = startLine;
         while (linearQuadratureSupport.hasNext()) {
             linearQuadratureSupport.next();
             integratePoint.setCoord(linearQuadratureSupport.getLinearCoord());
             integratePoint.setWeight(linearQuadratureSupport.getLinearWeight());
 
             line = getLineWhereCoordAt(line, endLine, integratePoint.getCoord());
            double parameter= Math2D.distance(integratePoint.getCoord(), line.getStartCoord())/line.length();
             LockableHolder<MFLoad> lockableHolder = loadMap.get(line);
             if (null == lockableHolder) {
                 lockableHolder = loadMap.get(line.getParent());
             }
             if (null == lockableHolder) {
                 integratePoint.setLoad(null);
             } else {
                 ReentrantLock lock = lockableHolder.getLock();
                 try {
                     lock.lock();
                     SegmentLoad load = (SegmentLoad) lockableHolder.getData();
                     load.setSegment(line);
                    load.setParameter(parameter);
                     integratePoint.setLoad(load.getValue());
                     integratePoint.setLoadValidity(load.getValidity());
                 } finally {
                     lock.unlock();
                 }
             }
             if (processType == MFProcessType.NEUMANN || processType == MFProcessType.DIRICHLET) {
                 integratePoint.setBoundary(line);
                 integratePoint.setBoundaryParameter(linearQuadratureSupport.getLinearParameter());
             }
             subIntegratorCore.setIntegrateUnit(integratePoint);
             subIntegratorCore.integrate();
         }
     }
 
     private Line getLineWhereCoordAt(Line startLine, Line endLine, double[] coord) {
         Line line = startLine;
         if (null != endLine && startLine != endLine) {
             double coordToStart = Math2D.distanceSquare(coord, startLine.getStartCoord());
             do {
                 double lineEndToStart = Math2D.distanceSquare(line.getEndCoord(), startLine.getStartCoord());
                 if (coordToStart > lineEndToStart) {
                     line = (Line) line.getSucc();
                 } else {
                     break;
                 }
             } while (line != endLine);
 
         }
         return line;
 
     }
 }
