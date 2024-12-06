package com.aygo.eciComm.model.db;

import java.util.List;
import java.util.Optional;

import com.aygo.eciComm.model.BaseItem;

public interface DynamoDBRepository<T extends BaseItem> {

	T save(T item);

	Optional<T> findById(String id);

	List<T> findAll();

	void delete(String id);

	List<T> findByType(String type);
}