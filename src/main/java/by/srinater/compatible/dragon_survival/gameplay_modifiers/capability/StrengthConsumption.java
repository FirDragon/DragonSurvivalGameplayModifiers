package by.srinater.compatible.dragon_survival.gameplay_modifiers.capability;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.srinater.compatible.dragon_survival.gameplay_modifiers.compatible.DragonStateHandlerApis;
import by.srinater.compatible.dragon_survival.gameplay_modifiers.network.Register;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
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
        if (!(event.getObject() instanceof Player player))
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
    public static void onHumansTick(Player player)
    {
        StrengthInformation.getCap(player).ifPresent(
            strengthInformation -> {
                if (player.isSprinting())
                {
                    if (strengthInformation.stamina > 0)
                        --strengthInformation.stamina;
                    else
                        player.setSprinting(false);
                }else {
                    if (strengthInformation.stamina < strengthInformation.maxStamina)
                        ++strengthInformation.stamina;
                }
            }
        );
    }
    public static boolean IsTired(StrengthInformation strength)
    {
        return (double)strength.stamina / strength.maxStamina < 0.2;
    }
    public static boolean IsFlight(Player player, Object dragonStateHandler)
    {
        DragonStateHandler _dragonStateHandler = (DragonStateHandler)dragonStateHandler;
        return _dragonStateHandler.hasWings() && _dragonStateHandler.isWingsSpread() && !player.isOnGround();
    }
    private static double CalculateStrength(boolean isSprinting, Vec3 movement)
    {
        double horizontalSpeed = movement.y;
        if (movement.y < -0.25)
            horizontalSpeed = -0.25;

        return (horizontalSpeed + 0.25) / (movement.length()) + 0.1 + (isSprinting ? 0 : 2);
    }
    public static void onDragonTick(Player player)
    {
        DragonStateProvider.getCap(player).ifPresent(
            dragonStateHandler -> StrengthInformation.getCap(player).ifPresent(
                strength -> {
                    // 取消飞行时体力不足将一段时间无法打开翅膀
                    if (strength.isFly && !IsFlight(player, dragonStateHandler) && IsTired(strength))
                    {
                        DragonStateHandlerApis.setHasWings(player, false);
                        DragonStateHandlerApis.setWingsSpread(player, false);
                    }
                    // 没在飞行时体力恢复后可再次飞行
                    if (!dragonStateHandler.hasWings() && !IsFlight(player, dragonStateHandler) && !IsTired(strength))
                        DragonStateHandlerApis.setHasWings(player, true);

                    // 在飞行时体力消耗，没在飞行时体力恢复
                    if (IsFlight(player, dragonStateHandler))
                    {
                        if (strength.stamina > 0)
                        {
                            Vec3 movement = player.getDeltaMovement();
                            strength.stamina -= CalculateStrength(player.isSprinting(), movement);
                        }
                        else {
                            DragonStateHandlerApis.setHasWings(player, false);
                            DragonStateHandlerApis.setWingsSpread(player, false);
                        }
                    }else {
                        if (strength.stamina < strength.maxStamina)
                            ++strength.stamina;
                    }
                    strength.isFly = IsFlight(player, dragonStateHandler);
                }
            )
        );

    }
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent tickEvent)
    {
        if (tickEvent.phase != TickEvent.Phase.END)
            return;

        if (!DragonStateHandlerApis.haveDragonSurvival() || !DragonUtils.isDragon(tickEvent.player))
            onHumansTick(tickEvent.player);
        else{
            onDragonTick(tickEvent.player);
        }
    }
}
