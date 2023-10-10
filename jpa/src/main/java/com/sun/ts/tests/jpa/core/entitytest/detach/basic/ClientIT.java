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

/*
 *  $Id$
 */

package com.sun.ts.tests.jpa.core.entitytest.detach.basic;

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

@ExtendWith(ArquillianExtension.class)
@TestInstance(Lifecycle.PER_CLASS)

public class ClientIT extends PMClientBase {

  public ClientIT() {
  }
  
  @Deployment(testable = false, managed = false)
	public static JavaArchive createDeployment() throws Exception {

		String pkgNameWithoutSuffix = ClientIT.class.getPackageName();
		String pkgName = ClientIT.class.getPackageName() + ".";
		String[] classes = { pkgName + "A"};
		return createDeploymentJar("jpa_core_entitytest_detach_basic.jar", pkgNameWithoutSuffix, classes);

	}


@BeforeAll
  public void setup() throws Exception {
    TestUtil.logTrace("setup");
    try {
      super.setup();
      removeTestData();
    } catch (Exception e) {
      throw new Exception("Setup failed:", e);

    }
  }

  /*
   * BEGIN Test Cases
   */

  /*
   * @testName: detachBasicTest1
   * 
   * @assertion_ids: PERSISTENCE:SPEC:635
   * 
   * @test_Strategy: If X is a detached entity, invoking the remove method on it
   * will cause an IllegalArgumentException to be thrown or the transaction
   * commit will fail. Invoke remove on a detached entity.
   *
   */
@Test
  public void detachBasicTest1() throws Exception {
    TestUtil.logTrace("Begin detachBasicTest1");
    boolean pass = false;
    final A aRef = new A("1", "a1", 1);

    try {

      TestUtil.logTrace("Persist Instance");
      createA(aRef);

      clearCache();

      getEntityTransaction().begin();
      TestUtil.logTrace("tx started, see if entity is detached");
      if (getEntityManager().contains(aRef)) {
        TestUtil.logErr("contains method returned true; expected false"
            + " (detached), test fails.");
        pass = false;
      } else {

        try {
          TestUtil.logTrace("try remove");
          getEntityManager().remove(aRef);
        } catch (IllegalArgumentException iae) {
          TestUtil.logTrace("IllegalArgumentException caught as expected", iae);
          pass = true;
        }

      }

      TestUtil.logTrace("tx commit");
      getEntityTransaction().commit();

    } catch (Exception e) {
      TestUtil.logTrace("or, Transaction commit will fail. "
          + " Test the commit failed by testing"
          + " the transaction is marked for rollback");
      if (!pass) {
        if (e instanceof jakarta.transaction.TransactionRolledbackException
            || e instanceof jakarta.persistence.PersistenceException) {
          pass = true;
        } else {
          TestUtil.logErr(
              "Not TransactionRolledbackException nor PersistenceException, totally unexpected:",
              e);
        }
      }

    } finally {
      try {
        if (getEntityTransaction().isActive()) {
          getEntityTransaction().rollback();
        }
      } catch (Exception re) {
        TestUtil.logErr("Unexpected Exception in rollback:", re);
      }
    }

    if (!pass)
      throw new Exception("detachBasicTest1 failed");
  }

  /*
   * @testName: detachBasicTest2
   * 
   * @assertion_ids: PERSISTENCE:JAVADOC:323; PERSISTENCE:SPEC:649;
   * PERSISTENCE:SPEC:650;
   * 
   * @test_Strategy: Do a find of an entity, detached it, then modify it. Do
   * another find and verify the changes were not persisted.
   *
   */
@Test
  public void detachBasicTest2() throws Exception {
    TestUtil.logTrace("Begin detachBasicTest2");
    boolean pass = false;
    final A expected = new A("1", "a1", 1);

    try {

      TestUtil.logTrace("Persist Instance");
      createA(new A("1", "a1", 1));

      getEntityTransaction().begin();
      TestUtil.logTrace("Executing find");
      A newA = getEntityManager().find(A.class, "1");
      TestUtil.logTrace("newA:" + newA.toString());

      TestUtil.logTrace("changing name");
      newA.setAName("foobar");
      TestUtil.logTrace("newA:" + newA.toString());
      TestUtil.logTrace("executing detach");
      getEntityManager().detach(newA);
      TestUtil.logTrace("newA:" + newA.toString());

      TestUtil.logTrace("tx commit");
      getEntityTransaction().commit();
      A newAA = getEntityManager().find(A.class, "1");
      TestUtil.logTrace("newAA:" + newAA.toString());

      if (expected.equals(newAA)) {
        pass = true;
      } else {
        TestUtil.logErr(
            "Changes made to entity were persisted even though it was detached without a flush");
        TestUtil.logErr("expected A:" + expected.toString() + ", actual A:"
            + newAA.toString());

      }

    } catch (Exception e) {
      TestUtil.logErr("Unexpected exception occurred", e);
    } finally {
      try {
        if (getEntityTransaction().isActive()) {
          getEntityTransaction().rollback();
        }
      } catch (Exception re) {
        TestUtil.logErr("Unexpected Exception in rollback:", re);
      }
    }

    if (!pass)
      throw new Exception("detachBasicTest2 failed");
  }

  /*
   * Business Methods for Test Cases
   */

  private void createA(final A a) {
    TestUtil.logTrace("Entered createA method");
    getEntityTransaction().begin();
    getEntityManager().persist(a);
    getEntityTransaction().commit();
  }

  @AfterAll
  public void cleanup() throws Exception {
    TestUtil.logTrace("Cleanup data");
    removeTestData();
    TestUtil.logTrace("cleanup complete, calling super.cleanup");
    super.cleanup();
  }

  private void removeTestData() {
    TestUtil.logTrace("removeTestData");
    if (getEntityTransaction().isActive()) {
      getEntityTransaction().rollback();
    }
    try {
      getEntityTransaction().begin();
      getEntityManager().createNativeQuery("DELETE FROM AEJB_1XM_BI_BTOB")
          .executeUpdate();
      getEntityTransaction().commit();
    } catch (Exception e) {
      TestUtil.logErr("Exception encountered while removing entities:", e);
    } finally {
      try {
        if (getEntityTransaction().isActive()) {
          getEntityTransaction().rollback();
        }
      } catch (Exception re) {
        TestUtil.logErr("Unexpected Exception in removeTestData:", re);
      }
    }
  }

}
