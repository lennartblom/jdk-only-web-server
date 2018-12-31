package de.blom.httpwebserver.adapter.inbound.http.commons;

import de.blom.httpwebserver.exception.DataNotModifiedException;
import de.blom.httpwebserver.exception.ETagException;
import de.blom.httpwebserver.representation.fileserver.CacheableData;

import java.util.Date;

public class CacheValidator {

    public static void validateCache(HttpRequest.CacheHeaders cacheHeaders, CacheableData cacheableData){
        if(cacheHeaders != null){
            validateModifiedSince(cacheHeaders.getIfModifiedSince(), cacheableData.getLastModified());
            validateETag(cacheHeaders, cacheableData.getETag());
        }
    }

    private static void validateModifiedSince(Date incomingModifiedSince, Date dataModifiedOn){

        if(incomingModifiedSince != null && dataModifiedOn != null){
            if(dataModifiedOn.after(incomingModifiedSince)){
                throw new DataNotModifiedException();
            }
        }
    }

    private static void validateETag(HttpRequest.CacheHeaders cacheHeaders, String dataETag){
        String ifMatch = cacheHeaders.getIfMatch();
        String ifNoneMatch = cacheHeaders.getIfNonMatch();

        if(dataETag != null){
            if(ifMatch != null){
                if(!ifMatch.equals(dataETag)){
                    throw new ETagException();
                }
            }else if(ifNoneMatch != null){
                if(ifNoneMatch.equals(dataETag)){
                    throw new ETagException();
                }
            }
        }

    }

}
