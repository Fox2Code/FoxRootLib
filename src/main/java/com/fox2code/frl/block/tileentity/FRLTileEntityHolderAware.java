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

import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.motion.CarriedBlock;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.ICarriable;
import net.minecraft.core.world.ICarrySource;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;

public abstract class FRLTileEntityHolderAware extends TileEntity implements ICarrySource {
	public FRLTileEntityHolderAware() {}

	public World getWorld() {
		return this.carriedBlock != null && this.carriedBlock.holder.addedToChunk ?
			this.carriedBlock.holder.world : this.tileEntityInvalid ? null : this.worldObj;
	}

	public double getX() {
		return this.carriedBlock != null && this.carriedBlock.holder.addedToChunk ?
			this.carriedBlock.holder.x : this.tilePos.x + 0.5D;
	}

	public double getY() {
		return this.carriedBlock != null && this.carriedBlock.holder.addedToChunk ?
			this.carriedBlock.holder.y : this.tilePos.y + 0.5D;
	}

	public double getZ() {
		return this.carriedBlock != null && this.carriedBlock.holder.addedToChunk ?
			this.carriedBlock.holder.z : this.tilePos.z + 0.5D;
	}

	public boolean allowCarry() {
		return false;
	}

	@Override
	public boolean tryPlace(World world, Entity holder, int blockX, int blockY, int blockZ, Side side, double xPlaced, double yPlaced) {
		CarriedBlock carriedBlock = this.carriedBlock;
		boolean place = super.tryPlace(world, holder, blockX, blockY, blockZ, side, xPlaced, yPlaced);
		if (place && carriedBlock != null && !this.tileEntityInvalid) {
			this.onTileEntityPlaced(world, holder, blockX, blockY, blockZ, side, xPlaced, yPlaced);
			if (this.carriedBlock == carriedBlock && carriedBlock.holder == holder) {
				this.carriedBlock = null; // Early free carriedBlock
			}
		}
		return place;
	}

	@Override
	public @NotNull ICarriable pickup(@NotNull World world, @NotNull Entity holder, @NotNull TilePosc tilePos_) {
		ICarriable carriable = super.pickup(world, holder, tilePos_);
		if (carriable instanceof CarriedBlock pickupCarriedBlock && pickupCarriedBlock.blockId == 0) {
			return carriable;
		}
		this.onTileEntityPickedUp(carriable, world, holder, tilePos_);
		return carriable;
	}

	public void onTileEntityPlaced(
		World world, Entity holder, int blockX, int blockY, int blockZ,
		Side side, double xPlaced, double yPlaced) {}

	public void onTileEntityPickedUp(
		@NotNull ICarriable carriable, @NotNull World world,
		@NotNull Entity holder, @NotNull TilePosc tilePos_) {}

	@Override
	public double getDistanceFrom(double x, double y, double z) {
		double dx;
		double dy;
		double dz;
		if (this.carriedBlock != null && this.carriedBlock.holder.addedToChunk) {
			dx = this.carriedBlock.holder.x - x;
			dy = this.carriedBlock.holder.y - y;
			dz = this.carriedBlock.holder.z - z;
		} else {
			dx = (double) this.tilePos.x + (double) 0.5F - x;
			dy = (double) this.tilePos.y + (double) 0.5F - y;
			dz = (double) this.tilePos.z + (double) 0.5F - z;
		}
		return dx * dx + dy * dy + dz * dz;
	}
}
