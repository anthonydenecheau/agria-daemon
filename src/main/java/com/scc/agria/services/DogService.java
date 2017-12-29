package com.scc.agria.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.stereotype.Service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.scc.agria.config.ServiceConfig;
import com.scc.agria.model.Dog;
import com.scc.agria.repository.DogRepository;
import com.scc.agria.template.ResponseObjectList;
import com.scc.agria.utils.UserContextHolder;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DogService {

    private static final Logger logger = LoggerFactory.getLogger(DogService.class);

    @Autowired
    private Tracer tracer;

    @Autowired
    private DogRepository dogRepository;

    @Autowired
    ServiceConfig config;

    public Dog getDogById(int dogId){
        Span newSpan = tracer.createSpan("getDogById");
        logger.debug("In the dogService.getDogById() call, trace id: {}", tracer.getCurrentSpan().traceIdString());
        try {
        	return dogRepository.findById(dogId);
        }
        finally{
          newSpan.tag("peer.service", "postgres");
          newSpan.logEvent(org.springframework.cloud.sleuth.Span.CLIENT_RECV);
          tracer.close(newSpan);
        }

    }

    @HystrixCommand(fallbackMethod = "buildFallbackDogIdentifiant",
            threadPoolKey = "dogByIdentifiantThreadPool",
            threadPoolProperties =
                    {@HystrixProperty(name = "coreSize",value="30"),
                     @HystrixProperty(name="maxQueueSize", value="10")},
            commandProperties={
                     @HystrixProperty(name="circuitBreaker.requestVolumeThreshold", value="10"),
                     @HystrixProperty(name="circuitBreaker.errorThresholdPercentage", value="75"),
                     @HystrixProperty(name="circuitBreaker.sleepWindowInMilliseconds", value="7000"),
                     @HystrixProperty(name="metrics.rollingStats.timeInMilliseconds", value="15000"),
                     @HystrixProperty(name="metrics.rollingStats.numBuckets", value="5")}
    )
    public ResponseObjectList<Dog> getDogByIdentifiant(String searchIdentifiant){

        Span newSpan = tracer.createSpan("getDogByIdentifiant");
        logger.debug("In the dogService.getDogByIdentifiant() call, trace id: {}", tracer.getCurrentSpan().traceIdString());
        try {
	    	/*
	        * norme ISO (FDXB) = 15 chiffres
	        */
	    	String regex = "^[0-9]{15}$";
	    	List<Dog> list = new ArrayList<Dog>(); 
	    	if (!searchIdentifiant.matches(regex))
	    		list = dogRepository.findByTatouage(searchIdentifiant);
	    	else
	    		list = dogRepository.findByTranspondeur(searchIdentifiant);
	    
	    	return new ResponseObjectList<Dog>(list.size(), list);
        }
	    finally{
	    	newSpan.tag("peer.service", "postgres");
	        newSpan.logEvent(org.springframework.cloud.sleuth.Span.CLIENT_RECV);
	        tracer.close(newSpan);
	    }
    }

    public void saveLicense(Dog license){

    }

    public void updateLicense(Dog license){

    }

    public void deleteLicense(Dog license){

    }

    private ResponseObjectList<Dog> buildFallbackDogIdentifiant(String searchIdentifiant){
    	
    	List<Dog> list = new ArrayList<Dog>(); 
    	list.add(new Dog()
                .withId(0))
    	;
        return new ResponseObjectList<Dog>(list.size(), list);
    }

}
