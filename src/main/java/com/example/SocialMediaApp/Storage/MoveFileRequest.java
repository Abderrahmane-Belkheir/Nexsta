package com.example.SocialMediaApp.Storage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public  class MoveFileRequest{
    private String bucketId;
    private String sourceKey;
    private String destinationBucket;
    private String destinationKey;
}
