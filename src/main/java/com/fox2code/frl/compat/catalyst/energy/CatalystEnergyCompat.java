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

public class CatalystEnergyCompat {
	CatalystEnergyCompat() {}

	public long tryPullEnergy(TileEntity tileEntity, Direction direction, Object networkHandle, long energy) {
		return 0L;
	}

	public Object getNetworkHandle(TileEntity tileEntity) {
		return null;
	}

	public long tryGetEnergy(TileEntity tileEntity) {
		return -1;
	}

	public long tryGetCapacity(TileEntity tileEntity) {
		return -1;
	}

	public long tryStealEnergy(TileEntity tileEntity, Direction direction, long energy) {
		return -1;
	}

	public long tryGiveEnergy(TileEntity tileEntity, Direction direction, long energy) {
		return -1;
	}

	public FRLEnergy.Archetype tryGetArchetype(TileEntity tileEntity) {
		return null;
	}

	public boolean isEnergyTileEntity(TileEntity tileEntity) {
		return false;
	}
}
