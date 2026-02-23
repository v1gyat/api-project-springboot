package com.example.apiproject.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Generic paged response wrapper for list endpoints.
 * Converts Spring's Page<T> into a clean, frontend-friendly envelope.
 *
 * @param <T> The type of content in the page
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {

    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;

    /**
     * Factory method to create PagedResponse from Spring's Page object
     *
     * @param page The Spring Page object
     * @return PagedResponse with all pagination metadata
     */
    public static <T> PagedResponse<T> from(Page<T> page) {
        return new PagedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast());
    }
}
