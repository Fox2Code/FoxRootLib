/*
 * MIT License
 *
 * Copyright (c) 2026 Fox2Code
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.fox2code.frl.mixin.compat.catalyst.energy;

import com.fox2code.frl.block.tileentity.FRLTileEntityEnergyReceiver;
import com.fox2code.frl.block.tileentity.FRLTileEntityHolderAware;
import com.fox2code.frl.compat.catalyst.core.CatalystDirectionCompat;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import sunsetsatellite.catalyst.core.util.Direction;
import sunsetsatellite.catalyst.core.util.network.Network;
import sunsetsatellite.catalyst.core.util.network.NetworkComponentTile;
import sunsetsatellite.catalyst.energy.electric.base.TileEntityElectricConductor;
import sunsetsatellite.catalyst.energy.simple.api.IEnergyContainer;

@Mixin(FRLTileEntityEnergyReceiver.class)
public abstract class MixinFRLTileEntityEnergyReceiver extends FRLTileEntityHolderAware
	implements IEnergyContainer, NetworkComponentTile {
	@Shadow
	protected Object catalystHandle;

	@Override
	public boolean canReceive(@NotNull Direction dir) {
		if (!this.tileEntityInvalid) {
			return ((FRLTileEntityEnergyReceiver) (Object) this)
				.canReceiveEnergy(CatalystDirectionCompat.toBTA(dir));
		}
		return false;
	}

	@Override
	public boolean canProvide(@NotNull Direction dir) {
		if (!this.tileEntityInvalid) {
			return ((FRLTileEntityEnergyReceiver) (Object) this)
				.canProvideEnergy(CatalystDirectionCompat.toBTA(dir));
		}
		return false;
	}

	@Override
	public long internalChangeEnergy(long difference) {
		return difference == 0 ? 0 : difference > 0 ?
			internalAddEnergy(difference) :
			internalRemoveEnergy(-difference);
	}

	@Override
	public long internalAddEnergy(long energy) {
		return ((FRLTileEntityEnergyReceiver) (Object) this).tryReceiveEnergy(energy);
	}

	@Override
	public long internalRemoveEnergy(long energy) {
		return 0;
	}

	@Override
	public long receiveEnergy(@NotNull Direction dir, long energy) {
		return this.tileEntityInvalid ? 0 :
			((FRLTileEntityEnergyReceiver) (Object) this)
				.tryReceiveEnergy(CatalystDirectionCompat.toBTA(dir), energy);
	}

	@Override
	public boolean isConnected(Direction direction) {
		return this.worldObj != null && direction.getTileEntity(
			this.worldObj, this) instanceof TileEntityElectricConductor;
	}

	@Override
	public void networkChanged(Network network) {
		this.catalystHandle = network;
	}

	@Override
	public void removedFromNetwork(Network network) {
		this.catalystHandle = null;
	}
}
