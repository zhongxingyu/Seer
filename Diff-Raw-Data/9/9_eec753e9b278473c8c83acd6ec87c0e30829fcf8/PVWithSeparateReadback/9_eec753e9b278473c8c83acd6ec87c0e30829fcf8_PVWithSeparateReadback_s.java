 /*-
  * Copyright © 2013 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gda.epics;
 
 import gda.observable.Observer;
 import gov.aps.jca.dbr.DBR;
 import gov.aps.jca.event.MonitorListener;
 import gov.aps.jca.event.PutListener;
 
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.util.concurrent.TimeoutException;
 
 /**
  * A {@link PV} representing an Epics Process Variable put/get PPV pair. Put calls are deffered to one PV and get the
  * other.
  */
 
 public class PVWithSeparateReadback<T> implements PV<T> {
 
 	final private PV<T> putPV;
 
 	final private ReadOnlyPV<T> getPV;
 
 	public PVWithSeparateReadback(PV<T> putPV, ReadOnlyPV<T> getPV) {
 		this.putPV = putPV;
 		this.getPV = getPV;
 	}
 	
 	@Override
 	public String toString() {
 		return MessageFormat.format("PVWithSeparateReadback({0}, {1})", putPV, getPV);
 	}
 
 	@Override
 	public void addObserver(Observer<T> observer) throws Exception {
 		getPV.addObserver(observer);
 	}
 
 	@Override
	public void deleteIObserver(Observer<T> observer) {
 		getPV.removeObserver(observer);
 	}
 
 	@Override
 	public String getPvName() {
 		return putPV.getPvName() + "/" + getPV.getPvName();
 	}
 
 	@Override
 	public T get() throws IOException {
 		return getPV.get();
 	}
 
 	@Override
 	public T getLast() throws IOException {
 		return getPV.getLast();
 	}
 
 	@Override
 	public T waitForValue(Predicate<T> predicate, double timeoutS) throws IllegalStateException, TimeoutException,
 			IOException {
 		return getPV.waitForValue(predicate, timeoutS);
 	}
 
 	@Override
 	public void setValueMonitoring(boolean shouldMonitor) throws IOException {
 		getPV.setValueMonitoring(shouldMonitor);
 	}
 
 	@Override
 	public void putNoWait(T value) throws IOException {
 		putPV.putNoWait(value);
 	}
 
 	@Override
 	public void putNoWait(T value, PutListener pl) throws IOException {
 		putPV.putNoWait(value, pl);
 	}
 
 	@Override
 	public void putWait(T value) throws IOException {
 		putPV.putWait(value);
 	}
 
 	@Override
 	public void putWait(T value, double timeoutS) throws IOException {
 		putPV.putWait(value, timeoutS);
 	}
 
 	@Override
 	public void putAsyncStart(T value) throws IOException {
 		putPV.putAsyncStart(value);
 	}
 
 	@Override
 	public void putAsyncWait() throws IOException {
 		putPV.putAsyncWait();
 	}
 
 	@Override
 	public boolean putAsyncIsWaiting() {
 		return putPV.putAsyncIsWaiting();
 	}
 
 	@Override
 	public void putAsyncCancel() {
 		putPV.putAsyncCancel();
 	}
 
 	@Override
 	public void putAsyncWait(double timeoutS) throws IOException {
 		putPV.putAsyncWait(timeoutS);
 	}
 
 	@Override
 	public gda.epics.PV.PVValues putWait(T value, ReadOnlyPV<?>... toReturn) throws IOException {
 		return putPV.putWait(value, toReturn);
 	}
 
 	@Override
 	public gda.epics.PV.PVValues putWait(T value, double timeoutS, ReadOnlyPV<?>... toReturn)
 			throws IOException {
 		return putPV.putWait(value, timeoutS, toReturn);
 	}
 
 	@Override
 	public boolean isValueMonitoring() {
 		return getPV.isValueMonitoring();
 	}
 
 	@Override
 	public void addMonitorListener(MonitorListener listener) throws IOException {
 		getPV.addMonitorListener(listener);
 	}
 
 	@Override
 	public void removeMonitorListener(MonitorListener listener) {
 		getPV.removeMonitorListener(listener);
 	}
 
 	@Override
 	public T extractValueFromDbr(DBR dbr) {
 		return getPV.extractValueFromDbr(dbr);
 	}
 
 	@Override
 	public T get(int numElements) throws IOException {
 		return getPV.get(numElements);
 	}
 
 }
