package com.aygo.eciComm.model.db;

import java.util.List;
import java.util.Optional;

import com.aygo.eciComm.model.Component;

public interface DynamoDBRepository<T extends Component> {

	T save(T item);

	Optional<T> findById(String id);

	List<T> findAll();

	void delete(String id);

	List<T> findByType(String type);
}