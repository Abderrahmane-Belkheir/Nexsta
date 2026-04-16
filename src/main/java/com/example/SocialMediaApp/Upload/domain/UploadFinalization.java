package com.example.SocialMediaApp.Upload.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
@AllArgsConstructor
@Getter
@NoArgsConstructor
public class UploadFinalization {

    private  List<MediaUpload> mediaUploads;
    private  List<String> filePaths=new ArrayList<>();

    public void addFilePath(String filePath){
        filePaths.add(filePath);
    }

    public boolean checkEmptyFiles(){
        return filePaths.isEmpty();
    }

}
