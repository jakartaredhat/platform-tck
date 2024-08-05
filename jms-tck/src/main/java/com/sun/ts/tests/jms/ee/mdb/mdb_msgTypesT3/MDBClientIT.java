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
 * $Id$
 */
package com.sun.ts.tests.jms.ee.mdb.mdb_msgTypesT3;

import java.lang.System.Logger;
import java.util.Properties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.ts.lib.util.TestUtil;
import com.sun.ts.tests.jms.commonee.MDB_T_Test;

import jakarta.ejb.EJB;

/**
 * The MDBClient class invokes a test session bean, which will ask and the
 * message driven bean to send a text, byte, map, stream, and object message to
 * a queue
 */


public class MDBClientIT {

	@EJB(name = "ejb/MDB_MSGT3_Test")
	private static MDB_T_Test hr;

	private Properties props = new Properties();

	private static final Logger logger = (Logger) System.getLogger(MDBClientIT.class.getName());

	/* Test setup: */
	/*
	 * @class.setup_props: jms_timeout; user; password;
	 */
	@BeforeEach
	public void setup() throws Exception {
		try {
			if (hr == null) {
				throw new Exception("@EJB injection failed");
			}
			props.put("jms_timeout", System.getProperty("jms_property"));
			props.put("user", System.getProperty("user"));
			props.put("password", System.getProperty("password"));

			hr.setup(props);
			if (hr.isThereSomethingInTheQueue()) {
				logger.log(Logger.Level.TRACE, "Error: message(s) left in Q");
				hr.cleanTheQueue();
			} else {
				logger.log(Logger.Level.TRACE, "Nothing left in queue");
			}
			logger.log(Logger.Level.INFO, "Setup ok;");
		} catch (Exception e) {
			throw new Exception("Setup Failed!", e);
		}
	}

	/* Run tests */
	//

	/*
	 * @testName: mdbMsgClearBodyTopicTextTest
	 *
	 * @assertion_ids: JMS:SPEC:71; JMS:SPEC:72; JMS:JAVADOC:431; JMS:JAVADOC:473;
	 * JMS:JAVADOC:449; JMS:SPEC:178; JMS:JAVADOC:291;
	 *
	 * @test_Strategy: Call a session bean. Have the session bean invoke an mdb to
	 * Create and send a Text message. Have the mdb read the message call clearBody,
	 * verify body is empty after clearBody. verify properties are not effected by
	 * clearBody. Write to the message again 3.11
	 */
	@Test
	public void mdbMsgClearBodyTopicTextTest() throws Exception {
		String testCase1 = "msgClearBodyTopicTextTestCreate";
		String testCase2 = "msgClearBodyTopicTextTest";
		try {
			// Have the EJB invoke the MDB
			System.out.println("client - run testcase msgClearBodyTopicTextTestCreate");
			hr.askMDBToRunATest(testCase1); // create and send message to MDB_QUEUE
			System.out.println("client - Check for response from msgClearBodyTopicTextTest");
			if (!hr.checkOnResponse(testCase2)) {
				logger.log(Logger.Level.TRACE, "Error: didn't get expected response from mdb");
				throw new Exception("ERROR: msgClearBodyTopicTextTest failed");
			}
			logger.log(Logger.Level.TRACE, "Test passed!");
		} catch (Exception e) {
			throw new Exception("Test Failed!", e);
		}
	}

	/*
	 * @testName: mdbMsgClearBodyTopicObjectTest
	 *
	 * @assertion_ids: JMS:SPEC:71; JMS:SPEC:72; JMS:JAVADOC:431; JMS:JAVADOC:473;
	 * JMS:JAVADOC:449; JMS:SPEC:178; JMS:JAVADOC:291;
	 *
	 * @test_Strategy: Call a session bean. Have the session bean invoke an mdb to
	 * Create and send a Object message. Have the mdb read the message call
	 * clearBody, verify body is empty after clearBody. verify properties are not
	 * effected by clearBody. Write to the message again 3.11
	 */
	@Test
	public void mdbMsgClearBodyTopicObjectTest() throws Exception {
		String testCase1 = "msgClearBodyTopicObjectTestCreate";
		String testCase2 = "msgClearBodyTopicObjectTest";
		try {
			// Have the EJB invoke the MDB
			System.out.println("client - run testcase msgClearBodyTopicObjectTestCreate");
			hr.askMDBToRunATest(testCase1); // create and send message to MDB_QUEUE
			System.out.println("client - Check for response from msgClearBodyTopicObjectTest");
			if (!hr.checkOnResponse(testCase2)) {
				logger.log(Logger.Level.TRACE, "Error: didn't get expected response from mdb");
				throw new Exception("ERROR: msgClearBodyTopicObjectTest failed");
			}
			logger.log(Logger.Level.TRACE, "Test passed!");
		} catch (Exception e) {
			throw new Exception("Test Failed!", e);
		}
	}

	/*
	 * @testName: mdbMsgClearBodyTopicMapTest
	 *
	 * @assertion_ids: JMS:SPEC:71; JMS:SPEC:72; JMS:JAVADOC:431; JMS:JAVADOC:473;
	 * JMS:JAVADOC:449; JMS:SPEC:178; JMS:JAVADOC:291;
	 *
	 * @test_Strategy: Call a session bean. Have the session bean invoke an mdb to
	 * Create and send a Map message. Have the mdb read the message call clearBody,
	 * verify body is empty after clearBody. verify properties are not effected by
	 * clearBody. Write to the message again 3.11
	 */
	@Test
	public void mdbMsgClearBodyTopicMapTest() throws Exception {
		String testCase1 = "msgClearBodyTopicMapTestCreate";
		String testCase2 = "msgClearBodyTopicMapTest";
		try {
			// Have the EJB invoke the MDB
			System.out.println("client - run testcase msgClearBodyTopicMapTestCreate");
			hr.askMDBToRunATest(testCase1); // create and send message to MDB_QUEUE
			System.out.println("client - Check for response from msgClearBodyTopicMapTest");
			if (!hr.checkOnResponse(testCase2)) {
				logger.log(Logger.Level.TRACE, "Error: didn't get expected response from mdb");
				throw new Exception("ERROR: msgClearBodyTopicMapTest failed");
			}
			logger.log(Logger.Level.TRACE, "Test passed!");
		} catch (Exception e) {
			throw new Exception("Test Failed!", e);
		}
	}

	/*
	 * @testName: mdbMsgClearBodyTopicBytesTest
	 *
	 * @assertion_ids: JMS:SPEC:71; JMS:SPEC:72; JMS:JAVADOC:431; JMS:JAVADOC:473;
	 * JMS:JAVADOC:449; JMS:SPEC:178; JMS:JAVADOC:291;
	 *
	 * @test_Strategy: Call a session bean. Have the session bean invoke an mdb to
	 * Create and send a Bytes message. Have the mdb read the message call
	 * clearBody, verify body is empty after clearBody. verify properties are not
	 * effected by clearBody. Write to the message again 3.11
	 */
	@Test
	public void mdbMsgClearBodyTopicBytesTest() throws Exception {
		String testCase1 = "msgClearBodyTopicBytesTestCreate";
		String testCase2 = "msgClearBodyTopicBytesTest";
		try {
			// Have the EJB invoke the MDB
			System.out.println("client - run testcase msgClearBodyTopicBytesTestCreate");
			hr.askMDBToRunATest(testCase1); // create and send message to MDB_QUEUE
			System.out.println("client - Check for response from msgClearBodyTopicBytesTest");
			if (!hr.checkOnResponse(testCase2)) {
				logger.log(Logger.Level.TRACE, "Error: didn't get expected response from mdb");
				throw new Exception("ERROR: msgClearBodyTopicBytesTest failed");
			}
			logger.log(Logger.Level.TRACE, "Test passed!");
		} catch (Exception e) {
			throw new Exception("Test Failed!", e);
		}
	}

	/*
	 * @testName: mdbMsgClearBodyTopicStreamTest
	 *
	 * @assertion_ids: JMS:SPEC:71; JMS:SPEC:72; JMS:JAVADOC:431; JMS:JAVADOC:473;
	 * JMS:JAVADOC:449; JMS:SPEC:178; JMS:JAVADOC:291;
	 *
	 * @test_Strategy: Call a session bean. Have the session bean invoke an mdb to
	 * Create and send a Stream message. Have the mdb read the message call
	 * clearBody, verify body is empty after clearBody. verify properties are not
	 * effected by clearBody. Write to the message again 3.11
	 */
	@Test
	public void mdbMsgClearBodyTopicStreamTest() throws Exception {
		String testCase1 = "msgClearBodyTopicStreamTestCreate";
		String testCase2 = "msgClearBodyTopicStreamTest";
		try {
			// Have the EJB invoke the MDB
			System.out.println("client - run testcase msgClearBodyTopicStreamTestCreate");
			hr.askMDBToRunATest(testCase1); // create and send message to MDB_QUEUE
			System.out.println("client - Check for response from msgClearBodyTopicStreamTest");
			if (!hr.checkOnResponse(testCase2)) {
				logger.log(Logger.Level.TRACE, "Error: didn't get expected response from mdb");
				throw new Exception("ERROR: msgClearBodyTopicStreamTest failed");
			}
			logger.log(Logger.Level.TRACE, "Test passed!");
		} catch (Exception e) {
			throw new Exception("Test Failed!", e);
		}
	}

	/*
	 * @testName: mdbMsgResetTopicTest
	 *
	 * @assertion_ids: JMS:JAVADOC:174; JMS:JAVADOC:584;
	 *
	 * @test_Strategy: Call a session bean. Have the session bean invoke an mdb to
	 * create a stream message and a byte message. write to the message body, call
	 * the reset method, try to write to the body expect a
	 * MessageNotWriteableException to be thrown.
	 * 
	 * 
	 * 
	 */
	@Test
	public void mdbMsgResetTopicTest() throws Exception {
		String testCase = "msgResetTopicTest";
		try {
			// Have the EJB invoke the MDB
			System.out.println("client - run testcase msgResetTopicTest");
			hr.askMDBToRunATest(testCase); // create and send message to MDB_QUEUE
			System.out.println("client - Check for response from msgResetTopicTest");
			if (!hr.checkOnResponse(testCase)) {
				logger.log(Logger.Level.TRACE, "Error: didn't get expected response from mdb");
				throw new Exception("ERROR: msgResetTopicTest failed");
			}
			logger.log(Logger.Level.TRACE, "Test passed!");
		} catch (Exception e) {
			throw new Exception("Test Failed!", e);
		}
	}

	/*
	 * @testName: mdbReadNullCharNotValidTopicMapTest
	 *
	 * @assertion_ids: JMS:SPEC:79; JMS:JAVADOC:134; JMS:JAVADOC:439;
	 *
	 * @test_Strategy: Call a session bean. Have the session bean invoke an mdb to
	 * write a null string to a MapMessage. Attempt to read the null value as a
	 * char.
	 */
	@Test
	public void mdbReadNullCharNotValidTopicMapTest() throws Exception {
		String testCase1 = "readNullCharNotValidTopicMapTestCreate";
		String testCase2 = "readNullCharNotValidTopicMapTest";
		try {
			// Have the EJB invoke the MDB
			System.out.println("client - run testcase readNullCharNotValidTopicMapTestCreate");
			hr.askMDBToRunATest(testCase1); // create and send message to MDB_QUEUE
			System.out.println("client - Check for response from readNullCharNotValidTopicMapTest");
			if (!hr.checkOnResponse(testCase2)) {
				logger.log(Logger.Level.TRACE, "Error: didn't get expected response from mdb");
				throw new Exception("ERROR: readNullCharNotValidTopicMapTest failed");
			}
			logger.log(Logger.Level.TRACE, "Test passed!");
		} catch (Exception e) {
			throw new Exception("Test Failed!", e);
		}
	}

	/*
	 * @testName: mdbReadNullCharNotValidTopicStreamTest
	 *
	 * @assertion_ids: JMS:SPEC:79; JMS:JAVADOC:134; JMS:JAVADOC:439;
	 *
	 * @test_Strategy: Call a session bean. Have the session bean invoke an mdb to
	 * write a null string to a StreamMessage. Attempt to read the null value as a
	 * char.
	 */
	@Test
	public void mdbReadNullCharNotValidTopicStreamTest() throws Exception {
		String testCase1 = "readNullCharNotValidTopicStreamTestCreate";
		String testCase2 = "readNullCharNotValidTopicStreamTest";
		try {
			// Have the EJB invoke the MDB
			System.out.println("client - run testcase readNullCharNotValidTopicStreamTestCreate");
			hr.askMDBToRunATest(testCase1); // create and send message to MDB_QUEUE
			System.out.println("client - Check for response from readNullCharNotValidTopicStreamTest");
			if (!hr.checkOnResponse(testCase2)) {
				logger.log(Logger.Level.TRACE, "Error: didn't get expected response from mdb");
				throw new Exception("ERROR: readNullCharNotValidTopicStreamTest failed");
			}
			logger.log(Logger.Level.TRACE, "Test passed!");
		} catch (Exception e) {
			throw new Exception("Test Failed!", e);
		}
	}

	/* cleanup -- none in this case */
	@AfterEach
	public void cleanup() throws Exception {
		try {
			if (hr.isThereSomethingInTheQueue()) {
				logger.log(Logger.Level.TRACE, "Error: message(s) left in Q");
				hr.cleanTheQueue();
			} else {
				logger.log(Logger.Level.TRACE, "Nothing left in queue");
			}
			logger.log(Logger.Level.INFO, "End  of client cleanup;");
		} catch (Exception e) {
			TestUtil.printStackTrace(e);
		}
		;
	}

}
