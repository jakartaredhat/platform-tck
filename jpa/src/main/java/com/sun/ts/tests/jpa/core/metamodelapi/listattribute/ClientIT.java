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

package com.sun.ts.tests.jpa.core.metamodelapi.listattribute;

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

import jakarta.persistence.metamodel.ListAttribute;
import jakarta.persistence.metamodel.ManagedType;
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
		String[] classes = {pkgName + "BiDirMX1Person",
				pkgName + "BiDirMX1Project"};
		return createDeploymentJar("jpa_core_metamodelapi_listattribute.jar", pkgNameWithoutSuffix, classes);

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

  /*
   * @testName: getList
   * 
   * @assertion_ids: PERSISTENCE:JAVADOC:1395; PERSISTENCE:JAVADOC:1362;
   * 
   * @test_Strategy:
   */
@Test
  public void getList() throws Exception {
    boolean pass = false;

    try {
      getEntityTransaction().begin();
      Metamodel metaModel = getEntityManager().getMetamodel();
      if (metaModel != null) {
        TestUtil.logTrace("Obtained Non-null Metamodel from EntityManager");
        ManagedType<BiDirMX1Project> mType = metaModel
            .managedType(BiDirMX1Project.class);
        if (mType != null) {
          TestUtil.logTrace("Obtained Non-null ManagedType");
          ListAttribute<? super BiDirMX1Project, BiDirMX1Person> listAttrib = mType
              .getList("biDirMX1Persons", BiDirMX1Person.class);
          Type t = listAttrib.getElementType();
          if (t != null) {
            TestUtil.logTrace("element Type  = " + t.getJavaType());
            String name = t.getJavaType().getName();
            if (name.equals(
                "com.sun.ts.tests.jpa.core.metamodelapi.listattribute.BiDirMX1Person")) {
              pass = true;
            } else {
              TestUtil.logErr("Expected: BiDirMX1Person, actual:" + name);
            }
          }
        }
      }

      getEntityTransaction().commit();
    } catch (Exception e) {
      TestUtil.logErr("Received unexpected exception", e);
    }
    if (!pass) {
      throw new Exception("getList Test  failed");
    }
  }

  /*
   * @testName: getCollectionType
   * 
   * @assertion_ids: PERSISTENCE:JAVADOC:1361;
   *
   * @test_Strategy:
   *
   */
@Test
  public void getCollectionType() throws Exception {
    boolean pass = false;

    String expected = PluralAttribute.CollectionType.LIST.name();
    try {
      getEntityTransaction().begin();
      Metamodel metaModel = getEntityManager().getMetamodel();
      if (metaModel != null) {
        TestUtil.logTrace("Obtained Non-null Metamodel from EntityManager");
        ManagedType<BiDirMX1Project> mType = metaModel
            .managedType(BiDirMX1Project.class);
        if (mType != null) {
          TestUtil.logTrace("Obtained Non-null ManagedType");
          ListAttribute<? super BiDirMX1Project, BiDirMX1Person> listAttrib = mType
              .getList("biDirMX1Persons", BiDirMX1Person.class);
          PluralAttribute.CollectionType t = listAttrib.getCollectionType();
          if (t != null) {
            String sType = t.name();
            if (sType.equals(expected)) {
              TestUtil.logTrace("Received expected: " + expected);
              pass = true;
            } else {
              TestUtil.logErr("Expected: " + expected + ", actual:" + sType);
            }
          } else {
            TestUtil.logErr("getCollectionType() returned null");
          }
        } else {
          TestUtil.logErr("managedType(...) returned null");
        }
      } else {
        TestUtil.logErr("getMetamodel() returned null");
      }

      getEntityTransaction().commit();
    } catch (Exception e) {
      TestUtil.logErr("Received unexpected exception", e);
    }
    if (!pass) {
      throw new Exception("getCollectionType Test failed");
    }
  }

  /*
   * @testName: getDeclaredList
   * 
   * @assertion_ids: PERSISTENCE:JAVADOC:1377
   *
   * @test_Strategy:
   *
   */
@Test
  public void getDeclaredList() throws Exception {
    boolean pass = false;

    try {
      getEntityTransaction().begin();
      Metamodel metaModel = getEntityManager().getMetamodel();
      if (metaModel != null) {
        TestUtil.logTrace("Obtained Non-null Metamodel from EntityManager");
        ManagedType<BiDirMX1Project> mType = metaModel
            .managedType(BiDirMX1Project.class);
        if (mType != null) {
          TestUtil.logTrace("Obtained Non-null ManagedType");
          ListAttribute<BiDirMX1Project, BiDirMX1Person> listAttrib = mType
              .getDeclaredList("biDirMX1Persons", BiDirMX1Person.class);
          Type t = listAttrib.getElementType();
          if (t != null) {
            TestUtil.logTrace("element Type  = " + t.getJavaType());
            String name = t.getJavaType().getName();
            if (name.equals(
                "com.sun.ts.tests.jpa.core.metamodelapi.listattribute.BiDirMX1Person")) {
              pass = true;
            } else {
              TestUtil.logErr("Expected: BiDirMX1Person, actual:" + name);
            }
          }
        }
      }

      getEntityTransaction().commit();
    } catch (Exception e) {
      TestUtil.logErr("Received unexpected exception", e);
    }
    if (!pass) {
      throw new Exception("getDeclaredList Test  failed");
    }
  }

  /*
   * @testName: getList2
   * 
   * @assertion_ids: PERSISTENCE:JAVADOC:1397;
   *
   * @test_Strategy:
   *
   */
@Test
  public void getList2() throws Exception {
    boolean pass = false;

    try {
      getEntityTransaction().begin();
      Metamodel metaModel = getEntityManager().getMetamodel();
      if (metaModel != null) {
        TestUtil.logTrace("Obtained Non-null Metamodel from EntityManager");
        ManagedType<BiDirMX1Project> mType = metaModel
            .managedType(BiDirMX1Project.class);
        if (mType != null) {
          TestUtil.logTrace("Obtained Non-null ManagedType");
          ListAttribute<? super BiDirMX1Project, ?> listAttrib = mType
              .getList("biDirMX1Persons");
          Type t = listAttrib.getElementType();
          if (t != null) {
            TestUtil.logTrace("element Type  = " + t.getJavaType());
            String name = t.getJavaType().getName();
            if (name.equals(
                "com.sun.ts.tests.jpa.core.metamodelapi.listattribute.BiDirMX1Person")) {
              pass = true;
            } else {
              TestUtil.logErr("Expected: BiDirMX1Person, actual:" + name);
            }
          }
        }
      }

      getEntityTransaction().commit();
    } catch (Exception e) {
      TestUtil.logErr("Received unexpected exception", e);
    }
    if (!pass) {
      throw new Exception("getList2 Test  failed");
    }
  }

  /*
   * @testName: getDeclaredList2
   * 
   * @assertion_ids: PERSISTENCE:JAVADOC:1246
   *
   * @test_Strategy:
   *
   */
@Test
  public void getDeclaredList2() throws Exception {
    boolean pass = false;
    try {

      getEntityTransaction().begin();
      Metamodel metaModel = getEntityManager().getMetamodel();
      if (metaModel != null) {
        TestUtil.logTrace("Obtained Non-null Metamodel from EntityManager");
        ManagedType<BiDirMX1Project> mType = metaModel
            .managedType(BiDirMX1Project.class);
        if (mType != null) {
          TestUtil.logTrace("Obtained Non-null ManagedType");
          ListAttribute<BiDirMX1Project, ?> listAttrib = mType
              .getDeclaredList("biDirMX1Persons");
          Type t = listAttrib.getElementType();
          if (t != null) {
            TestUtil.logTrace("element Type  = " + t.getJavaType());
            String name = t.getJavaType().getName();
            if (name.equals(
                "com.sun.ts.tests.jpa.core.metamodelapi.listattribute.BiDirMX1Person")) {
              pass = true;
            } else {
              TestUtil.logErr("Expected: BiDirMX1Person, actual:" + name);
            }
          }
        }
      }

      getEntityTransaction().commit();
    } catch (Exception e) {
      TestUtil.logErr("Received unexpected exception", e);
    }
    if (!pass) {
      throw new Exception("getDeclaredList2 Test  failed");
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

    try {
      if (getEntityTransaction().isActive()) {
        getEntityTransaction().rollback();
      }
    } catch (Exception re) {
      TestUtil.logErr("Unexpected Exception in removeTestData:", re);
    }

  }
}
