package com.vkruk.biometricauthenticationserver.controllers;

import com.vkruk.biometricauthenticationserver.models.Employee;
import com.vkruk.biometricauthenticationserver.models.Template;
import com.vkruk.biometricauthenticationserver.repository.EmployeeRepository;
import com.vkruk.biometricauthenticationserver.services.EmployeeValidator;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.data.rest.core.event.AfterCreateEvent;
import org.springframework.data.rest.core.event.BeforeCreateEvent;
import org.springframework.data.rest.webmvc.BasePathAwareController;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@BasePathAwareController
@RequestMapping("/employees")
public class EmployeeController implements ApplicationEventPublisherAware {

    private final EmployeeRepository repository;
    private final EmployeeValidator validator;
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public EmployeeController(EmployeeRepository repository, EmployeeValidator validator) {
        this.repository = repository;
        this.validator = validator;
    }

    @ApiOperation(value = "${employee-controller.addUpdate}")
    @RequestMapping(method = RequestMethod.POST, value = "/{id}/templates")
    public @ResponseBody List<Employee> addUpdate( @ApiParam(value = "${employee-controller.addUpdate.id}", required = true)
                                                      @PathVariable("id") final int id,
                                                      @RequestBody final List<Template> templates,
                                                        Errors errors) {

        repository.deleteAllByEmployeeId(id);

        templates.forEach(template -> {
            Employee employee = new Employee(id,template.getFinger(),template.getTemplate0(),template.getTemplate1());
            applicationEventPublisher.publishEvent(new BeforeCreateEvent(employee));
            repository.save(employee);
            applicationEventPublisher.publishEvent(new AfterCreateEvent(employee));
        });

        return repository.findByEmployeeId(id);

    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
}