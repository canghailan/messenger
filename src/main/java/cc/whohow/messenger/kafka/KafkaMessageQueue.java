package cc.whohow.messenger.kafka;

import cc.whohow.messenger.Message;
import cc.whohow.messenger.MessageFactory;
import cc.whohow.messenger.MessengerManager;
import cc.whohow.messenger.SimpleMessageQueue;
import cc.whohow.messenger.util.Closeables;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;

/**
 * Kafka消息队列
 */
public class KafkaMessageQueue extends SimpleMessageQueue {
    private static final Logger log = LogManager.getLogger();

    private String topic;
    private long timeout;
    private Producer<String, String> producer;
    private Consumer<String, String> consumer;

    public KafkaMessageQueue(JsonNode kafka, MessageFactory messageFactory, MessengerManager messengerManager) {
        super(messageFactory, messengerManager);
        try {
            // bootstrap.servers
            // group.id
            Properties properties = new Properties();
            for (Iterator<String> i = kafka.fieldNames(); i.hasNext(); ) {
                String name = i.next();
                properties.put(name, kafka.get(name).asText(null));
            }
            properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

            topic = kafka.path("topic").asText(null);
            if (topic == null) {
                throw new IllegalArgumentException("topic");
            }

            timeout = kafka.path("poll.timeout.ms").asLong(1000);
            producer = new KafkaProducer<>(properties);
            consumer = new KafkaConsumer<>(properties);
        } catch (Throwable e) {
            close();
            throw e;
        }
    }

    @Override
    public void send(Message message) {
        try {
            log.debug(producer.send(new ProducerRecord<>(topic, getKey(message), message.toString())).get());
        } catch (Throwable e) {
            log.error("send {}", message);
            log.error(e);
        }
    }

    private String getKey(Message message) {
        String to = message.getTo();
        if (to != null) {
            return to;
        }
        return message.getFrom();
    }

    @Override
    public void run() {
        consumer.subscribe(Collections.singleton(topic));
        log.info("subscribe");
        while (true) {
            try {
                ConsumerRecords<String, String> records = consumer.poll(timeout);
                for (ConsumerRecord<String, String> record : records) {
                    try {
                        log.debug(record.value());
                        receive(messageFactory.newMessage(record.value()));
                    } catch (Throwable e) {
                        log.error("receive {}", record);
                        log.error(e.getMessage(), e);
                    }
                }
                consumer.commitSync();
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void close() {
        Closeables.close(producer);
        Closeables.close(consumer);
    }
}
