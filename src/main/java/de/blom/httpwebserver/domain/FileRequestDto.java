package de.blom.httpwebserver.domain;

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

    FileRequestDto(Boolean found){
        this.found = found;

    }
}
