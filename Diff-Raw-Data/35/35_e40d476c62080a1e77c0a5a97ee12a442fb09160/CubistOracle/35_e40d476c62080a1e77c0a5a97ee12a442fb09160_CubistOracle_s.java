 /*
  *
  *  * INESC-ID, Instituto de Engenharia de Sistemas e Computadores Investigação e Desevolvimento em Lisboa
  *  * Copyright 2013 INESC-ID and/or its affiliates and other
  *  * contributors as indicated by the @author tags. All rights reserved.
  *  * See the copyright.txt in the distribution for a full listing of
  *  * individual contributors.
  *  *
  *  * This is free software; you can redistribute it and/or modify it
  *  * under the terms of the GNU Lesser General Public License as
  *  * published by the Free Software Foundation; either version 3.0 of
  *  * the License, or (at your option) any later version.
  *  *
  *  * This software is distributed in the hope that it will be useful,
  *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  *  * Lesser General Public License for more details.
  *  *
  *  * You should have received a copy of the GNU Lesser General Public
  *  * License along with this software; if not, write to the Free
  *  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  *  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  *
  */
 
 package oracle.CubistOracle.common;
 
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.io.*;
 import java.util.Arrays;
 
 /**
  * @author Diego Didona, didona@gsd.inesc-id.pt Date: 29/10/12
  */
 public abstract class CubistOracle implements Oracle {
    protected static final String DATA = ".data";
    protected static final String MODEL = ".model";
    protected final static Log log = LogFactory.getLog(CubistOracle.class);
    protected final static boolean t = log.isTraceEnabled();
    protected final static Object monitor = new Object();
    protected String pathToCubist;
    protected CubistConfig cubistConfig;
    private File trainingSet;
    private PrintWriter trainingSetWriter;
 
    public CubistOracle(CubistConfig config, boolean buildModel) throws OracleException {
       this.cubistConfig = config;
       init(buildModel);
    }
 
    private void init(boolean buildModel) throws OracleException {
       pathToCubist = cubistConfig.getPathToCubist();
       if (t) log.trace("Successfully set the path to cubist to " + pathToCubist);
       if (buildModel)
          buildModel();
       else loadModel();
 
    }
 
    private void buildModel() throws OracleException {
       if (t)
          log.trace("Building a new model for targetFeature " + cubistConfig.getTargetFeature());
 
       if (!OracleUtil.fileExists(pathToCubist + "/cubist"))
          throw new OracleException(pathToCubist + " not found");
       if (cubistConfig.getTrainingSet() == null)
          throw new OracleException("You cannot build a model without training set");
       if (trainingSet == null) {
          trainingSet = new File(pathToCubist + "/" + cubistConfig.getTrainingSet());
          if (t) log.trace("setting training set to " + trainingSet.getAbsolutePath());
       }
       if ((!trainingSet.exists() || trainingSet.length() == 0))
          throw new OracleException("You need a non-empty training set to build a new model");
       String builtModel;
       if (t) log.trace("Going to create " + cubistConfig.getTargetFeature() + MODEL);
       builtModel = createCubistModel(cubistConfig.getTargetFeature());
       if (builtModel == null)
          throw new OracleException("Impossible to build model!");
       if (t) log.trace("PostModelCreation to go");
       synchronized (monitor) {
          postModelCreation(pathToCubist + "/" + cubistConfig.getTargetFeature());
       }
    }
 
    private void loadModel() throws OracleException {
       String builtModel;
       builtModel = cubistConfig.getModel();
       if (t) log.trace("Loading model " + builtModel);
       if (builtModel != null) {   //if you don't want to build the model yet, do not init it!
          if (t) log.trace("PostModelCreation to go");
          synchronized (monitor) {
             postModelCreation(pathToCubist + "/" + cubistConfig.getTargetFeature());
          }
       } else
          throw new OracleException("You asked to load a model for " + cubistConfig.getTargetFeature() + ", but the model is not there");
    }
 
    protected abstract void postModelCreation(String pathToModel) throws OracleException;
 
    /**
     * @param features
     * @param target
     * @param init     if true, you rebuild the model after having added the point
     * @throws OracleException
     */
    public void addPoint(String features, String target, boolean init) throws OracleException {
       if (trainingSet == null) {
          trainingSet = new File(pathToCubist + "/" + cubistConfig.getTrainingSet());
          if (t) log.trace("setting training set to " + trainingSet.getAbsolutePath());
       }
       if (trainingSetWriter == null) {
          try {
             trainingSetWriter = new PrintWriter(trainingSet);
          } catch (FileNotFoundException e) {
             throw new OracleException(e.getMessage());
          }
       }
       trainingSetWriter.println(features);
       trainingSetWriter.flush();
       if (init)
          buildModel();
    }
 
    public void addPoint(String features, boolean init) throws OracleException {
       addPoint(features, this.cubistConfig.getTargetFeature(), init);
    }
 
    public void removePoint(String features, String target) {
       throw new RuntimeException("removePoint method not supported yet");
    }
 
    private String[] buildCommand(String filestem) {
       StringBuilder sb = new StringBuilder();
       sb.append(this.pathToCubist).append("/cubist");
       sb.append(";");
       sb.append("-f");
       sb.append(pathToCubist).append("/").append(filestem);
       int instances = cubistConfig.getInstances();
       int committee = cubistConfig.getCommittee();
       if (instances > 0) {
          sb.append(";");
          sb.append("-n");
          sb.append(instances);
       }
       if (committee > 0) {
          sb.append(";");
          sb.append("-C");
          sb.append(committee);
       }
       return sb.toString().split(";");
 
    }
 
    private String createCubistModel(String filestem) {
       try {
          String[] command = buildCommand(filestem);
          if (t) log.trace("Invoking " + Arrays.toString(command));
          Process p = Runtime.getRuntime().exec(buildCommand(filestem));
          checkForError(p);
          p.destroy();
       } catch (Exception e) {
          e.printStackTrace();
          throw new RuntimeException("Could not create CubistModel " + e.getMessage());
       }
       return modelString();
    }
 
    private String modelString() {
       String modelString = cubistConfig.getPathToCubist() + "/" + cubistConfig.getTargetFeature() + MODEL;
       if (t) log.trace("Returning new model " + modelString);
       return modelString;
    }
 
    protected void checkForError(Process p) throws OracleException {
       BufferedReader stderr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
       String read;
 
       StringBuilder errorString = null;
       try {
          while ((read = stderr.readLine()) != null) {
             if (errorString == null)
                errorString = new StringBuilder();
             errorString.append(read);
             System.out.println(read);
          }
       } catch (IOException e) {
          e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
          throw new OracleException(e.getMessage());
       }
       if (errorString != null)
          throw new OracleException((read));
    }
 
    @Override
    public final double query(String features) throws OracleException {
       synchronized (monitor) {
          String model = pathToCubist + "/" + cubistConfig.getTargetFeature();
          try {
             preQuery(model);
             return query(features, cubistConfig.getTargetFeature());
          } finally {
             postQuery(model);
          }
       }
    }
 
    protected abstract void preQuery(String s);
 
    protected abstract void postQuery(String s);
 }
