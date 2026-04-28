package com.Nexsta.Storage.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MoveTemporaryContentRequest {
    private String destinationBucket;
    private String destinationFolder;
    private List<String> filePaths;
}
