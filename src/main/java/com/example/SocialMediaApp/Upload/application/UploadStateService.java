package com.example.SocialMediaApp.Upload.application;

import com.example.SocialMediaApp.Upload.Exceptions.UnauthorizedResourceAccessException;
import com.example.SocialMediaApp.Upload.Exceptions.UploadSessionExpiredException;
import com.example.SocialMediaApp.Upload.domain.UploadPhase;
import com.example.SocialMediaApp.Upload.domain.UploadSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
 class UploadStateService {

    private final RedisTemplate<String,Object> objectRedisTemplate;

    public UploadSession validateUploadSession(String userId, String key,UploadPhase uploadPhase) {

        UploadSession uploadSession;

        if(uploadPhase==UploadPhase.CONFIRMED){
            log.info("Checking request id : "+key);
            uploadSession=(UploadSession) objectRedisTemplate.opsForValue().get(key);
        }else{
            log.info("Checking filePath : "+key);
            uploadSession =(UploadSession) objectRedisTemplate.opsForValue().getAndDelete(key);
        }

        if(uploadSession==null){
            log.error("Upload Session Not Found");
            throw new UploadSessionExpiredException("Upload Session expired or invalid");
        }
        // confirming the user relation with upload is only done in confirmed upload phase after the file being uploaded
        if(uploadPhase==UploadPhase.CONFIRMED){
            String actualUserId=uploadSession.getUserId();
            if(!userId.equals(actualUserId)) {
                // logging later
                throw new UnauthorizedResourceAccessException("Action could not be completed");
            }
        }

        log.info("Upload Session Validated");

        return uploadSession;
    }


}
