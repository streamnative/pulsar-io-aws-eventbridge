FROM apachepulsar/pulsar:latest

COPY .integrations/pulsar-io-aws-eventbridge.yaml /pulsar/connectors/

COPY target/pulsar-io-aws-eventbridge-*.nar /pulsar/connectors/pulsar-io-aws-eventbridge.nar

ENTRYPOINT ["/pulsar/bin/pulsar", "standalone"]
