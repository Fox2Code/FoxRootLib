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
package com.fox2code.frl;

import com.fox2code.frl.compat.btwaila.BTWailaCompat;
import com.fox2code.frl.compat.uselessnumerical.UselessNumericalCompat;
import com.fox2code.frl.event.lifecycle.GameStartedEvent;
import com.fox2code.frl.missing.BlockLogicMissing;
import com.fox2code.frl.missing.ItemBlockMissing;
import com.fox2code.frl.missing.ItemMissing;
import com.fox2code.frl.network.FRLNetConstants;
import com.fox2code.frl.render.FRLModelDispatcher;
import com.fox2code.frl.tag.FRLTagRegistry;
import com.fox2code.frl.utils.FRLEmptyArrays;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.BlockLogicSupplier;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.item.Item;
import net.minecraft.core.net.packet.PacketCustomPayload;
import net.minecraft.core.sound.BlockSounds;
import net.minecraft.core.util.HardIllegalArgumentException;
import net.minecraft.core.util.collection.NamespaceID;

import java.io.*;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public final class FRLRegistry {
	private static final File registry = new File(FRLCore.frlDirectory, "registry.bin");
	private static final Object2IntOpenHashMap<String> blockNamespaceIds = new Object2IntOpenHashMap<>();
	private static final Object2IntOpenHashMap<String> itemNamespaceIds = new Object2IntOpenHashMap<>();
	private static long staticRegistryHash;
	private static final int MISSING_BLOCK_ID = 13999;
	private static final int MIN_BLOCK_ID = 14000;
	private static final int MAX_BLOCK_ID = 16383;
	private static final int MISSING_ITEM_ID = 29999;
	private static final int MIN_ITEM_ID = 30000;
	private static final int MAX_ITEM_ID = 32768;
	private static int nextBlockId = MIN_BLOCK_ID;
	private static int nextItemId = MIN_ITEM_ID;
	private static boolean regInit = false;
	private static boolean dirty = false;
	private static boolean frozen = false;
	public static final Block<BlockLogicMissing> MISSING_BLOCK;
	public static final ItemMissing MISSING_ITEM;

	static {
		if (registry.isFile() && registry.length() > 0) {
			try (DataInputStream dataInputStream = new DataInputStream(
				new BufferedInputStream(new FileInputStream(registry)))) {
				readRegistry(dataInputStream);
			} catch (EOFException e) {
				blockNamespaceIds.clear();
				itemNamespaceIds.clear();
				nextBlockId = MIN_BLOCK_ID;
				nextItemId = MIN_ITEM_ID;
				dirty = true;
			} catch (IOException e) {
				throw new IOError(e);
			}
		}
		MISSING_BLOCK = Blocks.register("frl.missing_block",
				"frl:block/missing_block", MISSING_BLOCK_ID, BlockLogicMissing::new)
			.withSound(BlockSounds.STONE).withSetUnbreakable().withBlastResistance(6000000.0F)
			.withTags(BlockTags.PISTON_CRUSHING, BlockTags.CHAINLINK_FENCES_CONNECT, BlockTags.PREVENT_MOB_SPAWNS)
			.withDisabledStats().setBlockItem(ItemBlockMissing::new);
		MISSING_ITEM = new ItemMissing("frl.missing_item",
			"frl:item/missing_item", MISSING_ITEM_ID);
	}

	private static void readRegistry(DataInputStream dataInputStream) throws IOException {
		int headerVersion = dataInputStream.readUnsignedByte();
		if (headerVersion != 0) return;
		int blockEntryCount = dataInputStream.readUnsignedShort();
		nextBlockId = blockEntryCount + MIN_BLOCK_ID;
		for (int i = 0; i < blockEntryCount; i++) {
			String nameSpace = dataInputStream.readUTF();
			blockNamespaceIds.put(nameSpace, MIN_BLOCK_ID + i);
		}
		int itemEntryCount = dataInputStream.readUnsignedShort();
		nextItemId = itemEntryCount + MIN_ITEM_ID;
		for (int i = 0; i < itemEntryCount; i++) {
			String nameSpace = dataInputStream.readUTF();
			itemNamespaceIds.put(nameSpace, MIN_ITEM_ID + i);
		}
	}

	private static void writeRegistry(DataOutputStream dataOutputStream) throws IOException {
		dataOutputStream.writeByte(0);
		dataOutputStream.writeShort(blockNamespaceIds.size());
		for (String string : blockNamespaceIds.keySet()) {
			dataOutputStream.writeUTF(string);
		}
		dataOutputStream.writeShort(itemNamespaceIds.size());
		for (String string : itemNamespaceIds.keySet()) {
			dataOutputStream.writeUTF(string);
		}
	}

	public static <T extends BlockLogic> Block<T> registerBlock(
		String translationKey, String namespaceId, BlockLogicSupplier<T> logicSupplier) {
		validateNamespace(namespaceId);
		NamespaceID namespaceID;
		try {
			namespaceID = new NamespaceID(namespaceId);
		} catch (HardIllegalArgumentException e) {
			throw new IllegalArgumentException(e);
		}
		if (frozen) throw new IllegalStateException("Too late to register IDs");
		if (translationKey == null) {
			translationKey = namespaceId.replace(':', '.').replace('/', '.');
		}
		int nsBlockId = blockNamespaceIds.getInt(namespaceId);
		if (nsBlockId == 0) {
			nsBlockId = nextBlockId;
			nextBlockId++;
			blockNamespaceIds.put(namespaceId, nsBlockId);
			dirty = true;
		}
		UselessNumericalCompat.setBlockId(namespaceID, nsBlockId);
		return Blocks.register(translationKey, namespaceId, nsBlockId, logicSupplier);
	}

	public static int reserveItemId(String namespaceId) {
		validateNamespace(namespaceId);
		try {
			return reserveItemId(new NamespaceID(namespaceId));
		} catch (HardIllegalArgumentException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static int reserveItemId(NamespaceID namespaceId) {
		validateNamespace(namespaceId.toString());
		if (frozen) throw new IllegalStateException("Too late to register IDs");
		int nsItemId = itemNamespaceIds.getInt(namespaceId.toString());
		if (nsItemId == 0) {
			nsItemId = nextItemId;
			nextItemId++;
			itemNamespaceIds.put(namespaceId.toString(), nsItemId);
			dirty = true;
		}
		UselessNumericalCompat.setItemId(namespaceId, nsItemId);
		return nsItemId;
	}

	public static void validateNamespace(String namespaceId) {
		if (namespaceId.indexOf(':') == -1 || namespaceId.indexOf(' ') != -1) {
			throw new IllegalArgumentException("Invalid namespace: \"" + namespaceId + "\"");
		}
	}

	public static void notifyGameStarting() {
		FRLCore.init();
	}

	public static void notifyRegistryInitialized() {
		if (regInit) return;
		regInit = true;
	}

	public static void notifyGameInitialized() {
		if (!regInit) throw new IllegalStateException("Out of order events!");
		frozen = true;
		int low = blockNamespaceIds.hashCode();
		int high = itemNamespaceIds.hashCode();
		staticRegistryHash = ((long) high << 32) | (low & 0xFFFFFFFFL);
		if (FRLCore.isServer) {
			Server.compileRegistryPacket();
		} else {
			Client.initialize();
		}
		if (dirty || !registry.exists()) {
			dirty = false;
			try (DataOutputStream dataOutputStream = new DataOutputStream(
				new BufferedOutputStream(new FileOutputStream(registry)))) {
				writeRegistry(dataOutputStream);
			} catch (IOException ignored) {}
		}
		FRLTagRegistry.refreshTags();
		BTWailaCompat.notifyGameStarted();
		final Item missingItemBlock = MISSING_BLOCK.asItem();
		for (int i = MIN_BLOCK_ID; i < MAX_BLOCK_ID; i++) {
			if (Blocks.blocksList[i] == null) {
				Blocks.blocksList[i] = MISSING_BLOCK;
			}
			if (Item.itemsList[i] == null) {
				Item.itemsList[i] = missingItemBlock;
			}
		}
		for (int i = MIN_ITEM_ID; i < MAX_ITEM_ID; i++) {
			if (Item.itemsList[i] == null) {
				Item.itemsList[i] = MISSING_ITEM;
			}
		}
		GameStartedEvent.INSTANCE.callEvent();
	}

	public static long getStaticRegistryHash() {
		if (!frozen) throw new IllegalStateException("Registry is still mutable!");
		return staticRegistryHash;
	}

	@Environment(EnvType.CLIENT)
	public static final class Client {
		private Client() {}
		private static final Int2ObjectOpenHashMap<String> remoteBlockIdsNamespace = new Int2ObjectOpenHashMap<>();
		private static final Int2ObjectOpenHashMap<String> remoteItemIdsNamespace = new Int2ObjectOpenHashMap<>();

		static void initialize() {
			FRLModelDispatcher.ensureModelsForBlock(MISSING_BLOCK);
			FRLModelDispatcher.ensureModelsForItem(MISSING_ITEM);
			Client.resetToClientState();
		}

		public static void resetToNullState() {
			remoteBlockIdsNamespace.clear();
			remoteItemIdsNamespace.clear();
		}

		public static void resetToClientState() {
			resetToNullState();
			blockNamespaceIds.object2IntEntrySet().fastForEach(e ->
				remoteBlockIdsNamespace.put(e.getIntValue(), e.getKey()));
			itemNamespaceIds.object2IntEntrySet().fastForEach(e ->
				remoteItemIdsNamespace.put(e.getIntValue(), e.getKey()));
		}

		public static void initializeFromServerRegistryPacket(PacketCustomPayload packetCustomPayload)
			throws IOException {
			resetToNullState();
			try (DataInputStream dataInputStream = new DataInputStream(new InflaterInputStream(
				new ByteArrayInputStream(packetCustomPayload.data)))) {
				int headerVersion = dataInputStream.readUnsignedByte();
				if (headerVersion != 0) throw new IOException("Unsupported remote registry version: " + headerVersion);
				int blockEntryCount = dataInputStream.readUnsignedShort();
				for (int i = 0; i < blockEntryCount; i++) {
					String nameSpace = dataInputStream.readUTF();
					remoteBlockIdsNamespace.put(MIN_BLOCK_ID + i, nameSpace);
				}
				int itemEntryCount = dataInputStream.readUnsignedShort();
				for (int i = 0; i < itemEntryCount; i++) {
					String nameSpace = dataInputStream.readUTF();
					remoteItemIdsNamespace.put(MIN_ITEM_ID + i, nameSpace);
				}
			}
		}

		public static String getRemoteIdNameSpaceMissingText(int id) {
			String missingText = null;
			if (id >= MIN_BLOCK_ID && id <= MAX_BLOCK_ID) {
				missingText = remoteBlockIdsNamespace.get(id);
			} else if (id >= MIN_ITEM_ID && id <= MAX_ITEM_ID) {
				missingText = remoteItemIdsNamespace.get(id);
			}
			return missingText == null ? "#" + id : missingText;
		}

		public static boolean checkServerRegistryPacket(PacketCustomPayload packetCustomPayload) {
			byte[] b = packetCustomPayload.data;
			if (b.length != 8) return false;
			return staticRegistryHash == (((long) (b[0] & 0xFF) << 56) |
				((long) (b[1] & 0xFF) << 48) |
				((long) (b[2] & 0xFF) << 40) |
				((long) (b[3] & 0xFF) << 32) |
				((long) (b[4] & 0xFF) << 24) |
				((long) (b[5] & 0xFF) << 16) |
				((long) (b[6] & 0xFF) << 8)  |
				((long) (b[7] & 0xFF)));
		}
	}

	@Environment(EnvType.SERVER)
	public static final class Server {
		private static PacketCustomPayload REGISTRY_PACKET;
		private static String REGISTRY_PING_DATA = "\u0000\u0000";

		static void compileRegistryPacket() {
			REGISTRY_PING_DATA = blockNamespaceIds.size() + "\u0000" +
				itemNamespaceIds.size() + "\u0000" + staticRegistryHash;
			if (blockNamespaceIds.isEmpty() && itemNamespaceIds.isEmpty()) {
				REGISTRY_PACKET = new PacketCustomPayload(
					FRLNetConstants.REGISTRY_NULL,
					FRLEmptyArrays.EMPTY_BYTE_ARRAY);
				return;
			}
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			DeflaterOutputStream deflaterOutputStream;
			try (DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(
				deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream)))) {
				writeRegistry(dataOutputStream);
				dataOutputStream.flush();
				deflaterOutputStream.finish();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			byte[] data = byteArrayOutputStream.toByteArray();
			if (data.length > 32768) {
				long registryHash = getStaticRegistryHash();
				REGISTRY_PACKET = new PacketCustomPayload(FRLNetConstants.REGISTRY_SLIM, new byte[]{
					(byte) (registryHash >>> 56), (byte) (registryHash >>> 48),
					(byte) (registryHash >>> 40), (byte) (registryHash >>> 32),
					(byte) (registryHash >>> 24), (byte) (registryHash >>> 16),
					(byte) (registryHash >>> 8), (byte) registryHash,
				});
			} else {
				REGISTRY_PACKET = new PacketCustomPayload(FRLNetConstants.REGISTRY, data);
			}
		}

		public static PacketCustomPayload getPreCompiledRegistryPacket() {
			PacketCustomPayload preCompiledRegistryPacket = REGISTRY_PACKET;
			if (preCompiledRegistryPacket == null) throw new IllegalStateException();
			return preCompiledRegistryPacket;
		}

		public static String getRegistryPingData() {
			return REGISTRY_PING_DATA;
		}
	}
}
