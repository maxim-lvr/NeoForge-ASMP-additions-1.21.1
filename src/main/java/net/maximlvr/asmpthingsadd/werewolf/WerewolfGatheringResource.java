package net.maximlvr.asmpthingsadd.werewolf;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public enum WerewolfGatheringResource {
    WOOD("bois", Items.STICK),
    COAL("charbon", Items.COAL),
    IRON("fer", Items.IRON_INGOT);

    private final String displayName;
    private final Item reward;

    WerewolfGatheringResource(String displayName, Item reward) {
        this.displayName = displayName;
        this.reward = reward;
    }

    public String displayName() {
        return displayName;
    }

    public Item reward() {
        return reward;
    }
}
