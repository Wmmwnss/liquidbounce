/*
 * This file is part of LiquidBounce (https://github.com/CCBlueX/LiquidBounce)
 *
 * Copyright (c) 2015 - 2025 CCBlueX
 *
 * LiquidBounce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LiquidBounce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LiquidBounce. If not, see <https://www.gnu.org/licenses/>.
 */
package net.ccbluex.liquidbounce.utils.kotlin

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RequestHandlerTest {

    // Create a test-specific implementation that implements RunningProvider without Minecraft dependencies
    class TestRunningProvider(name: String, var enabled: Boolean = true) : RunningProvider {
        override val running: Boolean
            get() = enabled
        val name: String = name
    }

    private lateinit var module1: TestRunningProvider
    private lateinit var module2: TestRunningProvider
    private lateinit var module3: TestRunningProvider
    private lateinit var module4: TestRunningProvider

    @BeforeEach
    fun resetModules() {
        module1 = TestRunningProvider("module1")
        module2 = TestRunningProvider("module2")
        module3 = TestRunningProvider("module3")
        module4 = TestRunningProvider("module4")
        
        module1.enabled = true
        module2.enabled = true
        module3.enabled = true
        module4.enabled = true
    }

    @Test
    fun testRequestHandler() {
        val requestHandler = RequestHandler<String>()

        assertNull(requestHandler.getActiveRequestValue())

        requestHandler.request(RequestHandler.Request(1000, -1, module1, "requestA"))
        requestHandler.request(RequestHandler.Request(3, 0, module2, "requestB"))
        requestHandler.request(RequestHandler.Request(2, 1, module3, "requestC"))
        requestHandler.request(RequestHandler.Request(1, 100, module4, "requestD"))

        assertEquals("requestD", requestHandler.getActiveRequestValue())
        requestHandler.tick()

        assertEquals("requestC", requestHandler.getActiveRequestValue())
        requestHandler.tick()

        assertEquals("requestB", requestHandler.getActiveRequestValue())
        requestHandler.tick()

        assertEquals("requestA", requestHandler.getActiveRequestValue())
        requestHandler.tick()

        module1.enabled = false

        requestHandler.tick()

        assertNull(requestHandler.getActiveRequestValue())
    }
}
