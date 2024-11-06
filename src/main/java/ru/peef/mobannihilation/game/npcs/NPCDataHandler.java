package ru.peef.mobannihilation.game.npcs;

import com.google.gson.*;
import ru.peef.mobannihilation.MobAnnihilation;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class NPCDataHandler {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static File file = null;

    public static void init() {
        file = new File(MobAnnihilation.getInstance().getDataFolder(), "npcs.json");
        if (!file.exists()) {
            try {
                MobAnnihilation.getInstance().getDataFolder().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void saveNPC(NPC npc) {
        try {
            Map<String, NPC> npcData = loadAllNPCs();
            npcData.put(npc.getName(), npc);

            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(npcData, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean hasNPC(String name) { return loadAllNPCs().containsKey(name); }
    public static void updateNPC(NPC npc) { saveNPC(npc); }

    public static NPC loadNPC(String name) {
        Map<String, NPC> npcData = loadAllNPCs();
        return npcData.get(name);
    }

    public static void deleteNPC(String name) {
        try {
            Map<String, NPC> npcData = loadAllNPCs();
            if (npcData.remove(name) != null) {
                try (FileWriter writer = new FileWriter(file)) {
                    gson.toJson(npcData, writer);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, NPC> loadAllNPCs() {
        try {
            if (!file.exists()) {
                file.createNewFile();
                return new HashMap<>();
            }

            try (FileReader reader = new FileReader(file)) {
                JsonElement element = new JsonParser().parse(reader);
                Map<String, NPC> npcData = new HashMap<>();
                if (element.isJsonObject()) {
                    JsonObject jsonObject = element.getAsJsonObject();
                    for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                        NPC npc = gson.fromJson(entry.getValue(), NPC.class);
                        npcData.put(entry.getKey(), npc);
                    }
                }
                return npcData;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }
}
