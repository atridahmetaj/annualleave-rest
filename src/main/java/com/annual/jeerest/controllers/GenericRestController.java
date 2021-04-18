package com.annual.jeerest.controllers;

import java.util.List;

/**
 * Useful for typical REST controllers
 * @param <E>
 *   The entity to return, it can be a DTO too
 * @param <R>
 *   The DTO you accept in request body
 */

public interface GenericRestController<E, R> {

    List<E> findAll();

    E getById(Long id);

    E create(R requestBody);

    E update(Long id, R requestBody);

    void delete(Long id);

}
