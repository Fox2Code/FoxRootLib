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
package com.fox2code.frl.mixin.compat.catalyst.core;

import com.fox2code.frl.FRLCore;
import com.fox2code.frl.entity.FRLFakePlayerBase;
import com.mojang.nbt.tags.CompoundTag;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.player.inventory.container.Container;
import net.minecraft.core.player.inventory.menu.MenuAbstract;
import net.minecraft.core.world.World;
import org.spongepowered.asm.mixin.Mixin;
import sunsetsatellite.catalyst.Catalyst;
import sunsetsatellite.catalyst.core.util.mp.GuiEntry;
import sunsetsatellite.catalyst.core.util.mp.IMpGui;

import java.lang.reflect.InvocationTargetException;

@Mixin(FRLFakePlayerBase.class)
public abstract class MixinFRLFakePlayerBase extends Player implements IMpGui {
	public MixinFRLFakePlayerBase(World world) {
		super(world);
	}

	@Override
	public void catalyst$displayCustomGUI(Container inventory, int slotIndex, boolean isArmor, String id) {
		if (this.world.isClientSide) return;
		GuiEntry<?,?> entry = Catalyst.GUIS.getItem(id);
		if (entry != null && entry.containerClass != null) {
			try {
				this.containerMenu = (MenuAbstract) entry.containerClass
					.getDeclaredConstructors()[0].newInstance(this.inventory, slotIndex, isArmor);
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
				FRLCore.LOGGER.atWarn().setCause(e).log("Failed to open catalyst container with ID \"" + id + "\"");
			}
			this.containerMenu.containerId = -1;
		}
	}

	@Override
	public void catalyst$displayCustomGUI(TileEntity tileEntity, String id) {
		if (this.world.isClientSide) return;
		GuiEntry<?,?> entry = Catalyst.GUIS.getItem(id);
		if (entry != null && entry.containerClass != null) {
			try {
				this.containerMenu = (MenuAbstract) entry.containerClass
					.getDeclaredConstructors()[0].newInstance(this.inventory, tileEntity);
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
				FRLCore.LOGGER.atWarn().setCause(e).log(
					"Failed to open catalyst container with ID \"" + id + "\" at " +
						(tileEntity == null ? "(?, ?, ?)" : tileEntity.tilePos.toString()));
			}
			this.containerMenu.containerId = -1;
		}
	}

	@Override
	public void catalyst$displayCustomGUI(TileEntity tileEntity, String id, CompoundTag data) {
		if (this.world.isClientSide) return;
		GuiEntry<?,?> entry = Catalyst.GUIS.getItem(id);
		if (entry != null && entry.containerClass != null) {
			try {
				this.containerMenu = (MenuAbstract) entry.containerClass
					.getDeclaredConstructors()[0].newInstance(this.inventory, tileEntity, data);
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
				FRLCore.LOGGER.atWarn().setCause(e).log(
					"Failed to open catalyst container with ID \"" + id + "\" at " +
						(tileEntity == null ? "(?, ?, ?)" : tileEntity.tilePos.toString()));
			}
			this.containerMenu.containerId = -1;
		}
	}
}
