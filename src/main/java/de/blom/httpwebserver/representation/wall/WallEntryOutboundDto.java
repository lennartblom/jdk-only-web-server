package de.blom.httpwebserver.representation.wall;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WallEntryOutboundDto {
    private String author;
    private String text;
    private Date created;

}
