package com.example.SocialMediaApp.Storage;

import lombok.Getter;

@Getter
public enum StorageDir {

    PERMANENT("permanent"),
    TEMPORARY("temporary"),
    DELETED("deleted");

    private final String dirName;

    StorageDir(String dirName) {
        this.dirName = dirName;
    }

}
