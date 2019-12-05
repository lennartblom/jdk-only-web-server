package de.blom.httpwebserver.fileserver;

import de.blom.httpserver.crosscutting.representation.fileserver.DirectoryRequestDto;
import de.blom.httpserver.crosscutting.representation.fileserver.FileRequestDto;
import de.blom.persistence.FileSystem;
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

  public DirectoryService(final String directoryParam) {
    if (directoryParam != null) {
      log.info("Initializing DirectoryService with root fileserver='" + directoryParam + "'");
      this.filesystem = new FileSystem(directoryParam);
    } else {
      log.info("Initializing DirectoryService with root fileserver='" + WEB_ROOT_DIR + "'");
      this.filesystem = new FileSystem(WEB_ROOT_DIR);
    }

  }

  public DirectoryService(final FileSystem fileSystem, final String directoryParam) {
    this(directoryParam);
    this.filesystem = fileSystem;
  }

  public DirectoryService() {
    this(null);
  }

  public DirectoryRequestDto handleDirectoryRequest(final String directoryPath) {
    final File directory = this.filesystem.retrieveFile(directoryPath);

    if (directory == null) {
      return new DirectoryRequestDto();
    }
    final Date lastModified = new Date(directory.lastModified());
    final File[] directoryElements = directory.listFiles();

    if (directoryElements == null) {
      final DirectoryRequestDto dto = DirectoryRequestDto.builder()
          .lastModified(lastModified)
          .found(true)
          .build();

      dto.setETag(DirectoryRequestDto.generateETag(dto));
      return dto;
    } else {

      final List<String> files = new ArrayList<>();
      final List<String> subdirectories = new ArrayList<>();

      for (final File file : directoryElements) {
        if (file.listFiles() != null) {
          subdirectories.add(file.getName());
        } else {
          files.add(file.getName());
        }
      }
      final DirectoryRequestDto dto = DirectoryRequestDto.builder()
          .found(true)
          .files(files)
          .subdirectories(subdirectories)
          .lastModified(lastModified)
          .build();
      dto.setETag(DirectoryRequestDto.generateETag(dto));

      return dto;
    }
  }

  public FileRequestDto handleFileRequest(final String retrieveFile) {
    final File file = this.filesystem.retrieveFile(retrieveFile);
    if (file != null) {
      final String contentType = DirectoryService.getContentType(retrieveFile);
      final int fileLength = (int) file.length();
      final Date lastModified = new Date(file.lastModified());

      try {
        final byte[] fileContent = this.filesystem.readFileData(file, fileLength);
        final FileRequestDto dto = FileRequestDto.builder()
            .found(true)
            .contentType(contentType)
            .fileLength(fileLength)
            .fileContent(fileContent)
            .lastModified(lastModified)
            .build();

        dto.setETag(FileRequestDto.generateETag(dto));
        return dto;

      } catch (final IOException ioexception) {
        log.log(Level.SEVERE, "Error while retrieving file content", ioexception);
        return new FileRequestDto(false);
      }
    } else {
      return new FileRequestDto(false);
    }
  }

  static String getContentType(final String fileRequested) {
    if (fileRequested.endsWith(".htm") || fileRequested.endsWith(".html")) {
      return CONTENT_TYPE_TEXT_HTML;
    } else {
      return CONTENT_TYPE_TEXT_PLAIN;
    }
  }
}
