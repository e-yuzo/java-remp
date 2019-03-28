/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cps.kafkaremp.main;

/**
 *
 * @author yuzo
 */
import com.cps.kafkaremp.constants.IKafkaConstants;
import com.cps.kafkaremp.constants.PublisherCreator;
import com.cps.kafkaremp.constants.SubscriberCreator;
import java.time.Duration;

import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

public class Initializer {

    public static void main(String[] args) {
        runProducer();
        //runConsumer();
    }

    static void runConsumer() {
        try (Consumer<Long, String> consumer = SubscriberCreator.createSubscriber()) {
            int noMessageFound = 0;
            while (true) {
                ConsumerRecords<Long, String> consumerRecords = consumer.poll(Duration.ofSeconds(1));
                // 1000 is the time in milliseconds consumer will wait if no record is found at broker.
                if (consumerRecords.count() == 0) {
                    noMessageFound++;
                    if (noMessageFound > IKafkaConstants.MAX_NO_MESSAGE_FOUND_COUNT) // If no message found count is reached to threshold exit loop.
                    {
                        break;
                    } else {
                        continue;
                    }
                }
                //print each record.
                consumerRecords.forEach(record -> {
                    System.out.println("Record Key " + record.key());
                    System.out.println("Record value " + record.value());
                    System.out.println("Record partition " + record.partition());
                    System.out.println("Record offset " + record.offset());
                });
                // commits the offset of record to broker.
                consumer.commitAsync();
            }
        }
    }

    static void runProducer() {
        Producer<Long, String> producer = PublisherCreator.createPublisher();
        for (int index = 0; index < IKafkaConstants.MESSAGE_COUNT; index++) {
            ProducerRecord<Long, String> record = new ProducerRecord<>(IKafkaConstants.TOPIC_NAME,
                    "This is record " + index);
            try {
                RecordMetadata metadata = producer.send(record).get();
                System.out.println("Record sent with key " + index + " to partition " + metadata.partition()
                        + " with offset " + metadata.offset());
            } catch (ExecutionException | InterruptedException e) {
                System.out.println("Error in sending record");
                System.out.println(e);
            }
        }
    }
}
