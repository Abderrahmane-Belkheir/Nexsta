package com.Nexsta.Upload.application;

import com.Nexsta.Upload.Exceptions.UnauthorizedResourceAccessException;
import com.Nexsta.Upload.Exceptions.UploadSessionExpiredException;
import com.Nexsta.Upload.domain.UploadPhase;
import com.Nexsta.Upload.domain.UploadSession;
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
            uploadSession=(UploadSession) objectRedisTemplate.opsForValue().get(key);
        }else{
            uploadSession =(UploadSession) objectRedisTemplate.opsForValue().getAndDelete(key);
        }

        if(uploadSession==null){
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

        return uploadSession;
    }


}
