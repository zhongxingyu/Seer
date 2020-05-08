 /*
  * Copyright (c) 2012 Tripwire, Inc.
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  *
  *  o Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  *
  *  o Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
  * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.jmxdatamart.JMXTestServer;
 
import com.sun.jmx.interceptor.DefaultMBeanServerInterceptor;
 import java.lang.management.ManagementFactory;
 import javax.management.MBeanServer;
 import javax.management.ObjectName;
 import java.io.FileWriter;
import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
 import org.jmxdatamart.Extractor.*;
 
 /**
  *
  * @author Binh Tran <mynameisbinh@gmail.com>
  */
 public class Main {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) throws Exception {
 
        RandomValueDynamicMBean dmb = new RandomValueDynamicMBean();
        dmb.addAttribute("Car", Car.class.toString(), "a Car", true, true, false);
        dmb.setAttribute(new Attribute("Car", new Car("123", "234", 3, 2)));
        
         MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName on = new ObjectName("org.jmxdatamart.jmxtestserver:type=randomDMBean");
        mbs.registerMBean(dmb, on);
     }
 }
