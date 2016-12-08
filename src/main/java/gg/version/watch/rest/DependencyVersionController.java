package gg.version.watch.rest;

import gg.version.watch.model.DependencyInfo;
import gg.version.watch.model.VersioneState;
import gg.version.watch.service.DependencyService;
import gg.version.watch.service.ExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Gokcen Guner
 * 05.12.2016
 */
@Controller
@RequestMapping(value = "/rest/dependency")
public class DependencyVersionController {
  @Autowired
  private DependencyService dependencyService;
  @Autowired
  private ExcelService excelService;

  @ResponseBody
  @RequestMapping(method = RequestMethod.GET, value = "/dependencies", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<DependencyInfo> getDependencies_Json(@RequestParam("projectPath") String projectPath,
                                                             @RequestParam("versionState") VersioneState versionState,
                                                             @RequestParam("includeTransitive") boolean includeTransitive) {
    DependencyInfo dependencyInfo = dependencyService.retrieveDependencies(projectPath, versionState, includeTransitive);
    return ResponseEntity.ok(dependencyInfo);
  }

  @RequestMapping(method = RequestMethod.GET, value = "/dependencies")
  public void getDependencies_Excel(HttpServletResponse response,
                                    @RequestParam("projectPath") String projectPath,
                                    @RequestParam("versionState") VersioneState versionState,
                                    @RequestParam("includeTransitive") boolean includeTransitive) throws IOException {
    DependencyInfo dependencyInfo = dependencyService.retrieveDependencies(projectPath, versionState, includeTransitive);
    // Set the content type and attachment header.
    response.setHeader("Content-disposition", "attachment;filename=AllDependencies.xlsx");
    response.setContentType("application/vnd.ms-excel");
    ServletOutputStream outputStream = response.getOutputStream();
    excelService.saveToFile(dependencyInfo.getDependencySet(), outputStream);
    response.flushBuffer();
  }
}
