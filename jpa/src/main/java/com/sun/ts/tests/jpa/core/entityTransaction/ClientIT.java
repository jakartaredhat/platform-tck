/*
 * Copyright (c) 2007, 2023 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.ts.tests.jpa.core.entityTransaction;

import java.util.Properties;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;

import com.sun.ts.lib.util.TestUtil;
import com.sun.ts.tests.jpa.common.PMClientBase;

import jakarta.persistence.EntityTransaction;

@ExtendWith(ArquillianExtension.class)
@TestInstance(Lifecycle.PER_CLASS)

public class ClientIT extends PMClientBase {

  Properties props = null;

  public ClientIT() {
  }
  
  @Deployment(testable = false, managed = false)
 	public static JavaArchive createDeployment() throws Exception {

 		String pkgNameWithoutSuffix = ClientIT.class.getPackageName();
 		String pkgName = ClientIT.class.getPackageName() + ".";
 		String[] classes = { };
 		return createDeploymentJar("jpa_core_entityTransaction.jar", pkgNameWithoutSuffix, classes);

 	}



  @BeforeAll
  public void setup() throws Exception {
    TestUtil.logTrace("setup");
    try {
      super.setup();
    } catch (Exception e) {
      TestUtil.logErr("Exception: ", e);
      throw new Exception("Setup failed:", e);
    }
  }

  @AfterAll
  public void cleanup() throws Exception {
    // Nothing to cleanup
    TestUtil.logTrace("done cleanup, calling super.cleanup");
    super.cleanup();
  }

  /*
   * @testName: beginIllegalStateExceptionTest
   * 
   * @assertion_ids: PERSISTENCE:JAVADOC:539; PERSISTENCE:SPEC:608;
   * 
   * @test_Strategy: Call begin twice and verify IllegalStateException is thrown
   */
  @Test
  public void beginIllegalStateExceptionTest() throws Exception {
    boolean pass = false;
    try {
      EntityTransaction t = getEntityTransaction();
      t.begin();
      if (t.isActive()) {
        try {
          t.begin();
          TestUtil.logErr("IllegalStateException was not thrown");
        } catch (IllegalStateException ise) {
          TestUtil.logTrace("IllegalStateException Caught as Expected.");
          pass = true;
        }
      } else {
        TestUtil
            .logErr("isActive() returned false when a transaction was active");
      }
    } catch (Exception e) {
      TestUtil.logErr("Unexpected exception occurred", e);
    } finally {
      try {
        if (getEntityTransaction().isActive()) {
          getEntityTransaction().rollback();
        }
      } catch (Exception fe) {
        TestUtil.logErr("Unexpected exception rolling back TX:", fe);
      }
    }

    if (!pass) {
      throw new Exception("beginIllegalStateExceptionTest failed");
    }
  }

  /*
   * @testName: commitIllegalStateExceptionTest
   * 
   * @assertion_ids: PERSISTENCE:JAVADOC:540; PERSISTENCE:SPEC:608;
   * 
   * @test_Strategy: Call commit without an active transaction verify
   * IllegalStateException is thrown
   */
  @Test
  public void commitIllegalStateExceptionTest() throws Exception {
    boolean pass = false;
    try {
      EntityTransaction t = getEntityTransaction();
      if (!t.isActive()) {
        try {
          t.commit();
          TestUtil.logErr("IllegalStateException was not thrown");
        } catch (IllegalStateException ise) {
          TestUtil.logTrace("IllegalStateException Caught as Expected.");
          pass = true;
        }
      } else {
        TestUtil.logErr(
            "isActive() returened true when no transaction  was active");
      }
    } catch (Exception e) {
      TestUtil.logErr("Unexpected exception occurred", e);
    } finally {
      try {
        if (getEntityTransaction().isActive()) {
          getEntityTransaction().rollback();
        }
      } catch (Exception fe) {
        TestUtil.logErr("Unexpected exception rolling back TX:", fe);
      }
    }

    if (!pass) {
      throw new Exception("commitIllegalStateExceptionTest failed");
    }
  }

  /*
   * @testName: getRollbackOnlyIllegalStateExceptionTest
   * 
   * @assertion_ids: PERSISTENCE:JAVADOC:542; PERSISTENCE:SPEC:608;
   * 
   * @test_Strategy: Call getRollbackOnly without an active transaction verify
   * IllegalStateException is thrown
   */
  @Test
  public void getRollbackOnlyIllegalStateExceptionTest() throws Exception {
    boolean pass = false;
    try {
      EntityTransaction t = getEntityTransaction();
      if (!t.isActive()) {
        try {
          t.getRollbackOnly();
          TestUtil.logErr("IllegalStateException was not thrown");
        } catch (IllegalStateException ise) {
          TestUtil.logTrace("IllegalStateException Caught as Expected.");
          pass = true;
        }
      } else {
        TestUtil
            .logErr("isActive() returned true when no transaction  was active");
      }
    } catch (Exception e) {
      TestUtil.logErr("Unexpected exception occurred", e);
    } finally {
      try {
        if (getEntityTransaction().isActive()) {
          getEntityTransaction().rollback();
        }
      } catch (Exception fe) {
        TestUtil.logErr("Unexpected exception rolling back TX:", fe);
      }
    }

    if (!pass) {
      throw new Exception("getRollbackOnlyIllegalStateExceptionTest failed");
    }
  }

  /*
   * @testName: rollbackIllegalStateExceptionTest
   * 
   * @assertion_ids: PERSISTENCE:JAVADOC:544; PERSISTENCE:SPEC:608;
   * 
   * @test_Strategy: Call rollback without an active transaction verify
   * IllegalStateException is thrown
   */
  @Test
  public void rollbackIllegalStateExceptionTest() throws Exception {
    boolean pass = false;

    try {
      EntityTransaction t = getEntityTransaction();
      if (!t.isActive()) {
        try {
          t.rollback();
          TestUtil.logErr("IllegalStateException was not thrown");
        } catch (IllegalStateException ise) {
          TestUtil.logTrace("IllegalStateException Caught as Expected.");
          pass = true;
        }
      } else {
        TestUtil
            .logErr("isActive() returned true when no transaction  was active");
      }
    } catch (Exception e) {
      TestUtil.logErr("Unexpected exception occurred", e);
    } finally {
      try {
        if (getEntityTransaction().isActive()) {
          getEntityTransaction().rollback();
        }
      } catch (Exception fe) {
        TestUtil.logErr("Unexpected exception rolling back TX:", fe);
      }
    }
    if (!pass) {
      throw new Exception("rollbackIllegalStateExceptionTest failed");
    }
  }

  /*
   * @testName: setRollbackOnlyIllegalStateExceptionTest
   * 
   * @assertion_ids: PERSISTENCE:JAVADOC:546; PERSISTENCE:SPEC:608;
   * 
   * @test_Strategy: Call setRollbackOnly without an active transaction verify
   * IllegalStateException is thrown
   */
  @Test
  public void setRollbackOnlyIllegalStateExceptionTest() throws Exception {
    boolean pass = false;
    try {
      EntityTransaction t = getEntityTransaction();
      if (!t.isActive()) {
        try {
          t.setRollbackOnly();
          TestUtil.logErr("IllegalStateException was not thrown");
        } catch (IllegalStateException ise) {
          TestUtil.logTrace("IllegalStateException Caught as Expected.");
          pass = true;
        }
      } else {
        TestUtil
            .logErr("isActive() returned true when no transaction  was active");
      }
    } catch (Exception e) {
      TestUtil.logErr("Unexpected exception occurred", e);
    } finally {
      try {
        if (getEntityTransaction().isActive()) {
          getEntityTransaction().rollback();
        }
      } catch (Exception fe) {
        TestUtil.logErr("Unexpected exception rolling back TX:", fe);
      }
    }

    if (!pass) {
      throw new Exception("setRollbackOnlyIllegalStateExceptionTest failed");
    }
  }

}
