package org.bbrk24.amurians;

import java.util.UUID;
import org.jetbrains.annotations.Nullable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ExperienceOrbEntity;
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
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.TridentItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.world.World;

// Bug list:
// - won't pick up food (ergo, can't test eat goal)
// - won't walk towards items
// - won't trade sword for tool
// - doesn't save/despawns when world closes

public class AmurianEntity extends MerchantEntity implements Angerable {
    private static final UniformIntProvider ANGER_TIME_RANGE = TimeHelper.betweenSeconds(1, 5);
    private static final double MAX_SPEED = 0.5;
    private static final double WANDER_SPEED = 0.35;
    private UUID angryAt = null;
    private int angryTime = 0;

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
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, WANDER_SPEED);
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
        this.goalSelector.add(1, new FleeEntityGoal<>(this, CreeperEntity.class, 3.0f, MAX_SPEED, MAX_SPEED));
        this.goalSelector.add(1, new AmurianEscapeDangerGoal(this));
        this.goalSelector.add(2, new AttackGoal(this));
        this.goalSelector.add(3, new StopFollowingCustomerGoal(this));
        this.goalSelector.add(3, new LookAtCustomerGoal(this));
        this.goalSelector.add(4, new AmurianEatGoal(this));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, WANDER_SPEED));
        this.goalSelector.add(6, new StopAndLookAtEntityGoal(this, PlayerEntity.class, 3.0f, 1.0f));
        this.targetSelector.add(1, new RevengeGoal(this, new Class[0]));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, ZombieEntity.class, true, true));
    }

    @Override
    public int getAngerTime() {
        return this.angryTime;
    }

    @Override
    public void setAngerTime(int angerTime) {
        this.angryTime = angerTime;
    }

    @Override
    public UUID getAngryAt() {
        return angryAt;
    }

    @Override
    public void setAngryAt(UUID uuid) {
        this.angryAt = uuid;
    }

    @Override
    public void chooseRandomAngerTime() {
        setAngerTime(ANGER_TIME_RANGE.get(random));
    }

    @Override
    protected void mobTick() {
        this.tickAngerLogic((ServerWorld)this.world, false);
        super.mobTick();
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
        EntityAttributeModifier modifier =
            (EntityAttributeModifier)item.getAttributeModifiers(EquipmentSlot.MAINHAND)
                .get(EntityAttributes.GENERIC_ATTACK_SPEED)
                .toArray()[0];
        return modifier.getValue();
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

    static class AmurianEatGoal extends Goal {
        /** How long it takes to eat something, in seconds */
        private static float EAT_TIME_SECONDS = 1.6f;
        private AmurianEntity mob;
        private byte eatingTicks = 0;

        public AmurianEatGoal(AmurianEntity mob) {
            this.mob = mob;
        }

        private float getEatingDuration() {
            if (mob.getOffHandStack().isEmpty() || !mob.getOffHandStack().getItem().isFood()) {
                return 0.0f;
            }
            return (float)eatingTicks *
                (mob.getOffHandStack().getItem().getFoodComponent().isSnack() ? 20.0f : 10.0f);
        }

        @Override
        public boolean canStart() {
            // taken at least 1.5 hearts damage and holding food in offhand
            return mob.getHealth() <= 15.0f &&
                !mob.getOffHandStack().isEmpty() &&
                mob.getOffHandStack().getItem().isFood();
        }

        @Override
        public void start() {
            this.eatingTicks = 0;
        }
        
        @Override
        public void tick() {
            ++eatingTicks;
            ItemStack foodStack = mob.getOffHandStack();
            mob.tickItemStackUsage(foodStack);
            if (getEatingDuration() >= EAT_TIME_SECONDS) {
                FoodComponent food = foodStack.getItem().getFoodComponent();
                mob.heal(food.getHunger());
                mob.eatFood(mob.world, foodStack);
            }
        }

        @Override
        public boolean shouldContinue() {
            return canStart() && getEatingDuration() < EAT_TIME_SECONDS;
        }
    }
}
