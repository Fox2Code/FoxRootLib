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

import com.fox2code.frl.block.tileentity.FRLTileEntityEnergyReceiver;
import com.fox2code.frl.block.tileentity.FRLTileEntityEnergyStorage;
import ic2.energy.Direction;
import ic2.energy.IEnergySource;
import net.minecraft.core.block.entity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FRLTileEntityEnergyStorage.class)
public abstract class MixinFRLTileEntityEnergyStorage extends FRLTileEntityEnergyReceiver implements IEnergySource {
	@Override
	public boolean emitsEnergyTo(TileEntity tileEntity, Direction direction) {
		return this.canProvideEnergy(direction.toBta());
	}

	@Override
	public int getMaxEnergyOutput() {
		return (int) Math.min(((FRLTileEntityEnergyStorage) (Object)this).getMaxProvide() * 10L, Short.MAX_VALUE);
	}
}
