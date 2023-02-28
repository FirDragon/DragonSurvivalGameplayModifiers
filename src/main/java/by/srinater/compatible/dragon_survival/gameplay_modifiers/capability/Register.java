package by.srinater.compatible.dragon_survival.gameplay_modifiers.capability;

import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static by.srinater.compatible.dragon_survival.gameplay_modifiers.GameplayModifiers.LOGGER;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class Register {
    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent ev){
        LOGGER.info("Register capabilities.");
        ev.register(StrengthInformation.class);
    }
}
