package gg.version.watch.model;

import java.util.Set;

/**
 * Gokcen Guner
 * 06.12.2016
 */
public class DependencyInfo {
  private int uptoDateDependencyCount;
  private int outdatedDependencyCount;
  private int unknownDependencyCount;
  private final Set<Dependency> dependencySet;

  public DependencyInfo(Set<Dependency> dependencySet) {
    this.dependencySet = dependencySet;
    for (Dependency dependency : dependencySet) {
      VersioneState versioneState = dependency.getVersioneState();
      switch (versioneState) {
        case OUTDATED:
          outdatedDependencyCount++;
          break;
        case UNKNOWN:
          unknownDependencyCount++;
          break;
        case UP_TO_DATE:
          uptoDateDependencyCount++;
          break;
      }
    }
  }

  public Set<Dependency> getDependencySet() {
    return dependencySet;
  }

  public int getOutdatedDependencyCount() {
    return outdatedDependencyCount;
  }

  public int getUnknownDependencyCount() {
    return unknownDependencyCount;
  }

  public int getUptoDateDependencyCount() {
    return uptoDateDependencyCount;
  }
}
