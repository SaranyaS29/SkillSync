package com.freelanceplatform.dto.file;
import java.time.Instant;
public record FileResponse(Long id,String originalName,String downloadUrl,Instant createdAt) {}
