package com.samantha.app.core.json;

import android.graphics.drawable.Drawable;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.samantha.app.utils.ImageUtils;

import java.io.IOException;

public class DrawableSerializer extends JsonSerializer<Drawable> {

    @Override
    public void serialize(Drawable drawable, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
        jsonGenerator.writeString(ImageUtils.drawableToBase64(drawable));
    }

}
