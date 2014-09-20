/*
 * Copyright (c) 2014 Mounir Boudraa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.perfly.android.core.net;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.perfly.android.utils.Json;
import timber.log.Timber;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Message<T> {

    public final T body;

    public final String address;

    @JsonCreator
    public Message(@JsonProperty("body") T body, @JsonProperty("address") String address) {
        this.body = body;
        this.address = address;
    }

    @Override
    public String toString() {
        try {
            return Json.toJson(this);
        } catch (JsonProcessingException e) {
            Timber.w("Impossible to stringify message");
            return super.toString();
        }
    }

    public String serialize() throws JsonProcessingException {
        return Json.toJson(this);
    }
}
