package com.team9.virtualwallet.controllers.mvc;

import com.team9.virtualwallet.controllers.AuthenticationHelper;
import com.team9.virtualwallet.exceptions.AuthenticationFailureException;
import com.team9.virtualwallet.exceptions.DuplicateEntityException;
import com.team9.virtualwallet.models.User;
import com.team9.virtualwallet.models.dtos.LoginDto;
import com.team9.virtualwallet.models.dtos.RegisterDto;
import com.team9.virtualwallet.services.contracts.UserService;
import com.team9.virtualwallet.services.mappers.UserModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.Optional;
import java.util.function.Predicate;

import static com.team9.virtualwallet.configs.ApplicationConstants.CURRENT_USER_SESSION_KEY;

@Controller
@RequestMapping("/auth")
public class AuthenticationController {

    private final UserModelMapper mapper;
    private final UserService userService;
    private final AuthenticationHelper authenticationHelper;

    @Autowired
    public AuthenticationController(UserModelMapper mapper, UserService userService, AuthenticationHelper authenticationHelper) {
        this.mapper = mapper;
        this.userService = userService;
        this.authenticationHelper = authenticationHelper;
    }

    @GetMapping("/login")
    public String showLoginPage(Model model) {
        model.addAttribute("login", new LoginDto());
        return "login";
    }

    @PostMapping("/login")
    public String handleLogin(@ModelAttribute("login") LoginDto dto,
                              BindingResult bindingResult,
                              HttpSession session) {
        if (bindingResult.hasErrors()) {
            return "login";
        }

        try {
            authenticationHelper.verifyAuthentication(dto.getUsername(), dto.getPassword());
            User user = userService.getByUsername(dto.getUsername());
            if (!user.isDeleted()) {
                session.setAttribute(CURRENT_USER_SESSION_KEY, dto.getUsername());
                return "redirect:/panel";
            } else {
                bindingResult.rejectValue("username", "account_deleted", "You have deleted your account!");
                return "login";
            }
        } catch (AuthenticationFailureException e) {
            bindingResult.rejectValue("username", "auth_error", e.getMessage());
            return "login";
        }
    }

    @GetMapping("/logout")
    public String handleLogout(HttpSession session) {
        session.removeAttribute(CURRENT_USER_SESSION_KEY);
        return "redirect:/";
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model, @RequestParam(name = "invitation-token", required = false) String invitationToken) {
        model.addAttribute("register", new RegisterDto());
        model.addAttribute("invitationToken", invitationToken);
        return "register";
    }

    @PostMapping("/register")
    public String handleRegister(@Valid @ModelAttribute("register") RegisterDto registerDto,
                                 BindingResult bindingResult, HttpSession session, Model model,
                                 @RequestParam(name = "invitation-token", required = false) String invitationToken) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("invitationToken", invitationToken);
            return "register";
        }

        try {
            User user = mapper.fromRegisterDto(registerDto);
            userService.create(user, Optional.ofNullable(invitationToken).filter(Predicate.not(String::isEmpty)));
            session.setAttribute(CURRENT_USER_SESSION_KEY, user.getUsername());
            model.addAttribute("email", user.getEmail());
            return "verify-email";
        } catch (DuplicateEntityException e) {
            String field = e.getMessage().split(" ")[2];
            if (field.equalsIgnoreCase("username")) {
                bindingResult.rejectValue("username", "duplicate_username", e.getMessage());
            }
            if (field.equalsIgnoreCase("email")) {
                bindingResult.rejectValue("email", "duplicate_email", e.getMessage());
            }
            if (field.equalsIgnoreCase("phone")) {
                bindingResult.rejectValue("phoneNumber", "duplicate_phoneNumber", e.getMessage());
            }
            return "register";
        } catch (AuthenticationFailureException e) {
            bindingResult.rejectValue("email", "auth_error", e.getMessage());
            return "login";
        }
    }

    @GetMapping("/verify-email")
    public String handleEmailVerification(@RequestParam("token") String token, @RequestParam(name = "invitation-token", required = false) String invitationToken) {
        try {
            userService.confirmUser(token, Optional.ofNullable(invitationToken));
            return "redirect:/panel";
        } catch (IllegalArgumentException e) {
            return "redirect:/panel";
        }
    }

}
