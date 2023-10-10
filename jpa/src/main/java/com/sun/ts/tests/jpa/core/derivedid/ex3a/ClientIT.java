/*
 * Copyright (c) 2009, 2023 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.ts.tests.jpa.core.derivedid.ex3a;

import java.util.List;

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
		String[] classes = { pkgName + "DID3Dependent", pkgName + "DID3DependentId", pkgName + "DID3Employee",
				pkgName + "DID3EmployeeId"};
		return createDeploymentJar("jpa_core_derivedid_ex3a.jar", pkgNameWithoutSuffix, classes);

	}


@BeforeAll
  public void setup() throws Exception {
    TestUtil.logTrace("setup");
    try {
      super.setup();
      removeTestData();
    } catch (Exception e) {
      TestUtil.logErr("Exception: ", e);
      throw new Exception("Setup failed:", e);
    }
  }

  /*
   * @testName: DIDTest
   *
   * @assertion_ids: PERSISTENCE:SPEC:1341; PERSISTENCE:SPEC:1182;
   * PERSISTENCE:SPEC:1183; PERSISTENCE:SPEC:1184; PERSISTENCE:SPEC:1185;
   *
   * @test_Strategy: Derived Identifier The parent entity uses EmbeddedId Case
   * (a): The dependent entity uses IdClass
   */
@Test
  public void DIDTest() throws Exception {
    boolean pass = false;

    try {

      getEntityTransaction().begin();
      final DID3EmployeeId eId1 = new DID3EmployeeId("Java", "Duke");
      final DID3EmployeeId eId2 = new DID3EmployeeId("C", "foo");

      final DID3Employee employee1 = new DID3Employee(eId1);
      final DID3Employee employee2 = new DID3Employee(eId2);

      final DID3DependentId depId1 = new DID3DependentId("Obama", eId1);
      final DID3DependentId depId2 = new DID3DependentId("Michelle", eId1);
      final DID3DependentId depId3 = new DID3DependentId("John", eId2);

      final DID3Dependent dep1 = new DID3Dependent(depId1, employee1);
      final DID3Dependent dep2 = new DID3Dependent(depId2, employee1);
      final DID3Dependent dep3 = new DID3Dependent(depId3, employee2);

      getEntityManager().persist(dep1);
      getEntityManager().persist(dep2);
      getEntityManager().persist(dep3);

      getEntityManager().persist(employee1);
      getEntityManager().persist(employee2);

      getEntityManager().flush();
      TestUtil.logTrace("persisted Employees and Dependents");

      // Refresh dependent
      DID3Dependent newDependent = getEntityManager().find(DID3Dependent.class,
          depId1);
      if (newDependent != null) {
        getEntityManager().refresh(newDependent);
      }

      List depList = getEntityManager().createQuery(
          "Select d from DID3Dependent d where d.name2='Obama' and d.emp.empId.firstName='Java'")
          .getResultList();
      newDependent = null;
      if (depList.size() > 0) {
        newDependent = (DID3Dependent) depList.get(0);
        if (newDependent == dep1) {
          pass = true;
          TestUtil.logTrace("Received Expected Dependent");
        } else {
          TestUtil.logErr("Searched Dependent not found");
        }
      } else {
        TestUtil.logErr("getEntityManager().createQuery returned null entry");
      }

      getEntityTransaction().commit();
    } catch (Exception e) {
      TestUtil.logErr("Unexpected exception occurred", e);
      getEntityTransaction().rollback();
    }

    if (!pass) {
      throw new Exception("DTDTest failed");
    }
  }

@AfterAll
  public void cleanup() throws Exception {
    TestUtil.logTrace("cleanup");
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
      getEntityManager().createNativeQuery("DELETE FROM DID3DEPENDENT")
          .executeUpdate();
      getEntityManager().createNativeQuery("DELETE FROM DID3EMPLOYEE")
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
