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

import com.fox2code.frl.FRLConfig;
import com.fox2code.frl.FRLCore;
import com.fox2code.frl.FRLRegistry;
import com.fox2code.frl.ext.net.ḞRLPacketHandler;
import com.fox2code.frl.network.FRLNetConstants;
import com.fox2code.frl.network.auth2.FRLAuth2;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.net.handler.PacketHandlerClient;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.NetworkManager;
import net.minecraft.core.net.handler.PacketHandler;
import net.minecraft.core.net.packet.PacketCustomPayload;
import net.minecraft.core.net.packet.PacketDisconnect;
import net.minecraft.core.net.packet.PacketHandshake;
import net.minecraft.core.net.packet.PacketPingHandshake;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Mixin(value = PacketHandlerClient.class,remap = false)
public abstract class MixinPacketHandlerClient extends PacketHandler implements ḞRLPacketHandler {
	@Shadow @Final private NetworkManager netManager;

	@Shadow private boolean disconnected;

	@Shadow @Final private Minecraft mc;

	@Unique private boolean frl$hasReceivedHandshake;

	@Unique private FRLAuth2 frl$Auth2;

	@Inject(method = "<init>", at = @At("RETURN"))
	public void onInitialized(Minecraft minecraft, String host, int port, CallbackInfo ci) {
		this.netManager.frl$notifyConnectionHostname(host);
	}

	@Unique private void frl$ReplyPayload() {
		if (!this.netManager.frl$hasFoxRootLib()) {
			this.netManager.frl$notifyPresence();
			this.netManager.addToSendQueue(new PacketPingHandshake(0, 0,
				"FRL", 0, this.netManager.frl$getConnectionHostname(), 0));
		} else if (this.frl$Auth2 != null) {
			this.frl$Auth2.notifyReply();
		}
	}

	@Inject(method = "handleHandshake", at = @At(value = "HEAD"), cancellable = true)
	public void onHandleHandshake(PacketHandshake packetHandshake, CallbackInfo ci) {
		if (this.frl$hasReceivedHandshake) {
			this.handleDisconnect(new PacketDisconnect("PacketHandshake received twice."));
			ci.cancel();
		} else if (this.frl$Auth2 != null && packetHandshake.username == null) {
			packetHandshake.username = this.frl$Auth2.getFinalServerId();
		} else if (FRLConfig.forceAuth2 && !"-".equals(packetHandshake.username)) {
			this.handleDisconnect(new PacketDisconnect(
					FRLConfig.modpackForceAuth2 ?
							"The server does not use your current modpack!" :
							"Server does not support Auth2"));
			ci.cancel();
		}
		this.frl$hasReceivedHandshake = true;
	}

	@Inject(method = "handleCustomPayload", at = @At(value = "HEAD"), cancellable = true)
	public void onHandleCustomPayload(PacketCustomPayload packetCustomPayload, CallbackInfo ci) {
		if (this.disconnected) return;
		if (FRLNetConstants.REGISTRY.equals(packetCustomPayload.channel)) {
			try {
				FRLRegistry.Client.initializeFromServerRegistryPacket(packetCustomPayload);
			} catch (IOException e) {
				throw new RuntimeException("Failed to read server-side registry!", e);
			}
			this.frl$ReplyPayload();
			ci.cancel();
		} else if (FRLNetConstants.REGISTRY_SLIM.equals(packetCustomPayload.channel)) {
			if (FRLRegistry.Client.checkServerRegistryPacket(packetCustomPayload)) {
				FRLRegistry.Client.resetToClientState();
			} else {
				throw new RuntimeException("Modpack registry mismatch!");
			}
			this.frl$ReplyPayload();
			ci.cancel();
		} else if (FRLNetConstants.REGISTRY_NULL.equals(packetCustomPayload.channel)) {
			if (packetCustomPayload.data == null || packetCustomPayload.data.length == 0) {
				FRLRegistry.Client.resetToNullState();
			} else {
				throw new RuntimeException("Failed to read registry value!");
			}
			this.frl$ReplyPayload();
			ci.cancel();
		} else if (FRLNetConstants.REPEAT_HANDSHAKE.equals(packetCustomPayload.channel) &&
				!this.frl$hasReceivedHandshake && this.frl$Auth2 == null) {
			this.netManager.addToSendQueue(new PacketHandshake(Minecraft.getMinecraft().session.username));
		} else if (FRLNetConstants.FRL_AUTH2.equals(packetCustomPayload.channel) && !this.frl$hasReceivedHandshake) {
			if (this.frl$Auth2 == null) {
				this.frl$ReplyPayload();
				this.frl$Auth2 = new FRLAuth2(this.netManager, this.mc.session);
			}
			if (this.frl$Auth2.onReceivePacket(packetCustomPayload)) {
				if (this.frl$Auth2.getKickMessage() != null) {
					this.handleDisconnect(new PacketDisconnect(this.frl$Auth2.getKickMessage()));
					this.frl$hasReceivedHandshake = true;
					this.frl$Auth2 = null;
				} else {
					// Trigger Auth2 fake handshake.
					FRLCore.LOGGER.info("Auth2 initialization completed, finalizing authentification.");
					this.handleHandshake(new PacketHandshake());
				}
			}
			ci.cancel();
		} else if (this.frl$Auth2 != null && this.frl$Auth2.isAuth2KeyPacket(packetCustomPayload)) {
			ci.cancel();
		}
	}

	@WrapOperation(method = "handleAddPlayer", at = @At(value = "INVOKE",
		target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
	public Object wrapPutField(Map<?,?> instance, Object k, Object v, Operation<Object> original) {
		// Avoid adding non-player UUIDs to the UUID cache
		if (k == null || v == null || ((String) k).isEmpty() ||
			((String) k).length() > 16 || ((String) k).indexOf('§') != -1 ||
			((String) k).indexOf(' ') != -1 || ((UUID) v).version() != 4) {
			return null;
		}
		// Disallow filling UUID cache with current player UUID
		Player player = Minecraft.getMinecraft().thePlayer;
		if (player != null && Objects.equals(player.username, k)) {
			return null;
		}
		return original.call(instance, k, v);
	}

	@Override
	public @NotNull NetworkManager frl$getNetworkManager() {
		return this.netManager;
	}

	@Override
	public @Nullable Player frl$getPlayer() {
		return Minecraft.getMinecraft().thePlayer;
	}
}
