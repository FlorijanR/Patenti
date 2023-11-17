package hr.fer.patenti.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import hr.fer.patenti.domain.Result;

@Repository
public interface ResultRepository extends JpaRepository<Result, Long>{

}
