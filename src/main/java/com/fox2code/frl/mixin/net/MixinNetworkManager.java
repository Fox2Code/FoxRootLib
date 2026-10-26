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

import com.fox2code.frl.FRLCore;
import com.fox2code.frl.ext.net.ḞRLNetworkManager;
import com.fox2code.frl.network.FRLHardenedPacketReader;
import com.fox2code.frl.network.FRLNetConstants;
import com.fox2code.frl.network.ext.PacketCustomPayloadKey;
import com.fox2code.frl.utils.FRLStreamCipherHelper;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.NetworkManager;
import net.minecraft.core.net.handler.PacketHandler;
import net.minecraft.core.net.packet.Packet;
import net.minecraft.core.net.packet.PacketCustomPayload;
import net.minecraft.core.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.crypto.SecretKey;
import java.io.*;
import java.net.Socket;
import java.util.Objects;

@Mixin(NetworkManager.class)
public abstract class MixinNetworkManager implements ḞRLNetworkManager {
	@Shadow private PacketHandler packetListener;

	@Shadow private DataInputStream dis;

	@Shadow private DataOutputStream dos;

	@Shadow private Socket socket;

	@Shadow private boolean running;

	@Shadow private boolean quitting;

	@Unique private boolean frl$hasFoxRootLib;

	@Unique private String frl$connectionHostname;

	@Unique private volatile boolean frl$useReadCrypto;

	@Unique private volatile boolean frl$useWriteCrypto;

	@Unique private SecretKey frl$sharedKey;

	@Unique private final Object frl$cryptoLock = new Object();

	@Override
	public boolean frl$hasFoxRootLib() {
		return this.frl$hasFoxRootLib;
	}

	@Override
	public void frl$notifyPresence() {
		this.frl$hasFoxRootLib = true;
	}

	@Override
	public boolean frl$useFullCrypto() {
		return this.frl$useReadCrypto && this.frl$useWriteCrypto;
	}

	@Override
	public void frl$setSharedKey(SecretKey sharedKey) {
		Objects.requireNonNull(sharedKey, "sharedKey cannot be null");
		synchronized (this.frl$cryptoLock) {
			if (this.frl$sharedKey != null || this.frl$useReadCrypto)
				throw new IllegalStateException("Full shared key already has been set!");
			if (this.dis == null || this.dos == null)
				throw new IllegalStateException("Socket closed");
			this.frl$sharedKey = sharedKey;
			FRLCore.LOGGER.debug("Auth2 shared key registered on NetworkManager");
			this.frl$cryptoLock.notifyAll();
		}
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

	@Inject(method = "wakeThreads", at = @At("HEAD"))
	public void onWakeThreads(CallbackInfo ci) {
		synchronized (this.frl$cryptoLock) {
			this.frl$cryptoLock.notifyAll();
		}
	}

	@Inject(method = "networkShutdown", at = @At("RETURN"))
	public void onNetworkShutdown(String reason, Object[] reasonObjects, CallbackInfo ci) {
		synchronized (this.frl$cryptoLock) {
			this.frl$cryptoLock.notifyAll();
		}
	}

	@Redirect(method = "sendPacket", at = @At(value = "INVOKE", target =
			"Lnet/minecraft/core/net/packet/Packet;writePacket(Lnet/minecraft/core/net/packet/Packet;Ljava/io/DataOutputStream;)V"))
	public void onWritePacket(Packet packet, DataOutputStream dos) throws IOException {
		Packet.writePacket(packet, dos);
		if (packet == PacketCustomPayloadKey.INSTANCE) {
			if (this.frl$useWriteCrypto) {
				throw new IOException("Already received key packet!");
			}
			SecretKey key = this.frl$sharedKey;
			Objects.requireNonNull(key);
			this.frl$useWriteCrypto = true;
			if (this.frl$useReadCrypto)
				this.frl$sharedKey = null;
			FRLCore.LOGGER.debug("Enabling AES stream cipher encryption on outgoing stream");
			this.dos.flush(); // <- The flush is mandatory.
			this.dos = new DataOutputStream(new BufferedOutputStream(
					FRLStreamCipherHelper.wrapOutputStream(this.socket.getOutputStream(),
							key), 65536));
		}
	}

	@Redirect(method = "readPacket", at = @At(value = "INVOKE", target =
		"Lnet/minecraft/core/net/packet/Packet;readPacket(Ljava/io/DataInputStream;Z)Lnet/minecraft/core/net/packet/Packet;"))
	public Packet onReadPacket(DataInputStream dis, boolean isServer) throws IOException {
		Packet packet;
		if (this.packetListener.frl$isHardened()) {
			// Don't allow any unrelated packets during login/ping to improve safety
			packet = FRLHardenedPacketReader.readPacketHardened(((NetworkManager) (Object) this), dis, isServer);
		} else {
			packet = Packet.readPacket(dis, isServer);
		}
		if (packet instanceof PacketCustomPayload packetCustomPayload &&
				FRLNetConstants.FRL_AUTH2_KEY.equals(packetCustomPayload.channel)) {
			if (this.frl$useReadCrypto) {
				throw new IOException("Already received key packet!");
			}
			SecretKey key;
			synchronized (this.frl$cryptoLock) {
				long startWait = System.currentTimeMillis();
				long deadline = startWait + 1000L;
				while (this.frl$sharedKey == null) {
					if (!this.running || this.quitting || this.dis == null ||
							this.socket == null || this.socket.isClosed()) {
						throw new IOException("Socket closed while waiting for shared key");
					}
					long remaining = deadline - System.currentTimeMillis();
					if (remaining <= 0L) {
						throw new IOException("Timed out waiting for shared key after " +
								(System.currentTimeMillis() - startWait) + "ms");
					}
					try {
						this.frl$cryptoLock.wait(remaining);
					} catch (InterruptedException ignored) {
						// NetworkManager.wakeThreads() interrupts the reader thread on every
						// processReadPackets() call, so interrupts are just wake-up signals here.
					}
				}
				key = this.frl$sharedKey;
				this.frl$useReadCrypto = true;
				if (this.frl$useWriteCrypto)
					this.frl$sharedKey = null;
			}
			this.dis = new DataInputStream(FRLStreamCipherHelper.wrapInputStream(dis, key));
		}
		return packet;
	}
}
