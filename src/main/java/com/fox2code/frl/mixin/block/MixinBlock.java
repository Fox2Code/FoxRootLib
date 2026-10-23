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
package com.fox2code.frl.mixin.block;

import com.fox2code.frl.block.FRLTileProxyBlockLogic;
import com.fox2code.frl.block.tileentity.FRLTileEntityEnergyDirectAccess;
import com.fox2code.frl.compat.catalyst.core.CatalystCoreShim;
import com.fox2code.frl.compat.catalyst.energy.CatalystEnergyShim;
import com.fox2code.frl.ext.block.ḞRLBlock;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.BlockLogicAir;
import net.minecraft.core.block.entity.TileEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(Block.class)
public abstract class MixinBlock<T extends BlockLogic> implements ḞRLBlock {
	@Shadow
	public boolean isEntityTile;

	@Shadow
	@Nullable public Supplier<@NotNull TileEntity> entitySupplier;

	@Shadow
	private @NotNull T logic;

	@Shadow
	public abstract void init();

	@Unique private int frl$blockFlags;

	@Override
	public final int frl$getFRLFlags() {
		return this.frl$blockFlags;
	}

	@Override
	public final boolean frl$checkAnyFRLFlags(int flags) {
		return (this.frl$blockFlags & flags) != 0;
	}

	@Override
	public final boolean frl$checkAllFRLFlags(int flags) {
		return (this.frl$blockFlags & flags) == flags;
	}

	@Inject(method = "init", at = @At("RETURN"))
	public void onInit(CallbackInfo ci) {
		int flags = 0;
		if (this.logic instanceof FRLTileProxyBlockLogic)
			flags |= FRL_FLAG_MAY_PROXY_TILE_ENTITY;
		if (this.logic instanceof BlockLogicAir)
			flags |= FRL_FLAG_BLOCK_IS_AIR_VARIANT;
		TileEntity tileEntity;
		Supplier<TileEntity> tileEntitySupplier = this.entitySupplier;
		if (tileEntitySupplier == null && !this.isEntityTile) {
			this.frl$blockFlags = flags;
			return;
		}
		if (tileEntitySupplier == null || !this.isEntityTile ||
			(tileEntity = tileEntitySupplier.get()) == null) {
			this.frl$blockFlags = flags | FRL_FLAG_INCOMPLETE_FLAG_DATA;
			return;
		}
		if (tileEntity instanceof FRLTileEntityEnergyDirectAccess ||
			CatalystEnergyShim.ENERGY.isEnergyTileEntity(tileEntity)) {
			flags |= FRL_FLAG_FRL_ENERGY_COMPATIBLE;
		}
		if (CatalystCoreShim.CORE.isNetworkTileEntity(tileEntity)) {
			flags |= FRL_FLAG_CATALYST_NETWORK_TILE;
		}
		this.frl$blockFlags = flags;
	}
}
