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
package com.fox2code.frl.compat.ic2;

import ic2.energy.EnergyNet;
import ic2.energy.IEnergySource;
import net.minecraft.core.block.entity.TileEntity;

class IC2EnergyCompatSlim extends IC2EnergyCompat {
	IC2EnergyCompatSlim() {}

	static IC2EnergyCompat make() {
		return new IC2EnergyCompatSlim();
	}

	@Override
	public long emitEnergyToENet(TileEntity tileEntity, long energy) {
		if (tileEntity.worldObj == null || tileEntity.isInvalid() || energy < 10) {
			return 0L;
		}
		int sentEnergy = (int) Math.min(energy / 10, Short.MAX_VALUE);
		int returnEnergy = EnergyNet.getForWorld(tileEntity.worldObj)
			.emitEnergyFrom((IEnergySource) tileEntity, sentEnergy);
		return (sentEnergy - returnEnergy) * 10L;
	}
}
