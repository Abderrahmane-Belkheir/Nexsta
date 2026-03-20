package com.example.SocialMediaApp.Storage;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class StorageTransfer {

    private final StorageDir source;
    private final StorageDir destination;

    public String getSource(){
        return source.getDirName();
    }

    public String getDestination(){
        return destination.getDirName();
    }

}
