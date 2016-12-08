package gg.version.watch.model;

import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * Gokcen Guner
 * 06.12.2016
 */
public class Dependency implements Comparable<Dependency> {
  private final String groupId;
  private final String artifactId;
  private final String currentVersion;
  private String latestVersion;
  private String releaseVersion;

  public Dependency(String groupId, String artifactId, String currentVersion) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.currentVersion = currentVersion;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public String getCurrentVersion() {
    return currentVersion;
  }

  public String getGroupId() {
    return groupId;
  }

  public String getLatestVersion() {
    return latestVersion;
  }

  public void setLatestVersion(String latestVersion) {
    this.latestVersion = latestVersion;
  }

  public String getReleaseVersion() {
    return releaseVersion;
  }

  public void setReleaseVersion(String releaseVersion) {
    this.releaseVersion = releaseVersion;
  }

  public VersioneState getVersioneState() {
    if (!StringUtils.hasText(latestVersion)) {
      return VersioneState.UNKNOWN;
    }
    return currentVersion.equals(latestVersion) ? VersioneState.UP_TO_DATE : VersioneState.OUTDATED;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Dependency that = (Dependency) o;
    return Objects.equals(groupId, that.groupId) &&
        Objects.equals(artifactId, that.artifactId) &&
        Objects.equals(currentVersion, that.currentVersion);
  }

  @Override
  public int hashCode() {
    return Objects.hash(groupId, artifactId, currentVersion);
  }

  @Override
  public String toString() {
    return "Dependency{" +
        "artifactId='" + artifactId + '\'' +
        ", groupId='" + groupId + '\'' +
        ", currentVersion='" + currentVersion + '\'' +
        ", latestVersion='" + latestVersion + '\'' +
        ", releaseVersion='" + releaseVersion + '\'' +
        '}';
  }

  @Override
  public int compareTo(Dependency o) {
    int groupIdComparison = this.groupId.compareTo(o.groupId);
    if (groupIdComparison != 0) {
      return groupIdComparison;
    }
    int artifactIdComparison = this.artifactId.compareTo(o.artifactId);
    if (artifactIdComparison != 0) {
      return artifactIdComparison;
    }
    return this.currentVersion.compareTo(o.currentVersion);
  }
}
