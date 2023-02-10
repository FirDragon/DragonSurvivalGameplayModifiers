package by.srinater.compatible.dragon_survival.gameplay_modifiers.capability;

import by.srinater.compatible.dragon_survival.gameplay_modifiers.network.IMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class StrengthInformation implements ICapabilitySerializable<CompoundTag>, IMessage<StrengthInformation> {
    public static Capability<StrengthInformation> STRENGTH_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    private final LazyOptional<StrengthInformation> instance = LazyOptional.of(() -> this);
    public double stamina = 500;
    public double maxStamina = 500;
    public boolean isFly = false;
    public double prevY = 0.0;
    public boolean hasWing = true;

    @Override
    public void encode(StrengthInformation strengthInformation, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeDouble(strengthInformation.stamina);
        friendlyByteBuf.writeDouble(strengthInformation.maxStamina);
    }
    @Override
    public StrengthInformation decode(FriendlyByteBuf friendlyByteBuf) {
        this.stamina = friendlyByteBuf.readDouble();
        this.maxStamina = friendlyByteBuf.readDouble();
        return this;
    }
    @Override
    public void handle(StrengthInformation strengthInformation, Supplier<NetworkEvent.Context> supplier) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> (DistExecutor.SafeRunnable)() -> run(strengthInformation, supplier));
    }
    @OnlyIn( Dist.CLIENT )
    public void run(StrengthInformation information, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            Player thisPlayer = Minecraft.getInstance().player;
            if(thisPlayer == null)
                return;

            StrengthInformation.getCap(thisPlayer).ifPresent(
                strengthInformation -> strengthInformation.copyFrom(information)
            );
        });
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == STRENGTH_CAPABILITY ? instance.cast() : LazyOptional.empty();
    }

    public CompoundTag serializeNBT() {
        return serializeNBT(this);
    }
    public static CompoundTag serializeNBT(StrengthInformation information) {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("stamina", information.stamina);
        tag.putDouble("maxStamina", information.maxStamina);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        deserializeNBT(this, nbt);
    }
    public void copyFrom(StrengthInformation information){
        this.stamina = information.stamina;
        this.maxStamina = information.maxStamina;
    }
    public static void deserializeNBT(StrengthInformation information,CompoundTag nbt) {
        information.stamina = nbt.getDouble("stamina");
        information.maxStamina = nbt.getDouble("maxStamina");
    }
    public static LazyOptional<StrengthInformation> getCap(Entity entity)
    {
        return entity.getCapability(STRENGTH_CAPABILITY);
    }
}
