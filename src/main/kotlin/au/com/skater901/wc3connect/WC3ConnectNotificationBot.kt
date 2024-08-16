package au.com.skater901.wc3connect

import au.com.skater901.wc3connect.application.database.MigrationsManager
import au.com.skater901.wc3connect.application.module.*
import au.com.skater901.wc3connect.core.job.NotifyGamesJob
import au.com.skater901.wc3connect.core.service.NotificationService
import au.com.skater901.wc3connect.utils.ifNotEmpty
import com.google.inject.Guice
import java.io.File
import java.util.*

public fun main() {
    val configFilePath = System.getProperty("configFile")
        ?: throw IllegalArgumentException("Required system property [ configFile ] has not been set. Please set it, with a path to a config file, using -DconfigFile=/path/to/config/file.properties")

    // check file exists
    if (!File(configFilePath).exists()) throw IllegalArgumentException("Config file [ $configFilePath ] does not exist.")

    val notificationModules = ServiceLoader.load(NotificationModule::class.java)
        .map { it as NotificationModule<Any, Any> }

    notificationModules.groupBy { it.moduleName }
        .filter { it.value.size > 1 }
        .map { (moduleName, modules) ->
            "Multiple modules registered with name [ $moduleName ]: ${modules.joinToString { it::class.qualifiedName!! }}"
        }
        .ifNotEmpty { throw IllegalArgumentException(it.joinToString()) }

    val guiceModules = notificationModules.map { it.guiceModule() }

    val injector = Guice.createInjector(
        AppModule(),
        ClientModule(),
        ConfigModule(notificationModules),
        DatabaseModule(),
        GameNotifierModule(notificationModules),
        *guiceModules.toTypedArray()
    )

    // Run database migrations
    injector.getInstance(MigrationsManager::class.java).runMigrations()

    notificationModules.forEach {
        it.initializeNotificationHandlers(
            injector.getInstance(it.mainSystemClass.java),
            injector.getInstance(it.configClass.java),
            injector,
            injector.getInstance(NotificationService::class.java)
        )
    }

    injector.getInstance(NotifyGamesJob::class.java).start()

    // Hack to keep the app running
    while (true) {

    }
}