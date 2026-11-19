package com.smartcampus.dao;

import com.smartcampus.exception.DatabaseOperationException;
import java.util.List;

/**
 * Generic interface defining standard CRUD operations across DAOs.
 * Demonstrates Interfaces & Abstraction from the Java syllabus.
 * Shared dependency implemented by StudentDAO.
 */
public interface CrudRepository<T, ID> {
    void save(T entity) throws DatabaseOperationException;
    T findById(ID id) throws DatabaseOperationException;
    List<T> findAll() throws DatabaseOperationException;
    boolean update(T entity) throws DatabaseOperationException;
    boolean delete(ID id) throws DatabaseOperationException;
}
