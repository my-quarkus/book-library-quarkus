package org.ciberaccion.booklibrary.dto;

import java.util.List;

public class PagedResponse<T> {

    public List<T> content;
    public int page;
    public int size;
    public long totalElements;
    public int totalPages;
    public boolean first;
    public boolean last;

    public static <T> PagedResponse<T> of(List<T> content, int page, int size, long totalElements) {
        PagedResponse<T> response = new PagedResponse<>();
        response.content = content;
        response.page = page;
        response.size = size;
        response.totalElements = totalElements;
        response.totalPages = (int) Math.ceil((double) totalElements / size);
        response.first = page == 0;
        response.last = page >= response.totalPages - 1;
        return response;
    }
}
