package ru.mishapp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.charset.StandardCharsets;


@RequiredArgsConstructor
public class JsonSerializer<T> implements Serializer<T> {
    
    private final String encoding = StandardCharsets.UTF_8.name();
    private final ObjectMapper mapper;
    
    @Override
    public byte[] serialize(String topic, T data) {
        try {
            if (data == null) {
                return new byte[]{};
            } else {
                return mapper.writeValueAsString(data).getBytes(encoding);
            }
        } catch (Exception e) {
            throw new SerializationException("Error when serializing StringValue to byte[] ", e);
        }
    }
}
