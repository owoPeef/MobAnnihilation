package ru.peef.mobannihilation.game.npcs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NPCManager {
    public static List<NPC> CHARACTERS = new ArrayList<>();

    public static void init() {
        for (Map.Entry<String, NPC> character : NPCDataHandler.loadAllNPCs().entrySet()) {
            NPC currentNPC = character.getValue();
            currentNPC.setName(character.getKey());
            currentNPC.spawn();

            CHARACTERS.add(currentNPC);
        }
    }

    public static NPC get(UUID uniqueId) {
        for (NPC character : CHARACTERS) if (character.uniqueId.equals(uniqueId)) return character;
        return null;
    }
}
