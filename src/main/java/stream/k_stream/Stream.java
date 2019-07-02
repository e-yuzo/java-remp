
package stream.k_stream;

import java.security.Security;
import java.util.Arrays;
import stream.interfaces.KafkaConstantsInterface;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import remp_operations.BrokerOperations;
import utils.ProtocolStructureLength;

/**
 *
 * @author yuzo
 */
public class Stream {
    
    public static byte[] handleMessage(byte[] dataX, BrokerOperations bo) {
        Security.addProvider(new BouncyCastleProvider());
        bo.protocol = new ProtocolStructureLength("broker", dataX.length);
        byte[] ticket = new byte[bo.protocol.ticket];
        System.out.println(bo.protocol.ticket);
        System.out.println(bo.protocol.xk);
        System.out.println(bo.protocol.encryptedWithAI);
        System.out.println(dataX.length);
        System.arraycopy(dataX, bo.protocol.xk + bo.protocol.encryptedWithAI, ticket, 0, bo.protocol.ticket);
        bo.decryptTicket(ticket, bo.tk);
        //decrypting stuff that were encrypted with AI
        System.out.println("STATE OF IDENTITY (TICKET): " + Arrays.toString(bo.i));
        byte[] encryptedByAI = new byte[bo.protocol.encryptedWithAI];
        System.arraycopy(dataX, bo.protocol.xk, encryptedByAI, 0, bo.protocol.encryptedWithAI);
        bo.decryptEncryptedDataByAuthKey(encryptedByAI);
        //create DATAy
        byte[] xk = new byte[bo.protocol.xk];
        System.arraycopy(dataX, 0, xk, 0, bo.protocol.xk);
        byte[] dataY = bo.createDataY(xk);
        return dataY;
    }

    public static void main(final String[] args) {
        final Properties streamsConfiguration = new Properties();
        // Give the Streams application a unique name.  The name must be unique in the Kafka cluster
        // against which the application is run.
        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "secure-kafka-streams");
        streamsConfiguration.put(StreamsConfig.CLIENT_ID_CONFIG, "secure-kafka-streams-client");
        // Where to find secure (!) Kafka broker(s).  In the VM, the broker listens on port 9093 for
        // SSL connections.
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConstantsInterface.KAFKA_BROKERS);
        // Specify default (de)serializers for record keys and for record values.
        streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.ByteArray().getClass().getName()); //can convert to byteArray()
        // Security settings.
        // 1. These settings must match the security settings of the secure Kafka cluster.
        // 2. The SSL trust store and key store files must be locally accessible to the application.
        //    Typically, this means they would be installed locally in the client machine (or container)
        //    on which the application runs.  To simplify running this example, however, these files
        //    were generated and stored in the VM in which the secure Kafka broker is running.  This
        //    also explains why you must run this example application from within the VM.
        //streamsConfiguration.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
//    streamsConfiguration.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, "/etc/security/tls/kafka.client.truststore.jks");
//    streamsConfiguration.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "test1234");
//    streamsConfiguration.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, "/etc/security/tls/kafka.client.keystore.jks");
//    streamsConfiguration.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, "test1234");
//    streamsConfiguration.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, "test1234");

        final StreamsBuilder builder = new StreamsBuilder();
        KStream<String, byte[]> source = builder.stream("Blue_Sea");
        BrokerOperations bo = new BrokerOperations();
        KStream<String, byte[]> transformed = source.flatMap(//use filter with predicate to DELETE (method) records
                (key, value) -> {
                    List<KeyValue<String, byte[]>> result = new LinkedList<>();
                    System.out.println(value.length);
                    byte[] v = handleMessage(value, bo);
                    result.add(KeyValue.pair(key, v));
                    return result;
                }
        );
        
        transformed.to("Red_Sea");
//        Stream<String, String> clearText = builder.stream("Blue_Sea");
//        clearText.flatMapValues(value -> Arrays.asList(value.toLowerCase(Locale.getDefault()).split("\\W+")))
//                .groupBy((key, value) -> value)
//                .count(Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as("counts-store"))
//                .toStream()
//                .to("Red_Sea", Produced.with(Serdes.String(), Serdes.Long()));
        //clearText.to("Red_Sea");
        final KafkaStreams streams = new KafkaStreams(builder.build(), streamsConfiguration);
        // Always (and unconditionally) clean local state prior to starting the processing topology.
        // We opt for this unconditional call here because this will make it easier for you to play around with the example
        // when resetting the application for doing a re-run (via the Application Reset Tool,
        // http://docs.confluent.io/current/streams/developer-guide.html#application-reset-tool).
        //
        // The drawback of cleaning up local state prior is that your app must rebuilt its local state from scratch, which
        // will take time and will require reading all the state-relevant data from the Kafka cluster over the network.
        // Thus in a production scenario you typically do not want to clean up always as we do here but rather only when it
        // is truly needed, i.e., only under certain conditions (e.g., the presence of a command line flag for your app).
        // See `ApplicationResetExample.java` for a production-like example.
        streams.cleanUp();
        streams.start();

        // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            streams.close();
        }));
    }
}
