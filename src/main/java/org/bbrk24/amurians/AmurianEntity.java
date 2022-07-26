package org.bbrk24.amurians;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.tag.TagKey;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class AmurianEntity extends MerchantEntity {
    public static final double MAX_SPEED = 0.5;
    public static final double WANDER_SPEED = 0.35;
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_MODULES = ImmutableList.of(
        MemoryModuleType.PATH,
        MemoryModuleType.DOORS_TO_CLOSE,
        MemoryModuleType.LOOK_TARGET,
        MemoryModuleType.WALK_TARGET,
        MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
        MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM,
        MemoryModuleType.HOME
    );
    protected static final ImmutableList<SensorType<? extends Sensor<? super AmurianEntity>>> SENSORS
        = ImmutableList.of();
    private static final TagKey<Item> PREFERRED_FOODS = TagKey.of(
        Registry.ITEM_KEY,
        new Identifier("amurians", "amurian_preferred_food")
    );

    private float lastActionableDamage = 0.0f;

    public AmurianEntity(EntityType<? extends AmurianEntity> entityType, World world) {
        super(entityType, world);

        this.setPathfindingPenalty(PathNodeType.DANGER_CACTUS, 16.0f);
        this.setPathfindingPenalty(PathNodeType.DANGER_OTHER, 2.0f);
        this.setPathfindingPenalty(PathNodeType.WATER_BORDER, 0.0f);

        MobNavigation nav = (MobNavigation)this.getNavigation();
        nav.setCanPathThroughDoors(true);
        this.setCanPickUpLoot(true);
    }

    @Override
    protected void afterUsing(TradeOffer offer) {
        this.world.spawnEntity(
            new ExperienceOrbEntity(
                this.world,
                this.getX(),
                this.getY() + 0.5,
                this.getZ(),
                3 + this.random.nextInt(4)
            )
        );
    }

    @Override
    protected void fillRecipes() {
        TradeOfferList offers = this.getOffers();
        if (!offers.isEmpty()) {
            return;
        }

        offers.add(
            new TradeOffer(
                new ItemStack(Items.COAL, 15),
                new ItemStack(Items.AIR),
                new ItemStack(Initializer.RUBY),
                16,
                0,
                1.0f
            )
        );
        offers.add(
            new TradeOffer(
                new ItemStack(Initializer.RUBY),
                new ItemStack(Items.AIR),
                new ItemStack(Items.COOKED_COD, 15),
                16,
                0,
                1.0f
            )
        );
    }

    @Override
    @Nullable
    public PassiveEntity createChild(ServerWorld serverWorld, PassiveEntity passiveEntity) {
        // TODO Auto-generated method stub
        return null;
    }

    public static DefaultAttributeContainer.Builder createAmurianAttributes() {
        return MobEntity.createMobAttributes()
            .add(EntityAttributes.GENERIC_MAX_HEALTH, 18.0)
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 1.5)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, MAX_SPEED)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 20.0);
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        // copied from WanderingTraderEntity::interactMob
        ItemStack itemStack = player.getStackInHand(hand);
        if (!itemStack.isOf(Items.VILLAGER_SPAWN_EGG) && this.isAlive() && !this.hasCustomer() && !this.isBaby()) {
            if (hand == Hand.MAIN_HAND) {
                player.incrementStat(Stats.TALKED_TO_VILLAGER);
            }
            if (this.getOffers().isEmpty()) {
                return ActionResult.success(this.world.isClient);
            }
            if (!this.world.isClient) {
                this.setCustomer(player);
                this.sendOffers(player, this.getDisplayName(), 1);
            }
            return ActionResult.success(this.world.isClient);
        }
        return super.interactMob(player, hand);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public Brain<AmurianEntity> getBrain() {
        return (Brain<AmurianEntity>)super.getBrain();
    }

    @Override
    protected Brain<AmurianEntity> deserializeBrain(Dynamic<?> dynamic) {
        Brain<AmurianEntity> brain = this.createBrainProfile().deserialize(dynamic);
        this.initBrain(brain);
        return brain;
    }

    @Override
    protected Brain.Profile<AmurianEntity> createBrainProfile() {
        return Brain.createProfile(MEMORY_MODULES, SENSORS);
    }

    protected void initBrain(Brain<AmurianEntity> brain) {
        brain.setTaskList(Activity.CORE, AmurianTaskListProvider.createCoreTasks());

        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.refreshActivities(this.world.getTimeOfDay(), this.world.getTime());
    }

    @Override
    public void setAttacker(LivingEntity attacker) {
        if (
            attacker != null &&
            this.canTarget(attacker) &&
            (attacker != this.getAttacker() || this.lastDamageTaken > this.lastActionableDamage)
        ) {
            this.lastActionableDamage = this.lastDamageTaken;
            this.setTarget(attacker);
        }
        super.setAttacker(attacker);
    }

    @Override
    public boolean tryEquip(ItemStack equipment) {
        EquipmentSlot equipmentSlot = AmurianEntity.getPreferredEquipmentSlot(equipment);
        ItemStack itemStack = getEquippedStack(equipmentSlot);
        if (prefersNewEquipment(equipment, itemStack) && this.canPickupItem(equipment)) {
            if (
                !itemStack.isEmpty() &&
                    Math.max(this.random.nextDouble() - 0.1, 0.0) < getDropChance(equipmentSlot)
            ) {
                dropStack(itemStack);
            }
            equipLootStack(equipmentSlot, equipment);
            return true;
        }
        return false;
    }

    @Override
    public boolean canEquip(ItemStack stack) {
        if (stack.getItem() instanceof ArmorItem) {
            return false;
        }
        EquipmentSlot equipmentSlot = AmurianEntity.getPreferredEquipmentSlot(stack);
        ItemStack itemStack = this.getEquippedStack(equipmentSlot);
        return this.prefersNewEquipment(stack, itemStack);
    }

    public static EquipmentSlot getPreferredEquipmentSlot(ItemStack stack) {
        if (stack.getItem().isFood()) {
            return EquipmentSlot.OFFHAND;
        }
        return MobEntity.getPreferredEquipmentSlot(stack);
    }

    @Override
    protected boolean prefersNewEquipment(ItemStack newStack, ItemStack oldStack) {
        if (oldStack.isEmpty()) {
            return true;
        }

        Item newItem = newStack.getItem();
        Item oldItem = oldStack.getItem();

        if (newItem.isFood() && oldItem.isFood()) {
            // short-circuit; don't check the status effect list for suspicious stew
            if (newItem instanceof SuspiciousStewItem) {
                return false;
            }
            if (oldItem instanceof SuspiciousStewItem) {
                return true;
            }

            FoodComponent newFood = newItem.getFoodComponent();
            FoodComponent oldFood = oldItem.getFoodComponent();

            // If only one gives poison, or one is more poisonous, prefer the one with less poison
            List<Pair<StatusEffectInstance, Float>> newStatusEffects = newFood.getStatusEffects();
            List<Pair<StatusEffectInstance, Float>> oldStatusEffects = oldFood.getStatusEffects();
            // poison amount = poison time * pow(2, poison level), since higher level poison damages
            // faster in that exact way
            // regen 2 = poison 1, but negative
            float newFoodPoisonAmount = 0.0f;
            float oldFoodPoisonAmount = 0.0f;

            for (Pair<StatusEffectInstance, ?> p : newStatusEffects) {
                StatusEffectInstance effect = p.getFirst();
                if (effect.getEffectType() == StatusEffects.POISON) {
                    newFoodPoisonAmount +=
                        (float)effect.getDuration() * (float)Math.pow(2.0, (double)effect.getAmplifier());
                } else if (effect.getEffectType() == StatusEffects.REGENERATION) {
                    newFoodPoisonAmount += -0.5f *
                        (float)effect.getDuration() *
                        (float)Math.pow(2.0, (double)effect.getAmplifier());
                }
            }
            for (Pair<StatusEffectInstance, ?> p : oldStatusEffects) {
                StatusEffectInstance effect = p.getFirst();
                if (effect.getEffectType() == StatusEffects.POISON) {
                    oldFoodPoisonAmount +=
                        (float)effect.getDuration() * (float)Math.pow(2.0, (double)effect.getAmplifier());
                } else if (effect.getEffectType() == StatusEffects.REGENERATION) {
                    oldFoodPoisonAmount += -0.5f *
                        (float)effect.getDuration() *
                        (float)Math.pow(2.0, (double)effect.getAmplifier());
                }
            }

            if (newFoodPoisonAmount != oldFoodPoisonAmount) {
                return newFoodPoisonAmount < oldFoodPoisonAmount;
            }

            // if there's no poison/regen difference, prefer specific foods
            boolean newFoodPreferred = newStack.isIn(PREFERRED_FOODS);
            if (newFoodPreferred != oldStack.isIn(PREFERRED_FOODS)) {
                return newFoodPreferred;
            }

            // failing that, go by those with higher hunger
            if (newFood.getHunger() != oldFood.getHunger()) {
                return newFood.getHunger() > oldFood.getHunger();
            }

            // if hunger is equal, prefer those eaten quickly
            if (newFood.isSnack() != oldFood.isSnack()) {
                return newFood.isSnack();
            }
        }
        if (newItem instanceof TridentItem) {
            return this.prefersTrident(newStack, oldStack);
        }
        if (oldItem instanceof TridentItem) {
            return !this.prefersTrident(oldStack, newStack);
        }
        if (oldItem instanceof CrossbowItem && newItem instanceof BowItem) {
            return true;
        }
        if (newItem instanceof MiningToolItem && oldItem instanceof SwordItem) {
            MiningToolItem tool = (MiningToolItem) newItem;
            SwordItem sword = (SwordItem)oldStack.getItem();
            float newDPS = tool.getAttackDamage() * (float)getAttackSpeed(tool);
            float oldDPS = sword.getAttackDamage() * (float)getAttackSpeed(sword);
            if (newDPS != oldDPS) {
                return newDPS > oldDPS;
            }
        }
        if (newItem instanceof SwordItem && oldItem instanceof MiningToolItem) {
            SwordItem sword = (SwordItem)newStack.getItem();
            MiningToolItem tool = (MiningToolItem)oldStack.getItem();
            float newDPS = sword.getAttackDamage() * (float)getAttackSpeed(sword);
            float oldDPS = tool.getAttackDamage() * (float)getAttackSpeed(tool);
            if (newDPS != oldDPS) {
                return newDPS > oldDPS;
            }
        }

        return super.prefersNewEquipment(newStack, oldStack);
    }

    private boolean prefersTrident(ItemStack trident, ItemStack other) {
        if (other.getItem() instanceof TridentItem) {
            return this.prefersNewDamageableItem(trident, other);
        }
        if (other.getItem() instanceof MiningToolItem) {
            MiningToolItem otherItem = (MiningToolItem)other.getItem();
            float otherDPS = otherItem.getAttackDamage() * (float)getAttackSpeed(otherItem);
            if (otherDPS > 9.9f) {
                return false;
            }
        }
        if (other.getItem() instanceof SwordItem) {
            SwordItem otherItem = (SwordItem)other.getItem();
            float otherDamage = otherItem.getAttackDamage();
            if (otherDamage > 6.1875f) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the attack speed of a tool or weapon.
     * @param item Must be a {@c ToolItem} or {@c TridentItem}.
     */
    private double getAttackSpeed(Item item) {
        EntityAttributeModifier modifier = item.getAttributeModifiers(EquipmentSlot.MAINHAND)
                .get(EntityAttributes.GENERIC_ATTACK_SPEED)
                .toArray(new EntityAttributeModifier[0])[0];
        return modifier.getValue();
    }

    @Override
    protected void consumeItem() {
        super.consumeItem();
        if (this.activeItemStack.isFood()) {
            FoodComponent food = this.activeItemStack.getItem().getFoodComponent();
            this.heal(food.getHunger());
            this.activeItemStack.decrement(1);
        }
    }

    public static enum BiomeGroup {
        COLD,
        MODERATE,
        JUNGLE,
        SAVANNAH;

        private static final TagKey<Biome> JUNGLE_BIOMES = TagKey.of(
            Registry.BIOME_KEY,
            new Identifier("amurians", "spawns_jungle_amurians")
        );

        public static BiomeGroup of(RegistryEntry<Biome> biomeEntry) {
            // All jungle-like climates, and lush caves, fall into the "jungle" group.
            if (biomeEntry.isIn(JUNGLE_BIOMES)) {
                return JUNGLE;
            }
            // All hot, dry climates fall into the "savannah" group.
            float temperature = biomeEntry.value().getTemperature();
            if (temperature >= 1.0f) {
                return SAVANNAH;
            }
            // All biomes cold enough to support spruce trees fall into the "cold" group.
            if (temperature <= 0.3f) {
                return COLD;
            }
            // All other biomes fall into the "moderate" group.
            return MODERATE;
        }

        @Override
        public String toString() {
            switch (this) {
            case COLD:
                return "cold";
            case JUNGLE:
                return "jungle";
            case MODERATE:
                return "moderate";
            case SAVANNAH:
                return "savanna"; // [sic]
            }
        }

        public static BiomeGroup fromString(String s) {
            switch (s) {
            case "cold":
                return COLD;
            case "jungle":
                return JUNGLE;
            case "moderate":
                return MODERATE;
            case "savanna":
                return SAVANNAH;
            default:
                throw new IllegalArgumentException(
                    String.format("Unrecognized biome group '%s'", s)
                );
            }
        }
    }

    public static enum Profession {
        UNEMPLOYED,
        FISHERMAN,
        FARMER,
        BUTCHER,
        WEAPONSMITH;

        @Override
        public String toString() {
            return this.name().toLowerCase();
        }

        public static Profession fromString(String s) {
            return valueOf(s.toUpperCase());
        }
    }
}
