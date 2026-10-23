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
package com.fox2code.frl.mixin.compat.ic2;

import com.fox2code.frl.block.tileentity.FRLTileEntityEnergyDirectAccess;
import ic2.tileentity.TileEntityElectricBlock;
import ic2.tileentity.TileEntityIC2Block;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.util.helper.Direction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TileEntityElectricBlock.class)
public abstract class MixinTileEntityElectricBlock extends TileEntityIC2Block implements FRLTileEntityEnergyDirectAccess {
	@Shadow
	public int energy;

	@Shadow
	public abstract boolean acceptsEnergyFrom(TileEntity emitter, ic2.energy.Direction direction);

	@Shadow
	public abstract boolean emitsEnergyTo(TileEntity receiver, ic2.energy.Direction direction);

	@Shadow
	public int output;

	@Shadow
	public abstract int getMaxEnergyOutput();

	@Shadow
	public int maxStorage;

	@Shadow
	public abstract int injectEnergy(ic2.energy.Direction direction, int amount);

	@Override
	public long frl$tryStealEnergy(@Nullable Direction direction, long energy) {
		int maxEnergyOutput = (int) Math.min(this.energy, energy);
		if (maxEnergyOutput <= 0) return 0L;
		if (direction != null) {
			if (this.tileEntityInvalid || this.worldObj == null) {
				return 0L;
			}
			ic2.energy.Direction ic2Direction = ic2.energy.Direction.fromBta(direction);
			TileEntity tileEntity = ic2Direction.applyToTileEntity(this.worldObj, this);
			if (tileEntity == null || !this.emitsEnergyTo(tileEntity, ic2Direction)) {
				return 0L;
			}
		}
		maxEnergyOutput = Math.min(maxEnergyOutput, this.getMaxEnergyOutput());
		if (maxEnergyOutput <= 0) return 0L;
		this.energy -= maxEnergyOutput;
		return maxEnergyOutput * 10L;
	}

	@Override
	public long frl$tryGiveEnergy(@Nullable Direction direction, long energy) {
		int maxEnergyInput = (int) Math.min(this.maxStorage - this.energy, energy / 10L);
		if (this.worldObj == null || maxEnergyInput <= 0) return 0L;
		if (direction != null) {
			ic2.energy.Direction ic2Direction = ic2.energy.Direction.fromBta(direction);
			TileEntity tileEntity = ic2Direction.applyToTileEntity(this.worldObj, this);
			if (tileEntity == null || !this.acceptsEnergyFrom(tileEntity, ic2Direction)) {
				return 0L;
			}
			return (maxEnergyInput - this.injectEnergy(ic2Direction, maxEnergyInput)) * 10L;
		}
		this.energy += maxEnergyInput;
		return maxEnergyInput * 10L;
	}
}
