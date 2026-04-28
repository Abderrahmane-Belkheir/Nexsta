package com.Nexsta.Storage.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MoveFolderRequest {
    private String bucketId;
    private String sourceFolder;
    private String destinationFolder;
    private String destinationBucket;

    @Override
    public String toString() {
        return "MoveFolderRequest{" +
                "bucketId='" + bucketId + '\'' +
                ", sourceFolder='" + sourceFolder + '\'' +
                ", destinationFolder='" + destinationFolder + '\'' +
                ", destinationBucket='" + destinationBucket + '\'' +
                '}';
    }
}
