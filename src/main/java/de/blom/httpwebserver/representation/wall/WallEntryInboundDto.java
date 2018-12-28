package de.blom.httpwebserver.representation.wall;

import com.google.gson.Gson;
import de.blom.httpwebserver.exception.InvalidDataException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class WallEntryInboundDto {
    private String author;
    private String text;

    public static WallEntryInboundDto parseFromRawJson(String rawJson){
        WallEntryInboundDto parsedObject = new Gson().fromJson(rawJson, WallEntryInboundDto.class);
        if(parsedObject.author == null || parsedObject.text == null){
            throw new InvalidDataException();
        }else {
            return parsedObject;
        }
    }
}
