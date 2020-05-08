 /**
  * This file is part of Project Control Center (PCC).
  * 
  * PCC (Project Control Center) project is intellectual property of 
  * Dmitri Anatol'evich Pisarenko.
  * 
  * Copyright 2010, 2011 Dmitri Anatol'evich Pisarenko
  * All rights reserved
  *
  **/
 
 package ru.altruix.commons.api.translationtester;
 
 import java.util.List;
 
 /**
  * @author DP118M
  *
  */
 public interface ProblemTuple {
 
    public abstract List<String> getProblematicKeys();
 
    public abstract String getCulture();
 
 }
