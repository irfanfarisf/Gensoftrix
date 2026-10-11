package com.gensoftrix.dao;

import java.sql.SQLException;
import java.util.List;

/**
 * Generic DAO interface demonstrating OOP Abstraction for database CRUD operations.
 *
 * @param <T> Entity type extending BaseEntity
 */
public interface CrudDAO<T> {

    T findById(int id) throws SQLException;

    List<T> findAllByParentId(int parentId) throws SQLException;

    T create(T entity) throws SQLException;

    void update(T entity) throws SQLException;

    void delete(int id) throws SQLException;
}
