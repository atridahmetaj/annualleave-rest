package com.annual.jeerest.controllers;

import com.annual.jeeshared.beans.ApplicationDTO;
import com.annual.jeeshared.beans.PostVacationDTO;
import com.annual.jeeshared.constants.Constants;
import com.annual.jeeshared.entity.Application;
import com.annual.jeeshared.enums.ApplicationStatus;
import com.annual.jeeshared.service.ApplicationService;
import com.annual.jeeshared.service.EmailService;
import com.annual.jeeshared.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.Date;

@RestController
@RequestMapping()
public class ApplicationController {

    private static final Logger logger = LogManager.getLogger(ApplicationController.class);

    @Autowired
    ApplicationService applicationService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService;

    @PostMapping("/application")
    public Application createApplication(@RequestBody ApplicationDTO applicationDTO){
        logger.debug(Constants.CREATING_LEAVING_APPLICATION);

        Application application = new Application();
        application.setFrom(applicationDTO.getFrom());
        application.setTo(applicationDTO.getTo());
        application.setApplicationType(applicationDTO.getApplicationType());
        application.setRequestedBy(userService.getById(applicationDTO.getUserId()));
        application.setStatus(ApplicationStatus.PENDING);

        if (application.getRequestedBy().getCreatedAt().compareTo(new Date())>Constants.PROBATION_PERIOD) {
            emailService.sendApplicationMail(application,Constants.APPLICATION_CREATED);
            return applicationService.save(application);
        }
        return null;
    }

    @PutMapping(value = "/application/{id}")
    public ResponseEntity<?> updateVacation(@PathVariable("id") Long id, @RequestBody PostVacationDTO vacationDTO) throws ParseException {

        Application application = applicationService.getById(id);
        application.setFrom(vacationDTO.getStartDate());
        application.setTo(vacationDTO.getEndDate());
        application.setApplicationType(vacationDTO.getApplicationType());
        application.setStatus(vacationDTO.getStatus());

        logger.debug(Constants.UPDATING_LEAVING_APPLICATION);
        applicationService.save(application);

        emailService.sendApplicationMail(application,Constants.APPLICATION_UPDATED);

        return ResponseEntity.ok(application);
    }

    @DeleteMapping("/application/{id}")
    public void deleteApplication(@PathVariable("id") Long id) {
        applicationService.deleteById(id);
    }
}
