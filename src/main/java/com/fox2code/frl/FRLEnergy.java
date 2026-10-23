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
package com.fox2code.frl;

import com.fox2code.frl.block.tileentity.FRLTileEntityEnergyDirectAccess;
import com.fox2code.frl.compat.catalyst.energy.CatalystEnergyShim;
import com.fox2code.frl.compat.ic2.IC2CompatShims;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.piston.IMovingBlock;
import net.minecraft.core.util.helper.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FRLEnergy {
	public static final boolean USE_CATALYST_ENERGY = FabricLoader.getInstance().isModLoaded("catalyst-energy");
	public static final boolean USE_IC2_ENERGY = FabricLoader.getInstance().isModLoaded("ic2");
	public static final boolean USE_HYBRID_ENERGY = USE_CATALYST_ENERGY && USE_IC2_ENERGY;
	public static final boolean USE_DIRECT_ENERGY = !USE_CATALYST_ENERGY && !USE_IC2_ENERGY;
	public static final @NotNull String ENERGY_SYSTEM_NAME = USE_HYBRID_ENERGY ? "Hybrid" :
		USE_CATALYST_ENERGY ? "Catalyst" : USE_IC2_ENERGY ? "IC2" : "Direct";

	private FRLEnergy() {}

	private static @NotNull TileEntity getTargetTileEntity(@NotNull TileEntity tileEntity) {
		TileEntity nestedTileEntity;
		if (tileEntity instanceof IMovingBlock movingBlock &&
			!(tileEntity instanceof FRLTileEntityEnergyDirectAccess) &&
			(nestedTileEntity = movingBlock.getTileEntity()) != null) {
			return nestedTileEntity;
		}
		return tileEntity;
	}

	public static long getEnergy(@NotNull TileEntity tileEntity) {
		tileEntity = getTargetTileEntity(tileEntity);
		long energy;
		if (tileEntity instanceof FRLTileEntityEnergyDirectAccess frlTileEntityEnergyDirectAccess) {
			energy = frlTileEntityEnergyDirectAccess.frl$tryGetEnergy();
			if (energy != 0) return energy;
		}
		energy = CatalystEnergyShim.ENERGY.tryGetEnergy(tileEntity);
		return energy == -1 ? 0 : energy;
	}

	public static long getCapacity(@NotNull TileEntity tileEntity) {
		tileEntity = getTargetTileEntity(tileEntity);
		long energy;
		if (tileEntity instanceof FRLTileEntityEnergyDirectAccess frlTileEntityEnergyDirectAccess) {
			energy = frlTileEntityEnergyDirectAccess.frl$tryGetCapacity();
			if (energy != 0) return energy;
		}
		energy = CatalystEnergyShim.ENERGY.tryGetCapacity(tileEntity);
		return energy == -1 ? 0 : energy;
	}

	public static long stealEnergy(@NotNull TileEntity tileEntity, @Nullable Direction direction, long energy) {
		tileEntity = getTargetTileEntity(tileEntity);
		long sentEnergy;
		if (tileEntity instanceof FRLTileEntityEnergyDirectAccess frlTileEntityEnergyDirectAccess) {
			sentEnergy = frlTileEntityEnergyDirectAccess.frl$tryStealEnergy(direction, energy);
			if (sentEnergy != -1) return sentEnergy;
		}
		if (tileEntity.worldObj == null || tileEntity.isInvalid()) return 0;
		sentEnergy = CatalystEnergyShim.ENERGY.tryStealEnergy(tileEntity, direction, energy);
		return sentEnergy == -1 ? 0 : sentEnergy;
	}

	public static long giveEnergy(@NotNull TileEntity tileEntity, @Nullable Direction direction, long energy) {
		tileEntity = getTargetTileEntity(tileEntity);
		long sentEnergy;
		if (tileEntity instanceof FRLTileEntityEnergyDirectAccess frlTileEntityEnergyDirectAccess) {
			sentEnergy = frlTileEntityEnergyDirectAccess.frl$tryGiveEnergy(direction, energy);
			if (sentEnergy != -1) return sentEnergy;
		}
		if (tileEntity.worldObj == null || tileEntity.isInvalid()) return 0;
		sentEnergy = CatalystEnergyShim.ENERGY.tryGiveEnergy(tileEntity, direction, energy);
		if (sentEnergy != -1) return sentEnergy;
		sentEnergy = IC2CompatShims.ENERGY.tryGiveEnergy(tileEntity, direction, energy);
		return sentEnergy == -1 ? 0 : sentEnergy;
	}

	public static Archetype getArchetype(@NotNull TileEntity tileEntity) {
		Archetype archetype;
		if (tileEntity instanceof FRLTileEntityEnergyDirectAccess frlTileEntityEnergyDirectAccess) {
			archetype = frlTileEntityEnergyDirectAccess.frl$tryGetArchetype();
			if (archetype != null) return archetype;
		}
		archetype = CatalystEnergyShim.ENERGY.tryGetArchetype(tileEntity);
		if (archetype != null) return archetype;
		return Archetype.INERT;
	}

	public static long calcStorage2StorageEnergyExchange(
		long maxWant, long destEnergy, long destMaxCapacity,
		long sourceEnergy, long sourceMaxCapacity) {
		destEnergy = Math.max(0L, destEnergy);
		maxWant = Math.min(Math.min(maxWant, destMaxCapacity - destEnergy), sourceEnergy);
		if (maxWant <= 0) {
			return 0L;
		}
		if (sourceMaxCapacity <= 0) {
			return Math.min(maxWant, sourceEnergy);
		}
		double ratioSource = (double) sourceEnergy / sourceMaxCapacity;
		double ratioDest = (double) destEnergy / destMaxCapacity;
		double exactTransfer = (ratioSource - ratioDest) /
			((1D / sourceMaxCapacity) + (1D / destMaxCapacity));
		return exactTransfer > 0 ? Math.min(((long) exactTransfer), maxWant) : 0L;
	}

	public enum Archetype {
		GENERATOR, STORAGE, CABLE, MACHINE, CONVERTER, INERT
	}
}
