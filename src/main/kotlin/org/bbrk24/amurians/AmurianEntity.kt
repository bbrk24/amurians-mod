package org.bbrk24.amurians

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import com.mojang.serialization.Dynamic

import net.minecraft.entity.EntityType
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.ExperienceOrbEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.brain.Activity
import net.minecraft.entity.ai.brain.Brain
import net.minecraft.entity.ai.brain.MemoryModuleType
import net.minecraft.entity.ai.brain.sensor.Sensor
import net.minecraft.entity.ai.brain.sensor.SensorType
import net.minecraft.entity.ai.pathing.MobNavigation
import net.minecraft.entity.ai.pathing.PathNodeType
import net.minecraft.entity.attribute.DefaultAttributeContainer
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.passive.MerchantEntity
import net.minecraft.entity.passive.PassiveEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.*
import net.minecraft.server.world.ServerWorld
import net.minecraft.stat.Stats
import net.minecraft.tag.TagKey
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryEntry
import net.minecraft.village.TradeOffer
import net.minecraft.world.World
import net.minecraft.world.biome.Biome

val PREFERRED_FOODS = TagKey.of(Registry.ITEM_KEY, Identifier("amurians", "amurian_preferred_food"))
val JUNGLE_BIOMES = TagKey.of(Registry.BIOME_KEY, Identifier("amurians", "spawns_jungle_amurians"))

class AmurianEntity(entityType: EntityType<out AmurianEntity>, world: World) : MerchantEntity(
    entityType,
    world
) {
    companion object {
        private val MAX_SPEED = 0.5
        private val WANDER_SPEED = 0.35

        @JvmStatic
        fun getWanderSpeed(): Double = WANDER_SPEED

        protected val MEMORY_MODULES = ImmutableList.of(
            MemoryModuleType.PATH,
            MemoryModuleType.DOORS_TO_CLOSE,
            MemoryModuleType.LOOK_TARGET,
            MemoryModuleType.WALK_TARGET,
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
            MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM,
            MemoryModuleType.HOME
        )
        protected val SENSORS: ImmutableList<SensorType<out Sensor<in AmurianEntity>>> =
            ImmutableList.of()

        @JvmStatic
        fun createAmurianAttributes(): DefaultAttributeContainer.Builder {
            return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 18.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 1.5)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, MAX_SPEED)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 20.0)
        }

        fun getPreferredEquipmentSlot(stack: ItemStack): EquipmentSlot {
            if (stack.getItem().isFood()) {
                return EquipmentSlot.OFFHAND
            }
            return MobEntity.getPreferredEquipmentSlot(stack)
        }
    }

    private var lastActionableDamage = 0.0f

    init {
        setPathfindingPenalty(PathNodeType.DANGER_CACTUS, 16.0f)
        setPathfindingPenalty(PathNodeType.DANGER_OTHER, 2.0f)
        setPathfindingPenalty(PathNodeType.WATER_BORDER, 0.0f)

        val nav = getNavigation() as MobNavigation
        nav.setCanPathThroughDoors(true)

        setCanPickUpLoot(true)
    }

    protected override fun afterUsing(offer: TradeOffer) {
        world.spawnEntity(
            ExperienceOrbEntity(
                world,
                getX(),
                getY() + 0.5,
                getZ(),
                3 + random.nextInt(4)
            )
        )
    }

    protected override fun fillRecipes() {
        val offers = getOffers();
        if (!offers.isEmpty()) {
            return
        }

        offers.add(
            TradeOffer(
                ItemStack(Items.COAL, 15),
                ItemStack.EMPTY,
                ItemStack(Initializer.RUBY),
                16,
                0,
                1.0f
            )
        )
        offers.add(
            TradeOffer(
                ItemStack(Initializer.RUBY),
                ItemStack.EMPTY,
                ItemStack(Items.COOKED_COD, 15),
                16,
                0,
                1.0f
            )
        )
    }

    override fun createChild(serverWorld: ServerWorld, otherParent: PassiveEntity): PassiveEntity? {
        // TODO
        return null
    }

    override fun interactMob(player: PlayerEntity, hand: Hand): ActionResult {
        val handStack = player.getStackInHand(hand)
        if (handStack.isOf(Items.VILLAGER_SPAWN_EGG) || !isAlive() || hasCustomer() || isBaby()) {
            return super.interactMob(player, hand)
        }
        if (hand == Hand.MAIN_HAND) {
            player.incrementStat(Stats.TALKED_TO_VILLAGER)
        }
        if (!(getOffers().isEmpty() || world.isClient)) {
            setCustomer(player);
            sendOffers(player, getDisplayName(), 1);
        }
        return ActionResult.success(world.isClient);
    }

    protected override fun deserializeBrain(dyn: Dynamic<*>): Brain<out AmurianEntity> {
        val brain = createBrainProfile().deserialize(dyn)
        initBrain(brain)
        return brain
    }

    protected override fun createBrainProfile(): Brain.Profile<out AmurianEntity> {
        return Brain.createProfile(MEMORY_MODULES, SENSORS)
    }

    protected fun initBrain(brain: Brain<out AmurianEntity>) {
        brain.setTaskList(Activity.CORE, AmurianTaskListProvider.createCoreTasks())

        brain.setCoreActivities(ImmutableSet.of(Activity.CORE))
        brain.refreshActivities(world.getTimeOfDay(), world.getTime())
    }

    override fun setAttacker(attacker: LivingEntity?) {
        if (
            attacker != null &&
            canTarget(attacker) &&
            (attacker != getAttacker() || lastDamageTaken > lastActionableDamage)
        ) {
            lastActionableDamage = lastDamageTaken
            setTarget(attacker)
        }
        super.setAttacker(attacker)
    }

    override fun tryEquip(equipment: ItemStack): Boolean {
        val equipmentSlot = getPreferredEquipmentSlot(equipment)
        val itemStack = getEquippedStack(equipmentSlot)
        if (prefersNewEquipment(equipment, itemStack) && canPickupItem(equipment)) {
            if (
                !itemStack.isEmpty() &&
                Math.max(random.nextDouble() - 0.1, 0.0) < getDropChance(equipmentSlot)
            ) {
                dropStack(itemStack)
            }
            equipLootStack(equipmentSlot, equipment)
            return true
        }
        return false
    }

    override fun canEquip(stack: ItemStack): Boolean {
        if (stack.getItem() is ArmorItem) {
            return false
        }
        val equipmentSlot = getPreferredEquipmentSlot(stack)
        val itemStack = getEquippedStack(equipmentSlot)
        return prefersNewEquipment(stack, itemStack)
    }

    protected override fun prefersNewEquipment(newStack: ItemStack, oldStack: ItemStack): Boolean {
        if (oldStack.isEmpty()) {
            return true
        }

        val newItem = newStack.getItem()
        val oldItem = oldStack.getItem()

        if (newItem.isFood() && oldItem.isFood()) {
            // short-circuit; don't check the status effect list for suspicious stew
            if (newItem is SuspiciousStewItem) {
                return false
            }
            if (oldItem is SuspiciousStewItem) {
                return true
            }

            // known to be non-null because of the .isFood() checks
            val newFood = newItem.getFoodComponent()!!
            val oldFood = oldItem.getFoodComponent()!!

            // If only one gives poison, or one is more poisonous, prefer the one with less poison
            val newStatusEffects = newFood.getStatusEffects()
            val oldStatusEffects = oldFood.getStatusEffects()
            // poison amount = poison time * pow(2, poison level), since higher level poison damages
            // faster in that exact way
            // regen 2 = poison 1, but negative
            var newFoodPoisonAmount = 0.0f
            var oldFoodPoisonAmount = 0.0f

            for (p in newStatusEffects) {
                val effect = p.getFirst()
                if (effect.getEffectType() == StatusEffects.POISON) {
                    newFoodPoisonAmount += effect.getDuration().toFloat() *
                        Math.pow(2.0, effect.getAmplifier().toDouble()).toFloat()
                } else if (effect.getEffectType() == StatusEffects.REGENERATION) {
                    newFoodPoisonAmount += -0.5f *
                        effect.getDuration().toFloat() *
                        Math.pow(2.0, effect.getAmplifier().toDouble()).toFloat()
                }
            }
            for (p in oldStatusEffects) {
                val effect = p.getFirst()
                if (effect.getEffectType() == StatusEffects.POISON) {
                    oldFoodPoisonAmount += effect.getDuration().toFloat() *
                        Math.pow(2.0, effect.getAmplifier().toDouble()).toFloat()
                } else if (effect.getEffectType() == StatusEffects.REGENERATION) {
                    oldFoodPoisonAmount += -0.5f *
                        effect.getDuration().toFloat() *
                        Math.pow(2.0, effect.getAmplifier().toDouble()).toFloat()
                }
            }

            if (newFoodPoisonAmount != oldFoodPoisonAmount) {
                return newFoodPoisonAmount < oldFoodPoisonAmount;
            }

            // if there's no poison/regen difference, prefer specific foods
            val newFoodPreferred = newStack.isIn(PREFERRED_FOODS)
            if (newFoodPreferred != oldStack.isIn(PREFERRED_FOODS)) {
                return newFoodPreferred
            }

            // failing that, go by those with higher hunger
            if (newFood.getHunger() != oldFood.getHunger()) {
                return newFood.getHunger() > oldFood.getHunger()
            }

            // if hunger is equal, prefer those eaten quickly
            if (newFood.isSnack() != oldFood.isSnack()) {
                return newFood.isSnack()
            }
        }
        if (newItem is TridentItem) {
            return prefersTrident(newStack, oldStack)
        }
        if (oldItem is TridentItem) {
            return !prefersTrident(oldStack, newStack)
        }
        if (oldItem is CrossbowItem && newItem is BowItem) {
            return true
        }
        if (newItem is MiningToolItem && oldItem is SwordItem) {
            val newDPS = newItem.getAttackDamage() * getAttackSpeed(newItem).toFloat()
            val oldDPS = oldItem.getAttackDamage() * getAttackSpeed(oldItem).toFloat()
            if (newDPS != oldDPS) {
                return newDPS > oldDPS
            }
        }
        if (newItem is SwordItem && oldItem is MiningToolItem) {
            val newDPS = newItem.getAttackDamage() * getAttackSpeed(newItem).toFloat()
            val oldDPS = oldItem.getAttackDamage() * getAttackSpeed(oldItem).toFloat()
            if (newDPS != oldDPS) {
                return newDPS > oldDPS
            }
        }

        return super.prefersNewEquipment(newStack, oldStack)
    }

    private fun prefersTrident(trident: ItemStack, other: ItemStack): Boolean {
        val otherItem = other.getItem()
        if (otherItem is TridentItem) {
            return prefersNewDamageableItem(trident, other)
        }
        if (otherItem is MiningToolItem) {
            val otherDPS = otherItem.getAttackDamage() * getAttackSpeed(otherItem).toFloat()
            if (otherDPS > 9.9f) {
                return false
            }
        }
        if (otherItem is SwordItem) {
            val otherDamage = otherItem.getAttackDamage()
            if (otherDamage > 6.1875f) {
                return false
            }
        }
        return true
    }

    private fun getAttackSpeed(item: Item): Double {
        return item.getAttributeModifiers(EquipmentSlot.MAINHAND)
            .get(EntityAttributes.GENERIC_ATTACK_SPEED)
            .toTypedArray()[0]
            .getValue()
    }

    protected override fun consumeItem() {
        super.consumeItem()
        if (activeItemStack.isFood()) {
            heal(activeItemStack.getItem().getFoodComponent()!!.getHunger().toFloat())
            activeItemStack.decrement(1)
        }
    }

    enum class BiomeGroup {
        COLD,
        MODERATE,
        JUNGLE,
        SAVANNA;

        companion object {
            fun of(biomeEntry: RegistryEntry<Biome>): BiomeGroup {
                // All jungle-like climates, and lush caves, fall into the "jungle" group.
                if (biomeEntry.isIn(JUNGLE_BIOMES)) {
                    return JUNGLE
                }
                // All hot, dry climates fall into the "savannah" group.
                val temperature = biomeEntry.value().getTemperature()
                if (temperature >= 1.0f) {
                    return SAVANNA
                }
                // All biomes cold enough to support spruce trees fall into the "cold" group.
                if (temperature <= 0.3f) {
                    return COLD
                }
                // All other biomes fall into the "moderate" group.
                return MODERATE
            }

            fun fromString(s: String): BiomeGroup = valueOf(s.uppercase())
        }

        override fun toString(): String = name.lowercase()
    }

    enum class Profession {
        UNEMPLOYED,
        FISHERMAN,
        FARMER,
        BUTCHER,
        WEAPONSMITH;

        companion object {
            fun fromString(s: String): Profession = valueOf(s.uppercase())
        }

        override fun toString(): String = name.lowercase()
    }
}
