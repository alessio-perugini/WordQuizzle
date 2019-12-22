package server.MyMemoryAPI;

import java.util.*;
import java.io.IOException;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.*;

@JsonDeserialize(using = Subject.Deserializer.class)
@JsonSerialize(using = Subject.Serializer.class)
public class Subject {
    public Boolean boolValue;
    public String stringValue;

    static class Deserializer extends JsonDeserializer<Subject> {
        @Override
        public Subject deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            Subject value = new Subject();
            switch (jsonParser.getCurrentToken()) {
            case VALUE_TRUE:
            case VALUE_FALSE:
                value.boolValue = jsonParser.readValueAs(Boolean.class);
                break;
            case VALUE_STRING:
                value.stringValue = jsonParser.readValueAs(String.class);
                break;
            default: throw new IOException("Cannot deserialize Subject");
            }
            return value;
        }
    }

    static class Serializer extends JsonSerializer<Subject> {
        @Override
        public void serialize(Subject obj, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            if (obj.boolValue != null) {
                jsonGenerator.writeObject(obj.boolValue);
                return;
            }
            if (obj.stringValue != null) {
                jsonGenerator.writeObject(obj.stringValue);
                return;
            }
            throw new IOException("Subject must not be null");
        }
    }
}
