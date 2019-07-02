package stream.consumer;

import java.security.Security;
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
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import remp_operations.SubscriberOperations;
import utils.ByteUtils;
import utils.ProtocolStructureLength;

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
    private KafkaConsumer<String, byte[]> kafkaConsumer;

    public ConsumerThread(String topicName, String groupId) {
        this.topicName = topicName;
        this.groupId = groupId;
    }

    @Override
    public void run() {
        Properties configProperties = new Properties();
        configProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConstantsInterface.KAFKA_BROKERS);
        configProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        configProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        configProperties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProperties.put(ConsumerConfig.CLIENT_ID_CONFIG, KafkaConstantsInterface.CLIENT_ID);
        configProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, KafkaConstantsInterface.OFFSET_RESET_EARLIER);

        //Figure out where to start processing messages from
        kafkaConsumer = new KafkaConsumer<>(configProperties);
        kafkaConsumer.subscribe(Arrays.asList(topicName));
        
        //Start processing messages
        SubscriberOperations so = new SubscriberOperations();
        try {
            while (true) {
                ConsumerRecords<String, byte[]> records = kafkaConsumer.poll(Duration.ofSeconds(100));
                for (ConsumerRecord<String, byte[]> record : records) {
                    String message = handleMessage(record.value(), so);
                    System.out.println(message);
                }
            }
        } catch (WakeupException ex) {
            System.out.println("Exception caught " + ex.getMessage());
        } finally {
            kafkaConsumer.close();
            System.out.println("After closing KafkaConsumer");
        }
    }
    
    public String handleMessage(byte[] dataY, SubscriberOperations so) {
        Security.addProvider(new BouncyCastleProvider());
        so.protocol = new ProtocolStructureLength("subscriber", dataY.length);
        System.out.println("SIGNED SIZIZE: " + so.protocol.signed);
        byte[] id = so.extractIdentity(dataY);
        byte[] randomN = so.extracRandomNumber(dataY);
        byte[] sk = so.deriveSk(id, randomN);
        byte[] hash = so.extracHash(dataY);
        byte[] hashIdentity = ByteUtils.combine(id, hash);
        boolean signok = so.verifyData(so.extractSignature(dataY), hashIdentity);
        System.out.println("Verify returned: " + signok);
        System.out.println(dataY.length);
        return so.extractClearMessage(sk, dataY);
    }

    public KafkaConsumer<String, byte[]> getKafkaConsumer() {
        return this.kafkaConsumer;
    }
    
}
