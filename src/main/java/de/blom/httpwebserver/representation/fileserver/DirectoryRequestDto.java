package de.blom.httpwebserver.representation.fileserver;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class DirectoryRequestDto extends CacheableData {
    private Boolean found;
    private List<String> subdirectories;
    private List<String> files;


    @Builder
    public DirectoryRequestDto(Date lastModified, String eTag, Boolean found, List<String> subdirectories, List<String> files) {
        super(lastModified, eTag);
        this.found = found;
        this.subdirectories = subdirectories;
        this.files = files;
    }

    public DirectoryRequestDto() {
        super();
        this.found = false;
        this.subdirectories = new ArrayList<>();
        this.files = new ArrayList<>();
    }

    public static String generateETag(DirectoryRequestDto dto) {

        List fileList = dto.files;
        List directoryList = dto.subdirectories;
        if(fileList == null){
            fileList = Collections.emptyList();
        }

        if(directoryList == null){
            directoryList = Collections.emptyList();
        }

        return DigestUtils.md5Hex(fileList.toString() + directoryList.toString());
    }
}
