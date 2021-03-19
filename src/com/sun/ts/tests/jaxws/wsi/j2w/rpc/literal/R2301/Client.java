/*
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

/*
 * @(#)Client.java	1.3	03/05/09
 */

package com.sun.ts.tests.jaxws.wsi.j2w.rpc.literal.R2301;

import java.util.Iterator;
import java.util.Properties;

import jakarta.xml.soap.SOAPBody;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPMessage;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.javatest.Status;
import com.sun.ts.tests.jaxws.sharedclients.ClientFactory;
import com.sun.ts.tests.jaxws.sharedclients.SOAPClient;
import com.sun.ts.tests.jaxws.sharedclients.rpclitclient.*;
import com.sun.ts.tests.jaxws.wsi.constants.DescriptionConstants;
import com.sun.ts.tests.jaxws.wsi.requests.SOAPRequests;
import com.sun.ts.tests.jaxws.wsi.utils.DescriptionUtils;
import com.sun.ts.lib.harness.*;

public class Client extends ServiceEETest implements DescriptionConstants {
  /**
   * The client.
   */
  private SOAPClient client;

  static J2WRLShared service = null;

  /**
   * The document.
   */
  private Document document;

  /**
   * Test entry point.
   * 
   * @param args
   *          the command-line arguments.
   */
  public static void main(String[] args) {
    Client test = new Client();
    Status status = test.run(args, System.out, System.err);
    status.exit();
  }

  /**
   * @class.testArgs: -ap jaxws-url-props.dat
   * @class.setup_props: webServerHost; webServerPort; platform.mode;
   *
   * @param args
   * @param properties
   *
   * @throws Fault
   */
  public void setup(String[] args, Properties properties) throws Fault {
    client = ClientFactory.getClient(J2WRLSharedClient.class, properties, this,
        service);
    logMsg("setup ok");
  }

  public void cleanup() {
    logMsg("cleanup");
  }

  /**
   * @testName: testOrder
   *
   * @assertion_ids: WSI:SPEC:R2301
   *
   * @test_Strategy: Retrieve the WSDL, generated by the Java-to-WSDL tool, and
   *                 locate the "getBean" operation, examine the response
   *                 message and capture the part order. A request to the
   *                 end-point should yield a response with the parts in that
   *                 particular order.
   *
   * @throws Fault
   */
  public void testOrder() throws Fault {
    document = client.getDocument();
    Element operation = getOperation();
    Element output = getOutput(operation);
    Element message = getMessage(output);
    SOAPMessage response = makeRequest();
    verifyOrder(response, message);
  }

  protected Element getOperation() throws Fault {
    Element portType = DescriptionUtils.getPortType(document,
        "J2WRLSharedEndpoint");
    if (portType == null) {
      throw new Fault(
          "Required portType 'J2WRLSharedEndpoint' not found (BP-R2301)");
    }
    Element operation = DescriptionUtils.getNamedChildElement(portType,
        WSDL_NAMESPACE_URI, WSDL_OPERATION_LOCAL_NAME, "getBean");
    if (operation == null) {
      throw new Fault("Required operation 'getBean' not found (BP-R2301)");
    }
    return operation;
  }

  protected Element getOutput(Element operation) throws Fault {
    Element output = DescriptionUtils.getChildElement(operation,
        WSDL_NAMESPACE_URI, WSDL_OUTPUT_LOCAL_NAME);
    if (output == null) {
      throw new Fault(
          "Required output for operation 'getBean' not found (BP-R2301)");
    }
    return output;
  }

  protected Element getMessage(Element output) throws Fault {
    String name = output.getAttribute(WSDL_MESSAGE_ATTR);
    int index = name.indexOf(':');
    if (index > 0) {
      name = name.substring(index + 1);
    }
    Element message = DescriptionUtils.getMessage(document, name);
    if (message == null) {
      throw new Fault(
          "Requirement message '" + name + "' for output not found (BP-R2301)");
    }
    return message;
  }

  protected SOAPMessage makeRequest() throws Fault {
    try {
      return client.makeSaajRequest(SOAPRequests.R2301_REQUEST);
    } catch (Exception e) {
      throw new Fault("Unable to make request (BP-R2301)", e);
    }
  }

  protected void verifyOrder(SOAPMessage response, Element message)
      throws Fault {
    SOAPBody body;
    try {
      body = response.getSOAPBody();
    } catch (Exception e) {
      throw new Fault("Unable to retrieve SOAP body from response (BP-R2301)",
          e);
    }
    SOAPElement beanResponse = (SOAPElement) body.getFirstChild();
    Iterator responseParts = beanResponse.getChildElements();
    Element[] messageParts = DescriptionUtils.getChildElements(message);
    for (int i = 0; i < messageParts.length; i++) {
      if (!responseParts.hasNext()) {
        throw new Fault(
            "Message has additional part(s) but response has not (BP-R2301)");
      }
      String partName1 = messageParts[i].getAttribute(WSDL_NAME_ATTR);
      SOAPElement part = (SOAPElement) responseParts.next();
      String partName2 = part.getElementName().getLocalName();
      if (!partName2.equals(partName1)) {
        throw new Fault("Expected part named '" + partName1 + "' but got '"
            + partName2 + "' (BP-R2301)");
      }
    }
  }
}
