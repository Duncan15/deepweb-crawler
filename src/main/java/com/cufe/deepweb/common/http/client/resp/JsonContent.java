package com.cufe.deepweb.common.http.client.resp;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JsonContent extends RespContent {
    private static ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
    }
    private JsonNode root;
    JsonContent(String rawContent) {
        try {
            root = mapper.readTree(rawContent);
        } catch (IOException ex) {
            //ignored
        }
    }

    public JsonNode getRoot() {
        return root;
    }
}
