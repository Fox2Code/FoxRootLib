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
package com.fox2code.frl.block.tileentity;

import com.fox2code.frl.FRLEnergy;
import com.fox2code.frl.compat.catalyst.energy.CatalystEnergyShim;
import com.fox2code.frl.compat.ic2.IC2CompatShims;
import com.mojang.nbt.tags.CompoundTag;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.world.World;
import org.jetbrains.annotations.NotNull;

public abstract class FRLTileEntityEnergyReceiver extends
	FRLTileEntityEnergyAware implements FRLTileEntityEnergyDirectAccess {
	protected long energy = 0;
	protected boolean ic2net;
	protected Object catalystHandle;

	public FRLTileEntityEnergyReceiver() {}

	@Override
	public void readAdditionalData(@NotNull CompoundTag compoundTag) {
		this.energy = compoundTag.getLongOrDefault("Energy", 0L);
		this.ic2net = IC2CompatShims.ENERGY.readAddedToENet(this, compoundTag);
	}

	@Override
	public void writeAdditionalData(@NotNull CompoundTag compoundTag) {
		compoundTag.putLong("Energy", this.energy);
		IC2CompatShims.ENERGY.writeAddedToENet(this, compoundTag, this.ic2net);
	}

	@Override
	public void heldTick(World unused, Entity holder) {
		if (!holder.world.isClientSide) {
			this.serverTick(holder.world, false);
		}
		this.commonTick(holder.world, false);
	}

	@Override
	public void tick() {
		World world = this.worldObj;
		if (world == null) return;
		if (!world.isClientSide) {
			if (!this.ic2net) {
				this.ic2net = IC2CompatShims.ENERGY.tryAddToENet(this);
			}
			this.serverTick(world, true);
			this.frlPrivateBlockTick();
		}
		this.commonTick(world, false);
		long maxReceiveEnergy;
		long maxEnergyWant;
		long energyCapacity = this.getCapacity();
		if (!world.isClientSide && this.demandsEnergy() &&
			(maxReceiveEnergy = this.getMaxReceive()) > 0 &&
			(maxEnergyWant = Math.min(maxReceiveEnergy,
				energyCapacity - this.getEnergy())) > 0) {
			if (FRLEnergy.USE_DIRECT_ENERGY || catalystHandle == null) {
				for (Direction direction : Direction.all) {
					if (!this.canReceiveEnergy(direction)) {
						continue;
					}
					TileEntity tileEntity = world.frl$fastGetProxiedTileEntity(
						this.tilePos.x + direction.offsetX(),
						this.tilePos.y + direction.offsetY(),
						this.tilePos.z + direction.offsetZ());
					if (tileEntity instanceof FRLTileEntityEnergyDirectAccess directAccess && tileEntity != this) {
						FRLEnergy.Archetype archetype = directAccess.frl$tryGetArchetype();
						long energyDrained = 0L;
						if (archetype == FRLEnergy.Archetype.GENERATOR) {
							energyDrained = directAccess.frl$tryStealEnergy(
								direction.opposite(), maxEnergyWant);
						} else if (archetype == FRLEnergy.Archetype.STORAGE) {
							if (this.getArchetype() == FRLEnergy.Archetype.STORAGE) {
								energyDrained = directAccess.frl$tryStealEnergy(direction.opposite(),
									FRLEnergy.calcStorage2StorageEnergyExchange(maxEnergyWant,
										this.getEnergy(), energyCapacity,
										directAccess.frl$tryGetEnergy(),
										directAccess.frl$tryGetCapacity()));
							} else {
								energyDrained = directAccess.frl$tryStealEnergy(
									direction.opposite(), maxEnergyWant);
							}
						}
						if (energyDrained > 0L) {
							this.tryReceiveEnergy(direction, energyDrained);
							maxEnergyWant = Math.min(maxReceiveEnergy, energyCapacity - this.getEnergy());
							if (maxEnergyWant <= 0) break;
						}
					}
				}
			} else {
				for (Direction direction : Direction.all) {
					if (!this.canReceiveEnergy(direction)) {
						continue;
					}
					long energyDrained = CatalystEnergyShim.ENERGY.tryPullEnergy(
						this, direction, this.catalystHandle, maxEnergyWant);
					if (energyDrained > 0L) {
						this.tryReceiveEnergy(direction, energyDrained);
						maxEnergyWant = Math.min(maxReceiveEnergy, energyCapacity - this.getEnergy());
						if (maxEnergyWant <= 0) break;
					}
				}
			}
		}
	}

	public abstract void serverTick(World world, boolean asBlock);

	public abstract void commonTick(World world, boolean asBlock);

	void frlPrivateBlockTick() {

	}

	public boolean demandsEnergy() {
		return (!this.tileEntityInvalid) && this.getCapacityRemaining() > 0;
	}

	@Override
	public boolean canReceiveEnergy(@NotNull Direction dir) {
		return true;
	}

	@Override
	public long tryReceiveEnergy(@NotNull Direction dir, long energy) {
		return this.canReceiveEnergy(dir) ? this.tryReceiveEnergy(energy) : 0;
	}

	@Override
	public long tryReceiveEnergy(long energy) {
		if (!this.demandsEnergy()) return 0;
		energy = Math.min(energy, this.getMaxReceive());
		long change = Math.max(Math.min(this.getCapacityRemaining(), energy), 0);
		this.addEnergyImpl(change);
		return change;
	}

	@Override
	protected void addEnergyImpl(long change) {
		this.energy += change;
	}

	@Override
	public long tryEmitEnergy(long energy) {
		return this.consumeEnergy(energy) ? energy : 0L;
	}

	public boolean consumeEnergy(long energy) {
		if (this.energy > energy) {
			this.removeEnergyImpl(energy);
			return true;
		}
		return false;
	}

	@Override
	protected void removeEnergyImpl(long change) {
		this.energy -= change;
	}

	@Override
	public long getEnergy() {
		return this.energy;
	}

	public abstract long getMaxReceive();

	@Override
	public abstract long getCapacity();

	public long getCapacityRemaining() {
		return Math.max(this.getCapacity() - this.energy, 0);
	}

	@Override
	public @NotNull FRLEnergy.Archetype getArchetype() {
		return FRLEnergy.Archetype.MACHINE;
	}
}
