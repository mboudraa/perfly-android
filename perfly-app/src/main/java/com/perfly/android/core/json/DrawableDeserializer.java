package com.perfly.android.core.json;

import android.graphics.drawable.Drawable;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.perfly.android.utils.ImageUtils;

import java.io.IOException;

public class DrawableDeserializer extends JsonDeserializer<Drawable> {

    @Override
    public Drawable deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        return ImageUtils.base64ToDrawable(jsonParser.getValueAsString());
    }
}
