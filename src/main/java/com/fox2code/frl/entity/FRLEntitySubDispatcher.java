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
package com.fox2code.frl.entity;

import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.EntityDispatcher;
import net.minecraft.core.entity.factories.EntityDontUseFactory;
import net.minecraft.core.entity.factories.EntityFactory;
import net.minecraft.core.util.collection.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * This allows for servers to use an entity either without needing for the client to have a compatible equivalent or
 * to allow entities to be safely downgraded to vanilla equivalent of those entities when missing.
 */
public final class FRLEntitySubDispatcher {
	private static final HashMap<String, EntitySubDispatcherEntry<?>> SUB_DISPATCHERS = new HashMap<>();
	private static final IdentityHashMap<Class<?>, EntitySubDispatcherEntry<?>>
		CLS_TO_SUB_DISPATCHERS = new IdentityHashMap<>();
	private static final Collection<EntitySubDispatcherEntry<?>> SUB_DISPATCHERS_COLLECTION =
		Collections.unmodifiableCollection(SUB_DISPATCHERS.values());

	private FRLEntitySubDispatcher() {}

	public static <T extends Entity> EntitySubDispatcherEntry<T> registerSubDispatcher(
		Class<T> entityClass, NamespaceID namespaceID,String originalId,
		EntityFactory<T> factory, boolean netClarity) {
		EntityDispatcher.EntityDispatcherEntry<?> originalEntry =
			EntityDispatcher.getInstance().entryForId(originalId);
		if (originalEntry == null) {
			throw new IllegalArgumentException("Unable to find entity entry for " + originalId);
		}
		return registerSubDispatcher(entityClass, namespaceID, originalEntry, factory, netClarity);
	}

	public static <T extends Entity> EntitySubDispatcherEntry<T> registerSubDispatcher(
		Class<T> entityClass, NamespaceID namespaceID,NamespaceID originalId,
		EntityFactory<T> factory, boolean netClarity) {
		EntityDispatcher.EntityDispatcherEntry<?> originalEntry =
			EntityDispatcher.getInstance().entryForId(originalId);
		if (originalEntry == null) {
			throw new IllegalArgumentException("Unable to find entity entry for " + originalId);
		}
		return registerSubDispatcher(entityClass, namespaceID, originalEntry, factory, netClarity);
	}

	public static <T extends Entity> EntitySubDispatcherEntry<T> registerSubDispatcher(
		Class<T> entityClass, NamespaceID namespaceID,
		EntityDispatcher.EntityDispatcherEntry<?> originalEntry, EntityFactory<T> factory, boolean netClarity) {
		Objects.requireNonNull(entityClass);
		Objects.requireNonNull(namespaceID);
		Objects.requireNonNull(originalEntry);
		Objects.requireNonNull(factory);
		if (originalEntry instanceof EntitySubDispatcherEntry<?>) {
			throw new IllegalArgumentException(
				"Cannot sub register on a sub dispatcher, did you mean to use \"" +
					originalEntry.namespaceID + "\" instead?");
		}
		if (CLS_TO_SUB_DISPATCHERS.containsKey(entityClass) ||
			SUB_DISPATCHERS.containsKey(namespaceID.toString())) {
			throw new IllegalStateException(
				"Cannot register the same ID twice: " + namespaceID);
		}
		if (EntityDispatcher.getInstance().classToEntryMap.containsKey(entityClass) ||
			EntityDispatcher.getInstance().idToEntryMap.containsKey(namespaceID)) {
			throw new IllegalStateException(
				"Cannot register the same entity in the main dispatcher and the sub dispatcher!");
		}
		EntitySubDispatcherEntry<T> replacementEntry =
			new EntitySubDispatcherEntry<>(namespaceID,
				originalEntry, entityClass, factory, netClarity);
		SUB_DISPATCHERS.put(namespaceID.toString(), replacementEntry);
		CLS_TO_SUB_DISPATCHERS.put(entityClass, replacementEntry);
		return replacementEntry;
	}

	public static @Nullable EntitySubDispatcherEntry<?> getSubDispatcher(NamespaceID namespaceID) {
		return namespaceID == null ? null : SUB_DISPATCHERS.get(namespaceID.toString());
	}

	public static @Nullable EntitySubDispatcherEntry<?> getSubDispatcher(String id) {
		return SUB_DISPATCHERS.get(id);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Entity> @Nullable EntitySubDispatcherEntry<T> getSubDispatcher(Class<T> id) {
		return (EntitySubDispatcherEntry<T>) CLS_TO_SUB_DISPATCHERS.get(id);
	}

	public static Collection<EntitySubDispatcherEntry<?>> entries() {
		return SUB_DISPATCHERS_COLLECTION;
	}

	public static class EntitySubDispatcherEntry<T extends Entity> extends EntityDispatcher.EntityDispatcherEntry<T> {
		public final EntityDispatcher.EntityDispatcherEntry<?> originalEntry;
		public final boolean netClarity;
		public final boolean subSavable;

		public EntitySubDispatcherEntry(
			@NotNull NamespaceID namespaceID, EntityDispatcher.EntityDispatcherEntry<?> originalEntry,
			@NotNull Class<T> entityClass, @NotNull EntityFactory<T> factory, boolean netClarity) {
			super(namespaceID, originalEntry.numericID, entityClass, factory, null);
			this.originalEntry = originalEntry;
			this.netClarity = netClarity;
			this.subSavable = factory.getClass() != EntityDontUseFactory.class;
		}

		@Override
		public void updateID(int newNumericID) {
			this.numericID = this.originalEntry.numericID;
		}
	}
}
