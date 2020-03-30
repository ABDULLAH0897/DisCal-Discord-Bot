package org.dreamexposure.discal.server.api.endpoints.v2.event;

import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.logger.Logger;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.web.AuthenticationState;
import org.dreamexposure.discal.core.utils.EventUtils;
import org.dreamexposure.discal.core.utils.JsonUtils;
import org.dreamexposure.discal.server.utils.Authentication;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import discord4j.core.object.util.Snowflake;

@RestController
@RequestMapping("/v2/events")
public class DeleteEventEndpoint {
	@PostMapping(value = "/delete", produces = "application/json")
	public String deleteEvent(HttpServletRequest request, HttpServletResponse response, @RequestBody String rBody) {
		//Authenticate...
		AuthenticationState authState = Authentication.authenticate(request);
		if (!authState.isSuccess()) {
			response.setStatus(authState.getStatus());
			response.setContentType("application/json");
			return authState.toJson();
		} else if (authState.isReadOnly()) {
			response.setStatus(401);
			response.setContentType("application/json");
			return JsonUtils.getJsonResponseMessage("Read-Only key not Allowed");
		}

		//Okay, now handle actual request.
		try {
			JSONObject requestBody = new JSONObject(rBody);

			String guildId = requestBody.getString("guild_id");
			int calNumber = requestBody.getInt("calendar_number");
			String eventId = requestBody.getString("event_id");


			//okay, lets actually delete the event
			GuildSettings settings = DatabaseManager.getManager().getSettings(Snowflake.of(guildId));

			if (EventUtils.eventExists(settings, calNumber, eventId)) {
				if (EventUtils.deleteEvent(settings, eventId)) {
					response.setContentType("application/json");
					response.setStatus(200);
					return JsonUtils.getJsonResponseMessage("Event successfully deleted");
				}


				//Something went wrong, but didn't error.... this should never happen...
				response.setContentType("application/json");
				response.setStatus(500);

				return JsonUtils.getJsonResponseMessage("Internal Server Error");
			} else {
				response.setContentType("application/json");
				response.setStatus(404);

				return JsonUtils.getJsonResponseMessage("Event Not Found");
			}
		} catch (JSONException e) {
			e.printStackTrace();

			response.setContentType("application/json");
			response.setStatus(400);

			return JsonUtils.getJsonResponseMessage("Bad Request");
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[API-v2] Failed to update event.", e, true, this.getClass());

			response.setContentType("application/json");
			response.setStatus(500);

			return JsonUtils.getJsonResponseMessage("Internal Server Error");
		}
	}
}