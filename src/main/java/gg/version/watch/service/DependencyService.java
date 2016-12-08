package gg.version.watch.service;

import gg.version.watch.model.Dependency;
import gg.version.watch.model.DependencyInfo;
import gg.version.watch.model.VersioneState;
import gg.version.watch.repo.DependencyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Gokcen Guner
 * 08.12.2016
 */
@Service
public class DependencyService {
  private static final Logger LOGGER = LoggerFactory.getLogger(DependencyService.class);

  @Autowired
  private DependencyRepository repository;
  @Autowired
  NexusService nexusService;
  @Autowired
  private GradleService gradleService;

  public DependencyInfo retrieveDependencies(String projectPath, VersioneState versionState, boolean includeTransitive) {
    Set<Dependency> dependencySet = gradleService.loadData(projectPath, includeTransitive);
    Set<Dependency> dependencies = retrieveDependencies(dependencySet, versionState);
    return new DependencyInfo(dependencies);
  }

  private Set<Dependency> retrieveDependencies(Set<Dependency> dependencySet, VersioneState versioneState) {
    Set<Dependency> dependencies = updateDependencies(dependencySet);
    return filterDependencies(dependencies, versioneState);
  }

  private Set<Dependency> updateDependencies(Set<Dependency> dependencySet) {
    Set<Dependency> dependencies = new TreeSet<>();
    for (Dependency dependency : dependencySet) {
      String dependencyId = dependency.getDependencyId();
      Dependency one = repository.findOne(dependencyId);
      if (one == null) {
        LOGGER.warn("Can't find {} in DB.", dependencyId);
        one = nexusService.retrieveDependency(dependency.getGroupId(), dependency.getArtifactId());
        if (one != null) {
          one = repository.save(one);
        }
      }
      if (one == null) {
        LOGGER.warn("Can't find {} in Nexus.", dependencyId);
        continue;
      }
      one.setCurrentVersion(dependency.getCurrentVersion());
      dependencies.add(one);
    }
    return dependencies;
  }

  private Set<Dependency> filterDependencies(Set<Dependency> dependencies, VersioneState versioneState) {
    if (VersioneState.ALL.equals(versioneState)) {
      return dependencies;
    }
    Iterator<Dependency> iterator = dependencies.iterator();
    while (iterator.hasNext()) {
      Dependency next = iterator.next();
      if (!next.getVersioneState().equals(versioneState)) {
        iterator.remove();
      }
    }
    return dependencies;
  }
}
