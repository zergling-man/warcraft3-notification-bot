package au.com.skater901.w3cconnect.application.logging

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.filter.ThresholdFilter
import ch.qos.logback.classic.spi.Configurator
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.OutputStreamAppender
import ch.qos.logback.core.encoder.LayoutWrappingEncoder
import ch.qos.logback.core.rolling.DefaultTimeBasedFileNamingAndTriggeringPolicy
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy
import ch.qos.logback.core.spi.ContextAwareBase
import java.util.*

class LoggingConfiguration : Configurator, ContextAwareBase() {
    override fun configure(context: LoggerContext): Configurator.ExecutionStatus {
        context.stop()
        context.getRootLogger().detachAndStopAllAppenders()

        val consoleLogLevel = Level.INFO // TODO drive from config

        val fileLogLevel = Level.INFO // TODO drive from config

        // Pretty much all of this code is copied from DefaultLoggingFactory and FileAppenderFactory classes in dropwizard-logging library.
        if (consoleLogLevel != Level.OFF) {
            val consoleAppender = ConsoleAppender<ILoggingEvent>().apply {
                configure(context, consoleLogLevel)
                start()
            }

            context.getRootLogger().addAppender(consoleAppender)
        }

        if (fileLogLevel != Level.OFF) {
            val logsDirectory = "build/logs" // TODO drive from config

            val logPath = "$logsDirectory/wc3connect-discord-notification-bot/wc3connect-discord-notification-bot"

            val fileAppender = RollingFileAppender<ILoggingEvent>().apply {
                configure(context, fileLogLevel)
                file = "$logPath.log"
            }

            val rollingPolicy = TimeBasedRollingPolicy<ILoggingEvent>().apply {
                setContext(context)
                fileNamePattern = "$logPath-%d.tar.gz"
                maxHistory = System.getenv("logFileArchiveCount")?.toIntOrNull() ?: 7
            }

            DefaultTimeBasedFileNamingAndTriggeringPolicy<ILoggingEvent>().apply {
                setContext(context)
                setTimeBasedRollingPolicy(rollingPolicy)
                fileAppender.triggeringPolicy = this
            }

            fileAppender.rollingPolicy = rollingPolicy

            rollingPolicy.setParent(fileAppender)
            rollingPolicy.start()

            context.getRootLogger().addAppender(fileAppender)
            fileAppender.start()
        }

        return Configurator.ExecutionStatus.DO_NOT_INVOKE_NEXT_IF_ANY
    }

    private fun LoggerContext.getRootLogger(): Logger = getLogger(Logger.ROOT_LOGGER_NAME)

    private fun OutputStreamAppender<ILoggingEvent>.configure(context: LoggerContext, level: Level) {
        val loggingPattern = PatternLayout().apply {
            pattern = "%-5p [%d{ISO8601," + TimeZone.getDefault().id + "}] %c: %m%n"
            setContext(context)
            start()
        }

        val encoder = LayoutWrappingEncoder<ILoggingEvent>().apply { layout = loggingPattern }

        addFilter(ThresholdFilter().apply { setLevel(level.levelStr); start() })
        setContext(context)
        setEncoder(encoder)
    }
}