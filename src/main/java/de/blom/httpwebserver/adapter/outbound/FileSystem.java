package de.blom.httpwebserver.adapter.outbound;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FileSystem {

    private File webRoot;

    public FileSystem(String directoryRoot){
        this.webRoot = new File(directoryRoot);
    }

    /**
     * @param file
     * @param fileLength
     * @return
     * @throws IOException
     * Copyright by Sylvain Saurel (https://medium.com/@ssaurel/create-a-simple-http-web-server-in-java-3fc12b29d5fd)
     */
    public byte[] readFileData(File file, int fileLength) throws IOException {
        FileInputStream fileIn = null;
        byte[] fileData = new byte[fileLength];

        try {
            fileIn = new FileInputStream(file);
            fileIn.read(fileData);
        } finally {
            if (fileIn != null)
                fileIn.close();
        }

        return fileData;
    }

    /**
     * @param fileRequested
     * @return
     * @throws IOException
     * Copyright by Sylvain Saurel (https://medium.com/@ssaurel/create-a-simple-http-web-server-in-java-3fc12b29d5fd)
     */
    public File retrieveFile(String fileRequested) {
        return new File(this.webRoot, fileRequested);
    }
}
