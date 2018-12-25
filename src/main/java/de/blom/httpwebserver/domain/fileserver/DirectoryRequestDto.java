package de.blom.httpwebserver.domain.fileserver;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Builder
@AllArgsConstructor
@Getter
public class DirectoryRequestDto {
    private Boolean found;
    private List<String> subdirectories;
    private List<String> files;

    public DirectoryRequestDto(){
        this.found = false;
        this.subdirectories = new ArrayList<>();
        this.files = new ArrayList<>();
    }
}
