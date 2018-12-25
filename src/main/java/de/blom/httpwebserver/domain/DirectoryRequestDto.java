package de.blom.httpwebserver.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Builder
@AllArgsConstructor
@Getter
public class DirectoryRequestDto {
    private List<String> subdirectories;
    private List<String> files;

    public DirectoryRequestDto(){
        this.subdirectories = new ArrayList<>();
        this.files = new ArrayList<>();
    }
}
