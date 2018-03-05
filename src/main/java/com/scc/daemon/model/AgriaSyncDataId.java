package com.scc.daemon.model;

import java.io.Serializable;

import javax.persistence.Embeddable;

@Embeddable
public class AgriaSyncDataId implements Serializable {

	private static final long serialVersionUID = 1L;
	private int id;
	private String action;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}

}
