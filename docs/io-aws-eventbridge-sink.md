---
dockerfile: "https://hub.docker.com/r/streamnative/pulsar-io-aws-eventbridge"
alias: AWS EventBridge Sink Connector
---

The [AWS EventBridge](https://aws.amazon.com/eventbridge/) sink connector pulls data from Pulsar topics and persists
data to AWS EventBridge.

![](/docs/aws-eventbridge-sink.png)

# Features

This section describes features of the AWS EventBridge sink connector. For details about how to configure these
features, see [how to configure](#how-to-configure).

## At least once delivery

Pulsar connector framework provides three guarantees: **at-most-once**、**at-least-once** and **effectively-once**. The *
*effectively-once** needs the support of the Sink downstream system. After investigation, it was found that EventBridge
did not meet the requirements.

Therefore, this connector can provide the **at-least-once** delivery at most, and when the user configures the *
*effectively-once**, it will throw exception.

## Data convert

Referring to the above, we know that all events in EventBridge are
in [JSON format](https://docs.aws.amazon.com/eventbridge/latest/userguide/eb-events.html).

The Pulsar support multi schema type, When receive a data, the connectors will convert data to JSON string. The
connectors automatically recognizes the pulsar data and convert it according to the following table:

| Pulsar Schema  | Convert to JSON | Note                                                                                                                                       |
|----------------|-----------------|--------------------------------------------------------------------------------------------------------------------------------------------|
| Primitive      | ✔*              | Just support primitive type is string and data is JSON format.                                                                             |
| Avro           | ✔               | Take advantage of toolkit conversions                                                                                                      |
| Json           | ✔               | Just send it directly                                                                                                                      |
| Protobuf       | X               | The Protobuf schema is based on the Avro schema. It uses Avro as an intermediate format, so it may not provide the best effort conversion. |
| ProtobufNative | ✔               | Take advantage of toolkit conversions                                                                                                      |

In event bridge, the user data is in the `detail$data` field.

```json
{
  "version": "0",
  "id": "6a7e8feb-b491-4cf7-a9f1-bf3703467718",
  "detail": {
    "data": {
      "instance-id": " i-1234567890abcdef0",
      "state": "terminated"
    }
  }
}
```

## Meta data mapping

In EventBridge, a complete event contains
many [system fields](https://docs.aws.amazon.com/eventbridge/latest/userguide/eb-events.html#eb-custom-event). This
system fields can help users to config rule.

A event data **Event**:

```json
{
  "version": "0",
  "id": "6a7e8feb-b491-4cf7-a9f1-bf3703467718",
  "source-type": "test-aws-event-bridge-sink-connector",
  "detail-type": "topic_name_test_1",
  "source": "aws.ec2",
  "account": "111122223333",
  "time": "2017-12-22T18:43:48Z",
  "region": "us-west-1",
  "resources": [
    "arn:aws:ec2:us-west-1:123456789012:instance/i-1234567890abcdef0"
  ],
  "detail": {
    "data": {
      "instance-id": " i-1234567890abcdef0",
      "state": "terminated"
    }
  }
}
```

This connectors will mapping following fields:

- sourceType →The default value is `${{Connector Name}}`
- detailType → The default value is `${{Topic Name}}`

And, This connector support set metadata of pulsar to every **Event**(Set in the **detail** field).

Users can select the desired meata-data through the following configuration:

```jsx
#
optional: schema_version | partition | event_time | publish_time
#
message_id | sequence_id | producer_name | key | properties
metaDataField = event_time, message_id
```

A contain meta data **Event**:

```json
{
  "version": "0",
  "id": "6a7e8feb-b491-4cf7-a9f1-bf3703467718",
  "source-type": "test-aws-event-bridge-sink-connector",
  "detail-type": "topic_name_test_1",
  "source": "aws.ec2",
  "account": "111122223333",
  "time": "2017-12-22T18:43:48Z",
  "region": "us-west-1",
  "resources": [
    "arn:aws:ec2:us-west-1:123456789012:instance/i-1234567890abcdef0"
  ],
  "detail": {
    "data": {
      "instance-id": " i-1234567890abcdef0",
      "state": "terminated"
    },
    "event_time": 789894645625,
    "message_id": "1,1,1"
  }
}
```

## Parallelism

The parallelism of Sink execution can be configured, use the scheduling mechanism of the Function itself, and multiple
sink instances will be scheduled to run on different worker nodes. Multiple sinks will consume message together
according to the configured subscription mode.

Since EventBus doesn't need to guarantee sequentiality, So the connectors supports the `shared` subscription model, When
you need to increase write throughput, you can configure:

```jsx
parallelism = 4
```

> When sink config set `retainOrdering` is false, it means use `Shared` subscription mode.
>

## Batch Put

This connectors support batch put event, It is mainly controlled by the following three parameters:

- **batchSize**: When the buffered message is larger than batchSize, will trigger flush(put) events. `0` means no
  trigger.
- **maxBatchBytes**: When the buffered message data size is larger than maxBatchBytes, will trigger flush(put) events.
  This value should be less than 256KB and **greater** then 0, The default value is 256KB.
- **batchTimeMs**: When the interval between the last flush is exceeded batchTimeMs, will trigger flush(put)
  events.  `0` means no trigger.

In addition to these three parameters that control flush
behavior, [in AWS EventBridge](https://docs.aws.amazon.com/eventbridge/latest/userguide/eb-putevent-size.html), batches
larger than 265KB per write are not allowed. So, when buffered message larger than 256KB, will trigger flush.

## Retry Put

In AWS Event Bridge, about Handling failures with PutEvents, It just suggests to retry. we'll retry each error message
until it succeeds.

This connector will provider flow two config to controller retry strategy:

```jsx
maxRetryCount: 100 // Maximum retry send event count, when event send failed.
intervalRetryTimeMs: 1000 //The interval time(milliseconds) for each retry, when event send failed.
```

# How to get

This section describes how to build the AWS EventBridge sink connector.

## Work with Function Worker

You can get the AWS EventBridge sink connector using one of the following methods if you
use [Pulsar Function Worker](https://pulsar.apache.org/docs/en/functions-worker/) to run the connector in a cluster.

- Download the NAR package
  from [the download page](https://github.com/streamnative/pulsar-io-aws-eventbridge/releases/).

- Build it from the source code.

To build the AWS EventBridge sink connector from the source code, follow these steps.

1. Clone the source code to your machine.

   ```bash
   git clone https://github.com/streamnative/pulsar-io-aws-eventbridge
   ```

2. Build the connector in the `pulsar-io-aws-eventbridge` directory.

   ```bash
   mvn clean install -DskipTests
   ```

   After the connector is successfully built, a `NAR` package is generated under the target directory.

   ```bash
   ls target
   pulsar-io-aws-eventbridge-{{connector:version}}.nar
   ```

## Work with Function Mesh

You can pull the AWS EventBridge sink connector Docker image from
the [Docker Hub](https://hub.docker.com/r/streamnative/pulsar-io-aws-eventbridge) if you
use [Function Mesh](https://functionmesh.io/docs/connectors/run-connector) to run the connector.

# How to configure

Before using the AWS EventBridge sink connector, you need to configure it. This table outlines the properties and the
descriptions.

| Name                    | Type   | Required | Default           | Description                                                                                                                                                                                                            |
|-------------------------|--------|----------|-------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `accessKeyId`           | String | No       | "" (empty string) | The Event Bridge access key ID.                                                                                                                                                                                        |
| `secretAccessKey`       | String | no       | "" (empty string) | The Event Bridge secret access key.                                                                                                                                                                                    |
| `role`                  | String | no       | "" (empty string) | The aws role to use. Implies to use an assume role.                                                                                                                                                                    |
| `roleSessionName`       | String | no       | "" (empty string) | The aws role session name to use. Implies to use an assume role.                                                                                                                                                       |
| `stsEndpoint`           | String | no       | "" (empty string) | The sts endpoint to use, default to default sts endpoint.                                                                                                                                                              |
| `stsRegion`             | String | no       | "" (empty string) | The sts region to use, defaults to the 'region' config or env region.                                                                                                                                                  |
| `region`                | String | yes      | "" (empty string) | The Event Bridge region.                                                                                                                                                                                               |
| `eventBusName`          | String | yes      | "" (empty string) | The Event Bus name.                                                                                                                                                                                                    |
| `eventBusResourceName`  | String | no       | "" (empty string) | The Event Bus Aws resource name(ARN).                                                                                                                                                                                  |
| `metaDataField`         | String | no       | "" (empty string) | The metadata field will add to the event. separate multiple fields with commas. optional: schema_version `partition`,  `event_time`, `publish_time`, `message_id`, `sequence_id`, `producer_name`, `key`, `properties` |
| `batchPendingQueueSize` | int    | no       | 1000              | Pending Queue size, This value must greater than batchMaxSize.                                                                                                                                                         |
| `batchMaxSize`          | int    | no       | 10                | Maximum number of batch messages. Member must less than or equal to 10(AWS Required)                                                                                                                                   |
| `batchMaxBytesSize`     | long   | no       | 640               | Maximum number of batch bytes payload size. This value cannot be greater than 512KB.                                                                                                                                   |
| `batchMaxTimeMs`        | long   | no       | 5000              | Batch max wait time: milliseconds.                                                                                                                                                                                     |
| `maxRetryCount`         | long   | no       | 100               | Maximum retry send event count, when the event put failed.                                                                                                                                                             |
| `intervalRetryTimeMs`   | long   | no       | 1000              | The interval time(milliseconds) for each retry, when the event put failed.                                                                                                                                             |


## Work with Function Worker

You can create a configuration file (JSON or YAML) to set the properties if you
use [Pulsar Function Worker](https://pulsar.apache.org/docs/en/functions-worker/) to run connectors in a cluster.

**Example**

* JSON

   ```json
    {
        "name": "eventbridge-sink",
        "archive": "connectors/pulsar-io-aws-eventbridge-{{connector:version}}.nar",
        "tenant": "public",
        "namespace": "default",
        "inputs": [
          "test-aws-eventbridge-pulsar"
        ],
        "parallelism": 1,
        "configs": {
          "accessKeyId": "{{Your access access key}}",
          "secretAccessKey": "{{Your secret access key}}",
          "region": "test-region",
          "eventBusName": "test-event-bus-name"
      }
    }
    ```

* YAML

    ```yaml
     name: eventbridge-sink
     archive: 'connectors/pulsar-io-aws-eventbridge-{{connector:version}}.nar'
     tenant: public
     namespace: default
     inputs:
     - test-aws-eventbridge-pulsar
     parallelism: 1
     configs:
       accessKeyId: '{{Your access access key}}'
       secretAccessKey: '{{Your secret access key}}'
       region: test-region
       eventBusName: test-event-bus-name
    ```

## Work with Function Mesh

You can create
a [CustomResourceDefinitions (CRD)](https://kubernetes.io/docs/concepts/extend-kubernetes/api-extension/custom-resources/)
to create a AWS EventBridge sink connector. Using CRD makes Function Mesh naturally integrate with the Kubernetes
ecosystem. For more information about Pulsar sink CRD configurations,
see [sink CRD configurations](https://functionmesh.io/docs/connectors/io-crd-config/sink-crd-config).

You can define a CRD file (YAML) to set the properties as below.

```yaml
apiVersion: compute.functionmesh.io/v1alpha1
kind: Sink
metadata:
  name: aws-eventbridge-sink-sample
spec:
  image: streamnative/pulsar-io-aws-eventbridge:{{connector:version}}
  replicas: 1
  maxReplicas: 1
  input:
    topics:
      - persistent://public/default/test-aws-eventbridge-pulsar
  sinkConfig:
    accessKeyId: '{{Your access access key}}'
    secretAccessKey: '{{Your secret access key}}'
    region: test-region
    eventBusName: test-event-bus-name
  pulsar:
    pulsarConfig: "test-pulsar-sink-config"
  resources:
    limits:
    cpu: "0.2"
    memory: 1.1G
    requests:
    cpu: "0.1"
    memory: 1G
  java:
    jar: connectors/pulsar-io-aws-eventbridge-{{connector:version}}.jar
  clusterName: test-pulsar
  autoAck: false
```

# How to use

You can use the AWS EventBridge sink connector with Function Worker or Function Mesh.

## Work with Function Worker

> **Note**
>
> Currently, the AWS EventBridge sink connector cannot run as a built-in connector as it uses the JAR package.

1. Start a Pulsar cluster in standalone mode.
    ```
    PULSAR_HOME/bin/pulsar standalone
    ```

2. Run the AWS EventBridge sink connector.
    ```
    PULSAR_HOME/bin/pulsar-admin sinks localrun \
    --sink-config-file <aws-eventbridge-sink-config.yaml>
    --archive <pulsar-io-aws-eventbridge-{{connector:version}}.nar>
    ```

   Or, you can create a connector for the Pulsar cluster.
    ```
    PULSAR_HOME/bin/pulsar-admin sinks create \
    --sink-config-file <aws-eventbridge-sink-config.yaml>
    --archive <pulsar-io-aws-eventbridge-{{connector:version}}.nar>
    ```

3. Send messages to a Pulsar topic.

   This example sends ten “hello” messages to the `test-aws-eventbridge-pulsar` topic in the `default` namespace of
   the `public` tenant.

     ```
    PULSAR_HOME/bin/pulsar-client produce public/default/test-aws-eventbridge-pulsar --messages hello -n 10
     ```

4. Show data on AWS EventBridge.

The connector will send flow format JSON event to event bridge.

```json
{
  "version": "0",
  "id": "6a7e8feb-b491-4cf7-a9f1-bf3703467718",
  "source-type": "test-aws-event-bridge-sink-connector",
  "detail-type": "topic_name_test_1",
  "source": "aws.ec2",
  "account": "111122223333",
  "time": "2017-12-22T18:43:48Z",
  "region": "us-west-1",
  "resources": [
    "arn:aws:ec2:us-west-1:123456789012:instance/i-1234567890abcdef0"
  ],
  "detail": {
    "data": "hello"
  }
}
```

You can config [rule](https://docs.aws.amazon.com/eventbridge/latest/userguide/eb-rules.html) to match event on AWS
EventBridge, and set target
to [CloudWatch](https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/WhatIsCloudWatch.html) to look data.

## Work with Function Mesh

This example describes how to create a AWS EventBridge sink connector for a Kubernetes cluster using Function
Mesh.

### Prerequisites

- Create and connect to a [Kubernetes cluster](https://kubernetes.io/).

- Create a [Pulsar cluster](https://pulsar.apache.org/docs/en/kubernetes-helm/) in the Kubernetes cluster.

- [Install the Function Mesh Operator and CRD](https://functionmesh.io/docs/install-function-mesh/) into the Kubernetes
  cluster.

- Prepare AWS EventBridge service. For details,
  see [Getting Started with AWS EventBridge](https://docs.aws.amazon.com/eventbridge/latest/userguide/eb-setup.html).

### Step

1. Define the AWS EventBridge sink connector with a YAML file and save it as `sink-sample.yaml`.

   This example shows how to publish the AWS EventBridge sink connector to Function Mesh with a Docker image.
   ```yaml
   apiVersion: compute.functionmesh.io/v1alpha1
   kind: Sink
   metadata:
      name: aws-eventbridge-sink-sample
   spec:
      image: streamnative/pulsar-io-aws-eventbridge:{{connector:version}}
      replicas: 1
      maxReplicas: 1
      input:
         topics:
            - persistent://public/default/test-aws-eventbridge-pulsar
      sinkConfig:
         accessKeyId: '{{Your access access key}}'
         secretAccessKey: '{{Your secret access key}}'
         region: test-region
         eventBusName: test-event-bus-name
      pulsar:
         pulsarConfig: "test-pulsar-sink-config"
      resources:
         limits:
         cpu: "0.2"
         memory: 1.1G
         requests:
         cpu: "0.1"
         memory: 1G
      java:
         jar: connectors/pulsar-io-aws-eventbridge-{{connector:version}}.jar
      clusterName: test-pulsar
      autoAck: false
   ```

2. Apply the YAML file to create the AWS EventBridge sink connector.

   **Input**

    ```
    kubectl apply -f <path-to-sink-sample.yaml>
    ```

   **Output**

    ```
    sink.compute.functionmesh.io/aws-eventbridge-sink-sample created
    ```

3. Check whether the AWS EventBridge sink connector is created successfully.

   **Input**

    ```
    kubectl get all
    ```

   **Output**

    ```
    NAME                                         READY   STATUS      RESTARTS   AGE
    pod/aws-eventbridge-sink-sample-0               1/1    Running     0          77s
    ```

   After that, you can produce and consume messages using the AWS EventBridge sink connector between Pulsar and
   EventBridge.

