package com.example.SocialMediaApp.Upload.application;

import com.example.SocialMediaApp.Upload.Exceptions.UnauthorizedResourceAccessException;
import com.example.SocialMediaApp.Upload.Exceptions.UploadSessionExpiredException;
import com.example.SocialMediaApp.Upload.domain.UploadPhase;
import com.example.SocialMediaApp.Upload.domain.UploadSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
 class UploadStateService {

    private final RedisTemplate<String,Object> redisTemplate;

    public UploadSession validateUploadSession(String userId, String key,UploadPhase uploadPhase) {

        UploadSession uploadSession;

        if(uploadPhase==UploadPhase.CONFIRMED){
            uploadSession=(UploadSession) redisTemplate.opsForValue().get(key);
        }else{
            uploadSession =(UploadSession) redisTemplate.opsForValue().getAndDelete(key);
        }

        if(uploadSession==null) throw new UploadSessionExpiredException("Upload Session expired or invalid");

        // confirming the user relation with upload is only done in confirmed upload phase after the file being uploaded
        if(uploadPhase==UploadPhase.CONFIRMED){
            String actualUserId=uploadSession.getUserId();
            if(!userId.equals(actualUserId)) {
                // logging later
                throw new UnauthorizedResourceAccessException("Action could not be completed");
            }
        }

        return uploadSession;
    }


}
