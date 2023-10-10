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

package com.sun.ts.tests.jpa.core.metamodelapi.mapattribute;

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

import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.MapAttribute;
import jakarta.persistence.metamodel.Metamodel;
import jakarta.persistence.metamodel.PluralAttribute;
import jakarta.persistence.metamodel.Type;

@ExtendWith(ArquillianExtension.class)
@TestInstance(Lifecycle.PER_CLASS)

public class ClientIT extends PMClientBase {

  public ClientIT() {
  }

  @Deployment(testable = false, managed = false)
	public static JavaArchive createDeployment() throws Exception {

		String pkgNameWithoutSuffix = ClientIT.class.getPackageName();
		String pkgName = ClientIT.class.getPackageName() + ".";
		String[] classes = { pkgName + "Department", pkgName + "Employee"};
		return createDeploymentJar("jpa_core_metamodelapi_mapattribute.jar", pkgNameWithoutSuffix, classes);

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
   * @testName: getJavaKeyType
   * 
   * @assertion_ids: PERSISTENCE:JAVADOC:1415
   *
   * @test_Strategy:
   *
   */
@Test
  public void getJavaKeyType() throws Exception {
    boolean pass = false;

    getEntityTransaction().begin();
    Metamodel metaModel = getEntityManager().getMetamodel();
    if (metaModel != null) {
      TestUtil.logTrace("Obtained Non-null Metamodel from EntityManager");
      ManagedType<Department> mType = metaModel.managedType(Department.class);
      if (mType != null) {
        TestUtil.logTrace("Obtained Non-null ManagedType");
        MapAttribute<Department, String, Employee> mapAttrib = mType
            .getDeclaredMap("lastNameEmployees", java.lang.String.class,
                com.sun.ts.tests.jpa.core.metamodelapi.mapattribute.Employee.class);
        Class javaKeyType = mapAttrib.getKeyJavaType();

        if (javaKeyType.getName().equals("java.lang.String")) {
          TestUtil
              .logTrace("Received Expected Map Attribute's Java Key Type  = "
                  + javaKeyType.getName());
          pass = true;

        } else {
          TestUtil
              .logTrace("Received UnExpected Map Attribute's Java Key Type  = "
                  + javaKeyType.getName());
        }
      }
    }

    getEntityTransaction().commit();

    if (!pass) {
      throw new Exception("getJavaKeyType Test  failed");
    }
  }

  /*
   * @testName: getKeyType
   * 
   * @assertion_ids: PERSISTENCE:JAVADOC:1416
   *
   * @test_Strategy:
   *
   */
@Test
  public void getKeyType() throws Exception {
    boolean pass = false;

    getEntityTransaction().begin();
    Metamodel metaModel = getEntityManager().getMetamodel();
    if (metaModel != null) {
      TestUtil.logTrace("Obtained Non-null Metamodel from EntityManager");
      ManagedType<Department> mType = metaModel.managedType(Department.class);
      if (mType != null) {
        TestUtil.logTrace("Obtained Non-null ManagedType");
        MapAttribute<Department, String, Employee> mapAttrib = mType
            .getDeclaredMap("lastNameEmployees", java.lang.String.class,
                com.sun.ts.tests.jpa.core.metamodelapi.mapattribute.Employee.class);
        Type keyType = mapAttrib.getKeyType();
        String javaKeyTypeName = keyType.getJavaType().getName();

        if (javaKeyTypeName.equals("java.lang.String")) {
          TestUtil
              .logTrace("Received Expected Map Attribute's Java Key Type  = "
                  + javaKeyTypeName);
          pass = true;

        } else {
          TestUtil
              .logTrace("Received UnExpected Map Attribute's Java Key Type  = "
                  + javaKeyTypeName);
        }
      }
    }

    getEntityTransaction().commit();

    if (!pass) {
      throw new Exception("getKeyType Test  failed");
    }
  }

  /*
   * @testName: getCollectionType
   * 
   * @assertion_ids: PERSISTENCE:JAVADOC:1417;
   *
   * @test_Strategy:
   *
   */
@Test
  public void getCollectionType() throws Exception {
    boolean pass = false;

    String expected = PluralAttribute.CollectionType.MAP.name();

    getEntityTransaction().begin();
    Metamodel metaModel = getEntityManager().getMetamodel();
    if (metaModel != null) {
      TestUtil.logTrace("Obtained Non-null Metamodel from EntityManager");
      ManagedType<Department> mType = metaModel.managedType(Department.class);
      if (mType != null) {
        TestUtil.logTrace("Obtained Non-null ManagedType");
        MapAttribute<Department, String, Employee> mapAttrib = mType
            .getDeclaredMap("lastNameEmployees", java.lang.String.class,
                com.sun.ts.tests.jpa.core.metamodelapi.mapattribute.Employee.class);
        PluralAttribute.CollectionType type = mapAttrib.getCollectionType();
        if (type != null) {
          String name = type.name();

          if (name.equals(expected)) {
            TestUtil.logTrace("Received expected result: " + name);
            pass = true;

          } else {
            TestUtil.logTrace("Expected: " + expected + ", actual: " + name);
          }
        } else {
          TestUtil.logErr("getCollectionType() returned null");
        }
      } else {
        TestUtil.logErr("managedType() returned null");
      }
    } else {
      TestUtil.logErr("getMetamodel() returned null");
    }

    getEntityTransaction().commit();

    if (!pass) {
      throw new Exception("getCollectionType Test  failed");
    }
  }

  /*
   * @testName: getElementType
   * 
   * @assertion_ids: PERSISTENCE:JAVADOC:1418;
   *
   * @test_Strategy:
   *
   */
@Test
  public void getElementType() throws Exception {
    boolean pass = false;

    String expected = "com.sun.ts.tests.jpa.core.metamodelapi.mapattribute.Employee";

    getEntityTransaction().begin();
    Metamodel metaModel = getEntityManager().getMetamodel();
    if (metaModel != null) {
      TestUtil.logTrace("Obtained Non-null Metamodel from EntityManager");
      ManagedType<Department> mType = metaModel.managedType(Department.class);
      if (mType != null) {
        TestUtil.logTrace("Obtained Non-null ManagedType");
        MapAttribute<Department, String, Employee> mapAttrib = mType
            .getDeclaredMap("lastNameEmployees", java.lang.String.class,
                com.sun.ts.tests.jpa.core.metamodelapi.mapattribute.Employee.class);
        Type type = mapAttrib.getElementType();
        if (type != null) {
          String name = type.getJavaType().getName();

          if (name.equals(expected)) {
            TestUtil.logTrace("Received expected result: " + name);
            pass = true;

          } else {
            TestUtil.logTrace("Expected: " + expected + ", actual: " + name);
          }
        } else {
          TestUtil.logErr("getElementType() returned null");
        }
      } else {
        TestUtil.logErr("managedType() returned null");
      }
    } else {
      TestUtil.logErr("getMetamodel() returned null");
    }

    getEntityTransaction().commit();

    if (!pass) {
      throw new Exception("getElementType Test  failed");
    }
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
  }
}
