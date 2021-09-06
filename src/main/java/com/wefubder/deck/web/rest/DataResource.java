package com.wefubder.deck.web.rest;

import com.wefubder.deck.exciptions.FileNotFoundException;
import com.wefubder.deck.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/data")
@CrossOrigin("*")
public class DataResource {

    private final Logger log = LoggerFactory.getLogger(DeckResource.class);
    private final FileService fileService;

    public DataResource(FileService fileService) {
        this.fileService = fileService;
    }


    @GetMapping("/{filePath}/**")
    private ResponseEntity<Resource> loadResource(@PathVariable String filePath, HttpServletRequest request) {
        try {
            final String path =
                    request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
            final String bestMatchingPattern =
                    request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE).toString();
            String arguments = new AntPathMatcher().extractPathWithinPattern(bestMatchingPattern, path);
            if (!arguments.isEmpty()) {
                filePath = filePath + '/' + arguments;
            }
            // Load file as Resource
            Resource resource = fileService.loadFileAsResource(filePath);
            // Try to determine file's content type
            String contentType = null;
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());

            // Fallback to the default content type if type could not be determined
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception ex) {
            throw new FileNotFoundException("file");
        }
    }
}
