## AWS EventBridge sink connector

The AWS EventBridge sink connector is a [Pulsar IO connector](http://pulsar.apache.org/docs/en/next/io-overview/) for
copying data from Pulsar to [Amazon EventBridge](https://aws.amazon.com/eventbridge/).

![](docs/aws-eventbridge-sink.png)

Currently, AWS EventBridge connector versions (`x.y.z`) are based on Pulsar versions (`x.y.z`).

| AWS EventBridge connector version                                            | Pulsar version                                              | Doc                                                                                                                                             |
|:-----------------------------------------------------------------------------|:------------------------------------------------------------|:------------------------------------------------------------------------------------------------------------------------------------------------|
| [3.0.x](https://github.com/streamnative/pulsar-io-aws-eventbridge/releases)  | [3.0.0](https://pulsar.apache.org/download/#older-release)  | [AWS EventBridge sink connector doc](https://github.com/streamnative/pulsar-io-aws-eventbridge/blob/branch-3.0/docs/aws-eventbridge-sink.md)    |
| [2.11.x](https://github.com/streamnative/pulsar-io-aws-eventbridge/releases) | [2.11.1](https://pulsar.apache.org/download/#older-release) | [AWS EventBridge sink connector doc](https://github.com/streamnative/pulsar-io-aws-eventbridge/blob/branch-2.11/docs/aws-eventbridge-sink.md)   |
| [2.10.x](https://github.com/streamnative/pulsar-io-aws-eventbridge/releases) | [2.10.4](https://pulsar.apache.org/download/#older-release) | [AWS EventBridge sink connector doc](https://github.com/streamnative/pulsar-io-aws-eventbridge/blob/branch-2.10.4/docs/aws-eventbridge-sink.md) |

## Project layout

Below are the sub folders and files of this project and their corresponding descriptions.

```bash
├── conf // examples of configuration files of this connector
├── docs // user guides of this connector
├── image // docker file of this connector
├── script // scripts of this connector
├── src // source code of this connector
│   ├── checkstyle // checkstyle configuration files of this connector
│   ├── license // license header for this project. `mvn license:format` can
    be used for formatting the project with the stored license header in this directory
│   │   └── ALv2
│   ├── main // main source files of this connector
│   │   └── java
│   ├── spotbugs // spotbugs configuration files of this connector
│   └── test // test related files of this connector
│       └── java
```

## License

Licensed under the Apache License Version 2.0: http://www.apache.org/licenses/LICENSE-2.0