package cc.astralis.util.player;

import cc.astralis.interfaces.IAccess;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.ExperienceBottleItem;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.equipment.Equippable;
import javax.tools.Tool;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class InventoryUtil implements IAccess {

    public static ToolType getToolType(ItemStack itemStack) {
        Item item = itemStack.getItem();
        net.minecraft.world.item.component.Tool tool = itemStack.get(DataComponents.TOOL);
        if (tool == null || tool.rules().stream().noneMatch(rule -> rule.correctForDrops().isPresent() && rule.correctForDrops().get())) return null;
        Weapon weapon = itemStack.get(DataComponents.WEAPON);
        if (item instanceof AxeItem) {
            return ToolType.Axe;
        } else if (item instanceof HoeItem) {
            return ToolType.Hoe;
        } else if (item instanceof ShovelItem) {
            return ToolType.Shovel;
        } else if (item instanceof ShearsItem) {
            return ToolType.Shears;
        } else if (weapon != null) {
            int i = weapon.itemDamagePerAttack();
            if (i == 2) {
                return ToolType.Pickaxe;
            } else if (i == 1) {
                return ToolType.Sword;
            }
        }
        return null;
    }

    public static boolean isArmor(ItemStack itemStack) {
        Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
        return equippable != null && equippable.slot().getType() == EquipmentSlot.Type.HUMANOID_ARMOR;
    }

    public static int getArmor(ItemStack itemStack) {
        int armor = 0;

        for (ItemAttributeModifiers.Entry entry : itemStack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY).modifiers()) {
            if (entry.attribute() == Attributes.ARMOR && entry.modifier().operation() == AttributeModifier.Operation.ADD_VALUE) {
                armor += (int) entry.modifier().amount();
                break;
            }
        }

        return armor;
    }

    public static float calculateMitigatedDamage(ItemStack itemStack) {
        float damage = 20F * (25 - getArmor(itemStack)) / 25F;
        int p = 0;
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : itemStack.getEnchantments().entrySet()) {
            if (entry.getKey().is(Enchantments.PROTECTION)) {
                int i = entry.getIntValue();
                float f = (6 + i * i) / 3F * 0.75F;
                int j = (int) f;
                if (f < j) j -= 1;
                p += j;
            }
        }
        if (p > 25) p = 25;
        else if (p < 0) p = 0;
        p = p + 1 >> 1;
        if (p > 0) damage = damage * (25 - p) / 25F;
        return damage;
    }

    public static float calculateDestroySpeed(ItemStack itemStack) {
        float speed = Optional.ofNullable(itemStack.get(DataComponents.TOOL)).map(tool -> tool.rules().stream().filter(rule -> rule.correctForDrops().isPresent() && rule.correctForDrops().get() && rule.speed().isPresent()).map(rule -> rule.speed().get()).findAny().orElse(0F)).orElse(0F);
        if (speed > 1) {
            Optional<Integer> level = itemStack.getEnchantments().entrySet().stream().filter(holderEntry -> holderEntry.getKey().is(Enchantments.EFFICIENCY)).map(Object2IntMap.Entry::getIntValue).max(Comparator.comparingInt(value -> value));
            if (level.isPresent()) speed += Mth.square(level.get()) + 1;
        }
        return speed;
    }

    public static double calculateAttackDamage(ItemStack itemStack) {
        float f = 0.0F;
        for (ItemAttributeModifiers.Entry entry : itemStack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY).modifiers()) {
            if (entry.attribute() == Attributes.ATTACK_DAMAGE && entry.slot() == EquipmentSlotGroup.MAINHAND && entry.modifier().id() == Item.BASE_ATTACK_DAMAGE_ID &&
                    entry.modifier().operation() == AttributeModifier.Operation.ADD_VALUE) {
                f = (float) entry.modifier().amount();
                break;
            }
        }
        float f1 = 0.0F;
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : itemStack.getEnchantments().entrySet()) {
            if (entry.getKey().is(Enchantments.SHARPNESS)) {
                f1 = entry.getIntValue() * 1.25F;
                break;
            }
        }
        return f + f1;
    }

    public static boolean shouldReplaceItem(Slot slot, Slot slotO) {
        ItemStack itemStack = slot.getItem(), itemStackO = slotO.getItem();
        int damage = itemStack.getDamageValue(), damageO = itemStackO.getDamageValue();
        boolean damageEquals = damage == damageO;
        ToolType toolType = getToolType(itemStack), toolTypeO = getToolType(itemStackO);
        if (toolType == ToolType.Sword && toolTypeO == ToolType.Sword) {
            double[] attackDamage = {
                    calculateAttackDamage(itemStack),
                    calculateAttackDamage(itemStackO)
            };
            if (attackDamage[0] != attackDamage[1]) return attackDamage[0] > attackDamage[1];
            if (damageEquals && slot.index == 36) return true;
        }
        if (isArmor(itemStack) && isArmor(itemStackO)) {
            double[] mitigatedDamage = {
                    calculateMitigatedDamage(itemStack),
                    calculateMitigatedDamage(itemStackO)
            };
            if (mitigatedDamage[0] != mitigatedDamage[1]) return mitigatedDamage[0] < mitigatedDamage[1];
            if (damageEquals && slot.index < 9) return true;
        }
        if (toolType != null && toolTypeO != null) {
            if (toolType.isDigger() && toolTypeO.isDigger()) {
                double[] destroySpeed = {
                        calculateDestroySpeed(itemStack),
                        calculateDestroySpeed(itemStackO)
                };
                if (destroySpeed[0] != destroySpeed[1]) return destroySpeed[0] > destroySpeed[1];
                if (damageEquals && slot.index == 37 + toolType.getDiggerIndex()) return true;
            }
        }
        return damage < damageO;
    }

    public static Info sort(List<Slot> slots) {
        List<Slot> slotsToThrow = new ArrayList<>();
        Slot[] slotSword = new Slot[1];
        Slot[] slotsArmor = new Slot[4];
        Slot[] slotsTool = new Slot[3];
        slots.stream().filter(Slot::hasItem).forEach(slot -> {
            ItemStack itemStack = slot.getItem();
            Item item = itemStack.getItem();
            ToolType toolType = getToolType(itemStack);

            if (toolType == ToolType.Sword) {
                if (slotSword[0] == null || shouldReplaceItem(slot, slotSword[0])) {
                    if (slotSword[0] != null) slotsToThrow.add(slotSword[0]);
                    slotSword[0] = slot;
                } else slotsToThrow.add(slot);
            }

            if (isArmor(itemStack)) {
                Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
                int index = equippable != null ? equippable.slot().getIndex() : -1;
                Slot slotArmor = slotsArmor[index];

                if (slotArmor == null || shouldReplaceItem(slot, slotArmor)) {
                    if (slotArmor != null) slotsToThrow.add(slotArmor);
                    slotsArmor[index] = slot;
                } else slotsToThrow.add(slot);
            }

            if (toolType != null && toolType.isDigger()) {
                int index = toolType.getDiggerIndex();

                if (index >= 0) {
                    Slot slotTool = slotsTool[index];

                    if (slotTool == null || shouldReplaceItem(slot, slotTool)) {
                        if (slotTool != null) slotsToThrow.add(slotTool);
                        slotsTool[index] = slot;
                    } else slotsToThrow.add(slot);
                } else slotsToThrow.add(slot);
            }

            if (/*item instanceof FishingRodItem ||
                    item instanceof SnowballItem ||
                    item instanceof EggItem || */
                    item instanceof SpawnEggItem ||
                    item instanceof FlintAndSteelItem ||
                    item instanceof ExperienceBottleItem ||
                    item instanceof CompassItem ||
                    item instanceof BucketItem ||
                    item == Items.BOWL) {
                slotsToThrow.add(slot);
            }
        });
        return new Info(slotsToThrow, slotSword, slotsArmor, slotsTool);
    }

    public static void click(int index, int button, ClickType type) {
        if (mc.gameMode == null || mc.player == null) return;
        mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, index, button, type, mc.player);
    }

    public static Slot findBestBlocks() {
        if (mc.player == null) return null;
        
        Slot bestBlock = null;
        int highestCount = 0;

        for (Slot slot : mc.player.containerMenu.slots) {
            if (!slot.hasItem()) continue;
            if (slot.index < 9) continue; // Skip armor slots
            
            if (slot.getItem().getItem() instanceof BlockItem) {
                int count = slot.getItem().getCount();
                if (count > highestCount) {
                    highestCount = count;
                    bestBlock = slot;
                }
            }
        }

        return bestBlock;
    }

    public static Slot findGapples() {
        if (mc.player == null) return null;
        
        Slot enchantedGapple = null;
        Slot regularGapple = null;

        for (Slot slot : mc.player.containerMenu.slots) {
            if (!slot.hasItem()) continue;
            if (slot.index < 9) continue; // Skip armor slots
            
            if (slot.getItem().getItem() == Items.ENCHANTED_GOLDEN_APPLE) {
                enchantedGapple = slot;
            } else if (slot.getItem().getItem() == Items.GOLDEN_APPLE && regularGapple == null) {
                regularGapple = slot;
            }
        }
        return enchantedGapple != null ? enchantedGapple : regularGapple;
    }

    public record Info(List<Slot> slotsToThrow, Slot[] slotWeapon, Slot[] slotsArmor, Slot[] slotsTool) {}
}
