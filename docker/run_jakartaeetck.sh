#!/bin/bash

# Copyright (c) 2022 Contributors to the Eclipse Foundation
# Copyright (c) 2018, 2022 Oracle and/or its affiliates. All rights reserved.
# Copyright (c) 2019, 2022 Payara Foundation and/or its affiliates. All rights reserved.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License v. 2.0, which is available at
# http://www.eclipse.org/legal/epl-2.0.
#
# This Source Code may also be made available under the following Secondary
# Licenses when the conditions for such availability set forth in the
# Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
# version 2 with the GNU Classpath Exception, which is available at
# https://www.gnu.org/software/classpath/license.html.
#
# SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0


if [[ $1 = *'_'* ]]; then
  test_suite=$(echo "$1" | cut -f1 -d_)
  vehicle_name=$(echo "$1" | cut -f2 -d_)
  vehicle="${vehicle_name}_vehicle"
else
  test_suite="$1"
  vehicle=""
fi

echo "TEST_SUITE:${test_suite}."
echo "VEHICLE:${vehicle}."

if [ -z "${test_suite}" ]; then
  echo "Please supply a valid test_suite as argument"
  exit 1
fi

if [ -z "${CTS_HOME}" ]; then
  export CTS_HOME="${WORKSPACE}"
fi
if [ -z "${TS_HOME}" ]; then
  export TS_HOME="${CTS_HOME}/jakartaeetck/"
fi

if [ -z "${GF_RI_TOPLEVEL_DIR}" ]; then
    echo "Using glassfish7 for GF_RI_TOPLEVEL_DIR"
    export GF_RI_TOPLEVEL_DIR=glassfish7
fi

if [ -z "${GF_VI_TOPLEVEL_DIR}" ]; then
    echo "Using glassfish7 for GF_VI_TOPLEVEL_DIR"
    export GF_VI_TOPLEVEL_DIR=glassfish7
fi

if [[ "$JDK" == "JDK17" || "$JDK" == "jdk17" ]];then
  export JAVA_HOME=${JDK17_HOME}
fi
export PATH=$JAVA_HOME/bin:$PATH

export ANT_OPTS="-Xmx2G \
         -Djavax.xml.accessExternalStylesheet=all \
         -Djavax.xml.accessExternalSchema=all \
         -DenableExternalEntityProcessing=true \
         -Djavax.xml.accessExternalDTD=file,http"
export CTS_ANT_OPTS="-Djavax.xml.accessExternalStylesheet=all \
         -Djavax.xml.accessExternalSchema=all \
         -Djavax.xml.accessExternalDTD=file,http"

# Run CTS related steps
echo "JAVA_HOME ${JAVA_HOME}"
echo "ANT_HOME ${ANT_HOME}"
echo "CTS_HOME ${CTS_HOME}"
echo "TS_HOME ${TS_HOME}"
echo "PATH ${PATH}"
echo "Test suite to run ${test_suite}"

#Set default mailserver related env variables
if [ -z "$MAIL_HOST" ]; then
  export MAIL_HOST="localhost"
fi
if [ -z "$MAIL_USER" ]; then
  export MAIL_USER="user01@james.local"
fi
if [ -z "$MAIL_FROM" ]; then
  export MAIL_FROM="user01@james.local"
fi
if [ -z "$MAIL_PASSWORD" ]; then
  export MAIL_PASSWORD="1234"
fi
if [ -z "$SMTP_PORT" ]; then
  export SMTP_PORT="1025"
fi
if [ -z "$IMAP_PORT" ]; then
  export IMAP_PORT="1143"
fi

export TEST_SUITE=$(echo "${test_suite}" | tr '/' '_')
export JT_REPORT_DIR=${CTS_HOME}/jakartaeetck-report
export JT_WORK_DIR=${CTS_HOME}/jakartaeetck-work

##################################################

printf  "
******************************************************
* Installing Dependencies                            *
******************************************************

"

if [ -z "${GF_BUNDLE_ZIP}" ]; then
  echo "Download and install GlassFish 7"
  if [ -z "${GF_BUNDLE_URL}" ]; then
    if [ -z "$DEFAULT_GF_BUNDLE_URL" ]; then
      echo "[ERROR] GF_BUNDLE_URL not set"
      exit 1
    else
      echo "Using default url for GF bundle: $DEFAULT_GF_BUNDLE_URL"
      export GF_BUNDLE_URL="$DEFAULT_GF_BUNDLE_URL"
    fi
  fi
  if [ -z "${OLD_GF_BUNDLE_URL}" ]; then
    export OLD_GF_BUNDLE_URL="$GF_BUNDLE_URL"
  fi
  export GF_BUNDLE_ZIP="${CTS_HOME}/latest-glassfish.zip";
  wget --progress=bar:force --no-cache $GF_BUNDLE_URL -O "${GF_BUNDLE_ZIP}"
fi
rm -Rf "${CTS_HOME}/ri"
mkdir -p "${CTS_HOME}/ri"
unzip -q "${GF_BUNDLE_ZIP}" -d "${CTS_HOME}/ri"
chmod -R 777 "${CTS_HOME}/ri"

if [ -z "${GF_VI_BUNDLE_ZIP}" ]; then
  if [ -z "${GF_VI_BUNDLE_URL}" ]; then
    echo "Using GF_BUNDLE_URL for GF VI bundle: $GF_BUNDLE_URL"
    export GF_VI_BUNDLE_URL="$GF_BUNDLE_URL"
  fi
  export GF_VI_BUNDLE_ZIP="${CTS_HOME}/latest-glassfish-vi.zip"
  wget --progress=bar:force --no-cache "$GF_VI_BUNDLE_URL" -O "${GF_VI_BUNDLE_ZIP}"
fi
rm -Rf "${CTS_HOME}/vi"
mkdir -p "${CTS_HOME}/vi"
unzip -q "${GF_VI_BUNDLE_ZIP}" -d "${CTS_HOME}/vi"
chmod -R 777 "${CTS_HOME}/vi"

echo "Done, GlassFish RI and VI were both successfuly downloaded and unpacked."

export ADMIN_PASSWORD_FILE="${CTS_HOME}/admin-password.txt"
echo "Generating password file at ${ADMIN_PASSWORD_FILE} ..."
echo "AS_ADMIN_PASSWORD=adminadmin" > "${ADMIN_PASSWORD_FILE}"
echo "AS_ADMIN_PASSWORD=" > "${CTS_HOME}/change-admin-password.txt"
echo "AS_ADMIN_NEWPASSWORD=adminadmin" >> "${CTS_HOME}/change-admin-password.txt"
echo "" >> "${CTS_HOME}/change-admin-password.txt"

######################################################
######################################################
# Action done on failure. Until now there's nothing to stop or package.
set -e;
on_exit () {
  EXIT_CODE=$?
  printf  "
******************************************************
* Stopping all servers                               *
******************************************************

"
  set +e;
  if [ -d "${CTS_HOME}/ri/${GF_RI_TOPLEVEL_DIR}" ]; then
    "${CTS_HOME}/ri/${GF_RI_TOPLEVEL_DIR}/glassfish/bin/asadmin" stop-domain --kill || true;
  fi
  if [ -d "${CTS_HOME}/vi/${GF_VI_TOPLEVEL_DIR}" ]; then
    "${CTS_HOME}/vi/${GF_VI_TOPLEVEL_DIR}/glassfish/bin/asadmin" stop-domain --kill || true;
    "${JAVA_HOME}/bin/java" -Dderby.system.home="${CTS_HOME}/vi/${GF_VI_TOPLEVEL_DIR}/javadb/databases"\
      -classpath "${CTS_HOME}/vi/${GF_VI_TOPLEVEL_DIR}/javadb/lib/derbynet.jar\
:${CTS_HOME}/vi/${GF_VI_TOPLEVEL_DIR}/javadb/lib/derby.jar\
:${CTS_HOME}/vi/${GF_VI_TOPLEVEL_DIR}/javadb/lib/derbyshared.jar\
:${CTS_HOME}/vi/${GF_VI_TOPLEVEL_DIR}/javadb/lib/derbytools.jar"\
      org.apache.derby.drda.NetworkServerControl -h localhost -p 1527 shutdown || true;
  fi
  printf  "
******************************************************
* Processing and packaging results                   *
******************************************************

"
  if [ -d "${JT_REPORT_DIR}/${TEST_SUITE}" ]; then
    export HOST=$(hostname -f)
    echo "1 ${TEST_SUITE} ${HOST}" > "${CTS_HOME}/args.txt"
    mkdir -p "${WORKSPACE}/results/junitreports/"
    "${JAVA_HOME}/bin/java" -Djunit.embed.sysout=true -jar "${TS_HOME}/docker/JTReportParser/JTReportParser.jar" "${CTS_HOME}/args.txt"\
      "${JT_REPORT_DIR}" "${WORKSPACE}/results/junitreports/" || true
    rm -f "${CTS_HOME}/args.txt"

    if [ -z "${vehicle}" ]; then
      RESULT_FILE_NAME="${TEST_SUITE}-results.tar.gz"
      JUNIT_REPORT_FILE_NAME="${TEST_SUITE}-junitreports.tar.gz"
    else
      RESULT_FILE_NAME="${TEST_SUITE}_${vehicle_name}-results.tar.gz"
      JUNIT_REPORT_FILE_NAME="${TEST_SUITE}_${vehicle_name}-junitreports.tar.gz"
      sed -i.bak "s/name=\"${TEST_SUITE}\"/name=\"${TEST_SUITE}_${vehicle_name}\"/g" "${WORKSPACE}/results/junitreports/${TEST_SUITE}-junit-report.xml"
      mv "${WORKSPACE}/results/junitreports/${TEST_SUITE}-junit-report.xml" "${WORKSPACE}/results/junitreports/${TEST_SUITE}_${vehicle_name}-junit-report.xml"
    fi
    tar zcf "${JUNIT_REPORT_FILE_NAME}" -C "${WORKSPACE}" "results/junitreports/" || true
  fi

  tar zcf "${WORKSPACE}/${RESULT_FILE_NAME}" --ignore-failed-read -C "${WORKSPACE}"\
    "${CTS_HOME}/*.log"\
    "${JT_REPORT_DIR}"\
    "${JT_WORK_DIR}"\
    "${WORKSPACE}/results/junitreports/"\
    "${CTS_HOME}/jakartaeetck/bin/ts.*"\
    "${CTS_HOME}/vi/$GF_VI_TOPLEVEL_DIR/glassfish/domains/domain1/"\
    "${CTS_HOME}/ri/$GF_VI_TOPLEVEL_DIR/glassfish/domains/domain1/"\
    || true

  exit $EXIT_CODE;
}
trap on_exit EXIT

######################################################
######################################################

printf  "
******************************************************
* Configuring CI/RI (Glassfish 7)                    *
******************************************************

"
echo "Configuring RI domain at ${CTS_HOME}/ri/${GF_RI_TOPLEVEL_DIR}/glassfish/domains/domain1"
if [ -n "$GF_LOGGING_CFG_RI" ]; then
  cp "$GF_LOGGING_CFG_RI" "${CTS_HOME}/ri/${GF_RI_TOPLEVEL_DIR}/glassfish/domains/domain1/config/logging.properties"
fi
"${CTS_HOME}/ri/${GF_RI_TOPLEVEL_DIR}/glassfish/bin/asadmin" --user admin --passwordfile ${ADMIN_PASSWORD_FILE} start-domain
"${CTS_HOME}/ri/${GF_RI_TOPLEVEL_DIR}/glassfish/bin/asadmin" --user admin --passwordfile ${CTS_HOME}/change-admin-password.txt change-admin-password
"${CTS_HOME}/ri/${GF_RI_TOPLEVEL_DIR}/glassfish/bin/asadmin" --user admin --passwordfile ${ADMIN_PASSWORD_FILE} stop-domain
"${CTS_HOME}/ri/${GF_RI_TOPLEVEL_DIR}/glassfish/bin/asadmin" --user admin --passwordfile ${ADMIN_PASSWORD_FILE} start-domain
"${CTS_HOME}/ri/${GF_RI_TOPLEVEL_DIR}/glassfish/bin/asadmin" --user admin --passwordfile ${ADMIN_PASSWORD_FILE} enable-secure-admin
# secure admin will be applied after the restart, so we can still continue.
"${CTS_HOME}/ri/${GF_RI_TOPLEVEL_DIR}/glassfish/bin/asadmin" --user admin --passwordfile ${ADMIN_PASSWORD_FILE} version

"${CTS_HOME}/ri/${GF_RI_TOPLEVEL_DIR}/glassfish/bin/asadmin" --interactive=false  --user admin --passwordfile ${ADMIN_PASSWORD_FILE} delete-jvm-options -Dosgi.shell.telnet.port=6666
"${CTS_HOME}/ri/${GF_RI_TOPLEVEL_DIR}/glassfish/bin/asadmin" --user admin --passwordfile ${ADMIN_PASSWORD_FILE} create-jvm-options -Dosgi.shell.telnet.port=6667
"${CTS_HOME}/ri/${GF_RI_TOPLEVEL_DIR}/glassfish/bin/asadmin" --user admin --passwordfile ${ADMIN_PASSWORD_FILE} create-jvm-options -Dorg.glassfish.orb.iiop.orbserverid=200
"${CTS_HOME}/ri/${GF_RI_TOPLEVEL_DIR}/glassfish/bin/asadmin" --user admin --passwordfile ${ADMIN_PASSWORD_FILE} set server-config.jms-service.jms-host.default_JMS_host.port=7776
"${CTS_HOME}/ri/${GF_RI_TOPLEVEL_DIR}/glassfish/bin/asadmin" --user admin --passwordfile ${ADMIN_PASSWORD_FILE} set server-config.iiop-service.iiop-listener.orb-listener-1.port=3701
"${CTS_HOME}/ri/${GF_RI_TOPLEVEL_DIR}/glassfish/bin/asadmin" --user admin --passwordfile ${ADMIN_PASSWORD_FILE} set server-config.iiop-service.iiop-listener.SSL.port=4820
"${CTS_HOME}/ri/${GF_RI_TOPLEVEL_DIR}/glassfish/bin/asadmin" --user admin --passwordfile ${ADMIN_PASSWORD_FILE} set server-config.iiop-service.iiop-listener.SSL_MUTUALAUTH.port=4920
"${CTS_HOME}/ri/${GF_RI_TOPLEVEL_DIR}/glassfish/bin/asadmin" --user admin --passwordfile ${ADMIN_PASSWORD_FILE} set server-config.admin-service.jmx-connector.system.port=9696
"${CTS_HOME}/ri/${GF_RI_TOPLEVEL_DIR}/glassfish/bin/asadmin" --user admin --passwordfile ${ADMIN_PASSWORD_FILE} set server-config.network-config.network-listeners.network-listener.http-listener-1.port=8002
"${CTS_HOME}/ri/${GF_RI_TOPLEVEL_DIR}/glassfish/bin/asadmin" --user admin --passwordfile ${ADMIN_PASSWORD_FILE} set server-config.network-config.network-listeners.network-listener.http-listener-2.port=1045
"${CTS_HOME}/ri/${GF_RI_TOPLEVEL_DIR}/glassfish/bin/asadmin" --user admin --passwordfile ${ADMIN_PASSWORD_FILE} set server-config.network-config.network-listeners.network-listener.admin-listener.port=5858

echo "Stopping RI domain"
# We changed the admin port, server is not listening on port asadmin expects. Kill uses pid.
"${CTS_HOME}/ri/${GF_RI_TOPLEVEL_DIR}/glassfish/bin/asadmin" stop-domain --kill

### Update ts.jte for CTS run
cat "${TS_HOME}/bin/ts.jte" | sed "s/-Doracle.jdbc.mapDateToTimestamp/-Doracle.jdbc.mapDateToTimestamp -Djava.security.manager/"  > ts.save
cp ts.save $TS_HOME/bin/ts.jte


printf  "
******************************************************
* Configuring VI (Glassfish 7)                       *
******************************************************

"

if [[ $test_suite == ejb30/lite* ]] || [[ "ejb30" == $test_suite ]] ; then
  echo "Using higher JVM memory for EJB Lite suites to avoid OOM errors"
  sed -i.bak 's/-Xmx512m/-Xmx4096m/g' ${CTS_HOME}/vi/$GF_VI_TOPLEVEL_DIR/glassfish/domains/domain1/config/domain.xml
  sed -i.bak 's/-Xmx1024m/-Xmx4096m/g' ${CTS_HOME}/vi/$GF_VI_TOPLEVEL_DIR/glassfish/domains/domain1/config/domain.xml
  sed -i.bak 's/-Xmx512m/-Xmx2048m/g' ${CTS_HOME}/ri/${GF_RI_TOPLEVEL_DIR}/glassfish/domains/domain1/config/domain.xml
  sed -i.bak 's/-Xmx1024m/-Xmx2048m/g' ${CTS_HOME}/ri/${GF_RI_TOPLEVEL_DIR}/glassfish/domains/domain1/config/domain.xml

  # Change the memory setting in ts.jte as well.
  sed -i.bak 's/-Xmx1024m/-Xmx4096m/g' "${TS_HOME}/bin/ts.jte"
fi

# do ts.jte parameter substitution here of ${JVMOPTS_RUNTESTCOMMAND}
if [[ "$JDK" == "JDK17" || "$JDK" == "jdk17" ]];then
  echo "update ts.jte for GlassFish to use --add-opens options for Java SE 17"
  search="..JVMOPTS_RUNTESTCOMMAND."
  replace="--add-opens=java.base\\/java.io=ALL-UNNAMED --add-opens=java.base\\/java.lang=ALL-UNNAMED --add-opens=java.base\\/java.util=ALL-UNNAMED --add-opens=java.base\\/sun.net.www.protocol.jrt=ALL-UNNAMED --add-opens=java.naming\\/javax.naming.spi=ALL-UNNAMED --add-opens=java.rmi\\/sun.rmi.transport=ALL-UNNAMED --add-opens=jdk.management\\/com.sun.management.internal=ALL-UNNAMED --add-exports=java.naming\\/com.sun.jndi.ldap=ALL-UNNAMED"
  sed -i.bak "s/$search/$replace/" ${TS_HOME}/bin/ts.jte
  echo "updated ts.jte to use -add-opens for Java SE 17 "
fi

echo "Configuring VI domain at ${CTS_HOME}/ri/${GF_VI_TOPLEVEL_DIR}/glassfish/domains/domain1"
if [ -n "$GF_LOGGING_CFG_VI" ]; then
  cp "$GF_LOGGING_CFG_VI" "${CTS_HOME}/ri/${GF_VI_TOPLEVEL_DIR}/glassfish/domains/domain1/config/logging.properties"
fi
"${CTS_HOME}/vi/$GF_VI_TOPLEVEL_DIR/glassfish/bin/asadmin" --user admin --passwordfile ${ADMIN_PASSWORD_FILE} start-domain
"${CTS_HOME}/vi/$GF_VI_TOPLEVEL_DIR/glassfish/bin/asadmin" --user admin --passwordfile ${CTS_HOME}/change-admin-password.txt change-admin-password
"${CTS_HOME}/vi/$GF_VI_TOPLEVEL_DIR/glassfish/bin/asadmin" --user admin --passwordfile ${ADMIN_PASSWORD_FILE} version
"${CTS_HOME}/vi/$GF_VI_TOPLEVEL_DIR/glassfish/bin/asadmin" --user admin --passwordfile ${ADMIN_PASSWORD_FILE} create-jvm-options -Djava.security.manager
"${CTS_HOME}/vi/$GF_VI_TOPLEVEL_DIR/glassfish/bin/asadmin" --user admin --passwordfile ${ADMIN_PASSWORD_FILE} stop-domain

if [[ "$PROFILE" == "web" || "$PROFILE" == "WEB" ]];then
  KEYWORDS="javaee_web_profile|javamail_web_profile|connector_web_profile"
fi

if [ -z "${vehicle}" ];then
  echo "Vehicle not set. Running all vehichles"
else
  echo "Vehicle set. Running in vehicle: ${vehicle}"
  if [ -z "${KEYWORDS}" ]; then
    KEYWORDS=${vehicle}
  else
    KEYWORDS="(${KEYWORDS} & ${vehicle})"
  fi
fi

if [ ! -z "$KEYWORDS" ];then
  if [ ! -z "$USER_KEYWORDS" ]; then
    KEYWORDS="${KEYWORDS}${USER_KEYWORDS}"
  fi
else
  if [ ! -z "$USER_KEYWORDS" ]; then
    KEYWORDS="${USER_KEYWORDS}"
  fi
fi

if [ ! -z "${KEYWORDS}" ]; then
  CTS_ANT_OPTS="${CTS_ANT_OPTS} -Dkeywords=\"${KEYWORDS}\""
fi

echo "CTS_ANT_OPTS:${CTS_ANT_OPTS}"
echo "KEYWORDS:${KEYWORDS}"

cd ${TS_HOME}/bin
sed -i.bak "s#^report.dir=.*#report.dir=${JT_REPORT_DIR}#g" ts.jte
sed -i.bak "s#^work.dir=.*#work.dir=${JT_WORK_DIR}#g" ts.jte

sed -i.bak "s/^mailHost=.*/mailHost=${MAIL_HOST}/g" ts.jte
sed -i.bak "s/^mailuser1=.*/mailuser1=${MAIL_USER}/g" ts.jte
sed -i.bak "s/^mailFrom=.*/mailFrom=${MAIL_FROM}/g" ts.jte
sed -i.bak "s/^javamail.password=.*/javamail.password=${MAIL_PASSWORD}/g" ts.jte
sed -i.bak "s/^smtp.port=.*/smtp.port=${SMTP_PORT}/g" ts.jte
sed -i.bak "s/^imap.port=.*/imap.port=${IMAP_PORT}/g" ts.jte

sed -i.bak 's/^s1as.admin.passwd=.*/s1as.admin.passwd=adminadmin/g' ts.jte
sed -i.bak 's/^ri.admin.passwd=.*/ri.admin.passwd=adminadmin/g' ts.jte

sed -i.bak 's/^jdbc.maxpoolsize=.*/jdbc.maxpoolsize=30/g' ts.jte
sed -i.bak 's/^jdbc.steadypoolsize=.*/jdbc.steadypoolsize=5/g' ts.jte

sed -i.bak "s#^javaee.home=.*#javaee.home=${CTS_HOME}/vi/$GF_VI_TOPLEVEL_DIR/glassfish#g" ts.jte
sed -i.bak 's/^orb.host=.*/orb.host=localhost/g' ts.jte

sed -i.bak "s#^javaee.home.ri=.*#javaee.home.ri=${CTS_HOME}/ri/${GF_RI_TOPLEVEL_DIR}/glassfish#g" ts.jte
sed -i.bak 's/^orb.host.ri=.*/orb.host.ri=localhost/g' ts.jte

sed -i.bak 's/^ri.admin.port=.*/ri.admin.port=5858/g' ts.jte
sed -i.bak 's/^orb.port.ri=.*/orb.port.ri=3701/g' ts.jte

sed -i.bak "s#^registryURL=.*#registryURL=${UDDI_REGISTRY_URL}#g" ts.jte
sed -i.bak "s#^queryManagerURL=.*#queryManagerURL=${UDDI_REGISTRY_URL}#g" ts.jte

sed -i.bak "s/^wsgen.ant.classname=.*/wsgen.ant.classname=$\{ri.wsgen.ant.classname\}/g" ts.jte
sed -i.bak "s/^wsimport.ant.classname=.*/wsimport.ant.classname=$\{ri.wsimport.ant.classname\}/g" ts.jte

if [[ "$PROFILE" == "web" || "$PROFILE" == "WEB" ]]; then
  sed -i.bak "s/^javaee.level=.*/javaee.level=web connector jaxws jaxb javamail javaeedeploy wsmd/g" ts.jte
fi

sed -i.bak 's/^impl.deploy.timeout.multiplier=.*/impl.deploy.timeout.multiplier=240/g' ts.jte
sed -i.bak 's/^javatest.timeout.factor=.*/javatest.timeout.factor=2.0/g' ts.jte
sed -i.bak 's/^test.ejb.stateful.timeout.wait.seconds=.*/test.ejb.stateful.timeout.wait.seconds=480/g' ts.jte
sed -i.bak 's/^harness.log.traceflag=.*/harness.log.traceflag=false/g' ts.jte
sed -i.bak 's/^impl\.deploy\.timeout\.multiplier=240/impl\.deploy\.timeout\.multiplier=480/g' ts.jte
if [ ! -z "${CLIENT_LOGGING_CFG}" ]; then
  sed -i.bak "s#-Djava\.util\.logging\.config\.file=\${TS_HOME}/bin/client-logging.properties#-Djava\.util\.logging\.config\.file=${CLIENT_LOGGING_CFG}#g" ts.jte
fi
if [ "servlet" == "${test_suite}" ]; then
  sed -i.bak 's/s1as\.java\.endorsed\.dirs=.*/s1as.java.endorsed.dirs=\$\{endorsed.dirs\}\$\{pathsep\}\$\{ts.home\}\/endorsedlib/g' ts.jte
fi

if [ ! -z "${DATABASE}" ];then
  if [ "JavaDB" == "${DATABASE}" ]; then
    echo "Using the bundled JavaDB in GlassFish. No change in ts.jte required."
  else
    echo "Modifying DB related properties in ts.jte"
    "${TS_HOME}/docker/process_db_config.sh" ${DATABASE} ${TS_HOME}
  fi
fi

VI_SERVER_POLICY_FILE="${CTS_HOME}/vi/$GF_VI_TOPLEVEL_DIR/glassfish/domains/domain1/config/server.policy"
echo 'grant {' >> "${VI_SERVER_POLICY_FILE}"
echo 'permission java.io.FilePermission "${com.sun.aas.instanceRoot}${/}generated${/}policy${/}-", "read,write,execute,delete";' >> "${VI_SERVER_POLICY_FILE}"
echo '};' >> "${VI_SERVER_POLICY_FILE}"

printf  "
******************************************************
* Ant-based Configuration ...                        *
******************************************************

"
echo "Contents of ts.jte"
cat "${TS_HOME}/bin/ts.jte"

mkdir -p "${JT_REPORT_DIR}"
mkdir -p "${JT_WORK_DIR}"

export JAVA_VERSION=$(java -version 2>&1 | head -n 1 | awk -F '"' '{print $2}')
echo $JAVA_VERSION > ${JT_REPORT_DIR}/.jdk_version

cd  ${TS_HOME}/bin
ant config.vi.javadb
##### configVI.sh ends here #####

### populateMailbox for suites using mail server - Start ###
if [[ $test_suite == "javamail" || $test_suite == "samples" || $test_suite == "servlet" ]]; then
  ESCAPED_MAIL_USER=$(echo ${MAIL_USER} | sed -e 's/@/%40/g')
  cd  ${TS_HOME}/bin
  ant -DdestinationURL="imap://${ESCAPED_MAIL_USER}:${MAIL_PASSWORD}@${MAIL_HOST}:${IMAP_PORT}" populateMailbox
fi
### populateMailbox for javamail suite - End ###

if [[ $test_suite == javamail* || $test_suite == samples* || $test_suite == servlet* || $test_suite == appclient* || $test_suite == ejb* || $test_suite == jsp* ]]; then
  "${CTS_HOME}/ri/${GF_RI_TOPLEVEL_DIR}/glassfish/bin/asadmin" --user admin --passwordfile ${ADMIN_PASSWORD_FILE} create-jvm-options -Ddeployment.resource.validation=false
fi


##### configRI.sh ends here #####
cd  "${TS_HOME}/bin"
ant config.ri
##### configRI.sh ends here #####

### ctsStartStandardDeploymentServer.sh starts here #####
cd "$TS_HOME/bin";
echo "ant start.auto.deployment.server > /tmp/deploy.out 2>&1 & "
ant start.auto.deployment.server > /tmp/deploy.out 2>&1 &
### ctsStartStandardDeploymentServer.sh ends here #####

printf  "
******************************************************
* Executing Ant-based Tests ...                      *
******************************************************

"

cd "$TS_HOME/bin";
if [ -z "$KEYWORDS" ]; then
  ant -f xml/impl/glassfish/s1as.xml run.cts -Dant.opts="${CTS_ANT_OPTS} ${ANT_OPTS}" -Dtest.areas="${test_suite}"
else
  ant -f xml/impl/glassfish/s1as.xml run.cts -Dkeywords=\"${KEYWORDS}\" -Dant.opts="${CTS_ANT_OPTS} ${ANT_OPTS}" -Dtest.areas="${test_suite}"
fi


cd "$TS_HOME/bin";
# Check if there are any failures in the test. If so, re-run those tests.
FAILED_COUNT=0
ERROR_COUNT=0
FAILED_COUNT=$(cat ${JT_REPORT_DIR}/${TEST_SUITE}/text/summary.txt | grep 'Failed.' | wc -l)
ERROR_COUNT=$(cat ${JT_REPORT_DIR}/${TEST_SUITE}/text/summary.txt | grep 'Error.' | wc -l)
if [[ $FAILED_COUNT -gt 0 || $ERROR_COUNT -gt 0 ]]; then
  echo "One or more tests failed. Failure count:$FAILED_COUNT/Error count:$ERROR_COUNT"
  echo "Re-running only the failed, error tests"
  if [ -z "$KEYWORDS" ]; then
    ant -f xml/impl/glassfish/s1as.xml run.cts -Dant.opts="${CTS_ANT_OPTS} ${ANT_OPTS}" -Drun.client.args="-DpriorStatus=fail,error"  -DbuildJwsJaxws=false -Dtest.areas="${test_suite}"
  else
    ant -f xml/impl/glassfish/s1as.xml run.cts -Dkeywords=\"${KEYWORDS}\" -Dant.opts="${CTS_ANT_OPTS} ${ANT_OPTS}" -Drun.client.args="-DpriorStatus=fail,error"  -DbuildJwsJaxws=false -Dtest.areas="${test_suite}"
  fi

  # Generate combined report for both the runs.
  ant -Dreport.for=com/sun/ts/tests/$test_suite -Dreport.dir="${JT_REPORT_DIR}/${TEST_SUITE}" -Dwork.dir="${JT_WORK_DIR}/${TEST_SUITE}" report
fi
