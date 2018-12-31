package de.blom.httpwebserver.domain.fileserver;

import de.blom.httpwebserver.adapter.outbound.FileSystem;
import de.blom.httpwebserver.representation.fileserver.DirectoryRequestDto;
import de.blom.httpwebserver.representation.fileserver.FileRequestDto;
import org.apache.commons.codec.digest.DigestUtils;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DirectoryServiceTest {

    private static final String FILE_A = "A.html";
    private static final String FILE_B = "B.html";
    private static final String FILE_C = "C.html";
    private static final String SUBDIR_1 = "subdir1";
    private static final String SUBDIR_2 = "subdir2";
    private static final String DIRECTORY_PATH = "/";
    private static final String EXAMPLE_FILE = "index.html";
    private static final long MOCKED_TIMESTAMP = 1546194487000L;
    private static final int MOCKED_FILE_LENGTH = 123;
    private static final Date MOCKED_LAST_MODIFIED = new Date(MOCKED_TIMESTAMP);
    private static final DirectoryRequestDto EXPECTED_EMPTY_DTO = new DirectoryRequestDto();
    private static final byte[] MOCKED_FILE_DATA = new byte[MOCKED_FILE_LENGTH];


    @Mock
    private FileSystem fileSystem;


    @InjectMocks
    private DirectoryService directoryService;

    private File mockDirectoryInformation() {
        File mockedDirectory = mock(File.class);
        File mockedFile1 = mock(File.class);
        File mockedFile2 = mock(File.class);
        File mockedFile3 = mock(File.class);
        File mockedSubDirectory1 = mock(File.class);
        File mockedSubDirectory2 = mock(File.class);

        File mockedSubdirectoryFile1 = mock(File.class);
        File mockedSubdirectoryFile2 = mock(File.class);

        File[] subdirectory1 = new File[1];
        File[] subdirectory2 = new File[1];

        subdirectory1[0] = mockedSubdirectoryFile1;
        subdirectory2[0] = mockedSubdirectoryFile2;
        when(mockedSubDirectory1.listFiles()).thenReturn(subdirectory1);
        when(mockedSubDirectory2.listFiles()).thenReturn(subdirectory2);
        when(mockedDirectory.lastModified()).thenReturn(MOCKED_TIMESTAMP);

        when(mockedFile1.getName()).thenReturn(FILE_A);
        when(mockedFile2.getName()).thenReturn(FILE_B);
        when(mockedFile3.getName()).thenReturn(FILE_C);
        when(mockedSubDirectory1.getName()).thenReturn(SUBDIR_1);
        when(mockedSubDirectory2.getName()).thenReturn(SUBDIR_2);

        File[] files = new File[5];
        files[0] = mockedFile1;
        files[1] = mockedFile2;
        files[2] = mockedFile3;
        files[3] = mockedSubDirectory1;
        files[4] = mockedSubDirectory2;

        when(mockedDirectory.listFiles()).thenReturn(files);
        return mockedDirectory;
    }



    @Test
    public void expectToReturnDirectoriesAndFiles() {
        File mockedDirectory = mockDirectoryInformation();
        List files = Arrays.asList(FILE_A, FILE_B, FILE_C);
        List subdirs = Arrays.asList(SUBDIR_1, SUBDIR_2);

        DirectoryRequestDto expectedDirectoryInformation = DirectoryRequestDto.builder()
                .files(files)
                .subdirectories(subdirs)
                .lastModified(MOCKED_LAST_MODIFIED)
                .eTag(DigestUtils.md5Hex(files.toString() + subdirs.toString()))
                .found(true)
                .build();

        when(this.fileSystem.retrieveFile(DIRECTORY_PATH)).thenReturn(mockedDirectory);


        DirectoryRequestDto returnedDirectoryInformation = this.directoryService.handleDirectoryRequest(DIRECTORY_PATH);


        assertThat(returnedDirectoryInformation, Matchers.samePropertyValuesAs(expectedDirectoryInformation));
    }



    @Test
    public void expectToReturnEmptyDtoIfDirectoryIsEmpty() {
        File mockedDirectory = mock(File.class);
        when(mockedDirectory.lastModified()).thenReturn(MOCKED_TIMESTAMP);
        when(mockedDirectory.listFiles()).thenReturn(null);
        when(this.fileSystem.retrieveFile(DIRECTORY_PATH)).thenReturn(mockedDirectory);
        DirectoryRequestDto expectedDto = DirectoryRequestDto.builder().found(true)
                .lastModified(MOCKED_LAST_MODIFIED)
                .eTag(DigestUtils.md5Hex(Collections.EMPTY_LIST.toString() + Collections.EMPTY_LIST.toString()))
                .build();


        DirectoryRequestDto returnedDirectoryRequestDto = this.directoryService.handleDirectoryRequest(DIRECTORY_PATH);


        assertThat(returnedDirectoryRequestDto, Matchers.samePropertyValuesAs(expectedDto));

    }

    @Test
    public void expectToReturnEmptyDtoIfNullIsReturned() {
        when(this.fileSystem.retrieveFile(DIRECTORY_PATH)).thenReturn(null);
        DirectoryRequestDto expectedDto = DirectoryRequestDto.builder().found(false)
                .subdirectories(Collections.EMPTY_LIST)
                .files(Collections.EMPTY_LIST)
                .build();


        DirectoryRequestDto returnedDirectoryRequestDto = this.directoryService.handleDirectoryRequest(DIRECTORY_PATH);


        assertThat(returnedDirectoryRequestDto, Matchers.samePropertyValuesAs(expectedDto));
    }

    @Test
    public void expectToReturnFileDto() throws IOException {
        File mockedFile = mock(File.class);
        when(mockedFile.length()).thenReturn((long)MOCKED_FILE_LENGTH);
        when(mockedFile.lastModified()).thenReturn(MOCKED_TIMESTAMP);
        when(this.fileSystem.retrieveFile(EXAMPLE_FILE)).thenReturn(mockedFile);
        when(this.fileSystem.readFileData(mockedFile, MOCKED_FILE_LENGTH)).thenReturn(MOCKED_FILE_DATA);
        FileRequestDto expectedFileInformation = FileRequestDto.builder()
                .contentType("text/html")
                .fileContent(MOCKED_FILE_DATA)
                .fileLength(MOCKED_FILE_LENGTH)
                .lastModified(MOCKED_LAST_MODIFIED)
                .eTag(DigestUtils.md5Hex(Arrays.toString(MOCKED_FILE_DATA)))
                .found(true)
                .build();

        FileRequestDto returnedDto = this.directoryService.handleFileRequest(EXAMPLE_FILE);


        assertThat(returnedDto, Matchers.samePropertyValuesAs(expectedFileInformation));
    }
}
