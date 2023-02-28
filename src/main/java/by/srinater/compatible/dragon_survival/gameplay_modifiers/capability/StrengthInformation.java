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
    public double strength = 100;
    public final double MaxStrength = 100;
    public double multiplier = 1;
    public boolean falling = false;

    @Override
    public void encode(StrengthInformation strengthInformation, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeDouble(strengthInformation.strength);
        friendlyByteBuf.writeDouble(strengthInformation.multiplier);
        friendlyByteBuf.writeBoolean(strengthInformation.falling);
    }
    @Override
    public StrengthInformation decode(FriendlyByteBuf friendlyByteBuf) {
        this.strength = friendlyByteBuf.readDouble();
        this.multiplier = friendlyByteBuf.readDouble();
        this.falling = friendlyByteBuf.readBoolean();
        return this;
    }
    @Override
    public void handle(StrengthInformation strengthInformation, Supplier<NetworkEvent.Context> supplier) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> (DistExecutor.SafeRunnable)() -> {
            run(strengthInformation, supplier);
        });
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
        tag.putDouble("stamina", information.strength);
        tag.putDouble("stamina_multiplier", information.multiplier);
        tag.putBoolean("falling", information.falling);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        deserializeNBT(this, nbt);
    }
    public void copyFrom(StrengthInformation information){
        this.strength = information.strength;
        this.multiplier = information.multiplier;
        this.falling = information.falling;
    }
    public static void deserializeNBT(StrengthInformation information,CompoundTag nbt) {
        information.strength = nbt.getDouble("stamina");
        information.multiplier = nbt.getDouble("stamina_multiplier");
        information.falling = nbt.getBoolean("falling");
    }
    public static LazyOptional<StrengthInformation> getCap(Entity entity)
    {
        return entity.getCapability(STRENGTH_CAPABILITY);
    }

    public boolean isTired()
    {
        return strength / (MaxStrength * multiplier) < 0.2;
    }

    public double maxStrength()
    {
        return MaxStrength * multiplier;
    }
}
