package me.codexadrian.tempad.common.items;

import me.codexadrian.tempad.api.options.TempadOption;
import me.codexadrian.tempad.api.options.TempadOptionApi;
import me.codexadrian.tempad.client.config.TempadClientConfig;
import me.codexadrian.tempad.common.Tempad;
import me.codexadrian.tempad.common.config.TempadConfig;
import me.codexadrian.tempad.common.data.LocationData;
import me.codexadrian.tempad.common.data.TempadLocationHandler;
import me.codexadrian.tempad.common.entity.TimedoorEntity;
import me.codexadrian.tempad.common.network.NetworkHandler;
import me.codexadrian.tempad.common.network.messages.OpenTempadScreenPacket;
import me.codexadrian.tempad.common.registry.TempadRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TempadItem extends Item implements TempadPower {
    public TempadItem(Properties properties) {
        super(properties);
    }

    public TempadOption getOption() {
        return TempadOptionApi.getOption(TempadConfig.tempadFuelType);
    }

    public int getFuelCost() {
        return TempadConfig.tempadFuelConsumptionValue;
    }

    public int getFuelCapacity() {
        return TempadConfig.tempadFuelCapacityValue;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand interactionHand) {
        ItemStack stack = player.getItemInHand(interactionHand);
        if (!level.isClientSide) {
            OpenTempadScreenPacket packet = new OpenTempadScreenPacket(new ArrayList<>(TempadLocationHandler.getLocations(level, player.getUUID()).values()), interactionHand);
            NetworkHandler.CHANNEL.sendToPlayer(packet, player);
        }
        return InteractionResultHolder.success(stack);
    }

    public static void summonTimeDoor(LocationData locationData, Player player, int color) {
        TimedoorEntity timedoor = new TimedoorEntity(TempadRegistry.TIMEDOOR_ENTITY.get(), player.level());
        var dir = player.getDirection();
        timedoor.setColor(color);
        timedoor.setLocation(locationData);
        timedoor.setOwner(player.getUUID());
        var position = player.position();
        timedoor.setPos(position.x() + dir.getStepX() * TempadConfig.distanceFromPlayer, position.y(), position.z() + dir.getStepZ() * TempadConfig.distanceFromPlayer);
        timedoor.setYRot(dir.getOpposite().toYRot());
        player.level().addFreshEntity(timedoor);
        timedoor.playSound(Tempad.TIMEDOOR_SOUND.get());
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> components, @NotNull TooltipFlag flag) {
        this.getOption().addToolTip(stack, level, components, flag);
    }

    @Override
    public int getBarColor(@NotNull ItemStack $$0) {
        return TempadClientConfig.color;
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return this.getOption().isDurabilityBarVisible(stack);
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        return this.getOption().durabilityBarWidth(stack);
    }
}