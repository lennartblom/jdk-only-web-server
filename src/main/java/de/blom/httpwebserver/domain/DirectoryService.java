package de.blom.httpwebserver.domain;

import de.blom.httpwebserver.adapter.outbound.FileSystem;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class DirectoryService {
    private static final String CONTENT_TYPE_TEXT_HTML = "text/html";
    private static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain";
    private static final String WEB_ROOT_DIR = "./dir";
    private static final Logger log = Logger.getLogger(DirectoryService.class.getName());

    private FileSystem filesystem;

    public DirectoryService() {
        this.filesystem = new FileSystem(WEB_ROOT_DIR);
    }

    public FileRequestDto handleFileRequest(String retrieveFile) {
        File file = this.filesystem.retrieveFile(retrieveFile);
        if (file != null) {
            String contentType = this.getContentType(retrieveFile);
            int fileLength = (int) file.length();

            try {
                byte[] fileContent = this.filesystem.readFileData(file, fileLength);
                return FileRequestDto.builder()
                        .found(true)
                        .contentType(contentType)
                        .fileLength(fileLength)
                        .fileContent(fileContent)
                        .build();

            } catch (IOException ioexception) {
                log.log(Level.SEVERE, "Error while retrieving file content", ioexception);
                return new FileRequestDto(false);
            }
        } else {
            return new FileRequestDto(false);
        }
    }

    String getContentType(String fileRequested) {
        if (fileRequested.endsWith(".htm") || fileRequested.endsWith(".html"))
            return CONTENT_TYPE_TEXT_HTML;
        else
            return CONTENT_TYPE_TEXT_PLAIN;
    }
}
