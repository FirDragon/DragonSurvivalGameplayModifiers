package by.srinater.compatible.dragon_survival.gameplay_modifiers.capability;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.srinater.compatible.dragon_survival.gameplay_modifiers.GameplayModifiers;
import by.srinater.compatible.dragon_survival.gameplay_modifiers.compatible.DragonStateHandlerApis;
import by.srinater.compatible.dragon_survival.gameplay_modifiers.network.Register;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import static by.srinater.compatible.dragon_survival.gameplay_modifiers.GameplayModifiers.LOGGER;

@Mod.EventBusSubscriber
public class StrengthConsumption {

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event){
        if (!(event.getObject() instanceof Player))
            return;
        LOGGER.info("Attach capabilities.");
        StrengthInformation provider = new StrengthInformation();
        event.addCapability(new ResourceLocation("strength", "strength_state"), provider);
    }
    public static void syncPlayerInformation(ServerPlayer player)
    {
        StrengthInformation.getCap(player).ifPresent(
            information -> {
                Register.CHANNEL.send(PacketDistributor.PLAYER.with(()-> player), information);
            }
        );
    }
    @SubscribeEvent
    public static void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event){
        if (event.getPlayer() instanceof ServerPlayer player)
            syncPlayerInformation(player);
    }
    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event)
    {
        if (event.getPlayer() instanceof ServerPlayer player)
            syncPlayerInformation(player);
    }
    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        Player player = event.getPlayer();
        Player original = event.getOriginal();
        original.reviveCaps();
        StrengthInformation.getCap(player).ifPresent(
            newInformation -> StrengthInformation.getCap(original).ifPresent(
                newInformation::copyFrom
            )
        );
        original.invalidateCaps();
    }
    public static boolean IsLanding(Player player)
    {
        return player.isOnGround() || player.isInLava() || player.isInWaterRainOrBubble();
    }
    private static double CalculateStrength(boolean isFlight, boolean isSprinting, Vec3 movement)
    {
        double verticalSpeed = movement.y;
        if (movement.y < -0.25)
            verticalSpeed = -0.25;

        // 固定消耗为0.1
        double strengthFloat = 0.1;
        // 飞行过程中
        if (isFlight) {
            // 计算爬升高度与体力消耗
            if (movement.length() == 0)
                strengthFloat += 0.5;
            else
                strengthFloat += (verticalSpeed + 0.25) / (movement.length());
            // 悬停追加消耗
            if (!isSprinting)
                strengthFloat += 2;
            //疾跑消耗
        }else if(isSprinting){
            strengthFloat = 1;
        }else // 休息时的消耗（补充体力）
            strengthFloat = -1;
        return strengthFloat;
    }
    public static void onHumansTick(ServerPlayer player)
    {
        StrengthInformation.getCap(player).ifPresent(
            strengthInfo -> {
                strengthInfo.strength -= CalculateStrength(false, player.isSprinting(), player.getDeltaMovement());
                if (strengthInfo.strength < 0)
                {
                    strengthInfo.strength = 0;
                    player.setSprinting(false);
                    player.sendMessage(new TranslatableComponent("tips.exhausted"), GameplayModifiers.TIPS_UUID);
                }else if(strengthInfo.strength > strengthInfo.maxStrength()){
                    strengthInfo.strength = strengthInfo.maxStrength();
                }
            }
        );
    }
    public static void onDragonTick(ServerPlayer player)
    {
        DragonStateProvider.getCap(player).ifPresent(
            dragonStateHandler -> StrengthInformation.getCap(player).ifPresent(
                strength -> {
                    boolean isLanding = IsLanding(player);
                    if (strength.falling)
                    {
                        if (isLanding) {
                            strength.falling = false;
                            player.removeEffect(MobEffects.SLOW_FALLING);
                        }else
                            return;
                    }
                    boolean isFlight = DragonStateHandlerApis.IsFlight(dragonStateHandler) && !isLanding;
                    Vec3 movement = player.getDeltaMovement();
                    strength.strength -= CalculateStrength(isFlight, player.isSprinting(), movement);
                    if (strength.strength < 0) {
                        strength.strength = 0;
                        DragonStateHandlerApis.setWingsSpread(player, false);
                        strength.falling = true;
                        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 15 * 20, 0, false, false, true));
                        player.sendMessage(new TranslatableComponent("tips.exhausted"), GameplayModifiers.TIPS_UUID);
                    }else if(strength.strength > strength.maxStrength())
                        strength.strength = strength.maxStrength();
                }
            )
        );
    }
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent tickEvent)
    {
        if (tickEvent.phase != TickEvent.Phase.END)
            return;
        if (!(tickEvent.player instanceof ServerPlayer serverPlayer))
            return;
        if (tickEvent.player.tickCount % 5 != 0)
            return;
        if (tickEvent.player.isCreative())
            return;
        if (!DragonStateHandlerApis.haveDragonSurvival() || !DragonUtils.isDragon(serverPlayer))
            onHumansTick(serverPlayer);
        else{
            onDragonTick(serverPlayer);
        }
        syncPlayerInformation(serverPlayer);
    }
}
