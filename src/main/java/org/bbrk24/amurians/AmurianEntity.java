package org.bbrk24.amurians;

import com.mojang.datafixers.util.Pair;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.AttackGoal;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtCustomerGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.StopAndLookAtEntityGoal;
import net.minecraft.entity.ai.goal.StopFollowingCustomerGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.entity.mob.RavagerEntity;
import net.minecraft.entity.mob.SpellcastingIllagerEntity;
import net.minecraft.entity.mob.VindicatorEntity;
import net.minecraft.entity.mob.ZoglinEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.PandaEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.PufferfishEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.SuspiciousStewItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.TridentItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.tag.TagKey;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.world.World;

// - custom canEquip not called [bug]
// - won't walk towards items [missing feature]
// - doesn't correctly call for help [bug]

public class AmurianEntity extends MerchantEntity {
    private static final TagKey<Item> PREFERRED_FOODS = TagKey.of(
        Registry.ITEM_KEY,
        new Identifier("amurians", "amurian_preferred_food")
    );
    private static final double MAX_SPEED = 0.5;
    private static final double WANDER_SPEED = 0.35;
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
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(
            1,
            new FleeEntityGoal<>(this, CreeperEntity.class, 3.0f, MAX_SPEED, MAX_SPEED)
        );
        this.goalSelector.add(1, new AmurianEscapeDangerGoal(this));
        this.goalSelector.add(2, new AmurianAttackGoal(this));
        // The FleeEntityGoal for ZoglinEntity is lower priority than the AttackGoal, so they flee
        // until the Zoglin lands an attack.
        this.goalSelector.add(
            3,
            new FleeEntityGoal<>(this, ZoglinEntity.class, 16.0f, MAX_SPEED, MAX_SPEED)
        );
        this.goalSelector.add(4, new StopFollowingCustomerGoal(this));
        this.goalSelector.add(4, new LookAtCustomerGoal(this));
        this.goalSelector.add(5, new AmurianEatGoal(this));
        this.goalSelector.add(6, new WanderAroundFarGoal(this, WANDER_SPEED));
        this.goalSelector.add(7, new StopAndLookAtEntityGoal(this, PlayerEntity.class, 3.0f, 1.0f));
        this.targetSelector.add(
            1,
            new AmurianRevengeGoal(this, AmurianEntity.class, IronGolemEntity.class)
                .setGroupRevenge()
        );
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, RavagerEntity.class, false, true));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PillagerEntity.class, false));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, SpellcastingIllagerEntity.class, false));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, VindicatorEntity.class, false, true));
        this.targetSelector.add(
            3,
            new ActiveTargetGoal<>(
                this,
                ZombieEntity.class,
                10,
                true,
                true,
                (zombie) -> !(zombie instanceof ZombifiedPiglinEntity || zombie instanceof DrownedEntity)
            )
        );
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
        EntityAttributeModifier modifier = (EntityAttributeModifier) item.getAttributeModifiers(EquipmentSlot.MAINHAND)
                .get(EntityAttributes.GENERIC_ATTACK_SPEED)
                .toArray()[0];
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

    static class AmurianEscapeDangerGoal extends EscapeDangerGoal {
        public AmurianEscapeDangerGoal(AmurianEntity mob) {
            super(mob, AmurianEntity.MAX_SPEED);
        }

        @Override
        protected boolean isInDanger() {
            return this.mob.shouldEscapePowderSnow() || this.mob.isOnFire();
        }
    }

    static class AmurianAttackGoal extends AttackGoal {
        protected AmurianEntity mob;

        public AmurianAttackGoal(AmurianEntity mob) {
            super(mob);
            this.mob = mob;
        }

        @Override
        public boolean canStart() {
            if (this.mob.isBaby()) {
                return false;
            }
            return super.canStart();
        }
    }

    static class AmurianEatGoal extends Goal {
        private AmurianEntity mob;

        public AmurianEatGoal(AmurianEntity mob) {
            super();
            this.mob = mob;
        }

        /** Whether it is possible to eat, regardless of whether the mob is currently eating */
        private boolean canEat() {
            return mob.getMaxHealth() - mob.getHealth() >= 3.0f &&
                !mob.getOffHandStack().isEmpty() &&
                mob.getOffHandStack().getItem().isFood() &&
                mob.getTarget() == null;
        }

        @Override
        public boolean canStart() {
            return canEat() && mob.itemUseTimeLeft == 0;
        }

        @Override
        public boolean shouldRunEveryTick() {
            return true;
        }

        @Override
        public void start() {
            ItemStack offhandStack = this.mob.getOffHandStack();
            this.mob.itemUseTimeLeft = offhandStack.getMaxUseTime();
            this.mob.activeItemStack = offhandStack;
            this.mob.setCurrentHand(Hand.OFF_HAND);
            this.mob.navigation.stop();
        }

        @Override
        public void stop() {
            mob.clearActiveItem();
        }

        @Override
        public boolean shouldContinue() {
            return canEat() && this.mob.itemUseTimeLeft > 0;
        }
    }

    static class AmurianRevengeGoal extends RevengeGoal {
        private static final UniformIntProvider ANGER_TIME_RANGE = TimeHelper.betweenSeconds(1, 5);
        protected final Random random = Random.create();
        protected int ticksRemaining = 0;

        public AmurianRevengeGoal(AmurianEntity mob, Class<?>... noRevengeTypes) {
            super(mob, noRevengeTypes);
        }

        @Override
        protected void setMobEntityTarget(MobEntity mob, LivingEntity target) {
            if (mob instanceof AmurianEntity) {
                AmurianEntity amurian = (AmurianEntity) mob;
                // amurian.setAttacker(this.target);
                amurian.lastActionableDamage = ((AmurianEntity)this.mob).lastActionableDamage;
            }
            super.setMobEntityTarget(mob, target);
        }
        
        @Override
        public void start() {
            AmurianEntity amurian = (AmurianEntity)this.mob;
            int baseTime = ANGER_TIME_RANGE.get(this.random);
            this.ticksRemaining = 1 + (int)Math.floor(amurian.lastActionableDamage * (float)baseTime);
        }

        /** 
         * Whether a mob is guaranteed to continue attacking.
         */
        private boolean isPersistentAttacker(LivingEntity entity) {
            if (entity instanceof PandaEntity) {
                return ((PandaEntity)entity).isAttacking();
            }
            if (
                entity instanceof Monster ||
                entity instanceof Angerable ||
                entity instanceof PufferfishEntity
            ) {
                return true;
            }
            return false;
        }

        @Override
        public boolean shouldContinue() {
            if (!isPersistentAttacker(this.target) && this.ticksRemaining == 0) {
                return false;
            }
            return super.shouldContinue();
        }

        @Override
        public void stop() {
            this.mob.setAttacker(null);
            super.stop();
        }

        @Override
        public void tick() {
            super.tick();
            if (this.ticksRemaining > 0) {
                --this.ticksRemaining;
            }
        }
    }
}
