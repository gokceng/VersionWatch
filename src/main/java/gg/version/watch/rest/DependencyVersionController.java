package gg.version.watch.rest;

import gg.version.watch.model.Dependency;
import gg.version.watch.model.DependencyInfo;
import gg.version.watch.model.VersioneState;
import gg.version.watch.service.ExcelService;
import gg.version.watch.service.GradleService;
import gg.version.watch.service.NexusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

/**
 * Gokcen Guner
 * 05.12.2016
 */
@Controller
@RequestMapping(value = "/rest/dependency")
public class DependencyVersionController {
  @Autowired
  private NexusService nexusService;
  @Autowired
  private GradleService gradleService;
  @Autowired
  private ExcelService excelService;

  @ResponseBody
  @RequestMapping(method = RequestMethod.GET, value = "/dependencies", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<DependencyInfo> getDependencies_Json(@RequestParam("projectPath") String projectPath,
                                                             @RequestParam("versionState") VersioneState versionState,
                                                             @RequestParam("includeTransitive") boolean includeTransitive) {
    Set<Dependency> dependencySet = gradleService.loadData(projectPath, includeTransitive);
    if (CollectionUtils.isEmpty(dependencySet)) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    Set<Dependency> dependencies = nexusService.updateDependencies(dependencySet, versionState);
    return ResponseEntity.ok(new DependencyInfo(dependencies));
  }

  @RequestMapping(method = RequestMethod.GET, value = "/dependencies")
  public void getDependencies_Excel(HttpServletResponse response,
                                    @RequestParam("projectPath") String projectPath,
                                    @RequestParam("versionState") VersioneState versionState,
                                    @RequestParam("includeTransitive") boolean includeTransitive) throws IOException {
    Set<Dependency> dependencySet = gradleService.loadData(projectPath, includeTransitive);
    if (CollectionUtils.isEmpty(dependencySet)) {
      return;
    }
    Set<Dependency> dependencies = nexusService.updateDependencies(dependencySet, versionState);
    // Set the content type and attachment header.
    response.setHeader("Content-disposition", "attachment;filename=AllDependencies.xlsx");
    response.setContentType("application/vnd.ms-excel");
    ServletOutputStream outputStream = response.getOutputStream();
    excelService.saveToFile(dependencies, outputStream);
    response.flushBuffer();
  }
}
