package gg.version.watch.model.nexus;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Gokcen Guner
 * 06.12.2016
 */
@XmlRootElement
public class Metadata {
  private String groupId;
  private String artifactId;
  private String version;
  private Versioning versioning;

  public String getArtifactId() {
    return artifactId;
  }

  public void setArtifactId(String artifactId) {
    this.artifactId = artifactId;
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public Versioning getVersioning() {
    return versioning;
  }

  public void setVersioning(Versioning versioning) {
    this.versioning = versioning;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }
}
