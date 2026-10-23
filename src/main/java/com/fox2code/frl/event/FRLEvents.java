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
package com.fox2code.frl.event;

import com.fox2code.foxevents.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

public final class FRLEvents extends FoxEvents {
	private static final EventHolder<EventHandlerFailedEvent> ON_EVENT_ERROR =
		EventHolder.getHolderFromEvent(EventHandlerFailedEvent.class);
	public static FRLEvents INSTANCE = new FRLEvents();

	static {
		try {
			FoxEvents.setFoxEvents(INSTANCE);
		} catch (RuntimeException ignored) {
			FoxEvents.Unsafe.setFoxEventsUnsafe(INSTANCE);
		}
	}

	@Override
	public void registerEvents(@NotNull Object handler) throws EventRegistrationException {
		for (EventCallback eventCallback : this.getEventCallbacks(handler)) {
			this.registerEventCallback(eventCallback);
		}
	}

	@Override
	public void registerEvents(@NotNull Object handler,@Nullable BooleanSupplier validator)
		throws EventRegistrationException {
		for (EventCallback eventCallback : this.getEventCallbacks(handler, validator)) {
			this.registerEventCallback(eventCallback);
		}
	}

	@Override
	public void unregisterEvents(@NotNull Object handler) {
		this.unregisterEventsForClassLoader(FRLEvents.class.getClassLoader(), handler);
	}

	@Override
	protected void onEventError(@NotNull Event event, @NotNull EventCallback eventCallback, @NotNull Throwable throwable) {
		if (event.getClass() == EventHandlerFailedEvent.class) {
			this.unregisterEventCallback(eventCallback);
			return;
		}
		if (!ON_EVENT_ERROR.isEmpty()) {
			EventHandlerFailedEvent eventHandlerFailedEvent =
				new EventHandlerFailedEvent(event, throwable);
			ON_EVENT_ERROR.callEvent(eventHandlerFailedEvent);
			if (eventHandlerFailedEvent.unregisterHandler) {
				this.unregisterEventCallback(eventCallback);
			}
		}
	}

	@Override
	protected void onUnsafeAccess(String method) throws SecurityException {
		throw new SecurityException("Usage of unsafe API is not allowed!");
	}
}
