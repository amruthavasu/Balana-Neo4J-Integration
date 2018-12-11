package org.wso2.balana.performance;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.wso2.balana.Balana;
import org.wso2.balana.ConfigurationStore;
import org.wso2.balana.PDP;
import org.wso2.balana.PDPConfig;
import org.wso2.balana.TestConstants;
import org.wso2.balana.TestUtil;
import org.wso2.balana.ctx.ResponseCtx;
import org.wso2.balana.finder.PolicyFinder;
import org.wso2.balana.finder.PolicyFinderModule;
import org.wso2.balana.finder.impl.FileBasedPolicyFinderModule;

import junit.framework.TestCase;

public class PerformanceTest extends TestCase {
	/*
	 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
	 *
	 * WSO2 Inc. licenses this file to you under the Apache License, Version 2.0
	 * (the "License"); you may not use this file except in compliance with the
	 * License. You may obtain a copy of the License at
	 *
	 * http://www.apache.org/licenses/LICENSE-2.0
	 *
	 * Unless required by applicable law or agreed to in writing, software
	 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
	 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
	 * License for the specific language governing permissions and limitations under
	 * the License.
	 */

	/**
	 * Configuration store
	 */
	private static ConfigurationStore store;

	/**
	 * directory name that states the test type
	 */
	private final static String ROOT_DIRECTORY = "performance";

	/**
	 * directory name that states XACML version
	 */
	private final static String VERSION_DIRECTORY = "suite6";
	private static int count = 0;

	/**
	 * the logger we'll use for all messages
	 */
	private static Log log = LogFactory.getLog(PerformanceTest.class);

	public void setUp() throws Exception {

		String configFile = (new File(".")).getCanonicalPath() + File.separator + TestConstants.CONFIG_FILE;
		store = new ConfigurationStore(new File(configFile));
	}

	public void testConformanceTestA() throws Exception {
		for (int i = 1; i < 2; i++) {
			File file = new File(".");
			if (new File(file.getCanonicalPath() + File.separator + TestConstants.RESOURCE_PATH + File.separator
					+ ROOT_DIRECTORY + File.separator + VERSION_DIRECTORY + File.separator
					+ TestConstants.REQUEST_DIRECTORY + File.separator + "IIA001" + "Request.xml")
							.exists()) {
				String request = TestUtil.createRequest(ROOT_DIRECTORY, VERSION_DIRECTORY,
						"IIA001" + "Request.xml");
				if (request != null) {
					count++;
					log.info("Request that is sent to the PDP :  " + request);
					log.info("Test case number : " + count);
					ResponseCtx response = TestUtil.evaluate(getPDPNewInstance(), request);
					if (response != null) {
						ResponseCtx expectedResponseCtx = TestUtil.createResponse(ROOT_DIRECTORY, VERSION_DIRECTORY,
								"IIA001" + "Response.xml");
						log.info("Response that is received from the PDP :  " + response.encode());
						if (expectedResponseCtx != null) {
							assertTrue(TestUtil.isMatching(response, expectedResponseCtx));
						} else {
							assertTrue("Response read from file is Null", false);
						}
					} else {
						assertFalse("Response received PDP is Null", false);
					}
				} else {
					assertTrue("Request read from file is Null", false);
				}

				log.info("Conformance Test IIA001" + " is finished");
			}
		}
	}

	

	/**
	 * Returns a new PDP instance with new XACML policies
	 *
	 * @param policies
	 *            Set of XACML policy file names
	 * @return a PDP instance
	 */
	private static PDP getPDPNewInstance() {

		PolicyFinder finder = new PolicyFinder();

		FileBasedPolicyFinderModule testPolicyFinderModule = new FileBasedPolicyFinderModule();
		Set<PolicyFinderModule> policyModules = new HashSet<PolicyFinderModule>();
		policyModules.add(testPolicyFinderModule);
		finder.setModules(policyModules);

		Balana balana = Balana.getInstance();
		PDPConfig pdpConfig = balana.getPdpConfig();
		pdpConfig = new PDPConfig(pdpConfig.getAttributeFinder(), finder, pdpConfig.getResourceFinder(), false);
		return new PDP(pdpConfig);

	}

	private static PDP getPDPNewInstance(Set<String> policies) {

		PolicyFinder finder = new PolicyFinder();
		Set<String> policyLocations = new HashSet<String>();

		for (String policy : policies) {
			try {
				String policyPath = (new File(".")).getCanonicalPath() + File.separator + TestConstants.RESOURCE_PATH
						+ File.separator + ROOT_DIRECTORY + File.separator + VERSION_DIRECTORY + File.separator
						+ TestConstants.POLICY_DIRECTORY + File.separator + policy;
				policyLocations.add(policyPath);
			} catch (IOException e) {
				// ignore.
			}
		}

		FileBasedPolicyFinderModule testPolicyFinderModule = new FileBasedPolicyFinderModule(policyLocations);
		Set<PolicyFinderModule> policyModules = new HashSet<PolicyFinderModule>();
		policyModules.add(testPolicyFinderModule);
		finder.setModules(policyModules);

		Balana balana = Balana.getInstance();
		PDPConfig pdpConfig = balana.getPdpConfig();
		pdpConfig = new PDPConfig(pdpConfig.getAttributeFinder(), finder, pdpConfig.getResourceFinder(), false);
		return new PDP(pdpConfig);

	}
}
