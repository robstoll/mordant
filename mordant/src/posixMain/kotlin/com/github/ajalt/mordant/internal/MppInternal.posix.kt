package com.github.ajalt.mordant.internal

import com.github.ajalt.mordant.terminal.TerminalInterface
import com.github.ajalt.mordant.terminal.`interface`.SyscallHandlerNativePosix

internal actual fun getStandardTerminalInterface(): TerminalInterface = SyscallHandlerNativePosix
