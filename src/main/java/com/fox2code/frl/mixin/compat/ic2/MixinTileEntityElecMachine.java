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

import com.fox2code.frl.FRLEnergy;
import com.fox2code.frl.block.tileentity.FRLTileEntityEnergyDirectAccess;
import ic2.tileentity.TileEntityElecMachine;
import ic2.tileentity.TileEntityIC2Block;
import net.minecraft.core.util.helper.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TileEntityElecMachine.class)
public abstract class MixinTileEntityElecMachine extends TileEntityIC2Block implements FRLTileEntityEnergyDirectAccess {
	@Shadow
	public int maxEnergy;

	@Shadow
	public int energy;

	@Shadow
	public int maxInput;

	@Shadow
	public abstract int injectEnergy(ic2.energy.Direction direction, int amount);

	@Override
	public long frl$tryStealEnergy(@Nullable Direction direction, long energy) {
		if (direction != null) return 0L;
		int stoleEnergy = (int) Math.min(Math.min(energy / 10, this.energy), this.maxInput);
		if (stoleEnergy <= 0) return 0L;
		this.energy -= stoleEnergy;
		return stoleEnergy * 10L;
	}

	@Override
	public long frl$tryGiveEnergy(@Nullable Direction direction, long energy) {
		int sentEnergy = (int) Math.min(Math.min(energy / 10, this.maxEnergy - this.energy), this.maxInput);
		if (sentEnergy <= 0) return 0L;
		if (direction != null) {
			return this.tileEntityInvalid || this.worldObj == null ? 0L :
				(sentEnergy - this.injectEnergy(ic2.energy.Direction.fromBta(direction), sentEnergy)) * 10L;
		} else {
			this.energy += sentEnergy;
			return sentEnergy * 10L;
		}
	}

	@Override
	public long frl$tryGetEnergy() {
		return this.energy * 10L;
	}

	@Override
	public long frl$tryGetCapacity() {
		return this.maxEnergy * 10L;
	}

	@Override
	public @NotNull FRLEnergy.Archetype frl$tryGetArchetype() {
		return FRLEnergy.Archetype.MACHINE;
	}
}
