package au.com.skater901.wc3.discord.core.dao

import net.dv8tion.jda.api.entities.Role

internal interface RoleNotificationDAO {
    suspend fun save(channelId: Long, role: Role)

    suspend fun find(channelId: Long): Role?
}