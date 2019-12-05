package de.blom.httpserver.crosscutting.representation.wall;

import com.google.gson.Gson;
import de.blom.httpserver.crosscutting.exception.InvalidDataException;
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
