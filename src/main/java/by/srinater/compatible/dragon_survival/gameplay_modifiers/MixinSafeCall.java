package by.srinater.compatible.dragon_survival.gameplay_modifiers;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class MixinSafeCall {
    public static Player getClientPlayer()
    {
        return Minecraft.getInstance().player;
    }
}
