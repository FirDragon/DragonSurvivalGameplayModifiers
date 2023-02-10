package by.srinater.compatible.dragon_survival.gameplay_modifiers.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public interface IMessage<T> {
    void encode(T var1, FriendlyByteBuf var2);

    T decode(FriendlyByteBuf var1);

    void handle(T var1, Supplier<NetworkEvent.Context> var2);
}