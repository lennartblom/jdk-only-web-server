package de.blom.httpwebserver.adapter.inbound.http.commons;

import de.blom.httpwebserver.crosscutting.exception.DataNotModifiedException;
import de.blom.httpwebserver.crosscutting.exception.ETagException;
import de.blom.httpwebserver.crosscutting.representation.fileserver.CacheableData;
import java.util.Date;

public class CacheValidator {

  public static void validateCache(final HttpRequest.CacheHeaders cacheHeaders,
      final CacheableData cacheableData) {
    if (cacheHeaders != null) {
      validateModifiedSince(cacheHeaders.getIfModifiedSince(), cacheableData.getLastModified());
      validateETag(cacheHeaders, cacheableData.getETag());
    }
  }

  private static void validateModifiedSince(final Date incomingModifiedSince,
      final Date dataModifiedOn) {

    if (incomingModifiedSince != null && dataModifiedOn != null) {
      if (dataModifiedOn.after(incomingModifiedSince)) {
        throw new DataNotModifiedException();
      }
    }
  }

  private static void validateETag(final HttpRequest.CacheHeaders cacheHeaders,
      final String dataETag) {
    final String ifMatch = cacheHeaders.getIfMatch();
    final String ifNoneMatch = cacheHeaders.getIfNonMatch();

    if (dataETag != null) {
      if (ifMatch != null) {
        if (!ifMatch.equals(dataETag)) {
          throw new ETagException();
        }
      } else if (ifNoneMatch != null) {
        if (ifNoneMatch.equals(dataETag)) {
          throw new ETagException();
        }
      }
    }

  }

}
