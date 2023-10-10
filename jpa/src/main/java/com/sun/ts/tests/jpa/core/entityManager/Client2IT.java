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

package com.sun.ts.tests.jpa.core.entityManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import com.sun.ts.lib.harness.CleanupMethod;
import com.sun.ts.lib.harness.SetupMethod;
import com.sun.ts.lib.util.TestUtil;
import com.sun.ts.tests.jpa.common.PMClientBase;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.StoredProcedureQuery;

@ExtendWith(ArquillianExtension.class)
@TestInstance(Lifecycle.PER_CLASS)

public class Client2IT extends PMClientBase {

  List<Employee> empRef = new ArrayList<Employee>();

  Employee emp0 = null;

  Order[] orders = new Order[5];

  Properties props = null;

  Map map = new HashMap<String, Object>();

  String dataBaseName = null;

  final static String ORACLE = "oracle";

  final static String POSTGRESQL = "postgresql";

  public Client2IT() {
  }
  
  @Deployment(testable = false, managed = false)
	public static JavaArchive createDeployment() throws Exception {

		String pkgNameWithoutSuffix = Client1IT.class.getPackageName();
		String pkgName = Client1IT.class.getPackageName() + ".";
		String[] classes = { pkgName + "Employee",
				pkgName + "Order"};
		return createDeploymentJar("jpa_core_entityManager2.jar", pkgNameWithoutSuffix, classes);

	}


  /*
   * setupOrderData() is called before each test
   *
   * @class.setup_props: jdbc.db;
   */
  @BeforeAll
  public void setupOrderData() throws Exception {
    TestUtil.logTrace("setupOrderData");
    try {
      super.setup();
      removeTestData();
      createOrderData();
      map.putAll(getEntityManager().getProperties());
      map.put("foo", "bar");
      displayMap(map);
      dataBaseName = System.getProperty("jdbc.db");
    } catch (Exception e) {
      TestUtil.logErr("Exception: ", e);
      throw new Exception("Setup failed:", e);
    }
  }

  @AfterAll
  public void cleanup() throws Exception {
    TestUtil.logTrace("cleanup complete, calling super.cleanup");
    super.cleanup();
  }


  public List<List> getResultSetsFromStoredProcedure(StoredProcedureQuery spq) {
    TestUtil.logTrace("in getResultSetsFromStoredProcedure");
    boolean results = true;
    List<List> listOfList = new ArrayList<List>();
    int rsnum = 1;
    int rowsAffected = 0;

    do {
      if (results) {
        TestUtil.logTrace("Processing set:" + rsnum);
        List<Employee> empList = new ArrayList<Employee>();
        List list = spq.getResultList();
        if (list != null) {
          TestUtil.logTrace(
              "Getting result set: " + (rsnum) + ", size:" + list.size());
          for (Object o : list) {
            if (o instanceof Employee) {
              Employee e = (Employee) o;
              TestUtil.logTrace("Saving:" + e);
              empList.add(e);
            } else {
              TestUtil.logErr("Did not get instance of Employee, instead got:"
                  + o.getClass().getName());
            }
          }
          if (empList.size() > 0) {
            listOfList.add(empList);
          }
        } else {
          TestUtil.logErr("Result set[" + rsnum + "] returned was null");
        }
        rsnum++;
      } else {
        rowsAffected = spq.getUpdateCount();
        if (rowsAffected >= 0)
          TestUtil.logTrace("rowsAffected:" + rowsAffected);
      }
      results = spq.hasMoreResults();
      TestUtil.logTrace("Results:" + results);

    } while (results || rowsAffected != -1);
    return listOfList;
  }

  public boolean verifyListOfListEmployeeIds(List<Integer> expected,
      List<List> listOfList) {
    boolean result = false;
    int count = 0;
    for (List<Employee> lEmp : listOfList) {

      if (lEmp.size() > 0) {
        List<Integer> actual = new ArrayList<Integer>();
        for (Employee e : lEmp) {
          actual.add(e.getId());
        }

        if (expected.containsAll(actual) && actual.containsAll(expected)
            && expected.size() == actual.size()) {
          TestUtil.logTrace("Received expected result:");
          for (Integer a : actual) {
            TestUtil.logTrace("id:" + a);
          }
          count++;
        } else {
          TestUtil.logErr("Did not receive expected result:");
          for (Integer e : expected) {
            TestUtil.logErr(" Expected id:" + e);
          }
          for (Integer a : actual) {
            TestUtil.logErr("Actual id:" + a);
          }
        }

      } else {
        TestUtil.logErr("Result set that was returned had 0 length");
      }

    }
    if (count == listOfList.size()) {
      result = true;
    }
    return result;
  }

  public boolean verifyListOfListEmployees(List<Employee> expected,
      List<List> listOfList) {
    boolean result = false;
    int count = 0;
    for (List<Employee> lEmp : listOfList) {

      if (lEmp.size() > 0) {
        List<Employee> actual = new ArrayList<Employee>();
        for (Employee e : lEmp) {
          actual.add(e);
        }
        if (verifyListEmployees(expected, actual)) {
          count++;
        }
      } else {
        TestUtil.logErr("Result set that was returned had 0 length");
      }
    }
    if (count == listOfList.size()) {
      result = true;
    }
    return result;
  }

  public boolean verifyListEmployees(List<Employee> expected,
      List<Employee> actual) {
    boolean result = false;
    if (expected.containsAll(actual) && actual.containsAll(expected)
        && expected.size() == actual.size()) {
      for (Employee e : expected) {
        TestUtil.logTrace("Received expected result:" + e);
      }
      result = true;
    } else {
      TestUtil.logErr("Did not receive expected result:");
      for (Employee e : expected) {
        TestUtil.logErr("expected employee:" + e);
      }
      for (Employee e : actual) {
        TestUtil.logErr("actual employee :" + e);
      }
    }
    return result;
  }

  /*
   * @testName: persistExceptionsTest
   *
   * @assertion_ids: PERSISTENCE:JAVADOC:31; PERSISTENCE:JAVADOC:506;
   * PERSISTENCE:JAVADOC:507; PERSISTENCE:SPEC:618.1; PERSISTENCE:SPEC:618.2
   *
   * @test_Strategy: Call EntityManager.persist()
   */
  @SetupMethod(name = "setupOrderData")
  @CleanupMethod(name = "cleanupData")
  @Test
  public void persistExceptionsTest() throws Exception {
    boolean pass1 = false;
    boolean pass2 = false;
    TestUtil.logMsg("Testing persisting an entity twice ");

    try {
      getEntityManager().detach(orders[0]);
      getEntityTransaction().begin();
      TestUtil.logTrace("Try to persist an existing Order");
      getEntityManager().persist(orders[0]);
      getEntityManager().flush();
      getEntityTransaction().commit();

      TestUtil.logErr("A PersistenceException was not thrown");
    } catch (EntityExistsException eee) {
      TestUtil.logTrace("EntityExistsException Caught as Expected:", eee);
      pass1 = true;
    } catch (PersistenceException pe) {
      TestUtil.logTrace("A PersistentException was caught:", pe);
      pass1 = true;
    } catch (Exception e) {
      TestUtil.logErr("Unexpected exception occurred", e);
    } finally {
      try {
        if (getEntityTransaction().isActive()) {
          getEntityTransaction().rollback();
        }
      } catch (Exception re) {
        TestUtil.logErr("Unexpected Exception in while rolling back TX:", re);
      }
    }

    TestUtil.logMsg("Testing non-entity ");
    try {
      getEntityTransaction().begin();
      getEntityManager().persist(this);
      TestUtil.logErr("IllegalArgumentException was not thrown");
    } catch (IllegalArgumentException iae) {
      TestUtil.logTrace("IllegalArgumentException caught as expected");
      pass2 = true;
    } catch (Exception e) {
      TestUtil.logErr("Unexpected exception occurred", e);
    } finally {
      try {
        if (getEntityTransaction().isActive()) {
          getEntityTransaction().rollback();
        }
      } catch (Exception re) {
        TestUtil.logErr("Unexpected Exception in while rolling back TX:", re);
      }
    }

    if (!pass1 || !pass2) {
      throw new Exception("persistExceptionsTest failed");
    }
  }


  /*
   * @testName: refreshRemovedObjectEntityNotFoundExceptionTest
   *
   * @assertion_ids: PERSISTENCE:JAVADOC:511
   *
   * @test_Strategy: Call EntityManager.refresh() method
   */
  @SetupMethod(name = "setupOrderData")
  @CleanupMethod(name = "cleanupData")
  @Test
  public void refreshRemovedObjectEntityNotFoundExceptionTest() throws Exception {
    boolean pass = false;
    try {
      getEntityTransaction().begin();
      TestUtil.logTrace("Finding Order");
      Order o = getEntityManager().find(Order.class, 1);
      TestUtil.logTrace("Removing all data");
      getEntityManager().createNativeQuery("DELETE FROM PURCHASE_ORDER")
          .executeUpdate();
      TestUtil.logTrace("Refreshing previous order");
      getEntityManager().refresh(o);
      getEntityTransaction().commit();
      TestUtil.logErr("EntityNotFoundException not thrown");
    } catch (EntityNotFoundException e) {
      TestUtil.logTrace("EntityNotFoundException Caught as Expected.");
      pass = true;
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
      throw new Exception("refreshRemovedObjectEntityNotFoundExceptionTest failed");
    }
  }


  /*
   * @testName: refreshRemovedObjectMapEntityNotFoundExceptionTest
   *
   * @assertion_ids: PERSISTENCE:JAVADOC:514;
   *
   * @test_Strategy: Call EntityManager.refresh() method
   */
  @SetupMethod(name = "setupOrderData")
  @CleanupMethod(name = "cleanupData")
  @Test
  public void refreshRemovedObjectMapEntityNotFoundExceptionTest()
      throws Exception {
    boolean pass = false;
    Map<String, Object> myMap = new HashMap<String, Object>();
    myMap.put("some.cts.specific.property", "nothing.in.particular");
    try {
      getEntityTransaction().begin();
      Order o = getEntityManager().find(Order.class, 2);
      TestUtil.logTrace("Removing all data");
      getEntityManager().createNativeQuery("DELETE FROM PURCHASE_ORDER")
          .executeUpdate();
      TestUtil.logTrace("Refreshing previous order");
      getEntityManager().refresh(o, myMap);
      getEntityTransaction().commit();
      TestUtil.logErr("EntityNotFoundException not thrown");
    } catch (EntityNotFoundException e) {
      TestUtil.logTrace("EntityNotFoundException Caught as Expected.");
      pass = true;
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
      throw new Exception(
          "refreshRemovedObjectMapEntityNotFoundExceptionTest failed");
    }
  }

  /*
   * @testName: refreshRemovedObjectLockModeTypeEntityNotFoundExceptionTest
   *
   * @assertion_ids: PERSISTENCE:JAVADOC:517
   *
   * @test_Strategy: Call EntityManager.refresh() method
   */
  @SetupMethod(name = "setupOrderData")
  @CleanupMethod(name = "cleanupData")
  @Test
  public void refreshRemovedObjectLockModeTypeEntityNotFoundExceptionTest()
      throws Exception {
    boolean pass = false;

    try {
      getEntityTransaction().begin();
      Order o = getEntityManager().find(Order.class, 3);
      TestUtil.logTrace("Removing all data");
      getEntityManager().createNativeQuery("DELETE FROM PURCHASE_ORDER")
          .executeUpdate();
      getEntityManager().refresh(o, LockModeType.PESSIMISTIC_READ);
      TestUtil.logTrace("Refreshing previous order");
      getEntityManager().refresh(o, LockModeType.PESSIMISTIC_READ);
      getEntityTransaction().commit();
      TestUtil.logErr("EntityNotFoundException not thrown");
    } catch (EntityNotFoundException e) {
      TestUtil.logTrace("EntityNotFoundException Caught as Expected.");
      pass = true;
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
      throw new Exception(
          "refreshRemovedObjectLockModeTypeEntityNotFoundExceptionTest failed");
    }
  }


  /*
   * @testName: refreshRemovedObjectLockModeTypeMapEntityNotFoundExceptionTest
   *
   * @assertion_ids: PERSISTENCE:JAVADOC:523
   *
   * @test_Strategy: Call EntityManager.refresh() method
   */
  @SetupMethod(name = "setupOrderData")
  @CleanupMethod(name = "cleanupData")
  @Test
  public void refreshRemovedObjectLockModeTypeMapEntityNotFoundExceptionTest()
      throws Exception {
    boolean pass = false;
    Map<String, Object> myMap = new HashMap<String, Object>();
    myMap.put("some.cts.specific.property", "nothing.in.particular");
    try {
      getEntityTransaction().begin();
      Order o = getEntityManager().find(Order.class, 4);
      TestUtil.logTrace("Removing all data");
      getEntityManager().createNativeQuery("DELETE FROM PURCHASE_ORDER")
          .executeUpdate();
      getEntityManager().refresh(o, LockModeType.PESSIMISTIC_READ, myMap);
      TestUtil.logTrace("Refreshing previous order");
      getEntityManager().refresh(o, LockModeType.PESSIMISTIC_READ, myMap);
      getEntityTransaction().commit();
      TestUtil.logErr("EntityNotFoundException not thrown");
    } catch (EntityNotFoundException e) {
      TestUtil.logTrace("EntityNotFoundException Caught as Expected.");
      pass = true;
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
      throw new Exception(
          "refreshRemovedObjectLockModeTypeMapEntityNotFoundExceptionTest failed");
    }
  }

  private void createOrderData() {

    try {
      getEntityTransaction().begin();
      TestUtil.logMsg("Creating Orders");
      orders[0] = new Order(1, 111, "desc1");
      orders[1] = new Order(2, 222, "desc2");
      orders[2] = new Order(3, 333, "desc3");
      orders[3] = new Order(4, 444, "desc4");
      orders[4] = new Order(5, 555, "desc5");
      for (Order o : orders) {
        TestUtil.logTrace("Persisting order:" + o.toString());
        getEntityManager().persist(o);
      }
      getEntityTransaction().commit();
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
  }

  private void createEmployeeData() {

    try {
      getEntityTransaction().begin();
      TestUtil.logMsg("Creating Employees");

      final Date d1 = getUtilDate("2000-02-14");
      final Date d2 = getUtilDate("2001-06-27");
      final Date d3 = getUtilDate("2002-07-07");
      final Date d4 = getUtilDate("2003-03-03");
      final Date d5 = getUtilDate();

      emp0 = new Employee(1, "Alan", "Frechette", d1, (float) 35000.0);
      empRef.add(emp0);
      empRef.add(new Employee(2, "Arthur", "Frechette", d2, (float) 35000.0));
      empRef.add(new Employee(3, "Shelly", "McGowan", d3, (float) 50000.0));
      empRef.add(new Employee(4, "Robert", "Bissett", d4, (float) 55000.0));
      empRef.add(new Employee(5, "Stephen", "DMilla", d5, (float) 25000.0));
      for (Employee e : empRef) {
        if (e != null) {
          getEntityManager().persist(e);
          TestUtil.logTrace("persisted employee:" + e);
        }
      }
      getEntityManager().flush();
      getEntityTransaction().commit();
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
  }

  private void removeTestData() {
    TestUtil.logTrace("removeTestData");
    if (getEntityTransaction().isActive()) {
      getEntityTransaction().rollback();
    }
    try {
      getEntityTransaction().begin();
      getEntityManager().createNativeQuery("DELETE FROM EMPLOYEE")
          .executeUpdate();
      getEntityManager().createNativeQuery("DELETE FROM PURCHASE_ORDER")
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
