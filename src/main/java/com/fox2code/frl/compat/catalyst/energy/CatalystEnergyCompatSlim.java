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
package com.fox2code.frl.compat.catalyst.energy;

import com.fox2code.frl.FRLEnergy;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.util.helper.Direction;
import sunsetsatellite.catalyst.energy.simple.api.IEnergyContainer;
import sunsetsatellite.catalyst.energy.simple.impl.TileEntityEnergyConductor;

class CatalystEnergyCompatSlim extends CatalystEnergyCompat {
	CatalystEnergyCompatSlim() {}

	static CatalystEnergyCompat make() {
		return new CatalystEnergyCompatSlim();
	}

	@Override
	public long tryGetEnergy(TileEntity tileEntity) {
		return tileEntity instanceof IEnergyContainer energyContainer ? energyContainer.getEnergy() : -1;
	}

	@Override
	public long tryGetCapacity(TileEntity tileEntity) {
		return tileEntity instanceof IEnergyContainer energyContainer ? energyContainer.getCapacity() : -1;
	}

	@Override
	public long tryStealEnergy(TileEntity tileEntity, Direction direction, long energy) {
		if (tileEntity instanceof IEnergyContainer energyContainer) {
			energy = Math.min(Math.min(energy,
					energyContainer.getEnergy()),
				energyContainer.getMaxProvide());
			return energy <= 0L ? 0L : -energyContainer.internalRemoveEnergy(energy);
		}
		return -1;
	}

	@Override
	public long tryGiveEnergy(TileEntity tileEntity, Direction direction, long energy) {
		if (tileEntity instanceof IEnergyContainer energyContainer) {
			energy = Math.min(Math.min(energy,
					energyContainer.getCapacityRemaining()),
				energyContainer.getMaxReceive());
			return energy <= 0L ? 0L : energyContainer.internalAddEnergy(energy);
		}
		return -1;
	}

	@Override
	public FRLEnergy.Archetype tryGetArchetype(TileEntity tileEntity) {
		return tileEntity instanceof TileEntityEnergyConductor ? FRLEnergy.Archetype.CABLE : null;
	}

	@Override
	public boolean isEnergyTileEntity(TileEntity tileEntity) {
		return tileEntity instanceof IEnergyContainer;
	}
}
