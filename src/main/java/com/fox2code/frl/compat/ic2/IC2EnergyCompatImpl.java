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

import com.mojang.nbt.tags.CompoundTag;
import ic2.energy.EnergyNet;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.world.World;

final class IC2EnergyCompatImpl extends IC2EnergyCompatSlim {
	private IC2EnergyCompatImpl() {}

	static IC2EnergyCompat make() {
		return new IC2EnergyCompatImpl();
	}

	@Override
	public boolean readAddedToENet(TileEntity tileEntity, CompoundTag compoundTag) {
		return compoundTag.getBoolean("ic2net");
	}

	@Override
	public void writeAddedToENet(TileEntity tileEntity, CompoundTag compoundTag, boolean value) {
		compoundTag.putBoolean("ic2net", value);
	}

	@Override
	public boolean tryAddToENet(TileEntity tileEntity) {
		if (tileEntity.worldObj == null || tileEntity.isInvalid()) {
			return false;
		}
		EnergyNet.getForWorld(tileEntity.worldObj).addTileEntity(tileEntity);
		return true;
	}

	@Override
	public void removeFromENet(TileEntity tileEntity) {
		World world = tileEntity.worldObj != null ? tileEntity.worldObj :
			tileEntity.carriedBlock != null ? tileEntity.carriedBlock.holder.world : null;
		if (world == null) {
			throw new IllegalStateException("Cannot figure out the world for " + tileEntity + "!");
		}
		EnergyNet.getForWorld(world).removeTileEntity(tileEntity);
	}
}
