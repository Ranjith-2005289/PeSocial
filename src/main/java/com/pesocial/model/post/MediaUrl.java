package com.pesocial.model.post;

import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MediaUrl {

    @Field("storage_type")
    private MediaStorageType storageType;

    @Field("grid_fs_file_id")
    private String gridFsFileId;

    @Field("external_url")
    private String externalUrl;

    @Field("content_type")
    private String contentType;

    @Field("size_bytes")
    private Long sizeBytes;

    public static MediaUrl gridFs(String gridFsFileId, String contentType, Long sizeBytes) {
        return new MediaUrl(MediaStorageType.GRID_FS, gridFsFileId, null, contentType, sizeBytes);
    }

    public static MediaUrl external(String externalUrl, String contentType, Long sizeBytes) {
        return new MediaUrl(MediaStorageType.EXTERNAL_URL, null, externalUrl, contentType, sizeBytes);
    }
}
