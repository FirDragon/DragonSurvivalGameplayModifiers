package by.srinater.compatible.dragon_survival.gameplay_modifiers.render;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.srinater.compatible.dragon_survival.gameplay_modifiers.capability.StrengthConsumption;
import by.srinater.compatible.dragon_survival.gameplay_modifiers.capability.StrengthInformation;
import by.srinater.compatible.dragon_survival.gameplay_modifiers.compatible.DragonStateHandlerApis;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.atomic.AtomicInteger;

import static by.srinater.compatible.dragon_survival.gameplay_modifiers.GameplayModifiers.LOGGER;
import static by.srinater.compatible.dragon_survival.gameplay_modifiers.GameplayModifiers.MODID;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class StrengthSlot {
    private static final ResourceLocation STRENGTH_ICONS = new ResourceLocation(MODID + ":textures/gui/dragon_hud.png");
    private static int renderRightHeight = 0;
    private static final int BarIndex = 0;
    private static final int SlotIndex = 1;
    private static final int MovementIndex = 2;
    private static final int ImageWidth = 70;
    private static final int ImageHeight = 24;
    public static void renderStrengthSlot(ForgeIngameGui gui, PoseStack poseStack, float partialTick, int width, int height)
    {
        renderRightHeight = gui.right_height;
        gui.right_height += 10;
        RenderSystem.setShaderTexture(0, STRENGTH_ICONS);
        Screen.blit(poseStack, width / 2 + 20, height - renderRightHeight, 0, 8 * SlotIndex, ImageWidth, 8,ImageWidth, ImageHeight);
        RenderSystem.setShaderTexture(0, Gui.GUI_ICONS_LOCATION);
    }
    public static int tick = 0;
    public static void OutputInTick(String data){
        if (tick % 20 == 0)
        {
            LOGGER.info(data);
        }
        ++tick;
    }
    @SubscribeEvent
    public static void renderStrengthBar(RenderGameOverlayEvent.Post event)
    {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL)
            return;

        Player player = Minecraft.getInstance().player;
        if (player == null)
            return;
        StrengthInformation.getCap(player).ifPresent(
            strengthInformation -> {
                Window window = Minecraft.getInstance().getWindow();
                int width = window.getGuiScaledWidth();
                int height = window.getGuiScaledHeight();
                int left = width / 2;
                int top = height - renderRightHeight;
                AtomicInteger movement = new AtomicInteger(0);
                if (DragonStateHandlerApis.haveDragonSurvival() && DragonUtils.isDragon(player)) {
                    DragonStateProvider.getCap(player).ifPresent(
                        dragonStateHandler -> {
                            movement.set(
                                DragonStateHandlerApis.IsFlight(dragonStateHandler)
                                && !StrengthConsumption.IsLanding(player)
                                    ? 1 : 0
                            );
                        }
                    );
                }else
                    movement.set(player.isSprinting()?3:2);
                event.getMatrixStack().pushPose();
                RenderSystem.setShaderTexture(0, STRENGTH_ICONS);
                Screen.blit(event.getMatrixStack(), left + 10, top, movement.get() * 8, 8 * MovementIndex, 8, 8, ImageWidth, ImageHeight);
                Screen.blit(event.getMatrixStack(), left + 20, top, 0, 8 * BarIndex, (int)(ImageWidth * (strengthInformation.strength / (strengthInformation.multiplier * strengthInformation.MaxStrength))), 8, ImageWidth, ImageHeight);
                event.getMatrixStack().popPose();
            }
        );
    }
}
