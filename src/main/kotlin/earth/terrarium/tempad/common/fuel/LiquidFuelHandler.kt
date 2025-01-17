package earth.terrarium.tempad.common.fuel

import earth.terrarium.tempad.Tempad
import earth.terrarium.tempad.api.context.ContextInstance
import earth.terrarium.tempad.common.recipe.SingleFluidRecipeInput
import earth.terrarium.tempad.common.registries.ModRecipes
import earth.terrarium.tempad.common.registries.ModTags
import earth.terrarium.tempad.common.utils.contains
import earth.terrarium.tempad.common.utils.get
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.capabilities.Capabilities
import net.neoforged.neoforge.fluids.FluidStack
import net.neoforged.neoforge.fluids.capability.IFluidHandler
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem
import kotlin.jvm.optionals.getOrNull

class LiquidFuelHandler(tempadStack: ItemStack, override val totalCharges: Int): BaseFuelHandler(tempadStack, "liquid"), IFluidHandlerItem {
    override fun getTanks(): Int = 1

    override fun getFluidInTank(tank: Int): FluidStack = FluidStack.EMPTY

    override fun getTankCapacity(tank: Int): Int = Int.MAX_VALUE

    override fun isFluidValid(tank: Int, stack: FluidStack): Boolean = stack in ModTags.LIQUID_FUEL

    override fun fill(resource: FluidStack, action: IFluidHandler.FluidAction): Int {
        val input = SingleFluidRecipeInput(resource)
        val recipe = Tempad.server!!.recipeManager.getAllRecipesFor(ModRecipes.liquidFuelType)
            .find { it.value().matches(input) }
            ?: return 0

        if (resource.isEmpty || charges >= totalCharges) return 0

        val amount = (resource.amount / recipe.value().amount).coerceAtMost(totalCharges - charges)
        if (amount == 0) return 0
        if (action.execute()) charges += amount
        return amount * recipe.value().amount
    }

    override fun drain(resource: FluidStack, action: IFluidHandler.FluidAction): FluidStack = FluidStack.EMPTY

    override fun drain(maxDrain: Int, action: IFluidHandler.FluidAction): FluidStack = FluidStack.EMPTY

    override fun getContainer(): ItemStack = stack

    override fun addChargeFromItem(context: ContextInstance): Boolean {
        val handler = context.stack[Capabilities.FluidHandler.ITEM] ?: return false
        val fluid = handler.getFluidInTank(0)
        if (fluid.isEmpty) return false
        val input = SingleFluidRecipeInput(fluid)
        val recipe = context.level.recipeManager.getRecipeFor(ModRecipes.liquidFuelType, input, context.level).getOrNull()
        recipe?.value()?.let {
            val toExtract = fluid.copyWithAmount(it.amount)
            val drained = handler.drain(toExtract, FluidAction.SIMULATE)
            if (drained.amount < it.amount) return false
            handler.drain(toExtract, FluidAction.EXECUTE)
            this += 1
            context.stack = handler.container
            return true
        }
        return false
    }
}