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
package com.fox2code.frl.entity;

import com.mojang.nbt.tags.CompoundTag;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class FRLFakePlayerNPC extends FRLFakePlayerBase {
	public boolean allowSaving = false;

	public FRLFakePlayerNPC(@NotNull World world) {
		super(world);
	}

	public FRLFakePlayerNPC(@NotNull World world, boolean allowSaving) {
		super(world);
		this.allowSaving = allowSaving;
	}

	public FRLFakePlayerNPC(@NotNull World world, String username, UUID uuid) {
		super(world, username, uuid);
	}

	public FRLFakePlayerNPC(@NotNull World world, String username, UUID uuid, boolean allowSaving) {
		super(world, username, uuid);
		this.allowSaving = allowSaving;
	}

	public FRLFakePlayerNPC(@NotNull World world, @NotNull Player player) {
		super(world, player);
	}

	public FRLFakePlayerNPC(@NotNull World world, @NotNull Player player, boolean allowSaving) {
		super(world, player);
		this.allowSaving = allowSaving;
	}

	@Override
	public boolean sendsDeathMessage(Entity entityKilledBy) {
		return false;
	}

	@Override
	public boolean checkDoRecalculateEntityPath() {
		return this.random.nextInt(100) == 0;
	}

	@Override
	public boolean save(@NotNull CompoundTag tag) {
		return this.allowSaving && super.save(tag);
	}
}
