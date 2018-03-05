package com.scc.tasks;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.scc.daemon.model.AgriaDog;
import com.scc.daemon.model.AgriaSyncDog;
import com.scc.daemon.services.AgriaService;

@Component
public class AgriaScheduler {
    
	private static final Logger logger = LoggerFactory.getLogger(AgriaScheduler.class);
	
	private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
	
    @Autowired
    private Tracer tracer;
    
	@Autowired
	AgriaService agriaService;
	
	@Scheduled (fixedDelayString = "${fixedDelay.in.milliseconds}")
    public void readDog() {

		try {
        
	    	//logger.debug("readDog :: Execution Time - {}", dateTimeFormatter.format(LocalDateTime.now()));
	    	List<AgriaSyncDog> dogList = new ArrayList<AgriaSyncDog>();
	    	int idDog = 0;
	    	
	    	// 0. Lecture dans la table demande de synchro (ex: RAGRIA_SYNC_CHIEN) de l'ensemble des chiens pour lesquels 1 maj est demandée
	    	dogList = agriaService.getAllDogs();
	    	logger.debug("readDog :: dogList {}", dogList.size());
	    	
	    	if (dogList.size() > 0 ) {
		    	// [[Boucle]] s/ le chien
		    	for (AgriaSyncDog syncDog : dogList) {
		    		
		    		Span newSpan = tracer.createSpan("AgriaScheduler");
		            logger.debug("In the agriaScheduler.readDog() call, trace id: {}", tracer.getCurrentSpan().traceIdString());

		            try {
		    				
		    	    	// 1. Maj du chien de la table (AGRIA_SYNC_DATA)
		    			idDog = syncDog.getId();
		    			syncDog.setTransfert("O");
		    			agriaService.saveDog(syncDog);
		    			
		    	    	// 2. Lecture des infos pour le chien à synchroniser 
		    			// Note : vue AGRIA_CHIEN (Oracle) == image de la table AGRIA_CHIEN (PostGRE)
		    			// Si UPDATE/INSERT et dog == null alors le chien n'est pas dans le périmètre -> on le supprime de la liste
		    			// + DELETE, dog == null -> on publie uniquement l'id à supprimer
		    			AgriaDog dog = new AgriaDog();
		    			if (!syncDog.getAction().equals("D")) {
		    				dog = agriaService.getDogById(idDog);
		    				if (dog == null ) {
		    					agriaService.deleteDog(syncDog);
		    					continue;		    	
		    				}
		    			} else
		    				dog.withId(idDog);	
		    			
		    	    	// 3. Envoi du message à agria-service pour maj Postgre
		    			agriaService.refreshDog(dog, syncDog.getAction());
		    			
		    		} catch (Exception e) {
		    			logger.error(" idDog {} : {}", idDog, e.getMessage());
		    		} finally {
		    			newSpan.tag("peer.service", "agriascheduler");
		    			newSpan.logEvent(org.springframework.cloud.sleuth.Span.CLIENT_RECV);
		    			tracer.close(newSpan);		    			
		    		}
		    	}	    	
		    	// [[Boucle]]
	    	}
		
		} finally {
		}
    }
    
}
