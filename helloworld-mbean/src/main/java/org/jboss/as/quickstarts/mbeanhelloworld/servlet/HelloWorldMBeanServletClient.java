/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the 
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.as.quickstarts.mbeanhelloworld.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * A simple servlet 3 as client that use mbean service.
 * </p>
 * 
 * <p>
 * The servlet is registered and mapped to /HelloWorldMBeanServletClient using the {@linkplain WebServlet @HttpServlet}.
 * </p>
 * 
 * @author Jérémie Lagarde
 * 
 */
@SuppressWarnings("serial")
@WebServlet("/HelloWorldMBeanServletClient")
public class HelloWorldMBeanServletClient extends HttpServlet {

	private MBeanServer mbeanServer;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setContentType("text/html");
		PrintWriter out = resp.getWriter();
		out.write("<h1>Quickstart: Example demonstrates the use of *MBean* in JBoss AS 7.</h1>");
		try {			
			if(mbeanServer == null)
				mbeanServer = ManagementFactory.getPlatformMBeanServer();
			Set<ObjectName> names = mbeanServer.queryNames( null, null );

			String submit = (String) req.getParameter("submit");
			String mbean = (String) req.getParameter("mbean");
			String attribute = (String) req.getParameter("attribute");
			String operation = (String) req.getParameter("operation");
			
			for (ObjectName objectName : names) {
				if(objectName.getDomain().equals("quickstarts")) {
					out.write("<h3> Mbean : " + objectName.getCanonicalName() +"</h3>");
					MBeanInfo mbeanInfo = mbeanServer.getMBeanInfo(objectName);
					String callResult = null;
					if(objectName.getKeyProperty("type").equals(mbean)) {
						if("save".equals(submit)) {
							String value = (String) req.getParameter("value");
							Attribute attr = new Attribute(attribute, value);
							mbeanServer.setAttribute(objectName, attr);
						}else if("call".equals(submit)) {
							for (MBeanOperationInfo operationInfo : mbeanInfo.getOperations()) {
								if(operationInfo.getName().equals(operation)) {
									List<String> params = new ArrayList<String>();
									List<String> signature = new ArrayList<String>();
									for (MBeanParameterInfo parameterInfo : operationInfo.getSignature()) {
										params.add((String) req.getParameter(parameterInfo.getName()));	
										signature.add(parameterInfo.getType());	
									}
									callResult = (String) mbeanServer.invoke(objectName, operation, params.toArray() , signature.toArray(new String[signature.size()]));
								}
							}
							
						}
					}
					for (MBeanAttributeInfo attributeInfo : mbeanInfo.getAttributes()) {
						if(!attributeInfo.isWritable()) {
							out.write("<li>  " + attributeInfo.getName() + " : " + mbeanServer.getAttribute(objectName, attributeInfo.getName()  ) + " </li>");
						}else{
							out.write("<form> <li>  " + attributeInfo.getName() + " :  <input type=\"text\" name=\"value\" value=\"" + mbeanServer.getAttribute(objectName, attributeInfo.getName()  ) + "\"/> </input>");
							out.write("<input type=\"hidden\" name=\"attribute\" value=\""+attributeInfo.getName()+"\" />");
							out.write("<input type=\"hidden\" name=\"mbean\" value=\""+objectName.getKeyProperty("type")+"\" />");
							out.write("<input type=\"submit\" name=\"submit\" value=\"save\" />");
							out.write("</li></form>");
						}
					}
					for (MBeanOperationInfo operationInfo : mbeanInfo.getOperations()) {
						String mbeanName = objectName.getKeyProperty("type");
						out.write("<form> <li>  " + operationInfo.getName() + " :  ");
						out.write("<input type=\"hidden\" name=\"mbean\" value=\""+mbeanName+"\" />");
						out.write("<input type=\"hidden\" name=\"operation\" value=\""+ operationInfo.getName()+"\" />");
						for (MBeanParameterInfo parameterInfo : operationInfo.getSignature()) {
							out.write(parameterInfo.getName() + " : <input type=\"text\" name=\""+parameterInfo.getName()+"\" value=\"\" /> </input>");	
						}
						out.write("<input type=\"submit\" name=\"submit\" value=\"call\" />");
						if(mbeanName.equals(mbean) && operationInfo.getName().equals(operation) ) {
							out.write("<a style=\"color:#000099\"> == > " + callResult + " <a>");
						}
						out.write("</li></form> ");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			out.write("<h2>A problem occurred during the delivery of this message</h2>");
			out.write("</br>");
			out.write("<p><i>Go your the JBoss Application Server console or Server log to see the error stack trace</i></p>");
		} finally {
			if(out != null) {
				out.close();
			}
		}
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}

}
