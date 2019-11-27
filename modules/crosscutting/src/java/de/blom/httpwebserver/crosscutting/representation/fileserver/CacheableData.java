package de.blom.httpwebserver.crosscutting.representation.fileserver;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class CacheableData {
    Date lastModified;
    String eTag;
}

