 /*
  * Copyright 2012 The Clustermeister Team.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.github.nethad.clustermeister.example.fractals;
 
 import com.github.nethad.clustermeister.api.Clustermeister;
import com.github.nethad.clustermeister.api.impl.Job;
 import com.github.nethad.clustermeister.api.impl.ClustermeisterFactory;
 import com.github.nethad.clustermeister.api.impl.JobFactory;
 import java.awt.image.BufferedImage;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.swing.SwingUtilities;
 import org.jppf.utils.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author thomas
  */
 public class ComputeMandelbrotBlocking extends AbstractComputeMandelbrot {
     private static final Logger logger = LoggerFactory.getLogger(ComputeMandelbrotBlocking.class);
     
     private Clustermeister clustermeister;
 
     protected ComputeMandelbrotBlocking(ControlsPanel controlsPanel, ImagePanel imagePanel) {
         super(controlsPanel, imagePanel);
     }
 
     public void computePicture(FractalConfiguration config) {
         try {
             if (clustermeister == null) {
                 clustermeister = ClustermeisterFactory.create();
             }
             int nbTask = config.bsize;
             logger.info("Executing " + nbTask + " tasks");
             
             Map<String, Object> jobData = new HashMap<String, Object>();
             jobData.put("config", config);
             
             Job<MandelbrotResult> job = JobFactory.create("Clustermeister Mandelbrot", jobData);
 
 
             newImage(config);
 
             for (int i = 0; i < nbTask; i++) {
                 job.addTask(new MandelbrotCMTask(i, config));
             }
             
             long start = System.currentTimeMillis();
 
             List<MandelbrotResult> results = clustermeister.executeJob(job);
 
             long elapsed = System.currentTimeMillis() - start;
             logger.info("Computation performed in " + StringUtils.toStringDuration(elapsed));
 
             generateMandelbrotImage(results, config);
             refreshImage();
 
         } catch (Exception ex) {
             ex.printStackTrace();
         }
     }
     
     private void generateMandelbrotImage(final List<MandelbrotResult> taskList, final FractalConfiguration config) throws Exception {
         int max = config.nmax;
 
         for (int j = 0; j < config.bsize; j++) {
             MandelbrotResult task = taskList.get(j);
 //          int[] values = task.getValues();
             int[] colors = task.getColors();
             for (int i = 0; i < config.asize; i++) {
                 getImage().setRGB(i, config.bsize - j - 1, colors[i]);
             }
         }
 //        try {
 ////            ImageIO.write(image, "jpeg", new File("data/mandelbrot.jpg"));
 //        } catch (Exception e) {
 //            log(e.getMessage());
 //            e.printStackTrace();
 //        }
     }
     
 }
