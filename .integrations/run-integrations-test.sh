#!/usr/bin/env bash

set -e

SRC_DIR=$(git rev-parse --show-toplevel)
(cd "${SRC_DIR}" && mvn clean package -DskipTests)

(cd "${SRC_DIR}/.integrations" && docker-compose up --build --force-recreate -d)

CONTAINER_NAME="pulsar-io-aws-eventbridge-test"
PULSAR_ADMIN="docker exec ${CONTAINER_NAME} /pulsar/bin/pulsar-admin"

echo "Waiting for Pulsar service ..."
until curl http://localhost:8080/metrics > /dev/null 2>&1 ; do sleep 1; done
echo "Pulsar service available"

echo "Run sink connector"
SINK_NAME="test-pulsar-io-aws-eventbridge"
NAR_PATH="/pulsar/connectors/pulsar-io-aws-eventbridge.nar"
SINK_CONFIG_FILE="/pulsar/connectors/pulsar-io-aws-eventbridge.yaml"
INPUT_TOPIC="test-aws-eventbridge"
${PULSAR_ADMIN} sinks localrun -a ${NAR_PATH} \
        --tenant public --namespace default --name ${SINK_NAME} \
        --sink-config-file ${SINK_CONFIG_FILE} \
        -i ${INPUT_TOPIC}


echo "Waiting for sink and source ..."
sleep 30

echo "Run integration tests"
(cd "$SRC_DIR" && mvn -Dtest="*TestIntegration" test -DfailIfNoTests=false)
(cd "${SRC_DIR}/.integrations" && docker-compose down --rmi local)
