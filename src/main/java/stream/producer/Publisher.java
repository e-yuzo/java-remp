
package stream.producer;

import java.security.Security;
import java.util.Arrays;
import stream.interfaces.KafkaConstantsInterface;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.util.Properties;
import java.util.Scanner;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import remp_operations.PublisherOperations;

/**
 * 
 * @author yuzo
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
        //Configure the Publisher
        Properties configProperties = new Properties();
        configProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConstantsInterface.KAFKA_BROKERS);
        configProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        configProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());

        Producer producer = new KafkaProducer(configProperties);
        PublisherOperations po = new PublisherOperations();
        //Create records
        String message = input.nextLine();
        while(!message.equals("exit")) {
            byte[] structuredMessage = messageHandler(message, po);
            ProducerRecord<String, byte[]> record = new ProducerRecord<>(topicName, structuredMessage);
            producer.send(record);
            message = input.nextLine();
        }
        input.close();
        producer.close();
    }
    
    public static byte[] messageHandler(String message, PublisherOperations po) {
        Security.addProvider(new BouncyCastleProvider());
        System.out.println("AI from PUBLISHER: " + Arrays.toString(po.ai));
        System.out.println(po.ai.length);
        byte[] rn = po.generateRandomNumber();
        byte[] sessionKey = po.deriveSK(rn);
        byte[] encryptedMessageK = po.encryptMessageK(message, sessionKey, rn); //important messageK
        System.out.println("XK LENGTH: " + encryptedMessageK.length);
        byte[] dataX = po.createDataX(encryptedMessageK);
        System.out.println("DATAX: " + dataX.length);
        return dataX;
    }
    
}
