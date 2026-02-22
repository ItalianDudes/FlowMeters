package it.italiandudes.flowmeters.fluid;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public class FluidMeterHandler implements IFluidHandler {

    private final IFluidMeter meter;
    private final EnumFacing side;

    public FluidMeterHandler(IFluidMeter meter, EnumFacing side) {
        this.meter = meter;
        this.side = side;
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        // Accept only if input side
        if (!meter.canReceiveFluid(side)) {
            return 0;
        }
        if (resource == null || resource.amount <= 0) {
            return 0;
        }
        long accepted = meter.receiveFluid(resource, !doFill, side);
        return (int) Math.min(Integer.MAX_VALUE, accepted);
    }

    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        return null;
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        return null;
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return new IFluidTankProperties[0];
    }
}