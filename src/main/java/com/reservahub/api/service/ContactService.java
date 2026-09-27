package com.reservahub.api.service;

import com.reservahub.api.dto.ContactRequest;
import com.reservahub.api.model.Contact;
import com.reservahub.api.repository.ContactRepository;
import org.springframework.stereotype.Service;

@Service
public class ContactService {

    private final ContactRepository contactRepository;

    public ContactService(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    public Contact saveContact(ContactRequest request) {
        Contact contact = new Contact(
                request.name(),
                request.email().trim().toLowerCase(),
                request.subject(),
                request.message()
        );

        return contactRepository.save(contact);
    }
}