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
import com.fox2code.frl.block.tileentity.FRLTileEntityEnergyStorage;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.util.helper.Direction;
import sunsetsatellite.catalyst.core.util.network.Network;
import sunsetsatellite.catalyst.core.util.network.NetworkComponentTile;
import sunsetsatellite.catalyst.core.util.network.NetworkPath;
import sunsetsatellite.catalyst.core.util.vector.Vec3i;
import sunsetsatellite.catalyst.energy.simple.api.IEnergyContainer;
import sunsetsatellite.catalyst.energy.simple.impl.TileEntityEnergyConductor;

final class CatalystEnergyCompatImpl extends CatalystEnergyCompatSlim {
	private static final Vec3i CACHE_VEC3I = new Vec3i();

	private CatalystEnergyCompatImpl() {}

	static CatalystEnergyCompat make() {
		return new CatalystEnergyCompatImpl();
	}

	@Override
	public long tryPullEnergy(TileEntity tileEntity, Direction direction, Object networkHandle, long energy) {
		if (tileEntity == null || tileEntity.worldObj == null || energy <= 0L) {
			return 0L;
		}

		Network energyNet = (Network) networkHandle;
		TileEntity neighborTileEntity = tileEntity.worldObj.frl$fastGetTileEntity(
			tileEntity.tilePos.x + direction.offsetX(),
			tileEntity.tilePos.y + direction.offsetY(),
			tileEntity.tilePos.z + direction.offsetZ()
		);
		if (neighborTileEntity instanceof TileEntityEnergyConductor wire && energyNet != null) {
			long energyReceived = 0;
			CACHE_VEC3I.set(wire.tilePos);
			for (NetworkPath path : energyNet.getPathData(CACHE_VEC3I)) {
				long energyReceivedFromPath = 0;
				if (path.target == tileEntity || !(path.target instanceof IEnergyContainer dest)) {
					continue;
				}

				if (dest.canProvide(path.targetDirection)) {
					long maxThroughput = Long.MAX_VALUE;
					for (NetworkComponentTile component : path.path) {
						if (component instanceof TileEntityEnergyConductor) {
							maxThroughput = Math.min(maxThroughput,
								((TileEntityEnergyConductor) component).getMaxThroughput());
						}
					}
					energyReceivedFromPath = Math.min(Math.min(energy - energyReceived,
						maxThroughput), Math.min(dest.getMaxProvide(), dest.getEnergy()));
					if (energyReceivedFromPath <= 0) continue;
					dest.internalChangeEnergy(-energyReceivedFromPath);
				}
				energyReceived += energyReceivedFromPath;
			}
			return energyReceived;
		} else if (neighborTileEntity instanceof IEnergyContainer energyContainer) {
			FRLEnergy.Archetype archetype = FRLEnergy.getArchetype(neighborTileEntity);
			if (archetype == FRLEnergy.Archetype.GENERATOR) {
				return tryStealEnergy(neighborTileEntity, direction.opposite(), energy);
			} else if (archetype == FRLEnergy.Archetype.STORAGE) {
				if (tileEntity instanceof FRLTileEntityEnergyStorage frlEnergyStorage &&
					frlEnergyStorage.equalizeWithOtherStorages()) {
					energy = FRLEnergy.calcStorage2StorageEnergyExchange(energy,
						frlEnergyStorage.getEnergy(), frlEnergyStorage.getCapacity(),
						energyContainer.getEnergy(), energyContainer.getCapacity());
				}
				return tryStealEnergy(neighborTileEntity, direction.opposite(), energy);
			}
		}
		return 0L;
	}

	@Override
	public Object getNetworkHandle(TileEntity tileEntity) {


		return super.getNetworkHandle(tileEntity);
	}
}
