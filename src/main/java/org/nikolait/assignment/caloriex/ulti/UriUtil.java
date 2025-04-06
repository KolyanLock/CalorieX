package org.nikolait.assignment.caloriex.ulti;

import lombok.experimental.UtilityClass;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@UtilityClass
public class UriUtil {
    public URI buildResourceUriForId(Long resourceId) {
        return ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(resourceId)
                .toUri();
    }

    public URI buildResourceUriForPath(String path) {
        return ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/" + path)
                .build()
                .toUri();
    }
}
