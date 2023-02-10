package by.srinater.compatible.dragon_survival.gameplay_modifiers.network;

import by.srinater.compatible.dragon_survival.gameplay_modifiers.GameplayModifiers;
import by.srinater.compatible.dragon_survival.gameplay_modifiers.capability.StrengthInformation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class Register {
    public static final String PROTOCOL_VERSION = "1.0";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation(GameplayModifiers.MODID, "main"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
    static int PacketCount = 0;
    public static <T> void register(Class<T> clazz, IMessage<T> message){
        CHANNEL.registerMessage(PacketCount++, clazz, message::encode, message::decode, message::handle);
    }
    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent event)
    {
        register(StrengthInformation.class, new StrengthInformation());
    }

}
