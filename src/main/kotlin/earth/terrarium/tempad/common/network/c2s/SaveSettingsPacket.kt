package earth.terrarium.tempad.common.network.c2s

import com.teamresourceful.bytecodecs.base.`object`.ObjectByteCodec
import com.teamresourceful.resourcefullib.common.bytecodecs.ExtraByteCodecs
import com.teamresourceful.resourcefullib.common.network.Packet
import com.teamresourceful.resourcefullib.common.network.base.NetworkHandle
import com.teamresourceful.resourcefullib.common.network.base.PacketType
import com.teamresourceful.resourcefullib.common.network.defaults.CodecPacketType
import earth.terrarium.tempad.Tempad.Companion.tempadId
import earth.terrarium.tempad.api.context.ItemContext
import earth.terrarium.tempad.common.data.OrganizationMethod
import earth.terrarium.tempad.common.registries.defaultApp
import earth.terrarium.tempad.common.registries.defaultMacro
import net.minecraft.resources.ResourceLocation

class SaveSettingsPacket(val ctxData: ItemContext, val defaultApp: ResourceLocation, val defaultMacro: ResourceLocation, val organizationMethod: OrganizationMethod) :
    Packet<SaveSettingsPacket> {
    companion object {
        val TYPE = CodecPacketType.Server.create(
            "save_settings".tempadId,
            ObjectByteCodec.create(
                ItemContext.codec.fieldOf(SaveSettingsPacket::ctxData),
                ExtraByteCodecs.RESOURCE_LOCATION.fieldOf(SaveSettingsPacket::defaultApp),
                ExtraByteCodecs.RESOURCE_LOCATION.fieldOf(SaveSettingsPacket::defaultMacro),
                OrganizationMethod.BYTE_CODEC.fieldOf(SaveSettingsPacket::organizationMethod),
                ::SaveSettingsPacket
            ),
            NetworkHandle.handle { packet, player ->
                val tempad = packet.ctxData.getStack(player)
                tempad.defaultApp = packet.defaultApp
                tempad.defaultMacro = packet.defaultMacro
            }
        )
    }

    override fun type(): PacketType<SaveSettingsPacket> = TYPE
}