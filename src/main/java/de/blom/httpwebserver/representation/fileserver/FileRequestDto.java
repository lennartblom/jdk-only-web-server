package de.blom.httpwebserver.representation.fileserver;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@Builder
public class FileRequestDto {
    Boolean found;
    String contentType;
    byte[] fileContent;
    int fileLength;

    public FileRequestDto(Boolean found){
        this.found = found;
    }
}
