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

import net.ccbluex.liquidbounce.features.module.ClientModule
import net.ccbluex.liquidbounce.utils.kotlin.RunningProvider
import net.ccbluex.liquidbounce.utils.client.logger
import net.minecraft.client.MinecraftClient
import java.util.concurrent.PriorityBlockingQueue

class RequestHandler<T> {

    private var currentTick = 0

    private val activeRequests = PriorityBlockingQueue<Request<T>>(11, compareBy { -it.priority })

    fun tick(deltaTime: Int = 1) {
        currentTick += deltaTime
    }

    fun request(request: Request<T>) {
        // we remove all requests provided by module on new request
        activeRequests.removeIf { it.provider === request.provider }
        request.expiresIn += currentTick
        activeRequests.add(request)
    }

    fun getActiveRequestValue(): T? {
        var top = activeRequests.peek() ?: return null

        // Check if Minecraft client is available before accessing thread status
        val minecraftClient = try {
            MinecraftClient.getInstance()
        } catch (e: Exception) {
            // If Minecraft client is not available (e.g., in tests), just return the value directly
            logger.debug("Minecraft client not available, returning value directly", e)
            return top.value
        }

        if (minecraftClient?.isOnThread != false) {
            // we remove all outdated requests here - same as original logic: (expired OR not running)
            while (true) {
                val isExpired = top.expiresIn <= currentTick
                val isProviderRunning = try {
                    top.provider.running
                } catch (e: Exception) {
                    // If we can't access the running property (e.g., in tests), assume it's not running
                    logger.debug("Failed to access provider running state, assuming not running", e)
                    false
                }
                
                if (isExpired || !isProviderRunning) {
                    // Either expired or not running -> remove
                    activeRequests.remove()
                    top = activeRequests.peek() ?: return null
                } else {
                    // Not expired AND running -> stop removing
                    break
                }
            }
        }

        return top.value
    }

    /**
     * A requested state of the system.
     *
     * Note: A request is deleted when its corresponding module is disabled.
     *
     * @param expiresIn in how many ticks units should this request expire?
     * @param priority higher = higher priority
     * @param provider module which requested value
     */
    class Request<T>(
        var expiresIn: Int, val priority: Int, val provider: RunningProvider, val value: T
    )
}
