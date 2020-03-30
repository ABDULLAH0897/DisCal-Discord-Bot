package org.dreamexposure.discal.core.object.web;

import org.dreamexposure.discal.core.object.GuildSettings;
import org.json.JSONObject;

import discord4j.core.object.entity.Role;

/**
 * Created by Nova Fox on 1/6/18.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class WebRole {
	private long id;
	private String name;

	private boolean managed;
	private boolean controlRole;

	private boolean everyone;


	//Getters
	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public boolean isManaged() {
		return managed;
	}

	public boolean isControlRole() {
		return controlRole;
	}

	public boolean isEveryone() {
		return everyone;
	}

	//Setters
	public void setId(long _id) {
		id = _id;
	}

	public void setName(String _name) {
		name = _name;
	}

	public void setManaged(boolean _managed) {
		managed = _managed;
	}

	public void setControlRole(boolean _control) {
		controlRole = _control;
	}

	public void setEveryone(boolean _everyone) {
		everyone = _everyone;
	}

	//functions
	public WebRole fromRole(Role r, GuildSettings settings) {
		id = r.getId().asLong();
		name = r.getName();

		managed = r.isManaged();

		everyone = r.isEveryone();

		if (r.isEveryone() && settings.getControlRole().equalsIgnoreCase("everyone"))
			controlRole = true;
		else
			controlRole = settings.getControlRole().equalsIgnoreCase(String.valueOf(id));


		return this;
	}

	public JSONObject toJson() {
		JSONObject data = new JSONObject();

		data.put("id", String.valueOf(id));
		data.put("name", name);
		data.put("managed", managed);
		data.put("control_role", controlRole);
		data.put("everyone", everyone);

		return data;
	}

	public WebRole fromJson(JSONObject data) {
		id = Long.parseLong(data.getString("id"));
		name = data.getString("name");
		managed = data.getBoolean("managed");
		controlRole = data.getBoolean("control_role");
		everyone = data.getBoolean("everyone");

		return this;
	}
}