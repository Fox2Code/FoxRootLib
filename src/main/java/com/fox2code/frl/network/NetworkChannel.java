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
package com.fox2code.frl.network;

import com.fox2code.frl.annotation.CalledAsync;
import com.fox2code.frl.network.ext.PacketCustomPayloadOutbound;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.NetworkManager;
import net.minecraft.core.net.handler.PacketHandler;
import net.minecraft.core.net.packet.PacketCustomPayload;

import java.io.IOException;
import java.util.Objects;

public abstract class NetworkChannel<T extends NetworkChannel.DataHandle> {
	private final String channel;

	public NetworkChannel(String channel) {
		this.channel = Objects.requireNonNull(channel);
	}

	public String getChannel() {
		return channel;
	}

	@CalledAsync
	protected abstract DataExecutor onReceiveData(boolean isServer, byte[] data) throws IOException;

	@CalledAsync
	protected abstract byte[] compileDataHandle(boolean isServer, T object) throws IOException;

	protected void onEmitDataHandle(NetworkManager networkManager, boolean isServer, T handle) {
		networkManager.addToSendQueue(new PacketCustomPayloadOutbound<>(this, isServer, handle));
	}

	protected final void onEmitData(NetworkManager networkManager, byte[] data) {
		networkManager.addToSendQueue(new PacketCustomPayload(this.channel, data));
	}

	private void sendPacketTo(Player player, T packet) {
		if (!player.frl$isRealPlayer()) return;
	}

	public NetworkChannel<T> register() {
		NetworkChannelManager.registerNetworkChannel(this);
		return this;
	}



	@FunctionalInterface
	public interface DataExecutor {
		void execute(PacketHandler packetHandler);
	}

	public interface DataHandle {
		int getEstimatedSize();
	}
}
