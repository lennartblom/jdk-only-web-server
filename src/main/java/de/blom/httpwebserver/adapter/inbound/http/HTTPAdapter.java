package de.blom.httpwebserver.adapter.inbound.http;

import de.blom.httpwebserver.adapter.inbound.http.util.ResponseWriter;
import de.blom.httpwebserver.domain.DirectoryRequestDto;
import de.blom.httpwebserver.domain.DirectoryService;
import de.blom.httpwebserver.domain.FileRequestDto;
import de.blom.httpwebserver.enums.HTTPMethod;
import org.apache.commons.httpclient.HttpStatus;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HTTPAdapter implements Runnable {

    private static final Logger log = Logger.getLogger(HTTPAdapter.class.getName());

    private static final String DIR = "./dir";
    private static final File WEB_ROOT = new File(DIR);
    private static final String FILE_NOT_FOUND = "404.html";
    private static final String METHOD_NOT_SUPPORTED = "not_supported.html";
    private static final int PORT = 8080;
    private static final boolean VERBOSE = true;
    private static final String CONTENT_TYPE_TEXT_HTML = "text/html";
    private static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain";

    private ResponseWriter responseWriter;
    private Socket connect;
    private DirectoryService directoryService;

    HTTPAdapter(Socket c) {
        this.responseWriter = new ResponseWriter();
        this.connect = c;
        this.directoryService = new DirectoryService();
    }

    HTTPAdapter(Socket c, ResponseWriter responseWriter) {
        this(c);
        this.responseWriter = responseWriter;
    }


    public static void main(String[] args) {
        try {
            ServerSocket serverConnect = new ServerSocket(PORT);
            log.info("Server started.\nListening for connections on port : " + PORT + " ...\n");

            while (true) {
                HTTPAdapter myServer = new HTTPAdapter(serverConnect.accept());

                if (VERBOSE) {
                    log.info("Connection opened. (" + new Date() + ")");
                }

                // create dedicated thread to manage the client connection
                Thread thread = new Thread(myServer);
                thread.start();
            }

        } catch (IOException e) {
            log.log(Level.SEVERE, "Server Connection error : " + e.getMessage(), e);
        }
    }

    public void run() {

        /*

        Copied from https://medium.com/@ssaurel/create-a-simple-http-web-server-in-java-3fc12b29d5fd

        BEGIN

         */

        // we manage our particular client connection
        BufferedReader in = null;
        PrintWriter out = null;
        BufferedOutputStream dataOut = null;
        String uri = null;

        try {
            // we read characters from the client via input stream on the socket
            in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
            // we get character output stream to client (for headers)
            out = new PrintWriter(connect.getOutputStream());
            // get binary output stream to client (for requested data)
            dataOut = new BufferedOutputStream(connect.getOutputStream());

            // get first line of the request from the client
            String input = in.readLine();
            // we parse the request with a string tokenizer
            if (input != null) {

                StringTokenizer parse = new StringTokenizer(input);
                String method = parse.nextToken().toUpperCase(); // we get the HTTP method of the client
                // we get file requested
                uri = parse.nextToken().toLowerCase();

            /*

            Copied from https://medium.com/@ssaurel/create-a-simple-http-web-server-in-java-3fc12b29d5fd

            END

             */

                HTTPMethod httpMethod = this.identifyHTTPMethod(method);
                if (httpMethod == null) {
                    this.responseWriter.respondeWith501(out, dataOut);

                } else {
                    switch (httpMethod) {
                        case HEAD:
                        case GET:
                            if (uri.endsWith("/")) {

                                DirectoryRequestDto directoryRequestDto = this.directoryService.handleDirectoryRequest(uri);
                                if (!directoryRequestDto.getFound()) {
                                    this.responseWriter.respondeWith404(out, dataOut);
                                } else {
                                    this.handleDirectoryRequest(directoryRequestDto, out, dataOut);
                                }
                            } else {
                                FileRequestDto response = this.directoryService.handleFileRequest(uri);

                                if (!response.getFound()) {
                                    this.responseWriter.respondeWith404(out, dataOut);
                                } else {
                                    this.responseWriter.writeHttpResponse(out, dataOut, response.getFileLength(), response.getContentType(), response.getFileContent(), HttpStatus.SC_OK);
                                }

                            }
                            break;
                        default:
                            this.responseWriter.respondeWith501(out, dataOut);
                            break;
                    }
                }


            }

        } catch (IOException ioe) {
            log.log(Level.SEVERE, "Server error : " + ioe.getMessage(), ioe);
        } finally {
            try {
                this.closeElements(in, out, dataOut);
            } catch (Exception e) {
                log.log(Level.SEVERE, "Error closing stream : " + e.getMessage(), e);
            }

            if (VERBOSE) {
                log.info("Connection closed.\n");
            }
        }


    }

    String handleHTTPMethod(HTTPMethod method) {
        return null;
    }

    HTTPMethod identifyHTTPMethod(String method) {
        method = method.toUpperCase();
        try {
            return HTTPMethod.valueOf(method);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    StringTokenizer parseInput(BufferedReader in) throws IOException {
        String input = in.readLine();
        if (input != null) {
            return new StringTokenizer(input);
        } else {
            throw new IOException();
        }

    }

    void handleDirectoryRequest(DirectoryRequestDto directoryInformation, PrintWriter out, BufferedOutputStream dataOut) throws IOException {
        log.info("Directory requested");
        this.responseWriter.writeHttpResponse(directoryInformation, out, dataOut);
    }


    void closeElements(BufferedReader in, PrintWriter out, BufferedOutputStream dataOut) throws IOException {
        in.close();
        out.close();
        dataOut.close();
        connect.close();
    }

}