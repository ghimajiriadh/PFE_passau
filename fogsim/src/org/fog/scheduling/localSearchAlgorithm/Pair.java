package org.fog.scheduling.localSearchAlgorithm;

public class Pair {

	private int cloudletId;
	private int fogId;
	
	public Pair(int cloudletId, int fogId) {
		this.setCloudletId(cloudletId);
		this.setFogId(fogId);
	}

	public int getCloudletId() {
		return cloudletId;
	}

	public void setCloudletId(int cloudletId) {
		this.cloudletId = cloudletId;
	}

	public int getFogId() {
		return fogId;
	}

	public void setFogId(int fogId) {
		this.fogId = fogId;
	}
}
