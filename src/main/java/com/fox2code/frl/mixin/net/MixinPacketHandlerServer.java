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
package com.fox2code.frl.mixin.net;

import com.fox2code.frl.ext.net.ḞRLPacketHandler;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.NetworkManager;
import net.minecraft.core.net.packet.Packet;
import net.minecraft.core.net.packet.PacketAddPlayer;
import net.minecraft.server.entity.player.PlayerServer;
import net.minecraft.server.net.handler.PacketHandlerServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PacketHandlerServer.class)
public abstract class MixinPacketHandlerServer implements ḞRLPacketHandler {
	@Shadow
	public NetworkManager netManager;

	@Shadow
	private PlayerServer playerEntity;

	@Override
	public @NotNull NetworkManager frl$getNetworkManager() {
		return this.netManager;
	}

	@Override
	public @Nullable Player frl$getPlayer() {
		return this.playerEntity;
	}

	@Inject(method = "sendPacket", at = @At("HEAD"))
	public void onSendPacket(Packet packet, CallbackInfo ci) {
		// BTA Has an issue with client side UUIDCache poisoning from PacketAddPlayer packets, adding a text
		// reset control sequence to the username partially fixes the issue without any visual artifact.
		if (packet.getClass() == PacketAddPlayer.class && !this.netManager.frl$hasFoxRootLib()) {
			PacketAddPlayer packetAddPlayer = (PacketAddPlayer) packet;
			if (this.playerEntity.username.equals(packetAddPlayer.name) &&
				this.playerEntity.id != packetAddPlayer.entityId) {
				packetAddPlayer.name = packetAddPlayer.name + "§l";
			}
		}
	}
}
