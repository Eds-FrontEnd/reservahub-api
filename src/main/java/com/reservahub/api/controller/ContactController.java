package com.reservahub.api.controller;

import com.reservahub.api.dto.ContactRequest;
import com.reservahub.api.model.Contact;
import com.reservahub.api.service.ContactService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contato")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping
    public ResponseEntity<Contact> saveContact(
            @RequestBody ContactRequest request
    ) {
        Contact contact = contactService.saveContact(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(contact);
    }
}