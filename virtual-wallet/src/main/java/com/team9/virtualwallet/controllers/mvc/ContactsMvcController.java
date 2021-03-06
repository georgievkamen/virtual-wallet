package com.team9.virtualwallet.controllers.mvc;

import com.team9.virtualwallet.controllers.AuthenticationHelper;
import com.team9.virtualwallet.exceptions.AuthenticationFailureException;
import com.team9.virtualwallet.exceptions.EntityNotFoundException;
import com.team9.virtualwallet.exceptions.UnauthorizedOperationException;
import com.team9.virtualwallet.models.User;
import com.team9.virtualwallet.services.contracts.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/panel/contacts")
public class ContactsMvcController extends BaseAuthenticationController {

    private final AuthenticationHelper authenticationHelper;
    private final UserService service;

    public ContactsMvcController(AuthenticationHelper authenticationHelper, UserService service) {
        super(authenticationHelper);
        this.authenticationHelper = authenticationHelper;
        this.service = service;
    }

    @GetMapping
    public String showContactsPage(HttpSession session, Model model) {
        try {
            User user = authenticationHelper.tryGetUser(session);
            List<User> contacts = service.getContacts(user);
            model.addAttribute("contacts", contacts);
            model.addAttribute("contactsExist", !contacts.isEmpty());
            return "contacts";
        } catch (AuthenticationFailureException e) {
            return LOGIN_REDIRECT_CONSTANT;
        }
    }

    @GetMapping("/add")
    public String showAddContactPage(HttpSession session, Model model,
                                     @RequestParam(name = "fieldName", required = false) String fieldName,
                                     @RequestParam(name = "search-field", required = false) String searchTerm) {
        try {
            User user = authenticationHelper.tryGetUser(session);
            if (fieldName != null && searchTerm != null) {
                model.addAttribute("user", service.getByField(user, fieldName, searchTerm));
            }
            return "contact-add";
        } catch (AuthenticationFailureException e) {
            return LOGIN_REDIRECT_CONSTANT;
        } catch (EntityNotFoundException e) {
            model.addAttribute("notFound", "User not found!");
            return "contact-add";
        }
    }

    @GetMapping("/{id}/add")
    public String addContact(@PathVariable int id, HttpSession session) {
        try {
            User user = authenticationHelper.tryGetUser(session);
            service.addContact(user, id);
            return "redirect:/panel/contacts";
        } catch (AuthenticationFailureException e) {
            return LOGIN_REDIRECT_CONSTANT;
        }
    }


    @GetMapping("/{id}/delete")
    public String deleteContact(HttpSession session, @PathVariable int id) {
        try {
            User user = authenticationHelper.tryGetUser(session);
            service.removeContact(user, id);
            return "redirect:/panel/contacts";
        } catch (AuthenticationFailureException e) {
            return LOGIN_REDIRECT_CONSTANT;
        } catch (UnauthorizedOperationException e) {
            return "redirect:/panel/wallets";
        }
    }

}