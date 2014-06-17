package com.samantha.app.core;

import android.graphics.drawable.Drawable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.samantha.app.core.json.DrawableDeserializer;
import com.samantha.app.core.json.DrawableSerializer;

public class Application {

    @JsonProperty
    public final String label;

    @JsonSerialize(using = DrawableSerializer.class)
    @JsonDeserialize(using = DrawableDeserializer.class)
    @JsonProperty
    public final Drawable logo;

    @JsonProperty
    public final String version;

    @JsonProperty
    public final int uid;

    @JsonProperty
    public final boolean debuggable;

    @JsonProperty
    public final String packageName;

    @JsonCreator
    public Application(int uid, String label, Drawable logo, String version, String packageName, boolean debuggable) {
        this.label = label;
        this.logo = logo;
        this.version = version;
        this.uid = uid;
        this.debuggable = debuggable;
        this.packageName = packageName;
    }
}
