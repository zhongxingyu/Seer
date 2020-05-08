 /*
     Copyright (C) 2010 LearningWell AB (www.learningwell.com), Kärnkraftsäkerhet och Utbildning AB (www.ksu.se)
 
     This file is part of GIL (Generic Integration Layer).
 
     GIL is free software: you can redistribute it and/or modify
     it under the terms of the GNU Lesser General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     GIL is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU Lesser General Public License for more details.
 
     You should have received a copy of the GNU Lesser General Public License
     along with GIL.  If not, see <http://www.gnu.org/licenses/>.
 */
 package gil.core;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.ExecutionException;
 import org.apache.log4j.Logger;
 import gil.common.AsyncResult;
 import gil.common.CurrentTime;
 import gil.io.ExternalSystemAdapter;
 import gil.common.GILConfiguration;
 import gil.common.IInvokeable;
 import gil.common.IProgressEventListener;
 import gil.common.Invoker;
 import gil.common.Result;
 import gil.common.Timeout;
 import gil.common.ValueResult;
 import gil.io.ISignalDataListener;
 
 /**
  * This is the logic separated from the controlling thread to handle the communication with the external system.
  * The method runOnce will continously be called from a controlling thread.
  *
  * Methods in this object are not thread safe unless otherwise noted.
  *
  * @author Göran Larsson @ LearningWell AB
  */
 public class ExternalSystemProcedure  implements IProcedure, ISignalDataListener {
 
     private static Logger _logger = Logger.getLogger(ExternalSystemProcedure.class);
 
     private long _frameCount = - 1;    
     private ByteBuffer _valuesBuf = null;    
     private IState _currentState = null;
 
     private final int _valuesBufSize;
     private final IntegrationContext _context;
     private final ExternalSystemAdapter _esAdapter;
     private final Timeout _readTimeout;
     private final GILConfiguration _config;
     Invoker _controlCommandInvoker = new Invoker();
     
     private volatile boolean _reconnect = false;
     private volatile int _commandExecutionFailureCount;
     private volatile int _dataWriteFailureCount;
     private volatile int _dataReadFailureCount;
     private volatile int _droppedExternalSystemFrames;
     private volatile int _writeFrameCount;
     private volatile int _readFrameCount;
     private volatile SystemStatus _disconnectedStatus = new SystemStatus(SystemStatus.UNKNOWN, "");
     private volatile Throwable _pendingException = null;
     
     /**
      * @param esAdapter The external system boundary class.
      * @param context The context data to be shared between the {@link ExternalSystemProcedure} and the
      * {@link ProcessModelProcedure}.
      * @param valuesBufSize The number of bytes that must be allocated for signal values read from the external system.
      * @param config Object containing configuration parameters that may be used by this object.
      */
     public ExternalSystemProcedure(ExternalSystemAdapter esAdapter, IntegrationContext context, int valuesBufSize, GILConfiguration config) {
         _esAdapter = esAdapter;
         _context = context;
         _config = config;
         _frameCount = _context.esFrameCount;
         _valuesBufSize = valuesBufSize;
         _readTimeout = new Timeout(config.getESAdapterReadPollRate());
         _currentState = new DisconnectedState();
         if (esAdapter.isReadEventDriven()) {
             esAdapter.setSignalDataEventListener(this);
         }
     }
 
    public void dataChanged(ByteBuffer data, SimTime origin, Result result, Throwable ex) {
        handleReceivedData(data, origin, result);
         if (ex != null) {
             _pendingException = ex;
         }
     }
 
     private void handleReceivedData(ByteBuffer data, SimTime origin, Result result) {
         if (result.isSuccess()) {
             data.rewind();
             synchronized(_context) {
                 if (!_context.pendingTransferToPM.isEmpty()) {
                     _droppedExternalSystemFrames += _context.pendingTransferToPM.size();
                     _logger.warn(String.format("Dropped %d ES-frame(s) due to still pending transfers.", _context.pendingTransferToPM.size()));
                     _context.pendingTransferToPM.clear();
                 }
                 _context.pendingTransferToPM.add(new Data(data, origin));
             }
             ++_readFrameCount;
         }
         else {
             _logger.warn("Failed to read: " + result.getErrorDescription());
             ++_dataReadFailureCount;
         }
     }
 
     public void runOnce(long currentTimeInMilliseconds) {
         _currentState = _currentState.handle(currentTimeInMilliseconds);
         _controlCommandInvoker.executeNextCommandInQueue();
     }
 
     public Statistics getStatistics() {
         return new Statistics(_droppedExternalSystemFrames, _commandExecutionFailureCount,
                 _dataWriteFailureCount, _dataReadFailureCount, _readFrameCount, _writeFrameCount);
     }
 
     public int getExternalSystemState() {
         if (!_esAdapter.canReportState())
             return SimState.NOT_AVAILABLE;
         if (_currentState instanceof ConnectedState)
             return _esAdapter.getState();
         return SimState.UNKNOWN;
     }
 
     public SystemStatus getExternalSystemStatus() {
         if (!_esAdapter.canReportStatus())
             return new SystemStatus(SystemStatus.NOT_AVAILABLE, "The adapter does not support status reporting.");
         if (_currentState instanceof ConnectedState)
             return _esAdapter.getStatus();
         if (_currentState instanceof DisconnectedState)
             return _disconnectedStatus;
 
         return new SystemStatus(SystemStatus.UNKNOWN, "Not connected");
     }
 
     public IState currentState() {        
         return _currentState;
     }
 
     public void reconnect() {
         _reconnect = true;
     }
     
     public Map<String, String> invokeControlCommand(final String commandID, final Map<String, String> parameters)
             throws InterruptedException, ExecutionException {
         if (commandID.equals("reconnect")) {
             reconnect();
         }
         else {
             AsyncResult result = _controlCommandInvoker.schedule(new IInvokeable() {
                 public Object invoke() throws Exception {
                     Command cmd = new Command(commandID, parameters, new SimTime());
                     try {
                         return _esAdapter.invokeControlCommand(cmd);
                     } catch(Exception ex) {
                         _logger.error("Error when invoking control command '" + cmd.toString() + "'.", ex);
                         throw ex;
                     }
                 }
             });
             return (Map<String, String>) result.get(); // Block until async operation completes
         }
         return new HashMap<String, String>();
     }
 
     public class DisconnectedState implements IState {
         public IState handle(long currentTimeInMilliseconds)  {
             try {
                 _logger.debug("Try connect...");
                 _pendingException = null;
                 if (!_esAdapter.connect()) {
                     try {
                         Thread.sleep(1000);
                     } catch (InterruptedException ex) { /* ignore */ }
                     return this;
                 }
                 synchronized(_context) {
                     _context.pendingTransferToPM.clear();
                 }
                 _reconnect = false;
                 return new ConnectedState();
             } catch(IOException ex) {
                  _disconnectedStatus = new SystemStatus(SystemStatus.NOK, ex.getMessage());
                 _logger.error("Failure when connecting: " + ex.getMessage());
                 _logger.debug(ex.getMessage(), ex);
                 _esAdapter.disconnect();
                 return this;
             }
         }
     }
 
     public class ConnectedState implements IState {
         public IState handle(long currentTimeInMilliseconds)  {
             try {
                 if (_esAdapter.isSynchronous()) {
                     long newFrameCount = _context.esFrameCount;
                     for (long i = _frameCount; i < newFrameCount; i++) {
                         Result result = _esAdapter.timeStepControl();
                         if (!result.isSuccess()) {
                             _logger.warn("Unsuccessful call to timeStepControl. May cause the external system to lag");
                             ++_dataWriteFailureCount;
                         }
                     }
                     _frameCount = newFrameCount;
                 }
 
                 if (_esAdapter.isReadEventDriven()) {
                     if (_pendingException != null)
                         throw _pendingException;
                 } 
                 else {
                     if (_valuesBuf == null) {
                          // Must allocate direct since the buffer may be used across boundaries to native code (JNI).
                         _valuesBuf = ByteBuffer.allocateDirect(_valuesBufSize);
                         _valuesBuf.order(_config.getESAdapterByteOrder());
                     }
 
                     if (_readTimeout.isTimeout(currentTimeInMilliseconds)) {
                         _readTimeout.reset(currentTimeInMilliseconds);
                         ValueResult<SimTime> result = _esAdapter.readSignalData(_valuesBuf);
 
                         if (result != null) {
                             handleReceivedData(_valuesBuf, result.getReturnValue(), result);
                         }
                          // Must allocate direct since the buffer may be used across boundaries to native code (JNI).
                         _valuesBuf = ByteBuffer.allocateDirect(_valuesBufSize);
                         _valuesBuf.order(_config.getESAdapterByteOrder());
                     }
                 }
 
                 Command nextCommand;
                 synchronized(_context) {
                     nextCommand = _context.pendingSimCommands.poll();
                 }
                 if (_esAdapter.expectsSimulatorCommands()) {
                     if (nextCommand != null) {
                         _logger.debug("Executing command: " + nextCommand.getID());
                         Result cmdResult = _esAdapter.executeSimCommand(nextCommand);
                         if (cmdResult.isSuccess()) {
                             _logger.debug("Command executed: " + nextCommand.getID());
                         }
                         else {
                             _logger.warn("Failed to execute command " + nextCommand.toString() + ": " + cmdResult.getErrorDescription());
                             ++_commandExecutionFailureCount;
                         }
                     }
                 }
                 Data values;
                 synchronized(_context) {
                     values = _context.pendingTransferToES.pollLast();
                     _context.pendingTransferToES.clear();
                 }
                 if (values != null) {
                     transferSignalDataES(values);
                 }
             } catch(IOException ex) {
                 _logger.error("Failure in communication with external system. ", ex);
                 _disconnectedStatus = new SystemStatus(SystemStatus.NOK, ex.getMessage());
                 _reconnect = true;
             } catch(Throwable ex) {
                 throw new RuntimeException(ex);
             }
             if (_reconnect) {
                 _esAdapter.disconnect();                
                 return new DisconnectedState();
             }
             return this;
         }        
         private void transferSignalDataES(Data values) throws IOException {
             Result result = _esAdapter.writeSignalData(values.getData(), values.getOrigin());
             if (result.isSuccess()) {
                 ++_writeFrameCount;
             } else {
                 _logger.warn("Failed to write: " + result.getErrorDescription());
                 ++_dataWriteFailureCount;                
             }
         }
     }
 
     public class ErrorState implements IState {
         private String _cause = "";
 
         public ErrorState(String cause) {
             _cause = cause;
         }
 
         public IState handle(long currentTimeInMilliseconds)  {
             if (_reconnect) {
                 return new DisconnectedState();
             }
             return this;
         }
 
         public String getCause() {
             return _cause;
         }
     }   
 }
