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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.net.packet.*;
import net.minecraft.core.world.ICarriable;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.EntityTrackerImpl;
import org.jetbrains.annotations.Nullable;

/**
 * See {@link net.minecraft.server.entity.player.PlayerServer}
 */
@Environment(EnvType.SERVER)
final class FRLFakePlayerNetServer extends FRLFakePlayerBase.FRLFakePlayerNet {
	private final FRLFakePlayerBase fakePlayer;
	private final ItemStack[] playerInventory;
	private EntityTrackerImpl entityTracker;

	static FRLFakePlayerBase.FRLFakePlayerNet make(FRLFakePlayerBase fakePlayer) {
		return new FRLFakePlayerNetServer(fakePlayer);
	}

	private FRLFakePlayerNetServer(FRLFakePlayerBase fakePlayer) {
		this.fakePlayer = fakePlayer;
		this.playerInventory = new ItemStack[]{null, null, null, null, null};
		this.entityTracker = MinecraftServer.getInstance().getEntityTracker(this.fakePlayer.dimension);
	}

	private void broadcastPacket(Packet packet) {
		if (this.entityTracker == null) return;
		this.entityTracker.sendPacketToTrackedPlayers(this.fakePlayer, packet);
	}

	@Override
	void updateDimension(int dimension) {
		this.entityTracker = MinecraftServer.getInstance().getEntityTracker(dimension);
	}

	@Override
	void tick() {
		for(int i = 0; i < 5; ++i) {
			ItemStack itemstack = this.fakePlayer.getEquipmentInSlot(i);
			if (itemstack != this.playerInventory[i]) {
				this.broadcastPacket(new PacketSetEquippedItem(this.fakePlayer.id, i, itemstack));
				this.playerInventory[i] = itemstack;
			}
		}
	}

	@Override
	void swingHand() {
		this.broadcastPacket(new PacketAnimate(this.fakePlayer, 1));
	}

	@Override
	void notifyHeldObject(@Nullable ICarriable heldObject) {
		this.broadcastPacket(new PacketSetHeldObject(this.fakePlayer.id, heldObject));
	}

	@Override
	void notifySleep(int x, int y, int z) {
		this.broadcastPacket(new PacketSleep(this.fakePlayer, 0, x, y, z));
	}

	@Override
	void notifyWakeUp() {
		this.broadcastPacket(new PacketAnimate(this.fakePlayer, 3));
	}
}
