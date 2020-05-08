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
 
 package oracle.CubistOracle.exec;
 
 import oracle.CubistOracle.common.CubistOracle;
 import oracle.CubistOracle.common.OracleException;
 import oracle.CubistOracle.common.OracleUtil;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 /**
  * @author Diego Didona, didona@gsd.inesc-id.pt Date: 29/10/12
  */
 
 
 public class ExecCubistOracle extends CubistOracle {
 
 
    protected static final String CASES = ".cases";
    private String pathToOracle;
    private String datasetFolder;
 
    @Override
    protected void postModelCreation(String pathToModel) throws OracleException {
       //no op
    }
 
    @Override
    protected void preQuery(String s) {
       //nop
    }
 
    @Override
    protected void postQuery(String s) {
       //nop
    }
 
    public ExecCubistOracle(ExecCubistConfig config, boolean buildModel) throws OracleException {
       super(config, buildModel);
       this.pathToOracle = config.getPathToOracle();
       datasetFolder = dataSetFolder();
    }
 
    public double query(String features, String target) throws OracleException {
 
       try {
 
          this.buildCaseFile(target, features);
          //NB after "-f" it does not want to see the space!!!
          String fileStem = "-f" + datasetFolder + target;
          String command[] = {this.pathToOracle, fileStem};
          Process p = Runtime.getRuntime().exec(command);
 
          this.checkForError(p);
          String read = this.getCubistOutput(p);
          p.destroy();
          return getRealPredicted(read);
 
       } catch (IOException io) {
          throw new OracleException(io.getMessage());
       }
 
 
    }
 
    /*
      Returns the predicted value  the string is like  * int(index)  double(real) double(predicted)
      It works also with "suspicious prediction but ONLY one prediction per time
       */
    private double getRealPredicted(String s) {
       String[] split = s.split("\\s");
       return Double.parseDouble(split[split.length - 1]);
    }
 
 
    private String getCubistOutput(Process p) throws IOException {
       String read = null;
       java.io.BufferedReader stdInput = new java.io.BufferedReader(new java.io.InputStreamReader(p.getInputStream()));
 
       //first 3 lines are dummy
       for (int kk = 0; kk < 4; kk++) {
          read = stdInput.readLine();
       }
       return read;
    }
 
    private void buildCaseFile(String target, String feature) throws IOException {
       FileWriter fw = new FileWriter(new File(this.datasetFolder + target + CASES));
       fw.write(feature);
       fw.flush();
       fw.close();
    }
 
    private String dataSetFolder() {
       File f = new File(cubistConfig.getTrainingSet());
       f = f.getParentFile();
       log.trace("Returning new model " + OracleUtil.slashedPath(f.getAbsolutePath()));
       return OracleUtil.slashedPath(f.getAbsolutePath());
    }
 

 }
