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
package com.fox2code.frl.tag;

import it.unimi.dsi.fastutil.objects.Object2ReferenceArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.data.tag.Tag;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.tag.ItemTags;
import net.minecraft.core.world.biome.Biome;
import net.minecraft.core.world.biome.BiomeTags;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public final class FRLTagRegistry {
	private static final Object2ReferenceArrayMap<String, Tag<Block<?>>> blockTags = new Object2ReferenceArrayMap<>();
	private static final ObjectSet<String> knownBlockTags = ObjectSets.unmodifiable(blockTags.keySet());
	private static final Object2ReferenceArrayMap<String, Tag<Item>> itemTags = new Object2ReferenceArrayMap<>();
	private static final ObjectSet<String> knownItemTags = ObjectSets.unmodifiable(itemTags.keySet());
	private static final Object2ReferenceArrayMap<String, Tag<Biome>> biomeTags = new Object2ReferenceArrayMap<>();
	private static final ObjectSet<String> knownBiomeTags = ObjectSets.unmodifiable(biomeTags.keySet());

	static {
		for (Tag<Block<?>> blockTag : BlockTags.TAG_LIST) {
			blockTags.put(blockTag.getName(), blockTag);
		}
		for (Tag<Item> itemTag : ItemTags.TAG_LIST) {
			itemTags.put(itemTag.getName(), itemTag);
		}
		for (Field field : BiomeTags.class.getDeclaredFields()) {
			if (((field.getModifiers() & (Modifier.PUBLIC | Modifier.STATIC)) ==
				(Modifier.PUBLIC | Modifier.STATIC)) && field.getType() == Tag.class) {
				try {
					@SuppressWarnings("unchecked")
					Tag<Biome> biomeTag = (Tag<Biome>) field.get(null);
					if (biomeTag != null) {
						biomeTags.put(biomeTag.getName(), biomeTag);
					}
				} catch (Exception ignored) {}
			}
		}
	}

	private FRLTagRegistry() {}

	public static Tag<Block<?>> getBlockTag(String blockTag) {
		if (blockTag == null) blockTag = "null";
		return blockTags.computeIfAbsent(blockTag, FRLTagRegistry::registerBlockTag);
	}

	private static Tag<Block<?>> registerBlockTag(String blockTag) {
		// Check saved tags.
		for (Tag<Block<?>> listedBlockTag : BlockTags.TAG_LIST) {
			if (blockTag.equals(listedBlockTag.getName())) {
				return listedBlockTag;
			}
		}
		Tag<Block<?>> tag = Tag.of(blockTag);
		BlockTags.TAG_LIST.add(tag);
		return tag;
	}

	public static ObjectSet<String> getKnownBlockTags() {
		return knownBlockTags;
	}

	public static Tag<Item> getItemTag(String itemTag) {
		if (itemTag == null) itemTag = "null";
		return itemTags.computeIfAbsent(itemTag, FRLTagRegistry::registerItemTag);
	}

	private static Tag<Item> registerItemTag(String itemTag) {
		// Check saved tags.
		for (Tag<Item> listedBlockTag : ItemTags.TAG_LIST) {
			if (itemTag.equals(listedBlockTag.getName())) {
				return listedBlockTag;
			}
		}
		Tag<Item> tag = Tag.of(itemTag);
		ItemTags.TAG_LIST.add(tag);
		return tag;
	}

	public static ObjectSet<String> getKnownItemTags() {
		return knownItemTags;
	}
	public static Tag<Biome> getBiomeTag(String biomeTag) {
		if (biomeTag == null) biomeTag = "null";
		return biomeTags.computeIfAbsent(biomeTag, FRLTagRegistry::registerBiomeTag);
	}

	private static Tag<Biome> registerBiomeTag(String itemTag) {
		return Tag.of(itemTag);
	}

	public static ObjectSet<String> getKnownBiomeTags() {
		return knownBiomeTags;
	}

	public static void refreshTags() {
		for (Tag<Block<?>> blockTag : BlockTags.TAG_LIST) {
			blockTags.putIfAbsent(blockTag.getName(), blockTag);
		}
		for (Tag<Item> itemTag : ItemTags.TAG_LIST) {
			itemTags.putIfAbsent(itemTag.getName(), itemTag);
		}
	}
}
