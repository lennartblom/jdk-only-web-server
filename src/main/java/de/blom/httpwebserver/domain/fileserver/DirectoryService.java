package de.blom.httpwebserver.domain.fileserver;

import de.blom.httpwebserver.adapter.outbound.FileSystem;
import de.blom.httpwebserver.representation.fileserver.DirectoryRequestDto;
import de.blom.httpwebserver.representation.fileserver.FileRequestDto;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DirectoryService {
    private static final String CONTENT_TYPE_TEXT_HTML = "text/html";
    private static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain";
    private static final String WEB_ROOT_DIR = "./dir";
    private static final Logger log = Logger.getLogger(DirectoryService.class.getName());

    private FileSystem filesystem;

    public DirectoryService(String directoryParam) {
        if (directoryParam != null) {
            log.info("Initializing DirectoryService with root fileserver='" + directoryParam + "'");
            this.filesystem = new FileSystem(directoryParam);
        } else {
            log.info("Initializing DirectoryService with root fileserver='" + WEB_ROOT_DIR + "'");
            this.filesystem = new FileSystem(WEB_ROOT_DIR);
        }

    }

    public DirectoryService(FileSystem fileSystem, String directoryParam){
        this(directoryParam);
        this.filesystem = fileSystem;
    }

    public DirectoryService() {
        this(null);
    }

    public DirectoryRequestDto handleDirectoryRequest(String directoryPath) {
        File directory = this.filesystem.retrieveFile(directoryPath);

        if (directory == null) {
            return new DirectoryRequestDto();
        }
        Date lastModified = new Date(directory.lastModified());
        File[] directoryElements = directory.listFiles();

        if (directoryElements == null) {
            DirectoryRequestDto dto = DirectoryRequestDto.builder()
                    .lastModified(lastModified)
                    .found(true)
                    .build();

            dto.setETag(DirectoryRequestDto.generateETag(dto));
            return dto;
        } else {
            File[] containedFiles = directoryElements;

            List<String> files = new ArrayList<>();
            List<String> subdirectories = new ArrayList<>();


            for (File file : containedFiles) {
                if (file.listFiles() != null) {
                    subdirectories.add(file.getName());
                } else {
                    files.add(file.getName());
                }
            }
            DirectoryRequestDto dto = DirectoryRequestDto.builder()
                    .found(true)
                    .files(files)
                    .subdirectories(subdirectories)
                    .lastModified(lastModified)
                    .build();
            dto.setETag(DirectoryRequestDto.generateETag(dto));

            return dto;
        }
    }

    public FileRequestDto handleFileRequest(String retrieveFile) {
        File file = this.filesystem.retrieveFile(retrieveFile);
        if (file != null) {
            String contentType = this.getContentType(retrieveFile);
            int fileLength = (int) file.length();
            Date lastModified = new Date(file.lastModified());

            try {
                byte[] fileContent = this.filesystem.readFileData(file, fileLength);
                FileRequestDto dto = FileRequestDto.builder()
                        .found(true)
                        .contentType(contentType)
                        .fileLength(fileLength)
                        .fileContent(fileContent)
                        .lastModified(lastModified)
                        .build();

                dto.setETag(FileRequestDto.generateETag(dto));
                return dto;

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
