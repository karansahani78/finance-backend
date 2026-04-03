package com.finance.dto.response;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

@Getter
public class PageResponse<T> {

    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean last;

    private PageResponse(Page<?> sourcePage, List<T> mappedContent) {
        this.content = mappedContent;
        this.page = sourcePage.getNumber();
        this.size = sourcePage.getSize();
        this.totalElements = sourcePage.getTotalElements();
        this.totalPages = sourcePage.getTotalPages();
        this.last = sourcePage.isLast();
    }

    public static <S, T> PageResponse<T> of(Page<S> page, Function<S, T> mapper) {
        List<T> mapped = page.getContent().stream().map(mapper).toList();
        return new PageResponse<>(page, mapped);
    }

    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(page, page.getContent());
    }
}
