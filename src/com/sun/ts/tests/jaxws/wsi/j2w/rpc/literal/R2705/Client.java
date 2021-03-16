/*
 * Copyright (c) 2007, 2021 Oracle and/or its affiliates. All rights reserved.
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
 * $Id$
 */

package com.sun.ts.tests.jaxws.wsi.j2w.rpc.literal.R2705;

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.javatest.Status;
import com.sun.ts.tests.jaxws.sharedclients.ClientFactory;
import com.sun.ts.tests.jaxws.sharedclients.SOAPClient;
import com.sun.ts.tests.jaxws.sharedclients.rpclitclient.*;
import com.sun.ts.tests.jaxws.wsi.constants.DescriptionConstants;
import com.sun.ts.tests.jaxws.wsi.constants.SOAPConstants;
import com.sun.ts.tests.jaxws.wsi.utils.DescriptionUtils;

import com.sun.ts.lib.harness.*;

public class Client extends ServiceEETest
    implements DescriptionConstants, SOAPConstants {
  /**
   * The client.
   */
  private SOAPClient client;

  static J2WRLShared service = null;

  /**
   * The style.
   */
  private String style;

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
   * @testName: testOperationStyles
   *
   * @assertion_ids: WSI:SPEC:R2705
   *
   * @test_Strategy: Retrieve the WSDL, generated by the Java-to-WSDL tool, and
   *                 examine the wsdl:binding elements to ensure that their
   *                 wsdl:operation's soap:operation elements have the same
   *                 style.
   *
   * @throws Fault
   */
  public void testOperationStyles() throws Fault {
    Document document = client.getDocument();
    Element[] bindings = DescriptionUtils.getBindings(document);
    for (int i = 0; i < bindings.length; i++) {
      verifyBinding(bindings[i]);
    }
  }

  protected void verifyBinding(Element element) throws Fault {
    style = null;
    Element[] children;
    children = DescriptionUtils.getChildElements(element, WSDL_NAMESPACE_URI,
        WSDL_OPERATION_LOCAL_NAME);
    for (int i = 0; i < children.length; i++) {
      verifyOperation(children[i]);
    }
  }

  protected void verifyOperation(Element element) throws Fault {
    Element[] children = DescriptionUtils.getChildElements(element,
        SOAP_NAMESPACE_URI, SOAP_OPERATION_LOCAL_NAME);
    if (children.length == 0) {
      throw new Fault("Required soap:operation element not found (BP-R2705)");
    }
    verifySOAPOperation(children[0]);
  }

  protected void verifySOAPOperation(Element element) throws Fault {
    String style = element.getAttribute(SOAP_STYLE_ATTR);
    if (style.length() == 0) {
      style = DescriptionUtils.SOAP_DOCUMENT;
    }
    if (this.style == null) {
      this.style = style;
    } else {
      if (!this.style.equals(style)) {
        throw new Fault("soap:operation style '" + style
            + "' conflicts with previous style '" + this.style
            + "' (BP-R2705)");
      }
    }
  }
}
