package cc.astralis.util.player;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author Kawase
 * @since 16.08.2025
 */
public class StorageUtil {
    private static final Long2ObjectOpenHashMap<ObjectArrayList<BlockPos>> BY_CHUNK = new Long2ObjectOpenHashMap<>();

    @Setter @Getter
    private static boolean collectData = false;

    private static final Predicate<BlockEntity> IS_CHESTY = be ->
            be instanceof ChestBlockEntity
                    || be instanceof EnderChestBlockEntity
                    || be instanceof BarrelBlockEntity;

    private StorageUtil() {}

    public static void clearAll() {
        BY_CHUNK.clear();
    }

    public static void collectLoadedAroundPlayer() {
        if (!collectData || !hasWorld()) return;

        final Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        final int view = mc.options.renderDistance().get();
        final int radius = Math.max(2, view + 2);
        final ChunkPos center = new ChunkPos(mc.player.blockPosition());

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int cx = center.x + dx;
                int cz = center.z + dz;
                LevelChunk chunk = mc.level.getChunkSource().getChunkNow(cx, cz);
                if (chunk != null) {
                    onChunkLoaded(chunk);
                }
            }
        }
    }

    public static void onChunkLoaded(LevelChunk chunk) {
        if (!collectData) return;

        final long key = chunk.getPos().toLong();
        final ObjectArrayList<BlockPos> list = new ObjectArrayList<>();
        for (BlockEntity be : chunk.getBlockEntities().values()) {
            if (IS_CHESTY.test(be)) list.add(be.getBlockPos().immutable());
        }
        BY_CHUNK.put(key, list);
    }

    public static void onChunkUnloaded(ChunkPos pos) {
        BY_CHUNK.remove(pos.toLong());
    }

    public static void onBlockEntityAdded(LevelChunk chunk, BlockEntity be) {
        if (!collectData) return;
        if (!IS_CHESTY.test(be)) return;

        final long key = chunk.getPos().toLong();
        BY_CHUNK.computeIfAbsent(key, k -> new ObjectArrayList<>()).add(be.getBlockPos().immutable());
    }

    public static void onBlockEntityRemoved(LevelChunk chunk, BlockPos pos) {
        if (!collectData) return;

        final long key = chunk.getPos().toLong();
        final ObjectArrayList<BlockPos> list = BY_CHUNK.get(key);
        if (list == null) return;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(pos)) {
                list.remove(i);
                break;
            }
        }
        if (list.isEmpty()) BY_CHUNK.remove(key);
    }

    public static ObjectArrayList<BlockPos> snapshotAll() {
        final ObjectArrayList<BlockPos> out = new ObjectArrayList<>();
        for (List<BlockPos> l : BY_CHUNK.values()) out.addAll(l);
        return out;
    }

    public static ObjectArrayList<BlockPos> snapshotVisible(
            net.minecraft.client.Camera cam,
            double maxDistSq,
            java.util.function.Predicate<AABB> frustumVisible
    ) {
        final ObjectArrayList<BlockPos> out = new ObjectArrayList<>();
        final double cx = cam.getPosition().x, cy = cam.getPosition().y, cz = cam.getPosition().z;
        for (List<BlockPos> list : BY_CHUNK.values()) {
            for (BlockPos pos : list) {
                double dx = pos.getX() + 0.5 - cx;
                double dy = pos.getY() + 0.5 - cy;
                double dz = pos.getZ() + 0.5 - cz;
                if (dx*dx + dy*dy + dz*dz > maxDistSq) continue;
                AABB aabb = new AABB(pos);
                if (!frustumVisible.test(aabb)) continue;
                out.add(pos);
            }
        }
        return out;
    }

    public static boolean hasWorld() {
        return Minecraft.getInstance().level != null;
    }
}
