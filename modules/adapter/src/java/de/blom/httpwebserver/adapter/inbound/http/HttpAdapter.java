package de.blom.httpwebserver.adapter.inbound.http;

import de.blom.httpwebserver.adapter.inbound.http.commons.CacheValidator;
import de.blom.httpwebserver.adapter.inbound.http.commons.HttpRequest;
import de.blom.httpwebserver.adapter.inbound.http.commons.ResponseWriter;
import de.blom.httpwebserver.crosscutting.enums.HttpMethod;
import de.blom.httpwebserver.crosscutting.exception.DataNotModifiedException;
import de.blom.httpwebserver.crosscutting.exception.ETagException;
import de.blom.httpwebserver.crosscutting.exception.InvalidDataException;
import de.blom.httpwebserver.crosscutting.exception.NotFoundException;
import de.blom.persistence.ServiceNotAvailableException;
import de.blom.httpwebserver.crosscutting.exception.WrongContentTypeException;
import de.blom.httpwebserver.crosscutting.representation.fileserver.CacheableData;
import de.blom.httpwebserver.crosscutting.representation.fileserver.DirectoryRequestDto;
import de.blom.httpwebserver.crosscutting.representation.fileserver.FileRequestDto;
import de.blom.httpwebserver.crosscutting.representation.wall.WallEntryInboundDto;
import de.blom.httpwebserver.crosscutting.representation.wall.WallEntryOutboundDto;
import de.blom.httpwebserver.fileserver.DirectoryService;
import de.blom.httpwebserver.fileserver.wall.WallContentService;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpAdapter implements Runnable {
  private static final Logger log = Logger.getLogger(HttpAdapter.class.getName());

  private static final int PORT = 8080;

  private ResponseWriter responseWriter;
  private final Socket connection;
  private DirectoryService directoryService;
  private WallContentService wallContentService;

  private HttpAdapter(final Socket c, final String directoryParam) {
    this.wallContentService = new WallContentService();
    this.responseWriter = new ResponseWriter();
    this.connection = c;
    this.directoryService = new DirectoryService(directoryParam);
  }

  private HttpAdapter(final Socket c) {
    this.responseWriter = new ResponseWriter();
    this.connection = c;
    this.directoryService = new DirectoryService();
  }

  private HttpAdapter(final Socket c, final ResponseWriter responseWriter) {
    this(c);
    this.responseWriter = responseWriter;
  }

  HttpAdapter(final Socket c, final ResponseWriter responseWriter,
      final DirectoryService directoryService,
      final WallContentService wallContentService) {
    this(c, responseWriter);
    this.directoryService = directoryService;
    this.wallContentService = wallContentService;
  }

  public static void main(final String[] args) {
    try (final ServerSocket serverConnect = new ServerSocket(PORT)) {

      log.info(
          "HTTP Adapter started.\nWaiting for incoming connections on port '" + PORT + "' ...\n");

      String directoryParam = null;
      if (args.length == 1) {
        directoryParam = args[0];
      }

      while (true) {
        final HttpAdapter httpAdapter = new HttpAdapter(serverConnect.accept(), directoryParam);
        final Thread thread = new Thread(httpAdapter);

        thread.start();
      }
    } catch (final IOException e) {
      log.log(Level.SEVERE, "A Server Connection error occured: " + e.getMessage(), e);
    }
  }

  public void run() {
    try (final BufferedReader in = new BufferedReader(
        new InputStreamReader(this.connection.getInputStream()));
        final PrintWriter httpResponseHead = new PrintWriter(this.connection.getOutputStream());
        final BufferedOutputStream httpResponseBody = new BufferedOutputStream(
            this.connection.getOutputStream())
    ) {
      boolean keepAlive = true;

      while (keepAlive) {
        keepAlive = false;

        final HttpRequest httpRequest = HttpRequest.parseFrom(in);
        if (httpRequest != null) {
          this.processRequest(httpResponseHead, httpResponseBody, httpRequest);

          keepAlive = httpRequest.keepConnectionAlive();
          if (keepAlive) {
            log.info("Connection will stay alive");
          }
        }
      }

    } catch (final IOException ioe) {
      log.log(Level.SEVERE, "Server error : " + ioe.getMessage(), ioe);
    } finally {

      log.info("Connection closed.\n");
    }

  }

  void processRequest(final PrintWriter httpResponseHead,
      final BufferedOutputStream httpResponseBody,
      final HttpRequest httpRequest) throws IOException {
    switch (httpRequest.getMethod()) {
      case POST:
        try {
          this.handlePostRequest(httpRequest, httpResponseHead, httpResponseBody);

        } catch (final InvalidDataException | WrongContentTypeException e) {
          log.info("Client sent data, which can not be handled.");
          this.responseWriter.respondeWith400(httpResponseHead, httpResponseBody);

        } catch (final NotFoundException e) {
          log.info("Provided URI can't be found for POST request.");
          this.responseWriter.respondeWith404(httpResponseHead, httpResponseBody);

        } catch (final ServiceNotAvailableException e) {
          log.info("Service, which handles data is not available right now.");
          this.responseWriter.respondeWith503(httpResponseHead, httpResponseBody);

        }
        break;

      case HEAD:
      case GET:
        try {
          this.handleDirectoryServerRequest(httpRequest, httpResponseHead, httpResponseBody);
        } catch (final DataNotModifiedException | ETagException e) {
          log.info("Cache is valid. No need to send data");
          this.responseWriter.respondeWith304(httpResponseHead);
        }

        break;

      default:
        this.responseWriter.respondeWith501(httpResponseHead, httpResponseBody);
        break;
    }
  }

  void handleDirectoryServerRequest(final HttpRequest httpRequest,
      final PrintWriter httpResponseHead,
      final BufferedOutputStream httpResponseBody) throws IOException {
    final String uri = httpRequest.getUri();

    if (uri.endsWith("/")) {
      this.handleDirectoryRequest(httpRequest, httpResponseHead, httpResponseBody);
    } else {
      this.handleFileRequest(httpRequest, httpResponseHead, httpResponseBody);
    }
  }

  void handlePostRequest(final HttpRequest httpRequest, final PrintWriter httpResponseHead,
      final BufferedOutputStream httpResponseBody) throws IOException {
    log.info("HTTP Request uri='" + httpRequest + "'");

    switch (httpRequest.getUri()) {
      case "/wall_entries":
      case "/wall_entries/":
        log.info("Handle WallEntry creation");
        if (!httpRequest.isContentTypeApplicationJson()) {
          throw new WrongContentTypeException();
        }
        final WallEntryInboundDto dto =
            WallEntryInboundDto.parseFromRawJson(httpRequest.getRawBody());

        this.wallContentService.createNewEntry(dto);
        this.responseWriter.respondeWith201(httpResponseHead, httpResponseBody);
        break;

      case "/wall_entries/query":
      case "/wall_entries/query/":
        log.info("Handle WallEntry retrievement");

        final List<WallEntryOutboundDto> wallEntries = this.wallContentService.getAllEntries();
        this.responseWriter.writeHttpResponse(wallEntries, httpResponseHead, httpResponseBody);

        break;

      default:
        throw new NotFoundException();
    }
  }

  void handleFileRequest(final HttpRequest httpRequest, final PrintWriter httpResponseHead,
      final BufferedOutputStream httpResponseBody) throws IOException {
    final FileRequestDto fileRequestDto =
        this.directoryService.handleFileRequest(httpRequest.getUri());

    if (!fileRequestDto.getFound()) {
      if (httpRequest.getMethod() == HttpMethod.HEAD) {
        this.responseWriter.respondeWith404(httpResponseHead, null);
      } else {
        this.responseWriter.respondeWith404(httpResponseHead, httpResponseBody);
      }
    } else {
      HttpAdapter.validateCache(httpRequest.getCacheHeaders(), fileRequestDto);

      if (httpRequest.getMethod() == HttpMethod.HEAD) {
        this.responseWriter.writeHttpResponseWithFileData(fileRequestDto, httpResponseHead, null);
      } else {
        this.responseWriter
            .writeHttpResponseWithFileData(fileRequestDto, httpResponseHead, httpResponseBody);

      }
    }
  }

  void handleDirectoryRequest(final HttpRequest httpRequest, final PrintWriter httpResponseHead,
      final BufferedOutputStream httpResponseBody) throws IOException {
    final DirectoryRequestDto directoryRequestDto =
        this.directoryService.handleDirectoryRequest(httpRequest.getUri());

    if (!directoryRequestDto.getFound()) {
      if (httpRequest.getMethod() == HttpMethod.HEAD) {
        this.responseWriter.respondeWith404(httpResponseHead, null);
      } else {
        this.responseWriter.respondeWith404(httpResponseHead, httpResponseBody);
      }

    } else {
      HttpAdapter.validateCache(httpRequest.getCacheHeaders(), directoryRequestDto);

      if (httpRequest.getMethod() == HttpMethod.HEAD) {
        this.responseWriter
            .writeHttpResponseWithDirectoryData(directoryRequestDto, httpResponseHead, null);
      } else {
        this.responseWriter
            .writeHttpResponseWithDirectoryData(directoryRequestDto, httpResponseHead,
                httpResponseBody);
      }

    }
  }

  static void validateCache(final HttpRequest.CacheHeaders headers,
      final CacheableData cacheableData) {
    CacheValidator.validateCache(headers, cacheableData);
  }

}
