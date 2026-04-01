package com.example.SocialMediaApp.Storage;

import lombok.Getter;

@Getter
public enum StorageDir {

    PERMANENT("permanent"),
    PERMANENT_PUBLIC("public"),
    TEMPORARY("temporary"),
    DELETED("deleted"),
    DRAFT("draft");


    private final String dirName;

    StorageDir(String dirName) {
        this.dirName = dirName;
    }

}
