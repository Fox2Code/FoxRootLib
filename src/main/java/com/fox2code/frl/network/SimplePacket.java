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
import com.mojang.nbt.tags.CompoundTag;
import net.minecraft.core.net.handler.PacketHandler;
import net.minecraft.core.net.packet.Packet;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

public abstract class SimplePacket implements NetworkChannel.DataExecutor, NetworkChannel.DataHandle {
	public SimplePacket() {}

	@CalledAsync
	public abstract void writePacket(DataOutputStream dataOutputStream) throws IOException;

	@CalledAsync
	public abstract void readPacket(DataInputStream dataInputStream) throws IOException;

	@Override
	public abstract void execute(PacketHandler networkManager);

	@Override
	public abstract int getEstimatedSize();

	public static void writeStringUTF8(String string, DataOutputStream dos) throws IOException {
		Packet.writeStringUTF8(string, dos);
	}

	public static String readStringUTF8(DataInputStream dis, int maxLength) throws IOException {
		return Packet.readStringUTF8(dis, maxLength);
	}

	public static void writeStringUTF16BE(String string, DataOutputStream dos) throws IOException {
		Packet.writeStringUTF16BE(string, dos);
	}

	public static String readStringUTF16BE(DataInputStream dis, int maxLength) throws IOException {
		return Packet.readStringUTF16BE(dis, maxLength);
	}

	public static void writeCompressedCompoundTag(@NotNull CompoundTag tag, DataOutputStream dos) throws IOException {
		Packet.writeCompressedCompoundTag(tag, dos);
	}

	public static CompoundTag readCompressedCompoundTag(DataInputStream dis) throws IOException {
		return Packet.readCompressedCompoundTag(dis);
	}

	public static void writeUUID(@NotNull UUID uuid, DataOutputStream dos) throws IOException {
		Packet.writeUUID(uuid, dos);
	}

	public static UUID readUUID(DataInputStream dis) throws IOException {
		return Packet.readUUID(dis);
	}
}
