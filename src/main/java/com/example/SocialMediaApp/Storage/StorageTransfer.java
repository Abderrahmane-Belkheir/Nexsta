package com.example.SocialMediaApp.Storage;

import lombok.AllArgsConstructor;


@AllArgsConstructor
public class StorageTransfer {


    private final StorageDir sourceDir;
    private final StorageDir destinationDir;

    public StorageDir getSource(){
        return sourceDir;
    }

    public StorageDir getDestination(){
        return destinationDir;
    }

}
