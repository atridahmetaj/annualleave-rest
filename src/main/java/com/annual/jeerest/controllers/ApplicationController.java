package com.annual.jeerest.controllers;

import com.annual.jeeshared.beans.ApplicationDTO;
import com.annual.jeeshared.constants.Constants;
import com.annual.jeeshared.entity.Application;
import com.annual.jeeshared.enums.ApplicationStatus;
import com.annual.jeeshared.service.ApplicationService;
import com.annual.jeeshared.service.EmailService;
import com.annual.jeeshared.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;

@RestController
@RequestMapping()
@CrossOrigin(origins = "http://localhost:4200/")
public class ApplicationController {

    private static final Logger logger = LogManager.getLogger(ApplicationController.class);

    @Autowired
    ApplicationService applicationService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService;


    @GetMapping("/{id}/applications")
    public List<Application> getAllApplications(@PathVariable("id") Long id){
        return applicationService.getUserApplications(userService.getById(id));
    }

    @GetMapping("/applications/{id}")
    public Application getApplication(@PathVariable("id") Long id){
        return applicationService.getById(id);
    }


    @PostMapping("/application")
    public ResponseEntity<Object> createApplication(@RequestBody ApplicationDTO applicationDTO) throws Exception {
        logger.debug(Constants.CREATING_LEAVING_APPLICATION);

        Application application = new Application();
        application.setFrom(applicationDTO.getFrom());
        application.setTo(applicationDTO.getTo());
        application.setApplicationType(applicationDTO.getApplicationType());
        application.setRequestedBy(userService.getById(applicationDTO.getUserId()));
        application.setStatus(ApplicationStatus.PENDING);

        int diffInDays = (int) ((application.getRequestedBy().getCreatedAt().getTime()-application.getFrom().getTime()) / (1000 * 60 * 60 * 24));

        if (diffInDays>Constants.PROBATION_PERIOD) {
            emailService.sendApplicationMail(application,Constants.APPLICATION_CREATED);
            return new ResponseEntity(applicationService.save(application), HttpStatus.OK);
        }
        return new ResponseEntity("You are tired already? You are to early for vacations",HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PutMapping(value = "/application/{id}")
    public ResponseEntity<?> updateApplication(@PathVariable("id") Long id, @RequestBody ApplicationDTO applicationDTO) throws ParseException {

        Application application = applicationService.getById(id);
        application.setFrom(applicationDTO.getFrom());
        application.setTo(applicationDTO.getTo());
        application.setApplicationType(applicationDTO.getApplicationType());

        logger.debug(Constants.UPDATING_LEAVING_APPLICATION);
        applicationService.save(application);

        emailService.sendApplicationMail(application,Constants.APPLICATION_UPDATED);

        return ResponseEntity.ok(application);
    }

    @DeleteMapping("/application/{id}")
    public void deleteApplication(@PathVariable("id") Long id) {
        applicationService.deleteById(id);
    }

    @DeleteMapping("/application")
    public void deleteApplication() {
        applicationService.deleteAllApplications();
    }
}
