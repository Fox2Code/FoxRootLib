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
import com.fox2code.frl.block.tileentity.FRLTileEntityEnergyStorage;
import com.fox2code.frl.compat.catalyst.core.CatalystDirectionCompat;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import sunsetsatellite.catalyst.core.util.Direction;
import sunsetsatellite.catalyst.energy.simple.api.IEnergyContainer;

@Mixin(FRLTileEntityEnergyStorage.class)
public abstract class MixinFRLTileEntityEnergyStorage extends FRLTileEntityEnergyReceiver implements IEnergyContainer {
	@Override
	public boolean canProvide(@NotNull Direction dir) {
		if (!this.tileEntityInvalid) {
			return this.canProvideEnergy(CatalystDirectionCompat.toBTA(dir));
		}
		return false;
	}

	@Override
	public long internalRemoveEnergy(long energy) {
		return -this.tryEmitEnergy(energy);
	}
}
