package gg.version.watch.model;

import gg.version.watch.model.nexus.Metadata;
import gg.version.watch.model.nexus.Versioning;
import gg.version.watch.util.VersionComparator;
import org.springframework.data.annotation.Id;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * Gokcen Guner
 * 06.12.2016
 */
public class Dependency implements Comparable<Dependency> {
  private static final String ID_FORMAT = "%s:%s";
  private static final String EMPTY = "";

  @Id
  private final String dependencyId;
  private final String groupId;
  private final String artifactId;
  private String currentVersion;
  private String latestVersion;
  private String releaseVersion;

  private Dependency() {
    this(null, null);
  }

  public Dependency(String groupId, String artifactId) {
    this(groupId, artifactId, null);
  }

  public Dependency(String groupId, String artifactId, String currentVersion) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.currentVersion = currentVersion;
    this.dependencyId = String.format(ID_FORMAT, groupId, artifactId);
  }

  public Dependency(Metadata metadata) {
    Versioning versioning = metadata.getVersioning();
    String latestVersion = null;
    String releaseVersion = null;
    if (versioning != null) {
      latestVersion = versioning.getLatest();
      releaseVersion = versioning.getRelease();
    }
    String metadataVersion = metadata.getVersion();
    if (!StringUtils.hasText(latestVersion)) {
      latestVersion = metadataVersion;
    }
    if (!StringUtils.hasText(releaseVersion)) {
      releaseVersion = metadataVersion;
    }

    this.groupId = metadata.getGroupId();
    this.artifactId = metadata.getArtifactId();
    this.currentVersion = EMPTY;
    this.latestVersion = latestVersion;
    this.releaseVersion = releaseVersion;
    this.dependencyId = String.format(ID_FORMAT, groupId, artifactId);
  }

  public String getDependencyId() {
    return dependencyId;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public String getCurrentVersion() {
    return currentVersion;
  }

  public void setCurrentVersion(String currentVersion) {
    this.currentVersion = currentVersion;
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
    return new VersionComparator().compare(currentVersion, o.currentVersion);
  }
}
