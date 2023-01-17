package com.ed.geospatial.core.versioning;

public class AtmState {
    private String id;
    private long latestCommittedVersion = -1;
    private long inSyncVersion = -1;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getLatestCommittedVersion() {
        return latestCommittedVersion;
    }

    public void setLatestCommittedVersion(long latestCommittedVersion) {
        this.latestCommittedVersion = latestCommittedVersion;
    }

    public long getInSyncVersion() {
        return inSyncVersion;
    }

    public void setInSyncVersion(long inSyncVersion) {
        this.inSyncVersion = inSyncVersion;
    }
}
