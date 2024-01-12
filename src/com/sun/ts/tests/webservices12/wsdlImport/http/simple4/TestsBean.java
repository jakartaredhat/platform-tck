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

package com.sun.ts.tests.webservices12.wsdlImport.http.simple4;

import jakarta.ejb.Stateless;
import jakarta.jws.WebService;

@WebService(portName = "TestsPort", serviceName = "Simple4HttpSvc", targetNamespace = "http://Simple4HttpSvc.org/wsdl", wsdlLocation = "META-INF/wsdl/Simple4HttpSvc.wsdl", endpointInterface = "com.sun.ts.tests.webservices12.wsdlImport.http.simple4.Tests")
@Stateless(name = "WSSimple4Http")
public class TestsBean {

  public String invokeTest1() {
    return "Hello";
  }

}