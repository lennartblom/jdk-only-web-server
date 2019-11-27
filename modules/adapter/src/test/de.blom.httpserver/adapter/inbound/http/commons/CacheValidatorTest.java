package de.blom.httpwebserver.adapter.inbound.http.commons;

import de.blom.httpwebserver.crosscutting.exception.DataNotModifiedException;
import de.blom.httpwebserver.crosscutting.exception.ETagException;
import de.blom.httpwebserver.crosscutting.representation.fileserver.FileRequestDto;
import org.junit.Test;

import java.util.Date;

public class CacheValidatorTest {


    private static final long DATE_TIMESTAMP = 1546194487000L;
    private static final String MOCKED_ETAG_1 = "bfc13a64729c4290ef5b2c2730249c88ca92d82d";
    private static final String MOCKED_ETAG_2 = "0815";

    @Test(expected = DataNotModifiedException.class)
    public void expectToThrowDataModifiedException() {
        Date expectedDate = new Date(DATE_TIMESTAMP);

        HttpRequest.CacheHeaders expectedCacheHeaders = HttpRequest.CacheHeaders.builder()
                .ifModifiedSince(expectedDate)
                .build();
        FileRequestDto data = FileRequestDto.builder()
                .eTag("")
                .lastModified(new Date(DATE_TIMESTAMP + 500L))
                .build();


        CacheValidator.validateCache(expectedCacheHeaders, data);
    }

    @Test(expected = ETagException.class)
    public void expectToThrowETagException_IfMatch() {

        HttpRequest.CacheHeaders expectedCacheHeaders = HttpRequest.CacheHeaders.builder()
                .ifMatch(MOCKED_ETAG_1)
                .build();
        FileRequestDto data = FileRequestDto.builder()
                .eTag(MOCKED_ETAG_2)
                .build();


        CacheValidator.validateCache(expectedCacheHeaders, data);
    }

    @Test(expected = ETagException.class)
    public void expectToThrowETagException_IfNoneMatch() {

        HttpRequest.CacheHeaders expectedCacheHeaders = HttpRequest.CacheHeaders.builder()
                .ifNonMatch(MOCKED_ETAG_1)
                .build();
        FileRequestDto data = FileRequestDto.builder()
                .eTag(MOCKED_ETAG_1)
                .build();


        CacheValidator.validateCache(expectedCacheHeaders, data);
    }

    @Test
    public void expectToThrowNothingIfNoHeadersExist(){
        FileRequestDto data = FileRequestDto.builder()
                .eTag(MOCKED_ETAG_1)
                .build();

        CacheValidator.validateCache(null, data);
    }

}
