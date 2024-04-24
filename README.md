# 07 - Messaging using Kafka

## Messaging

Messaging is another method of communication between services.

### Broker

A message broker is a middleware that ensures the communication between services. It is responsible for receiving and distributing messages between producers and consumers. It can also provide additional features such as message queuing, message persistence, and fault tolerance.

Message brokers can support different messaging patterns, such as point-to-point, publish-subscribe, and request-reply. We will focus on the publish-subscribe pattern, which is the most common in event-driven architectures.

### Channel (Topic)

A channel is a logical path for communication between the producer(s) and the consumer(s). The producer sends messages to the channel, and the consumer receives messages from the channel.

### Producer/Consumer

The producer (Publisher) sends messages to the broker, and the Consumer (Subscriber or Processor) receives messages from the broker and processes them. The producer and consumer do not need to know about each other, and they can be added or removed without affecting the other components.

The communication between the producer and the consumer is asynchronous, which means that the producer does not wait for the consumer to process the message. This allows for better scalability and performance.

The message is sent to a topic, which is a logical channel for communication. The producer sends messages to a topic, and the consumer receives messages from a topic. The broker ensures that the messages are delivered to all consumers subscribed to the topic.

![Messaging](./img/pubsub.png)

Image source: *https://aws.amazon.com/what-is/pub-sub-messaging/*

### Challenges

- **Idempotency**: The same message may be delivered multiple times.
- **Ordering**: The order of messages may not be guaranteed.
- **Monitoring**: It's harder to monitor the communication between services.

### REST vs Messaging

| **Aspect**              | **REST**                                                                                         | **Messaging**                                                                                 |
|-------------------------|--------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------|
| **Communication Style** | Synchronous: The client sends a request and waits for a response. It is one to one.              | Asynchronous: Producers send messages without waiting for a response. It may be many to many. |
| **Receiver**            | The client knows the address of the server and sends requests directly to it.                    | Producers send messages to a broker, which then distributes them to consumers.                |
| **Data Format**         | Typically JSON or XML.                                                                           | Can vary widely (e.g., JSON, XML, binary) depending on the messaging protocol and system.     |
| **Reliability**         | Depends on HTTP(S) protocol: can implement retries for idempotent requests.                      | Higher reliability through message queuing, durable storage, and retry mechanisms.            |
| **Scalability**         | Can scale vertically and horizontally but may require load balancers for efficient distribution. | Naturally supports high scalability through message brokers that manage message distribution. |
| **Use Cases**           | Ideal for CRUD operations and direct interactions between services.                              | Suited for event-driven architectures.                                                        |
| **Coupling**            | Tends to couple client and server to the API contract.                                           | Reduces coupling between services by using events or messages.                                |

## Kafka

Apache Kafka is one of the implementations of the message broker. It is a distributed streaming platform that can be used for event-driven architectures, real-time analytics, and data integration. It is designed to be scalable, fault-tolerant, and durable.

### Producer

The producer is responsible for sending messages to the Kafka broker. It can send messages to one or more topics.

We can use the Emitter interface to send messages to the Kafka broker. The Emitter interface is a part of the Reactive Messaging API, which is a part of the MicroProfile specification.

```java
@ApplicationScoped
public class MyProducer {

    @Channel("my-topic") // The name of the channel
    Emitter<String> emitter; // Emitter interface

    public void send(String message) {
        emitter.send(message); // Send the message to the channel
    }
}
```

#### Serialization

The producer serializes the message before sending it to the broker. Serialization is the process of converting the message into a format that can be transmitted over the network. The message can be serialized to different formats, such as JSON, XML, or binary.

In Quarkus, we can define the serialization using the `application.properties` file:

```properties
mp.messaging.outgoing.<channel-name>.value.serializer=io.quarkus.kafka.client.serialization.ObjectMapperSerializer
```

### Consumer

The consumer is responsible for receiving messages from the Kafka broker. We can use the `@Incoming` annotation to create a consumer that listens to a specific topic.

```java
@ApplicationScoped
public class MyConsumer {

    @Incoming("my-topic") // The name of the channel
    public void process(String message) {
        // Process the message 
        System.out.println(message);
    }
}
```

Additionally, `@Blocking` annotation can be used to process the message in a blocking way due to the nature of Quarkus Reactive Architecture. Similarly, `@Transactional` can be used to process the message in a blocking way to perform a transaction in the database.

### Consumer and Producer

We can additionally use the `@Outgoing` along with the `@Incoming` annotations to create a consumer and producer in the same class. Thus, accepting the message from one channel, doing some work, and sending it to another.

The method annotated with `@Outgoing` cannot be called from the code, but the framework needs to invoke it.

```java
@ApplicationScoped
public class MyConsumerProducer {

    @Incoming("my-topic") // The name of the channel
    @Outgoing("my-other-topic") // The name of the channel
    public String process(String message) {
        // Process the message
        return message;
    }
}
```

### Zookeeper

Apache Kafka uses Apache Zookeeper to manage the brokers and the topics. Zookeeper is a distributed coordination service that is used to maintain configuration information, provide distributed synchronization, and provide group services.

## State of the project

- In the `passenger-service` `PassengerService` class, we have a new method `addNotificationForPassenger` that creates a
  new `Notification` for the passenger.

## Tasks

### 0. Running docker

Install [Docker desktop](https://docs.docker.com/engine/install/) or another docker client.

### 1. Introduction to task

Right now, we have a simple notification system in the `passenger-service` that handles notifications about flight
cancellations. Now, we want to add a new feature that will also notify the passengers about the baggage status change. We
want to use Kafka to handle communication between the `passenger-service` and the `baggage-service`.

### 2. Add Kafka producer to the `baggage-service`

#### 2.1. Prepare `BaggageStateChange` class

Prepare a new class `BaggageStateChange` in the `baggage-service`, which will be the payload of the message. The class
should contain the following fields:

- `baggageId` - the id of the baggage
- `passengerId` - the id of the passenger
- `newState` - the new state of the baggage

#### 2.2. Add Baggage State Change producer

Prepare a new class `BaggageStateChangeProducer` in the `baggage-service` that will be responsible for sending
the `BaggageStateChange` message to the Kafka `baggage-state-change` topic.

Inject `Emitter` with `@Channel` annotation to publish messages to the `baggage-state-change` topic in the `send`
method.

#### 2.3. Configure Kafka serialization

Because we are sending an object, we need to configure the serialization of the messages. Add the following properties
to the `application.properties` file in `baggage-service`:

```properties
mp.messaging.outgoing.baggage-state-change.value.serializer=io.quarkus.kafka.client.serialization.ObjectMapperSerializer
```

#### 2.4. Connect the `BaggageStateChangeProducer` with the `BaggageService`

When the state changes, we want to send the message as well. Go to the `BaggageService` and inject
the `BaggageStateChangeProducer` and call the `send` method when the baggage state changes.

### 3. Add Kafka consumer to the `passenger-service`

#### 3.1. Copy Baggage State Change class to the `passenger-service`

Copy the `BaggageStateChange` class along with the `BaggageStatus` enum to the `passenger-service` to `kafka/model`
package.

#### 3.2. Add Baggage State Change consumer

Now, we need to process the messages incoming from the `baggage-service`. Prepare a new
class `BaggageStateChangeConsumer` in the `passenger-service` that will be responsible for processing
the `BaggageStateChange` message from the Kafka `baggage-state-change` topic.

This class should accept the `@Incomming` requests from the `baggage-state-change` topic, process the message, and
create a new `Notification` for the passenger.

### 4. Test the communication

Thanks to the Quarkus Dev Services, we can easily test the communication between the services using Kafka. Dev
services will start the Kafka broker for us. We will prepare it for production later.

Scenario:

1. Start the `baggage-service` and `passenger-service` in dev mode.
2. Create a new passenger.
3. Create new baggage for the passenger.
4. Change the state of the baggage as claimed.
5. Get the notifications and see if both check-in and claimed notifications are present.

### 5. Prepare the application for production

Kill the dev services and prepare the application for production.

#### 5.1. Add Zookeeper to the docker-compose

Add the following service to the `docker-compose.yaml` file:

```yaml
  zookeeper:
    image: quay.io/strimzi/kafka:0.39.0-kafka-3.6.1
    command: [
      "sh", "-c",
      "bin/zookeeper-server-start.sh config/zookeeper.properties"
    ]
    ports:
      - "2181:2181"
    environment:
      LOG_DIR: /tmp/logs
    networks:
      - app-network
```

#### 5.2. Add Kafka to the docker-compose

Add the following service to the `docker-compose.yaml` file:

```yaml
    kafka:
    image: quay.io/strimzi/kafka:0.39.0-kafka-3.6.1
    command: [
      "sh", "-c",
      "bin/kafka-server-start.sh config/server.properties --override listeners=$${KAFKA_LISTENERS} --override advertised.listeners=$${KAFKA_ADVERTISED_LISTENERS} --override zookeeper.connect=$${KAFKA_ZOOKEEPER_CONNECT}"
    ]
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      LOG_DIR: "/tmp/logs"
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
      KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
    networks:
      - app-network
```

#### 5.3. Add environment variables for `passenger-service` and `baggage-service`

Now, we need to add the environment variables for the `passenger-service` and `baggage-service` for them to be able to
connect to the Kafka broker. In the `docker-compose.yaml` file, add the following environment variables to
the `passenger-service` and `baggage-service`:

```yaml
    environment:
      KAFKA_BOOTSTRAP_SERVERS: kafka:9092
```

#### 5.4. Build and run the application

```bash
cd passenger-service && mvn clean install && cd ../baggage-service && mvn clean install && cd ../flight-service && mvn clean install && cd .. && docker compose up --build
```

#### 5.5. Test the application

Test the application as in the previous step.

### 6. Submit the solution

1. Finish the tasks
2. Push the changes to the main branch
3. GitHub Classroom automatically prepared a feedback pull request for you
4. Go to the repository on GitHub and find the feedback pull request
5. Set label to "Submitted"
6. GitHub Actions will run basic checks for your submission
7. Teacher will evaluate the submission as well and give you feedback

## Hints

- It's good to get yourself familiar with the messaging concepts.

## Troubleshooting

- Check if your docker engine is running.

## Further reading

- https://quarkus.io/guides/kafka-reactive-getting-started
- https://quarkus.io/guides/kafka
- https://www.cloudkarafka.com/blog/cloudkarafka-what-is-zookeeper.html
- https://aws.amazon.com/what-is/pub-sub-messaging/