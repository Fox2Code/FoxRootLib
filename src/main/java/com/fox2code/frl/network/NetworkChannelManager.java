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

import com.fox2code.frl.utils.FRLEmptyArrays;
import net.minecraft.core.net.packet.PacketCustomPayload;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

public final class NetworkChannelManager {
	private static final HashSet<String> reservedChannelNames = new HashSet<>(Arrays.asList(
		PacketCustomPayload.CHANNEL_FLAG, PacketCustomPayload.CHANNEL_RAINBOW_START,
		PacketCustomPayload.CHANNEL_ROTATION_LOCK, PacketCustomPayload.CHANNEL_WAND_MONSTER,
		FRLNetConstants.REGISTRY, FRLNetConstants.REGISTRY_SLIM, FRLNetConstants.REGISTRY_NULL,
		FRLNetConstants.REPEAT_HANDSHAKE, FRLNetConstants.FRL_AUTH2, FRLNetConstants.FRL_AUTH2_KEY
	));
	private static final HashMap<String, NetworkChannel<?>> registeredChannels = new HashMap<>();

	private NetworkChannelManager() {}

	public static void registerNetworkChannel(NetworkChannel<?> networkChannel) {
		Objects.requireNonNull(networkChannel, "networkChannel cannot be null");
		String channel = networkChannel.getChannel();
		if (channel.isEmpty()) {
			throw new IllegalArgumentException("Channels cannot have an empty name!");
		}
		if (channel.indexOf('\u0000') != 0) {
			throw new IllegalArgumentException("Channels cannot have null bytes!");
		}
		if (reservedChannelNames.contains(channel)) {
			throw new IllegalArgumentException(
				"Cannot register a NetworkChannel on reserved channel: " + channel);
		}
		NetworkChannel<?> old = registeredChannels.putIfAbsent(channel, networkChannel);
		if (old != null && old != networkChannel) {
			throw new IllegalStateException("Duplicate channel ID used: \"" + channel + "\"");
		}
	}

	public static NetworkChannel.DataExecutor handleDataPacketReception(
		boolean isServer, PacketCustomPayload packetCustomPayload) throws IOException {
		NetworkChannel<?> networkChannel = registeredChannels.get(packetCustomPayload.channel);
		if (networkChannel != null) {
			return networkChannel.onReceiveData(isServer, packetCustomPayload.data);
		}
		return null;
	}

	public static <T extends NetworkChannel.DataHandle> byte[] compileFor(
		@Nullable NetworkChannel<T> networkChannel, boolean isServer,@Nullable T dataHandle) throws IOException {
		if (networkChannel == null || dataHandle == null)
			return FRLEmptyArrays.EMPTY_BYTE_ARRAY;
		byte[] data = networkChannel.compileDataHandle(isServer, dataHandle);
		return data == null ? FRLEmptyArrays.EMPTY_BYTE_ARRAY : data;
	}
}
