 /**
  * palava - a java-php-bridge
  * Copyright (C) 2007-2010  CosmoCode GmbH
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 
 package de.cosmocode.palava.services.tracking;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Preconditions;
 import com.google.inject.Singleton;
 
 import de.cosmocode.palava.bridge.request.HttpRequest;
 
 /**
  * Default implementation of the {@link TrackingService} which
  * logs all incoming requests using its {@link Logger}.
  *
  * @author Willi Schoenborn
  */
 @Singleton
 public final class LogTrackingService implements TrackingService {
 
    private static final Logger LOG = LoggerFactory.getLogger(LogTrackingService.class);
 
     @Override
     public void save(HttpRequest request) {
         Preconditions.checkNotNull(request, "Request");
        LOG.info("Request on {} (address={}, sessionId={}, userAgent={})", new Object[] {
             request.getRequestUri(), request.getRemoteAddress(), 
             request.getHttpSession().getSessionId(), request.getUserAgent()
         });
     }
 
 }
