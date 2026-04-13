package com.pesocial.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.pesocial.exception.EntityNotFoundException;

@RestController
@RequestMapping("/api/media")
@CrossOrigin("*")
public class MediaController {

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadMedia(Authentication auth, @RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "File is empty or missing");
                return ResponseEntity.status(400).body(error);
            }

            String userId = auth != null ? auth.getName() : "anonymous";
            Map<String, String> metadata = new HashMap<>();
            metadata.put("uploadedBy", userId);
            metadata.put("originalFilename", file.getOriginalFilename());
            metadata.put("contentType", file.getContentType());

            ObjectId fileId = gridFsTemplate.store(file.getInputStream(), file.getOriginalFilename(),
                file.getContentType(), metadata);

            Map<String, String> response = new HashMap<>();
            response.put("mediaUrl", "/api/media/" + fileId.toString());
            response.put("id", fileId.toString());
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "IO Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<Resource> getMedia(@PathVariable String fileId) throws IOException {
        Query query = new Query(Criteria.where("_id").is(toObjectIdOrString(fileId)));
        GridFSFile file = gridFsTemplate.findOne(query);
        if (file == null) {
            throw new EntityNotFoundException("Media not found");
        }

        GridFsResource resource = gridFsTemplate.getResource(file);
        String contentType = file.getMetadata() != null ? file.getMetadata().getString("contentType") : null;
        if (contentType == null || contentType.isBlank()) {
            contentType = file.getMetadata() != null ? file.getMetadata().getString("_contentType") : null;
        }
        if (contentType == null || contentType.isBlank()) {
            contentType = MediaType.IMAGE_JPEG_VALUE;
        }

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .body(new InputStreamResource(resource.getInputStream()));
    }

    private Object toObjectIdOrString(String fileId) {
        if (ObjectId.isValid(fileId)) {
            return new ObjectId(fileId);
        }
        return fileId;
    }
}
