package by.srinater.compatible.dragon_survival.gameplay_modifiers.compatible;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.provider.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.network.NetworkHandler;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SyncFlyingStatus;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;

import static by.srinater.compatible.dragon_survival.gameplay_modifiers.GameplayModifiers.LOGGER;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class DragonStateHandlerApis {
    private static Class<DragonStateProvider> dragonStateProviderClass = null;
    @SubscribeEvent
    public static void onSetup(FMLCommonSetupEvent event)
    {
        try {
            dragonStateProviderClass = (Class<DragonStateProvider>) Class.forName("by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider");
            LOGGER.info("Found mod [Dragon Survival].");
        } catch (ClassNotFoundException e) {
            LOGGER.warn("Dragon survival mod not found.");
        }
    }
    public static boolean haveDragonSurvival()
    {
        return dragonStateProviderClass != null;
    }
    public static void setWingsSpread(Player player, boolean spread)
    {
        if (!haveDragonSurvival())
            return;
        DragonStateProvider.getCap(player).ifPresent(dragonStateHandler -> {
            dragonStateHandler.setWingsSpread(spread);
            if (!player.level.isClientSide)
                NetworkHandler.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new SyncFlyingStatus(player.getId(), spread));
        });
    }
    public static void setHasWings(Player player, boolean has)
    {
        if (!haveDragonSurvival())
            return;
        DragonStateProvider.getCap(player).ifPresent(dragonStateHandler -> {
            dragonStateHandler.setHasWings(has);
        });
    }

}
