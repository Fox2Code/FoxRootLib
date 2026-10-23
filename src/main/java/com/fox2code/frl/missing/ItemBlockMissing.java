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
package com.fox2code.frl.missing;

import com.fox2code.frl.FRLCore;
import com.fox2code.frl.FRLRegistry;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.entity.TileEntityActivator;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.block.ItemBlock;
import net.minecraft.core.item.tag.ItemTags;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class ItemBlockMissing extends ItemBlock<BlockLogicMissing> {
	@SuppressWarnings("unchecked")
	public ItemBlockMissing(@NotNull Block<BlockLogicMissing> block) {
		super(block);
		withTags(ItemTags.PREVENT_CREATIVE_MINING,
			ItemTags.NOT_IN_CREATIVE_MENU,
			ItemTags.PREVENT_LEFT_CLICK_INTERACTIONS);
	}

	@Override
	public float getStrVsBlock(@NotNull ItemStack selfStack, @NotNull Block<?> block) {
		return 0F;
	}

	@Override
	public @NotNull String getTranslatedDescription(@NotNull ItemStack selfStack) {
		if (FRLCore.isClient && selfStack.itemID != this.id) {
			return I18n.getInstance().translateKey(selfStack.getItemKey() + "_adv.desc")
				.replace("%s", FRLRegistry.Client.getRemoteIdNameSpaceMissingText(selfStack.itemID));
		}
		return super.getTranslatedDescription(selfStack);
	}

	@Override
	public boolean canPlaceDirectlyAtPosition(@NotNull ItemStack selfStack, @NotNull World world, @Nullable Player player, @NotNull TilePosc blockPos, @NotNull Side side, double xHit, double yHit) {
		return false;
	}

	@Override
	public boolean onUseOnBlock(@NotNull ItemStack selfStack, @NotNull World world, @Nullable Player player, @NotNull TilePosc blockPos, @NotNull Side side, double xHit, double yHit) {
		return false;
	}

	@Override
	public void onUseByActivator(@NotNull ItemStack selfStack, @NotNull World world, @NotNull TileEntityActivator activator, @NotNull Random random, @NotNull TilePosc blockPos, @NotNull Direction direction, double offX, double offY, double offZ) {}
}
