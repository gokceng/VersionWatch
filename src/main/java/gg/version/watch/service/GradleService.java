package gg.version.watch.service;

import gg.version.watch.model.Dependency;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.GradleTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Gokcen Guner
 * 06.12.2016
 */
@Service
public class GradleService {
  private static final Logger LOGGER = LoggerFactory.getLogger(GradleService.class);

  public Set<Dependency> loadData(String projectPath, boolean includeTransitive) {
    LOGGER.info("Connecting to project {}", projectPath);
    GradleConnector connector = GradleConnector.newConnector();
    connector.forProjectDirectory(new File(projectPath));
    ProjectConnection connection = null;

    Set<Dependency> dependencySet = new HashSet<>();
    try {
      connection = connector.connect();
      GradleProject project = connection.getModel(GradleProject.class);
      LOGGER.info("Connected to project {}", projectPath);
      Set<Dependency> dependencies = getDependencies(connection, project, includeTransitive);
      dependencySet.addAll(dependencies);

      DomainObjectSet<? extends GradleProject> children = project.getChildren();
      for (GradleProject child : children) {
        Set<Dependency> d = getDependencies(connection, child, includeTransitive);
        dependencySet.addAll(d);
//        if (dependencySet.size() > 20) break;
      }
      LOGGER.info("{} total dependencies found for {}", dependencySet.size(), project.getName());
    } finally {
      if (connection != null) {
        connection.close();
      }
    }

    return dependencySet;
  }

  private Set<Dependency> getDependencies(ProjectConnection connection, GradleProject project, boolean includeTransitive) {
    Set<Dependency> dependencySet = new HashSet<>();

    DomainObjectSet<? extends GradleTask> tasks = project.getTasks();
    GradleTask task = null;
    for (GradleTask gt : tasks) {
      if ("dependencies".equals(gt.getName())) {
        task = gt;
        break;
      }
    }
    if (task == null) {
      throw new UnsupportedOperationException("Dependencies task doesn't exist!");
    }

    BuildLauncher buildLauncher = connection.newBuild();
    ByteArrayOutputStream standardOutput = new ByteArrayOutputStream(2048);
    buildLauncher.setStandardOutput(standardOutput);
    ByteArrayOutputStream errorOutput = new ByteArrayOutputStream(2048);
    buildLauncher.setStandardError(errorOutput);
    buildLauncher.forTasks(task).run();
    String error = errorOutput.toString();
    if (StringUtils.hasText(error)) {
      LOGGER.error("Error occured: {}. Project:{}", error, project.getName());
      return dependencySet;
    }

    String data = standardOutput.toString();
    String[] lines = StringUtils.tokenizeToStringArray(data, "\n", false, true);
    lines = StringUtils.removeDuplicateStrings(lines);
    Set<String> lineList = new HashSet<>();
    for (String line : lines) {
      line = getDependencyLine(includeTransitive, line);
      if (line == null) continue;
      lineList.add(line);
    }

    for (String line : lineList) {
      String[] strings = line.split(":");
      Dependency dependency = new Dependency(strings[0], strings[1], strings[2]);
      dependencySet.add(dependency);
    }
    LOGGER.info("{} dependencies found for {}", dependencySet.size(), project.getName());
    return dependencySet;
  }

  private String getDependencyLine(boolean includeTransitive, String line) {
    if (!includeTransitive && (!line.startsWith("\\--- ") && !line.startsWith("+--- "))) {
      return null;
    }
    if (line.startsWith(":")) {
      return null;
    }
    if (line.contains("(*)")) {
      return null;
    }
    if (StringUtils.countOccurrencesOf(line, ":") < 2) {
      return null;
    }
    return trimLine(line);
  }

  private String trimLine(String line) {
    line = StringUtils.delete(line, "+---");
    line = StringUtils.delete(line, "\\---");
    line = StringUtils.delete(line, "|");
    line = StringUtils.trimAllWhitespace(line);
    return line;
  }
}
