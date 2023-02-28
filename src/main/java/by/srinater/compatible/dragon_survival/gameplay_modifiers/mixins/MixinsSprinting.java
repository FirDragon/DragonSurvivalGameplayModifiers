package by.srinater.compatible.dragon_survival.gameplay_modifiers.mixins;

import by.srinater.compatible.dragon_survival.gameplay_modifiers.GameplayModifiers;
import by.srinater.compatible.dragon_survival.gameplay_modifiers.capability.StrengthInformation;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MixinsSprinting extends net.minecraftforge.common.capabilities.CapabilityProvider<Entity>{

    protected MixinsSprinting(Class<Entity> baseClass) {
        super(baseClass);
    }

    @Inject( at = @At( "HEAD" ), method = "setSprinting", cancellable = true)
    public void MixinSetSprinting(boolean sprinting, CallbackInfo ci)
    {
        if (!((LivingEntity)(net.minecraftforge.common.capabilities.CapabilityProvider<Entity>)this instanceof Player player))
            return;
        StrengthInformation.getCap(player).ifPresent(
            strength->{
                if((strength.falling || strength.isTired()) && sprinting) {
                    ci.cancel();
                    if (player instanceof ServerPlayer)
                        player.sendMessage(new TranslatableComponent("tips.exhausted"), GameplayModifiers.TIPS_UUID);
                }
            }
        );
    }
}
