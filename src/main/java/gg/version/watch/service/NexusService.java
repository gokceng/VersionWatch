package gg.version.watch.service;

import gg.version.watch.model.Dependency;
import gg.version.watch.model.VersioneState;
import gg.version.watch.model.nexus.Metadata;
import gg.version.watch.model.nexus.Versioning;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Gokcen Guner
 * 06.12.2016
 */
@Service
public class NexusService {
  private static final Logger LOGGER = LoggerFactory.getLogger(NexusService.class);

  @Value("#{'${gg.version.watch.nexus.urls}'.split(',')}")
  private List<String> nexusUrls;
  private RestTemplate restTemplate;

  public NexusService() {
    this.restTemplate = new RestTemplateBuilder()
        .additionalMessageConverters(new Jaxb2RootElementHttpMessageConverter())
        .setConnectTimeout(3000)
        .setReadTimeout(5000)
        .build();
  }

  public Set<Dependency> updateDependencies(Set<Dependency> dependencySet, VersioneState versioneState) {
    if (VersioneState.ALL.equals(versioneState)) {
      return getAll(dependencySet);
    }
    return keepByFlag(dependencySet, versioneState);
  }

  private Set<Dependency> getAll(Set<Dependency> dependencySet) {
    for (Dependency dependency : dependencySet) {
      updateFromNexus(dependency);
    }
    return dependencySet;
  }

  private Set<Dependency> keepByFlag(Set<Dependency> dependencySet, VersioneState versioneState) {
    Iterator<Dependency> iterator = dependencySet.iterator();
    while (iterator.hasNext()) {
      Dependency next = iterator.next();
      updateFromNexus(next);
      VersioneState depVersioneState = next.getVersioneState();
      if (depVersioneState != versioneState) iterator.remove();
    }
    return dependencySet;
  }

  private void updateFromNexus(Dependency dependency) {
    Metadata metadata = getMetadata(dependency);
    if (metadata == null) {
      return;
    }
    Versioning versioning = metadata.getVersioning();
    String latest = versioning.getLatest();
    if (StringUtils.hasText(latest)) {
      dependency.setLatestVersion(latest);
    } else {
      dependency.setLatestVersion(metadata.getVersion());
    }

    String release = versioning.getRelease();
    if (StringUtils.hasText(release)) {
      dependency.setReleaseVersion(release);
    } else {
      dependency.setReleaseVersion(metadata.getVersion());
    }
  }

  private Metadata getMetadata(Dependency dependency) {
    String groupId = dependency.getGroupId();
    String groupPath = StringUtils.replace(groupId, ".", "/");
    String artifactPath = groupPath + "/" + dependency.getArtifactId() + "/maven-metadata.xml";
    ResponseEntity<Metadata> metadataResponseEntity = getMetadata(dependency, artifactPath);
    if (metadataResponseEntity == null) return null;
    return metadataResponseEntity.getBody();
  }

  private ResponseEntity<Metadata> getMetadata(Dependency dependency, String artifactPath) {
    for (String nexusUrl : nexusUrls) {
      ResponseEntity<Metadata> metadataFromNexus = getMetadataFromNexus(dependency, artifactPath, nexusUrl);
      if (metadataFromNexus != null) return metadataFromNexus;
    }
    LOGGER.error("Error for path {}", artifactPath);
    return null;
  }

  private ResponseEntity<Metadata> getMetadataFromNexus(Dependency dependency, String artifactPath, String nexusUrl) {
    ResponseEntity<Metadata> metadataResponseEntity;
    String url = nexusUrl + artifactPath;
    try {
      metadataResponseEntity = restTemplate.getForEntity(url, Metadata.class);
    } catch (RestClientException e) {
      return null;
    }
    HttpStatus statusCode = metadataResponseEntity.getStatusCode();
    if (!HttpStatus.OK.equals(statusCode)) {
      LOGGER.error("Code:{} Can't get metadata for {}.", statusCode, dependency);
      return null;
    }
    return metadataResponseEntity;
  }
}
