package gg.version.watch.model.nexus;

import java.util.List;

/**
 * Gokcen Guner
 * 06.12.2016
 */
public class Versioning {
  private String latest;
  private String release;
  private List<Version> versions;
  private String lastUpdated;

  public String getLastUpdated() {
    return lastUpdated;
  }

  public void setLastUpdated(String lastUpdated) {
    this.lastUpdated = lastUpdated;
  }

  public String getLatest() {
    return latest;
  }

  public void setLatest(String latest) {
    this.latest = latest;
  }

  public String getRelease() {
    return release;
  }

  public void setRelease(String release) {
    this.release = release;
  }

  public List<Version> getVersions() {
    return versions;
  }

  public void setVersions(List<Version> versions) {
    this.versions = versions;
  }
}
