package com.castellanos;

import java.util.UUID;

public class EurekaTask {
	private String uuid;
	private long userId;
	private String name;
	private String script;
	private String dataset;
	private String resultFile;
	private String base_paht;
	public EurekaTask(){
		this.uuid = UUID.randomUUID().toString();
	}
	public String getBasePath(){
		return base_paht;
	}
	public void setBasePath(String base){
		this.base_paht = base;
	}
	public String getUuid() {
		return uuid;
	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDataset() {
		return dataset;
	}
	public void setDataset(String dataset) {
		this.dataset = dataset;
	}
	public String getResultFile() {
		return resultFile;
	}
	public void setResultFile(String resultFile) {
		this.resultFile = resultFile;
	}
	public String getScript() {
		return script;
	}
	public void setScript(String script) {
		this.script = script;
	}

	@Override
	public String toString() {
		return "EurekaTask [dataset=" + dataset + ", name=" + name + ", resultFile=" + resultFile + ", script=" + script
				+ ", userId=" + userId + ", uuid=" + uuid + "]";
	}
	
}
