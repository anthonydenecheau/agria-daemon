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

		Span newSpan = tracer.createSpan("AgriaScheduler");
        logger.debug("In the agriaScheduler.readDog() call, trace id: {}", tracer.getCurrentSpan().traceIdString());

		try {
        
	    	//logger.debug("readDog :: Execution Time - {}", dateTimeFormatter.format(LocalDateTime.now()));
	    	List<AgriaSyncDog> dogList = new ArrayList<AgriaSyncDog>();
	    	int idDog = 0;
	    	
	    	// 0. Lecture dans la table demande de synchro (ex: RAGRIA_SYNC_CHIEN) de l'ensemble des chiens pour lesquels 1 maj est demandée
	    	dogList = agriaService.getAllDogs();
	    	logger.debug("readDog :: dogList {}", dogList.size());
	    	
	    	// [[Boucle]] s/ le chien
	    	for (AgriaSyncDog syncDog : dogList) {
	    		try {
	    				
	    	    	// 1. Maj du chien de la table (RAGRIA_SYNC_CHIEN)
	    			idDog = syncDog.getId();
	    			syncDog.setTransfert("O");
	    			agriaService.saveDog(syncDog);
	    			
	    	    	// 2. Lecture des infos pour le chien à synchroniser 
	    			// Note : vue AGRIA_CHIEN (Oracle) == image de la table AGRIA_CHIEN (PostGRE)
	    			AgriaDog dog = agriaService.getDogById(idDog);
	    	    	
	    	    	// 3. Envoi du message à Agria-service pour maj Postgre
	    			agriaService.refreshDog(idDog, syncDog.getAction());
	    			
	    		} catch (Exception e) {
	    			logger.error(" idDog {} : {}", idDog, e.getMessage());
	    		} finally {
	    			
	    		}
	    	}	    	
	    	// [[Boucle]]
		
		} finally {
			newSpan.tag("peer.service", "scheduler");
			newSpan.logEvent(org.springframework.cloud.sleuth.Span.CLIENT_RECV);
			tracer.close(newSpan);
		}
    }
    
}