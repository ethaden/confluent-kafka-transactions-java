/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package io.confluent.csta.examples.transactions.kstreams;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.KeyValue;

public class KStreamsHeaderForward {

    public static Properties loadConfig(final String configFile) throws IOException {
        if (!Files.exists(Paths.get(configFile))) {
            throw new IOException(configFile + " not found.");
        }
        final Properties cfg = new Properties();
        try (InputStream inputStream = new FileInputStream(configFile)) {
            cfg.load(inputStream);
        }
        return cfg;
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java <jar file> <property file>");
            System.exit(1);
        }
        try {
            final Properties config = loadConfig(args[0]);
/*             config.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            config.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            config.put("value.serializer",
                    "org.apache.kafka.common.serialization.StringSerializer");
            config.put("value.deserializer",
                    "org.apache.kafka.common.serialization.StringDeserializer"); */
            config.put("acks", "all");
            config.put("enable.idempotence", "true");
            String topicFrom = config.getProperty("topic.from");
            String topicTo = config.getProperty("topic.to");
            config.remove("topic.from");
            config.remove("topic.to");
            Serde<String> stringSerde = Serdes.String();
            StreamsBuilder builder = new StreamsBuilder();
            // Simple topology which just forwards the messages from topicFrom to topicTo
/*             builder
                .stream(topicFrom, Consumed.with(stringSerde, stringSerde))
                .to(topicTo, Produced.with(stringSerde, stringSerde)); */
            // This topology maps each value to itself (just as an example of a simple transformation), but retains context including headers
/*             builder
                .stream(topicFrom, Consumed.with(stringSerde, stringSerde))
                .mapValues(s -> s)
                .to(topicTo, Produced.with(stringSerde, stringSerde)); */
            // This topology disassembles the incoming messages and re-assembles them (simulating some work load).
            // Context including headers is still preserved automatically.
            builder
                .stream(topicFrom, Consumed.with(stringSerde, stringSerde))
                .map((key, value) -> KeyValue.pair(key, value.toUpperCase()))
                .to(topicTo, Produced.with(stringSerde, stringSerde));
            try (KafkaStreams streams = new KafkaStreams(builder.build(), config)) {
                streams.start();
            }

        } catch (IOException e) {
            System.err.println("An exception occurred while load properties file: " + e);
            System.exit(1);
        }

    }
}
