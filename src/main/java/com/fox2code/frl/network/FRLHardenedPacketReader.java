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

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.core.net.NetworkManager;
import net.minecraft.core.net.packet.Packet;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;

public final class FRLHardenedPacketReader {
	private static final IntOpenHashSet loginPacketIds = new IntOpenHashSet();
	private static final IntOpenHashSet pingPacketIds = new IntOpenHashSet();

	private FRLHardenedPacketReader() {}

	public static Packet readPacketHardened(
		NetworkManager networkManager, DataInputStream dis, boolean isServer) throws IOException {
		try {
			int id = dis.read();
			if (id == -1) {
				return null;
			} else if ((isServer ? loginPacketIds : pingPacketIds).contains(id) ||
				// Allow switching from hardened to non-hardened mode from PacketCustomPayload
				(id == 250 && !networkManager.frl$getPacketHandler().frl$isHardened())) {
				Packet packet = Packet.getNewPacket(id);
				if (packet == null) {
					throw new IOException("Bad packet id " + id);
				} else {
					packet.read(dis);
					return packet;
				}
			} else {
				throw new IOException("Bad packet id " + id);
			}
		} catch (EOFException var4) {
			return null;
		}
	}

	static {
		loginPacketIds.add(1); // PacketLogin
		loginPacketIds.add(2); // PacketHandshake
		loginPacketIds.add(254); // PacketPingHandshake
		loginPacketIds.add(255); // Disconnect
		pingPacketIds.add(253); // Server Icon
		pingPacketIds.add(255); // Disconnect
	}
}
