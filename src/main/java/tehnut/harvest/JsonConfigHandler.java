package tehnut.harvest;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class JsonConfigHandler {

    public static Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().registerTypeAdapter(BlockStack.class, new SerializerBlockStack()).create();
    public static List<Crop> tempList = new ArrayList<Crop>();

    public static void init(File jsonConfig) {
        try {
            if (!jsonConfig.exists() && jsonConfig.createNewFile()) {
                List<Crop> defaultList = handleDefaults();
                String json = gson.toJson(defaultList, new TypeToken<ArrayList<Crop>>() { }.getType());
                FileWriter writer = new FileWriter(jsonConfig);
                writer.write(json);
                writer.close();
            }

            tempList = gson.fromJson(new FileReader(jsonConfig), new TypeToken<ArrayList<Crop>>() { }.getType());

            for (Crop crop : tempList)
                Harvest.instance.cropMap.put(crop.getInitialBlock(), crop);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<Crop> handleDefaults() {
        List<Crop> defaultList = new ArrayList<Crop>();
        Crop wheat = new Crop(
                new BlockStack(Blocks.wheat, 7),
                new BlockStack(Blocks.wheat, 0)
        );
        defaultList.add(wheat);
        Crop carrot = new Crop(
                new BlockStack(Blocks.carrots, 7),
                new BlockStack(Blocks.carrots, 0)
        );
        defaultList.add(carrot);
        Crop potato = new Crop(
                new BlockStack(Blocks.potatoes, 7),
                new BlockStack(Blocks.potatoes, 0)
        );
        defaultList.add(potato);

        return defaultList;
    }

    public static class SerializerBlockStack implements JsonDeserializer<BlockStack>, JsonSerializer<BlockStack> {

        @Override
        public BlockStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String name = json.getAsJsonObject().get("blockName").getAsString();
            int meta = 0;
            if (json.getAsJsonObject().get("meta") != null)
                meta = json.getAsJsonObject().get("meta").getAsInt();

            return new BlockStack(Block.blockRegistry.getObject(new ResourceLocation(name)), meta);
        }

        @Override
        public JsonElement serialize(BlockStack src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("blockName", Block.blockRegistry.getNameForObject(src.getBlock()).toString());
            jsonObject.addProperty("meta", src.getMeta());
            return jsonObject;
        }
    }
}
