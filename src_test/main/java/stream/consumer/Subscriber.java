package stream.consumer;

import stream.interfaces.KafkaConstantsInterface;
import java.time.Duration;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;

import java.util.Arrays;
import java.util.Properties;
import java.util.Scanner;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

/**
 * 
 * @author yuzo
 */

public class Subscriber {

    public static void main(String[] argv) throws Exception {
        String topicName = "Red_Sea";
        String groupId = "ClientID_10";
        
        ConsumerThread consumerRunnable = new ConsumerThread(topicName, groupId);
        Thread consumerThread = new Thread(consumerRunnable);
        consumerThread.start();
        
        Scanner input = new Scanner(System.in);
        String message = "";
        while (!message.equals("exit")) {
            message = input.next();
        }
        
        consumerRunnable.getKafkaConsumer().wakeup();
        System.out.println("Bye...");
        consumerThread.join();
    }
}

class ConsumerThread implements Runnable {

    private final String topicName;
    private final String groupId;
    private KafkaConsumer<String, String> kafkaConsumer;

    public ConsumerThread(String topicName, String groupId) {
        this.topicName = topicName;
        this.groupId = groupId;
    }

    @Override
    public void run() {
        Properties configProperties = new Properties();
        configProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConstantsInterface.KAFKA_BROKERS);
        configProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName()); //this is important serializer, deserializer
        configProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        configProperties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProperties.put(ConsumerConfig.CLIENT_ID_CONFIG, KafkaConstantsInterface.CLIENT_ID);
        configProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, KafkaConstantsInterface.OFFSET_RESET_EARLIER);

        //Figure out where to start processing messages from
        kafkaConsumer = new KafkaConsumer<>(configProperties);
        kafkaConsumer.subscribe(Arrays.asList(topicName));
        
        //Start processing messages
        try {
            while (true) {
                ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofSeconds(100));
                for (ConsumerRecord<String, String> record : records) {
                    System.out.println(record.value());
                }
            }
        } catch (WakeupException ex) {
            System.out.println("Exception caught " + ex.getMessage());
        } finally {
            kafkaConsumer.close();
            System.out.println("After closing KafkaConsumer");
        }
    }

    public KafkaConsumer<String, String> getKafkaConsumer() {
        return this.kafkaConsumer;
    }
}
