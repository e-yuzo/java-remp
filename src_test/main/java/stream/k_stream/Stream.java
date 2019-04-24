package stream.k_stream;

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

/**
 *
 * @author yuzo
 */
public class Stream {

    public static String wat(String value) {
        return "0";
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
        streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName()); //can convert to byteArray()
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
        KStream<String, String> source = builder.stream("Blue_Sea");
        //source.to("Red_Sea");
        // Write the input data as-is to the output topic.
        //builder.stream("Blue_Sea").to("Red_Sea");

        KStream<String, String> transformed = source.flatMap(//use filter with predicate to DELETE (method) records
                // Here, we generate two output records for each input record.
                // We also change the key and value types.
                // Example: (345L, "Hello") -> ("HELLO", 1000), ("hello", 9000)
                (key, value) -> {
                    List<KeyValue<String, String>> result = new LinkedList<>();
                    result.add(KeyValue.pair("i'm encrypted lol", value + "i think i'm encrypted"));
                    result.add(KeyValue.pair("uc ant re a d me", value + "u cant readme md"));
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
