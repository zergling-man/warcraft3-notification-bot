package au.rakka.java.mastoapi

import au.com.skater901.wc3.api.scheduled.ScheduledTask
import au.com.skater901.wc3.api.core.service.WC3GameNotificationService
import java.net.http.HttpClient // This should be significantly less hassle.
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.URI
// Used to get my config class in here
import jakarta.inject.Inject

import kotlinx.coroutines.future.await
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.JsonNode

import org.slf4j.LoggerFactory

public class MastoReplyGuy @Inject constructor(private val conf:MastoConfig, private val wc3GameNotificationService: WC3GameNotificationService) : ScheduledTask
{
	private val logger = LoggerFactory.getLogger(MastoReplyGuy::class.java)
	private val mapper = ObjectMapper()
	
	private val client: HttpClient = HttpClient.newHttpClient()
	private val builder: HttpRequest.Builder = HttpRequest.newBuilder().setHeader("Authorization","Bearer ${conf.token}").setHeader("Content-Type","application/json")
	private val baseurl = "https://${conf.instance}/api/v1/" // Well it was half useful
	private val notifurl = "${baseurl}notifications"
	private val clearurl = "${baseurl}notifications/clear"
	
	
	override val schedule: Int = 30
	override suspend fun task()
	{
		logger.debug("Blep")
		var finished=false
		var max_id=""
		while (!finished)
		{
			val response = client.sendAsync(builder.uri(URI.create(notifurl+max_id)).GET().build(),HttpResponse.BodyHandlers.ofInputStream()).await()
			logger.debug(response.statusCode().toString())
			val node=mapper.readTree(response.body())
			node.forEach{ process_post(it) }
			logger.debug(node.size().toString())
			finished=node.size()<20
			max_id=node.get(-1).get("id").asText()
		}
	}
	private suspend fun process_post(post:JsonNode)
	{
		logger.debug("Processing notif "+post.get("id"))
		if (post.get("type").asText()!="mention") {logger.debug("Was not a mention");return}
		val text= post.get("status").get("pleroma").get("content").get("text/plain").asText()
		val (tag,regex)=process_post_contents(text)
		if (tag=="") {logger.debug("tag: ${tag} was null");return}
		if (regex=="")
		{
			logger.debug("Deleting ${tag}")
			wc3GameNotificationService.deleteNotification(tag) // lol hope you meant it
		}
		else
		{
			logger.debug("Adding ${tag} with ${regex}")
			wc3GameNotificationService.createNotification(tag,regex)
		}
	}
	private fun process_post_contents(contents:String) : Pair<String,String>
	{
		logger.debug("Processing post contents")
		var tag=""
		var regex=""
		val words=contents.split(' ')
		for (word in words)
		{
			logger.debug("Testing word ${word}")
			if (word[0]=='@') {continue}
			if (word[0]=='#' && tag=="") {tag=word; continue}
			if (regex=="") {regex=word}
		}
		logger.debug("Reporting ${tag} and ${regex}")
		return Pair(tag,regex)
	}
}