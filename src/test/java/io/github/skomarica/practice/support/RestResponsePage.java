package io.github.skomarica.practice.support;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sinisa Komarica
 */
public class RestResponsePage<T> extends PageImpl<T> {

    public RestResponsePage(List<T> content, Pageable pageable, Long total) {
        super(content, pageable, total);
    }

    public RestResponsePage(List<T> content) {
        super(content);
    }

    public RestResponsePage() {
        super(new ArrayList<T>());
    }

}