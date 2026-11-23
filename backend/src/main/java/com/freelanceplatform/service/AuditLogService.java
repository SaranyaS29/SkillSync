package com.freelanceplatform.service;

import com.freelanceplatform.entity.AuditLog;
import com.freelanceplatform.repository.AuditLogRepository;
import com.freelanceplatform.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {
    private final AuditLogRepository logs; private final UserAccountRepository users;
    public AuditLogService(AuditLogRepository logs,UserAccountRepository users){this.logs=logs;this.users=users;}
    @Transactional
    public void record(String email,String action,String resourceType,Long resourceId){
        AuditLog log=new AuditLog(); log.setActor(email==null?null:users.findByEmailIgnoreCase(email).orElse(null)); log.setAction(action); log.setResourceType(resourceType); log.setResourceId(resourceId); logs.save(log);
    }
}
