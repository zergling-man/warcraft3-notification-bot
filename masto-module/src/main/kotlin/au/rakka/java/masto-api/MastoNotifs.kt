package au.rakka.java.mastoapi

// Required imports that make sense
import au.com.skater901.wc3.api.NotificationModule
// Required imports for some reason
import au.com.skater901.wc3.api.core.service.WC3GameNotificationService
import com.google.inject.Injector
// Maybe required imports
import kotlin.reflect.KClass

import org.slf4j.LoggerFactory

public class MastoNotificationModule : NotificationModule<MastoConfig, MastoNotifier, MastoReplyGuy>
{
	override val moduleName: String = "mastodon"
	override val configClass: KClass<MastoConfig> = MastoConfig::class
	private val logger = LoggerFactory.getLogger(MastoNotificationModule::class.java)
	override public fun initializeNotificationHandlers(config:MastoConfig, injector:Injector, wc3GameNotificationService:WC3GameNotificationService)
	{
		logger.debug("Blep")
	}
	override val gameNotifierClass: KClass<MastoNotifier> = MastoNotifier::class
	override val scheduledTaskClass: KClass<MastoReplyGuy> = MastoReplyGuy::class
}