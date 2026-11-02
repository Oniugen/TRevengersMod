package com.tokyorevengers.gang;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;

/** Controle persistente de territórios por chunk. A gangue dominante ocupa o chunk. */
public class TerritoryData extends SavedData {
    private static final String DATA_NAME = "tokyorevengers_territories";
    private final Map<Long, String> owners = new HashMap<>();

    public static TerritoryData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage()
                .computeIfAbsent(TerritoryData::load, TerritoryData::new, DATA_NAME);
    }

    public Gang owner(ChunkPos pos) {
        String id = owners.get(pos.toLong());
        return id == null ? null : Gang.byId(id);
    }

    public void claim(ChunkPos pos, Gang gang) {
        if (gang == null || gang.isNeutral()) return;
        String previous = owners.put(pos.toLong(), gang.id());
        if (!gang.id().equals(previous)) setDirty();
    }

    public Map<Long, String> snapshot() { return Map.copyOf(owners); }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag data = new CompoundTag();
        owners.forEach((chunk, gang) -> data.putString(Long.toString(chunk), gang));
        tag.put("owners", data);
        return tag;
    }

    public static TerritoryData load(CompoundTag tag) {
        TerritoryData result = new TerritoryData();
        CompoundTag data = tag.getCompound("owners");
        for (String key : data.getAllKeys()) {
            try { result.owners.put(Long.parseLong(key), data.getString(key)); }
            catch (NumberFormatException ignored) { }
        }
        return result;
    }
}
