
package stream.producer;

import stream.interfaces.KafkaConstantsInterface;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.util.Properties;
import java.util.Scanner;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

/**
 * 
 * @author yuzo
 * 
 */

public class Publisher {
    
//    public static Publisher<Long, String> createPublisher() {
//        Properties configProperties = new Properties();
//        configProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConstantsInterface.KAFKA_BROKERS);
//        configProperties.put(ProducerConfig.CLIENT_ID_CONFIG, KafkaConstantsInterface.CLIENT_ID);
//        configProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
//        configProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
//        //props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, CustomPartitioner.class.getName());
//        return new KafkaProducer<>(configProperties);
//    }

    public static void main(String[] argv) {
        String topicName = "Blue_Sea";
        Scanner input = new Scanner(System.in);
        System.out.println("Enter message (type exit to quit)");

        //Configure the Publisher
        Properties configProperties = new Properties();
        configProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConstantsInterface.KAFKA_BROKERS);
        configProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        configProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        Producer producer = new KafkaProducer(configProperties);
        
        //Create records
        String message = input.nextLine();
        while(!message.equals("exit")) {
            //TODO: Make sure to use the ProducerRecord constructor that does not take partition Id
            ProducerRecord<String, String> record = new ProducerRecord<>(topicName, message);
            producer.send(record);
            message = input.nextLine();
        }
        input.close();
        producer.close();
    }
}
