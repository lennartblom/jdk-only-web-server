package de.blom.httpwebserver.domain;

import de.blom.httpwebserver.adapter.outbound.FileSystem;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class DirectoryServiceTest {

    private static final String FILE_A = "A.html";
    private static final String FILE_B = "B.html";
    private static final String FILE_C = "C.html";
    private static final String SUBDIR_1 = "subdir1";
    private static final String SUBDIR_2 = "subdir2";
    private static final String DIRECTORY_PATH = "/";
    private static final String EXAMPLE_FILE = "index.html";

    @Mock
    private FileSystem fileSystem;

    @InjectMocks
    private DirectoryService directoryService;

    private DirectoryRequestDto expectedDirectoryInformation = DirectoryRequestDto.builder()
            .files(Arrays.asList(FILE_A, FILE_B, FILE_C))
            .subdirectories(Arrays.asList(SUBDIR_1, SUBDIR_2))
            .found(true)
            .build();

    private static final DirectoryRequestDto EXPECTED_EMPTY_DTO = new DirectoryRequestDto();


    @Test
    public void expectToReturnDirectoriesAndFiles() {
        File mockedDirectory = Mockito.mock(File.class);
        File mockedFile1 = Mockito.mock(File.class);
        File mockedFile2 = Mockito.mock(File.class);
        File mockedFile3 = Mockito.mock(File.class);
        File mockedSubDirectory1 = Mockito.mock(File.class);
        File mockedSubDirectory2 = Mockito.mock(File.class);

        File mockedSubdirectoryFile1 = Mockito.mock(File.class);
        File mockedSubdirectoryFile2 = Mockito.mock(File.class);

        File[] subdirectory1 = new File[1];
        File[] subdirectory2 = new File[1];
        subdirectory1[0] = mockedSubdirectoryFile1;
        subdirectory2[0] = mockedSubdirectoryFile2;
        Mockito.when(mockedSubDirectory1.listFiles()).thenReturn(subdirectory1);
        Mockito.when(mockedSubDirectory2.listFiles()).thenReturn(subdirectory2);

        Mockito.when(mockedFile1.getName()).thenReturn(FILE_A);
        Mockito.when(mockedFile2.getName()).thenReturn(FILE_B);
        Mockito.when(mockedFile3.getName()).thenReturn(FILE_C);
        Mockito.when(mockedSubDirectory1.getName()).thenReturn(SUBDIR_1);
        Mockito.when(mockedSubDirectory2.getName()).thenReturn(SUBDIR_2);


        File[] files = new File[5];
        files[0] = mockedFile1;
        files[1] = mockedFile2;
        files[2] = mockedFile3;
        files[3] = mockedSubDirectory1;
        files[4] = mockedSubDirectory2;

        Mockito.when(mockedDirectory.listFiles()).thenReturn(files);
        Mockito.when(this.fileSystem.retrieveFile(DIRECTORY_PATH)).thenReturn(mockedDirectory);

        DirectoryRequestDto returnedDirectoryInformation = this.directoryService.handleDirectoryRequest(DIRECTORY_PATH);

        assertThat(returnedDirectoryInformation, Matchers.samePropertyValuesAs(expectedDirectoryInformation));


    }

    @Test
    public void expectToReturnEmptyDtoIfDirectoryIsEmpty() {
        File mockedDirectory = Mockito.mock(File.class);
        Mockito.when(mockedDirectory.listFiles()).thenReturn(null);
        Mockito.when(this.fileSystem.retrieveFile(DIRECTORY_PATH)).thenReturn(mockedDirectory);


        DirectoryRequestDto returnedDirectoryRequestDto = this.directoryService.handleDirectoryRequest(DIRECTORY_PATH);

        assertThat(returnedDirectoryRequestDto, Matchers.samePropertyValuesAs(EXPECTED_EMPTY_DTO));

    }

    @Test
    public void expectToReturnEmptyDtoIfNullIsReturned() {
        Mockito.when(this.fileSystem.retrieveFile(DIRECTORY_PATH)).thenReturn(null);

        DirectoryRequestDto returnedDirectoryRequestDto = this.directoryService.handleDirectoryRequest(DIRECTORY_PATH);

        assertThat(returnedDirectoryRequestDto, Matchers.samePropertyValuesAs(EXPECTED_EMPTY_DTO));

    }
}
