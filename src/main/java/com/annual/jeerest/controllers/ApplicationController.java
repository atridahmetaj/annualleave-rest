package com.annual.jeerest.controllers;

import com.annual.jeeshared.beans.PostVacationDTO;
import com.annual.jeeshared.constants.Constants;
import com.annual.jeeshared.entity.Application;
import com.annual.jeeshared.entity.User;
import com.annual.jeeshared.service.ApplicationService;
import com.annual.jeeshared.service.EmailService;
import com.annual.jeeshared.utils.UserUtils;
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

    @PostMapping("/application")
    public Application createApplication(@RequestBody Application application){
        logger.debug(Constants.CREATING_LEAVING_APPLICATION);
        if (application.getRequestedBy().getCreatedAt().compareTo(new Date())<Constants.PROBATION_PERIOD) {
            return applicationService.save(application);
        }
        return null;
    }

    @PutMapping(value = "/application/{id}")
    public ResponseEntity<?> updateVacation(@PathVariable("id") Long id, @RequestBody PostVacationDTO vacationDTO) throws ParseException {

        Application application = applicationService.getById(id);
        User loggedInUser = UserUtils.getLoggedInUser();
        application.setFrom(vacationDTO.getStartDate());
        application.setTo(vacationDTO.getEndDate());
        application.setApplicationType(vacationDTO.getApplicationType());
        application.setStatus(vacationDTO.getStatus());

        logger.debug(Constants.UPDATING_LEAVING_APPLICATION);
        applicationService.save(application);

        // send email
        User requester = application.getRequestedBy();

        String[] sendTo = new String[2];
        sendTo[0] = requester.getEmail();
        if (loggedInUser.equals(requester.getAdmin()))
            sendTo[1] = requester.getAdmin().getEmail();
        else
            sendTo[1] = requester.getTeamLeader().getEmail();

        emailService.sendCustomEmail(sendTo, "STATUS UPDATE", application.toString());

        return ResponseEntity.ok(application);
    }

    @DeleteMapping("/application/{id}")
    public void deleteApplication(@PathVariable("id") Long id) {
        applicationService.deleteById(id);
    }
}
