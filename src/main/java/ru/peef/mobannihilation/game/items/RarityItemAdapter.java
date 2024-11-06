package ru.peef.mobannihilation.game.items;

import com.google.gson.*;

import java.lang.reflect.Type;

public class RarityItemAdapter implements JsonSerializer<RarityItem>, JsonDeserializer<RarityItem> {
    @Override
    public JsonElement serialize(RarityItem src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        obj.addProperty("title", src.getTitle());
        obj.addProperty("description", src.getDescription());
        obj.addProperty("base_value", src.baseValue);
        obj.addProperty("boost_type", src.boost.name().toUpperCase());
        obj.addProperty("boost_percent", src.boostPercent);

        return obj;
    }

    @Override
    public RarityItem deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        String title = obj.get("title").getAsString();
        String description = obj.get("description").getAsString();
        String boost_type = obj.get("boost_type").getAsString();
        float boost_percent = obj.get("boost_percent").getAsFloat();
        float base_value = obj.get("base_value").getAsFloat();

        return RarityItem.create(title, description, base_value, boost_type, boost_percent);
    }
}