 /**
  * Copyright (C) 2012 eXo Platform SAS.
  * 
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package com.khoinguyen.simplecaptcha.example;
 
 import static nl.captcha.Captcha.NAME;
 
 import nl.captcha.Captcha;
 import nl.captcha.audio.AudioCaptcha;
 import nl.captcha.audio.Sample;
 import nl.captcha.servlet.CaptchaServletUtil;
 
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.io.OutputStream;
 
 import javax.portlet.ActionRequest;
 import javax.portlet.ActionResponse;
 import javax.portlet.GenericPortlet;
 import javax.portlet.PortletException;
 import javax.portlet.PortletSession;
 import javax.portlet.PortletURL;
 import javax.portlet.ProcessAction;
 import javax.portlet.RenderMode;
 import javax.portlet.RenderRequest;
 import javax.portlet.RenderResponse;
 import javax.portlet.ResourceRequest;
 import javax.portlet.ResourceResponse;
 import javax.portlet.ResourceURL;
 
 /**
  * @author <a href="mailto:ndkhoi168@gmail.com">Nguyen Duc Khoi</a>
  * Feb 9, 2012
  */
 public class CaptchaPortlet extends GenericPortlet
 {
    protected int _width = 200;
 
    protected int _height = 50;
 
    @RenderMode(name = "VIEW")
    public void viewImage(RenderRequest request, RenderResponse response) throws PortletException, IOException
    {
       _generateURLs(request, response);
 
       String jspPath = "/imagecaptcha.jsp";
       ResourceURL resourceURL = response.createResourceURL();
       request.setAttribute("resourceURL", resourceURL);
       String myAction = (String) request.getAttribute("myAction");
       if (myAction != null)
       {
          if (myAction.equals(PortletConstant.CHANGE_IMAGE))
          {
             jspPath = "/imagecaptcha.jsp";
          }
          else if (myAction.equals(PortletConstant.CHANGE_AUDIO))
          {
             jspPath = "/audiocaptcha.jsp";
          }
       }
 
       Boolean status = (Boolean) request.getPortletSession().getAttribute("STATUS");
       if (status != null)
       {
          request.setAttribute("status", status);
       }
 
       getPortletContext().getRequestDispatcher(jspPath).forward(request, response);
    }
 
    private void _generateURLs(RenderRequest request, RenderResponse response)
    {
       PortletURL refreshActionURL = response.createActionURL();
       refreshActionURL.setParameter(ActionRequest.ACTION_NAME, PortletConstant.VALIDATE_IMAGE);
       request.setAttribute(PortletConstant.VALIDATE_IMAGE, refreshActionURL);
 
       PortletURL validateActionURL = response.createActionURL();
       validateActionURL.setParameter(ActionRequest.ACTION_NAME, PortletConstant.VALIDATE_AUDIO);
       request.setAttribute(PortletConstant.VALIDATE_AUDIO, validateActionURL);
 
       PortletURL changeImageModeActionURL = response.createActionURL();
       changeImageModeActionURL.setParameter(ActionRequest.ACTION_NAME, PortletConstant.CHANGE_IMAGE);
       request.setAttribute(PortletConstant.CHANGE_IMAGE, changeImageModeActionURL);
 
       PortletURL changeAudioModeActionURL = response.createActionURL();
       changeAudioModeActionURL.setParameter(ActionRequest.ACTION_NAME, PortletConstant.CHANGE_AUDIO);
       request.setAttribute(PortletConstant.CHANGE_AUDIO, changeAudioModeActionURL);
    }
 
    @ProcessAction(name = PortletConstant.VALIDATE_IMAGE)
    public void validateImageAction(ActionRequest request, ActionResponse response)
    {
       PortletSession session = request.getPortletSession();
       Captcha captcha = (Captcha) session.getAttribute(Captcha.NAME);
       session.setAttribute("STATUS", (captcha != null && captcha.isCorrect(request.getParameter("answer"))));
       session.removeAttribute(Captcha.NAME);
       request.setAttribute("myAction", PortletConstant.CHANGE_IMAGE);
    }
 
    @ProcessAction(name = PortletConstant.VALIDATE_AUDIO)
    public void validateAudioAction(ActionRequest request, ActionResponse response)
    {
       PortletSession session = request.getPortletSession();
       AudioCaptcha audioCaptcha = (AudioCaptcha) session.getAttribute(AudioCaptcha.NAME);
       session.setAttribute("STATUS", (audioCaptcha != null && audioCaptcha.isCorrect(request.getParameter("answer"))));
       session.removeAttribute(AudioCaptcha.NAME);
       request.setAttribute("myAction", PortletConstant.CHANGE_AUDIO);
    }
 
    @ProcessAction(name = PortletConstant.CHANGE_IMAGE)
    public void changeImageModeAction(ActionRequest request, ActionResponse response)
    {
       request.setAttribute("myAction", PortletConstant.CHANGE_IMAGE);
       request.getPortletSession().removeAttribute("STATUS");
    }
 
    @ProcessAction(name = PortletConstant.CHANGE_AUDIO)
    public void changeAudioModeAction(ActionRequest request, ActionResponse response)
    {
       request.setAttribute("myAction", PortletConstant.CHANGE_AUDIO);
       request.getPortletSession().removeAttribute("STATUS");
    }
 
    @Override
    public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException
    {
       PortletSession session = request.getPortletSession();
       String type = (String) request.getAttribute("myAction");
       if (type == null)
          type = PortletConstant.CHANGE_IMAGE;
 
       if (type.equals(PortletConstant.CHANGE_IMAGE))
       {
          // Process for TEXT CAPTCHA
          Captcha captcha;
          if (session.getAttribute(Captcha.NAME) == null)
          {
             captcha = new Captcha.Builder(_width, _height).addText().gimp().addNoise().addBackground().build();
             session.setAttribute(NAME, captcha);
             writeImage(response, captcha.getImage());
             return;
          }
          captcha = (Captcha) session.getAttribute(Captcha.NAME);
          writeImage(response, captcha.getImage());
       }
       else if (type.equals(PortletConstant.CHANGE_AUDIO))
       {
          // Process for AUDIO CAPTCHA
          AudioCaptcha audioCaptcha;
          if (session.getAttribute(AudioCaptcha.NAME) == null)
          {
             audioCaptcha = new AudioCaptcha.Builder().addVoice().addAnswer().build();
             session.setAttribute(AudioCaptcha.NAME, audioCaptcha);
             writeAudio(response, audioCaptcha.getChallenge());
          }
       }
    }
 
    private static void writeImage(ResourceResponse response, BufferedImage bi)
    {
       response.setProperty("Cache-Control", "private,no-cache,no-store");
       response.setContentType("image/png");
       try
       {
          CaptchaServletUtil.writeImage(response.getPortletOutputStream(), bi);
       }
       catch (IOException e)
       {
          e.printStackTrace();
       }
    }
 
    private static void writeAudio(ResourceResponse response, Sample sp)
    {
       response.setProperty("Cache-Control", "private,no-cache,no-store");
       response.setContentType("audio/wave");
       try
       {
          OutputStream os = response.getPortletOutputStream();
          CaptchaServletUtil.writeAudio(os, sp);

       }
       catch (IOException e)
       {
          e.printStackTrace();
       }
    }
 }
