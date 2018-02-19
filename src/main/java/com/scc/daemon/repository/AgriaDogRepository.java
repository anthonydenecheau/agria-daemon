package com.scc.daemon.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.scc.daemon.model.AgriaSyncDog;

@Repository
public interface AgriaDogRepository extends CrudRepository<AgriaSyncDog,String>  {
    public List<AgriaSyncDog> findByTransfert(String transfert);
    public AgriaSyncDog save(AgriaSyncDog dog);
    public void delete(AgriaSyncDog dog);
}
