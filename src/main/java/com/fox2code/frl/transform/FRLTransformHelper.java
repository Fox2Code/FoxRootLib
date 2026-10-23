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
package com.fox2code.frl.transform;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public abstract class FRLTransformHelper {
	public FRLTransformHelper() {}

	public static @NotNull AbstractInsnNode getBooleanInsn(boolean bool) {
		return new InsnNode(bool ? Opcodes.ICONST_1 : Opcodes.ICONST_0);
	}

	public static @NotNull AbstractInsnNode getNumberInsn(int number) {
		if (number >= -1 && number <= 5)
			return new InsnNode(number + 3);
		else if (number >= -128 && number <= 127)
			return new IntInsnNode(Opcodes.BIPUSH, number);
		else if (number >= -32768 && number <= 32767)
			return new IntInsnNode(Opcodes.SIPUSH, number);
		else
			return new LdcInsnNode(number);
	}
}
