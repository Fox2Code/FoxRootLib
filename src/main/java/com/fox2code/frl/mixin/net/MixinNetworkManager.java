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

import com.fox2code.frl.ext.net.ḞRLNetworkManager;
import com.fox2code.frl.network.FRLHardenedPacketReader;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.NetworkManager;
import net.minecraft.core.net.handler.PacketHandler;
import net.minecraft.core.net.packet.Packet;
import net.minecraft.core.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.DataInputStream;
import java.io.IOException;

@Mixin(NetworkManager.class)
public class MixinNetworkManager implements ḞRLNetworkManager {
	@Shadow
	private PacketHandler packetListener;

	@Unique private boolean frl$hasFoxRootLib;

	@Unique private String frl$connectionHostname;

	@Override
	public boolean frl$hasFoxRootLib() {
		return this.frl$hasFoxRootLib;
	}

	@Override
	public void frl$notifyPresence() {
		this.frl$hasFoxRootLib = true;
	}

	@Override
	public PacketHandler frl$getPacketHandler() {
		return this.packetListener;
	}

	@Override
	public String frl$getConnectionHostname() {
		return this.frl$connectionHostname;
	}

	@Override
	public void frl$notifyConnectionHostname(String hostname) {
		this.frl$connectionHostname = hostname;
	}

	@WrapMethod(method = "processReadPackets")
	public void wrapProcessReadPackets(Operation<Void> original) {
		Player player = this.packetListener.frl$getPlayer();
		World world = player == null ? null : player.world;
		if (world != null) {
			world.frl$networkMode(true);
		}
		try {
			original.call();
		} finally {
			if (world != null) {
				world.frl$networkMode(false);
			}
		}
	}

	@Redirect(method = "readPacket", at = @At(value = "INVOKE", target =
		"Lnet/minecraft/core/net/packet/Packet;readPacket(Ljava/io/DataInputStream;Z)Lnet/minecraft/core/net/packet/Packet;"))
	public Packet onReadPacket(DataInputStream dis, boolean isServer) throws IOException {
		if (this.packetListener.frl$isHardened()) {
			// Don't allow any unrelated packets during login/ping to improve safety
			return FRLHardenedPacketReader.readPacketHardened(((NetworkManager) (Object) this), dis, isServer);
		} else {
			return Packet.readPacket(dis, isServer);
		}
	}
}
