package de.blom.httpwebserver.domain.wall;

import de.blom.httpwebserver.adapter.outbound.MongoDb;
import de.blom.httpwebserver.representation.wall.WallEntryInboundDto;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class WallContentService {

    private MongoDb mongoDb;

    public void createNewWallEntry(WallEntryInboundDto data){

    }

}
