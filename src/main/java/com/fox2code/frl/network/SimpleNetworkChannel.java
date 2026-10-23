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

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.core.net.packet.PacketCustomPayload;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

public final class SimpleNetworkChannel extends NetworkChannel<SimplePacket> {
	private final ReferenceArrayList<SimplePacketEntry<? extends SimplePacket>> registeredS2CPackets;
	private final ReferenceArrayList<SimplePacketEntry<? extends SimplePacket>> registeredC2SPackets;

	public SimpleNetworkChannel(String channel) {
		super(channel);
		this.registeredS2CPackets = new ReferenceArrayList<>();
		this.registeredC2SPackets = new ReferenceArrayList<>();
	}

	private static <T extends SimplePacket> SimplePacketEntry<T> buildPacketEntry(Class<T> packet) {
		if (packet == null) return null;
		packet.asSubclass(SimplePacket.class);
		if (Modifier.isAbstract(packet.getModifiers())) {
			throw new IllegalArgumentException("Cannot register an abstract class as a packet!");
		}
		Constructor<T> constructor;
		try {
			constructor = packet.getConstructor();
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("Cannot register a class without a default constructor as a packet!");
		}
		return new SimplePacketEntry<>(packet, constructor);
	}

	private static int indexOf(ReferenceArrayList<SimplePacketEntry<? extends SimplePacket>> packets, Class<?> packetClass) {
		for (int i = 0; i < packets.size(); i++) {
			SimplePacketEntry<? extends SimplePacket> packetEntry = packets.get(i);
			if (packetEntry != null && packetEntry.cls == packetClass) {
				return i;
			}
		}
		return -1;
	}

	public SimpleNetworkChannel registerS2CPacket(Class<? extends SimplePacket> packet) {
		if (packet != null && this.registeredS2CPackets.stream().anyMatch(e -> e.cls == packet)) {
			throw new IllegalArgumentException("Duplicate packet for S2C on " + this.getChannel());
		}
		this.registeredS2CPackets.add(buildPacketEntry(packet));
		return this;
	}

	public SimpleNetworkChannel registerC2SPacket(Class<? extends SimplePacket> packet) {
		if (packet != null && this.registeredC2SPackets.stream().anyMatch(e -> e.cls == packet)) {
			throw new IllegalArgumentException("Duplicate packet for C2S on " + this.getChannel());
		}
		this.registeredC2SPackets.add(buildPacketEntry(packet));
		return this;
	}

	public SimpleNetworkChannel registerPacket(Class<? extends SimplePacket> packet) {
		if (packet != null && this.registeredS2CPackets.stream().anyMatch(e -> e.cls == packet)) {
			throw new IllegalArgumentException("Duplicate packet for S2C on " + this.getChannel());
		}
		if (packet != null && this.registeredC2SPackets.stream().anyMatch(e -> e.cls == packet)) {
			throw new IllegalArgumentException("Duplicate packet for C2S on " + this.getChannel());
		}
		SimplePacketEntry<? extends SimplePacket> packetEntry = buildPacketEntry(packet);
		this.registeredS2CPackets.add(packetEntry);
		this.registeredC2SPackets.add(packetEntry);
		return this;
	}

	@Override
	protected DataExecutor onReceiveData(boolean isServer, byte[] data) throws IOException {
		if (data == null || data.length < 2) return null;
		DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(data));
		int packetId = dataInputStream.readUnsignedShort();
		ReferenceArrayList<SimplePacketEntry<? extends SimplePacket>> packets =
			isServer ? this.registeredC2SPackets :
				this.registeredS2CPackets;
		if (packetId >= packets.size()) return null;
		SimplePacketEntry<? extends SimplePacket> packetClass = packets.get(packetId);
		if (packetClass == null) return null;
		SimplePacket simplePacket = packetClass.makeNewPacket();
		simplePacket.readPacket(dataInputStream);
		return simplePacket;
	}

	@Override
	protected byte[] compileDataHandle(boolean isServer, SimplePacket simplePacket) throws IOException {
		return isServer ? compileS2CData(simplePacket) : compileC2SData(simplePacket);
	}

	private byte[] compileS2CData(SimplePacket simplePacket) throws IOException {
		int id = indexOf(this.registeredS2CPackets, simplePacket.getClass());
		if (id == -1) {
			throw new IllegalArgumentException("Packet " +
				simplePacket.getClass().getSimpleName() +
				" has not been registered in S2C (Compile Failed)");
		}
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
		dataOutputStream.writeShort(id);
		simplePacket.writePacket(dataOutputStream);
		return byteArrayOutputStream.toByteArray();
	}

	private byte[] compileC2SData(SimplePacket simplePacket) throws IOException {
		int id = indexOf(this.registeredC2SPackets, simplePacket.getClass());
		if (id == -1) {
			throw new IllegalArgumentException("Packet " +
				simplePacket.getClass().getSimpleName() +
				" has not been registered in C2S (Compile Failed)");
		}
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
		dataOutputStream.writeShort(id);
		simplePacket.writePacket(dataOutputStream);
		return byteArrayOutputStream.toByteArray();
	}

	public PacketCustomPayload compileS2CPacket(SimplePacket simplePacket) throws IOException {
		return new PacketCustomPayload(this.getChannel(), this.compileS2CData(simplePacket));
	}

	public PacketCustomPayload compileC2SPacket(SimplePacket simplePacket) throws IOException {
		return new PacketCustomPayload(this.getChannel(), this.compileC2SData(simplePacket));
	}

	private record SimplePacketEntry<T extends SimplePacket>(Class<T> cls, Constructor<T> cst) {
		public T makeNewPacket() {
			try {
				return cst.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (InvocationTargetException e) {
				throw e.getCause() instanceof RuntimeException r ? r : new RuntimeException(e);
			}
		}

	}
}
