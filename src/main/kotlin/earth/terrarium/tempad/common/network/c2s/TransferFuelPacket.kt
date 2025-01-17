package earth.terrarium.tempad.common.network.c2s

import com.teamresourceful.resourcefullib.common.network.Packet
import com.teamresourceful.resourcefullib.common.network.base.NetworkHandle
import com.teamresourceful.resourcefullib.common.network.base.PacketType
import com.teamresourceful.resourcefullib.common.network.defaults.CodecPacketType
import earth.terrarium.tempad.Tempad.Companion.tempadId
import earth.terrarium.tempad.api.context.ItemContext
import earth.terrarium.tempad.api.fuel.FuelHandler
import earth.terrarium.tempad.common.menu.FuelMenu
import earth.terrarium.tempad.common.utils.get

class TransferFuelPacket(val ctxData: ItemContext): Packet<TransferFuelPacket> {
    companion object {
        val TYPE = CodecPacketType.Server.create(
            "transfer_fuel".tempadId,
            ItemContext.codec.map(::TransferFuelPacket, TransferFuelPacket::ctxData),
            NetworkHandle.handle { packet, player ->
                val menu = player.containerMenu
                if (menu is FuelMenu) {
                    FuelHandler.moveCharge(packet.ctxData.getStack(player), menu.container[1])
                }
            }
        )
    }

    override fun type(): PacketType<TransferFuelPacket> = TYPE
}