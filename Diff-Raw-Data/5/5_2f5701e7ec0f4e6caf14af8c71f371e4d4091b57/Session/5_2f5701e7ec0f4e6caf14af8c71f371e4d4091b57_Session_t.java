 /*
  * $Id$
  * (c) Copyright 2000 wingS development team.
  *
  * This file is part of wingS (http://wings.mercatis.de).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 
 package org.wings.session;
 
 import java.beans.PropertyChangeListener;
 import java.util.Locale;
 
 import javax.servlet.ServletConfig;
 
 import org.wings.plaf.CGManager;
 import org.wings.externalizer.ExternalizeManager;
 
 /**
  * A Session contains information which is associated with one
  * user session. More doc needed.
  *
  * @author <a href="mailto:engels@mercatis.de">Holger Engels</a>
  * @version $Revision$
  */
 public interface Session
 {
     String LOCALE_PROPERTY = "locale";
     String LOOK_AND_FEEL_PROPERTY = "lookAndFeel";
 
     /**
      * used by servlet session to initialize session
      */
     void init(ServletConfig config);
 
     void setExternalizeManager(ExternalizeManager em);
 
     void putService(Object key, Service service);
 
     Service getService(Object key);
 
     CGManager getCGManager();
 
     ExternalizeManager getExternalizeManager();
 
     void addPropertyChangeListener(PropertyChangeListener listener);
 
     void removePropertyChangeListener(PropertyChangeListener listener);
 
    void addPropertyChangeListener(String propertyName,
                                   PropertyChangeListener listener);
 
     void removePropertyChangeListener(String propertyName,
                                       PropertyChangeListener listener);
 
     void setLocale(Locale l);
 
     Locale getLocale();
 
     /**
      * Get char set of this session (i. e. iso-8859-1).
      *
      * @return Char set of this session
      */
     String getCharSet();
 }
 
 /*
  * Local variables:
  * c-basic-offset: 4
  * indent-tabs-mode: nil
  * End:
  */
