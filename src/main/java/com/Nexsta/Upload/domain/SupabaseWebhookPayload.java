package com.Nexsta.Upload.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SupabaseWebhookPayload {
    private String type;
    private String table;
    private String schema;
    private StorageRecord record;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class StorageRecord {
        private String id;
        private String name;

        @JsonProperty("bucket_id")
        private String bucketId;

        @JsonProperty("created_at")
        private Instant createdAt;

        @JsonProperty("path_tokens")
        private List<String> pathTokens;

        private Map<String, Object> metadata;
    }
}
