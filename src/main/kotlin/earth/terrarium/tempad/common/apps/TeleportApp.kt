package earth.terrarium.tempad.common.apps

import com.teamresourceful.bytecodecs.base.ByteCodec
import com.teamresourceful.bytecodecs.base.`object`.ObjectByteCodec
import com.teamresourceful.resourcefullib.common.bytecodecs.ExtraByteCodecs
import earth.terrarium.tempad.api.locations.ProviderSettings
import earth.terrarium.tempad.api.locations.TempadLocations
import earth.terrarium.tempad.api.app.TempadApp
import earth.terrarium.tempad.api.context.ContextInstance
import earth.terrarium.tempad.api.context.ItemContext
import earth.terrarium.tempad.api.locations.LocationData
import earth.terrarium.tempad.common.data.FavoriteLocationAttachment
import earth.terrarium.tempad.common.registries.ModMenus
import earth.terrarium.tempad.common.registries.pinnedLocationData
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import java.util.*
import kotlin.jvm.optionals.getOrNull

data class TeleportApp(val ctx: ContextInstance): TempadApp<TeleportData> {
    override fun createMenu(pContainerId: Int, inventory: Inventory, player: Player): AbstractContainerMenu {
        return ModMenus.TeleportMenu(
            pContainerId,
            inventory,
            Optional.of(createContent())
        )
    }

    override fun getDisplayName(): Component = Component.translatable("app.tempad.teleport")

    private fun createContent() = createContent(ctx.player as ServerPlayer)

    override fun createContent(player: ServerPlayer): TeleportData = TeleportData(TempadLocations[ctx], player.pinnedLocationData, ctx.ctx)

    override fun isEnabled(player: Player): Boolean = true
}

class TeleportData(val locations: Map<ProviderSettings, Map<UUID, LocationData>>, val favoriteLocation: FavoriteLocationAttachment?, ctx: ItemContext): AppContent<TeleportData>(ctx, codec) {
    constructor(locations: Map<ProviderSettings, Map<UUID, LocationData>>, fav: Optional<FavoriteLocationAttachment>, ctx: ItemContext): this(locations, fav.getOrNull(), ctx)
    companion object {
        val codec: ByteCodec<TeleportData> = ObjectByteCodec.create(
            ByteCodec.mapOf(
                ProviderSettings.BYTE_CODEC,
                ByteCodec.mapOf(
                    ByteCodec.UUID,
                    LocationData.BYTE_CODEC
                )
            ).fieldOf { it.locations },
            ObjectByteCodec.create(
                ExtraByteCodecs.RESOURCE_LOCATION.fieldOf { it.providerId },
                ByteCodec.UUID.fieldOf { it.locationId },
                ::FavoriteLocationAttachment
            ).optionalFieldOf { Optional.ofNullable(it.favoriteLocation) },
            ItemContext.codec.fieldOf { it.ctx },
            ::TeleportData
        )
    }
}
