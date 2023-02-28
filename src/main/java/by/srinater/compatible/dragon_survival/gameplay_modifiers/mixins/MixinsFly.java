package by.srinater.compatible.dragon_survival.gameplay_modifiers.mixins;

import by.dragonsurvivalteam.dragonsurvival.network.flight.SyncFlyingStatus;
import by.srinater.compatible.dragon_survival.gameplay_modifiers.GameplayModifiers;
import by.srinater.compatible.dragon_survival.gameplay_modifiers.MixinSafeCall;
import by.srinater.compatible.dragon_survival.gameplay_modifiers.capability.StrengthInformation;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(SyncFlyingStatus.class)
public class MixinsFly {
    @Inject( at = @At( "HEAD" ), method = "handle(Lby/dragonsurvivalteam/dragonsurvival/network/flight/SyncFlyingStatus;Ljava/util/function/Supplier;)V")
    private void MixinSyncFlyingStatusHandle(SyncFlyingStatus message, Supplier<NetworkEvent.Context> supplier, CallbackInfo ci)
    {
        Player sender;
        if(supplier.get().getDirection() != NetworkDirection.PLAY_TO_SERVER)
            sender = MixinSafeCall.getClientPlayer();
        else
            sender = supplier.get().getSender();

        if(sender == null)
            return;

        if (sender.isCreative())
            return;

        Player finalSender = sender;
        StrengthInformation.getCap(sender).ifPresent(
            strength->{
                if(strength.falling || strength.isTired()) {
                    message.state = false;
                    if (finalSender instanceof ServerPlayer player)
                        player.sendMessage(new TranslatableComponent("tips.exhausted"), GameplayModifiers.TIPS_UUID);
                }
            }
        );
    }
}
