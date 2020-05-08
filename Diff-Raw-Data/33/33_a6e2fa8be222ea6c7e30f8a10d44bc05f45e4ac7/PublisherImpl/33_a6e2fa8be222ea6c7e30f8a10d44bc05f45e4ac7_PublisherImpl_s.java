 /*
  * This program is part of Zenoss Core, an open source monitoring platform.
  * Copyright (C) 2010, Zenoss Inc.
  * 
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License version 2 or (at your
  * option) any later version as published by the Free Software Foundation.
  * 
  * For complete information please visit: http://www.zenoss.com/oss/
  */
 package org.zenoss.amqp.impl;
 
 import com.rabbitmq.client.AMQP.BasicProperties;
 import org.zenoss.amqp.*;
 
 class PublisherImpl<T> implements Publisher<T> {
 
     protected final ChannelImpl channel;
     protected final Exchange exchange;
     protected final MessageConverter<T> converter;
 
     PublisherImpl(ChannelImpl channel, Exchange exchange) {
         this(channel, exchange, null);
     }
 
     PublisherImpl(ChannelImpl channel, Exchange exchange,
                   MessageConverter<T> converter) {
         this.channel = channel;
         this.exchange = exchange;
         this.converter = converter;
     }
 
     @Override
     public void publish(T body, String routingKey) throws AmqpException {
         publish(body, null, routingKey);
     }
 
     @Override
     public void publish(T body, MessagePropertiesBuilder propertiesBuilder,
                         String routingKey) throws AmqpException {
         if (propertiesBuilder == null) {
             propertiesBuilder = MessagePropertiesBuilder.newBuilder();
         }
         try {
             final byte[] rawBody;
             if (converter != null) {
                 rawBody = this.converter.toBytes(body, propertiesBuilder);
             } else {
                 rawBody = (byte[]) body;
             }
             synchronized (this.channel) {
                 this.channel.getWrapped().basicPublish(exchange.getName(),
                         routingKey, convertProperties(propertiesBuilder.build()),
                         rawBody);
             }
         } catch (Exception e) {
             throw new AmqpException(e);
         }
     }
 
     private BasicProperties convertProperties(MessageProperties properties) {
         if (properties == null) {
             return null;
         }
         // TODO: Figure out a better way to share this data and not duplicate
        BasicProperties props = new BasicProperties();
        props.setAppId(properties.getAppId());
         // props.setClusterId(?);
        props.setContentEncoding(properties.getContentEncoding());
        props.setContentType(properties.getContentType());
        props.setCorrelationId(properties.getCorrelationId());
         if (properties.getDeliveryMode() != null) {
            props.setDeliveryMode(properties.getDeliveryMode().getMode());
         }
        props.setExpiration(properties.getExpiration());
        props.setHeaders(properties.getHeaders());
        props.setMessageId(properties.getMessageId());
        props.setPriority(properties.getPriority());
        props.setReplyTo(properties.getReplyTo());
        props.setTimestamp(properties.getTimestamp());
        props.setType(properties.getType());
        props.setUserId(properties.getUserId());
        return props;
     }
 
     @Override
     public Exchange getExchange() {
         return this.exchange;
     }
 
     @Override
     public Channel getChannel() {
         return this.channel;
     }
 }
