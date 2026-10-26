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
package com.fox2code.frl.network.auth2;

import com.fox2code.frl.FRLCore;
import com.fox2code.frl.network.FRLNetConstants;
import com.fox2code.frl.network.ext.PacketCustomPayloadKey;
import net.minecraft.core.net.NetworkManager;
import net.minecraft.core.net.packet.PacketCustomPayload;
import net.minecraft.core.player.Session;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

public final class FRLAuth2 {
	private static final String RSA_CIPHER_ALGORITHM = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();
	private static final int ADVANCED_SUPER_SECURE_KEY = 4;

	private static final KeyPair keyPair;
	private static final String encodedPublicKey;

	private final NetworkManager networkManager;
	private final boolean isServer;
	private final Session session;
	private String serverId;
	private PublicKey remotePublicKey;
	private int remotePublicKeyHash;
	private SecretKey sharedKey;
	private String kickMessage;
	private byte[] nonce;
	private String username;
	private UUID uuid;
	private boolean r;
	private boolean k;

	public FRLAuth2(NetworkManager networkManager) {
		if (!networkManager.frl$getPacketHandler().isServerHandler()) {
			throw new IllegalArgumentException();
		}
		this.networkManager = networkManager;
		this.isServer = true;
		this.serverId = Long.toHexString(SECURE_RANDOM.nextLong());
		this.session = null;
	}

	public FRLAuth2(NetworkManager networkManager, Session session) {
		if (networkManager.frl$getPacketHandler().isServerHandler()) {
			throw new IllegalArgumentException();
		}
		Objects.requireNonNull(session);
		this.networkManager = networkManager;
		this.isServer = false;
		this.serverId = null;
		this.session = session;
		this.username = session.username;
		this.uuid = session.uuid;
	}

	private void assertClient() {
		if (this.isServer) throw new IllegalArgumentException("Not client");
	}

	private void assertServer() {
		if (!this.isServer) throw new IllegalArgumentException("Not server");
	}

	private void sendPacket(FRLAuth2Packet packet) {
		if (this.kickMessage != null) return;
		this.networkManager.addToSendQueue(FRLAuth2Channel.toBTA(packet, this.isServer));
	}

	public String getFinalServerId() {
		if (this.isServer) {
			return this.serverId +
					Integer.toHexString(ADVANCED_SUPER_SECURE_KEY + encodedPublicKey.hashCode()) +
					Integer.toHexString(ADVANCED_SUPER_SECURE_KEY + this.remotePublicKeyHash);
		} else {
			return this.serverId +
					Integer.toHexString(ADVANCED_SUPER_SECURE_KEY + this.remotePublicKeyHash) +
					Integer.toHexString(ADVANCED_SUPER_SECURE_KEY + encodedPublicKey.hashCode());
		}
	}

	public boolean onReceivePacket(PacketCustomPayload packetCustomPayload) {
		if (this.kickMessage != null) return false;
		if (!FRLNetConstants.FRL_AUTH2.equals(packetCustomPayload.channel) ||
				packetCustomPayload.data == null || packetCustomPayload.data.length < 1) {
			this.kickMessage = "Malformed Packet Received";
			return true;
		}
		return FRLAuth2Channel.fromBTA(packetCustomPayload, this.isServer).execute(this);
	}

	public String getKickMessage() {
		return this.kickMessage;
	}

	public String getUsername() {
		return this.username;
	}

	public UUID getUUID() {
		return this.uuid;
	}

	public void initializeAuth() {
		this.assertServer();
		this.sendPacket(new ServerHelloAuth(encodedPublicKey));
	}

	void clientReplyAuth(ServerHelloAuth serverHelloAuth) {
		this.assertClient();
		if (this.nonce != null) throw new IllegalStateException();
		if (this.session.uuid.version() != 4 || this.session.sessionId.length() < 2) {
			this.sendPacket(new ClientAuthOffline(this.session.username));
			return;
		}
		this.remotePublicKey = decodeBase64ToPublicKey(serverHelloAuth.serverPublicKey);
		this.remotePublicKeyHash = serverHelloAuth.serverPublicKey.hashCode();
		this.nonce = new byte[32];
		SECURE_RANDOM.nextBytes(this.nonce);
		byte[] challenge = cipher(this.nonce, this.remotePublicKey);
		if (challenge.length != 512) {
			throw new IllegalArgumentException(challenge.length + " != 512");
		}
		SecretKey sharedKey = generateSharedKey();
		byte[] netSharedKey = cipher(sharedKey.getEncoded(), this.remotePublicKey);
		if (netSharedKey.length != 512) {
			throw new IllegalArgumentException(netSharedKey.length + " != 512");
		}
		this.sharedKey = sharedKey;
		this.networkManager.frl$setSharedKey(sharedKey);
		this.sendPacket(new ClientHelloAuth(encodedPublicKey,
				this.session.username, this.session.uuid, challenge, netSharedKey));
		this.networkManager.addToSendQueue(PacketCustomPayloadKey.INSTANCE);
	}

	boolean serverOfflineAuth(ClientAuthOffline clientAuthOffline) {
		this.assertServer();
		this.username = clientAuthOffline.username;
		this.kickMessage = "This server requires an online account to join!";
		return true;
	}

	void serverReplyAuth(ClientHelloAuth clientHelloAuth) {
		this.assertServer();
		this.remotePublicKey = decodeBase64ToPublicKey(clientHelloAuth.clientPublicKey);
		this.remotePublicKeyHash = clientHelloAuth.clientPublicKey.hashCode();
		this.username = clientHelloAuth.username;
		this.uuid = clientHelloAuth.uuid;
		byte[] nonce = decipher(clientHelloAuth.challenge, keyPair.getPrivate());
		if (nonce.length != 32) {
			this.kickMessage = "Failed to verify client side implementation integrity";
			return;
		}
		this.nonce = nonce;
		byte[] solution = cipher(nonce, this.remotePublicKey);
		if (solution.length != 512) {
			throw new IllegalArgumentException(solution.length + " != 512");
		}
		this.sharedKey = new SecretKeySpec(decipher(
				clientHelloAuth.sharedKey, keyPair.getPrivate()), "AES");
		this.networkManager.frl$setSharedKey(this.sharedKey);
		if (!this.networkManager.frl$hasFoxRootLib()) {
			this.kickMessage = "Failed to verify client side implementation integrity";
		} else {
			this.networkManager.addToSendQueue(PacketCustomPayloadKey.INSTANCE);
		}
	}

	public void notifyReply() {
		this.r = true;
	}

	public void authRequestAccepted() {
		this.sendPacket(new ServerAuthPass(this.serverId));
	}

	public boolean isLoginStateInvalid() {
		return this.sharedKey == null || r ||
				this.kickMessage != null || !k ||
				this.username == null;
	}

	boolean clientFinishLogin(ServerAuthPass serverAuthPass) {
		this.assertClient();
		if (this.serverId != null || this.sharedKey == null) {
			throw new IllegalStateException();
		}

		this.serverId = serverAuthPass.serverId;
		if (this.serverId.length() != 16 || !(r&k) ||
				!this.networkManager.frl$useFullCrypto() ||
				!this.networkManager.frl$hasFoxRootLib()) {
			this.kickMessage = "Server side Auth2 implementation is compromised, aborted authentification attempt!";
			return false;
		}
		return true;
	}

	public boolean isAuth2KeyPacket(PacketCustomPayload packetCustomPayload) {
		if (FRLNetConstants.FRL_AUTH2_KEY.equals(packetCustomPayload.channel)) {
			return this.k = packetCustomPayload.data == null ||
					packetCustomPayload.data.length == 0;
		}
		return false;
	}

	private static KeyPair generateKeyPair() throws Exception {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(4096, SECURE_RANDOM);
		return keyPairGenerator.generateKeyPair();
	}

	private static String encodePublicKeyToBase64(PublicKey publicKey) {
		return Base64.getEncoder().encodeToString(publicKey.getEncoded());
	}

	private static PublicKey decodeBase64ToPublicKey(String base64PublicKey) {
		try {
			return KeyFactory.getInstance("RSA").generatePublic(
					new X509EncodedKeySpec(Base64.getDecoder().decode(base64PublicKey)));
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
	}

	private static byte[] cipher(byte[] rawData, PublicKey publicKey) {
		Objects.requireNonNull(rawData);
		try {
			Cipher cipher = Cipher.getInstance(RSA_CIPHER_ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			return cipher.doFinal(rawData);
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
	}

	private static byte[] cipher(byte[] rawData, SecretKey sharedKey) {
		try {
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, sharedKey);
			return cipher.doFinal(rawData);
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
	}

	private static byte[] decipher(byte[] encryptedBytes, PrivateKey privateKey) {
		Objects.requireNonNull(encryptedBytes);
		try {
			Cipher cipher = Cipher.getInstance(RSA_CIPHER_ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			byte[] ret = cipher.doFinal(encryptedBytes);
			if (ret == null) throw new RuntimeException("Failed to decipher bytes!");
			return ret;
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
	}

	private static byte[] decipher(byte[] rawData, SecretKey sharedKey) {
		try {
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, sharedKey);
			return cipher.doFinal(rawData);
		} catch (GeneralSecurityException e) {
			FRLCore.LOGGER.error("Failed to decipher Auth2 payload", e);
			throw new RuntimeException(e);
		}
	}

	private static SecretKey generateSharedKey() {
		try {
			KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
			keyGenerator.init(256, SECURE_RANDOM);
			return keyGenerator.generateKey();
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
	}

	static {
		try {
			keyPair = generateKeyPair();
			encodedPublicKey = encodePublicKeyToBase64(keyPair.getPublic());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
