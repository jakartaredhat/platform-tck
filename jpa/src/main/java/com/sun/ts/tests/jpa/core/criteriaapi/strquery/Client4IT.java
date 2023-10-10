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

package com.sun.ts.tests.jpa.core.criteriaapi.strquery;

import java.sql.Date;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;

import com.sun.ts.lib.harness.SetupMethod;
import com.sun.ts.lib.util.TestUtil;
import com.sun.ts.tests.jpa.common.schema30.Address;
import com.sun.ts.tests.jpa.common.schema30.Alias;
import com.sun.ts.tests.jpa.common.schema30.CreditCard;
import com.sun.ts.tests.jpa.common.schema30.Customer;
import com.sun.ts.tests.jpa.common.schema30.LineItem;
import com.sun.ts.tests.jpa.common.schema30.Order;
import com.sun.ts.tests.jpa.common.schema30.Phone;
import com.sun.ts.tests.jpa.common.schema30.Product;
import com.sun.ts.tests.jpa.common.schema30.Spouse;
import com.sun.ts.tests.jpa.common.schema30.UtilProductData;
import com.sun.ts.tests.jpa.core.criteriaapi.Root.Client2IT;

import jakarta.persistence.NoResultException;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import jakarta.persistence.metamodel.Attribute;

@ExtendWith(ArquillianExtension.class)
@TestInstance(Lifecycle.PER_CLASS)

public class Client4IT extends UtilProductData {
	
	 @Deployment(testable = false, managed = false)
	 	public static JavaArchive createDeployment() throws Exception {

	 		String pkgNameWithoutSuffix = Client4IT.class.getPackageName();
	 		String pkgName = Client4IT.class.getPackageName() + ".";
	 		String[] classes = {};
	 		return createDeploymentJar("jpa_core_criteriaapi_root4.jar", pkgNameWithoutSuffix, classes);
	 }

 
  /*
   * @testName: queryTest68
   * 
   * @assertion_ids: PERSISTENCE:SPEC:383; PERSISTENCE:SPEC:406;
   * 
   * @test_Strategy: Execute a query which contains the aggregate function SUM.
   * SUM returns Double when applied to state-fields of floating types. Verify
   * the results are accurately returned.
   *
   */
  @SetupMethod(name = "setupProductData")
  @Test
  public void queryTest68() throws Exception {
    boolean pass = false;
    final Double d1 = 33387.14D;
    final Double d2 = 33387.15D;
    Double d3;

    CriteriaBuilder cbuilder = getEntityManager().getCriteriaBuilder();

    try {
      TestUtil.logTrace("find SUM of all product prices");
      CriteriaQuery<Double> cquery = cbuilder.createQuery(Double.class);
      Root<Product> product = cquery.from(Product.class);
      cquery.select(cbuilder.sum(product.<Double> get("price")));
      TypedQuery<Double> tquery = getEntityManager().createQuery(cquery);
      d3 = (Double) tquery.getSingleResult();

      if (((d3 >= d1) && (d3 < d2))) {
        TestUtil.logTrace("queryTest68 returned expected results: " + d1);
        pass = true;
      } else {
        TestUtil.logTrace("queryTest68 returned " + d3 + "expected: " + d1);
      }
    } catch (Exception e) {
      TestUtil.logErr("Caught unexpected exception", e);
    }
    if (!pass) {
      throw new Exception("queryTest68 failed");
    }
  }

  /*
   * @testName: queryTest70
   * 
   * @assertion_ids: PERSISTENCE:SPEC:383; PERSISTENCE:SPEC:406;
   * PERSISTENCE:SPEC:827; PERSISTENCE:SPEC:821
   * 
   * @test_Strategy: Execute a query which contains the aggregate function SUM.
   * SUM returns Long when applied to state-fields of integral types. Verify the
   * results are accurately returned.
   *
   */
  @SetupMethod(name = "setupProductData")
  @Test
  public void queryTest70() throws Exception {
    boolean pass = false;
    final Integer expectedValue = Integer.valueOf(3277);
    Integer result;
    CriteriaBuilder cbuilder = getEntityManager().getCriteriaBuilder();

    try {
      TestUtil.logTrace("find SUM of all product prices");
      CriteriaQuery<Integer> cquery = cbuilder.createQuery(Integer.class);
      Root<Product> product = cquery.from(Product.class);
      cquery.select(cbuilder.sum(product.<Integer> get("quantity")));
      TypedQuery<Integer> tquery = getEntityManager().createQuery(cquery);
      result = (Integer) tquery.getSingleResult();

      if (expectedValue.equals(result)) {
        TestUtil.logTrace("queryTest70 returned expected results: " + result);
        pass = true;
      } else {
        TestUtil.logTrace(
            "queryTest70 returned " + result + "expected: " + expectedValue);
      }
    } catch (Exception e) {
      TestUtil.logErr("Caught unexpected exception", e);
    }
    if (!pass) {
      throw new Exception("queryTest70 failed");
    }
  }


  /*
   * @testName: test_betweenDates
   * 
   * @assertion_ids: PERSISTENCE:SPEC:349.2; PERSISTENCE:SPEC:553;
   * PERSISTENCE:JAVADOC:15; PERSISTENCE:JAVADOC:166; PERSISTENCE:JAVADOC:189;
   * PERSISTENCE:SPEC:1049; PERSISTENCE:SPEC:1059; PERSISTENCE:SPEC:1060;
   * PERSISTENCE:JAVADOC:743
   * 
   * @test_Strategy: Execute a query containing using the operator BETWEEN with
   * datetime_expression. Verify the results were accurately returned.
   *
   */

  @SetupMethod(name = "setupProductData")
  @Test
  public void test_betweenDates() throws Exception {
    boolean pass = false;
    String expectedPKs[];

    CriteriaBuilder cbuilder = getEntityManager().getCriteriaBuilder();

    try {
      getEntityTransaction().begin();
      final Date date1 = getSQLDate(2000, 2, 14);
      final Date date6 = getSQLDate(2005, 2, 18);
      TestUtil.logTrace("The dates used in test_betweenDates is : " + date1
          + " and " + date6);
      CriteriaQuery<Product> cquery = cbuilder.createQuery(Product.class);
      Root<Product> product = cquery.from(Product.class);
      cquery.where(cbuilder.between(
          product.get("shelfLife").<java.sql.Date> get("soldDate"),
          cbuilder.parameter(java.sql.Date.class, "date1"),
          cbuilder.parameter(java.sql.Date.class, "date6")));
      cquery.select(product);
      TypedQuery<Product> tquery = getEntityManager().createQuery(cquery);
      tquery.setParameter("date1", date1);
      tquery.setParameter("date6", date6);
      List<Product> result = tquery.getResultList();

      expectedPKs = new String[4];
      expectedPKs[0] = "31";
      expectedPKs[1] = "32";
      expectedPKs[2] = "33";
      expectedPKs[3] = "37";

      if (!checkEntityPK(result, expectedPKs)) {
        TestUtil.logErr(
            "Did not get expected results.  Expected 3 references, got: "
                + result.size());
      } else {

        TestUtil.logTrace("Expected results received");
        pass = true;
      }
      getEntityTransaction().commit();
    } catch (Exception e) {
      TestUtil.logErr("Caught unexpected exception", e);
    }

    if (!pass)
      throw new Exception("test_betweenDates failed");
  }

  
  /*
   * @testName: test_notBetweenDates
   * 
   * @assertion_ids: PERSISTENCE:SPEC:349.2; PERSISTENCE:SPEC:600
   * 
   * @test_Strategy: Execute a query containing using the operator NOT BETWEEN.
   * Verify the results were accurately returned.
   */

  @SetupMethod(name = "setupProductData")
  @Test
  public void test_notBetweenDates() throws Exception {
    boolean pass = false;
    String expectedPKs[];
    final Date date1 = getSQLDate("2000-02-14");
    final Date newdate = getSQLDate("2005-02-17");
    TestUtil.logTrace("The dates used in test_betweenDates is : " + date1
        + " and " + newdate);

    CriteriaBuilder cbuilder = getEntityManager().getCriteriaBuilder();

    try {
      getEntityTransaction().begin();
      CriteriaQuery<Product> cquery = cbuilder.createQuery(Product.class);
      Root<Product> product = cquery.from(Product.class);
      cquery.where(cbuilder.not(cbuilder.between(
          product.get("shelfLife").<java.sql.Date> get("soldDate"),
          cbuilder.parameter(java.sql.Date.class, "date1"),
          cbuilder.parameter(java.sql.Date.class, "newdate"))));
      cquery.select(product);
      TypedQuery<Product> tquery = getEntityManager().createQuery(cquery);
      tquery.setParameter("date1", date1);
      tquery.setParameter("newdate", newdate);
      List<Product> result = tquery.getResultList();

      expectedPKs = new String[1];
      expectedPKs[0] = "31";
      if (!checkEntityPK(result, expectedPKs)) {
        TestUtil.logErr(
            "Did not get expected results.  Expected 1 references, got: "
                + result.size());
      } else {
        TestUtil.logTrace("Expected results received");
        pass = true;
      }
      getEntityTransaction().commit();
    } catch (Exception e) {
      TestUtil.logErr("Caught unexpected exception", e);
    }

    if (!pass)
      throw new Exception("test_notBetweenDates failed");
  }

}
