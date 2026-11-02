package com.tokyorevengers.entity;

import com.tokyorevengers.gang.Gang;
import com.tokyorevengers.gang.GangCharacter;
import com.tokyorevengers.gang.Reputation;
import com.tokyorevengers.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.UUID;

public class GangMemberEntity extends PathfinderMob {
    private static final UUID BONUS_ID = UUID.fromString("5f1c3a52-6c1d-4a0e-9a53-0f6e2f0b7a11");

    private final GangCharacter character;
    private int abilityCooldown;

    public GangMemberEntity(EntityType<? extends PathfinderMob> type, Level level, GangCharacter character) {
        super(type, level);
        this.character = character;
        this.xpReward = character.leader() ? 30 : 10;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes(GangCharacter c) {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, c.health())
                .add(Attributes.ATTACK_DAMAGE, c.damage())
                .add(Attributes.MOVEMENT_SPEED, c.speed())
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE,
                        c.ability() == GangCharacter.Ability.GUARD ? 0.6D : 0.1D);
    }

    public GangCharacter getCharacter() { return character; }
    public Gang getGang() { return character.gang(); }

    // ------------------------------------------------------------------ IA
    // Atenção: registerGoals() roda dentro do construtor da superclasse, antes de
    // "character" existir. Por isso só usamos getGang() dentro de lambdas (executadas depois).
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2D, true));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(5, new TerritoryRaidGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                e -> e instanceof Player p && shouldAttackPlayer(p)));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, GangMemberEntity.class, 10, true, false,
                e -> e instanceof GangMemberEntity o && shouldAttackMember(o)));
    }

    /** Regras especiais de hostilidade entre organizações e personagens neutros. */
    private boolean shouldAttackMember(GangMemberEntity other) {
        if (getGang().isNeutral()) return false;
        if (getGang() == Gang.KIYOMASA && (other.character.id().equals("draken")
                || other.character.id().equals("draken_retired"))) return true;
        if (other.getGang() == Gang.NEUTRAL) return getGang() != Gang.TOMAN && other.character.id().equals("kojiro_hishi");
        if (getGang() == Gang.KIYOMASA) return false;
        if (other.getGang() == Gang.KIYOMASA) {
            if (getGang() == Gang.TOMAN) return isTomanOfficer(character.id());
            return true;
        }
        return Gang.areRivals(other.getGang(), getGang());
    }

    private static boolean isTomanOfficer(String id) {
        return java.util.Set.of("mikey", "draken", "mitsuya", "baji", "pah_chin", "peh_yan",
                "muto_toman", "haruchiyo_toman", "nahoya", "souya", "chifuyu", "hakkai", "ryusei_satou").contains(id);
    }

    /** Regras de hostilidade baseadas na reputação. */
    public boolean shouldAttackPlayer(Player player) {
        if (getGang().isNeutral() || player.isCreative() || player.isSpectator()) return false;
        Gang own = Reputation.getMembership(player);
        if (own == getGang()) return false;
        int rep = Reputation.get(player, getGang());
        if (rep <= Reputation.HOSTILE_THRESHOLD) return true;
        // Membro de gangue rival só é tolerado se tiver boa reputação com esta gangue
        return own != null && Gang.areRivals(own, getGang()) && rep < Reputation.JOIN_REQUIREMENT;
    }

    @Override
    public boolean isAlliedTo(Entity other) {
        if (other instanceof GangMemberEntity g) return g.getGang() == getGang();
        return super.isAlliedTo(other);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (abilityCooldown > 0) abilityCooldown--;
        // Kiyomasa saca uma faca ao reconhecer Draken.
        if (character.id().equals("kiyomasa") && tickCount % 10 == 0) {
            GangMemberEntity draken = level().getEntitiesOfClass(GangMemberEntity.class,
                    getBoundingBox().inflate(20.0D), e -> (e.character.id().equals("draken")
                            || e.character.id().equals("draken_retired")) && e.isAlive())
                    .stream().findFirst().orElse(null);
            if (draken != null) {
                setTarget(draken);
                setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.KNIFE.get()));
            }
        }
    }

    // ------------------------------------------------------------- Combate
    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        if (result && !level().isClientSide && source.getEntity() instanceof LivingEntity attacker && attacker != this) {
            rallyAllies(attacker);
            abilityOnHurt(attacker);
        }
        return result;
    }

    private void rallyAllies(LivingEntity attacker) {
        if (attacker instanceof GangMemberEntity g && g.getGang() == getGang()) return;
        AABB area = getBoundingBox().inflate(16.0D);
        for (GangMemberEntity ally : level().getEntitiesOfClass(GangMemberEntity.class, area,
                o -> o != this && o.isAlive() && o.getGang() == getGang())) {
            if (ally.getTarget() == null) ally.setTarget(attacker);
        }
    }

    private void abilityOnHurt(LivingEntity attacker) {
        if (abilityCooldown > 0) return;
        float ratio = getHealth() / getMaxHealth();
        switch (character.ability()) {
            case GUARD -> {
                if (ratio < 0.6F) {
                    addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 1));
                    abilityCooldown = 400;
                }
            }
            case TENACITY -> {
                if (ratio < 0.3F) {
                    heal(getMaxHealth() * 0.3F);
                    addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1));
                    abilityCooldown = 1200;
                }
            }
            case BERSERK -> {
                if (ratio < 0.5F) {
                    addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 0));
                    addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 0));
                    abilityCooldown = 600;
                }
            }
            case RALLY -> {
                AABB area = getBoundingBox().inflate(16.0D);
                for (GangMemberEntity ally : level().getEntitiesOfClass(GangMemberEntity.class, area,
                        o -> o.isAlive() && o.getGang() == getGang())) {
                    ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 0));
                }
                abilityCooldown = 600;
            }
            default -> { }
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        double base = getAttributeValue(Attributes.ATTACK_DAMAGE);
        double bonus = 0;
        boolean kick = false;
        float ratio = getHealth() / getMaxHealth();

        switch (character.ability()) {
            case KICK -> {
                if (random.nextInt(4) == 0) {
                    bonus = base * 0.75D;
                    kick = true;
                }
            }
            case BERSERK -> {
                if (ratio < 0.5F) bonus = base * 0.3D;
            }
            default -> { }
        }

        AttributeInstance inst = getAttribute(Attributes.ATTACK_DAMAGE);
        if (bonus > 0 && inst != null) {
            inst.addTransientModifier(new AttributeModifier(BONUS_ID, "ability_bonus", bonus,
                    AttributeModifier.Operation.ADDITION));
        }
        boolean hit = super.doHurtTarget(target);
        if (inst != null) inst.removeModifier(BONUS_ID);

        if (hit && target instanceof LivingEntity living) {
            if (kick) {
                living.knockback(1.2D, getX() - target.getX(), getZ() - target.getZ());
                level().playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_KNOCKBACK,
                        SoundSource.HOSTILE, 1.0F, 1.0F);
                if (level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY(0.5D), target.getZ(),
                            12, 0.3D, 0.3D, 0.3D, 0.1D);
                }
            }
            if (character.ability() == GangCharacter.Ability.COMBO && random.nextFloat() < 0.3F) {
                living.invulnerableTime = 0; // permite o próximo golpe quase imediatamente
            }
        }
        return hit;
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!level().isClientSide && source.getEntity() instanceof ServerPlayer player) {
            Reputation.onKill(player, this);
        }
    }

    // --------------------------------------------------------- Interação
    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (level().isClientSide) return InteractionResult.sidedSuccess(true);

        Gang gang = getGang();
        ItemStack stack = player.getItemInHand(hand);

        if (stack.is(ModItems.TAIYAKI.get())) {
            if (!player.isCreative()) stack.shrink(1);
            Reputation.add(player, gang, 5);
            heal(2.0F);
            say(player, "Hm, taiyaki! Valeu, vou me lembrar disso.");
            return InteractionResult.CONSUME;
        }

        if (shouldAttackPlayer(player)) {
            say(player, "Você tem coragem de aparecer aqui? Prepare-se!");
            setTarget(player);
            return InteractionResult.CONSUME;
        }

        int rep = Reputation.get(player, gang);

        if (player.isShiftKeyDown() && Reputation.getMembership(player) == null) {
            if (rep >= Reputation.JOIN_REQUIREMENT) {
                Reputation.join(player, gang);
                say(player, "Bem-vindo à gangue. Não nos decepcione.");
                player.displayClientMessage(Component.literal("Você entrou na ")
                        .append(gang.displayName()).append("!"), false);
            } else {
                say(player, "Ainda não confio em você. Reputação: " + rep + "/" + Reputation.JOIN_REQUIREMENT);
            }
            return InteractionResult.CONSUME;
        }

        say(player, character.quote());
        player.displayClientMessage(Component.literal("Reputação com ").append(gang.displayName())
                .append(": " + rep + " (" + Reputation.tier(rep) + ")")
                .withStyle(ChatFormatting.GRAY), false);
        return InteractionResult.CONSUME;
    }

    private void say(Player player, String text) {
        player.displayClientMessage(Component.literal("<")
                .append(getName().copy().withStyle(getGang().displayName().getStyle()))
                .append("> " + text), false);
    }

    // ------------------------------------------------------------- Sons
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PLAYER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PLAYER_DEATH;
    }

    @Override
    public boolean shouldShowName() {
        return true;
    }
}
