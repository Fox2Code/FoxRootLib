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
import net.minecraft.core.util.helper.Direction;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.NonExtendable
public abstract class FRLTileEntityEnergyAware extends FRLTileEntityHolderAware implements FRLTileEntityEnergyDirectAccess {
	FRLTileEntityEnergyAware() {}

	public boolean canReceiveEnergy(@NotNull Direction dir) {
		return false;
	}

	public boolean canProvideEnergy(@NotNull Direction dir) {
		return false;
	}

	public long tryReceiveEnergy(@NotNull Direction dir, long energy) {
		return this.canReceiveEnergy(dir) ? this.tryReceiveEnergy(energy) : 0;
	}

	public long tryEmitEnergy(Direction direction, long energy) {
		return this.canProvideEnergy(direction) ? this.tryEmitEnergy(energy) : 0L;
	}

	public abstract long tryEmitEnergy(long energy);

	public abstract long tryReceiveEnergy(long energy);

	protected abstract void addEnergyImpl(long change);

	protected abstract void removeEnergyImpl(long change);

	public abstract long getEnergy();

	public abstract long getCapacity();

	public abstract FRLEnergy.Archetype getArchetype();

	// FRLTileEntityEnergyDirectAccess implementations, redirect to FRL regular methods.
	@Override
	public final long frl$tryStealEnergy(@Nullable Direction direction, long energy) {
		return direction == null ? this.tryEmitEnergy(energy) :
			this.tryEmitEnergy(direction, energy);
	}

	@Override
	public final long frl$tryGiveEnergy(@Nullable Direction direction, long energy) {
		return direction == null ? this.tryReceiveEnergy(energy) :
			this.tryReceiveEnergy(direction, energy);
	}

	@Override
	public final long frl$tryGetEnergy() {
		return this.getEnergy();
	}

	@Override
	public final long frl$tryGetCapacity() {
		return this.getCapacity();
	}

	@Override
	public final @Nullable FRLEnergy.Archetype frl$tryGetArchetype() {
		return this.getArchetype();
	}
}
