package com.scc.daemon.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "agria_sync_data")
public class AgriaSyncDog {

	@Id
	@Column(name = "id", nullable = false)
	private int id;

	@Column(name = "action")
	private String action;

	@Column(name = "on_transfert")
	private String transfert;
	
	@Column(name = "date_creation")
	private Date dateCreation;

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }

	public String getAction() { return action; }
	public void setAction(String action) { this.action = action; }

	public String getTransfert() { return transfert; }
	public void setTransfert(String transfert) { this.transfert = transfert; }

	public Date getDateCreation() { return dateCreation; }
	public void setDateCreation(Date dateCreation) { this.dateCreation = dateCreation; }

	
}
