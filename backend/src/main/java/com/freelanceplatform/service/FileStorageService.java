package com.freelanceplatform.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Locale;
import java.util.UUID;

@Service
public class FileStorageService {
    public record StoredFile(String key,String originalName) {}
    private static final long MAX_BYTES=10L*1024*1024;
    private final Path root;
    public FileStorageService(@Value("${app.files.directory}") String directory) {
        root=Path.of(directory).toAbsolutePath().normalize();
        try { Files.createDirectories(root); } catch(IOException ex) { throw new IllegalStateException("Could not initialize private file storage",ex); }
    }
    public StoredFile store(MultipartFile file) {
        if(file==null||file.isEmpty()) throw new IllegalArgumentException("Select a file to upload");
        if(file.getSize()>MAX_BYTES) throw new IllegalArgumentException("File exceeds the 10 MB limit");
        String original=file.getOriginalFilename()==null?"upload":Path.of(file.getOriginalFilename()).getFileName().toString();
        String extension=extension(original);
        byte[] bytes;
        try { bytes=file.getBytes(); } catch(IOException ex) { throw new IllegalArgumentException("Could not read uploaded file"); }
        validateSignature(extension,bytes);
        String key=UUID.randomUUID()+"."+extension;
        Path destination=resolve(key);
        try { Files.write(destination,bytes,StandardOpenOption.CREATE_NEW,StandardOpenOption.WRITE); }
        catch(IOException ex) { throw new IllegalStateException("Could not store uploaded file",ex); }
        return new StoredFile(key,original);
    }
    public byte[] read(String key) {
        try { return Files.readAllBytes(resolve(key)); } catch(IOException ex) { throw new IllegalArgumentException("File not found"); }
    }
    public void delete(String key) { try { Files.deleteIfExists(resolve(key)); } catch(IOException ignored) {} }
    private Path resolve(String key) {
        Path path=root.resolve(key).normalize();
        if(!path.startsWith(root)||path.equals(root)) throw new IllegalArgumentException("Invalid file key");
        return path;
    }
    private String extension(String filename) {
        int dot=filename.lastIndexOf('.'); if(dot<0||dot==filename.length()-1) throw new IllegalArgumentException("Unsupported file type");
        String ext=filename.substring(dot+1).toLowerCase(Locale.ROOT);
        if(!java.util.Set.of("pdf","txt","png","jpg","jpeg","doc","docx","zip").contains(ext)) throw new IllegalArgumentException("Allowed file types: PDF, TXT, PNG, JPG, DOC, DOCX, ZIP");
        return ext;
    }
    private void validateSignature(String ext,byte[] b) {
        boolean valid=switch(ext){
            case "pdf" -> starts(b,new byte[]{'%', 'P','D','F','-'});
            case "png" -> starts(b,new byte[]{(byte)0x89,'P','N','G',0x0d,0x0a,0x1a,0x0a});
            case "jpg","jpeg" -> b.length>=3&&(b[0]&255)==255&&(b[1]&255)==216&&(b[2]&255)==255;
            case "zip","docx" -> starts(b,new byte[]{'P','K',3,4});
            case "doc" -> starts(b,new byte[]{(byte)0xD0,(byte)0xCF,0x11,(byte)0xE0,(byte)0xA1,(byte)0xB1,0x1A,(byte)0xE1});
            case "txt" -> validText(b);
            default -> false;
        };
        if(!valid) throw new IllegalArgumentException("File content does not match its extension");
    }
    private boolean starts(byte[] actual,byte[] expected){if(actual.length<expected.length)return false;for(int i=0;i<expected.length;i++)if(actual[i]!=expected[i])return false;return true;}
    private boolean validText(byte[] bytes){
        for(byte b:bytes)if(b==0)return false;
        try {
            StandardCharsets.UTF_8.newDecoder().decode(java.nio.ByteBuffer.wrap(bytes));
            return true;
        } catch (java.nio.charset.CharacterCodingException exception) {
            return false;
        }
    }
}
