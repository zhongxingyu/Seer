 /* Copyright 2012 Google, Inc.
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
  */
 
 package org.arbeitspferde.friesian.utility;
 
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 import com.google.inject.ProvisionException;
 import com.google.inject.Singleton;
 
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 import org.kohsuke.args4j.Option;
 import org.reflections.Reflections;
 import org.reflections.scanners.FieldAnnotationsScanner;
 import org.reflections.util.ClasspathHelper;
 import org.reflections.util.ConfigurationBuilder;
 import org.reflections.util.FilterBuilder;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 /**
  * A {@link Provider} for {@link Settings}.
  */
 @Singleton
 public class SettingsProvider implements Provider<Settings> {
   @Option(
       name = "--rateSlopeConstant",
       aliases = {"--rsc"},
       usage = "The 'M' used to control the rate function's slope constant [rate is N per second]")
   private Double rateSlopeConstant = 1.0;
 
   @Option(
       name = "--rateInterceptConstant",
       aliases = {"--ric"},
       usage = "The 'B' used to control the rate function's intercept constant ")
   private Double rateInterceptConstant = 0.0;
 
   @Option(
       name = "--maxListSize",
       aliases = {"--mxls"},
       usage = "The maximum number of elements in the list workers are sorting")
   private Integer maxListSize = 1000000;
 
   @Option(
       name = "--minListSize",
       aliases = {"--mnls"},
       usage = "The minimum number of elements in the list workers are sorting")
   private Integer minListSize = 10000;
 
   @Option(
       name = "--maxNumberOfListPartitions",
       aliases = {"--mxnolp"},
       usage = "The maximum number of partitions a worker divides a list into")
   private Integer maxNumberOfListPartitions = 1000;
 
   @Option(
       name = "--minNumberOfListPartitions",
       aliases = {"--mnnolp"},
       usage = "The minimum number of partitions a worker divides a list into")
   private Integer minNumberOfListPartitions = 10;
 
   @Option(
       name = "--hotProbability",
       aliases = {"--hp"},
       usage = "Percentage from 0 to 100 that the hot cache is updated " +
           "(must be > --coldProbability)")
   private Integer hotProbability = 30;
 
   @Option(
       name = "--hotCacheSize",
       aliases = {"--hcs"},
       usage = "Number of elements in the hot cache.")
   private Integer hotCacheSize = 1000;
 
   @Option(
       name = "--coldProbability",
       aliases = {"--cp"},
       usage = "A percentage from 0 to 100 that the cold cache is updated [drop+hot+cold = 100%]")
   private Integer coldProbability = 10;
 
   @Option(
       name = "--coldCacheSize",
       aliases = {"--ccs"},
       usage = "Number of elements in the cold cache")
   private Integer coldCacheSize = 1000;
 
   @Option(
       name = "--workerSleepTime",
       aliases = {"--wst"},
       usage = "Number of milliseconds a worker thread sleeps")
   private Integer workerSleepTime = 1000;
 
   @Option(
       name = "--sleepProbability",
       aliases = {"--sp"},
       usage = "A percentage from 0 to 100 that the worker thread sleeps after sorting a partition")
   private Integer sleepProbability = 5;
 
   @Option(
       name = "--diurnalPeriod",
       aliases = {"--dp"},
       usage = "The amount of time in hours that the diurnal function takes before repeating")
   private Double diurnalPeriod = 24.0;
 
   @Option(
       name = "--port",
       aliases = {"--p"},
       usage = "The port to serve listener requests on.")
   private Integer port = 8080;
 
   private final String[] args;
   private final SupplementalSettingsProcessor supplementalSettingsProcessor;
 
   @Inject
     public SettingsProvider(final String[] args,
         final SupplementalSettingsProcessor supplementalSettingsProcessor) {
     this.args = args;
     this.supplementalSettingsProcessor = supplementalSettingsProcessor;
   }
 
   @Override
   public Settings get() {
     final Reflections reflections = new Reflections(new ConfigurationBuilder()
        .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix("com.google")))
        .setUrls(ClasspathHelper.forPackage("com.google"))
         .setScanners(new FieldAnnotationsScanner()));
 
     final Set<String> args4jWhitelist = new HashSet<String>();
 
     for (final Field field : reflections.getFieldsAnnotatedWith(Option.class)) {
       final Option optionAnnotation = field.getAnnotation(Option.class);
       if (optionAnnotation != null) {
         args4jWhitelist.add(optionAnnotation.name());
         final String[] aliases = optionAnnotation.aliases();
         if (aliases != null) {
           for (final String alias : aliases) {
             args4jWhitelist.add(alias);
           }
         }
       }
     }
 
     final List<String> args4jArguments = new ArrayList<String>();
     final List<String> unmatchedArguments = new ArrayList<String>();
 
     for (final String argument : args) {
       if (argument.startsWith("--")) {
         boolean wasAdded = false;
         for (final String args4jCandidate : args4jWhitelist) {
           if (!wasAdded && argument.startsWith(args4jCandidate)) {
             args4jArguments.add(argument);
             wasAdded = true;
           }
         }
         if (!wasAdded) {
           unmatchedArguments.add(argument);
         }
       }
     }
 
     final List<String> gnuSanitizedArguments = new ArrayList<String>(args4jArguments.size());
 
     for (int i = 0; i < args4jArguments.size(); i++) {
       final String str = args4jArguments.get(i);
       if (str.equals("--")) {
         while (i < args4jArguments.size()) {
           gnuSanitizedArguments.add(args4jArguments.get(i++));
         }
         break;
       }
 
       if (str.startsWith("--")) {
         final int eq = str.indexOf('=');
         if (eq > 0) {
           gnuSanitizedArguments.add(str.substring(0, eq));
           gnuSanitizedArguments.add(str.substring(eq + 1));
           continue;
         } else {
           gnuSanitizedArguments.add(str);
         }
       }
 
       gnuSanitizedArguments.add(str);
     }
 
     try {
       new CmdLineParser(this)
           .parseArgument(gnuSanitizedArguments.toArray(new String[gnuSanitizedArguments.size()]));
     } catch (final CmdLineException e) {
       throw new ProvisionException(e.getMessage());
     }
 
     final String[] unmatchedAsArray =
         unmatchedArguments.toArray(new String[unmatchedArguments.size()]);
     supplementalSettingsProcessor.process(unmatchedAsArray);
 
     return new Settings() {
       @Override
       public Double getRateSlopeConstant() {
         return rateSlopeConstant;
       }
 
       @Override
       public Double getRateInterceptConstant() {
         return rateInterceptConstant;
       }
 
       @Override
       public Integer getMaxListSize() {
         return maxListSize;
       }
 
       @Override
       public Integer getMinListSize() {
         return minListSize;
       }
 
       @Override
       public Integer getMaxNumberOfListPartitions() {
         return maxNumberOfListPartitions;
       }
 
       @Override
       public Integer getMinNumberOfListPartitions() {
         return minNumberOfListPartitions;
       }
 
       @Override
       public Integer getHotProbability() {
         return hotProbability;
       }
 
       @Override
       public Integer getHotCacheSize() {
         return hotCacheSize;
       }
 
       @Override
       public Integer getColdProbability() {
         return coldProbability;
       }
 
       @Override
       public Integer getColdCacheSize() {
         return coldCacheSize;
       }
 
       @Override
       public Integer getWorkerSleepTime() {
         return workerSleepTime;
       }
 
       @Override
       public Integer getSleepProbability() {
         return sleepProbability;
       }
 
       @Override
       public Double getDiurnalPeriod() {
         return diurnalPeriod;
       }
 
       @Override
       public Integer getPort() {
         return port;
       }
     };
   }
 }
