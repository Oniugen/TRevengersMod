package com.tokyorevengers.entity;

import com.tokyorevengers.gang.Gang;
import com.tokyorevengers.gang.TerritoryData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.ChunkPos;

import java.util.EnumSet;
import java.util.Map;

/** Faz integrantes sem alvo avançarem até territórios rivais conhecidos. */
public class TerritoryRaidGoal extends Goal {
    private final GangMemberEntity mob;
    private BlockPos destination;
    private int repathDelay;

    public TerritoryRaidGoal(GangMemberEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (mob.level().isClientSide || mob.getGang().isNeutral() || mob.getTarget() != null || mob.tickCount % 100 != 0)
            return false;
        if (!(mob.level() instanceof ServerLevel level)) return false;
        Map<Long, String> claims = TerritoryData.get(level).snapshot();
        double best = Double.MAX_VALUE;
        BlockPos choice = null;
        for (Map.Entry<Long, String> entry : claims.entrySet()) {
            Gang owner = Gang.byId(entry.getValue());
            if (!Gang.areRivals(mob.getGang(), owner)) continue;
            ChunkPos chunk = new ChunkPos(entry.getKey());
            BlockPos center = new BlockPos(chunk.getMinBlockX() + 8, mob.blockPosition().getY(), chunk.getMinBlockZ() + 8);
            double distance = center.distSqr(mob.blockPosition());
            if (distance < best && distance < 48.0D * 48.0D) {
                best = distance;
                choice = center;
            }
        }
        destination = choice;
        return destination != null;
    }

    @Override
    public void start() {
        repathDelay = 0;
        move();
    }

    private void move() {
        if (destination != null) mob.getNavigation().moveTo(destination.getX(), destination.getY(), destination.getZ(), 0.85D);
    }

    @Override
    public boolean canContinueToUse() {
        return destination != null && mob.getTarget() == null && !mob.getNavigation().isDone()
                && mob.distanceToSqr(destination.getX(), destination.getY(), destination.getZ()) > 9.0D;
    }

    @Override
    public void tick() {
        if (--repathDelay <= 0) {
            repathDelay = 40;
            move();
        }
    }

    @Override
    public void stop() {
        destination = null;
        mob.getNavigation().stop();
    }
}
