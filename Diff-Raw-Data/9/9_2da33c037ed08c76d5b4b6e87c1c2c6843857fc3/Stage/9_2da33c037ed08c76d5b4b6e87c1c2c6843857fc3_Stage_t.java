 /*
  *  LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 43):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a non alcohol-free beer in return.
  *
  * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf@googlemail.com>
  */
 package org.lafayette.server;
 
 /**
  * Defines the stage in which the server runs.
  *
  * Supported stages at the moment are:
  * <ul>
  * <li>{@link Stages#DEVELOPMENT development stage}
  * <li>{@link Stages#PRODUCTION production stage}
  * </ul>
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class Stage {
 
     /**
      * Default stage used as fallback is {@value Stages#PRODUCTION}.
      */
     private static final Stages DEFUALT_STAGE = Stages.PRODUCTION;
     /**
      * Current stage.
      */
     private final Stages stage;
 
     /**
     * Convenience constructor which initializes the current stage with {@link #DEFUALT_STAGE}.
      */
     public Stage() {
         this(DEFUALT_STAGE);
     }
 
     /**
      * Convenience constructor which maps given string to a {@link Stages stage}
      * via {@link Stages#map(java.lang.String)}.
      *
      * @param stageFromEnviroment mapped environment name
      */
     public Stage(final String stageFromEnviroment) {
         this(Stages.map(stageFromEnviroment));
     }
 
     /**
      * Dedicated constructor.
      *
      * @param s current stage
      */
     public Stage(final Stages s) {
         super();
         stage = s;
     }
 
     /**
      * Get the current stage.
      *
      * @return never {@code null}
      */
     public Stages getStage() {
         return stage;
     }
 
     @Override
     public String toString() {
         return stage.toString();
     }
 
     /**
      * Supported stages.
      */
     public static enum Stages {
 
         /**
          * Development stage.
          */
         DEVELOPMENT,
         /**
          * Production stage.
          */
         PRODUCTION;
 
         @Override
         public String toString() {
             return name();
         }
 
         /**
          * Try to find an according {@link Stages stage} for a given name.
          *
          * Supported names are:
          * <ul>
          * <li>development
          * <li>production
          * </ul>
          *
          * @param name case does not matter
          * @return never returns {@code null}, if no proper stage was found by the name {@value Stage#DEFUALT_STAGE}
          *         will be returned
          */
         static Stages map(final String name) {
             if ("development".equalsIgnoreCase(name)) {
                 return DEVELOPMENT;
             } else if ("production".equalsIgnoreCase(name)) {
                 return PRODUCTION;
             }
 
             return DEFUALT_STAGE;
         }
     }
 }
